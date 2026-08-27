package com.yourserver.apexsionschat.channel;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class KingdomChannel implements ChatChannel {

    private final ApexsionsChatPlugin plugin;

    public KingdomChannel(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() { return "kingdom"; }

    @Override
    public String getName() {
        FileConfiguration config = plugin.getConfigManager().getChannelsConfig();
        return config.getString("channels.kingdom.name", "Kingdom");
    }

    @Override
    public String getPrefix() {
        FileConfiguration config = plugin.getConfigManager().getChannelsConfig();
        return config.getString("channels.kingdom.prefix", "<gray>[<gold>Kingdom</gold>]</gray>");
    }

    @Override
    public String getPermission() {
        FileConfiguration config = plugin.getConfigManager().getChannelsConfig();
        return config.getString("channels.kingdom.permission", "apexsionschat.channel.kingdom");
    }

    @Override
    public String getFormat() {
        FileConfiguration config = plugin.getConfigManager().getChannelsConfig();
        return config.getString("channels.kingdom.format",
                "{channel_prefix} <gray>[<yellow>Lv. {level}</yellow>]</gray> <gray>[{rank}]</gray> {kingdom} <white>{player}</white> <dark_gray>»</dark_gray> <yellow>{message}</yellow>");
    }

    @Override
    public boolean canSpeak(Player player) {
        if (!player.hasPermission(getPermission())) return false;
        String regionKey = plugin.getApexsionsCoreHook().getPlayerRegionKey(player.getUniqueId());
        return !regionKey.equalsIgnoreCase("NONE");
    }

    @Override
    public boolean canReceive(Player recipient, Player sender) {
        if (sender == null || recipient == null) return false;
        String senderRegion = plugin.getApexsionsCoreHook().getPlayerRegionKey(sender.getUniqueId());
        String recipientRegion = plugin.getApexsionsCoreHook().getPlayerRegionKey(recipient.getUniqueId());
        return !senderRegion.equalsIgnoreCase("NONE") && senderRegion.equalsIgnoreCase(recipientRegion);
    }
}
