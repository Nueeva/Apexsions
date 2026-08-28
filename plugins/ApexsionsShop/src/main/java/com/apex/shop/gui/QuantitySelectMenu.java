package com.apex.shop.gui;

import com.apex.shop.ApexsionsShop;
import com.apex.shop.category.ShopItem;
import com.apex.shop.dynamic.DynamicPriceCalculator.PriceResult;
import com.apex.shop.gui.core.ShopGui;
import com.apex.shop.gui.core.ShopGuiButton;
import com.apex.shop.gui.core.ShopItemBuilder;
import com.apex.shop.gui.navigation.BackButton;
import com.apex.shop.util.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class QuantitySelectMenu extends ShopGui {

    private final ShopItem shopItem;

    public QuantitySelectMenu(ApexsionsShop plugin, Player player, ShopItem shopItem, ShopGui parent) {
        super(plugin, player, "<dark_gray>[ TRANSAKSI: " + shopItem.getDisplayName() + " ]</dark_gray>", 45, parent);
        this.shopItem = shopItem;
    }

    @Override
    public void initialize() {
        fillBorder();

        PriceResult buy1 = plugin.getDynamicPriceCalculator().calculateBuyPrice(shopItem, player, 1);
        PriceResult buy64 = plugin.getDynamicPriceCalculator().calculateBuyPrice(shopItem, player, 64);
        PriceResult sell1 = plugin.getDynamicPriceCalculator().calculateSellPrice(shopItem, player, 1);
        PriceResult sell64 = plugin.getDynamicPriceCalculator().calculateSellPrice(shopItem, player, 64);

        int playerHas = InventoryUtil.countItems(player, shopItem.getMaterial());
        double balance = plugin.getEconomyHook().getBalance(player);

        // 1. Center Item Preview Card (Slot 4)
        List<String> lore = new ArrayList<>();
        lore.add("<gray>Kategori: <gold>" + shopItem.getCategory().getDisplayName() + "</gold></gray>");
        lore.add("<dark_gray>────────────────────────</dark_gray>");
        lore.add("<green>Harga Beli: <gold>" + plugin.getEconomyHook().format(buy1.finalTotalPrice()) + "</gold> <gray>(64x: " + plugin.getEconomyHook().format(buy64.finalTotalPrice()) + ")</gray></green>");
        lore.add("<red>Harga Jual: <gold>" + plugin.getEconomyHook().format(sell1.finalTotalPrice()) + "</gold> <gray>(64x: " + plugin.getEconomyHook().format(sell64.finalTotalPrice()) + ")</gray></red>");
        lore.add("<dark_gray>────────────────────────</dark_gray>");
        lore.add("<gray>Saldo Kamu: <yellow>" + plugin.getEconomyHook().format(balance) + "</yellow></gray>");
        lore.add("<gray>Dimiliki di Tas: <yellow>" + playerHas + " butir</yellow></gray>");
        lore.add("<gray>Pajak Kerajaan: <red>" + String.format("%.1f", buy1.taxPercent()) + "%</red></gray>");

        setButton(4, new ShopGuiButton(new ShopItemBuilder(shopItem.getMaterial())
                .name(shopItem.getDisplayName())
                .lore(lore)
                .hideAttributes()
                .build()));

        // 2. Buy Buttons (Row 2: Slots 10, 11, 12, 13, 14)
        int[] buyAmounts = { 1, 16, 32, 64 };
        int[] buySlots = { 10, 11, 12, 13 };

        for (int i = 0; i < buyAmounts.length; i++) {
            int qty = buyAmounts[i];
            int slot = buySlots[i];
            PriceResult res = plugin.getDynamicPriceCalculator().calculateBuyPrice(shopItem, player, qty);

            setButton(slot, new ShopGuiButton(new ShopItemBuilder(Material.LIME_DYE, Math.min(64, qty))
                    .name("<green><bold>BELI " + qty + "x</bold></green>")
                    .lore(List.of(
                            "<gray>Total Biaya: <gold>" + plugin.getEconomyHook().format(res.finalTotalPrice()) + "</gold></gray>",
                            "<dark_gray>Termasuk Pajak (" + String.format("%.1f", res.taxPercent()) + "%): " + plugin.getEconomyHook().format(res.taxAmount()) + "</dark_gray>",
                            " ",
                            "<yellow>Sentuh / Klik untuk Beli ▶</yellow>"
                    ))
                    .build(), event -> {
                buy(qty);
            }));
        }

        // Buy Max Button (Slot 14)
        int rawAffordable = buy1.effectiveUnitPrice() > 0 ? (int) (balance / (buy1.effectiveUnitPrice() * (1.0 + buy1.taxPercent() / 100.0))) : 0;
        final int maxAffordable = Math.max(0, rawAffordable);
        PriceResult maxBuyRes = plugin.getDynamicPriceCalculator().calculateBuyPrice(shopItem, player, Math.max(1, maxAffordable));
        setButton(14, new ShopGuiButton(new ShopItemBuilder(Material.EMERALD)
                .name("<green><bold>BELI MAKSIMAL (" + maxAffordable + "x)</bold></green>")
                .lore(List.of(
                        "<gray>Mampu Dibeli: <yellow>" + maxAffordable + " butir</yellow></gray>",
                        "<gray>Total Biaya: <gold>" + plugin.getEconomyHook().format(maxAffordable > 0 ? maxBuyRes.finalTotalPrice() : 0) + "</gold></gray>",
                        " ",
                        "<yellow>Sentuh / Klik untuk Beli Semua ▶</yellow>"
                ))
                .build(), event -> {
            if (maxAffordable > 0) buy(maxAffordable);
            else {
                player.sendMessage(MM.deserialize(plugin.getConfigManager().getMessage("not-enough-money", "<red>Saldo tidak cukup!</red>")
                        .replace("%required%", plugin.getEconomyHook().format(buy1.finalTotalPrice()))));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }));

        // 3. Sell Buttons (Row 3: Slots 19, 20, 21, 22, 23)
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
                            "<dark_gray>Potongan Pajak (" + String.format("%.1f", res.taxPercent()) + "%): " + plugin.getEconomyHook().format(res.taxAmount()) + "</dark_gray>",
                            " ",
                            "<yellow>Sentuh / Klik untuk Jual ▶</yellow>"
                    ))
                    .build(), event -> {
                sell(qty);
            }));
        }

        // Sell All Button (Slot 23)
        PriceResult maxSellRes = plugin.getDynamicPriceCalculator().calculateSellPrice(shopItem, player, Math.max(1, playerHas));
        setButton(23, new ShopGuiButton(new ShopItemBuilder(Material.CHEST)
                .name("<red><bold>JUAL SEMUA (" + playerHas + "x)</bold></red>")
                .lore(List.of(
                        "<gray>Total Dimiliki: <yellow>" + playerHas + " butir</yellow></gray>",
                        "<gray>Hasil Jual: <gold>" + plugin.getEconomyHook().format(playerHas > 0 ? maxSellRes.finalTotalPrice() : 0) + "</gold></gray>",
                        " ",
                        "<yellow>Sentuh / Klik untuk Jual Semua ▶</yellow>"
                ))
                .build(), event -> {
            sell(playerHas);
        }));

        // 4. Navigation (Bottom Row: Slot 40)
        setButton(40, new BackButton(this, parent));
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
        if (!InventoryUtil.hasEnoughSpace(player, toAdd)) {
            player.sendMessage(MM.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.inventory-full", "<red>Inventori penuh!</red>")));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

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

        int playerHas = InventoryUtil.countItems(player, shopItem.getMaterial());
        int actualQuantity = Math.min(quantity, playerHas);
        if (actualQuantity <= 0) {
            player.sendMessage(MM.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.not-enough-items", "<red>Kamu tidak memiliki item!</red>")
                            .replace("%item%", shopItem.getDisplayName())));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        PriceResult result = plugin.getDynamicPriceCalculator().calculateSellPrice(shopItem, player, actualQuantity);
        double payout = result.finalTotalPrice();

        InventoryUtil.removeItems(player, shopItem.getMaterial(), actualQuantity);
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
}
