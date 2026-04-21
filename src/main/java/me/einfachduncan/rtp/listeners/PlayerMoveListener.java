package me.einfachduncan.rtp.listeners;

import me.einfachduncan.rtp.config.ConfigManager;
import me.einfachduncan.rtp.service.TeleportService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Cancels an active RTP search if the player moves their block position
 * while the movement lock is active.
 */
public class PlayerMoveListener implements Listener {

    private final ConfigManager configManager;
    private final TeleportService teleportService;

    public PlayerMoveListener(ConfigManager configManager, TeleportService teleportService) {
        this.configManager = configManager;
        this.teleportService = teleportService;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!teleportService.isMovementLocked(player)) return;

        Location from = teleportService.getLockedPosition(player);
        Location to = event.getTo();
        if (to == null) return;

        // Only react to actual block-position changes (ignores head rotation)
        if (from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()) {
            teleportService.cancelTeleport(player);
            player.sendMessage(configManager.getMovementCancelledMessage());
        }
    }
}
