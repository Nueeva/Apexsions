package com.apexsions.media.engine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ImageRenderer {

    private final Cache<String, byte[][][]> imageTileCache;

    public ImageRenderer(int maxCachedImages, int expireMinutes) {
        this.imageTileCache = Caffeine.newBuilder()
                .maximumSize(maxCachedImages)
                .expireAfterAccess(expireMinutes, TimeUnit.MINUTES)
                .build();
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

    public static class CustomMapRenderer extends MapRenderer {
        private final byte[] pixelData;
        private boolean rendered = false;

        public CustomMapRenderer(byte[] pixelData) {
            super(false);
            this.pixelData = pixelData;
        }

        @Override
        public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
            if (rendered) return;
            for (int y = 0; y < 128; y++) {
                for (int x = 0; x < 128; x++) {
                    canvas.setPixel(x, y, pixelData[(y * 128) + x]);
                }
            }
            rendered = true;
        }
    }
}
