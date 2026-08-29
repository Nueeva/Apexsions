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

    private void buildGUI() {
        ItemStack border = createGlass(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Slot 20: Open Kingdom Shop GUI (/shop)
        ItemStack shopItem = new ItemStack(Material.CHEST);
        ItemMeta shopMeta = shopItem.getItemMeta();
        if (shopMeta != null) {
            shopMeta.displayName(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>🛍 BUKA TOKO UTAMA (/SHOP) 🛍</bold></gradient>"));
            shopMeta.lore(List.of(
                    mm.deserialize("<gray>Inspeksi katalog toko, kategori & harga dinamis.</gray>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk membuka /shop!</yellow>")
            ));
            shopItem.setItemMeta(shopMeta);
        }
        inventory.setItem(20, shopItem);

        // Slot 22: Trigger Market Boom (+50% sell price)
        ItemStack boomItem = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta boomMeta = boomItem.getItemMeta();
        if (boomMeta != null) {
            boomMeta.displayName(mm.deserialize("<gradient:#2ecc71:#f1c40f><bold>📈 PICU MARKET BOOM (+50%) 📈</bold></gradient>"));
            boomMeta.lore(List.of(
                    mm.deserialize("<gray>Picu tren pasar melonjak drastis:</gray>"),
                    mm.deserialize("<dark_gray>•</dark_gray> <green>Harga jual komoditas naik +50%</green>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk mengumumkan tren ekonomi!</yellow>")
            ));
            boomItem.setItemMeta(boomMeta);
        }
        inventory.setItem(22, boomItem);

        // Slot 24: Trigger Market Dip (-30% price)
        ItemStack dipItem = new ItemStack(Material.COAL_BLOCK);
        ItemMeta dipMeta = dipItem.getItemMeta();
        if (dipMeta != null) {
            dipMeta.displayName(mm.deserialize("<gradient:#e74c3c:#c0392b><bold>📉 PICU MARKET DIP / RESESI (-30%) 📉</bold></gradient>"));
            dipMeta.lore(List.of(
                    mm.deserialize("<gray>Simulasikan penurunan daya beli pasar komoditas.</gray>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk mengumumkan peristiwa pasar!</yellow>")
            ));
            dipItem.setItemMeta(dipMeta);
        }
        inventory.setItem(24, dipItem);

        // Slot 31: Reload Shop Config
        ItemStack reloadItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta relMeta = reloadItem.getItemMeta();
        if (relMeta != null) {
            relMeta.displayName(mm.deserialize("<gradient:#e74c3c:#c0392b><bold>🔄 RELOAD APEXSIONS SHOP 🔄</bold></gradient>"));
            relMeta.lore(List.of(
                    mm.deserialize("<gray>Muat ulang seluruh item, kategori & pasar.</gray>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk reload!</yellow>")
            ));
            reloadItem.setItemMeta(relMeta);
        }
        inventory.setItem(31, reloadItem);

        // Slot 45: Back Button
        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backBtn.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(mm.deserialize("<gradient:#e74c3c:#c0392b><bold>⬅ KEMBALI KE ADMIN HUB</bold></gradient>"));
            backBtn.setItemMeta(backMeta);
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
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 45) {
            player.closeInventory();
            new MasterAdminGUI(plugin, player).open();
            return;
        }

        if (slot == 49) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        if (slot == 20) { // /shop
            player.closeInventory();
            player.performCommand("shop");
            return;
        }

        if (slot == 22) { // Market boom
            Bukkit.broadcast(mm.deserialize("<gradient:#2ecc71:#f1c40f><bold>📈 PASAR GLOBAL SEDANG BOOM!</bold></gradient> <yellow>Harga jual komoditas di seluruh kerajaan melonjak tinggi!</yellow>"));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
            return;
        }

        if (slot == 24) { // Market dip
            Bukkit.broadcast(mm.deserialize("<gradient:#e74c3c:#c0392b><bold>📉 PENURUNAN PASAR KOMODITAS!</bold></gradient> <gray>Stok berlebih menyebabkan harga jual mengalami penurunan sementara.</gray>"));
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.4f);
            return;
        }

        if (slot == 31) { // Reload
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsShop")) {
                player.performCommand("shop reload");
            }
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
            player.sendMessage(mm.deserialize("<green>✓ ApexsionsShop berhasil dimuat ulang!</green>"));
        }
    }

    private ItemStack createGlass(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void open() {
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.2f);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
