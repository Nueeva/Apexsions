package com.apexsions.media.engine;

import java.awt.Color;
import java.util.Arrays;

/**
 * Fast Color Palette Matcher for Minecraft Map Byte Conversion (Standard Bukkit / Paper 1.21 Map Palette).
 */
public class MapPalette {

    private static final int[] PALETTE = new int[256];
    private static final byte[] COLOR_INDEX_CACHE = new byte[32 * 32 * 32];

    static {
        // Base Minecraft Map Base Colors
        int[] baseColors = new int[]{
                0x000000, 0x7FB238, 0xF7E9A3, 0xA7A7A7, 0xFF0000, 0xA0A0FF, 0xA7A7A7, 0x007C00,
                0xFFFFFF, 0xA4A8B8, 0x976D4D, 0x707070, 0x4040FF, 0x8F7748, 0xFFFCF5, 0xD87F33,
                0xB24CD8, 0x6699D8, 0xE5E533, 0x7FCC19, 0xF27FA5, 0x4C4C4C, 0x999999, 0x4C7F99,
                0x7F3FB2, 0x334CB2, 0x664C33, 0x667F33, 0x993333, 0x191919, 0xFAEE36, 0x5CDBD5,
                0x4A80FF, 0x00D93A, 0x815631, 0x700200, 0xD1B1A1, 0x95576C, 0x706C8A, 0xBA8523,
                0x677535, 0xA04D4E, 0x352923, 0x876B62, 0x575C5C, 0x7A4A58, 0x4C3E5C, 0x4C3223,
                0x4C522A, 0x8E3C2E, 0x251610, 0xBD3031, 0x943F61, 0x5C191D, 0x167E86, 0x3A8E8C,
                0x562C3E, 0x142929
        };

        int idx = 4;
        for (int base : baseColors) {
            Color c = new Color(base);
            int r = c.getRed();
            int g = c.getGreen();
            int b = c.getBlue();

            // 4 Shades for each base color (180, 220, 255, 135)
            PALETTE[idx++] = new Color((r * 180) / 255, (g * 180) / 255, (b * 180) / 255).getRGB();
            PALETTE[idx++] = new Color((r * 220) / 255, (g * 220) / 255, (b * 220) / 255).getRGB();
            PALETTE[idx++] = new Color(r, g, b).getRGB();
            PALETTE[idx++] = new Color((r * 135) / 255, (g * 135) / 255, (b * 135) / 255).getRGB();
            if (idx >= 256) break;
        }

        // Precompute 15-bit RGB lookup table for lightning-fast rendering
        for (int r = 0; r < 32; r++) {
            for (int g = 0; g < 32; g++) {
                for (int b = 0; b < 32; b++) {
                    COLOR_INDEX_CACHE[(r << 10) | (g << 5) | b] = computeNearestByte((r << 3), (g << 3), (b << 3));
                }
            }
        }
    }

    public static byte matchColor(Color color) {
        if (color.getAlpha() < 128) {
            return 0; // Transparent
        }
        int r = color.getRed() >> 3;
        int g = color.getGreen() >> 3;
        int b = color.getBlue() >> 3;
        return COLOR_INDEX_CACHE[(r << 10) | (g << 5) | b];
    }

    public static byte matchColor(int rgb) {
        int alpha = (rgb >> 24) & 0xFF;
        if (alpha < 128) return 0;
        int r = ((rgb >> 16) & 0xFF) >> 3;
        int g = ((rgb >> 8) & 0xFF) >> 3;
        int b = (rgb & 0xFF) >> 3;
        return COLOR_INDEX_CACHE[(r << 10) | (g << 5) | b];
    }

    private static byte computeNearestByte(int r, int g, int b) {
        double minDistance = Double.MAX_VALUE;
        byte bestMatch = 0;

        for (int i = 4; i < PALETTE.length; i++) {
            if (PALETTE[i] == 0) continue;
            Color c = new Color(PALETTE[i]);
            double dR = r - c.getRed();
            double dG = g - c.getGreen();
            double dB = b - c.getBlue();
            // Weighted Euclidean distance for human eye perception
            double distance = (dR * dR * 0.299) + (dG * dG * 0.587) + (dB * dB * 0.114);
            if (distance < minDistance) {
                minDistance = distance;
                bestMatch = (byte) i;
            }
        }
        return bestMatch;
    }
}
