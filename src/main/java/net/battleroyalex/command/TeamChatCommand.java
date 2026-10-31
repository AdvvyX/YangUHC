package net.battleroyalex.command;

import net.battleroyalex.BattleRoyaleX;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamChatCommand implements CommandExecutor {

    private final BattleRoyaleX plugin;

    public TeamChatCommand(BattleRoyaleX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo jugadores pueden usar el chat de equipo.");
            return true;
        }

        if (args.length == 0) {
            plugin.getTeamManager().toggleTeamChat(player);
            return true;
        }

        String msg = String.join(" ", args);
        plugin.getTeamManager().sendTeamChatMessage(player, msg);
        return true;
    }
}
