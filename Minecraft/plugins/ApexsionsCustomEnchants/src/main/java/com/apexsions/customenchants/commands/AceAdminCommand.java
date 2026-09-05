package com.apexsions.customenchants.commands;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
import com.apexsions.customenchants.gui.AceAdminHubGUI;
import com.apexsions.customenchants.gui.AceEnchantsCatalogGUI;
import com.apexsions.customenchants.gui.AdminItemCreatorGUI;
import com.apexsions.customenchants.gui.AdminTierPricingGUI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
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
import java.util.Locale;

/**
 * Main admin command handler for /ace.
 * Typing /ace directly opens the central admin GUI hub.
 */
public class AceAdminCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public AceAdminCommand(ApexsionsCustomEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("apexsions.admin") && !sender.hasPermission("apexsions.ace.admin")) {
            sender.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk menggunakan perintah admin Custom Enchants.</red>"));
            return true;
        }

        // Typing /ace directly with no args opens the AceAdminHubGUI
        if (args.length == 0) {
            if (sender instanceof Player player) {
                new AceAdminHubGUI(plugin, player).open();
            } else {
                sender.sendMessage(mm.deserialize("<yellow>Gunakan /" + label + " help untuk daftar perintah console.</yellow>"));
            }
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "enchants", "catalog", "ae" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(mm.deserialize("<red>Katalog GUI hanya bisa dibuka oleh pemain.</red>"));
                    return true;
                }
                int page = 1;
                String filter = null;
                if (args.length >= 2) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        filter = args[1];
                    }
                }
                if (args.length >= 3) {
                    filter = args[2];
                }
                new AceEnchantsCatalogGUI(plugin, player, page, filter).open();
                return true;
            }
            case "create", "creator", "builder" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(mm.deserialize("<red>Item Creator GUI hanya bisa dibuka oleh pemain.</red>"));
                    return true;
                }
                new AdminItemCreatorGUI(plugin, player).open();
                return true;
            }
            case "pricing", "prices", "rates" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(mm.deserialize("<red>Pricing GUI hanya bisa dibuka oleh pemain.</red>"));
                    return true;
                }
                new AdminTierPricingGUI(plugin, player).open();
                return true;
            }
            case "reload" -> {
                plugin.reload();
                sender.sendMessage(mm.deserialize("<green>ApexsionsCustomEnchants berhasil direload!</green>"));
                return true;
            }
            case "givebook" -> {
                if (args.length < 4) {
                    sender.sendMessage(mm.deserialize("<red>Gunakan: /" + label + " givebook <player> <enchant> <level> [success] [destroy]</red>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(mm.deserialize("<red>Pemain '</red><yellow>" + args[1] + "</yellow><red>' tidak online!</red>"));
                    return true;
                }
                CustomEnchant enchant = plugin.getEnchantmentRegistry().getEnchantment(args[2]);
                if (enchant == null) {
                    sender.sendMessage(mm.deserialize("<red>Enchant '</red><yellow>" + args[2] + "</yellow><red>' tidak ditemukan!</red>"));
                    return true;
                }
                int level = 1;
                int success = 100;
                int destroy = 0;
                try {
                    level = Integer.parseInt(args[3]);
                    if (args.length >= 5) success = Integer.parseInt(args[4]);
                    if (args.length >= 6) destroy = Integer.parseInt(args[5]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(mm.deserialize("<red>Level, success, dan destroy harus berupa angka valid!</red>"));
                    return true;
                }
                ItemStack book = plugin.getEnchantBookManager().createBook(enchant, level, success, destroy);
                target.getInventory().addItem(book);
                sender.sendMessage(mm.deserialize("<green>Buku sihir <gold>" + enchant.getDisplayName() + " " + level + "</gold> diberikan kepada <yellow>" + target.getName() + "</yellow>!</green>"));
                return true;
            }
            case "givedust" -> {
                if (args.length < 3) {
                    sender.sendMessage(mm.deserialize("<red>Gunakan: /" + label + " givedust <player> <mystery|magic> [rate%]</red>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(mm.deserialize("<red>Pemain '</red><yellow>" + args[1] + "</yellow><red>' tidak online!</red>"));
                    return true;
                }
                String dustType = args[2].toLowerCase(Locale.ROOT);
                ItemStack dust;
                if (dustType.equals("mystery")) {
                    dust = plugin.getMagicDustManager().createMysteryDust();
                } else if (dustType.equals("magic")) {
                    int rate = 10;
                    if (args.length >= 4) {
                        try {
                            rate = Integer.parseInt(args[3]);
                        } catch (NumberFormatException ignored) {}
                    }
                    dust = plugin.getMagicDustManager().createMagicDust(rate);
                } else {
                    sender.sendMessage(mm.deserialize("<red>Tipe dust tidak valid. Pilih: mystery atau magic.</red>"));
                    return true;
                }
                target.getInventory().addItem(dust);
                sender.sendMessage(mm.deserialize("<green>Dust diberikan kepada <yellow>" + target.getName() + "</yellow>!</green>"));
                return true;
            }
            case "givescroll" -> {
                if (args.length < 3) {
                    sender.sendMessage(mm.deserialize("<red>Gunakan: /" + label + " givescroll <player> <white|black></red>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(mm.deserialize("<red>Pemain '</red><yellow>" + args[1] + "</yellow><red>' tidak online!</red>"));
                    return true;
                }
                String scrollType = args[2].toLowerCase(Locale.ROOT);
                ItemStack scroll;
                if (scrollType.equals("white")) {
                    scroll = plugin.getScrollManager().createWhiteScroll();
                } else if (scrollType.equals("black")) {
                    scroll = plugin.getScrollManager().createBlackScroll();
                } else {
                    sender.sendMessage(mm.deserialize("<red>Tipe scroll tidak valid. Pilih: white atau black.</red>"));
                    return true;
                }
                target.getInventory().addItem(scroll);
                sender.sendMessage(mm.deserialize("<green>Scroll diberikan kepada <yellow>" + target.getName() + "</yellow>!</green>"));
                return true;
            }
            case "help", "?" -> {
                sendHelp(sender, label);
                return true;
            }
            default -> {
                sender.sendMessage(mm.deserialize("<red>Subcommand tidak dikenal. Ketik <yellow>/" + label + " help</yellow> untuk bantuan.</red>"));
                return true;
            }
        }
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(mm.deserialize("<dark_gray>----------------------------------------</dark_gray>"));
        sender.sendMessage(mm.deserialize("<gradient:#e74c3c:#f39c12><bold>⚙ APEXSIONS CUSTOM ENCHANTS ADMIN HELP ⚙</bold></gradient>"));
        sender.sendMessage(mm.deserialize("<yellow>/" + label + "</yellow> <dark_gray>»</dark_gray> <gray>Buka Central Admin GUI Hub</gray>"));
        sender.sendMessage(mm.deserialize("<yellow>/" + label + " enchants [page/filter]</yellow> <dark_gray>»</dark_gray> <gray>Katalog Custom Enchants (Replika /ae admin)</gray>"));
        sender.sendMessage(mm.deserialize("<yellow>/" + label + " create</yellow> <dark_gray>»</dark_gray> <gray>Interactive Item & Armor Set Creator GUI</gray>"));
        sender.sendMessage(mm.deserialize("<yellow>/" + label + " pricing</yellow> <dark_gray>»</dark_gray> <gray>Atur Harga Tier, Mata Uang, & Odds Gacha</gray>"));
        sender.sendMessage(mm.deserialize("<yellow>/" + label + " givebook <player> <enchant> <lvl> [suc] [des]</yellow> <dark_gray>»</dark_gray> <gray>Berikan buku enchant</gray>"));
        sender.sendMessage(mm.deserialize("<yellow>/" + label + " givedust <player> <mystery|magic> [rate]</yellow> <dark_gray>»</dark_gray> <gray>Berikan Magic/Mystery Dust</gray>"));
        sender.sendMessage(mm.deserialize("<yellow>/" + label + " givescroll <player> <white|black></yellow> <dark_gray>»</dark_gray> <gray>Berikan Protection/Extraction Scroll</gray>"));
        sender.sendMessage(mm.deserialize("<yellow>/" + label + " reload</yellow> <dark_gray>»</dark_gray> <gray>Reload seluruh konfigurasi plugin</gray>"));
        sender.sendMessage(mm.deserialize("<dark_gray>----------------------------------------</dark_gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("apexsions.admin") && !sender.hasPermission("apexsions.ace.admin")) {
            return List.of();
        }

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> subs = List.of("enchants", "create", "pricing", "reload", "givebook", "givedust", "givescroll", "help");
            for (String s : subs) {
                if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(s);
                }
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("givebook") || sub.equals("givedust") || sub.equals("givescroll")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(p.getName());
                    }
                }
            } else if (sub.equals("enchants")) {
                for (String id : plugin.getEnchantmentRegistry().getAllIds()) {
                    if (id.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(id);
                    }
                }
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("givebook")) {
                for (String id : plugin.getEnchantmentRegistry().getAllIds()) {
                    if (id.toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(id);
                    }
                }
            } else if (sub.equals("givedust")) {
                List.of("mystery", "magic").forEach(d -> {
                    if (d.startsWith(args[2].toLowerCase())) completions.add(d);
                });
            } else if (sub.equals("givescroll")) {
                List.of("white", "black").forEach(s -> {
                    if (s.startsWith(args[2].toLowerCase())) completions.add(s);
                });
            }
        }
        return completions;
    }
}
