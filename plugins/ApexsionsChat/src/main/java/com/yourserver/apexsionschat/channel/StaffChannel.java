package com.yourserver.apexsionschat.channel;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class StaffChannel implements ChatChannel {

    private final ApexsionsChatPlugin plugin;

    public StaffChannel(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() { return "staff"; }

    @Override
    public String getName() {
        FileConfiguration config = plugin.getConfigManager().getChannelsConfig();
        return config.getString("channels.staff.name", "Staff");
    }

    @Override
    public String getPrefix() {
        FileConfiguration config = plugin.getConfigManager().getChannelsConfig();
        return config.getString("channels.staff.prefix", "<gray>[<red>STAFF</red>]</gray>");
    }

    @Override
    public String getPermission() {
        FileConfiguration config = plugin.getConfigManager().getChannelsConfig();
        return config.getString("channels.staff.permission", "apexsionschat.channel.staff");
    }

    @Override
    public String getFormat() {
        FileConfiguration config = plugin.getConfigManager().getChannelsConfig();
        return config.getString("channels.staff.format",
                "{channel_prefix} <gray>[{rank}]</gray> <red>{player}</red> <dark_gray>»</dark_gray> <color:#fca5a5>{message}</color>");
    }

    @Override
    public boolean canSpeak(Player player) {
        return player.hasPermission(getPermission());
    }

    @Override
    public boolean canReceive(Player recipient, Player sender) {
        return recipient.hasPermission(getPermission());
    }
}
