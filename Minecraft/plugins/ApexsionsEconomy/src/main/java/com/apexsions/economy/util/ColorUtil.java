package com.apexsions.economy.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Robust Color & Hex Utility for ApexsionsEconomy.
 * Pure native translation without external BungeeChat dependencies.
 */
public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    public static final char COLOR_CHAR = '§';

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // 1. Process Hex format &#RRGGBB -> §x§r§r§g§g§b§b
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(Character.toLowerCase(c));
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);

        // 2. Process standard color codes &a, &b, &0-9, &l, &r, etc.
        char[] b = buffer.toString().toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    public static List<String> colorize(List<String> list) {
        if (list == null) return List.of();
        return list.stream().map(ColorUtil::colorize).collect(Collectors.toList());
    }
}
