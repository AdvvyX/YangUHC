package net.yanguhc.kit;

import net.yanguhc.YangUHC;
import net.yanguhc.util.ItemBuilder;
import net.yanguhc.util.TextUtil;
import net.yanguhc.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class KitManagerGUI implements Listener, InventoryHolder {

    private final YangUHC plugin;
    private final Inventory inventory;
    private final NamespacedKey kitKey;

    public KitManagerGUI(YangUHC plugin) {
        this.plugin = plugin;
        this.kitKey = new NamespacedKey(plugin, "kit_id");
        this.inventory = Bukkit.createInventory(this, 54, TextUtil.parse("<gold><bold>Gestor de Kits UHC</bold>"));
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        refresh();
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }

    public void refresh() {
        inventory.clear();
        int slot = 10;
        int activeImmediate = 0;
        int activeDelayed = 0;
        int disabledCount = 0;

        for (Kit kit : plugin.getKitManager().getAllKits()) {
            boolean enabled = kit.isEnabled();
            if (enabled) {
                if (kit.getDelaySeconds() == 0) activeImmediate++;
                else activeDelayed++;
            } else {
                disabledCount++;
            }

            Material icon = Material.CHEST;
            if (kit.getChestplate() != null && kit.getChestplate().getType() != Material.AIR) {
                icon = kit.getChestplate().getType();
            } else if (!kit.getItems().isEmpty()) {
                icon = kit.getItems().get(0).getType();
            }

            List<String> lore = new ArrayList<>();
            lore.add("<gray>Estado:");
            if (enabled) {
                lore.add("<green><bold>✔ ACTIVADO (Se entregará en la partida)</bold>");
            } else {
                lore.add("<red><bold>✖ DESACTIVADO (No se entregará)</bold>");
            }
            lore.add("");
            lore.add("<gray>Detalles:");
            lore.add("<white>• Ítems: <yellow>" + kit.getItems().size());
            lore.add("<white>• Armadura: " + formatArmor(kit));
            lore.add("<white>• Entrega: <gold>" + (kit.getDelaySeconds() == 0 ? "Al inicio (0s)" : TimeUtil.formatSeconds(kit.getDelaySeconds())));
            lore.add("");
            lore.add("<yellow>▶ Click Izquierdo: <white>" + (enabled ? "<red>Desactivar" : "<green>Activar") + " entrega");
            lore.add("<aqua>▶ Click Derecho: <white>Editar contenido en menú");
            lore.add("<light_purple>▶ Shift + Click Izq: <white>Previsualizar");
            lore.add("<red>▶ Shift + Click Der: <white>Eliminar kit");

            ItemBuilder builder = new ItemBuilder(icon)
                    .name((enabled ? "<green><bold>✔ " : "<red><bold>✖ ") + kit.getName() + "</bold>")
                    .lore(lore);

            if (enabled) {
                builder.enchant(Enchantment.UNBREAKING, 1).flags(ItemFlag.HIDE_ENCHANTS);
            }

            ItemStack item = builder.build();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(kitKey, PersistentDataType.STRING, kit.getName().toLowerCase());
                item.setItemMeta(meta);
            }

            inventory.setItem(slot, item);
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
            if (slot >= 44) break;
        }

        ItemStack filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }

        ItemStack disableAllBtn = new ItemBuilder(Material.REDSTONE_BLOCK)
                .name("<red><bold>✖ Desactivar Todos los Kits</bold>")
                .lore(
                        "<gray>Ningún kit se entregará durante",
                        "<gray>la partida de UHC.",
                        "",
                        "<yellow>Haz click para desactivar todos."
                )
                .build();
        inventory.setItem(45, disableAllBtn);

        ItemStack enableAllBtn = new ItemBuilder(Material.EMERALD_BLOCK)
                .name("<green><bold>✔ Activar Todos los Kits</bold>")
                .lore(
                        "<gray>Activa todos los kits configurados",
                        "<gray>para ser entregados en la partida.",
                        "",
                        "<yellow>Haz click para activar todos."
                )
                .build();
        inventory.setItem(46, enableAllBtn);

        ItemStack createBtn = new ItemBuilder(Material.EMERALD)
                .name("<green><bold>➕ Crear Nuevo Kit</bold>")
                .lore(
                        "<gray>Crea un kit nuevo clonando tu",
                        "<gray>inventario y armadura actuales.",
                        "",
                        "<yellow>Haz click para crear."
                )
                .build();
        inventory.setItem(49, createBtn);

        ItemStack summaryBtn = new ItemBuilder(Material.BOOK)
                .name("<gold><bold>Información de Kits en Partida</bold>")
                .lore(
                        "<gray>Resumen de configuración actual:",
                        "<green>• Kits activos al inicio (0s): <white>" + activeImmediate,
                        "<aqua>• Kits activos con demora: <white>" + activeDelayed,
                        "<red>• Kits desactivados: <white>" + disabledCount,
                        "",
                        "<gray>Los kits <green>ACTIVADOS</green> se entregarán",
                        "<gray>automáticamente al iniciar la partida o",
                        "<gray>al cumplirse su tiempo programado."
                )
                .build();
        inventory.setItem(53, summaryBtn);
    }

    private String formatArmor(Kit kit) {
        StringBuilder sb = new StringBuilder();
        sb.append(kit.getHelmet() != null ? "<green>H" : "<red>H").append(" ");
        sb.append(kit.getChestplate() != null ? "<green>C" : "<red>C").append(" ");
        sb.append(kit.getLeggings() != null ? "<green>L" : "<red>L").append(" ");
        sb.append(kit.getBoots() != null ? "<green>B" : "<red>B");
        return sb.toString();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof KitManagerGUI)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        if (!player.isOp() && !player.hasPermission("yanguhc.admin") && !player.hasPermission("yanguhc.host")) {
            plugin.getMessages().send(player, "general.no-permission");
            return;
        }

        if (event.getSlot() == 45 && clicked.getType() == Material.REDSTONE_BLOCK) {
            plugin.getKitManager().setAllKitsEnabled(false);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.8f);
            TextUtil.sendMessage(player, "<red>Todos los kits han sido desactivados para la partida.");
            refresh();
            return;
        }

        if (event.getSlot() == 46 && clicked.getType() == Material.EMERALD_BLOCK) {
            plugin.getKitManager().setAllKitsEnabled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
            TextUtil.sendMessage(player, "<green>Todos los kits han sido activados para la partida.");
            refresh();
            return;
        }

        if (event.getSlot() == 49 && clicked.getType() == Material.EMERALD) {
            String newKitName = "kit_" + (plugin.getKitManager().getAllKits().size() + 1);
            plugin.getKitManager().createKitFromPlayer(newKitName, player, 0);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            refresh();
            return;
        }

        if (clicked.hasItemMeta()) {
            String kitId = clicked.getItemMeta().getPersistentDataContainer().get(kitKey, PersistentDataType.STRING);
            if (kitId != null) {
                Kit kit = plugin.getKitManager().getKit(kitId);
                if (kit == null) return;

                if (event.getClick() == ClickType.SHIFT_RIGHT) {

                    plugin.getKitManager().deleteKit(kit.getName());
                    plugin.getMessages().send(player, "kits.deleted", "kit", kit.getName());
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                    refresh();
                } else if (event.getClick() == ClickType.MIDDLE || event.getClick() == ClickType.SHIFT_LEFT) {

                    plugin.getKitGUI().openPreview(player, kit);
                } else if (event.isRightClick()) {

                    plugin.getKitEditorGUI().open(player, kit);
                } else if (event.isLeftClick()) {

                    boolean newState = !kit.isEnabled();
                    kit.setEnabled(newState);
                    plugin.getKitManager().saveKit(kit);

                    if (newState) {
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
                        plugin.getMessages().send(player, "kits.enabled", "kit", kit.getName());
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.9f);
                        plugin.getMessages().send(player, "kits.disabled", "kit", kit.getName());
                    }
                    refresh();
                }
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
