package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.pose.PlayerPoseType;
import com.apexsions.core.pose.PoseManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PoseCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final PoseManager poseManager;
    private final MiniMessage mm;

    public PoseCommand(ApexsionsCorePlugin plugin, PoseManager poseManager) {
        this.plugin = plugin;
        this.poseManager = poseManager;
        this.mm = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>🎭 DAFTAR POSE & EMOTE APEXSIONS</bold></gradient>"));
            sender.sendMessage(mm.deserialize("<gray>• <yellow>/sit</yellow> - Duduk di lantai atau klik kanan tangga/slab</gray>"));
            sender.sendMessage(mm.deserialize("<gray>• <yellow>/lay</yellow> - Tiduran / rebahan di lantai</gray>"));
            sender.sendMessage(mm.deserialize("<gray>• <yellow>/crawl</yellow> - Merangkak menembus celah 1 blok</gray>"));
            sender.sendMessage(mm.deserialize("<gray>• <yellow>/pose bellyflop</yellow> - Tengkurap di tanah</gray>"));
            sender.sendMessage(mm.deserialize("<gray>• <yellow>/pose spin</yellow> - Berputar di tempat</gray>"));
            sender.sendMessage(mm.deserialize("<gray>• <yellow>/pose stand</yellow> - Berdiri dan batalkan semua pose</gray>"));
            return true;
        }

        String sub = args[0].toLowerCase();
        Player targetPlayer = null;

        if (args.length >= 2) {
            if (!sender.hasPermission("apexsions.pose.admin")) {
                sender.sendMessage(mm.deserialize("<red>❌ Anda tidak memiliki izin untuk mengubah pose pemain lain.</red>"));
                return true;
            }
            targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null || !targetPlayer.isOnline()) {
                sender.sendMessage(mm.deserialize("<red>❌ Pemain <yellow>" + args[1] + "</yellow> tidak ditemukan atau offline.</red>"));
                return true;
            }
        } else if (sender instanceof Player p) {
            targetPlayer = p;
        } else {
            sender.sendMessage(mm.deserialize("<red>❌ Konsol harus menyertakan nama target pemain: /pose <subcommand> <player></red>"));
            return true;
        }

        switch (sub) {
            case "sit" -> {
                if (sender == targetPlayer && !sender.hasPermission("apexsions.pose.sit")) {
                    sender.sendMessage(mm.deserialize("<red>❌ Anda tidak memiliki izin untuk duduk.</red>"));
                    return true;
                }
                poseManager.sit(targetPlayer, null, false);
                if (sender != targetPlayer) {
                    sender.sendMessage(mm.deserialize("<green>✔ Berhasil mendudukkan <yellow>" + targetPlayer.getName() + "</yellow>.</green>"));
                }
            }
            case "lay", "lie" -> {
                if (sender == targetPlayer && !sender.hasPermission("apexsions.pose.lay")) {
                    sender.sendMessage(mm.deserialize("<red>❌ Anda tidak memiliki izin untuk tiduran.</red>"));
                    return true;
                }
                poseManager.lay(targetPlayer);
                if (sender != targetPlayer) {
                    sender.sendMessage(mm.deserialize("<green>✔ Berhasil menidurkan <yellow>" + targetPlayer.getName() + "</yellow>.</green>"));
                }
            }
            case "crawl" -> {
                if (sender == targetPlayer && !sender.hasPermission("apexsions.pose.crawl")) {
                    sender.sendMessage(mm.deserialize("<red>❌ Anda tidak memiliki izin untuk merangkak.</red>"));
                    return true;
                }
                poseManager.crawl(targetPlayer);
                if (sender != targetPlayer) {
                    sender.sendMessage(mm.deserialize("<green>✔ Berhasil mengubah pose <yellow>" + targetPlayer.getName() + "</yellow> menjadi merangkak.</green>"));
                }
            }
            case "bellyflop", "flop" -> {
                if (sender == targetPlayer && !sender.hasPermission("apexsions.pose.bellyflop")) {
                    sender.sendMessage(mm.deserialize("<red>❌ Anda tidak memiliki izin untuk pose tengkurap.</red>"));
                    return true;
                }
                poseManager.bellyflop(targetPlayer);
                if (sender != targetPlayer) {
                    sender.sendMessage(mm.deserialize("<green>✔ Berhasil mengubah pose <yellow>" + targetPlayer.getName() + "</yellow> menjadi tengkurap.</green>"));
                }
            }
            case "spin", "rotate" -> {
                if (sender == targetPlayer && !sender.hasPermission("apexsions.pose.spin")) {
                    sender.sendMessage(mm.deserialize("<red>❌ Anda tidak memiliki izin untuk pose berputar.</red>"));
                    return true;
                }
                poseManager.spin(targetPlayer);
                if (sender != targetPlayer) {
                    sender.sendMessage(mm.deserialize("<green>✔ Berhasil membuat <yellow>" + targetPlayer.getName() + "</yellow> berputar.</green>"));
                }
            }
            case "stand", "clear", "cancel" -> {
                poseManager.standUp(targetPlayer, true);
                if (sender != targetPlayer) {
                    sender.sendMessage(mm.deserialize("<green>✔ Berhasil memberdirikan <yellow>" + targetPlayer.getName() + "</yellow>.</green>"));
                }
            }
            default -> sender.sendMessage(mm.deserialize("<red>❌ Subcommand tidak valid. Pilihan: sit, lay, crawl, bellyflop, spin, stand.</red>"));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subs = List.of("sit", "lay", "crawl", "bellyflop", "spin", "stand");
            List<String> list = new ArrayList<>();
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) {
                    list.add(s);
                }
            }
            return list;
        } else if (args.length == 2 && sender.hasPermission("apexsions.pose.admin")) {
            List<String> list = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    list.add(p.getName());
                }
            }
            return list;
        }
        return Collections.emptyList();
    }
}
