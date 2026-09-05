package com.apexsions.core.cosmetics.gui;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.cosmetics.CosmeticItem;
import com.apexsions.core.cosmetics.CosmeticType;
import com.apexsions.core.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 54-Slot Interactive Cosmetics GUI for Particle Auras, Footstep Trails, and Kill FX.
 */
public class CosmeticsMainGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private CosmeticType activeTab = CosmeticType.AURA;
    private final Map<Integer, CosmeticItem> slotItemMap = new HashMap<>();

    public CosmeticsMainGUI(ApexsionsCorePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>✨ KOLEKSI KOSMETIK PARTIKEL ✨</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotItemMap.clear();

        ItemStack border = createGlass(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        ItemStack decor = createGlass(Material.ORANGE_STAINED_GLASS_PANE, "<gold>✦</gold>");

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }
        inventory.setItem(1, decor);
        inventory.setItem(7, decor);
        inventory.setItem(46, decor);
        inventory.setItem(52, decor);

        // ════════════════ CATEGORY TABS (Slots 2, 4, 6) ════════════════
        // Tab 1: AURA (Slot 2)
        boolean isAura = (activeTab == CosmeticType.AURA);
        inventory.setItem(2, createTabItem(Material.GLOWSTONE, "<gradient:#ffe900:#f39c12><bold>👑 AURA KEPALA</bold></gradient>", isAura));

        // Tab 2: TRAIL (Slot 4)
        boolean isTrail = (activeTab == CosmeticType.TRAIL);
        inventory.setItem(4, createTabItem(Material.LEATHER_BOOTS, "<gradient:#ff9f43:#ee5253><bold>👟 JEJAK KAKI (TRAIL)</bold></gradient>", isTrail));

        // Tab 3: KILL_EFFECT (Slot 6)
        boolean isKill = (activeTab == CosmeticType.KILL_EFFECT);
        inventory.setItem(6, createTabItem(Material.DIAMOND_SWORD, "<gradient:#00d2d3:#54a0ff><bold>⚔ EFEK ELIMINASI</bold></gradient>", isKill));

        // ════════════════ COSMETIC ITEMS MATRIX ════════════════
        PlayerData data = plugin.getPlayerDataService().getCached(player.getUniqueId()).orElse(null);
        String currentActiveId = null;
        if (data != null) {
            currentActiveId = switch (activeTab) {
                case AURA -> data.getActiveAura();
                case TRAIL -> data.getActiveTrail();
                case KILL_EFFECT -> data.getActiveKillEffect();
            };
        }

        int[] displaySlots = new int[]{
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        List<CosmeticItem> items = plugin.getCosmeticsManager().getCosmeticsByType(activeTab);
        for (int i = 0; i < Math.min(displaySlots.length, items.size()); i++) {
            int slot = displaySlots[i];
            CosmeticItem item = items.get(i);
            slotItemMap.put(slot, item);

            boolean unlocked = plugin.getCosmeticsManager().isCosmeticUnlocked(player, item);
            boolean equipped = (currentActiveId != null && currentActiveId.equalsIgnoreCase(item.getId()));

            ItemStack displayItem = new ItemStack(item.getIcon());
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                meta.displayName(mm.deserialize(item.getDisplayName()));
                List<Component> lore = new ArrayList<>();
                lore.add(mm.deserialize("<gray>" + item.getDescription() + "</gray>"));
                lore.add(Component.empty());

                if (item.getCondition() != null) {
                    lore.add(mm.deserialize(item.getCondition().getDescription()));
                }

                lore.add(Component.empty());
                if (equipped) {
                    lore.add(mm.deserialize("<green><bold>✔ SEDANG AKTIF</bold></green>"));
                    lore.add(mm.deserialize("<yellow>▶ Klik untuk menonaktifkan efek ini</yellow>"));
                } else if (unlocked) {
                    lore.add(mm.deserialize("<aqua><bold>★ TERBUKA (UNLOCKED)</bold></aqua>"));
                    lore.add(mm.deserialize("<yellow>▶ Klik untuk mengaktifkan efek ini!</yellow>"));
                } else {
                    lore.add(mm.deserialize("<red><bold>🔒 TERKUNCI</bold></red>"));
                    lore.add(mm.deserialize("<gray>Penuhi syarat di atas untuk membuka efek ini.</gray>"));
                }

                meta.lore(lore);
                displayItem.setItemMeta(meta);
            }
            inventory.setItem(slot, displayItem);
        }

        // Slot 48: Clear Active
        ItemStack clearBtn = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta clMeta = clearBtn.getItemMeta();
        if (clMeta != null) {
            clMeta.displayName(mm.deserialize("<red><bold>✖ MATIKAN EFEK KATEGORI INI</bold></red>"));
            clMeta.lore(List.of(mm.deserialize("<gray>Nonaktifkan efek kosmetik yang sedang dipakai pada kategori ini.</gray>")));
            clearBtn.setItemMeta(clMeta);
        }
        inventory.setItem(48, clearBtn);

        // Slot 49: Close
        ItemStack closeBtn = new ItemStack(Material.OAK_DOOR);
        ItemMeta cMeta = closeBtn.getItemMeta();
        if (cMeta != null) {
            cMeta.displayName(mm.deserialize("<red><bold>◀ TUTUP MENU KOSMETIK</bold></red>"));
            closeBtn.setItemMeta(cMeta);
        }
        inventory.setItem(49, closeBtn);
    }

    private ItemStack createTabItem(Material mat, String name, boolean selected) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            List<Component> lore = new ArrayList<>();
            if (selected) {
                lore.add(mm.deserialize("<green><bold>● KATEGORI TERPILIH</bold></green>"));
            } else {
                lore.add(mm.deserialize("<gray>Klik untuk membuka kategori ini.</gray>"));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        // Tab Switching
        if (slot == 2) {
            activeTab = CosmeticType.AURA;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            buildGUI();
            return;
        }
        if (slot == 4) {
            activeTab = CosmeticType.TRAIL;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            buildGUI();
            return;
        }
        if (slot == 6) {
            activeTab = CosmeticType.KILL_EFFECT;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            buildGUI();
            return;
        }

        if (slot == 48) {
            plugin.getCosmeticsManager().clearCosmetic(player, activeTab);
            player.sendMessage(mm.deserialize("<yellow>Efek kosmetik pada kategori ini telah dimatikan.</yellow>"));
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.8f, 1.2f);
            buildGUI();
            return;
        }

        if (slotItemMap.containsKey(slot)) {
            CosmeticItem item = slotItemMap.get(slot);
            boolean unlocked = plugin.getCosmeticsManager().isCosmeticUnlocked(player, item);

            if (!unlocked) {
                player.sendMessage(mm.deserialize("<red>🔒 Efek kosmetik ini masih terkunci! Penuhi syarat level / kerajaan untuk membukanya.</red>"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
                return;
            }

            PlayerData data = plugin.getPlayerDataService().getCached(player.getUniqueId()).orElse(null);
            String currentId = null;
            if (data != null) {
                currentId = switch (activeTab) {
                    case AURA -> data.getActiveAura();
                    case TRAIL -> data.getActiveTrail();
                    case KILL_EFFECT -> data.getActiveKillEffect();
                };
            }

            if (currentId != null && currentId.equalsIgnoreCase(item.getId())) {
                // Unequip
                plugin.getCosmeticsManager().clearCosmetic(player, activeTab);
                player.sendMessage(mm.deserialize("<yellow>Efek kosmetik berhasil dilepas.</yellow>"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 1.2f);
            } else {
                // Equip
                plugin.getCosmeticsManager().setCosmetic(player, item);
                player.sendMessage(mm.deserialize("<green>✓ Efek kosmetik </green>" + item.getDisplayName() + "<green> berhasil diaktifkan!</green>"));
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.4f);
            }
            buildGUI();
        }
    }

    private ItemStack createGlass(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
