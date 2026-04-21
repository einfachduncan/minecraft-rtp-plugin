package me.einfachduncan.rtp;

import me.einfachduncan.rtp.commands.RTPCommand;
import me.einfachduncan.rtp.config.ConfigManager;
import me.einfachduncan.rtp.listeners.GUIListener;
import me.einfachduncan.rtp.listeners.PlayerDamageListener;
import me.einfachduncan.rtp.listeners.PlayerMoveListener;
import me.einfachduncan.rtp.service.TeleportService;
import org.bukkit.plugin.java.JavaPlugin;

public class RandomTeleportPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private TeleportService teleportService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        teleportService = new TeleportService(this, configManager);

        RTPCommand rtpCommand = new RTPCommand(this, configManager, teleportService);
        getCommand("rtp").setExecutor(rtpCommand);
        getCommand("rtp").setTabCompleter(rtpCommand);

        getServer().getPluginManager().registerEvents(
                new GUIListener(configManager, teleportService), this);
        getServer().getPluginManager().registerEvents(
                new PlayerMoveListener(configManager, teleportService), this);
        getServer().getPluginManager().registerEvents(
                new PlayerDamageListener(teleportService), this);

        getLogger().info("RandomTeleportPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RandomTeleportPlugin has been disabled!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
