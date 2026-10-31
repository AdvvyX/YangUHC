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

public class DiamondlessScenario extends Scenario {

    public DiamondlessScenario(BattleRoyaleX plugin) {
        super(plugin, "Diamondless", "No se pueden minar diamantes. Se obtiene 1 diamante por kill.", Material.DIAMOND);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        if (b.getType() == Material.DIAMOND_ORE || b.getType() == Material.DEEPSLATE_DIAMOND_ORE) {
            event.setDropItems(false);
            event.setExpToDrop(3);
            TextUtil.sendMessage(event.getPlayer(), "<red>Diamondless está activo: ¡Los diamantes solo se consiguen con kills!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onKill(PlayerDeathEvent event) {
        Player killer = event.getPlayer().getKiller();
        if (killer != null && killer != event.getPlayer()) {
            event.getDrops().add(new ItemStack(Material.DIAMOND, 1));
        }
    }
}
