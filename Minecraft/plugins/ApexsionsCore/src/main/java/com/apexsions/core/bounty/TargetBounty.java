package com.apexsions.core.bounty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory aggregate of all active bounty contributions placed on a single target.
 */
public class TargetBounty {

    public record Contributor(UUID uuid, String name, double amount) {
    }

    private final UUID targetUuid;
    private volatile String targetName;
    private final Map<UUID, Contributor> contributors = new ConcurrentHashMap<>();

    public TargetBounty(UUID targetUuid, String targetName) {
        this.targetUuid = targetUuid;
        this.targetName = targetName;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        if (targetName != null && !targetName.isBlank()) {
            this.targetName = targetName;
        }
    }

    public void add(UUID placerUuid, String placerName, double amount) {
        if (placerUuid == null || amount <= 0) return;
        contributors.compute(placerUuid, (k, existing) -> {
            double sum = (existing != null ? existing.amount() : 0.0) + amount;
            String name = placerName != null ? placerName : (existing != null ? existing.name() : "Unknown");
            return new Contributor(placerUuid, name, sum);
        });
    }

    public void clear() {
        contributors.clear();
    }

    public double total() {
        double sum = 0.0;
        for (Contributor c : contributors.values()) {
            sum += c.amount();
        }
        return sum;
    }

    public List<Contributor> contributors() {
        List<Contributor> list = new ArrayList<>(contributors.values());
        list.sort(Comparator.comparingDouble(Contributor::amount).reversed());
        return list;
    }

    public int size() {
        return contributors.size();
    }
}
