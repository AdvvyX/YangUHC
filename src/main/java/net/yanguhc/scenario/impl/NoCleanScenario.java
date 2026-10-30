package net.yanguhc.scenario.impl;

import net.yanguhc.YangUHC;
import net.yanguhc.scenario.Scenario;
import net.yanguhc.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NoCleanScenario extends Scenario {

    private final Map<UUID, Long> protectedUntil = new ConcurrentHashMap<>();

    public NoCleanScenario(YangUHC plugin) {
        super(plugin, "NoClean", "Otorga 20 segundos de inmunidad tras conseguir una kill.", Material.SHIELD);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onKill(PlayerDeathEvent event) {
        Player killer = event.getPlayer().getKiller();
        if (killer != null && killer != event.getPlayer()) {
            long until = System.currentTimeMillis() + 20_000L;
            protectedUntil.put(killer.getUniqueId(), until);
            TextUtil.sendMessage(killer, "<green>NoClean activo: Eres invulnerable durante <gold>20 segundos</gold> (se cancela si atacas a otro jugador).");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            Long until = protectedUntil.get(player.getUniqueId());
            if (until != null) {
                if (System.currentTimeMillis() < until) {
                    event.setCancelled(true);
                } else {
                    protectedUntil.remove(player.getUniqueId());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damager && event.getEntity() instanceof Player) {
            if (protectedUntil.remove(damager.getUniqueId()) != null) {
                TextUtil.sendMessage(damager, "<yellow>Has atacado a un jugador: tu protección NoClean ha sido desactivada.");
            }
        }
    }
}
