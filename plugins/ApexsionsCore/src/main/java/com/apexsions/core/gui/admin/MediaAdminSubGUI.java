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
 * 54-Slot Sub-GUI for ApexsionsMedia Administration.
 */
public class MediaAdminSubGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player admin;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public MediaAdminSubGUI(ApexsionsCorePlugin plugin, Player admin) {
        this.plugin = plugin;
        this.admin = admin;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e67e22:#f39c12><bold>🖼 MEDIA & BANNER DASHBOARD 🖼</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        admin.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();

        ItemStack borderPane = createGlass(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        ItemStack decorPane = createGlass(Material.ORANGE_STAINED_GLASS_PANE, "<gold>✦</gold>");

        // Fill border
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderPane);
            }
        }
        inventory.setItem(1, decorPane);
        inventory.setItem(7, decorPane);
        inventory.setItem(46, decorPane);
        inventory.setItem(52, decorPane);

        // Header Slot 4: Media Info
        ItemStack header = new ItemStack(Material.PAINTING);
        ItemMeta hMeta = header.getItemMeta();
        if (hMeta != null) {
            hMeta.displayName(mm.deserialize("<gradient:#e67e22:#f1c40f><bold>👑 SISTEM BANNER & MEDIA APEXSIONS 👑</bold></gradient>"));
            hMeta.lore(List.of(
                    mm.deserialize("<gray>Plugin:</gray> <gold>ApexsionsMedia v1.0.0</gold>"),
                    mm.deserialize("<gray>Raytrace Engine:</gray> <green>● Real-time Glow Active</green>"),
                    mm.deserialize("<gray>Fungsi:</gray> <yellow>Holographic Map Banners & URL Links</yellow>"),
                    Component.empty(),
                    mm.deserialize("<yellow>Pilih opsi di bawah untuk mengelola media display.</yellow>")
            ));
            header.setItemMeta(hMeta);
        }
        inventory.setItem(4, header);

        // Actions (Slots 20, 21, 22, 23, 31)
        inventory.setItem(20, createActionItem(Material.ITEM_FRAME, "<gold><bold>🖼 BUAT BANNER INTERAKTIF</bold></gold>",
                List.of("<gray>Buat display banner baru pada lokasimu saat ini.</gray>", "<yellow>▶ Klik untuk input nama banner di chat</yellow>")));

        inventory.setItem(21, createActionItem(Material.GLOW_ITEM_FRAME, "<aqua><bold>📜 DAFTAR BANNER AKTIF</bold></aqua>",
                List.of("<gray>Tampilkan daftar banner aktif yang terpasang di server.</gray>", "<yellow>▶ Klik untuk lihat daftar di chat</yellow>")));

        inventory.setItem(22, createActionItem(Material.COMPASS, "<light_purple><bold>🔗 ATUR AKSI KLIK URL BANNER</bold></light_purple>",
                List.of("<gray>Setel tautan web / Discord / Store saat banner diklik.</gray>", "<yellow>▶ Klik untuk atur URL di chat</yellow>")));

        inventory.setItem(23, createActionItem(Material.GLOWSTONE_DUST, "<yellow><bold>✨ TOGGLE RAYTRACE GLOW</bold></yellow>",
                List.of("<gray>Aktifkan/nonaktifkan efek hover glow saat disorot.</gray>", "<yellow>▶ Klik untuk toggle efek</yellow>")));

        inventory.setItem(31, createActionItem(Material.REDSTONE_BLOCK, "<red><bold>⚡ RELOAD APEXSIONS MEDIA</bold></red>",
                List.of("<gray>Muat ulang gambar, map cache, dan konfigurasi media.</gray>", "<yellow>▶ Klik untuk reload</yellow>")));

        // Bottom Slot 49: Back to Master Hub
        ItemStack backHub = createActionItem(Material.OAK_DOOR, "<red><bold>◀ KEMBALI KE MASTER ADMIN HUB</bold></red>",
                List.of("<gray>Kembali ke menu utama panel administrasi.</gray>"));
        inventory.setItem(49, backHub);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 20) { // Create Banner
            plugin.getAdminChatInputManager().startSession(admin,
                    "Ketik nama file image untuk banner baru (contoh: logo.png):",
                    imageFile -> {
                        admin.performCommand("media create " + imageFile);
                        admin.sendMessage(mm.deserialize("<green>✓ Perintah Media Create dieksekusi untuk <yellow>" + imageFile + "</yellow>!</green>"));
                        open();
                    },
                    this::open
            );
            return;
        }

        if (slot == 21) { // List Banners
            admin.closeInventory();
            admin.performCommand("media list");
            return;
        }

        if (slot == 22) { // Set URL
            plugin.getAdminChatInputManager().startSession(admin,
                    "Ketik ID Banner dan URL Link (format: <id> <url>):",
                    input -> {
                        String[] parts = input.split(" ");
                        if (parts.length >= 2) {
                            admin.performCommand("media seturl " + parts[0] + " " + parts[1]);
                            admin.sendMessage(mm.deserialize("<green>✓ URL untuk Banner " + parts[0] + " disetel ke <yellow>" + parts[1] + "</yellow>!</green>"));
                        } else {
                            admin.sendMessage(mm.deserialize("<red>Format salah! Gunakan: <id> <url></red>"));
                        }
                        open();
                    },
                    this::open
            );
            return;
        }

        if (slot == 23) { // Toggle Glow
            admin.performCommand("media toggleglow");
            admin.sendMessage(mm.deserialize("<green>✓ Status Raytrace Glow berhasil di-toggle!</green>"));
            admin.playSound(admin.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
            return;
        }

        if (slot == 31) { // Reload
            admin.performCommand("media reload");
            admin.sendMessage(mm.deserialize("<green>✓ ApexsionsMedia berhasil dimuat ulang!</green>"));
            admin.playSound(admin.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
            return;
        }

        if (slot == 49) {
            admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            new MasterAdminGUI(plugin, admin).open();
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
