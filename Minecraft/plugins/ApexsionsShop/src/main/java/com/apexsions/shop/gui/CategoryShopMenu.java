package com.apexsions.shop.gui;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.category.ShopCategory;
import com.apexsions.shop.category.ShopItem;
import com.apexsions.shop.dynamic.DynamicPriceCalculator.PriceResult;
import com.apexsions.shop.gui.core.ShopGui;
import com.apexsions.shop.gui.core.ShopGuiButton;
import com.apexsions.shop.gui.core.ShopItemBuilder;
import com.apexsions.shop.gui.navigation.BackButton;
import com.apexsions.shop.gui.navigation.CloseButton;
import com.apexsions.shop.util.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CategoryShopMenu extends ShopGui {

    private static final int ITEMS_PER_PAGE = 28;
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final ShopCategory category;
    private final int page;

    public CategoryShopMenu(ApexsionsShop plugin, Player player, ShopCategory category, ShopGui parent, int page) {
        super(plugin, player, plugin.getConfigManager().getGuiConfig().getString("titles.category-menu", "<dark_gray>[ TOKO: %category% ]</dark_gray>")
                .replace("%category%", category.getDisplayName()), 54, parent);
        this.category = category;
        this.page = Math.max(1, page);
    }

    @Override
    public void initialize() {
        fillBorder();

        List<ShopItem> allItems = plugin.getItemRegistry().getItemsByCategory(category);
        int maxPages = Math.max(1, (int) Math.ceil((double) allItems.size() / ITEMS_PER_PAGE));
        int validPage = Math.max(1, Math.min(maxPages, page));

        int startIndex = (validPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allItems.size());

        // Header Category Info (Slot 4)
        double balance = plugin.getEconomyHook().getBalance(player);
        setButton(4, new ShopGuiButton(new ShopItemBuilder(category.getIcon())
                .name("<gold><bold>" + category.getDisplayName() + "</bold></gold>")
                .lore(List.of(
                        category.getDescription(),
                        "<gray>Saldo Kamu: <yellow>" + plugin.getEconomyHook().format(balance) + "</yellow></gray>",
                        "<gray>Pajak Wilayah: <red>" + String.format("%.1f", plugin.getTaxService().getTaxPercent(player)) + "%</red></gray>"
                ))
                .build()));

        // Populate Items in Grid
        int slotIdx = 0;
        for (int i = startIndex; i < endIndex; i++) {
            if (slotIdx >= SLOTS.length) break;
            ShopItem item = allItems.get(i);
            int currentSlot = SLOTS[slotIdx++];

            PriceResult buy1 = plugin.getDynamicPriceCalculator().calculateBuyPrice(item, player, 1);
            PriceResult buy64 = plugin.getDynamicPriceCalculator().calculateBuyPrice(item, player, 64);
            PriceResult sell1 = plugin.getDynamicPriceCalculator().calculateSellPrice(item, player, 1);
            PriceResult sell64 = plugin.getDynamicPriceCalculator().calculateSellPrice(item, player, 64);

            int playerHas = InventoryUtil.countItems(player, item.getMaterial());

            List<String> lore = new ArrayList<>();
            lore.add("<dark_gray>────────────────────────</dark_gray>");
            if (item.isBuyEnabled()) {
                lore.add("<green>Harga Beli: <gold>" + plugin.getEconomyHook().format(buy1.finalTotalPrice()) + "</gold> <gray>(64x: " + plugin.getEconomyHook().format(buy64.finalTotalPrice()) + ")</gray></green>");
            } else {
                lore.add("<red>Harga Beli: <gray>[PEMBELIAN DITUTUP]</gray></red>");
            }
            lore.add("<red>Harga Jual: <gold>" + plugin.getEconomyHook().format(sell1.finalTotalPrice()) + "</gold> <gray>(64x: " + plugin.getEconomyHook().format(sell64.finalTotalPrice()) + ")</gray></red>");
            lore.add("<dark_gray>────────────────────────</dark_gray>");
            lore.add("<gray>Di Tas Kamu: <yellow>" + playerHas + " butir</yellow></gray>");

            // Dynamic factors badges
            if (buy1.supplyMultiplier() < 0.95) {
                lore.add("<green><bold>🟢 PASOKAN MELIMPAH (-" + String.format("%.0f", (1.0 - buy1.supplyMultiplier()) * 100) + "% Diskon)</bold></green>");
            } else if (buy1.supplyMultiplier() > 1.05) {
                lore.add("<red><bold>🔴 LANGKA / PERMINTAAN TINGGI (+" + String.format("%.0f", (buy1.supplyMultiplier() - 1.0) * 100) + "% Nilai)</bold></red>");
            }

            if (buy1.weatherMultiplier() != 1.0) {
                lore.add("<aqua><bold>⚡ PENGARUH CUACA DUNIA (" + String.format("%.0f", buy1.weatherMultiplier() * 100) + "%)</bold></aqua>");
            }

            if (buy1.kingdomMultiplier() < 1.0) {
                lore.add("<gold><bold>👑 DISKON KERAJAAN LOKAL (" + String.format("%.0f", (1.0 - buy1.kingdomMultiplier()) * 100) + "% Hemat)</bold></gold>");
            }

            if (!item.isBuyEnabled()) {
                lore.add("<yellow><bold>⚠ HANYA BISA DIJUAL (SELL ONLY)</bold></yellow>");
            }
            if (plugin.getConfig().getBoolean("tax.show-tax-in-lore", true)) {
                lore.add("<dark_gray>Termasuk Pajak Kerajaan (" + String.format("%.1f", buy1.taxPercent()) + "%)</dark_gray>");
            }
            lore.add(" ");
            lore.add(item.isBuyEnabled() ? "<yellow>Sentuh / Klik untuk Beli / Jual ▶</yellow>" : "<yellow>Sentuh / Klik untuk Jual Item ▶</yellow>");

            ItemStack displayItem = new ShopItemBuilder(item.getMaterial())
                    .name(item.getDisplayName())
                    .lore(lore)
                    .hideAttributes()
                    .build();

            setButton(currentSlot, new ShopGuiButton(displayItem, event -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
                new QuantitySelectMenu(plugin, player, item, this).open();
            }));
        }

        // Navigation (Row 5: slots 45..53)
        setButton(45, new BackButton(this, parent));

        if (validPage > 1) {
            setButton(48, new ShopGuiButton(new ShopItemBuilder(Material.PAPER)
                    .name("<yellow>◀ Halaman " + (validPage - 1) + "</yellow>")
                    .build(), event -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
                new CategoryShopMenu(plugin, player, category, parent, validPage - 1).open();
            }));
        }

        setButton(49, new ShopGuiButton(new ShopItemBuilder(Material.MAP)
                .name("<gray>Halaman <yellow>" + validPage + "</yellow> / <white>" + maxPages + "</white></gray>")
                .build()));

        if (validPage < maxPages) {
            setButton(50, new ShopGuiButton(new ShopItemBuilder(Material.PAPER)
                    .name("<yellow>Halaman " + (validPage + 1) + " ▶</yellow>")
                    .build(), event -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
                new CategoryShopMenu(plugin, player, category, parent, validPage + 1).open();
            }));
        }

        setButton(53, new CloseButton());
    }
}
