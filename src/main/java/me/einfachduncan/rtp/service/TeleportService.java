package me.einfachduncan.rtp.service;

import me.einfachduncan.rtp.RandomTeleportPlugin;
import me.einfachduncan.rtp.config.ConfigManager;
import me.einfachduncan.rtp.utils.TeleportUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TeleportService {

    private final RandomTeleportPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Set<UUID> teleporting = new HashSet<>();
    /** Tracks when each player last took damage (epoch ms). */
    private final Map<UUID, Long> combatTimestamps = new HashMap<>();
    /** Stores the position where movement was locked for each player in an active RTP search. */
    private final Map<UUID, Location> movementLockPositions = new HashMap<>();

    public TeleportService(RandomTeleportPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    // ── Cooldown ─────────────────────────────────────────────────────────────

    public boolean isOnCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return false;
        }
        long cooldownMillis = configManager.getCooldown() * 1000L;
        return System.currentTimeMillis() - cooldowns.get(uuid) < cooldownMillis;
    }

    public long getRemainingCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return 0;
        }
        long cooldownMillis = configManager.getCooldown() * 1000L;
        long elapsed = System.currentTimeMillis() - cooldowns.get(uuid);
        long remaining = cooldownMillis - elapsed;
        // +999 implements ceiling division: rounds up to the nearest whole second
        return Math.max(0, (remaining + 999) / 1000);
    }

    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    // ── Combat ───────────────────────────────────────────────────────────────

    /**
     * Records that the player took damage right now, marking them as "in combat".
     */
    public void recordDamage(Player player) {
        combatTimestamps.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean isInCombat(Player player) {
        UUID uuid = player.getUniqueId();
        if (!combatTimestamps.containsKey(uuid)) return false;
        long combatCooldownMs = configManager.getCombatCooldown() * 1000L;
        return System.currentTimeMillis() - combatTimestamps.get(uuid) < combatCooldownMs;
    }

    public long getRemainingCombatCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        if (!combatTimestamps.containsKey(uuid)) return 0;
        long combatCooldownMs = configManager.getCombatCooldown() * 1000L;
        long elapsed = System.currentTimeMillis() - combatTimestamps.get(uuid);
        long remaining = combatCooldownMs - elapsed;
        // +999 implements ceiling division: rounds up to the nearest whole second
        return Math.max(0, (remaining + 999) / 1000);
    }

    // ── Movement lock ────────────────────────────────────────────────────────

    public boolean isMovementLocked(Player player) {
        return movementLockPositions.containsKey(player.getUniqueId());
    }

    public Location getLockedPosition(Player player) {
        return movementLockPositions.get(player.getUniqueId());
    }

    /**
     * Cancels an active RTP search for the player and releases all locks.
     * Safe to call even if no search is active.
     */
    public void cancelTeleport(Player player) {
        UUID uuid = player.getUniqueId();
        teleporting.remove(uuid);
        movementLockPositions.remove(uuid);
    }

    // ── Teleport ─────────────────────────────────────────────────────────────

    /**
     * Returns true if the player is already waiting for a teleport.
     */
    public boolean isTeleporting(Player player) {
        return teleporting.contains(player.getUniqueId());
    }

    /**
     * Asynchronously finds a safe location in the target world and teleports the player.
     * <p>
     * Before starting the search the player's movement is locked; if they move the
     * search is cancelled.  An action-bar countdown is shown throughout.
     * If the player is in combat the request is silently rejected with a message.
     */
    public void performTeleport(Player player, World targetWorld) {
        UUID uuid = player.getUniqueId();

        // World check – defence-in-depth for callers that bypass the command/GUI layer
        if (configManager.isWorldDisabled(player.getWorld())) {
            player.sendMessage(configManager.getMessage("invalid-world"));
            return;
        }

        // Target world check – block teleportation into disabled worlds
        if (configManager.isWorldDisabled(targetWorld)) {
            player.sendMessage(configManager.getMessage("invalid-world"));
            return;
        }

        // Combat check
        if (isInCombat(player)) {
            player.sendMessage(configManager.getCombatMessage());
            return;
        }

        teleporting.add(uuid);
        movementLockPositions.put(uuid, player.getLocation().clone());

        player.sendMessage(configManager.getMessage("teleporting"));

        int radius = configManager.getWorldRadius(targetWorld.getName());
        int maxHeight = configManager.getMaxHeight();
        int searchTime = configManager.getSearchTime();

        // Record start time so the async search can compute how much of the countdown remains
        final long startTimeMs = System.currentTimeMillis();

        // Action-bar countdown shown every second while searching
        new BukkitRunnable() {
            int ticks = searchTime;

            @Override
            public void run() {
                if (!teleporting.contains(uuid) || ticks <= 0) {
                    cancel();
                    return;
                }
                player.sendActionBar(LegacyComponentSerializer.legacySection()
                        .deserialize(configManager.getSearchingMessage()));
                ticks--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        // Async location search; teleport is delayed so the total wait equals searchTime seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                Location safeLocation = TeleportUtil.findSafeLocation(targetWorld, radius, maxHeight);

                // Calculate remaining ticks so the teleport always fires at the countdown end
                long elapsedMs = System.currentTimeMillis() - startTimeMs;
                long remainingMs = Math.max(0L, searchTime * 1000L - elapsedMs);
                long remainingTicks = (remainingMs + 25L) / 50L; // round to nearest tick (1 tick = 50 ms)

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Movement listener may have already cancelled the teleport
                        if (!teleporting.contains(uuid)) {
                            return;
                        }

                        teleporting.remove(uuid);
                        movementLockPositions.remove(uuid);

                        if (safeLocation == null) {
                            player.sendMessage(configManager.getMessage("no-safe-location"));
                            return;
                        }

                        player.teleport(safeLocation);
                        player.sendMessage(configManager.getMessage("teleported"));

                        if (!player.hasPermission("rtp.admin")) {
                            setCooldown(player);
                        }
                    }
                }.runTaskLater(plugin, remainingTicks);
            }
        }.runTaskAsynchronously(plugin);
    }
}
