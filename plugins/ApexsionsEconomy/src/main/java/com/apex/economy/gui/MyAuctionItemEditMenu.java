package com.apex.economy.gui;

import com.apex.economy.gui.core.Gui;
import com.apex.economy.gui.core.GuiButton;
import com.apex.economy.gui.navigation.BackButton;
import com.apex.economy.gui.navigation.CloseButton;
import com.apex.economy.gui.util.ItemBuilder;
import com.apex.economy.util.ItemSerializer;
import com.apex.economy.ApexsionsEconomy;
import com.apex.economy.auction.AuctionListing;
import com.apex.economy.currency.Currency;
import com.apex.economy.util.NumberFormatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MyAuctionItemEditMenu extends Gui {

    private final ApexsionsEconomy plugin;
    private final AuctionListing listing;

    public MyAuctionItemEditMenu(ApexsionsEconomy plugin, Player player, AuctionListing listing, Gui parent) {
        super(null, player, "&8[ &6&lKELOLA BARANG LELANG &8]", 36, parent);
        this.plugin = plugin;
        this.listing = listing;
    }

    @Override
    public void initialize() {
        fillBackground();

        ItemStack item = listing.getItemStack();
        Currency curr = plugin.getCurrencyRegistry().get(listing.getCurrencyId());
        String itemName = item != null ? ItemSerializer.getItemDisplayName(item) : "Item";

        // 1. Item Preview (Slot 13)
        setButton(13, new GuiButton(new ItemBuilder(item != null ? item : new ItemStack(Material.CHEST))
                .name("&e&l" + itemName)
                .lore(List.of(
                        "&7Harga Saat Ini: &a&l" + NumberFormatUtil.format(listing.getPrice(), curr) + " &7(" + NumberFormatUtil.formatFull(listing.getPrice(), curr) + ")",
                        "&7Mata Uang: &f" + (curr != null ? curr.getDisplayName() : listing.getCurrencyId()),
                        "&7Sisa Waktu: &f" + listing.getTimeRemainingFormatted(),
                        " ",
                        "&7Pilih aksi yang ingin Anda lakukan pada lelang ini."
                ))
                .build()));

        // 2. Change Price Button (Slot 20)
        setButton(20, new GuiButton(new ItemBuilder(Material.GOLD_INGOT)
                .name("&e&l[âœ] UBAH HARGA LELANG")
                .lore(List.of(
                        "&7Ubah harga barang lelang Anda.",
                        "&7Harga saat ini: &a" + NumberFormatUtil.format(listing.getPrice(), curr),
                        " ",
                        "&eKlik untuk memasukkan harga baru via chat >"
                ))
                .build(), event -> {
            plugin.getChatInputManager().startInput(player, "Masukkan harga lelang baru untuk " + itemName + " (contoh: 500, 10k, 1.5jt):", input -> {
                try {
                    double newPrice = NumberFormatUtil.parse(input);
                    if (newPrice <= 0) {
                        player.sendMessage("Â§cHarga harus lebih besar dari 0!");
                        open();
                        return;
                    }
                    plugin.getAuctionService().updateAuctionPrice(player, listing.getId(), newPrice);
                    open();
                } catch (Exception e) {
                    player.sendMessage("Â§cFormat harga tidak valid!");
                    open();
                }
            }, this::open);
        }));

        // 3. Cancel / Delete Auction Button (Slot 24)
        setButton(24, new GuiButton(new ItemBuilder(Material.RED_CONCRETE)
                .name("&c&l[âœ–] BATALKAN & AMBIL BARANG")
                .lore(List.of(
                        "&7Hapus barang ini dari pasar lelang",
                        "&7dan kembalikan ke inventory Anda.",
                        " ",
                        "&cKlik untuk membatalkan lelang sekarang >"
                ))
                .build(), event -> {
            plugin.getAuctionService().cancelAuction(player, listing.getId());
            if (parent != null) parent.open();
        }));

        // Navigation
        setButton(18, new BackButton(this, parent));
        setButton(26, new CloseButton());
    }
}
