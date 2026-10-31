package net.yanguhc.world;

import net.kyori.adventure.title.Title;
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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ScatterManager {

    private final YangUHC plugin;
    private final WorldManager worldManager;
    private final Random random = new Random();
    private boolean scattering = false;
    private final Map<Block, Material> originalBlockState = new HashMap<>();
    private BukkitTask countdownTask;

    public ScatterManager(YangUHC plugin, WorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }

    public void startScatter(Runnable onComplete) {
        if (scattering) return;
        scattering = true;
        cleanupCages();

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
                    startCountdown(onComplete);
                    return;
                }

                List<Player> group = groups.get(index);
                int[] point = targetPoints.get(index);
                index++;

                Location loc = LocationUtil.getSafeHighestLocation(world, point[0], point[1]);
                if (loc == null || !LocationUtil.isSafeLocation(loc)) {
                    int y = Math.max(70, world.getHighestBlockYAt(point[0], point[1]));
                    loc = new Location(world, point[0] + 0.5, y + 1.0, point[1] + 0.5);
                }

                createCage(loc);

                for (Player player : group) {
                    player.teleportAsync(loc).thenAccept(success -> {
                        if (!player.isOnline()) return;
                        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 40, 255, false, false));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 40, 255, false, false));
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                        Title title = Title.title(
                                TextUtil.parse("<gold><bold>¡Cargando mundo!</bold>"),
                                TextUtil.parse("<yellow>Espera a que tus chunks carguen..."),
                                Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(1500), Duration.ofMillis(300))
                        );
                        player.showTitle(title);
                    });
                }
            }
        }.runTaskTimer(plugin, 2L, 4L);
    }

    private void createCage(Location center) {
        World w = center.getWorld();
        if (w == null) return;
        int bx = center.getBlockX();
        int by = center.getBlockY();
        int bz = center.getBlockZ();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block floor = w.getBlockAt(bx + x, by - 1, bz + z);
                setTempGlass(floor);

                Block roof = w.getBlockAt(bx + x, by + 3, bz + z);
                setTempGlass(roof);

                if (x == -1 || x == 1 || z == -1 || z == 1) {
                    Block wall1 = w.getBlockAt(bx + x, by, bz + z);
                    Block wall2 = w.getBlockAt(bx + x, by + 1, bz + z);
                    Block wall3 = w.getBlockAt(bx + x, by + 2, bz + z);
                    setTempGlass(wall1);
                    setTempGlass(wall2);
                    setTempGlass(wall3);
                }
            }
        }
    }

    private void setTempGlass(Block block) {
        if (!originalBlockState.containsKey(block)) {
            originalBlockState.put(block, block.getType());
            block.setType(Material.GLASS);
        }
    }

    public void cleanupCages() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        for (Map.Entry<Block, Material> entry : originalBlockState.entrySet()) {
            Block b = entry.getKey();
            b.setType(entry.getValue());
        }
        originalBlockState.clear();
    }

    private void startCountdown(Runnable onComplete) {
        countdownTask = new BukkitRunnable() {
            int seconds = 5;

            @Override
            public void run() {
                if (seconds <= 0) {
                    cancel();
                    cleanupCages();
                    scattering = false;

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.removePotionEffect(PotionEffectType.SLOWNESS);
                    }

                    plugin.getMessages().broadcast("scatter.completed");
                    if (onComplete != null) {
                        onComplete.run();
                    }
                    return;
                }

                String numColor = seconds <= 2 ? "<green>" : (seconds <= 3 ? "<yellow>" : "<gold>");
                float pitch = 0.8f + ((5 - seconds) * 0.1f);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, pitch);
                    Title title = Title.title(
                            TextUtil.parse(numColor + "<bold>" + seconds + "</bold>"),
                            TextUtil.parse("<yellow>Cargando terreno..."),
                            Title.Times.times(Duration.ZERO, Duration.ofMillis(1200), Duration.ofMillis(200))
                    );
                    player.showTitle(title);
                }

                seconds--;
            }
        }.runTaskTimer(plugin, 10L, 20L);
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
