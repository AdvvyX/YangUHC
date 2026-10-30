package net.yanguhc.config;

import net.kyori.adventure.text.Component;
import net.yanguhc.YangUHC;
import net.yanguhc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Messages {

    private final YangUHC plugin;
    private File file;
    private FileConfiguration config;

    public Messages(YangUHC plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        this.file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource("messages.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);

        // Merge defaults if new keys exist
        InputStream defStream = plugin.getResource("messages.yml");
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream, StandardCharsets.UTF_8));
            config.setDefaults(defConfig);
        }
    }

    public void reload() {
        load();
    }

    public String getRaw(String path) {
        return config.getString(path, "<red>Mensaje no configurado: " + path);
    }

    public String getFormatted(String path, Object... placeholders) {
        String msg = getRaw(path);
        if (placeholders != null && placeholders.length > 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                if (i + 1 < placeholders.length) {
                    String key = String.valueOf(placeholders[i]);
                    String val = String.valueOf(placeholders[i + 1]);
                    msg = msg.replace("{" + key + "}", val);
                }
            }
        }
        return msg;
    }

    public Component get(String path, Object... placeholders) {
        return TextUtil.parse(getFormatted(path, placeholders));
    }

    public void send(CommandSender sender, String path, Object... placeholders) {
        if (sender == null) return;
        sender.sendMessage(get(path, placeholders));
    }

    public void broadcast(String path, Object... placeholders) {
        Bukkit.broadcast(get(path, placeholders));
    }

    public void sendTitle(Player player, String titlePath, String subtitlePath, int fadeIn, int stay, int fadeOut, Object... placeholders) {
        if (player == null) return;
        String title = getFormatted(titlePath, placeholders);
        String sub = getFormatted(subtitlePath, placeholders);
        TextUtil.sendTitle(player, title, sub, fadeIn, stay, fadeOut);
    }

    public String getPrefix() {
        return config.getString("prefix", "");
    }
}
