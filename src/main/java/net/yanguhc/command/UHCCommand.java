package net.yanguhc.command;

import net.yanguhc.YangUHC;
import net.yanguhc.game.GameState;
import net.yanguhc.kit.Kit;
import net.yanguhc.util.TextUtil;
import net.yanguhc.util.TimeUtil;
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
        if (!sender.hasPermission("yanguhc.admin") && !sender.hasPermission("yanguhc.host") && !sender.isOp()) {
            plugin.getMessages().send(sender, "general.no-permission");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "config" -> {
                if (sender instanceof Player p) {
                    plugin.getConfigGUI().open(p);
                } else {
                    TextUtil.sendMessage(sender, "<red>Solo jugadores pueden abrir el menú de configuración.");
                }
            }
            case "start" -> {
                GameState state = plugin.getGameManager().getState();
                if (state != GameState.LOBBY) {
                    plugin.getMessages().send(sender, "general.game-already-started");
                    return true;
                }
                if (Bukkit.getOnlinePlayers().isEmpty()) {
                    plugin.getMessages().send(sender, "general.no-players-online");
                    return true;
                }
                boolean started = plugin.getGameManager().startGame();
                if (started) {
                    TextUtil.sendMessage(sender, "<green>Iniciando proceso de scatter y partida...");
                }
            }
            case "stop" -> {
                GameState state = plugin.getGameManager().getState();
                if (state == GameState.LOBBY) {
                    plugin.getMessages().send(sender, "general.game-not-active");
                    return true;
                }
                plugin.getGameManager().stopGame();
            }
            case "scenarios" -> {
                if (sender instanceof Player p) {
                    plugin.getScenarioGUI().open(p);
                } else {
                    TextUtil.sendMessage(sender, "<red>Solo jugadores pueden abrir el menú de escenarios.");
                }
            }
            case "forcepvp" -> {
                GameState currentState = plugin.getGameManager().getState();
                if (currentState == GameState.INGAME || currentState == GameState.DEATHMATCH) {
                    plugin.getMessages().send(sender, "general.pvp-already-active");
                    return true;
                }
                if (currentState != GameState.GRACE_PERIOD) {
                    plugin.getMessages().send(sender, "general.pvp-not-grace");
                    return true;
                }
                plugin.getGameManager().enablePvP();
            }
            case "border" -> {
                handleBorder(sender, args);
            }
            case "kit", "kits" -> {
                handleKit(sender, args);
            }
            case "setlobby", "setspawn" -> {
                if (sender instanceof Player p) {
                    plugin.getWorldManager().setLobbySpawn(p.getLocation());
                    plugin.getMessages().send(p, "general.setlobby-success");
                } else {
                    TextUtil.sendMessage(sender, "<red>Solo jugadores pueden establecer el lobby.");
                }
            }
            case "revive" -> {
                if (!plugin.getGameManager().getState().isGameActive()) {
                    plugin.getMessages().send(sender, "general.game-not-active");
                    return true;
                }
                if (args.length < 2) {
                    TextUtil.sendMessage(sender, "<red>Uso: /uhc revive <jugador>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    plugin.getMessages().send(sender, "general.player-not-found");
                    return true;
                }
                if (plugin.getGameManager().isAlive(target)) {
                    TextUtil.sendMessage(sender, "<red>Ese jugador ya está vivo.");
                    return true;
                }
                plugin.getGameManager().revivePlayer(target);
            }
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getMessages().reload();
                plugin.getAnimationManager().reload();
                plugin.getScoreboardManager().reload();
                TextUtil.sendMessage(sender, "<green>¡Configuraciones recargadas exitosamente!");
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleBorder(CommandSender sender, String[] args) {
        if (args.length < 2) {
            double current = plugin.getWorldManager().getBorderController().getCurrentRadius();
            TextUtil.sendMessage(sender, "<yellow>Radio actual del borde: <gold>±" + (int) current);
            TextUtil.sendMessage(sender, "<gray>Uso: /uhc border <set|shrink> <radio> [tiempo_segundos]");
            return;
        }

        String action = args[1].toLowerCase();
        if (action.equals("set")) {
            if (args.length < 3) {
                TextUtil.sendMessage(sender, "<red>Uso: /uhc border set <radio>");
                return;
            }
            try {
                double radius = Double.parseDouble(args[2]);
                plugin.getWorldManager().getBorderController().resetBorder(radius);
                TextUtil.sendMessage(sender, "<green>Borde fijado en ±" + (int) radius + " bloques.");
            } catch (NumberFormatException e) {
                TextUtil.sendMessage(sender, "<red>El radio debe ser un número válido.");
            }
        } else if (action.equals("shrink")) {
            if (args.length < 3) {
                TextUtil.sendMessage(sender, "<red>Uso: /uhc border shrink <radio> [segundos]");
                return;
            }
            try {
                double radius = Double.parseDouble(args[2]);
                long seconds = args.length >= 4 ? Long.parseLong(args[3]) : 60;
                plugin.getWorldManager().getBorderController().shrinkTo(radius, seconds);
            } catch (NumberFormatException e) {
                TextUtil.sendMessage(sender, "<red>Los parámetros deben ser números válidos.");
            }
        }
    }

    private void handleKit(CommandSender sender, String[] args) {
        if (args.length < 2) {
            if (sender instanceof Player p) {
                plugin.getKitManagerGUI().open(p);
            } else {
                TextUtil.sendMessage(sender, "<gray>Kits disponibles: <yellow>" + String.join(", ", plugin.getKitManager().getAllKits().stream().map(Kit::getName).toList()));
                TextUtil.sendMessage(sender, "<gray>Uso: /uhc kit <gui|create|delete|setstarter> [nombre] [delay]");
            }
            return;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "gui", "manage" -> {
                if (sender instanceof Player p) {
                    plugin.getKitManagerGUI().open(p);
                } else {
                    TextUtil.sendMessage(sender, "<red>Solo jugadores pueden abrir el gestor de kits.");
                }
            }
            case "create" -> {
                if (!(sender instanceof Player p)) {
                    TextUtil.sendMessage(sender, "<red>Solo jugadores pueden crear kits.");
                    return;
                }
                if (args.length < 3) {
                    TextUtil.sendMessage(sender, "<red>Uso: /uhc kit create <nombre> [delaySegundos]");
                    return;
                }
                String name = args[2];
                int delay = 0;
                if (args.length >= 4) {
                    try {
                        delay = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        TextUtil.sendMessage(sender, "<red>El delay debe ser un número de segundos válido.");
                        return;
                    }
                }
                plugin.getKitManager().createKitFromPlayer(name, p, delay);
            }
            case "edit" -> {
                if (!(sender instanceof Player p)) {
                    TextUtil.sendMessage(sender, "<red>Solo jugadores pueden editar kits.");
                    return;
                }
                if (args.length < 3) {
                    TextUtil.sendMessage(sender, "<red>Uso: /uhc kit edit <nombre>");
                    return;
                }
                Kit k = plugin.getKitManager().getKit(args[2]);
                if (k == null) {
                    TextUtil.sendMessage(sender, "<red>No se encontró ningún kit con ese nombre.");
                    return;
                }
                plugin.getKitEditorGUI().open(p, k);
            }
            case "delete", "remove" -> {
                if (args.length < 3) {
                    TextUtil.sendMessage(sender, "<red>Uso: /uhc kit delete <nombre>");
                    return;
                }
                String name = args[2];
                boolean deleted = plugin.getKitManager().deleteKit(name);
                if (deleted) {
                    plugin.getMessages().send(sender, "kits.deleted", "kit", name);
                } else {
                    plugin.getMessages().send(sender, "kits.not-found", "kit", name);
                }
            }
            case "setstarter" -> {
                if (args.length < 3) {
                    TextUtil.sendMessage(sender, "<red>Uso: /uhc kit setstarter <nombre>");
                    return;
                }
                String name = args[2];
                Kit kit = plugin.getKitManager().getKit(name);
                if (kit == null) {
                    plugin.getMessages().send(sender, "kits.not-found", "kit", name);
                    return;
                }
                plugin.getKitManager().setStarterKitName(name);
                plugin.getMessages().send(sender, "kits.starter-set", "kit", name);
            }
            case "list" -> {
                TextUtil.sendMessage(sender, "<gold><bold>Kits configurados:");
                for (Kit k : plugin.getKitManager().getAllKits()) {
                    String status = k.isEnabled() ? "<green>[ON]" : "<red>[OFF]";
                    String starter = k.getName().equalsIgnoreCase(plugin.getKitManager().getStarterKitName()) ? " <gold>(Starter)" : "";
                    String delay = k.getDelaySeconds() > 0 ? " <gray>(Delay: " + TimeUtil.formatSeconds(k.getDelaySeconds()) + ")" : " <gray>(Inicio)";
                    TextUtil.sendMessage(sender, "  <yellow>• " + k.getName() + " " + status + starter + delay);
                }
            }
            default -> {
                TextUtil.sendMessage(sender, "<red>Subcomando de kit desconocido. Usa: gui, create, edit, delete, setstarter, list");
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        TextUtil.sendMessage(sender, "<gold><bold>=== YangUHC Comandos de Administración ===");
        TextUtil.sendMessage(sender, "<yellow>/uhc config <gray>- Configuración interactiva previa (Borde, Kits, PvP, Teams).");
        TextUtil.sendMessage(sender, "<yellow>/uhc start <gray>- Inicia la partida UHC.");
        TextUtil.sendMessage(sender, "<yellow>/uhc stop <gray>- Detiene la partida actual.");
        TextUtil.sendMessage(sender, "<yellow>/uhc forcepvp <gray>- Activa el PvP inmediatamente.");
        TextUtil.sendMessage(sender, "<yellow>/uhc scenarios <gray>- Abre el menú de escenarios.");
        TextUtil.sendMessage(sender, "<yellow>/uhc kit [gui|create|edit|delete|setstarter|list] <gray>- Gestiona los kits.");
        TextUtil.sendMessage(sender, "<yellow>/uhc border <set|shrink> <radio> [tiempo] <gray>- Controla el borde.");
        TextUtil.sendMessage(sender, "<yellow>/uhc revive <jugador> <gray>- Revive a un jugador eliminado.");
        TextUtil.sendMessage(sender, "<yellow>/uhc setlobby <gray>- Establece el punto de aparición del lobby.");
        TextUtil.sendMessage(sender, "<yellow>/uhc reload <gray>- Recarga los archivos de configuración.");
        TextUtil.sendMessage(sender, "<gold>==========================================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("yanguhc.admin") && !sender.hasPermission("yanguhc.host") && !sender.isOp()) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subs = Arrays.asList("config", "start", "stop", "scenarios", "forcepvp", "border", "kit", "kits", "setlobby", "setspawn", "revive", "reload");
            return filter(subs, args[0]);
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("kit") || args[0].equalsIgnoreCase("kits"))) {
            return filter(Arrays.asList("gui", "create", "edit", "delete", "setstarter", "list"), args[1]);
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("kit") || args[0].equalsIgnoreCase("kits"))) {
            String act = args[1].toLowerCase();
            if (act.equals("delete") || act.equals("setstarter") || act.equals("edit")) {
                List<String> names = plugin.getKitManager().getAllKits().stream().map(Kit::getName).toList();
                return filter(names, args[2]);
            }
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("border")) {
            return filter(Arrays.asList("set", "shrink"), args[1]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("revive")) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!plugin.getGameManager().isAlive(p)) {
                    names.add(p.getName());
                }
            }
            return filter(names, args[1]);
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String query) {
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(query.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }
}
