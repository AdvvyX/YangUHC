package net.battleroyalex.scenario.impl;

import net.battleroyalex.BattleRoyaleX;
import net.battleroyalex.scenario.Scenario;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SoupScenario extends Scenario {

    public SoupScenario(BattleRoyaleX plugin) {
        super(plugin, "Soup", "Las sopas curan 3.5 corazones al hacer click derecho.", Material.MUSHROOM_STEW);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        Material type = item.getType();
        if (type == Material.MUSHROOM_STEW || type == Material.BEETROOT_SOUP || type == Material.RABBIT_STEW) {
            Player player = event.getPlayer();
            AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double max = maxHealth != null ? maxHealth.getValue() : 20.0;

            if (player.getHealth() < max) {
                event.setCancelled(true);
                double healed = Math.min(max, player.getHealth() + 7.0);
                player.setHealth(healed);

                if (event.getHand() != null) {
                    player.getInventory().setItem(event.getHand(), new ItemStack(Material.BOWL));
                }
            }
        }
    }
}
