package com.apex.economy.gui;

import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.navigation.CloseButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.economy.ApexsionsEconomy;
import com.apex.economy.auction.AuctionListing;
import com.apex.economy.auction.AuctionStatus;
import com.apex.economy.currency.Currency;
import com.apex.economy.util.NumberFormatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MyAuctionsMenu extends Gui {

    private final ApexsionsEconomy plugin;

    public MyAuctionsMenu(ApexsionsEconomy plugin, Player player, Gui parent) {
        super(null, player, "&8[ &2&lLELANG SAYA & PENGELOLAAN &8]", 54, parent);
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        fillBorder();

        List<AuctionListing> myList = plugin.getAuctionService().getPlayerAuctionsCached(player.getUniqueId());

        // 1. Top Header Info (Slot 0 & Slot 4)
        setButton(0, new GuiButton(new ItemBuilder(Material.BOOK)
                .name("&e&l[💡] CARA MENJUAL BARANG")
                .lore(List.of(
                        "&7Pegang item di tangan utama, lalu ketik:",
                        "&a/ah sell <harga> [rupiah|diamond] [durasi_jam]",
                        " ",
                        "&7Contoh: &f/ah sell 10k rupiah 24"
                ))
                .build()));

        setButton(4, new GuiButton(new ItemBuilder(Material.CHEST)
                .name("&6&lDAFTAR LELANG SAYA (" + myList.size() + " Barang)")
                .lore(List.of(
                        "&7Status barang lelang milik Anda:",
                        " ",
                        "&a● Aktif: &7Klik item untuk ubah harga atau hapus lelang.",
                        "&c○ Kedaluwarsa: &7Klik item untuk klaim barang kembali.",
                        "&e✔ Terjual: &7Saldo otomatis masuk ke dompet Anda."
                ))
                .build()));

        // 2. Render Player Listings in Center Grid (Slots 10..16, 19..25, 28..34, 37..43)
        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
        int idx = 0;

        for (AuctionListing listing : myList) {
            if (idx >= slots.length) break;

            ItemStack item = listing.getItemStack();
            if (item == null) continue;

            Currency curr = plugin.getCurrencyRegistry().get(listing.getCurrencyId());
            List<String> lore = new ArrayList<>();
            if (item.getItemMeta() != null && item.getItemMeta().hasLore()) {
                lore.addAll(item.getItemMeta().getLore());
                lore.add(" ");
            }

            lore.add("&7Harga: &e&l" + NumberFormatUtil.format(listing.getPrice(), curr) + " &7(" + NumberFormatUtil.formatFull(listing.getPrice(), curr) + ")");
            lore.add("&7Status: " + getStatusBadge(listing.getStatus()));

            if (listing.getStatus() == AuctionStatus.ACTIVE) {
                lore.add("&7Sisa Waktu: &f" + listing.getTimeRemainingFormatted());
                lore.add(" ");
                lore.add("&e&l[KLIK UNTUK KELOLA / UBAH HARGA / HAPUS]");
            } else if (listing.getStatus() == AuctionStatus.EXPIRED) {
                lore.add(" ");
                lore.add("&a&l[KLIK UNTUK KLAIM BARANG KEMBALI]");
            } else if (listing.getStatus() == AuctionStatus.SOLD) {
                lore.add("&7Pembeli: &a" + (listing.getBuyerUuid() != null ? "Player" : "Pembeli"));
                lore.add("&a✔ Saldo penjualan telah masuk ke dompet Anda.");
            }

            ItemStack display = new ItemBuilder(item)
                    .lore(lore)
                    .build();

            setButton(slots[idx++], new GuiButton(display, event -> {
                if (listing.getStatus() == AuctionStatus.ACTIVE) {
                    new MyAuctionItemEditMenu(plugin, player, listing, this).open();
                } else if (listing.getStatus() == AuctionStatus.EXPIRED) {
                    plugin.getAuctionService().claimExpiredAuction(player, listing);
                    open();
                }
            }));
        }

        // 3. Bottom Navigation Bar
        setButton(45, new BackButton(this, parent));

        setButton(49, new GuiButton(new ItemBuilder(Material.GOLDEN_HORSE_ARMOR)
                .name("&a&l[🏛] KEMBALI KE PASAR LELANG")
                .lore(List.of(
                        "&7Buka katalog lelang umum seluruh pemain.",
                        " ",
                        "&aKlik untuk membuka pasar lelang >"
                ))
                .build(), event -> {
            new AuctionBrowseMenu(plugin, player, null).open();
        }));

        setButton(53, new CloseButton());
    }

    private String getStatusBadge(AuctionStatus status) {
        return switch (status) {
            case ACTIVE -> "&a&lAKTIF";
            case EXPIRED -> "&c&lKEDALUWARSA";
            case SOLD -> "&e&lTERJUAL";
            case CANCELLED -> "&7&lDIBATALKAN";
        };
    }
}
