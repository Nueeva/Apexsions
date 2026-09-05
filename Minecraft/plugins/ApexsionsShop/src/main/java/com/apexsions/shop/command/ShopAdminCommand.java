package com.apexsions.shop.command;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.gui.AdminKingdomShopSelectorGUI;
import com.apexsions.shop.gui.ShopMainMenu;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dedicated Admin command (/shopadmin) for managing the shop and inspecting
 * all 3 Kingdom Shops (Zenithar, Solterra, Sylvamoor).
 */
public class ShopAdminCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsShop plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final List<String> KINGDOMS = List.of("zenithar", "solterra", "sylvamoor");

    public ShopAdminCommand(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("apexsionsshop.admin")) {
            sender.sendMessage(miniMessage.deserialize("<red>Kamu tidak memiliki izin untuk menggunakan perintah admin shop!</red>"));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPluginConfig();
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.reload-success", "<green>Konfigurasi berhasil dimuat ulang!</green>")));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Perintah inspeksi toko hanya dapat digunakan oleh pemain in-game!</red>"));
            return true;
        }

        // /shopadmin view <kingdom>
        if (args.length >= 2 && args[0].equalsIgnoreCase("view")) {
            String targetKingdom = args[1].toUpperCase(Locale.ROOT);
            if (!targetKingdom.equals("ZENITHAR") && !targetKingdom.equals("SOLTERRA") && !targetKingdom.equals("SYLVAMOOR")) {
                player.sendMessage(miniMessage.deserialize("<red>Kerajaan tidak dikenal! Pilih antara: <yellow>zenithar, solterra, sylvamoor</yellow></red>"));
                return true;
            }
            player.sendMessage(miniMessage.deserialize("<green>Membuka katalog pasar kerajaan: <gold><bold>" + targetKingdom + "</bold></gold> (Admin Preview)...</green>"));
            new ShopMainMenu(plugin, player, targetKingdom).open();
            return true;
        }

        // /shopadmin <kingdom> direct shortcut
        if (args.length == 1) {
            String arg = args[0].toUpperCase(Locale.ROOT);
            if (arg.equals("ZENITHAR") || arg.equals("SOLTERRA") || arg.equals("SYLVAMOOR")) {
                player.sendMessage(miniMessage.deserialize("<green>Membuka katalog pasar kerajaan: <gold><bold>" + arg + "</bold></gold> (Admin Preview)...</green>"));
                new ShopMainMenu(plugin, player, arg).open();
                return true;
            }
        }

        // Default /shopadmin or /shopadmin kingdoms: Open 3-Kingdom Selector GUI
        new AdminKingdomShopSelectorGUI(plugin, player).open();
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("apexsionsshop.admin")) {
            return List.of();
        }

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            for (String sub : List.of("view", "kingdoms", "reload", "zenithar", "solterra", "sylvamoor")) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("view")) {
            String input = args[1].toLowerCase(Locale.ROOT);
            for (String k : KINGDOMS) {
                if (k.startsWith(input)) {
                    completions.add(k);
                }
            }
        }

        return completions;
    }
}
