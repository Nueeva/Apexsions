package com.apexsions.media.api;

import com.apexsions.media.banner.MediaBanner;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Public service interface for ApexsionsMedia.
 */
public interface ApexsionsMediaAPI {

    @Nullable
    MediaBanner getBanner(@NotNull String id);

    @NotNull
    Collection<MediaBanner> getAllBanners();

    @NotNull
    CompletableFuture<Boolean> createBanner(@NotNull String id, @NotNull Location location, @NotNull BlockFace facing,
                                            int width, int height, @NotNull String source,
                                            @Nullable String linkUrl, @NotNull MediaBanner.ClickMode mode);

    boolean deleteBanner(@NotNull String id);
}
