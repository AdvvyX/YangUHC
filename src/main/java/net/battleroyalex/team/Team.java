package net.battleroyalex.team;

import net.kyori.adventure.text.Component;
import net.battleroyalex.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class Team {

    private final String id;
    private String name;
    private UUID leader;
    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> aliveMembers = new HashSet<>();
    private int kills = 0;
    private String colorTag = "<yellow>";

    public Team(String id, String name, UUID leader) {
        this.id = id;
        this.name = name;
        this.leader = leader;
        this.members.add(leader);
        this.aliveMembers.add(leader);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public Set<UUID> getAliveMembers() {
        return Collections.unmodifiableSet(aliveMembers);
    }

    public boolean addMember(UUID uuid) {
        aliveMembers.add(uuid);
        return members.add(uuid);
    }

    public boolean removeMember(UUID uuid) {
        aliveMembers.remove(uuid);
        boolean removed = members.remove(uuid);
        if (uuid.equals(leader) && !members.isEmpty()) {
            this.leader = members.iterator().next();
        }
        return removed;
    }

    public void setMemberDead(UUID uuid) {
        aliveMembers.remove(uuid);
    }

    public boolean isAlive(UUID uuid) {
        return aliveMembers.contains(uuid);
    }

    public boolean isEliminated() {
        return aliveMembers.isEmpty();
    }

    public void resetForNewGame() {
        this.aliveMembers.clear();
        this.aliveMembers.addAll(this.members);
        this.kills = 0;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        this.kills++;
    }

    public String getColorTag() {
        return colorTag;
    }

    public void setColorTag(String colorTag) {
        this.colorTag = colorTag;
    }

    public Component getFormattedDisplayName() {
        return TextUtil.parse(colorTag + name + "</" + colorTag.replace("<", "").replace(">", "") + ">");
    }

    public List<Player> getOnlinePlayers() {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : members) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                players.add(p);
            }
        }
        return players;
    }

    public void broadcast(String message) {
        for (Player p : getOnlinePlayers()) {
            TextUtil.sendMessage(p, message);
        }
    }
}
