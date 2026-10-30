package net.yanguhc.team;

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
import java.util.UUID;

public class TeamGUI implements Listener, InventoryHolder {

    private final YangUHC plugin;

    public TeamGUI(YangUHC plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(this, 36, TextUtil.parse("<aqua><bold>Gestión de Equipos</bold>"));
        Team team = plugin.getTeamManager().getTeam(player);

        if (team == null) {
            // Not in a team: Option to Create Team
            ItemStack create = new ItemBuilder(Material.EMERALD_BLOCK)
                    .name("<green><bold>Crear Equipo</bold>")
                    .lore("<gray>Haz click para formar tu propio equipo.", "<yellow>Capacidad: " + plugin.getTeamManager().getMaxTeamSize() + " jugadores.")
                    .build();
            inv.setItem(13, create);
        } else {
            // In a team: Show members & leave button
            int slot = 10;
            for (UUID uuid : team.getMembers()) {
                Player member = Bukkit.getPlayer(uuid);
                String memberName = member != null ? member.getName() : "Desconectado";
                boolean isLeader = uuid.equals(team.getLeader());

                ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                        .name("<yellow>" + memberName + (isLeader ? " <gold>(Líder)" : ""))
                        .lore("<gray>Estado: " + (team.isAlive(uuid) ? "<green>Vivo" : "<red>Eliminado"))
                        .build();
                inv.setItem(slot++, head);
            }

            ItemStack leave = new ItemBuilder(Material.BARRIER)
                    .name("<red><bold>Abandonar Equipo</bold>")
                    .lore("<gray>Haz click para salir de este equipo.")
                    .build();
            inv.setItem(31, leave);
        }

        // Fillers
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 36; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof TeamGUI)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        if (clicked.getType() == Material.EMERALD_BLOCK) {
            plugin.getTeamManager().createTeam(player);
            player.closeInventory();
        } else if (clicked.getType() == Material.BARRIER) {
            plugin.getTeamManager().leaveTeam(player);
            player.closeInventory();
        }
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
