package com.apexsions.shop.command;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.category.ShopItem;
import com.apexsions.shop.dynamic.DynamicPriceCalculator.PriceResult;
import com.apexsions.shop.gui.SellGuiMenu;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SellCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsShop plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public SellCommand(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Perintah ini hanya dapat digunakan oleh pemain in-game!</red>"));
            return true;
        }

        if (args.length > 0) {
            String sub = args[0].toLowerCase();
            if (sub.equals("hand")) {
                sellHand(player);
                return true;
            } else if (sub.equals("all")) {
                sellAll(player);
                return true;
            } else if (sub.equals("gui")) {
                new SellGuiMenu(plugin, player).open();
                return true;
            }
        }

        if (label.equalsIgnoreCase("sellhand")) {
            sellHand(player);
            return true;
        } else if (label.equalsIgnoreCase("sellall")) {
            sellAll(player);
            return true;
        }

        // Default: Open Sell GUI
        new SellGuiMenu(plugin, player).open();
        return true;
    }

    private void sellHand(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType() == Material.AIR) {
            player.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                    "<red>Pegang item di tangan utama untuk menjual!</red>"));
            return;
        }

        ShopItem item = plugin.getItemRegistry().getItem(held.getType());
        if (item == null) {
            player.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.cannot-sell-item", "<red>Item ini tidak dapat dijual ke toko!</red>")));
            return;
        }

        int amount = held.getAmount();
        PriceResult res = plugin.getDynamicPriceCalculator().calculateSellPrice(item, player, amount);
        double payout = res.finalTotalPrice();

        player.getInventory().setItemInMainHand(null);
        plugin.getEconomyHook().deposit(player, payout);

        player.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                plugin.getConfig().getString("messages.sell-success", "<green>Berhasil menjual item!</green>")
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%item%", item.getDisplayName())
                        .replace("%price%", plugin.getEconomyHook().format(payout))
                        .replace("%tax%", plugin.getEconomyHook().format(res.taxAmount()))));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.6f);
    }

    private void sellAll(Player player) {
        double totalPayout = 0.0;
        double totalTax = 0.0;
        int totalItemsSold = 0;

        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack is = contents[i];
            if (is == null || is.getType() == Material.AIR) continue;

            ShopItem item = plugin.getItemRegistry().getItem(is.getType());
            if (item != null) {
                PriceResult res = plugin.getDynamicPriceCalculator().calculateSellPrice(item, player, is.getAmount());
                totalPayout += res.finalTotalPrice();
                totalTax += res.taxAmount();
                totalItemsSold += is.getAmount();
                contents[i] = null;
            }
        }

        if (totalItemsSold > 0) {
            player.getInventory().setStorageContents(contents);
            plugin.getEconomyHook().deposit(player, totalPayout);

            player.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.sell-success", "<green>Berhasil menjual seluruh item di inventori!</green>")
                            .replace("%amount%", String.valueOf(totalItemsSold))
                            .replace("%item%", "Item")
                            .replace("%price%", plugin.getEconomyHook().format(totalPayout))
                            .replace("%tax%", plugin.getEconomyHook().format(totalTax))));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.6f);
        } else {
            player.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.cannot-sell-item", "<red>Tidak ada item di inventori yang dapat dijual ke toko!</red>")));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (String sub : List.of("gui", "hand", "all")) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
        }
        return completions;
    }
}
