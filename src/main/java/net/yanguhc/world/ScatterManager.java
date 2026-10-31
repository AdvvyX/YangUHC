package net.yanguhc.world;

import net.yanguhc.YangUHC;
import net.yanguhc.team.Team;
import net.yanguhc.util.LocationUtil;
import net.yanguhc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ScatterManager {

    private final YangUHC plugin;
    private final WorldManager worldManager;
    private final Random random = new Random();
    private boolean scattering = false;

    public ScatterManager(YangUHC plugin, WorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }

    public void startScatter(Runnable onComplete) {
        if (scattering) return;
        scattering = true;

        World world = worldManager.getUhcWorld();
        if (world == null) {
            plugin.getLogger().warning("No se puede iniciar el scatter: el mundo UHC no existe.");
            scattering = false;
            return;
        }

        double radius = worldManager.getBorderController().getCurrentRadius() * 0.85;

        List<List<Player>> groups = new ArrayList<>();
        Set<UUID> processed = new HashSet<>();

        if (plugin.getTeamManager().isTeamsEnabled()) {
            for (Team team : plugin.getTeamManager().getAllTeams()) {
                List<Player> teamPlayers = new ArrayList<>();
                for (UUID uuid : team.getMembers()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null && p.isOnline()) {
                        teamPlayers.add(p);
                        processed.add(uuid);
                    }
                }
                if (!teamPlayers.isEmpty()) {
                    groups.add(teamPlayers);
                }
            }
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!processed.contains(p.getUniqueId())) {
                groups.add(Collections.singletonList(p));
            }
        }

        int totalGroups = groups.size();
        if (totalGroups == 0) {
            scattering = false;
            if (onComplete != null) onComplete.run();
            return;
        }

        plugin.getMessages().broadcast("scatter.starting", "groups", totalGroups);

        List<int[]> targetPoints = generateSpacedPoints((int) radius, totalGroups);

        Set<Long> uniqueChunkKeys = new HashSet<>();
        List<int[]> chunksToPreload = new ArrayList<>();

        for (int[] point : targetPoints) {
            int centerChunkX = point[0] >> 4;
            int centerChunkZ = point[1] >> 4;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    int cx = centerChunkX + dx;
                    int cz = centerChunkZ + dz;
                    long key = (((long) cx) << 32) | (cz & 0xFFFFFFFFL);
                    if (uniqueChunkKeys.add(key)) {
                        chunksToPreload.add(new int[]{cx, cz});
                    }
                }
            }
        }

        int totalChunks = chunksToPreload.size();
        AtomicInteger loadedChunks = new AtomicInteger(0);

        List<CompletableFuture<Chunk>> chunkFutures = new ArrayList<>();
        for (int[] chunkCoords : chunksToPreload) {
            CompletableFuture<Chunk> future = world.getChunkAtAsync(chunkCoords[0], chunkCoords[1], true);
            future.thenAccept(c -> {
                int current = loadedChunks.incrementAndGet();
                int percent = (int) (((double) current / totalChunks) * 100);
                if (current % 5 == 0 || current == totalChunks) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendActionBar(TextUtil.parse("<yellow>Precargando terreno seguro: <green>" + percent + "%</green> <gray>(" + current + "/" + totalChunks + " chunks)</gray>"));
                    }
                }
            });
            chunkFutures.add(future);
        }

        CompletableFuture.allOf(chunkFutures.toArray(new CompletableFuture[0])).thenRun(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                executeScatterTeleport(world, groups, targetPoints, onComplete);
            });
        }).exceptionally(ex -> {
            plugin.getLogger().warning("Error precargando chunks de scatter: " + ex.getMessage());
            Bukkit.getScheduler().runTask(plugin, () -> {
                executeScatterTeleport(world, groups, targetPoints, onComplete);
            });
            return null;
        });
    }

    private void executeScatterTeleport(World world, List<List<Player>> groups, List<int[]> targetPoints, Runnable onComplete) {
        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= groups.size()) {
                    cancel();
                    scattering = false;
                    plugin.getMessages().broadcast("scatter.completed");
                    if (onComplete != null) {
                        onComplete.run();
                    }
                    return;
                }

                List<Player> group = groups.get(index);
                int[] point = targetPoints.get(index);
                index++;

                Location loc = LocationUtil.getSafeHighestLocation(world, point[0], point[1]);
                if (loc == null || !LocationUtil.isSafeLocation(loc)) {
                    int y = Math.max(70, world.getHighestBlockYAt(point[0], point[1]));
                    loc = new Location(world, point[0] + 0.5, y + 1.0, point[1] + 0.5);
                    loc.getBlock().getRelative(0, -1, 0).setType(Material.GLASS);
                }

                for (Player player : group) {
                    player.teleportAsync(loc).thenAccept(success -> {
                        if (!player.isOnline()) return;
                        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 30, 255, false, false));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 3, 1, false, false));
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                        plugin.getMessages().sendTitle(player, "titles.scatter.title", "titles.scatter.subtitle", 10, 40, 10);
                    });
                }
            }
        }.runTaskTimer(plugin, 2L, 4L);
    }

    private List<int[]> generateSpacedPoints(int radius, int count) {
        List<int[]> points = new ArrayList<>();
        double minDistance = Math.max(40, (radius * 1.5) / Math.max(1, Math.sqrt(count)));

        for (int i = 0; i < count; i++) {
            int bestX = 0;
            int bestZ = 0;
            boolean found = false;

            for (int attempt = 0; attempt < 50; attempt++) {
                int x = random.nextInt(radius * 2) - radius;
                int z = random.nextInt(radius * 2) - radius;

                boolean tooClose = false;
                for (int[] existing : points) {
                    double distSq = Math.pow(x - existing[0], 2) + Math.pow(z - existing[1], 2);
                    if (distSq < minDistance * minDistance) {
                        tooClose = true;
                        break;
                    }
                }

                if (!tooClose) {
                    bestX = x;
                    bestZ = z;
                    found = true;
                    break;
                }
            }

            if (!found) {
                bestX = random.nextInt(radius * 2) - radius;
                bestZ = random.nextInt(radius * 2) - radius;
            }

            points.add(new int[]{bestX, bestZ});
        }
        return points;
    }

    public boolean isScattering() {
        return scattering;
    }
}
