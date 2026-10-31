package net.yanguhc.kit;

import net.yanguhc.YangUHC;
import net.yanguhc.util.ItemBuilder;
import net.yanguhc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class KitSelectorGUI implements Listener, InventoryHolder {

    private final YangUHC plugin;
    private final Inventory inventory;
    private final NamespacedKey kitKey;

    public KitSelectorGUI(YangUHC plugin) {
        this.plugin = plugin;
        this.kitKey = new NamespacedKey(plugin, "kit_select_id");
        this.inventory = Bukkit.createInventory(this, 54, TextUtil.parse("<gold><bold>Seleccionar Kit Inicial</bold>"));
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        refresh();
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }

    public void refresh() {
        inventory.clear();
        String currentSelected = plugin.getKitManager().getStarterKitName();

        int slot = 10;
        for (Kit kit : plugin.getKitManager().getAllKits()) {
            boolean isSelected = kit.getName().equalsIgnoreCase(currentSelected);

            Material icon = Material.CHEST;
            if (kit.getChestplate() != null && kit.getChestplate().getType() != Material.AIR) {
                icon = kit.getChestplate().getType();
            } else if (!kit.getItems().isEmpty()) {
                icon = kit.getItems().get(0).getType();
            }

            List<String> lore = new ArrayList<>();
            lore.add("<gray>Objetos en kit: <white>" + kit.getItems().size());
            lore.add("<gray>Tiene armadura: " + (hasArmor(kit) ? "<green>Sí" : "<red>No"));
            lore.add("");
            if (isSelected) {
                lore.add("<green><bold>✔ SELECCIONADO ACTUALMENTE</bold>");
            } else {
                lore.add("<yellow>▶ Clic para seleccionar este kit");
            }

            ItemStack item = new ItemBuilder(icon)
                    .name((isSelected ? "<green><bold>" : "<yellow><bold>") + kit.getName())
                    .lore(lore)
                    .glow(isSelected)
                    .build();

            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(kitKey, PersistentDataType.STRING, kit.getName());
            item.setItemMeta(meta);

            inventory.setItem(slot, item);
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
            if (slot >= 44) break;
        }

        inventory.setItem(49, new ItemBuilder(Material.ARROW)
                .name("<red><bold>Volver a Configuración</bold>")
                .lore("<gray>Regresa al menú principal de /uhc config.")
                .build());
    }

    private boolean hasArmor(Kit kit) {
        return (kit.getHelmet() != null && kit.getHelmet().getType() != Material.AIR) ||
                (kit.getChestplate() != null && kit.getChestplate().getType() != Material.AIR) ||
                (kit.getLeggings() != null && kit.getLeggings().getType() != Material.AIR) ||
                (kit.getBoots() != null && kit.getBoots().getType() != Material.AIR);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        if (clicked.getType() == Material.ARROW) {
            plugin.getConfigGUI().open(player);
            return;
        }

        String kitName = clicked.getItemMeta().getPersistentDataContainer().get(kitKey, PersistentDataType.STRING);
        if (kitName != null) {
            plugin.getKitManager().setStarterKitName(kitName);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
            TextUtil.sendMessage(player, "<green>Kit inicial establecido en: <yellow>" + kitName);
            plugin.getConfigGUI().open(player);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
