package net.battleroyalex.scenario.impl;

import net.battleroyalex.BattleRoyaleX;
import net.battleroyalex.scenario.Scenario;
import net.battleroyalex.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class TimeBombScenario extends Scenario {

    public TimeBombScenario(BattleRoyaleX plugin) {
        super(plugin, "TimeBomb", "El loot de los jugadores muertos se guarda en un cofre que explota en 30s.", Material.TNT);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        List<ItemStack> drops = new ArrayList<>(event.getDrops());
        event.getDrops().clear();

        Location loc = player.getLocation().getBlock().getLocation();
        Block block1 = loc.getBlock();
        Block block2 = loc.clone().add(1, 0, 0).getBlock();

        block1.setType(Material.CHEST);
        block2.setType(Material.CHEST);

        if (block1.getState() instanceof Chest chest1 && block2.getState() instanceof Chest chest2) {
            int slot = 0;
            for (ItemStack drop : drops) {
                if (drop == null || drop.getType() == Material.AIR) continue;
                if (slot < 27) {
                    chest1.getInventory().addItem(drop);
                } else if (slot < 54) {
                    chest2.getInventory().addItem(drop);
                } else {
                    loc.getWorld().dropItemNaturally(loc, drop);
                }
                slot++;
            }
        }

        ArmorStand hologram = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().add(1.0, 1.0, 0.5), EntityType.ARMOR_STAND);
        hologram.setVisible(false);
        hologram.setGravity(false);
        hologram.setCustomNameVisible(true);
        hologram.customName(TextUtil.parse("<yellow>" + player.getName() + " <gray>| <red>Explosión: 30s"));

        new BukkitRunnable() {
            int seconds = 30;

            @Override
            public void run() {
                if (seconds <= 0) {
                    cancel();
                    hologram.remove();
                    block1.setType(Material.AIR);
                    block2.setType(Material.AIR);
                    loc.getWorld().createExplosion(loc.clone().add(0.5, 0.5, 0.5), 5.0f, false, true);
                    return;
                }

                hologram.customName(TextUtil.parse("<yellow>" + player.getName() + " <gray>| <red>Explosión: " + seconds + "s"));
                seconds--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}
