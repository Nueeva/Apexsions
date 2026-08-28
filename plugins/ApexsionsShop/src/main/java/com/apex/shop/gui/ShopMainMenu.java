package com.apex.shop.gui;

import com.apex.shop.ApexsionsShop;
import com.apex.shop.category.ShopCategory;
import com.apex.shop.gui.core.ShopGui;
import com.apex.shop.gui.core.ShopGuiButton;
import com.apex.shop.gui.core.ShopItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public class ShopMainMenu extends ShopGui {

    public ShopMainMenu(ApexsionsShop plugin, Player player) {
        super(plugin, player, plugin.getConfigManager().getGuiConfig().getString("titles.main-menu", "<dark_gray><bold>[ APEXSIONS MARKET ]</bold></dark_gray>"), 54);
    }

    @Override
    public void initialize() {
        fillBorder();

        // 1. Player Info Banner (Slot 4)
        double balance = plugin.getEconomyHook().getBalance(player);
        setButton(4, new ShopGuiButton(new ShopItemBuilder(Material.PLAYER_HEAD)
                .skullOwner(player)
                .name("<gold><bold>" + player.getName() + "</bold></gold>")
                .lore(List.of(
                        "<gray>Saldo Rupiah: <yellow>" + plugin.getEconomyHook().format(balance) + "</yellow></gray>",
                        "<gray>Kerajaan: " + plugin.getKingdomMarketService().getKingdomNameFormatted(player) + "</gray>"
                ))
                .build()));

        // 2. 6 Category Buttons
        for (ShopCategory category : ShopCategory.values()) {
            int itemCount = plugin.getItemRegistry().getItemsByCategory(category).size();
            setButton(category.getSlot(), new ShopGuiButton(new ShopItemBuilder(category.getIcon())
                    .name("<gold><bold>" + category.getDisplayName() + "</bold></gold>")
                    .lore(List.of(
                            category.getDescription(),
                            " ",
                            "<gray>Total Item: <yellow>" + itemCount + " komoditas</yellow></gray>",
                            "<yellow>Sentuh / Klik untuk Buka ▶</yellow>"
                    ))
                    .build(), event -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
                new CategoryShopMenu(plugin, player, category, this, 1).open();
            }));
        }

        // 3. Market Live Overview Card (Slot 22)
        String weatherDesc = plugin.getWeatherPriceService().getWeatherDescription(player.getWorld());
        double taxPercent = plugin.getTaxService().getTaxPercent(player);
        setButton(22, new ShopGuiButton(new ShopItemBuilder(Material.COMPASS)
                .name("<aqua><bold>INDIKATOR PASAR DINAMIS</bold></aqua>")
                .lore(List.of(
                        "<gray>Kondisi Cuaca: " + weatherDesc + "</gray>",
                        "<gray>Pasar Wilayah: " + plugin.getKingdomMarketService().getKingdomNameFormatted(player) + "</gray>",
                        "<gray>Tarif Pajak: <red>" + String.format("%.1f", taxPercent) + "%</red></gray>",
                        " ",
                        "<dark_aqua>Harga menyesuaikan cuaca dan spesialisasi bioma!</dark_aqua>"
                ))
                .glow()
                .build()));

        // 4. Quick Sell GUI Button (Slot 32)
        setButton(32, new ShopGuiButton(new ShopItemBuilder(Material.HOPPER)
                .name("<green><bold>JUAL CEPAT (SELL GUI)</bold></green>")
                .lore(List.of(
                        "<gray>Jual banyak item sekaligus secara instan.</gray>",
                        "<gray>Cukup drag & drop item ke dalam kotak kosong.</gray>",
                        " ",
                        "<yellow>Sentuh / Klik untuk Buka Sell GUI ▶</yellow>"
                ))
                .build(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            new SellGuiMenu(plugin, player, this).open();
        }));

        // 5. Close Button (Slot 49)
        setButton(49, new com.apex.shop.gui.navigation.CloseButton());
    }
}
