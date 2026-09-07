package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.region.Region;
import com.apexsions.core.sions.SionsTemporalService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SionsCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final SionsTemporalService service;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public SionsCommand(ApexsionsCorePlugin plugin, SionsTemporalService service) {
        this.plugin = plugin;
        this.service = service;
    }

    private SionsTemporalService getTemporalService() {
        if (this.service != null) {
            return this.service;
        }
        return plugin.getSionsTemporalService();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("apexsions.admin")) {
            sender.sendMessage(miniMessage.deserialize("<red>✖ Anda tidak memiliki izin untuk menggunakan perintah ini.</red>"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        SionsTemporalService s = getTemporalService();

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "status" -> {
                if (s == null) {
                    sender.sendMessage(miniMessage.deserialize("<red>✖ Sistem Rekonstruksi Temporal Kerajaan Sions saat ini tidak aktif.</red>"));
                    return true;
                }
                int count = s.getModifiedBlockCount();
                long remainingSec = s.getRemainingSeconds();
                long remMin = remainingSec / 60;
                long remSec = remainingSec % 60;

                sender.sendMessage(miniMessage.deserialize("<gradient:#8e44ad:#9b59b6><bold>✦ KERAJAAN SIONS — STATUS REKONSTRUKSI TEMPORAL</bold></gradient>"));
                sender.sendMessage(miniMessage.deserialize("<gray>Sistem Temporal: <green>" + (s.isEnabled() ? "AKTIF" : "NONAKTIF") + "</green></gray>"));
                sender.sendMessage(miniMessage.deserialize("<gray>Interval Reset: <gold>" + s.getIntervalMinutes() + " Menit</gold></gray>"));
                sender.sendMessage(miniMessage.deserialize("<gray>Block Terubah Saat Ini: <yellow><bold>" + count + "</bold></yellow> block</gray>"));
                sender.sendMessage(miniMessage.deserialize("<gray>Sisa Waktu Menuju Reset: <aqua>" + remMin + "m " + remSec + "s</aqua></gray>"));
                if (sender instanceof Player p) {
                    boolean isBypass = s.isBypassing(p);
                    sender.sendMessage(miniMessage.deserialize("<gray>Status Bypass Anda: " + (isBypass ? "<green>AKTIF (Permanent Build)</green>" : "<yellow>NONAKTIF (Tracked)</yellow>") + "</gray>"));
                }
            }
            case "restore" -> {
                if (s == null) {
                    sender.sendMessage(miniMessage.deserialize("<red>✖ Sistem Rekonstruksi Temporal Kerajaan Sions saat ini tidak aktif.</red>"));
                    return true;
                }
                sender.sendMessage(miniMessage.deserialize("<yellow>Menjalankan rekonstruksi manual untuk Kerajaan Sions...</yellow>"));
                int restored = s.restoreAll(true);
                sender.sendMessage(miniMessage.deserialize("<green>✔ Berhasil merekonstruksi <gold>" + restored + "</gold> block kembali ke wujud aslinya!</green>"));
            }
            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(miniMessage.deserialize("<red>Perintah ini hanya dapat digunakan oleh pemain in-game.</red>"));
                    return true;
                }
                if (s == null) {
                    player.sendMessage(miniMessage.deserialize("<red>✖ Sistem Rekonstruksi Temporal Kerajaan Sions saat ini tidak aktif.</red>"));
                    return true;
                }
                boolean nowBypassing = s.toggleBypass(player);
                if (nowBypassing) {
                    player.sendMessage(miniMessage.deserialize("<green>✔ Mode bypass <bold>AKTIF</bold>! Perubahan block Anda di Kerajaan Sions bersifat PERMANEN dan tidak akan di-rollback.</green>"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                } else {
                    player.sendMessage(miniMessage.deserialize("<yellow>Mode bypass <bold>NONAKTIF</bold>. Perubahan block Anda sekarang akan dicatat dan di-restore setiap jam.</yellow>"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
                }
            }
            case "tp" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(miniMessage.deserialize("<red>Perintah ini hanya dapat digunakan oleh pemain in-game.</red>"));
                    return true;
                }
                Optional<Region> sionsOpt = plugin.getRegionManager().getRegion("SIONS");
                if (sionsOpt.isPresent()) {
                    Optional<Location> spawnOpt = sionsOpt.get().getBukkitSpawnLocation();
                    if (spawnOpt.isPresent()) {
                        player.teleport(spawnOpt.get());
                        player.sendMessage(miniMessage.deserialize("<dark_purple>✦</dark_purple> <light_purple>Diteleportasi ke jantung rahasia Kerajaan Sions.</light_purple>"));
                        player.playSound(player.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.0f);
                        return true;
                    }
                }
                sender.sendMessage(miniMessage.deserialize("<red>Lokasi spawn Kerajaan Sions belum tersedia.</red>"));
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<gradient:#8e44ad:#9b59b6><bold>✦ BANTUAN PERINTAH /sions</bold></gradient>"));
        sender.sendMessage(miniMessage.deserialize("<gold>/sions status</gold> <gray>➔ Cek jumlah block terubah dan timer reset</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gold>/sions restore</gold> <gray>➔ Trigger rekonstruksi manual seketika</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gold>/sions bypass</gold> <gray>➔ Toggle mode edit permanen bagi admin</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gold>/sions tp</gold> <gray>➔ Teleport ke koordinat pusat Kerajaan Sions</gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("apexsions.admin")) return List.of();
        if (args.length == 1) {
            List<String> subs = List.of("status", "restore", "bypass", "tp");
            List<String> matches = new ArrayList<>();
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) {
                    matches.add(s);
                }
            }
            return matches;
        }
        return List.of();
    }
}
