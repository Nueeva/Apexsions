package com.apexsions.chat.moderation;

import com.apexsions.chat.ApexsionsChatPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class HateSpeechChecker {

    private final ApexsionsChatPlugin plugin;

    public HateSpeechChecker(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    public ModerationResult check(Player player, String rawMessage) {
        FileConfiguration config = plugin.getConfigManager().getModerationConfig();
        if (!config.getBoolean("hate-speech.enabled", true)) {
            return ModerationResult.allow(rawMessage);
        }

        if (plugin.getModerationEngine().shouldBypass(player, "apexsionschat.bypass.profanity")) {
            return ModerationResult.allow(rawMessage);
        }

        List<String> bannedPhrases = config.getStringList("hate-speech.banned-phrases");
        if (bannedPhrases.isEmpty()) {
            return ModerationResult.allow(rawMessage);
        }

        String normalizedInspection = TextNormalizer.stripSeparators(TextNormalizer.normalizeForInspection(rawMessage));
        for (String phrase : bannedPhrases) {
            String cleanPhrase = TextNormalizer.stripSeparators(TextNormalizer.normalizeForInspection(phrase));
            if (normalizedInspection.contains(cleanPhrase)) {
                return ModerationResult.block(ModerationRule.HATE_SPEECH,
                        "Discriminatory, hateful, or abusive remarks are strictly prohibited.");
            }
        }

        return ModerationResult.allow(rawMessage);
    }
}
