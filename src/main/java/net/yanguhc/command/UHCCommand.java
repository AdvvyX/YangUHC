package net.yanguhc.command;

import net.yanguhc.YangUHC;
import net.yanguhc.kit.Kit;
import net.yanguhc.scenario.Scenario;
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

public class UHCCommand implements CommandExecutor, TabCompleter {

    private final YangUHC plugin;

    public UHCCommand(YangUHC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("yanguhc.admin") && !sender.hasPermission("yanguhc.host")) {
            TextUtil.sendMessage(sender, "<red>No tienes permisos para ejecutar comandos de administración de UHC.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "start" -> {
                plugin.getGameManager().startGame();
                TextUtil.sendMessage(sender, "<green>Iniciando proceso de scatter y partida...");
            }
            case "stop" -> {
                plugin.getGameManager().stopGame();
                TextUtil.sendMessage(sender, "<red>Partida de UHC detenida.");
            }
            case "scenarios" -> {
                if (sender instanceof Player p) {
                    plugin.getScenarioGUI().open(p);
                } else {
                    TextUtil.sendMessage(sender, "<red>Solo jugadores pueden abrir la GUI de escenarios.");
                }
            }
            case "forcepvp" -> {
                plugin.getGameManager().enablePvP();
                TextUtil.sendMessage(sender, "<green>PvP forzado manualmente.");
            }
            case "border" -> handleBorder(sender, args);
            case "kit" -> handleKit(sender, args);
            case "revive" -> {
                if (args.length < 2) {
                    TextUtil.sendMessage(sender, "<red>Uso: /uhc revive <jugador>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    TextUtil.sendMessage(sender, "<red>Jugador no encontrado.");
                    return true;
                }
                plugin.getGameManager().revivePlayer(target);
                TextUtil.sendMessage(sender, "<green>Has revivido a " + target.getName() + ".");
            }
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getTeamManager().loadConfig();
                plugin.getScoreboardManager().reload();
                plugin.getScenarioManager().saveConfig();
                TextUtil.sendMessage(sender, "<green>Configuración de YangUHC recargada exitosamente.");
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    private void handleBorder(CommandSender sender, String[] args) {
        if (args.length < 2) {
            TextUtil.sendMessage(sender, "<yellow>Uso: /uhc border <set|shrink> <radio> [tiempo_seg]");
            return;
        }

        String action = args[1].toLowerCase();
        if (action.equals("shrink")) {
            if (args.length >= 4) {
                double radius = Double.parseDouble(args[2]);
                long seconds = Long.parseLong(args[3]);
                plugin.getWorldManager().getBorderController().shrinkTo(radius, seconds);
            } else {
                plugin.getWorldManager().getBorderController().shrinkNextStage(120);
            }
            TextUtil.sendMessage(sender, "<green>Comando de reducción de borde ejecutado.");
        } else if (action.equals("set") && args.length >= 3) {
            double radius = Double.parseDouble(args[2]);
            plugin.getWorldManager().getBorderController().resetBorder(radius);
            TextUtil.sendMessage(sender, "<green>Borde establecido a ±" + (int) radius + " bloques.");
        }
    }

    private void handleKit(CommandSender sender, String[] args) {
        if (args.length < 2) {
            TextUtil.sendMessage(sender, "<yellow>Uso: /uhc kit <create|preview|give|setstarter|list> [argumentos]");
            return;
        }

        String sub = args[1].toLowerCase();
        if (sub.equals("create") && sender instanceof Player p) {
            if (args.length < 3) {
                TextUtil.sendMessage(p, "<red>Uso: /uhc kit create <nombre> [demora_seg]");
                return;
            }
            String name = args[2];
            int delay = args.length >= 4 ? Integer.parseInt(args[3]) : 0;
            plugin.getKitManager().createKitFromPlayer(name, p, delay);
        } else if (sub.equals("preview") && sender instanceof Player p) {
            if (args.length < 3) {
                TextUtil.sendMessage(p, "<red>Uso: /uhc kit preview <nombre>");
                return;
            }
            Kit kit = plugin.getKitManager().getKit(args[2]);
            if (kit == null) {
                TextUtil.sendMessage(p, "<red>Kit no encontrado.");
                return;
            }
            plugin.getKitGUI().openPreview(p, kit);
        } else if (sub.equals("setstarter")) {
            if (args.length < 3) {
                TextUtil.sendMessage(sender, "<red>Uso: /uhc kit setstarter <nombre>");
                return;
            }
            plugin.getKitManager().setStarterKitName(args[2]);
            TextUtil.sendMessage(sender, "<green>Kit inicial establecido a: " + args[2]);
        } else if (sub.equals("list")) {
            TextUtil.sendMessage(sender, "<gold><bold>Kits Disponibles:</bold>");
            for (Kit k : plugin.getKitManager().getAllKits()) {
                TextUtil.sendMessage(sender, "<yellow>- " + k.getName() + " <gray>(Demora: " + k.getDelaySeconds() + "s)");
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        TextUtil.sendMessage(sender, "<gold><bold>--- YangUHC Comandos Admin ---</bold>");
        TextUtil.sendMessage(sender, "<yellow>/uhc start <gray>- Inicia la partida y el scatter");
        TextUtil.sendMessage(sender, "<yellow>/uhc stop <gray>- Detiene la partida");
        TextUtil.sendMessage(sender, "<yellow>/uhc scenarios <gray>- Abre el gestor de escenarios");
        TextUtil.sendMessage(sender, "<yellow>/uhc forcepvp <gray>- Activa el PvP de inmediato");
        TextUtil.sendMessage(sender, "<yellow>/uhc border <shrink|set> <gray>- Controla el borde del mundo");
        TextUtil.sendMessage(sender, "<yellow>/uhc kit <create|preview|setstarter|list> <gray>- Gestor de kits");
        TextUtil.sendMessage(sender, "<yellow>/uhc revive <jugador> <gray>- Revive a un jugador");
        TextUtil.sendMessage(sender, "<yellow>/uhc reload <gray>- Recarga las configuraciones");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("yanguhc.admin")) return Collections.emptyList();

        if (args.length == 1) {
            return filter(Arrays.asList("start", "stop", "scenarios", "forcepvp", "border", "kit", "revive", "reload"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("border")) {
            return filter(Arrays.asList("set", "shrink"), args[1]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("kit")) {
            return filter(Arrays.asList("create", "preview", "give", "setstarter", "list"), args[1]);
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
