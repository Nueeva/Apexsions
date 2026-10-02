package com.apexsions.core.container;

import com.apexsions.core.api.Permissions;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Chat-command surface for container and personal inventory management.
 * Specifically adapted for Bedrock Edition players (who lack middle-click and close
 * GUIs when opening chat) with smart targeted container resolution and full inventory sorting.
 */
public class ContainerCommand implements CommandExecutor, TabCompleter {

    private final ContainerSortManager manager;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ContainerCommand(@NotNull ContainerSortManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Hanya pemain dalam game yang dapat menggunakan perintah ini.");
            return true;
        }

        String name = command.getName().toLowerCase(Locale.ROOT);
        boolean deposit = name.equals("deposit") || name.equals("quickdeposit") || label.toLowerCase(Locale.ROOT).startsWith("deposit");

        if (deposit) {
            handleDeposit(player);
        } else {
            handleSort(player, args);
        }
        return true;
    }

    private void handleSort(Player player, String[] args) {
        if (!player.hasPermission(Permissions.CONTAINER_USE)) {
            player.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk merapikan item.</red>"));
            return;
        }
        if (!manager.isSortEnabled()) {
            player.sendMessage(mm.deserialize("<red>Fitur rapikan item sedang dinonaktifkan.</red>"));
            return;
        }

        // Subcommand parsing: /sort inv [all], /sort chest, /sort all
        if (args.length > 0) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("inv") || sub.equals("inventory") || sub.equals("tas") || sub.equals("bag") || sub.equals("me")) {
                boolean all = args.length > 1 && (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("semua") || args[1].equalsIgnoreCase("hotbar"));
                sortPlayer(player, all);
                return;
            }
            if (sub.equals("all") || sub.equals("semua")) {
                sortPlayer(player, true);
                return;
            }
            if (sub.equals("chest") || sub.equals("peti") || sub.equals("box")) {
                sortChest(player);
                return;
            }
        }

        // Default smart sort (/sort):
        // 1. If currently viewing, facing, or recently opened a container -> sort that container
        Inventory container = manager.resolveTargetContainer(player);
        if (container != null) {
            sortContainerInventory(player, container);
            return;
        }

        // 2. Otherwise sort personal inventory storage (keeping hotbar intact)
        sortPlayer(player, false);
    }

    private void sortPlayer(Player player, boolean includeHotbar) {
        if (!manager.isPlayerSortEnabled()) {
            player.sendMessage(mm.deserialize("<red>Fitur rapikan tas sedang dinonaktifkan.</red>"));
            return;
        }
        int count = manager.sortPlayerInventory(player, includeHotbar);
        if (count < 0) {
            player.sendMessage(mm.deserialize("<gray>Inventory tas Anda kosong.</gray>"));
            return;
        }
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
        player.sendMessage(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>TAS DIRAPIKAN!</bold></gradient> <gray>" + count + " tumpukan item di tas disusun & digabung." + (includeHotbar ? " <gold>(Termasuk hotbar)</gold>" : "") + "</gray>"));
    }

    private void sortChest(Player player) {
        Inventory container = manager.resolveTargetContainer(player);
        if (container == null) {
            player.sendMessage(mm.deserialize("<yellow>Tidak ada peti/tong terbuka atau di depan Anda. Buka atau arahkan pandangan ke peti terlebih dahulu.</yellow>"));
            return;
        }
        sortContainerInventory(player, container);
    }

    private void sortContainerInventory(Player player, Inventory container) {
        int count = manager.sort(container);
        if (count < 0) {
            player.sendMessage(mm.deserialize("<gray>Peti sudah kosong.</gray>"));
            return;
        }
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
        player.sendMessage(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>PETI DIRAPIKAN!</bold></gradient> <gray>" + count + " jenis item disusun & digabung.</gray>"));
    }

    private void handleDeposit(Player player) {
        if (!player.hasPermission(Permissions.CONTAINER_USE)) {
            player.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk menyetor item.</red>"));
            return;
        }
        if (!manager.isDepositEnabled()) {
            player.sendMessage(mm.deserialize("<red>Fitur setor cepat sedang dinonaktifkan.</red>"));
            return;
        }
        Inventory container = manager.resolveTargetContainer(player);
        if (container == null) {
            player.sendMessage(mm.deserialize("<yellow>Buka atau arahkan pandangan ke peti/tong terlebih dahulu, lalu gunakan <gold>/deposit</gold>.</yellow>"));
            return;
        }
        int moved = manager.quickDeposit(player, container);
        if (moved <= 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<yellow>Tidak ada item yang cocok untuk disetor ke peti ini.</yellow>"));
            return;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
        player.sendMessage(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>SETOR CEPAT!</bold></gradient> <gray>" + moved + " tumpuk item dipindahkan ke peti.</gray>"));
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        String name = command.getName().toLowerCase(Locale.ROOT);
        if (name.equals("deposit") || name.equals("quickdeposit")) {
            return completions;
        }

        if (args.length == 1) {
            String token = args[0].toLowerCase(Locale.ROOT);
            for (String sub : List.of("inv", "chest", "all")) {
                if (sub.startsWith(token)) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("inv") || args[0].equalsIgnoreCase("inventory"))) {
            if ("all".startsWith(args[1].toLowerCase(Locale.ROOT))) {
                completions.add("all");
            }
        }
        return completions;
    }
}