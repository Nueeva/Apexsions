package com.apexsions.crates.milestone;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Milestone {

    private final int requiredOpens;
    private final String name;
    private final List<String> rewardCommands;
    private final String broadcast;

    public Milestone(int requiredOpens, String name, List<String> rewardCommands, String broadcast) {
        this.requiredOpens = requiredOpens;
        this.name = name != null ? name : "Milestone " + requiredOpens;
        this.rewardCommands = rewardCommands != null ? rewardCommands : new ArrayList<>();
        this.broadcast = broadcast;
    }

    public int getRequiredOpens() {
        return requiredOpens;
    }

    public String getName() {
        return name;
    }

    public Component getDisplayName() {
        return MiniMessage.miniMessage().deserialize(name);
    }

    public List<String> getRewardCommands() {
        return rewardCommands;
    }

    public String getBroadcast() {
        return broadcast;
    }

    public void award(Player player) {
        for (String cmd : rewardCommands) {
            String processed = cmd.replace("%player%", player.getName()).trim();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processed);
        }

        if (broadcast != null && !broadcast.isBlank()) {
            String processed = broadcast.replace("%player%", player.getName()).trim();
            Bukkit.broadcast(MiniMessage.miniMessage().deserialize(processed));
        }
    }
}
