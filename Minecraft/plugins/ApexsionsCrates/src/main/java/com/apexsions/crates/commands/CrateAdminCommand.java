package com.apexsions.crates.commands;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.crate.Crate;
import com.apexsions.crates.crate.CrateLocation;
import com.apexsions.crates.gui.CrateAdminHubGUI;
import com.apexsions.crates.key.CrateKey;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CrateAdminCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCratesPlugin plugin;

    public CrateAdminCommand(ApexsionsCratesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("apexsions.crates.admin")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Anda tidak memiliki izin untuk menggunakan perintah admin peti.</red>"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("editor") || args[0].equalsIgnoreCase("menu")) {
            if (sender instanceof Player player) {
                new CrateAdminHubGUI(plugin, player).open();
            } else {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Menu GUI hanya bisa dibuka oleh pemain.</red>"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPlugin();
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>ApexsionsCrates berhasil dimuat ulang!</green>"));
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#f39c12:#e74c3c><bold>DAFTAR PETI KERAJAAN:</bold></gradient>"));
            for (Crate c : plugin.getCrateManager().getCrates()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(
                        "<gray>• </gray><yellow>" + c.getId() + "</yellow> <dark_gray>-</dark_gray> " + c.getName() + " <gray>(Kunci: " + c.getRequiredKeyId() + ", Hadiah: " + c.getRewards().size() + ")</gray>"
                ));
            }
            return true;
        }

        // /acrates set <crateId>
        if (args[0].equalsIgnoreCase("set")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Perintah ini hanya bisa dijalankan oleh pemain.</red>"));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Penggunaan: /acrates set <id_peti></red>"));
                return true;
            }

            Crate crate = plugin.getCrateManager().getCrate(args[1]);
            if (crate == null) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Peti <yellow>" + args[1] + "</yellow> tidak ditemukan.</red>"));
                return true;
            }

            Block target = player.getTargetBlockExact(5);
            if (target == null || target.getType().isAir()) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Silakan tatap blok (peti/ender chest) yang ingin dijadikan lokasi Crate.</red>"));
                return true;
            }

            CrateLocation cl = CrateLocation.fromLocation(target.getLocation(), crate.getId());
            plugin.getCrateManager().addLocation(cl);
            plugin.getRepository().saveLocation(cl);
            plugin.getHologramManager().spawnHologram(cl);

            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<green>Berhasil menautkan blok pada <white>" + cl.getX() + ", " + cl.getY() + ", " + cl.getZ() + "</white> ke Peti <yellow>" + crate.getName() + "</yellow>!</green>"
            ));
            return true;
        }

        // /acrates remove
        if (args[0].equalsIgnoreCase("remove")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Perintah ini hanya bisa dijalankan oleh pemain.</red>"));
                return true;
            }

            Block target = player.getTargetBlockExact(5);
            if (target == null || target.getType().isAir()) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Silakan tatap blok Crate yang ingin dihapus.</red>"));
                return true;
            }

            CrateLocation cl = plugin.getCrateManager().getCrateLocationAt(target.getLocation());
            if (cl == null) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Blok ini bukan lokasi Crate yang terdaftar.</red>"));
                return true;
            }

            plugin.getCrateManager().removeLocation(cl);
            plugin.getRepository().deleteLocation(cl);
            plugin.getHologramManager().removeHologram(cl);

            player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Lokasi peti berhasil dihapus.</yellow>"));
            return true;
        }

        // /acrates key give/take/set <player> <key> [amount] [physical|virtual]
        if (args[0].equalsIgnoreCase("key")) {
            if (args.length < 4) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Penggunaan: /acrates key <give|take|set> <pemain> <kunci> [jumlah] [physical|virtual]</red>"));
                return true;
            }

            String action = args[1].toLowerCase();
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Pemain <yellow>" + args[2] + "</yellow> tidak ditemukan.</red>"));
                return true;
            }

            String keyId = args[3].toLowerCase();
            CrateKey key = plugin.getKeyManager().getKey(keyId);
            if (key == null) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Kunci <yellow>" + keyId + "</yellow> tidak ditemukan.</red>"));
                return true;
            }

            int amount = 1;
            if (args.length >= 5) {
                try {
                    amount = Integer.parseInt(args[4]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Jumlah harus berupa angka.</red>"));
                    return true;
                }
            }

            boolean virtual = false;
            if (args.length >= 6 && args[5].equalsIgnoreCase("virtual")) {
                virtual = true;
            }

            if (action.equals("giveall")) {
                int count = 0;
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (virtual) {
                        plugin.getRepository().addVirtualKeys(online.getUniqueId(), keyId, amount);
                        online.sendMessage(MiniMessage.miniMessage().deserialize("<green>★ Anda menerima <white>" + amount + "x</white> kunci virtual <yellow>" + key.getName() + "</yellow> dari server event!</green>"));
                    } else {
                        plugin.getKeyManager().givePhysicalKey(online, keyId, amount);
                        online.sendMessage(MiniMessage.miniMessage().deserialize("<green>★ Anda menerima <white>" + amount + "x</white> kunci fisik <yellow>" + key.getName() + "</yellow> dari server event!</green>"));
                    }
                    count++;
                }
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Berhasil membagikan <white>" + amount + "x</white> kunci <yellow>" + key.getName() + "</yellow> ke seluruh <aqua>" + count + "</aqua> pemain online!</green>"));
                return true;
            } else if (action.equals("give")) {
                if (virtual) {
                    plugin.getRepository().addVirtualKeys(target.getUniqueId(), keyId, amount);
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Berhasil menambahkan <white>" + amount + "x</white> kunci virtual <yellow>" + key.getName() + "</yellow> ke <aqua>" + target.getName() + "</aqua>.</green>"));
                    target.sendMessage(MiniMessage.miniMessage().deserialize("<green>Anda menerima <white>" + amount + "x</white> kunci virtual <yellow>" + key.getName() + "</yellow>!</green>"));
                } else {
                    plugin.getKeyManager().givePhysicalKey(target, keyId, amount);
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Berhasil memberikan <white>" + amount + "x</white> kunci fisik <yellow>" + key.getName() + "</yellow> ke <aqua>" + target.getName() + "</aqua>.</green>"));
                    target.sendMessage(MiniMessage.miniMessage().deserialize("<green>Anda menerima <white>" + amount + "x</white> kunci fisik <yellow>" + key.getName() + "</yellow>!</green>"));
                }
                return true;
            } else if (action.equals("take")) {
                if (virtual) {
                    plugin.getRepository().takeVirtualKeys(target.getUniqueId(), keyId, amount);
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Berhasil menarik <white>" + amount + "x</white> kunci virtual <yellow>" + key.getName() + "</yellow> dari <aqua>" + target.getName() + "</aqua>.</yellow>"));
                } else {
                    plugin.getKeyManager().takePhysicalKey(target, keyId, amount);
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Berhasil menarik <white>" + amount + "x</white> kunci fisik <yellow>" + key.getName() + "</yellow> dari <aqua>" + target.getName() + "</aqua>.</yellow>"));
                }
                return true;
            } else if (action.equals("set")) {
                plugin.getRepository().setVirtualKeys(target.getUniqueId(), keyId, amount);
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Saldo kunci virtual <yellow>" + key.getName() + "</yellow> milik <aqua>" + target.getName() + "</aqua> diatur menjadi <white>" + amount + "</white>.</green>"));
                return true;
            }
        }

        // /acrates tp <crateId>
        if (args[0].equalsIgnoreCase("tp")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Perintah ini hanya bisa dijalankan oleh pemain.</red>"));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Penggunaan: /acrates tp <id_peti></red>"));
                return true;
            }

            for (CrateLocation cl : plugin.getCrateManager().getLocations()) {
                if (cl.getCrateId().equalsIgnoreCase(args[1])) {
                    Location loc = cl.toBukkitLocation();
                    if (loc != null) {
                        player.teleport(loc.add(0.5, 1.0, 0.5));
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Teleport ke lokasi Peti <yellow>" + args[1] + "</yellow>!</green>"));
                        return true;
                    }
                }
            }
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Tidak ditemukan lokasi fisik di dunia untuk peti <yellow>" + args[1] + "</yellow>.</red>"));
            return true;
        }

        sender.sendMessage(MiniMessage.miniMessage().deserialize("""
            <gradient:#f39c12:#e74c3c><bold>=== APEXSIONS CRATES ADMIN ===</bold></gradient>
            <yellow>/acrates editor</yellow> <gray>- Buka Control Panel Admin.</gray>
            <yellow>/acrates set <peti></yellow> <gray>- Tautkan blok yang ditatap menjadi Crate.</gray>
            <yellow>/acrates remove</yellow> <gray>- Hapus Crate pada blok yang ditatap.</gray>
            <yellow>/acrates key give <pemain> <kunci> [jumlah] [physical|virtual]</yellow> <gray>- Berikan kunci.</gray>
            <yellow>/acrates key take <pemain> <kunci> [jumlah] [physical|virtual]</yellow> <gray>- Tarik kunci.</gray>
            <yellow>/acrates key set <pemain> <kunci> <jumlah></yellow> <gray>- Atur saldo kunci virtual.</gray>
            <yellow>/acrates list</yellow> <gray>- Lihat daftar semua peti.</gray>
            <yellow>/acrates reload</yellow> <gray>- Muat ulang seluruh sistem Crate.</gray>
        """));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> sub = List.of("editor", "set", "remove", "key", "list", "tp", "reload");
            for (String s : sub) {
                if (s.toLowerCase().startsWith(args[0].toLowerCase())) completions.add(s);
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("tp"))) {
            for (Crate c : plugin.getCrateManager().getCrates()) {
                if (c.getId().toLowerCase().startsWith(args[1].toLowerCase())) completions.add(c.getId());
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("key")) {
            List<String> actions = List.of("give", "giveall", "take", "set");
            for (String a : actions) {
                if (a.toLowerCase().startsWith(args[1].toLowerCase())) completions.add(a);
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("key")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[2].toLowerCase())) completions.add(p.getName());
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("key")) {
            for (CrateKey k : plugin.getKeyManager().getKeys()) {
                if (k.getId().toLowerCase().startsWith(args[3].toLowerCase())) completions.add(k.getId());
            }
        } else if (args.length == 5 && args[0].equalsIgnoreCase("key")) {
            completions.addAll(List.of("1", "5", "10", "32", "64"));
        } else if (args.length == 6 && args[0].equalsIgnoreCase("key")) {
            completions.addAll(List.of("physical", "virtual"));
        }
        return completions;
    }
}
