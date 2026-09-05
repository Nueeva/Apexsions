package com.apexsions.media.api;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.banner.MediaBanner;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ApexsionsMediaAPIImpl implements ApexsionsMediaAPI {

    private final ApexsionsMediaPlugin plugin;

    public ApexsionsMediaAPIImpl(ApexsionsMediaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable MediaBanner getBanner(@NotNull String id) {
        return plugin.getBannerManager().getBanner(id);
    }

    @Override
    public @NotNull Collection<MediaBanner> getAllBanners() {
        return plugin.getBannerManager().getAllBanners();
    }

    @Override
    public @NotNull CompletableFuture<Boolean> createBanner(@NotNull String id, @NotNull Location location, @NotNull BlockFace facing,
                                                            int width, int height, @NotNull String source,
                                                            @Nullable String linkUrl, @NotNull MediaBanner.ClickMode mode) {
        return plugin.getBannerManager().createAndSpawnBanner(id, location, facing, width, height, source, linkUrl, mode);
    }

    @Override
    public boolean deleteBanner(@NotNull String id) {
        return plugin.getBannerManager().deleteBanner(id);
    }
}
