package net.yanguhc.world;

import net.yanguhc.YangUHC;
import net.yanguhc.team.Team;
import net.yanguhc.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

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

                List<Player> group = groups.get(index++);
                Location loc = findSafeLocation(world, (int) radius);

                for (Player player : group) {
                    player.teleport(loc);

                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 30, 255, false, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 3, 1, false, false));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                    plugin.getMessages().sendTitle(player, "titles.scatter.title", "titles.scatter.subtitle", 10, 40, 10);
                }
            }
        }.runTaskTimer(plugin, 5L, 10L);
    }

    private Location findSafeLocation(World world, int radius) {
        int attempts = 0;
        while (attempts < 50) {
            attempts++;
            int x = random.nextInt(radius * 2) - radius;
            int z = random.nextInt(radius * 2) - radius;

            Location safe = LocationUtil.getSafeHighestLocation(world, x, z);
            if (safe != null && LocationUtil.isSafeLocation(safe)) {
                return safe;
            }
        }

        int x = random.nextInt(radius * 2) - radius;
        int z = random.nextInt(radius * 2) - radius;
        int y = Math.max(70, world.getHighestBlockYAt(x, z));
        Location loc = new Location(world, x + 0.5, y + 1.0, z + 0.5);
        loc.getBlock().getRelative(0, -1, 0).setType(Material.GLASS);
        return loc;
    }

    public boolean isScattering() {
        return scattering;
    }
}
