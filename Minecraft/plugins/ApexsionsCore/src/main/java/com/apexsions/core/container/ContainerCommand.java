package com.apexsions.core.container;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Chat-command surface for container management so Bedrock players (who lack
 * middle-click and reliable keybinds) can sort and deposit with one word.
 */
public class ContainerCommand implements CommandExecutor {

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

        String name = command.getName().toLowerCase();
        boolean deposit = name.equals("deposit") || name.equals("quickdeposit") || label.toLowerCase().startsWith("deposit");

        if (deposit) {
            handleDeposit(player);
        } else {
            handleSort(player);
        }
        return true;
    }

    private void handleSort(Player player) {
        if (!player.hasPermission("apexsions.container.use")) {
            player.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk merapikan peti.</red>"));
            return;
        }
        if (!manager.isSortEnabled()) {
            player.sendMessage(mm.deserialize("<red>Fitur rapikan peti sedang dinonaktifkan.</red>"));
            return;
        }
        Inventory container = manager.getOpenContainer(player);
        if (container == null) {
            player.sendMessage(mm.deserialize("<yellow>Buka peti/tong terlebih dahulu, lalu gunakan <gold>/sort</gold>.</yellow>"));
            return;
        }
        int count = manager.sort(container);
        if (count < 0) {
            player.sendMessage(mm.deserialize("<gray>Peti sudah kosong.</gray>"));
            return;
        }
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
        player.sendMessage(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>PETI DIRAPIKAN!</bold></gradient> <gray>" + count + " jenis item disusun & digabung.</gray>"));
    }

    private void handleDeposit(Player player) {
        if (!player.hasPermission("apexsions.container.use")) {
            player.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk menyetor item.</red>"));
            return;
        }
        if (!manager.isDepositEnabled()) {
            player.sendMessage(mm.deserialize("<red>Fitur setor cepat sedang dinonaktifkan.</red>"));
            return;
        }
        Inventory container = manager.getOpenContainer(player);
        if (container == null) {
            player.sendMessage(mm.deserialize("<yellow>Buka peti/tong terlebih dahulu, lalu gunakan <gold>/deposit</gold>.</yellow>"));
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
}