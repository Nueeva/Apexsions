package com.apexsions.fishing.integration;

import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.model.PlayerFishingStats;
import com.apexsions.fishing.model.PlayerVaultData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * PlaceholderAPI expansion for ApexsionsFishing.
 * Provides %apexsionsfishing_*% placeholders to TAB, scoreboards, and GUIs.
 */
public class FishingPlaceholderExpansion extends PlaceholderExpansion {

    private final ApexsionsFishing plugin;

    public FishingPlaceholderExpansion(ApexsionsFishing plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "apexsionsfishing";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Nueeva / Apexsions Team";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || player.getUniqueId() == null) {
            return "";
        }

        PlayerFishingStats stats = plugin.getVaultStorageManager().getStats(player.getUniqueId());
        PlayerVaultData vault = plugin.getVaultStorageManager().getVaultData(player.getUniqueId());

        String param = params.toLowerCase(Locale.ROOT);
        switch (param) {
            case "caught_total":
            case "total_caught":
                return String.valueOf(stats.getTotalFishCaught());

            case "heaviest_weight":
                return String.format(Locale.US, "%.1f kg", stats.getHeaviestFishWeight());

            case "heaviest_fish":
                return stats.getHeaviestFishName() != null ? stats.getHeaviestFishName() : "-";

            case "vault_pages":
            case "vault_unlocked_pages":
                return String.valueOf(vault.getUnlockedPages());

            case "vault_max_pages":
                return "30";

            case "bait_count":
            case "virtual_bait":
                return String.valueOf(stats.getVirtualBait());

            case "journal_discovered":
                return String.valueOf(stats.getPersonalBestPerSpecies().size());

            case "journal_total":
                return String.valueOf(plugin.getLootGenerator().getFishTable().size());

            case "top_angler_rank":
                return "Unranked";

            default:
                return null;
        }
    }
}
