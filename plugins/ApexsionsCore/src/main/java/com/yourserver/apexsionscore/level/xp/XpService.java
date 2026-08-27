package com.yourserver.apexsionscore.level.xp;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;

import java.util.UUID;

/**
 * Service providing high level operations for granting and managing player XP.
 */
public class XpService {

    private final ApexsionsCorePlugin plugin;

    public XpService(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void awardXp(UUID playerUuid, long amount, XpSource source) {
        plugin.getLevelManager().addXp(playerUuid, amount, source);
    }
}
