package com.apexsions.economy.auction;

import com.apexsions.economy.util.ItemSerializer;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class AuctionListing {

    private final String id;
    private final UUID sellerUuid;
    private final String sellerName;
    private final String currencyId;
    private double price;
    private final String itemData; // Base64 serialized ItemStack
    private final long createdAt;
    private final long expiresAt;
    private AuctionStatus status;
    private UUID buyerUuid;

    public AuctionListing(String id, UUID sellerUuid, String sellerName, String currencyId, double price, String itemData, long createdAt, long expiresAt, AuctionStatus status, UUID buyerUuid) {
        this.id = id;
        this.sellerUuid = sellerUuid;
        this.sellerName = sellerName != null ? sellerName : "Seller";
        this.currencyId = currencyId != null ? currencyId : "rupiah";
        this.price = price;
        this.itemData = itemData;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = status != null ? status : AuctionStatus.ACTIVE;
        this.buyerUuid = buyerUuid;
    }

    public ItemStack getItemStack() {
        if (itemData != null && !itemData.isBlank()) {
            return ItemSerializer.fromBase64(itemData);
        }
        return null;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiresAt;
    }

    public String getTimeRemainingFormatted() {
        if (isExpired()) return "Kedaluwarsa";
        long diff = expiresAt - System.currentTimeMillis();
        long hours = diff / (1000 * 60 * 60);
        long minutes = (diff / (1000 * 60)) % 60;
        return hours + " jam " + minutes + " mnt";
    }

    public String getId() { return id; }
    public UUID getSellerUuid() { return sellerUuid; }
    public String getSellerName() { return sellerName; }
    public String getCurrencyId() { return currencyId; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getItemData() { return itemData; }
    public long getCreatedAt() { return createdAt; }
    public long getExpiresAt() { return expiresAt; }
    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }
    public UUID getBuyerUuid() { return buyerUuid; }
    public void setBuyerUuid(UUID buyerUuid) { this.buyerUuid = buyerUuid; }
}
