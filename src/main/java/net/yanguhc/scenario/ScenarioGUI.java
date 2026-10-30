package net.yanguhc.scenario;

import net.kyori.adventure.text.Component;
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

import java.util.ArrayList;
import java.util.List;

public class ScenarioGUI implements Listener, InventoryHolder {

    private final YangUHC plugin;
    private final Inventory inventory;

    public ScenarioGUI(YangUHC plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, 54, TextUtil.parse("<gold><bold>Escenarios de UHC</bold>"));
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

        for (Scenario scenario : plugin.getScenarioManager().getAllScenarios()) {
            boolean enabled = scenario.isEnabled();
            String status = enabled ? "<green><bold>ACTIVADO</bold>" : "<red><bold>DESACTIVADO</bold>";

            List<String> lore = new ArrayList<>();
            lore.add("<gray>" + scenario.getDescription());
            lore.add("");
            lore.add("<white>Estado: " + status);
            lore.add("<yellow>Haz click para alternar estado.");

            ItemStack item = new ItemBuilder(scenario.getIcon())
                    .name("<gold><bold>" + scenario.getName() + "</bold>")
                    .lore(lore)
                    .build();

            inventory.setItem(slot, item);

            slot++;
            if ((slot + 1) % 9 == 0) {
                slot += 2;
            }
            if (slot >= 53) break;
        }

        // Fillers
        ItemStack filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ScenarioGUI)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        if (!player.hasPermission("yanguhc.admin") && !player.hasPermission("yanguhc.host")) {
            TextUtil.sendMessage(player, "<red>Solo los administradores u hosts pueden alternar escenarios.");
            return;
        }

        if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
            String name = TextUtil.toLegacy(clicked.getItemMeta().displayName());
            name = org.bukkit.ChatColor.stripColor(name).trim();

            Scenario scenario = plugin.getScenarioManager().getScenario(name);
            if (scenario != null) {
                plugin.getScenarioManager().toggleScenario(scenario.getName());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                refresh();
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
