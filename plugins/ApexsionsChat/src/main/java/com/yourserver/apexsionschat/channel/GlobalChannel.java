package com.yourserver.apexsionschat.channel;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class GlobalChannel implements ChatChannel {

    private final ApexsionsChatPlugin plugin;

    public GlobalChannel(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() { return "global"; }

    @Override
    public String getName() {
        FileConfiguration config = plugin.getConfigManager().getChannelsConfig();
        return config.getString("channels.global.name", "Global");
    }

    @Override
    public String getPrefix() {
        FileConfiguration config = plugin.getConfigManager().getChannelsConfig();
        return config.getString("channels.global.prefix", "<gray>[<blue>G</blue>]</gray>");
    }

    @Override
    public String getPermission() {
        FileConfiguration config = plugin.getConfigManager().getChannelsConfig();
        return config.getString("channels.global.permission", "apexsionschat.channel.global");
    }

    @Override
    public String getFormat() {
        FileConfiguration config = plugin.getConfigManager().getChannelsConfig();
        return config.getString("channels.global.format",
                "{channel_prefix} <gray>[<yellow>Lv. {level} <gold>{title}</gold></yellow>]</gray> <gray>[{rank}]</gray> {kingdom} <white>{player}</white> <dark_gray>»</dark_gray> <white>{message}</white>");
    }

    @Override
    public boolean canSpeak(Player player) {
        return player.hasPermission(getPermission());
    }

    @Override
    public boolean canReceive(Player recipient, Player sender) {
        return true;
    }
}
