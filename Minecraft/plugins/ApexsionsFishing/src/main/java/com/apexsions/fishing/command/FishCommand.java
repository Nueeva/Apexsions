package com.apexsions.fishing.command;

import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.gui.FishSellGUI;
import com.apexsions.fishing.gui.FishingVaultGUI;
import com.apexsions.fishing.gui.RodShopGUI;
import com.apexsions.fishing.gui.VaultShopGUI;
import com.apexsions.fishing.gui.admin.AdminRodCreatorGUI;
import com.apexsions.fishing.gui.admin.FishingAdminHubGUI;
import com.apexsions.fishing.gui.profile.FishingJournalGUI;
import com.apexsions.fishing.gui.profile.FishingLeaderboardGUI;
import com.apexsions.fishing.gui.profile.FishingProfileGUI;
import com.apexsions.fishing.model.FishingRodData;
import com.apexsions.fishing.util.PlayerResolver;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
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

/**
 * Command handler for /fish and /vault.
 */
public class FishCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsFishing plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public FishCommand(ApexsionsFishing plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize("<red>Perintah ini hanya dapat dijalankan oleh pemain.</red>"));
            return true;
        }

        // Direct /vault or /fvault shortcut
        if (label.equalsIgnoreCase("vault") || label.equalsIgnoreCase("fishvault") || label.equalsIgnoreCase("fvault")) {
            int page = 1;
            if (args.length > 0) {
                try {
                    page = Integer.parseInt(args[0]);
                } catch (NumberFormatException ignored) {}
            }
            new FishingVaultGUI(plugin, player, page).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return true;
        }

        // Default /fish with no args opens the Fishing Profile Hub
        if (args.length == 0) {
            new FishingProfileGUI(plugin, player).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "menu", "profile", "hub" -> {
                new FishingProfileGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case "vault", "brankas" -> {
                if (args.length >= 2 && player.hasPermission("apexsions.fishing.admin")) {
                    Player target = PlayerResolver.resolveOnline(args[1]);
                    if (target != null) {
                        int page = 1;
                        if (args.length >= 3) {
                            try { page = Integer.parseInt(args[2]); } catch (NumberFormatException ignored) {}
                        }
                        new FishingVaultGUI(plugin, player, target.getUniqueId(), target.getName(), page, true).open();
                        player.sendMessage(mm.deserialize("<gold>Membuka brankas pemancing milik <yellow>" + target.getName() + "</yellow> (Admin View).</gold>"));
                        return true;
                    }
                }
                int page = 1;
                if (args.length >= 2) {
                    try { page = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
                }
                new FishingVaultGUI(plugin, player, page).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case "vaultshop", "belibrankas" -> {
                new VaultShopGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case "shop", "toko" -> {
                new RodShopGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case "sell", "jual" -> {
                new FishSellGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case "top", "leaderboard", "peringkat" -> {
                new FishingLeaderboardGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case "journal", "pedia", "jurnal" -> {
                new FishingJournalGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
            }
            case "admin" -> {
                if (!player.hasPermission("apexsions.fishing.admin")) {
                    player.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk mengakses panel admin ini.</red>"));
                    return true;
                }
                new FishingAdminHubGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case "creator", "create" -> {
                if (!player.hasPermission("apexsions.fishing.admin")) {
                    player.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk membuka Rod Creator.</red>"));
                    return true;
                }
                new AdminRodCreatorGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case "bait", "umpan" -> {
                if (args.length >= 2 && (args[1].equalsIgnoreCase("give") || args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("take"))) {
                    if (!player.hasPermission("apexsions.fishing.admin")) {
                        player.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk perintah ini.</red>"));
                        return true;
                    }
                    if (args.length < 4) {
                        player.sendMessage(mm.deserialize("<red>Penggunaan: /fish bait <give|set|take> <pemain> <jumlah></red>"));
                        return true;
                    }
                    Player target = PlayerResolver.resolveOnline(args[2]);
                    if (target == null) {
                        player.sendMessage(mm.deserialize("<red>Pemain " + args[2] + " tidak ditemukan atau sedang offline.</red>"));
                        return true;
                    }
                    int amount;
                    try {
                        amount = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(mm.deserialize("<red>Jumlah harus berupa angka bulat positif!</red>"));
                        return true;
                    }
                    var stats = plugin.getVaultStorage().getStats(target.getUniqueId());
                    String action = args[1].toLowerCase();
                    if (action.equals("give")) {
                        stats.addVirtualBait(amount);
                        player.sendMessage(mm.deserialize("<green>Berhasil memberikan <gold>" + amount + " kuota umpan</gold> kepada <white>" + target.getName() + "</white>!</green>"));
                        target.sendMessage(mm.deserialize("<green>Anda menerima <gold>" + amount + " kuota umpan virtual</gold> dari Admin!</green>"));
                    } else if (action.equals("set")) {
                        stats.setVirtualBait(amount);
                        player.sendMessage(mm.deserialize("<green>Berhasil mengatur saldo umpan <white>" + target.getName() + "</white> menjadi <gold>" + amount + " kuota</gold>!</green>"));
                    } else if (action.equals("take")) {
                        stats.setVirtualBait(Math.max(0, stats.getVirtualBait() - amount));
                        player.sendMessage(mm.deserialize("<green>Berhasil mengurangi <gold>" + amount + " kuota umpan</gold> dari <white>" + target.getName() + "</white>!</green>"));
                    }
                    plugin.getVaultStorage().savePlayerData(target.getUniqueId());
                    return true;
                }
                new com.apexsions.fishing.gui.BaitShopGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case "give" -> {
                if (!player.hasPermission("apexsions.fishing.admin")) {
                    player.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk perintah ini.</red>"));
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(mm.deserialize("<red>Penggunaan: /fish give <pemain> <rod_id></red>"));
                    return true;
                }
                Player target = PlayerResolver.resolveOnline(args[1]);
                if (target == null) {
                    player.sendMessage(mm.deserialize("<red>Pemain " + args[1] + " tidak ditemukan atau sedang offline.</red>"));
                    return true;
                }
                String rodId = args[2].toLowerCase();
                ItemStack rod = plugin.getRodManager().createRodById(rodId);
                if (rod == null) {
                    player.sendMessage(mm.deserialize("<red>Pancingan dengan ID '" + rodId + "' tidak terdaftar di rods.yml.</red>"));
                    return true;
                }
                target.getInventory().addItem(rod);
                player.sendMessage(mm.deserialize("<green>Berhasil memberikan pancingan <yellow>" + rodId + "</yellow> kepada <white>" + target.getName() + "</white>!</green>"));
                target.sendMessage(mm.deserialize("<green>Anda menerima pancingan resmi </green>").append(rod.getItemMeta().displayName()).append(mm.deserialize("<green> dari Admin!</green>")));
            }
            case "zone", "area", "zonamancing" -> {
                handleZoneCommand(player, args);
            }
            case "reload" -> {
                if (!player.hasPermission("apexsions.fishing.admin")) {
                    player.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk perintah reload.</red>"));
                    return true;
                }
                plugin.reloadConfig();
                plugin.getLootGenerator().reload();
                plugin.getRodManager().reload();
                plugin.getZoneManager().reload();
                player.sendMessage(mm.deserialize("<green><bold>RELOAD SUKSES!</bold> Seluruh konfigurasi ApexsionsFishing telah diperbarui.</green>"));
            }
            default -> {
                player.sendMessage(mm.deserialize("<yellow>Perintah tidak dikenal. Ketik <gold>/fish</gold> untuk membuka menu utama.</yellow>"));
            }
        }
        return true;
    }

    private void handleZoneCommand(Player player, String[] args) {
        if (!player.hasPermission("apexsions.fishing.admin")) {
            player.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk mengelola zona memancing.</red>"));
            return;
        }

        if (args.length < 2) {
            sendZoneHelp(player);
            return;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "gui", "menu" -> {
                new com.apexsions.fishing.gui.admin.FishingZoneListGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case "wand" -> {
                if (!com.apexsions.fishing.integration.WorldEditHook.isAvailable()) {
                    player.sendMessage(mm.deserialize("<red>Plugin WorldEdit tidak terpasang di server!</red>"));
                    return;
                }
                player.performCommand("/wand");
                player.sendMessage(mm.deserialize("<gradient:#00c6ff:#0072ff>[Zona Memancing]</gradient> <yellow>Gunakan Wand untuk memilih Pos 1 (Klik Kiri) & Pos 2 (Klik Kanan), lalu jalankan <white>/fish zone create <nama></white>.</yellow>"));
            }
            case "list", "daftar" -> {
                var zones = plugin.getZoneManager().getAllZones();
                if (zones.isEmpty()) {
                    player.sendMessage(mm.deserialize("<yellow>Belum ada zona memancing yang terdaftar. Buat dengan <gold>/fish zone create <nama></gold>.</yellow>"));
                    return;
                }
                player.sendMessage(mm.deserialize("<gradient:#00c6ff:#0072ff><bold>=== DAFTAR ZONA MEMANCING APEXSIONS (" + zones.size() + ") ===</bold></gradient>"));
                for (var z : zones) {
                    player.sendMessage(mm.deserialize("<gold>● </gold><yellow><bold>" + z.getName() + "</bold></yellow> " +
                            "<gray>(" + z.getWorldName() + ": " + z.getMinX() + "," + z.getMinY() + "," + z.getMinZ() + " s/d " + z.getMaxX() + "," + z.getMaxY() + "," + z.getMaxZ() + ")</gray><newline>" +
                            "  <gray>AFK: " + (z.isAfkAllowed() ? "<green>YA</green>" : "<red>TIDAK</red>") + " • Rate: <yellow>x" + z.getRateMultiplier() + "</yellow> • Rarity: <gold>x" + z.getRarityMultiplier() + "</gold> • Weight: <aqua>x" + z.getWeightMultiplier() + "</aqua></gray>"));
                }
            }
            case "create", "buat" -> {
                if (args.length < 3) {
                    player.sendMessage(mm.deserialize("<red>Penggunaan: /fish zone create <nama> [rate_mult] [rarity_mult]</red>"));
                    return;
                }
                if (!com.apexsions.fishing.integration.WorldEditHook.isAvailable()) {
                    player.sendMessage(mm.deserialize("<red>Plugin WorldEdit tidak ditemukan di server! Pastikan WorldEdit aktif.</red>"));
                    return;
                }
                String name = args[2].toLowerCase().trim();
                if (plugin.getZoneManager().getZone(name) != null) {
                    player.sendMessage(mm.deserialize("<red>Zona dengan nama '" + name + "' sudah ada! Hapus dulu via <yellow>/fish zone delete " + name + "</yellow>.</red>"));
                    return;
                }

                com.apexsions.fishing.integration.WorldEditHook.SelectionBounds sel;
                try {
                    sel = com.apexsions.fishing.integration.WorldEditHook.getPlayerSelection(player);
                } catch (com.sk89q.worldedit.IncompleteRegionException e) {
                    player.sendMessage(mm.deserialize("<red>Seleksi WorldEdit belum lengkap! Tentukan Pos 1 dan Pos 2 dengan //wand terlebih dahulu.</red>"));
                    return;
                }

                if (sel == null) {
                    player.sendMessage(mm.deserialize("<red>Anda belum menentukan area seleksi WorldEdit! Gunakan //wand untuk memilih batas area.</red>"));
                    return;
                }

                double rateMult = 1.5;
                double rarityMult = 1.5;
                if (args.length >= 4) {
                    try { rateMult = Double.parseDouble(args[3]); } catch (NumberFormatException ignored) {}
                }
                if (args.length >= 5) {
                    try { rarityMult = Double.parseDouble(args[4]); } catch (NumberFormatException ignored) {}
                }

                plugin.getZoneManager().createZone(
                        name,
                        "<gradient:#00c6ff:#0072ff>" + name + "</gradient>",
                        sel.worldName(),
                        sel.minX(), sel.minY(), sel.minZ(),
                        sel.maxX(), sel.maxY(), sel.maxZ(),
                        true,
                        rateMult,
                        rarityMult,
                        1.2,
                        1.5
                );

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                player.sendMessage(mm.deserialize("<green><bold>ZONA BERHASIL DIBUAT!</bold></green><newline>" +
                        "<yellow>Nama: <white><bold>" + name + "</bold></white><newline>" +
                        "Dunia: <aqua>" + sel.worldName() + "</aqua><newline>" +
                        "Koordinat: <white>(" + sel.minX() + "," + sel.minY() + "," + sel.minZ() + ") s/d (" + sel.maxX() + "," + sel.maxY() + "," + sel.maxZ() + ")</white><newline>" +
                        "Volume: <gold>" + String.format("%,d", sel.getVolume()) + " Blok</gold><newline>" +
                        "Pengganda: <yellow>Rate x" + rateMult + "</yellow> • <gold>Rarity x" + rarityMult + "</gold></yellow>"));
            }
            case "delete", "hapus", "remove" -> {
                if (args.length < 3) {
                    player.sendMessage(mm.deserialize("<red>Penggunaan: /fish zone delete <nama></red>"));
                    return;
                }
                String name = args[2].toLowerCase().trim();
                boolean removed = plugin.getZoneManager().deleteZone(name);
                if (removed) {
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<green>Zona memancing <yellow>" + name + "</yellow> berhasil dihapus dari sistem!</green>"));
                } else {
                    player.sendMessage(mm.deserialize("<red>Zona dengan nama '" + name + "' tidak ditemukan!</red>"));
                }
            }
            case "info" -> {
                if (args.length < 3) {
                    player.sendMessage(mm.deserialize("<red>Penggunaan: /fish zone info <nama></red>"));
                    return;
                }
                var zone = plugin.getZoneManager().getZone(args[2]);
                if (zone == null) {
                    player.sendMessage(mm.deserialize("<red>Zona '" + args[2] + "' tidak ditemukan!</red>"));
                    return;
                }
                player.sendMessage(mm.deserialize("<gradient:#00c6ff:#0072ff><bold>=== INFORMASI ZONA: " + zone.getName() + " ===</bold></gradient><newline>" +
                        "<gray>Display Name: </gray>" + zone.getDisplayName() + "<newline>" +
                        "<gray>Dunia: <yellow>" + zone.getWorldName() + "</yellow><newline>" +
                        "<gray>Batas: <aqua>(" + zone.getMinX() + "," + zone.getMinY() + "," + zone.getMinZ() + ") s/d (" + zone.getMaxX() + "," + zone.getMaxY() + "," + zone.getMaxZ() + ")</aqua><newline>" +
                        "<gray>Volume: <gold>" + String.format("%,d", zone.getVolume()) + " blok</gold><newline>" +
                        "<gray>Izin AFK: " + (zone.isAfkAllowed() ? "<green>DIIZINKAN</green>" : "<red>DILARANG</red>") + "<newline>" +
                        "<gray>Rate Multiplier: <yellow>x" + zone.getRateMultiplier() + "</yellow><newline>" +
                        "<gray>Rarity Multiplier: <gold>x" + zone.getRarityMultiplier() + "</gold><newline>" +
                        "<gray>Weight Multiplier: <aqua>x" + zone.getWeightMultiplier() + "</aqua><newline>" +
                        "<gray>EXP Multiplier: <light_purple>x" + zone.getXpMultiplier() + "</light_purple></gray>"));
            }
            case "tp", "teleport" -> {
                if (args.length < 3) {
                    player.sendMessage(mm.deserialize("<red>Penggunaan: /fish zone tp <nama></red>"));
                    return;
                }
                var zone = plugin.getZoneManager().getZone(args[2]);
                if (zone == null) {
                    player.sendMessage(mm.deserialize("<red>Zona '" + args[2] + "' tidak ditemukan!</red>"));
                    return;
                }
                org.bukkit.Location center = zone.getCenter(Bukkit.getServer());
                if (center != null) {
                    player.teleport(center);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
                    player.sendMessage(mm.deserialize("<green>Teleportasi ke tengah zona memancing <yellow>" + zone.getName() + "</yellow>!</green>"));
                } else {
                    player.sendMessage(mm.deserialize("<red>Dunia '" + zone.getWorldName() + "' tidak sedang aktif!</red>"));
                }
            }
            case "set" -> {
                if (args.length < 5) {
                    player.sendMessage(mm.deserialize("<red>Penggunaan: /fish zone set <nama> <rate|rarity|weight|xp|afk> <nilai></red>"));
                    return;
                }
                var zone = plugin.getZoneManager().getZone(args[2]);
                if (zone == null) {
                    player.sendMessage(mm.deserialize("<red>Zona '" + args[2] + "' tidak ditemukan!</red>"));
                    return;
                }
                String property = args[3].toLowerCase();
                String valStr = args[4];
                try {
                    switch (property) {
                        case "rate" -> {
                            double r = Double.parseDouble(valStr);
                            zone.setRateMultiplier(r);
                            player.sendMessage(mm.deserialize("<green>Rate multiplier untuk zona <yellow>" + zone.getName() + "</yellow> diubah menjadi <gold>x" + zone.getRateMultiplier() + "</gold>!</green>"));
                        }
                        case "rarity" -> {
                            double rm = Double.parseDouble(valStr);
                            zone.setRarityMultiplier(rm);
                            player.sendMessage(mm.deserialize("<green>Rarity multiplier untuk zona <yellow>" + zone.getName() + "</yellow> diubah menjadi <gold>x" + zone.getRarityMultiplier() + "</gold>!</green>"));
                        }
                        case "weight" -> {
                            double wm = Double.parseDouble(valStr);
                            zone.setWeightMultiplier(wm);
                            player.sendMessage(mm.deserialize("<green>Weight multiplier untuk zona <yellow>" + zone.getName() + "</yellow> diubah menjadi <gold>x" + zone.getWeightMultiplier() + "</gold>!</green>"));
                        }
                        case "xp" -> {
                            double xm = Double.parseDouble(valStr);
                            zone.setXpMultiplier(xm);
                            player.sendMessage(mm.deserialize("<green>EXP multiplier untuk zona <yellow>" + zone.getName() + "</yellow> diubah menjadi <gold>x" + zone.getXpMultiplier() + "</gold>!</green>"));
                        }
                        case "afk" -> {
                            boolean afk = Boolean.parseBoolean(valStr) || valStr.equalsIgnoreCase("1") || valStr.equalsIgnoreCase("ya");
                            zone.setAfkAllowed(afk);
                            player.sendMessage(mm.deserialize("<green>Status AFK untuk zona <yellow>" + zone.getName() + "</yellow> diubah menjadi: " + (afk ? "<green>AKTIF</green>" : "<red>NON-AKTIF</red>") + "!</green>"));
                        }
                        default -> {
                            player.sendMessage(mm.deserialize("<red>Properti tidak valid! Pilihan: rate, rarity, weight, xp, afk</red>"));
                            return;
                        }
                    }
                    plugin.getZoneManager().save();
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
                } catch (NumberFormatException e) {
                    player.sendMessage(mm.deserialize("<red>Nilai harus berupa angka valid!</red>"));
                }
            }
            default -> sendZoneHelp(player);
        }
    }

    private void sendZoneHelp(Player player) {
        player.sendMessage(mm.deserialize("<gradient:#00c6ff:#0072ff><bold>--- MANAJEMEN ZONA MEMANCING (WORLDEDIT) ---</bold></gradient><newline>" +
                "<yellow>/fish zone gui</yellow> <gray>- Buka panel GUI pengelola zona</gray><newline>" +
                "<yellow>/fish zone wand</yellow> <gray>- Dapatkan wand WorldEdit & instruksi</gray><newline>" +
                "<yellow>/fish zone list</yellow> <gray>- Lihat daftar seluruh zona mancing</gray><newline>" +
                "<yellow>/fish zone create <nama> [rate] [rarity]</yellow> <gray>- Buat zona dari seleksi //wand</gray><newline>" +
                "<yellow>/fish zone delete <nama></yellow> <gray>- Hapus zona memancing</gray><newline>" +
                "<yellow>/fish zone info <nama></yellow> <gray>- Rincian zona memancing</gray><newline>" +
                "<yellow>/fish zone tp <nama></yellow> <gray>- Teleportasi ke tengah zona</gray><newline>" +
                "<yellow>/fish zone set <nama> <rate|rarity|weight|xp|afk> <nilai></yellow> <gray>- Atur parameter</gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("menu");
            list.add("vault");
            list.add("shop");
            list.add("sell");
            list.add("top");
            list.add("journal");
            list.add("bait");
            list.add("umpan");
            if (sender.hasPermission("apexsions.fishing.admin")) {
                list.add("admin");
                list.add("creator");
                list.add("give");
                list.add("zone");
                list.add("reload");
            }
            return list.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("vault") || args[0].equalsIgnoreCase("brankas")) && sender.hasPermission("apexsions.fishing.admin")) {
            return PlayerResolver.completePlayerNames(sender, args[1]);
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("bait") || args[0].equalsIgnoreCase("umpan")) && sender.hasPermission("apexsions.fishing.admin")) {
            return List.of("give", "set", "take").stream().filter(s -> s.startsWith(args[1].toLowerCase())).toList();
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("bait") || args[0].equalsIgnoreCase("umpan")) && sender.hasPermission("apexsions.fishing.admin")) {
            return PlayerResolver.completePlayerNames(sender, args[2]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give") && sender.hasPermission("apexsions.fishing.admin")) {
            return PlayerResolver.completePlayerNames(sender, args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give") && sender.hasPermission("apexsions.fishing.admin")) {
            return plugin.getRodManager().getAllRods().stream().map(FishingRodData::getId)
                    .filter(id -> id.toLowerCase().startsWith(args[2].toLowerCase())).toList();
        }

        // Zone Tab Completions
        if (args[0].equalsIgnoreCase("zone") && sender.hasPermission("apexsions.fishing.admin")) {
            if (args.length == 2) {
                return List.of("create", "delete", "list", "info", "set", "tp", "gui", "wand")
                        .stream().filter(s -> s.startsWith(args[1].toLowerCase())).toList();
            }
            if (args.length == 3 && List.of("delete", "info", "tp", "set").contains(args[1].toLowerCase())) {
                return plugin.getZoneManager().getAllZones().stream()
                        .map(com.apexsions.fishing.model.FishingZone::getName)
                        .filter(n -> n.startsWith(args[2].toLowerCase())).toList();
            }
            if (args.length == 4 && args[1].equalsIgnoreCase("set")) {
                return List.of("rate", "rarity", "weight", "xp", "afk")
                        .stream().filter(s -> s.startsWith(args[3].toLowerCase())).toList();
            }
            if (args.length == 5 && args[1].equalsIgnoreCase("set") && args[3].equalsIgnoreCase("afk")) {
                return List.of("true", "false");
            }
        }

        return List.of();
    }
}
