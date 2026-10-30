package net.yanguhc.scenario.impl;

import net.yanguhc.YangUHC;
import net.yanguhc.scenario.Scenario;
import net.yanguhc.util.ItemBuilder;
import net.yanguhc.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GoldenRetrieverScenario extends Scenario {

    public GoldenRetrieverScenario(YangUHC plugin) {
        super(plugin, "GoldenRetriever", "Cada kill suelta una Golden Head que cura 4 corazones + Speed.", Material.PLAYER_HEAD);
    }

    public static ItemStack createGoldenHead() {
        return new ItemBuilder(Material.GOLDEN_APPLE)
                .name("<gold><bold>Golden Head</bold>")
                .lore(
                        "<gray>Cabeza dorada obtenida de un jugador.",
                        "<green>+4 Corazones de regeneración",
                        "<aqua>+Velocidad II (15s)"
                )
                .build();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        event.getDrops().add(createGoldenHead());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item.getType() == Material.GOLDEN_APPLE && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String name = TextUtil.toLegacy(item.getItemMeta().displayName());
            if (name.contains("Golden Head")) {
                Player player = event.getPlayer();
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 8, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 15, 1));
                TextUtil.sendMessage(player, "<gold>¡Has consumido una Golden Head!");
            }
        }
    }
}
