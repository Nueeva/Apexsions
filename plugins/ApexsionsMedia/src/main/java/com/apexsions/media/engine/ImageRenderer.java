package com.apexsions.media.engine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ImageRenderer {

    private final Cache<String, byte[][][]> imageTileCache;

    public ImageRenderer(int maxCachedImages, int expireMinutes) {
        this.imageTileCache = Caffeine.newBuilder()
                .maximumSize(maxCachedImages)
                .expireAfterAccess(expireMinutes, TimeUnit.MINUTES)
                .build();
    }

    public Dimension detectDimensions(String source, File dataFolder) {
        try {
            BufferedImage original;
            if (source.startsWith("http://") || source.startsWith("https://")) {
                URL url = URI.create(source).toURL();
                try (InputStream in = url.openStream()) {
                    original = ImageIO.read(in);
                }
            } else {
                File imgFile = new File(source);
                if (!imgFile.isAbsolute()) {
                    imgFile = new File(new File(dataFolder, "images"), source);
                }
                if (!imgFile.exists()) return new Dimension(1, 1);
                original = ImageIO.read(imgFile);
            }

            if (original != null) {
                int w = Math.max(1, Math.min(10, Math.round((float) original.getWidth() / 128.0f)));
                int h = Math.max(1, Math.min(10, Math.round((float) original.getHeight() / 128.0f)));
                return new Dimension(w, h);
            }
        } catch (Exception ignored) {}
        return new Dimension(1, 1);
    }

    public CompletableFuture<byte[][][]> loadAndProcessTiles(String source, int widthTiles, int heightTiles, File dataFolder) {
        String cacheKey = source + ":" + widthTiles + "x" + heightTiles;
        byte[][][] cached = imageTileCache.getIfPresent(cacheKey);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                BufferedImage original;
                if (source.startsWith("http://") || source.startsWith("https://")) {
                    URL url = URI.create(source).toURL();
                    try (InputStream in = url.openStream()) {
                        original = ImageIO.read(in);
                    }
                } else {
                    File imgFile = new File(source);
                    if (!imgFile.isAbsolute()) {
                        imgFile = new File(new File(dataFolder, "images"), source);
                    }
                    if (!imgFile.exists()) {
                        throw new IllegalArgumentException("Image file not found: " + imgFile.getAbsolutePath());
                    }
                    original = ImageIO.read(imgFile);
                }

                if (original == null) {
                    throw new IllegalStateException("Failed to decode image from source: " + source);
                }

                int targetW = widthTiles * 128;
                int targetH = heightTiles * 128;

                BufferedImage scaled = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = scaled.createGraphics();
                g2d.drawImage(original.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH), 0, 0, null);
                g2d.dispose();

                byte[][][] tiles = new byte[widthTiles][heightTiles][128 * 128];

                for (int x = 0; x < widthTiles; x++) {
                    for (int y = 0; y < heightTiles; y++) {
                        byte[] tilePixels = new byte[128 * 128];
                        for (int py = 0; py < 128; py++) {
                            for (int px = 0; px < 128; px++) {
                                int pixelRgb = scaled.getRGB((x * 128) + px, (y * 128) + py);
                                tilePixels[(py * 128) + px] = MapPalette.matchColor(pixelRgb);
                            }
                        }
                        tiles[x][y] = tilePixels;
                    }
                }

                imageTileCache.put(cacheKey, tiles);
                return tiles;
            } catch (Exception e) {
                throw new RuntimeException("Error processing image tiles: " + e.getMessage(), e);
            }
        });
    }

    public void invalidateCache() {
        imageTileCache.invalidateAll();
    }

    public static class CustomMapRenderer extends MapRenderer {
        private final byte[] pixelData;
        private final Set<UUID> renderedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());

        public CustomMapRenderer(byte[] pixelData) {
            super(true); // contextual per player
            this.pixelData = pixelData;
        }

        @Override
        public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
            if (renderedPlayers.contains(player.getUniqueId())) {
                return;
            }
            for (int y = 0; y < 128; y++) {
                for (int x = 0; x < 128; x++) {
                    canvas.setPixel(x, y, pixelData[(y * 128) + x]);
                }
            }
            renderedPlayers.add(player.getUniqueId());
        }

        public void resetForPlayer(UUID uuid) {
            renderedPlayers.remove(uuid);
        }

        public void resetAll() {
            renderedPlayers.clear();
        }
    }
}
