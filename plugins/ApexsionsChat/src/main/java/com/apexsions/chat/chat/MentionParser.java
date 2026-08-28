package com.apexsions.chat.chat;

import com.apexsions.chat.ApexsionsChatPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MentionParser {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, Long> allMentionCooldowns = new ConcurrentHashMap<>();

    public MentionParser(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    public Component parseMentions(Player sender, String word, Set<UUID> notifiedPlayers) {
        FileConfiguration config = plugin.getConfigManager().getModerationConfig();

        if (word.equalsIgnoreCase("@all")) {
            if (config.getBoolean("mentions.all.enabled", true) && canUseAllMention(sender)) {
                recordAllMentionUsage(sender);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.equals(sender) && !notifiedPlayers.contains(p.getUniqueId())) {
                        notifiedPlayers.add(p.getUniqueId());
                        notifyPlayer(p, sender);
                    }
                }
                String highlightFmt = config.getString("mentions.all.highlight-format", "<gradient:#f59e0b:#ef4444><bold>@all</bold></gradient>");
                return miniMessage.deserialize(highlightFmt);
            }
            return Component.text(word);
        }

        if (word.startsWith("@") && word.length() > 1) {
            String targetName = word.substring(1);
            // Handle trailing punctuation if any (e.g. @Player!)
            String cleanName = targetName.replaceAll("[^a-zA-Z0-9_]", "");
            String punctuation = targetName.substring(cleanName.length());

            Player target = Bukkit.getPlayerExact(cleanName);
            if (target != null && target.isOnline() && !target.equals(sender)) {
                if (!notifiedPlayers.contains(target.getUniqueId())) {
                    notifiedPlayers.add(target.getUniqueId());
                    notifyPlayer(target, sender);
                }
                String highlightFmt = config.getString("mentions.player.highlight-format", "<color:#38bdf8><bold>@{player}</bold></color>")
                        .replace("{player}", target.getName());
                Component highlighted = miniMessage.deserialize(highlightFmt);
                if (!punctuation.isEmpty()) {
                    return highlighted.append(Component.text(punctuation));
                }
                return highlighted;
            }
        }

        return Component.text(word);
    }

    private void notifyPlayer(Player target, Player sender) {
        Component actionBar = miniMessage.deserialize("<gold>🔔 You were mentioned by <yellow>" + sender.getName() + "</yellow> in chat!</gold>");
        target.sendActionBar(actionBar);

        FileConfiguration config = plugin.getConfigManager().getMainConfig();
        if (config.getBoolean("sounds.mention.enabled", true)) {
            String soundName = config.getString("sounds.mention.sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
            float volume = (float) config.getDouble("sounds.mention.volume", 1.0);
            float pitch = (float) config.getDouble("sounds.mention.pitch", 1.2);
            try {
                target.playSound(target.getLocation(), Sound.valueOf(soundName), volume, pitch);
            } catch (Exception ignored) {}
        }
    }

    private boolean canUseAllMention(Player sender) {
        if (sender.hasPermission("apexsionschat.mention.all")) {
            return true;
        }
        long now = System.currentTimeMillis();
        long last = allMentionCooldowns.getOrDefault(sender.getUniqueId(), 0L);
        int cdSec = plugin.getConfigManager().getModerationConfig().getInt("mentions.all.cooldown-seconds", 60);
        return (now - last) >= (cdSec * 1000L);
    }

    private void recordAllMentionUsage(Player sender) {
        if (!sender.hasPermission("apexsionschat.mention.all")) {
            allMentionCooldowns.put(sender.getUniqueId(), System.currentTimeMillis());
        }
    }
}
