package net.yanguhc.command;

import net.yanguhc.YangUHC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ScenariosCommand implements CommandExecutor {

    private final YangUHC plugin;

    public ScenariosCommand(YangUHC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo jugadores pueden abrir la interfaz de escenarios.");
            return true;
        }

        plugin.getScenarioGUI().open(player);
        return true;
    }
}
