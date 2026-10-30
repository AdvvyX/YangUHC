package net.yanguhc.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private TextUtil() {
    }

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        // If string contains MiniMessage tags (<tag>...</tag>)
        if (input.contains("<") && input.contains(">")) {
            try {
                // If it also contains legacy & codes, convert & to section first
                String translated = ChatColor.translateAlternateColorCodes('&', input);
                // If it has MiniMessage tags, deserialize directly
                return MINI_MESSAGE.deserialize(input);
            } catch (Exception ignored) {
            }
        }

        // Standard legacy format (&a, &l, §c, etc.)
        String formatted = ChatColor.translateAlternateColorCodes('&', input);
        return SECTION_SERIALIZER.deserialize(formatted);
    }

    public static String toLegacy(Component component) {
        if (component == null) return "";
        return SECTION_SERIALIZER.serialize(component);
    }

    public static String toAmpersand(Component component) {
        if (component == null) return "";
        return AMPERSAND_SERIALIZER.serialize(component);
    }

    public static List<Component> parseList(List<String> list) {
        List<Component> components = new ArrayList<>();
        if (list != null) {
            for (String s : list) {
                components.add(parse(s));
            }
        }
        return components;
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (sender != null && message != null && !message.isEmpty()) {
            sender.sendMessage(parse(message));
        }
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        if (player == null) return;
        Component titleComp = parse(title);
        Component subComp = parse(subtitle);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeInTicks * 50L),
                Duration.ofMillis(stayTicks * 50L),
                Duration.ofMillis(fadeOutTicks * 50L)
        );

        player.showTitle(Title.title(titleComp, subComp, times));
    }

    public static void sendActionBar(Player player, String message) {
        if (player != null && message != null) {
            player.sendActionBar(parse(message));
        }
    }
}
