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
            case "create" -> plugin.getTeamManager().createTeam(player);
            case "leave" -> plugin.getTeamManager().leaveTeam(player);
            case "accept" -> plugin.getTeamManager().acceptInvite(player);
            case "invite" -> {
                if (args.length < 2) {
                    TextUtil.sendMessage(player, "<red>Uso: /team invite <jugador>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    TextUtil.sendMessage(player, "<red>Jugador no encontrado.");
                    return true;
                }
                plugin.getTeamManager().invitePlayer(player, target);
            }
            case "list" -> {
                TextUtil.sendMessage(player, "<gold><bold>--- Equipos Registrados ---</bold>");
                for (Team t : plugin.getTeamManager().getAllTeams()) {
                    TextUtil.sendMessage(player, "<yellow>" + t.getName() + " <gray>(" + t.getMembers().size() + "/" + plugin.getTeamManager().getMaxTeamSize() + " miembros, Kills: " + t.getKills() + ")");
                }
            }
            default -> {
                TextUtil.sendMessage(player, "<yellow>Uso: /team [create|invite|accept|leave|list]");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "invite", "accept", "leave", "list");
        }
        return Collections.emptyList();
    }
}
