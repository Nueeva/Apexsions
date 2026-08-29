package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.gui.warp.WarpAdminGUI;
import com.apexsions.core.gui.warp.WarpGUI;
import com.apexsions.core.warp.Warp;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WarpCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public WarpCommand(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize("<red>Perintah ini hanya dapat dijalankan oleh pemain di dalam game.</red>"));
            return true;
        }

        // Direct /warpmgr or /warpadmin command
        if (label.equalsIgnoreCase("warpmgr") || label.equalsIgnoreCase("warpadmin")) {
            if (!player.hasPermission("apexsionscore.warp.admin") && !player.hasPermission("kingdomcore.admin")) {
                player.sendMessage(mm.deserialize("<red>Kamu tidak memiliki izin untuk mengakses menu admin warp.</red>"));
                return true;
            }
            player.openInventory(new WarpAdminGUI(plugin, player).getInventory());
            return true;
        }

        // /warp without args -> Open Player GUI
        if (args.length == 0) {
            player.openInventory(new WarpGUI(plugin, player).getInventory());
            return true;
        }

        String sub = args[0].toLowerCase();

        // Subcommands
        if (sub.equals("admin") || sub.equals("mgr") || sub.equals("manage")) {
            if (!player.hasPermission("apexsionscore.warp.admin") && !player.hasPermission("kingdomcore.admin")) {
                player.sendMessage(mm.deserialize("<red>Kamu tidak memiliki izin untuk mengakses menu admin warp.</red>"));
                return true;
            }
            player.openInventory(new WarpAdminGUI(plugin, player).getInventory());
            return true;
        }

        if (sub.equals("set") || sub.equals("create")) {
            if (!player.hasPermission("apexsionscore.warp.admin") && !player.hasPermission("kingdomcore.admin")) {
                player.sendMessage(mm.deserialize("<red>Kamu tidak memiliki izin untuk membuat warp.</red>"));
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(mm.deserialize("<gray>Penggunaan: </gray><yellow>/warp set <nama> [kategori] [delay]</yellow>"));
                return true;
            }

            String id = args[1].toLowerCase();
            String name = args[1];
            String category = args.length > 2 ? args[2].toUpperCase() : "SERVER";
            int delay = 3;
            if (args.length > 3) {
                try {
                    delay = Integer.parseInt(args[3]);
                } catch (NumberFormatException ignored) {}
            }

            ItemStack hand = player.getInventory().getItemInMainHand();
            Material icon = (hand != null && !hand.getType().isAir()) ? hand.getType() : Material.COMPASS;

            plugin.getWarpManager().setWarp(id, name, category, icon, player.getLocation(), null, delay, "Warp " + name)
                    .thenAccept(success -> {
                        if (success) {
                            player.sendMessage(mm.deserialize("<green>✔ Berhasil membuat warp <yellow>" + name + "</yellow> (Kategori: " + category + ") di lokasi kamu!</green>"));
                        } else {
                            player.sendMessage(mm.deserialize("<red>✖ Gagal menyimpan warp.</red>"));
                        }
                    });
            return true;
        }

        if (sub.equals("del") || sub.equals("delete") || sub.equals("remove")) {
            if (!player.hasPermission("apexsionscore.warp.admin") && !player.hasPermission("kingdomcore.admin")) {
                player.sendMessage(mm.deserialize("<red>Kamu tidak memiliki izin untuk menghapus warp.</red>"));
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(mm.deserialize("<gray>Penggunaan: </gray><yellow>/warp delete <nama></yellow>"));
                return true;
            }
            String id = args[1].toLowerCase();
            if (plugin.getWarpManager().deleteWarp(id)) {
                player.sendMessage(mm.deserialize("<green>✔ Warp <yellow>" + id + "</yellow> berhasil dihapus.</green>"));
            } else {
                player.sendMessage(mm.deserialize("<red>✖ Warp <yellow>" + id + "</yellow> tidak ditemukan.</red>"));
            }
            return true;
        }

        if (sub.equals("list") || sub.equals("menu") || sub.equals("gui")) {
            player.openInventory(new WarpGUI(plugin, player).getInventory());
            return true;
        }

        // Otherwise, args[0] is the warp name
        Warp warp = plugin.getWarpManager().getWarp(sub);
        if (warp != null) {
            plugin.getWarpManager().teleportPlayer(player, warp);
        } else {
            player.sendMessage(mm.deserialize("<red>Warp '<yellow>" + sub + "</yellow>' tidak ditemukan. Ketik <yellow>/warp</yellow> untuk melihat daftar warp.</red>"));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (Warp w : plugin.getWarpManager().getWarps()) {
                if (sender instanceof Player p && !w.canAccess(p)) continue;
                suggestions.add(w.getId());
            }
            if (sender.hasPermission("apexsionscore.warp.admin") || sender.hasPermission("kingdomcore.admin")) {
                suggestions.addAll(Arrays.asList("admin", "set", "delete", "list"));
            }
            return filter(suggestions, args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("delete"))) {
            return filter(new ArrayList<>(plugin.getWarpManager().getWarps().stream().map(Warp::getId).toList()), args[1]);
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("create"))) {
            return filter(Arrays.asList("SERVER", "RESOURCE", "EVENT", "KINGDOM", "PVP", "GENERAL"), args[2]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String input) {
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(input.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }
}
