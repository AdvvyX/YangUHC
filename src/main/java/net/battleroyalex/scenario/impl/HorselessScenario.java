package net.battleroyalex.scenario.impl;

import net.battleroyalex.BattleRoyaleX;
import net.battleroyalex.scenario.Scenario;
import net.battleroyalex.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.entity.EntityTameEvent;

public class HorselessScenario extends Scenario {

    public HorselessScenario(BattleRoyaleX plugin) {
        super(plugin, "Horseless", "No se permite montar ni domesticar caballos o burros.", Material.SADDLE);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMount(EntityMountEvent event) {
        if (event.getEntity() instanceof Player player && event.getMount() instanceof AbstractHorse) {
            event.setCancelled(true);
            TextUtil.sendMessage(player, "<red>Horseless está activo: ¡No puedes montar caballos!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTame(EntityTameEvent event) {
        if (event.getEntity() instanceof AbstractHorse) {
            event.setCancelled(true);
            if (event.getOwner() instanceof Player player) {
                TextUtil.sendMessage(player, "<red>Horseless está activo: ¡No puedes domesticar caballos!");
            }
        }
    }
}
