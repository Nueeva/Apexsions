package com.apexsions.crates.commands;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.crate.Crate;
import com.apexsions.crates.gui.CratePreviewGUI;
import com.apexsions.crates.gui.CrateVirtualKeysGUI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CrateCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCratesPlugin plugin;

    public CrateCommand(ApexsionsCratesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Perintah ini hanya bisa dijalankan oleh pemain.</red>"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("keys")) {
            new CrateVirtualKeysGUI(plugin, player).open();
            return true;
        }

        if (args[0].equalsIgnoreCase("preview")) {
            if (args.length < 2) {
                // Open preview of first crate or help
                Crate first = plugin.getCrateManager().getCrates().stream().findFirst().orElse(null);
                if (first != null) {
                    new CratePreviewGUI(plugin, player, first).open();
                } else {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Belum ada peti yang terdaftar.</red>"));
                }
                return true;
            }

            Crate crate = plugin.getCrateManager().getCrate(args[1]);
            if (crate == null) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Peti <yellow>" + args[1] + "</yellow> tidak ditemukan.</red>"));
                return true;
            }

            new CratePreviewGUI(plugin, player, crate).open();
            return true;
        }

        if (args[0].equalsIgnoreCase("open")) {
            if (args.length < 2) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Penggunaan: /crate open <id_peti></red>"));
                return true;
            }

            Crate crate = plugin.getCrateManager().getCrate(args[1]);
            if (crate == null) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Peti <yellow>" + args[1] + "</yellow> tidak ditemukan.</red>"));
                return true;
            }

            // Attempt virtual open
            plugin.openCrate(player, crate, true);
            return true;
        }

        // Help
        player.sendMessage(MiniMessage.miniMessage().deserialize("""
            <gradient:#f39c12:#e74c3c><bold>=== APEXSIONS CRATES MENU ===</bold></gradient>
            <yellow>/crate keys</yellow> <gray>- Buka dompet saldo kunci peti Anda.</gray>
            <yellow>/crate preview <peti></yellow> <gray>- Lihat daftar hadiah dan peluang drop.</gray>
            <yellow>/crate open <peti></yellow> <gray>- Buka peti menggunakan saldo kunci virtual.</gray>
        """));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> sub = List.of("keys", "preview", "open");
            for (String s : sub) {
                if (s.toLowerCase().startsWith(args[0].toLowerCase())) completions.add(s);
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("preview") || args[0].equalsIgnoreCase("open"))) {
            for (Crate c : plugin.getCrateManager().getCrates()) {
                if (c.getId().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(c.getId());
                }
            }
        }
        return completions;
    }
}
