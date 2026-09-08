package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.maintenance.MaintenanceManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MaintenanceCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MaintenanceCommand(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("apexsions.maintenance.admin") && !sender.hasPermission("apexsions.admin")) {
            sender.sendMessage(miniMessage.deserialize("<red>Anda tidak memiliki izin untuk mengelola mode pemeliharaan server.</red>"));
            return true;
        }

        MaintenanceManager mm = plugin.getMaintenanceManager();
        if (mm == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Maintenance manager belum terinisialisasi.</red>"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender, mm);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "enable":
            case "on": {
                String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Pemeliharaan berkala";
                mm.enableMaintenance(reason, null, true);
                sender.sendMessage(miniMessage.deserialize("<green>✔ Maintenance Mode berhasil <bold>DIAKTIFKAN</bold>. Alasan: <white>" + reason + "</white></green>"));
                break;
            }
            case "disable":
            case "off": {
                mm.disableMaintenance();
                sender.sendMessage(miniMessage.deserialize("<green>✔ Maintenance Mode berhasil <bold>DINONAKTIFKAN</bold>. Server terbuka normal.</green>"));
                break;
            }
            case "status": {
                boolean active = mm.isMaintenanceActive();
                sender.sendMessage(miniMessage.deserialize("<gold>✦ STATUS PEMELIHARAAN SERVER ✦</gold>"));
                sender.sendMessage(miniMessage.deserialize("<gray>Status: " + (active ? "<red><bold>AKTIF</bold></red>" : "<green><bold>TIDAK AKTIF</bold></green>") + "</gray>"));
                sender.sendMessage(miniMessage.deserialize("<gray>Alasan: <white>" + mm.getReason() + "</white></gray>"));
                sender.sendMessage(miniMessage.deserialize("<gray>Pesan: <white>" + mm.getMessage() + "</white></gray>"));
                sender.sendMessage(miniMessage.deserialize("<gray>Bypass Staf: <white>" + (mm.isAllowStaff() ? "Ya" : "Tidak") + "</white></gray>"));
                break;
            }
            case "setmessage": {
                if (args.length < 2) {
                    sender.sendMessage(miniMessage.deserialize("<red>Gunakan: /maintenance setmessage <pesan...></red>"));
                    return true;
                }
                String newMsg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                mm.enableMaintenance(mm.getReason(), newMsg, mm.isAllowStaff());
                sender.sendMessage(miniMessage.deserialize("<green>✔ Pesan kick pemeliharaan diperbarui: <white>" + newMsg + "</white></green>"));
                break;
            }
            default:
                sendHelp(sender, mm);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender, MaintenanceManager mm) {
        sender.sendMessage(miniMessage.deserialize("<gradient:#f1c40f:#e67e22><bold>✦ PANDUAN MAINTENANCE MODE ✦</bold></gradient>"));
        sender.sendMessage(miniMessage.deserialize("<gray>Status saat ini: " + (mm.isMaintenanceActive() ? "<red>AKTIF</red>" : "<green>TIDAK AKTIF</green>") + "</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/maintenance enable [alasan]</yellow> <gray>- Mengaktifkan mode pemeliharaan</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/maintenance disable</yellow> <gray>- Menonaktifkan mode pemeliharaan</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/maintenance status</yellow> <gray>- Melihat rincian status pemeliharaan</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/maintenance setmessage <pesan></yellow> <gray>- Mengubah pesan kick tampilan pemain</gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("apexsions.maintenance.admin") && !sender.hasPermission("apexsions.admin")) {
            return List.of();
        }
        if (args.length == 1) {
            List<String> subs = List.of("enable", "disable", "status", "setmessage");
            List<String> completions = new ArrayList<>();
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) {
                    completions.add(s);
                }
            }
            return completions;
        }
        return List.of();
    }
}
