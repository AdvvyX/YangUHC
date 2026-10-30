package net.yanguhc.team;

import net.kyori.adventure.text.Component;
import net.yanguhc.YangUHC;
import net.yanguhc.util.TextUtil;
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
        if (getTeam(leader) != null) {
            TextUtil.sendMessage(leader, "<red>¡Ya perteneces a un equipo!");
            return null;
        }

        String id = "team-" + (teamCounter++);
        String name = "Equipo #" + (teams.size() + 1);
        Team team = new Team(id, name, leader.getUniqueId());

        String color = TEAM_COLORS[(teams.size()) % TEAM_COLORS.length];
        team.setColorTag(color);

        teams.put(id, team);
        playerTeamMap.put(leader.getUniqueId(), team);

        TextUtil.sendMessage(leader, "<green>Has creado el equipo <bold>" + name + "</bold> exitosamente.");
        return team;
    }

    public boolean joinTeam(Player player, Team team) {
        if (team == null || player == null) return false;

        if (getTeam(player) != null) {
            TextUtil.sendMessage(player, "<red>Ya estás en un equipo. Sal primero usando /team leave.");
            return false;
        }

        if (team.getMembers().size() >= maxTeamSize) {
            TextUtil.sendMessage(player, "<red>El equipo ya está lleno (" + maxTeamSize + " miembros máx).");
            return false;
        }

        team.addMember(player.getUniqueId());
        playerTeamMap.put(player.getUniqueId(), team);
        pendingInvites.remove(player.getUniqueId());

        team.broadcast("<green>" + player.getName() + " se ha unido al equipo.");
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

        TextUtil.sendMessage(player, "<yellow>Has abandonado el equipo " + team.getName() + ".");
        team.broadcast("<red>" + player.getName() + " ha salido del equipo.");

        if (team.getMembers().isEmpty()) {
            teams.remove(team.getId());
        }
        return true;
    }

    public void invitePlayer(Player inviter, Player target) {
        Team team = getTeam(inviter);
        if (team == null) {
            team = createTeam(inviter);
        }

        if (!team.getLeader().equals(inviter.getUniqueId())) {
            TextUtil.sendMessage(inviter, "<red>Solo el líder del equipo puede enviar invitaciones.");
            return;
        }

        if (team.getMembers().size() >= maxTeamSize) {
            TextUtil.sendMessage(inviter, "<red>El equipo ya está lleno (" + maxTeamSize + " miembros máx).");
            return;
        }

        if (getTeam(target) != null) {
            TextUtil.sendMessage(inviter, "<red>" + target.getName() + " ya tiene un equipo.");
            return;
        }

        pendingInvites.put(target.getUniqueId(), team.getId());
        TextUtil.sendMessage(inviter, "<green>Invitación enviada a " + target.getName() + ".");
        TextUtil.sendMessage(target, "<gold><bold>UHC TEAMS</bold> <gray>» <yellow>" + inviter.getName() + " te ha invitado a su equipo. Escribe <green><click:run_command:'/team accept'>/team accept</click></green> para unirte.");
    }

    public boolean acceptInvite(Player player) {
        String teamId = pendingInvites.get(player.getUniqueId());
        if (teamId == null) {
            TextUtil.sendMessage(player, "<red>No tienes ninguna invitación pendiente.");
            return false;
        }

        Team team = teams.get(teamId);
        if (team == null) {
            TextUtil.sendMessage(player, "<red>La invitación ya no es válida.");
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
            return false;
        } else {
            teamChatEnabled.add(player.getUniqueId());
            return true;
        }
    }

    public boolean isTeamChatActive(Player player) {
        return teamChatEnabled.contains(player.getUniqueId());
    }

    public void sendTeamChatMessage(Player sender, String message) {
        Team team = getTeam(sender);
        if (team == null) {
            TextUtil.sendMessage(sender, "<red>No estás en un equipo para usar el chat de equipo.");
            return;
        }

        String formatted = "<dark_gray>[<aqua>TeamChat</aqua>]</dark_gray> <yellow>" + sender.getName() + "<gray>: <white>" + message;
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
