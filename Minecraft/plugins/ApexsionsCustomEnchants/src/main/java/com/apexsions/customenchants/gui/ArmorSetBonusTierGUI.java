package com.apexsions.customenchants.gui;

import com.apexsions.core.kit.KitStatType;
import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dedicated GUI for configuring a specific Armor Set Bonus Tier (2-Piece Half Set or 4-Piece Full Set).
 * Each stat opens its own StatValuePickerGUI when clicked.
 */
public class ArmorSetBonusTierGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final int tierPieces; // 2 or 4
    private final Map<KitStatType, Double> tierStats;
    private final ArmorSetBonusPickerGUI hubGUI;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ArmorSetBonusTierGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, int tierPieces,
                                Map<KitStatType, Double> tierStats, ArmorSetBonusPickerGUI hubGUI) {
        this.plugin = plugin;
        this.player = player;
        this.tierPieces = tierPieces;
        this.tierStats = tierStats;
        this.hubGUI = hubGUI;
        String title = tierPieces == 2
                ? "<gradient:#3498db:#2ecc71><bold>🛡 BONUS 2-PIECE (HALF SET) 🛡</bold></gradient>"
                : "<gradient:#e74c3c:#f39c12><bold>👑 BONUS 4-PIECE (FULL SET) 👑</bold></gradient>";
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize(title));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null, false);
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, border);
        }

        // Slot 4: Info Header
        Material headerMat = tierPieces == 2 ? Material.CHAINMAIL_CHESTPLATE : Material.NETHERITE_CHESTPLATE;
        String tierName = tierPieces == 2 ? "2 Pieces (Half Set)" : "4 Pieces (Full Set)";
        inventory.setItem(4, createItem(headerMat,
                "<gradient:#f1c40f:#e67e22><bold>⚙ KONFIGURASI BONUS TIER: " + tierName + "</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Syarat Aktif: Memakai minimal <gold>" + tierPieces + " bagian armor</gold> set ini.</gray>"),
                        mm.deserialize("<gray>Stat aktif saat ini: <yellow>" + tierStats.size() + " Stat Efek</yellow></gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik Kiri stat untuk membuka GUI pengaturan nilainya.</yellow>"),
                        mm.deserialize("<red>▶ Klik Kanan stat untuk menonaktifkannya langsung.</red>")
                ), true));

        // 6 Stat Buttons
        addStatButton(11, KitStatType.DAMAGE_REDUCTION, Material.SHIELD);
        addStatButton(12, KitStatType.ATTACK_DAMAGE_BOOST, Material.DIAMOND_SWORD);
        addStatButton(13, KitStatType.DODGE_CHANCE, Material.FEATHER);
        addStatButton(20, KitStatType.CRITICAL_DAMAGE_BOOST, Material.BLAZE_POWDER);
        addStatButton(21, KitStatType.EXTRA_MAX_HEALTH, Material.GOLDEN_APPLE);
        addStatButton(22, KitStatType.MOVEMENT_SPEED_BOOST, Material.SUGAR);

        // Slot 40: Clear All in this tier
        inventory.setItem(40, createItem(Material.LAVA_BUCKET,
                "<red><bold>✖ HAPUS SELURUH STAT PADA TIER INI</bold></red>",
                List.of(
                        mm.deserialize("<gray>Kosongkan semua efek bonus untuk tier " + tierPieces + "-Pieces ini.</gray>"),
                        Component.empty(),
                        mm.deserialize("<red>▶ Klik untuk mengosongkan tier ini!</red>")
                ), false));

        // Slot 45: Back to Hub
        inventory.setItem(45, createItem(Material.ARROW,
                "<gradient:#3498db:#2980b9><bold>⬅ SIMPAN & KEMBALI KE MENU UTAMA</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Kembali ke menu pemilihan set bonus.</gray>")
                ), false));
    }

    private void addStatButton(int slot, KitStatType type, Material icon) {
        boolean isActive = tierStats.containsKey(type);
        double val = tierStats.getOrDefault(type, 0.0);

        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<gray>Stat: <gold>" + type.getDisplayName() + "</gold></gray>"));
        lore.add(Component.empty());
        if (isActive) {
            lore.add(mm.deserialize("<green>● Status: AKTIF (<gold>" + type.formatValue(val) + "</gold>)</green>"));
        } else {
            lore.add(mm.deserialize("<dark_gray>● Status: Non-aktif</dark_gray>"));
        }
        lore.add(Component.empty());
        lore.add(mm.deserialize("<green>▶ Klik Kiri: Buka GUI atur nilai/persentase</green>"));
        if (isActive) {
            lore.add(mm.deserialize("<red>▶ Klik Kanan: Hapus stat ini</red>"));
        }

        String title = (isActive ? "<green><bold>[✓] " : "<gray>") + type.getDisplayName() + (isActive ? "</bold></green>" : "</gray>");
        inventory.setItem(slot, createItem(icon, title, lore, isActive));
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        ClickType click = event.getClick();

        // Back
        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            hubGUI.open();
            return;
        }

        // Clear All
        if (slot == 40) {
            tierStats.clear();
            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<red>Seluruh stat bonus untuk tier " + tierPieces + "-Pieces berhasil dikosongkan.</red>"));
            buildGUI();
            return;
        }

        // Stat Selection
        KitStatType type = switch (slot) {
            case 11 -> KitStatType.DAMAGE_REDUCTION;
            case 12 -> KitStatType.ATTACK_DAMAGE_BOOST;
            case 13 -> KitStatType.DODGE_CHANCE;
            case 20 -> KitStatType.CRITICAL_DAMAGE_BOOST;
            case 21 -> KitStatType.EXTRA_MAX_HEALTH;
            case 22 -> KitStatType.MOVEMENT_SPEED_BOOST;
            default -> null;
        };

        if (type != null) {
            if (click.isRightClick()) {
                if (tierStats.containsKey(type)) {
                    tierStats.remove(type);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red>Stat <gold>" + type.getDisplayName() + "</gold> dihapus dari tier ini.</red>"));
                    buildGUI();
                }
            } else {
                // Left-click: Open dedicated StatValuePickerGUI!
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                double current = tierStats.getOrDefault(type, type.getDefaultValue());
                new StatValuePickerGUI(plugin, player, type, current, this, selectedVal -> {
                    if (selectedVal <= 0.0) {
                        tierStats.remove(type);
                    } else {
                        tierStats.put(type, selectedVal);
                    }
                }).open();
            }
        }
    }

    private ItemStack createItem(Material mat, String name, List<Component> lore, boolean glow) {
        ItemStack is = new ItemStack(mat);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            if (name != null) meta.displayName(mm.deserialize(name));
            if (lore != null) meta.lore(lore);
            if (glow) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            is.setItemMeta(meta);
        }
        return is;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
