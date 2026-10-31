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
                    plugin.getMessages().send(sender, "general.player-already-alive", "player", target.getName());
                    return true;
                }
                plugin.getGameManager().revivePlayer(target);
                plugin.getMessages().send(sender, "game.player-revived-broadcast", "player", target.getName());
            }
            case "reload" -> {
                plugin.getMessages().reload();
                plugin.getAnimationManager().reload();
                plugin.getScoreboardManager().reload();
                plugin.getKitManager().loadKits();
                plugin.reloadConfig();
                plugin.getMessages().send(sender, "general.config-reloaded");
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleBorder(CommandSender sender, String[] args) {
        if (args.length < 2) {
            TextUtil.sendMessage(sender, "<yellow>Uso: /uhc border <shrink|set> [parámetros]");
            return;
        }
        String action = args[1].toLowerCase();
        if (action.equals("shrink")) {
            if (!plugin.getGameManager().getState().isGameActive()) {
                plugin.getMessages().send(sender, "general.game-not-active");
                return;
            }
            if (args.length >= 4) {
                double radius = Double.parseDouble(args[2]);
                long seconds = Long.parseLong(args[3]);
                plugin.getWorldManager().getBorderController().shrinkTo(radius, seconds);
            } else {
                plugin.getWorldManager().getBorderController().shrinkNextStage(120);
            }
        } else if (action.equals("set") && args.length >= 3) {
            double radius = Double.parseDouble(args[2]);
            plugin.getWorldManager().getBorderController().resetBorder(radius);
            plugin.getMessages().broadcast("border.set", "radius", (int) radius);
        }
    }

    private void handleKit(CommandSender sender, String[] args) {

        if (args.length < 2) {
            if (sender instanceof Player p) {
                plugin.getKitManagerGUI().open(p);
                return;
            }
            TextUtil.sendMessage(sender, "<yellow>Uso: /uhc kit <enable|disable|toggle|create|edit|preview|delete|list> [argumentos]");
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
        } else if (sub.equals("enable")) {
            if (args.length < 3) {
                TextUtil.sendMessage(sender, "<red>Uso: /uhc kit enable <nombre>");
                return;
            }
            Kit kit = plugin.getKitManager().getKit(args[2]);
            if (kit == null) {
                plugin.getMessages().send(sender, "kits.not-found");
                return;
            }
            kit.setEnabled(true);
            plugin.getKitManager().saveKit(kit);
            plugin.getMessages().send(sender, "kits.enabled", "kit", kit.getName());
        } else if (sub.equals("disable")) {
            if (args.length < 3) {
                TextUtil.sendMessage(sender, "<red>Uso: /uhc kit disable <nombre>");
                return;
            }
            Kit kit = plugin.getKitManager().getKit(args[2]);
            if (kit == null) {
                plugin.getMessages().send(sender, "kits.not-found");
                return;
            }
            kit.setEnabled(false);
            plugin.getKitManager().saveKit(kit);
            plugin.getMessages().send(sender, "kits.disabled", "kit", kit.getName());
        } else if (sub.equals("toggle")) {
            if (args.length < 3) {
                TextUtil.sendMessage(sender, "<red>Uso: /uhc kit toggle <nombre>");
                return;
            }
            Kit kit = plugin.getKitManager().getKit(args[2]);
            if (kit == null) {
                plugin.getMessages().send(sender, "kits.not-found");
                return;
            }
            kit.setEnabled(!kit.isEnabled());
            plugin.getKitManager().saveKit(kit);
            plugin.getMessages().send(sender, kit.isEnabled() ? "kits.enabled" : "kits.disabled", "kit", kit.getName());
        } else if (sub.equals("delete")) {
            if (args.length < 3) {
                TextUtil.sendMessage(sender, "<red>Uso: /uhc kit delete <nombre>");
                return;
            }
            if (plugin.getKitManager().deleteKit(args[2])) {
                plugin.getMessages().send(sender, "kits.deleted", "kit", args[2]);
            } else {
                plugin.getMessages().send(sender, "kits.not-found");
            }
        } else if ((sub.equals("edit") || sub.equals("preview")) && sender instanceof Player p) {
            if (args.length < 3) {
                TextUtil.sendMessage(p, "<red>Uso: /uhc kit edit <nombre>");
                return;
            }
            Kit kit = plugin.getKitManager().getKit(args[2]);
            if (kit == null) {
                plugin.getMessages().send(p, "kits.not-found");
                return;
            }
            if (sub.equals("edit")) {
                plugin.getKitEditorGUI().open(p, kit);
            } else {
                plugin.getKitGUI().openPreview(p, kit);
            }
        } else if (sub.equals("list")) {
            TextUtil.sendMessage(sender, "<gold><bold>Kits Configurados YangUHC:</bold>");
            for (Kit k : plugin.getKitManager().getAllKits()) {
                String state = k.isEnabled() ? "<green>[ACTIVADO]</green>" : "<red>[DESACTIVADO]</red>";
                String delayStr = k.getDelaySeconds() == 0 ? "Al inicio (0s)" : TimeUtil.formatSeconds(k.getDelaySeconds());
                TextUtil.sendMessage(sender, "<yellow>- " + k.getName() + " " + state + " <gray>(Entrega: " + delayStr + ", Ítems: " + k.getItems().size() + ")");
            }
        } else if (sender instanceof Player p) {
            plugin.getKitManagerGUI().open(p);
        }
    }

    private void sendHelp(CommandSender sender) {
        TextUtil.sendMessage(sender, "<gold><bold>--- YangUHC Comandos Admin ---</bold>");
        TextUtil.sendMessage(sender, "<yellow>/uhc start <gray>- Inicia la partida y el scatter");
        TextUtil.sendMessage(sender, "<yellow>/uhc stop <gray>- Detiene la partida");
        TextUtil.sendMessage(sender, "<yellow>/uhc scenarios <gray>- Abre el gestor de escenarios");
        TextUtil.sendMessage(sender, "<yellow>/uhc forcepvp <gray>- Activa el PvP de inmediato");
        TextUtil.sendMessage(sender, "<yellow>/uhc border <shrink|set> <gray>- Controla el borde del mundo");
        TextUtil.sendMessage(sender, "<yellow>/uhc kits <gray>- Abre el menú interactivo para elegir y activar kits");
        TextUtil.sendMessage(sender, "<yellow>/uhc setlobby <gray>- Establece el spawn del lobby en tu ubicación");
        TextUtil.sendMessage(sender, "<yellow>/uhc revive <jugador> <gray>- Revive a un jugador");
        TextUtil.sendMessage(sender, "<yellow>/uhc reload <gray>- Recarga las configuraciones y messages.yml");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("yanguhc.admin") && !sender.isOp()) return Collections.emptyList();

        if (args.length == 1) {
            return filter(Arrays.asList("start", "stop", "scenarios", "forcepvp", "border", "kit", "kits", "setlobby", "setspawn", "revive", "reload"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("border")) {
            return filter(Arrays.asList("set", "shrink"), args[1]);
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("kit") || args[0].equalsIgnoreCase("kits"))) {
            return filter(Arrays.asList("enable", "disable", "toggle", "create", "edit", "preview", "delete", "list"), args[1]);
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("kit") || args[0].equalsIgnoreCase("kits"))) {
            String sub = args[1].toLowerCase();
            if (Arrays.asList("enable", "disable", "toggle", "edit", "preview", "delete").contains(sub)) {
                List<String> kitNames = new ArrayList<>();
                for (Kit k : plugin.getKitManager().getAllKits()) {
                    kitNames.add(k.getName());
                }
                return filter(kitNames, args[2]);
            }
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
