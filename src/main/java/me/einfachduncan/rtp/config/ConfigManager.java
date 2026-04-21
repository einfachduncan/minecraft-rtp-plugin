package me.einfachduncan.rtp.config;

import me.einfachduncan.rtp.RandomTeleportPlugin;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

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

    public int getCombatCooldown() {
        return config.getInt("rtp.combat-cooldown", 15);
    }

    public int getSearchTime() {
        return config.getInt("rtp.search-time", 5);
    }

    public int getMaxHeight() {
        return config.getInt("rtp.max-height", 320);
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

    /**
     * Returns true if the given world is blocked for RTP.
     * Worlds listed under {@code disabled-worlds} are off-limits for both
     * the source (player's current world) and the destination.
     * If the list is empty, no world is blocked.
     */
    public boolean isWorldDisabled(World world) {
        List<String> disabledWorlds = config.getStringList("rtp.disabled-worlds");
        return disabledWorlds.contains(world.getName());
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

    public String getGuiInfoName() {
        return config.getString("rtp.messages.gui-info-name", "Status");
    }

    public String getGuiInfoReadyLore() {
        return config.getString("rtp.messages.gui-info-ready-lore", "Ready to teleport!");
    }

    public String getGuiInfoCooldownLore(long seconds) {
        return config.getString("rtp.messages.gui-info-cooldown-lore", "Cooldown: {time}s remaining")
                .replace("{time}", String.valueOf(seconds));
    }

    public String getGuiInfoCombatLore(long seconds) {
        return config.getString("rtp.messages.gui-info-combat-lore", "In combat! Wait {time}s")
                .replace("{time}", String.valueOf(seconds));
    }

    public String getGuiCancelName() {
        return config.getString("rtp.messages.gui-cancel-name", "Close");
    }

    public String getGuiCancelLore() {
        return config.getString("rtp.messages.gui-cancel-lore", "Click to close this menu");
    }

    public String getCombatMessage() {
        return getMessage("in-combat");
    }

    public String getMovementCancelledMessage() {
        return getMessage("rtp-cancelled-moved");
    }

    public String getSearchingMessage() {
        return getMessage("searching");
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
