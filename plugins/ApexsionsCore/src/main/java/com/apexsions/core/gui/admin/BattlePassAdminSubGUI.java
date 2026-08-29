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
 * 54-Slot Sub-GUI for ApexsionsBattlepass Administration.
 */
public class BattlePassAdminSubGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player admin;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public BattlePassAdminSubGUI(ApexsionsCorePlugin plugin, Player admin) {
        this.plugin = plugin;
        this.admin = admin;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#9b59b6:#3498db><bold>📜 BATTLEPASS CONTROL CENTER 📜</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        admin.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();

        ItemStack borderPane = createGlass(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        ItemStack decorPane = createGlass(Material.PURPLE_STAINED_GLASS_PANE, "<light_purple>✦</light_purple>");

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

        // Header Slot 4: Season Info
        ItemStack header = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta hMeta = header.getItemMeta();
        if (hMeta != null) {
            hMeta.displayName(mm.deserialize("<gradient:#9b59b6:#f1c40f><bold>👑 STATUS MUSIM BATTLEPASS 👑</bold></gradient>"));
            hMeta.lore(List.of(
                    mm.deserialize("<gray>Plugin:</gray> <light_purple>ApexsionsBattlepass v1.0.0</light_purple>"),
                    mm.deserialize("<gray>Status Sistem:</gray> <green>● Berjalan Normal</green>"),
                    mm.deserialize("<gray>Tipe Akses:</gray> <yellow>Full Pass & Quest Automation</yellow>"),
                    Component.empty(),
                    mm.deserialize("<yellow>Pilih opsi di bawah untuk mengelola battlepass.</yellow>")
            ));
            header.setItemMeta(hMeta);
        }
        inventory.setItem(4, header);

        // Actions (Slots 20, 21, 22, 23, 24, 31)
        inventory.setItem(20, createActionItem(Material.GOLD_BLOCK, "<gold><bold>🏆 BERIKAN PREMIUM PASS</bold></gold>",
                List.of("<gray>Beri Premium BattlePass ke pemain.</gray>", "<yellow>▶ Klik untuk pilih pemain di chat</yellow>")));

        inventory.setItem(21, createActionItem(Material.WRITABLE_BOOK, "<aqua><bold>🔄 RESET QUEST HARIAN (DAILY)</bold></aqua>",
                List.of("<gray>Paksa reset seluruh quest harian pemain aktif.</gray>", "<yellow>▶ Klik untuk eksekusi reset</yellow>")));

        inventory.setItem(22, createActionItem(Material.BOOKSHELF, "<light_purple><bold>🔄 RESET QUEST MINGGUAN (WEEKLY)</bold></light_purple>",
                List.of("<gray>Paksa reset seluruh quest mingguan pemain aktif.</gray>", "<yellow>▶ Klik untuk eksekusi reset</yellow>")));

        inventory.setItem(23, createActionItem(Material.EMERALD, "<green><bold>🛒 FORCE ROTASI TOKO BP</bold></green>",
                List.of("<gray>Putar rotasi komoditas toko BattlePass sekarang.</gray>", "<yellow>▶ Klik untuk putar rotasi toko</yellow>")));

        inventory.setItem(24, createActionItem(Material.EXPERIENCE_BOTTLE, "<yellow><bold>⭐ SET TIER BATTLEPASS PEMAIN</bold></yellow>",
                List.of("<gray>Atur pencapaian Tier BattlePass pemain.</gray>", "<yellow>▶ Klik untuk input di chat</yellow>")));

        inventory.setItem(31, createActionItem(Material.REDSTONE_BLOCK, "<red><bold>⚡ RELOAD APEXSIONS BATTLEPASS</bold></red>",
                List.of("<gray>Muat ulang seluruh file konfigurasi & quest BP.</gray>", "<yellow>▶ Klik untuk reload</yellow>")));

        // Bottom Slot 49: Back to Master Hub
        ItemStack backHub = createActionItem(Material.OAK_DOOR, "<red><bold>◀ KEMBALI KE MASTER ADMIN HUB</bold></red>",
                List.of("<gray>Kembali ke menu utama panel administrasi.</gray>"));
        inventory.setItem(49, backHub);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 20) { // Give Pass
            plugin.getAdminChatInputManager().startSession(admin,
                    "Ketik nama pemain yang ingin diberikan Premium Pass:",
                    targetName -> {
                        admin.performCommand("abp givepass " + targetName + " premium");
                        admin.sendMessage(mm.deserialize("<green>✓ Perintah Give Pass dieksekusi untuk <yellow>" + targetName + "</yellow>!</green>"));
                        open();
                    },
                    this::open
            );
            return;
        }

        if (slot == 21) { // Reset Daily
            admin.performCommand("abp reset daily");
            admin.sendMessage(mm.deserialize("<green>✓ Seluruh quest harian berhasil di-reset!</green>"));
            admin.playSound(admin.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
            return;
        }

        if (slot == 22) { // Reset Weekly
            admin.performCommand("abp reset weekly");
            admin.sendMessage(mm.deserialize("<green>✓ Seluruh quest mingguan berhasil di-reset!</green>"));
            admin.playSound(admin.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
            return;
        }

        if (slot == 23) { // Force Rotate Shop
            admin.performCommand("abp shop rotate");
            admin.sendMessage(mm.deserialize("<green>✓ Toko BattlePass berhasil diputar secara paksa!</green>"));
            admin.playSound(admin.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.3f);
            return;
        }

        if (slot == 24) { // Set Tier
            plugin.getAdminChatInputManager().startSession(admin,
                    "Ketik nama pemain dan tier (format: <nama> <tier>):",
                    input -> {
                        String[] parts = input.split(" ");
                        if (parts.length >= 2) {
                            admin.performCommand("abp setlevel " + parts[0] + " " + parts[1]);
                            admin.sendMessage(mm.deserialize("<green>✓ Tier BP " + parts[0] + " disetel ke <yellow>" + parts[1] + "</yellow>!</green>"));
                        } else {
                            admin.sendMessage(mm.deserialize("<red>Format salah! Gunakan: <nama> <tier></red>"));
                        }
                        open();
                    },
                    this::open
            );
            return;
        }

        if (slot == 31) { // Reload
            admin.performCommand("abp reload");
            admin.sendMessage(mm.deserialize("<green>✓ ApexsionsBattlepass berhasil dimuat ulang!</green>"));
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
