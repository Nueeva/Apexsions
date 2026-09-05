package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Command to apply vanilla and custom enchantments exceeding vanilla limits.
 * Default max formula: vanilla_max_level * multiplier (4x), with configurable overrides (e.g. protection 12).
 *
 * Syntax:
 *   /enchant <enchantment> <level>
 *   /enchant <player> <enchantment> <level>
 *   /enchant remove <enchantment> (or level 0)
 */
public class EnchantCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public EnchantCommand(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("apexsionscore.command.enchant") && !sender.hasPermission("apexsionscore.admin")) {
            sender.sendMessage(miniMessage.deserialize("<red>Kamu tidak memiliki izin untuk menggunakan perintah ini.</red>"));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        Player targetPlayer = null;
        Enchantment enchantment = null;
        int level = 1;

        // Check if first argument is an online player
        Player candidatePlayer = Bukkit.getPlayerExact(args[0]);

        if (candidatePlayer != null && args.length >= 2 && matchEnchantment(args[0]) == null) {
            // First argument is clearly a player
            targetPlayer = candidatePlayer;
            enchantment = matchEnchantment(args[1]);
            if (enchantment == null) {
                sender.sendMessage(miniMessage.deserialize("<red>Enchantment '<yellow>" + args[1] + "</yellow>' tidak ditemukan!</red>"));
                return true;
            }
            if (args.length >= 3) {
                try {
                    level = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(miniMessage.deserialize("<red>Level enchantment harus berupa angka bulat!</red>"));
                    return true;
                }
            }
        } else if (candidatePlayer != null && args.length >= 3) {
            // Both player and enchantment might match, but 3 args provided -> player is first
            targetPlayer = candidatePlayer;
            enchantment = matchEnchantment(args[1]);
            if (enchantment == null) {
                sender.sendMessage(miniMessage.deserialize("<red>Enchantment '<yellow>" + args[1] + "</yellow>' tidak ditemukan!</red>"));
                return true;
            }
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(miniMessage.deserialize("<red>Level enchantment harus berupa angka bulat!</red>"));
                return true;
            }
        } else {
            // First argument is enchantment (or subaction like remove)
            if (!(sender instanceof Player playerSender)) {
                sender.sendMessage(miniMessage.deserialize("<red>Console harus menyertakan target pemain: /" + label + " <player> <enchantment> <level></red>"));
                return true;
            }
            targetPlayer = playerSender;

            if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")) {
                if (args.length < 2) {
                    sender.sendMessage(miniMessage.deserialize("<red>Gunakan: /" + label + " remove <enchantment></red>"));
                    return true;
                }
                enchantment = matchEnchantment(args[1]);
                if (enchantment == null) {
                    sender.sendMessage(miniMessage.deserialize("<red>Enchantment '<yellow>" + args[1] + "</yellow>' tidak ditemukan!</red>"));
                    return true;
                }
                level = 0;
            } else {
                enchantment = matchEnchantment(args[0]);
                if (enchantment == null) {
                    sender.sendMessage(miniMessage.deserialize("<red>Enchantment '<yellow>" + args[0] + "</yellow>' tidak ditemukan!</red>"));
                    return true;
                }
                if (args.length >= 2) {
                    try {
                        level = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(miniMessage.deserialize("<red>Level enchantment harus berupa angka bulat!</red>"));
                        return true;
                    }
                }
            }
        }

        ItemStack item = targetPlayer.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            if (targetPlayer.equals(sender)) {
                sender.sendMessage(miniMessage.deserialize("<red>Kamu harus memegang item di tangan utama untuk diberi enchantment!</red>"));
            } else {
                sender.sendMessage(miniMessage.deserialize("<red>Pemain <yellow>" + targetPlayer.getName() + "</yellow> tidak memegang item apapun di tangan utama!</red>"));
            }
            return true;
        }

        // Removal if level is 0
        if (level == 0) {
            if (item.containsEnchantment(enchantment)) {
                item.removeEnchantment(enchantment);
                sender.sendMessage(miniMessage.deserialize("<green>✓ Berhasil menghapus enchantment <yellow>" + formatEnchantName(enchantment) + "</yellow> dari item!</green>"));
                if (!targetPlayer.equals(sender)) {
                    targetPlayer.sendMessage(miniMessage.deserialize("<green>✓ Enchantment <yellow>" + formatEnchantName(enchantment) + "</yellow> telah dihapus dari itemmu oleh admin.</green>"));
                }
            } else {
                sender.sendMessage(miniMessage.deserialize("<yellow>Item tersebut tidak memiliki enchantment <gold>" + formatEnchantName(enchantment) + "</gold>.</yellow>"));
            }
            return true;
        }

        if (level < 0) {
            sender.sendMessage(miniMessage.deserialize("<red>Level enchantment tidak boleh bernilai negatif!</red>"));
            return true;
        }

        int maxAllowed = getMaxAllowedLevel(enchantment);
        int hardCap = plugin.getConfigManager().getEnchantMaxAbsoluteLevel();

        if (level > maxAllowed) {
            boolean hasBypass = sender.hasPermission("apexsionscore.enchant.bypass") || sender.hasPermission("apexsionscore.admin");
            if (hasBypass) {
                if (level > hardCap) {
                    sender.sendMessage(miniMessage.deserialize("<red>Level <gold>" + level + "</gold> melebihi batas mutlak server (<gold>" + hardCap + "</gold>)!</red>"));
                    return true;
                }
            } else {
                sender.sendMessage(miniMessage.deserialize("<red>Level enchantment melebihi batas maksimal (<gold>" + maxAllowed + "</gold> untuk <yellow>" + formatEnchantName(enchantment) + "</yellow>)!</red>"));
                return true;
            }
        }

        // Apply enchantment safely
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.addEnchant(enchantment, level, true);
                item.setItemMeta(meta);
            } else {
                item.addUnsafeEnchantment(enchantment, level);
            }

            String romanOrNum = toRomanOrNumber(level);
            sender.sendMessage(miniMessage.deserialize("<green>✓ Berhasil memberikan <gold>" + formatEnchantName(enchantment) + " " + romanOrNum + "</gold> (Level <yellow>" + level + "</yellow>) ke item" + (targetPlayer.equals(sender) ? "!" : " milik <yellow>" + targetPlayer.getName() + "</yellow>!")));
            if (!targetPlayer.equals(sender)) {
                targetPlayer.sendMessage(miniMessage.deserialize("<green>✓ Item yang kamu pegang telah diberi enchantment <gold>" + formatEnchantName(enchantment) + " " + romanOrNum + "</gold> oleh admin.</green>"));
            }
        } catch (Exception e) {
            sender.sendMessage(miniMessage.deserialize("<red>Gagal menerapkan enchantment: " + e.getMessage() + "</red>"));
        }

        return true;
    }

    public int getMaxAllowedLevel(Enchantment enchantment) {
        String key = enchantment.getKey().getKey().toLowerCase();
        int override = plugin.getConfigManager().getEnchantOverride(key);
        if (override > 0) {
            return override;
        }
        int multiplier = plugin.getConfigManager().getEnchantMultiplier();
        if (multiplier <= 0) multiplier = 4;
        return Math.max(1, enchantment.getMaxLevel() * multiplier);
    }

    @Nullable
    public static Enchantment matchEnchantment(String query) {
        if (query == null || query.isBlank()) return null;
        String clean = query.toLowerCase().trim();
        if (clean.startsWith("minecraft:")) {
            clean = clean.substring("minecraft:".length());
        }

        NamespacedKey key = NamespacedKey.minecraft(clean);
        Enchantment byKey = Registry.ENCHANTMENT.get(key);
        if (byKey != null) return byKey;

        String stripped = clean.replace("_", "").replace("-", "");
        for (Enchantment ench : Registry.ENCHANTMENT) {
            String eKey = ench.getKey().getKey().toLowerCase();
            if (eKey.equals(clean) || eKey.replace("_", "").equals(stripped)) {
                return ench;
            }
        }
        return null;
    }

    public static String formatEnchantName(Enchantment enchantment) {
        String key = enchantment.getKey().getKey();
        String[] words = key.replace('_', ' ').split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase()).append(" ");
        }
        return sb.toString().trim();
    }

    public static String toRomanOrNumber(int number) {
        if (number <= 0) return String.valueOf(number);
        if (number > 100) return String.valueOf(number);

        int[] values = {100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] numerals = {"C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                result.append(numerals[i]);
                number -= values[i];
            }
        }
        return result.toString();
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(miniMessage.deserialize("<gold><bold>=== Apexsions Custom Enchant ===</bold></gold>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/" + label + " <enchantment> <level></yellow> <gray>- Beri enchant pada item di tangan</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/" + label + " <player> <enchantment> <level></yellow> <gray>- Beri enchant pada item pemain lain</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/" + label + " remove <enchantment></yellow> <gray>- Hapus enchant dari item di tangan</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gray>Batas level: <gold>" + plugin.getConfigManager().getEnchantMultiplier() + "x level vanilla</gold> (cth: Sharpness 20, Protection 12, Mending 4).</gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("apexsionscore.command.enchant") && !sender.hasPermission("apexsionscore.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            // Suggest enchantments
            for (Enchantment ench : Registry.ENCHANTMENT) {
                suggestions.add(ench.getKey().getKey());
            }
            // Also suggest online players if sender can enchant others
            for (Player p : Bukkit.getOnlinePlayers()) {
                suggestions.add(p.getName());
            }
            suggestions.add("remove");
            return filter(suggestions, args[0]);
        }

        if (args.length == 2) {
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target != null) {
                // Arg 0 is a player, suggest enchantments
                List<String> list = new ArrayList<>();
                for (Enchantment ench : Registry.ENCHANTMENT) {
                    list.add(ench.getKey().getKey());
                }
                return filter(list, args[1]);
            } else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")) {
                List<String> list = new ArrayList<>();
                if (sender instanceof Player p) {
                    ItemStack item = p.getInventory().getItemInMainHand();
                    for (Enchantment ench : item.getEnchantments().keySet()) {
                        list.add(ench.getKey().getKey());
                    }
                }
                if (list.isEmpty()) {
                    for (Enchantment ench : Registry.ENCHANTMENT) {
                        list.add(ench.getKey().getKey());
                    }
                }
                return filter(list, args[1]);
            } else {
                // Arg 0 is an enchantment, suggest levels
                Enchantment ench = matchEnchantment(args[0]);
                if (ench != null) {
                    int max = getMaxAllowedLevel(ench);
                    int vanilla = ench.getMaxLevel();
                    List<String> levels = new ArrayList<>();
                    levels.add("1");
                    if (vanilla != max && vanilla > 1) levels.add(String.valueOf(vanilla));
                    levels.add(String.valueOf(max));
                    return filter(levels, args[1]);
                }
            }
        }

        if (args.length == 3) {
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target != null) {
                Enchantment ench = matchEnchantment(args[1]);
                if (ench != null) {
                    int max = getMaxAllowedLevel(ench);
                    int vanilla = ench.getMaxLevel();
                    List<String> levels = new ArrayList<>();
                    levels.add("1");
                    if (vanilla != max && vanilla > 1) levels.add(String.valueOf(vanilla));
                    levels.add(String.valueOf(max));
                    return filter(levels, args[2]);
                }
            }
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String input) {
        String lower = input.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(lower)) {
                result.add(s);
            }
        }
        return result;
    }
}
