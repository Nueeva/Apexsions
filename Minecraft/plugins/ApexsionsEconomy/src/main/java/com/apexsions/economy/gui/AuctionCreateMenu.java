package com.apexsions.economy.gui;

import com.apexsions.economy.gui.core.Gui;
import com.apexsions.economy.gui.core.GuiButton;
import com.apexsions.economy.gui.navigation.BackButton;
import com.apexsions.economy.gui.util.ItemBuilder;
import com.apexsions.economy.util.ItemSerializer;
import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.currency.Currency;
import com.apexsions.economy.util.NumberFormatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AuctionCreateMenu extends Gui {

    private final ApexsionsEconomy plugin;
    private Currency currency;
    private double price = 1000.0;
    private int durationHours = 24;

    public AuctionCreateMenu(ApexsionsEconomy plugin, Player player, Gui parent) {
        super(null, player, "&8[ &2&lPASANG BARANG LELANG &8]", 36, parent);
        this.plugin = plugin;
        this.currency = plugin.getCurrencyRegistry().getDefault();
    }

    @Override
    public void initialize() {
        fillBackground();

        // 1. Info Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.HOPPER)
                .name("&6&lPASANG BARANG KE AUCTION HOUSE")
                .lore(List.of(
                        "&7Letakkan barang dari inventory Anda ke slot 13.",
                        "&7Atur mata uang, harga, dan durasi lelang.",
                        " ",
                        "&7Item akan disimpan secara aman di sistem escrow."
                ))
                .build()));

        // Slot 13 is the input slot (empty)
        inventory.setItem(13, null);

        // 2. Select Currency (Slot 19)
        setButton(19, new GuiButton(new ItemBuilder(currency.getIcon())
                .name("&a&lMATA UANG: &e" + currency.getDisplayName())
                .lore(List.of(
                        "&7Pilih mata uang yang diinginkan untuk pembayaran.",
                        " ",
                        "&aKlik untuk beralih (Rupiah / Diamond 💎) >"
                ))
                .build(), event -> {
            Currency rup = plugin.getCurrencyRegistry().get("rupiah");
            Currency dia = plugin.getCurrencyRegistry().get("diamond");
            this.currency = (this.currency == rup && dia != null) ? dia : rup;
            open();
        }));

        // 3. Set Price (Slot 20)
        setButton(20, new GuiButton(new ItemBuilder(Material.GOLD_INGOT)
                .name("&e&lHARGA: &f" + NumberFormatUtil.format(price, currency))
                .lore(List.of(
                        "&7Harga pembelian langsung (Instant Buyout).",
                        " ",
                        "&eKlik untuk mengubah harga via chat >"
                ))
                .build(), event -> {
            plugin.getChatInputManager().startInput(player, "Masukkan harga lelang (contoh: 1000, 10k, 1.5jt, 2m):", input -> {
                try {
                    double p = NumberFormatUtil.parse(input);
                    if (p > 0) this.price = p;
                    open();
                } catch (Exception e) {
                    player.sendMessage("§cFormat harga tidak valid!");
                    open();
                }
            }, this::open);
        }));

        // 4. Select Duration (Slot 21)
        setButton(21, new GuiButton(new ItemBuilder(Material.CLOCK)
                .name("&b&lDURASI LELANG: &f" + durationHours + " Jam")
                .lore(List.of(
                        "&7Lama barang ditampilkan di pasar lelang.",
                        " ",
                        "&bKlik untuk beralih durasi (12j, 24j, 48j) >"
                ))
                .build(), event -> {
            if (durationHours == 12) durationHours = 24;
            else if (durationHours == 24) durationHours = 48;
            else durationHours = 12;
            open();
        }));

        // 5. Confirm & List Button (Slot 23)
        setButton(23, new GuiButton(new ItemBuilder(Material.LIME_CONCRETE)
                .name("&a&l[✔] KONFIRMASI & PASANG LELANG")
                .lore(List.of(
                        "&7Klik untuk memasang barang ke pasar lelang.",
                        " ",
                        "&aKlik untuk pasang sekarang >"
                ))
                .build(), event -> {
            ItemStack placed = inventory.getItem(13);
            if (placed == null || placed.getType() == Material.AIR) {
                player.sendMessage("§c[!] Harap letakkan barang ke dalam Slot 13 terlebih dahulu!");
                return;
            }

            ItemStack finalItem = placed.clone();
            inventory.setItem(13, null); // Clear slot before passing to escrow
            plugin.getAuctionService().createAuction(player, finalItem, currency, price, durationHours);
            if (parent != null) parent.open();
        }));

        // Back Button (Returns item if placed)
        setButton(18, new BackButton(this, parent));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        if (rawSlot == 13 || rawSlot >= size) {
            return;
        }
        super.handleClick(event);
    }
}
