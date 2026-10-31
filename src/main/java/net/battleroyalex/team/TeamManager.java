package net.battleroyalex.team;

import net.battleroyalex.BattleRoyaleX;
import net.battleroyalex.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeamManager {

    private final BattleRoyaleX plugin;
    private final Map<String, Team> teams = new HashMap<>();
    private final Map<UUID, Team> playerTeamMap = new HashMap<>();
    private final Map<UUID, String> pendingInvites = new ConcurrentHashMap<>();
    private final Set<UUID> teamChatEnabled = new HashSet<>();

    private int maxTeamSize = 2;
    private boolean friendlyFire = false;
    private int teamCounter = 1;

    private static final String[] TEAM_COLORS = {
            "<red>", "<blue>", "<green>", "<yellow>", "<gold>", "<aqua>",
            "<light_purple>", "<dark_green>", "<dark_purple>", "<dark_aqua>",
            "<white>", "<gray>", "<dark_red>"
    };

    public TeamManager(BattleRoyaleX plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        this.maxTeamSize = plugin.getConfig().getInt("teams.max-size", 2);
        this.friendlyFire = plugin.getConfig().getBoolean("teams.friendly-fire", false);
    }

    public boolean isTeamsEnabled() {
        return maxTeamSize > 1;
    }

    public int getMaxTeamSize() {
        return maxTeamSize;
    }

    public void setMaxTeamSize(int maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
        plugin.getConfig().set("teams.max-size", maxTeamSize);
        plugin.saveConfig();
    }

    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
        plugin.getConfig().set("teams.friendly-fire", friendlyFire);
        plugin.saveConfig();
    }

    public Team getTeam(Player player) {
        return player == null ? null : playerTeamMap.get(player.getUniqueId());
    }

    public Team getTeam(UUID uuid) {
        return playerTeamMap.get(uuid);
    }

    public Team getTeamById(String id) {
        return teams.get(id);
    }

    public Collection<Team> getAllTeams() {
        return Collections.unmodifiableCollection(teams.values());
    }

    public Team createTeam(Player leader) {
        return createTeam(leader, "Equipo #" + (teams.size() + 1), true);
    }

    public Team createTeam(Player leader, String customName, boolean notify) {
        if (getTeam(leader) != null) {
            if (notify) plugin.getMessages().send(leader, "teams.already-in");
            return null;
        }

        String id = "team-" + (teamCounter++);
        String name = customName != null ? customName : "Equipo #" + (teams.size() + 1);
        Team team = new Team(id, name, leader.getUniqueId());

        String color = TEAM_COLORS[(teams.size()) % TEAM_COLORS.length];
        team.setColorTag(color);

        teams.put(id, team);
        playerTeamMap.put(leader.getUniqueId(), team);

        if (notify) {
            plugin.getMessages().send(leader, "teams.created", "team", name);
        }

        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().updateNametags();
        }
        return team;
    }

    public void createRandomTeams() {
        clearAll();

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) return;

        Collections.shuffle(players);

        if (maxTeamSize <= 1) {
            for (Player p : players) {
                String teamName = "Equipo " + p.getName();
                Team team = createTeam(p, teamName, false);
                if (team != null) {
                    plugin.getMessages().send(p, "teams.auto-created", "team", teamName);
                }
            }
        } else {
            Team currentTeam = null;
            for (Player p : players) {
                if (currentTeam == null || currentTeam.getMembers().size() >= maxTeamSize) {
                    String teamName = "Equipo #" + (teams.size() + 1);
                    currentTeam = createTeam(p, teamName, false);
                } else {
                    currentTeam.addMember(p.getUniqueId());
                    playerTeamMap.put(p.getUniqueId(), currentTeam);
                }
            }

            for (Team team : teams.values()) {
                List<String> memberNames = new ArrayList<>();
                for (UUID u : team.getMembers()) {
                    Player mp = Bukkit.getPlayer(u);
                    if (mp != null) memberNames.add(mp.getName());
                }
                String namesStr = String.join(", ", memberNames);
                for (UUID u : team.getMembers()) {
                    Player mp = Bukkit.getPlayer(u);
                    if (mp != null) {
                        TextUtil.sendMessage(mp, "<gold>¡Equipo Asignado! <gray>Perteneces a " + team.getColorTag() + team.getName() + " <gray>con: <yellow>" + namesStr);
                    }
                }
            }
        }

        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().updateNametags();
        }
    }

    public void autoAssignTeams() {
        createRandomTeams();
    }

    public void autoAssignSoloTeams() {
        createRandomTeams();
    }

    public void resetTeamsForNewGame() {
        for (Team team : teams.values()) {
            team.resetForNewGame();
        }
        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().updateNametags();
        }
    }

    public boolean joinTeam(Player player, Team team) {
        if (team == null || player == null) return false;

        if (getTeam(player) != null) {
            plugin.getMessages().send(player, "teams.must-leave-first");
            return false;
        }

        if (team.getMembers().size() >= maxTeamSize) {
            plugin.getMessages().send(player, "teams.full", "max", maxTeamSize);
            return false;
        }

        team.addMember(player.getUniqueId());
        playerTeamMap.put(player.getUniqueId(), team);

        String msg = plugin.getMessages().getFormatted("teams.joined", "player", player.getName());
        team.broadcast(msg);

        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().updateNametags();
        }
        return true;
    }

    public boolean areTeammates(Player p1, Player p2) {
        if (p1 == null || p2 == null) return false;
        Team t1 = getTeam(p1);
        Team t2 = getTeam(p2);
        return t1 != null && t1.equals(t2);
    }

    public boolean leaveTeam(Player player) {
        if (player == null) return false;
        Team team = getTeam(player);
        if (team == null) return false;

        team.removeMember(player.getUniqueId());
        playerTeamMap.remove(player.getUniqueId());
        teamChatEnabled.remove(player.getUniqueId());

        plugin.getMessages().send(player, "teams.left", "team", team.getName());
        String msg = plugin.getMessages().getFormatted("teams.left-broadcast", "player", player.getName());
        team.broadcast(msg);

        if (team.getMembers().isEmpty()) {
            teams.remove(team.getId());
        }

        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().updateNametags();
        }
        return true;
    }

    public void invitePlayer(Player leader, Player target) {
        Team team = getTeam(leader);
        if (team == null) {
            team = createTeam(leader);
        }

        if (!leader.getUniqueId().equals(team.getLeader())) {
            plugin.getMessages().send(leader, "general.no-permission");
            return;
        }

        if (team.getMembers().size() >= maxTeamSize) {
            plugin.getMessages().send(leader, "teams.full", "max", maxTeamSize);
            return;
        }

        if (getTeam(target) != null) {
            plugin.getMessages().send(leader, "teams.already-in");
            return;
        }

        pendingInvites.put(target.getUniqueId(), team.getId());
        plugin.getMessages().send(leader, "teams.invite-sent", "player", target.getName());
        plugin.getMessages().send(target, "teams.invite-received", "player", leader.getName());
    }

    public boolean acceptInvite(Player player) {
        String teamId = pendingInvites.remove(player.getUniqueId());
        if (teamId == null) {
            plugin.getMessages().send(player, "teams.no-invite");
            return false;
        }

        Team team = getTeamById(teamId);
        if (team == null) {
            plugin.getMessages().send(player, "teams.invite-expired");
            return false;
        }

        return joinTeam(player, team);
    }

    public boolean toggleTeamChat(Player player) {
        if (getTeam(player) == null) {
            plugin.getMessages().send(player, "teams.not-in-team-chat");
            return false;
        }

        if (teamChatEnabled.contains(player.getUniqueId())) {
            teamChatEnabled.remove(player.getUniqueId());
            plugin.getMessages().send(player, "teams.chat-disabled");
            return false;
        } else {
            teamChatEnabled.add(player.getUniqueId());
            plugin.getMessages().send(player, "teams.chat-enabled");
            return true;
        }
    }

    public boolean isTeamChatActive(Player player) {
        return teamChatEnabled.contains(player.getUniqueId());
    }

    public void sendTeamChatMessage(Player sender, String message) {
        Team team = getTeam(sender);
        if (team == null) {
            plugin.getMessages().send(sender, "teams.not-in-team-chat");
            return;
        }

        String formatted = plugin.getMessages().getFormatted("teams.chat-format", "player", sender.getName(), "message", message);
        team.broadcast(formatted);
    }

    public void clearAll() {
        teams.clear();
        playerTeamMap.clear();
        pendingInvites.clear();
        teamChatEnabled.clear();
        teamCounter = 1;
        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().updateNametags();
        }
    }
}
