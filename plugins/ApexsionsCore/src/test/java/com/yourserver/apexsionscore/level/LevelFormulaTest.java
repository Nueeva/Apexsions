package com.yourserver.apexsionscore.level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LevelFormulaTest {

    private LevelFormula formula;

    @BeforeEach
    public void setUp() {
        formula = new LevelFormula(100.0, 1.5, 100);
    }

    @Test
    public void testLevel1Progression() {
        long xp = formula.getRequiredXpForNextLevel(1);
        assertEquals(100L, xp);
    }

    @Test
    public void testLevelMaxProgression() {
        long xp = formula.getRequiredXpForNextLevel(100);
        assertEquals(Long.MAX_VALUE, xp);
    }

    @Test
    public void testCumulativeProgression() {
        long totalForLvl3 = formula.getTotalXpForLevel(3);
        long lvl1 = formula.getRequiredXpForNextLevel(1);
        long lvl2 = formula.getRequiredXpForNextLevel(2);
        assertEquals(lvl1 + lvl2, totalForLvl3);
    }
}
