package com.apexsions.crates.shop.gui;

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
 * Player-facing Crate Key Shop GUI.
 */
public class CrateKeyShopGUI implements InventoryHolder {

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

    public CrateKeyShopGUI(@NotNull ApexsionsCratesPlugin plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.shopManager = plugin.getKeyShopManager();
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#f39c12:#e74c3c><bold>✦ TOKO CRATE KEYS ✦</bold></gradient>"));
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
        ItemStack decorPane = createGlass(Material.ORANGE_STAINED_GLASS_PANE, "<gold>✦</gold>");

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

        // Header slot 4: Info Beacon
        inventory.setItem(4, createHeaderItem());

        // Keys list
        List<KeyShopEntry> enabledEntries = shopManager.getEnabledEntries();
        int itemsPerPage = CONTENT_SLOTS.length;
        int totalPages = Math.max(1, (int) Math.ceil((double) enabledEntries.size() / itemsPerPage));
        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, enabledEntries.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slot = CONTENT_SLOTS[i - startIndex];
            KeyShopEntry entry = enabledEntries.get(i);
            CrateKey crateKey = plugin.getKeyManager().getKeyById(entry.getKeyId());
            if (crateKey == null) continue;

            slotKeyMap.put(slot, entry.getKeyId());
            inventory.setItem(slot, createKeyShopItem(entry, crateKey));
        }

        // Navigation controls
        if (page > 1) {
            inventory.setItem(48, createNavButton(Material.ARROW, "<yellow>◀ Halaman " + (page - 1) + "</yellow>"));
        }
        if (page < totalPages) {
            inventory.setItem(50, createNavButton(Material.ARROW, "<yellow>Halaman " + (page + 1) + " ▶</yellow>"));
        }

        // Slot 49: Close
        ItemStack closeBtn = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeBtn.getItemMeta();
        if (closeMeta != null) {
            closeMeta.displayName(mm.deserialize("<red><bold>✖ TUTUP</bold></red>"));
            closeBtn.setItemMeta(closeMeta);
        }
        inventory.setItem(49, closeBtn);
    }

    private ItemStack createHeaderItem() {
        ItemStack item = new ItemStack(Material.BEACON);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize("<gradient:#e67e22:#f1c40f><bold>🗝 PASAR CRATE KEY APEXSIONS</bold></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Beli kunci peti misteri kerajaan dengan aman.</gray>"));
            lore.add(Component.empty());

            double rupiahBal = shopManager.getPlayerBalance(player, "rupiah");
            double diamondBal = shopManager.getPlayerBalance(player, "diamond");
            double coinsBal = shopManager.getPlayerBalance(player, "battle_coins");

            lore.add(mm.deserialize("<gray>Saldo Rupiah Kamu:</gray> <green><bold>" + shopManager.formatPrice(rupiahBal, "rupiah") + "</bold></green>"));
            lore.add(mm.deserialize("<gray>Saldo Diamond Kamu:</gray> <aqua><bold>" + (long) diamondBal + " 💎</bold></aqua>"));
            lore.add(mm.deserialize("<gray>Saldo Battle Coins Kamu:</gray> <yellow><bold>" + (long) coinsBal + " 🪙</bold></yellow>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<yellow>Klik pada kunci di bawah untuk melakukan pembelian!</yellow>"));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createKeyShopItem(KeyShopEntry entry, CrateKey crateKey) {
        ItemStack base = crateKey.getItemStack();
        if (base.getType().isAir()) {
            base = new ItemStack(Material.TRIPWIRE_HOOK);
        } else {
            base = base.clone();
        }

        ItemMeta meta = base.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize("<gradient:#f39c12:#f1c40f><bold>" + crateKey.getName() + "</bold></gradient>"));

            double unitPrice = entry.getPrice();
            String currency = entry.getCurrency();
            String unitFormatted = shopManager.formatPrice(unitPrice, currency);
            String x5Formatted = shopManager.formatPrice(unitPrice * 5, currency);
            String x10Formatted = shopManager.formatPrice(unitPrice * 10, currency);

            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Tipe Kunci: <yellow>" + (crateKey.isVirtual() ? "Virtual Key ✦" : "Physical Item 🗝") + "</yellow></gray>"));
            String currTag = "diamond".equalsIgnoreCase(currency) ? "DIAMOND 💎" : (("battle_coins".equalsIgnoreCase(currency) || "battlecoins".equalsIgnoreCase(currency)) ? "BATTLE COINS 🪙" : "RUPIAH (Rp.)");
            lore.add(mm.deserialize("<gray>Mata Uang: <gold>" + currTag + "</gold></gray>"));
            lore.add(mm.deserialize("<gray>Harga Satuan:</gray> <gold><bold>" + unitFormatted + "</bold></gold>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<white>Pilihan Pembelian:</white>"));
            lore.add(mm.deserialize("<yellow>▶ Klik Kiri:</yellow> <gray>Beli 1x (</gray><gold>" + unitFormatted + "</gold><gray>)</gray>"));
            lore.add(mm.deserialize("<yellow>▶ Klik Kanan:</yellow> <gray>Beli 5x (</gray><gold>" + x5Formatted + "</gold><gray>)</gray>"));
            lore.add(mm.deserialize("<yellow>▶ Shift + Klik:</yellow> <gray>Beli 10x (</gray><gold>" + x10Formatted + "</gold><gray>)</gray>"));

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
            int amount = 1;
            if (event.isShiftClick()) {
                amount = 10;
            } else if (event.isRightClick()) {
                amount = 5;
            }

            boolean bought = shopManager.buyKey(player, keyId, amount);
            if (bought) {
                buildGUI(); // Refresh balance and display
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
