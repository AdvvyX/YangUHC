package net.yanguhc.scoreboard;

import net.kyori.adventure.text.Component;
import net.yanguhc.YangUHC;
import net.yanguhc.game.GameState;
import net.yanguhc.team.Team;
import net.yanguhc.util.TextUtil;
import net.yanguhc.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ScoreboardManager {

    private final YangUHC plugin;
    private final Map<UUID, UHCScoreboard> scoreboards = new HashMap<>();
    private File file;
    private FileConfiguration config;
    private BukkitTask updateTask;

    public ScoreboardManager(YangUHC plugin) {
        this.plugin = plugin;
        loadFile();
        startTask();
    }

    private void loadFile() {
        this.file = new File(plugin.getDataFolder(), "scoreboard.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            if (plugin.getResource("scoreboard.yml") != null) {
                plugin.saveResource("scoreboard.yml", false);
            } else {
                createDefaultScoreboardFile();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    private void createDefaultScoreboardFile() {
        YamlConfiguration c = new YamlConfiguration();
        c.set("title", "%animation:titleAnimation%");
        c.set("update-interval-ticks", 2);
        c.set("server-ip", "play.yanguhc.net");

        c.set("layouts.LOBBY", Arrays.asList(
                "<gray><st>-------------------</st>",
                "<white>Estado: <yellow>Esperando",
                "<white>Jugadores: <gold>{alive}<gray>/{total_players}",
                "<white>Equipo: <aqua>{team}",
                "<white>Escenarios: <green>{scenarios_count} activos",
                "",
                "%animation:glowIp%",
                "<gray><st>-------------------</st>"
        ));

        c.set("layouts.SCATTERING", Arrays.asList(
                "<gray><st>-------------------</st>",
                "<white>Estado: <aqua>Teleportando",
                "<white>Jugadores: <gold>{alive}",
                "",
                "<yellow>Preparando terreno...",
                "<gray><st>-------------------</st>"
        ));

        c.set("layouts.GRACE_PERIOD", Arrays.asList(
                "<gray><st>-------------------</st>",
                "<white>Tiempo: <gold>{timer}",
                "<white>PvP Activo en: <red>{pvp_timer}",
                "<white>Vivos: <green>{alive}",
                "<white>Borde: <aqua>±{border}",
                "<white>Tus Kills: <red>{kills}",
                "<white>Equipo: <yellow>{team}",
                "",
                "<white>Coords: <gray>{x}, {y}, {z}",
                "%animation:glowIp%",
                "<gray><st>-------------------</st>"
        ));

        c.set("layouts.INGAME", Arrays.asList(
                "<gray><st>-------------------</st>",
                "<white>Tiempo: <gold>{timer}",
                "<white>Borde: <aqua>±{border}",
                "<white>Vivos: <green>{alive}",
                "<white>Kills: <red>{kills}",
                "<white>Equipo Kills: <gold>{team_kills}",
                "<white>Coords: <gray>{x}, {y}, {z}",
                "",
                "%animation:glowIp%",
                "<gray><st>-------------------</st>"
        ));

        c.set("layouts.DEATHMATCH", Arrays.asList(
                "<gray><st>-------------------</st>",
                "<dark_red><bold>DEATHMATCH</bold>",
                "<white>Borde Final: <red>±{border}",
                "<white>Sobrevivientes: <green>{alive}",
                "<white>Kills: <gold>{kills}",
                "",
                "%animation:glowIp%",
                "<gray><st>-------------------</st>"
        ));

        c.set("layouts.ENDED", Arrays.asList(
                "<gray><st>-------------------</st>",
                "<gold><bold>¡JUEGO TERMINADO!</bold>",
                "<white>Kills Totales: <red>{kills}",
                "",
                "<yellow>¡Gracias por jugar!",
                "<gray><st>-------------------</st>"
        ));

        try {
            c.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo crear scoreboard.yml: " + e.getMessage());
        }
    }

    public void reload() {
        if (file != null) {
            this.config = YamlConfiguration.loadConfiguration(file);
        }
        startTask();
    }

    public void registerPlayer(Player player) {
        if (player == null) return;
        scoreboards.put(player.getUniqueId(), new UHCScoreboard(player));
        updateNametags();
    }

    public void unregisterPlayer(Player player) {
        if (player == null) return;
        scoreboards.remove(player.getUniqueId());
    }

    public void updateNametags() {
        for (UHCScoreboard uhcScoreboard : scoreboards.values()) {
            uhcScoreboard.updateTeams(plugin.getTeamManager().getAllTeams());
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            Team team = plugin.getTeamManager().getTeam(p);
            if (team != null) {
                String colorTag = team.getColorTag() != null ? team.getColorTag() : "<yellow>";
                Component displayName = TextUtil.parse(colorTag + "[" + team.getName() + "] <white>" + p.getName());
                p.displayName(displayName);
                p.playerListName(displayName);
                p.customName(displayName);
                p.setCustomNameVisible(true);
            } else {
                p.displayName(TextUtil.parse("<white>" + p.getName()));
                p.playerListName(TextUtil.parse("<white>" + p.getName()));
                p.customName(null);
                p.setCustomNameVisible(false);
            }
        }
    }

    private void startTask() {
        if (updateTask != null) updateTask.cancel();

        long intervalTicks = Math.max(1, config != null ? config.getLong("update-interval-ticks", 2) : 2);

        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateAll();
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);
    }

    public void updateAll() {
        GameState state = plugin.getGameManager().getState();
        String rawTitle = config.getString("title", "%animation:titleAnimation%");
        String animatedTitle = plugin.getAnimationManager().replaceAnimations(rawTitle);

        List<String> template = config.getStringList("layouts." + state.name());
        if (template.isEmpty()) {
            template = config.getStringList("layouts.INGAME");
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            UHCScoreboard board = scoreboards.computeIfAbsent(player.getUniqueId(), u -> new UHCScoreboard(player));
            board.setTitle(animatedTitle);

            List<String> formatted = new ArrayList<>();
            for (String line : template) {
                formatted.add(replacePlaceholders(line, player, state));
            }
            board.updateLines(formatted);
        }
    }

    private String replacePlaceholders(String text, Player player, GameState state) {
        String resolved = plugin.getAnimationManager().replaceAnimations(text);

        String ip = config.getString("server-ip", "play.yanguhc.net");
        int timerSeconds = plugin.getGameManager().getGameTimeSeconds();
        int graceRemaining = plugin.getGameManager().getGraceRemainingSeconds();
        int border = (int) plugin.getWorldManager().getBorderController().getCurrentRadius();
        int nextBorder = (int) plugin.getWorldManager().getBorderController().getNextStageRadius();
        int alive = plugin.getGameManager().getAlivePlayersCount();
        int spectators = plugin.getGameManager().getSpectatorCount();
        int kills = plugin.getGameManager().getPlayerKills(player.getUniqueId());

        Team team = plugin.getTeamManager().getTeam(player);
        String teamName = team != null ? team.getName() : "Ninguno";
        int teamKills = team != null ? team.getKills() : kills;
        int scenariosCount = plugin.getScenarioManager().getEnabledScenarios().size();

        return resolved
                .replace("{state}", state.getDisplayName())
                .replace("{timer}", TimeUtil.formatSeconds(timerSeconds))
                .replace("{pvp_timer}", TimeUtil.formatSeconds(graceRemaining))
                .replace("{border}", String.valueOf(border))
                .replace("{next_border}", String.valueOf(nextBorder))
                .replace("{alive}", String.valueOf(alive))
                .replace("{spectators}", String.valueOf(spectators))
                .replace("{total_players}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{kills}", String.valueOf(kills))
                .replace("{team}", teamName)
                .replace("{team_kills}", String.valueOf(teamKills))
                .replace("{scenarios_count}", String.valueOf(scenariosCount))
                .replace("{player}", player.getName())
                .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                .replace("{y}", String.valueOf(player.getLocation().getBlockY()))
                .replace("{z}", String.valueOf(player.getLocation().getBlockZ()))
                .replace("{server_ip}", ip);
    }

    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        scoreboards.clear();
    }
}
