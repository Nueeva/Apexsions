package com.apexsions.crates.integration;

import com.apexsions.battlepass.api.ApexsionsBattlepassAPI;
import com.apexsions.battlepass.api.ApexsionsBattlepassProvider;
import com.apexsions.core.api.ApexsionsCoreAPI;
import com.apexsions.core.api.ApexsionsCoreProvider;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.crates.ApexsionsCratesPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import com.apexsions.crates.api.event.CrateObtainRewardEvent;
import com.apexsions.crates.api.event.CrateOpenEvent;

public class ApexsionsIntegrationListener implements Listener {

    private final ApexsionsCratesPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ApexsionsIntegrationListener(ApexsionsCratesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCratePreOpen(CrateOpenEvent event) {
        Player player = event.getPlayer();
        if (com.apexsions.core.ApexsionsCorePlugin.getInstance() != null) {
            var combatService = com.apexsions.core.ApexsionsCorePlugin.getInstance().getCombatTagService();
            if (combatService != null && combatService.isCombatTagged(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(mm.deserialize("<red>✖ Kamu tidak dapat membuka crate saat sedang dalam status Combat Tag!</red>"));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCrateOpen(CrateOpenEvent event) {
        Player player = event.getPlayer();

        // 1. ApexsionsCore XP Integration
        if (ApexsionsCoreProvider.isAvailable()) {
            try {
                ApexsionsCoreAPI coreAPI = ApexsionsCoreProvider.get();
                coreAPI.addXp(player.getUniqueId(), 25L, XpSource.CUSTOM);
                player.sendActionBar(mm.deserialize("<gradient:#f1c40f:#e67e22>✦ +25 Core XP</gradient> <gray>(Membuka Crate " + event.getCrate().getName() + ")</gray>"));
            } catch (Exception ignored) {}
        }

        // 2. ApexsionsBattlepass Integration
        if (ApexsionsBattlepassProvider.isAvailable()) {
            try {
                ApexsionsBattlepassAPI bpAPI = ApexsionsBattlepassProvider.get();
                bpAPI.addPlayerXp(player.getUniqueId(), 15);
                bpAPI.addPlayerPoints(player.getUniqueId(), 1);
                player.sendActionBar(mm.deserialize("<gradient:#3498db:#9b59b6>✦ +15 Battlepass XP & +1 BP Point</gradient>"));
            } catch (Exception ignored) {}
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRewardObtain(CrateObtainRewardEvent event) {
        Player player = event.getPlayer();

        // Bonus XP on high-tier rewards
        if (event.getReward() != null) {
            double weight = event.getReward().getWeight();
            if (weight > 0 && weight <= 5.0) { // Rare reward (weight <= 5%)
                if (ApexsionsCoreProvider.isAvailable()) {
                    try {
                        ApexsionsCoreProvider.get().addXp(player.getUniqueId(), 100L, XpSource.CUSTOM);
                        player.sendActionBar(mm.deserialize("<gold><bold>★ JACKPOT REWARD! +100 Bonus Core XP!</bold></gold>"));
                    } catch (Exception ignored) {}
                }
                if (ApexsionsBattlepassProvider.isAvailable()) {
                    try {
                        ApexsionsBattlepassProvider.get().addPlayerXp(player.getUniqueId(), 50);
                    } catch (Exception ignored) {}
                }
            }
        }
    }
}
