package com.apexsions.shop;

import com.apexsions.shop.contract.TradeContract;
import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TradeContractTest {

    @Test
    @DisplayName("Verify TradeContract attributes and getter values")
    void testTradeContractModel() {
        TradeContract contract = new TradeContract(
                "solterra_copper",
                "SOLTERRA",
                "Solterra",
                "#e74c3c",
                "Baja Benteng Gurun",
                "Kebutuhan perbaikan baju zirah",
                Material.COPPER_INGOT,
                64,
                4500.0,
                250,
                Material.COPPER_INGOT
        );

        assertEquals("solterra_copper", contract.getId());
        assertEquals("SOLTERRA", contract.getKingdomKey());
        assertEquals("Solterra", contract.getKingdomDisplayName());
        assertEquals("#e74c3c", contract.getKingdomColor());
        assertEquals("Baja Benteng Gurun", contract.getTitle());
        assertEquals(Material.COPPER_INGOT, contract.getRequiredMaterial());
        assertEquals(64, contract.getRequiredAmount());
        assertEquals(4500.0, contract.getRewardRupiah(), 0.001);
        assertEquals(250, contract.getRewardKingdomXp());
        assertEquals(Material.COPPER_INGOT, contract.getIcon());
    }
}
