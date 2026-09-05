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

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();

        ItemStack border = createGlass(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        ItemStack decor = createGlass(Material.ORANGE_STAINED_GLASS_PANE, "<gold>✦</gold>");

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
        ItemStack header = new ItemStack(Material.NETHER_STAR);
        ItemMeta hMeta = header.getItemMeta();
        if (hMeta != null) {
            hMeta.displayName(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>👑 KONTROL SENTRAL APEXSIONS CORE 👑</bold></gradient>"));
            hMeta.lore(List.of(
                    mm.deserialize("<gray>Plugin:</gray> <gold>ApexsionsCore v1.0.0</gold>"),
                    mm.deserialize("<gray>Status:</gray> <green>● Running (Paper 1.21.4)</green>"),
                    mm.deserialize("<gray>Total Kerajaan:</gray> <yellow>" + plugin.getRegionManager().getRegions().size() + " Kerajaan</yellow>"),
                    Component.empty(),
                    mm.deserialize("<yellow>Pilih opsi di bawah untuk kontrol kerajaan & navigasi.</yellow>")
            ));
            header.setItemMeta(hMeta);
        }
        inventory.setItem(4, header);

        // Slot 19: Kits Management GUI
        inventory.setItem(19, createActionItem(Material.CHEST_MINECART, "<gradient:#f1c40f:#e67e22><bold>📦 KELOLA KITS KERJAAN 📦</bold></gradient>",
                List.of("<gray>Kelola kit kerajaan, preview, & validasi armor set:</gray>",
                        "<dark_gray>•</dark_gray> <yellow>Buka Menu Kits (/kits)</yellow>",
                        "<dark_gray>•</dark_gray> <yellow>Builder Kit Baru (Maks 1 Full Set Armor)</yellow>",
                        "<dark_gray>•</dark_gray> <yellow>Atur Armor Set Bonus Persentase Stat</yellow>",
                        "",
                        "<yellow>▶ Klik untuk buka Panel Kits!</yellow>")));

        // Slot 20: Warp Management GUI
        inventory.setItem(20, createActionItem(Material.ENDER_PEARL, "<gradient:#3498db:#2ecc71><bold>✦ WARP MANAGER ✦</bold></gradient>",
                List.of("<gray>Kelola seluruh titik warp server.</gray>", "<yellow>▶ Klik untuk membuka Warp Admin GUI</yellow>")));

        // Slot 21: Set King Zenithar
        String kingZ = plugin.getConfigManager().getKingdomKing("ZENITHAR");
        inventory.setItem(21, createActionItem(Material.GOLD_BLOCK, "<gold><bold>👑 RAJA ZENITHAR</bold></gold>",
                List.of("<gray>Raja Saat Ini: <yellow>" + (kingZ.isEmpty() ? "Belum Ditunjuk" : kingZ) + "</yellow></gray>", "<yellow>▶ Klik untuk setel Raja Zenithar di chat</yellow>")));

        // Slot 22: Kingdom War Controls
        boolean isWar = plugin.getWarManager().isWarActive();
        inventory.setItem(22, createActionItem(isWar ? Material.NETHERITE_SWORD : Material.IRON_SWORD, "<gradient:#e74c3c:#c0392b><bold>⚔ KINGDOM WAR CONTROLS ⚔</bold></gradient>",
                List.of("<gray>Status War: " + (isWar ? "<red><bold>SEDANG AKTIF</bold></red>" : "<green>Damai</green>") + "</gray>",
                        isWar ? "<red>▶ Klik untuk Hentikan Perang</red>" : "<yellow>▶ Klik untuk Deklarasi Perang (30m)</yellow>")));

        // Slot 23: Set King Solterra
        String kingS = plugin.getConfigManager().getKingdomKing("SOLTERRA");
        inventory.setItem(23, createActionItem(Material.REDSTONE_BLOCK, "<red><bold>👑 RAJA SOLTERRA</bold></red>",
                List.of("<gray>Raja Saat Ini: <yellow>" + (kingS.isEmpty() ? "Belum Ditunjuk" : kingS) + "</yellow></gray>", "<yellow>▶ Klik untuk setel Raja Solterra di chat</yellow>")));

        // Slot 24: Set King Sylvamoor
        String kingSyl = plugin.getConfigManager().getKingdomKing("SYLVAMOOR");
        inventory.setItem(24, createActionItem(Material.EMERALD_BLOCK, "<green><bold>👑 RAJA SYLVAMOOR</bold></green>",
                List.of("<gray>Raja Saat Ini: <yellow>" + (kingSyl.isEmpty() ? "Belum Ditunjuk" : kingSyl) + "</yellow></gray>", "<yellow>▶ Klik untuk setel Raja Sylvamoor di chat</yellow>")));

        // Slot 25: Custom Enchant Tool & ACE Panel
        inventory.setItem(25, createActionItem(Material.ENCHANTED_BOOK, "<gradient:#9b59b6:#8e44ad><bold>✨ APEXSIONS CUSTOM ENCHANTS ✨</bold></gradient>",
                List.of("<gray>Akses admin sihir & tool enchant limit:</gray>",
                        "<dark_gray>•</dark_gray> <yellow>Buka Admin Hub /ace</yellow>",
                        "<dark_gray>•</dark_gray> <yellow>Enchant Tool limit (" + plugin.getConfigManager().getEnchantMultiplier() + "x vanilla limit)</yellow>",
                        "",
                        "<yellow>▶ [Klik Kiri] Buka Panel /ace</yellow>",
                        "<aqua>▶ [Klik Kanan] Panduan /enchant</aqua>")));

        // Slot 28: Level Rewards Editor
        inventory.setItem(28, createActionItem(Material.CHEST, "<gradient:#f1c40f:#e67e22><bold>🎁 KELOLA HADIAH LEVEL 🎁</bold></gradient>",
                List.of("<gray>Kelola hadiah Level 1-100 via Drag & Drop item.</gray>", "<yellow>▶ Klik untuk buka Level Reward Editor</yellow>")));

        // Row 4: Spawn & System Configs (Slots 29..34)
        inventory.setItem(29, createActionItem(Material.BEACON, "<light_purple><bold>🏛 ATUR SPAWN LOBBY</bold></light_purple>",
                List.of("<gray>Tetapkan titik spawn lobby server pada posisimu.</gray>", "<yellow>▶ Klik untuk setel spawn</yellow>")));

        inventory.setItem(30, createActionItem(Material.GOLD_INGOT, "<gold><bold>📍 ATUR SPAWN ZENITHAR</bold></gold>",
                List.of("<gray>Tetapkan titik ibukota Zenithar pada posisimu.</gray>", "<yellow>▶ Klik untuk setel titik spawn</yellow>")));

        inventory.setItem(31, createActionItem(Material.FIRE_CHARGE, "<red><bold>📍 ATUR SPAWN SOLTERRA</bold></red>",
                List.of("<gray>Tetapkan titik ibukota Solterra pada posisimu.</gray>", "<yellow>▶ Klik untuk setel titik spawn</yellow>")));

        inventory.setItem(32, createActionItem(Material.LILY_PAD, "<green><bold>📍 ATUR SPAWN SYLVAMOOR</bold></green>",
                List.of("<gray>Tetapkan titik ibukota Sylvamoor pada posisimu.</gray>", "<yellow>▶ Klik untuk setel titik spawn</yellow>")));

        inventory.setItem(33, createActionItem(Material.COMPASS, "<aqua><bold>🧭 UJI COBA KINGDOM RTP</bold></aqua>",
                List.of("<gray>Teleportasi acak di wilayah kerajaan pemain.</gray>", "<yellow>▶ Klik untuk uji coba RTP</yellow>")));

        inventory.setItem(34, createActionItem(Material.REDSTONE, "<red><bold>⚡ RELOAD APEXSIONS CORE</bold></red>",
                List.of("<gray>Muat ulang seluruh file konfigurasi Core.</gray>", "<yellow>▶ Klik untuk reload</yellow>")));

        // Slot 49: Back to Master Hub
        ItemStack backBtn = createActionItem(Material.OAK_DOOR, "<red><bold>◀ KEMBALI KE MASTER ADMIN HUB</bold></red>",
                List.of("<gray>Kembali ke menu utama panel administrasi.</gray>"));
        inventory.setItem(49, backBtn);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) { // Back to Hub
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            new MasterAdminGUI(plugin, player).open();
            return;
        }

        if (slot == 19) { // Kits Management
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            player.closeInventory();
            new com.apexsions.core.kit.KitUserGUI(plugin, player).open();
            return;
        }

        if (slot == 20) { // Warp Admin GUI
            player.closeInventory();
            new WarpAdminGUI(plugin, player).open();
            return;
        }

        if (slot == 21) { // Set King Zenithar
            plugin.getAdminChatInputManager().startSession(player,
                    "Ketik nama pemain untuk dinobatkan sebagai Raja Zenithar:",
                    kingName -> {
                        player.performCommand("kingdom setking ZENITHAR " + kingName);
                        open();
                    },
                    this::open
            );
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

        if (slot == 23) { // Set King Solterra
            plugin.getAdminChatInputManager().startSession(player,
                    "Ketik nama pemain untuk dinobatkan sebagai Raja Solterra:",
                    kingName -> {
                        player.performCommand("kingdom setking SOLTERRA " + kingName);
                        open();
                    },
                    this::open
            );
            return;
        }

        if (slot == 24) { // Set King Sylvamoor
            plugin.getAdminChatInputManager().startSession(player,
                    "Ketik nama pemain untuk dinobatkan sebagai Raja Sylvamoor:",
                    kingName -> {
                        player.performCommand("kingdom setking SYLVAMOOR " + kingName);
                        open();
                    },
                    this::open
            );
            return;
        }

        if (slot == 25) { // Custom Enchant Tool & ACE Hub
            if (event.isRightClick()) {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f, 1.2f);
                player.sendMessage(mm.deserialize("<gold><bold>════════════════════════════════════════</bold></gold>"));
                player.sendMessage(mm.deserialize("<gradient:#9b59b6:#8e44ad><bold>✨ APEXSIONS CUSTOM ENCHANT GUIDE ✨</bold></gradient>"));
                player.sendMessage(mm.deserialize("<gray>Pegang item di tangan utama, lalu jalankan command:</gray>"));
                player.sendMessage(mm.deserialize("<yellow>/enchant <enchantment> <level></yellow> <gray>(cth: <gold>/enchant sharpness 20</gold>)</gray>"));
                player.sendMessage(mm.deserialize("<yellow>/enchant <player> <enchantment> <level></yellow> <gray>(cth: <gold>/enchant " + player.getName() + " protection 12</gold>)</gray>"));
                player.sendMessage(mm.deserialize("<yellow>/enchant remove <enchantment></yellow> <gray>atau level 0 untuk menghapus enchant.</gray>"));
                player.sendMessage(mm.deserialize("<gray>Batas level: <gold>" + plugin.getConfigManager().getEnchantMultiplier() + "x vanilla limit</gold> (Protection: 12, Mending: 4, Sharpness: 20).</gray>"));
                player.sendMessage(mm.deserialize("<gold><bold>════════════════════════════════════════</bold></gold>"));
            } else {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                player.closeInventory();
                player.performCommand("ace");
            }
            return;
        }

        if (slot == 28) { // Level Rewards Editor
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            player.openInventory(new AdminLevelRewardListGUI(plugin, player, 1).getInventory());
            return;
        }

        if (slot == 29) { // Set Lobby Spawn
            player.performCommand("ac setlobby");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
            player.sendMessage(mm.deserialize("<green>✓ Titik spawn lobby berhasil disetel pada posisimu!</green>"));
            return;
        }

        if (slot == 30) { // Set Spawn Zenithar
            player.performCommand("ac setspawn ZENITHAR");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
            player.sendMessage(mm.deserialize("<green>✓ Titik spawn Zenithar berhasil disetel pada posisimu!</green>"));
            return;
        }

        if (slot == 31) { // Set Spawn Solterra
            player.performCommand("ac setspawn SOLTERRA");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
            player.sendMessage(mm.deserialize("<green>✓ Titik spawn Solterra berhasil disetel pada posisimu!</green>"));
            return;
        }

        if (slot == 32) { // Set Spawn Sylvamoor
            player.performCommand("ac setspawn SYLVAMOOR");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
            player.sendMessage(mm.deserialize("<green>✓ Titik spawn Sylvamoor berhasil disetel pada posisimu!</green>"));
            return;
        }

        if (slot == 33) { // Kingdom RTP
            player.closeInventory();
            plugin.getKingdomRtpService().executeRtp(player);
            return;
        }

        if (slot == 34) { // Reload Core
            plugin.getConfigManager().reload();
            player.sendMessage(mm.deserialize("<green>✓ ApexsionsCore berhasil dimuat ulang!</green>"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
            buildGUI();
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
