package com.apexsions.media.command;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.banner.MediaBanner;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
            case "create", "new" -> handleCreate(sender, args);
            case "place", "apply", "paste" -> handlePlace(sender, args);
            case "copy", "clone" -> handleCopy(sender, args);
            case "moveto", "move" -> handleMove(sender, args);
            case "delete", "remove" -> handleDelete(sender, args);
            case "list" -> handleList(sender);
            case "setlink", "link" -> handleSetLink(sender, args);
            case "resize" -> handleResize(sender, args);
            case "reload" -> handleReload(sender);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Hanya pemain di dalam game yang dapat memasang banner.</red>"));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(miniMessage.deserialize("<gray>Penggunaan: </gray><yellow>/media create <id> <file/url> [lebar] [tinggi] [linkUrl] [mode]</yellow>"));
            player.sendMessage(miniMessage.deserialize("<dark_gray>Contoh sederhana: </dark_gray><gold>/media create discord https://example.com/logo.png</gold>"));
            return;
        }

        String id = args[1].toLowerCase();
        String source = args[2];
        int width = 1;
        int height = 1;

        if (args.length >= 5) {
            try {
                width = Integer.parseInt(args[3]);
                height = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                player.sendMessage(miniMessage.deserialize("<red>Lebar dan tinggi harus berupa angka.</red>"));
                return;
            }
        } else {
            Dimension detected = plugin.getImageRenderer().detectDimensions(source, plugin.getDataFolder());
            width = detected.width;
            height = detected.height;
        }

        final int finalWidth = Math.max(1, Math.min(10, width));
        final int finalHeight = Math.max(1, Math.min(10, height));

        String linkUrl = null;
        if (args.length > 5 && !args[5].equalsIgnoreCase("none") && !args[5].equalsIgnoreCase("null")) {
            linkUrl = args[5];
        }

        MediaBanner.ClickMode mode = MediaBanner.ClickMode.CHAT_PROMPT;
        if (args.length > 6) {
            try {
                mode = MediaBanner.ClickMode.valueOf(args[6].toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        PlacementResult placement = getPlacement(player);

        player.sendMessage(miniMessage.deserialize("<yellow>⏳ Sedang memproses dan merender banner <gold>" + id + "</gold> (" + finalWidth + "x" + finalHeight + ")...</yellow>"));

        plugin.getBannerManager().createAndSpawnBanner(id, placement.location(), placement.facing(), finalWidth, finalHeight, source, linkUrl, mode)
                .thenAccept(success -> {
                    if (success) {
                        player.sendMessage(miniMessage.deserialize("<green>✔ Berhasil memasang banner <yellow>" + id + "</yellow> (" + finalWidth + "x" + finalHeight + ") di " +
                                String.format("%.0f, %.0f, %.0f", placement.location().getX(), placement.location().getY(), placement.location().getZ()) + "!</green>"));
                    } else {
                        player.sendMessage(miniMessage.deserialize("<red>✖ Gagal memasang banner. Periksa URL atau file gambar.</red>"));
                    }
                });
    }

    private void handlePlace(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Hanya pemain di dalam game yang dapat memindahkan banner.</red>"));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(miniMessage.deserialize("<gray>Penggunaan: </gray><yellow>/media place <id></yellow> <gray>(Pasang/pindahkan banner yang sudah ada ke lokasi yang kamu bidik)</gray>"));
            return;
        }

        String id = args[1].toLowerCase();
        MediaBanner banner = plugin.getBannerManager().getBanner(id);
        if (banner == null) {
            player.sendMessage(miniMessage.deserialize("<red>Banner dengan ID '<yellow>" + id + "</yellow>' tidak ditemukan.</red>"));
            return;
        }

        PlacementResult placement = getPlacement(player);
        plugin.getBannerManager().moveBanner(id, placement.location(), placement.facing())
                .thenAccept(success -> {
                    if (success) {
                        player.sendMessage(miniMessage.deserialize("<green>✔ Banner <yellow>" + id + "</yellow> berhasil dipindahkan ke lokasi target baru!</green>"));
                    } else {
                        player.sendMessage(miniMessage.deserialize("<red>✖ Gagal memindahkan banner.</red>"));
                    }
                });
    }

    private void handleCopy(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Hanya pemain di dalam game yang dapat menduplikasi banner.</red>"));
            return;
        }
        if (args.length < 3) {
            player.sendMessage(miniMessage.deserialize("<gray>Penggunaan: </gray><yellow>/media copy <sourceId> <newId></yellow> <gray>(Duplikasi banner ke lokasi baru)</gray>"));
            return;
        }

        String sourceId = args[1].toLowerCase();
        String targetId = args[2].toLowerCase();

        MediaBanner sourceBanner = plugin.getBannerManager().getBanner(sourceId);
        if (sourceBanner == null) {
            player.sendMessage(miniMessage.deserialize("<red>Banner asal '<yellow>" + sourceId + "</yellow>' tidak ditemukan.</red>"));
            return;
        }

        PlacementResult placement = getPlacement(player);
        player.sendMessage(miniMessage.deserialize("<yellow>⏳ Menduplikasi banner <gold>" + sourceId + "</gold> menjadi <gold>" + targetId + "</gold>...</yellow>"));

        plugin.getBannerManager().cloneBanner(sourceId, targetId, placement.location(), placement.facing())
                .thenAccept(success -> {
                    if (success) {
                        player.sendMessage(miniMessage.deserialize("<green>✔ Berhasil membuat duplikat banner <yellow>" + targetId + "</yellow> di lokasi target!</green>"));
                    } else {
                        player.sendMessage(miniMessage.deserialize("<red>✖ Gagal menduplikasi banner.</red>"));
                    }
                });
    }

    private void handleMove(CommandSender sender, String[] args) {
        handlePlace(sender, args);
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(miniMessage.deserialize("<gray>Penggunaan: </gray><yellow>/media delete <id></yellow>"));
            return;
        }

        String id = args[1].toLowerCase();
        boolean deleted = plugin.getBannerManager().deleteBanner(id);
        if (deleted) {
            sender.sendMessage(miniMessage.deserialize("<green>✔ Banner <yellow>" + id + "</yellow> berhasil dihapus.</green>"));
        } else {
            sender.sendMessage(miniMessage.deserialize("<red>✖ Banner dengan ID '<yellow>" + id + "</yellow>' tidak ditemukan.</red>"));
        }
    }

    private void handleList(CommandSender sender) {
        var list = plugin.getBannerManager().getBanners();
        if (list.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize("<yellow>Tidak ada media banner yang terpasang saat ini.</yellow>"));
            return;
        }

        sender.sendMessage(miniMessage.deserialize("<gold><bold>=== DAFTAR APEXSIONS MEDIA BANNERS (" + list.size() + ") ===</bold></gold>"));
        for (MediaBanner b : list) {
            String linkInfo = b.getLinkUrl() != null ? "<aqua>" + b.getLinkUrl() + "</aqua>" : "<gray>[Tanpa URL]</gray>";
            Component line = miniMessage.deserialize("• <yellow><bold>" + b.getId() + "</bold></yellow> (" + b.getWidthTiles() + "x" + b.getHeightTiles() + ") @ <white>" +
                    b.getWorldName() + " [" + (int) b.getX() + ", " + (int) b.getY() + ", " + (int) b.getZ() + "] " + b.getFacing() + "</white> Link: " + linkInfo)
                    .hoverEvent(HoverEvent.showText(miniMessage.deserialize("<gray>Klik untuk memasang ulang banner ini di target kamu</gray>")))
                    .clickEvent(ClickEvent.suggestCommand("/media place " + b.getId()));
            sender.sendMessage(line);
        }
    }

    private void handleSetLink(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(miniMessage.deserialize("<gray>Penggunaan: </gray><yellow>/media setlink <id> [linkUrl|none] [mode]</yellow>"));
            return;
        }

        String id = args[1].toLowerCase();
        MediaBanner banner = plugin.getBannerManager().getBanner(id);
        if (banner == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Banner '<yellow>" + id + "</yellow>' tidak ditemukan.</red>"));
            return;
        }

        String linkUrl = args.length > 2 ? args[2] : null;
        if (linkUrl != null && (linkUrl.equalsIgnoreCase("none") || linkUrl.equalsIgnoreCase("clear") || linkUrl.equalsIgnoreCase("remove"))) {
            linkUrl = null;
        }

        MediaBanner.ClickMode mode = banner.getClickMode();
        if (args.length > 3) {
            try {
                mode = MediaBanner.ClickMode.valueOf(args[3].toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        plugin.getBannerManager().updateBannerLink(id, linkUrl, mode)
                .thenAccept(success -> {
                    if (success) {
                        sender.sendMessage(miniMessage.deserialize("<green>✔ Tautan untuk banner <yellow>" + id + "</yellow> berhasil diperbarui!</green>"));
                    } else {
                        sender.sendMessage(miniMessage.deserialize("<red>✖ Gagal memperbarui tautan.</red>"));
                    }
                });
    }

    private void handleResize(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(miniMessage.deserialize("<gray>Penggunaan: </gray><yellow>/media resize <id> <lebar> <tinggi></yellow>"));
            return;
        }

        String id = args[1].toLowerCase();
        int width, height;
        try {
            width = Integer.parseInt(args[2]);
            height = Integer.parseInt(args[3]);
            if (width < 1 || width > 10 || height < 1 || height > 10) {
                sender.sendMessage(miniMessage.deserialize("<red>Lebar dan tinggi harus antara 1 sampai 10.</red>"));
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(miniMessage.deserialize("<red>Ukuran harus berupa angka.</red>"));
            return;
        }

        plugin.getBannerManager().updateBannerSize(id, width, height)
                .thenAccept(success -> {
                    if (success) {
                        sender.sendMessage(miniMessage.deserialize("<green>✔ Ukuran banner <yellow>" + id + "</yellow> diubah menjadi " + width + "x" + height + "!</green>"));
                    } else {
                        sender.sendMessage(miniMessage.deserialize("<red>✖ Gagal mengubah ukuran banner.</red>"));
                    }
                });
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadConfig();
        plugin.getImageRenderer().invalidateCache();
        plugin.getBannerManager().loadAllBanners();
        sender.sendMessage(miniMessage.deserialize("<green>✔ Konfigurasi dan seluruh render banner ApexsionsMedia berhasil dimuat ulang.</green>"));
    }

    private PlacementResult getPlacement(Player player) {
        RayTraceResult ray = player.rayTraceBlocks(10.0);
        if (ray != null && ray.getHitBlock() != null && ray.getHitBlockFace() != null) {
            Block hitBlock = ray.getHitBlock();
            BlockFace hitFace = ray.getHitBlockFace();
            Location loc = hitBlock.getRelative(hitFace).getLocation();

            BlockFace facing = (hitFace == BlockFace.UP || hitFace == BlockFace.DOWN)
                    ? player.getFacing().getOppositeFace()
                    : hitFace;

            return new PlacementResult(loc, facing);
        }

        Location loc = player.getLocation().add(player.getLocation().getDirection().multiply(2.5));
        loc.setY(Math.floor(player.getLocation().getY() + 1.0));
        return new PlacementResult(loc, player.getFacing().getOppositeFace());
    }

    private record PlacementResult(Location location, BlockFace facing) {}

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<gold><bold>=== APEXSIONS MEDIA COMMANDS ===</bold></gold>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/media create <id> <file/url> [w] [h] [link] [mode]</yellow> <gray>- Buat banner baru (URL/ukuran opsional)</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/media place <id></yellow> <gray>- Pasang banner yang ada di lokasi bidikan baru</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/media copy <idAsal> <idBaru></yellow> <gray>- Duplikasi banner ke target kamu</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/media setlink <id> [url|none] [mode]</yellow> <gray>- Ubah/hapus URL interaktif banner</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/media resize <id> <w> <h></yellow> <gray>- Ubah ukuran banner (1-10)</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/media delete <id></yellow> <gray>- Hapus banner yang ada</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/media list</yellow> <gray>- Daftar seluruh banner aktif</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/media reload</yellow> <gray>- Muat ulang konfigurasi & render</gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("create", "place", "copy", "moveto", "delete", "list", "setlink", "resize", "reload"), args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("place") || args[0].equalsIgnoreCase("copy") ||
                args[0].equalsIgnoreCase("moveto") || args[0].equalsIgnoreCase("delete") ||
                args[0].equalsIgnoreCase("setlink") || args[0].equalsIgnoreCase("resize"))) {
            return filter(new ArrayList<>(plugin.getBannerManager().getBanners().stream().map(MediaBanner::getId).toList()), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            File imagesDir = new File(plugin.getDataFolder(), "images");
            List<String> files = new ArrayList<>();
            if (imagesDir.exists() && imagesDir.isDirectory()) {
                String[] list = imagesDir.list();
                if (list != null) {
                    files.addAll(Arrays.asList(list));
                }
            }
            files.add("https://");
            return filter(files, args[2]);
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("create")) {
            return filter(Arrays.asList("1", "2", "3", "4", "5"), args[3]);
        }
        if (args.length == 5 && args[0].equalsIgnoreCase("create")) {
            return filter(Arrays.asList("1", "2", "3", "4", "5"), args[4]);
        }
        if (args.length == 6 && args[0].equalsIgnoreCase("create")) {
            return filter(Arrays.asList("https://discord.gg/", "none"), args[5]);
        }
        if (args.length == 7 && args[0].equalsIgnoreCase("create")) {
            return filter(Arrays.asList("CHAT_PROMPT", "GUI_CONFIRM", "DIRECT_URL"), args[6]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String input) {
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(input.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }
}
