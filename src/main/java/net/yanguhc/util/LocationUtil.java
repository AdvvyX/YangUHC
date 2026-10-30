package net.yanguhc.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class LocationUtil {

    private LocationUtil() {
    }

    public static Location getSafeHighestLocation(World world, int x, int z) {
        if (world == null) return null;

        int highestY = world.getHighestBlockYAt(x, z);
        if (highestY < world.getMinHeight()) {
            highestY = 64;
        }

        // Check if top block is liquid or dangerous
        Block block = world.getBlockAt(x, highestY, z);
        Material type = block.getType();

        if (type == Material.LAVA || type == Material.WATER || type == Material.FIRE) {
            return null; // not safe
        }

        // Return location centered at block
        return new Location(world, x + 0.5, highestY + 1.0, z + 0.5);
    }

    public static boolean isSafeLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        Block feet = loc.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        Block ground = feet.getRelative(0, -1, 0);

        if (ground.isLiquid() || ground.getType() == Material.LAVA || ground.getType() == Material.WATER) {
            return false;
        }
        if (feet.getType().isSolid() || head.getType().isSolid()) {
            return false;
        }
        return !feet.isLiquid() && !head.isLiquid();
    }
}
