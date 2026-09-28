package com.apexsions.jobs.integration;

import com.apexsions.jobs.ApexsionsJobs;
import com.apexsions.jobs.model.JobDefinition;
import com.apexsions.jobs.model.PlayerJobData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final ApexsionsJobs plugin;

    public PlaceholderAPIExpansion(ApexsionsJobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "apexsionsjobs";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Nueeva";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        Map<String, PlayerJobData> map = plugin.getJobService().getPlayerJobs(player.getUniqueId());
        PlayerJobData active = null;
        for (PlayerJobData d : map.values()) {
            if (d.isActive()) {
                active = d;
                break;
            }
        }

        String lower = params.toLowerCase();
        switch (lower) {
            case "active" -> {
                if (active == null) return "None";
                JobDefinition def = plugin.getJobService().getDefinition(active.getJobId());
                return def != null ? def.getName() : active.getJobId();
            }
            case "level" -> {
                return String.valueOf(active != null ? active.getLevel() : 1);
            }
            case "exp" -> {
                return String.format("%.0f", active != null ? active.getExp() : 0.0);
            }
            case "req_exp" -> {
                return String.format("%.0f", active != null ? active.getRequiredExp() : 125.0);
            }
            case "daily_earned" -> {
                return String.format("%,.0f", active != null ? active.getDailyEarnings() : 0.0);
            }
        }

        if (lower.startsWith("level_")) {
            String jobId = lower.replace("level_", "");
            PlayerJobData d = map.get(jobId);
            return String.valueOf(d != null ? d.getLevel() : 1);
        }

        return null;
    }
}
