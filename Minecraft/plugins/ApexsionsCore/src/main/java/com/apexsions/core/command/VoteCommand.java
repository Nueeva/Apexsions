package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
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
 * Displays immersive MiniMessage layout with clickable web link and clear rewards:
 * - 3x Vote Crate Keys
 * - Rp 1.000 In-Game Currency
 */
public class VoteCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private static final String VOTE_URL = "http://web.apexsions.my.id/vote";

    public VoteCommand(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Component divider = mm.deserialize("<gradient:#f59e0b:#d97706>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gradient>");
        Component title = mm.deserialize("<gold>⚔ </gold><yellow><bold>APEXSIONS BILIK SUARA (VOTING)</bold></yellow><gold> ⚔</gold>");
        Component subtitle = mm.deserialize("<gray>Dukung kedaulatan peradaban Apexsions di peringkat dunia dan klaim imbalan pusaka Anda!</gray>");

        Component rewardHeader = mm.deserialize("<gold>Imbalan Resmi Tiap Suara Sah:</gold>");
        Component rewardKey = mm.deserialize("  <yellow>🎁 3x Kunci Peti Pusaka (Vote Keys)</yellow>");
        Component rewardMoney = mm.deserialize("  <green>💰 Rp 1.000 Saldo Peradaban</green>");

        Component clickPrompt = mm.deserialize("<click:open_url:'" + VOTE_URL + "'>"
                + "<hover:show_text:'<yellow>Klik di sini untuk membuka Bilik Suara Web Apexsions!</yellow>'>"
                + "<gradient:#fbbf24:#f59e0b><bold>[ 🗳 KLIK DI SINI UNTUK MEMBERIKAN SUARA ]</bold></gradient>"
                + "</hover></click>");

        Component footer = mm.deserialize("<dark_gray>Cooldown: 24 Jam per platform. Imbalan disinkronkan otomatis.</dark_gray>");

        sender.sendMessage(Component.empty());
        sender.sendMessage(divider);
        sender.sendMessage(title);
        sender.sendMessage(subtitle);
        sender.sendMessage(Component.empty());
        sender.sendMessage(rewardHeader);
        sender.sendMessage(rewardKey);
        sender.sendMessage(rewardMoney);
        sender.sendMessage(Component.empty());
        sender.sendMessage(clickPrompt);
        sender.sendMessage(footer);
        sender.sendMessage(divider);
        sender.sendMessage(Component.empty());

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
