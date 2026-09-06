package com.apexsions.media.engine;

import java.awt.Color;

/**
 * Fast Color Palette Matcher for Minecraft Map Byte Conversion (Standard Bukkit / Paper 1.21 Map Palette).
 */
@SuppressWarnings("deprecation")
public class MapPalette {

    private static final byte[] COLOR_INDEX_CACHE = new byte[32 * 32 * 32];

    static {
        // Precompute 15-bit RGB lookup table using Bukkit's official MapPalette matcher
        for (int r = 0; r < 32; r++) {
            int red = (r * 255) / 31;
            for (int g = 0; g < 32; g++) {
                int green = (g * 255) / 31;
                for (int b = 0; b < 32; b++) {
                    int blue = (b * 255) / 31;
                    COLOR_INDEX_CACHE[(r << 10) | (g << 5) | b] = org.bukkit.map.MapPalette.matchColor(red, green, blue);
                }
            }
        }
    }

    public static byte matchColor(Color color) {
        if (color == null || color.getAlpha() < 128) {
            return org.bukkit.map.MapPalette.TRANSPARENT; // 0 = Transparent
        }
        int r = color.getRed() >> 3;
        int g = color.getGreen() >> 3;
        int b = color.getBlue() >> 3;
        return COLOR_INDEX_CACHE[(r << 10) | (g << 5) | b];
    }

    public static byte matchColor(int rgb) {
        int alpha = (rgb >> 24) & 0xFF;
        if (alpha < 128) {
            return org.bukkit.map.MapPalette.TRANSPARENT; // 0 = Transparent
        }
        int r = ((rgb >> 16) & 0xFF) >> 3;
        int g = ((rgb >> 8) & 0xFF) >> 3;
        int b = (rgb & 0xFF) >> 3;
        return COLOR_INDEX_CACHE[(r << 10) | (g << 5) | b];
    }
}

