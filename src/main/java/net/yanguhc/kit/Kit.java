package net.yanguhc.kit;

import net.yanguhc.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class Kit {

    private final String name;
    private final List<ItemStack> items = new ArrayList<>();
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;
    private ItemStack offHand;
    private int delaySeconds = 0;
    private final List<PotionEffect> effects = new ArrayList<>();

    public Kit(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public void clearItems() {
        items.clear();
    }

    public void setItems(List<ItemStack> newItems) {
        items.clear();
        if (newItems != null) {
            for (ItemStack item : newItems) {
                if (item != null && item.getType() != Material.AIR) {
                    items.add(item.clone());
                }
            }
        }
    }

    public void addItem(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            items.add(item.clone());
        }
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public void setHelmet(ItemStack helmet) {
        this.helmet = helmet;
    }

    public ItemStack getChestplate() {
        return chestplate;
    }

    public void setChestplate(ItemStack chestplate) {
        this.chestplate = chestplate;
    }

    public ItemStack getLeggings() {
        return leggings;
    }

    public void setLeggings(ItemStack leggings) {
        this.leggings = leggings;
    }

    public ItemStack getBoots() {
        return boots;
    }

    public void setBoots(ItemStack boots) {
        this.boots = boots;
    }

    public ItemStack getOffHand() {
        return offHand;
    }

    public void setOffHand(ItemStack offHand) {
        this.offHand = offHand;
    }

    public int getDelaySeconds() {
        return delaySeconds;
    }

    public void setDelaySeconds(int delaySeconds) {
        this.delaySeconds = Math.max(0, delaySeconds);
    }

    public List<PotionEffect> getEffects() {
        return effects;
    }

    public void apply(Player player) {
        if (player == null) return;
        PlayerInventory inv = player.getInventory();

        if (helmet != null) inv.setHelmet(helmet.clone());
        if (chestplate != null) inv.setChestplate(chestplate.clone());
        if (leggings != null) inv.setLeggings(leggings.clone());
        if (boots != null) inv.setBoots(boots.clone());
        if (offHand != null) inv.setItemInOffHand(offHand.clone());

        for (ItemStack item : items) {
            inv.addItem(item.clone());
        }

        for (PotionEffect effect : effects) {
            player.addPotionEffect(effect);
        }

        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1f);
        TextUtil.sendMessage(player, "<green>¡Has recibido el kit <bold>" + name + "</bold>!");
    }
}
