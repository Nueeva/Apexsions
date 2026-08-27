package com.yourserver.apexsionschat.api;

import com.yourserver.apexsionschat.channel.ChatChannel;
import com.yourserver.apexsionschat.model.Mail;
import com.yourserver.apexsionschat.model.Report;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ApexsionsChatAPI {

    @NotNull
    CompletableFuture<Long> sendMail(@NotNull UUID senderUuid, @NotNull String senderName,
                                     @NotNull UUID recipientUuid, @NotNull String recipientName,
                                     @NotNull String subject, @NotNull String body);

    @NotNull
    CompletableFuture<Integer> getUnreadMailCount(@NotNull UUID playerUuid);

    @NotNull
    CompletableFuture<Long> createReport(@NotNull Report report);

    @NotNull
    ChatChannel getPlayerChannel(@NotNull Player player);

    boolean setPlayerChannel(@NotNull Player player, @NotNull String channelId);

    void broadcastAnnouncement(@NotNull String miniMessageContent);
}
