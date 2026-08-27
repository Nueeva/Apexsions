package com.yourserver.apexsionschat.integration;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderApiHook extends PlaceholderExpansion {

    private final ApexsionsChatPlugin plugin;

    public PlaceholderApiHook(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "apexsionschat";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Antigravity";
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
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) return "";

        switch (params.toLowerCase()) {
            case "channel":
                if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
                    return plugin.getChannelManager().getPlayerChannel(offlinePlayer.getPlayer()).getName();
                }
                return "Global";

            case "unread_mail":
                try {
                    int unread = plugin.getMailRepository().countUnreadMailAsync(offlinePlayer.getUniqueId()).join();
                    return String.valueOf(unread);
                } catch (Exception e) {
                    return "0";
                }

            case "open_reports":
                try {
                    int open = plugin.getReportRepository().countOpenReportsAsync().join();
                    return String.valueOf(open);
                } catch (Exception e) {
                    return "0";
                }

            default:
                return null;
        }
    }
}
