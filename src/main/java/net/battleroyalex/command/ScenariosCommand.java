package net.battleroyalex.command;

import net.battleroyalex.BattleRoyaleX;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ScenariosCommand implements CommandExecutor {

    private final BattleRoyaleX plugin;

    public ScenariosCommand(BattleRoyaleX plugin) {
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
