package com.apexsions.fishing;

import com.apexsions.core.api.ApexsionsCoreAPI;
import com.apexsions.core.api.ApexsionsCoreProvider;
import com.apexsions.fishing.command.FishCommand;
import com.apexsions.fishing.gui.FishingGUIListener;
import com.apexsions.fishing.listener.FishingListener;
import com.apexsions.fishing.listener.RodAnvilMergeListener;
import com.apexsions.fishing.listener.RodLevelRestrictionListener;
import com.apexsions.fishing.service.AFKFishingService;
import com.apexsions.fishing.service.AutoCatchRodManager;
import com.apexsions.fishing.service.LootGenerator;
import com.apexsions.fishing.service.VaultStorageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * ApexsionsFishing — The Peak Civilizations Fishing Ecosystem.
 * Features:
 * - AFK Fishing Engine with custom strike speed & auto-reel.
 * - Dynamic Species Loot Table with Gaussian weight and Rarity (Common -> Secret).
 * - Multi-Page Fishing Vaults (up to 30 pages) with strict fishing-only item restriction.
 * - Dual-Currency Expansion Shop (Rupiah up to page 5, Diamond for pages 6-30).
 * - Official Auto-Catch Rod Shop (Anvil-merge safe with custom enchants).
 * - Dedicated Admin Rod Creator GUI (Level requirement, custom model, no armor set bonus).
 * - Fish Selling GUI (Strictly accepts fish, rejects junk, dynamic price payout).
 * - Player Fishing Profile Hub, Fish-o-pedia Journal, and Top 10 Leaderboards.
 */
public class ApexsionsFishing extends JavaPlugin {

    private static ApexsionsFishing instance;

    private AutoCatchRodManager rodManager;
    private LootGenerator lootGenerator;
    private VaultStorageManager vaultStorageManager;
    private AFKFishingService afkFishingService;

    public static ApexsionsFishing getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Save default configs if not present
        saveDefaultConfig();
        saveResourceIfNotExists("loot.yml");
        saveResourceIfNotExists("rods.yml");
        saveResourceIfNotExists("vault-prices.yml");

        // Initialize Services
        this.rodManager = new AutoCatchRodManager(this);
        this.lootGenerator = new LootGenerator(this);
        this.vaultStorageManager = new VaultStorageManager(this);
        this.afkFishingService = new AFKFishingService(this);

        // Register Listeners
        Bukkit.getPluginManager().registerEvents(new FishingListener(this), this);
        Bukkit.getPluginManager().registerEvents(new RodAnvilMergeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new RodLevelRestrictionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FishingGUIListener(), this);

        // Register Commands
        FishCommand fishCmd = new FishCommand(this);
        if (getCommand("fish") != null) {
            Objects.requireNonNull(getCommand("fish")).setExecutor(fishCmd);
            Objects.requireNonNull(getCommand("fish")).setTabCompleter(fishCmd);
        }
        if (getCommand("vault") != null) {
            Objects.requireNonNull(getCommand("vault")).setExecutor(fishCmd);
            Objects.requireNonNull(getCommand("vault")).setTabCompleter(fishCmd);
        }

        getLogger().info("=========================================");
        getLogger().info(" ApexsionsFishing v" + getDescription().getVersion() + " telah aktif!");
        getLogger().info(" The Peak Civilizations — Fishing Module ");
        getLogger().info("=========================================");
    }

    @Override
    public void onDisable() {
        if (afkFishingService != null) {
            afkFishingService.cancelAllTasks();
        }
        if (vaultStorageManager != null) {
            vaultStorageManager.saveAll();
        }
        getLogger().info("ApexsionsFishing telah dinonaktifkan dengan aman.");
    }

    public AutoCatchRodManager getRodManager() {
        return rodManager;
    }

    public LootGenerator getLootGenerator() {
        return lootGenerator;
    }

    public VaultStorageManager getVaultStorageManager() {
        return vaultStorageManager;
    }

    public VaultStorageManager getVaultStorage() {
        return vaultStorageManager;
    }

    public AFKFishingService getAfkFishingService() {
        return afkFishingService;
    }

    public int getPlayerCoreLevel(Player player) {
        if (player == null) return 1;
        try {
            ApexsionsCoreAPI coreAPI = ApexsionsCoreProvider.get();
            if (coreAPI != null) {
                return coreAPI.getLevel(player.getUniqueId());
            }
        } catch (Throwable ignored) {}
        return player.getLevel();
    }

    private void saveResourceIfNotExists(String resourcePath) {
        java.io.File file = new java.io.File(getDataFolder(), resourcePath);
        if (!file.exists()) {
            saveResource(resourcePath, false);
        }
    }
}
