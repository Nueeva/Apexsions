package com.apex.shop.gui;

import com.apex.shop.ApexsionsShop;
import com.apex.shop.category.ShopItem;
import com.apex.shop.dynamic.DynamicPriceCalculator.PriceResult;
import com.apex.shop.gui.core.ShopGui;
import com.apex.shop.gui.core.ShopGuiButton;
import com.apex.shop.gui.core.ShopItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class QuantitySelectMenu extends ShopGui {

    private final ShopItem shopItem;

    public QuantitySelectMenu(ApexsionsShop plugin, Player player, ShopItem shopItem, ShopGui parent) {
        super(plugin, player, "<dark_gray>[ JUMLAH: " + shopItem.getDisplayName() + " ]</dark_gray>", 45, parent);
        this.shopItem = shopItem;
    }

    @Override
    public void initialize() {
        fillBorder();

        // 1. Center Item Preview (Slot 4)
        setButton(4, new ShopGuiButton(new ShopItemBuilder(shopItem.getMaterial())
                .name(shopItem.getDisplayName())
                .lore(List.of(
                        "<gray>Kategori: <gold>" + shopItem.getCategory().getDisplayName() + "</gold></gray>",
                        "<gray>Pilih kuantitas transaksi di bawah.</gray>"
                ))
                .build()));

        // 2. Buy Options (Row 2: 10, 11, 12, 13, 14)
        int[] buyAmounts = { 1, 16, 32, 64, 128 };
        int[] buySlots = { 10, 11, 12, 13, 14 };

        for (int i = 0; i < buyAmounts.length; i++) {
            int qty = buyAmounts[i];
            int slot = buySlots[i];
            PriceResult res = plugin.getDynamicPriceCalculator().calculateBuyPrice(shopItem, player, qty);

            setButton(slot, new ShopGuiButton(new ShopItemBuilder(Material.LIME_DYE, Math.min(64, qty))
                    .name("<green><bold>BELI " + qty + "x</bold></green>")
                    .lore(List.of(
                            "<gray>Total Biaya: <gold>" + plugin.getEconomyHook().format(res.finalTotalPrice()) + "</gold></gray>",
                            "<dark_gray>Termasuk Pajak: " + plugin.getEconomyHook().format(res.taxAmount()) + "</dark_gray>",
                            " ",
                            "<yellow>Klik untuk konfirmasi pembelian ▶</yellow>"
                    ))
                    .build(), event -> {
                buy(qty);
            }));
        }

        // 3. Sell Options (Row 3: 19, 20, 21, 22, 23)
        int[] sellAmounts = { 1, 16, 32, 64 };
        int[] sellSlots = { 19, 20, 21, 22 };

        for (int i = 0; i < sellAmounts.length; i++) {
            int qty = sellAmounts[i];
            int slot = sellSlots[i];
            PriceResult res = plugin.getDynamicPriceCalculator().calculateSellPrice(shopItem, player, qty);

            setButton(slot, new ShopGuiButton(new ShopItemBuilder(Material.RED_DYE, Math.min(64, qty))
                    .name("<red><bold>JUAL " + qty + "x</bold></red>")
                    .lore(List.of(
                            "<gray>Hasil Jual: <gold>" + plugin.getEconomyHook().format(res.finalTotalPrice()) + "</gold></gray>",
                            "<dark_gray>Potongan Pajak: " + plugin.getEconomyHook().format(res.taxAmount()) + "</dark_gray>",
                            " ",
                            "<yellow>Klik untuk konfirmasi penjualan ▶</yellow>"
                    ))
                    .build(), event -> {
                sell(qty);
            }));
        }

        // Sell Max Button (Slot 23)
        int playerHas = countItems(player, shopItem.getMaterial());
        PriceResult maxSellRes = plugin.getDynamicPriceCalculator().calculateSellPrice(shopItem, player, Math.max(1, playerHas));
        setButton(23, new ShopGuiButton(new ShopItemBuilder(Material.CHEST)
                .name("<red><bold>JUAL SEMUA (" + playerHas + "x)</bold></red>")
                .lore(List.of(
                        "<gray>Total Dimiliki: <yellow>" + playerHas + "x</yellow></gray>",
                        "<gray>Hasil Jual: <gold>" + plugin.getEconomyHook().format(playerHas > 0 ? maxSellRes.finalTotalPrice() : 0) + "</gold></gray>",
                        " ",
                        "<yellow>Klik untuk menjual seluruh item ini ▶</yellow>"
                ))
                .build(), event -> {
            sell(playerHas);
        }));

        // Back Button (Slot 40)
        setButton(40, new ShopGuiButton(new ShopItemBuilder(Material.ARROW)
                .name("<yellow>◀ KEMBALI</yellow>")
                .build(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            if (parent != null) parent.open();
            else new ShopMainMenu(plugin, player).open();
        }));
    }

    private void buy(int quantity) {
        PriceResult result = plugin.getDynamicPriceCalculator().calculateBuyPrice(shopItem, player, quantity);
        double totalCost = result.finalTotalPrice();

        if (!plugin.getEconomyHook().has(player, totalCost)) {
            player.sendMessage(MM.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.not-enough-money", "<red>Saldo tidak cukup!</red>")
                            .replace("%required%", plugin.getEconomyHook().format(totalCost))));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        ItemStack toAdd = new ItemStack(shopItem.getMaterial(), quantity);
        if (plugin.getEconomyHook().withdraw(player, totalCost)) {
            player.getInventory().addItem(toAdd);
            player.sendMessage(MM.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.buy-success", "<green>Beli berhasil!</green>")
                            .replace("%amount%", String.valueOf(quantity))
                            .replace("%item%", shopItem.getDisplayName())
                            .replace("%price%", plugin.getEconomyHook().format(totalCost))
                            .replace("%tax%", plugin.getEconomyHook().format(result.taxAmount()))));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f);
            open();
        }
    }

    private void sell(int quantity) {
        if (quantity <= 0) {
            player.sendMessage(MM.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.not-enough-items", "<red>Kamu tidak memiliki item!</red>")
                            .replace("%item%", shopItem.getDisplayName())));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        int playerHas = countItems(player, shopItem.getMaterial());
        int actualQuantity = Math.min(quantity, playerHas);
        if (actualQuantity <= 0) return;

        PriceResult result = plugin.getDynamicPriceCalculator().calculateSellPrice(shopItem, player, actualQuantity);
        double payout = result.finalTotalPrice();

        removeItems(player, shopItem.getMaterial(), actualQuantity);
        plugin.getEconomyHook().deposit(player, payout);

        player.sendMessage(MM.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                plugin.getConfig().getString("messages.sell-success", "<green>Jual berhasil!</green>")
                        .replace("%amount%", String.valueOf(actualQuantity))
                        .replace("%item%", shopItem.getDisplayName())
                        .replace("%price%", plugin.getEconomyHook().format(payout))
                        .replace("%tax%", plugin.getEconomyHook().format(result.taxAmount()))));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.8f);
        open();
    }

    private int countItems(Player player, Material material) {
        int count = 0;
        for (ItemStack is : player.getInventory().getStorageContents()) {
            if (is != null && is.getType() == material) {
                count += is.getAmount();
            }
        }
        return count;
    }

    private void removeItems(Player player, Material material, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack is = contents[i];
            if (is != null && is.getType() == material) {
                if (is.getAmount() <= remaining) {
                    remaining -= is.getAmount();
                    contents[i] = null;
                } else {
                    is.setAmount(is.getAmount() - remaining);
                    remaining = 0;
                    break;
                }
            }
        }
        player.getInventory().setStorageContents(contents);
    }
}
