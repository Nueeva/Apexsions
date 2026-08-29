package com.apexsions.core.level;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.event.KingdomLevelUpEvent;
import com.apexsions.core.event.KingdomXpGainEvent;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Core Level and XP Progression Manager.
 */
public class LevelManager {

    private final ApexsionsCorePlugin plugin;
    private final LevelFormula formula;
    private final LevelTitleResolver titleResolver;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public LevelManager(ApexsionsCorePlugin plugin, LevelFormula formula, LevelTitleResolver titleResolver) {
        this.plugin = plugin;
        this.formula = formula;
        this.titleResolver = titleResolver;
    }

    public int getLevel(UUID uuid) {
        return plugin.getPlayerDataService().getCached(uuid)
                .map(PlayerData::getLevel)
                .orElse(1);
    }

    public long getXp(UUID uuid) {
        return plugin.getPlayerDataService().getCached(uuid)
                .map(PlayerData::getXp)
                .orElse(0L);
    }

    public String getLevelTitle(UUID uuid) {
        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(uuid);
        if (dataOpt.isEmpty()) {
            return "Citizen";
        }
        PlayerData data = dataOpt.get();
        Region region = data.getRegionId() != null ? plugin.getRegionManager().getRegion(data.getRegionId()).orElse(null) : null;
        return titleResolver.resolveTitle(region, data.getLevel());
    }

    public long getRequiredXpForNextLevel(int currentLevel) {
        return formula.getRequiredXpForNextLevel(currentLevel);
    }

    public void addXp(UUID uuid, long amount, XpSource source) {
        if (amount <= 0) return;

        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(uuid);
        if (dataOpt.isEmpty()) return;

        PlayerData data = dataOpt.get();
        int maxLevel = plugin.getConfigManager().getLevelMax();

        if (data.getLevel() >= maxLevel && !plugin.getConfigManager().isAllowOverflow()) {
            return; // At max level and overflow is disallowed
        }

        Player player = Bukkit.getPlayer(uuid);

        long oldXp = data.getXp();
        long newXp = oldXp + amount;

        // Fire XP gain event
        KingdomXpGainEvent xpEvent = new KingdomXpGainEvent(uuid, player, source, amount, oldXp, newXp);
        Bukkit.getPluginManager().callEvent(xpEvent);
        if (xpEvent.isCancelled()) {
            return;
        }

        amount = xpEvent.getAmount();
        newXp = oldXp + amount;
        data.setXp(newXp);

        // Check for level ups
        int currentLevel = data.getLevel();
        int startingLevel = currentLevel;

        while (currentLevel < maxLevel) {
            long reqXp = formula.getRequiredXpForNextLevel(currentLevel);
            if (data.getXp() >= reqXp) {
                data.setXp(data.getXp() - reqXp);
                currentLevel++;
                data.setLevel(currentLevel);
            } else {
                break;
            }
        }

        // If level up occurred
        if (currentLevel > startingLevel) {
            handleLevelUp(data, player, startingLevel, currentLevel);
        }

        // Async save
        plugin.getPlayerDataService().save(data);
    }

    private void handleLevelUp(PlayerData data, Player player, int oldLevel, int newLevel) {
        Region region = data.getRegionId() != null ? plugin.getRegionManager().getRegion(data.getRegionId()).orElse(null) : null;
        String oldTitle = titleResolver.resolveTitle(region, oldLevel);
        String newTitle = titleResolver.resolveTitle(region, newLevel);

        // Fire event
        KingdomLevelUpEvent event = new KingdomLevelUpEvent(data.getUuid(), player, oldLevel, newLevel, region, oldTitle, newTitle);
        Bukkit.getPluginManager().callEvent(event);

        if (player != null && player.isOnline()) {
            // Visual / Audio feedback
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            // Particle Halo & Burst
            org.bukkit.Location pLoc = player.getLocation().clone();
            pLoc.getWorld().spawnParticle(org.bukkit.Particle.TOTEM_OF_UNDYING, pLoc.clone().add(0, 1.2, 0), 40, 0.5, 0.8, 0.5, 0.3);
            pLoc.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, pLoc.clone().add(0, 2.0, 0), 20, 0.4, 0.2, 0.4, 0.05);
            pLoc.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, pLoc.clone().add(0, 1.0, 0), 30, 0.6, 0.6, 0.6, 0.1);

            Component titleText = miniMessage.deserialize("<gradient:#f1c40f:#e67e22><bold>✦ LEVEL UP! ✦</bold></gradient>");
            Component subtitleText = miniMessage.deserialize("<yellow>Level <white>" + newLevel + "</white> <gray>•</gray> <gold>" + newTitle + "</gold></yellow>");

            Title title = Title.title(titleText, subtitleText, Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(2500), Duration.ofMillis(800)));
            player.showTitle(title);

            player.sendMessage(miniMessage.deserialize("<gradient:#ffe900:#f39c12><bold>════════════════ [ LEVEL UP! ] ════════════════</bold></gradient>"));
            player.sendMessage(miniMessage.deserialize("<gray>Selamat! Kamu telah naik dari <gold>Level " + oldLevel + "</gold> ke <gold><bold>Level " + newLevel + "</bold></gold>!</gray>"));
            player.sendMessage(miniMessage.deserialize("<gray>Gelar Tingkat: <yellow><bold>" + newTitle + "</bold></yellow></gray>"));
            player.sendMessage(miniMessage.deserialize("<gray>Hadiah Level: <green><bold><click:run_command:'/kingdom rewards'><hover:show_text:'<yellow>Klik untuk membuka Menu Hadiah</yellow>'><u>[KLIK DI SINI UNTUK KLAIM REWARD]</u></click></bold></green></gray>"));
            player.sendMessage(miniMessage.deserialize("<gradient:#ffe900:#f39c12><bold>═══════════════════════════════════════════════</bold></gradient>"));
        }

        // Server milestone broadcast for major tiers
        if (newLevel % 10 == 0 || newLevel == 25 || newLevel == 75) {
            String kName = region != null ? region.getDisplayName() : "Apexsions";
            String pName = player != null ? player.getName() : data.getUsername();
            Component milestoneMsg = miniMessage.deserialize("<gradient:#f39c12:#f1c40f><bold>👑 APEXSIONS MILESTONE 👑</bold></gradient> <yellow>Pemain <white>" + pName + "</white> dari kerajaan <gold>" + kName + "</gold> baru saja mencapai <gold><bold>Level " + newLevel + " (" + newTitle + ")</bold></gold>!</yellow>");
            Bukkit.broadcast(milestoneMsg);
        }

        // Milestone reward check
        if (plugin.getRewardManager() != null) {
            plugin.getRewardManager().getReward(newLevel).ifPresent(reward -> {
                if (reward.isMilestone() && reward.getBroadcast() != null && !reward.getBroadcast().isEmpty() && player != null) {
                    String kName = region != null ? region.getDisplayName() : "Apexsions";
                    String msg = reward.getBroadcast()
                            .replace("%player%", player.getName())
                            .replace("%level%", String.valueOf(newLevel))
                            .replace("%kingdom%", kName);
                    Bukkit.broadcast(miniMessage.deserialize(msg));
                }
            });
        }
    }

    public void setLevel(UUID uuid, int level) {
        plugin.getPlayerDataService().getCached(uuid).ifPresent(data -> {
            int maxLevel = plugin.getConfigManager().getLevelMax();
            int minLevel = plugin.getConfigManager().getLevelMin();
            int clamped = Math.clamp(level, minLevel, maxLevel);
            int oldLevel = data.getLevel();
            data.setLevel(clamped);

            Player player = Bukkit.getPlayer(uuid);
            if (clamped > oldLevel) {
                handleLevelUp(data, player, oldLevel, clamped);
            }
            plugin.getPlayerDataService().save(data);
        });
    }

    public void setXp(UUID uuid, long xp) {
        plugin.getPlayerDataService().getCached(uuid).ifPresent(data -> {
            data.setXp(Math.max(0, xp));
            plugin.getPlayerDataService().save(data);
        });
    }
}
