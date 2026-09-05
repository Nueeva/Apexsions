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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dedicated sub-GUI for configuring the exact value/percentage of a single armor stat.
 */
public class StatValuePickerGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final KitStatType statType;
    private double currentValue;
    private final InventoryHolder parentGUI;
    private final Consumer<Double> onValueSelected;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public StatValuePickerGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, KitStatType statType,
                              double currentValue, InventoryHolder parentGUI, Consumer<Double> onValueSelected) {
        this.plugin = plugin;
        this.player = player;
        this.statType = statType;
        this.currentValue = currentValue;
        this.parentGUI = parentGUI;
        this.onValueSelected = onValueSelected;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>⚡ ATUR STAT: " + statType.getDisplayName().toUpperCase() + " ⚡</bold></gradient>"));
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
        Material statIcon = switch (statType) {
            case DAMAGE_REDUCTION -> Material.SHIELD;
            case ATTACK_DAMAGE_BOOST -> Material.DIAMOND_SWORD;
            case DODGE_CHANCE -> Material.FEATHER;
            case CRITICAL_DAMAGE_BOOST -> Material.BLAZE_POWDER;
            case EXTRA_MAX_HEALTH -> Material.GOLDEN_APPLE;
            case MOVEMENT_SPEED_BOOST -> Material.SUGAR;
        };

        inventory.setItem(4, createItem(statIcon,
                "<gradient:#f1c40f:#e67e22><bold>Stat: " + statType.getDisplayName() + "</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Nilai saat ini: <gold>" + statType.formatValue(currentValue) + "</gold></gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>Pilih salah satu persentase / nilai di bawah ini</yellow>"),
                        mm.deserialize("<gray>untuk diterapkan ke tier set bonus ini.</gray>")
                ), true));

        // Value options
        double[] options;
        if (statType == KitStatType.EXTRA_MAX_HEALTH) {
            options = new double[]{2.0, 4.0, 6.0, 8.0, 10.0, 14.0, 20.0};
        } else {
            options = new double[]{5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 50.0};
        }

        int[] valueSlots = {20, 21, 22, 23, 24, 30, 31};
        for (int i = 0; i < options.length && i < valueSlots.length; i++) {
            double v = options[i];
            boolean isSelected = (Math.abs(currentValue - v) < 0.01);
            int slot = valueSlots[i];

            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Terapkan nilai: <gold>" + statType.formatValue(v) + "</gold></gray>"));
            if (statType == KitStatType.EXTRA_MAX_HEALTH) {
                lore.add(mm.deserialize("<gray>Setara dengan <gold>" + (long) (v / 2.0) + " Buah Hati</gold></gray>"));
            }
            lore.add(Component.empty());
            lore.add(mm.deserialize(isSelected ? "<green>● Sedang terpilih!</green>" : "<yellow>▶ Klik untuk memilih nilai ini.</yellow>"));

            inventory.setItem(slot, createItem(Material.EXPERIENCE_BOTTLE,
                    "<gold><bold>" + statType.formatValue(v) + "</bold></gold>" + (isSelected ? " <green>[AKTIF]</green>" : ""),
                    lore, isSelected));
        }

        // Slot 40: Disable / Remove Stat
        inventory.setItem(40, createItem(Material.LAVA_BUCKET,
                "<red><bold>✖ NONAKTIFKAN STAT INI</bold></red>",
                List.of(
                        mm.deserialize("<gray>Hapus stat <yellow>" + statType.getDisplayName() + "</yellow> dari tier ini.</gray>"),
                        Component.empty(),
                        mm.deserialize("<red>▶ Klik untuk menonaktifkan stat!</red>")
                ), false));

        // Slot 45: Back
        inventory.setItem(45, createItem(Material.ARROW,
                "<gradient:#3498db:#2980b9><bold>⬅ KEMBALI</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Kembali ke menu pengaturan tier.</gray>")
                ), false));
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        // Back
        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            if (parentGUI instanceof ArmorSetBonusTierGUI tierGUI) {
                tierGUI.open();
            } else if (parentGUI != null) {
                player.openInventory(parentGUI.getInventory());
            } else {
                player.closeInventory();
            }
            return;
        }

        // Disable / Remove
        if (slot == 40) {
            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<yellow>Stat <gold>" + statType.getDisplayName() + "</gold> dinonaktifkan.</yellow>"));
            if (onValueSelected != null) {
                onValueSelected.accept(0.0);
            }
            if (parentGUI instanceof ArmorSetBonusTierGUI tierGUI) {
                tierGUI.open();
            } else if (parentGUI != null) {
                player.openInventory(parentGUI.getInventory());
            }
            return;
        }

        // Value Selection
        double[] options;
        if (statType == KitStatType.EXTRA_MAX_HEALTH) {
            options = new double[]{2.0, 4.0, 6.0, 8.0, 10.0, 14.0, 20.0};
        } else {
            options = new double[]{5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 50.0};
        }

        int[] valueSlots = {20, 21, 22, 23, 24, 30, 31};
        for (int i = 0; i < options.length && i < valueSlots.length; i++) {
            if (slot == valueSlots[i]) {
                double chosen = options[i];
                this.currentValue = chosen;
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                player.sendMessage(mm.deserialize("<green>✓ Nilai <gold>" + statType.getDisplayName() + "</gold> diatur ke <gold>" + statType.formatValue(chosen) + "</gold>!</green>"));
                if (onValueSelected != null) {
                    onValueSelected.accept(chosen);
                }
                if (parentGUI instanceof ArmorSetBonusTierGUI tierGUI) {
                    tierGUI.open();
                } else if (parentGUI != null) {
                    player.openInventory(parentGUI.getInventory());
                }
                return;
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
