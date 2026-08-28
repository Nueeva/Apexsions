package com.apex.economy.service;

import com.apex.battlepass.util.ItemSerializer;
import com.apex.economy.ApexsionsEconomy;
import com.apex.economy.auction.AuctionListing;
import com.apex.economy.auction.AuctionStatus;
import com.apex.economy.currency.Currency;
import com.apex.economy.util.NumberFormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionService {

    private final ApexsionsEconomy plugin;
    private final Map<String, AuctionListing> activeAuctions = new ConcurrentHashMap<>();
    private final Map<UUID, List<AuctionListing>> playerAuctionsCache = new ConcurrentHashMap<>();

    public AuctionService(ApexsionsEconomy plugin) {
        this.plugin = plugin;
        loadActiveAuctions();
        startExpirationTask();
    }

    public void loadActiveAuctions() {
        try {
            List<AuctionListing> list = plugin.getRepository().loadActiveAuctions().get();
            activeAuctions.clear();
            for (AuctionListing al : list) {
                if (al.isExpired()) {
                    al.setStatus(AuctionStatus.EXPIRED);
                    plugin.getRepository().saveAuction(al);
                } else {
                    activeAuctions.put(al.getId(), al);
                }
                playerAuctionsCache.computeIfAbsent(al.getSellerUuid(), k -> Collections.synchronizedList(new ArrayList<>())).add(al);
            }
        } catch (Exception ignored) {}
    }

    private void startExpirationTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (AuctionListing al : activeAuctions.values()) {
                if (al.getStatus() == AuctionStatus.ACTIVE && al.isExpired()) {
                    al.setStatus(AuctionStatus.EXPIRED);
                    activeAuctions.remove(al.getId());
                    plugin.getRepository().saveAuction(al);
                }
            }
        }, 600L, 600L); // every 30s
    }

    public synchronized boolean createAuction(Player seller, ItemStack item, Currency currency, double price, int durationHours) {
        if (seller == null || item == null || currency == null || price <= 0) return false;

        // Take item safely from seller
        seller.getInventory().removeItem(item);

        String id = UUID.randomUUID().toString().substring(0, 8);
        String itemData = ItemSerializer.toBase64(item);
        long now = System.currentTimeMillis();
        long expiresAt = now + ((long) durationHours * 60 * 60 * 1000);

        AuctionListing listing = new AuctionListing(id, seller.getUniqueId(), seller.getName(), currency.getId(), price, itemData, now, expiresAt, AuctionStatus.ACTIVE, null);
        activeAuctions.put(id, listing);
        playerAuctionsCache.computeIfAbsent(seller.getUniqueId(), k -> Collections.synchronizedList(new ArrayList<>())).add(0, listing);
        plugin.getRepository().saveAuction(listing);

        String itemName = ItemSerializer.getItemDisplayName(item);
        seller.sendMessage("§a[✔] Berhasil mendaftarkan §e" + itemName + " §ake Auction House seharga §e" + NumberFormatUtil.format(price, currency) + "§a!");
        return true;
    }

    public synchronized boolean buyAuction(Player buyer, String auctionId) {
        if (buyer == null || auctionId == null) return false;

        AuctionListing listing = activeAuctions.get(auctionId);
        if (listing == null || listing.getStatus() != AuctionStatus.ACTIVE || listing.isExpired()) {
            buyer.sendMessage("§cBarang lelang ini sudah tidak tersedia atau telah kedaluwarsa!");
            return false;
        }

        if (buyer.getUniqueId().equals(listing.getSellerUuid())) {
            buyer.sendMessage("§cAnda tidak dapat membeli barang lelang milik Anda sendiri!");
            return false;
        }

        Currency currency = plugin.getCurrencyRegistry().get(listing.getCurrencyId());
        if (currency == null) {
            buyer.sendMessage("§cMata uang untuk transaksi ini tidak dikenali.");
            return false;
        }

        CurrencyService cs = plugin.getCurrencyService();
        if (!cs.has(buyer.getUniqueId(), currency.getId(), listing.getPrice())) {
            buyer.sendMessage("§cSaldo " + currency.getDisplayName() + " Anda tidak mencukupi untuk membeli barang ini!");
            return false;
        }

        // 1. Withdraw money from buyer
        cs.removeBalance(buyer.getUniqueId(), currency.getId(), listing.getPrice());

        // 2. Deposit money to seller
        cs.addBalance(listing.getSellerUuid(), currency.getId(), listing.getPrice());

        // 3. Mark auction as sold
        listing.setStatus(AuctionStatus.SOLD);
        listing.setBuyerUuid(buyer.getUniqueId());
        activeAuctions.remove(auctionId);
        plugin.getRepository().saveAuction(listing);

        // 4. Give item to buyer
        ItemStack item = listing.getItemStack();
        if (item != null) {
            HashMap<Integer, ItemStack> overflow = buyer.getInventory().addItem(item);
            if (!overflow.isEmpty()) {
                for (ItemStack drop : overflow.values()) {
                    buyer.getWorld().dropItemNaturally(buyer.getLocation(), drop);
                }
            }
        }

        String itemName = item != null ? ItemSerializer.getItemDisplayName(item) : "Item";
        buyer.sendMessage("§a[✔] Selamat! Anda berhasil membeli §e" + itemName + " §aseharga §e" + NumberFormatUtil.format(listing.getPrice(), currency) + "§a!");

        // Notify seller if online
        Player seller = Bukkit.getPlayer(listing.getSellerUuid());
        if (seller != null && seller.isOnline()) {
            seller.sendMessage("§a[✔] Barang lelang Anda (§e" + itemName + "§a) telah dibeli oleh §e" + buyer.getName() + " §aseharga §e" + NumberFormatUtil.format(listing.getPrice(), currency) + "§a!");
        }

        return true;
    }

    public synchronized boolean cancelAuction(Player seller, String auctionId) {
        if (seller == null || auctionId == null) return false;

        AuctionListing listing = activeAuctions.get(auctionId);
        if (listing == null || listing.getStatus() != AuctionStatus.ACTIVE) {
            seller.sendMessage("§cLelang tidak ditemukan atau sudah tidak aktif!");
            return false;
        }

        if (!seller.getUniqueId().equals(listing.getSellerUuid())) {
            seller.sendMessage("§cAnda bukan pemilik barang lelang ini!");
            return false;
        }

        listing.setStatus(AuctionStatus.CANCELLED);
        activeAuctions.remove(auctionId);
        plugin.getRepository().saveAuction(listing);

        // Return item
        ItemStack item = listing.getItemStack();
        if (item != null) {
            HashMap<Integer, ItemStack> overflow = seller.getInventory().addItem(item);
            if (!overflow.isEmpty()) {
                for (ItemStack drop : overflow.values()) {
                    seller.getWorld().dropItemNaturally(seller.getLocation(), drop);
                }
            }
        }

        seller.sendMessage("§a[✔] Lelang berhasil dibatalkan dan barang telah dikembalikan ke inventory Anda.");
        return true;
    }

    public synchronized boolean updateAuctionPrice(Player seller, String auctionId, double newPrice) {
        if (seller == null || auctionId == null || newPrice <= 0 || Double.isNaN(newPrice) || Double.isInfinite(newPrice)) return false;

        AuctionListing listing = activeAuctions.get(auctionId);
        if (listing == null || listing.getStatus() != AuctionStatus.ACTIVE) {
            seller.sendMessage("§cLelang tidak ditemukan atau sudah tidak aktif!");
            return false;
        }

        if (!seller.getUniqueId().equals(listing.getSellerUuid())) {
            seller.sendMessage("§cAnda bukan pemilik barang lelang ini!");
            return false;
        }

        listing.setPrice(newPrice);
        plugin.getRepository().saveAuction(listing);

        Currency curr = plugin.getCurrencyRegistry().get(listing.getCurrencyId());
        seller.sendMessage("§a[✔] Berhasil mengubah harga lelang menjadi §e" + NumberFormatUtil.format(newPrice, curr) + "§a!");
        return true;
    }

    public synchronized boolean claimExpiredAuction(Player seller, AuctionListing listing) {
        if (seller == null || listing == null) return false;
        if (!seller.getUniqueId().equals(listing.getSellerUuid())) return false;
        if (listing.getStatus() != AuctionStatus.EXPIRED) return false;

        listing.setStatus(AuctionStatus.CANCELLED);
        plugin.getRepository().saveAuction(listing);

        ItemStack item = listing.getItemStack();
        if (item != null) {
            HashMap<Integer, ItemStack> overflow = seller.getInventory().addItem(item);
            if (!overflow.isEmpty()) {
                for (ItemStack drop : overflow.values()) {
                    seller.getWorld().dropItemNaturally(seller.getLocation(), drop);
                }
            }
        }

        seller.sendMessage("§a[✔] Barang lelang kedaluwarsa berhasil diklaim kembali!");
        return true;
    }

    public Collection<AuctionListing> getActiveAuctions() {
        return Collections.unmodifiableCollection(activeAuctions.values());
    }

    public List<AuctionListing> getPlayerAuctionsCached(UUID sellerUuid) {
        List<AuctionListing> list = playerAuctionsCache.get(sellerUuid);
        if (list != null) {
            return new ArrayList<>(list);
        }
        try {
            List<AuctionListing> dbList = plugin.getRepository().loadPlayerAuctions(sellerUuid).get();
            playerAuctionsCache.put(sellerUuid, Collections.synchronizedList(new ArrayList<>(dbList)));
            return dbList;
        } catch (Exception e) {
            return List.of();
        }
    }

    public CompletableFuture<List<AuctionListing>> getPlayerAuctions(UUID sellerUuid) {
        return plugin.getRepository().loadPlayerAuctions(sellerUuid);
    }
}
