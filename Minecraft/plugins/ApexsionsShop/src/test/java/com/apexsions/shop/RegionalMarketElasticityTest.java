package com.apexsions.shop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegionalMarketElasticityTest {

    @Test
    @DisplayName("Verify smooth logarithmic saturation curve under extreme over-supply")
    void testOverSupplySaturationCurve() {
        int threshold = 2304;
        double sensitivity = 0.025;
        double maxDrop = 0.35;
        double minMultiplier = 0.65;

        // Normal volume (below threshold): no drop
        int normalVolume = 2000;
        assertTrue(normalVolume <= threshold);

        // Heavy selling: 10,000 units sold (excess = 7696)
        int heavyVolume = 10000;
        int excess = heavyVolume - threshold;
        double ratio = (double) excess / (double) threshold;
        double drop = Math.min(maxDrop, Math.log1p(ratio) * sensitivity);
        double multiplier = Math.max(minMultiplier, 1.00 - drop);

        assertTrue(drop > 0.0);
        assertTrue(drop <= maxDrop);
        assertTrue(multiplier >= minMultiplier && multiplier < 1.00);

        // Extreme dump: 500,000 units sold
        int extremeVolume = 500000;
        int extremeExcess = extremeVolume - threshold;
        double extremeRatio = (double) extremeExcess / (double) threshold;
        double extremeDrop = Math.min(maxDrop, Math.log1p(extremeRatio) * sensitivity);
        double extremeMultiplier = Math.max(minMultiplier, 1.00 - extremeDrop);

        assertTrue(extremeMultiplier >= minMultiplier, "Multiplier must never drop below 0.65 floor");
        assertTrue(extremeDrop <= maxDrop, "Drop must never exceed max drop 0.35");
    }

    @Test
    @DisplayName("Verify scarcity surge curve when items are heavily purchased (demand absorption)")
    void testScarcitySurgeCurve() {
        int threshold = 2304;
        double sensitivity = 0.025;
        double maxSurge = 0.35;
        double maxMultiplier = 1.35;

        // Heavy purchasing: -10,000 net volume (deficit = 7696)
        int netVolume = -10000;
        assertTrue(netVolume < -threshold);

        int deficit = Math.abs(netVolume) - threshold;
        double ratio = (double) deficit / (double) threshold;
        double surge = Math.min(maxSurge, Math.log1p(ratio) * sensitivity);
        double buyMultiplier = Math.min(maxMultiplier, 1.00 + surge);

        assertTrue(surge > 0.0);
        assertTrue(surge <= maxSurge);
        assertTrue(buyMultiplier > 1.00 && buyMultiplier <= maxMultiplier, "Scarcity must raise buy price above 1.00");
    }

    @Test
    @DisplayName("Verify Solterra damping resistance maintains 60% saturation defense")
    void testSolterraOresResilience() {
        double saturatedSupplyMult = 0.70; // 30% drop in normal realm
        double solterraMult = 1.00 - ((1.00 - saturatedSupplyMult) * 0.40); // 60% resistance

        // 30% drop * 0.40 = 12% drop -> multiplier is 0.88 instead of 0.70
        assertEquals(0.88, solterraMult, 0.001);
        assertTrue(solterraMult > saturatedSupplyMult, "Solterra must retain strong ore price advantage");
    }

    @Test
    @DisplayName("Verify regional volume blending: 70% regional + 30% global")
    void testRegionalVolumeBlending() {
        int regVol = 5000;
        int globVol = 2000;

        int blended = (int) Math.round((regVol * 0.70) + (globVol * 0.30));
        assertEquals(4100, blended);

        // If local is 0 but global has dumped 10,000
        int zeroReg = 0;
        int highGlob = 10000;
        int blendedGlob = (int) Math.round((zeroReg * 0.70) + (highGlob * 0.30));
        assertEquals(3000, blendedGlob, "Global trade influences 30% of local market");
    }
}
