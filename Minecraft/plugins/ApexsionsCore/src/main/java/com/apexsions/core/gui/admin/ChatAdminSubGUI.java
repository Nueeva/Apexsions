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
 * 54-Slot Interactive Sub-Menu for ApexsionsChat administration.
 */
public class ChatAdminSubGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ChatAdminSubGUI(ApexsionsCorePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#3498db:#2980b9><bold>💬 APEXSIONS CHAT CONTROLS 💬</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();

        ItemStack border = createGlass(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        ItemStack decor = createGlass(Material.CYAN_STAINED_GLASS_PANE, "<aqua>✦</aqua>");

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
        ItemStack header = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta hMeta = header.getItemMeta();
        if (hMeta != null) {
            hMeta.displayName(mm.deserialize("<gradient:#3498db:#9b59b6><bold>💬 PUSAT MODERASI & CHAT APEXSIONS 💬</bold></gradient>"));
            hMeta.lore(List.of(
                    mm.deserialize("<gray>Plugin:</gray> <aqua>ApexsionsChat v1.0.0</aqua>"),
                    mm.deserialize("<gray>Saluran:</gray> <yellow>Global, Kingdom, Local, Staff</yellow>"),
                    mm.deserialize("<gray>Filter:</gray> <green>● 3-Layer Moderation Engine Active</green>"),
                    Component.empty(),
                    mm.deserialize("<yellow>Pilih kontrol obrolan di bawah.</yellow>")
            ));
            header.setItemMeta(hMeta);
        }
        inventory.setItem(4, header);

        // Slot 20: Open Staff Reports GUI (/reports)
        inventory.setItem(20, createActionItem(Material.BOOK, "<gradient:#e74c3c:#f39c12><bold>📋 STAFF REPORTS INBOX</bold></gradient>",
                List.of("<gray>Buka daftar tiket laporan pemain.</gray>", "<yellow>▶ Klik untuk buka GUI Laporan</yellow>")));

        // Slot 21: Toggle Global Mute
        inventory.setItem(21, createActionItem(Material.BELL, "<gold><bold>🔇 TOGGLE GLOBAL MUTE</bold></gold>",
                List.of("<gray>Kunci atau buka seluruh saluran chat publik.</gray>", "<yellow>▶ Klik untuk toggle mute server</yellow>")));

        // Slot 22: Clear Global Chat
        inventory.setItem(22, createActionItem(Material.BUCKET, "<gradient:#e67e22:#d35400><bold>🧹 BERSIHKAN OBROLAN GLOBAL</bold></gradient>",
                List.of("<gray>Bersihkan riwayat obrolan layar seluruh pemain.</gray>", "<yellow>▶ Klik untuk bersihkan chat</yellow>")));

        // Slot 23: Broadcast Global Announcement (Chat Input)
        inventory.setItem(23, createActionItem(Material.OAK_SIGN, "<yellow><bold>📢 SIARKAN PENGUMUMAN (BROADCAST)</bold></yellow>",
                List.of("<gray>Kirim pengumuman resmi ke seluruh server.</gray>", "<yellow>▶ Klik untuk input teks di chat</yellow>")));

        // Slot 24: Start Chat Game
        inventory.setItem(24, createActionItem(Material.FIREWORK_ROCKET, "<gradient:#2ecc71:#27ae60><bold>🎯 MULAI CHAT MINI-GAME</bold></gradient>",
                List.of("<gray>Picu event mini-game tebak kata / matematika.</gray>", "<yellow>▶ Klik untuk mulai chat game</yellow>")));

        // Slot 31: Reload Chat Plugin
        inventory.setItem(31, createActionItem(Material.REDSTONE_BLOCK, "<gradient:#e74c3c:#c0392b><bold>🔄 RELOAD APEXSIONS CHAT</bold></gradient>",
                List.of("<gray>Muat ulang channels, filter kata, dan pesan chat.</gray>", "<yellow>▶ Klik untuk reload</yellow>")));

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

        if (slot == 20) { // Reports
            player.closeInventory();
            player.performCommand("reports");
            return;
        }

        if (slot == 21) { // Mute Chat
            player.performCommand("mutechat");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
            return;
        }

        if (slot == 22) { // Clear Chat
            player.performCommand("clearchat");
            player.sendMessage(mm.deserialize("<green>✓ Obrolan global berhasil dibersihkan!</green>"));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
            return;
        }

        if (slot == 23) { // Broadcast
            plugin.getAdminChatInputManager().startSession(player,
                    "Ketik pesan pengumuman yang ingin disiarkan ke seluruh server:",
                    broadcastText -> {
                        Bukkit.broadcast(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>📢 PENGUMUMAN RESMI 📢</bold></gradient>\n<white>" + broadcastText + "</white>"));
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            online.playSound(online.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);
                        }
                        open();
                    },
                    this::open
            );
            return;
        }

        if (slot == 24) { // Start Chat Game
            player.performCommand("chatgame start");
            player.sendMessage(mm.deserialize("<green>✓ Event chat mini-game dimulai!</green>"));
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8f, 1.2f);
            return;
        }

        if (slot == 31) { // Reload
            player.performCommand("chat reload");
            player.sendMessage(mm.deserialize("<green>✓ ApexsionsChat berhasil dimuat ulang!</green>"));
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
