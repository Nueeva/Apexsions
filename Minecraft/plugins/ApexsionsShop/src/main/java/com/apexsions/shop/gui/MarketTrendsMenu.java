package com.apexsions.shop.gui;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.category.ShopItem;
import com.apexsions.shop.dynamic.DynamicPriceCalculator.PriceResult;
import com.apexsions.shop.dynamic.event.MarketEvent;
import com.apexsions.shop.gui.core.ShopGui;
import com.apexsions.shop.gui.core.ShopGuiButton;
import com.apexsions.shop.gui.core.ShopItemBuilder;
import com.apexsions.shop.gui.navigation.BackButton;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 54-Slot Interactive Market Trends & Live Commodity Ticker Dashboard.
 * Displays real-time price fluctuations, active realm economic events, and royal contracts.
 */
public class MarketTrendsMenu extends ShopGui {

    private String kingdomFilter;

    public MarketTrendsMenu(ApexsionsShop plugin, Player player, ShopGui parent) {
        this(plugin, player, parent, null);
    }

    public MarketTrendsMenu(ApexsionsShop plugin, Player player, ShopGui parent, String kingdomFilter) {
        super(plugin, player, "<dark_gray><bold>[ TREN PASAR & EKONOMI ]</bold></dark_gray>", 54, parent);
        this.kingdomFilter = kingdomFilter;
    }

    @Override
    public void initialize() {
        fillBorder();

        String effectiveKingdom = kingdomFilter != null
                ? kingdomFilter
                : (plugin.getKingdomMarketService() != null ? plugin.getKingdomMarketService().resolveKingdom(player, null) : "NONE");

        // 1. Slot 4: Active Economic Event & Climate Dashboard
        MarketEvent activeEvent = plugin.getMarketEventService() != null
                ? plugin.getMarketEventService().getActiveEvent()
                : MarketEvent.MARKET_EQUILIBRIUM;

        String timeLeft = plugin.getMarketEventService() != null
                ? plugin.getMarketEventService().getFormattedRemainingTime()
                : "04j 00m";

        String weatherDesc = plugin.getWeatherPriceService() != null
                ? plugin.getWeatherPriceService().getWeatherDescription(player.getWorld())
                : "<yellow>☀ Normal</yellow>";
        String kingdomName = plugin.getKingdomMarketService() != null
                ? plugin.getKingdomMarketService().getKingdomNameFormatted(effectiveKingdom)
                : "<gray>Tanpa Kerajaan</gray>";
        double tax = plugin.getTaxService() != null
                ? plugin.getTaxService().getTaxPercent(player, effectiveKingdom)
                : 0.0;

        setButton(4, new ShopGuiButton(new ShopItemBuilder(activeEvent.getIcon())
                .name("<gradient:" + activeEvent.getPrimaryColor() + ":" + activeEvent.getSecondaryColor() + "><bold>❖ " + activeEvent.getDisplayName() + "</bold></gradient>")
                .lore(List.of(
                        "<gray>" + activeEvent.getDescription() + "</gray>",
                        " ",
                        "<dark_gray>────────────────────────</dark_gray>",
                        "<gray>Sisa Waktu Peristiwa:</gray> <yellow><bold>" + timeLeft + "</bold></yellow>",
                        "<gray>Pasar Kerajaan Terpantau:</gray> " + kingdomName,
                        "<gray>Kondisi Cuaca:</gray> <aqua>" + weatherDesc + "</aqua>",
                        "<gray>Tarif Pajak Wilayah:</gray> <red>" + String.format("%.1f", tax) + "%</red>",
                        "<dark_gray>────────────────────────</dark_gray>",
                        " ",
                        "<yellow>Manfaatkan disparitas harga antar-wilayah untuk keuntungan dagang!</yellow>"
                ))
                .glow()
                .build()));

        // 2. Scan items with price fluctuations & trending volume in effective kingdom
        List<ShopItem> allItems = new ArrayList<>(plugin.getItemRegistry().getAllItems());
        int[] trendSlots = { 20, 21, 22, 23, 24, 29, 31, 33 };
        int slotIdx = 0;

        for (ShopItem item : allItems) {
            if (slotIdx >= trendSlots.length) break;

            PriceResult buyRes = plugin.getDynamicPriceCalculator().calculateBuyPrice(item, player, 1, effectiveKingdom);
            PriceResult sellRes = plugin.getDynamicPriceCalculator().calculateSellPrice(item, player, 1, effectiveKingdom);

            double baseSell = item.getBaseSellPrice();
            double effectiveSell = sellRes.effectiveUnitPrice();
            double pctDelta = baseSell > 0 ? ((effectiveSell / baseSell) - 1.0) * 100.0 : 0.0;

            boolean hasFluctuation = buyRes.supplyMultiplier() != 1.0
                    || buyRes.weatherMultiplier() != 1.0
                    || buyRes.kingdomMultiplier() != 1.0
                    || sellRes.eventMultiplier() != 1.0;

            if (hasFluctuation) {
                int currentSlot = trendSlots[slotIdx++];

                List<String> lore = new ArrayList<>();
                lore.add("<dark_gray>────────────────────────</dark_gray>");

                if (pctDelta > 0.5) {
                    lore.add("<green><bold>▲ +" + String.format("%.1f", pctDelta) + "% (Harga Jual Melonjak)</bold></green>");
                } else if (pctDelta < -0.5) {
                    lore.add("<red><bold>▼ " + String.format("%.1f", pctDelta) + "% (Pasokan Jenuh / Turun)</bold></red>");
                } else {
                    lore.add("<yellow><bold>▬ 0.0% (Harga Stabil)</bold></yellow>");
                }

                lore.add("<dark_gray>────────────────────────</dark_gray>");
                lore.add("<gray>Harga Beli:</gray> <gold>" + plugin.getEconomyHook().format(buyRes.finalTotalPrice()) + "</gold>");
                lore.add("<gray>Harga Jual Bersih:</gray> <green><bold>" + plugin.getEconomyHook().format(sellRes.finalTotalPrice()) + "</bold></green>");
                lore.add("<dark_gray>────────────────────────</dark_gray>");

                if (sellRes.eventMultiplier() > 1.0) {
                    int eventBonus = (int) Math.round((sellRes.eventMultiplier() - 1.0) * 100);
                    lore.add("<gold>❖ Event Peristiwa Realm: <green>+" + eventBonus + "%</green></gold>");
                }

                if (buyRes.supplyMultiplier() < 0.95) {
                    lore.add("<green>🟢 Pasokan Melimpah (Diskon Beli)</green>");
                } else if (buyRes.supplyMultiplier() > 1.05) {
                    lore.add("<red>🔴 Komoditas Langka (Permintaan Tinggi)</red>");
                }

                if (buyRes.kingdomMultiplier() > 1.05) {
                    lore.add("<yellow>⭐ Komoditas Impor Bernilai Tinggi</yellow>");
                } else if (buyRes.kingdomMultiplier() < 0.95) {
                    lore.add("<aqua>⛏ Komoditas Unggulan Lokal</aqua>");
                }

                if (buyRes.weatherMultiplier() != 1.0) {
                    lore.add("<aqua>⚡ Pengaruh Cuaca Dunia</aqua>");
                }

                lore.add(" ");
                lore.add("<yellow>Sentuh / Klik untuk transaksi langsung ▶</yellow>");

                setButton(currentSlot, new ShopGuiButton(new ShopItemBuilder(item.getMaterial())
                        .name(item.getDisplayName())
                        .lore(lore)
                        .hideAttributes()
                        .build(), event -> {
                    new QuantitySelectMenu(plugin, player, item, this, effectiveKingdom).open();
                }));
            }
        }

        // 3. Slot 39: Cycle Kingdom Market Filter (Arbitrage Intelligence)
        String nextKingdom = switch (effectiveKingdom.toUpperCase()) {
            case "SOLTERRA" -> "SYLVAMOOR";
            case "SYLVAMOOR" -> "ZENITHAR";
            case "ZENITHAR" -> "SOLTERRA";
            default -> "SOLTERRA";
        };
        setButton(39, new ShopGuiButton(new ShopItemBuilder(Material.SPYGLASS)
                .name("<gradient:#00c6ff:#0072ff><bold>🔭 INTEL PASAR: " + effectiveKingdom.toUpperCase() + "</bold></gradient>")
                .lore(List.of(
                        "<gray>Memantau fluktuasi komoditas di: " + kingdomName + "</gray>",
                        "<dark_gray>────────────────────────</dark_gray>",
                        "<gray>Bandingkan harga antar-kerajaan untuk mencari</gray>",
                        "<gray>jalur dagang arbitrase (beli murah, jual mahal)!</gray>",
                        " ",
                        "<yellow>Sentuh / Klik untuk pantau pasar <bold>" + nextKingdom + "</bold> ▶</yellow>"
                ))
                .build(), event -> {
            player.playSound(player.getLocation(), Sound.ITEM_SPYGLASS_USE, 0.8f, 1.2f);
            new MarketTrendsMenu(plugin, player, parent, nextKingdom).open();
        }));

        // 4. Slot 41: Navigation to Royal Trade Contracts
        setButton(41, new ShopGuiButton(new ShopItemBuilder(Material.WRITABLE_BOOK)
                .name("<gradient:#f1c40f:#e67e22><bold>📜 KONTRAK EKSPOR KERAJAAN</bold></gradient>")
                .lore(List.of(
                        "<gray>Setor komoditas yang dibutuhkan kerajaan</gray>",
                        "<gray>untuk meraih imbalan Rupiah ekstra & XP Kerajaan.</gray>",
                        " ",
                        "<yellow>Sentuh / Klik untuk Buka Kontrak ▶</yellow>"
                ))
                .glow()
                .build(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            new TradeContractsMenu(plugin, player, this).open();
        }));

        // 5. Slot 49: Back Button
        setButton(49, new BackButton(this, parent));
    }
}
