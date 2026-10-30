package net.yanguhc.scenario;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.yanguhc.YangUHC;
import net.yanguhc.util.ItemBuilder;
import net.yanguhc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ScenarioGUI implements Listener, InventoryHolder {

    private final YangUHC plugin;
    private final Inventory inventory;
    private final NamespacedKey scenarioKey;

    public ScenarioGUI(YangUHC plugin) {
        this.plugin = plugin;
        this.scenarioKey = new NamespacedKey(plugin, "scenario_id");
        this.inventory = Bukkit.createInventory(this, 54, TextUtil.parse("<gold><bold>Gestor de Escenarios UHC</bold>"));
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
        int activeCount = 0;

        for (Scenario scenario : plugin.getScenarioManager().getAllScenarios()) {
            boolean enabled = scenario.isEnabled();
            if (enabled) activeCount++;

            String status = enabled ? "<green><bold>ACTIVADO</bold>" : "<red><bold>DESACTIVADO</bold>";

            List<String> lore = new ArrayList<>();
            lore.add("<gray>" + scenario.getDescription());
            lore.add("");
            lore.add("<white>Estado: " + status);
            lore.add("<yellow><italic>Click para alternar (Solo Admins/OP)");

            ItemBuilder builder = new ItemBuilder(scenario.getIcon())
                    .name((enabled ? "<green><bold>" : "<gray>") + scenario.getName() + "</bold>")
                    .lore(lore);

            if (enabled) {
                builder.enchant(Enchantment.UNBREAKING, 1).flags(ItemFlag.HIDE_ENCHANTS);
            }

            ItemStack item = builder.build();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(scenarioKey, PersistentDataType.STRING, scenario.getName().toLowerCase());
                item.setItemMeta(meta);
            }

            inventory.setItem(slot, item);

            slot++;
            if ((slot + 1) % 9 == 0) {
                slot += 2;
            }
            if (slot >= 44) break;
        }

        // Fillers
        ItemStack filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }

        // Summary item in slot 49
        ItemStack summary = new ItemBuilder(Material.NETHER_STAR)
                .name("<gold><bold>Resumen de Escenarios</bold>")
                .lore(
                        "<gray>Activos actualmente: <green><bold>" + activeCount + "</bold><gray>/" + plugin.getScenarioManager().getAllScenarios().size(),
                        "<gray>Los cambios se guardan automáticamente."
                )
                .build();
        inventory.setItem(49, summary);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ScenarioGUI)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE || clicked.getType() == Material.NETHER_STAR) {
            return;
        }

        boolean isAdmin = player.isOp() || player.hasPermission("yanguhc.admin") || player.hasPermission("yanguhc.host");
        if (!isAdmin) {
            TextUtil.sendMessage(player, "<red>Solo los administradores u operadores pueden alternar escenarios.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        String targetScenarioId = null;
        if (clicked.hasItemMeta()) {
            targetScenarioId = clicked.getItemMeta().getPersistentDataContainer().get(scenarioKey, PersistentDataType.STRING);
            if (targetScenarioId == null && clicked.getItemMeta().hasDisplayName()) {
                String rawName = PlainTextComponentSerializer.plainText().serialize(clicked.getItemMeta().displayName());
                targetScenarioId = rawName.replaceAll("[^a-zA-Z0-9]", "").trim().toLowerCase();
            }
        }

        if (targetScenarioId != null) {
            Scenario scenario = plugin.getScenarioManager().getScenario(targetScenarioId);
            if (scenario != null) {
                boolean newState = plugin.getScenarioManager().toggleScenario(scenario.getName());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, newState ? 1.2f : 0.8f);
                refresh();
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
