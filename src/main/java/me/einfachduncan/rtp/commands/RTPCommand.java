package me.einfachduncan.rtp.commands;

import me.einfachduncan.rtp.RandomTeleportPlugin;
import me.einfachduncan.rtp.config.ConfigManager;
import me.einfachduncan.rtp.utils.TeleportUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RTPCommand implements CommandExecutor, TabCompleter {

    private final RandomTeleportPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public RTPCommand(RandomTeleportPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("\u00A7cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("rtp.use")) {
            player.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        // Admin bypass for cooldown
        if (!player.hasPermission("rtp.admin") && isOnCooldown(player)) {
            long secondsLeft = getRemainingCooldown(player);
            player.sendMessage(configManager.getCooldownMessage(secondsLeft));
            return true;
        }

        World world = player.getWorld();
        String worldName = world.getName();

        boolean isAllowed = configManager.isWorldConfigured(worldName)
                || world.getEnvironment() == World.Environment.NORMAL;
        if (!isAllowed) {
            player.sendMessage(configManager.getMessage("invalid-world"));
            return true;
        }

        int radius = configManager.getWorldRadius(worldName);

        player.sendMessage(configManager.getMessage("teleporting"));

        new BukkitRunnable() {
            @Override
            public void run() {
                Location safeLocation = TeleportUtil.findSafeLocation(world, radius);

                // Schedule the teleport back on the main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (safeLocation == null) {
                            player.sendMessage("\u00A7cCould not find a safe location. Please try again.");
                            return;
                        }

                        player.teleport(safeLocation);
                        player.sendMessage(configManager.getMessage("teleported"));

                        if (!player.hasPermission("rtp.admin")) {
                            setCooldown(player);
                        }
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    private boolean isOnCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return false;
        }
        long cooldownMillis = configManager.getCooldown() * 1000L;
        return System.currentTimeMillis() - cooldowns.get(uuid) < cooldownMillis;
    }

    private long getRemainingCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return 0;
        }
        long cooldownMillis = configManager.getCooldown() * 1000L;
        long elapsed = System.currentTimeMillis() - cooldowns.get(uuid);
        long remaining = cooldownMillis - elapsed;
        return Math.max(0, (remaining + 999) / 1000);
    }

    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
