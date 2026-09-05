package com.apexsions.chat.moderation;

import com.apexsions.chat.ApexsionsChatPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ProfanityChecker {

    private final ApexsionsChatPlugin plugin;

    public ProfanityChecker(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    public ModerationResult check(Player player, String rawMessage) {
        FileConfiguration config = plugin.getConfigManager().getModerationConfig();
        if (!config.getBoolean("profanity.enabled", true)) {
            return ModerationResult.allow(rawMessage);
        }

        if (plugin.getModerationEngine().shouldBypass(player, "apexsionschat.bypass.profanity")) {
            return ModerationResult.allow(rawMessage);
        }

        List<String> bannedWords = config.getStringList("profanity.banned-words");
        if (bannedWords.isEmpty()) {
            return ModerationResult.allow(rawMessage);
        }

        List<String> exceptions = config.getStringList("profanity.exceptions");
        Set<String> exceptionSet = new HashSet<>();
        for (String ex : exceptions) {
            exceptionSet.add(ex.toLowerCase().trim());
        }

        String action = config.getString("profanity.action", "BLOCK").toUpperCase();
        String mask = config.getString("profanity.replacement-mask", "***");

        // 1. Normalized inspection copies
        String normalizedInspection = TextNormalizer.normalizeForInspection(rawMessage);
        String denseInspection = TextNormalizer.stripSeparators(normalizedInspection);

        boolean found = false;
        String filteredMessage = rawMessage;

        for (String banned : bannedWords) {
            String cleanBanned = banned.toLowerCase().trim();
            if (cleanBanned.isEmpty()) continue;

            // Check if entire message token is in whitelist exceptions (e.g. "pass", "grass", "title")
            if (isExceptionWord(normalizedInspection, exceptionSet)) {
                continue;
            }

            // Method A: Word Boundary Matching on normalized inspection
            String regex = "(?i)\\b" + Pattern.quote(cleanBanned) + "\\b";
            Pattern pattern = Pattern.compile(regex);
            if (pattern.matcher(normalizedInspection).find()) {
                found = true;
                if ("BLOCK".equals(action)) {
                    return ModerationResult.block(ModerationRule.PROFANITY,
                            "Your message contains prohibited or inappropriate language.");
                }
                filteredMessage = filteredMessage.replaceAll("(?i)" + Pattern.quote(banned), mask);
            }

            // Method B: Dense string matching (handles bypasses like f.u.c.k, f u c k, b_a_b_i, k.o.n.t.o.l)
            if (denseInspection.contains(cleanBanned)) {
                if (!isExceptionWord(denseInspection, exceptionSet)) {
                    found = true;
                    if ("BLOCK".equals(action)) {
                        return ModerationResult.block(ModerationRule.PROFANITY,
                            "Your message contains prohibited or inappropriate language.");
                    }
                    filteredMessage = filteredMessage.replaceAll("(?i)" + Pattern.quote(banned), mask);
                }
            }
        }

        if (found) {
            if ("WARN".equals(action)) {
                return ModerationResult.warn(rawMessage, ModerationRule.PROFANITY,
                        "Please keep chat language polite and respectful.");
            }
            return ModerationResult.replace(filteredMessage, ModerationRule.PROFANITY, "Profanity censored.");
        }

        return ModerationResult.allow(rawMessage);
    }

    private boolean isExceptionWord(String text, Set<String> exceptions) {
        if (exceptions.isEmpty()) return false;
        String[] tokens = text.split("\\s+");
        for (String token : tokens) {
            if (exceptions.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
