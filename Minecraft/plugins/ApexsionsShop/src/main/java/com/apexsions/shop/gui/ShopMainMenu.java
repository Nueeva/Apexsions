package com.apexsions.shop.gui;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.category.ShopCategory;
import com.apexsions.shop.gui.core.ShopGui;
import com.apexsions.shop.gui.core.ShopGuiButton;
import com.apexsions.shop.gui.core.ShopItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public class ShopMainMenu extends ShopGui {

    private final String kingdomOverride;

    public ShopMainMenu(ApexsionsShop plugin, Player player) {
        this(plugin, player, null);
    }

    public ShopMainMenu(ApexsionsShop plugin, Player player, String kingdomOverride) {
        super(plugin, player, kingdomOverride != null
                ? "<gradient:#f1c40f:#e67e22><bold>[ PASAR " + kingdomOverride.toUpperCase() + " ]</bold></gradient>"
                : plugin.getConfigManager().getGuiConfig().getString("titles.main-menu", "<dark_gray><bold>[ APEXSIONS MARKET ]</bold></dark_gray>"), 54);
        this.kingdomOverride = kingdomOverride;
    }

    @Override
    public void initialize() {
        fillBorder();

        // 1. Unified Player & Kingdom Info Banner (Slot 4 - Top Center)
        double balance = plugin.getEconomyHook().getBalance(player);
        String kingdomName = kingdomOverride != null
                ? plugin.getKingdomMarketService().getKingdomNameFormatted(kingdomOverride) + " <yellow>(Admin Preview)</yellow>"
                : plugin.getKingdomMarketService().getKingdomNameFormatted(player);
        double taxPercent = plugin.getTaxService().getTaxPercent(player, kingdomOverride);
        String weatherDesc = plugin.getWeatherPriceService().getWeatherDescription(player.getWorld());

        setButton(4, new ShopGuiButton(new ShopItemBuilder(Material.PLAYER_HEAD)
                .skullOwner(player)
                .name("<gold><bold>" + player.getName() + "</bold></gold>")
                .lore(List.of(
                        "<gray>Kerajaan: " + kingdomName + "</gray>",
                        "<gray>Saldo Rupiah: <yellow><bold>" + plugin.getEconomyHook().format(balance) + "</bold></yellow></gray>",
                        "<gray>Pajak Pasar: <red>" + String.format("%.1f", taxPercent) + "%</red></gray>",
                        "<gray>Kondisi Cuaca: <aqua>" + weatherDesc + "</aqua></gray>"
                ))
                .build()));

        // 2. 6 Categories cleanly arranged in 2 symmetrical rows
        // Row 2: Slots 20, 22, 24
        // Row 3: Slots 29, 31, 33
        int[] catSlots = { 20, 22, 24, 29, 31, 33 };
        ShopCategory[] categories = ShopCategory.values();

        for (int i = 0; i < categories.length && i < catSlots.length; i++) {
            ShopCategory category = categories[i];
            int slot = catSlots[i];
            int itemCount = plugin.getItemRegistry().getItemsByCategory(category).size();

            setButton(slot, new ShopGuiButton(new ShopItemBuilder(category.getIcon())
                    .name("<gold><bold>" + category.getDisplayName() + "</bold></gold>")
                    .lore(List.of(
                            category.getDescription(),
                            " ",
                            "<gray>Total Komoditas: <yellow>" + itemCount + " item</yellow></gray>",
                            "<yellow>Sentuh / Klik untuk Buka ▶</yellow>"
                    ))
                    .build(), event -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
                new CategoryShopMenu(plugin, player, category, this, 1, kingdomOverride).open();
            }));
        }

        // 3. Market Trends Button (Slot 38)
        setButton(38, new ShopGuiButton(new ShopItemBuilder(Material.SPYGLASS)
                .name("<gradient:#f1c40f:#e67e22><bold>📈 TREN PASAR & CUACA</bold></gradient>")
                .lore(List.of(
                        "<gray>Lihat komoditas langka, pasokan melimpah,</gray>",
                        "<gray>serta pengaruh cuaca terhadap harga jual-beli.</gray>",
                        " ",
                        "<yellow>Sentuh / Klik untuk Buka Tren Pasar ▶</yellow>"
                ))
                .build(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            new MarketTrendsMenu(plugin, player, this).open();
        }));

        // 4. Quick Sell GUI Button (Slot 42 - Bottom Row Accent)
        setButton(42, new ShopGuiButton(new ShopItemBuilder(Material.HOPPER)
                .name("<green><bold>⚡ JUAL CEPAT (SELL GUI)</bold></green>")
                .lore(List.of(
                        "<gray>Jual banyak item sekaligus secara instan.</gray>",
                        "<gray>Cukup masukkan item ke dalam kotak penjualan.</gray>",
                        " ",
                        "<yellow>Sentuh / Klik untuk Buka Sell GUI ▶</yellow>"
                ))
                .build(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            new SellGuiMenu(plugin, player, this).open();
        }));

        // 5. Close Button (Slot 49 - Bottom Center)
        setButton(49, new com.apexsions.shop.gui.navigation.CloseButton());
    }
}
