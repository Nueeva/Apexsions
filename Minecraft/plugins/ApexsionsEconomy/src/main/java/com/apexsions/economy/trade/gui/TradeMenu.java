package com.apexsions.economy.trade.gui;

import com.apexsions.economy.gui.core.Gui;
import com.apexsions.economy.gui.core.GuiButton;
import com.apexsions.economy.gui.util.ItemBuilder;
import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.trade.TradeOffer;
import com.apexsions.economy.trade.TradeSession;
import com.apexsions.economy.util.NumberFormatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TradeMenu extends Gui {

    public static final int[] MY_OFFER_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30, 37, 38, 39};
    public static final int[] PARTNER_OFFER_SLOTS = {14, 15, 16, 23, 24, 25, 32, 33, 34, 41, 42, 43};
    public static final int[] DIVIDER_SLOTS = {4, 13, 22, 31, 40};

    public static final int MY_MONEY_SLOT = 2;
    public static final int PARTNER_MONEY_SLOT = 6;
    public static final int MY_CONFIRM_SLOT = 47;
    public static final int CANCEL_SLOT = 49;
    public static final int PARTNER_CONFIRM_SLOT = 51;

    private final ApexsionsEconomy plugin;
    private final TradeSession session;
    private final Player partner;

    public TradeMenu(ApexsionsEconomy plugin, Player player, TradeSession session) {
        super(null, player, "&8[ &2&lTRADE: &f" + session.getPartner(player).getName() + " &8]", 54, null);
        this.plugin = plugin;
        this.session = session;
        this.partner = session.getPartner(player);
    }

    @Override
    public void initialize() {
        fillBackground(Material.BLACK_STAINED_GLASS_PANE);

        // 1. Divider in column 4
        for (int slot : DIVIDER_SLOTS) {
            setButton(slot, new GuiButton(new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE)
                    .name("&7| &fPEMISAH TRADE &7|")
                    .lore(List.of(
                            "&7Sisi Kiri: &aTawaran Anda (12 Slot)",
                            "&7Sisi Kanan: &eTawaran " + partner.getName() + " (12 Slot)"
                    ))
                    .build(), null));
        }

        // 2. Render My Offers (Left 12 slots)
        TradeOffer myOffer = session.getOffer(player);
        for (int i = 0; i < MY_OFFER_SLOTS.length; i++) {
            int slot = MY_OFFER_SLOTS[i];
            int offerIdx = i;
            ItemStack item = (myOffer != null) ? myOffer.getItem(offerIdx) : null;

            if (item != null && item.getType() != Material.AIR) {
                setButton(slot, new GuiButton(item.clone(), event -> {
                    ItemStack removed = session.removeItem(player, offerIdx);
                    if (removed != null) {
                        player.getInventory().addItem(removed);
                    }
                }));
            } else {
                setButton(slot, new GuiButton(new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .name("&7Slot Tawaran Anda (" + (offerIdx + 1) + "/12)")
                        .lore(List.of(
                                "&7Klik item di inventory Anda",
                                "&7untuk memasukkan ke tawaran trade."
                        ))
                        .build(), null));
            }
        }

        // 3. Render Partner Offers (Right 12 slots)
        TradeOffer partnerOffer = session.getPartnerOffer(player);
        for (int i = 0; i < PARTNER_OFFER_SLOTS.length; i++) {
            int slot = PARTNER_OFFER_SLOTS[i];
            int offerIdx = i;
            ItemStack item = (partnerOffer != null) ? partnerOffer.getItem(offerIdx) : null;

            if (item != null && item.getType() != Material.AIR) {
                // View-only button (hoverable to inspect)
                setButton(slot, new GuiButton(item.clone(), event -> {
                    // Partner items cannot be taken
                }));
            } else {
                setButton(slot, new GuiButton(new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .name("&7Slot Tawaran Lawan (" + (offerIdx + 1) + "/12)")
                        .lore(List.of(
                                "&7Menunggu tawaran item",
                                "&7dari &e" + partner.getName() + "&7."
                        ))
                        .build(), null));
            }
        }

        // 4. Player Money Offer Button (Slot 2)
        if (myOffer == null || myOffer.getCurrency() == null || myOffer.getMoneyAmount() <= 0) {
            setButton(MY_MONEY_SLOT, new GuiButton(new ItemBuilder(Material.REDSTONE)
                    .name("&c&l[+] KIRIM SALDO / DIAMOND")
                    .lore(List.of(
                            "&7Status: &cTidak mengirim uang/diamond",
                            " ",
                            "&eKlik untuk memilih mata uang & jumlah >"
                    ))
                    .build(), event -> {
                session.setTemporarilyClosing(true);
                new TradeCurrencySelectMenu(plugin, player, session).open();
            }));
        } else {
            String formatted = NumberFormatUtil.format(myOffer.getMoneyAmount(), myOffer.getCurrency());
            setButton(MY_MONEY_SLOT, new GuiButton(new ItemBuilder(Material.EMERALD)
                    .name("&a&l[✔] KIRIM SALDO: &e" + formatted)
                    .lore(List.of(
                            "&7Mata Uang: &f" + myOffer.getCurrency().getDisplayName(),
                            "&7Nominal: &e" + formatted,
                            " ",
                            "&eKlik untuk ubah atau hapus tawaran uang >"
                    ))
                    .build(), event -> {
                session.setTemporarilyClosing(true);
                new TradeCurrencyEditMenu(plugin, player, session).open();
            }));
        }

        // 5. Partner Money Info Display (Slot 6)
        if (partnerOffer == null || partnerOffer.getCurrency() == null || partnerOffer.getMoneyAmount() <= 0) {
            setButton(PARTNER_MONEY_SLOT, new GuiButton(new ItemBuilder(Material.REDSTONE)
                    .name("&c&lTAWARAN SALDO LAWAN")
                    .lore(List.of(
                            "&7Pemain &e" + partner.getName() + " &7tidak",
                            "&7mengirim saldo atau diamond."
                    ))
                    .build(), null));
        } else {
            String formatted = NumberFormatUtil.format(partnerOffer.getMoneyAmount(), partnerOffer.getCurrency());
            setButton(PARTNER_MONEY_SLOT, new GuiButton(new ItemBuilder(Material.EMERALD)
                    .name("&a&lTAWARAN SALDO LAWAN: &e" + formatted)
                    .lore(List.of(
                            "&7Mata Uang: &f" + partnerOffer.getCurrency().getDisplayName(),
                            "&7Nominal: &e" + formatted
                    ))
                    .build(), null));
        }

        // 6. Player Confirmation Button (Slot 47)
        boolean myConfirmed = myOffer != null && myOffer.isConfirmed();
        if (!myConfirmed) {
            setButton(MY_CONFIRM_SLOT, new GuiButton(new ItemBuilder(Material.REDSTONE_BLOCK)
                    .name("&c&l[✖] STATUS: BELUM KONFIRMASI")
                    .lore(List.of(
                            "&7Periksa barang & saldo yang ditawarkan.",
                            " ",
                            "&aKlik untuk konfirmasi & menyetujui trade >"
                    ))
                    .build(), event -> {
                session.toggleConfirm(player);
            }));
        } else {
            setButton(MY_CONFIRM_SLOT, new GuiButton(new ItemBuilder(Material.EMERALD_BLOCK)
                    .name("&a&l[✔] STATUS: SUDAH KONFIRMASI")
                    .lore(List.of(
                            "&aAnda telah menyetujui tawaran ini.",
                            "&7Menunggu konfirmasi dari &e" + partner.getName() + "&7...",
                            " ",
                            "&cKlik untuk membatalkan konfirmasi >"
                    ))
                    .build(), event -> {
                session.toggleConfirm(player);
            }));
        }

        // 7. Partner Confirmation Display (Slot 51)
        boolean partnerConfirmed = partnerOffer != null && partnerOffer.isConfirmed();
        if (!partnerConfirmed) {
            setButton(PARTNER_CONFIRM_SLOT, new GuiButton(new ItemBuilder(Material.REDSTONE_BLOCK)
                    .name("&c&lSTATUS LAWAN: BELUM KONFIRMASI")
                    .lore(List.of(
                            "&7Pemain &e" + partner.getName() + " &7belum",
                            "&7mengonfirmasi tawaran trade."
                    ))
                    .build(), null));
        } else {
            setButton(PARTNER_CONFIRM_SLOT, new GuiButton(new ItemBuilder(Material.EMERALD_BLOCK)
                    .name("&a&lSTATUS LAWAN: SUDAH KONFIRMASI")
                    .lore(List.of(
                            "&aPemain &e" + partner.getName() + " &atelah",
                            "&amenyetujui tawaran trade ini!"
                    ))
                    .build(), null));
        }

        // 8. Cancel Button (Slot 49)
        setButton(CANCEL_SLOT, new GuiButton(new ItemBuilder(Material.BARRIER)
                .name("&c&lBATALKAN TRADE")
                .lore(List.of(
                        "&7Klik untuk membatalkan sesi trade.",
                        "&7Semua barang akan dikembalikan ke inventory."
                ))
                .build(), event -> {
            session.cancelTrade(player, "Dibatalkan oleh pemain");
        }));
    }

    public void refreshContents() {
        if (!player.isOnline()) return;
        buttons.clear();
        inventory.clear();
        initialize();
        for (var entry : buttons.entrySet()) {
            if (entry.getKey() >= 0 && entry.getKey() < size && entry.getValue() != null) {
                inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
            }
        }
    }

    @Override
    public void handleBottomInventoryClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        TradeOffer myOffer = session.getOffer(player);
        if (myOffer == null) return;

        if (myOffer.isFull()) {
            player.sendMessage("§cSlot tawaran trade Anda sudah penuh (maksimal 12 item)!");
            return;
        }

        // Move item to trade offer
        ItemStack itemToAdd = clickedItem.clone();
        event.setCurrentItem(null);
        session.addItem(player, itemToAdd);
    }


    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        if (session.isTemporarilyClosing()) {
            session.setTemporarilyClosing(false);
            return;
        }
        if (session.getState() == TradeSession.TradeState.ACTIVE) {
            session.cancelTrade(player, "Menu trade ditutup");
        }
    }
}
