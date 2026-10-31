package net.battleroyalex.scenario.impl;

import net.battleroyalex.BattleRoyaleX;
import net.battleroyalex.scenario.Scenario;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class BleedingSweetsScenario extends Scenario {

    public BleedingSweetsScenario(BattleRoyaleX plugin) {
        super(plugin, "BleedingSweets", "Cada kill suelta 1 diamante, 5 lingotes de oro, 1 libro y 16 flechas.", Material.SUGAR);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        Player killer = event.getPlayer().getKiller();
        if (killer != null && killer != event.getPlayer()) {
            event.getDrops().add(new ItemStack(Material.DIAMOND, 1));
            event.getDrops().add(new ItemStack(Material.GOLD_INGOT, 5));
            event.getDrops().add(new ItemStack(Material.BOOK, 1));
            event.getDrops().add(new ItemStack(Material.ARROW, 16));
            event.getDrops().add(new ItemStack(Material.STRING, 2));
        }
    }
}
