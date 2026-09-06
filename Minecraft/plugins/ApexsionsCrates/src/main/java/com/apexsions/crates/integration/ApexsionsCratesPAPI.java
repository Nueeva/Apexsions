package com.apexsions.crates.integration;

import com.apexsions.crates.ApexsionsCratesPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ApexsionsCratesPAPI extends PlaceholderExpansion {

    private final ApexsionsCratesPlugin plugin;

    public ApexsionsCratesPAPI(ApexsionsCratesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "apexsionscrates";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Nueeva";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        // %apexsionscrates_keys_<keyid>%
        if (params.startsWith("keys_")) {
            String keyId = params.substring(5).toLowerCase();
            try {
                return String.valueOf(plugin.getRepository().getVirtualKeys(player.getUniqueId(), keyId).getNow(0));
            } catch (Exception e) {
                return "0";
            }
        }

        // %apexsionscrates_opened_<crateid>%
        if (params.startsWith("opened_")) {
            String crateId = params.substring(7).toLowerCase();
            try {
                return String.valueOf(plugin.getRepository().getCrateOpenCount(player.getUniqueId(), crateId).getNow(0));
            } catch (Exception e) {
                return "0";
            }
        }

        return null;
    }
}
