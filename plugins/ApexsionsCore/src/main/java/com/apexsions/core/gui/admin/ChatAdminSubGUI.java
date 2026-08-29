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

    private void buildGUI() {
        ItemStack border = createGlass(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Slot 20: Open Staff Reports GUI (/reports)
        ItemStack reportItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta repMeta = reportItem.getItemMeta();
        if (repMeta != null) {
            repMeta.displayName(mm.deserialize("<gradient:#e74c3c:#f39c12><bold>📋 STAFF REPORTS INBOX 📋</bold></gradient>"));
            repMeta.lore(List.of(
                    mm.deserialize("<gray>Buka daftar tiket laporan pemain:</gray>"),
                    mm.deserialize("<dark_gray>•</dark_gray> <white>Investigasi bukti chat & teleportasi</white>"),
                    mm.deserialize("<dark_gray>•</dark_gray> <white>Ambil tindakan sanksi / dismiss</white>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk membuka /reports GUI!</yellow>")
            ));
            reportItem.setItemMeta(repMeta);
        }
        inventory.setItem(20, reportItem);

        // Slot 22: Clear Global Chat
        ItemStack clearItem = new ItemStack(Material.BUCKET);
        ItemMeta clearMeta = clearItem.getItemMeta();
        if (clearMeta != null) {
            clearMeta.displayName(mm.deserialize("<gradient:#e67e22:#d35400><bold>🧹 BERSIHKAN OBROLAN GLOBAL 🧹</bold></gradient>"));
            clearMeta.lore(List.of(
                    mm.deserialize("<gray>Membersihkan riwayat obrolan layar seluruh pemain.</gray>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk membersihkan chat sekarang!</yellow>")
            ));
            clearItem.setItemMeta(clearMeta);
        }
        inventory.setItem(22, clearItem);

        // Slot 24: Start Chat Game
        ItemStack gameItem = new ItemStack(Material.FIREWORK_ROCKET);
        ItemMeta gameMeta = gameItem.getItemMeta();
        if (gameMeta != null) {
            gameMeta.displayName(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>🎯 MULAI CHAT MINI-GAME 🎯</bold></gradient>"));
            gameMeta.lore(List.of(
                    mm.deserialize("<gray>Picu event mini-game kuis / susun kata di chat.</gray>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk memulai event chat game!</yellow>")
            ));
            gameItem.setItemMeta(gameMeta);
        }
        inventory.setItem(24, gameItem);

        // Slot 31: Reload Chat Plugin
        ItemStack reloadItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta relMeta = reloadItem.getItemMeta();
        if (relMeta != null) {
            relMeta.displayName(mm.deserialize("<gradient:#e74c3c:#c0392b><bold>🔄 RELOAD APEXSIONS CHAT 🔄</bold></gradient>"));
            relMeta.lore(List.of(
                    mm.deserialize("<gray>Muat ulang konfigurasi, channels & filter.</gray>"),
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

        if (slot == 20) { // /reports
            player.closeInventory();
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsChat")) {
                player.performCommand("reports");
            } else {
                player.sendMessage(mm.deserialize("<red>Plugin ApexsionsChat tidak aktif.</red>"));
            }
            return;
        }

        if (slot == 22) { // Clear chat
            player.closeInventory();
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsChat")) {
                player.performCommand("clearchat");
            } else {
                for (int i = 0; i < 100; i++) {
                    Bukkit.broadcast(Component.empty());
                }
                Bukkit.broadcast(mm.deserialize("<yellow>Obrolan telah dibersihkan oleh Administrator <white>" + player.getName() + "</white>.</yellow>"));
            }
            return;
        }

        if (slot == 24) { // Start chat game
            player.closeInventory();
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsChat")) {
                player.performCommand("chatgame start");
            } else {
                player.sendMessage(mm.deserialize("<red>Plugin ApexsionsChat tidak aktif.</red>"));
            }
            return;
        }

        if (slot == 31) { // Reload
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsChat")) {
                player.performCommand("apexsionschat reload");
            }
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
            player.sendMessage(mm.deserialize("<green>✓ ApexsionsChat berhasil dimuat ulang!</green>"));
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
