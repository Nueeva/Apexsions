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
            case "reload" -> {
                if (!player.hasPermission("apexsions.fishing.admin")) {
                    player.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk perintah reload.</red>"));
                    return true;
                }
                plugin.reloadConfig();
                plugin.getLootGenerator().reload();
                plugin.getRodManager().reload();
                player.sendMessage(mm.deserialize("<green><bold>RELOAD SUKSES!</bold> Seluruh konfigurasi ApexsionsFishing telah diperbarui.</green>"));
            }
            default -> {
                player.sendMessage(mm.deserialize("<yellow>Perintah tidak dikenal. Ketik <gold>/fish</gold> untuk membuka menu utama.</yellow>"));
            }
        }
        return true;
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

        return List.of();
    }
}
