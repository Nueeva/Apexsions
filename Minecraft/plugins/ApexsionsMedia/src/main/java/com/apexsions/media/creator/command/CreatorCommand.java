package com.apexsions.media.creator.command;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.creator.gui.CreatorHubGUI;
import com.apexsions.media.creator.gui.CreatorTiersGUI;
import com.apexsions.media.creator.model.Platform;
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

import java.util.ArrayList;
import java.util.List;

public class CreatorCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsMediaPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public CreatorCommand(ApexsionsMediaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("menu") || args[0].equalsIgnoreCase("gui")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(mm.deserialize("<red>Perintah ini hanya dapat digunakan oleh pemain in-game!</red>"));
                return true;
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            player.openInventory(new CreatorHubGUI(plugin, player).getInventory());
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "submit", "claim" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(mm.deserialize("<red>Hanya pemain in-game yang dapat submit video!</red>"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(mm.deserialize("<yellow>Penggunaan: <white>/creator submit &lt;URL Video YouTube / TikTok&gt;</white></yellow>"));
                    return true;
                }
                plugin.getCreatorManager().processVideoSubmission(player, args[1]);
                return true;
            }

            case "link" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(mm.deserialize("<red>Hanya pemain in-game yang dapat menautkan akun!</red>"));
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(mm.deserialize("<yellow>Penggunaan: <white>/creator link &lt;youtube|tiktok&gt; &lt;identifier/@handle&gt;</white></yellow>"));
                    return true;
                }

                String platformStr = args[1].toLowerCase();
                String id = args[2];

                if (platformStr.equals("youtube") || platformStr.equals("yt")) {
                    plugin.getCreatorManager().startLinking(player, Platform.YOUTUBE, id).thenAccept(code -> {
                        player.sendMessage(mm.deserialize(
                                "\n<gradient:#f39c12:#f1c40f><b>✦ VERIFIKASI KEPEMILIKAN YOUTUBE ✦</b></gradient>\n" +
                                "<gray>Kode Verifikasi: </gray><yellow><bold>" + code + "</bold></yellow>\n" +
                                "<gray>Silakan masukkan kode di atas ke dalam <b>Deskripsi Channel (About)</b> atau <b>Deskripsi Video Terbarumu</b> di YouTube.</gray>\n" +
                                "<gray>Setelah ditaruh, ketik perintah: <aqua>/creator verify youtube</aqua></gray>\n"
                        ));
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
                    });
                } else if (platformStr.equals("tiktok") || platformStr.equals("tt")) {
                    plugin.getCreatorManager().startLinking(player, Platform.TIKTOK, id).thenCompose(code ->
                            plugin.getCreatorManager().verifyLinking(player, Platform.TIKTOK)
                    ).thenAccept(success -> {
                        player.sendMessage(mm.deserialize("<green><b>[Creator]</b> Akun TikTok @" + id.replace("@", "") + " berhasil ditautkan!</green>"));
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.5f);
                    });
                } else {
                    player.sendMessage(mm.deserialize("<red>Platform tidak valid! Pilihan: <yellow>youtube</yellow> atau <yellow>tiktok</yellow>.</red>"));
                }
                return true;
            }

            case "verify" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(mm.deserialize("<red>Hanya pemain in-game yang dapat verifikasi!</red>"));
                    return true;
                }
                String platformStr = args.length > 1 ? args[1].toLowerCase() : "youtube";
                Platform platform = (platformStr.equals("tiktok") || platformStr.equals("tt")) ? Platform.TIKTOK : Platform.YOUTUBE;

                player.sendMessage(mm.deserialize("<gray>Sedang memeriksa verifikasi channel YouTube...</gray>"));
                plugin.getCreatorManager().verifyLinking(player, platform).thenAccept(success -> {
                    if (success) {
                        player.sendMessage(mm.deserialize("<green><b>[Creator]</b> Verifikasi kepemilikan channel YouTube berhasil! Channel kamu kini resmi terhubung.</green>"));
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.5f);
                    } else {
                        player.sendMessage(mm.deserialize("<red><b>[Creator]</b> Verifikasi gagal! Kode verifikasi tidak ditemukan di deskripsi channel atau sesi telah kedaluwarsa.</red>"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                });
                return true;
            }

            case "unlink" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(mm.deserialize("<red>Hanya pemain in-game yang dapat unlink!</red>"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(mm.deserialize("<yellow>Penggunaan: <white>/creator unlink &lt;youtube|tiktok&gt;</white></yellow>"));
                    return true;
                }
                String platformStr = args[1].toLowerCase();
                Platform platform = (platformStr.equals("tiktok") || platformStr.equals("tt")) ? Platform.TIKTOK : Platform.YOUTUBE;

                plugin.getCreatorManager().unlinkPlatform(player, platform).thenRun(() -> {
                    player.sendMessage(mm.deserialize("<yellow><b>[Creator]</b> Akun " + platform.getDisplayName() + " berhasil diputuskan dari profilmu.</yellow>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                });
                return true;
            }

            case "tiers" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(mm.deserialize("<red>Perintah ini hanya dapat digunakan oleh pemain in-game!</red>"));
                    return true;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                player.openInventory(new CreatorTiersGUI(plugin, player).getInventory());
                return true;
            }

            case "admin" -> {
                if (!sender.hasPermission("apexsionsmedia.creator.admin")) {
                    sender.sendMessage(mm.deserialize("<red>Kamu tidak memiliki izin untuk menggunakan perintah admin kreator!</red>"));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(mm.deserialize("<yellow>Penggunaan Admin: <white>/creator admin &lt;reload|info|reset&gt; [pemain]</white></yellow>"));
                    return true;
                }

                String adminSub = args[1].toLowerCase();
                if (adminSub.equals("reload")) {
                    plugin.reloadConfig();
                    plugin.getCreatorManager().loadConfiguration();
                    sender.sendMessage(mm.deserialize("<green><b>[Creator Admin]</b> Konfigurasi kreator dan tiers berhasil dimuat ulang!</green>"));
                    return true;
                }

                if (adminSub.equals("info") && args.length >= 3) {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
                    plugin.getCreatorManager().getProfile(target.getUniqueId(), target.getName() != null ? target.getName() : args[2]).thenAccept(prof -> {
                        sender.sendMessage(mm.deserialize("\n<gradient:#f39c12:#f1c40f><b>✦ INFO KREATOR: " + args[2] + " ✦</b></gradient>"));
                        sender.sendMessage(mm.deserialize("<gray>YouTube: " + (prof.isYouTubeLinked() ? "<green>" + prof.getYoutubeChannelId() + "</green>" : "<red>None</red>")));
                        sender.sendMessage(mm.deserialize("<gray>TikTok: " + (prof.isTikTokLinked() ? "<green>@" + prof.getTiktokUsername() + "</green>" : "<red>None</red>")));
                    });
                    return true;
                }

                if (adminSub.equals("reset") && args.length >= 3) {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
                    plugin.getCreatorManager().getProfile(target.getUniqueId(), target.getName() != null ? target.getName() : args[2]).thenAccept(prof -> {
                        prof.setYoutubeChannelId(null);
                        prof.setYoutubeHandle(null);
                        prof.setTiktokUsername(null);
                        plugin.getCreatorManager().getRepository().saveProfile(prof).thenRun(() -> {
                            sender.sendMessage(mm.deserialize("<green><b>[Creator Admin]</b> Berhasil me-reset data penautan kreator untuk " + args[2] + "!</green>"));
                        });
                    });
                    return true;
                }

                sender.sendMessage(mm.deserialize("<yellow>Penggunaan Admin: <white>/creator admin &lt;reload|info|reset&gt; [pemain]</white></yellow>"));
                return true;
            }

            default -> {
                sender.sendMessage(mm.deserialize("<yellow>Perintah Creator:</yellow>"));
                sender.sendMessage(mm.deserialize("<gray>/creator menu</gray> - <white>Buka Creator Hub GUI</white>"));
                sender.sendMessage(mm.deserialize("<gray>/creator submit &lt;url&gt;</gray> - <white>Submit video untuk verifikasi</white>"));
                sender.sendMessage(mm.deserialize("<gray>/creator link &lt;youtube|tiktok&gt; &lt;id&gt;</gray> - <white>Tautkan akun</white>"));
                sender.sendMessage(mm.deserialize("<gray>/creator verify &lt;youtube&gt;</gray> - <white>Verifikasi kode YouTube</white>"));
                sender.sendMessage(mm.deserialize("<gray>/creator unlink &lt;youtube|tiktok&gt;</gray> - <white>Putuskan tautan</white>"));
                sender.sendMessage(mm.deserialize("<gray>/creator tiers</gray> - <white>Lihat daftar tier & reward</white>"));
                return true;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.addAll(List.of("menu", "submit", "link", "verify", "unlink", "tiers"));
            if (sender.hasPermission("apexsionsmedia.creator.admin")) {
                list.add("admin");
            }
            return filter(list, args[0]);
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("link") || args[0].equalsIgnoreCase("unlink") || args[0].equalsIgnoreCase("verify")) {
                list.addAll(List.of("youtube", "tiktok"));
                return filter(list, args[1]);
            }
            if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("apexsionsmedia.creator.admin")) {
                list.addAll(List.of("reload", "info", "reset"));
                return filter(list, args[1]);
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("admin") && (args[1].equalsIgnoreCase("info") || args[1].equalsIgnoreCase("reset"))) {
                return null; // suggest player names
            }
        }

        return List.of();
    }

    private List<String> filter(List<String> raw, String input) {
        String lower = input.toLowerCase();
        List<String> out = new ArrayList<>();
        for (String s : raw) {
            if (s.toLowerCase().startsWith(lower)) {
                out.add(s);
            }
        }
        return out;
    }
}
