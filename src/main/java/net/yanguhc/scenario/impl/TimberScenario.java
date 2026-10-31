package net.yanguhc.scenario.impl;

import net.yanguhc.YangUHC;
import net.yanguhc.scenario.Scenario;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class TimberScenario extends Scenario {

    private static final BlockFace[] FACES = {
            BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
            BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST
    };

    public TimberScenario(YangUHC plugin) {
        super(plugin, "Timber", "Talar un tronco rompe el árbol entero.", Material.OAK_LOG);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block root = event.getBlock();
        if (!Tag.LOGS.isTagged(root.getType())) return;

        Player player = event.getPlayer();
        if (player.isSneaking()) return;

        Material axe = player.getInventory().getItemInMainHand().getType();
        if (!axe.name().endsWith("_AXE")) return;

        Set<Block> toBreak = new HashSet<>();
        Queue<Block> queue = new ArrayDeque<>();
        queue.add(root);

        int maxLogs = 150;
        while (!queue.isEmpty() && toBreak.size() < maxLogs) {
            Block current = queue.poll();
            for (BlockFace face : FACES) {
                Block relative = current.getRelative(face);
                if (Tag.LOGS.isTagged(relative.getType()) && toBreak.add(relative)) {
                    queue.add(relative);
                }
            }
        }

        toBreak.remove(root);
        for (Block log : toBreak) {
            log.breakNaturally(player.getInventory().getItemInMainHand());
        }
    }
}
