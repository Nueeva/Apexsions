package com.apexsions.chat.moderation;

import com.apexsions.chat.ApexsionsChatPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpamChecker {

    private final ApexsionsChatPlugin plugin;

    // Per-player rate limit tracking: timestamps of recent messages
    private final Map<UUID, Deque<Long>> messageTimestamps = new ConcurrentHashMap<>();
    // Per-player recent message history for duplicate & near-duplicate checks (max 3 messages)
    private final Map<UUID, List<String>> recentMessages = new ConcurrentHashMap<>();
    // Per-player spam violation counter
    private final Map<UUID, Integer> violationCounts = new ConcurrentHashMap<>();
    // Temporary mute expiration timestamps (millis)
    private final Map<UUID, Long> temporaryMutes = new ConcurrentHashMap<>();

    public SpamChecker(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    public ModerationResult check(Player player, String rawMessage) {
        FileConfiguration config = plugin.getConfigManager().getModerationConfig();
        if (!config.getBoolean("spam.enabled", true)) {
            return ModerationResult.allow(rawMessage);
        }

        if (plugin.getModerationEngine().shouldBypass(player, "apexsionschat.bypass.spam")) {
            return ModerationResult.allow(rawMessage);
        }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // 1. Check if player is currently under temporary mute
        Long muteExpiry = temporaryMutes.get(uuid);
        if (muteExpiry != null && now < muteExpiry) {
            long remainingSec = Math.max(1, (muteExpiry - now) / 1000);
            return ModerationResult.block(ModerationRule.TEMPORARY_MUTE,
                    "You are temporarily muted for spamming! Try again in " + remainingSec + "s.");
        } else if (muteExpiry != null) {
            temporaryMutes.remove(uuid);
            violationCounts.remove(uuid);
        }

        // 2. Sliding Window Rate Limiting (e.g. max 3 messages per 4 seconds)
        int maxMessages = config.getInt("spam.rate-limit.messages", 3);
        int windowSeconds = config.getInt("spam.rate-limit.window-seconds", 4);
        long windowMillis = windowSeconds * 1000L;

        Deque<Long> timestamps = messageTimestamps.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            // Expire old timestamps
            while (!timestamps.isEmpty() && (now - timestamps.peekFirst()) > windowMillis) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= maxMessages) {
                return handleSpamViolation(uuid, ModerationRule.RATE_LIMIT,
                        "You are sending messages too quickly! Please slow down.");
            }
            timestamps.addLast(now);
        }

        // 3. Minimum message delay check (e.g. 1200ms)
        long minDelay = config.getLong("spam.min-message-delay-ms", 1200);
        if (timestamps.size() > 1) {
            Iterator<Long> descIt = timestamps.descendingIterator();
            descIt.next(); // current
            long prevTime = descIt.next();
            if ((now - prevTime) < minDelay) {
                return handleSpamViolation(uuid, ModerationRule.RATE_LIMIT,
                        "Please wait at least " + (minDelay / 1000.0) + "s between messages.");
            }
        }

        // 4. Normalized Duplicate & Near-Duplicate Spam Detection
        String normalizedInspection = TextNormalizer.normalizeForSpam(rawMessage);
        List<String> history = recentMessages.computeIfAbsent(uuid, k -> new ArrayList<>());
        synchronized (history) {
            if (!normalizedInspection.isEmpty() && normalizedInspection.length() >= 3) {
                for (String prevNormalized : history) {
                    // Exact match after normalization (e.g. "hello", "HELLO", "h e l l o", "h.e.l.l.o")
                    if (normalizedInspection.equalsIgnoreCase(prevNormalized)) {
                        return handleSpamViolation(uuid, ModerationRule.DUPLICATE_SPAM,
                                "Please do not repeat the same message.");
                    }

                    // Near-duplicate similarity check (Levenshtein distance)
                    double threshold = config.getDouble("spam.duplicate-similarity-threshold", 0.80);
                    double similarity = calculateSimilarity(normalizedInspection, prevNormalized);
                    if (similarity >= threshold) {
                        return handleSpamViolation(uuid, ModerationRule.SIMILARITY_SPAM,
                                "Please do not send repetitive or near-duplicate messages.");
                    }
                }

                // Add to history (keep max 3)
                history.add(normalizedInspection);
                if (history.size() > 3) {
                    history.remove(0);
                }
            }
        }

        // 5. Caps percentage check
        int maxCaps = config.getInt("spam.max-caps-percentage", 65);
        String finalMessage = rawMessage;
        if (rawMessage.length() >= 5) {
            int upperCount = 0;
            int letterCount = 0;
            for (char c : rawMessage.toCharArray()) {
                if (Character.isLetter(c)) {
                    letterCount++;
                    if (Character.isUpperCase(c)) upperCount++;
                }
            }
            if (letterCount >= 5 && ((double) upperCount / letterCount * 100) > maxCaps) {
                finalMessage = rawMessage.toLowerCase();
            }
        }

        // 6. Consecutive repeating characters check
        int maxSymbols = config.getInt("spam.max-consecutive-symbols", 4);
        if (hasExcessiveRepeatingChars(rawMessage, maxSymbols)) {
            return handleSpamViolation(uuid, ModerationRule.EXCESSIVE_SYMBOLS,
                    "Your message contains excessive repeating characters or symbols.");
        }

        return ModerationResult.allow(finalMessage);
    }

    private ModerationResult handleSpamViolation(UUID uuid, ModerationRule rule, String reason) {
        FileConfiguration config = plugin.getConfigManager().getModerationConfig();
        boolean muteEnabled = config.getBoolean("spam.mute.enabled", true);
        int muteDurationSec = config.getInt("spam.mute.duration-seconds", 15);
        int violationThreshold = config.getInt("spam.mute.violation-threshold", 3);

        int currentViolations = violationCounts.merge(uuid, 1, Integer::sum);
        if (muteEnabled && currentViolations >= violationThreshold) {
            long expiry = System.currentTimeMillis() + (muteDurationSec * 1000L);
            temporaryMutes.put(uuid, expiry);
            return ModerationResult.tempMute(ModerationRule.TEMPORARY_MUTE,
                    "You have been temporarily muted for " + muteDurationSec + "s due to repeated spam violations!");
        }

        return ModerationResult.block(rule, reason);
    }

    private boolean hasExcessiveRepeatingChars(String text, int max) {
        if (text == null || text.length() <= max) return false;
        int repeat = 1;
        for (int i = 1; i < text.length(); i++) {
            if (text.charAt(i) == text.charAt(i - 1)) {
                repeat++;
                if (repeat > max) return true;
            } else {
                repeat = 1;
            }
        }
        return false;
    }

    public static double calculateSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLen);
    }

    private static int levenshteinDistance(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int j = 0; j <= s2.length(); j++) costs[j] = j;
        for (int i = 1; i <= s1.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= s2.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        s1.charAt(i - 1) == s2.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[s2.length()];
    }
}
