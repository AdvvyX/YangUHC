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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private static final Pattern HEX_AMP_6 = Pattern.compile("&#([a-fA-F0-9]{6})");
    private static final Pattern HEX_BRACKET_6 = Pattern.compile("\\{#([a-fA-F0-9]{6})\\}");
    private static final Pattern HEX_BARE_6 = Pattern.compile("(?<![<:;#&])#([a-fA-F0-9]{6})");
    private static final Pattern HEX_AMP_3 = Pattern.compile("&#([a-fA-F0-9])([a-fA-F0-9])([a-fA-F0-9])(?![a-fA-F0-9])");
    private static final Pattern HEX_BARE_3 = Pattern.compile("(?<![<:;#&])#([a-fA-F0-9])([a-fA-F0-9])([a-fA-F0-9])(?![a-fA-F0-9])");

    private static final Pattern LEGACY_PATTERN = Pattern.compile("&([0-9a-fk-orA-FK-OR])");

    private TextUtil() {
    }

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        String processed = toMiniMessageFormat(input);
        try {
            return MINI_MESSAGE.deserialize(processed);
        } catch (Exception e) {
            String fallback = ChatColor.translateAlternateColorCodes('&', input);
            return SECTION_SERIALIZER.deserialize(fallback);
        }
    }

    public static String toMiniMessageFormat(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String s = input.replace('§', '&');

        s = HEX_AMP_6.matcher(s).replaceAll("<#$1>");

        s = HEX_BRACKET_6.matcher(s).replaceAll("<#$1>");

        s = HEX_BARE_6.matcher(s).replaceAll("<#$1>");

        s = HEX_AMP_3.matcher(s).replaceAll("<#$1$1$2$2$3$3>");

        s = HEX_BARE_3.matcher(s).replaceAll("<#$1$1$2$2$3$3>");

        Matcher matcher = LEGACY_PATTERN.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            char code = Character.toLowerCase(matcher.group(1).charAt(0));
            String tag = getTagForLegacyCode(code);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(tag));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static String getTagForLegacyCode(char code) {
        return switch (code) {
            case '0' -> "<black>";
            case '1' -> "<dark_blue>";
            case '2' -> "<dark_green>";
            case '3' -> "<dark_aqua>";
            case '4' -> "<dark_red>";
            case '5' -> "<dark_purple>";
            case '6' -> "<gold>";
            case '7' -> "<gray>";
            case '8' -> "<dark_gray>";
            case '9' -> "<blue>";
            case 'a' -> "<green>";
            case 'b' -> "<aqua>";
            case 'c' -> "<red>";
            case 'd' -> "<light_purple>";
            case 'e' -> "<yellow>";
            case 'f' -> "<white>";
            case 'k' -> "<obfuscated>";
            case 'l' -> "<bold>";
            case 'm' -> "<strikethrough>";
            case 'n' -> "<underlined>";
            case 'o' -> "<italic>";
            case 'r' -> "<reset>";
            default -> "&" + code;
        };
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

    public static void broadcast(String message) {
        if (message != null && !message.isEmpty()) {
            org.bukkit.Bukkit.broadcast(parse(message));
        }
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
