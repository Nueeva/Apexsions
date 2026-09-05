package com.apexsions.media.api;

import com.apexsions.media.banner.MediaBanner;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * Graceful No-Op Fallback implementation of ApexsionsMediaAPI.
 */
public class NoOpApexsionsMediaAPI implements ApexsionsMediaAPI {

    public static final NoOpApexsionsMediaAPI INSTANCE = new NoOpApexsionsMediaAPI();

    private NoOpApexsionsMediaAPI() {}

    @Override
    public @Nullable MediaBanner getBanner(@NotNull String id) {
        return null;
    }

    @Override
    public @NotNull Collection<MediaBanner> getAllBanners() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull CompletableFuture<Boolean> createBanner(@NotNull String id, @NotNull Location location, @NotNull BlockFace facing,
                                                            int width, int height, @NotNull String source,
                                                            @Nullable String linkUrl, @NotNull MediaBanner.ClickMode mode) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public boolean deleteBanner(@NotNull String id) {
        return false;
    }
}
