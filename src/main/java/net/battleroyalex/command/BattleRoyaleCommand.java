package net.battleroyalex.command;

import net.battleroyalex.BattleRoyaleX;
import net.battleroyalex.game.GameState;
import net.battleroyalex.kit.Kit;
import net.battleroyalex.util.TextUtil;
import net.battleroyalex.util.TimeUtil;
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

public class BattleRoyaleCommand implements CommandExecutor, TabCompleter {

    private final BattleRoyaleX plugin;

    public BattleRoyaleCommand(BattleRoyaleX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("battleroyale.admin") && !sender.hasPermission("battleroyale.host") && !sender.isOp()) {
            plugin.getMessages().send(sender, "general.no-permission");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "start", "config" -> {
                boolean instant = args.length >= 2 && (args[1].equalsIgnoreCase("now") || args[1].equalsIgnoreCase("force") || args[1].equalsIgnoreCase("instant"));
                if (instant || !(sender instanceof Player p)) {
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
                } else {
                    plugin.getConfigGUI().open(p);
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
            case "teamsize", "teams", "team-size" -> {
                if (args.length < 2) {
                    TextUtil.sendMessage(sender, "<yellow>Tamaño actual de equipos: <gold>" + plugin.getTeamManager().getMaxTeamSize() + " jugadores por equipo.");
                    TextUtil.sendMessage(sender, "<gray>Uso: /br teamsize <número>");
                    return true;
                }
                try {
                    int size = Integer.parseInt(args[1]);
                    if (size < 1) {
                        TextUtil.sendMessage(sender, "<red>El tamaño de equipo debe ser al menos 1.");
                        return true;
                    }
                    plugin.getTeamManager().setMaxTeamSize(size);
                    TextUtil.sendMessage(sender, "<green>Tamaño de equipo configurado a: <gold>" + size + " jugadores por equipo.");
                } catch (NumberFormatException e) {
                    TextUtil.sendMessage(sender, "<red>Por favor ingresa un número entero válido.");
                }
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
                    TextUtil.sendMessage(sender, "<red>Uso: /br revive <jugador>");
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
            TextUtil.sendMessage(sender, "<gray>Uso: /br border <set|shrink> <radio> [tiempo_segundos]");
            return;
        }

        String action = args[1].toLowerCase();
        if (action.equals("set")) {
            if (args.length < 3) {
                TextUtil.sendMessage(sender, "<red>Uso: /br border set <radio>");
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
                TextUtil.sendMessage(sender, "<red>Uso: /br border shrink <radio> [segundos]");
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
                TextUtil.sendMessage(sender, "<gray>Uso: /br kit <gui|create|delete|setstarter> [nombre] [delay]");
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
                    TextUtil.sendMessage(sender, "<red>Uso: /br kit create <nombre> [delaySegundos]");
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
                    TextUtil.sendMessage(sender, "<red>Uso: /br kit edit <nombre>");
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
                    TextUtil.sendMessage(sender, "<red>Uso: /br kit delete <nombre>");
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
                    TextUtil.sendMessage(sender, "<red>Uso: /br kit setstarter <nombre>");
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
        TextUtil.sendMessage(sender, "<gold><bold>=== Battle Royale Comandos de Administración ===");
        TextUtil.sendMessage(sender, "<yellow>/br start [now] <gray>- Menú de inicio y configuración (Borde, Kits, PvP, Teams).");
        TextUtil.sendMessage(sender, "<yellow>/br teamsize <número> <gray>- Configura el tamaño de los equipos.");
        TextUtil.sendMessage(sender, "<yellow>/br stop <gray>- Detiene la partida actual.");
        TextUtil.sendMessage(sender, "<yellow>/br forcepvp <gray>- Activa el PvP inmediatamente.");
        TextUtil.sendMessage(sender, "<yellow>/br scenarios <gray>- Abre el menú de escenarios.");
        TextUtil.sendMessage(sender, "<yellow>/br kit [gui|create|edit|delete|setstarter|list] <gray>- Gestiona los kits.");
        TextUtil.sendMessage(sender, "<yellow>/br border <set|shrink> <radio> [tiempo] <gray>- Controla el borde.");
        TextUtil.sendMessage(sender, "<yellow>/br revive <jugador> <gray>- Revive a un jugador eliminado.");
        TextUtil.sendMessage(sender, "<yellow>/br setlobby <gray>- Establece el punto de aparición del lobby.");
        TextUtil.sendMessage(sender, "<yellow>/br reload <gray>- Recarga los archivos de configuración.");
        TextUtil.sendMessage(sender, "<gold>==========================================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("battleroyale.admin") && !sender.hasPermission("battleroyale.host") && !sender.isOp()) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subs = Arrays.asList("start", "teamsize", "stop", "scenarios", "forcepvp", "border", "kit", "kits", "setlobby", "setspawn", "revive", "reload");
            return filter(subs, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            return filter(Collections.singletonList("now"), args[1]);
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
