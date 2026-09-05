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
 * 54-Slot Interactive Sub-Menu for ApexsionsEconomy administration.
 */
public class EconomyAdminSubGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public EconomyAdminSubGUI(ApexsionsCorePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#2ecc71:#27ae60><bold>💰 APEXSIONS ECONOMY MANAGER 💰</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();

        ItemStack border = createGlass(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        ItemStack decor = createGlass(Material.LIME_STAINED_GLASS_PANE, "<green>✦</green>");

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
        ItemStack header = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta hMeta = header.getItemMeta();
        if (hMeta != null) {
            hMeta.displayName(mm.deserialize("<gradient:#2ecc71:#f1c40f><bold>👑 SISTEM MULTI-CURRENCY APEXSIONS 👑</bold></gradient>"));
            hMeta.lore(List.of(
                    mm.deserialize("<gray>Plugin:</gray> <green>ApexsionsEconomy v1.0.0</green>"),
                    mm.deserialize("<gray>Mata Uang:</gray> <yellow>Rupiah (IDR) & Diamond 💎 (Premium)</yellow>"),
                    mm.deserialize("<gray>Fitur:</gray> <aqua>Bank, Lelang (/ah), Barter (/trade)</aqua>"),
                    Component.empty(),
                    mm.deserialize("<yellow>Pilih opsi kontrol ekonomi di bawah.</yellow>")
            ));
            header.setItemMeta(hMeta);
        }
        inventory.setItem(4, header);

        // Slot 20: Give Rupiah (Chat Input)
        inventory.setItem(20, createActionItem(Material.EMERALD, "<green><bold>💵 TRANSFER SALDO RUPIAH</bold></green>",
                List.of("<gray>Kirim saldo Rupiah ke pemain.</gray>", "<yellow>▶ Klik untuk input di chat</yellow>")));

        // Slot 21: Give Diamond (Chat Input)
        inventory.setItem(21, createActionItem(Material.DIAMOND, "<aqua><bold>💎 TRANSFER SALDO DIAMOND 💎</bold></aqua>",
                List.of("<gray>Kirim saldo Diamond 💎 ke pemain.</gray>", "<yellow>▶ Klik untuk input di chat</yellow>")));

        // Slot 22: Open Auction House
        inventory.setItem(22, createActionItem(Material.GOLD_INGOT, "<gold><bold>🏛 BUKA AUCTION HOUSE (/ah)</bold></gold>",
                List.of("<gray>Inspeksi seluruh lelang pasar global.</gray>", "<yellow>▶ Klik untuk buka pasar lelang</yellow>")));

        // Slot 23: Clear Expired Auctions
        inventory.setItem(23, createActionItem(Material.HOPPER, "<yellow><bold>🧹 BERSIHKAN LELANG EXPIRED</bold></yellow>",
                List.of("<gray>Hapus lelang yang sudah kedaluwarsa.</gray>", "<yellow>▶ Klik untuk eksekusi</yellow>")));

        // Slot 24: Top Balances (/economy top)
        inventory.setItem(24, createActionItem(Material.TOTEM_OF_UNDYING, "<gradient:#f1c40f:#e67e22><bold>🏆 TOP SALDO SERVER</bold></gradient>",
                List.of("<gray>Lihat daftar pemain terkaya di server.</gray>", "<yellow>▶ Klik untuk lihat peringkat</yellow>")));

        // Slot 25: Kingdom Treasury Inspector
        inventory.setItem(25, createActionItem(Material.GOLD_BLOCK, "<gradient:#ffd700:#f39c12><bold>🏛 KAS KERAJAAN (TREASURY)</bold></gradient>",
                List.of(
                        "<gray>Akumulasi pajak transaksi dari seluruh kerajaan.</gray>",
                        "<gray>Pajak Lelang Aktif:</gray> <green>5% per transaksi</green>",
                        " ",
                        "<yellow>▶ Klik untuk cek kas kerajaan</yellow>"
                )));

        // Slot 31: Reload Economy
        inventory.setItem(31, createActionItem(Material.REDSTONE_BLOCK, "<red><bold>🔄 RELOAD APEXSIONS ECONOMY</bold></red>",
                List.of("<gray>Muat ulang konfigurasi, database, & pajak.</gray>", "<yellow>▶ Klik untuk reload</yellow>")));

        // Slot 32: Bank Deposito Manager
        inventory.setItem(32, createActionItem(Material.CHEST, "<gradient:#2ecc71:#f1c40f><bold>🏦 DEPOSITO BERJANGKA BANK</bold></gradient>",
                List.of(
                        "<gray>Kelola dan buka brankas deposito berjangka.</gray>",
                        "<gray>Paket:</gray> <white>1 Hari (1.5%), 3 Hari (5%), 7 Hari (12.5%)</white>",
                        " ",
                        "<yellow>▶ Klik untuk buka menu deposito</yellow>"
                )));

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

        if (slot == 20) { // Give Rupiah
            plugin.getAdminChatInputManager().startSession(player,
                    "Ketik nama pemain dan jumlah Rupiah (format: <nama> <jumlah>):",
                    input -> {
                        String[] parts = input.trim().split("\\s+");
                        if (parts.length >= 2) {
                            player.performCommand("ecoadmin give " + parts[0] + " " + parts[1] + " rupiah");
                        } else {
                            player.sendMessage(mm.deserialize("<red>Format salah! Gunakan: <nama> <jumlah></red>"));
                        }
                        open();
                    },
                    this::open
            );
            return;
        }

        if (slot == 21) { // Give Diamond
            plugin.getAdminChatInputManager().startSession(player,
                    "Ketik nama pemain dan jumlah Diamond 💎 (format: <nama> <jumlah>):",
                    input -> {
                        String[] parts = input.trim().split("\\s+");
                        if (parts.length >= 2) {
                            player.performCommand("ecoadmin give " + parts[0] + " " + parts[1] + " diamond");
                        } else {
                            player.sendMessage(mm.deserialize("<red>Format salah! Gunakan: <nama> <jumlah></red>"));
                        }
                        open();
                    },
                    this::open
            );
            return;
        }

        if (slot == 22) { // Open AH
            player.closeInventory();
            player.performCommand("ah");
            return;
        }

        if (slot == 23) { // Clear Expired AH
            player.performCommand("ah admin clear");
            player.sendMessage(mm.deserialize("<green>✓ Seluruh lelang expired berhasil dibersihkan!</green>"));
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
            return;
        }

        if (slot == 24) { // Top Balances
            player.closeInventory();
            player.performCommand("economy top");
            return;
        }

        if (slot == 25) { // Kingdom Treasury
            player.closeInventory();
            player.sendMessage(mm.deserialize("<dark_gray><strikethrough>────────────────────────────────────────</strikethrough></dark_gray>"));
            player.sendMessage(mm.deserialize("<gradient:#ffd700:#f39c12><bold>🏛 KAS KERAJAAN & STATUS TREASURY 🏛</bold></gradient>"));
            player.sendMessage(mm.deserialize("<gray>Pajak Lelang (5%) disetorkan otomatis ke kas kerajaan penjual.</gray>"));
            player.sendMessage(mm.deserialize("<dark_gray><strikethrough>────────────────────────────────────────</strikethrough></dark_gray>"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
            return;
        }

        if (slot == 31) { // Reload
            player.performCommand("ecoadmin reload");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
            return;
        }

        if (slot == 32) { // Bank Deposito
            player.closeInventory();
            player.performCommand("bank");
            return;
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
