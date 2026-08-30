package com.apexsions.chat.gui;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.chat.model.Report;
import com.apexsions.chat.model.ReportStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReportDetailGUI extends BaseChatGUI {

    private final ApexsionsChatPlugin plugin;
    private final Report report;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public ReportDetailGUI(ApexsionsChatPlugin plugin, Report report) {
        this.plugin = plugin;
        this.report = report;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<dark_gray>🛡️ Manajemen Laporan #" + report.getReportId() + "</dark_gray>"));
        build();
    }

    private void build() {
        ItemStack border = createBorderItem();
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Summary Info at Slot 4
        inventory.setItem(4, createSummaryItem());

        // Row 2: Status Lifecycle Actions (Slots 10, 12, 14, 16)
        inventory.setItem(10, createActionButton("<yellow><bold>⏳ SEDANG DITINJAU (IN REVIEW)</bold></yellow>", Material.CLOCK,
                "<gray>Ubah status laporan menjadi sedang diinvestigasi.</gray>"));

        inventory.setItem(12, createActionButton("<green><bold>✔ SELESAI (RESOLVE)</bold></green>", Material.EMERALD_BLOCK,
                "<gray>Tandai laporan telah berhasil diselesaikan/diberi sanksi.</gray>"));

        inventory.setItem(14, createActionButton("<red><bold>✖ TOLAK LAPORAN (DISMISS)</bold></red>", Material.REDSTONE_BLOCK,
                "<gray>Tolak laporan palsu atau tidak cukup bukti.</gray>"));

        inventory.setItem(16, createActionButton("<aqua><bold>🚀 TELEPORT KE TERLAPOR</bold></aqua>", Material.ENDER_PEARL,
                "<gray>Teleportasikan dirimu ke posisi pemain terlapor atau dunianya.</gray>"));

        // Row 4: Rapid Moderation Execution (Slots 28, 30, 32, 34)
        inventory.setItem(28, createActionButton("<yellow><bold>🔇 MUTE 10 MENIT</bold></yellow>", Material.BELL,
                "<gray>Bungkam chat pemain terlapor selama 10 menit.</gray>"));

        inventory.setItem(30, createActionButton("<gold><bold>⚠️ PERINGATAN (WARN)</bold></gold>", Material.PAPER,
                "<gray>Kirim teguran resmi ke pemain terlapor.</gray>"));

        inventory.setItem(32, createActionButton("<red><bold>👢 KICK DARI SERVER</bold></red>", Material.IRON_BOOTS,
                "<gray>Keluarkan pemain terlapor dari permainan saat ini.</gray>"));

        inventory.setItem(34, createActionButton("<dark_red><bold>🔨 BAN SEMENTARA (1H)</bold></dark_red>", Material.NETHERITE_AXE,
                "<gray>Larang pemain masuk server selama 1 jam.</gray>"));

        // Back button at Slot 49
        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta meta = backBtn.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize("<yellow><bold>◀ Kembali ke Daftar Laporan</bold></yellow>"));
            backBtn.setItemMeta(meta);
        }
        inventory.setItem(49, backBtn);
    }

    private ItemStack createSummaryItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize("<gold><bold>📄 RINCIAN TIKET #" + report.getReportId() + "</bold></gold>"));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Pelapor:</gray> <white>" + report.getReporterName() + "</white>"));
            lore.add(mm.deserialize("<gray>Terlapor:</gray> <red><bold>" + report.getReportedName() + "</bold></red>"));
            lore.add(mm.deserialize("<gray>Alasan:</gray> <yellow>" + report.getReason() + "</yellow>"));
            lore.add(mm.deserialize("<gray>Dunia:</gray> <white>" + report.getWorld() + "</white>"));
            lore.add(mm.deserialize("<gray>Status:</gray> <gold>" + report.getStatus().name() + "</gold>"));
            lore.add(mm.deserialize("<gray>Waktu Kejadian:</gray> <white>" + dtf.format(report.getTimestamp()) + "</white>"));
            if (report.getModeratorName() != null) {
                lore.add(mm.deserialize("<gray>Staf Penangan:</gray> <aqua>" + report.getModeratorName() + "</aqua>"));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(Player staff, InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) { // Back to report list
            new ReportListGUI(plugin, 1).loadAndOpen(staff);
            return;
        }

        if (slot == 10) { // Mark Reviewing
            plugin.getReportRepository().updateReportStatusAsync(
                    report.getReportId(), ReportStatus.REVIEWING, staff.getUniqueId(), staff.getName(), "Sedang ditinjau oleh " + staff.getName()
            ).thenAccept(success -> {
                staff.sendMessage(mm.deserialize("<yellow>✔ Laporan #" + report.getReportId() + " berstatus: <gold>REVIEWING</gold>.</yellow>"));
                staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                staff.closeInventory();
            });
            return;
        }

        if (slot == 12) { // Mark Resolved
            plugin.getReportRepository().updateReportStatusAsync(
                    report.getReportId(), ReportStatus.RESOLVED, staff.getUniqueId(), staff.getName(), "Ditindaklanjuti oleh " + staff.getName()
            ).thenAccept(success -> {
                staff.sendMessage(mm.deserialize("<green>✔ Laporan #" + report.getReportId() + " berhasil diselesaikan (<bold>RESOLVED</bold>)!</green>"));
                staff.playSound(staff.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
                staff.closeInventory();
            });
            return;
        }

        if (slot == 14) { // Mark Dismissed
            plugin.getReportRepository().updateReportStatusAsync(
                    report.getReportId(), ReportStatus.DISMISSED, staff.getUniqueId(), staff.getName(), "Ditolak oleh " + staff.getName()
            ).thenAccept(success -> {
                staff.sendMessage(mm.deserialize("<red>✔ Laporan #" + report.getReportId() + " telah <bold>DITOLAK (DISMISSED)</bold>.</red>"));
                staff.playSound(staff.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 1.0f);
                staff.closeInventory();
            });
            return;
        }

        if (slot == 16) { // Teleport
            Player target = Bukkit.getPlayer(report.getReportedUuid());
            if (target != null && target.isOnline()) {
                staff.teleport(target.getLocation());
                staff.sendMessage(mm.deserialize("<green>✔ Teleportasi ke posisi <yellow>" + target.getName() + "</yellow> berhasil!</green>"));
            } else {
                World w = Bukkit.getWorld(report.getWorld());
                if (w != null) {
                    staff.teleport(w.getSpawnLocation());
                    staff.sendMessage(mm.deserialize("<yellow>✔ Pemain offline, teleportasi ke Spawn dunia <white>" + report.getWorld() + "</white>.</yellow>"));
                } else {
                    staff.sendMessage(mm.deserialize("<red>Dunia TKP laporan tidak ditemukan.</red>"));
                }
            }
            staff.closeInventory();
            return;
        }

        if (slot == 28) { // Mute 10m
            staff.performCommand("mute " + report.getReportedName() + " 10m Pelanggaran pada Laporan #" + report.getReportId());
            staff.sendMessage(mm.deserialize("<green>✔ Berhasil mengeksekusi Mute 10m untuk " + report.getReportedName() + ".</green>"));
            staff.playSound(staff.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f);
            staff.closeInventory();
            return;
        }

        if (slot == 30) { // Warn
            Player target = Bukkit.getPlayer(report.getReportedUuid());
            if (target != null && target.isOnline()) {
                target.sendMessage(mm.deserialize("<red><bold>[PERINGATAN STAF]:</bold> Harap patuhi aturan server! Terkait laporan #" + report.getReportId() + "</red>"));
                target.playSound(target.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 1.2f);
            }
            staff.sendMessage(mm.deserialize("<gold>✔ Peringatan resmi dikirim ke " + report.getReportedName() + ".</gold>"));
            staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            staff.closeInventory();
            return;
        }

        if (slot == 32) { // Kick
            Player target = Bukkit.getPlayer(report.getReportedUuid());
            if (target != null && target.isOnline()) {
                target.kick(mm.deserialize("<red><bold>KAMU DI-KICK DARI SERVER</bold></red>\n<gray>Tindakan penanganan Laporan #" + report.getReportId() + "</gray>"));
                staff.sendMessage(mm.deserialize("<red>✔ " + target.getName() + " berhasil di-kick dari server.</red>"));
            } else {
                staff.sendMessage(mm.deserialize("<red>Pemain terlapor sedang tidak online.</red>"));
            }
            staff.playSound(staff.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.6f, 1.0f);
            staff.closeInventory();
            return;
        }

        if (slot == 34) { // Ban 1h
            staff.performCommand("ban " + report.getReportedName() + " 1h Pelanggaran berat Laporan #" + report.getReportId());
            staff.sendMessage(mm.deserialize("<dark_red>✔ Sanksi Ban 1 Jam dieksekusi untuk " + report.getReportedName() + ".</dark_red>"));
            staff.playSound(staff.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.6f, 1.0f);
            staff.closeInventory();
        }
    }

    private ItemStack createActionButton(String name, Material mat, String desc) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            meta.lore(Collections.singletonList(mm.deserialize(desc)));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBorderItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }

    public Report getReport() { return report; }
}
