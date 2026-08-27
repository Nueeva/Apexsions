package com.apex.economy.command;

import com.apex.economy.ApexsionsEconomy;
import com.apex.economy.currency.Currency;
import com.apex.economy.gui.AuctionBrowseMenu;
import com.apex.economy.gui.MyAuctionsMenu;
import com.apex.economy.util.NumberFormatUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AuctionCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsEconomy plugin;

    public AuctionCommand(ApexsionsEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cPerintah ini hanya dapat digunakan oleh player!");
            return true;
        }

        if (args.length == 0) {
            new AuctionBrowseMenu(plugin, player).open();
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("my") || sub.equals("lelangku") || sub.equals("kelola") || sub.equals("manage")) {
            new MyAuctionsMenu(plugin, player, null).open();
            return true;
        }

        if (sub.equals("sell") || sub.equals("list") || sub.equals("jual")) {
            if (args.length < 2) {
                player.sendMessage("§cPenggunaan: /ah sell <harga> [rupiah|diamond] [durasi_jam]");
                player.sendMessage("§7Contoh: §e/ah sell 10k rupiah 24");
                return true;
            }

            ItemStack inHand = player.getInventory().getItemInMainHand();
            if (inHand == null || inHand.getType() == Material.AIR) {
                player.sendMessage("§c[!] Harap pegang barang yang ingin Anda jual di tangan utama!");
                return true;
            }

            double price;
            try {
                price = NumberFormatUtil.parse(args[1]);
                if (price <= 0 || Double.isNaN(price) || Double.isInfinite(price)) {
                    player.sendMessage("§c[!] Harga harus berupa angka positif yang valid!");
                    return true;
                }
            } catch (Exception e) {
                player.sendMessage("§c[!] Format harga tidak valid! Contoh: 1000, 10k, 1.5jt, 2m.");
                return true;
            }

            String currName = (args.length >= 3) ? args[2].toLowerCase() : "rupiah";
            Currency curr = plugin.getCurrencyRegistry().get(currName);
            if (curr == null) {
                player.sendMessage("§c[!] Mata uang '" + currName + "' tidak dikenali! Pilihan: rupiah, diamond.");
                return true;
            }

            int durationHours = 24;
            if (args.length >= 4) {
                try {
                    durationHours = Integer.parseInt(args[3]);
                    if (durationHours < 1 || durationHours > 72) {
                        player.sendMessage("§c[!] Durasi lelang harus berada di antara 1 hingga 72 jam.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§c[!] Format durasi jam harus berupa angka!");
                    return true;
                }
            }

            plugin.getAuctionService().createAuction(player, inHand, curr, price, durationHours);
            return true;
        }

        if (sub.equals("help") || sub.equals("bantuan")) {
            player.sendMessage("§8§m----------------------------------------");
            player.sendMessage("§6§lBANTUAN AUCTION HOUSE (LELANG):");
            player.sendMessage(" §e/ah §7- Membuka pasar lelang (GUI)");
            player.sendMessage(" §e/ah sell <harga> [mata_uang] [jam] §7- Menjual barang yang dipegang");
            player.sendMessage(" §e/ah my §7- Mengelola barang lelang milik Anda");
            player.sendMessage("§8§m----------------------------------------");
            return true;
        }

        new AuctionBrowseMenu(plugin, player).open();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("sell", "my", "help");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("sell") || args[0].equalsIgnoreCase("jual") || args[0].equalsIgnoreCase("list"))) {
            return List.of("1000", "5000", "10k", "50k", "100k", "1jt");
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("sell") || args[0].equalsIgnoreCase("jual") || args[0].equalsIgnoreCase("list"))) {
            return List.of("rupiah", "diamond");
        }
        if (args.length == 4 && (args[0].equalsIgnoreCase("sell") || args[0].equalsIgnoreCase("jual") || args[0].equalsIgnoreCase("list"))) {
            return List.of("12", "24", "48");
        }
        return List.of();
    }
}
