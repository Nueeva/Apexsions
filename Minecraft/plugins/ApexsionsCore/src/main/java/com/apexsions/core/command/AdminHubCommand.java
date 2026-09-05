package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.admin.AdminModule;
import com.apexsions.core.gui.admin.PlayerInspectorGUI;
import com.apexsions.core.gui.admin.PlayerManagerGUI;
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
import java.util.Optional;

/**
 * Handles master admin hub commands: /admingui, /apexadmin, /aadmin, /aa.
 */
public class AdminHubCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public AdminHubCommand(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize("<red>Perintah /admingui hanya dapat digunakan oleh pemain di dalam game!</red>"));
            return true;
        }

        if (!player.hasPermission("apexsions.admin.gui") && !player.hasPermission("apexsions.admin") && !player.isOp()) {
            player.sendMessage(mm.deserialize("<red>🔒 Kamu tidak memiliki hak akses untuk membuka Master Admin Hub!</red>"));
            return true;
        }

        if (args.length == 0) {
            plugin.getAdminHubManager().openHub(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("reload")) {
            player.performCommand("ac reload");
            return true;
        }

        if (sub.equals("player") || sub.equals("inspect")) {
            if (args.length >= 2) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    new PlayerInspectorGUI(plugin, player, target).open();
                    return true;
                } else {
                    player.sendMessage(mm.deserialize("<red>Pemain '" + args[1] + "' tidak ditemukan atau sedang offline.</red>"));
                    return true;
                }
            } else {
                new PlayerManagerGUI(plugin, player).open();
                return true;
            }
        }

        Optional<AdminModule> moduleOpt = plugin.getAdminHubManager().getModule(sub);
        if (moduleOpt.isPresent()) {
            AdminModule module = moduleOpt.get();
            if (player.hasPermission(module.getPermission()) || player.isOp() || player.hasPermission("apexsions.admin")) {
                module.open(player);
            } else {
                player.sendMessage(mm.deserialize("<red>🔒 Kamu tidak memiliki izin <yellow>" + module.getPermission() + "</yellow> untuk membuka modul ini!</red>"));
            }
        } else {
            plugin.getAdminHubManager().openHub(player);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("reload");
            list.add("player");
            list.add("inspect");
            for (AdminModule m : plugin.getAdminHubManager().getAllModules()) {
                list.add(m.getId());
            }
            String query = args[0].toLowerCase();
            return list.stream().filter(s -> s.toLowerCase().startsWith(query)).toList();
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("player") || args[0].equalsIgnoreCase("inspect"))) {
            String query = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(query))
                    .toList();
        }

        return Collections.emptyList();
    }
}
