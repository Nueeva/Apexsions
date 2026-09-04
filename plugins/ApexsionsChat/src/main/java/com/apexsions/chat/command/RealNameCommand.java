package com.apexsions.chat.command;

import com.apexsions.chat.ApexsionsChatPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Command allowing players and staff to resolve the real Minecraft account behind any active nickname.
 */
public class RealNameCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public RealNameCommand(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(miniMessage.deserialize("<red>Penggunaan: <yellow>/realname <nickname></yellow></red>"));
            return true;
        }

        String query = args[0].replace("~", "").trim();

        plugin.getNicknameService().findRealName(query).thenAccept(opt -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (opt.isPresent()) {
                    String realName = opt.get();
                    Player target = Bukkit.getPlayerExact(realName);
                    boolean isOnline = target != null && target.isOnline();

                    sender.sendMessage(miniMessage.deserialize(
                            "<gradient:#ffeaa7:#55efc4><bold>✦ IDENTITAS PEMAIN REALNAME ✦</bold></gradient>\n" +
                            "<gray>Nickname:</gray> <yellow><bold>" + query + "</bold></yellow>\n" +
                            "<gray>Akun Asli Minecraft:</gray> <white><bold>" + realName + "</bold></white>\n" +
                            "<gray>Status:</gray> " + (isOnline ? "<green>● Sedang Online</green>" : "<dark_gray>○ Sedang Offline</dark_gray>")
                    ));
                } else {
                    sender.sendMessage(miniMessage.deserialize("<red>Tidak ditemukan pemain dengan nama panggilan <yellow>" + query + "</yellow>!</red>"));
                }
            });
        });

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                var data = plugin.getNicknameService().getNicknameData(p.getUniqueId());
                if (data.hasNickname()) {
                    list.add(data.getNicknameRaw());
                }
            }
            return list;
        }
        return Collections.emptyList();
    }
}
