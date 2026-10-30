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
        String starterName = plugin.getKitManager().getStarterKitName();
        int slot = 10;

        for (Kit kit : plugin.getKitManager().getAllKits()) {
            boolean isStarter = kit.getName().equalsIgnoreCase(starterName);

            Material icon = Material.CHEST;
            if (kit.getChestplate() != null && kit.getChestplate().getType() != Material.AIR) {
                icon = kit.getChestplate().getType();
            } else if (!kit.getItems().isEmpty()) {
                icon = kit.getItems().get(0).getType();
            }

            List<String> lore = new ArrayList<>();
            lore.add("<gray>Contenido:");
            lore.add("<white>• Ítems: <yellow>" + kit.getItems().size());
            lore.add("<white>• Armadura: " + formatArmor(kit));
            lore.add("<white>• Entrega: <gold>" + (kit.getDelaySeconds() == 0 ? "Al inicio (0s)" : TimeUtil.formatSeconds(kit.getDelaySeconds())));
            lore.add("");
            if (isStarter) {
                lore.add("<green><bold>⭐ KIT INICIAL ACTIVO</bold>");
            } else {
                lore.add("<gray><italic>Click derecho para marcar como inicial");
            }
            lore.add("");
            lore.add("<aqua>▶ Click Izquierdo: <white>Editar kit en menú interactivo");
            lore.add("<yellow>▶ Click Derecho: <white>Hacer Kit Inicial");
            lore.add("<red>▶ Shift + Click Der: <white>Eliminar Kit");

            ItemBuilder builder = new ItemBuilder(icon)
                    .name((isStarter ? "<green><bold>" : "<gold><bold>") + "Kit: " + kit.getName() + "</bold>")
                    .lore(lore);

            if (isStarter) {
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

        // Fillers
        ItemStack filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }

        // Action Buttons in bottom row
        ItemStack createBtn = new ItemBuilder(Material.EMERALD)
                .name("<green><bold>➕ Crear Nuevo Kit</bold>")
                .lore(
                        "<gray>Crea un kit nuevo clonando tu",
                        "<gray>inventario y armadura actuales.",
                        "",
                        "<yellow>Haz click para crear."
                )
                .build();
        inventory.setItem(48, createBtn);

        Kit starterKit = plugin.getKitManager().getStarterKit();
        int currentDelay = starterKit != null ? starterKit.getDelaySeconds() : 0;
        ItemStack delayBtn = new ItemBuilder(Material.CLOCK)
                .name("<gold><bold>⏱ Demora del Kit Inicial</bold>")
                .lore(
                        "<gray>Tiempo actual: <yellow>" + (currentDelay == 0 ? "Inmediato (0s)" : currentDelay + "s"),
                        "",
                        "<green>▶ Click Izquierdo: <white>+30 segundos",
                        "<red>▶ Click Derecho: <white>Restablecer a 0s (Inmediato)"
                )
                .build();
        inventory.setItem(50, delayBtn);
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
            TextUtil.sendMessage(player, "<red>Solo administradores u operadores pueden gestionar kits.");
            return;
        }

        // Click on "Crear Nuevo Kit"
        if (clicked.getType() == Material.EMERALD) {
            String newKitName = "kit_" + (plugin.getKitManager().getAllKits().size() + 1);
            plugin.getKitManager().createKitFromPlayer(newKitName, player, 0);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            refresh();
            return;
        }

        // Click on "Demora del Kit Inicial"
        if (clicked.getType() == Material.CLOCK) {
            Kit starter = plugin.getKitManager().getStarterKit();
            if (starter != null) {
                if (event.isRightClick()) {
                    starter.setDelaySeconds(0);
                } else {
                    int next = starter.getDelaySeconds() + 30;
                    if (next > 1800) next = 0;
                    starter.setDelaySeconds(next);
                }
                plugin.getKitManager().saveKit(starter);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                refresh();
            }
            return;
        }

        // Click on a Kit Item
        if (clicked.hasItemMeta()) {
            String kitId = clicked.getItemMeta().getPersistentDataContainer().get(kitKey, PersistentDataType.STRING);
            if (kitId != null) {
                Kit kit = plugin.getKitManager().getKit(kitId);
                if (kit == null) return;

                if (event.getClick() == ClickType.SHIFT_RIGHT) {
                    // Delete Kit
                    plugin.getKitManager().deleteKit(kit.getName());
                    TextUtil.sendMessage(player, "<red>El kit <bold>" + kit.getName() + "</bold> ha sido eliminado.");
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                    refresh();
                } else if (event.isRightClick()) {
                    // Set as Starter
                    plugin.getKitManager().setStarterKitName(kit.getName());
                    TextUtil.sendMessage(player, "<green>El kit <bold>" + kit.getName() + "</bold> es ahora el kit inicial de la partida.");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    refresh();
                } else if (event.isLeftClick()) {
                    // Open Kit Interactive Editor
                    plugin.getKitEditorGUI().open(player, kit);
                }
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
