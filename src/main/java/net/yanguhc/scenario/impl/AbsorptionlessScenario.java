package net.yanguhc.scenario.impl;

import net.yanguhc.YangUHC;
import net.yanguhc.scenario.Scenario;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class AbsorptionlessScenario extends Scenario {

    public AbsorptionlessScenario(YangUHC plugin) {
        super(plugin, "Absorptionless", "Las manzanas doradas no otorgan corazones de absorción.", Material.GOLDEN_APPLE);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        Material type = event.getItem().getType();
        if (type == Material.GOLDEN_APPLE || type == Material.ENCHANTED_GOLDEN_APPLE) {
            Player player = event.getPlayer();
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.removePotionEffect(PotionEffectType.ABSORPTION);
                }
            }.runTaskLater(plugin, 1L);
        }
    }
}
