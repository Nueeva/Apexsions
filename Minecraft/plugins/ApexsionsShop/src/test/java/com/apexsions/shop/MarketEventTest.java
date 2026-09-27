package com.apexsions.shop;

import com.apexsions.shop.category.ShopCategory;
import com.apexsions.shop.dynamic.event.MarketEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MarketEventTest {

    @Test
    @DisplayName("Verify Market Equilibrium has 1.0 multipliers across all categories")
    void testMarketEquilibriumMultipliers() {
        MarketEvent eq = MarketEvent.MARKET_EQUILIBRIUM;
        for (ShopCategory cat : ShopCategory.values()) {
            assertEquals(1.0, eq.getSellMultiplier(cat), 0.001);
            assertEquals(1.0, eq.getBuyMultiplier(cat), 0.001);
        }
    }

    @Test
    @DisplayName("Verify Agrarian Boom boosts farming and food by +25%")
    void testAgrarianBoomMultipliers() {
        MarketEvent boom = MarketEvent.AGRARIAN_BOOM;
        assertEquals(1.25, boom.getSellMultiplier(ShopCategory.FARMING), 0.001);
        assertEquals(1.25, boom.getSellMultiplier(ShopCategory.FOOD), 0.001);
        assertEquals(1.00, boom.getSellMultiplier(ShopCategory.ORES), 0.001);
        assertEquals(1.10, boom.getSellMultiplier(ShopCategory.DYES), 0.001);
    }

    @Test
    @DisplayName("Verify Gold Rush boosts ores by +25%")
    void testGoldRushMultipliers() {
        MarketEvent rush = MarketEvent.GOLD_RUSH;
        assertEquals(1.25, rush.getSellMultiplier(ShopCategory.ORES), 0.001);
        assertEquals(1.15, rush.getSellMultiplier(ShopCategory.BLOCKS), 0.001);
        assertEquals(1.00, rush.getSellMultiplier(ShopCategory.FARMING), 0.001);
    }

    @Test
    @DisplayName("Verify War Mobilization boosts mob drops by +30% and ores by +20%")
    void testWarMobilizationMultipliers() {
        MarketEvent war = MarketEvent.WAR_MOBILIZATION;
        assertEquals(1.30, war.getSellMultiplier(ShopCategory.MOB_DROPS), 0.001);
        assertEquals(1.20, war.getSellMultiplier(ShopCategory.ORES), 0.001);
        assertEquals(1.15, war.getSellMultiplier(ShopCategory.FOOD), 0.001);
    }

    @Test
    @DisplayName("Verify Construction Wave boosts building blocks by +25%")
    void testConstructionWaveMultipliers() {
        MarketEvent wave = MarketEvent.CONSTRUCTION_WAVE;
        assertEquals(1.25, wave.getSellMultiplier(ShopCategory.BLOCKS), 0.001);
        assertEquals(1.10, wave.getSellMultiplier(ShopCategory.ORES), 0.001);
        assertEquals(1.00, wave.getSellMultiplier(ShopCategory.FARMING), 0.001);
    }
}
