package me.einfachduncan.rtp.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;

public class TeleportUtil {

    private static final Random RANDOM = new Random();
    private static final int MAX_SAFE_LOCATION_ATTEMPTS = 50;

    private TeleportUtil() {}

    /**
     * Finds a safe random location within the given radius in the specified world.
     *
     * @param world  the world to search in
     * @param radius the maximum radius from world center (0, 0)
     * @return a safe Location, or null if none found after max attempts
     */
    public static Location findSafeLocation(World world, int radius) {
        for (int attempt = 0; attempt < MAX_SAFE_LOCATION_ATTEMPTS; attempt++) {
            int x = RANDOM.nextInt(radius * 2) - radius;
            int z = RANDOM.nextInt(radius * 2) - radius;

            Location candidate = getSafeY(world, x, z);
            if (candidate != null) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * Scans downward from the world's max height to find a safe Y position.
     */
    private static Location getSafeY(World world, int x, int z) {
        int maxHeight = world.getMaxHeight();
        int minHeight = world.getMinHeight();

        for (int y = maxHeight - 1; y > minHeight; y--) {
            Block feet = world.getBlockAt(x, y, z);
            Block head = world.getBlockAt(x, y + 1, z);
            Block ground = world.getBlockAt(x, y - 1, z);

            if (isSafeToStandOn(ground) && isPassable(feet) && isPassable(head)) {
                return new Location(world, x + 0.5, y, z + 0.5);
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
     * Returns true if the block is passable (air, not a solid block, not harmful liquid).
     */
    private static boolean isPassable(Block block) {
        Material type = block.getType();
        return type == Material.AIR
                || type == Material.CAVE_AIR
                || type == Material.VOID_AIR;
    }
}
