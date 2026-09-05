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

public class WarpEditorGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Warp warp;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public WarpEditorGUI(ApexsionsCorePlugin plugin, Player player, Warp warp) {
        this.plugin = plugin;
        this.player = player;
        this.warp = warp;
        this.inventory = Bukkit.createInventory(this, 27, mm.deserialize("<gradient:#e67e22:#f39c12><bold>Editor Warp: " + warp.getName() + "</bold></gradient>"));
        render();
    }

    public void render() {
        inventory.clear();

        // Fill background
        ItemStack pane = createFiller(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inventory.setItem(i, pane);

        // Slot 4: Current Warp Preview
        ItemStack preview = new ItemStack(warp.getIcon());
        ItemMeta pm = preview.getItemMeta();
        if (pm != null) {
            pm.displayName(mm.deserialize("<gold><bold>Warp: " + warp.getName() + " (" + warp.getId() + ")</bold></gold>"));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Kategori: <aqua>" + warp.getCategory() + "</aqua></gray>"));
            lore.add(mm.deserialize("<gray>Koordinat: <white>" + warp.getWorldName() + " [" +
                    String.format("%.1f, %.1f, %.1f", warp.getX(), warp.getY(), warp.getZ()) + "]</white></gray>"));
            lore.add(mm.deserialize("<gray>Delay: <yellow>" + warp.getDelaySeconds() + "s</yellow></gray>"));
            lore.add(mm.deserialize("<gray>Status: " + (warp.isHidden() ? "<red>Hidden</red>" : "<green>Public</green>") + "</gray>"));
            pm.lore(lore);
            preview.setItemMeta(pm);
        }
        inventory.setItem(4, preview);

        // Slot 10: Set Location to Current
        ItemStack locBtn = new ItemStack(Material.COMPASS);
        ItemMeta lm = locBtn.getItemMeta();
        if (lm != null) {
            lm.displayName(mm.deserialize("<green><bold>⚑ Perbarui Lokasi</bold></green>"));
            lm.lore(List.of(
                    mm.deserialize("<gray>Ubah koordinat warp menjadi lokasi kamu berdiri saat ini.</gray>"),
                    mm.deserialize("<yellow>▶ Klik Kiri untuk memperbarui lokasi</yellow>")
            ));
            locBtn.setItemMeta(lm);
        }
        inventory.setItem(10, locBtn);

        // Slot 12: Change Icon Material
        ItemStack handItem = player.getInventory().getItemInMainHand();
        Material handMat = (handItem != null && !handItem.getType().isAir()) ? handItem.getType() : Material.COMPASS;
        ItemStack iconBtn = new ItemStack(handMat);
        ItemMeta im = iconBtn.getItemMeta();
        if (im != null) {
            im.displayName(mm.deserialize("<aqua><bold>✦ Ubah Ikon / Material</bold></aqua>"));
            im.lore(List.of(
                    mm.deserialize("<gray>Ikon Saat Ini: <yellow>" + warp.getIcon().name() + "</yellow></gray>"),
                    mm.deserialize("<gray>Item di Tangan: <yellow>" + handMat.name() + "</yellow></gray>"),
                    mm.deserialize("<yellow>▶ Klik Kiri untuk menerapkan item di tangan sebagai ikon</yellow>")
            ));
            iconBtn.setItemMeta(im);
        }
        inventory.setItem(12, iconBtn);

        // Slot 13: Change Category
        ItemStack catBtn = new ItemStack(Material.NAME_TAG);
        ItemMeta cm = catBtn.getItemMeta();
        if (cm != null) {
            cm.displayName(mm.deserialize("<light_purple><bold>🏷 Ubah Kategori</bold></light_purple>"));
            cm.lore(List.of(
                    mm.deserialize("<gray>Kategori Saat Ini: <aqua>" + warp.getCategory() + "</aqua></gray>"),
                    mm.deserialize("<gray>Pilihan: SERVER, RESOURCE, EVENT, KINGDOM, PVP, GENERAL</gray>"),
                    mm.deserialize("<yellow>▶ Klik Kiri untuk mengganti kategori berikutnya</yellow>")
            ));
            catBtn.setItemMeta(cm);
        }
        inventory.setItem(13, catBtn);

        // Slot 14: Change Delay
        ItemStack delayBtn = new ItemStack(Material.CLOCK);
        ItemMeta dm = delayBtn.getItemMeta();
        if (dm != null) {
            dm.displayName(mm.deserialize("<yellow><bold>⏱ Ubah Delay Teleportasi</bold></yellow>"));
            dm.lore(List.of(
                    mm.deserialize("<gray>Delay Saat Ini: <yellow>" + (warp.getDelaySeconds() == 0 ? "Instan (0s)" : warp.getDelaySeconds() + "s") + "</yellow></gray>"),
                    mm.deserialize("<yellow>▶ Klik Kiri untuk beralih (0s -> 3s -> 5s -> 10s)</yellow>")
            ));
            delayBtn.setItemMeta(dm);
        }
        inventory.setItem(14, delayBtn);

        // Slot 15: Toggle Hidden
        ItemStack hiddenBtn = new ItemStack(warp.isHidden() ? Material.ENDER_EYE : Material.ENDER_PEARL);
        ItemMeta hm = hiddenBtn.getItemMeta();
        if (hm != null) {
            hm.displayName(mm.deserialize("<blue><bold>👁 Status Tampilan (Public/Hidden)</bold></blue>"));
            hm.lore(List.of(
                    mm.deserialize("<gray>Status Saat Ini: " + (warp.isHidden() ? "<red>Hidden (Hanya Admin)</red>" : "<green>Public (Semua Pemain)</green>") + "</gray>"),
                    mm.deserialize("<yellow>▶ Klik Kiri untuk toggle status</yellow>")
            ));
            hiddenBtn.setItemMeta(hm);
        }
        inventory.setItem(15, hiddenBtn);

        // Slot 16: Teleport to warp
        ItemStack tpBtn = new ItemStack(Material.ENDER_PEARL);
        ItemMeta tm = tpBtn.getItemMeta();
        if (tm != null) {
            tm.displayName(mm.deserialize("<aqua><bold>⚡ Teleport ke Warp ini</bold></aqua>"));
            tm.lore(List.of(mm.deserialize("<yellow>▶ Klik Kiri untuk langsung teleportasi</yellow>")));
            tpBtn.setItemMeta(tm);
        }
        inventory.setItem(16, tpBtn);

        // Slot 21: Delete Warp
        ItemStack delBtn = new ItemStack(Material.TNT);
        ItemMeta dlm = delBtn.getItemMeta();
        if (dlm != null) {
            dlm.displayName(mm.deserialize("<red><bold>✖ Hapus Warp</bold></red>"));
            dlm.lore(List.of(
                    mm.deserialize("<gray>Menghapus warp ini secara permanen dari server.</gray>"),
                    mm.deserialize("<red>▶ Klik Kiri untuk menghapus</red>")
            ));
            delBtn.setItemMeta(dlm);
        }
        inventory.setItem(21, delBtn);

        // Slot 22: Back to WarpAdminGUI
        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta bm = backBtn.getItemMeta();
        if (bm != null) {
            bm.displayName(mm.deserialize("<yellow><bold>◀ Kembali ke Daftar Admin</bold></yellow>"));
            backBtn.setItemMeta(bm);
        }
        inventory.setItem(22, backBtn);
    }

    public void handleClick(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= 27) return;

        // Slot 10: Update Location
        if (slot == 10) {
            warp.updateLocation(player.getLocation());
            plugin.getWarpManager().saveWarp(warp).thenAccept(s -> Bukkit.getScheduler().runTask(plugin, () -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
                player.sendMessage(mm.deserialize("<green>✔ Lokasi warp <yellow>" + warp.getName() + "</yellow> berhasil diperbarui ke koordinat saat ini!</green>"));
                render();
            }));
            return;
        }

        // Slot 12: Change Icon
        if (slot == 12) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand != null && !hand.getType().isAir()) {
                warp.setIcon(hand.getType());
                plugin.getWarpManager().saveWarp(warp).thenAccept(s -> Bukkit.getScheduler().runTask(plugin, () -> {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.4f);
                    player.sendMessage(mm.deserialize("<green>✔ Ikon warp diubah menjadi <yellow>" + hand.getType().name() + "</yellow>!</green>"));
                    render();
                }));
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<red>Pegang item yang ingin kamu jadikan ikon di tangan utama!</red>"));
            }
            return;
        }

        // Slot 13: Cycle Category
        if (slot == 13) {
            String[] cats = {"SERVER", "RESOURCE", "EVENT", "KINGDOM", "PVP", "GENERAL"};
            int nextIdx = 0;
            for (int i = 0; i < cats.length; i++) {
                if (cats[i].equalsIgnoreCase(warp.getCategory())) {
                    nextIdx = (i + 1) % cats.length;
                    break;
                }
            }
            warp.setCategory(cats[nextIdx]);
            plugin.getWarpManager().saveWarp(warp).thenAccept(s -> Bukkit.getScheduler().runTask(plugin, () -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.4f);
                render();
            }));
            return;
        }

        // Slot 14: Cycle Delay
        if (slot == 14) {
            int cur = warp.getDelaySeconds();
            int nextDelay = switch (cur) {
                case 0 -> 3;
                case 3 -> 5;
                case 5 -> 10;
                default -> 0;
            };
            warp.setDelaySeconds(nextDelay);
            plugin.getWarpManager().saveWarp(warp).thenAccept(s -> Bukkit.getScheduler().runTask(plugin, () -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.4f);
                render();
            }));
            return;
        }

        // Slot 15: Toggle Hidden
        if (slot == 15) {
            warp.setHidden(!warp.isHidden());
            plugin.getWarpManager().saveWarp(warp).thenAccept(s -> Bukkit.getScheduler().runTask(plugin, () -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.4f);
                render();
            }));
            return;
        }

        // Slot 16: Teleport
        if (slot == 16) {
            player.closeInventory();
            plugin.getWarpManager().teleportPlayer(player, warp);
            return;
        }

        // Slot 21: Delete
        if (slot == 21) {
            plugin.getWarpManager().deleteWarp(warp.getId());
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.2f);
            player.sendMessage(mm.deserialize("<green>✔ Warp <yellow>" + warp.getId() + "</yellow> berhasil dihapus.</green>"));
            player.openInventory(new WarpAdminGUI(plugin, player).getInventory());
            return;
        }

        // Slot 22: Back
        if (slot == 22) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            player.openInventory(new WarpAdminGUI(plugin, player).getInventory());
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

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
