package com.apexsions.shop.gui;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.category.ShopItem;
import com.apexsions.shop.dynamic.DynamicPriceCalculator.PriceResult;
import com.apexsions.shop.gui.navigation.BackButton;
import com.apexsions.shop.gui.core.ShopGui;
import com.apexsions.shop.gui.core.ShopGuiButton;
import com.apexsions.shop.gui.core.ShopItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 54-Slot Market Trends & Economy Insights Dashboard.
 */
public class MarketTrendsMenu extends ShopGui {

    public MarketTrendsMenu(ApexsionsShop plugin, Player player, ShopGui parent) {
        super(plugin, player, "<dark_gray><bold>[ TREN PASAR & EKONOMI ]</bold></dark_gray>", 54, parent);
    }

    @Override
    public void initialize() {
        fillBorder();

        // Slot 4: Market Climate Header
        String weatherDesc = plugin.getWeatherPriceService().getWeatherDescription(player.getWorld());
        String kingdomName = plugin.getKingdomMarketService().getKingdomNameFormatted(player);
        double tax = plugin.getTaxService().getTaxPercent(player);

        setButton(4, new ShopGuiButton(new ShopItemBuilder(Material.SPYGLASS)
                .name("<gradient:#f1c40f:#e67e22><bold>📊 IKHTISAR PASAR KERAJAAN</bold></gradient>")
                .lore(List.of(
                        "<gray>Dunia Saat Ini:</gray> <white>" + player.getWorld().getName() + "</white>",
                        "<gray>Kondisi Cuaca:</gray> <aqua>" + weatherDesc + "</aqua>",
                        "<gray>Afiliasi Kerajaan:</gray> " + kingdomName,
                        "<gray>Tarif Pajak Pasar:</gray> <red>" + String.format("%.1f", tax) + "%</red>",
                        " ",
                        "<yellow>Gunakan informasi tren harga untuk memaksimalkan keuntungan dagang!</yellow>"
                ))
                .glow()
                .build()));

        // Scan items with dynamic supply index
        List<ShopItem> allItems = new ArrayList<>(plugin.getItemRegistry().getAllItems());

        // Slots for top trending items: 20, 21, 22, 23, 24
        int[] trendSlots = { 20, 21, 22, 23, 24 };
        int slotIdx = 0;

        for (ShopItem item : allItems) {
            if (slotIdx >= trendSlots.length) break;

            PriceResult buyRes = plugin.getDynamicPriceCalculator().calculateBuyPrice(item, player, 1);
            PriceResult sellRes = plugin.getDynamicPriceCalculator().calculateSellPrice(item, player, 1);

            if (buyRes.supplyMultiplier() != 1.0 || buyRes.weatherMultiplier() != 1.0 || buyRes.kingdomMultiplier() != 1.0) {
                int currentSlot = trendSlots[slotIdx++];

                List<String> lore = new ArrayList<>();
                lore.add("<dark_gray>────────────────────────</dark_gray>");
                lore.add("<green>Harga Beli: <gold>" + plugin.getEconomyHook().format(buyRes.finalTotalPrice()) + "</gold></green>");
                lore.add("<red>Harga Jual: <gold>" + plugin.getEconomyHook().format(sellRes.finalTotalPrice()) + "</gold></red>");
                lore.add("<dark_gray>────────────────────────</dark_gray>");

                if (buyRes.supplyMultiplier() < 0.95) {
                    lore.add("<green><bold>🟢 PASOKAN MELIMPAH (Diskon Beli)</bold></green>");
                } else if (buyRes.supplyMultiplier() > 1.05) {
                    lore.add("<red><bold>🔴 LANGKA (Nilai Jual Naik)</bold></red>");
                }

                if (buyRes.weatherMultiplier() != 1.0) {
                    lore.add("<aqua><bold>⚡ PENGARUH CUACA DUNIA</bold></aqua>");
                }

                if (buyRes.kingdomMultiplier() < 1.0) {
                    lore.add("<gold><bold>👑 KOMODITAS UNGGULAN KERAJAAN</bold></gold>");
                }

                lore.add(" ");
                lore.add("<yellow>Sentuh / Klik untuk menuju transaksi ▶</yellow>");

                setButton(currentSlot, new ShopGuiButton(new ShopItemBuilder(item.getMaterial())
                        .name(item.getDisplayName())
                        .lore(lore)
                        .hideAttributes()
                        .build(), event -> {
                    new QuantitySelectMenu(plugin, player, item, this).open();
                }));
            }
        }

        // Slot 49: Back Button
        setButton(49, new BackButton(this, parent));
    }
}
