package com.apexsions.crates.shop.gui;

import com.apexsions.core.api.ApexsionsCoreProvider;
import com.apexsions.core.gui.input.ApexsionsInputManager;
import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.key.CrateKey;
import com.apexsions.crates.shop.CrateKeyShopManager;
import com.apexsions.crates.shop.KeyShopEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Admin GUI for configuring Crate Key prices, active status, and currencies.
 */
public class CrateKeyShopAdminGUI implements InventoryHolder {

    private static final int[] CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final ApexsionsCratesPlugin plugin;
    private final Player player;
    private final CrateKeyShopManager shopManager;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Inventory inventory;
    private final Map<Integer, String> slotKeyMap = new HashMap<>();
    private int page = 1;

    public CrateKeyShopAdminGUI(@NotNull ApexsionsCratesPlugin plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.shopManager = plugin.getKeyShopManager();
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>⚙ ADMIN CRATE KEY SHOP ⚙</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotKeyMap.clear();

        ItemStack borderPane = createGlass(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        ItemStack decorPane = createGlass(Material.RED_STAINED_GLASS_PANE, "<red>✦</red>");

        // Fill borders
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderPane);
            }
        }
        inventory.setItem(1, decorPane);
        inventory.setItem(7, decorPane);
        inventory.setItem(46, decorPane);
        inventory.setItem(52, decorPane);

        // Header
        inventory.setItem(4, createHeaderItem());

        // Get all keys sorted by ID
        List<CrateKey> allKeys = new ArrayList<>(plugin.getKeyManager().getKeys());
        allKeys.sort(Comparator.comparing(CrateKey::getId));

        int itemsPerPage = CONTENT_SLOTS.length;
        int totalPages = Math.max(1, (int) Math.ceil((double) allKeys.size() / itemsPerPage));
        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allKeys.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slot = CONTENT_SLOTS[i - startIndex];
            CrateKey crateKey = allKeys.get(i);
            KeyShopEntry entry = shopManager.getOrCreateEntry(crateKey.getId());

            slotKeyMap.put(slot, crateKey.getId());
            inventory.setItem(slot, createAdminKeyItem(entry, crateKey));
        }

        // Navigation controls
        if (page > 1) {
            inventory.setItem(48, createNavButton(Material.ARROW, "<yellow>◀ Halaman " + (page - 1) + "</yellow>"));
        }
        if (page < totalPages) {
            inventory.setItem(50, createNavButton(Material.ARROW, "<yellow>Halaman " + (page + 1) + " ▶</yellow>"));
        }

        // Slot 45: Back to player shop
        ItemStack backBtn = new ItemStack(Material.OAK_DOOR);
        ItemMeta bMeta = backBtn.getItemMeta();
        if (bMeta != null) {
            bMeta.displayName(mm.deserialize("<yellow><bold>◀ KEMBALI KE TOKO PEMAIN</bold></yellow>"));
            bMeta.lore(List.of(mm.deserialize("<gray>Kembali ke menu pembelian /crateshop</gray>")));
            backBtn.setItemMeta(bMeta);
        }
        inventory.setItem(45, backBtn);

        // Slot 49: Close
        ItemStack closeBtn = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeBtn.getItemMeta();
        if (closeMeta != null) {
            closeMeta.displayName(mm.deserialize("<red><bold>✖ TUTUP</bold></red>"));
            closeBtn.setItemMeta(closeMeta);
        }
        inventory.setItem(49, closeBtn);

        // Slot 53: Refresh / Save
        ItemStack saveBtn = new ItemStack(Material.EMERALD);
        ItemMeta sMeta = saveBtn.getItemMeta();
        if (sMeta != null) {
            sMeta.displayName(mm.deserialize("<green><bold>💾 SIMPAN & MUAT ULANG</bold></green>"));
            sMeta.lore(List.of(mm.deserialize("<gray>Sinkronkan ulang kunci dengan disk.</gray>")));
            saveBtn.setItemMeta(sMeta);
        }
        inventory.setItem(53, saveBtn);
    }

    private ItemStack createHeaderItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize("<gradient:#e74c3c:#f39c12><bold>⚙ PENGATURAN ADMIN: CRATE KEY SHOP</bold></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Atur ketersediaan kunci, harga satuan, dan mata uang.</gray>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<gray>Total Kunci Sistem:</gray> <yellow>" + plugin.getKeyManager().getKeys().size() + "</yellow>"));
            lore.add(mm.deserialize("<gray>Kunci Aktif Dijual:</gray> <green>" + shopManager.getEnabledEntries().size() + "</green>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<yellow>Gunakan tombol di bawah untuk mengatur masing-masing kunci!</yellow>"));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createAdminKeyItem(KeyShopEntry entry, CrateKey crateKey) {
        ItemStack base = crateKey.getItemStack();
        if (base.getType().isAir()) {
            base = new ItemStack(Material.TRIPWIRE_HOOK);
        } else {
            base = base.clone();
        }

        ItemMeta meta = base.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize("<gradient:#f39c12:#e74c3c><bold>" + crateKey.getName() + "</bold></gradient>"));

            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>ID Kunci:</gray> <yellow>" + crateKey.getId() + "</yellow>"));
            lore.add(mm.deserialize("<gray>Tipe Kunci:</gray> <yellow>" + (crateKey.isVirtual() ? "Virtual Key ✦" : "Physical Item 🗝") + "</yellow>"));
            lore.add(Component.empty());

            if (entry.isEnabled()) {
                lore.add(mm.deserialize("<gray>Status Penjualan:</gray> <green><bold>✔ AKTIF (DIJUAL)</bold></green>"));
            } else {
                lore.add(mm.deserialize("<gray>Status Penjualan:</gray> <red><bold>✖ NONAKTIF (DISEMBUNYIKAN)</bold></red>"));
            }

            lore.add(mm.deserialize("<gray>Harga Satuan:</gray> <gold><bold>" + shopManager.formatPrice(entry.getPrice(), entry.getCurrency()) + "</bold></gold>"));
            String currBadge;
            if (entry.isDiamond()) {
                currBadge = "<aqua><bold>DIAMOND 💎</bold></aqua>";
            } else if (entry.isBattleCoins()) {
                currBadge = "<yellow><bold>BATTLE COINS 🪙</bold></yellow>";
            } else {
                currBadge = "<green><bold>RUPIAH (Rp.)</bold></green>";
            }
            lore.add(mm.deserialize("<gray>Mata Uang:</gray> " + currBadge));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<yellow>▶ Klik Kiri:</yellow> <white>Toggle Status (Jual / Sembunyikan)</white>"));
            lore.add(mm.deserialize("<yellow>▶ Klik Kanan:</yellow> <gold>Ubah Harga Kunci</gold>"));
            lore.add(mm.deserialize("<yellow>▶ Shift + Klik:</yellow> <aqua>Ganti Mata Uang (Rp. ⇄ 💎 ⇄ 🪙)</aqua>"));

            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            base.setItemMeta(meta);
        }
        return base;
    }

    private ItemStack createNavButton(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGlass(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            new CrateKeyShopGUI(plugin, player).open();
            return;
        }

        if (slot == 53) {
            shopManager.load();
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            player.sendMessage(mm.deserialize("<green>Konfigurasi Crate Key Shop berhasil dimuat ulang & disimpan!</green>"));
            buildGUI();
            return;
        }

        if (slot == 48 && page > 1) {
            page--;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            buildGUI();
            return;
        }

        if (slot == 50) {
            page++;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            buildGUI();
            return;
        }

        String keyId = slotKeyMap.get(slot);
        if (keyId != null) {
            CrateKey crateKey = plugin.getKeyManager().getKeyById(keyId);
            KeyShopEntry entry = shopManager.getOrCreateEntry(keyId);

            if (event.isShiftClick()) {
                // Cycle Currency: rupiah <-> diamond
                entry.cycleCurrency();
                shopManager.save();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.5f);
                player.sendMessage(mm.deserialize("<green>Mata uang untuk kunci <yellow>" + (crateKey != null ? crateKey.getName() : keyId) + "</yellow> diubah menjadi <gold>" + entry.getCurrency().toUpperCase(Locale.ROOT) + "</gold>!</green>"));
                buildGUI();
                return;
            }

            if (event.isRightClick()) {
                // Edit Price
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);

                String keyName = (crateKey != null) ? crateKey.getName() : keyId;
                if (ApexsionsCoreProvider.isAvailable()) {
                    ApexsionsInputManager.openDoubleInput(
                            plugin,
                            player,
                            "HARGA KUNCI " + keyId.toUpperCase(Locale.ROOT),
                            "Masukkan harga per kunci untuk " + keyName + " (angka):",
                            entry.getPrice(),
                            0.0,
                            100_000_000.0,
                            newPrice -> {
                                entry.setPrice(newPrice);
                                shopManager.save();
                                player.sendMessage(mm.deserialize("<green>Harga kunci <yellow>" + keyName + "</yellow> berhasil diubah menjadi <gold>" + shopManager.formatPrice(newPrice, entry.getCurrency()) + "</gold>!</green>"));
                                Bukkit.getScheduler().runTask(plugin, () -> new CrateKeyShopAdminGUI(plugin, player).open());
                            },
                            () -> Bukkit.getScheduler().runTask(plugin, () -> new CrateKeyShopAdminGUI(plugin, player).open())
                    );
                } else {
                    player.sendMessage(mm.deserialize("<red>ApexsionsCore tidak tersedia untuk input dialog.</red>"));
                }
                return;
            }

            // Left click: Toggle enabled/disabled
            entry.setEnabled(!entry.isEnabled());
            shopManager.save();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, entry.isEnabled() ? 1.4f : 0.8f);
            buildGUI();
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
