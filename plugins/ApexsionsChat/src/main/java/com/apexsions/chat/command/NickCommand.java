package com.apexsions.chat.command;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.chat.nick.NicknameData;
import com.apexsions.chat.nick.NicknameService;
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

import java.util.*;

/**
 * Command executor and tab completer for /nick, /nickname, and token management.
 */
public class NickCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public NickCommand(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            // Console handling for token rewards
            if (args.length >= 4 && args[0].equalsIgnoreCase("token") && args[1].equalsIgnoreCase("give")) {
                handleTokenGive(sender, args[2], args[3]);
                return true;
            }
            sender.sendMessage(miniMessage.deserialize("<red>Perintah ini hanya dapat digunakan oleh pemain dalam game!</red>"));
            return true;
        }

        NicknameService service = plugin.getNicknameService();

        // 1. /nick (no args) -> Open color GUI if nickname exists, or show help
        if (args.length == 0) {
            NicknameData data = service.getNicknameData(player.getUniqueId());
            if (data.hasNickname()) {
                plugin.getNickColorGUI().open(player);
            } else {
                sendHelp(player);
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        // 2. /nick color OR /nick gui
        if (sub.equals("color") || sub.equals("gui") || sub.equals("warna")) {
            plugin.getNickColorGUI().open(player);
            return true;
        }

        // 3. /nick reset OR /nick off
        if (sub.equals("reset") || sub.equals("off") || sub.equals("clear") || sub.equals("hapus")) {
            NicknameService.NicknameResult res = service.resetNickname(player);
            player.sendMessage(miniMessage.deserialize(res.message()));
            return true;
        }

        // 4. /nick token ...
        if (sub.equals("token") || sub.equals("tokens")) {
            if (args.length == 1) {
                int tokens = service.getTokens(player.getUniqueId());
                player.sendMessage(miniMessage.deserialize(
                        "<gradient:#ffeaa7:#55efc4><bold>✦ SALDO TOKEN GANTI NAMA ✦</bold></gradient>\n" +
                        "<gray>Kamu memiliki: <gold><bold>" + tokens + " Token</bold></gold>.</gray>\n" +
                        "<gray>Gunakan <yellow>/nick <nama_baru></yellow> untuk mengganti nama (1 Token/ganti).</gray>"
                ));
                return true;
            }

            if (args[1].equalsIgnoreCase("give")) {
                if (!player.hasPermission("apexsions.nick.admin") && !player.isOp()) {
                    player.sendMessage(miniMessage.deserialize("<red>Kamu tidak memiliki izin untuk memberikan token!</red>"));
                    return true;
                }
                if (args.length < 4) {
                    player.sendMessage(miniMessage.deserialize("<red>Penggunaan: <yellow>/nick token give <player> <jumlah></yellow></red>"));
                    return true;
                }
                handleTokenGive(player, args[2], args[3]);
                return true;
            }

            if (args[1].equalsIgnoreCase("item")) {
                if (!player.hasPermission("apexsions.nick.admin") && !player.isOp()) {
                    player.sendMessage(miniMessage.deserialize("<red>Kamu tidak memiliki izin untuk membuat item token!</red>"));
                    return true;
                }
                if (args.length < 4) {
                    player.sendMessage(miniMessage.deserialize("<red>Penggunaan: <yellow>/nick token item <player> <jumlah></yellow></red>"));
                    return true;
                }
                handleTokenItemGive(player, args[2], args[3]);
                return true;
            }
        }

        // 5. /nick setother <player> <nama>
        if (sub.equals("setother")) {
            if (!player.hasPermission("apexsions.nick.admin") && !player.isOp()) {
                player.sendMessage(miniMessage.deserialize("<red>Kamu tidak memiliki izin untuk mengubah nama pemain lain!</red>"));
                return true;
            }
            if (args.length < 3) {
                player.sendMessage(miniMessage.deserialize("<red>Penggunaan: <yellow>/nick setother <player> <nama></yellow></red>"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(miniMessage.deserialize("<red>Pemain <yellow>" + args[1] + "</yellow> tidak sedang online!</red>"));
                return true;
            }
            NicknameService.NicknameResult res = service.setOtherNickname(target, args[2]);
            player.sendMessage(miniMessage.deserialize(res.message()));
            target.sendMessage(miniMessage.deserialize("<green>Nama panggilanmu telah diatur oleh staff menjadi <yellow>" + args[2] + "</yellow>!</green>"));
            return true;
        }

        // 6. /nick resetother <player>
        if (sub.equals("resetother")) {
            if (!player.hasPermission("apexsions.nick.admin") && !player.isOp()) {
                player.sendMessage(miniMessage.deserialize("<red>Kamu tidak memiliki izin untuk mereset nama pemain lain!</red>"));
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(miniMessage.deserialize("<red>Penggunaan: <yellow>/nick resetother <player></yellow></red>"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(miniMessage.deserialize("<red>Pemain <yellow>" + args[1] + "</yellow> tidak sedang online!</red>"));
                return true;
            }
            NicknameService.NicknameResult res = service.resetOtherNickname(target);
            player.sendMessage(miniMessage.deserialize(res.message()));
            target.sendMessage(miniMessage.deserialize("<yellow>Nama panggilanmu telah direset kembali ke nama asli oleh staff.</yellow>"));
            return true;
        }

        // 7. /nick <nama_baru> -> Default name change
        String newNick = args[0];
        NicknameService.NicknameResult res = service.setNickname(player, newNick);
        player.sendMessage(miniMessage.deserialize(res.message()));

        // If successfully changed, automatically open Color GUI for immediate customization!
        if (res.success()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    plugin.getNickColorGUI().open(player);
                }
            }, 10L);
        }

        return true;
    }

    private void handleTokenGive(CommandSender sender, String targetName, String amountStr) {
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(miniMessage.deserialize("<red>Jumlah token harus berupa angka positif!</red>"));
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target != null && target.isOnline()) {
            plugin.getNicknameService().addTokens(target.getUniqueId(), target.getName(), amount);
            sender.sendMessage(miniMessage.deserialize("<green>✓ Berhasil memberikan <yellow>" + amount + " Token Ganti Nama</yellow> kepada <white>" + target.getName() + "</white>!</green>"));
            target.sendMessage(miniMessage.deserialize(
                    "<gradient:#ffeaa7:#55efc4><bold>🎁 REWARD TOKEN GANTI NAMA:</bold></gradient> <green>Kamu mendapatkan <yellow><bold>+" + amount + " Token Ganti Nama</bold></yellow>!</green>\n" +
                    "<gray>Gunakan <yellow>/nick <nama_baru></yellow> untuk mengubah nama panggilanmu.</gray>"
            ));
        } else {
            // Offline player support
            org.bukkit.OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
            if (op.hasPlayedBefore() || op.isOnline()) {
                plugin.getNicknameService().addTokens(op.getUniqueId(), op.getName(), amount);
                sender.sendMessage(miniMessage.deserialize("<green>✓ Berhasil memberikan <yellow>" + amount + " Token Ganti Nama</yellow> kepada pemain offline <white>" + op.getName() + "</white>!</green>"));
            } else {
                sender.sendMessage(miniMessage.deserialize("<red>Pemain <yellow>" + targetName + "</yellow> tidak ditemukan!</red>"));
            }
        }
    }

    private void handleTokenItemGive(CommandSender sender, String targetName, String amountStr) {
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(miniMessage.deserialize("<red>Jumlah token harus berupa angka positif!</red>"));
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(miniMessage.deserialize("<red>Pemain <yellow>" + targetName + "</yellow> harus sedang online untuk menerima voucher item!</red>"));
            return;
        }

        ItemStack voucher = plugin.getNicknameService().createTokenItem(amount);
        target.getInventory().addItem(voucher);
        sender.sendMessage(miniMessage.deserialize("<green>✓ Berhasil memberikan voucher <yellow>+" + amount + " Token</yellow> kepada <white>" + target.getName() + "</white>!</green>"));
        target.sendMessage(miniMessage.deserialize("<green>Kamu menerima <yellow>Voucher Token Ganti Nama (+" + amount + ")</yellow> di inventory-mu! Klik Kanan untuk klaim.</green>"));
    }

    private void sendHelp(Player player) {
        int tokens = plugin.getNicknameService().getTokens(player.getUniqueId());
        player.sendMessage(miniMessage.deserialize(
                "<gradient:#f1c40f:#e67e22><bold>👑 SISTEM NICKNAME APEXSIONS 👑</bold></gradient>\n" +
                "<gray>Saldo Token:</gray> <gold><bold>" + tokens + " Token</bold></gold>\n" +
                "<yellow>/nick <nama_baru></yellow> <gray>- Mengganti nama panggilan (Biaya: 1 Token)</gray>\n" +
                "<yellow>/nick color</yellow> <gray>- Membuka menu pemilihan warna & gradien</gray>\n" +
                "<yellow>/nick reset</yellow> <gray>- Mengembalikan ke nama asli (Gratis)</gray>\n" +
                "<yellow>/nick token</yellow> <gray>- Mengecek sisa saldo token kamu</gray>\n" +
                "<yellow>/realname <nickname></yellow> <gray>- Cek pemilik asli nama panggilan</gray>"
        ));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("color", "reset", "token", "off"));
            if (sender.hasPermission("apexsions.nick.admin") || sender.isOp()) {
                list.add("setother");
                list.add("resetother");
            }
            for (String s : list) {
                if (s.toLowerCase().startsWith(args[0].toLowerCase())) completions.add(s);
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("token")) {
            if (sender.hasPermission("apexsions.nick.admin") || sender.isOp()) {
                return Arrays.asList("give", "item");
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("token") && (args[1].equalsIgnoreCase("give") || args[1].equalsIgnoreCase("item"))) {
            return null; // suggest player names
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("token")) {
            return Arrays.asList("1", "2", "5", "10");
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("setother") || args[0].equalsIgnoreCase("resetother"))) {
            return null; // suggest player names
        }

        return completions;
    }
}
