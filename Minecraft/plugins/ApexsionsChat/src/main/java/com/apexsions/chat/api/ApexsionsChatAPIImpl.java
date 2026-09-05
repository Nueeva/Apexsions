package com.apexsions.chat.api;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.chat.channel.ChatChannel;
import com.apexsions.chat.model.Mail;
import com.apexsions.chat.model.Report;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ApexsionsChatAPIImpl implements ApexsionsChatAPI {

    private final ApexsionsChatPlugin plugin;

    public ApexsionsChatAPIImpl(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull CompletableFuture<Long> sendMail(@NotNull UUID senderUuid, @NotNull String senderName,
                                                     @NotNull UUID recipientUuid, @NotNull String recipientName,
                                                     @NotNull String subject, @NotNull String body) {
        Mail mail = new Mail(senderUuid, senderName, recipientUuid, recipientName, subject, body);
        return plugin.getMailRepository().sendMailAsync(mail);
    }

    @Override
    public @NotNull CompletableFuture<Integer> getUnreadMailCount(@NotNull UUID playerUuid) {
        return plugin.getMailRepository().countUnreadMailAsync(playerUuid);
    }

    @Override
    public @NotNull CompletableFuture<Long> createReport(@NotNull Report report) {
        return plugin.getReportRepository().createReportAsync(report);
    }

    @Override
    public @Nullable ChatChannel getPlayerChannel(@NotNull Player player) {
        return plugin.getChannelManager().getPlayerChannel(player);
    }

    @Override
    public boolean setPlayerChannel(@NotNull Player player, @NotNull String channelId) {
        return plugin.getChannelManager().setPlayerChannel(player, channelId);
    }

    @Override
    public void broadcastAnnouncement(@NotNull String miniMessageContent) {
        Bukkit.broadcast(MiniMessage.miniMessage().deserialize(miniMessageContent));
    }
}
