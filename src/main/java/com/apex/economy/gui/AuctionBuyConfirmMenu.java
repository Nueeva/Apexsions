package com.apex.economy.gui;

import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.battlepass.util.ItemSerializer;
import com.apex.economy.ApexsionsEconomy;
import com.apex.economy.auction.AuctionListing;
import com.apex.economy.currency.Currency;
import com.apex.economy.util.NumberFormatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AuctionBuyConfirmMenu extends Gui {

    private final ApexsionsEconomy plugin;
    private final AuctionListing listing;

    public AuctionBuyConfirmMenu(ApexsionsEconomy plugin, Player player, AuctionListing listing, Gui parent) {
        super(null, player, "&8[ &2&lKONFIRMASI BELI LELANG &8]", 36, parent);
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
                        "&7Penjual: &e" + listing.getSellerName(),
                        "&7Harga: &a&l" + NumberFormatUtil.format(listing.getPrice(), curr) + " &7(" + NumberFormatUtil.formatFull(listing.getPrice(), curr) + ")",
                        " ",
                        "&7Apakah Anda yakin ingin membeli barang ini sekarang?"
                ))
                .build()));

        // 2. Confirm Buy Button (Slot 20)
        setButton(20, new GuiButton(new ItemBuilder(Material.LIME_CONCRETE)
                .name("&a&l[✔] YA, BELI SEKARANG")
                .lore(List.of(
                        "&7Saldo Anda akan dipotong &e" + NumberFormatUtil.format(listing.getPrice(), curr) + "&7.",
                        " ",
                        "&aKlik untuk konfirmasi pembelian >"
                ))
                .build(), event -> {
            plugin.getAuctionService().buyAuction(player, listing.getId());
            if (parent != null) parent.open();
        }));

        // 3. Cancel Button (Slot 24)
        setButton(24, new GuiButton(new ItemBuilder(Material.RED_CONCRETE)
                .name("&c&l[✖] BATALKAN")
                .lore(List.of(
                        "&7Kembali ke pasar lelang tanpa membeli.",
                        " ",
                        "&eKlik untuk kembali >"
                ))
                .build(), event -> {
            if (parent != null) parent.open();
            else player.closeInventory();
        }));
    }
}
