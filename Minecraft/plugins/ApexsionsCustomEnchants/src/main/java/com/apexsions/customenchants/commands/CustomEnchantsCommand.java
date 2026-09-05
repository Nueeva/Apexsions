package com.apexsions.customenchants.commands;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
import com.apexsions.customenchants.gui.EnchanterGUI;
import com.apexsions.customenchants.gui.SpecificBookShopGUI;
import com.apexsions.customenchants.gui.TinkererComingSoonGUI;
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
import java.util.Locale;

/**
 * Main player command handler for /ce.
 */
public class CustomEnchantsCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public CustomEnchantsCommand(ApexsionsCustomEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize("<red>Perintah ini hanya dapat dijalankan oleh pemain di dalam game.</red>"));
            return true;
        }

        if (!player.hasPermission("apexsions.ce.user")) {
            player.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk menggunakan Custom Enchants.</red>"));
            return true;
        }

        if (args.length == 0) {
            new EnchanterGUI(plugin, player).open();
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "shop", "books", "beli" -> {
                new com.apexsions.customenchants.gui.ShopCategorySelectGUI(plugin, player).open();
                return true;
            }
            case "tinkerer", "tinker" -> {
                new TinkererComingSoonGUI(player).open();
                return true;
            }
            case "info", "detail" -> {
                if (args.length < 2) {
                    player.sendMessage(mm.deserialize("<red>Gunakan: /" + label + " info <nama_enchant></red>"));
                    return true;
                }
                String enchantId = args[1].toLowerCase(Locale.ROOT);
                CustomEnchant enchant = plugin.getEnchantmentRegistry().getEnchantment(enchantId);
                if (enchant == null) {
                    player.sendMessage(mm.deserialize("<red>Sihir dengan nama '</red><yellow>" + args[1] + "</yellow><red>' tidak ditemukan!</red>"));
                    return true;
                }

                player.sendMessage(mm.deserialize("<dark_gray>----------------------------------------</dark_gray>"));
                player.sendMessage(mm.deserialize("<gradient:#9b59b6:#e74c3c><bold>✨ INFORMASI CUSTOM ENCHANT ✨</bold></gradient>"));
                player.sendMessage(mm.deserialize("<gray>Nama: " + enchant.getFormattedName()));
                player.sendMessage(mm.deserialize("<gray>Tier: " + enchant.getGroup().getDisplayName()));
                player.sendMessage(mm.deserialize("<gray>Level Maks: <gold>" + CustomEnchant.toRoman(enchant.getMaxLevel()) + " (" + enchant.getMaxLevel() + ")</gold>"));
                player.sendMessage(mm.deserialize("<gray>Dapat Di-apply Ke: <aqua>" + String.join(", ", enchant.getTargets()) + "</aqua>"));
                player.sendMessage(mm.deserialize("<gray>Deskripsi: <yellow>" + enchant.getDescription() + "</yellow>"));
                player.sendMessage(mm.deserialize("<dark_gray>----------------------------------------</dark_gray>"));
                return true;
            }
            case "help", "?" -> {
                sendHelp(player, label);
                return true;
            }
            default -> {
                player.sendMessage(mm.deserialize("<red>Subcommand tidak dikenal. Ketik <yellow>/" + label + " help</yellow> untuk bantuan.</red>"));
                return true;
            }
        }
    }

    private void sendHelp(Player player, String label) {
        player.sendMessage(mm.deserialize("<dark_gray>----------------------------------------</dark_gray>"));
        player.sendMessage(mm.deserialize("<gradient:#9b59b6:#e74c3c><bold>⚡ APEXSIONS CUSTOM ENCHANTS BANTUAN ⚡</bold></gradient>"));
        player.sendMessage(mm.deserialize("<yellow>/" + label + "</yellow> <dark_gray>»</dark_gray> <gray>Buka Enchanter Gacha Utama</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/" + label + " shop</yellow> <dark_gray>»</dark_gray> <gray>Beli buku sihir spesifik (3x harga, 50% sukses)</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/" + label + " tinkerer</yellow> <dark_gray>»</dark_gray> <gray>Buka Tinkerer Kerajaan (Coming Soon)</gray>"));
        player.sendMessage(mm.deserialize("<yellow>/" + label + " info <enchant></yellow> <dark_gray>»</dark_gray> <gray>Lihat detail dan deskripsi sihir</gray>"));
        player.sendMessage(mm.deserialize("<dark_gray>----------------------------------------</dark_gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> subs = List.of("shop", "tinkerer", "info", "help");
            for (String s : subs) {
                if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(s);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            for (String id : plugin.getEnchantmentRegistry().getAllIds()) {
                if (id.toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(id);
                }
            }
        }
        return completions;
    }
}
