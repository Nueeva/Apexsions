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
    private static final String FULL_VOTE_URL = "http://web.apexsions.my.id/vote";
    private static final String SHORT_VOTE_URL = "apexsions.my.id/vote";

    public VoteCommand(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (BedrockFormAdapter.isBedrockPlayer(player)) {
                // Bedrock Experience: Open SimpleForm first
                boolean opened = BedrockFormAdapter.openVoteForm(plugin, player, FULL_VOTE_URL, SHORT_VOTE_URL);
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

        Component clickPrompt = mm.deserialize("<click:open_url:'" + FULL_VOTE_URL + "'>"
                + "<hover:show_text:'<yellow>Klik di sini untuk membuka Bilik Suara Web Apexsions!</yellow>'>"
                + "<gradient:#fbbf24:#f59e0b><bold>[ 🗳 KLIK DI SINI UNTUK MEMBERIKAN SUARA ]</bold></gradient>"
                + "</hover></click>");

        Component shortUrlLine = mm.deserialize("<gray>Tautan Singkat / Short URL: </gray><aqua><underlined>" + SHORT_VOTE_URL + "</underlined></aqua> <dark_gray>(atau <aqua>" + FULL_VOTE_URL + "</aqua>)</dark_gray>");
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
        player.sendMessage(clickPrompt);
        player.sendMessage(shortUrlLine);
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

        Component linkHeader = mm.deserialize("<yellow>Buka Alamat Berikut di Browser HP / Komputer Anda:</yellow>");
        Component shortUrlLine = mm.deserialize("  <gradient:#38bdf8:#818cf8><bold>" + SHORT_VOTE_URL + "</bold></gradient> <gray>(atau " + FULL_VOTE_URL + ")</gray>");
        Component userHint = mm.deserialize("<gray>Masukkan Username Minecraft Anda: </gray><gold>" + player.getName() + "</gold>");
        Component footer = mm.deserialize("<green>⚡ <bold>Auto-Reward:</bold> Hadiah otomatis diproses begitu Anda vote di platform tanpa perlu verifikasi manual!</green>");

        player.sendMessage(Component.empty());
        player.sendMessage(divider);
        player.sendMessage(title);
        player.sendMessage(subtitle);
        player.sendMessage(Component.empty());
        player.sendMessage(rewardHeader);
        player.sendMessage(rewardKey);
        player.sendMessage(rewardMoney);
        player.sendMessage(Component.empty());
        player.sendMessage(linkHeader);
        player.sendMessage(shortUrlLine);
        player.sendMessage(userHint);
        player.sendMessage(footer);
        player.sendMessage(divider);
        player.sendMessage(Component.empty());
    }

    private void sendUniversalChatMessage(CommandSender sender) {
        Component divider = mm.deserialize("<gradient:#f59e0b:#d97706>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gradient>");
        Component title = mm.deserialize("<gold>⚔ </gold><yellow><bold>APEXSIONS BILIK SUARA (VOTING)</bold></yellow><gold> ⚔</gold>");
        Component rewards = mm.deserialize("<yellow>🎁 3x Vote Keys</yellow> <gray>+</gray> <green>💰 Rp 1.000 Saldo Peradaban</green>");
        Component url = mm.deserialize("<gray>Kunjungi: </gray><aqua>" + SHORT_VOTE_URL + "</aqua> <gray>(" + FULL_VOTE_URL + ")</gray>");

        sender.sendMessage(divider);
        sender.sendMessage(title);
        sender.sendMessage(rewards);
        sender.sendMessage(url);
        sender.sendMessage(divider);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
