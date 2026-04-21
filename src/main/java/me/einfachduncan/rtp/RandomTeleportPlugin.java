package me.einfachduncan.rtp;

import me.einfachduncan.rtp.commands.RTPCommand;
import me.einfachduncan.rtp.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class RandomTeleportPlugin extends JavaPlugin {

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);

        RTPCommand rtpCommand = new RTPCommand(this, configManager);
        getCommand("rtp").setExecutor(rtpCommand);
        getCommand("rtp").setTabCompleter(rtpCommand);

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
