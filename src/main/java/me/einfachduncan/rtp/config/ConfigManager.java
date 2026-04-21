package me.einfachduncan.rtp.config;

import me.einfachduncan.rtp.RandomTeleportPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final RandomTeleportPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(RandomTeleportPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public int getGlobalRadius() {
        return config.getInt("rtp.radius", 5000);
    }

    public int getCooldown() {
        return config.getInt("rtp.cooldown", 30);
    }

    public int getWorldRadius(String worldName) {
        String path = "rtp.worlds." + worldName;
        if (config.contains(path)) {
            return config.getInt(path);
        }
        return getGlobalRadius();
    }

    public boolean isWorldConfigured(String worldName) {
        return config.contains("rtp.worlds." + worldName);
    }

    public String getMessage(String key) {
        String path = "rtp.messages." + key;
        String message = config.getString(path, "&cMessage not found: " + key);
        return colorize(message);
    }

    public String getCooldownMessage(long secondsLeft) {
        String message = config.getString("rtp.messages.cooldown",
                "&cPlease wait &e{time} &cseconds before teleporting again.");
        return colorize(message.replace("{time}", String.valueOf(secondsLeft)));
    }

    private String colorize(String message) {
        return message.replace("&", "\u00A7");
    }
}
