package com.apexsions.battlepass.shop.gui;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.navigation.CloseButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.player.PlayerData;
import com.apexsions.battlepass.shop.ShopCategory;
import com.apexsions.battlepass.shop.refresh.ShopRefreshService;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class ShopRefreshConfirmMenu extends Gui {

    private final ShopCategory category;
    private final Gui shopMenu;

    public ShopRefreshConfirmMenu(ApexsionsBattlepass plugin, Player player, ShopCategory category, Gui shopMenu) {
        super(plugin, player, plugin.getGuiConfig().getString("titles.shop-refresh-confirm", "&8[ &c&lKONFIRMASI REFRESH SHOP &8]"), 45, shopMenu);
        this.category = category;
        this.shopMenu = shopMenu;
    }

    @Override
    public void initialize() {
        fillBackground();

        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data == null) return;

        int cost = plugin.getShopRefreshService().calculateRefreshCost(player, category);
        int currentCoins = data.getCurrency();
        int remainingCoins = currentCoins - cost;
        boolean canAfford = currentCoins >= cost;
        String categoryName = category.name().substring(0, 1).toUpperCase() + category.name().substring(1).toLowerCase() + " Shop";

        // 1. Refresh Details Card (Slot 13)
        setButton(13, new GuiButton(new ItemBuilder(Material.COMPASS)
                .name("&e&lRINCIAN REFRESH SHOP")
                .lore(List.of(
                        "&7Kategori: &f" + categoryName,
                        "&7Biaya Refresh: &e" + cost + " Battle Coins",
                        " ",
                        "&7Saldo Saat Ini: &e" + currentCoins + " Battle Coins",
                        "&7Sisa Setelah Refresh: " + (canAfford ? "&a" + remainingCoins + " Battle Coins" : "&cSaldo Kurang!"),
                        "&7Refresh Hari Ini: &b" + data.getDailyRefreshCount() + " kali",
                        " ",
                        "&7Merotasi daftar item shop secara acak",
                        "&7berdasarkan item pool dan tingkat kelangkaan."
                ))
                .build()));

        // 2. Confirm Button (Slot 29)
        if (canAfford) {
            setButton(29, new GuiButton(new ItemBuilder(Material.LIME_CONCRETE)
                    .name("&a&l[✔] YA, REFRESH SEKARANG")
                    .lore(List.of(
                            "&7Potong &e" + cost + " Battle Coins",
                            "&7dan dapatkan rotasi item baru!",
                            " ",
                            "&eKlik untuk konfirmasi refresh >"
                    ))
                    .build(), event -> {
                ShopRefreshService.RefreshResult result = plugin.getShopRefreshService().executeRefresh(player, category);
                switch (result) {
                    case SUCCESS -> {
                        player.sendMessage("§aBerhasil merefresh katalog §e" + categoryName + " §aseharga §e" + cost + " Battle Coins§a!");
                        shopMenu.open();
                    }
                    case INSUFFICIENT_FUNDS -> {
                        player.sendMessage("§cSaldo Battle Coins Anda tidak cukup untuk refresh! Butuh §e" + cost + " Battle Coins");
                        shopMenu.open();
                    }
                    case ON_COOLDOWN -> {
                        long remSec = (plugin.getShopRefreshService().getRemainingCooldownMillis(data) / 1000L) + 1;
                        player.sendMessage(plugin.getMessage("shop-refresh-cooldown")
                                .replace("%seconds%", String.valueOf(remSec)));
                        shopMenu.open();
                    }
                    case SEASON_INACTIVE -> {
                        player.sendMessage(plugin.getMessage("shop-locked-transition"));
                        shopMenu.open();
                    }
                    case FAILED -> {
                        player.sendMessage(plugin.getMessage("shop-refresh-failed"));
                        shopMenu.open();
                    }
                }
            }));
        } else {
            setButton(29, new GuiButton(new ItemBuilder(Material.GRAY_CONCRETE)
                    .name("&c&l[✖] SALDO TIDAK CUKUP")
                    .lore(List.of(
                            "&7Anda membutuhkan &e" + cost + " Battle Coins",
                            "&7untuk melakukan refresh pada kategori ini.",
                            " ",
                            "&cKumpulkan coins dari Quest BattlePass!"
                    ))
                    .build(), event -> {
                player.sendMessage("§cSaldo Battle Coins Anda tidak cukup untuk refresh! Butuh §e" + cost + " Battle Coins");
            }));
        }

        // 3. Cancel Button (Slot 33)
        setButton(33, new GuiButton(new ItemBuilder(Material.RED_CONCRETE)
                .name("&c&l[✖] BATALKAN")
                .lore(List.of(
                        "&7Batalkan proses refresh dan",
                        "&7kembali ke menu shop sebelumnya.",
                        " ",
                        "&eKlik untuk kembali >"
                ))
                .build(), event -> {
            shopMenu.open();
        }));

        // Navigation
        setButton(36, new BackButton(shopMenu));
        setButton(44, new CloseButton());
    }
}
