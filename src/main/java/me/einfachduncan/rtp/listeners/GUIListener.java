package me.einfachduncan.rtp.listeners;

import me.einfachduncan.rtp.config.ConfigManager;
import me.einfachduncan.rtp.gui.RTPGui;
import me.einfachduncan.rtp.service.TeleportService;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener {

    private final ConfigManager configManager;
    private final TeleportService teleportService;

    public GUIListener(ConfigManager configManager, TeleportService teleportService) {
        this.configManager = configManager;
        this.teleportService = teleportService;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Check if this is the RTP GUI by comparing the plain-text title
        String title = PlainTextComponentSerializer.plainText()
                .serialize(event.getView().title());
        if (!configManager.getGuiTitle().equals(title)) {
            return;
        }

        // Always cancel clicks inside the RTP GUI to prevent item theft
        event.setCancelled(true);

        int slot = event.getSlot();
        String worldName;

        if (slot == RTPGui.OVERWORLD_SLOT) {
            worldName = configManager.getOverworldName();
        } else if (slot == RTPGui.NETHER_SLOT) {
            worldName = configManager.getNetherName();
        } else if (slot == RTPGui.END_SLOT) {
            worldName = configManager.getEndName();
        } else {
            // Clicked on a filler or empty slot – do nothing
            return;
        }

        World targetWorld = Bukkit.getWorld(worldName);
        if (targetWorld == null) {
            player.sendMessage(configManager.getMessage("invalid-world"));
            return;
        }

        player.closeInventory();

        // Guard: prevent double-triggering while still finding a location
        if (teleportService.isTeleporting(player)) {
            return;
        }

        // Check cooldown
        if (!player.hasPermission("rtp.admin") && teleportService.isOnCooldown(player)) {
            long secondsLeft = teleportService.getRemainingCooldown(player);
            player.sendMessage(configManager.getCooldownMessage(secondsLeft));
            return;
        }

        teleportService.performTeleport(player, targetWorld);
    }
}
