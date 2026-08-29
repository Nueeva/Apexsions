package com.apexsions.core.gui.admin;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.gui.warp.WarpAdminGUI;
import com.apexsions.core.region.Region;
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
 * 54-Slot Sub-Menu dedicated to ApexsionsCore administrative tools.
 */
public class CoreAdminSubGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public CoreAdminSubGUI(ApexsionsCorePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#f1c40f:#e67e22><bold>👑 APEXSIONS CORE MANAGEMENT 👑</bold></gradient>"));
        buildGUI();
    }

    private void buildGUI() {
        ItemStack border = createGlass(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Slot 20: Warp Management GUI
        ItemStack warpItem = new ItemStack(Material.ENDER_PEARL);
        ItemMeta warpMeta = warpItem.getItemMeta();
        if (warpMeta != null) {
            warpMeta.displayName(mm.deserialize("<gradient:#3498db:#2ecc71><bold>✦ WARP MANAGER ✦</bold></gradient>"));
            warpMeta.lore(List.of(
                    mm.deserialize("<gray>Kelola seluruh titik warp server:</gray>"),
                    mm.deserialize("<dark_gray>•</dark_gray> <white>Tambah, Edit Lokasi, Ubah Ikon</white>"),
                    mm.deserialize("<dark_gray>•</dark_gray> <white>Atur Delay, Kategori & Izin</white>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk membuka Warp Admin GUI!</yellow>")
            ));
            warpItem.setItemMeta(warpMeta);
        }
        inventory.setItem(20, warpItem);

        // Slot 22: Kingdom War Controls
        boolean isWar = plugin.getWarManager().isWarActive();
        ItemStack warItem = new ItemStack(isWar ? Material.NETHERITE_SWORD : Material.IRON_SWORD);
        ItemMeta warMeta = warItem.getItemMeta();
        if (warMeta != null) {
            warMeta.displayName(mm.deserialize("<gradient:#e74c3c:#c0392b><bold>⚔ KINGDOM WAR CONTROLS ⚔</bold></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Status War:</gray> " + (isWar ? "<red><bold>SEDANG AKTIF</bold></red>" : "<green>Tidak Ada Perang</green>")));
            lore.add(Component.empty());
            if (isWar) {
                lore.add(mm.deserialize("<red>▶ Klik untuk Menghentikan Perang Aktif (/ac war stop)!</red>"));
            } else {
                lore.add(mm.deserialize("<yellow>▶ Klik untuk Memulai Perang Zenithar vs Solterra (30m)!</yellow>"));
            }
            warMeta.lore(lore);
            warItem.setItemMeta(warMeta);
        }
        inventory.setItem(22, warItem);

        // Slot 24: Set Lobby Spawn (Multiverse-ready)
        ItemStack lobbyItem = new ItemStack(Material.BEACON);
        ItemMeta lobbyMeta = lobbyItem.getItemMeta();
        if (lobbyMeta != null) {
            lobbyMeta.displayName(mm.deserialize("<gradient:#9b59b6:#8e44ad><bold>🏛 ATUR TITIK LOBBY SPAWN 🏛</bold></gradient>"));
            lobbyMeta.lore(List.of(
                    mm.deserialize("<gray>Menetapkan koordinat & world berdiri</gray>"),
                    mm.deserialize("<gray>sebagai titik spawn utama server (/lobby).</gray>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk set spawn lobby di posisi ini!</yellow>")
            ));
            lobbyItem.setItemMeta(lobbyMeta);
        }
        inventory.setItem(24, lobbyItem);

        // Slot 45: Back Button to Master Admin Hub
        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backBtn.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(mm.deserialize("<gradient:#3498db:#2980b9><bold>⬅ KEMBALI KE ADMIN HUB</bold></gradient>"));
            backMeta.lore(List.of(mm.deserialize("<gray>Kembali ke menu dashboard utama /admingui.</gray>")));
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

        if (slot == 45) { // Back to Hub
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            plugin.getAdminHubManager().openHub(player);
            return;
        }

        if (slot == 49) { // Close
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        if (slot == 20) { // Warp Manager
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new WarpAdminGUI(plugin, player).open();
            return;
        }

        if (slot == 22) { // War Controls
            if (plugin.getWarManager().isWarActive()) {
                plugin.getWarManager().stopWar();
                player.sendMessage(mm.deserialize("<green>✓ Perang kerajaan berhasil dihentikan!</green>"));
            } else {
                var zOpt = plugin.getRegionManager().getRegion("ZENITHAR");
                var sOpt = plugin.getRegionManager().getRegion("SOLTERRA");
                if (zOpt.isPresent() && sOpt.isPresent()) {
                    plugin.getWarManager().startWar(zOpt.get(), sOpt.get(), 30);
                    player.sendMessage(mm.deserialize("<red>⚔ Perang resmi dimulai antara Zenithar dan Solterra (30 menit)!</red>"));
                } else {
                    player.sendMessage(mm.deserialize("<red>Region Zenithar atau Solterra tidak ditemukan di database.</red>"));
                }
            }
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
            buildGUI(); // Refresh
            return;
        }

        if (slot == 24) { // Set Lobby Spawn
            player.performCommand("ac setlobby");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
            return;
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
