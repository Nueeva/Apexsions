package com.apexsions.media;

import com.apexsions.media.banner.MediaBannerManager;
import com.apexsions.media.command.MediaCommand;
import com.apexsions.media.engine.ImageRenderer;
import com.apexsions.media.listener.MediaInteractListener;
import com.apexsions.media.raytrace.MediaRaytraceService;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ApexsionsMediaPlugin extends JavaPlugin {

    // Apexsions Media Plugin Instance
    private static ApexsionsMediaPlugin instance;
    private ImageRenderer imageRenderer;
    private MediaBannerManager bannerManager;
    private MediaRaytraceService raytraceService;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Ensure images directory
        File imgDir = new File(getDataFolder(), "images");
        if (!imgDir.exists()) {
            imgDir.mkdirs();
        }

        int maxCached = getConfig().getInt("cache.max-cached-images", 50);
        int expireMin = getConfig().getInt("cache.expire-after-access-minutes", 60);

        this.imageRenderer = new ImageRenderer(maxCached, expireMin);
        this.bannerManager = new MediaBannerManager(this);
        this.raytraceService = new MediaRaytraceService(this);

        // Register Command & Listener
        MediaCommand cmd = new MediaCommand(this);
        if (getCommand("media") != null) {
            getCommand("media").setExecutor(cmd);
            getCommand("media").setTabCompleter(cmd);
        }

        getServer().getPluginManager().registerEvents(new MediaInteractListener(this), this);

        // Register Public API
        com.apexsions.media.api.ApexsionsMediaProvider.register(new com.apexsions.media.api.ApexsionsMediaAPIImpl(this));

        // Start Raytrace Service
        this.raytraceService.start();

        getLogger().info("========================================");
        getLogger().info("ApexsionsMedia v" + getDescription().getVersion() + " enabled successfully!");
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        com.apexsions.media.api.ApexsionsMediaProvider.unregister();
        if (raytraceService != null) {
            raytraceService.stop();
        }
        if (bannerManager != null) {
            bannerManager.shutdown();
        }
        getLogger().info("ApexsionsMedia disabled.");
        instance = null;
    }

    public static ApexsionsMediaPlugin getInstance() {
        return instance;
    }

    public ImageRenderer getImageRenderer() {
        return imageRenderer;
    }

    public MediaBannerManager getBannerManager() {
        return bannerManager;
    }

    public MediaRaytraceService getRaytraceService() {
        return raytraceService;
    }
}
