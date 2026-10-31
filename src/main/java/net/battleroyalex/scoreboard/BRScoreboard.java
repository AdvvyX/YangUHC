package net.battleroyalex.scoreboard;

import net.kyori.adventure.text.Component;
import net.battleroyalex.team.Team;
import net.battleroyalex.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class BRScoreboard {

    private final Player player;
    private final Scoreboard scoreboard;
    private final Objective objective;

    public BRScoreboard(Player player) {
        this.player = player;
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        this.scoreboard = manager.getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("battleroyalex", Criteria.DUMMY, Component.empty());
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        try {
            this.objective.numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat.blank());
        } catch (Throwable ignored) {
        }

        for (int i = 0; i < 15; i++) {
            String entry = ChatColor.values()[i].toString() + ChatColor.RESET;
            org.bukkit.scoreboard.Team team = scoreboard.registerNewTeam("line_" + i);
            team.addEntry(entry);
        }

        player.setScoreboard(scoreboard);
    }

    public void setTitle(String title) {
        objective.displayName(TextUtil.parse(title));
    }

    public void updateLines(List<String> lines) {
        if (lines == null) lines = new ArrayList<>();

        int count = Math.min(lines.size(), 15);

        for (int i = count; i < 15; i++) {
            String entry = ChatColor.values()[i].toString() + ChatColor.RESET;
            scoreboard.resetScores(entry);
        }

        for (int i = 0; i < count; i++) {
            String line = lines.get(i);
            int score = count - i;
            String entry = ChatColor.values()[i].toString() + ChatColor.RESET;

            org.bukkit.scoreboard.Team team = scoreboard.getTeam("line_" + i);
            if (team != null) {
                team.prefix(TextUtil.parse(line));
            }

            Score objectiveScore = objective.getScore(entry);
            if (objectiveScore.getScore() != score) {
                objectiveScore.setScore(score);
            }
        }
    }

    public void updateTeams(Collection<Team> teams) {
        for (Team team : teams) {
            String scTeamName = "tm_" + team.getId().replace("-", "_");
            if (scTeamName.length() > 16) {
                scTeamName = scTeamName.substring(0, 16);
            }

            org.bukkit.scoreboard.Team scTeam = scoreboard.getTeam(scTeamName);
            if (scTeam == null) {
                scTeam = scoreboard.registerNewTeam(scTeamName);
            }

            String colorTag = team.getColorTag() != null ? team.getColorTag() : "<yellow>";
            scTeam.prefix(TextUtil.parse(colorTag + "[" + team.getName() + "] "));

            for (UUID memberUuid : team.getMembers()) {
                Player member = Bukkit.getPlayer(memberUuid);
                if (member != null && !scTeam.hasEntry(member.getName())) {
                    scTeam.addEntry(member.getName());
                }
            }
        }
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public Player getPlayer() {
        return player;
    }
}
