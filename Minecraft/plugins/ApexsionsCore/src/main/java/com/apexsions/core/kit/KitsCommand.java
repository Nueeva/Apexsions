package com.apexsions.core.kit;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Universal command handler for /kits and /kit.
 */
public class KitsCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public KitsCommand(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(mm.deserialize("<red>Command ini hanya dapat dijalankan oleh pemain!</red>"));
                return true;
            }
            new KitUserGUI(plugin, player).open();
            return true;
        }

        String sub = args[0].toLowerCase();

        // 1. Preview
        if (sub.equals("preview")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(mm.deserialize("<red>Command ini hanya dapat dijalankan oleh pemain!</red>"));
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(mm.deserialize("<red>Format: /kits preview <kitId></red>"));
                return true;
            }
            Kit kit = plugin.getKitManager().getKit(args[1]);
            if (kit == null) {
                player.sendMessage(mm.deserialize("<red>Kit '" + args[1] + "' tidak ditemukan!</red>"));
                return true;
            }
            new KitPreviewGUI(plugin, player, kit).open();
            return true;
        }

        // 2. Claim
        if (sub.equals("claim")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(mm.deserialize("<red>Command ini hanya dapat dijalankan oleh pemain!</red>"));
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(mm.deserialize("<red>Format: /kits claim <kitId></red>"));
                return true;
            }
            Kit kit = plugin.getKitManager().getKit(args[1]);
            if (kit == null) {
                player.sendMessage(mm.deserialize("<red>Kit '" + args[1] + "' tidak ditemukan!</red>"));
                return true;
            }
            if (!plugin.getKitManager().claimKit(player, kit)) {
                long cd = plugin.getKitManager().getRemainingCooldownSeconds(player, kit);
                if (cd > 0) {
                    player.sendMessage(mm.deserialize("<red>⏳ Kit ini sedang cooldown! Tunggu: <yellow>" + plugin.getKitManager().formatRemainingCooldown(cd) + "</yellow></red>"));
                } else {
                    player.sendMessage(mm.deserialize("<red>✖ Kamu belum memenuhi syarat rank untuk mengklaim kit ini (" + kit.getRequiredRank().toUpperCase() + ")!</red>"));
                }
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
            return true;
        }

        // 3. Admin subcommands
        if (!sender.hasPermission("apexsions.admin") && !sender.isOp()) {
            sender.sendMessage(mm.deserialize("<red>Kamu tidak memiliki izin untuk menggunakan perintah admin ini!</red>"));
            return true;
        }

        switch (sub) {
            case "create" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(mm.deserialize("<red>Command ini hanya dapat dijalankan oleh pemain!</red>"));
                    return true;
                }
                String id = args.length > 1 ? args[1].toLowerCase() : "kit_" + (System.currentTimeMillis() % 10000);
                new KitAdminCreatorGUI(plugin, player, null).open();
            }
            case "edit" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(mm.deserialize("<red>Command ini hanya dapat dijalankan oleh pemain!</red>"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(mm.deserialize("<red>Format: /kits edit <kitId></red>"));
                    return true;
                }
                Kit kit = plugin.getKitManager().getKit(args[1]);
                if (kit == null) {
                    player.sendMessage(mm.deserialize("<red>Kit '" + args[1] + "' tidak ditemukan!</red>"));
                    return true;
                }
                new KitAdminCreatorGUI(plugin, player, kit).open();
            }
            case "delete" -> {
                if (args.length < 2) {
                    sender.sendMessage(mm.deserialize("<red>Format: /kits delete <kitId></red>"));
                    return true;
                }
                if (plugin.getKitManager().deleteKit(args[1])) {
                    sender.sendMessage(mm.deserialize("<green>✓ Kit '" + args[1] + "' berhasil dihapus!</green>"));
                } else {
                    sender.sendMessage(mm.deserialize("<red>Kit '" + args[1] + "' tidak ditemukan!</red>"));
                }
            }
            case "give" -> {
                if (args.length < 3) {
                    sender.sendMessage(mm.deserialize("<red>Format: /kits give <player> <kitId></red>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(mm.deserialize("<red>Pemain '" + args[1] + "' tidak sedang online!</red>"));
                    return true;
                }
                Kit kit = plugin.getKitManager().getKit(args[2]);
                if (kit == null) {
                    sender.sendMessage(mm.deserialize("<red>Kit '" + args[2] + "' tidak ditemukan!</red>"));
                    return true;
                }
                // Force claim ignoring cooldown and rank
                ItemStack helm = plugin.getKitManager().prepareArmorPiece(kit, kit.getHelmet(), "Helmet");
                ItemStack chest = plugin.getKitManager().prepareArmorPiece(kit, kit.getChestplate(), "Chestplate");
                ItemStack legs = plugin.getKitManager().prepareArmorPiece(kit, kit.getLeggings(), "Leggings");
                ItemStack boots = plugin.getKitManager().prepareArmorPiece(kit, kit.getBoots(), "Boots");

                if (helm != null) target.getInventory().addItem(helm);
                if (chest != null) target.getInventory().addItem(chest);
                if (legs != null) target.getInventory().addItem(legs);
                if (boots != null) target.getInventory().addItem(boots);

                for (ItemStack it : kit.getExtraItems()) {
                    if (it != null) target.getInventory().addItem(it.clone());
                }

                target.sendMessage(mm.deserialize("<green><bold>✓</bold> Kamu menerima kit <gold>" + kit.getDisplayName() + "</gold> dari admin!</green>"));
                sender.sendMessage(mm.deserialize("<green>✓ Berhasil memberikan kit '" + kit.getId() + "' ke " + target.getName() + "!</green>"));
            }
            case "resetcooldown" -> {
                if (args.length < 3) {
                    sender.sendMessage(mm.deserialize("<red>Format: /kits resetcooldown <player> <kitId></red>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(mm.deserialize("<red>Pemain '" + args[1] + "' tidak sedang online!</red>"));
                    return true;
                }
                plugin.getKitManager().resetCooldown(target.getUniqueId(), args[2]);
                sender.sendMessage(mm.deserialize("<green>✓ Cooldown kit '" + args[2] + "' untuk pemain " + target.getName() + " berhasil di-reset!</green>"));
            }
            case "list" -> {
                sender.sendMessage(mm.deserialize("<gold><bold>════════════ DAFTAR KITS ════════════</bold></gold>"));
                for (Kit k : plugin.getKitManager().getAllKits()) {
                    sender.sendMessage(mm.deserialize("<dark_gray>•</dark_gray> <yellow>" + k.getId() + "</yellow> <gray>(Rank: <gold>" + k.getRequiredRank().toUpperCase() + "</gold>, Cooldown: " + (k.getCooldownSeconds() / 3600) + "h)</gray>"));
                }
                sender.sendMessage(mm.deserialize("<gold><bold>═════════════════════════════════════</bold></gold>"));
            }
            default -> {
                sender.sendMessage(mm.deserialize("<yellow>Command Kits: /kits [preview/claim/create/edit/delete/give/resetcooldown/list]</yellow>"));
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("preview");
            list.add("claim");
            if (sender.hasPermission("apexsions.admin") || sender.isOp()) {
                list.add("create");
                list.add("edit");
                list.add("delete");
                list.add("give");
                list.add("resetcooldown");
                list.add("list");
            }
            return filter(list, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("preview") || sub.equals("claim") || sub.equals("edit") || sub.equals("delete")) {
                for (Kit k : plugin.getKitManager().getAllKits()) {
                    list.add(k.getId());
                }
            } else if (sub.equals("give") || sub.equals("resetcooldown")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    list.add(p.getName());
                }
            }
            return filter(list, args[1]);
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give") || sub.equals("resetcooldown")) {
                for (Kit k : plugin.getKitManager().getAllKits()) {
                    list.add(k.getId());
                }
            }
            return filter(list, args[2]);
        }

        return list;
    }

    private List<String> filter(List<String> list, String prefix) {
        String p = prefix.toLowerCase();
        List<String> out = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(p)) {
                out.add(s);
            }
        }
        return out;
    }
}
