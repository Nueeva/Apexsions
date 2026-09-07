package com.apexsions.crates.command;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.shop.gui.CrateKeyShopAdminGUI;
import com.apexsions.crates.shop.gui.CrateKeyShopGUI;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
import java.util.Locale;

/**
 * Dedicated command handler for /crateshop, /keyshop, /cratekeyshop.
 */
public class CrateShopCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCratesPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public CrateShopCommand(@NotNull ApexsionsCratesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                plugin.getKeyShopManager().load();
                sender.sendMessage(mm.deserialize("<green>Konfigurasi Crate Key Shop berhasil dimuat ulang!</green>"));
                return true;
            }
            sender.sendMessage(mm.deserialize("<red>Perintah ini hanya dapat dijalankan oleh pemain!</red>"));
            return true;
        }

        if (args.length > 0) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("admin") || sub.equals("manage")) {
                if (!player.hasPermission("apexsions.admin") && !player.hasPermission("apexsions.crates.admin")) {
                    player.sendMessage(mm.deserialize("<red>Kamu tidak memiliki izin untuk mengakses pengaturan admin toko kunci!</red>"));
                    return true;
                }
                new CrateKeyShopAdminGUI(plugin, player).open();
                return true;
            }

            if (sub.equals("reload")) {
                if (!player.hasPermission("apexsions.admin") && !player.hasPermission("apexsions.crates.admin")) {
                    player.sendMessage(mm.deserialize("<red>Kamu tidak memiliki izin untuk memuat ulang toko kunci!</red>"));
                    return true;
                }
                plugin.getKeyShopManager().load();
                player.sendMessage(mm.deserialize("<green>Konfigurasi Crate Key Shop berhasil dimuat ulang!</green>"));
                return true;
            }
        }

        // Default: Open player crate key shop GUI
        new CrateKeyShopGUI(plugin, player).open();
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("apexsions.admin") || sender.hasPermission("apexsions.crates.admin")) {
                completions.add("admin");
                completions.add("reload");
            }
            String current = args[0].toLowerCase(Locale.ROOT);
            return completions.stream()
                    .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(current))
                    .toList();
        }
        return Collections.emptyList();
    }
}
