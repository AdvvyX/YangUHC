package net.yanguhc.scenario;

import net.yanguhc.YangUHC;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class Scenario implements Listener {

    protected final YangUHC plugin;
    private final String name;
    private final String description;
    private final Material icon;
    private boolean enabled = false;

    public Scenario(YangUHC plugin, String name, String description, Material icon) {
        this.plugin = plugin;
        this.name = name;
        this.description = description;
        this.icon = icon != null ? icon : Material.BOOK;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Material getIcon() {
        return icon;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            onEnable();
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        } else {
            onDisable();
            HandlerList.unregisterAll(this);
        }
    }

    public void onEnable() {
    }

    public void onDisable() {
    }
}
