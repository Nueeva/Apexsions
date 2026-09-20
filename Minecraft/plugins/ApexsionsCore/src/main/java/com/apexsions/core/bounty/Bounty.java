package com.apexsions.core.bounty;

import java.util.UUID;

/**
 * Immutable persisted bounty row (one contributor placing an amount on a target).
 */
public record Bounty(
        String id,
        UUID targetUuid,
        String targetName,
        UUID placerUuid,
        String placerName,
        double amount,
        boolean active,
        UUID claimedBy,
        long claimedAt
) {
}
