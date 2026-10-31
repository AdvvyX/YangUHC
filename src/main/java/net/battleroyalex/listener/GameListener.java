package net.battleroyalex.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.battleroyalex.BattleRoyaleX;
import net.battleroyalex.game.GameState;
import net.battleroyalex.team.Team;
import net.battleroyalex.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
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
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameListener implements Listener {

    private final BattleRoyaleX plugin;
    private final Map<UUID, Location> deathLocations = new ConcurrentHashMap<>();

    public GameListener(BattleRoyaleX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GameState state = plugin.getGameManager().getState();

        plugin.getScoreboardManager().registerPlayer(player);
        plugin.getScoreboardManager().updateNametags();

        if (state == GameState.LOBBY) {
            plugin.getGameManager().prepareLobbyPlayer(player);
            plugin.getMessages().send(player, "general.welcome");
        } else {
            if (!plugin.getGameManager().isAlive(player)) {
                plugin.getGameManager().setSpectator(player);
                player.teleport(plugin.getWorldManager().getUhcWorld().getSpawnLocation());
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        deathLocations.remove(player.getUniqueId());
        plugin.getScoreboardManager().unregisterPlayer(player);

        if (plugin.getGameManager().getState().isGameActive() && plugin.getGameManager().isAlive(player)) {
            plugin.getMessages().broadcast("game.player-disconnect", "player", player.getName());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (plugin.getTeamManager().isTeamChatActive(player)) {
            event.setCancelled(true);
            String plain = PlainTextComponentSerializer.plainText().serialize(event.message());
            plugin.getTeamManager().sendTeamChatMessage(player, plain);
            return;
        }

        Team team = plugin.getTeamManager().getTeam(player);
        event.renderer((source, sourceDisplayName, message, viewer) -> {
            Component prefix = Component.empty();
            if (team != null) {
                String colorTag = team.getColorTag() != null ? team.getColorTag() : "<yellow>";
                prefix = TextUtil.parse(colorTag + "[" + team.getName() + "]<reset> ");
            }
            return Component.empty()
                    .append(prefix)
                    .append(Component.text(player.getName()))
                    .append(TextUtil.parse("<gray>: </gray>"))
                    .append(message);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        if (plugin.getGameManager().getState() == GameState.ENDED) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        Player killer = player.getKiller();

        if (plugin.getGameManager().getState().isGameActive()) {
            Location deathLoc = player.getLocation().clone();
            deathLocations.put(player.getUniqueId(), deathLoc);

            if (killer != null && killer != player) {
                plugin.getGameManager().addKill(killer.getUniqueId());
                event.deathMessage(TextUtil.parse("<red>" + player.getName() + " <gray>fue asesinado por <gold>" + killer.getName() + "<gray>!"));
            } else {
                event.deathMessage(TextUtil.parse("<red>" + player.getName() + " <gray>ha muerto!"));
            }

            if (deathLoc.getWorld() != null) {
                deathLoc.getWorld().strikeLightningEffect(deathLoc);
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;

                try {
                    player.spigot().respawn();
                } catch (Exception ignored) {
                }

                plugin.getGameManager().setSpectator(player);
                Location specLoc = deathLoc.clone().add(0, 1.2, 0);
                if (specLoc.getY() < 10) {
                    specLoc.setY(64);
                }
                player.teleport(specLoc);

                Vector impulse;
                if (killer != null && killer.isOnline() && killer != player) {
                    Vector diff = deathLoc.toVector().subtract(killer.getLocation().toVector()).setY(0);
                    if (diff.lengthSquared() > 0.001) {
                        impulse = diff.normalize().multiply(0.7).setY(0.45);
                    } else {
                        impulse = deathLoc.getDirection().setY(0).normalize().multiply(-0.7).setY(0.45);
                    }
                } else {
                    Vector dir = deathLoc.getDirection().setY(0);
                    if (dir.lengthSquared() > 0.001) {
                        impulse = dir.normalize().multiply(-0.7).setY(0.45);
                    } else {
                        impulse = new Vector(0, 0.45, 0);
                    }
                }
                player.setVelocity(impulse);

                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.6f, 1.2f);
                plugin.getMessages().sendTitle(player, "titles.eliminated.title", "titles.eliminated.subtitle", 5, 50, 15);
            });
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (plugin.getGameManager().getState().isGameActive()) {
            Location deathLoc = deathLocations.remove(player.getUniqueId());
            if (deathLoc != null) {
                Location specLoc = deathLoc.clone().add(0, 1.2, 0);
                if (specLoc.getY() < 10) specLoc.setY(64);
                event.setRespawnLocation(specLoc);
            } else {
                event.setRespawnLocation(plugin.getWorldManager().getUhcWorld().getSpawnLocation());
            }
        } else {
            event.setRespawnLocation(plugin.getWorldManager().getLobbySpawn());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        GameState state = plugin.getGameManager().getState();
        if (state == GameState.LOBBY || state == GameState.SCATTERING || state == GameState.ENDED) {
            event.setCancelled(true);
            if (state == GameState.LOBBY && player.getLocation().getY() < -20) {
                player.teleport(plugin.getWorldManager().getLobbySpawn());
            }
            return;
        }

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
                    plugin.getMessages().send(attacker, "game.pvp-disabled-grace");
                    return;
                }

                if (!plugin.getTeamManager().isFriendlyFire() && plugin.getTeamManager().areTeammates(attacker, player)) {
                    event.setCancelled(true);
                    plugin.getMessages().send(attacker, "game.friendly-fire-disabled");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.getGameManager().getState() == GameState.LOBBY) {
            if (!event.getPlayer().hasPermission("battleroyale.admin")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.getGameManager().getState() == GameState.LOBBY) {
            if (!event.getPlayer().hasPermission("battleroyale.admin")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRegen(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLoss(FoodLevelChangeEvent event) {
        GameState state = plugin.getGameManager().getState();
        if (state == GameState.LOBBY || state == GameState.SCATTERING || state == GameState.ENDED) {
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
            if (!event.getWhoClicked().hasPermission("battleroyale.admin")) {
                event.setCancelled(true);
            }
        }
    }
}
