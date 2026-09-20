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
 * Enhanced command handler for /claim, /unclaim, /trust, /untrust, and tax/role/flag subcommands.
 */
public class ClaimCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private ClaimManager claimManager;
    private ClaimGUI claimGUI;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ClaimCommand(ApexsionsCorePlugin plugin, ClaimManager claimManager, ClaimGUI claimGUI) {
        this.plugin = plugin;
        this.claimManager = claimManager != null ? claimManager : (plugin != null ? plugin.getClaimManager() : null);
        this.claimGUI = claimGUI != null ? claimGUI : (plugin != null ? plugin.getClaimGUI() : null);
    }

    private ClaimManager getClaimManager() {
        if (this.claimManager != null) return this.claimManager;
        if (plugin != null) this.claimManager = plugin.getClaimManager();
        return this.claimManager;
    }

    private ClaimGUI getClaimGUI() {
        if (this.claimGUI != null) return this.claimGUI;
        if (plugin != null) this.claimGUI = plugin.getClaimGUI();
        return this.claimGUI;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (this.claimManager == null && plugin != null) this.claimManager = plugin.getClaimManager();
        if (this.claimGUI == null && plugin != null) this.claimGUI = plugin.getClaimGUI();

        String cmdName = command.getName().toLowerCase();

        // Shortcuts
        if (cmdName.equals("unclaim")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Hanya pemain yang dapat mengeksekusi perintah ini.");
                return true;
            }
            if (args.length >= 2) {
                try {
                    int cx = Integer.parseInt(args[0]);
                    int cz = Integer.parseInt(args[1]);
                    String worldName = (args.length >= 3) ? args[2] : player.getWorld().getName();
                    var res = getClaimManager().unclaimChunk(player, worldName, cx, cz);
                    player.sendMessage(mm.deserialize(res.message()));
                    return true;
                } catch (NumberFormatException ignored) {}
            }
            var res = getClaimManager().unclaimCurrentChunk(player);
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

        // Admin subcommands for Console and In-game Staff
        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            if (!sender.hasPermission("apexsions.admin") && !sender.isOp()) {
                sender.sendMessage(mm.deserialize("<red>✖ Anda tidak memiliki izin untuk administrasi klaim tanah.</red>"));
                return true;
            }
            handleAdminSubcommand(sender, args);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("apexsions.admin") && !sender.isOp()) {
                sender.sendMessage(mm.deserialize("<red>✖ Anda tidak memiliki izin untuk memuat ulang claims.</red>"));
                return true;
            }
            claimManager.loadConfig();
            sender.sendMessage(mm.deserialize("<green>✔ Konfigurasi claims.yml berhasil dimuat ulang.</green>"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Hanya pemain yang dapat menggunakan perintah interaktif /claim.");
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
            case "gui", "menu" -> getClaimGUI().open(player);
            case "info" -> handleInfo(player);
            case "home", "tp" -> handleHome(player, args);
            case "unclaim" -> {
                if (args.length >= 3) {
                    try {
                        int cx = Integer.parseInt(args[1]);
                        int cz = Integer.parseInt(args[2]);
                        String worldName = (args.length >= 4) ? args[3] : player.getWorld().getName();
                        var res = getClaimManager().unclaimChunk(player, worldName, cx, cz);
                        player.sendMessage(mm.deserialize(res.message()));
                        return true;
                    } catch (NumberFormatException ignored) {}
                }
                var res = getClaimManager().unclaimCurrentChunk(player);
                player.sendMessage(mm.deserialize(res.message()));
            }
            case "unclaimall" -> {
                var res = getClaimManager().unclaimAll(player);
                player.sendMessage(mm.deserialize(res.message()));
            }
            case "deposit" -> {
                if (args.length < 2) {
                    player.sendMessage(mm.deserialize("<yellow>Penggunaan: /claim deposit <nominal></yellow>"));
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[1]);
                    var res = claimManager.depositBank(player, amount);
                    player.sendMessage(mm.deserialize(res.message()));
                } catch (NumberFormatException e) {
                    player.sendMessage(mm.deserialize("<red>Nominal harus berupa angka valid!</red>"));
                }
            }
            case "withdraw" -> {
                if (args.length < 2) {
                    player.sendMessage(mm.deserialize("<yellow>Penggunaan: /claim withdraw <nominal></yellow>"));
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[1]);
                    var res = claimManager.withdrawBank(player, amount);
                    player.sendMessage(mm.deserialize(res.message()));
                } catch (NumberFormatException e) {
                    player.sendMessage(mm.deserialize("<red>Nominal harus berupa angka valid!</red>"));
                }
            }
            case "bank", "tax" -> handleBankInfo(player);
            case "flag" -> {
                if (args.length < 3) {
                    player.sendMessage(mm.deserialize("<yellow>Penggunaan: /claim flag <pvp|mob_spawn|fire_spread|explosions|greeting|farewell> <nilai></yellow>"));
                    return true;
                }
                String flagKey = args[1].toLowerCase();
                String flagVal = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                var res = claimManager.setFlag(player, flagKey, flagVal);
                player.sendMessage(mm.deserialize(res.message()));
            }
            case "role" -> {
                if (args.length < 3) {
                    player.sendMessage(mm.deserialize("<yellow>Penggunaan: /claim role <nama_pemain> <manager|builder|visitor|remove></yellow>"));
                    return true;
                }
                handleRole(player, args[1], args[2]);
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
            case "name", "rename", "setname" -> handleSetName(player, args);
            case "radius" -> handleRadius(player, args);
            case "outpost" -> handleOutpost(player, args);
            case "border", "visualizer", "view" -> handleBorder(player);
            case "list" -> handleList(player);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleBorder(Player player) {
        getClaimManager().showChunkBoundary(player, player.getLocation().getChunk());
        player.sendMessage(mm.deserialize("<gold>✨ <b>[BATAS WILAYAH]</b> Memancarkan 4 tiang suar sudut dan dinding energi 16x16 di sekeliling Anda!</gold>"));
    }

    private void handleOutpost(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(mm.deserialize("<yellow>Penggunaan: /claim outpost <set|remove></yellow> <gray>(Tetapkan petak saat ini sebagai Pos Depan / Diskon Pajak 50%)</gray>"));
            return;
        }

        String sub = args[1].toLowerCase();
        if (sub.equals("set") || sub.equals("enable") || sub.equals("add")) {
            var res = getClaimManager().setOutpostCurrentChunk(player, true);
            player.sendMessage(mm.deserialize(res.message()));
        } else if (sub.equals("remove") || sub.equals("disable") || sub.equals("unset") || sub.equals("clear")) {
            var res = getClaimManager().setOutpostCurrentChunk(player, false);
            player.sendMessage(mm.deserialize(res.message()));
        } else {
            player.sendMessage(mm.deserialize("<yellow>Penggunaan: /claim outpost <set|remove></yellow>"));
        }
    }

    private void handleSetName(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(mm.deserialize("<yellow>Penggunaan: /claim name <Nama Baru></yellow> <gray>(atau: /claim name <chunkX> <chunkZ> <Nama>)</gray>"));
            return;
        }

        // Format: /claim name <chunkX> <chunkZ> <Nama...>
        if (args.length >= 4) {
            try {
                int cx = Integer.parseInt(args[1]);
                int cz = Integer.parseInt(args[2]);
                String name = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                var res = getClaimManager().setClaimName(player, player.getWorld().getName(), cx, cz, name);
                player.sendMessage(mm.deserialize(res.message()));
                return;
            } catch (NumberFormatException ignored) {}
        }

        // Format: /claim name <Nama Baru...> on current standing chunk
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        var res = getClaimManager().setClaimNameCurrentChunk(player, name);
        player.sendMessage(mm.deserialize(res.message()));
    }

    private void handleRadius(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(mm.deserialize("<yellow>Penggunaan: /claim radius <1|2></yellow> <gray>(1 = 3x3 petak, 2 = 5x5 petak)</gray>"));
            return;
        }
        try {
            int radius = Integer.parseInt(args[1]);
            var res = getClaimManager().claimRadius(player, radius);
            player.sendMessage(mm.deserialize(res.message()));
        } catch (NumberFormatException e) {
            player.sendMessage(mm.deserialize("<red>Radius harus berupa angka 1 atau 2!</red>"));
        }
    }

    private void handleRole(Player player, String targetName, String roleStr) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        ClaimRole role = roleStr.equalsIgnoreCase("remove") ? ClaimRole.VISITOR : ClaimRole.fromString(roleStr);
        var res = claimManager.setRole(player, target.getUniqueId(), targetName, role);
        player.sendMessage(mm.deserialize(res.message()));
    }

    private void handleBankInfo(Player player) {
        List<ClaimChunk> claims = claimManager.getClaimsByOwner(player.getUniqueId());
        if (claims.isEmpty()) {
            player.sendMessage(mm.deserialize("<yellow>⚠ Anda belum memiliki wilayah klaim tanah.</yellow>"));
            return;
        }

        double totalVaulted = 0.0;
        double dailyTaxTotal = claimManager.calculateTotalDailyTax(player.getUniqueId());
        double perChunkTax = claimManager.calculateChunkDailyTax(player.getUniqueId());

        boolean anyInGrace = false;
        long minGraceTime = Long.MAX_VALUE;

        for (ClaimChunk c : claims) {
            totalVaulted += c.getBankBalance();
            if (c.isInGracePeriod()) {
                anyInGrace = true;
                if (c.getGracePeriodUntil() < minGraceTime) minGraceTime = c.getGracePeriodUntil();
            }
        }

        double daysRemaining = dailyTaxTotal > 0 ? (totalVaulted / dailyTaxTotal) : 999.0;

        player.sendMessage(mm.deserialize("<gradient:#ffd700:#ffa500><bold>━━━━━━━━━━━━━ [ BRANKAS PAJAK WILAYAH ] ━━━━━━━━━━━━━</bold></gradient>"));
        player.sendMessage(mm.deserialize("<gray>Total Wilayah: </gray><gold>" + claims.size() + " chunks</gold>"));
        player.sendMessage(mm.deserialize("<gray>Tarif Pajak: </gray><yellow>Rp" + String.format("%,.0f", perChunkTax) + "/chunk/hari</yellow> (Progresif)"));
        player.sendMessage(mm.deserialize("<gray>Total Tagihan: </gray><red>Rp" + String.format("%,.0f", dailyTaxTotal) + "/hari</red> (50% Kas Kerajaan)"));
        player.sendMessage(mm.deserialize("<gray>Saldo Brankas: </gray><green><b>Rp" + String.format("%,.0f", totalVaulted) + "</b></green>"));
        player.sendMessage(mm.deserialize("<gray>Estimasi Bertahan: </gray><aqua>" + String.format("%.1f", daysRemaining) + " hari</aqua>"));

        if (anyInGrace) {
            long remainingMs = minGraceTime - System.currentTimeMillis();
            long hours = Math.max(0, remainingMs / (3600 * 1000L));
            player.sendMessage(mm.deserialize("<dark_red><b>⚠ STATUS: MENUNGGAK PAJAK!</b> Sisa waktu masa tenggang: <b>" + hours + " jam</b> sebelum penyitaan!</dark_red>"));
        } else {
            player.sendMessage(mm.deserialize("<green>✔ Status Pembayaran: <b>LUNAS & AKTIF</b></green>"));
        }
        player.sendMessage(mm.deserialize("<gray>Setor saldo: <yellow>/claim deposit <nominal></yellow> • Tarik: <yellow>/claim withdraw <nominal></yellow></gray>"));
        player.sendMessage(mm.deserialize("<dark_gray><i>💡 Auto-Debet Aktif: Jika saldo brankas kosong, biaya sewa otomatis dipotong dari dompet pribadi (/bal).</i></dark_gray>"));
    }

    private void handleAdminSubcommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(mm.deserialize("<gold>Penggunaan Admin:</gold> <yellow>/claim admin <unclaim|unclaimall|sync|collecttax|deposit></yellow>"));
            return;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "unclaim" -> {
                if (args.length < 5) {
                    sender.sendMessage(mm.deserialize("<yellow>Penggunaan: /claim admin unclaim <world> <chunkX> <chunkZ></yellow>"));
                    return;
                }
                String world = args[2];
                try {
                    int cx = Integer.parseInt(args[3]);
                    int cz = Integer.parseInt(args[4]);
                    boolean ok = claimManager.forceUnclaimChunk(world, cx, cz);
                    if (ok) {
                        sender.sendMessage(mm.deserialize("<green>✔ Berhasil melepas klaim tanah chunk [" + cx + ", " + cz + "] di dunia " + world + ".</green>"));
                    } else {
                        sender.sendMessage(mm.deserialize("<yellow>⚠ Tidak ada klaim aktif pada chunk tersebut.</yellow>"));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(mm.deserialize("<red>Koordinat chunk harus berupa angka integer.</red>"));
                }
            }
            case "unclaimall" -> {
                if (args.length < 3) {
                    sender.sendMessage(mm.deserialize("<yellow>Penggunaan: /claim admin unclaimall <nama_pemain></yellow>"));
                    return;
                }
                String target = args[2];
                OfflinePlayer off = Bukkit.getOfflinePlayer(target);
                int count = claimManager.forceUnclaimAll(off.getUniqueId());
                sender.sendMessage(mm.deserialize("<green>✔ Berhasil melepas seluruh (" + count + ") klaim tanah milik " + target + ".</green>"));
            }
            case "sync" -> {
                if (plugin.getWebBridgeService() != null) {
                    plugin.getWebBridgeService().syncClaimsAsync(claimManager.getAllClaims());
                    sender.sendMessage(mm.deserialize("<green>✔ Sinkronisasi seluruh klaim tanah (" + claimManager.getAllClaims().size() + ") ke Web Platform sedang dikirim...</green>"));
                } else {
                    sender.sendMessage(mm.deserialize("<red>WebBridgeService tidak aktif.</red>"));
                }
            }
            case "collecttax" -> {
                claimManager.processTaxCollectionCycle();
                sender.sendMessage(mm.deserialize("<green>✔ Siklus penagihan pajak dan pemeriksaan masa tenggang wilayah berhasil dieksekusi.</green>"));
            }
            case "deposit" -> {
                if (args.length < 4) {
                    sender.sendMessage(mm.deserialize("<yellow>Penggunaan: /claim admin deposit <nama_pemain> <nominal></yellow>"));
                    return;
                }
                String targetName = args[2];
                try {
                    double amount = Double.parseDouble(args[3]);
                    OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                    List<ClaimChunk> targetClaims = claimManager.getClaimsByOwner(target.getUniqueId());
                    if (targetClaims.isEmpty()) {
                        sender.sendMessage(mm.deserialize("<yellow>⚠ Pemain " + targetName + " tidak memiliki klaim tanah.</yellow>"));
                        return;
                    }
                    double perChunk = amount / targetClaims.size();
                    for (ClaimChunk c : targetClaims) {
                        c.deposit(perChunk);
                        plugin.getClaimRepository().updateClaimFinancials(c);
                    }
                    if (plugin.getWebBridgeService() != null) {
                        plugin.getWebBridgeService().syncClaimsAsync(claimManager.getAllClaims());
                    }
                    sender.sendMessage(mm.deserialize("<green>✔ Berhasil menyuntikkan saldo deposit Rp" + String.format("%,.0f", amount) + " ke brankas klaim " + targetName + ".</green>"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(mm.deserialize("<red>Nominal harus berupa angka valid!</red>"));
                }
            }
            default -> sender.sendMessage(mm.deserialize("<red>Aksi admin klaim tidak valid. Pilihan: unclaim, unclaimall, sync, collecttax, deposit.</red>"));
        }
    }

    private void handleInfo(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        Optional<ClaimChunk> claimOpt = claimManager.getClaimAt(player.getLocation());

        player.sendMessage(mm.deserialize("<gradient:#d4af37:#f39c12><bold>━━━━━━━━━━━━━━━ [ KEDAULATAN TANAH ] ━━━━━━━━━━━━━━━</bold></gradient>"));
        player.sendMessage(mm.deserialize("<gray>Dunia: </gray><white>" + chunk.getWorld().getName() + "</white> <gray>• Chunk: </gray><gold>[" + chunk.getX() + ", " + chunk.getZ() + "]</gold>"));

        if (claimOpt.isPresent()) {
            ClaimChunk c = claimOpt.get();
            player.sendMessage(mm.deserialize("<gray>Pemilik Tanah: </gray><gold><bold>" + c.getOwnerName() + "</bold></gold>"));
            player.sendMessage(mm.deserialize("<gray>Status: </gray>" + c.getStatus().getBadge()));
            player.sendMessage(mm.deserialize("<gray>Saldo Chunk: </gray><green>Rp" + String.format("%,.0f", c.getBankBalance()) + "</green> • Pajak: <yellow>Rp" + String.format("%,.0f", c.getDailyUpkeep()) + "/hari</yellow>"));

            var roles = c.getMemberRoles();
            if (roles.isEmpty()) {
                player.sendMessage(mm.deserialize("<gray>Warga Terdaftar: </gray><dark_gray>Tidak ada</dark_gray>"));
            } else {
                List<String> roleDisplays = new ArrayList<>();
                for (var entry : roles.entrySet()) {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(entry.getKey());
                    String name = op.getName() != null ? op.getName() : entry.getKey().toString().substring(0, 8);
                    roleDisplays.add(name + " (" + entry.getValue().name() + ")");
                }
                player.sendMessage(mm.deserialize("<gray>Warga: </gray><aqua>" + String.join(", ", roleDisplays) + "</aqua>"));
            }

            player.sendMessage(mm.deserialize("<gray>Flags: </gray><yellow>PvP=" + c.getFlag("pvp", "false") + ", MobSpawn=" + c.getFlag("mob_spawn", "false") + ", Api=" + c.getFlag("fire_spread", "false") + "</yellow>"));
        } else {
            player.sendMessage(mm.deserialize("<gray>Status Wilayah: </gray><green>Alam Liar (Belum Diklaim)</green>"));
            player.sendMessage(mm.deserialize("<gray>Ketik <yellow>/claim</yellow> untuk mengamankan tanah ini.</gray>"));
        }
    }

    private void handleHome(Player player, String[] args) {
        List<ClaimChunk> claims = getClaimManager().getClaimsByOwner(player.getUniqueId());
        if (claims.isEmpty()) {
            player.sendMessage(mm.deserialize("<yellow>⚠ Anda belum memiliki wilayah klaim tanah. Ketik <gold>/claim</gold> di tempat yang ingin Anda amankan.</yellow>"));
            return;
        }

        // /claim tp <chunkX> <chunkZ>
        if (args.length >= 3) {
            try {
                int cx = Integer.parseInt(args[1]);
                int cz = Integer.parseInt(args[2]);
                Optional<ClaimChunk> target = claims.stream().filter(c -> c.getChunkX() == cx && c.getChunkZ() == cz).findFirst();
                if (target.isPresent()) {
                    getClaimManager().teleportToClaim(player, target.get());
                    return;
                } else {
                    player.sendMessage(mm.deserialize("<red>✖ Anda tidak memiliki klaim pada chunk [" + cx + ", " + cz + "].</red>"));
                    return;
                }
            } catch (NumberFormatException ignored) {}
        }

        // /claim home <nomor>
        int index = 0;
        if (args.length >= 2) {
            try {
                int requested = Integer.parseInt(args[1]);
                if (requested >= 1 && requested <= claims.size()) {
                    index = requested - 1;
                } else {
                    player.sendMessage(mm.deserialize("<yellow>Pilihan petak tidak valid. Masukkan nomor antara 1 sampai " + claims.size() + ".</yellow>"));
                    return;
                }
            } catch (NumberFormatException ignored) {}
        }

        ClaimChunk target = claims.get(index);
        getClaimManager().teleportToClaim(player, target);
    }

    private void handleList(Player player) {
        List<ClaimChunk> list = getClaimManager().getClaimsByOwner(player.getUniqueId());
        int max = getClaimManager().getMaxClaims(player);
        String maxStr = max == Integer.MAX_VALUE ? "∞" : String.valueOf(max);

        // Open visual Bedrock touch-friendly territory list
        getClaimGUI().openTerritoryList(player, 0);

        player.sendMessage(mm.deserialize("<gradient:#ffd700:#ffa500><bold>━━━━━━━━ [ SENTRAL WILAYAH ANDA (" + list.size() + "/" + maxStr + ") ] ━━━━━━━━</bold></gradient>"));
        if (list.isEmpty()) {
            player.sendMessage(mm.deserialize("<gray>Anda belum memiliki petak tanah satupun. Berdirilah di tempat pilihan Anda dan ketik <yellow>/claim</yellow>.</gray>"));
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            ClaimChunk c = list.get(i);
            String status = c.isInGracePeriod() ? "<red>[MENUNGGAK]</red>" : "<green>[AMAN]</green>";
            player.sendMessage(mm.deserialize("<gold>#" + (i + 1) + " </gold><yellow>[" + c.getChunkX() + ", " + c.getChunkZ() + "]</yellow> <gray>(" + c.getWorld() + ")</gray> " + status + " <gray>Rp" + String.format("%,.0f", c.getBankBalance()) + "</gray> " +
                    "<click:run_command:'/claim home " + (i + 1) + "'><hover:show_text:'<green>Klik untuk Teleport ke sini</green>'><aqua><u>[Teleport]</u></aqua></hover></click> " +
                    "<click:run_command:'/claim unclaim " + c.getChunkX() + " " + c.getChunkZ() + " " + c.getWorld() + "'><hover:show_text:'<red>Klik untuk Melepas Klaim ini</red>'><red><u>[Lepas]</u></red></hover></click>"));
        }
        player.sendMessage(mm.deserialize("<dark_gray><i>💡 Tips Bedrock: Menu visual layar sentuh telah dibuka otomatis. Anda juga bisa ketik /claim home untuk langsung pulang.</i></dark_gray>"));
    }

    private void handleTrust(Player player, String targetName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        var res = claimManager.trustPlayer(player, target.getUniqueId(), targetName);
        player.sendMessage(mm.deserialize(res.message()));
    }

    private void handleUntrust(Player player, String targetName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        var res = claimManager.untrustPlayer(player, target.getUniqueId(), targetName);
        player.sendMessage(mm.deserialize(res.message()));
    }

    private void sendHelp(Player player) {
        player.sendMessage(mm.deserialize("<gradient:#ffd700:#ffa500><bold>Bantuan Kedaulatan Wilayah (Land Claim):</bold></gradient>"));
        player.sendMessage(mm.deserialize("<yellow>/claim</yellow> <gray>- Klaim chunk 16x16 tempat Anda berdiri saat ini</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/claim home [nomor]</yellow> <gray>- Pulang ke tanah klaim Anda (Bedrock friendly)</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/claim tp <chunkX> <chunkZ></yellow> <gray>- Teleportasi ke koordinat petak tertentu</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/claim gui</yellow> <gray>- Buka menu antarmuka visual manajemen klaim</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/claim list</yellow> <gray>- Buka menu sentralisasi & daftar petak Anda</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/claim name <nama></yellow> <gray>- Beri label/nama khusus pada petak tanah ini</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/claim radius <1|2></yellow> <gray>- Klaim cepat 3x3 atau 5x5 petak di sekeliling Anda</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/claim outpost <set|remove></yellow> <gray>- Tetapkan petak sebagai Pos Depan (Diskon Pajak 50%)</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/claim unclaim [chunkX] [chunkZ]</yellow> <gray>- Melepas klaim (bisa dari jarak jauh)</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/claim unclaimall</yellow> <gray>- Melepas seluruh klaim tanah Anda</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/claim bank</yellow> <gray>- Info saldo brankas, pajak progresif, & masa tenggang</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/claim deposit <jumlah></yellow> <gray>- Setor koin ke brankas pajak wilayah</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/claim withdraw <jumlah></yellow> <gray>- Tarik koin dari brankas wilayah</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/claim role <pemain> <peran></yellow> <gray>- Atur peran (manager, builder, visitor)</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/claim flag <flag> <nilai></yellow> <gray>- Atur flag (pvp, mob_spawn, fire_spread)</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/claim border</yellow> <gray>- Nyalakan 4 tiang suar sudut & dinding energi batas chunk</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/claim info</yellow> <gray>- Cek status kepemilikan dan flag chunk saat ini</gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (this.claimManager == null && plugin != null) this.claimManager = plugin.getClaimManager();
        if (this.claimGUI == null && plugin != null) this.claimGUI = plugin.getClaimGUI();

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(List.of("gui", "home", "tp", "border", "name", "rename", "radius", "outpost", "info", "bank", "deposit", "withdraw", "flag", "role", "trust", "untrust", "list", "unclaim", "unclaimall"));
            if (sender.hasPermission("apexsions.admin") || sender.isOp()) {
                subs.add("admin");
                subs.add("reload");
            }
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) completions.add(s);
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("outpost")) {
            for (String o : List.of("set", "remove")) {
                if (o.startsWith(args[1].toLowerCase())) completions.add(o);
            }
            return completions;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("name") || args[0].equalsIgnoreCase("rename") || args[0].equalsIgnoreCase("setname"))) {
            for (String n : List.of("clear", "reset")) {
                if (n.startsWith(args[1].toLowerCase())) completions.add(n);
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("radius")) {
            for (String r : List.of("1", "2")) {
                if (r.startsWith(args[1].toLowerCase())) completions.add(r);
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("flag")) {
            for (String f : List.of("pvp", "mob_spawn", "fire_spread", "explosions", "greeting", "farewell")) {
                if (f.startsWith(args[1].toLowerCase())) completions.add(f);
            }
            return completions;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("flag")) {
            for (String v : List.of("true", "false")) {
                if (v.startsWith(args[2].toLowerCase())) completions.add(v);
            }
            return completions;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("role")) {
            for (String r : List.of("manager", "builder", "visitor", "remove")) {
                if (r.startsWith(args[2].toLowerCase())) completions.add(r);
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            for (String a : List.of("unclaim", "unclaimall", "sync", "collecttax", "deposit")) {
                if (a.startsWith(args[1].toLowerCase())) completions.add(a);
            }
            return completions;
        }

        return completions;
    }
}
