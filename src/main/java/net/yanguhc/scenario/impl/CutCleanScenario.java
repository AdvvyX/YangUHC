package net.yanguhc.scenario.impl;

import net.yanguhc.YangUHC;
import net.yanguhc.scenario.Scenario;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ListIterator;

public class CutCleanScenario extends Scenario {

    public CutCleanScenario(YangUHC plugin) {
        super(plugin, "CutClean", "Los minerales y carnes caen cocinados automáticamente.", Material.IRON_INGOT);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material type = block.getType();
        ItemStack drop = null;
        int exp = 0;

        switch (type) {
            case IRON_ORE, DEEPSLATE_IRON_ORE -> {
                drop = new ItemStack(Material.IRON_INGOT);
                exp = 2;
            }
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> {
                drop = new ItemStack(Material.GOLD_INGOT);
                exp = 3;
            }
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> {
                drop = new ItemStack(Material.COPPER_INGOT);
                exp = 1;
            }
            case GRAVEL -> drop = new ItemStack(Material.FLINT);
            default -> {
            }
        }

        if (drop != null) {
            event.setDropItems(false);
            event.setExpToDrop(exp);
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), drop);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        ListIterator<ItemStack> it = event.getDrops().listIterator();

        while (it.hasNext()) {
            ItemStack item = it.next();
            if (item == null) continue;
            Material cooked = getCookedMeat(item.getType());
            if (cooked != null) {
                it.set(new ItemStack(cooked, item.getAmount()));
            }
        }

        if (entity instanceof Cow || entity instanceof Pig || entity instanceof Sheep || entity instanceof Chicken) {
            event.setDroppedExp(event.getDroppedExp() + 2);
        }
    }

    private Material getCookedMeat(Material raw) {
        return switch (raw) {
            case BEEF -> Material.COOKED_BEEF;
            case PORKCHOP -> Material.COOKED_PORKCHOP;
            case CHICKEN -> Material.COOKED_CHICKEN;
            case MUTTON -> Material.COOKED_MUTTON;
            case RABBIT -> Material.COOKED_RABBIT;
            case COD -> Material.COOKED_COD;
            case SALMON -> Material.COOKED_SALMON;
            default -> null;
        };
    }
}
