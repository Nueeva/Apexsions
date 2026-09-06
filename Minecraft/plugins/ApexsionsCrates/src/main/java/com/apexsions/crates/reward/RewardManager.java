package com.apexsions.crates.reward;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class RewardManager {

    public static double getTotalWeight(Collection<Reward> rewards) {
        if (rewards == null || rewards.isEmpty()) return 0.0;
        double total = 0.0;
        for (Reward r : rewards) {
            total += r.getWeight();
        }
        return total;
    }

    public static double calculateChance(Reward reward, Collection<Reward> rewards) {
        double total = getTotalWeight(rewards);
        if (total <= 0) return 0.0;
        return (reward.getWeight() / total) * 100.0;
    }

    public static Reward rollReward(Collection<Reward> rewards) {
        if (rewards == null || rewards.isEmpty()) return null;
        double totalWeight = getTotalWeight(rewards);
        if (totalWeight <= 0) return rewards.iterator().next();

        double random = ThreadLocalRandom.current().nextDouble(0.0, totalWeight);
        double current = 0.0;

        for (Reward reward : rewards) {
            current += reward.getWeight();
            if (random <= current) {
                return reward;
            }
        }

        return rewards.iterator().next();
    }
}
