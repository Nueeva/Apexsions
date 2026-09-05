package com.apexsions.core.api;

import java.util.UUID;

/**
 * Data Transfer Object (DTO) providing clean, decoupled player profile
 * metadata for chat formatters, hover cards, and tablists.
 */
public record PlayerChatProfile(
        UUID uuid,
        String playerName,
        int level,
        long xp,
        long requiredNextXp,
        String levelTitle,
        String activeTitle,
        String rank,
        String kingdomKey,
        String kingdomDisplayName,
        boolean isMonarch,
        double balanceRupiah,
        int ping,
        int health,
        int maxHealth
) {}
