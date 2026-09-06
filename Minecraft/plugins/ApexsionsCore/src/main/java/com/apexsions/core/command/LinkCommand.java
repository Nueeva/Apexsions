package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.integration.AuthMeHook;
import com.apexsions.core.integration.web.WebBridgeService;
import com.apexsions.core.level.xp.XpSource;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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
 * Command executor for /link <pin>.
 * Links player's in-game Minecraft account with their Azuriom web account.
 */
public class LinkCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public LinkCommand(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Perintah ini hanya dapat dijalankan oleh pemain.</red>"));
            return true;
        }

        // 1. AuthMe login check
        if (!AuthMeHook.isAuthenticated(player)) {
            player.sendMessage(miniMessage.deserialize("<red>Anda harus login terlebih dahulu menggunakan <gold>/login <password></gold> sebelum dapat menautkan akun!</red>"));
            return true;
        }

        // 2. Argument check
        if (args.length < 1) {
            player.sendMessage(miniMessage.deserialize("<gradient:#c0392b:#8e1b1b><bold>APEXSIONS LINK</bold></gradient> <dark_gray>»</dark_gray> <gray>Buka website portal kami untuk mendapatkan PIN 6 digit:</gray>"));
            player.sendMessage(miniMessage.deserialize("  <gold><click:open_url:'https://web.apexsions.my.id/link'><u>https://web.apexsions.my.id/link</u></click></gold>"));
            player.sendMessage(miniMessage.deserialize("  <gray>Lalu jalankan: <yellow>/link <PIN_6_DIGIT></yellow></gray>"));
            return true;
        }

        String pin = args[0].trim();
        if (!pin.matches("^\\d{6}$")) {
            player.sendMessage(miniMessage.deserialize("<red>Format PIN tidak valid! PIN verifikasi harus berupa 6 digit angka (contoh: /link 123456).</red>"));
            return true;
        }

        player.sendMessage(miniMessage.deserialize("<yellow>Sedang memverifikasi PIN penautan akun dengan portal web Apexsions...</yellow>"));

        WebBridgeService bridge = plugin.getWebBridgeService();
        if (bridge == null) {
            player.sendMessage(miniMessage.deserialize("<red>Layanan Web Bridge tidak aktif saat ini. Hubungi administrator.</red>"));
            return true;
        }

        bridge.verifyLink(player, pin).thenAccept(result -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }

                if (result.success()) {
                    player.sendMessage(miniMessage.deserialize("<green><bold>BERHASIL!</bold> Akun Minecraft Anda telah resmi terhubung dengan akun web Apexsions.</green>"));
                    try {
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    } catch (Throwable ignored) {}

                    // Distribute linking rewards if configured
                    boolean rewardsEnabled = plugin.getConfig().getBoolean("web-bridge.link-rewards.enabled", true);
                    if (rewardsEnabled) {
                        int xpReward = plugin.getConfig().getInt("web-bridge.link-rewards.xp", 500);
                        double coinsReward = plugin.getConfig().getDouble("web-bridge.link-rewards.coins", 10000.0);

                        if (xpReward > 0 && plugin.getLevelManager() != null) {
                            plugin.getLevelManager().addXp(player.getUniqueId(), xpReward, XpSource.COMMAND);
                            player.sendMessage(miniMessage.deserialize("<aqua>✦ Hadiah Penautan: <gold>+" + xpReward + " Kingdom EXP</gold></aqua>"));
                        }

                        if (coinsReward > 0 && plugin.getVaultHook() != null && plugin.getVaultHook().hasEconomy()) {
                            plugin.getVaultHook().deposit(player, coinsReward);
                            player.sendMessage(miniMessage.deserialize("<aqua>✦ Hadiah Penautan: <gold>+Rp " + String.format("%,.0f", coinsReward) + " Koin</gold></aqua>"));
                        }

                        boolean broadcast = plugin.getConfig().getBoolean("web-bridge.link-rewards.broadcast", true);
                        if (broadcast) {
                            Bukkit.broadcast(miniMessage.deserialize("<gradient:#c0392b:#8e1b1b><b>APEXSIONS</b></gradient> <dark_gray>»</dark_gray> <yellow>" + player.getName() + "</yellow> <gray>telah menautkan akunnya ke portal web resmi!</gray>"));
                        }
                    }
                } else {
                    player.sendMessage(miniMessage.deserialize("<red><bold>GAGAL:</bold> " + result.message() + "</red>"));
                    try {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    } catch (Throwable ignored) {}
                }
            });
        });

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("<PIN_6_DIGIT>");
        }
        return Collections.emptyList();
    }
}
