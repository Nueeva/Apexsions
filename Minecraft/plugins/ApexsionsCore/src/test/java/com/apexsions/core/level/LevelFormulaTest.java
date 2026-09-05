package com.apexsions.core.level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LevelFormulaTest {

    private LevelFormula multiplierFormula;
    private LevelFormula legacyExponentFormula;

    @BeforeEach
    public void setUp() {
        multiplierFormula = LevelFormula.multiplier(100.0, 1.1, 100);
        legacyExponentFormula = new LevelFormula(100.0, 1.5, 100);
    }

    @Test
    public void testMultiplierLevel1Progression() {
        // Level 1: 100 * (1.1 ^ 0) = 100
        long xp = multiplierFormula.getRequiredXpForNextLevel(1);
        assertEquals(100L, xp);
    }

    @Test
    public void testMultiplierLevel2Progression() {
        // Level 2: 100 * (1.1 ^ 1) = 110
        long xp = multiplierFormula.getRequiredXpForNextLevel(2);
        assertEquals(110L, xp);
    }

    @Test
    public void testMultiplierLevel3Progression() {
        // Level 3: 100 * (1.1 ^ 2) = 121
        long xp = multiplierFormula.getRequiredXpForNextLevel(3);
        assertEquals(121L, xp);
    }

    @Test
    public void testMultiplierMaxLevelProgression() {
        long xp = multiplierFormula.getRequiredXpForNextLevel(100);
        assertEquals(Long.MAX_VALUE, xp);
    }

    @Test
    public void testLegacyExponentProgression() {
        long xp = legacyExponentFormula.getRequiredXpForNextLevel(1);
        assertEquals(100L, xp);
    }

    @Test
    public void testCumulativeProgression() {
        long totalForLvl3 = multiplierFormula.getTotalXpForLevel(3);
        long lvl1 = multiplierFormula.getRequiredXpForNextLevel(1);
        long lvl2 = multiplierFormula.getRequiredXpForNextLevel(2);
        assertEquals(lvl1 + lvl2, totalForLvl3);
    }
}
