package me.einfachduncan.rtp.listeners;

import me.einfachduncan.rtp.service.TeleportService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Tracks when a player takes damage so that {@link TeleportService} can
 * prevent RTP usage while the player is considered to be "in combat".
 */
public class PlayerDamageListener implements Listener {

    private final TeleportService teleportService;

    public PlayerDamageListener(TeleportService teleportService) {
        this.teleportService = teleportService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        teleportService.recordDamage(player);
    }
}
