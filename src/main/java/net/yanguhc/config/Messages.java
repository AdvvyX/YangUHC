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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public List<String> getFormattedList(String path, Object... placeholders) {
        List<String> rawLines;
        if (config.isList(path)) {
            rawLines = config.getStringList(path);
        } else if (config.contains(path)) {
            rawLines = Collections.singletonList(config.getString(path));
        } else {
            rawLines = Collections.singletonList("<red>Mensaje no configurado: " + path);
        }

        List<String> formatted = new ArrayList<>();
        for (String line : rawLines) {
            String msg = line;
            if (placeholders != null && placeholders.length > 0) {
                for (int i = 0; i < placeholders.length; i += 2) {
                    if (i + 1 < placeholders.length) {
                        String key = String.valueOf(placeholders[i]);
                        String val = String.valueOf(placeholders[i + 1]);
                        msg = msg.replace("{" + key + "}", val);
                    }
                }
            }
            if (plugin.getAnimationManager() != null) {
                msg = plugin.getAnimationManager().replaceAnimations(msg);
            }
            formatted.add(msg);
        }
        return formatted;
    }

    public String getFormatted(String path, Object... placeholders) {
        List<String> list = getFormattedList(path, placeholders);
        return list.isEmpty() ? "" : list.get(0);
    }

    public Component get(String path, Object... placeholders) {
        return TextUtil.parse(getFormatted(path, placeholders));
    }

    public void send(CommandSender sender, String path, Object... placeholders) {
        if (sender == null) return;
        for (String line : getFormattedList(path, placeholders)) {
            sender.sendMessage(TextUtil.parse(line));
        }
    }

    public void broadcast(String path, Object... placeholders) {
        for (String line : getFormattedList(path, placeholders)) {
            Bukkit.broadcast(TextUtil.parse(line));
        }
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
