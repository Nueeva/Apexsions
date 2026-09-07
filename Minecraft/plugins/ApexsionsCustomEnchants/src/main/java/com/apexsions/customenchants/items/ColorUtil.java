package com.apexsions.customenchants.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Universal color parser and item piece name formatter.
 * Fully supports:
 * - Legacy color codes (&0-&9, &a-&f, &l, &o, &n, &m, &k, &r)
 * - Section symbol codes (§0-§9, §a-§f, etc.)
 * - Hex color codes (&#RRGGBB, &x&r&r&g&g&b&b, #RRGGBB)
 * - MiniMessage tags (<red>, <gradient:...>, <#hex>, <bold>, etc.)
 * - Disables vanilla italics by default on custom items
 */
public final class ColorUtil {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_AMP = LegacyComponentSerializer.legacyAmpersand();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    // Pattern for &#RRGGBB
    private static final Pattern HEX_AMP_PATTERN = Pattern.compile("(?i)&#([A-Fa-f0-9]{6})");
    // Pattern for &x&r&r&g&g&b&b
    private static final Pattern HEX_SPIGOT_PATTERN = Pattern.compile("(?i)&x&([A-Fa-f0-9])&([A-Fa-f0-9])&([A-Fa-f0-9])&([A-Fa-f0-9])&([A-Fa-f0-9])&([A-Fa-f0-9])");
    // Pattern for #RRGGBB when not preceded by & or <
    private static final Pattern HEX_RAW_PATTERN = Pattern.compile("(?i)(?<![&<#])#([A-Fa-f0-9]{6})");

    private ColorUtil() {}

    /**
     * Converts any input text (legacy &, section §, hex &#RRGGBB, #RRGGBB)
     * into a standardized string with MiniMessage tags.
     */
    public static String toMiniMessageTags(String input) {
        if (input == null || input.isBlank()) return "";
        String text = input.replace('§', '&');

        // Replace Spigot hex format &x&r&r&g&g&b&b with <#rrggbb>
        Matcher mSpigot = HEX_SPIGOT_PATTERN.matcher(text);
        text = mSpigot.replaceAll("<#$1$2$3$4$5$6>");

        // Replace &#RRGGBB with <#RRGGBB>
        Matcher mHexAmp = HEX_AMP_PATTERN.matcher(text);
        text = mHexAmp.replaceAll("<#$1>");

        // Replace #RRGGBB with <#RRGGBB>
        Matcher mHexRaw = HEX_RAW_PATTERN.matcher(text);
        text = mHexRaw.replaceAll("<#$1>");

        // Convert remaining legacy & color codes to MiniMessage tags
        if (text.contains("&")) {
            text = text
                    .replace("&0", "<black>")
                    .replace("&1", "<dark_blue>")
                    .replace("&2", "<dark_green>")
                    .replace("&3", "<dark_aqua>")
                    .replace("&4", "<dark_red>")
                    .replace("&5", "<dark_purple>")
                    .replace("&6", "<gold>")
                    .replace("&7", "<gray>")
                    .replace("&8", "<dark_gray>")
                    .replace("&9", "<blue>")
                    .replace("&a", "<green>")
                    .replace("&b", "<aqua>")
                    .replace("&c", "<red>")
                    .replace("&d", "<light_purple>")
                    .replace("&e", "<yellow>")
                    .replace("&f", "<white>")
                    .replace("&l", "<bold>")
                    .replace("&o", "<italic>")
                    .replace("&n", "<underlined>")
                    .replace("&m", "<strikethrough>")
                    .replace("&k", "<obfuscated>")
                    .replace("&r", "<reset>");
        }

        return text;
    }

    /**
     * Parses any colored input (MiniMessage tags, legacy &, section §, hex)
     * into an Adventure Component with italics disabled.
     */
    public static Component parse(String input) {
        if (input == null || input.isBlank()) return Component.empty();

        String mmText = toMiniMessageTags(input);
        try {
            return MM.deserialize(mmText).decoration(TextDecoration.ITALIC, false);
        } catch (Exception ignored) {
            try {
                return LEGACY_AMP.deserialize(input.replace('§', '&')).decoration(TextDecoration.ITALIC, false);
            } catch (Exception ignored2) {
                return Component.text(toPlainText(input)).decoration(TextDecoration.ITALIC, false);
            }
        }
    }

    /**
     * Formats an item piece name from a base name and a piece suffix (e.g. "Helmet", "Sword").
     * Preserves the base name's styling for the suffix as well, and disables italics.
     */
    public static Component formatPieceName(String baseName, String suffix) {
        if (baseName == null || baseName.isBlank()) {
            return Component.text(suffix != null ? suffix : "").decoration(TextDecoration.ITALIC, false);
        }
        if (suffix == null || suffix.isBlank()) {
            return parse(baseName);
        }

        String mmBase = toMiniMessageTags(baseName).trim();

        // Gradient tag handling: <gradient:#c1:#c2>BaseName</gradient>
        if (mmBase.startsWith("<gradient:") && mmBase.endsWith("</gradient>")) {
            int closeTagIdx = mmBase.lastIndexOf("</gradient>");
            String inner = mmBase.substring(0, closeTagIdx);
            return parse(inner + " " + suffix + "</gradient>");
        }

        // Rainbow tag handling: <rainbow>BaseName</rainbow>
        if (mmBase.startsWith("<rainbow>") && mmBase.endsWith("</rainbow>")) {
            int closeTagIdx = mmBase.lastIndexOf("</rainbow>");
            String inner = mmBase.substring(0, closeTagIdx);
            return parse(inner + " " + suffix + "</rainbow>");
        }

        // Strip trailing closing tags so suffix naturally inherits the active colors/formatting
        String tagStripped = mmBase;
        while (tagStripped.endsWith(">") && tagStripped.contains("</")) {
            int lastClose = tagStripped.lastIndexOf("</");
            if (lastClose != -1 && tagStripped.substring(lastClose).endsWith(">")) {
                tagStripped = tagStripped.substring(0, lastClose).trim();
            } else {
                break;
            }
        }

        // If tagStripped has opening tags, append suffix inside them
        if (tagStripped.contains("<") && tagStripped.contains(">")) {
            try {
                return parse(tagStripped + " " + suffix);
            } catch (Exception ignored) {}
        }

        // Legacy code handling
        if (baseName.contains("&") || baseName.contains("§")) {
            return parse(baseName + " " + suffix);
        }

        // Default: Gold Bold
        return parse("<gold><bold>" + baseName + " " + suffix + "</bold></gold>");
    }

    /**
     * Extracts pure plain text from any string (stripping MiniMessage tags and legacy codes).
     */
    public static String toPlainText(String text) {
        if (text == null || text.isBlank()) return "";
        try {
            Component c = parse(text);
            return PLAIN.serialize(c).trim();
        } catch (Exception e) {
            return text.replaceAll("<[^>]*>", "").replaceAll("[&§][0-9a-fk-orA-FK-OR]", "").trim();
        }
    }
}
