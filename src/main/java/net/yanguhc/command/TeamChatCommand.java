package net.yanguhc.command;

import net.yanguhc.YangUHC;
import net.yanguhc.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamChatCommand implements CommandExecutor {

    private final YangUHC plugin;

    public TeamChatCommand(YangUHC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo jugadores pueden usar el chat de equipo.");
            return true;
        }

        if (args.length == 0) {
            boolean active = plugin.getTeamManager().toggleTeamChat(player);
            if (active) {
                TextUtil.sendMessage(player, "<green>Chat de equipo <bold>activado</bold>. Todos tus mensajes irán a tu equipo.");
            } else {
                TextUtil.sendMessage(player, "<yellow>Chat de equipo <bold>desactivado</bold>. Ahora hablas por el chat global.");
            }
            return true;
        }

        String msg = String.join(" ", args);
        plugin.getTeamManager().sendTeamChatMessage(player, msg);
        return true;
    }
}
