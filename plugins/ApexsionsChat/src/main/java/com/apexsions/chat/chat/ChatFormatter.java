package com.apexsions.chat.chat;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.chat.channel.ChatChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ChatFormatter {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ChatFormatter(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    public Component format(Player player, ChatChannel channel, String rawMessage) {
        UUID uuid = player.getUniqueId();

        // 1. Gather domain metadata from ApexsionsCore & LuckPerms
        int level = plugin.getApexsionsCoreHook().getPlayerLevel(uuid);
        String title = plugin.getApexsionsCoreHook().getPlayerTitle(uuid);
        String rank = plugin.getLuckPermsHook().getPlayerRank(player);
        String regionKey = plugin.getApexsionsCoreHook().getPlayerRegionKey(uuid);
        String kingdomTag = getKingdomTag(regionKey);

        // 2. Build safe message component with mentions and item showcase
        Component messageComponent = buildMessageComponent(player, rawMessage);

        // 3. Format complete line using TagResolvers to prevent syntax leaking and tag breakage
        String template = channel.getFormat()
                .replace("{channel_prefix}", "<channel_prefix>")
                .replace("{channel_name}", "<channel_name>")
                .replace("{level}", "<level>")
                .replace("{title}", "<title>")
                .replace("{rank}", "<rank>")
                .replace("{kingdom}", "<kingdom>")
                .replace("{player}", "<player>")
                .replace("{message}", "<message>");

        return miniMessage.deserialize(
                template,
                Placeholder.parsed("channel_prefix", channel.getPrefix()),
                Placeholder.parsed("channel_name", channel.getName()),
                Placeholder.unparsed("level", String.valueOf(level)),
                Placeholder.parsed("title", title),
                Placeholder.parsed("rank", rank),
                Placeholder.parsed("kingdom", kingdomTag),
                Placeholder.unparsed("player", player.getName()),
                Placeholder.component("message", messageComponent)
        );
    }

    public Component buildMessageComponent(Player player, String rawMessage) {
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            return Component.empty();
        }

        List<String> itemKeywords = plugin.getConfigManager().getMainConfig().getStringList("item-showcase.keywords");
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        boolean hasHeldItem = mainHand != null && mainHand.getType() != Material.AIR;

        String[] tokens = rawMessage.split(" ");
        Component result = null;
        Set<UUID> notifiedPlayers = new HashSet<>();

        for (String token : tokens) {
            Component tokenComponent;

            // 1. Check for item showcase keyword
            if (isItemKeyword(token, itemKeywords) && hasHeldItem) {
                tokenComponent = plugin.getItemShowcaseService().buildShowcaseComponent(player, mainHand);
            } else {
                // 2. Parse Mentions (@Player, @all) or fallback to safe plain text component
                tokenComponent = plugin.getMentionParser().parseMentions(player, token, notifiedPlayers);
            }

            if (result == null) {
                result = tokenComponent;
            } else {
                result = result.append(Component.space()).append(tokenComponent);
            }
        }

        return result != null ? result : Component.empty();
    }

    private boolean isItemKeyword(String token, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return token.equalsIgnoreCase("[item]");
        for (String k : keywords) {
            if (token.equalsIgnoreCase(k)) return true;
        }
        return false;
    }

    private String getKingdomTag(String regionKey) {
        FileConfiguration config = plugin.getConfigManager().getChannelsConfig();
        if (regionKey == null || regionKey.isEmpty() || regionKey.equalsIgnoreCase("NONE")) {
            return config.getString("kingdom-tags.none", "<gray>[Unpledged]</gray>");
        }
        return config.getString("kingdom-tags." + regionKey.toUpperCase(), "<gold>[" + regionKey + "]</gold>");
    }
}
