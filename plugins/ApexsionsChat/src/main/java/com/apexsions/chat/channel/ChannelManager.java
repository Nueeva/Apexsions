package com.apexsions.chat.channel;

import com.apexsions.chat.ApexsionsChatPlugin;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelManager {

    private final ApexsionsChatPlugin plugin;
    private final Map<String, ChatChannel> channels = new HashMap<>();
    private final Map<UUID, String> playerActiveChannels = new ConcurrentHashMap<>();

    public ChannelManager(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
        registerDefaultChannels();
    }

    public void registerDefaultChannels() {
        channels.clear();
        registerChannel(new GlobalChannel(plugin));
        registerChannel(new KingdomChannel(plugin));
        registerChannel(new StaffChannel(plugin));
    }

    public void registerChannel(ChatChannel channel) {
        channels.put(channel.getId().toLowerCase(), channel);
    }

    public Optional<ChatChannel> getChannel(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(channels.get(id.toLowerCase()));
    }

    public ChatChannel getPlayerChannel(Player player) {
        if (player == null) return channels.get("global");
        String chId = playerActiveChannels.getOrDefault(player.getUniqueId(), "global");
        return channels.getOrDefault(chId, channels.get("global"));
    }

    public boolean setPlayerChannel(Player player, String channelId) {
        Optional<ChatChannel> chOpt = getChannel(channelId);
        if (chOpt.isPresent()) {
            ChatChannel channel = chOpt.get();
            if (channel.canSpeak(player)) {
                playerActiveChannels.put(player.getUniqueId(), channel.getId());
                return true;
            }
        }
        return false;
    }

    public Collection<ChatChannel> getChannels() {
        return Collections.unmodifiableCollection(channels.values());
    }
}
