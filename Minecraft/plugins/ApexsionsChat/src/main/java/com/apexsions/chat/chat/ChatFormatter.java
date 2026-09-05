package com.apexsions.chat.chat;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.chat.channel.ChatChannel;
import com.apexsions.core.api.PlayerChatProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
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

        // 1. Gather clean profile DTO from Core API
        PlayerChatProfile profile = plugin.getApexsionsCoreHook().getPlayerChatProfile(uuid);

        String title = profile != null && profile.activeTitle() != null ? profile.activeTitle() : (profile != null ? profile.levelTitle() : "Citizen");
        String rank = profile != null ? profile.rank() : plugin.getLuckPermsHook().getPlayerRank(player);
        int level = profile != null ? profile.level() : 1;
        String regionKey = profile != null ? profile.kingdomKey() : "NONE";
        String kingdomTag = getKingdomTag(regionKey);

        // 2. Build interactive player name component with rich ID-Card hover tooltip and click-to-profile action
        Component playerComponent = buildInteractivePlayerComponent(player, profile);

        // 3. Build safe message component with mentions and item showcase
        Component messageComponent = buildMessageComponent(player, rawMessage);

        // 4. Format complete line using TagResolvers
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
                Placeholder.component("player", playerComponent),
                Placeholder.component("message", messageComponent)
        );
    }

    private Component buildInteractivePlayerComponent(Player player, PlayerChatProfile profile) {
        String pName = profile != null ? profile.playerName() : player.getName();
        String title = profile != null && profile.activeTitle() != null ? profile.activeTitle() : (profile != null ? profile.levelTitle() : "Wanderer");
        String rank = profile != null ? profile.rank() : "Wanderer";
        int level = profile != null ? profile.level() : 1;
        long xp = profile != null ? profile.xp() : 0;
        long reqXp = profile != null ? profile.requiredNextXp() : 1000;
        double balance = profile != null ? profile.balanceRupiah() : 0.0;
        int hp = profile != null ? profile.health() : (int) player.getHealth();
        int maxHp = profile != null ? profile.maxHealth() : (int) player.getMaxHealth();
        int ping = profile != null ? profile.ping() : player.getPing();
        String kingdomDisplay = profile != null ? profile.kingdomDisplayName() : "Belum Memilih";

        String rankBadge = switch (rank.toLowerCase()) {
            case "ancestor" -> "<gradient:#8B0000:#FF0000><bold>👑 ANCESTOR</bold></gradient>";
            case "warden" -> "<gradient:#1e3c72:#2a5298><bold>🛡 WARDEN</bold></gradient>";
            case "herald" -> "<gradient:#f857a6:#ff5858><bold>📜 HERALD</bold></gradient>";
            case "sions" -> "<gradient:#00FFFF:#FFD700><bold>✦ SIONS ✦</bold></gradient>";
            case "emperor" -> "<gradient:#e52d27:#b31217><bold>⚔ EMPEROR</bold></gradient>";
            case "sovereign" -> "<gradient:#f39c12:#f1c40f><bold>⚜ SOVEREIGN</bold></gradient>";
            case "archon" -> "<gradient:#00c6ff:#0072ff><bold>💎 ARCHON</bold></gradient>";
            case "ascendant" -> "<gradient:#11998e:#38ef7d><bold>☘ ASCENDANT</bold></gradient>";
            default -> "<gray>" + rank + "</gray>";
        };

        var nickData = plugin.getNicknameService() != null ? plugin.getNicknameService().getNicknameData(player.getUniqueId()) : null;
        boolean hasNick = nickData != null && nickData.hasNickname();

        Component baseNameComponent = plugin.getNicknameService() != null
                ? plugin.getNicknameService().getFormattedNickname(player)
                : Component.text(player.getName());

        String nickLine = hasNick
                ? "<gray>Akun Asli:</gray> <white><bold>" + player.getName() + "</bold></white>\n<gray>Nama Panggilan:</gray> <yellow>~" + nickData.getNicknameRaw() + "</yellow>\n"
                : "<gray>Pemain:</gray> <white><bold>" + pName + "</bold></white>\n";

        String hoverCard =
                "<gradient:#f1c40f:#e67e22><bold>👑 KARTU IDENTITAS KARAKTER 👑</bold></gradient>\n" +
                nickLine +
                "<gray>Gelar:</gray> " + title + "\n" +
                "<gray>Rank:</gray> " + rankBadge + "\n" +
                "<gray>Kerajaan:</gray> <gold>" + kingdomDisplay + "</gold>" + (profile != null && profile.isMonarch() ? " <yellow><bold>[RAJA]</bold></yellow>" : "") + "\n" +
                "<gray>Level Karakter:</gray> <yellow>Lv. " + level + "</yellow> <dark_gray>(" + xp + " / " + (reqXp == Long.MAX_VALUE ? "MAX" : reqXp) + " XP)</dark_gray>\n" +
                "<gray>Saldo Rupiah:</gray> <green><bold>Rp " + String.format("%,.0f", balance) + "</bold></green>\n" +
                "<gray>Status Darah:</gray> <red>" + hp + "/" + maxHp + " ❤</red> <gray>• Ping:</gray> <green>" + ping + "ms</green>\n\n" +
                "<yellow>▶ Klik untuk membuka menu interaksi sosial & profil pemain!</yellow>";

        return baseNameComponent
                .hoverEvent(HoverEvent.showText(miniMessage.deserialize(hoverCard)))
                .clickEvent(ClickEvent.runCommand("/channel profile " + player.getName()));
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
