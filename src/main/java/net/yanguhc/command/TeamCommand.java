package net.yanguhc.command;

import net.yanguhc.YangUHC;
import net.yanguhc.team.Team;
import net.yanguhc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TeamCommand implements CommandExecutor, TabCompleter {

    private final YangUHC plugin;

    public TeamCommand(YangUHC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo jugadores pueden usar este comando.");
            return true;
        }

        if (args.length == 0) {
            plugin.getTeamGUI().open(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "help", "?" -> sendHelp(player);
            case "create" -> plugin.getTeamManager().createTeam(player);
            case "leave" -> plugin.getTeamManager().leaveTeam(player);
            case "accept" -> plugin.getTeamManager().acceptInvite(player);
            case "gui", "menu" -> plugin.getTeamGUI().open(player);
            case "invite" -> {
                if (args.length < 2) {
                    TextUtil.sendMessage(player, "<red>Uso: /team invite <jugador>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    plugin.getMessages().send(player, "general.player-not-found");
                    return true;
                }
                plugin.getTeamManager().invitePlayer(player, target);
            }
            case "list" -> {
                TextUtil.sendMessage(player, "<gold><bold>--- Equipos Registrados YangUHC ---</bold>");
                int count = 0;
                for (Team t : plugin.getTeamManager().getAllTeams()) {
                    count++;
                    String color = t.getColorTag() != null ? t.getColorTag() : "<yellow>";
                    TextUtil.sendMessage(player, color + "• " + t.getName() + " <gray>(" + t.getMembers().size() + "/" + plugin.getTeamManager().getMaxTeamSize() + " miembros, Kills: " + t.getKills() + ")");
                }
                if (count == 0) {
                    TextUtil.sendMessage(player, "<gray>No hay equipos creados todavía. Usa <yellow>/team create</yellow> para empezar.");
                }
            }
            case "info" -> {
                Team team = null;
                if (args.length >= 2) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target != null) {
                        team = plugin.getTeamManager().getTeam(target);
                    }
                } else {
                    team = plugin.getTeamManager().getTeam(player);
                }

                if (team == null) {
                    TextUtil.sendMessage(player, "<red>No perteneces a ningún equipo o el jugador especificado no tiene equipo.");
                    return true;
                }

                Player leader = Bukkit.getPlayer(team.getLeader());
                String leaderName = leader != null ? leader.getName() : "Desconocido";

                TextUtil.sendMessage(player, "<gold><bold>--- Información de " + team.getName() + " ---</bold>");
                TextUtil.sendMessage(player, "<yellow>Líder: <white>" + leaderName);
                TextUtil.sendMessage(player, "<yellow>Miembros (" + team.getMembers().size() + "/" + plugin.getTeamManager().getMaxTeamSize() + "):");
                for (UUID memberUuid : team.getMembers()) {
                    Player p = Bukkit.getPlayer(memberUuid);
                    String pName = p != null ? p.getName() : "Offline";
                    boolean isAlive = team.isAlive(memberUuid);
                    TextUtil.sendMessage(player, "  <gray>• <white>" + pName + " " + (isAlive ? "<green>[VIVO]</green>" : "<red>[ELIMINADO]</red>"));
                }
                TextUtil.sendMessage(player, "<yellow>Kills del equipo: <gold>" + team.getKills());
            }
            default -> sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        TextUtil.sendMessage(player, "<gold><bold>--- Comandos de Equipos YangUHC ---</bold>");
        TextUtil.sendMessage(player, "<yellow>/team <gray>- Abre el menú interactivo de equipos");
        TextUtil.sendMessage(player, "<yellow>/team create <gray>- Crea un nuevo equipo");
        TextUtil.sendMessage(player, "<yellow>/team invite <jugador> <gray>- Invita a un jugador a tu equipo");
        TextUtil.sendMessage(player, "<yellow>/team accept <gray>- Acepta una invitación pendiente");
        TextUtil.sendMessage(player, "<yellow>/team leave <gray>- Abandona tu equipo actual");
        TextUtil.sendMessage(player, "<yellow>/team list <gray>- Lista todos los equipos registrados");
        TextUtil.sendMessage(player, "<yellow>/team info [jugador] <gray>- Muestra la información de un equipo");
        TextUtil.sendMessage(player, "<yellow>/tc <mensaje> <gray>- Habla por el chat exclusivo de tu equipo");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("create", "invite", "accept", "leave", "list", "info", "help", "gui"), args[0]);
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("info"))) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getName().equalsIgnoreCase(sender.getName())) {
                    names.add(p.getName());
                }
            }
            return filter(names, args[1]);
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String prefix) {
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(prefix.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }
}
