package net.yanguhc.team;

import net.yanguhc.YangUHC;
import net.yanguhc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeamManager {

    private final YangUHC plugin;
    private final Map<String, Team> teams = new HashMap<>();
    private final Map<UUID, Team> playerTeamMap = new HashMap<>();
    private final Map<UUID, String> pendingInvites = new ConcurrentHashMap<>();
    private final Set<UUID> teamChatEnabled = new HashSet<>();

    private int maxTeamSize = 2; // Default To2
    private boolean friendlyFire = false;
    private int teamCounter = 1;

    private static final String[] TEAM_COLORS = {
            "<red>", "<blue>", "<green>", "<yellow>", "<gold>", "<aqua>",
            "<light_purple>", "<dark_green>", "<dark_purple>", "<dark_aqua>",
            "<white>", "<gray>", "<dark_red>"
    };

    public TeamManager(YangUHC plugin) {
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
        return team;
    }

    public void autoAssignSoloTeams() {
        if (!isTeamsEnabled()) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (getTeam(p) == null) {
                String teamName = "Equipo " + p.getName();
                Team team = createTeam(p, teamName, false);
                if (team != null) {
                    plugin.getMessages().send(p, "teams.auto-created", "team", teamName);
                }
            }
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
        pendingInvites.remove(player.getUniqueId());

        String msg = plugin.getMessages().getFormatted("teams.joined", "player", player.getName());
        team.broadcast(msg);
        return true;
    }

    public boolean leaveTeam(Player player) {
        Team team = getTeam(player);
        if (team == null) {
            TextUtil.sendMessage(player, "<red>No estás en ningún equipo.");
            return false;
        }

        team.removeMember(player.getUniqueId());
        playerTeamMap.remove(player.getUniqueId());
        teamChatEnabled.remove(player.getUniqueId());

        plugin.getMessages().send(player, "teams.left", "team", team.getName());
        String msg = plugin.getMessages().getFormatted("teams.left-broadcast", "player", player.getName());
        team.broadcast(msg);

        // Delete team if empty
        if (team.getMembers().isEmpty()) {
            teams.remove(team.getId());
        }
        return true;
    }

    public void invitePlayer(Player leader, Player target) {
        Team team = getTeam(leader);
        if (team == null) {
            team = createTeam(leader);
        }

        if (team == null) return;

        if (!team.getLeader().equals(leader.getUniqueId())) {
            TextUtil.sendMessage(leader, "<red>Solo el líder del equipo puede invitar jugadores.");
            return;
        }

        if (team.getMembers().size() >= maxTeamSize) {
            plugin.getMessages().send(leader, "teams.full", "max", maxTeamSize);
            return;
        }

        if (getTeam(target) != null) {
            TextUtil.sendMessage(leader, "<red>" + target.getName() + " ya está en un equipo.");
            return;
        }

        pendingInvites.put(target.getUniqueId(), team.getId());
        plugin.getMessages().send(leader, "teams.invite-sent", "player", target.getName());
        plugin.getMessages().send(target, "teams.invite-received", "player", leader.getName());
    }

    public boolean acceptInvite(Player player) {
        String teamId = pendingInvites.get(player.getUniqueId());
        if (teamId == null) {
            plugin.getMessages().send(player, "teams.no-invite");
            return false;
        }

        Team team = teams.get(teamId);
        if (team == null) {
            plugin.getMessages().send(player, "teams.invite-expired");
            pendingInvites.remove(player.getUniqueId());
            return false;
        }

        return joinTeam(player, team);
    }

    public boolean areTeammates(Player p1, Player p2) {
        if (p1 == null || p2 == null || !isTeamsEnabled()) return false;
        Team t1 = getTeam(p1);
        Team t2 = getTeam(p2);
        return t1 != null && t1 == t2;
    }

    public boolean toggleTeamChat(Player player) {
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
    }
}
