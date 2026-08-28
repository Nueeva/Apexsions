package com.apex.shop.gui;

import com.apex.shop.ApexsionsShop;
import com.apex.shop.category.ShopCategory;
import com.apex.shop.category.ShopItem;
import com.apex.shop.dynamic.DynamicPriceCalculator.PriceResult;
import com.apex.shop.gui.core.ShopGui;
import com.apex.shop.gui.core.ShopGuiButton;
import com.apex.shop.gui.core.ShopItemBuilder;
import com.apex.shop.gui.navigation.BackButton;
import com.apex.shop.gui.navigation.CloseButton;
import com.apex.shop.util.InventoryUtil;
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
            lore.add("<green>Harga Beli: <gold>" + plugin.getEconomyHook().format(buy1.finalTotalPrice()) + "</gold> <gray>(64x: " + plugin.getEconomyHook().format(buy64.finalTotalPrice()) + ")</gray></green>");
            lore.add("<red>Harga Jual: <gold>" + plugin.getEconomyHook().format(sell1.finalTotalPrice()) + "</gold> <gray>(64x: " + plugin.getEconomyHook().format(sell64.finalTotalPrice()) + ")</gray></red>");
            lore.add("<dark_gray>────────────────────────</dark_gray>");
            lore.add("<gray>Di Tas Kamu: <yellow>" + playerHas + " butir</yellow></gray>");

            // Dynamic factors indicator
            if (buy1.weatherMultiplier() != 1.0 || buy1.kingdomMultiplier() != 1.0 || sell1.weatherMultiplier() != 1.0) {
                lore.add("<aqua>⚡ Pengaruh Pasar Dinamis Aktif</aqua>");
            }
            if (plugin.getConfig().getBoolean("tax.show-tax-in-lore", true)) {
                lore.add("<dark_gray>Termasuk Pajak Kerajaan (" + String.format("%.1f", buy1.taxPercent()) + "%)</dark_gray>");
            }
            lore.add(" ");
            lore.add("<yellow>Sentuh / Klik untuk Beli / Jual ▶</yellow>");

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
