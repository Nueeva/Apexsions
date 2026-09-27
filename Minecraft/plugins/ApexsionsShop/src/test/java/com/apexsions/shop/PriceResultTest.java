package com.apexsions.shop;

import com.apexsions.shop.dynamic.DynamicPriceCalculator.PriceResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PriceResultTest {

    @Test
    @DisplayName("Verify backward compatibility of 10-parameter PriceResult constructor")
    void testPriceResultBackwardCompatibility() {
        PriceResult res = new PriceResult(
                100.0, // base
                1.10,  // weather
                1.05,  // kingdom
                0.95,  // supply
                104.2, // effective
                64,    // qty
                6668.8,// rawTotal
                10.0,  // taxPercent
                666.88,// taxAmount
                7335.68// finalTotal
        );

        assertEquals(1.0, res.eventMultiplier(), 0.001);
        assertEquals(100.0, res.baseUnitPrice(), 0.001);
        assertEquals(1.10, res.weatherMultiplier(), 0.001);
        assertEquals(64, res.quantity());
    }

    @Test
    @DisplayName("Verify 11-parameter PriceResult constructor with explicit event multiplier")
    void testPriceResultWithEventMultiplier() {
        PriceResult res = new PriceResult(
                100.0, // base
                1.00,  // weather
                1.00,  // kingdom
                1.00,  // supply
                1.25,  // event (+25% Gold Rush)
                125.0, // effective
                10,    // qty
                1250.0,// rawTotal
                15.0,  // taxPercent
                187.5, // taxAmount
                1437.5 // finalTotal
        );

        assertEquals(1.25, res.eventMultiplier(), 0.001);
        assertEquals(125.0, res.effectiveUnitPrice(), 0.001);
        assertEquals(10, res.quantity());
    }
}
