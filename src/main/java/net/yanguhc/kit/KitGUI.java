package net.yanguhc.kit;

import net.yanguhc.YangUHC;
import net.yanguhc.util.ItemBuilder;
import net.yanguhc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class KitGUI implements Listener, InventoryHolder {

    private final YangUHC plugin;

    public KitGUI(YangUHC plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openPreview(Player player, Kit kit) {
        if (kit == null) return;

        Inventory inv = Bukkit.createInventory(this, 54, TextUtil.parse("<gold>Kit Preview: " + kit.getName()));

        // Display Armor in top row
        inv.setItem(0, kit.getHelmet());
        inv.setItem(1, kit.getChestplate());
        inv.setItem(2, kit.getLeggings());
        inv.setItem(3, kit.getBoots());
        inv.setItem(4, kit.getOffHand());

        // Info item in slot 8
        ItemStack info = new ItemBuilder(Material.PAPER)
                .name("<yellow><bold>Información del Kit</bold>")
                .lore(
                        "<gray>Nombre: <white>" + kit.getName(),
                        "<gray>Demora de entrega: <gold>" + kit.getDelaySeconds() + "s"
                )
                .build();
        inv.setItem(8, info);

        // Separator
        ItemStack sep = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 9; i < 18; i++) {
            inv.setItem(i, sep);
        }

        // Contents
        int slot = 18;
        for (ItemStack item : kit.getItems()) {
            if (slot >= 54) break;
            inv.setItem(slot++, item);
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof KitGUI) {
            event.setCancelled(true);
        }
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
