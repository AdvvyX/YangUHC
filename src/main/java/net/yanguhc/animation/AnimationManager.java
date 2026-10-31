package net.yanguhc.animation;

import net.yanguhc.YangUHC;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnimationManager {

    private static final Pattern ANIMATION_PATTERN = Pattern.compile("(?i)[%<{]animation:([a-zA-Z0-9_-]+)[%>}]");

    private final YangUHC plugin;
    private final Map<String, TextAnimation> animations = new HashMap<>();
    private File file;
    private FileConfiguration config;

    public AnimationManager(YangUHC plugin) {
        this.plugin = plugin;
        loadFile();
    }

    public void loadFile() {
        this.file = new File(plugin.getDataFolder(), "animations.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource("animations.yml", false);
        }

        this.config = YamlConfiguration.loadConfiguration(file);

        // Check defaults from jar
        InputStream defStream = plugin.getResource("animations.yml");
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream, StandardCharsets.UTF_8));
            config.setDefaults(defConfig);
        }

        loadAnimations();
    }

    public void loadAnimations() {
        animations.clear();
        ConfigurationSection section = config.getConfigurationSection("animations");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            int interval = section.getInt(key + ".change-interval", 200);
            List<String> texts = section.getStringList(key + ".texts");
            TextAnimation anim = new TextAnimation(key, interval, texts);
            animations.put(key.toLowerCase(), anim);
        }
    }

    public TextAnimation getAnimation(String name) {
        return name == null ? null : animations.get(name.toLowerCase());
    }

    public Collection<TextAnimation> getAllAnimations() {
        return animations.values();
    }

    public String replaceAnimations(String text) {
        if (text == null || text.isEmpty() || animations.isEmpty()) {
            return text;
        }

        Matcher matcher = ANIMATION_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String animName = matcher.group(1);
            TextAnimation anim = getAnimation(animName);
            String replacement = anim != null ? anim.getCurrentFrame() : matcher.group(0);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public void reload() {
        loadFile();
    }
}
