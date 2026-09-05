package com.apexsions.shop.gui;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.gui.core.ShopGui;
import com.apexsions.shop.gui.core.ShopGuiButton;
import com.apexsions.shop.gui.core.ShopItemBuilder;
import com.apexsions.shop.gui.navigation.CloseButton;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Interactive Admin GUI for inspecting and previewing all 3 Kingdom Shops
 * (Zenithar 25% tax, Solterra 20% tax + 65% ore sell ratio, Sylvamoor 15% tax).
 */
public class AdminKingdomShopSelectorGUI extends ShopGui {

    public AdminKingdomShopSelectorGUI(ApexsionsShop plugin, Player player) {
        super(plugin, player, "<gradient:#2ecc71:#f1c40f><bold>👑 INSPEKSI PASAR KERAJAAN (ADMIN) 👑</bold></gradient>", 27);
    }

    @Override
    public void initialize() {
        fillBorder();

        // Slot 11: Zenithar
        setButton(11, new ShopGuiButton(new ShopItemBuilder(Material.GOLD_BLOCK)
                .name("<gradient:#ffe900:#f39c12><bold>👑 PASAR KERAJAAN ZENITHAR 👑</bold></gradient>")
                .lore(List.of(
                        "<dark_gray>Celestial & Solar Market</dark_gray>",
                        "",
                        "<gray>Pajak Kerajaan:</gray> <gold><bold>25.0%</bold></gold>",
                        "<gray>Tren Pasar:</gray> <red><bold>Fluktuatif & Cenderung Tidak Stabil</bold></red>",
                        "<gray>Karakteristik:</gray> <yellow>Harga Beli Tinggi & Jual Rendah</yellow>",
                        "",
                        "<yellow>▶ Klik untuk Inspeksi / Buka Toko Zenithar</yellow>"
                ))
                .build(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            new ShopMainMenu(plugin, player, "ZENITHAR").open();
        }));

        // Slot 13: Solterra
        setButton(13, new ShopGuiButton(new ShopItemBuilder(Material.REDSTONE_BLOCK)
                .name("<gradient:#ff4d4d:#c0392b><bold>🔥 PASAR KERAJAAN SOLTERRA 🔥</bold></gradient>")
                .lore(List.of(
                        "<dark_gray>Crimson Earth & Fire Empire</dark_gray>",
                        "",
                        "<gray>Pajak Kerajaan:</gray> <gold><bold>20.0%</bold></gold>",
                        "<gray>Tren Pasar:</gray> <green><bold>Harga Ore Stabil & Tinggi</bold></green>",
                        "<gray>Keunggulan:</gray> <aqua>Rasio Jual Ore 65% dari Harga Beli!</aqua>",
                        "",
                        "<yellow>▶ Klik untuk Inspeksi / Buka Toko Solterra</yellow>"
                ))
                .build(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            new ShopMainMenu(plugin, player, "SOLTERRA").open();
        }));

        // Slot 15: Sylvamoor
        setButton(15, new ShopGuiButton(new ShopItemBuilder(Material.DIAMOND_BLOCK)
                .name("<gradient:#87ceeb:#3498db><bold>🌿 PASAR KERAJAAN SYLVAMOOR 🌿</bold></gradient>")
                .lore(List.of(
                        "<dark_gray>Azure Crystal & World Tree Market</dark_gray>",
                        "",
                        "<gray>Pajak Kerajaan:</gray> <gold><bold>15.0%</bold></gold>",
                        "<gray>Tren Pasar:</gray> <green><bold>Produk Alam & Pertanian Terjangkau</bold></green>",
                        "<gray>Keunggulan:</gray> <light_purple>Pajak Terendah se-Realm</light_purple>",
                        "",
                        "<yellow>▶ Klik untuk Inspeksi / Buka Toko Sylvamoor</yellow>"
                ))
                .build(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            new ShopMainMenu(plugin, player, "SYLVAMOOR").open();
        }));

        // Slot 22: Close
        setButton(22, new CloseButton());
    }
}
