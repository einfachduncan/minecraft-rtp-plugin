package me.einfachduncan.rtp.commands;

import me.einfachduncan.rtp.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AdminCommand {

    private final ConfigManager configManager;

    public AdminCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Handles all /rtp admin ... subcommands.
     *
     * @param sender the command sender
     * @param args   the arguments after "admin" (e.g. ["world", "disable", "world_nether"])
     * @return true always (to suppress Bukkit's default usage message)
     */
    public boolean handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rtp.admin")) {
            sender.sendMessage(colorize("&cYou don't have permission to use this command."));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "world" -> handleWorld(sender, args);
            case "cooldown" -> handleCooldown(sender, args);
            case "combat-cooldown" -> handleCombatCooldown(sender, args);
            case "search-time" -> handleSearchTime(sender, args);
            case "max-height" -> handleMaxHeight(sender, args);
            case "list" -> handleList(sender);
            case "reload" -> handleReload(sender);
            default -> sendUsage(sender);
        }
        return true;
    }

    // --- World subcommands ---

    private void handleWorld(CommandSender sender, String[] args) {
        // args: ["world", <action>, <worldName>, [radius]]
        if (args.length < 3) {
            sender.sendMessage(colorize("&cUsage: /rtp admin world <disable|enable|setradius> <world> [radius]"));
            return;
        }

        String action = args[1].toLowerCase();
        String worldName = args[2];

        switch (action) {
            case "disable" -> {
                if (Bukkit.getWorld(worldName) == null) {
                    sender.sendMessage(colorize("&cWorld '&e" + worldName + "&c' does not exist."));
                    return;
                }
                configManager.disableWorld(worldName);
                sender.sendMessage(colorize("&aRTP has been &cdisabled&a in world '&e" + worldName + "&a'."));
            }
            case "enable" -> {
                if (Bukkit.getWorld(worldName) == null) {
                    sender.sendMessage(colorize("&cWorld '&e" + worldName + "&c' does not exist."));
                    return;
                }
                configManager.enableWorld(worldName);
                sender.sendMessage(colorize("&aRTP has been &2enabled&a in world '&e" + worldName + "&a'."));
            }
            case "setradius" -> {
                if (args.length < 4) {
                    sender.sendMessage(colorize("&cUsage: /rtp admin world setradius <world> <radius>"));
                    return;
                }
                if (Bukkit.getWorld(worldName) == null) {
                    sender.sendMessage(colorize("&cWorld '&e" + worldName + "&c' does not exist."));
                    return;
                }
                try {
                    int radius = Integer.parseInt(args[3]);
                    if (radius <= 0) {
                        sender.sendMessage(colorize("&cRadius must be greater than 0."));
                        return;
                    }
                    configManager.setWorldRadius(worldName, radius);
                    sender.sendMessage(colorize("&aRadius for world '&e" + worldName + "&a' set to &e" + radius + "&a blocks."));
                } catch (NumberFormatException e) {
                    sender.sendMessage(colorize("&cInvalid number: &e" + args[3]));
                }
            }
            default -> sender.sendMessage(colorize("&cUnknown world action. Use: disable, enable, setradius"));
        }
    }

    // --- Global setting subcommands ---

    private void handleCooldown(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(colorize("&cUsage: /rtp admin cooldown <seconds>"));
            return;
        }
        try {
            int seconds = Integer.parseInt(args[1]);
            if (seconds < 0) {
                sender.sendMessage(colorize("&cCooldown must be 0 or greater."));
                return;
            }
            configManager.setCooldown(seconds);
            sender.sendMessage(colorize("&aCooldown set to &e" + seconds + "&a seconds."));
        } catch (NumberFormatException e) {
            sender.sendMessage(colorize("&cInvalid number: &e" + args[1]));
        }
    }

    private void handleCombatCooldown(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(colorize("&cUsage: /rtp admin combat-cooldown <seconds>"));
            return;
        }
        try {
            int seconds = Integer.parseInt(args[1]);
            if (seconds < 0) {
                sender.sendMessage(colorize("&cCombat cooldown must be 0 or greater."));
                return;
            }
            configManager.setCombatCooldown(seconds);
            sender.sendMessage(colorize("&aCombat cooldown set to &e" + seconds + "&a seconds."));
        } catch (NumberFormatException e) {
            sender.sendMessage(colorize("&cInvalid number: &e" + args[1]));
        }
    }

    private void handleSearchTime(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(colorize("&cUsage: /rtp admin search-time <seconds>"));
            return;
        }
        try {
            int seconds = Integer.parseInt(args[1]);
            if (seconds <= 0) {
                sender.sendMessage(colorize("&cSearch time must be greater than 0."));
                return;
            }
            configManager.setSearchTime(seconds);
            sender.sendMessage(colorize("&aSearch time set to &e" + seconds + "&a seconds."));
        } catch (NumberFormatException e) {
            sender.sendMessage(colorize("&cInvalid number: &e" + args[1]));
        }
    }

    private void handleMaxHeight(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(colorize("&cUsage: /rtp admin max-height <blocks>"));
            return;
        }
        try {
            int blocks = Integer.parseInt(args[1]);
            if (blocks <= 0) {
                sender.sendMessage(colorize("&cMax height must be greater than 0."));
                return;
            }
            configManager.setMaxHeight(blocks);
            sender.sendMessage(colorize("&aMax height set to &e" + blocks + "&a blocks."));
        } catch (NumberFormatException e) {
            sender.sendMessage(colorize("&cInvalid number: &e" + args[1]));
        }
    }

    // --- List ---

    private void handleList(CommandSender sender) {
        List<String> disabledWorlds = configManager.getDisabledWorlds();
        String disabledStr = disabledWorlds.isEmpty() ? "none" : String.join(", ", disabledWorlds);

        Map<String, Integer> worldRadii = configManager.getAllWorldRadii();

        sender.sendMessage(colorize("&6=== RTP Settings ==="));
        sender.sendMessage(colorize("&eDisabled Worlds: &f" + disabledStr));
        sender.sendMessage(colorize("&eCooldown: &f" + configManager.getCooldown() + "s"));
        sender.sendMessage(colorize("&eCombat Cooldown: &f" + configManager.getCombatCooldown() + "s"));
        sender.sendMessage(colorize("&eSearch Time: &f" + configManager.getSearchTime() + "s"));
        sender.sendMessage(colorize("&eMax Height: &f" + configManager.getMaxHeight()));
        sender.sendMessage(colorize("&eWorld Radius:"));
        if (worldRadii.isEmpty()) {
            sender.sendMessage(colorize("&f  (none configured — using global: " + configManager.getGlobalRadius() + ")"));
        } else {
            for (Map.Entry<String, Integer> entry : worldRadii.entrySet()) {
                sender.sendMessage(colorize("&f  " + entry.getKey() + ": " + entry.getValue()));
            }
        }
    }

    // --- Reload ---

    private void handleReload(CommandSender sender) {
        configManager.reload();
        sender.sendMessage(colorize("&aRTP configuration reloaded from disk."));
    }

    // --- Usage ---

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(colorize("&6=== RTP Admin Commands ==="));
        sender.sendMessage(colorize("&e/rtp admin world disable <world>"));
        sender.sendMessage(colorize("&e/rtp admin world enable <world>"));
        sender.sendMessage(colorize("&e/rtp admin world setradius <world> <radius>"));
        sender.sendMessage(colorize("&e/rtp admin cooldown <seconds>"));
        sender.sendMessage(colorize("&e/rtp admin combat-cooldown <seconds>"));
        sender.sendMessage(colorize("&e/rtp admin search-time <seconds>"));
        sender.sendMessage(colorize("&e/rtp admin max-height <blocks>"));
        sender.sendMessage(colorize("&e/rtp admin list"));
        sender.sendMessage(colorize("&e/rtp admin reload"));
    }

    // --- Tab completion suggestions ---

    /**
     * Returns tab-completion suggestions for the portion of args that comes after "admin".
     */
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rtp.admin")) {
            return List.of();
        }

        List<String> subCommands = Arrays.asList("world", "cooldown", "combat-cooldown", "search-time", "max-height", "list", "reload");

        if (args.length == 1) {
            return filter(subCommands, args[0]);
        }

        if (args[0].equalsIgnoreCase("world")) {
            if (args.length == 2) {
                return filter(Arrays.asList("disable", "enable", "setradius"), args[1]);
            }
            if (args.length == 3) {
                List<String> worlds = new ArrayList<>();
                Bukkit.getWorlds().forEach(w -> worlds.add(w.getName()));
                return filter(worlds, args[2]);
            }
        }

        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(prefix.toLowerCase())) {
                result.add(option);
            }
        }
        return result;
    }

    private String colorize(String message) {
        return message.replace("&", "\u00A7");
    }
}
