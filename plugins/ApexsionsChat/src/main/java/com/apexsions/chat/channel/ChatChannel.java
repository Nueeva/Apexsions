package com.apexsions.chat.channel;

import org.bukkit.entity.Player;

public interface ChatChannel {
    String getId();
    String getName();
    String getPrefix();
    String getPermission();
    String getFormat();
    boolean canSpeak(Player player);
    boolean canReceive(Player recipient, Player sender);
}
