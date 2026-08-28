package com.apex.battlepass.api.event;

import com.apex.battlepass.reward.RewardItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class BattlePassRewardClaimEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final int level;
    private final String passId;
    private final List<RewardItem> rewards;

    public BattlePassRewardClaimEvent(Player player, int level, String passId, List<RewardItem> rewards) {
        this.player = player;
        this.level = level;
        this.passId = passId;
        this.rewards = rewards;
    }

    public Player getPlayer() {
        return player;
    }

    public int getLevel() {
        return level;
    }

    public String getPassId() {
        return passId;
    }

    public List<RewardItem> getRewards() {
        return rewards;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
