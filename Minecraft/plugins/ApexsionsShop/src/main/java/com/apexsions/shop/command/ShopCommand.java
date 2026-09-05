package com.apexsions.shop.command;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.category.ShopCategory;
import com.apexsions.shop.gui.CategoryShopMenu;
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

public class ShopCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsShop plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ShopCommand(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("apexsionsshop.admin")) {
                sender.sendMessage(miniMessage.deserialize("<red>Kamu tidak memiliki izin untuk reload plugin!</red>"));
                return true;
            }
            plugin.reloadPluginConfig();
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.reload-success", "<green>Konfigurasi berhasil dimuat ulang!</green>")));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Perintah ini hanya dapat digunakan oleh pemain in-game!</red>"));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("trends")) {
            new com.apexsions.shop.gui.MarketTrendsMenu(plugin, player, null).open();
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            if (!sender.hasPermission("apexsionsshop.admin")) {
                sender.sendMessage(miniMessage.deserialize("<red>Kamu tidak memiliki izin untuk menggunakan perintah admin shop!</red>"));
                return true;
            }
            if (args.length >= 2) {
                String targetKingdom = args[1].toUpperCase();
                if (targetKingdom.equals("ZENITHAR") || targetKingdom.equals("SOLTERRA") || targetKingdom.equals("SYLVAMOOR")) {
                    new ShopMainMenu(plugin, player, targetKingdom).open();
                    return true;
                }
            }
            new com.apexsions.shop.gui.AdminKingdomShopSelectorGUI(plugin, player).open();
            return true;
        }

        if (args.length > 0) {
            ShopCategory category = ShopCategory.fromId(args[0]);
            if (category != null) {
                new CategoryShopMenu(plugin, player, category, null, 1).open();
                return true;
            }
        }

        new ShopMainMenu(plugin, player).open();
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            if (sender.hasPermission("apexsionsshop.admin")) {
                if ("reload".startsWith(input)) completions.add("reload");
                if ("admin".startsWith(input)) completions.add("admin");
            }
            if ("trends".startsWith(input)) {
                completions.add("trends");
            }
            for (ShopCategory cat : ShopCategory.values()) {
                if (cat.getId().startsWith(input)) {
                    completions.add(cat.getId());
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("admin") && sender.hasPermission("apexsionsshop.admin")) {
            String input = args[1].toLowerCase();
            for (String k : List.of("zenithar", "solterra", "sylvamoor")) {
                if (k.startsWith(input)) {
                    completions.add(k);
                }
            }
        }
        return completions;
    }
}
