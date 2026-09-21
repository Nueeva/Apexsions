package com.apexsions.core.claim.gui;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.claim.ClaimChunk;
import com.apexsions.core.claim.ClaimManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * Aesthetic administrative and player management GUI for sovereign land claims,
 * taxes, deposit vaults, and flag toggles.
 */
public class ClaimGUI implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final ClaimManager claimManager;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ClaimGUI(ApexsionsCorePlugin plugin, ClaimManager claimManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
    }

    public static class MainHolder implements org.bukkit.inventory.InventoryHolder {
        @Override public Inventory getInventory() { return null; }
    }

    public static class ListHolder implements org.bukkit.inventory.InventoryHolder {
        private final int page;
        public ListHolder(int page) { this.page = page; }
        public int getPage() { return page; }
        @Override public Inventory getInventory() { return null; }
    }

    public static class ActionHolder implements org.bukkit.inventory.InventoryHolder {
        private final ClaimChunk claim;
        public ActionHolder(ClaimChunk claim) { this.claim = claim; }
        public ClaimChunk getClaim() { return claim; }
        @Override public Inventory getInventory() { return null; }
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(new MainHolder(), 45, mm.deserialize("<gradient:#d4af37:#f39c12><bold>⚑ KEDAULATAN TANAH</bold></gradient>"));

        // Fill background border with tinted glass
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, border);
            }
        }

        UUID playerId = player.getUniqueId();
        int owned = claimManager.getClaimCount(playerId);
        int max = claimManager.getMaxClaims(player);
        Chunk currentChunk = player.getLocation().getChunk();
        Optional<ClaimChunk> chunkClaim = claimManager.getClaimAt(player.getLocation());

        // Slot 4: Player Profile & Quota Info
        ItemStack profile = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) profile.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            skullMeta.displayName(mm.deserialize("<gold><bold>" + player.getName() + "</bold></gold> <gray>• Status Teritori</gray>"));
            List<Component> lore = new ArrayList<>();
            int levelBonus = claimManager.getLevelBonusClaims(player);
            String maxLabel = max == Integer.MAX_VALUE ? "∞ (Tak Terbatas)" : String.valueOf(max);
            lore.add(mm.deserialize("<gray>Total Kepemilikan: </gray><gold>" + owned + "</gold><gray>/</gray><yellow>" + maxLabel + "</yellow> <gray>chunks</gray>"));
            if (levelBonus > 0 && max != Integer.MAX_VALUE) {
                lore.add(mm.deserialize("<gray>Bonus Level Progression: </gray><green>+" + levelBonus + " Chunks (Leveling)</green>"));
            }
            if (max == Integer.MAX_VALUE) {
                lore.add(mm.deserialize("<gray>Penggunaan Kuota: </gray><aqua>Tanpa Batas (Upper Dimension)</aqua>"));
            } else {
                double pct = max > 0 ? (double) owned / max * 100.0 : 0;
                lore.add(mm.deserialize("<gray>Penggunaan Kuota: </gray><aqua>" + String.format("%.1f", pct) + "%</aqua>"));
            }
            lore.add(mm.deserialize(""));
            lore.add(mm.deserialize("<dark_gray>Chunk saat ini: [" + currentChunk.getX() + ", " + currentChunk.getZ() + "]</dark_gray>"));
            if (chunkClaim.isPresent()) {
                ClaimChunk c = chunkClaim.get();
                if (c.isOwner(playerId)) {
                    lore.add(mm.deserialize("<green>✔ Anda pemilik chunk ini.</green>"));
                } else {
                    lore.add(mm.deserialize("<yellow>⚑ Dimiliki oleh: </yellow><gold>" + c.getOwnerName() + "</gold>"));
                }
            } else {
                lore.add(mm.deserialize("<gray>Status Chunk: </gray><white>Belum Diklaim</white>"));
            }
            skullMeta.lore(lore);
            profile.setItemMeta(skullMeta);
        }
        inv.setItem(4, profile);

        // Slot 19: Claim or Unclaim Action (Dedicated to current location)
        if (chunkClaim.isEmpty()) {
            double rate = claimManager.calculateChunkDailyTax(playerId);
            boolean exempt = claimManager.isPurchaseExempt(playerId);
            double purchaseCost = claimManager.getPurchaseCostPerChunk();
            String purchaseCostStr = (!claimManager.isPurchaseEnabled() || purchaseCost <= 0)
                    ? "<aqua>Gratis</aqua>"
                    : (exempt ? "<aqua>Gratis (Upper Dimension)</aqua>" : "<gold>Rp" + String.format("%,.0f", purchaseCost) + "</gold> <dark_gray>(➔ Kas Kerajaan)</dark_gray>");

            inv.setItem(19, createItem(Material.GOLDEN_HOE,
                    "<green><bold>Klaim Chunk Saat Ini</bold></green>",
                    "<gray>Klaim petak 16x16 blok di koordinat ini:</gray>",
                    "<yellow>[" + currentChunk.getX() + ", " + currentChunk.getZ() + "]</yellow> <gray>(" + currentChunk.getWorld().getName() + ")</gray>",
                    "",
                    "<gray>Biaya Pembelian: </gray>" + purchaseCostStr,
                    "<gray>Pajak Upkeep: </gray><gold>Rp" + String.format("%,.0f", rate) + "/hari</gold>",
                    "",
                    "<gold>» Sentuh / Klik untuk Mengklaim «</gold>"));
        } else if (chunkClaim.get().isOwner(playerId)) {
            inv.setItem(19, createItem(Material.BARRIER,
                    "<red><bold>Lepas Klaim Chunk Ini (/unclaim)</bold></red>",
                    "<gray>Lepas klaim petak yang Anda injak saat ini:</gray>",
                    "<yellow>[" + currentChunk.getX() + ", " + currentChunk.getZ() + "]</yellow>",
                    "",
                    "<red>» Sentuh / Klik untuk Melepas «</red>"));
        } else {
            inv.setItem(19, createItem(Material.IRON_BARS,
                    "<gray><bold>Chunk Tidak Tersedia</bold></gray>",
                    "<red>Sudah dimiliki oleh: " + chunkClaim.get().getOwnerName() + "</red>"));
        }

        // Slot 21: Bank & Tax Status
        double totalVaulted = 0.0;
        List<ClaimChunk> playerClaims = claimManager.getClaimsByOwner(playerId);
        boolean inGrace = false;
        for (ClaimChunk c : playerClaims) {
            totalVaulted += c.getBankBalance();
            if (c.isInGracePeriod()) inGrace = true;
        }
        double dailyTax = claimManager.calculateTotalDailyTax(playerId);
        double daysLeft = dailyTax > 0 ? (totalVaulted / dailyTax) : 999.0;

        inv.setItem(21, createItem(Material.VAULT,
                "<gradient:#ffd700:#ffa500><bold>Brankas Pajak Wilayah</bold></gradient>",
                "<gray>Saldo Brankas: </gray><green><b>Rp" + String.format("%,.0f", totalVaulted) + "</b></green>",
                "<gray>Pajak Harian: </gray><red>Rp" + String.format("%,.0f", dailyTax) + "/hari</red>",
                "<gray>Estimasi Aktif: </gray><aqua>" + String.format("%.1f", daysLeft) + " hari</aqua>",
                "<gray>Status: </gray>" + (inGrace ? "<red><b>MENUNGGAK PAJAK (72 JAM)</b></red>" : "<green>Lunas & Aman</green>"),
                "<dark_gray>💡 Auto-Debet: Otomatis potong dari /bal jika brankas Rp0</dark_gray>",
                "",
                "<yellow>» Klik untuk Setor Rp1.000 «</yellow>"));

        // Slot 23: Quick Deposit 10,000
        inv.setItem(23, createItem(Material.GOLD_BLOCK,
                "<gradient:#f12711:#f5af19><bold>Setoran Cepat Rp10.000</bold></gradient>",
                "<gray>Suntik saldo Rp10.000 dari dompet</gray>",
                "<gray>ke seluruh petak klaim Anda secara merata.</gray>",
                "",
                "<gold>» Klik untuk Setor Rp10.000 «</gold>"));

        // Slot 25: Sentral Wilayah: Navigasi & Kelola (Dedicated List & Management Button)
        inv.setItem(25, createItem(Material.COMPASS,
                "<gradient:#00c6ff:#0072ff><bold>Sentral Wilayah: Navigasi & Kelola</bold></gradient>",
                "<gray>Buka daftar seluruh petak tanah milik Anda.</gray>",
                "<gray>• Teleportasi pulang ke tanah klaim</gray>",
                "<gray>• Lepas klaim dari jarak jauh (Remote Unclaim)</gray>",
                "<gray>• Cek status brankas & koordinat petak</gray>",
                "",
                "<dark_gray>Total: " + owned + " petak terdaftar</dark_gray>",
                "<aqua>» Sentuh / Klik untuk Buka Daftar «</aqua>"));

        // Slot 29: Flags & Policy Toggle (if standing on own claim)
        if (chunkClaim.isPresent() && (chunkClaim.get().isOwner(playerId) || player.isOp())) {
            ClaimChunk c = chunkClaim.get();
            boolean pvp = c.getBooleanFlag("pvp", false);
            inv.setItem(29, createItem(Material.COMPARATOR,
                    "<gradient:#ff5e62:#ff9966><bold>Pengaturan Flag Wilayah</bold></gradient>",
                    "<gray>PvP: </gray>" + (pvp ? "<green>Aktif</green>" : "<red>Mati</red>"),
                    "",
                    "<aqua>» Sentuh / Klik untuk Toggle PvP «</aqua>"));
        }

        // Slot 31: Visualize Chunk Boundary (/claim border)
        inv.setItem(31, createItem(Material.GLOWSTONE_DUST,
                "<yellow><bold>Nyalakan Batas Wilayah (/claim border)</bold></yellow>",
                "<gray>Memancarkan 4 tiang suar sudut dan dinding</gray>",
                "<gray>energi bercahaya di sepanjang batas 16x16 ini.</gray>",
                "",
                "<gold>» Sentuh / Klik untuk Memancarkan Batas «</gold>"));

        // Slot 33: Pulang ke Wilayah (/claim home)
        inv.setItem(33, createItem(Material.ENDER_PEARL,
                "<gradient:#a8ff78:#78ffd6><bold>Pulang ke Wilayah (/claim home)</bold></gradient>",
                "<gray>Teleportasi instan pulang ke tanah</gray>",
                "<gray>klaim pertama Anda dengan selamat.</gray>",
                "",
                "<green>» Sentuh / Klik untuk Teleport «</green>"));

        // Slot 40: Close
        inv.setItem(40, createItem(Material.ARROW, "<red><bold>Tutup Menu</bold></red>"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
    }

    /**
     * Centralized interactive territory list for Bedrock & Java.
     */
    public void openTerritoryList(Player player, int page) {
        List<ClaimChunk> claims = claimManager.getClaimsByOwner(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(new ListHolder(page), 54, mm.deserialize("<gradient:#d4af37:#f39c12><bold>⚑ DAFTAR WILAYAH ANDA</bold></gradient>"));

        // Border
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, border);
            }
        }

        // Header slot 4: Summary
        inv.setItem(4, createItem(Material.BOOK,
                "<gold><bold>Sentralisasi Wilayah</bold></gold>",
                "<gray>Total: </gray><yellow>" + claims.size() + " petak tanah</yellow>",
                "",
                "<aqua>Sentuh / klik petak tanah di bawah</aqua>",
                "<aqua>untuk Teleportasi atau Lepas Klaim.</aqua>"));

        int perPage = 21;
        int startIndex = page * perPage;
        int endIndex = Math.min(startIndex + perPage, claims.size());

        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        for (int i = startIndex; i < endIndex; i++) {
            ClaimChunk c = claims.get(i);
            int slot = slots[i - startIndex];
            boolean grace = c.isInGracePeriod();
            Material mat = grace ? Material.REDSTONE_BLOCK : Material.GRASS_BLOCK;
            int blockX = (c.getChunkX() << 4) + 8;
            int blockZ = (c.getChunkZ() << 4) + 8;

            String outpostTag = c.isOutpost() ? " <gradient:#ffd700:#ff8c00><b>[OUTPOST]</b></gradient>" : "";
            String label = (c.getName() != null && !c.getName().isBlank())
                    ? "<gold><bold>\"" + c.getName() + "\"</bold></gold>" + outpostTag + " <yellow>[" + c.getChunkX() + ", " + c.getChunkZ() + "]</yellow>"
                    : "<gold><bold>Petak #" + (i + 1) + "</bold></gold>" + outpostTag + " <yellow>[" + c.getChunkX() + ", " + c.getChunkZ() + "]</yellow>";

            inv.setItem(slot, createItem(mat,
                    label,
                    "<gray>Dunia: </gray><aqua>" + c.getWorld() + "</aqua>",
                    "<gray>Koordinat: </gray><white>~ X: " + blockX + ", Z: " + blockZ + "</white>",
                    "<gray>Status: </gray>" + (grace ? "<red><b>MENUNGGAK PAJAK</b></red>" : "<green>Lunas & Aktif</green>"),
                    "<gray>Saldo Brankas: </gray><green>Rp" + String.format("%,.0f", c.getBankBalance()) + "</green>",
                    "",
                    "<yellow>» Sentuh / Klik untuk Kelola Petak Ini «</yellow>"));
        }

        if (claims.isEmpty()) {
            inv.setItem(22, createItem(Material.BARRIER,
                    "<red><bold>Belum Ada Wilayah</bold></red>",
                    "<gray>Anda belum mengklaim tanah apapun.</gray>",
                    "<gray>Klaim chunk tempat Anda berdiri dengan </gray><yellow>/claim</yellow>."));
        }

        // Navigation controls
        if (page > 0) {
            inv.setItem(45, createItem(Material.ARROW, "<yellow>« Halaman Sebelumnya</yellow>"));
        }
        inv.setItem(49, createItem(Material.OAK_DOOR, "<red><bold>Kembali ke Menu Utama</bold></red>"));
        if (endIndex < claims.size()) {
            inv.setItem(53, createItem(Material.ARROW, "<yellow>Halaman Berikutnya »</yellow>"));
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
    }

    /**
     * Bedrock touch-friendly Action Menu for a single claimed territory.
     */
    public void openTerritoryAction(Player player, ClaimChunk claim) {
        Inventory inv = Bukkit.createInventory(new ActionHolder(claim), 27, mm.deserialize("<gradient:#d4af37:#f39c12><bold>⚑ KELOLA PETAK [" + claim.getChunkX() + ", " + claim.getChunkZ() + "]</bold></gradient>"));

        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, border);
        }

        int blockX = (claim.getChunkX() << 4) + 8;
        int blockZ = (claim.getChunkZ() << 4) + 8;
        String nameLabel = (claim.getName() != null && !claim.getName().isBlank()) ? claim.getName() : "<italic>Belum Dinamai</italic>";
        String outpostStatus = claim.isOutpost() ? "<gold><b>[POS DEPAN / OUTPOST]</b> (Diskon 50%)</gold>" : "<white>Markas Pribadi (Standar)</white>";

        // Slot 4: Info Card
        inv.setItem(4, createItem(Material.FILLED_MAP,
                "<gold><bold>Petak Wilayah [" + claim.getChunkX() + ", " + claim.getChunkZ() + "]</bold></gold>",
                "<gray>Nama Label: </gray><yellow>" + nameLabel + "</yellow>",
                "<gray>Tipe Teritori: </gray>" + outpostStatus,
                "<gray>Dunia: </gray><aqua>" + claim.getWorld() + "</aqua>",
                "<gray>Koordinat Blok: </gray><white>~ X: " + blockX + ", Z: " + blockZ + "</white>",
                "<gray>Status: </gray>" + (claim.isInGracePeriod() ? "<red>Menunggak Pajak</red>" : "<green>Lunas & Aktif</green>"),
                "<gray>Saldo Brankas: </gray><green>Rp" + String.format("%,.0f", claim.getBankBalance()) + "</green>"));

        // Slot 11: Teleportation Button (Single tap for Bedrock)
        inv.setItem(11, createItem(Material.ENDER_PEARL,
                "<green><bold>Teleportasi ke Wilayah Ini</bold></green>",
                "<gray>Teleportasi instan dan aman ke tengah</gray>",
                "<gray>petak tanah ini untuk memindahkan barang.</gray>",
                "",
                "<green>» Sentuh / Klik untuk Teleport «</green>"));

        // Slot 12: Rename Button (Bedrock & Java interactive)
        inv.setItem(12, createItem(Material.NAME_TAG,
                "<gradient:#ffd700:#ffae19><bold>Beri / Ganti Nama Petak</bold></gradient>",
                "<gray>Label saat ini: </gray><yellow>" + nameLabel + "</yellow>",
                "",
                "<gray>Memberi nama khusus agar mudah dikenali</gray>",
                "<gray>di daftar wilayah & notifikasi batas.</gray>",
                "",
                "<gold>» Sentuh / Klik untuk Instruksi Nama «</gold>"));

        // Slot 13: Quick Deposit Rp1,000 to this chunk
        inv.setItem(13, createItem(Material.GOLD_INGOT,
                "<yellow><bold>Suntik Saldo Rp1.000</bold></yellow>",
                "<gray>Setor Rp1.000 langsung ke brankas</gray>",
                "<gray>petak tanah ini.</gray>",
                "",
                "<yellow>» Sentuh / Klik untuk Setor «</yellow>"));

        // Slot 14: Outpost Toggle Button
        inv.setItem(14, createItem(claim.isOutpost() ? Material.BEACON : Material.CAMPFIRE,
                claim.isOutpost() ? "<gold><bold>Cabut Status Outpost</bold></gold>" : "<gradient:#ffd700:#ff8c00><bold>Jadikan Pos Depan (Outpost)</bold></gradient>",
                "<gray>Status: </gray>" + (claim.isOutpost() ? "<green>Aktif (Diskon Pajak 50%)</green>" : "<gray>Standar</gray>"),
                "",
                "<gray>Pos Depan mendapat diskon perawatan 50%</gray>",
                "<gray>dan berfungsi sebagai pangkalan ekspedisi.</gray>",
                "",
                "<yellow>» Sentuh / Klik untuk Beralih «</yellow>"));

        // Slot 15: Remote Unclaim Button (Safe single tap with confirmation notice)
        inv.setItem(15, createItem(Material.REDSTONE_BLOCK,
                "<red><bold>Lepas Klaim (Remote Unclaim)</bold></red>",
                "<red>PERINGATAN: TINDAKAN PERMANEN</red>",
                "<gray>Lepas klaim petak ini secara permanen</gray>",
                "<gray>dari jarak jauh tanpa harus ke lokasinya.</gray>",
                "",
                "<dark_red>» Sentuh / Klik untuk Melepas «</dark_red>"));

        // Slot 22: Back to list
        inv.setItem(22, createItem(Material.ARROW, "<yellow>« Kembali ke Daftar Wilayah</yellow>"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory inv = event.getInventory();
        int slot = event.getRawSlot();

        // 1. Main Menu Handler
        if (inv.getHolder() instanceof MainHolder) {
            event.setCancelled(true);
            if (slot == 19) {
                Optional<ClaimChunk> chunkClaim = claimManager.getClaimAt(player.getLocation());
                if (chunkClaim.isEmpty()) {
                    var res = claimManager.claimCurrentChunk(player);
                    player.sendMessage(mm.deserialize(res.message()));
                    player.closeInventory();
                } else if (chunkClaim.get().isOwner(player.getUniqueId())) {
                    var res = claimManager.unclaimCurrentChunk(player);
                    player.sendMessage(mm.deserialize(res.message()));
                    player.closeInventory();
                }
            } else if (slot == 21) {
                var res = claimManager.depositBank(player, 1000.0);
                player.sendMessage(mm.deserialize(res.message()));
                open(player);
            } else if (slot == 23) {
                var res = claimManager.depositBank(player, 10000.0);
                player.sendMessage(mm.deserialize(res.message()));
                open(player);
            } else if (slot == 25) {
                // Open Centralized Territory List
                openTerritoryList(player, 0);
            } else if (slot == 29) {
                Optional<ClaimChunk> chunkClaim = claimManager.getClaimAt(player.getLocation());
                if (chunkClaim.isPresent() && (chunkClaim.get().isOwner(player.getUniqueId()) || player.isOp())) {
                    ClaimChunk c = chunkClaim.get();
                    boolean curPvp = c.getBooleanFlag("pvp", false);
                    c.setFlag("pvp", String.valueOf(!curPvp));
                    plugin.getClaimRepository().updateClaimFlags(c);
                    player.sendMessage(mm.deserialize("<green>✔ Flag PvP wilayah ini sekarang: <yellow>" + (!curPvp ? "AKTIF" : "NONAKTIF") + "</yellow>.</green>"));
                    open(player);
                }
            } else if (slot == 31) {
                claimManager.showChunkBoundary(player, player.getLocation().getChunk());
                player.sendMessage(mm.deserialize("<gold>✨ <b>[BATAS WILAYAH]</b> Memancarkan 4 tiang suar sudut dan dinding energi 16x16 di sekeliling Anda!</gold>"));
                player.closeInventory();
            } else if (slot == 33) {
                // Pulang ke claim home
                player.closeInventory();
                List<ClaimChunk> claims = claimManager.getClaimsByOwner(player.getUniqueId());
                if (claims.isEmpty()) {
                    player.sendMessage(mm.deserialize("<yellow>⚠ Anda belum memiliki klaim tanah.</yellow>"));
                } else {
                    claimManager.teleportToClaim(player, claims.get(0));
                }
            } else if (slot == 40) {
                player.closeInventory();
            }
            return;
        }

        // 2. Territory List Handler
        if (inv.getHolder() instanceof ListHolder holder) {
            event.setCancelled(true);
            int page = holder.getPage();
            List<ClaimChunk> claims = claimManager.getClaimsByOwner(player.getUniqueId());

            if (slot == 45 && page > 0) {
                openTerritoryList(player, page - 1);
                return;
            }
            if (slot == 49) {
                open(player);
                return;
            }
            if (slot == 53 && (page + 1) * 21 < claims.size()) {
                openTerritoryList(player, page + 1);
                return;
            }

            int[] slots = {
                    10, 11, 12, 13, 14, 15, 16,
                    19, 20, 21, 22, 23, 24, 25,
                    28, 29, 30, 31, 32, 33, 34
            };

            for (int i = 0; i < slots.length; i++) {
                if (slots[i] == slot) {
                    int claimIndex = (page * 21) + i;
                    if (claimIndex < claims.size()) {
                        ClaimChunk target = claims.get(claimIndex);
                        openTerritoryAction(player, target);
                    }
                    return;
                }
            }
            return;
        }

        // 3. Territory Action Handler
        if (inv.getHolder() instanceof ActionHolder holder) {
            event.setCancelled(true);
            ClaimChunk claim = holder.getClaim();

            if (slot == 11) {
                // Teleportation
                player.closeInventory();
                claimManager.teleportToClaim(player, claim);
            } else if (slot == 12) {
                // Rename instruction
                player.closeInventory();
                player.sendMessage(mm.deserialize("<gold>✎ <b>[PENAMAAN PETAK]</b> Beri nama/label khusus untuk petak ini:</gold>"));
                player.sendMessage(mm.deserialize("<yellow>/claim name " + claim.getChunkX() + " " + claim.getChunkZ() + " &lt;NamaPetak&gt;</yellow>"));
                player.sendMessage(mm.deserialize("<gray>Contoh: <white>/claim name " + claim.getChunkX() + " " + claim.getChunkZ() + " Markas Utama</white></gray>"));
                player.sendMessage(mm.deserialize("<gray>Atau cukup berdiri di petak ini lalu ketik: <white>/claim name &lt;NamaPetak&gt;</white></gray>"));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            } else if (slot == 13) {
                // Deposit Rp1,000 to this chunk
                if (plugin.getVaultHook() != null && plugin.getVaultHook().hasEconomy()) {
                    double bal = plugin.getVaultHook().getBalance(player);
                    if (bal < 1000.0) {
                        player.sendMessage(mm.deserialize("<red>✖ Saldo dompet Anda tidak cukup! Memiliki: Rp" + String.format("%,.0f", bal) + ".</red>"));
                        return;
                    }
                    plugin.getVaultHook().withdraw(player, 1000.0);
                }
                claim.deposit(1000.0);
                plugin.getClaimRepository().updateClaimFinancials(claim);
                player.sendMessage(mm.deserialize("<green>✔ Berhasil menyetor <gold>Rp1.000</gold> ke brankas petak [" + claim.getChunkX() + ", " + claim.getChunkZ() + "].</green>"));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.4f);
                openTerritoryAction(player, claim);
            } else if (slot == 14) {
                // Outpost toggle
                boolean currentOutpost = claim.isOutpost();
                var res = claimManager.setOutpost(player, claim.getWorld(), claim.getChunkX(), claim.getChunkZ(), !currentOutpost);
                player.sendMessage(mm.deserialize(res.message()));
                openTerritoryAction(player, claim);
            } else if (slot == 15) {
                // Remote Unclaim
                var res = claimManager.unclaimChunk(player, claim.getWorld(), claim.getChunkX(), claim.getChunkZ());
                player.sendMessage(mm.deserialize(res.message()));
                openTerritoryList(player, 0);
            } else if (slot == 22) {
                // Back to list
                openTerritoryList(player, 0);
            }
        }
    }

    private ItemStack createItem(Material mat, String name, String... loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            if (loreLines != null && loreLines.length > 0) {
                List<Component> lore = new ArrayList<>();
                for (String line : loreLines) {
                    lore.add(mm.deserialize(line));
                }
                meta.lore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
