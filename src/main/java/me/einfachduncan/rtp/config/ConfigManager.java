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

    /**
     * Persists a new cooldown value to config.yml so it survives server restarts.
     */
    public void setCooldown(int seconds) {
        config.set("rtp.cooldown", seconds);
        plugin.saveConfig();
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

    // --- GUI world names ---

    public String getOverworldName() {
        return config.getString("rtp.gui.overworld-world", "world");
    }

    public String getNetherName() {
        return config.getString("rtp.gui.nether-world", "world_nether");
    }

    public String getEndName() {
        return config.getString("rtp.gui.end-world", "world_the_end");
    }

    // --- GUI text ---

    public String getGuiTitle() {
        return config.getString("rtp.messages.gui-title", "RTP - Wähle eine Welt");
    }

    public String getGuiOverworldName() {
        return config.getString("rtp.messages.gui-overworld-name", "Overworld");
    }

    public String getGuiOverworldLore() {
        return config.getString("rtp.messages.gui-overworld-lore", "Teleportiere dich in die Oberwelt");
    }

    public String getGuiNetherName() {
        return config.getString("rtp.messages.gui-nether-name", "Nether");
    }

    public String getGuiNetherLore() {
        return config.getString("rtp.messages.gui-nether-lore", "Teleportiere dich in den Nether");
    }

    public String getGuiEndName() {
        return config.getString("rtp.messages.gui-end-name", "The End");
    }

    public String getGuiEndLore() {
        return config.getString("rtp.messages.gui-end-lore", "Teleportiere dich ins Ende");
    }

    // --- Messages ---

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

    public String getCooldownSetMessage(int seconds) {
        String message = config.getString("rtp.messages.cooldown-set",
                "&aCooldown set to &e{seconds} &aseconds.");
        return colorize(message.replace("{seconds}", String.valueOf(seconds)));
    }

    public String getCooldownInvalidMessage() {
        return colorize(config.getString("rtp.messages.cooldown-invalid",
                "&cCooldown must be 0 or greater."));
    }

    public String getCooldownNaNMessage(String input) {
        String message = config.getString("rtp.messages.cooldown-nan",
                "&cInvalid number: {input}");
        return colorize(message.replace("{input}", input));
    }

    private String colorize(String message) {
        return message.replace("&", "\u00A7");
    }
}
