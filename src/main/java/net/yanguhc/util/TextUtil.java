package net.yanguhc.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.legacySection();

    private TextUtil() {
    }

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }
        // If string contains MiniMessage tags like <red>, <gradient>, etc.
        if (input.contains("<") && input.contains(">")) {
            // Translate legacy & first if mixed
            String converted = AMPERSAND_SERIALIZER.serialize(AMPERSAND_SERIALIZER.deserialize(input));
            try {
                return MINI_MESSAGE.deserialize(input);
            } catch (Exception ignored) {
                // Fallback to legacy
            }
        }
        // Fallback or legacy format
        return AMPERSAND_SERIALIZER.deserialize(input.replace('§', '&'));
    }

    public static String toLegacy(Component component) {
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

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) return;
        player.sendTitle(
                toLegacy(parse(title)),
                toLegacy(parse(subtitle)),
                fadeIn,
                stay,
                fadeOut
        );
    }

    public static void sendActionBar(Player player, String message) {
        if (player != null && message != null) {
            player.sendActionBar(parse(message));
        }
    }
}
