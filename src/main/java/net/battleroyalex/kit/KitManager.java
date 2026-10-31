package net.battleroyalex.kit;

import net.battleroyalex.BattleRoyaleX;
import net.battleroyalex.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KitManager {

    private final BattleRoyaleX plugin;
    private final Map<String, Kit> kits = new LinkedHashMap<>();
    private String starterKitName = "starter";
    private File file;
    private FileConfiguration config;
    private boolean starterGiven = false;
    private final List<BukkitTask> scheduledKitTasks = new ArrayList<>();

    public KitManager(BattleRoyaleX plugin) {
        this.plugin = plugin;
        loadFile();
    }

    private void loadFile() {
        this.file = new File(plugin.getDataFolder(), "kits.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            createDefaultKitsFile();
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadKits();
    }

    private void createDefaultKitsFile() {
        YamlConfiguration initial = new YamlConfiguration();
        initial.set("starter-kit", "starter");

        initial.set("kits.starter.enabled", true);
        initial.set("kits.starter.delay-seconds", 0);
        initial.set("kits.starter.helmet", new ItemBuilder(Material.LEATHER_HELMET).unbreakable(true).build());
        initial.set("kits.starter.chestplate", new ItemBuilder(Material.LEATHER_CHESTPLATE).unbreakable(true).build());
        initial.set("kits.starter.leggings", new ItemBuilder(Material.LEATHER_LEGGINGS).unbreakable(true).build());
        initial.set("kits.starter.boots", new ItemBuilder(Material.LEATHER_BOOTS).unbreakable(true).build());

        List<ItemStack> items = Arrays.asList(
                new ItemBuilder(Material.STONE_SWORD).build(),
                new ItemBuilder(Material.STONE_PICKAXE).build(),
                new ItemBuilder(Material.STONE_AXE).build(),
                new ItemBuilder(Material.COOKED_BEEF, 16).build(),
                new ItemBuilder(Material.STRING, 3).build(),
                new ItemBuilder(Material.FEATHER, 3).build(),
                new ItemBuilder(Material.OAK_LOG, 32).build()
        );
        initial.set("kits.starter.items", items);

        initial.set("kits.food_drop.enabled", true);
        initial.set("kits.food_drop.delay-seconds", 600);
        initial.set("kits.food_drop.items", Arrays.asList(
                new ItemBuilder(Material.GOLDEN_APPLE, 2).name("<gold>Manzana de Apoyo").build(),
                new ItemBuilder(Material.COOKED_PORKCHOP, 16).build(),
                new ItemBuilder(Material.ARROW, 32).build()
        ));

        try {
            initial.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo crear kits.yml: " + e.getMessage());
        }
    }

    public void loadKits() {
        kits.clear();
        this.starterKitName = config.getString("starter-kit", "starter");

        ConfigurationSection section = config.getConfigurationSection("kits");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            Kit kit = new Kit(key);
            kit.setEnabled(section.getBoolean(key + ".enabled", true));
            kit.setDelaySeconds(section.getInt(key + ".delay-seconds", 0));
            kit.setHelmet(section.getItemStack(key + ".helmet"));
            kit.setChestplate(section.getItemStack(key + ".chestplate"));
            kit.setLeggings(section.getItemStack(key + ".leggings"));
            kit.setBoots(section.getItemStack(key + ".boots"));
            kit.setOffHand(section.getItemStack(key + ".offhand"));

            List<?> itemsList = section.getList(key + ".items");
            if (itemsList != null) {
                for (Object obj : itemsList) {
                    if (obj instanceof ItemStack is) {
                        kit.addItem(is);
                    }
                }
            }
            kits.put(key.toLowerCase(), kit);
        }
    }

    public void saveKit(Kit kit) {
        kits.put(kit.getName().toLowerCase(), kit);
        String path = "kits." + kit.getName().toLowerCase();
        config.set(path + ".enabled", kit.isEnabled());
        config.set(path + ".delay-seconds", kit.getDelaySeconds());
        config.set(path + ".helmet", kit.getHelmet());
        config.set(path + ".chestplate", kit.getChestplate());
        config.set(path + ".leggings", kit.getLeggings());
        config.set(path + ".boots", kit.getBoots());
        config.set(path + ".offhand", kit.getOffHand());
        config.set(path + ".items", kit.getItems());

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Error al guardar kits.yml: " + e.getMessage());
        }
    }

    public void createKitFromPlayer(String name, Player player, int delaySeconds) {
        Kit kit = new Kit(name.toLowerCase());
        kit.setEnabled(true);
        kit.setDelaySeconds(delaySeconds);

        PlayerInventory inv = player.getInventory();
        kit.setHelmet(inv.getHelmet());
        kit.setChestplate(inv.getChestplate());
        kit.setLeggings(inv.getLeggings());
        kit.setBoots(inv.getBoots());
        kit.setOffHand(inv.getItemInOffHand());

        for (ItemStack item : inv.getStorageContents()) {
            if (item != null && item.getType() != Material.AIR) {
                kit.addItem(item);
            }
        }

        saveKit(kit);
        plugin.getMessages().send(player, "kits.saved", "kit", name);
    }

    public Kit getKit(String name) {
        return name == null ? null : kits.get(name.toLowerCase());
    }

    public String getStarterKitName() {
        return starterKitName;
    }

    public boolean deleteKit(String name) {
        if (name == null) return false;
        String key = name.toLowerCase();
        if (kits.remove(key) != null) {
            config.set("kits." + key, null);
            if (starterKitName.equalsIgnoreCase(key)) {
                starterKitName = kits.isEmpty() ? "" : kits.keySet().iterator().next();
                config.set("starter-kit", starterKitName);
            }
            try {
                config.save(file);
            } catch (IOException ignored) {
            }
            return true;
        }
        return false;
    }

    public Kit getStarterKit() {
        return getKit(starterKitName);
    }

    public void setStarterKitName(String name) {
        this.starterKitName = name;
        config.set("starter-kit", name);
        try {
            config.save(file);
        } catch (IOException ignored) {
        }
    }

    public Collection<Kit> getAllKits() {
        return Collections.unmodifiableCollection(kits.values());
    }

    public List<Kit> getEnabledKits() {
        List<Kit> list = new ArrayList<>();
        for (Kit k : kits.values()) {
            if (k.isEnabled()) list.add(k);
        }
        return list;
    }

    public void setAllKitsEnabled(boolean enabled) {
        for (Kit kit : kits.values()) {
            kit.setEnabled(enabled);
            config.set("kits." + kit.getName().toLowerCase() + ".enabled", enabled);
        }
        try {
            config.save(file);
        } catch (IOException ignored) {
        }
    }

    public void cancelPendingTasks() {
        for (BukkitTask task : scheduledKitTasks) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        scheduledKitTasks.clear();
    }

    public void giveStarterKits() {
        cancelPendingTasks();

        List<Kit> enabledImmediateKits = new ArrayList<>();
        List<Kit> enabledDelayedKits = new ArrayList<>();

        for (Kit kit : kits.values()) {
            if (!kit.isEnabled()) continue;
            if (kit.getDelaySeconds() <= 0) {
                enabledImmediateKits.add(kit);
            } else {
                enabledDelayedKits.add(kit);
            }
        }

        if (!enabledImmediateKits.isEmpty()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (plugin.getGameManager().isAlive(p)) {
                    for (Kit kit : enabledImmediateKits) {
                        kit.apply(p);
                    }
                }
            }
            starterGiven = true;
            plugin.getMessages().broadcast("kits.delivered-broadcast");
        }

        for (Kit kit : enabledDelayedKits) {
            int kitDelay = kit.getDelaySeconds();
            plugin.getMessages().broadcast("kits.delayed-warning", "kit", kit.getName(), "seconds", kitDelay);

            BukkitTask delayedTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!plugin.getGameManager().getState().isGameActive()) {
                        cancel();
                        return;
                    }
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (plugin.getGameManager().isAlive(p)) {
                            kit.apply(p);
                        }
                    }
                    plugin.getMessages().broadcast("kits.special-event", "kit", kit.getName());
                }
            }.runTaskLater(plugin, kitDelay * 20L);
            scheduledKitTasks.add(delayedTask);
        }
    }

    public void reset() {
        starterGiven = false;
        cancelPendingTasks();
    }
}
