package com.apexsions.core.gui.warp;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.warp.Warp;
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

public class WarpAdminGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final List<Warp> warpList = new ArrayList<>();

    public WarpAdminGUI(ApexsionsCorePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>⚙ WARP MANAGEMENT ADMIN ⚙</bold></gradient>"));
        render();
    }

    public void render() {
        inventory.clear();
        warpList.clear();
        warpList.addAll(plugin.getWarpManager().getWarps());

        // Fill background
        ItemStack pane = createFiller(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) inventory.setItem(i, pane);
        for (int i = 45; i < 54; i++) inventory.setItem(i, pane);

        // Header info
        ItemStack header = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta hm = header.getItemMeta();
        if (hm != null) {
            hm.displayName(mm.deserialize("<gold><bold>Panel Manajemen Warp Admin</bold></gold>"));
            hm.lore(List.of(
                    mm.deserialize("<gray>Total Warp Terdaftar: <yellow>" + warpList.size() + "</yellow></gray>"),
                    mm.deserialize("<yellow>Klik pada warp untuk membuka menu editor interaktif.</yellow>")
            ));
            header.setItemMeta(hm);
        }
        inventory.setItem(4, header);

        // Render warps (slots 9 to 44)
        int slot = 9;
        for (Warp warp : warpList) {
            if (slot > 44) break;
            inventory.setItem(slot++, createAdminWarpItem(warp));
        }

        // Bottom Controls
        // Slot 45: Back to Admin Hub
        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta bm = backBtn.getItemMeta();
        if (bm != null) {
            bm.displayName(mm.deserialize("<gradient:#3498db:#2980b9><bold>⬅ KEMBALI KE ADMIN HUB</bold></gradient>"));
            bm.lore(List.of(mm.deserialize("<gray>Kembali ke panel Master Admin Hub (/admingui).</gray>")));
            backBtn.setItemMeta(bm);
        }
        inventory.setItem(45, backBtn);

        // Slot 47: Add New Warp
        ItemStack addBtn = new ItemStack(Material.EMERALD);
        ItemMeta am = addBtn.getItemMeta();
        if (am != null) {
            am.displayName(mm.deserialize("<green><bold>+ Buat Warp Baru</bold></green>"));
            am.lore(List.of(
                    mm.deserialize("<gray>Membuat warp baru di lokasi koordinat kamu berdiri saat ini.</gray>"),
                    mm.deserialize("<yellow>▶ Klik Kiri untuk membuat warp baru</yellow>")
            ));
            addBtn.setItemMeta(am);
        }
        inventory.setItem(47, addBtn);

        // Slot 49: Reload Warps
        ItemStack reloadBtn = new ItemStack(Material.NETHER_STAR);
        ItemMeta rm = reloadBtn.getItemMeta();
        if (rm != null) {
            rm.displayName(mm.deserialize("<yellow><bold>⟳ Muat Ulang Database</bold></yellow>"));
            rm.lore(List.of(mm.deserialize("<gray>Sinkronisasi ulang seluruh warp dari database.</gray>")));
            reloadBtn.setItemMeta(rm);
        }
        inventory.setItem(49, reloadBtn);

        // Slot 53: Close
        ItemStack closeBtn = new ItemStack(Material.BARRIER);
        ItemMeta cm = closeBtn.getItemMeta();
        if (cm != null) {
            cm.displayName(mm.deserialize("<red><bold>✖ Tutup Menu</bold></red>"));
            closeBtn.setItemMeta(cm);
        }
        inventory.setItem(53, closeBtn);
    }

    private ItemStack createAdminWarpItem(Warp warp) {
        ItemStack item = new ItemStack(warp.getIcon());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>" + warp.getName() + " (" + warp.getId() + ")</bold></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Kategori: <aqua>" + warp.getCategory() + "</aqua></gray>"));
            lore.add(mm.deserialize("<gray>Koordinat: <white>" + warp.getWorldName() + " [" + (int) warp.getX() + ", " + (int) warp.getY() + ", " + (int) warp.getZ() + "]</white></gray>"));
            lore.add(mm.deserialize("<gray>Delay: <yellow>" + warp.getDelaySeconds() + "s</yellow></gray>"));
            lore.add(mm.deserialize("<gray>Status: " + (warp.isHidden() ? "<red>Hidden (Admin Only)</red>" : "<green>Public</green>") + "</gray>"));
            if (warp.getPermission() != null) {
                lore.add(mm.deserialize("<gray>Permission: <light_purple>" + warp.getPermission() + "</light_purple></gray>"));
            }
            lore.add(Component.empty());
            lore.add(mm.deserialize("<yellow>▶ Klik Kiri untuk Mengedit Warp ini</yellow>"));
            lore.add(mm.deserialize("<aqua>▶ Klik Kanan untuk Teleport Langsung</aqua>"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        // Back to Admin Hub (Slot 45)
        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            plugin.getAdminHubManager().openHub(player);
            return;
        }

        // Add Warp button (Slot 47)
        if (slot == 47) {
            String newId = "warp_" + (warpList.size() + 1);
            plugin.getWarpManager().setWarp(newId, newId, "SERVER", Material.COMPASS, player.getLocation(), null, 3, "Warp baru")
                    .thenAccept(success -> Bukkit.getScheduler().runTask(plugin, () -> {
                        if (success) {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
                            player.sendMessage(mm.deserialize("<green>✔ Berhasil membuat warp <yellow>" + newId + "</yellow> di lokasi kamu!</green>"));
                            Warp created = plugin.getWarpManager().getWarp(newId);
                            if (created != null) {
                                player.openInventory(new WarpEditorGUI(plugin, player, created).getInventory());
                            } else {
                                render();
                            }
                        }
                    }));
            return;
        }

        // Reload button (Slot 49)
        if (slot == 49) {
            plugin.getWarpManager().loadAllWarps();
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
            player.sendMessage(mm.deserialize("<green>✔ Database warp berhasil dimuat ulang.</green>"));
            render();
            return;
        }

        // Close button (Slot 53)
        if (slot == 53) {
            player.closeInventory();
            return;
        }

        // Warp item click (slots 9 to 44)
        if (slot >= 9 && slot <= 44) {
            int index = slot - 9;
            if (index < warpList.size()) {
                Warp warp = warpList.get(index);
                if (e.isRightClick()) {
                    player.closeInventory();
                    plugin.getWarpManager().teleportPlayer(player, warp);
                } else {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                    player.openInventory(new WarpEditorGUI(plugin, player, warp).getInventory());
                }
            }
        }
    }

    private ItemStack createFiller(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
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
