package me.einfachduncan.rtp.utils;

import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;

public class TeleportUtil {

    private static final Random RANDOM = new Random();
    private static final int MAX_SAFE_LOCATION_ATTEMPTS = 50;
    /** Maximum Y searched when scanning for a safe spot in the Nether (avoids bedrock ceiling). */
    private static final int NETHER_SCAN_START_Y = 100;

    private TeleportUtil() {}

    /**
     * Finds a safe random location within the given radius in the specified world.
     * <ul>
     *   <li>Overworld / End: uses the highest surface block – never spawns underground or in water.</li>
     *   <li>Nether: scans downward from y={@value #NETHER_SCAN_START_Y} to avoid the bedrock ceiling.</li>
     * </ul>
     *
     * @param world  the world to search in
     * @param radius the maximum radius from world center (0, 0)
     * @return a safe Location, or null if none found after max attempts
     */
    public static Location findSafeLocation(World world, int radius) {
        boolean isNether = world.getEnvironment() == World.Environment.NETHER;

        for (int attempt = 0; attempt < MAX_SAFE_LOCATION_ATTEMPTS; attempt++) {
            int x = RANDOM.nextInt(radius * 2) - radius;
            int z = RANDOM.nextInt(radius * 2) - radius;

            Location candidate = isNether
                    ? getSafeYNether(world, x, z)
                    : getSafeYSurface(world, x, z);

            if (candidate != null) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * Returns the highest surface location that is solid and not liquid.
     * Uses {@link HeightMap#MOTION_BLOCKING_NO_LEAVES} so the player lands on
     * solid ground rather than on top of a tree canopy.
     */
    private static Location getSafeYSurface(World world, int x, int z) {
        Block surface = world.getHighestBlockAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);

        // Reject oceans, rivers, and lava lakes
        if (surface.isLiquid()) {
            return null;
        }

        if (!isSafeToStandOn(surface)) {
            return null;
        }

        int y = surface.getY() + 1; // spawn on top of the surface block
        Block feet = world.getBlockAt(x, y, z);
        Block head = world.getBlockAt(x, y + 1, z);

        if (isPassable(feet) && isPassable(head)) {
            return new Location(world, x + 0.5, y, z + 0.5);
        }

        return null;
    }

    /**
     * Scans downward from {@value #NETHER_SCAN_START_Y} to find the first
     * safe standing spot, avoiding the solid bedrock ceiling above y=120.
     */
    private static Location getSafeYNether(World world, int x, int z) {
        int minY = world.getMinHeight() + 1;

        for (int y = NETHER_SCAN_START_Y; y > minY; y--) {
            Block ground = world.getBlockAt(x, y, z);
            Block feet = world.getBlockAt(x, y + 1, z);
            Block head = world.getBlockAt(x, y + 2, z);

            if (isSafeToStandOn(ground) && isPassable(feet) && isPassable(head)) {
                return new Location(world, x + 0.5, y + 1, z + 0.5);
            }
        }

        return null;
    }

    /**
     * Returns true if the block is solid and safe to stand on (not lava or fire).
     */
    private static boolean isSafeToStandOn(Block block) {
        Material type = block.getType();
        if (!type.isSolid()) {
            return false;
        }
        return type != Material.LAVA
                && type != Material.FIRE
                && type != Material.SOUL_FIRE
                && type != Material.MAGMA_BLOCK;
    }

    /**
     * Returns true if the block is passable (air only – excludes water, lava, etc.).
     */
    private static boolean isPassable(Block block) {
        Material type = block.getType();
        return type == Material.AIR
                || type == Material.CAVE_AIR
                || type == Material.VOID_AIR;
    }
}
