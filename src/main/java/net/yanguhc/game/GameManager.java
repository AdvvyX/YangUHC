package net.yanguhc.game;

import net.yanguhc.YangUHC;
import net.yanguhc.team.Team;
import net.yanguhc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {

    private final YangUHC plugin;
    private GameState state = GameState.LOBBY;
    private final Set<UUID> alivePlayers = new HashSet<>();
    private final Set<UUID> spectators = new HashSet<>();
    private final Map<UUID, Integer> killsMap = new ConcurrentHashMap<>();

    private int gameTimeSeconds = 0;
    private int gracePeriodDuration = 1200; // 20 minutes default (1200s)
    private int graceRemainingSeconds = 1200;
    private BukkitTask mainTimerTask;

    public GameManager(YangUHC plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        this.gracePeriodDuration = plugin.getConfig().getInt("game.grace-period-seconds", 1200);
        this.graceRemainingSeconds = gracePeriodDuration;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public boolean isAlive(Player player) {
        return player != null && alivePlayers.contains(player.getUniqueId());
    }

    public boolean isSpectator(Player player) {
        return player != null && spectators.contains(player.getUniqueId());
    }

    public int getAlivePlayersCount() {
        return alivePlayers.size();
    }

    public int getSpectatorCount() {
        return spectators.size();
    }

    public int getPlayerKills(UUID uuid) {
        return killsMap.getOrDefault(uuid, 0);
    }

    public void addKill(UUID uuid) {
        killsMap.put(uuid, getPlayerKills(uuid) + 1);
        Team team = plugin.getTeamManager().getTeam(uuid);
        if (team != null) {
            team.addKill();
        }
    }

    public int getGameTimeSeconds() {
        return gameTimeSeconds;
    }

    public int getGraceRemainingSeconds() {
        return graceRemainingSeconds;
    }

    public void startGame() {
        if (state != GameState.LOBBY) {
            return;
        }

        setState(GameState.SCATTERING);
        alivePlayers.clear();
        spectators.clear();
        killsMap.clear();
        gameTimeSeconds = 0;
        graceRemainingSeconds = gracePeriodDuration;

        for (Player p : Bukkit.getOnlinePlayers()) {
            alivePlayers.add(p.getUniqueId());
            p.setGameMode(GameMode.SURVIVAL);
            p.setHealth(20.0);
            p.setFoodLevel(20);
            p.setSaturation(5.0f);
            p.getInventory().clear();
        }

        plugin.getWorldManager().getScatterManager().startScatter(() -> {
            setState(GameState.GRACE_PERIOD);
            plugin.getWorldManager().getUhcWorld().setPVP(false);

            // Give kits
            plugin.getKitManager().giveStarterKits();

            Bukkit.broadcast(TextUtil.parse("<green><bold>¡EL UHC HA COMENZADO!</bold>"));
            Bukkit.broadcast(TextUtil.parse("<yellow>Periodo de gracia activo durante <gold>" + (gracePeriodDuration / 60) + " minutos</gold>. El PvP está desactivado."));

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                TextUtil.sendTitle(p, "<green><bold>¡UHC INICIADO!</bold>", "<yellow>Recolecta recursos y prepárate", 10, 60, 20);
            }

            startMainTimer();
        });
    }

    private void startMainTimer() {
        if (mainTimerTask != null) mainTimerTask.cancel();

        mainTimerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!state.isGameActive()) {
                    cancel();
                    return;
                }

                gameTimeSeconds++;

                // Handle Grace Period
                if (state == GameState.GRACE_PERIOD) {
                    graceRemainingSeconds--;
                    if (graceRemainingSeconds <= 0) {
                        enablePvP();
                    } else if (graceRemainingSeconds == 60 || graceRemainingSeconds == 30 || graceRemainingSeconds == 10 || graceRemainingSeconds <= 5) {
                        Bukkit.broadcast(TextUtil.parse("<red><bold>AVISO</bold> <gray>» El PvP se activará en <yellow>" + graceRemainingSeconds + " segundos</yellow>!"));
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                        }
                    }
                }

                // Check border shrink schedule
                checkBorderSchedule(gameTimeSeconds);

                // Check win condition
                checkWinCondition();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void enablePvP() {
        setState(GameState.INGAME);
        plugin.getWorldManager().getUhcWorld().setPVP(true);

        Bukkit.broadcast(TextUtil.parse("<red><bold>¡EL PVP HA SIDO ACTIVADO!</bold> <gray>La protección de gracia ha expirado."));
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 1f);
            TextUtil.sendTitle(p, "<red><bold>¡PVP ACTIVADO!</bold>", "<yellow>Que gane el mejor", 10, 60, 20);
        }
    }

    private void checkBorderSchedule(int seconds) {
        // Border shrink triggers configurable
        // Shrink stage 1 at 25 min (1500s)
        if (seconds == 1500) {
            plugin.getWorldManager().getBorderController().shrinkNextStage(120);
        } else if (seconds == 2400) { // 40 min
            plugin.getWorldManager().getBorderController().shrinkNextStage(120);
        } else if (seconds == 3300) { // 55 min
            plugin.getWorldManager().getBorderController().shrinkNextStage(120);
        } else if (seconds == 4200) { // 70 min
            setState(GameState.DEATHMATCH);
            plugin.getWorldManager().getBorderController().shrinkNextStage(60);
            Bukkit.broadcast(TextUtil.parse("<dark_red><bold>¡DEATHMATCH INICIADO!</bold> <yellow>El borde se reduce al centro!"));
        }
    }

    public void setSpectator(Player player) {
        if (player == null) return;
        alivePlayers.remove(player.getUniqueId());
        spectators.add(player.getUniqueId());

        Team team = plugin.getTeamManager().getTeam(player);
        if (team != null) {
            team.setMemberDead(player.getUniqueId());
        }

        player.setGameMode(GameMode.SPECTATOR);
        TextUtil.sendMessage(player, "<red>Has sido eliminado. Ahora estás en modo espectador.");
        checkWinCondition();
    }

    public void revivePlayer(Player player) {
        if (player == null) return;
        spectators.remove(player.getUniqueId());
        alivePlayers.add(player.getUniqueId());

        Team team = plugin.getTeamManager().getTeam(player);
        if (team != null) {
            team.addMember(player.getUniqueId());
        }

        player.setGameMode(GameMode.SURVIVAL);
        player.teleport(plugin.getWorldManager().getUhcWorld().getSpawnLocation());
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 10, 255));
        TextUtil.sendMessage(player, "<green>¡Has sido revivido por un administrador!");
        Bukkit.broadcast(TextUtil.parse("<yellow>" + player.getName() + " ha sido revivido en la partida."));
    }

    public void checkWinCondition() {
        if (!state.isGameActive()) return;

        if (plugin.getTeamManager().isTeamsEnabled()) {
            List<Team> activeTeams = new ArrayList<>();
            for (Team t : plugin.getTeamManager().getAllTeams()) {
                if (!t.isEliminated()) {
                    activeTeams.add(t);
                }
            }

            if (activeTeams.size() == 1 && alivePlayers.size() <= activeTeams.get(0).getMembers().size()) {
                endGame(activeTeams.get(0));
            } else if (activeTeams.isEmpty() && alivePlayers.size() <= 1) {
                endGame(null);
            }
        } else {
            if (alivePlayers.size() == 1) {
                UUID winnerUuid = alivePlayers.iterator().next();
                Player winner = Bukkit.getPlayer(winnerUuid);
                endGame(winner);
            } else if (alivePlayers.isEmpty()) {
                endGame(null);
            }
        }
    }

    public void endGame(Object winnerObj) {
        setState(GameState.ENDED);
        if (mainTimerTask != null) mainTimerTask.cancel();

        String winnerName = "Nadie";
        if (winnerObj instanceof Team team) {
            winnerName = team.getName();
        } else if (winnerObj instanceof Player player) {
            winnerName = player.getName();
        }

        Bukkit.broadcast(TextUtil.parse("<gold><bold>==============================</bold>"));
        Bukkit.broadcast(TextUtil.parse("<yellow><bold>           ¡YANG UHC HA FINALIZADO!           </bold>"));
        Bukkit.broadcast(TextUtil.parse("<white>Ganador: <green><bold>" + winnerName + "</bold>"));
        Bukkit.broadcast(TextUtil.parse("<gold><bold>==============================</bold>"));

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            TextUtil.sendTitle(p, "<gold><bold>¡VICTORIA!</bold>", "<yellow>Ganador: " + winnerName, 10, 100, 20);
        }

        new BukkitRunnable() {
            int countdown = 30;

            @Override
            public void run() {
                if (countdown <= 0) {
                    cancel();
                    resetGame();
                    return;
                }
                if (countdown == 10 || countdown <= 5) {
                    Bukkit.broadcast(TextUtil.parse("<gray>El servidor se reiniciará en <yellow>" + countdown + " segundos</yellow>..."));
                }
                countdown--;
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void resetGame() {
        setState(GameState.LOBBY);
        alivePlayers.clear();
        spectators.clear();
        killsMap.clear();
        gameTimeSeconds = 0;
        graceRemainingSeconds = gracePeriodDuration;

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();
            p.teleport(plugin.getWorldManager().getLobbySpawn());
        }

        plugin.getWorldManager().getBorderController().resetBorder(
                plugin.getConfig().getDouble("border.initial-radius", 1000.0)
        );
        plugin.getKitManager().reset();
    }

    public void stopGame() {
        setState(GameState.LOBBY);
        if (mainTimerTask != null) mainTimerTask.cancel();
        resetGame();
        Bukkit.broadcast(TextUtil.parse("<red><bold>UHC</bold> <gray>» La partida ha sido detenida por un administrador."));
    }
}
