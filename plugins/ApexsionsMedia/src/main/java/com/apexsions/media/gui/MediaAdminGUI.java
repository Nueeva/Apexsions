package com.apexsions.media.gui;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.banner.MediaBanner;
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

public class MediaAdminGUI implements InventoryHolder {

    private final ApexsionsMediaPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final List<MediaBanner> bannerList = new ArrayList<>();

    public MediaAdminGUI(ApexsionsMediaPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#1abc9c:#16a085><bold>🖼️ APEXSIONS MEDIA ADMIN 🖼️</bold></gradient>"));
        build();
    }

    public void build() {
        inventory.clear();
        bannerList.clear();
        bannerList.addAll(plugin.getBannerManager().getAllBanners());

        ItemStack border = createFiller(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Header Info (Slot 4)
        ItemStack infoItem = new ItemStack(Material.PAINTING);
        ItemMeta im = infoItem.getItemMeta();
        if (im != null) {
            im.displayName(mm.deserialize("<gradient:#1abc9c:#f1c40f><bold>✦ DAFTAR BANNER AKTIF ✦</bold></gradient>"));
            im.lore(List.of(
                    mm.deserialize("<gray>Total Banner Terpasang: <aqua>" + bannerList.size() + "</aqua></gray>"),
                    mm.deserialize("<gray>Klik banner untuk teleport ke lokasinya.</gray>")
            ));
            infoItem.setItemMeta(im);
        }
        inventory.setItem(4, infoItem);

        // Render Banners (Slots 10-16, 19-25, 28-34, 37-43)
        int slot = 9;
        for (MediaBanner banner : bannerList) {
            if (slot > 44) break;
            inventory.setItem(slot++, createBannerCard(banner));
        }

        // Bottom Controls
        // Slot 45: Back to Admin Hub
        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta bm = backBtn.getItemMeta();
        if (bm != null) {
            bm.displayName(mm.deserialize("<gradient:#3498db:#2980b9><bold>⬅ KEMBALI KE ADMIN HUB</bold></gradient>"));
            bm.lore(List.of(mm.deserialize("<gray>Kembali ke panel Master Admin Hub (/admingui).</gray>")));
            backBtn.setItemMeta(bm);
        }
        inventory.setItem(45, backBtn);

        // Slot 49: Reload Media
        ItemStack reloadBtn = new ItemStack(Material.NETHER_STAR);
        ItemMeta rm = reloadBtn.getItemMeta();
        if (rm != null) {
            rm.displayName(mm.deserialize("<yellow><bold>⟳ Muat Ulang Banner & Config</bold></yellow>"));
            rm.lore(List.of(mm.deserialize("<gray>Sinkronisasi ulang seluruh banner dari disk.</gray>")));
            reloadBtn.setItemMeta(rm);
        }
        inventory.setItem(49, reloadBtn);

        // Slot 53: Close
        ItemStack closeBtn = new ItemStack(Material.BARRIER);
        ItemMeta cm = closeBtn.getItemMeta();
        if (cm != null) {
            cm.displayName(mm.deserialize("<red><bold>✖ Tutup</bold></red>"));
            closeBtn.setItemMeta(cm);
        }
        inventory.setItem(53, closeBtn);
    }

    private ItemStack createBannerCard(MediaBanner banner) {
        ItemStack item = new ItemStack(Material.MAP);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize("<gold><bold>" + banner.getId().toUpperCase() + "</bold></gold>"));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Ukuran: <yellow>" + banner.getWidthTiles() + "x" + banner.getHeightTiles() + " Tile</yellow></gray>"));
            lore.add(mm.deserialize("<gray>Arah: <aqua>" + banner.getFacing().name() + "</aqua></gray>"));
            lore.add(mm.deserialize("<gray>Lokasi: <white>" + banner.getWorldName() + " [" + (int) banner.getX() + ", " + (int) banner.getY() + ", " + (int) banner.getZ() + "]</white></gray>"));
            lore.add(mm.deserialize("<gray>Tautan: <aqua>" + (banner.getLinkUrl() != null ? banner.getLinkUrl() : "Tidak Ada") + "</aqua></gray>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<yellow>▶ Klik Kiri untuk Teleport ke Banner</yellow>"));
            lore.add(mm.deserialize("<red>▶ Shift + Klik Kanan untuk Hapus Banner</red>"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createFiller(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        if (slot == 45) { // Back to Hub
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            player.closeInventory();
            player.performCommand("admingui");
            return;
        }

        if (slot == 49) { // Reload
            plugin.reloadConfig();
            plugin.getBannerManager().reload();
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
            player.sendMessage(mm.deserialize("<green>✓ Seluruh banner dan konfigurasi Media berhasil dimuat ulang!</green>"));
            build();
            return;
        }

        if (slot == 53) { // Close
            player.closeInventory();
            return;
        }

        if (slot >= 9 && slot <= 44) {
            int index = slot - 9;
            if (index < bannerList.size()) {
                MediaBanner banner = bannerList.get(index);
                if (e.isShiftClick() && e.isRightClick()) {
                    plugin.getBannerManager().deleteBanner(banner.getId());
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red>✖ Banner <gold>" + banner.getId() + "</gold> berhasil dihapus!</red>"));
                    build();
                } else {
                    var world = Bukkit.getWorld(banner.getWorldName());
                    if (world != null) {
                        var loc = new org.bukkit.Location(world, banner.getX() + 0.5, banner.getY(), banner.getZ() + 0.5);
                        player.teleport(loc);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
                        player.sendMessage(mm.deserialize("<green>⚡ Berhasil teleport ke banner <gold>" + banner.getId() + "</gold>!</green>"));
                        player.closeInventory();
                    }
                }
            }
        }
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
