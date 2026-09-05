package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Central Admin Dashboard GUI for ApexsionsCustomEnchants (/ace).
 */
public class AceAdminHubGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public AceAdminHubGUI(ApexsionsCustomEnchantsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 45, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>⚙ APEXSIONS CUSTOM ENCHANTS ADMIN HUB ⚙</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Header slot 4: Overview
        inventory.setItem(4, createItem(Material.NETHER_STAR, "<gradient:#f1c40f:#e67e22><bold>⚡ APEXSIONS CUSTOM ENCHANTS ⚡</bold></gradient>", List.of(
                "<gray>Versi:</gray> <gold>v1.0.0 (Paper 1.21.4)</gold>",
                "<gray>Total Sihir:</gray> <yellow>" + plugin.getEnchantmentRegistry().getAllEnchantments().size() + " Enchantments</yellow>",
                "<gray>Total Tier:</gray> <yellow>7 Kasta Groups</yellow>",
                "",
                "<yellow>Pilih menu admin di bawah untuk kontrol penuh sihir.</yellow>"
        )));

        // Slot 20: Katalog Enchants (/ace enchants)
        inventory.setItem(20, createItem(Material.ENCHANTED_BOOK, "<gradient:#9b59b6:#3498db><bold>📚 KATALOG ENCHANTS (/ace enchants)</bold></gradient>", List.of(
                "<gray>Format persis /ae admin AdvancedEnchantments:</gray>",
                "<dark_gray>•</dark_gray> <yellow>Katalog 45 slot per halaman & pencarian</yellow>",
                "<dark_gray>•</dark_gray> <yellow>Klik sihir untuk buka submenu buku per level</yellow>",
                "<dark_gray>•</dark_gray> <yellow>Left-Click: Ambil buku ke tas</yellow>",
                "<dark_gray>•</dark_gray> <yellow>Right-Click: Langsung enchant item di tangan</yellow>",
                "",
                "<yellow>▶ Klik untuk buka Katalog Enchants!</yellow>"
        )));

        // Slot 21: Admin Item Creator GUI (/ace create)
        inventory.setItem(21, createItem(Material.ANVIL, "<gradient:#e67e22:#f1c40f><bold>🔨 ADMIN ITEM CREATOR (/ace create)</bold></gradient>", List.of(
                "<gray>Buat senjata, alat & armor serba GUI:</gray>",
                "<dark_gray>•</dark_gray> <yellow>Pilih Base Equipment</yellow>",
                "<dark_gray>•</dark_gray> <yellow>Kombinasi Custom Enchants & Vanilla Enchants</yellow>",
                "<dark_gray>•</dark_gray> <yellow>Konfigurasi Armor Set Bonus Kustom</yellow>",
                "",
                "<yellow>▶ Klik untuk buka Item Creator!</yellow>"
        )));

        // Slot 22: Tier Pricing & Currency Settings (/ace pricing)
        inventory.setItem(22, createItem(Material.GOLD_INGOT, "<gradient:#2ecc71:#27ae60><bold>💰 TIER PRICING & ODDS (/ace pricing)</bold></gradient>", List.of(
                "<gray>Pengaturan harga & mata uang Enchanter:</gray>",
                "<dark_gray>•</dark_gray> <green>Ubah jenis pembayaran (Rupiah ↔ Diamond 💎)</green>",
                "<dark_gray>•</dark_gray> <green>Atur harga gacha acak per tier</green>",
                "<dark_gray>•</dark_gray> <green>Atur multiplier toko buku spesifik & peluang</green>",
                "",
                "<yellow>▶ Klik untuk buka Pengaturan Harga!</yellow>"
        )));

        // Slot 23: Enchanter Player View Preview (/ce)
        inventory.setItem(23, createItem(Material.ENDER_EYE, "<gradient:#3498db:#2ecc71><bold>👁 PREVIEW ENCHANTER PLAYER (/ce)</bold></gradient>", List.of(
                "<gray>Buka tampilan menu Enchanter sebagaimana</gray>",
                "<gray>yang dilihat oleh pemain biasa.</gray>",
                "",
                "<yellow>▶ Klik untuk uji coba menu /ce!</yellow>"
        )));

        // Slot 24: Beri Paket Magic Items
        inventory.setItem(24, createItem(Material.CHEST, "<gradient:#e74c3c:#c0392b><bold>🎁 PAKET MAGIC ITEMS</bold></gradient>", List.of(
                "<gray>Klaim langsung paket utilitas sihir:</gray>",
                "<dark_gray>•</dark_gray> <white>1x White Scroll (Proteksi Kehancuran)</white>",
                "<dark_gray>•</dark_gray> <white>1x Black Scroll (Ekstraksi Sihir)</white>",
                "<dark_gray>•</dark_gray> <white>5x Mystery Dust (Booster Rate)</white>",
                "",
                "<yellow>▶ Klik untuk klaim ke inventory!</yellow>"
        )));

        // Slot 30: Presets GUI (/ace presets)
        inventory.setItem(30, createItem(Material.CHEST_MINECART, "<gradient:#9b59b6:#e74c3c><bold>📦 PRESET SET ARMOR & TOOLS (/ace presets)</bold></gradient>", List.of(
                "<gray>Kelola seluruh set equipment yang tersimpan:</gray>",
                "<dark_gray>•</dark_gray> <yellow>Ambil seluruh item set armor & tools</yellow>",
                "<dark_gray>•</dark_gray> <yellow>Hapus preset usang / tidak terpakai</yellow>",
                "",
                "<yellow>▶ Klik untuk buka GUI Presets!</yellow>"
        )));

        // Slot 31: Reload Configurations
        inventory.setItem(31, createItem(Material.REDSTONE, "<red><bold>⚡ RELOAD KONFIGURASI ⚡</bold></red>", List.of(
                "<gray>Muat ulang seluruh file konfigurasi:</gray>",
                "<dark_gray>•</dark_gray> <white>config.yml, groups.yml, enchantments.yml</white>",
                "",
                "<yellow>▶ Klik untuk reload plugin!</yellow>"
        )));

        // Slot 40: Close
        inventory.setItem(40, createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>", List.of("<gray>Keluar dari panel admin.</gray>")));
    }

    private ItemStack createItem(Material mat, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            if (loreLines != null) {
                List<Component> cList = new ArrayList<>();
                for (String l : loreLines) {
                    cList.add(mm.deserialize(l));
                }
                meta.lore(cList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 40) { // Close
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        if (slot == 20) { // Catalog Enchants
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new AceEnchantsCatalogGUI(plugin, player, 1, null).open();
            return;
        }

        if (slot == 21) { // Item Creator
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new AdminItemCreatorGUI(plugin, player).open();
            return;
        }

        if (slot == 22) { // Tier Pricing
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new AdminTierPricingGUI(plugin, player).open();
            return;
        }

        if (slot == 23) { // Enchanter Preview
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new EnchanterGUI(plugin, player).open();
            return;
        }

        if (slot == 24) { // Give Magic Items
            player.getInventory().addItem(plugin.getScrollManager().createWhiteScroll());
            player.getInventory().addItem(plugin.getScrollManager().createBlackScroll());
            for (int i = 0; i < 5; i++) {
                player.getInventory().addItem(plugin.getMagicDustManager().createMysteryDust());
            }
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
            player.sendMessage(mm.deserialize("<green>✓ Berhasil mengklaim paket Magic Items!</green>"));
            return;
        }

        if (slot == 30) { // Presets GUI
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new AdminPresetsGUI(plugin, player, this).open();
            return;
        }

        if (slot == 31) { // Reload
            plugin.reload();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
            player.sendMessage(mm.deserialize("<green>✓ ApexsionsCustomEnchants berhasil dimuat ulang!</green>"));
            buildGUI();
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
