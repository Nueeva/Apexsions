package com.apexsions.media.command;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.banner.MediaBanner;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
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

public class MediaCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsMediaPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MediaCommand(ApexsionsMediaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("apexsionsmedia.admin")) {
            sender.sendMessage(miniMessage.deserialize("<red>Anda tidak memiliki izin untuk menggunakan perintah ini.</red>"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create", "spawn" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(miniMessage.deserialize("<red>Hanya pemain di dalam game yang dapat memasang banner.</red>"));
                    return true;
                }
                if (args.length < 5) {
                    player.sendMessage(miniMessage.deserialize("<gray>Penggunaan: </gray><yellow>/media create <id> <file/url> <width> <height> [linkUrl] [CHAT_PROMPT|GUI_CONFIRM]</yellow>"));
                    return true;
                }

                String id = args[1].toLowerCase();
                String source = args[2];
                int width, height;

                try {
                    width = Integer.parseInt(args[3]);
                    height = Integer.parseInt(args[4]);
                    if (width < 1 || width > 10 || height < 1 || height > 10) {
                        player.sendMessage(miniMessage.deserialize("<red>Ukuran lebar dan tinggi harus antara 1 sampai 10 blok.</red>"));
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(miniMessage.deserialize("<red>Lebar dan tinggi harus berupa angka.</red>"));
                    return true;
                }

                String linkUrl = args.length > 5 ? args[5] : null;
                MediaBanner.ClickMode mode = MediaBanner.ClickMode.CHAT_PROMPT;
                if (args.length > 6) {
                    try {
                        mode = MediaBanner.ClickMode.valueOf(args[6].toUpperCase());
                    } catch (IllegalArgumentException ignored) {}
                }

                Location targetLoc = player.getTargetBlockExact(5) != null
                        ? player.getTargetBlockExact(5).getLocation()
                        : player.getLocation();

                BlockFace facing = player.getFacing().getOppositeFace();

                player.sendMessage(miniMessage.deserialize("<yellow>⏳ Sedang memproses dan merender banner <gold>" + id + "</gold>...</yellow>"));

                plugin.getBannerManager().createAndSpawnBanner(id, targetLoc, facing, width, height, source, linkUrl, mode)
                        .thenAccept(success -> {
                            if (success) {
                                player.sendMessage(miniMessage.deserialize("<green>✔ Berhasil memasang banner <yellow>" + id + "</yellow> (" + width + "x" + height + ")!</green>"));
                            } else {
                                player.sendMessage(miniMessage.deserialize("<red>✖ Gagal memasang banner. Periksa URL atau file gambar.</red>"));
                            }
                        });
            }

            case "delete", "remove" -> {
                if (args.length < 2) {
                    sender.sendMessage(miniMessage.deserialize("<gray>Penggunaan: </gray><yellow>/media delete <id></yellow>"));
                    return true;
                }
                String id = args[1].toLowerCase();
                boolean deleted = plugin.getBannerManager().deleteBanner(id);
                if (deleted) {
                    sender.sendMessage(miniMessage.deserialize("<green>✔ Berhasil menghapus banner <yellow>" + id + "</yellow>.</green>"));
                } else {
                    sender.sendMessage(miniMessage.deserialize("<red>✖ Banner '" + id + "' tidak ditemukan.</red>"));
                }
            }

            case "list" -> {
                var banners = plugin.getBannerManager().getBanners();
                sender.sendMessage(miniMessage.deserialize("<gold><bold>=== DAFTAR APEXSIONS MEDIA BANNERS (" + banners.size() + ") ===</bold></gold>"));
                for (MediaBanner b : banners) {
                    sender.sendMessage(miniMessage.deserialize(
                            "<yellow>• <gold>" + b.getId() + "</gold> <gray>(" + b.getWidthTiles() + "x" + b.getHeightTiles() + ") @ " +
                                    b.getWorldName() + " [" + (int) b.getX() + ", " + (int) b.getY() + ", " + (int) b.getZ() + "] " +
                                    "Link: <aqua>" + (b.getLinkUrl() != null ? b.getLinkUrl() : "-") + "</aqua></gray>"
                    ));
                }
            }

            case "setlink" -> {
                if (args.length < 3) {
                    sender.sendMessage(miniMessage.deserialize("<gray>Penggunaan: </gray><yellow>/media setlink <id> <url> [CHAT_PROMPT|GUI_CONFIRM]</yellow>"));
                    return true;
                }
                String id = args[1].toLowerCase();
                MediaBanner banner = plugin.getBannerManager().getBanner(id);
                if (banner == null) {
                    sender.sendMessage(miniMessage.deserialize("<red>✖ Banner '" + id + "' tidak ditemukan.</red>"));
                    return true;
                }
                banner.setLinkUrl(args[2]);
                if (args.length > 3) {
                    try {
                        banner.setClickMode(MediaBanner.ClickMode.valueOf(args[3].toUpperCase()));
                    } catch (IllegalArgumentException ignored) {}
                }
                sender.sendMessage(miniMessage.deserialize("<green>✔ Tautan banner <yellow>" + id + "</yellow> berhasil diperbarui ke <aqua>" + args[2] + "</aqua>.</green>"));
            }

            case "reload" -> {
                plugin.reloadConfig();
                plugin.getBannerManager().loadAllBanners();
                plugin.getRaytraceService().start();
                sender.sendMessage(miniMessage.deserialize("<green>✔ Konfigurasi ApexsionsMedia berhasil dimuat ulang.</green>"));
            }

            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<gold><bold>=== APEXSIONS MEDIA COMMANDS ===</bold></gold>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/media create <id> <file/url> <w> <h> [link] [mode]</yellow> <gray>- Buat banner baru</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/media delete <id></yellow> <gray>- Hapus banner yang ada</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/media list</yellow> <gray>- Daftar seluruh banner aktif</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/media setlink <id> <url> [mode]</yellow> <gray>- Ubah URL interaktif banner</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/media reload</yellow> <gray>- Muat ulang konfigurasi & render</gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("apexsionsmedia.admin")) return Collections.emptyList();

        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.addAll(List.of("create", "delete", "list", "setlink", "reload"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("setlink"))) {
            for (MediaBanner b : plugin.getBannerManager().getBanners()) {
                list.add(b.getId());
            }
        } else if (args.length == 7 && args[0].equalsIgnoreCase("create")) {
            list.addAll(List.of("CHAT_PROMPT", "GUI_CONFIRM", "DIRECT_URL"));
        } else if (args.length == 4 && args[0].equalsIgnoreCase("setlink")) {
            list.addAll(List.of("CHAT_PROMPT", "GUI_CONFIRM", "DIRECT_URL"));
        }

        return list.stream().filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).toList();
    }
}
