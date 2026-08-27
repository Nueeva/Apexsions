package com.yourserver.apexsionschat.moderation;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Pattern;

public class AdvertisementChecker {

    private final ApexsionsChatPlugin plugin;
    // IPv4 Address Pattern (with dots, spaces, commas, or colons)
    private final Pattern ipPattern = Pattern.compile("(?i)\\b(?:\\d{1,3}[.,\\s]){3}\\d{1,3}(?::\\d{1,5})?\\b");
    // Web Domain Pattern
    private final Pattern domainPattern = Pattern.compile("(?i)\\b(?:https?://|www\\.)?[a-zA-Z0-9-]+\\.(?:com|net|org|io|gg|me|xyz|co|tv|app|dev|ru|de|uk)\\b");
    // Discord Invite Pattern
    private final Pattern discordPattern = Pattern.compile("(?i)(?:discord(?:\\.gg|\\.com/invite)/[a-zA-Z0-9]+)");

    public AdvertisementChecker(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    public ModerationResult check(Player player, String message) {
        FileConfiguration config = plugin.getConfigManager().getModerationConfig();
        if (!config.getBoolean("advertising.enabled", true)) {
            return ModerationResult.allow(message);
        }

        if (plugin.getModerationEngine().shouldBypass(player, "apexsionschat.bypass.advertising")) {
            return ModerationResult.allow(message);
        }

        List<String> whitelist = config.getStringList("advertising.whitelisted-domains");

        // 1. Discord Invite Check
        if (config.getBoolean("advertising.block-discord-invites", true)) {
            if (discordPattern.matcher(message).find() && !isWhitelisted(message, whitelist)) {
                return ModerationResult.block(ModerationRule.ADVERTISEMENT, "Advertising Discord invite links is not permitted.");
            }
        }

        // 2. IP Address Check
        if (config.getBoolean("advertising.block-ip-addresses", true)) {
            if (ipPattern.matcher(message).find() && !isWhitelisted(message, whitelist)) {
                return ModerationResult.block(ModerationRule.ADVERTISEMENT, "Posting server IP addresses is not permitted.");
            }
        }

        // 3. Domain Check
        if (config.getBoolean("advertising.block-domains", true)) {
            if (domainPattern.matcher(message).find() && !isWhitelisted(message, whitelist)) {
                return ModerationResult.block(ModerationRule.ADVERTISEMENT, "Posting external website links is not permitted.");
            }
        }

        return ModerationResult.allow(message);
    }

    private boolean isWhitelisted(String message, List<String> whitelist) {
        if (whitelist == null || whitelist.isEmpty()) return false;
        String lower = message.toLowerCase();
        for (String allowed : whitelist) {
            if (lower.contains(allowed.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
