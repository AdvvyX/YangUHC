package net.yanguhc.scenario.impl;

import net.yanguhc.YangUHC;
import net.yanguhc.scenario.Scenario;
import net.yanguhc.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;

public class BowlessScenario extends Scenario {

    public BowlessScenario(YangUHC plugin) {
        super(plugin, "Bowless", "No se permite disparar con arcos ni ballestas.", Material.BOW);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player) {
            event.setCancelled(true);
            TextUtil.sendMessage(player, "<red>Bowless está activo: ¡No puedes usar arcos ni ballestas!");
        }
    }
}
