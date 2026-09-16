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

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, mm.deserialize("<gradient:#d4af37:#f39c12><bold>⚑ KEDAULATAN TANAH</bold></gradient>"));

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
            lore.add(mm.deserialize("<gray>Total Kepemilikan: </gray><gold>" + owned + "</gold><gray>/</gray><yellow>" + max + "</yellow> <gray>chunks</gray>"));
            double pct = max > 0 ? (double) owned / max * 100.0 : 0;
            lore.add(mm.deserialize("<gray>Penggunaan Kuota: </gray><aqua>" + String.format("%.1f", pct) + "%</aqua>"));
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

        // Slot 19: Claim or Unclaim Action
        if (chunkClaim.isEmpty()) {
            inv.setItem(19, createItem(Material.GOLDEN_HOE,
                    "<green><bold>Klaim Chunk Ini</bold></green>",
                    "<gray>Klik untuk mengklaim 16x16 blok</gray>",
                    "<gray>di mana Anda sedang berdiri saat ini.</gray>",
                    "",
                    "<yellow>Biaya Kuota: 1 Chunk</yellow>",
                    "<gold>» Klik untuk Mengklaim «</gold>"));
        } else if (chunkClaim.get().isOwner(playerId)) {
            inv.setItem(19, createItem(Material.BARRIER,
                    "<red><bold>Lepas Klaim Chunk Ini (/unclaim)</bold></red>",
                    "<gray>Klik untuk membebaskan chunk ini</gray>",
                    "<gray>kembali ke wilayah publik/bebas.</gray>",
                    "",
                    "<red>» Klik untuk Melepas «</red>"));
        } else {
            inv.setItem(19, createItem(Material.IRON_BARS,
                    "<gray><bold>Chunk Tidak Tersedia</bold></gray>",
                    "<red>Sudah dimiliki oleh " + chunkClaim.get().getOwnerName() + "</red>"));
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
                "",
                "<yellow>» Klik untuk Setor Rp1.000 «</yellow>"));

        // Slot 23: Quick Deposit 10,000
        inv.setItem(23, createItem(Material.GOLD_BLOCK,
                "<gradient:#f12711:#f5af19><bold>Setoran Cepat Rp10.000</bold></gradient>",
                "<gray>Suntik saldo Rp10.000 dari dompet</gray>",
                "<gray>ke seluruh petak klaim Anda secara merata.</gray>",
                "",
                "<gold>» Klik untuk Setor Rp10.000 «</gold>"));

        // Slot 25: Flags & Policy Toggle
        if (chunkClaim.isPresent() && (chunkClaim.get().isOwner(playerId) || player.isOp())) {
            ClaimChunk c = chunkClaim.get();
            boolean pvp = c.getBooleanFlag("pvp", false);
            boolean mob = c.getBooleanFlag("mob_spawn", false);
            boolean fire = c.getBooleanFlag("fire_spread", false);

            inv.setItem(25, createItem(Material.COMPARATOR,
                    "<gradient:#00c6ff:#0072ff><bold>Pengaturan Flag Wilayah</bold></gradient>",
                    "<gray>PvP: </gray>" + (pvp ? "<green>Aktif</green>" : "<red>Mati</red>"),
                    "<gray>Spawn Mob Hostile: </gray>" + (mob ? "<green>Aktif</green>" : "<red>Mati</red>"),
                    "<gray>Penyebaran Api: </gray>" + (fire ? "<green>Aktif</green>" : "<red>Mati</red>"),
                    "",
                    "<aqua>» Klik untuk Toggle PvP Wilayah Ini «</aqua>"));
        } else {
            inv.setItem(25, createItem(Material.FILLED_MAP,
                    "<aqua><bold>Daftar Seluruh Tanah Anda</bold></aqua>",
                    "<gray>Melihat seluruh koordinat chunk</gray>",
                    "<gray>yang telah berhasil Anda kuasai.</gray>",
                    "",
                    "<dark_gray>Total: " + owned + " wilayah terdaftar</dark_gray>",
                    "<aqua>» Klik untuk Melihat di Chat «</aqua>"));
        }

        // Slot 31: Visualize Chunk Boundary
        inv.setItem(31, createItem(Material.GLOWSTONE_DUST,
                "<yellow><bold>Lihat Batas Chunk (/claim info)</bold></yellow>",
                "<gray>Memancarkan partikel debu emas di sekeliling</gray>",
                "<gray>4 sisi batas tanah chunk 16x16 ini.</gray>",
                "",
                "<gold>» Klik untuk Memunculkan Partikel «</gold>"));

        // Slot 40: Close
        inv.setItem(40, createItem(Material.ARROW, "<red><bold>Tutup Menu</bold></red>"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().title().equals(mm.deserialize("<gradient:#d4af37:#f39c12><bold>⚑ KEDAULATAN TANAH</bold></gradient>"))) {
            event.setCancelled(true);
            int slot = event.getRawSlot();

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
                // Deposit 1,000
                var res = claimManager.depositBank(player, 1000.0);
                player.sendMessage(mm.deserialize(res.message()));
                open(player);
            } else if (slot == 23) {
                // Deposit 10,000
                var res = claimManager.depositBank(player, 10000.0);
                player.sendMessage(mm.deserialize(res.message()));
                open(player);
            } else if (slot == 25) {
                Optional<ClaimChunk> chunkClaim = claimManager.getClaimAt(player.getLocation());
                if (chunkClaim.isPresent() && (chunkClaim.get().isOwner(player.getUniqueId()) || player.isOp())) {
                    ClaimChunk c = chunkClaim.get();
                    boolean curPvp = c.getBooleanFlag("pvp", false);
                    c.setFlag("pvp", String.valueOf(!curPvp));
                    plugin.getClaimRepository().updateClaimFlags(c);
                    player.sendMessage(mm.deserialize("<green>✔ Flag PvP wilayah ini sekarang: <yellow>" + (!curPvp ? "AKTIF" : "NONAKTIF") + "</yellow>.</green>"));
                    open(player);
                } else {
                    player.closeInventory();
                    player.performCommand("claim list");
                }
            } else if (slot == 31) {
                claimManager.showChunkBoundary(player, player.getLocation().getChunk());
                player.sendMessage(mm.deserialize("<gold>✨ Partikel batas chunk telah dimunculkan selama 8 detik!</gold>"));
                player.closeInventory();
            } else if (slot == 40) {
                player.closeInventory();
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
