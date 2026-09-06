package com.apexsions.crates.integration;

import com.apexsions.battlepass.api.ApexsionsBattlepassAPI;
import com.apexsions.battlepass.api.ApexsionsBattlepassProvider;
import com.apexsions.core.api.ApexsionsCoreAPI;
import com.apexsions.core.api.ApexsionsCoreProvider;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.api.CrateOpenEvent;
import com.apexsions.crates.api.CrateRewardWinEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ApexsionsIntegrationListener implements Listener {

    private final ApexsionsCratesPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ApexsionsIntegrationListener(ApexsionsCratesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCrateOpen(CrateOpenEvent event) {
        Player player = event.getPlayer();

        // 1. ApexsionsCore XP Integration
        if (ApexsionsCoreProvider.isAvailable()) {
            try {
                ApexsionsCoreAPI coreAPI = ApexsionsCoreProvider.get();
                coreAPI.addXp(player.getUniqueId(), 25L, XpSource.CUSTOM);
                player.sendMessage(mm.deserialize("<gradient:#f1c40f:#e67e22>✦ +25 Core XP</gradient> <gray>(Membuka Peti " + event.getCrate().getName() + ")</gray>"));
            } catch (Exception ignored) {}
        }

        // 2. ApexsionsBattlepass Integration
        if (ApexsionsBattlepassProvider.isAvailable()) {
            try {
                ApexsionsBattlepassAPI bpAPI = ApexsionsBattlepassProvider.get();
                bpAPI.addPlayerXp(player.getUniqueId(), 15);
                bpAPI.addPlayerPoints(player.getUniqueId(), 1);
                player.sendMessage(mm.deserialize("<gradient:#3498db:#9b59b6>✦ +15 Battlepass XP & +1 BP Point</gradient>"));
            } catch (Exception ignored) {}
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRewardWin(CrateRewardWinEvent event) {
        Player player = event.getPlayer();
        if (event.getReward().getRarity().getTier() >= 5) { // Legendary or higher
            if (ApexsionsCoreProvider.isAvailable()) {
                try {
                    ApexsionsCoreAPI coreAPI = ApexsionsCoreProvider.get();
                    coreAPI.addXp(player.getUniqueId(), 100L, XpSource.CUSTOM);
                    player.sendMessage(mm.deserialize("<gold><bold>★ JACKPOT REWARD! +100 Bonus Core XP!</bold></gold>"));
                } catch (Exception ignored) {}
            }
            if (ApexsionsBattlepassProvider.isAvailable()) {
                try {
                    ApexsionsBattlepassAPI bpAPI = ApexsionsBattlepassProvider.get();
                    bpAPI.addPlayerXp(player.getUniqueId(), 50);
                } catch (Exception ignored) {}
            }
        }
    }
}
