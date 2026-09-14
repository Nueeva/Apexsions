package com.apexsions.fishing.gui;

import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.model.PlayerVaultData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Interactive Fishing Vault GUI with bottom navigation bar (Slots 45-53)
 * and strict fishing-only item enforcement (Slots 0-44).
 */
public class FishingVaultGUI implements InventoryHolder {

    private final ApexsionsFishing plugin;
    private final Player player;
    private final UUID targetUuid;
    private final String targetName;
    private final boolean adminMode;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private int currentPage;
    private final int totalUnlockedPages;

    // Slot definitions for bottom navigation bar
    public static final int SLOT_PREV = 45;
    public static final int SLOT_SHOP = 46;
    public static final int SLOT_SELL = 47;
    public static final int SLOT_JUMP = 48;
    public static final int SLOT_INFO = 49;
    public static final int SLOT_SORT = 50;
    public static final int SLOT_DEPOSIT_ALL = 51;
    public static final int SLOT_STATUS = 52;
    public static final int SLOT_NEXT = 53;

    public FishingVaultGUI(@NotNull ApexsionsFishing plugin, @NotNull Player player) {
        this(plugin, player, player.getUniqueId(), player.getName(), 1, false);
    }

    public FishingVaultGUI(@NotNull ApexsionsFishing plugin, @NotNull Player player, int page) {
        this(plugin, player, player.getUniqueId(), player.getName(), page, false);
    }

    public FishingVaultGUI(@NotNull ApexsionsFishing plugin, @NotNull Player player, @NotNull UUID targetUuid, @NotNull String targetName, int page, boolean adminMode) {
        this.plugin = plugin;
        this.player = player;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.adminMode = adminMode;

        PlayerVaultData vaultData = plugin.getVaultStorageManager().getVaultData(targetUuid);
        this.totalUnlockedPages = Math.max(1, vaultData.getUnlockedPages());
        this.currentPage = Math.max(1, Math.min(page, totalUnlockedPages));

        String title = adminMode
                ? "<dark_red><bold>Admin Vault:</bold></dark_red> <yellow>" + targetName + "</yellow> <gray>(Hal. " + currentPage + "/" + totalUnlockedPages + ")</gray>"
                : "<gold><bold>Penyimpanan Mancing</bold></gold> <dark_gray>●</dark_gray> <yellow>Hal. " + currentPage + "/" + totalUnlockedPages + "</yellow>";

        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize(title));
        loadPage(currentPage);
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void loadPage(int page) {
        this.currentPage = Math.max(1, Math.min(page, totalUnlockedPages));
        PlayerVaultData vaultData = plugin.getVaultStorageManager().getVaultData(targetUuid);
        ItemStack[] items = vaultData.getPage(currentPage);

        // Load storage items into slots 0-44
        for (int i = 0; i < PlayerVaultData.SLOTS_PER_PAGE; i++) {
            inventory.setItem(i, items[i]);
        }

        // Render Navigation Bar (Slots 45-53)
        renderNavigationBar();
    }

    private void renderNavigationBar() {
        // Slot 45: Prev Page
        if (currentPage > 1) {
            inventory.setItem(SLOT_PREV, createGuiItem(Material.ARROW,
                    "<yellow><bold>◀ Halaman Sebelumnya</bold></yellow>",
                    List.of(
                            "<gray>Menuju ke Halaman <gold>" + (currentPage - 1) + "</gold></gray>",
                            "<dark_gray>Klik untuk berpindah halaman</dark_gray>"
                    )));
        } else {
            inventory.setItem(SLOT_PREV, createGuiItem(Material.BARRIER,
                    "<dark_gray>◀ Halaman Sebelumnya</dark_gray>",
                    List.of("<gray>Anda sudah berada di halaman pertama.</gray>")));
        }

        // Slot 46: Beli Slot Penyimpanan (Vault Shop)
        inventory.setItem(SLOT_SHOP, createGuiItem(Material.EMERALD,
                "<gradient:#00ff87:#60efff><bold>🛒 Beli Tambahan Halaman</bold></gradient>",
                List.of(
                        "<gray>Buka kapasitas halaman baru hingga 30!</gray>",
                        "<gray>Mendukung pembayaran <yellow>Rupiah</yellow> & <aqua>Diamond</aqua>.</gray>",
                        "",
                        "<yellow>▶ Klik untuk buka Toko Penyimpanan</yellow>"
                )));

        // Slot 47: Quick Sell Fish
        inventory.setItem(SLOT_SELL, createGuiItem(Material.GOLD_INGOT,
                "<gold><bold>⚖ Jual Ikan ke Nelayan</bold></gold>",
                List.of(
                        "<gray>Jual hasil tangkapan ikan segarmu</gray>",
                        "<gray>dan raih pundi-pundi rupiah!</gray>",
                        "",
                        "<yellow>▶ Klik untuk buka Menu Penjualan</yellow>"
                )));

        // Slot 48: Jump Page
        inventory.setItem(SLOT_JUMP, createGuiItem(Material.COMPASS,
                "<aqua><bold>🧭 Lompat Halaman</bold></aqua>",
                List.of(
                        "<gray>Halaman Saat Ini: <gold>" + currentPage + " / " + totalUnlockedPages + "</gold></gray>",
                        "",
                        "<yellow>● Klik Kiri:</yellow> <gray>Maju +5 Halaman</gray>",
                        "<yellow>● Klik Kanan:</yellow> <gray>Mundur -5 Halaman</gray>",
                        "<yellow>● Shift + Klik:</yellow> <gray>Halaman Pertama / Terakhir</gray>"
                )));

        // Slot 49: Info Status
        inventory.setItem(SLOT_INFO, createGuiItem(Material.BOOK,
                "<yellow><bold>ℹ Informasi Penyimpanan</bold></yellow>",
                List.of(
                        "<gray>Pemilik: <white>" + targetName + "</white></gray>",
                        "<gray>Halaman Aktif: <gold>" + currentPage + " / " + totalUnlockedPages + "</gold></gray>",
                        "<gray>Maksimal Halaman: <aqua>30 Halaman</aqua></gray>",
                        "",
                        "<green>✔ Khusus barang memancing</green>",
                        "<dark_gray>(Ikan, Sampah, Harta Karun, Umpan, Pancingan)</dark_gray>"
                )));

        // Slot 50: Sort Vault
        inventory.setItem(SLOT_SORT, createGuiItem(Material.HOPPER,
                "<light_purple><bold>⚡ Rapikan / Urutkan Ikan</bold></light_purple>",
                List.of(
                        "<gray>Urutkan semua ikan di halaman ini</gray>",
                        "<gray>berdasarkan Rarity & Bobot tertinggi!</gray>",
                        "",
                        "<yellow>▶ Klik untuk mengurutkan</yellow>"
                )));

        // Slot 51: Deposit All from Inventory
        inventory.setItem(SLOT_DEPOSIT_ALL, createGuiItem(Material.CHEST,
                "<gold><bold>📥 Masukkan Semua Ikan</bold></gold>",
                List.of(
                        "<gray>Otomatis memindahkan semua hasil mancing</gray>",
                        "<gray>dari inventory kamu ke dalam penyimpanan ini.</gray>",
                        "",
                        "<yellow>▶ Klik untuk deposit cepat</yellow>"
                )));

        // Slot 52: Status Indicator
        inventory.setItem(SLOT_STATUS, createGuiItem(Material.LIME_STAINED_GLASS_PANE,
                "<green><bold>STATUS: TERBUKA</bold></green>",
                List.of(
                        "<gray>Halaman ini aktif dan dapat digunakan.</gray>",
                        "<gray>Kapasitas: <yellow>45 Slot per Halaman</yellow></gray>"
                )));

        // Slot 53: Next Page
        if (currentPage < totalUnlockedPages) {
            inventory.setItem(SLOT_NEXT, createGuiItem(Material.ARROW,
                    "<yellow><bold>Halaman Berikutnya ▶</bold></yellow>",
                    List.of(
                            "<gray>Menuju ke Halaman <gold>" + (currentPage + 1) + "</gold></gray>",
                            "<dark_gray>Klik untuk berpindah halaman</dark_gray>"
                    )));
        } else {
            inventory.setItem(SLOT_NEXT, createGuiItem(Material.BARRIER,
                    "<red><bold>Halaman Terkunci ▶</bold></red>",
                    List.of(
                            "<gray>Anda belum membuka Halaman <gold>" + (currentPage + 1) + "</gold>.</gray>",
                            "<yellow>Beli di Toko Penyimpanan untuk membuka!</yellow>"
                    )));
        }
    }

    public void handleClick(@NotNull InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();

        // 1. Clicks on Navigation Bar (Slots 45-53)
        if (rawSlot >= 45 && rawSlot < 54) {
            event.setCancelled(true);
            handleNavClick(rawSlot, event.getClick());
            return;
        }

        // 2. Clicks in Top Inventory (Slots 0-44)
        if (rawSlot < 45) {
            // Check Cursor item placement
            ItemStack cursorItem = event.getCursor();
            if (cursorItem != null && !cursorItem.getType().isAir()) {
                if (!plugin.getLootGenerator().isFishingItem(cursorItem)) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red>⚠ Kotak Penyimpanan Mancing hanya dapat menyimpan ikan, hasil tangkapan, umpan, dan pancingan!</red>"));
                    return;
                }
            }

            // Check Number Key swap (1-9 hotbar buttons)
            if (event.getClick() == ClickType.NUMBER_KEY) {
                int hotbarSlot = event.getHotbarButton();
                if (hotbarSlot >= 0 && hotbarSlot < 9) {
                    ItemStack hotbarItem = player.getInventory().getItem(hotbarSlot);
                    if (hotbarItem != null && !hotbarItem.getType().isAir() && !plugin.getLootGenerator().isFishingItem(hotbarItem)) {
                        event.setCancelled(true);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        player.sendMessage(mm.deserialize("<red>⚠ Barang dari hotbar bukan barang hasil memancing!</red>"));
                        return;
                    }
                }
            }

            // Check Double Click / Collect to cursor
            if (event.getClick() == ClickType.DOUBLE_CLICK) {
                if (cursorItem != null && !cursorItem.getType().isAir() && !plugin.getLootGenerator().isFishingItem(cursorItem)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // 3. Shift-Clicking from player inventory into vault
        if (event.isShiftClick() && event.getView().getBottomInventory().equals(event.getClickedInventory())) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && !clickedItem.getType().isAir()) {
                if (!plugin.getLootGenerator().isFishingItem(clickedItem)) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red>⚠ Hanya barang hasil memancing yang dapat dipindahkan ke penyimpanan ini!</red>"));
                    return;
                }
            }
        }

        // Allow normal inventory interaction for valid items in slots 0-44
        // Automatically save on tick end
        Bukkit.getScheduler().runTask(plugin, this::saveCurrentPageToMemory);
    }

    public void handleDrag(@NotNull InventoryDragEvent event) {
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= 45 && rawSlot < 54) {
                event.setCancelled(true);
                return;
            }
            if (rawSlot < 45) {
                ItemStack item = event.getOldCursor();
                if (!plugin.getLootGenerator().isFishingItem(item)) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red>⚠ Kotak Penyimpanan Mancing hanya dapat menyimpan barang hasil memancing!</red>"));
                    return;
                }
            }
        }
        Bukkit.getScheduler().runTask(plugin, this::saveCurrentPageToMemory);
    }

    public void handleClose(@NotNull InventoryCloseEvent event) {
        saveCurrentPageToMemory();
        plugin.getVaultStorageManager().savePlayerData(targetUuid);
    }

    private void handleNavClick(int slot, ClickType click) {
        switch (slot) {
            case SLOT_PREV -> {
                if (currentPage > 1) {
                    saveCurrentPageToMemory();
                    new FishingVaultGUI(plugin, player, targetUuid, targetName, currentPage - 1, adminMode).open();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                }
            }
            case SLOT_NEXT -> {
                if (currentPage < totalUnlockedPages) {
                    saveCurrentPageToMemory();
                    new FishingVaultGUI(plugin, player, targetUuid, targetName, currentPage + 1, adminMode).open();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    new VaultShopGUI(plugin, player).open();
                }
            }
            case SLOT_SHOP -> {
                saveCurrentPageToMemory();
                new VaultShopGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            }
            case SLOT_SELL -> {
                saveCurrentPageToMemory();
                new FishSellGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            }
            case SLOT_JUMP -> {
                saveCurrentPageToMemory();
                int target;
                if (click.isShiftClick()) {
                    target = (currentPage == 1) ? totalUnlockedPages : 1;
                } else if (click.isRightClick()) {
                    target = Math.max(1, currentPage - 5);
                } else {
                    target = Math.min(totalUnlockedPages, currentPage + 5);
                }
                new FishingVaultGUI(plugin, player, targetUuid, targetName, target, adminMode).open();
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
            }
            case SLOT_SORT -> {
                sortVaultItems();
            }
            case SLOT_DEPOSIT_ALL -> {
                depositAllFromInventory();
            }
        }
    }

    private void sortVaultItems() {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < PlayerVaultData.SLOTS_PER_PAGE; i++) {
            ItemStack is = inventory.getItem(i);
            if (is != null && !is.getType().isAir()) {
                items.add(is);
            }
        }

        // Sort descending: by fish price/rarity tier, then weight
        items.sort((a, b) -> {
            double priceA = plugin.getLootGenerator().getFishPrice(a);
            double priceB = plugin.getLootGenerator().getFishPrice(b);
            if (Double.compare(priceB, priceA) != 0) {
                return Double.compare(priceB, priceA);
            }
            double wA = plugin.getLootGenerator().getFishWeight(a);
            double wB = plugin.getLootGenerator().getFishWeight(b);
            return Double.compare(wB, wA);
        });

        for (int i = 0; i < PlayerVaultData.SLOTS_PER_PAGE; i++) {
            if (i < items.size()) {
                inventory.setItem(i, items.get(i));
            } else {
                inventory.setItem(i, null);
            }
        }

        saveCurrentPageToMemory();
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
        player.sendMessage(mm.deserialize("<green>Penyimpanan Halaman " + currentPage + " berhasil dirapikan!</green>"));
    }

    private void depositAllFromInventory() {
        int moved = 0;
        Inventory pInv = player.getInventory();
        for (int s = 0; s < pInv.getSize(); s++) {
            ItemStack item = pInv.getItem(s);
            if (item != null && !item.getType().isAir() && plugin.getLootGenerator().isFishingItem(item)) {
                // Find empty slot in vault
                for (int vSlot = 0; vSlot < PlayerVaultData.SLOTS_PER_PAGE; vSlot++) {
                    ItemStack vItem = inventory.getItem(vSlot);
                    if (vItem == null || vItem.getType().isAir()) {
                        inventory.setItem(vSlot, item);
                        pInv.setItem(s, null);
                        moved++;
                        break;
                    }
                }
            }
        }

        if (moved > 0) {
            saveCurrentPageToMemory();
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
            player.sendMessage(mm.deserialize("<green>Sukses memindahkan <gold>" + moved + " barang mancing</gold> ke penyimpanan!</green>"));
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<yellow>Tidak ada barang mancing di inventory atau penyimpanan di halaman ini sudah penuh.</yellow>"));
        }
    }

    private void saveCurrentPageToMemory() {
        ItemStack[] items = new ItemStack[PlayerVaultData.SLOTS_PER_PAGE];
        for (int i = 0; i < PlayerVaultData.SLOTS_PER_PAGE; i++) {
            items[i] = inventory.getItem(i);
        }
        PlayerVaultData vaultData = plugin.getVaultStorageManager().getVaultData(targetUuid);
        vaultData.setPage(currentPage, items);
    }

    private ItemStack createGuiItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            List<Component> compLore = new ArrayList<>();
            for (String l : lore) {
                compLore.add(mm.deserialize(l));
            }
            meta.lore(compLore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
