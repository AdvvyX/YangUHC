package net.yanguhc.listener;

import net.yanguhc.YangUHC;
import net.yanguhc.game.GameState;
import net.yanguhc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class GameListener implements Listener {

    private final YangUHC plugin;

    public GameListener(YangUHC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GameState state = plugin.getGameManager().getState();

        plugin.getScoreboardManager().registerPlayer(player);

        if (state == GameState.LOBBY) {
            plugin.getGameManager().prepareLobbyPlayer(player);
            TextUtil.sendMessage(player, "<green>¡Bienvenido a <bold>YangUHC</bold>! Usa <gold>/scenarios</gold> y <gold>/team</gold> para prepararte.");
        } else {
            // Reconnect logic or spectator
            if (!plugin.getGameManager().isAlive(player)) {
                plugin.getGameManager().setSpectator(player);
                player.teleport(plugin.getWorldManager().getUhcWorld().getSpawnLocation());
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getScoreboardManager().unregisterPlayer(player);

        if (plugin.getGameManager().getState().isGameActive() && plugin.getGameManager().isAlive(player)) {
            Bukkit.broadcast(TextUtil.parse("<red>" + player.getName() + " se ha desconectado durante la partida."));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Player killer = player.getKiller();

        if (plugin.getGameManager().getState().isGameActive()) {
            if (killer != null && killer != player) {
                plugin.getGameManager().addKill(killer.getUniqueId());
                event.deathMessage(TextUtil.parse("<red>" + player.getName() + " <gray>fue asesinado por <gold>" + killer.getName() + "<gray>!"));
            } else {
                event.deathMessage(TextUtil.parse("<red>" + player.getName() + " <gray>ha muerto!"));
            }

            // Put in spectator mode on next tick
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getGameManager().setSpectator(player);
            });
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (plugin.getGameManager().getState().isGameActive()) {
            event.setRespawnLocation(plugin.getWorldManager().getUhcWorld().getSpawnLocation());
        } else {
            event.setRespawnLocation(plugin.getWorldManager().getLobbySpawn());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        GameState state = plugin.getGameManager().getState();
        if (state == GameState.LOBBY || state == GameState.SCATTERING) {
            event.setCancelled(true);
            if (player.getLocation().getY() < -20) {
                player.teleport(plugin.getWorldManager().getLobbySpawn());
            }
            return;
        }

        // Disable PvP during grace period
        if (event instanceof EntityDamageByEntityEvent byEntity) {
            Player attacker = null;
            if (byEntity.getDamager() instanceof Player p) {
                attacker = p;
            } else if (byEntity.getDamager() instanceof org.bukkit.entity.Projectile proj && proj.getShooter() instanceof Player p) {
                attacker = p;
            }

            if (attacker != null) {
                if (state == GameState.GRACE_PERIOD) {
                    event.setCancelled(true);
                    TextUtil.sendMessage(attacker, "<red>¡El PvP está desactivado durante el periodo de gracia!");
                    return;
                }

                // Friendly Fire check
                if (!plugin.getTeamManager().isFriendlyFire() && plugin.getTeamManager().areTeammates(attacker, player)) {
                    event.setCancelled(true);
                    TextUtil.sendMessage(attacker, "<red>¡El fuego amigo está desactivado!");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.getGameManager().getState() == GameState.LOBBY) {
            if (!event.getPlayer().hasPermission("yanguhc.admin")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.getGameManager().getState() == GameState.LOBBY) {
            if (!event.getPlayer().hasPermission("yanguhc.admin")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRegen(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
            // Natural regeneration strictly disabled in UHC
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLoss(FoodLevelChangeEvent event) {
        if (plugin.getGameManager().getState() == GameState.LOBBY) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (plugin.getGameManager().getState() == GameState.LOBBY) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (plugin.getGameManager().getState() == GameState.LOBBY) {
            if (!event.getWhoClicked().hasPermission("yanguhc.admin")) {
                event.setCancelled(true);
            }
        }
    }
}
