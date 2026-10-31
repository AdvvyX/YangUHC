package net.battleroyalex.kit;

import net.battleroyalex.BattleRoyaleX;
import net.battleroyalex.util.ItemBuilder;
import net.battleroyalex.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class KitEditorGUI implements Listener {

    private final BattleRoyaleX plugin;
    private final Map<UUID, Kit> editingSessions = new HashMap<>();

    public KitEditorGUI(BattleRoyaleX plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player, Kit kit) {
        if (player == null || kit == null) return;

        editingSessions.put(player.getUniqueId(), kit);
        Inventory inv = Bukkit.createInventory(new KitEditHolder(kit), 54, TextUtil.parse("<dark_purple><bold>Editando:</bold> <yellow>" + kit.getName()));

        inv.setItem(0, kit.getHelmet());
        inv.setItem(1, kit.getChestplate());
        inv.setItem(2, kit.getLeggings());
        inv.setItem(3, kit.getBoots());
        inv.setItem(4, kit.getOffHand());

        ItemStack delayBtn = new ItemBuilder(Material.CLOCK)
                .name("<gold><bold>⏱ Demora de Entrega: <yellow>" + kit.getDelaySeconds() + "s")
                .lore(
                        "<gray>Demora en segundos antes de recibir el kit.",
                        "<yellow>Click para alternar: 0s -> 30s -> 60s -> 300s -> 600s"
                )
                .build();
        inv.setItem(6, delayBtn);

        ItemStack saveBtn = new ItemBuilder(Material.LIME_DYE)
                .name("<green><bold>💾 GUARDAR KIT</bold>")
                .lore(
                        "<gray>Guarda los ítems y armaduras",
                        "<gray>configurados en este menú.",
                        "",
                        "<yellow>Haz click para guardar."
                )
                .build();
        inv.setItem(7, saveBtn);

        ItemStack backBtn = new ItemBuilder(Material.BARRIER)
                .name("<red><bold>🔙 VOLVER</bold>")
                .lore("<gray>Vuelve al gestor de kits.")
                .build();
        inv.setItem(8, backBtn);

        ItemStack sep = new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 9; i < 18; i++) {
            inv.setItem(i, sep);
        }

        int slot = 18;
        for (ItemStack item : kit.getItems()) {
            if (slot >= 54) break;
            inv.setItem(slot++, item != null ? item.clone() : null);
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.7f, 1f);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof KitEditHolder holder)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int rawSlot = event.getRawSlot();

        if (rawSlot >= 9 && rawSlot <= 17) {
            event.setCancelled(true);
            return;
        }

        if (rawSlot == 8) {
            event.setCancelled(true);
            saveKitFromInventory(player, event.getInventory(), holder.getKit());
            plugin.getKitManagerGUI().open(player);
            return;
        }

        if (rawSlot == 7) {
            event.setCancelled(true);
            saveKitFromInventory(player, event.getInventory(), holder.getKit());
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
            plugin.getKitManagerGUI().open(player);
            return;
        }

        if (rawSlot == 6) {
            event.setCancelled(true);
            Kit kit = holder.getKit();
            int current = kit.getDelaySeconds();
            int next;
            if (current == 0) next = 30;
            else if (current == 30) next = 60;
            else if (current == 60) next = 300;
            else if (current == 300) next = 600;
            else next = 0;

            kit.setDelaySeconds(next);
            ItemStack delayBtn = new ItemBuilder(Material.CLOCK)
                    .name("<gold><bold>⏱ Demora de Entrega: <yellow>" + next + "s")
                    .lore(
                            "<gray>Demora en segundos antes de recibir el kit.",
                            "<yellow>Click para alternar: 0s -> 30s -> 60s -> 300s -> 600s"
                    )
                    .build();
            event.getInventory().setItem(6, delayBtn);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof KitEditHolder holder) {
            if (event.getPlayer() instanceof Player player) {
                saveKitFromInventory(player, event.getInventory(), holder.getKit());
                editingSessions.remove(player.getUniqueId());
            }
        }
    }

    private void saveKitFromInventory(Player player, Inventory inv, Kit kit) {
        if (kit == null) return;

        kit.setHelmet(cleanItem(inv.getItem(0)));
        kit.setChestplate(cleanItem(inv.getItem(1)));
        kit.setLeggings(cleanItem(inv.getItem(2)));
        kit.setBoots(cleanItem(inv.getItem(3)));
        kit.setOffHand(cleanItem(inv.getItem(4)));

        List<ItemStack> items = new ArrayList<>();
        for (int i = 18; i < 54; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                items.add(item.clone());
            }
        }
        kit.setItems(items);

        plugin.getKitManager().saveKit(kit);
        TextUtil.sendMessage(player, "<green>¡Kit <bold>" + kit.getName() + "</bold> guardado exitosamente!");
    }

    private ItemStack cleanItem(ItemStack item) {
        return (item != null && item.getType() != Material.AIR) ? item.clone() : null;
    }

    public static class KitEditHolder implements InventoryHolder {
        private final Kit kit;

        public KitEditHolder(Kit kit) {
            this.kit = kit;
        }

        public Kit getKit() {
            return kit;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
