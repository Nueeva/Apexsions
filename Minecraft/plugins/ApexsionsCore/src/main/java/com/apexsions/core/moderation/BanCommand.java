package com.apexsions.core.moderation;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authoritative command executor for /ban, /tempban, /unban, /pardon, /banip, /unbanip, /checkban, and /banlist.
 */
public class BanCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private BanManager banManager;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private static final Pattern DURATION_PATTERN = Pattern.compile("(?i)(?:(\\d+)y)?(?:(\\d+)mo)?(?:(\\d+)w)?(?:(\\d+)d)?(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?");

    public BanCommand(ApexsionsCorePlugin plugin, BanManager banManager) {
        this.plugin = plugin;
        this.banManager = banManager != null ? banManager : (plugin != null ? plugin.getBanManager() : null);
    }

    private BanManager getBanManager() {
        if (this.banManager != null) return this.banManager;
        if (plugin != null) this.banManager = plugin.getBanManager();
        return this.banManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (this.banManager == null && plugin != null) this.banManager = plugin.getBanManager();

        String cmd = label.toLowerCase();

        switch (cmd) {
            case "ban" -> handleBan(sender, args, false);
            case "tempban" -> handleBan(sender, args, true);
            case "unban", "pardon" -> handleUnban(sender, args, false);
            case "banip" -> handleBanIp(sender, args);
            case "unbanip", "pardonip" -> handleUnban(sender, args, true);
            case "checkban", "baninfo" -> handleCheckBan(sender, args);
            case "banlist" -> handleBanList(sender, args);
            default -> sender.sendMessage(mm.deserialize("<red>Perintah sanksi tidak dikenal.</red>"));
        }
        return true;
    }

    private void handleBan(CommandSender sender, String[] args, boolean forceTemp) {
        if (!sender.hasPermission("apexsions.admin.ban") && !sender.hasPermission("apexsions.staff") && !sender.isOp()) {
            sender.sendMessage(mm.deserialize("<red>Anda tidak memiliki otoritas untuk menjatuhkan sanksi ban.</red>"));
            return;
        }

        if (args.length < 1 || (forceTemp && args.length < 2)) {
            if (forceTemp) {
                sender.sendMessage(mm.deserialize("<gold>Penggunaan:</gold> <yellow>/tempban <pemain> <durasi: 1d/7d/12h> [alasan...]</yellow>"));
            } else {
                sender.sendMessage(mm.deserialize("<gold>Penggunaan:</gold> <yellow>/ban <pemain> [durasi] [alasan...]</yellow>"));
            }
            return;
        }

        String targetName = args[0];
        Duration duration = null;
        String reason;
        int reasonStartIdx = 1;

        if (args.length >= 2) {
            Duration parsed = parseDuration(args[1]);
            if (parsed != null) {
                duration = parsed;
                reasonStartIdx = 2;
            } else if (forceTemp) {
                sender.sendMessage(mm.deserialize("<red>Format durasi tidak valid (contoh: 30m, 2h, 7d, 30d, 1y).</red>"));
                return;
            }
        }

        if (args.length > reasonStartIdx) {
            StringBuilder sb = new StringBuilder();
            for (int i = reasonStartIdx; i < args.length; i++) {
                if (!sb.isEmpty()) sb.append(" ");
                sb.append(args[i]);
            }
            reason = sb.toString();
        } else {
            reason = "Melanggar Regulasi & Ketertiban Apexsions.";
        }

        Player onlineTarget = Bukkit.getPlayerExact(targetName);
        UUID targetUuid;
        String ipAddress = null;

        if (onlineTarget != null) {
            targetUuid = onlineTarget.getUniqueId();
            targetName = onlineTarget.getName();
            if (onlineTarget.getAddress() != null) {
                ipAddress = onlineTarget.getAddress().getAddress().getHostAddress();
            }
        } else {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            targetUuid = offlineTarget.getUniqueId();
            targetName = offlineTarget.getName() != null ? offlineTarget.getName() : targetName;
        }

        String staffName = sender instanceof Player p ? p.getName() : "Console";
        final String finalTargetName = targetName;

        banManager.ban(targetUuid, targetName, ipAddress, staffName, reason, duration, BanRecord.BanType.NAME)
                .thenAccept(record -> {
                    String durStr = record.isPermanent() ? "permanen" : record.getTimeRemainingFormatted();
                    sender.sendMessage(mm.deserialize("<green>[Apexsions] Pemain <yellow>" + finalTargetName +
                            "</yellow> berhasil di-ban (<aqua>" + durStr + "</aqua>).</green>"));
                });
    }

    private void handleBanIp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("apexsions.admin.banip") && !sender.hasPermission("apexsions.staff") && !sender.isOp()) {
            sender.sendMessage(mm.deserialize("<red>Anda tidak memiliki otoritas untuk menjatuhkan IP ban.</red>"));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(mm.deserialize("<gold>Penggunaan:</gold> <yellow>/banip <pemain|alamat-ip> [durasi] [alasan...]</yellow>"));
            return;
        }

        String input = args[0];
        String ipAddress;
        UUID playerUuid = UUID.randomUUID();
        String playerName = input;

        if (input.contains(".")) {
            ipAddress = input;
        } else {
            Player onlineTarget = Bukkit.getPlayerExact(input);
            if (onlineTarget != null && onlineTarget.getAddress() != null) {
                ipAddress = onlineTarget.getAddress().getAddress().getHostAddress();
                playerUuid = onlineTarget.getUniqueId();
                playerName = onlineTarget.getName();
            } else {
                sender.sendMessage(mm.deserialize("<red>Pemain sedang offline dan IP tidak ditemukan. Masukkan IP langsung (contoh: /banip 1.2.3.4).</red>"));
                return;
            }
        }

        Duration duration = null;
        int reasonStartIdx = 1;
        if (args.length >= 2) {
            Duration parsed = parseDuration(args[1]);
            if (parsed != null) {
                duration = parsed;
                reasonStartIdx = 2;
            }
        }

        String reason = "Pelanggaran Berat Alamat Jaringan (IP Ban).";
        if (args.length > reasonStartIdx) {
            StringBuilder sb = new StringBuilder();
            for (int i = reasonStartIdx; i < args.length; i++) {
                if (!sb.isEmpty()) sb.append(" ");
                sb.append(args[i]);
            }
            reason = sb.toString();
        }

        String staffName = sender instanceof Player p ? p.getName() : "Console";
        final String finalIpAddress = ipAddress;
        final String finalPlayerName = playerName;

        banManager.ban(playerUuid, playerName, ipAddress, staffName, reason, duration, BanRecord.BanType.IP)
                .thenAccept(record -> {
                    sender.sendMessage(mm.deserialize("<green>[Apexsions] Alamat IP <yellow>" + finalIpAddress +
                            "</yellow> (" + finalPlayerName + ") berhasil di-ban jaringan.</green>"));
                });
    }

    private void handleUnban(CommandSender sender, String[] args, boolean isIp) {
        if (!sender.hasPermission("apexsions.admin.unban") && !sender.hasPermission("apexsions.staff") && !sender.isOp()) {
            sender.sendMessage(mm.deserialize("<red>Anda tidak memiliki otoritas untuk mencabut sanksi ban.</red>"));
            return;
        }

        if (args.length < 1) {
            String usage = isIp ? "/unbanip <alamat-ip>" : "/unban <pemain|uuid> [alasan...]";
            sender.sendMessage(mm.deserialize("<gold>Penggunaan:</gold> <yellow>" + usage + "</yellow>"));
            return;
        }

        String target = args[0];
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Pencabutan sanksi resmi.";
        String staffName = sender instanceof Player p ? p.getName() : "Console";

        banManager.unban(target, staffName, reason).thenAccept(success -> {
            if (success) {
                sender.sendMessage(mm.deserialize("<green>[Apexsions] Sanksi ban untuk <yellow>" + target + "</yellow> berhasil dicabut.</green>"));
            } else {
                sender.sendMessage(mm.deserialize("<yellow>[Apexsions] Tidak ditemukan catatan ban aktif untuk <white>" + target + "</white> (Pemberian ampunan native tetap dieksekusi).</yellow>"));
            }
        });
    }

    private void handleCheckBan(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(mm.deserialize("<gold>Penggunaan:</gold> <yellow>/checkban <pemain|uuid></yellow>"));
            return;
        }

        String target = args[0];
        OfflinePlayer offline = Bukkit.getOfflinePlayer(target);
        BanRecord ban = banManager.getActiveBan(offline.getUniqueId());

        if (ban == null) {
            sender.sendMessage(mm.deserialize("<green>[Apexsions] Pemain <yellow>" + target + "</yellow> TIDAK sedang di-ban.</green>"));
            return;
        }

        sender.sendMessage(mm.deserialize("<gold>━━━ Info Sanksi: <yellow>" + ban.getPlayerName() + "</yellow> ━━━</gold>"));
        sender.sendMessage(mm.deserialize("<gray>Alasan:</gray> <white>" + ban.getReason() + "</white>"));
        sender.sendMessage(mm.deserialize("<gray>Oleh:</gray> <gold>" + ban.getBannedBy() + "</gold>"));
        sender.sendMessage(mm.deserialize("<gray>Tipe:</gray> <aqua>" + ban.getBanType() + "</aqua>"));
        sender.sendMessage(mm.deserialize("<gray>Sisa Waktu:</gray> <red>" + ban.getTimeRemainingFormatted() + "</red>"));
    }

    private void handleBanList(CommandSender sender, String[] args) {
        Map<UUID, BanRecord> bans = banManager.getActiveBans();
        if (bans.isEmpty()) {
            sender.sendMessage(mm.deserialize("<green>[Apexsions] Tidak ada pemain yang sedang di-ban aktif saat ini.</green>"));
            return;
        }

        sender.sendMessage(mm.deserialize("<gold>━━━ Daftar Sanksi Ban Aktif (" + bans.size() + ") ━━━</gold>"));
        int shown = 0;
        for (BanRecord b : bans.values()) {
            if (b.isActive()) {
                String durStr = b.isPermanent() ? "<red>PERMANEN</red>" : "<aqua>" + b.getTimeRemainingFormatted() + "</aqua>";
                sender.sendMessage(mm.deserialize("<gray>•</gray> <yellow>" + b.getPlayerName() + "</yellow> <gray>(" + durStr + ")</gray> — <white>" + b.getReason() + "</white>"));
                shown++;
                if (shown >= 15) {
                    sender.sendMessage(mm.deserialize("<gray>... dan " + (bans.size() - shown) + " sanksi lainnya.</gray>"));
                    break;
                }
            }
        }
    }

    public static Duration parseDuration(String input) {
        if (input == null || input.isBlank()) return null;
        Matcher m = DURATION_PATTERN.matcher(input);
        if (!m.matches()) return null;

        long seconds = 0;
        if (m.group(1) != null) seconds += Long.parseLong(m.group(1)) * 365L * 86400L;
        if (m.group(2) != null) seconds += Long.parseLong(m.group(2)) * 30L * 86400L;
        if (m.group(3) != null) seconds += Long.parseLong(m.group(3)) * 7L * 86400L;
        if (m.group(4) != null) seconds += Long.parseLong(m.group(4)) * 86400L;
        if (m.group(5) != null) seconds += Long.parseLong(m.group(5)) * 3600L;
        if (m.group(6) != null) seconds += Long.parseLong(m.group(6)) * 60L;
        if (m.group(7) != null) seconds += Long.parseLong(m.group(7));

        return seconds > 0 ? Duration.ofSeconds(seconds) : null;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (this.banManager == null && plugin != null) this.banManager = plugin.getBanManager();

        String cmd = label.toLowerCase();
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            if (cmd.equals("unban") || cmd.equals("pardon")) {
                for (BanRecord b : banManager.getActiveBans().values()) {
                    if (b.getPlayerName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        list.add(b.getPlayerName());
                    }
                }
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        list.add(p.getName());
                    }
                }
            }
            return list;
        }
        if (args.length == 2 && (cmd.equals("ban") || cmd.equals("tempban") || cmd.equals("banip"))) {
            return List.of("1h", "12h", "1d", "3d", "7d", "30d", "1y");
        }
        return Collections.emptyList();
    }
}
