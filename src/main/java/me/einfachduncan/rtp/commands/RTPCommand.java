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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RTPCommand implements CommandExecutor, TabCompleter {

    private final RandomTeleportPlugin plugin;
    private final ConfigManager configManager;
    private final TeleportService teleportService;
    private final AdminCommand adminCommand;

    public RTPCommand(RandomTeleportPlugin plugin, ConfigManager configManager, TeleportService teleportService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.teleportService = teleportService;
        this.adminCommand = new AdminCommand(configManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /rtp admin ... – delegate to AdminCommand
        if (args.length >= 1 && args[0].equalsIgnoreCase("admin")) {
            String[] adminArgs = Arrays.copyOfRange(args, 1, args.length);
            return adminCommand.handle(sender, adminArgs);
        }

        // /rtp setcooldown <seconds> – operator-only sub-command (kept for backwards compatibility)
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

        // Check if RTP is blocked in the player's current world
        if (configManager.isWorldDisabled(player.getWorld())) {
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
            List<String> options = new ArrayList<>(Arrays.asList("admin", "setcooldown"));
            List<String> result = new ArrayList<>();
            for (String opt : options) {
                if (opt.startsWith(args[0].toLowerCase())) {
                    result.add(opt);
                }
            }
            return result;
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("admin") && sender.hasPermission("rtp.admin")) {
            String[] adminArgs = Arrays.copyOfRange(args, 1, args.length);
            return adminCommand.tabComplete(sender, adminArgs);
        }
        return Collections.emptyList();
    }
}
