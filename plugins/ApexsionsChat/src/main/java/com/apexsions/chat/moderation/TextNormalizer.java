package com.apexsions.chat.moderation;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class TextNormalizer {

    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern REPEATING_CHARS = Pattern.compile("(.)\\1{2,}");

    public static String normalizeUnicode(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text, Normalizer.Form.NFKC);
    }

    public static String normalizeLeetspeak(String text) {
        if (text == null) return "";
        String s = text.toLowerCase();
        // Obfuscation substitutions
        s = s.replace("f@ck", "fuck")
             .replace("fvck", "fuck")
             .replace("f*ck", "fuck")
             .replace("@", "a")
             .replace("4", "a")
             .replace("3", "e")
             .replace("1", "i")
             .replace("|", "i")
             .replace("0", "o")
             .replace("5", "s")
             .replace("$", "s")
             .replace("7", "t")
             .replace("+", "t")
             .replace("8", "b")
             .replace("v", "u");

        // Convert '!' to 'i' only if adjacent to letters (e.g. b!tch or b4b!)
        s = s.replaceAll("(?<=[a-zA-Z0-9])!|!(?=[a-zA-Z0-9])", "i");
        return s;
    }

    public static String collapseRepeatedChars(String text) {
        if (text == null) return "";
        return REPEATING_CHARS.matcher(text).replaceAll("$1");
    }

    public static String collapseWhitespace(String text) {
        if (text == null) return "";
        return MULTIPLE_SPACES.matcher(text.trim()).replaceAll(" ");
    }

    public static String stripSeparators(String text) {
        if (text == null) return "";
        return text.replaceAll("[^a-zA-Z0-9]", "");
    }

    public static String normalizeForSpam(String text) {
        if (text == null) return "";
        String s = normalizeUnicode(text).toLowerCase();
        s = stripSeparators(s);
        return collapseRepeatedChars(s);
    }

    public static String normalizeForInspection(String text) {
        if (text == null) return "";
        String normalized = normalizeUnicode(text).toLowerCase();
        normalized = normalizeLeetspeak(normalized);
        normalized = collapseRepeatedChars(normalized);
        return collapseWhitespace(normalized);
    }
}
