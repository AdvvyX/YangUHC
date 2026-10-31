package net.battleroyalex.scenario.impl;

import net.battleroyalex.BattleRoyaleX;
import net.battleroyalex.scenario.Scenario;
import net.battleroyalex.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class GoldlessScenario extends Scenario {

    public GoldlessScenario(BattleRoyaleX plugin) {
        super(plugin, "Goldless", "No se puede minar oro. Se obtienen 8 lingotes de oro por kill.", Material.GOLD_INGOT);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        if (b.getType() == Material.GOLD_ORE || b.getType() == Material.DEEPSLATE_GOLD_ORE || b.getType() == Material.NETHER_GOLD_ORE) {
            event.setDropItems(false);
            event.setExpToDrop(2);
            TextUtil.sendMessage(event.getPlayer(), "<red>Goldless está activo: ¡El oro solo se consigue con kills!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onKill(PlayerDeathEvent event) {
        Player killer = event.getPlayer().getKiller();
        if (killer != null && killer != event.getPlayer()) {
            event.getDrops().add(new ItemStack(Material.GOLD_INGOT, 8));
        }
    }
}
