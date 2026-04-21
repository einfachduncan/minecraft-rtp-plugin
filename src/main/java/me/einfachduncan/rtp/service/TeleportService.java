package me.einfachduncan.rtp.service;

import me.einfachduncan.rtp.RandomTeleportPlugin;
import me.einfachduncan.rtp.config.ConfigManager;
import me.einfachduncan.rtp.utils.TeleportUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TeleportService {

    private final RandomTeleportPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Set<UUID> teleporting = new HashSet<>();

    public TeleportService(RandomTeleportPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public boolean isOnCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return false;
        }
        long cooldownMillis = configManager.getCooldown() * 1000L;
        return System.currentTimeMillis() - cooldowns.get(uuid) < cooldownMillis;
    }

    public long getRemainingCooldown(Player player) {
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

    /**
     * Returns true if the player is already waiting for a teleport.
     */
    public boolean isTeleporting(Player player) {
        return teleporting.contains(player.getUniqueId());
    }

    /**
     * Asynchronously finds a safe location in the target world and teleports the player.
     */
    public void performTeleport(Player player, World targetWorld) {
        UUID uuid = player.getUniqueId();
        teleporting.add(uuid);

        player.sendMessage(configManager.getMessage("teleporting"));

        int radius = configManager.getWorldRadius(targetWorld.getName());

        new BukkitRunnable() {
            @Override
            public void run() {
                Location safeLocation = TeleportUtil.findSafeLocation(targetWorld, radius);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        teleporting.remove(uuid);

                        if (safeLocation == null) {
                            player.sendMessage(configManager.getMessage("no-safe-location"));
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
    }
}
