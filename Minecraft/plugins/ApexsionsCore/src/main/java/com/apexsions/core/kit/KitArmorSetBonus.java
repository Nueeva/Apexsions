package com.apexsions.core.kit;

/**
 * Model representing an Armor Set Bonus for Kits or Custom Armor Sets.
 */
public class KitArmorSetBonus {

    private final String setId;
    private final String setName;
    private final KitStatType statType;
    private final double value;
    private final int requiredPieces;

    public KitArmorSetBonus(String setId, String setName, KitStatType statType, double value, int requiredPieces) {
        this.setId = setId;
        this.setName = setName;
        this.statType = statType != null ? statType : KitStatType.DAMAGE_REDUCTION;
        this.value = value;
        this.requiredPieces = requiredPieces > 0 ? requiredPieces : 4;
    }

    public String getSetId() {
        return setId;
    }

    public String getSetName() {
        return setName;
    }

    public KitStatType getStatType() {
        return statType;
    }

    public double getValue() {
        return value;
    }

    public int getRequiredPieces() {
        return requiredPieces;
    }
}
