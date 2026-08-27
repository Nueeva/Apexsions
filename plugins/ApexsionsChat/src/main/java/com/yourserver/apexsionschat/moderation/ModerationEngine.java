package com.yourserver.apexsionschat.moderation;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import com.yourserver.apexsionschat.model.ModerationLogEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class ModerationEngine {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final SpamChecker spamChecker;
    private final AdvertisementChecker advertisementChecker;
    private final ProfanityChecker profanityChecker;
    private final HateSpeechChecker hateSpeechChecker;

    private final AtomicBoolean globalMuted = new AtomicBoolean(false);

    public ModerationEngine(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
        this.spamChecker = new SpamChecker(plugin);
        this.advertisementChecker = new AdvertisementChecker(plugin);
        this.profanityChecker = new ProfanityChecker(plugin);
        this.hateSpeechChecker = new HateSpeechChecker(plugin);
    }

    public boolean shouldBypass(Player player, String specificBypassPermission) {
        if (player == null) return false;
        // Check if player has explicit bypass permission node
        if (player.hasPermission("apexsionschat.bypass.all") || player.hasPermission(specificBypassPermission)) {
            return true;
        }
        // Check OP bypass toggle in configuration
        boolean opBypass = plugin.getConfigManager().getModerationConfig().getBoolean("general.bypass-for-op", false);
        return opBypass && player.isOp();
    }

    public ModerationResult process(Player player, String message, String channelName) {
        FileConfiguration config = plugin.getConfigManager().getModerationConfig();

        // 0. Global Chat Mute Check
        if (globalMuted.get() && !player.hasPermission("apexsionschat.staff.mutebypass")) {
            String muteMsg = config.getString("general.global-mute.message", "<red>✖ Global chat is currently muted by server staff.</red>");
            return ModerationResult.block(ModerationRule.NONE, muteMsg);
        }

        // 1. Spam & Rate-limit check (First line of defense against rapid floods)
        ModerationResult spamRes = spamChecker.check(player, message);
        if (spamRes.isBlocked()) {
            handleViolationFeedback(player, message, channelName, spamRes);
            return spamRes;
        }
        String currentMsg = spamRes.getMessage() != null ? spamRes.getMessage() : message;

        // 2. Hate speech check
        ModerationResult hateRes = hateSpeechChecker.check(player, currentMsg);
        if (hateRes.isBlocked()) {
            handleViolationFeedback(player, currentMsg, channelName, hateRes);
            return hateRes;
        }

        // 3. Advertising check
        ModerationResult adRes = advertisementChecker.check(player, currentMsg);
        if (adRes.isBlocked()) {
            handleViolationFeedback(player, currentMsg, channelName, adRes);
            return adRes;
        }

        // 4. Custom Regex Rules (Exploits, Token leaks, etc.)
        if (config.getBoolean("custom-rules.enabled", true)) {
            List<Map<?, ?>> rules = config.getMapList("custom-rules.rules");
            for (Map<?, ?> ruleMap : rules) {
                String patternStr = (String) ruleMap.get("pattern");
                String ruleName = (String) ruleMap.get("name");
                String reason = (String) ruleMap.get("reason");
                if (patternStr != null && Pattern.compile(patternStr).matcher(currentMsg).find()) {
                    ModerationResult customRes = ModerationResult.block(ModerationRule.PROFANITY, reason != null ? reason : "Prohibited syntax pattern.");
                    handleViolationFeedback(player, currentMsg, channelName, customRes);
                    return customRes;
                }
            }
        }

        // 5. Profanity check
        ModerationResult profRes = profanityChecker.check(player, currentMsg);
        if (profRes.isBlocked()) {
            handleViolationFeedback(player, currentMsg, channelName, profRes);
            return profRes;
        }

        if (profRes.getAction() == ModerationAction.REPLACE) {
            logViolation(player, currentMsg, channelName, profRes.getRuleViolated(), "CENSORED");
            return profRes;
        }

        return ModerationResult.allow(currentMsg);
    }

    private void handleViolationFeedback(Player player, String message, String channel, ModerationResult result) {
        logViolation(player, message, channel, result.getRuleViolated(), result.getAction().name());

        FileConfiguration config = plugin.getConfigManager().getModerationConfig();

        // 1. Play sound alert to the player
        if (config.getBoolean("general.sound-alerts.enabled", true)) {
            String soundName = config.getString("general.sound-alerts.sound", "BLOCK_NOTE_BLOCK_BASS");
            float vol = (float) config.getDouble("general.sound-alerts.volume", 1.0);
            float pitch = (float) config.getDouble("general.sound-alerts.pitch", 0.8);
            try {
                player.playSound(player.getLocation(), Sound.valueOf(soundName), vol, pitch);
            } catch (Exception ignored) {}
        }

        // 2. Broadcast staff alert
        if (config.getBoolean("general.staff-alerts.enabled", true)) {
            String staffPerm = config.getString("general.staff-alerts.permission", "apexsionschat.staff.alerts");
            String format = config.getString("general.staff-alerts.format",
                    "<gold>🛡️ <dark_gray>[<red>Chat Blocked</red>]</dark_gray> <yellow>{player}</yellow> <gray>({rule}):</gray> <color:#fca5a5>{message}</color></gold>")
                    .replace("{player}", player.getName())
                    .replace("{rule}", result.getRuleViolated())
                    .replace("{message}", message);

            Component alertComponent = miniMessage.deserialize(format);
            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission(staffPerm)) {
                    staff.sendMessage(alertComponent);
                }
            }
        }
    }

    private void logViolation(Player player, String message, String channel, String rule, String action) {
        ModerationLogEntry entry = new ModerationLogEntry(
                player.getUniqueId(),
                player.getName(),
                message.length() > 100 ? message.substring(0, 100) + "..." : message,
                channel,
                rule != null ? rule : "UNKNOWN",
                action
        );
        plugin.getModerationLogRepository().logAsync(entry);
    }

    public boolean isGlobalMuted() {
        return globalMuted.get();
    }

    public void setGlobalMuted(boolean muted) {
        globalMuted.set(muted);
    }

    public SpamChecker getSpamChecker() { return spamChecker; }
    public AdvertisementChecker getAdvertisementChecker() { return advertisementChecker; }
    public ProfanityChecker getProfanityChecker() { return profanityChecker; }
    public HateSpeechChecker getHateSpeechChecker() { return hateSpeechChecker; }
}
