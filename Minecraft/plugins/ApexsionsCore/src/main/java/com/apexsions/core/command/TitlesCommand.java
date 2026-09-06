package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.title.TitleItem;
import com.apexsions.core.title.gui.TitleVaultGUI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Handles /titles, /tags, and /title commands.
 * Supports GUI vault opening, as well as console/admin commands for web delivery execution:
 * - /titles (opens GUI)
 * - /titles equip <player> <titleId>
 * - /titles unequip <player>
 * - /titles unlock <player> <titleId>
 */
public class TitlesCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public TitlesCommand(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(mm.deserialize("<red>Konsol harus menyertakan sub-perintah: <yellow>/titles <equip|unequip|unlock> <player> [titleId]</yellow></red>"));
                return true;
            }
            new TitleVaultGUI(plugin, player).open();
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "equip" -> {
                if (args.length < 3) {
                    sender.sendMessage(mm.deserialize("<red>Penggunaan: /titles equip <player> <titleId></red>"));
                    return true;
                }
                if (!sender.hasPermission("apexsionscore.admin") && !sender.isOp() && !(sender instanceof org.bukkit.command.ConsoleCommandSender)) {
                    sender.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk mengubah gelar pemain lain.</red>"));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(mm.deserialize("<red>Pemain '" + args[1] + "' tidak ditemukan atau sedang offline.</red>"));
                    return true;
                }
                String titleId = args[2].toLowerCase();
                Optional<TitleItem> titleOpt = plugin.getTitleManager().getTitle(titleId);
                if (titleOpt.isEmpty()) {
                    sender.sendMessage(mm.deserialize("<red>Gelar dengan ID '" + titleId + "' tidak terdaftar di sistem.</red>"));
                    return true;
                }

                TitleItem title = titleOpt.get();
                plugin.getTitleManager().equipTitle(target, title);
                target.sendMessage(mm.deserialize("<green>✓ Gelar kehormatan Anda telah diperbarui menjadi </green>" + title.getDisplayName() + "<green>!</green>"));
                sender.sendMessage(mm.deserialize("<green>Berhasil memasangkan gelar " + title.getDisplayName() + " ke pemain " + target.getName() + ".</green>"));

                if (plugin.getWebBridgeService() != null) {
                    plugin.getWebBridgeService().syncPlayerAsync(target);
                }
                return true;
            }

            case "unequip" -> {
                if (args.length < 2) {
                    if (sender instanceof Player p) {
                        plugin.getTitleManager().unequipTitle(p);
                        p.sendMessage(mm.deserialize("<yellow>Gelar kehormatan Anda telah dilepas.</yellow>"));
                        if (plugin.getWebBridgeService() != null) {
                            plugin.getWebBridgeService().syncPlayerAsync(p);
                        }
                        return true;
                    }
                    sender.sendMessage(mm.deserialize("<red>Penggunaan: /titles unequip <player></red>"));
                    return true;
                }
                if (!sender.hasPermission("apexsionscore.admin") && !sender.isOp() && !(sender instanceof org.bukkit.command.ConsoleCommandSender)) {
                    sender.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk melepas gelar pemain lain.</red>"));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(mm.deserialize("<red>Pemain '" + args[1] + "' tidak ditemukan atau sedang offline.</red>"));
                    return true;
                }
                plugin.getTitleManager().unequipTitle(target);
                target.sendMessage(mm.deserialize("<yellow>Gelar kehormatan Anda telah dilepas.</yellow>"));
                sender.sendMessage(mm.deserialize("<green>Berhasil melepas gelar untuk pemain " + target.getName() + ".</green>"));

                if (plugin.getWebBridgeService() != null) {
                    plugin.getWebBridgeService().syncPlayerAsync(target);
                }
                return true;
            }

            case "unlock" -> {
                if (args.length < 3) {
                    sender.sendMessage(mm.deserialize("<red>Penggunaan: /titles unlock <player> <titleId></red>"));
                    return true;
                }
                if (!sender.hasPermission("apexsionscore.admin") && !sender.isOp() && !(sender instanceof org.bukkit.command.ConsoleCommandSender)) {
                    sender.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk membuka gelar pemain.</red>"));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(mm.deserialize("<red>Pemain '" + args[1] + "' tidak ditemukan atau sedang offline.</red>"));
                    return true;
                }
                String titleId = args[2].toLowerCase();
                PlayerData pData = plugin.getPlayerDataService().getCached(target.getUniqueId()).orElse(null);
                if (pData != null) {
                    pData.getUnlockedTitles().add(titleId);
                    sender.sendMessage(mm.deserialize("<green>Gelar '" + titleId + "' berhasil dibuka untuk " + target.getName() + ".</green>"));
                    target.sendMessage(mm.deserialize("<aqua>✦ Anda telah membuka gelar kehormatan baru: <gold>" + titleId + "</gold>!</aqua>"));
                    if (plugin.getWebBridgeService() != null) {
                        plugin.getWebBridgeService().syncPlayerAsync(target);
                    }
                }
                return true;
            }

            default -> {
                if (sender instanceof Player p) {
                    new TitleVaultGUI(plugin, p).open();
                } else {
                    sender.sendMessage(mm.deserialize("<red>Perintah tidak dikenal. Gunakan: /titles <equip|unequip|unlock></red>"));
                }
                return true;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("equip", "unequip", "unlock");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("equip") || args[0].equalsIgnoreCase("unequip") || args[0].equalsIgnoreCase("unlock"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).toList();
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("equip") || args[0].equalsIgnoreCase("unlock"))) {
            List<String> titles = new ArrayList<>();
            for (TitleItem item : plugin.getTitleManager().getAllTitles()) {
                titles.add(item.getId());
            }
            return titles.stream().filter(t -> t.toLowerCase().startsWith(args[2].toLowerCase())).toList();
        }
        return Collections.emptyList();
    }
}

