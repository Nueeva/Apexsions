package com.apexsions.core.gui.admin;

import com.apexsions.core.ApexsionsCorePlugin;
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
 * 54-Slot Interactive Sub-Menu for ApexsionsShop administration and market manipulation.
 */
public class ShopAdminSubGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ShopAdminSubGUI(ApexsionsCorePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e67e22:#d35400><bold>🛒 APEXSIONS SHOP & MARKET 🛒</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();

        ItemStack border = createGlass(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        ItemStack decor = createGlass(Material.YELLOW_STAINED_GLASS_PANE, "<yellow>✦</yellow>");

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }
        inventory.setItem(1, decor);
        inventory.setItem(7, decor);
        inventory.setItem(46, decor);
        inventory.setItem(52, decor);

        // Header Slot 4: Overview
        ItemStack header = new ItemStack(Material.EMERALD);
        ItemMeta hMeta = header.getItemMeta();
        if (hMeta != null) {
            hMeta.displayName(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>🛒 SISTEM PASAR DINAMIS APEXSIONS 🛒</bold></gradient>"));
            hMeta.lore(List.of(
                    mm.deserialize("<gray>Plugin:</gray> <gold>ApexsionsShop v1.0.0</gold>"),
                    mm.deserialize("<gray>Mekanisme:</gray> <yellow>Dynamic Pricing + Regional Kingdom Multiplier</yellow>"),
                    mm.deserialize("<gray>Pajak Wilayah:</gray> <aqua>10% Regional Tariff Active</aqua>"),
                    Component.empty(),
                    mm.deserialize("<yellow>Pilih opsi di bawah untuk mengatur dinamika pasar.</yellow>")
            ));
            header.setItemMeta(hMeta);
        }
        inventory.setItem(4, header);

        // Slot 20: Open Kingdom Shop GUI (/shop)
        inventory.setItem(20, createActionItem(Material.CHEST, "<gradient:#f1c40f:#e67e22><bold>🛍 BUKA TOKO UTAMA (/SHOP)</bold></gradient>",
                List.of("<gray>Inspeksi katalog toko, kategori & harga dinamis.</gray>", "<yellow>▶ Klik untuk membuka /shop</yellow>")));

        // Slot 21: Restricted Mob Drops Status
        inventory.setItem(21, createActionItem(Material.SHULKER_SHELL, "<light_purple><bold>⛔ STATUS MOB DROPS LANGKA</bold></light_purple>",
                List.of("<gray>Status: <yellow>Sell-Only (Pembelian Ditutup)</yellow></gray>",
                        "<gray>Item:</gray> <white>Ender Pearl, Blaze Rod, Ghast Tear, Shulker Shell</white>",
                        "",
                        "<green>● Proteksi Ekonomi Berjalan Normal</green>")));

        // Slot 22: Trigger Market Boom (+50% sell price)
        inventory.setItem(22, createActionItem(Material.GOLD_BLOCK, "<gradient:#2ecc71:#f1c40f><bold>📈 PICU MARKET BOOM (+50%)</bold></gradient>",
                List.of("<gray>Picu tren pasar melonjak drastis:</gray>",
                        "<dark_gray>•</dark_gray> <green>Harga jual komoditas naik +50%</green>",
                        "",
                        "<yellow>▶ Klik untuk mengumumkan peristiwa pasar!</yellow>")));

        // Slot 23: Trigger Market Dip (-30% price)
        inventory.setItem(23, createActionItem(Material.COAL_BLOCK, "<gradient:#e74c3c:#c0392b><bold>📉 PICU MARKET RESESI (-30%)</bold></gradient>",
                List.of("<gray>Simulasikan penurunan daya beli pasar komoditas.</gray>",
                        "",
                        "<yellow>▶ Klik untuk mengumumkan peristiwa pasar!</yellow>")));

        // Slot 24: Reset Dynamic Prices
        inventory.setItem(24, createActionItem(Material.WATER_BUCKET, "<aqua><bold>🔄 RESET HARGA DINAMIS</bold></aqua>",
                List.of("<gray>Kembalikan harga pasar ke nilai awal (base price).</gray>", "<yellow>▶ Klik untuk reset fluktuasi pasar</yellow>")));

        // Slot 31: Reload Shop Config
        inventory.setItem(31, createActionItem(Material.REDSTONE_BLOCK, "<gradient:#e74c3c:#c0392b><bold>🔄 RELOAD APEXSIONS SHOP</bold></gradient>",
                List.of("<gray>Muat ulang seluruh item, kategori & pasar.</gray>", "<yellow>▶ Klik untuk reload</yellow>")));

        // Slot 49: Back Button
        ItemStack backBtn = createActionItem(Material.OAK_DOOR, "<red><bold>◀ KEMBALI KE MASTER ADMIN HUB</bold></red>",
                List.of("<gray>Kembali ke menu utama panel administrasi.</gray>"));
        inventory.setItem(49, backBtn);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            new MasterAdminGUI(plugin, player).open();
            return;
        }

        if (slot == 20) { // Open Shop
            player.closeInventory();
            player.performCommand("shop");
            return;
        }

        if (slot == 22) { // Market Boom
            Bukkit.broadcast(mm.deserialize("<gradient:#f1c40f:#2ecc71><bold>📈 PERISTIWA PASAR: MARKET BOOM! 📈</bold></gradient>\n<green>Permintaan pasar kerajaan melonjak tinggi! Seluruh harga komoditas naik +50% selama 1 jam ke depan!</green>"));
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);
            }
            return;
        }

        if (slot == 23) { // Market Dip
            Bukkit.broadcast(mm.deserialize("<gradient:#e74c3c:#c0392b><bold>📉 PERISTIWA PASAR: RESESI SEMENTARA 📉</bold></gradient>\n<yellow>Pasar mengalami surplus pasokan komoditas! Harga beli & jual turun sementara waktu.</yellow>"));
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 1.5f);
            }
            return;
        }

        if (slot == 24) { // Reset Dynamic Prices
            player.sendMessage(mm.deserialize("<green>✓ Seluruh nilai pergeseran harga dinamis berhasil di-reset ke nilai default!</green>"));
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
            return;
        }

        if (slot == 31) { // Reload
            player.performCommand("shop reload");
            player.sendMessage(mm.deserialize("<green>✓ ApexsionsShop berhasil dimuat ulang!</green>"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
        }
    }

    private ItemStack createActionItem(Material mat, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            List<Component> components = new ArrayList<>();
            for (String l : loreLines) {
                components.add(mm.deserialize(l));
            }
            meta.lore(components);
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

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
