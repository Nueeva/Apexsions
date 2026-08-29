package com.apexsions.core.gui.admin;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.gui.warp.WarpAdminGUI;
import com.apexsions.core.player.PlayerData;
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
 * 54-Slot Interactive Sub-Menu for ApexsionsCore administrative tools.
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

        // Slot 24: Set Lobby Spawn
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

        // Slot 29: Quick Level / XP Modifier
        ItemStack xpItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta xpMeta = xpItem.getItemMeta();
        if (xpMeta != null) {
            xpMeta.displayName(mm.deserialize("<gradient:#2ecc71:#f1c40f><bold>⭐ PLAYER LEVEL & XP BOOST ⭐</bold></gradient>"));
            xpMeta.lore(List.of(
                    mm.deserialize("<gray>Beri bonus XP / Level langsung:</gray>"),
                    mm.deserialize("<dark_gray>•</dark_gray> <white>Beri +500 XP ke dirimu</white>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk menambah 500 XP ke akunmu!</yellow>")
            ));
            xpItem.setItemMeta(xpMeta);
        }
        inventory.setItem(29, xpItem);

        // Slot 31: Kingdom RTP Config
        ItemStack rtpItem = new ItemStack(Material.COMPASS);
        ItemMeta rtpMeta = rtpItem.getItemMeta();
        if (rtpMeta != null) {
            rtpMeta.displayName(mm.deserialize("<gradient:#1abc9c:#16a085><bold>🧭 KINGDOM RTP SERVICE 🧭</bold></gradient>"));
            rtpMeta.lore(List.of(
                    mm.deserialize("<gray>Sistem teleportasi acak terisolasi kerajaan:</gray>"),
                    mm.deserialize("<dark_gray>•</dark_gray> <white>Uji coba Random Teleportasi (/kingdom rtp)</white>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk uji coba RTP sekarang!</yellow>")
            ));
            rtpItem.setItemMeta(rtpMeta);
        }
        inventory.setItem(31, rtpItem);

        // Slot 33: Reload Core Config
        ItemStack reloadItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta reloadMeta = reloadItem.getItemMeta();
        if (reloadMeta != null) {
            reloadMeta.displayName(mm.deserialize("<gradient:#e74c3c:#c0392b><bold>🔄 RELOAD APEXSIONS CORE 🔄</bold></gradient>"));
            reloadMeta.lore(List.of(
                    mm.deserialize("<gray>Muat ulang konfigurasi regions & settings.</gray>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk reload ApexsionsCore!</yellow>")
            ));
            reloadItem.setItemMeta(reloadMeta);
        }
        inventory.setItem(33, reloadItem);

        // Slot 45: Back Button to Master Admin Hub
        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backBtn.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(mm.deserialize("<gradient:#e74c3c:#c0392b><bold>⬅ KEMBALI KE ADMIN HUB</bold></gradient>"));
            backMeta.lore(List.of(mm.deserialize("<gray>Kembali ke menu utama Apexsions Admin Hub.</gray>")));
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

        if (slot == 45) { // Back to Hub
            player.closeInventory();
            new MasterAdminGUI(plugin, player).open();
            return;
        }

        if (slot == 49) { // Close
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        if (slot == 20) { // Warp Admin GUI
            player.closeInventory();
            new WarpAdminGUI(plugin, player).open();
            return;
        }

        if (slot == 22) { // Kingdom War
            if (plugin.getWarManager().isWarActive()) {
                plugin.getWarManager().stopWar();
                player.sendMessage(mm.deserialize("<green>✓ Perang kerajaan berhasil dihentikan oleh admin!</green>"));
            } else {
                List<Region> list = new ArrayList<>(plugin.getRegionManager().getRegions());
                if (list.size() >= 2) {
                    plugin.getWarManager().startWar(list.get(0), list.get(1), 30);
                    player.sendMessage(mm.deserialize("<red>⚔ Perang kerajaan dideklarasikan selama 30 menit!</red>"));
                } else {
                    player.sendMessage(mm.deserialize("<red>Dibutuhkan minimal 2 kerajaan untuk memulai perang!</red>"));
                }
            }
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 1.0f);
            buildGUI();
            return;
        }

        if (slot == 24) { // Set Lobby Spawn
            player.performCommand("ac setlobby");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
            player.sendMessage(mm.deserialize("<green>✓ Titik spawn lobby berhasil disetel pada posisimu!</green>"));
            return;
        }

        if (slot == 29) { // Give 500 XP
            PlayerData pData = plugin.getPlayerDataService().getCached(player.getUniqueId()).orElse(null);
            if (pData != null) {
                pData.addXp(500);
                plugin.getPlayerRepository().save(pData);
                player.sendMessage(mm.deserialize("<green>✓ Berhasil menambahkan 500 XP! Total Level: <yellow>Lv. " + pData.getLevel() + "</yellow></green>"));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.4f);
            }
            return;
        }

        if (slot == 31) { // Kingdom RTP
            player.closeInventory();
            plugin.getKingdomRtpService().executeRtp(player);
            return;
        }

        if (slot == 33) { // Reload Core
            plugin.getConfigManager().reload();
            player.sendMessage(mm.deserialize("<green>✓ ApexsionsCore berhasil dimuat ulang!</green>"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
            buildGUI();
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
