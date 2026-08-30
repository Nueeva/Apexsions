package com.apexsions.chat.api;

import com.apexsions.chat.channel.ChatChannel;
import com.apexsions.chat.model.Report;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Graceful No-Op Fallback implementation of ApexsionsChatAPI.
 */
public class NoOpApexsionsChatAPI implements ApexsionsChatAPI {

    public static final NoOpApexsionsChatAPI INSTANCE = new NoOpApexsionsChatAPI();

    private NoOpApexsionsChatAPI() {}

    @Override
    public @NotNull CompletableFuture<Long> sendMail(@NotNull UUID senderUuid, @NotNull String senderName,
                                                     @NotNull UUID recipientUuid, @NotNull String recipientName,
                                                     @NotNull String subject, @NotNull String body) {
        return CompletableFuture.completedFuture(-1L);
    }

    @Override
    public @NotNull CompletableFuture<Integer> getUnreadMailCount(@NotNull UUID playerUuid) {
        return CompletableFuture.completedFuture(0);
    }

    @Override
    public @NotNull CompletableFuture<Long> createReport(@NotNull Report report) {
        return CompletableFuture.completedFuture(-1L);
    }

    @Override
    public @Nullable ChatChannel getPlayerChannel(@NotNull Player player) {
        return null;
    }

    @Override
    public boolean setPlayerChannel(@NotNull Player player, @NotNull String channelId) {
        return false;
    }

    @Override
    public void broadcastAnnouncement(@NotNull String miniMessageContent) {
        // No-Op
    }
}
