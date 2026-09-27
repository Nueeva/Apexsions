package com.apexsions.shop.contract;

import org.bukkit.Material;

/**
 * Represents a Royal Trade Order / Quota issued by the Kingdoms of Apexsions.
 */
public class TradeContract {

    private final String id;
    private final String kingdomKey; // "ZENITHAR", "SOLTERRA", "SYLVAMOOR"
    private final String kingdomDisplayName;
    private final String kingdomColor;
    private final String title;
    private final String lore;
    private final Material requiredMaterial;
    private final int requiredAmount;
    private final double rewardRupiah;
    private final int rewardKingdomXp;
    private final Material icon;

    public TradeContract(String id, String kingdomKey, String kingdomDisplayName, String kingdomColor,
                         String title, String lore, Material requiredMaterial, int requiredAmount,
                         double rewardRupiah, int rewardKingdomXp, Material icon) {
        this.id = id;
        this.kingdomKey = kingdomKey;
        this.kingdomDisplayName = kingdomDisplayName;
        this.kingdomColor = kingdomColor;
        this.title = title;
        this.lore = lore;
        this.requiredMaterial = requiredMaterial;
        this.requiredAmount = requiredAmount;
        this.rewardRupiah = rewardRupiah;
        this.rewardKingdomXp = rewardKingdomXp;
        this.icon = icon;
    }

    public String getId() { return id; }
    public String getKingdomKey() { return kingdomKey; }
    public String getKingdomDisplayName() { return kingdomDisplayName; }
    public String getKingdomColor() { return kingdomColor; }
    public String getTitle() { return title; }
    public String getLore() { return lore; }
    public Material getRequiredMaterial() { return requiredMaterial; }
    public int getRequiredAmount() { return requiredAmount; }
    public double getRewardRupiah() { return rewardRupiah; }
    public int getRewardKingdomXp() { return rewardKingdomXp; }
    public Material getIcon() { return icon; }
}
