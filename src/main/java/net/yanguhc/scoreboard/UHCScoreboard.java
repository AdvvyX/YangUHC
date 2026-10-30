package net.yanguhc.scoreboard;

import net.kyori.adventure.text.Component;
import net.yanguhc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;

public class UHCScoreboard {

    private final Player player;
    private final Scoreboard scoreboard;
    private final Objective objective;
    private final List<String> currentLines = new ArrayList<>();

    public UHCScoreboard(Player player) {
        this.player = player;
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        this.scoreboard = manager.getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("yanguhc", Criteria.DUMMY, Component.empty());
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Pre-create 15 team lines
        for (int i = 0; i < 15; i++) {
            String entry = ChatColor.values()[i].toString() + ChatColor.RESET;
            Team team = scoreboard.registerNewTeam("line_" + i);
            team.addEntry(entry);
        }

        player.setScoreboard(scoreboard);
    }

    public void setTitle(String title) {
        objective.displayName(TextUtil.parse(title));
    }

    public void updateLines(List<String> lines) {
        if (lines == null) lines = new ArrayList<>();

        // Scoreboard can display max 15 lines
        int count = Math.min(lines.size(), 15);

        // If line count decreased, unregister unused scores
        for (int i = count; i < 15; i++) {
            String entry = ChatColor.values()[i].toString() + ChatColor.RESET;
            scoreboard.resetScores(entry);
        }

        for (int i = 0; i < count; i++) {
            String line = lines.get(i);
            int score = count - i;
            String entry = ChatColor.values()[i].toString() + ChatColor.RESET;

            Team team = scoreboard.getTeam("line_" + i);
            if (team != null) {
                team.prefix(TextUtil.parse(line));
            }

            Score objectiveScore = objective.getScore(entry);
            if (objectiveScore.getScore() != score) {
                objectiveScore.setScore(score);
            }
        }
    }

    public Player getPlayer() {
        return player;
    }
}
