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
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;

/**
 * 54-Slot Centralized Master Admin Dashboard connecting all Apexsions plugins.
 */
public class MasterAdminGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public MasterAdminGUI(ApexsionsCorePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>⚙ APEXSIONS MASTER ADMIN HUB ⚙</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
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

        // Header Slot 0: Admin Profile
        inventory.setItem(0, createAdminProfileItem());

        // Header Slot 4: Server Status & Telemetry
        inventory.setItem(4, createServerStatusItem());

        // Header Slot 8: Reload All Plugins Suite
        inventory.setItem(8, createReloadAllItem());

        // ════════════════ 7 CORE MODULES MATRIX (Slots 19 to 25) ════════════════
        // 1. Slot 19: ApexsionsCore
        inventory.setItem(19, createModuleItem(Material.NETHER_STAR,
                "<gradient:#f1c40f:#e67e22><bold>👑 APEXSIONS CORE</bold></gradient>",
                List.of(
                        "<gray>Kontrol sentral kerajaan & navigasi:</gray>",
                        "<dark_gray>•</dark_gray> <yellow>Warp Manager & Penobatan Raja</yellow>",
                        "<dark_gray>•</dark_gray> <yellow>Kingdom War & Teritori</yellow>",
                        "<dark_gray>•</dark_gray> <yellow>Spawn Lobby & Realm Spawns</yellow>",
                        "<dark_gray>•</dark_gray> <yellow>Kingdom RTP System</yellow>",
                        "",
                        "<yellow>▶ Klik untuk buka Panel Core!</yellow>"
                )));

        // 2. Slot 20: Player Management & Inspector
        inventory.setItem(20, createModuleItem(Material.PLAYER_HEAD,
                "<gradient:#3498db:#9b59b6><bold>👤 PLAYER MANAGER & INSPECTOR</bold></gradient>",
                List.of(
                        "<gray>Akses penuh data pemain & moderasi:</gray>",
                        "<dark_gray>•</dark_gray> <aqua>Daftar Pemain Online & Offline</aqua>",
                        "<dark_gray>•</dark_gray> <aqua>Edit Saldo Rupiah & Diamond 💎 Langsung</aqua>",
                        "<dark_gray>•</dark_gray> <aqua>Edit Level Karakter & XP Progresi</aqua>",
                        "<dark_gray>•</dark_gray> <aqua>Ganti Kerajaan & Sanksi (Mute/Kick/Ban)</aqua>",
                        "<dark_gray>•</dark_gray> <aqua>Teleport & Live Inventory Viewer</aqua>",
                        "",
                        "<yellow>▶ Klik untuk buka Player Manager!</yellow>"
                )));

        // 3. Slot 21: ApexsionsChat
        inventory.setItem(21, createModuleItem(Material.WRITABLE_BOOK,
                "<gradient:#3498db:#2980b9><bold>💬 APEXSIONS CHAT</bold></gradient>",
                List.of(
                        "<gray>Manajemen obrolan & moderasi staf:</gray>",
                        "<dark_gray>•</dark_gray> <aqua>Staff Reports Inbox (/reports)</aqua>",
                        "<dark_gray>•</dark_gray> <aqua>Global Mute & Clear Chat</aqua>",
                        "<dark_gray>•</dark_gray> <aqua>Server Broadcast Announcement</aqua>",
                        "<dark_gray>•</dark_gray> <aqua>Trigger Chat Mini-Game</aqua>",
                        "",
                        "<yellow>▶ Klik untuk buka Panel Chat!</yellow>"
                )));

        // 4. Slot 22: ApexsionsEconomy
        inventory.setItem(22, createModuleItem(Material.EMERALD,
                "<gradient:#2ecc71:#27ae60><bold>💰 APEXSIONS ECONOMY</bold></gradient>",
                List.of(
                        "<gray>Multi-Currency, Bank & Lelang:</gray>",
                        "<dark_gray>•</dark_gray> <green>Transfer Saldo Rupiah & Diamond 💎</green>",
                        "<dark_gray>•</dark_gray> <green>Pasar Lelang Global (/ah)</green>",
                        "<dark_gray>•</dark_gray> <green>Bersihkan Lelang Expired</green>",
                        "<dark_gray>•</dark_gray> <green>Top Saldo Terkaya Server</green>",
                        "",
                        "<yellow>▶ Klik untuk buka Panel Ekonomi!</yellow>"
                )));

        // 5. Slot 23: ApexsionsBattlepass
        inventory.setItem(23, createModuleItem(Material.ENCHANTED_BOOK,
                "<gradient:#9b59b6:#3498db><bold>📜 APEXSIONS BATTLEPASS</bold></gradient>",
                List.of(
                        "<gray>Progresi musim & quest harian/mingguan:</gray>",
                        "<dark_gray>•</dark_gray> <light_purple>Berikan Premium Pass ke Pemain</light_purple>",
                        "<dark_gray>•</dark_gray> <light_purple>Reset Quest Harian / Mingguan</light_purple>",
                        "<dark_gray>•</dark_gray> <light_purple>Force Rotasi Toko BattlePass</light_purple>",
                        "<dark_gray>•</dark_gray> <light_purple>Set Tier BP Pemain</light_purple>",
                        "",
                        "<yellow>▶ Klik untuk buka Panel BattlePass!</yellow>"
                )));

        // 6. Slot 24: ApexsionsShop
        inventory.setItem(24, createModuleItem(Material.CHEST,
                "<gradient:#e67e22:#d35400><bold>🛒 APEXSIONS SHOP</bold></gradient>",
                List.of(
                        "<gray>Pasar dinamis & komoditas kerajaan:</gray>",
                        "<dark_gray>•</dark_gray> <gold>Inspeksi Toko Utama (/shop)</gold>",
                        "<dark_gray>•</dark_gray> <gold>Status Mob Drops Langka (Sell-Only)</gold>",
                        "<dark_gray>•</dark_gray> <gold>Picu Market Boom / Resesi</gold>",
                        "<dark_gray>•</dark_gray> <gold>Reset Fluktuasi Harga Dinamis</gold>",
                        "",
                        "<yellow>▶ Klik untuk buka Panel Shop!</yellow>"
                )));

        // 7. Slot 25: ApexsionsMedia
        inventory.setItem(25, createModuleItem(Material.PAINTING,
                "<gradient:#e67e22:#f39c12><bold>🖼 APEXSIONS MEDIA</bold></gradient>",
                List.of(
                        "<gray>Display visual banner & holografi:</gray>",
                        "<dark_gray>•</dark_gray> <yellow>Buat Banner Interaktif Baru</yellow>",
                        "<dark_gray>•</dark_gray> <yellow>Daftar Banner Aktif</yellow>",
                        "<dark_gray>•</dark_gray> <yellow>Atur Aksi Tautan Klik URL</yellow>",
                        "<dark_gray>•</dark_gray> <yellow>Toggle Raytrace Hover Glow</yellow>",
                        "",
                        "<yellow>▶ Klik untuk buka Panel Media!</yellow>"
                )));

        // 8. Slot 31: ApexsionsCustomEnchants (/ace)
        inventory.setItem(31, createModuleItem(Material.ENCHANTED_BOOK,
                "<gradient:#9b59b6:#e74c3c><bold>⚡ APEXSIONS CUSTOM ENCHANTS</bold></gradient>",
                List.of(
                        "<gray>Kontrol sihir, tier pricing & custom items:</gray>",
                        "<dark_gray>•</dark_gray> <yellow>Katalog Enchants (/ace enchants)</yellow>",
                        "<dark_gray>•</dark_gray> <yellow>Admin Item Creator (/ace create)</yellow>",
                        "<dark_gray>•</dark_gray> <yellow>Atur Harga Tier (Rupiah/Diamond 💎)</yellow>",
                        "",
                        "<yellow>▶ Klik untuk buka Panel Custom Enchants!</yellow>"
                )));

        // Bottom Row Slot 49: Close
        ItemStack closeBtn = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeBtn.getItemMeta();
        if (closeMeta != null) {
            closeMeta.displayName(mm.deserialize("<red><bold>✖ TUTUP DASHBOARD</bold></red>"));
            closeMeta.lore(List.of(mm.deserialize("<gray>Keluar dari panel administrasi.</gray>")));
            closeBtn.setItemMeta(closeMeta);
        }
        inventory.setItem(49, closeBtn);
    }

    private ItemStack createAdminProfileItem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.displayName(mm.deserialize("<gradient:#3498db:#9b59b6><bold>👑 PROFIL ADMINISTRATOR</bold></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Nama:</gray> <white>" + player.getName() + "</white>"));
            lore.add(mm.deserialize("<gray>Mode:</gray> <green>Full Master Access (OP)</green>"));
            lore.add(mm.deserialize("<gray>Level:</gray> <gold>" + plugin.getPlayerDataService().getCached(player.getUniqueId()).map(d -> String.valueOf(d.getLevel())).orElse("1") + "</gold>"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createServerStatusItem() {
        ItemStack item = new ItemStack(Material.BEACON);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize("<gradient:#2ecc71:#f1c40f><bold>✦ STATUS SERVER APEXSIONS ✦</bold></gradient>"));
            List<Component> lore = new ArrayList<>();

            // Memory Info
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heap = memoryBean.getHeapMemoryUsage();
            long usedMB = heap.getUsed() / (1024 * 1024);
            long maxMB = heap.getMax() / (1024 * 1024);

            lore.add(mm.deserialize("<gray>Memory RAM:</gray> <yellow>" + usedMB + " MB</yellow> <dark_gray>/</dark_gray> <gold>" + maxMB + " MB</gold>"));
            lore.add(mm.deserialize("<gray>Pemain Online:</gray> <green><bold>" + Bukkit.getOnlinePlayers().size() + "</bold></green>"));
            lore.add(mm.deserialize("<gray>Status War:</gray> " + (plugin.getWarManager().isWarActive() ? "<red><bold>⚔ PERANG AKTIF</bold></red>" : "<green>Damai</green>")));
            lore.add(mm.deserialize("<gray>Server Suite:</gray> <green>● 6/6 Plugin Terintegrasi</green>"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createReloadAllItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize("<gradient:#e74c3c:#f39c12><bold>⚡ RELOAD ALL PLUGINS ⚡</bold></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Muat ulang seluruh plugin ekosistem:</gray>"));
            lore.add(mm.deserialize("<dark_gray>•</dark_gray> <white>ApexsionsCore, Chat, Economy</white>"));
            lore.add(mm.deserialize("<dark_gray>•</dark_gray> <white>ApexsionsBattlepass, Shop, Media</white>"));
            lore.add(mm.deserialize(""));
            lore.add(mm.deserialize("<yellow>▶ Klik untuk reload semua konfigurasi!</yellow>"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createModuleItem(Material mat, String name, List<String> loreLines) {
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

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        if (slot == 8) { // Reload All
            player.closeInventory();
            player.performCommand("ac reload");
            player.performCommand("chat reload");
            player.performCommand("eco reload");
            player.performCommand("abp reload");
            player.performCommand("shop reload");
            player.performCommand("media reload");
            player.sendMessage(mm.deserialize("<gradient:#2ecc71:#f1c40f><bold>═════════════════════════════════════════════════</bold></gradient>"));
            player.sendMessage(mm.deserialize("<green><bold>✓ SELURUH PLUGIN APEXSIONS SUITE BERHASIL DIMUAT ULANG!</bold></green>"));
            player.sendMessage(mm.deserialize("<gray>ApexsionsCore, Chat, Economy, Battlepass, Shop, Media</gray>"));
            player.sendMessage(mm.deserialize("<gradient:#2ecc71:#f1c40f><bold>═════════════════════════════════════════════════</bold></gradient>"));
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            return;
        }

        if (slot == 19) { // Core
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new CoreAdminSubGUI(plugin, player).open();
            return;
        }

        if (slot == 20) { // Player Manager
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new PlayerManagerGUI(plugin, player).open();
            return;
        }

        if (slot == 21) { // Chat
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new ChatAdminSubGUI(plugin, player).open();
            return;
        }

        if (slot == 22) { // Economy
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new EconomyAdminSubGUI(plugin, player).open();
            return;
        }

        if (slot == 23) { // Battlepass
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new BattlePassAdminSubGUI(plugin, player).open();
            return;
        }

        if (slot == 24) { // Shop
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new ShopAdminSubGUI(plugin, player).open();
            return;
        }

        if (slot == 25) { // Media
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new MediaAdminSubGUI(plugin, player).open();
            return;
        }

        if (slot == 31) { // ApexsionsCustomEnchants (/ace)
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            player.closeInventory();
            player.performCommand("ace");
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
