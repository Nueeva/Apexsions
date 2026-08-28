package com.apexsions.chat.command;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.chat.gui.MailListGUI;
import com.apexsions.chat.model.Mail;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MailCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, Long> sendCooldowns = new ConcurrentHashMap<>();

    public MailCommand(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Only in-game players can use the mail system.</red>"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("read") || args[0].equalsIgnoreCase("inbox")) {
            new MailListGUI(plugin, 1).loadAndOpen(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("send")) {
            if (args.length < 3) {
                player.sendMessage(miniMessage.deserialize("<red>Usage: /mail send <player> <message></red>"));
                return true;
            }

            // Cooldown check
            int cdSec = plugin.getConfigManager().getMailConfig().getInt("mail.cooldown-seconds", 15);
            long last = sendCooldowns.getOrDefault(player.getUniqueId(), 0L);
            if ((System.currentTimeMillis() - last) < (cdSec * 1000L)) {
                long remaining = (cdSec * 1000L - (System.currentTimeMillis() - last)) / 1000;
                player.sendMessage(miniMessage.deserialize("<red>✖ Please wait " + remaining + "s before sending another mail.</red>"));
                return true;
            }

            String targetName = args[1];
            Player target = Bukkit.getPlayerExact(targetName);
            UUID targetUuid;
            String finalTargetName;

            if (target != null) {
                targetUuid = target.getUniqueId();
                finalTargetName = target.getName();
            } else {
                OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
                if (offline.hasPlayedBefore() || offline.isOnline()) {
                    targetUuid = offline.getUniqueId();
                    finalTargetName = offline.getName() != null ? offline.getName() : targetName;
                } else {
                    player.sendMessage(miniMessage.deserialize("<red>✖ Player '" + targetName + "' could not be found.</red>"));
                    return true;
                }
            }

            StringBuilder bodyBuilder = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                bodyBuilder.append(args[i]).append(" ");
            }
            String body = bodyBuilder.toString().trim();

            int maxBody = plugin.getConfigManager().getMailConfig().getInt("mail.max-body-length", 512);
            if (body.length() > maxBody) {
                body = body.substring(0, maxBody);
            }

            String subject = "Letter from " + player.getName();
            Mail mail = new Mail(
                    player.getUniqueId(),
                    player.getName(),
                    targetUuid,
                    finalTargetName,
                    subject,
                    body
            );

            plugin.getMailRepository().sendMailAsync(mail).thenAccept(mailId -> {
                if (mailId > 0) {
                    sendCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                    player.sendMessage(miniMessage.deserialize("<green>✔ Mail successfully sent to <yellow>" + finalTargetName + "</yellow>!</green>"));

                    // If target is currently online, notify them immediately
                    if (target != null && target.isOnline()) {
                        target.sendMessage(miniMessage.deserialize(
                                "<gold>📬 You just received a new offline letter from <yellow>" + player.getName() + "</yellow>! Type <yellow><underlined>/mail</underlined></yellow> to read.</gold>"
                        ));
                        try {
                            target.playSound(target.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                        } catch (Exception ignored) {}
                    }
                } else {
                    player.sendMessage(miniMessage.deserialize("<red>✖ Failed to send mail. Please try again later.</red>"));
                }
            });

            return true;
        }

        player.sendMessage(miniMessage.deserialize("<gold><bold>═════════ Mail System ═════════</bold></gold>"));
        player.sendMessage(miniMessage.deserialize("<yellow>/mail</yellow> <gray>- Open your mailbox</gray>"));
        player.sendMessage(miniMessage.deserialize("<yellow>/mail send <player> <msg></yellow> <gray>- Send offline mail</gray>"));
        player.sendMessage(miniMessage.deserialize("<gold><bold>═══════════════════════════════</bold></gold>"));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("read", "send");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("send")) {
            List<String> list = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getName().equalsIgnoreCase(sender.getName()) && p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    list.add(p.getName());
                }
            }
            return list;
        }
        return Collections.emptyList();
    }
}
