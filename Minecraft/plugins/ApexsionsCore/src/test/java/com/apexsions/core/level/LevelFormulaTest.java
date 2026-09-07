package com.apexsions.core.level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LevelFormulaTest {

    private LevelFormula quadraticFormula;
    private LevelFormula defaultFormula;
    private LevelFormula multiplierFormula;
    private LevelFormula legacyExponentFormula;

    @BeforeEach
    public void setUp() {
        quadraticFormula = LevelFormula.quadratic(510L, -10L, 100);
        defaultFormula = LevelFormula.defaultFormula(100);
        multiplierFormula = LevelFormula.multiplier(100.0, 1.1, 100);
        legacyExponentFormula = new LevelFormula(100.0, 1.5, 100);
    }

    @Test
    public void testQuadraticLevel1Progression() {
        // Level 1: (510 * 1^2) - (10 * 1) = 510 - 10 = 500
        long xp = quadraticFormula.getRequiredXpForNextLevel(1);
        assertEquals(500L, xp);
        assertEquals(500L, defaultFormula.getRequiredXpForNextLevel(1));
    }

    @Test
    public void testQuadraticLevel2Progression() {
        // Level 2: (510 * 2^2) - (10 * 2) = 2040 - 20 = 2020
        long xp = quadraticFormula.getRequiredXpForNextLevel(2);
        assertEquals(2020L, xp);
        assertEquals(2020L, defaultFormula.getRequiredXpForNextLevel(2));
    }

    @Test
    public void testQuadraticLevel3Progression() {
        // Level 3: (510 * 3^2) - (10 * 3) = 4590 - 30 = 4560
        long xp = quadraticFormula.getRequiredXpForNextLevel(3);
        assertEquals(4560L, xp);
    }

    @Test
    public void testQuadraticLevel4Progression() {
        // Level 4: (510 * 4^2) - (10 * 4) = 8160 - 40 = 8120
        long xp = quadraticFormula.getRequiredXpForNextLevel(4);
        assertEquals(8120L, xp);
    }

    @Test
    public void testQuadraticLevel10Progression() {
        // Level 10: (510 * 10^2) - (10 * 10) = 51000 - 100 = 50900
        long xp = quadraticFormula.getRequiredXpForNextLevel(10);
        assertEquals(50900L, xp);
    }

    @Test
    public void testQuadraticLevelBelow1Clamping() {
        // Level 0 or negative should clamp to Level 1
        long xp = quadraticFormula.getRequiredXpForNextLevel(0);
        assertEquals(500L, xp);
    }

    @Test
    public void testQuadraticMaxLevelProgression() {
        long xp = quadraticFormula.getRequiredXpForNextLevel(100);
        assertEquals(Long.MAX_VALUE, xp);
    }

    @Test
    public void testQuadraticCumulativeProgression() {
        // Total for Level 3 = Level 1 (500) + Level 2 (2020) = 2520
        long totalForLvl3 = quadraticFormula.getTotalXpForLevel(3);
        long lvl1 = quadraticFormula.getRequiredXpForNextLevel(1);
        long lvl2 = quadraticFormula.getRequiredXpForNextLevel(2);
        assertEquals(2520L, totalForLvl3);
        assertEquals(lvl1 + lvl2, totalForLvl3);
    }

    @Test
    public void testMultiplierLevel1Progression() {
        // Legacy Level 1: 100 * (1.1 ^ 0) = 100
        long xp = multiplierFormula.getRequiredXpForNextLevel(1);
        assertEquals(100L, xp);
    }

    @Test
    public void testLegacyExponentProgression() {
        long xp = legacyExponentFormula.getRequiredXpForNextLevel(1);
        assertEquals(100L, xp);
    }
}
