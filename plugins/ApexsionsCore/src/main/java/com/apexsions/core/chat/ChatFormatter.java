package com.apexsions.core.chat;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Formats chat messages according to KingdomCore design specifications using Adventure Components
 * and kingdom-specific color themes with LuckPerms rank integration.
 */
public class ChatFormatter {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ChatFormatter(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public Component format(Player player, Component messageComponent) {
        if (!plugin.getConfigManager().isChatEnabled()) {
            return messageComponent;
        }

        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
        int level = dataOpt.map(PlayerData::getLevel).orElse(1);
        String title = plugin.getLevelManager().getLevelTitle(player.getUniqueId());
        String rank = plugin.getLuckPermsHook().getPlayerRank(player);
        String prefix = plugin.getLuckPermsHook().getPlayerPrefix(player);

        String regionKey = "none";
        String regionDisplayName = plugin.getConfigManager().getDefaultRegion();
        if (dataOpt.isPresent() && dataOpt.get().getRegionId() != null) {
            Optional<Region> regionOpt = plugin.getRegionManager().getRegion(dataOpt.get().getRegionId());
            if (regionOpt.isPresent()) {
                regionKey = regionOpt.get().getKey();
                regionDisplayName = regionOpt.get().getDisplayName();
            }
        }

        String kingdomTag = plugin.getConfigManager().getKingdomChatTag(regionKey);
        String rawMessage = PlainTextComponentSerializer.plainText().serialize(messageComponent);

        String template = plugin.getConfigManager().getChatFormat();
        String formatted = template
                .replace("{level}", String.valueOf(level))
                .replace("{title}", title)
                .replace("{rank}", rank)
                .replace("{prefix}", prefix)
                .replace("{kingdom}", kingdomTag)
                .replace("{region}", regionDisplayName)
                .replace("{player}", player.getName())
                .replace("{message}", rawMessage);

        return miniMessage.deserialize(formatted);
    }
}
