package com.apexsions.core.region.gui;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import com.apexsions.core.region.gui.holder.KingdomInfoHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * 54-Slot Interactive Kingdom Detail & Lore Information GUI.
 */
public class KingdomInfoGUI implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public KingdomInfoGUI(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, String kingdomKey) {
        Optional<Region> regionOpt = plugin.getRegionManager().getRegion(kingdomKey);
        if (regionOpt.isEmpty()) {
            player.sendMessage(mm.deserialize("<red>Kerajaan tidak ditemukan!</red>"));
            return;
        }

        Region region = regionOpt.get();
        ConfigurationSection regSec = plugin.getConfigManager().getSection("regions." + kingdomKey.toUpperCase());

        KingdomInfoHolder holder = new KingdomInfoHolder(kingdomKey);
        String kDisplayName = regSec != null ? regSec.getString("display-name", region.getDisplayName()) : region.getDisplayName();
        Component title = mm.deserialize("<dark_gray><bold>👑 INFO KERAJAAN: </bold></dark_gray>" + kDisplayName);

        Inventory inv = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inv);

        // Determine Theme Accent
        Material accentMat = switch (kingdomKey.toUpperCase()) {
            case "ZENITHAR" -> Material.YELLOW_STAINED_GLASS_PANE;
            case "SOLTERRA" -> Material.RED_STAINED_GLASS_PANE;
            case "SYLVAMOOR" -> Material.CYAN_STAINED_GLASS_PANE;
            default -> Material.PURPLE_STAINED_GLASS_PANE;
        };

        // Fill background borders
        ItemStack darkGlass = createGlass(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack grayGlass = createGlass(Material.GRAY_STAINED_GLASS_PANE);
        ItemStack accentGlass = createGlass(accentMat);

        for (int i = 0; i < 54; i++) {
            inv.setItem(i, grayGlass);
        }
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, darkGlass);
            inv.setItem(45 + i, darkGlass);
        }
        inv.setItem(0, accentGlass);
        inv.setItem(8, accentGlass);
        inv.setItem(45, accentGlass);
        inv.setItem(53, accentGlass);
        inv.setItem(18, darkGlass);
        inv.setItem(26, darkGlass);
        inv.setItem(27, darkGlass);
        inv.setItem(35, darkGlass);

        // 1. Slot 4: Kingdom Banner Header & Lore
        Material iconMat = Material.matchMaterial(regSec != null ? regSec.getString("icon", "GOLD_BLOCK") : "GOLD_BLOCK");
        if (iconMat == null) iconMat = Material.GOLD_BLOCK;

        ItemStack headerItem = new ItemStack(iconMat);
        ItemMeta headerMeta = headerItem.getItemMeta();
        if (headerMeta != null) {
            headerMeta.displayName(mm.deserialize(kDisplayName));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            if (regSec != null) {
                List<String> loreLines = regSec.getStringList("lore");
                for (String line : loreLines) {
                    lore.add(mm.deserialize(line));
                }
            }
            lore.add(Component.empty());
            headerMeta.lore(lore);
            headerMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
            headerMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            headerItem.setItemMeta(headerMeta);
        }
        inv.setItem(4, headerItem);

        // 2. Slot 19: Ibukota & Bioma Realm
        ItemStack capitalItem = new ItemStack(Material.COMPASS);
        ItemMeta capitalMeta = capitalItem.getItemMeta();
        if (capitalMeta != null) {
            capitalMeta.displayName(mm.deserialize("<gold><bold>🏛 Ibukota & Bioma Wilayah</bold></gold>"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            String capName = regSec != null ? regSec.getString("capital.name", "Citadel") : "Citadel";
            String capCoords = regSec != null ? regSec.getString("capital.coordinates", "world (0, 64, 0)") : "world (0, 64, 0)";
            String biomes = regSec != null ? regSec.getString("capital.biomes", "Plains") : "Plains";

            lore.add(mm.deserialize("<gray>Nama Ibukota: <yellow>" + capName + "</yellow></gray>"));
            lore.add(mm.deserialize("<gray>Koordinat Realm: <yellow>" + capCoords + "</yellow></gray>"));
            lore.add(mm.deserialize("<gray>Bioma Dominan: <white>" + biomes + "</white></gray>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<dark_gray>Wilayah dilindungi secara sihir oleh Nexus Kerajaan.</dark_gray>"));
            capitalMeta.lore(lore);
            capitalItem.setItemMeta(capitalMeta);
        }
        inv.setItem(19, capitalItem);

        // 3. Slot 21: Total Warga Terdaftar (Async fetched)
        ItemStack citizensItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta citizensMeta = citizensItem.getItemMeta();
        if (citizensMeta != null) {
            citizensMeta.displayName(mm.deserialize("<aqua><bold>👥 Populasi & Warga Terdaftar</bold></aqua>"));
            citizensMeta.lore(List.of(
                    Component.empty(),
                    mm.deserialize("<gray>Memuat data sensus kerajaan...</gray>")
            ));
            citizensItem.setItemMeta(citizensMeta);
        }
        inv.setItem(21, citizensItem);

        // Fetch citizen count async and update item
        plugin.getPlayerRepository().getTotalPlayersInRegionAsync(region.getId()).thenAccept(total -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.getOpenInventory().getTopInventory().getHolder() instanceof KingdomInfoHolder h
                        && h.getKingdomKey().equalsIgnoreCase(kingdomKey)) {
                    ItemStack updated = inv.getItem(21);
                    if (updated != null) {
                        ItemMeta m = updated.getItemMeta();
                        if (m != null) {
                            m.lore(List.of(
                                    Component.empty(),
                                    mm.deserialize("<gray>Total Warga Resmi: <yellow><bold>" + total + "</bold></yellow> pemain</gray>"),
                                    Component.empty(),
                                    mm.deserialize("<gray>Kekuatan aliansi dan prestise kerajaan</gray>"),
                                    mm.deserialize("<gray>ditentukan oleh jumlah dan keaktifan warganya.</gray>")
                            ));
                            updated.setItemMeta(m);
                            inv.setItem(21, updated);
                        }
                    }
                }
            });
        });

        // 4. Slot 23: Raja Kerajaan (King / Monarch)
        ItemStack kingItem = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta kingMeta = kingItem.getItemMeta();
        if (kingMeta != null) {
            kingMeta.displayName(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>👑 Pemimpin Tertinggi (Raja)</bold></gradient>"));
            String kingName = regSec != null ? regSec.getString("king.name", "Belum Ada Raja") : "Belum Ada Raja";
            String kingTitle = regSec != null ? regSec.getString("king.title", "Monarch") : "Monarch";

            kingMeta.lore(List.of(
                    Component.empty(),
                    mm.deserialize("<gray>Nama Raja: <yellow><bold>" + kingName + "</bold></yellow></gray>"),
                    mm.deserialize("<gray>Gelar Tahta: <gold>" + kingTitle + "</gold></gray>"),
                    Component.empty(),
                    mm.deserialize("<dark_gray>Raja memiliki otoritas tertinggi atas kerajaan</dark_gray>"),
                    mm.deserialize("<dark_gray>dan diangkat langsung oleh Dewan Tetua Admin.</dark_gray>")
            ));
            kingMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            kingItem.setItemMeta(kingMeta);
        }
        inv.setItem(23, kingItem);

        // 5. Slot 25: Tarif Pajak Wilayah
        ItemStack taxItem = new ItemStack(Material.EMERALD);
        ItemMeta taxMeta = taxItem.getItemMeta();
        if (taxMeta != null) {
            taxMeta.displayName(mm.deserialize("<green><bold>💰 Tarif Pajak & Ekonomi Wilayah</bold></green>"));
            double taxRate = regSec != null ? regSec.getDouble("tax-percent", 10.0) : 10.0;
            taxMeta.lore(List.of(
                    Component.empty(),
                    mm.deserialize("<gray>Pajak Transaksi Pasar: <gold><bold>" + String.format("%.1f", taxRate) + "%</bold></gold></gray>"),
                    Component.empty(),
                    mm.deserialize("<gray>Pajak ditarik otomatis dari transaksi pasar</gray>"),
                    mm.deserialize("<gray>dan dialokasikan untuk kas perbendaharaan kerajaan.</gray>")
            ));
            taxItem.setItemMeta(taxMeta);
        }
        inv.setItem(25, taxItem);

        // 6. Slot 29: Buff Spesialisasi Kerajaan
        ItemStack buffItem = new ItemStack(Material.BEACON);
        ItemMeta buffMeta = buffItem.getItemMeta();
        if (buffMeta != null) {
            buffMeta.displayName(mm.deserialize("<green><bold>✦ Spesialisasi & Keunggulan (Buff)</bold></green>"));
            List<Component> buffLore = new ArrayList<>();
            buffLore.add(Component.empty());
            if (regSec != null) {
                List<String> buffs = regSec.getStringList("buffs");
                for (String b : buffs) {
                    buffLore.add(mm.deserialize(" • " + b));
                }
            }
            buffLore.add(Component.empty());
            buffLore.add(mm.deserialize("<dark_gray>Berlaku bagi seluruh warga resmi kerajaan ini.</dark_gray>"));
            buffMeta.lore(buffLore);
            buffItem.setItemMeta(buffMeta);
        }
        inv.setItem(29, buffItem);

        // 7. Slot 31: Nerf / Kelemahan Kerajaan
        ItemStack nerfItem = new ItemStack(Material.WITHER_ROSE);
        ItemMeta nerfMeta = nerfItem.getItemMeta();
        if (nerfMeta != null) {
            nerfMeta.displayName(mm.deserialize("<red><bold>✖ Kelemahan Kerajaan (Nerf)</bold></red>"));
            List<Component> nerfLore = new ArrayList<>();
            nerfLore.add(Component.empty());
            if (regSec != null) {
                List<String> nerfs = regSec.getStringList("nerfs");
                for (String n : nerfs) {
                    nerfLore.add(mm.deserialize(" • " + n));
                }
            }
            nerfLore.add(Component.empty());
            nerfLore.add(mm.deserialize("<dark_gray>Kelemahan alami untuk menjaga keseimbangan realm.</dark_gray>"));
            nerfMeta.lore(nerfLore);
            nerfItem.setItemMeta(nerfMeta);
        }
        inv.setItem(31, nerfItem);

        // 8. Slot 33: Progresi Gelar Level 1–100
        ItemStack titlesItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta titlesMeta = titlesItem.getItemMeta();
        if (titlesMeta != null) {
            titlesMeta.displayName(mm.deserialize("<light_purple><bold>📜 Gelar Kebangsawanan (Level 1–100)</bold></light_purple>"));
            String startTitle = regSec != null ? regSec.getString("titles.starting", "Acolyte") : "Acolyte";
            String supremeTitle = regSec != null ? regSec.getString("titles.supreme", "Emperor") : "Emperor";

            titlesMeta.lore(List.of(
                    Component.empty(),
                    mm.deserialize("<gray>Gelar Awal (Level 1): </gray>" + startTitle),
                    mm.deserialize("<gray>Gelar Tertinggi (Level 100): </gray>" + supremeTitle),
                    Component.empty(),
                    mm.deserialize("<gray>Tingkatkan levelmu untuk membuka 10 tier</gray>"),
                    mm.deserialize("<gray>gelar eksklusif kerajaan ini!</gray>")
            ));
            titlesItem.setItemMeta(titlesMeta);
        }
        inv.setItem(33, titlesItem);

        // 9. Slot 40: Tombol Kembali
        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backBtn.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(mm.deserialize("<yellow><bold>« Kembali ke Pilihan Kerajaan</bold></yellow>"));
            backBtn.setItemMeta(backMeta);
        }
        inv.setItem(40, backBtn);

        // 10. Slot 49: Tombol Sumpah Setia (PILIH KERAJAAN)
        Optional<PlayerData> pDataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
        UUID playerRegId = pDataOpt.map(PlayerData::getRegionId).orElse(null);

        ItemStack actionBtn;
        if (playerRegId != null && playerRegId.equals(region.getId())) {
            actionBtn = new ItemStack(Material.EMERALD_BLOCK);
            ItemMeta m = actionBtn.getItemMeta();
            if (m != null) {
                m.displayName(mm.deserialize("<green><bold>✔ ANDA ADALAH WARGA RESMI</bold></green>"));
                m.lore(List.of(
                        Component.empty(),
                        mm.deserialize("<gray>Anda telah bersumpah setia kepada kerajaan ini.</gray>")
                ));
                actionBtn.setItemMeta(m);
            }
        } else if (playerRegId != null) {
            actionBtn = new ItemStack(Material.BARRIER);
            ItemMeta m = actionBtn.getItemMeta();
            if (m != null) {
                m.displayName(mm.deserialize("<red><bold>🔒 TERKUNCI</bold></red>"));
                m.lore(List.of(
                        Component.empty(),
                        mm.deserialize("<gray>Anda sudah terikat pada kerajaan lain!</gray>")
                ));
                actionBtn.setItemMeta(m);
            }
        } else {
            actionBtn = new ItemStack(Material.NETHERITE_SWORD);
            ItemMeta m = actionBtn.getItemMeta();
            if (m != null) {
                m.displayName(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>⚡ BERSUMPAH SETIA KE " + region.getDisplayName().toUpperCase() + " ⚡</bold></gradient>"));
                m.lore(List.of(
                        Component.empty(),
                        mm.deserialize("<yellow>Klik untuk konfirmasi sumpah setia ke kerajaan ini.</yellow>"),
                        mm.deserialize("<red><bold>PENTING:</bold> Pilihan bersifat PERMANEN!</red>")
                ));
                m.addEnchant(Enchantment.UNBREAKING, 1, true);
                m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
                actionBtn.setItemMeta(m);
            }
        }
        inv.setItem(49, actionBtn);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f, 1.2f);
    }

    private ItemStack createGlass(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof KingdomInfoHolder holder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();
        String kingdomKey = holder.getKingdomKey();

        // Back button (Slot 40)
        if (slot == 40) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            plugin.getRegionSelectionGUI().open(player);
            return;
        }

        // Action / Pledge Allegiance button (Slot 49)
        if (slot == 49) {
            Optional<PlayerData> pDataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
            if (pDataOpt.isEmpty()) {
                player.sendMessage(mm.deserialize("<red>Data profilmu sedang dimuat. Silakan coba lagi.</red>"));
                return;
            }

            PlayerData data = pDataOpt.get();
            if (data.hasRegion()) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<red>Kamu sudah bersumpah setia pada suatu kerajaan!</red>"));
                return;
            }

            // Open Confirmation Dialog
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
            new KingdomConfirmGUI(plugin).open(player, kingdomKey);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof KingdomInfoHolder) {
            event.setCancelled(true);
        }
    }
}
