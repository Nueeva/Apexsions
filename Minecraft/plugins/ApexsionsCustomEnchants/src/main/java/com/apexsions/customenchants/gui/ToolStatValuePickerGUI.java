package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.tools.ToolStatType;
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
 * Sub-GUI for selecting the percentage / value of a single Tool or Weapon set bonus stat.
 */
public class ToolStatValuePickerGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final ToolStatType statType;
    private double currentValue;
    private final ToolBonusPickerGUI parentGUI;
    private final Consumer<Double> onValueSelected;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ToolStatValuePickerGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, ToolStatType statType,
                                  double currentValue, ToolBonusPickerGUI parentGUI, Consumer<Double> onValueSelected) {
        this.plugin = plugin;
        this.player = player;
        this.statType = statType;
        this.currentValue = currentValue;
        this.parentGUI = parentGUI;
        this.onValueSelected = onValueSelected;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#3498db:#9b59b6><bold>⚡ ATUR STAT TOOL: " + statType.getDisplayName().toUpperCase() + " ⚡</bold></gradient>"));
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
        inventory.setItem(4, createItem(statType.getIcon(),
                "<gradient:#f1c40f:#e67e22><bold>Stat: " + statType.getDisplayName() + "</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Nilai saat ini: <gold>" + statType.formatValue(currentValue) + "</gold></gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>Pilih salah satu nilai di bawah ini untuk diterapkan.</yellow>"),
                        mm.deserialize("<gray>Efek ini hanya aktif jika pemain memakai set armor yang sama.</gray>")
                ), true));

        // Value options tailored to stat
        if (statType == ToolStatType.UNBREAKABLE_SET || statType == ToolStatType.FATIGUE_IMMUNITY) {
            boolean isAktif = (currentValue > 0.0);
            inventory.setItem(22, createItem(isAktif ? Material.LIME_DYE : Material.GRAY_DYE,
                    isAktif ? "<green><bold>STATUS: AKTIF [KLIK UNTUK MATIKAN]</bold></green>" : "<red><bold>STATUS: NONAKTIF [KLIK UNTUK AKTIFKAN]</bold></red>",
                    List.of(
                            mm.deserialize("<gray>Klik untuk toggle status aktif / nonaktif efek ini.</gray>")
                    ), isAktif));
        } else {
            double[] options = switch (statType) {
                case ATTACK_REACH_BOOST, MINING_REACH_BOOST -> new double[]{1.0, 2.0};
                case EXP_MULTIPLIER -> new double[]{25.0, 50.0, 75.0, 100.0};
                default -> new double[]{10.0, 15.0, 20.0, 25.0, 30.0, 40.0, 50.0};
            };

            int[] slots = switch (options.length) {
                case 2 -> new int[]{21, 23};
                case 4 -> new int[]{20, 21, 23, 24};
                default -> new int[]{20, 21, 22, 23, 24, 30, 31};
            };

            for (int i = 0; i < options.length && i < slots.length; i++) {
                double v = options[i];
                boolean isSelected = (Math.abs(currentValue - v) < 0.01);
                int slot = slots[i];

                inventory.setItem(slot, createItem(Material.EXPERIENCE_BOTTLE,
                        "<gold><bold>" + statType.formatValue(v) + "</bold></gold>" + (isSelected ? " <green>[AKTIF]</green>" : ""),
                        List.of(
                                mm.deserialize("<gray>Terapkan nilai: <gold>" + statType.formatValue(v) + "</gold></gray>"),
                                Component.empty(),
                                mm.deserialize(isSelected ? "<green>● Sedang terpilih!</green>" : "<yellow>▶ Klik untuk memilih nilai ini.</yellow>")
                        ), isSelected));
            }
        }

        // Slot 40: Disable
        inventory.setItem(40, createItem(Material.LAVA_BUCKET,
                "<red><bold>✖ NONAKTIFKAN STAT INI</bold></red>",
                List.of(
                        mm.deserialize("<gray>Hapus efek <yellow>" + statType.getDisplayName() + "</yellow> dari tool ini.</gray>")
                ), false));

        // Slot 45: Back
        inventory.setItem(45, createItem(Material.ARROW,
                "<gradient:#3498db:#2980b9><bold>⬅ KEMBALI</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Kembali ke menu pemilihan bonus tool.</gray>")
                ), false));
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        // Back
        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            parentGUI.open();
            return;
        }

        // Disable
        if (slot == 40) {
            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<yellow>Stat <gold>" + statType.getDisplayName() + "</gold> dinonaktifkan.</yellow>"));
            if (onValueSelected != null) {
                onValueSelected.accept(0.0);
            }
            parentGUI.open();
            return;
        }

        // Toggle on/off for toggleable stats
        if (statType == ToolStatType.UNBREAKABLE_SET || statType == ToolStatType.FATIGUE_IMMUNITY) {
            if (slot == 22) {
                double newVal = (currentValue > 0.0) ? 0.0 : 1.0;
                this.currentValue = newVal;
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                if (onValueSelected != null) {
                    onValueSelected.accept(newVal);
                }
                parentGUI.open();
                return;
            }
        }

        // Value options
        double[] options = switch (statType) {
            case ATTACK_REACH_BOOST, MINING_REACH_BOOST -> new double[]{1.0, 2.0};
            case EXP_MULTIPLIER -> new double[]{25.0, 50.0, 75.0, 100.0};
            default -> new double[]{10.0, 15.0, 20.0, 25.0, 30.0, 40.0, 50.0};
        };

        int[] slots = switch (options.length) {
            case 2 -> new int[]{21, 23};
            case 4 -> new int[]{20, 21, 23, 24};
            default -> new int[]{20, 21, 22, 23, 24, 30, 31};
        };

        for (int i = 0; i < options.length && i < slots.length; i++) {
            if (slot == slots[i]) {
                double chosen = options[i];
                this.currentValue = chosen;
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                player.sendMessage(mm.deserialize("<green>✓ Nilai <gold>" + statType.getDisplayName() + "</gold> diatur ke <gold>" + statType.formatValue(chosen) + "</gold>!</green>"));
                if (onValueSelected != null) {
                    onValueSelected.accept(chosen);
                }
                parentGUI.open();
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
