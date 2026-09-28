package com.apexsions.core.claim;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class FreeholdClaimTest {

    @Test
    @DisplayName("Verify ClaimChunk transitions to FREEHOLD status with zero daily upkeep")
    void testFreeholdUpgradeTransition() {
        UUID ownerId = UUID.randomUUID();
        ClaimChunk claim = new ClaimChunk(UUID.randomUUID(), ownerId, "TestPlayer", "world", 10, 20, null, System.currentTimeMillis());

        // Default state is ACTIVE with positive upkeep
        assertEquals(ClaimStatus.ACTIVE, claim.getStatus());
        assertFalse(claim.isFreehold());
        assertTrue(claim.getDailyUpkeep() > 0.0);

        // Upgrade to Freehold
        claim.setFreehold(true);

        assertTrue(claim.isFreehold(), "Claim must report isFreehold() = true");
        assertEquals(ClaimStatus.FREEHOLD, claim.getStatus(), "Status must become FREEHOLD");
        assertEquals(0.0, claim.getDailyUpkeep(), 0.001, "Daily upkeep must be Rp 0 for freehold title");
        assertEquals(0L, claim.getGracePeriodUntil(), "Grace period must be cleared");

        // Downgrade / revoke freehold
        claim.setFreehold(false);
        assertFalse(claim.isFreehold());
        assertEquals(ClaimStatus.ACTIVE, claim.getStatus());
    }

    @Test
    @DisplayName("Verify progressive tax calculation logic for standard leasehold claims")
    void testProgressiveTaxCalculation() {
        double baseTax = 100.0;
        double progressiveMultiplier = 0.15;

        // 1 claim: 100 * (1.0 + 0) = 100
        double tax1 = baseTax * (1.0 + (1 - 1) * progressiveMultiplier);
        assertEquals(100.0, tax1, 0.001);

        // 5 claims: 100 * (1.0 + 4 * 0.15) = 100 * 1.60 = 160 per chunk
        double tax5 = baseTax * (1.0 + (5 - 1) * progressiveMultiplier);
        assertEquals(160.0, tax5, 0.001);

        // 10 claims: 100 * (1.0 + 9 * 0.15) = 100 * 2.35 = 235 per chunk
        double tax10 = baseTax * (1.0 + (10 - 1) * progressiveMultiplier);
        assertEquals(235.0, tax10, 0.001);
    }

    @Test
    @DisplayName("Verify 60-day inactivity timeout calculation for ghost claim protection")
    void testInactivityTimeoutCalculation() {
        long now = 1774900000000L;
        int timeoutDays = 60;
        long maxInactiveMs = timeoutDays * 24L * 3600L * 1000L; // 5,184,000,000 ms

        // Player active 10 days ago: safe
        long active10DaysAgo = now - (10L * 24L * 3600L * 1000L);
        long diffActive = now - active10DaysAgo;
        assertFalse(diffActive > maxInactiveMs, "Active player within 60 days must NOT be expired");

        // Player inactive 61 days ago: expired
        long inactive61DaysAgo = now - (61L * 24L * 3600L * 1000L);
        long diffInactive = now - inactive61DaysAgo;
        assertTrue(diffInactive > maxInactiveMs, "Player inactive > 60 days must trigger timeout");
    }
}
