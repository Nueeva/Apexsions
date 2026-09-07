package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.region.Region;
import com.apexsions.core.sions.SionsTemporalService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
        if (s == null) {
            sender.sendMessage(miniMessage.deserialize("<red>✖ Sistem Rekonstruksi Temporal Kerajaan Sions saat ini tidak aktif.</red>"));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "status" -> {
                int count = s.getModifiedBlockCount();
                long remainingSec = s.getRemainingSeconds();
                long remMin = remainingSec / 60;
                long remSec = remainingSec % 60;

                sender.sendMessage(miniMessage.deserialize("<gradient:#8e44ad:#9b59b6><bold>✦ KERAJAAN SIONS — STATUS REKONSTRUKSI TEMPORAL</bold></gradient>"));
                sender.sendMessage(miniMessage.deserialize("<gray>Sistem Temporal: <green>" + (s.isEnabled() ? "AKTIF" : "NONAKTIF") + "</green></gray>"));
                sender.sendMessage(miniMessage.deserialize("<gray>Interval Reset: <gold>" + s.getIntervalMinutes() + " Menit (per jam)</gold></gray>"));
                sender.sendMessage(miniMessage.deserialize("<gray>Block Terubah Aktif: <yellow><bold>" + count + "</bold></yellow> block</gray>"));

                if (s.hasBaseline()) {
                    sender.sendMessage(miniMessage.deserialize("<gray>Baseline Snapshot: <green>TERSIMPAN (<gold>" + s.getBaselineBlockCount() + "</gold> block)</green></gray>"));
                } else {
                    sender.sendMessage(miniMessage.deserialize("<gray>Baseline Snapshot: <red>BELUM DISNAPSHOT</red> <dark_gray>(Gunakan /sions set untuk mengunci kondisi awal)</dark_gray></gray>"));
                }

                sender.sendMessage(miniMessage.deserialize("<gray>Peti Kuno Terkunci: <green>" + (s.isContainerLockEnabled() ? "AKTIF (Butuh Sions Key)" : "NONAKTIF") + "</green></gray>"));
                sender.sendMessage(miniMessage.deserialize("<gray>Sisa Waktu Menuju Reset: <aqua>" + remMin + "m " + remSec + "s</aqua></gray>"));

                if (sender instanceof Player p) {
                    boolean isBypass = s.isBypassing(p);
                    sender.sendMessage(miniMessage.deserialize("<gray>Status Bypass Anda: " + (isBypass ? "<green>AKTIF (Permanent Build)</green>" : "<yellow>NONAKTIF (Tracked)</yellow>") + "</gray>"));
                }
            }
            case "set", "snapshot" -> {
                int defaultMinY = plugin.getConfig().getInt("sions-temporal.scan.default-min-y", 50);
                int defaultMaxY = plugin.getConfig().getInt("sions-temporal.scan.default-max-y", 200);

                int minY = defaultMinY;
                int maxY = defaultMaxY;

                if (args.length >= 3) {
                    try {
                        minY = Integer.parseInt(args[1]);
                        maxY = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(miniMessage.deserialize("<red>Nilai ketinggian minY dan maxY harus berupa angka integer!</red>"));
                        return true;
                    }
                }

                if (minY > maxY) {
                    int temp = minY;
                    minY = maxY;
                    maxY = temp;
                }

                final int fMinY = minY;
                final int fMaxY = maxY;

                sender.sendMessage(miniMessage.deserialize("<yellow>Memulai pemindaian dan pembuatan baseline Kerajaan Sions (Y: <gold>" + fMinY + "</gold> s/d <gold>" + fMaxY + "</gold>)... Harap tunggu sebentar.</yellow>"));

                s.saveBaseline(fMinY, fMaxY, savedCount -> {
                    sender.sendMessage(miniMessage.deserialize("<green>✔ Berhasil mengunci dan menyimpan <gold>" + savedCount + "</gold> block sebagai baseline permanen Kerajaan Sions!</green>"));
                    if (sender instanceof Player p) {
                        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
                    }
                });
            }
            case "restore" -> {
                sender.sendMessage(miniMessage.deserialize("<yellow>Menjalankan rekonstruksi manual untuk Kerajaan Sions...</yellow>"));
                int restored = s.restoreAll(true);
                sender.sendMessage(miniMessage.deserialize("<green>✔ Berhasil merekonstruksi <gold>" + restored + "</gold> block kembali ke wujud aslinya!</green>"));
            }
            case "setkey" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(miniMessage.deserialize("<red>Perintah ini hanya dapat digunakan oleh pemain in-game.</red>"));
                    return true;
                }
                boolean success = s.setKeyItemFromHand(player);
                if (success) {
                    player.sendMessage(miniMessage.deserialize("<green>✔ Item di tangan utama berhasil ditetapkan sebagai kunci resmi pembuka peti Kerajaan Sions!</green>"));
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.5f);
                } else {
                    player.sendMessage(miniMessage.deserialize("<red>Silakan pegang item yang ingin dijadikan kunci resmi di tangan utama Anda.</red>"));
                }
            }
            case "givekey" -> {
                Player target = (sender instanceof Player p) ? p : null;
                int amount = 1;

                if (args.length >= 2) {
                    Player candidate = Bukkit.getPlayer(args[1]);
                    if (candidate != null) {
                        target = candidate;
                    } else {
                        try {
                            amount = Integer.parseInt(args[1]);
                        } catch (NumberFormatException ignored) {}
                    }
                }

                if (args.length >= 3) {
                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (NumberFormatException ignored) {}
                }

                if (target == null) {
                    sender.sendMessage(miniMessage.deserialize("<red>Tentukan nama pemain target: /sions givekey <player> [amount]</red>"));
                    return true;
                }

                ItemStack keyItem = s.createKeyItem(amount);
                target.getInventory().addItem(keyItem);
                target.sendMessage(miniMessage.deserialize("<green>✔ Anda telah menerima <gold>" + amount + "x</gold> <gradient:#8e44ad:#d4af37><bold>Sions Ancient Key</bold></gradient>!</green>"));
                target.playSound(target.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);

                if (!target.equals(sender)) {
                    sender.sendMessage(miniMessage.deserialize("<green>✔ Berhasil memberikan <gold>" + amount + "x</gold> Sions Ancient Key kepada <aqua>" + target.getName() + "</aqua>.</green>"));
                }
            }
            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(miniMessage.deserialize("<red>Perintah ini hanya dapat digunakan oleh pemain in-game.</red>"));
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
        sender.sendMessage(miniMessage.deserialize("<gradient:#8e44ad:#9b59b6><bold>✦ BANTUAN PERINTAH /sions (ADMIN)</bold></gradient>"));
        sender.sendMessage(miniMessage.deserialize("<gold>/sions set [minY] [maxY]</gold> <gray>➔ Kunci kondisi dunia saat ini sebagai baseline permanen</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gold>/sions status</gold> <gray>➔ Cek jumlah block terubah, baseline, dan timer reset</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gold>/sions restore</gold> <gray>➔ Trigger rekonstruksi manual seketika</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gold>/sions setkey</gold> <gray>➔ Jadikan item di tangan sebagai kunci pembuka peti Sions</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gold>/sions givekey [player] [qty]</gold> <gray>➔ Berikan Sions Ancient Key untuk reward/testing</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gold>/sions bypass</gold> <gray>➔ Toggle mode edit permanen bagi arsitek/admin</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gold>/sions tp</gold> <gray>➔ Teleport ke koordinat pusat Kerajaan Sions</gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("apexsions.admin")) return List.of();
        if (args.length == 1) {
            List<String> subs = List.of("status", "restore", "set", "snapshot", "setkey", "givekey", "bypass", "tp");
            List<String> matches = new ArrayList<>();
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) {
                    matches.add(s);
                }
            }
            return matches;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("givekey")) {
            List<String> players = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    players.add(p.getName());
                }
            }
            return players;
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("snapshot"))) {
            return List.of("50", "40", "0", "-64");
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("snapshot"))) {
            return List.of("200", "256", "320");
        }
        return List.of();
    }
}
