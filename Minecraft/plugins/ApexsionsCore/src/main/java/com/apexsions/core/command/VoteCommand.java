package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.gui.input.BedrockFormAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Handles /vote command for Apexsions civilization support.
 * Crossplay Architecture:
 * - Detects Java Edition vs Bedrock Edition (via Floodgate / Geyser / prefix / UUID).
 * - Java Experience: Clean MiniMessage interactive text with clickable URL + short URL fallback.
 * - Bedrock Experience: Bedrock-compatible native Cumulus SimpleForm + Bedrock-safe chat layout fallback.
 * - Guaranteed Rewards: 3x Vote Keys + Rp 1.000.
 */
public class VoteCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private static final String DIRECT_VOTE_URL = "https://minecraft-mp.com/server/363636/vote/";
    private static final String WEB_PORTAL_URL = "https://web.apexsions.my.id/vote";

    public VoteCommand(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (BedrockFormAdapter.isBedrockPlayer(player)) {
                // Bedrock Experience: Open SimpleForm first
                boolean opened = BedrockFormAdapter.openVoteForm(plugin, player, DIRECT_VOTE_URL, WEB_PORTAL_URL);
                if (!opened) {
                    // Bedrock Chat Fallback (No misleading clickable prompt)
                    sendBedrockChatMessage(player);
                }
                return true;
            } else {
                // Java Experience: MiniMessage interactive clickable link + short URL
                sendJavaChatMessage(player);
                return true;
            }
        }

        // Console / Non-player fallback
        sendUniversalChatMessage(sender);
        return true;
    }

    private void sendJavaChatMessage(Player player) {
        Component divider = mm.deserialize("<gradient:#f59e0b:#d97706>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gradient>");
        Component title = mm.deserialize("<gold>⚔ </gold><yellow><bold>APEXSIONS BILIK SUARA (VOTING)</bold></yellow><gold> ⚔</gold>");
        Component subtitle = mm.deserialize("<gray>Dukung kedaulatan peradaban Apexsions di peringkat dunia dan klaim imbalan pusaka Anda!</gray>");

        Component rewardHeader = mm.deserialize("<gold>Imbalan Resmi Tiap Suara Sah:</gold>");
        Component rewardKey = mm.deserialize("  <yellow>🎁 3x Kunci Peti Pusaka (Vote Keys)</yellow>");
        Component rewardMoney = mm.deserialize("  <green>💰 Rp 1.000 Saldo Peradaban</green>");

        Component directVoteButton = mm.deserialize("<click:open_url:'" + DIRECT_VOTE_URL + "'>"
                + "<hover:show_text:'<green>Klik di sini untuk langsung memberikan suara di Minecraft-MP (Platform #1)!</green>'>"
                + "<gradient:#10b981:#059669><bold>[ 🗳 VOTE DI MINECRAFT-MP ]</bold></gradient>"
                + "</hover></click>");

        Component webPortalButton = mm.deserialize("<click:open_url:'" + WEB_PORTAL_URL + "'>"
                + "<hover:show_text:'<yellow>Klik di sini untuk membuka Portal Web Bilik Suara (Cek streak, leaderboard, & statistik)!</yellow>'>"
                + "<gradient:#fbbf24:#f59e0b><bold>[ 🌐 PORTAL BILIK SUARA WEB ]</bold></gradient>"
                + "</hover></click>");

        Component buttonLine = Component.text("  ")
                .append(directVoteButton)
                .append(Component.text("   "))
                .append(webPortalButton);

        Component footer = mm.deserialize("<green>⚡ <bold>Auto-Reward:</bold> Cukup beri suara di platform, 3x Keys & Rp 1.000 otomatis masuk!</green>");

        player.sendMessage(Component.empty());
        player.sendMessage(divider);
        player.sendMessage(title);
        player.sendMessage(subtitle);
        player.sendMessage(Component.empty());
        player.sendMessage(rewardHeader);
        player.sendMessage(rewardKey);
        player.sendMessage(rewardMoney);
        player.sendMessage(Component.empty());
        player.sendMessage(buttonLine);
        player.sendMessage(Component.empty());
        player.sendMessage(footer);
        player.sendMessage(divider);
        player.sendMessage(Component.empty());
    }

    private void sendBedrockChatMessage(Player player) {
        Component divider = mm.deserialize("<gradient:#06b6d4:#3b82f6>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gradient>");
        Component title = mm.deserialize("<aqua>⚔ </aqua><bold><white>APEXSIONS VOTE (BEDROCK)</white></bold><aqua> ⚔</aqua>");
        Component subtitle = mm.deserialize("<gray>Dukung kedaulatan peradaban Apexsions dari Bedrock Edition dan klaim imbalan Anda!</gray>");

        Component rewardHeader = mm.deserialize("<gold>Imbalan Resmi Tiap Suara Sah:</gold>");
        Component rewardKey = mm.deserialize("  <yellow>🎁 3x Kunci Peti Pusaka (Vote Keys)</yellow>");
        Component rewardMoney = mm.deserialize("  <green>💰 Rp 1.000 Saldo Peradaban</green>");

        Component linkDirectHeader = mm.deserialize("<yellow>1. Tautan Vote Langsung (Minecraft-MP):</yellow>");
        Component linkDirect = mm.deserialize("   <aqua>" + DIRECT_VOTE_URL + "</aqua>");

        Component linkWebHeader = mm.deserialize("<yellow>2. Portal Web Bilik Suara & Streak:</yellow>");
        Component linkWeb = mm.deserialize("   <aqua>" + WEB_PORTAL_URL + "</aqua>");

        Component userHint = mm.deserialize("<gray>Gunakan Username: </gray><gold>" + player.getName() + "</gold>");
        Component footer = mm.deserialize("<green>⚡ <bold>Auto-Reward:</bold> Hadiah otomatis diproses begitu Anda vote tanpa perlu klaim manual!</green>");

        player.sendMessage(Component.empty());
        player.sendMessage(divider);
        player.sendMessage(title);
        player.sendMessage(subtitle);
        player.sendMessage(Component.empty());
        player.sendMessage(rewardHeader);
        player.sendMessage(rewardKey);
        player.sendMessage(rewardMoney);
        player.sendMessage(Component.empty());
        player.sendMessage(linkDirectHeader);
        player.sendMessage(linkDirect);
        player.sendMessage(linkWebHeader);
        player.sendMessage(linkWeb);
        player.sendMessage(userHint);
        player.sendMessage(Component.empty());
        player.sendMessage(footer);
        player.sendMessage(divider);
        player.sendMessage(Component.empty());
    }

    private void sendUniversalChatMessage(CommandSender sender) {
        Component divider = mm.deserialize("<gradient:#f59e0b:#d97706>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gradient>");
        Component title = mm.deserialize("<gold>⚔ </gold><yellow><bold>APEXSIONS BILIK SUARA (VOTING)</bold></yellow><gold> ⚔</gold>");
        Component rewards = mm.deserialize("<yellow>🎁 3x Vote Keys</yellow> <gray>+</gray> <green>💰 Rp 1.000 Saldo Peradaban</green>");
        Component directLine = mm.deserialize("<gray>Vote Langsung: </gray><aqua>" + DIRECT_VOTE_URL + "</aqua>");
        Component webLine = mm.deserialize("<gray>Portal Web: </gray><aqua>" + WEB_PORTAL_URL + "</aqua>");

        sender.sendMessage(divider);
        sender.sendMessage(title);
        sender.sendMessage(rewards);
        sender.sendMessage(directLine);
        sender.sendMessage(webLine);
        sender.sendMessage(divider);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
