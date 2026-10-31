package net.yanguhc.game;

import net.yanguhc.YangUHC;
import net.yanguhc.kit.Kit;
import net.yanguhc.util.ItemBuilder;
import net.yanguhc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigGUI implements Listener, InventoryHolder {

    private final YangUHC plugin;
    private final Inventory inventory;

    private static final int[] GRACE_OPTIONS = {1200, 900, 600, 300, 0};

    public ConfigGUI(YangUHC plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, 27, TextUtil.parse("<gold><bold>Iniciar Partida UHC</bold>"));
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        refresh();
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }

    public void refresh() {
        inventory.clear();

        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        boolean borderShrink = plugin.getWorldManager().getBorderController().isShrinkEnabled();
        ItemStack borderItem = new ItemBuilder(borderShrink ? Material.BEACON : Material.BARRIER)
                .name("<gold><bold>Reducción de Borde</bold>")
                .lore(
                        "<gray>Estado: " + (borderShrink ? "<green><bold>SÍ</bold>" : "<red><bold>NO</bold>"),
                        borderShrink ? "<gray>El borde se encogerá periódicamente." : "<gray>El borde permanecerá fijo sin encogerse.",
                        "",
                        "<yellow>▶ Clic para alternar (Sí/No)"
                )
                .glow(borderShrink)
                .build();
        inventory.setItem(10, borderItem);

        String currentKit = plugin.getKitManager().getStarterKitName();
        Kit kit = plugin.getKitManager().getStarterKit();
        int kitItems = kit != null ? kit.getItems().size() : 0;
        ItemStack kitItem = new ItemBuilder(Material.CHEST)
                .name("<gold><bold>Kit Inicial</bold>")
                .lore(
                        "<gray>Kit seleccionado: <yellow><bold>" + (kit != null ? kit.getName() : currentKit) + "</bold>",
                        "<gray>Objetos en kit: <white>" + kitItems,
                        "<gray>Total kits creados: <aqua>" + plugin.getKitManager().getAllKits().size(),
                        "",
                        "<yellow>▶ Clic Izquierdo: <white>Siguiente kit",
                        "<yellow>▶ Clic Derecho: <white>Abrir selector de kits"
                )
                .build();
        inventory.setItem(12, kitItem);

        int graceDuration = plugin.getGameManager().getGracePeriodDuration();
        boolean graceActive = graceDuration > 0;
        ItemStack graceItem = new ItemBuilder(Material.CLOCK)
                .name("<gold><bold>Tiempo antes de PvP</bold>")
                .lore(
                        "<gray>Estado: " + (graceActive ? "<green><bold>SÍ (<gold>" + (graceDuration / 60) + "m<green>)</bold>" : "<red><bold>NO (Inmediato)</bold>"),
                        graceActive ? "<gray>PvP desactivado durante los primeros " + (graceDuration / 60) + " minutos." : "<gray>PvP habilitado inmediatamente al iniciar.",
                        "",
                        "<gray>Opciones: <white>20m ➜ 15m ➜ 10m ➜ 5m ➜ NO",
                        "",
                        "<yellow>▶ Clic para cambiar tiempo de gracia"
                )
                .glow(graceActive)
                .build();
        inventory.setItem(14, graceItem);

        int teamSize = plugin.getTeamManager().getMaxTeamSize();
        ItemStack teamItem = new ItemBuilder(Material.SHIELD, Math.min(64, Math.max(1, teamSize)))
                .name("<gold><bold>Modalidad de Equipos</bold>")
                .lore(
                        "<gray>Modalidad: <aqua><bold>" + formatTeamMode(teamSize) + "</bold>",
                        "<dark_gray>Al iniciar la partida, los equipos se formarán automáticamente.",
                        "",
                        "<yellow>▶ Clic Izquierdo: <white>+1 Jugador",
                        "<yellow>▶ Clic Derecho: <white>-1 Jugador",
                        "<yellow>▶ Shift + Clic: <white>Restablecer a Solos (1)",
                        "",
                        "<gray>O usa: <gold>/uhc teamsize <número>"
                )
                .build();
        inventory.setItem(16, teamItem);

        ItemStack startItem = new ItemBuilder(Material.EMERALD_BLOCK)
                .name("<green><bold>¡Comenzar UHC Ahora!</bold>")
                .lore(
                        "<gray>Haz clic aquí para iniciar la partida con",
                        "<gray>las configuraciones seleccionadas arriba.",
                        "",
                        "<green>▶ Clic para iniciar partida"
                )
                .build();
        inventory.setItem(22, startItem);
    }

    private String formatTeamMode(int size) {
        return switch (size) {
            case 1 -> "Solos (1 Jugador)";
            case 2 -> "Dúos (2 Jugadores)";
            case 3 -> "Tríos (3 Jugadores)";
            case 4 -> "Cuartetos (4 Jugadores)";
            case 5 -> "Quintetos (5 Jugadores)";
            default -> "Equipos de " + size + " Jugadores";
        };
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getRawSlot();

        if (slot == 10) {
            boolean current = plugin.getWorldManager().getBorderController().isShrinkEnabled();
            plugin.getWorldManager().getBorderController().setShrinkEnabled(!current);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            refresh();
        } else if (slot == 12) {
            if (event.getClick() == ClickType.RIGHT) {
                plugin.getKitSelectorGUI().open(player);
            } else {
                List<Kit> kits = new ArrayList<>(plugin.getKitManager().getAllKits());
                if (!kits.isEmpty()) {
                    String currentKit = plugin.getKitManager().getStarterKitName();
                    int currentIndex = -1;
                    for (int i = 0; i < kits.size(); i++) {
                        if (kits.get(i).getName().equalsIgnoreCase(currentKit)) {
                            currentIndex = i;
                            break;
                        }
                    }
                    int nextIndex = (currentIndex + 1) % kits.size();
                    String nextKit = kits.get(nextIndex).getName();
                    plugin.getKitManager().setStarterKitName(nextKit);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                    refresh();
                }
            }
        } else if (slot == 14) {
            int current = plugin.getGameManager().getGracePeriodDuration();
            int next = GRACE_OPTIONS[0];
            for (int i = 0; i < GRACE_OPTIONS.length; i++) {
                if (GRACE_OPTIONS[i] == current) {
                    next = GRACE_OPTIONS[(i + 1) % GRACE_OPTIONS.length];
                    break;
                }
            }
            plugin.getGameManager().setGracePeriodDuration(next);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            refresh();
        } else if (slot == 16) {
            int currentSize = plugin.getTeamManager().getMaxTeamSize();
            int nextSize;
            if (event.isShiftClick()) {
                nextSize = 1;
            } else if (event.isRightClick()) {
                nextSize = currentSize <= 1 ? 10 : currentSize - 1;
            } else {
                nextSize = currentSize >= 10 ? 1 : currentSize + 1;
            }
            plugin.getTeamManager().setMaxTeamSize(nextSize);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            refresh();
        } else if (slot == 22) {
            player.closeInventory();
            GameState state = plugin.getGameManager().getState();
            if (state != GameState.LOBBY) {
                plugin.getMessages().send(player, "general.game-already-started");
                return;
            }
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                plugin.getMessages().send(player, "general.no-players-online");
                return;
            }
            boolean started = plugin.getGameManager().startGame();
            if (started) {
                TextUtil.sendMessage(player, "<green>Iniciando proceso de scatter y partida...");
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
