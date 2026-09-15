package com.apexsions.core.claim;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.claim.gui.ClaimGUI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Command handler for /claim, /unclaim, /trust, /untrust, and /claiminfo.
 */
public class ClaimCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final ClaimManager claimManager;
    private final ClaimGUI claimGUI;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ClaimCommand(ApexsionsCorePlugin plugin, ClaimManager claimManager, ClaimGUI claimGUI) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.claimGUI = claimGUI;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String cmdName = command.getName().toLowerCase();

        // Shortcuts
        if (cmdName.equals("unclaim")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Hanya pemain yang dapat mengeksekusi perintah ini.");
                return true;
            }
            var res = claimManager.unclaimCurrentChunk(player);
            player.sendMessage(mm.deserialize(res.message()));
            return true;
        }

        if (cmdName.equals("trust")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Hanya pemain yang dapat mengeksekusi perintah ini.");
                return true;
            }
            if (args.length < 1) {
                player.sendMessage(mm.deserialize("<yellow>Penggunaan: /trust <nama_pemain></yellow>"));
                return true;
            }
            handleTrust(player, args[0]);
            return true;
        }

        if (cmdName.equals("untrust")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Hanya pemain yang dapat mengeksekusi perintah ini.");
                return true;
            }
            if (args.length < 1) {
                player.sendMessage(mm.deserialize("<yellow>Penggunaan: /untrust <nama_pemain></yellow>"));
                return true;
            }
            handleUntrust(player, args[0]);
            return true;
        }

        if (cmdName.equals("claiminfo")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Hanya pemain yang dapat mengeksekusi perintah ini.");
                return true;
            }
            handleInfo(player);
            return true;
        }

        // Main /claim command
        if (!(sender instanceof Player player)) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                claimManager.loadConfig();
                sender.sendMessage(mm.deserialize("<green>✔ Konfigurasi claims.yml berhasil dimuat ulang.</green>"));
                return true;
            }
            sender.sendMessage("Hanya pemain yang dapat menggunakan perintah /claim.");
            return true;
        }

        if (args.length == 0) {
            // Default /claim claims current chunk
            var res = claimManager.claimCurrentChunk(player);
            player.sendMessage(mm.deserialize(res.message()));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "gui", "menu" -> claimGUI.open(player);
            case "info" -> handleInfo(player);
            case "unclaim" -> {
                var res = claimManager.unclaimCurrentChunk(player);
                player.sendMessage(mm.deserialize(res.message()));
            }
            case "unclaimall" -> {
                var res = claimManager.unclaimAll(player);
                player.sendMessage(mm.deserialize(res.message()));
            }
            case "trust" -> {
                if (args.length < 2) {
                    player.sendMessage(mm.deserialize("<yellow>Penggunaan: /claim trust <nama_pemain></yellow>"));
                    return true;
                }
                handleTrust(player, args[1]);
            }
            case "untrust" -> {
                if (args.length < 2) {
                    player.sendMessage(mm.deserialize("<yellow>Penggunaan: /claim untrust <nama_pemain></yellow>"));
                    return true;
                }
                handleUntrust(player, args[1]);
            }
            case "list" -> handleList(player);
            case "reload" -> {
                if (!player.hasPermission("apexsions.admin")) {
                    player.sendMessage(mm.deserialize("<red>✖ Anda tidak memiliki izin untuk memuat ulang claims.</red>"));
                    return true;
                }
                claimManager.loadConfig();
                player.sendMessage(mm.deserialize("<green>✔ Konfigurasi claims.yml berhasil dimuat ulang.</green>"));
            }
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleInfo(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        Optional<ClaimChunk> claimOpt = claimManager.getClaimAt(player.getLocation());

        player.sendMessage(mm.deserialize("<gradient:#d4af37:#f39c12><bold>━━━━━━━━━━━━━━━ [ KEDAULATAN TANAH ] ━━━━━━━━━━━━━━━</bold></gradient>"));
        player.sendMessage(mm.deserialize("<gray>Dunia: </gray><white>" + chunk.getWorld().getName() + "</white> <gray>• Chunk: </gray><gold>[" + chunk.getX() + ", " + chunk.getZ() + "]</gold>"));

        if (claimOpt.isPresent()) {
            ClaimChunk c = claimOpt.get();
            player.sendMessage(mm.deserialize("<gray>Pemilik Tanah: </gray><gold><bold>" + c.getOwnerName() + "</bold></gold>"));
            Set<UUID> trusted = c.getTrustedPlayers();
            if (trusted.isEmpty()) {
                player.sendMessage(mm.deserialize("<gray>Warga Terpercaya: </gray><dark_gray>Tidak ada</dark_gray>"));
            } else {
                List<String> names = new ArrayList<>();
                for (UUID u : trusted) {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(u);
                    names.add(op.getName() != null ? op.getName() : u.toString().substring(0, 8));
                }
                player.sendMessage(mm.deserialize("<gray>Warga Terpercaya: </gray><green>" + String.join(", ", names) + "</green>"));
            }
        } else {
            player.sendMessage(mm.deserialize("<gray>Status Tanah: </gray><green>Publik / Wilderness (Belum Diklaim)</green>"));
            player.sendMessage(mm.deserialize("<gray>Ketik </gray><gold>/claim</gold><gray> untuk menguasai chunk ini.</gray>"));
        }

        claimManager.showChunkBoundary(player, chunk);
        player.sendMessage(mm.deserialize("<yellow>✨ Partikel debu emas menandai batas 16x16 chunk ini.</yellow>"));
        player.sendMessage(mm.deserialize("<gradient:#d4af37:#f39c12><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
    }

    private void handleTrust(Player player, String targetName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(mm.deserialize("<red>✖ Anda tidak perlu menambahkan izin trust pada diri Anda sendiri.</red>"));
            return;
        }
        var res = claimManager.trustPlayer(player, target.getUniqueId(), target.getName() != null ? target.getName() : targetName);
        player.sendMessage(mm.deserialize(res.message()));
    }

    private void handleUntrust(Player player, String targetName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        var res = claimManager.untrustPlayer(player, target.getUniqueId(), target.getName() != null ? target.getName() : targetName);
        player.sendMessage(mm.deserialize(res.message()));
    }

    private void handleList(Player player) {
        List<ClaimChunk> claims = claimManager.getClaimsByOwner(player.getUniqueId());
        int max = claimManager.getMaxClaims(player);

        player.sendMessage(mm.deserialize("<gradient:#d4af37:#f39c12><bold>━━━━━━ [ DAFTAR KLAIM TANAH ANDA (" + claims.size() + "/" + max + ") ] ━━━━━━</bold></gradient>"));
        if (claims.isEmpty()) {
            player.sendMessage(mm.deserialize("<gray>Anda belum memiliki tanah yang diklaim. Berdirilah di area bebas dan ketik <gold>/claim</gold>!</gray>"));
        } else {
            for (int i = 0; i < claims.size(); i++) {
                ClaimChunk c = claims.get(i);
                int blockX = (c.getChunkX() << 4) + 8;
                int blockZ = (c.getChunkZ() << 4) + 8;
                player.sendMessage(mm.deserialize("<gold>" + (i + 1) + ". </gold><white>" + c.getWorld() + "</white> <gray>• Chunk: </gray><yellow>[" + c.getChunkX() + ", " + c.getChunkZ() + "]</yellow> <dark_gray>(~X: " + blockX + ", Z: " + blockZ + ")</dark_gray>"));
            }
        }
        player.sendMessage(mm.deserialize("<gradient:#d4af37:#f39c12><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
    }

    private void sendHelp(Player player) {
        player.sendMessage(mm.deserialize("<gradient:#d4af37:#f39c12><bold>⚑ PANDUAN SISTEM KLAIM TANAH APEXSIONS</bold></gradient>"));
        player.sendMessage(mm.deserialize("<gold>/claim</gold> <gray>— Mengklaim chunk tanah tempat Anda berdiri.</gray>"));
        player.sendMessage(mm.deserialize("<gold>/claim gui</gold> <gray>— Membuka antarmuka menu manajemen tanah.</gray>"));
        player.sendMessage(mm.deserialize("<gold>/claim info</gold> <gray>— Melihat pemilik & memunculkan batas visual chunk.</gray>"));
        player.sendMessage(mm.deserialize("<gold>/claim trust <pemain></gold> <gray>— Memberikan izin bangun/buka peti pada teman.</gray>"));
        player.sendMessage(mm.deserialize("<gold>/claim untrust <pemain></gold> <gray>— Mencabut izin trust teman.</gray>"));
        player.sendMessage(mm.deserialize("<gold>/claim list</gold> <gray>— Menampilkan daftar koordinat seluruh tanah Anda.</gray>"));
        player.sendMessage(mm.deserialize("<gold>/unclaim</gold> <gray>— Melepas klaim chunk saat ini.</gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String cmdName = command.getName().toLowerCase();
        if (cmdName.equals("trust") || cmdName.equals("untrust")) {
            if (args.length == 1) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).toList();
            }
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subs = List.of("gui", "info", "trust", "untrust", "list", "unclaim", "unclaimall", "reload");
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("trust") || args[0].equalsIgnoreCase("untrust"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).toList();
        }

        return Collections.emptyList();
    }
}
