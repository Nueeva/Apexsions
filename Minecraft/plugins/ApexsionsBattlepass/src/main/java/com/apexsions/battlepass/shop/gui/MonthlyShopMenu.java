package com.apexsions.battlepass.shop.gui;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.navigation.CloseButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.player.PlayerData;
import com.apexsions.battlepass.shop.ShopCategory;
import com.apexsions.battlepass.shop.ShopItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MonthlyShopMenu extends Gui {

    public MonthlyShopMenu(ApexsionsBattlepass plugin, Player player, Gui parent) {
        super(plugin, player, "&8[ &d&lMONTHLY BATTLE SHOP &8]", 45, parent);
    }

    @Override
    public void initialize() {
        fillBackground();

        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data == null) return;

        String seasonTimeLeft = plugin.getSeasonManager().getTimeLeftFormatted();

        // 1. Category Switcher Tabs (Slots 1, 2, 3)
        setButton(1, new GuiButton(new ItemBuilder(Material.EMERALD)
                .name("&a&lDAILY SHOP")
                .lore(List.of("&7Menu belanja harian.", " ", "&eKlik untuk beralih ke Daily Shop >"))
                .build(), event -> {
            new DailyShopMenu(plugin, player, parent).open();
        }));

        setButton(2, new GuiButton(new ItemBuilder(Material.GOLD_INGOT)
                .name("&e&lWEEKLY SHOP")
                .lore(List.of("&7Menu belanja mingguan.", " ", "&eKlik untuk beralih ke Weekly Shop >"))
                .build(), event -> {
            new WeeklyShopMenu(plugin, player, parent).open();
        }));

        setButton(3, new GuiButton(new ItemBuilder(Material.DIAMOND_BLOCK)
                .name("&d&l[✔] MONTHLY SHOP")
                .lore(List.of("&7Menu belanja bulanan langka.", "&a(Kategori Sedang Dibuka)"))
                .build()));

        // 2. Rarity Chances Button (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.SPYGLASS)
                .name("&6&l[📊] PELUANG RARITY")
                .lore(List.of(
                        "&7Lihat rincian persentase peluang",
                        "&7kemunculan masing-masing rarity item.",
                        " ",
                        "&eKlik untuk membuka rincian peluang >"
                ))
                .build(), event -> {
            new RarityChancesMenu(plugin, player, ShopCategory.MONTHLY, this).open();
        }));

        // 3. Season Countdown Card (Slot 5)
        setButton(5, new GuiButton(new ItemBuilder(Material.CLOCK)
                .name("&6&lDURASI SEASON")
                .lore(List.of(
                        "&7Season ends in: &e" + seasonTimeLeft,
                        "&7Penawaran shop berlaku selama season aktif!"
                ))
                .build()));

        // 3. Player Balances Card (Slot 6)
        double rupiahBal = 0.0;
        try {
            rupiahBal = com.apexsions.economy.api.ApexsionsEconomyProvider.get().getBalance(player.getUniqueId(), "rupiah");
        } catch (Throwable ignored) {}

        setButton(6, new GuiButton(new ItemBuilder(Material.SUNFLOWER)
                .name("&e&lSALDO ANDA")
                .lore(List.of(
                        "&7Saldo Rupiah: &aRp." + String.format("%,.0f", rupiahBal),
                        "&7Battle Coins: &e" + data.getCurrency() + " Coins"
                ))
                .build()));

        // 4. Refresh Shop Button (Slot 8)
        int refreshCost = plugin.getShopRefreshService().calculateRefreshCost(player, ShopCategory.MONTHLY);
        setButton(8, new GuiButton(new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name("&e&l[🔄] REFRESH MONTHLY SHOP")
                .lore(List.of(
                        "&7Acak ulang item Monthly Shop bulan ini!",
                        " ",
                        "&7Biaya Refresh: &e" + refreshCost + " Battle Coins",
                        "&7Refresh Hari Ini: &b" + data.getDailyRefreshCount() + " kali",
                        " ",
                        "&eKlik untuk membuka konfirmasi refresh >"
                ))
                .build(), event -> {
            new ShopRefreshConfirmMenu(plugin, player, ShopCategory.MONTHLY, this).open();
        }));

        // 5. Shop Items (Exactly 10 Centered Slots)
        Collection<ShopItem> items = plugin.getShopManager().getDisplayItems(player, ShopCategory.MONTHLY);
        int[] itemSlots = { 20, 21, 22, 23, 24, 29, 30, 31, 32, 33 };
        int idx = 0;

        for (ShopItem item : items) {
            if (idx >= itemSlots.length) break;

            int boughtCount = data.getShopPurchaseCount(item.getId());
            boolean limitReached = item.getPurchaseLimit() > 0 && boughtCount >= item.getPurchaseLimit();
            boolean isRupiah = "rupiah".equalsIgnoreCase(item.getCurrencyType());
            boolean canAfford = isRupiah ? (rupiahBal >= item.getPrice()) : (data.getCurrency() >= (int) item.getPrice());

            String priceStr = isRupiah ? ("Rp." + String.format("%,.0f", item.getPrice())) : ((int) item.getPrice() + " Coins");

            ItemStack base = item.toItemStack();
            ItemStack displayItem = base != null ? base.clone() : new ItemStack(item.getMaterial(), item.getAmount());

            List<String> lore = new ArrayList<>();
            if (base != null && base.hasItemMeta() && base.getItemMeta().hasLore() && base.getItemMeta().getLore() != null) {
                lore.addAll(base.getItemMeta().getLore());
                lore.add(" ");
            } else if (!item.getLore().isEmpty()) {
                lore.addAll(item.getLore());
                lore.add(" ");
            }

            lore.add("&7Rarity: " + item.getRarity().getColor() + item.getRarity().getDisplayName());
            lore.add("&7Tipe: &f" + item.getCategoryTag());
            lore.add("&7Harga: &e" + priceStr);
            lore.add("&7Metode Bayar: &f" + (isRupiah ? "&aRupiah (Rp.)" : "&eBattle Coins"));
            lore.add("&7Status Saldo: " + (canAfford ? "&a✔ Saldo Cukup" : "&c✖ Saldo Kurang"));

            if (item.getPurchaseLimit() > 0) {
                lore.add("&7Batas Beli: &f" + boughtCount + " / " + item.getPurchaseLimit());
            }

            if (limitReached) {
                lore.add(" ");
                lore.add("&c&lSUDAH MENCAPAI BATAS BELI");
            } else if (!canAfford) {
                lore.add(" ");
                lore.add("&c&lSALDO TIDAK MENCUKUPI");
            } else {
                lore.add(" ");
                lore.add("&a&lKLIK UNTUK MEMBELI!");
            }

            displayItem = new ItemBuilder(displayItem)
                    .lore(lore)
                    .build();

            setButton(itemSlots[idx++], new GuiButton(displayItem, event -> {
                if (plugin.getShopManager().purchaseItem(player, ShopCategory.MONTHLY, item.getId())) {
                    open(); // In-place refresh
                }
            }));
        }

        // Navigation
        setButton(36, new BackButton(this));
        setButton(44, new CloseButton());
    }
}

