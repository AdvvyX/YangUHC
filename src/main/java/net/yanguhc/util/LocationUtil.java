package net.yanguhc.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

public final class LocationUtil {

    private LocationUtil() {
    }

    public static boolean isBlacklistedBiome(Biome biome) {
        if (biome == null) return false;
        String name = biome.name().toUpperCase();
        return name.contains("OCEAN") || name.contains("SWAMP") || name.contains("RIVER");
    }

    public static Location getSafeHighestLocation(World world, int x, int z) {
        if (world == null) return null;

        int highestY = world.getHighestBlockYAt(x, z);
        if (highestY < world.getMinHeight()) {
            highestY = 64;
        }

        Block block = world.getBlockAt(x, highestY, z);
        Material type = block.getType();

        if (type == Material.LAVA || type == Material.WATER || type == Material.FIRE
                || type == Material.SEAGRASS || type == Material.TALL_SEAGRASS
                || type == Material.KELP || type == Material.KELP_PLANT) {
            return null;
        }

        Biome biome = world.getBiome(x, highestY, z);
        if (isBlacklistedBiome(biome)) {
            return null;
        }

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
