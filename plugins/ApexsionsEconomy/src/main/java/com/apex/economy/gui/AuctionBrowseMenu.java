package com.apex.economy.gui;

import com.apex.battlepass.admin.gui.AdminConfirmMenu;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.navigation.CloseButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.battlepass.util.ItemSerializer;
import com.apex.economy.ApexsionsEconomy;
import com.apex.economy.auction.AuctionListing;
import com.apex.economy.currency.Currency;
import com.apex.economy.util.NumberFormatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AuctionBrowseMenu extends Gui {

    private static final int ITEMS_PER_PAGE = 28;
    private final ApexsionsEconomy plugin;
    private final int page;
    private final int sortMode; // 0 = Newest, 1 = Price Low-High, 2 = Price High-Low
    private final String filter;

    public AuctionBrowseMenu(ApexsionsEconomy plugin, Player player, Gui parent, int page, int sortMode, String filter) {
        super(null, player, "&8[ &2&lAUCTION HOUSE &8- Hal. " + page + " ]", 54, parent);
        this.plugin = plugin;
        this.page = Math.max(1, page);
        this.sortMode = sortMode;
        this.filter = filter != null ? filter.trim().toLowerCase() : "";
    }

    public AuctionBrowseMenu(ApexsionsEconomy plugin, Player player, Gui parent) {
        this(plugin, player, parent, 1, 0, "");
    }

    public AuctionBrowseMenu(ApexsionsEconomy plugin, Player player) {
        this(plugin, player, null, 1, 0, "");
    }

    @Override
    public void initialize() {
        fillBorder();

        List<AuctionListing> listings = new ArrayList<>(plugin.getAuctionService().getActiveAuctions());

        // Filter by item name
        if (!filter.isEmpty()) {
            listings.removeIf(l -> {
                ItemStack is = l.getItemStack();
                if (is == null) return true;
                String name = ItemSerializer.getItemDisplayName(is).toLowerCase();
                return !name.contains(filter);
            });
        }

        // Sort
        if (sortMode == 1) {
            listings.sort(Comparator.comparingDouble(AuctionListing::getPrice));
        } else if (sortMode == 2) {
            listings.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
        } else {
            listings.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        }

        int total = listings.size();
        int maxPages = Math.max(1, (int) Math.ceil((double) total / ITEMS_PER_PAGE));
        int validPage = Math.max(1, Math.min(maxPages, page));

        // 1. Top Header Info (Slot 0 & Slot 4)
        setButton(0, new GuiButton(new ItemBuilder(Material.GOLDEN_HORSE_ARMOR)
                .name("&6&lPASAR LELANG AKTIF (" + total + " Barang)")
                .lore(List.of(
                        "&7Urutan: &f" + getSortName(),
                        "&7Filter: &f" + (filter.isEmpty() ? "Semua Item" : filter),
                        " ",
                        "&7Klik barang untuk membeli langsung (Instant Buyout)."
                ))
                .build()));

        setButton(4, new GuiButton(new ItemBuilder(Material.BOOK)
                .name("&e&l[💡] CARA MENJUAL BARANG")
                .lore(List.of(
                        "&7Pegang item di tangan utama, lalu gunakan command:",
                        "&a/ah sell <harga> [rupiah|diamond] [durasi_jam]",
                        " ",
                        "&7Contoh: &f/ah sell 10k rupiah 24",
                        "&7Contoh: &f/ah sell 500 diamond 12"
                ))
                .build()));

        // 2. Search Filter (Slot 7)
        setButton(7, new GuiButton(new ItemBuilder(Material.NAME_TAG)
                .name("&e&l[🔍] CARI ITEM")
                .lore(List.of(
                        "&7Filter lelang berdasarkan nama barang.",
                        " ",
                        "&eKlik untuk mencari >"
                ))
                .build(), event -> {
            plugin.getChatInputManager().startInput(player, "Masukkan kata kunci nama item (atau 'semua' untuk reset):", query -> {
                String f = (query.equalsIgnoreCase("semua") || query.equalsIgnoreCase("all") || query.equalsIgnoreCase("reset")) ? "" : query.trim();
                new AuctionBrowseMenu(plugin, player, parent, 1, sortMode, f).open();
            }, this::open);
        }));

        // 3. Sort Toggle Button (Slot 8)
        setButton(8, new GuiButton(new ItemBuilder(Material.HOPPER)
                .name("&e&lURUTKAN: &f" + getSortName())
                .lore(List.of(
                        "&7Klik untuk mengubah urutan lelang.",
                        " ",
                        "&eKlik untuk beralih urutan >"
                ))
                .build(), event -> {
            int nextSort = (sortMode + 1) % 3;
            new AuctionBrowseMenu(plugin, player, parent, 1, nextSort, filter).open();
        }));

        // 4. Render Active Listings in Center (Slots 10..16, 19..25, 28..34, 37..43)
        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int startIdx = (validPage - 1) * ITEMS_PER_PAGE;
        int slotIdx = 0;

        for (int i = startIdx; i < total && slotIdx < slots.length; i++) {
            AuctionListing listing = listings.get(i);
            ItemStack item = listing.getItemStack();
            if (item == null) continue;

            Currency curr = plugin.getCurrencyRegistry().get(listing.getCurrencyId());
            boolean isOwner = listing.getSellerUuid().equals(player.getUniqueId());

            List<String> lore = new ArrayList<>();
            if (item.getItemMeta() != null && item.getItemMeta().hasLore()) {
                lore.addAll(item.getItemMeta().getLore());
                lore.add(" ");
            }
            lore.add("&7Penjual: &e" + listing.getSellerName() + (isOwner ? " &a(Anda)" : ""));
            lore.add("&7Harga: &a&l" + NumberFormatUtil.format(listing.getPrice(), curr) + " &7(" + NumberFormatUtil.formatFull(listing.getPrice(), curr) + ")");
            lore.add("&7Sisa Waktu: &f" + listing.getTimeRemainingFormatted());
            lore.add(" ");
            if (isOwner) {
                lore.add("&c[Barang Lelang Milik Anda Sendiri]");
                lore.add("&eKlik untuk kelola / ubah harga / batalkan.");
            } else {
                lore.add("&a&l[KLIK UNTUK MEMBELI SEKARANG]");
            }

            ItemStack display = new ItemBuilder(item)
                    .lore(lore)
                    .build();

            setButton(slots[slotIdx++], new GuiButton(display, event -> {
                if (isOwner) {
                    new MyAuctionItemEditMenu(plugin, player, listing, this).open();
                    return;
                }

                new AuctionBuyConfirmMenu(plugin, player, listing, this).open();
            }));
        }

        // 5. Bottom Navigation Bar
        setButton(45, new BackButton(this, parent));

        if (validPage > 1) {
            setButton(47, new GuiButton(new ItemBuilder(Material.ARROW).name("&e◀ Halaman Sebelumnya (" + (validPage - 1) + ")").build(), event -> {
                new AuctionBrowseMenu(plugin, player, parent, validPage - 1, sortMode, filter).open();
            }));
        }

        setButton(48, new GuiButton(new ItemBuilder(Material.MAP).name("&7Halaman &e" + validPage + " &8/ &f" + maxPages).build()));

        // Bottom Center: My Auctions Management Button (Slot 49)
        setButton(49, new GuiButton(new ItemBuilder(Material.CHEST)
                .name("&b&l[📦] LELANG SAYA & PENGELOLAAN")
                .lore(List.of(
                        "&7Kelola barang yang sedang Anda jual,",
                        "&7ubah harga, batalkan lelang, atau klaim barang.",
                        " ",
                        "&bKlik untuk membuka menu lelang saya >"
                ))
                .build(), event -> {
            new MyAuctionsMenu(plugin, player, this).open();
        }));

        if (validPage < maxPages) {
            setButton(51, new GuiButton(new ItemBuilder(Material.ARROW).name("&eHalaman Berikutnya (" + (validPage + 1) + ") ▶").build(), event -> {
                new AuctionBrowseMenu(plugin, player, parent, validPage + 1, sortMode, filter).open();
            }));
        }

        setButton(53, new CloseButton());
    }

    private String getSortName() {
        return switch (sortMode) {
            case 1 -> "Harga Terendah";
            case 2 -> "Harga Tertinggi";
            default -> "Terbaru";
        };
    }
}
