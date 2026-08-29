package com.apexsions.core.gui.admin;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.admin.AdminModule;
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
    private final List<AdminModule> mappedModules = new ArrayList<>();

    public MasterAdminGUI(ApexsionsCorePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>⚙ APEXSIONS MASTER ADMIN HUB ⚙</bold></gradient>"));
        buildGUI();
    }

    private void buildGUI() {
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

        // Header Slot 4: Server Status
        inventory.setItem(4, createServerStatusItem());

        // Header Slot 8: Reload All Plugins Suite
        inventory.setItem(8, createReloadAllItem());

        // Place Module Cards in Center Matrix (Slots 20 to 25)
        int[] moduleSlots = {20, 21, 22, 23, 24, 25};
        List<AdminModule> allModules = plugin.getAdminHubManager().getAllModules();
        mappedModules.clear();

        for (int idx = 0; idx < moduleSlots.length; idx++) {
            int slot = moduleSlots[idx];
            if (idx < allModules.size()) {
                AdminModule module = allModules.get(idx);
                mappedModules.add(module);
                inventory.setItem(slot, createModuleCard(module));
            } else {
                inventory.setItem(slot, createGlass(Material.GRAY_STAINED_GLASS_PANE, "<gray>Slot Modul Kosong</gray>"));
            }
        }

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
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize("<gradient:#3498db:#9b59b6><bold>👑 PROFIL ADMIN</bold></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Nama:</gray> <white>" + player.getName() + "</white>"));
            lore.add(mm.deserialize("<gray>Mode:</gray> <green>Full Master Access</green>"));
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

            lore.add(mm.deserialize("<gray>Online Players:</gray> <aqua>" + Bukkit.getOnlinePlayers().size() + "</aqua> / <dark_aqua>" + Bukkit.getMaxPlayers() + "</dark_aqua>"));
            lore.add(mm.deserialize("<gray>RAM Usage:</gray> <yellow>" + usedMB + " MB</yellow> <gray>/</gray> <gold>" + maxMB + " MB</gold>"));
            lore.add(mm.deserialize("<gray>Database Pool:</gray> " + (plugin.getDatabaseManager().isUsingFallback() ? "<yellow>SQLite (Local)</yellow>" : "<green>PostgreSQL (Live)</green>")));
            lore.add(mm.deserialize("<gray>Kingdom War:</gray> " + (plugin.getWarManager().isWarActive() ? "<red><bold>SEDANG AKTIF</bold></red>" : "<green>Damai / Tenang</green>")));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<dark_gray>Paper 1.21.4 • Java 21 LTS</dark_gray>"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createReloadAllItem() {
        ItemStack item = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize("<gradient:#e74c3c:#c0392b><bold>⚡ RELOAD SELURUH SUITE</bold></gradient>"));
            meta.lore(List.of(
                    mm.deserialize("<gray>Memuat ulang seluruh konfigurasi</gray>"),
                    mm.deserialize("<gray>6 plugin suite Apexsions serentak.</gray>"),
                    Component.empty(),
                    mm.deserialize("<red>▶ Klik untuk reload semua plugin!</red>")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createModuleCard(AdminModule module) {
        boolean hasPermission = player.hasPermission(module.getPermission()) || player.isOp() || player.hasPermission("apexsions.admin");

        ItemStack item;
        if (hasPermission) {
            item = new ItemStack(module.getIcon());
        } else {
            item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (hasPermission) {
                meta.displayName(module.getDisplayName());
                meta.lore(module.getDescription(player));
                meta.setEnchantmentGlintOverride(true);
            } else {
                meta.displayName(mm.deserialize("<red><bold>🔒 " + module.getId().toUpperCase() + " (TERKUNCI)</bold></red>"));
                meta.lore(List.of(
                        mm.deserialize("<red>Akses ditolak! Membutuhkan hak akses:</red>"),
                        mm.deserialize("<dark_red>• <yellow>" + module.getPermission() + "</yellow></dark_red>"),
                        Component.empty(),
                        mm.deserialize("<gray>Hubungi Administrator untuk wewenang ini.</gray>")
                ));
            }
            item.setItemMeta(meta);
        }
        return item;
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

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) { // Close
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        if (slot == 8) { // Reload All Suite
            if (!player.hasPermission("apexsions.admin") && !player.isOp()) {
                player.sendMessage(mm.deserialize("<red>Kamu tidak memiliki izin untuk reload seluruh suite!</red>"));
                return;
            }
            player.sendMessage(mm.deserialize("<yellow>⏳ Memuat ulang seluruh konfigurasi plugin Apexsions Suite...</yellow>"));
            plugin.getConfigManager().reload();
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsChat")) player.performCommand("apexsionschat reload");
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsEconomy")) player.performCommand("ecoadmin reload");
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsBattlepass")) player.performCommand("abp reload");
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsShop")) player.performCommand("shop reload");
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsMedia")) player.performCommand("media reload");

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
            player.sendMessage(mm.deserialize("<green>✓ Seluruh 6 plugin suite Apexsions berhasil dimuat ulang!</green>"));
            buildGUI(); // Refresh dashboard
            return;
        }

        // Module click
        int[] moduleSlots = {20, 21, 22, 23, 24, 25};
        for (int idx = 0; idx < moduleSlots.length; idx++) {
            if (slot == moduleSlots[idx] && idx < mappedModules.size()) {
                AdminModule module = mappedModules.get(idx);
                if (player.hasPermission(module.getPermission()) || player.isOp() || player.hasPermission("apexsions.admin")) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                    module.open(player);
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
                    player.sendMessage(mm.deserialize("<red>🔒 Kamu tidak memiliki hak akses <yellow>" + module.getPermission() + "</yellow> untuk modul ini!</red>"));
                }
                return;
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
