package net.battleroyalex.scenario.impl;

import net.battleroyalex.BattleRoyaleX;
import net.battleroyalex.scenario.Scenario;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HasteyBoysScenario extends Scenario {

    public HasteyBoysScenario(BattleRoyaleX plugin) {
        super(plugin, "HasteyBoys", "Todas las herramientas crafteadas reciben Eficiencia III y Durabilidad III.", Material.DIAMOND_PICKAXE);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        if (result == null) return;

        Material type = result.getType();
        String name = type.name();

        if (name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE")) {
            ItemStack modified = result.clone();
            ItemMeta meta = modified.getItemMeta();
            if (meta != null) {
                meta.addEnchant(Enchantment.EFFICIENCY, 3, true);
                meta.addEnchant(Enchantment.UNBREAKING, 3, true);
                modified.setItemMeta(meta);
            }
            event.setCurrentItem(modified);
        }
    }
}
