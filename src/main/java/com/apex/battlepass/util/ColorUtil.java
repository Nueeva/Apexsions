package com.apex.battlepass.util;

import org.bukkit.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.stream.Collectors;

public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String color = matcher.group(1);
            try {
                matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + color).toString());
            } catch (Throwable t) {
                matcher.appendReplacement(buffer, "");
            }
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static List<String> colorize(List<String> list) {
        if (list == null) return List.of();
        return list.stream().map(ColorUtil::colorize).collect(Collectors.toList());
    }
}
