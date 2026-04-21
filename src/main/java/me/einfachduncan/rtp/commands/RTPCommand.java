package me.einfachduncan.rtp.commands;

import me.einfachduncan.rtp.RandomTeleportPlugin;
import me.einfachduncan.rtp.config.ConfigManager;
import me.einfachduncan.rtp.gui.RTPGui;
import me.einfachduncan.rtp.service.TeleportService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class RTPCommand implements CommandExecutor, TabCompleter {

    private final RandomTeleportPlugin plugin;
    private final ConfigManager configManager;
    private final TeleportService teleportService;

    public RTPCommand(RandomTeleportPlugin plugin, ConfigManager configManager, TeleportService teleportService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.teleportService = teleportService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /rtp setcooldown <seconds> – operator-only sub-command
        if (args.length == 2 && args[0].equalsIgnoreCase("setcooldown")) {
            if (!sender.hasPermission("rtp.admin")) {
                sender.sendMessage(configManager.getMessage("no-permission"));
                return true;
            }
            try {
                int seconds = Integer.parseInt(args[1]);
                if (seconds < 0) {
                    sender.sendMessage(configManager.getCooldownInvalidMessage());
                    return true;
                }
                configManager.setCooldown(seconds);
                sender.sendMessage(configManager.getCooldownSetMessage(seconds));
            } catch (NumberFormatException e) {
                sender.sendMessage(configManager.getCooldownNaNMessage(args[1]));
            }
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("\u00A7cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("rtp.use")) {
            player.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        // Check if RTP is allowed in the player's current world
        if (!configManager.isWorldEnabled(player.getWorld())) {
            player.sendMessage(configManager.getMessage("invalid-world"));
            return true;
        }

        // Check cooldown before opening the GUI
        if (!player.hasPermission("rtp.admin") && teleportService.isOnCooldown(player)) {
            long secondsLeft = teleportService.getRemainingCooldown(player);
            player.sendMessage(configManager.getCooldownMessage(secondsLeft));
            return true;
        }

        RTPGui.open(player, configManager, teleportService);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("rtp.admin")) {
            return Collections.singletonList("setcooldown");
        }
        return Collections.emptyList();
    }
}
