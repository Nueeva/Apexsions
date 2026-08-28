package com.apex.shop.command;

import com.apex.shop.ApexsionsShop;
import com.apex.shop.category.ShopCategory;
import com.apex.shop.gui.CategoryShopMenu;
import com.apex.shop.gui.ShopMainMenu;
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
            if (sender.hasPermission("apexsionsshop.admin") && "reload".startsWith(input)) {
                completions.add("reload");
            }
            for (ShopCategory cat : ShopCategory.values()) {
                if (cat.getId().startsWith(input)) {
                    completions.add(cat.getId());
                }
            }
        }
        return completions;
    }
}
