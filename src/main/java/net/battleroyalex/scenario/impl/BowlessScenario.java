package net.battleroyalex.scenario.impl;

import net.battleroyalex.BattleRoyaleX;
import net.battleroyalex.scenario.Scenario;
import net.battleroyalex.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;

public class BowlessScenario extends Scenario {

    public BowlessScenario(BattleRoyaleX plugin) {
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
