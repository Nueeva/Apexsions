package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.group.EnchantmentGroup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Interactive Admin GUI for adjusting tier costs, currency types, and shop odds.
 */
public class AdminTierPricingGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<Integer, EnchantmentGroup> slotGroupMap = new HashMap<>();

    public AdminTierPricingGUI(ApexsionsCustomEnchantsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 45, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>⚙ ADMIN TIER PRICING & ODDS ⚙</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotGroupMap.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Header slot 4
        inventory.setItem(4, createItem(Material.GOLD_BLOCK, "<gradient:#f1c40f:#e67e22><bold>⚙ PENGATURAN HARGA & MATA UANG TIER ⚙</bold></gradient>", List.of(
                "<gray>Atur jenis mata uang dan nominal gacha per tier.</gray>",
                "",
                "<yellow>• Klik Kiri:</yellow> <white>Ganti Mata Uang (Rupiah ↔ Diamond)</white>",
                "<yellow>• Klik Kanan:</yellow> <white>Toggle Enabled / Coming Soon</white>",
                "<yellow>• Shift+Klik Kiri:</yellow> <green>Tambah Harga (+)</green>",
                "<yellow>• Shift+Klik Kanan:</yellow> <red>Kurangi Harga (-)</red>"
        )));

        // Tiers in Row 2 (Slots 10, 11, 12, 13, 14, 15, 16)
        String[] tierOrder = {"SIMPLE", "UNIQUE", "ELITE", "ULTIMATE", "LEGENDARY", "FABLED", "HEROIC"};
        int[] slots = {10, 11, 12, 13, 14, 15, 16};

        for (int i = 0; i < tierOrder.length; i++) {
            EnchantmentGroup grp = plugin.getGroupRegistry().getGroup(tierOrder[i]);
            if (grp == null) continue;

            int slot = slots[i];
            slotGroupMap.put(slot, grp);

            List<String> lore = new ArrayList<>();
            lore.add("<gray>Mata Uang:</gray> <yellow><bold>" + grp.getCurrency().toUpperCase() + "</bold></yellow>");
            lore.add("<gray>Harga Gacha:</gray> <gold><bold>" + grp.getFormattedCost() + "</bold></gold>");
            lore.add("<gray>Status:</gray> " + (grp.isComingSoon() ? "<red>Coming Soon</red>" : (grp.isEnabled() ? "<green>Aktif</green>" : "<red>Non-Aktif</red>")));
            lore.add("");
            lore.add("<yellow>▶ [Klik Kiri] Ganti Mata Uang</yellow>");
            lore.add("<aqua>▶ [Klik Kanan] Toggle Status</aqua>");
            lore.add("<green>▶ [Shift+Kiri] Tambah Harga</green>");
            lore.add("<red>▶ [Shift+Kanan] Kurangi Harga</red>");

            inventory.setItem(slot, createItem(grp.getIcon(), grp.getDisplayName(), lore));
        }

        // Specific Shop Settings Row 3 (Slots 21, 23)
        // Slot 21: Multiplier
        double mul = plugin.getSpecificBookMultiplier();
        inventory.setItem(21, createItem(Material.REPEATER, "<gradient:#3498db:#9b59b6><bold>MULTIPLIER TOKO SPESIFIK</bold></gradient>", List.of(
                "<gray>Faktor pengali harga beli buku spesifik:</gray>",
                "<gold><bold>" + (int) mul + "x Lipat Harga Gacha</bold></gold>",
                "",
                "<yellow>▶ Klik untuk ganti multiplier (2x, 3x, 4x, 5x)!</yellow>"
        )));

        // Slot 23: Success Chance
        int succ = (int) plugin.getSpecificBookSuccessChance();
        inventory.setItem(23, createItem(Material.EXPERIENCE_BOTTLE, "<gradient:#2ecc71:#27ae60><bold>PELUANG SUKSES BUKU SPESIFIK</bold></gradient>", List.of(
                "<gray>Persentase keberhasilan buku spesifik:</gray>",
                "<green><bold>" + succ + "% Success Rate</bold></green>",
                "",
                "<yellow>▶ Klik untuk ganti persentase (40%, 50%, 60%, 75%)!</yellow>"
        )));

        // Slot 40: Kembali ke Hub /ace
        inventory.setItem(40, createItem(Material.ARROW, "<red><bold>◀ KEMBALI KE ADMIN HUB</bold></red>", List.of("<gray>Buka menu /ace utama.</gray>")));
    }

    private ItemStack createItem(Material mat, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(mat != null ? mat : Material.STONE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            if (loreLines != null) {
                List<Component> cList = new ArrayList<>();
                for (String l : loreLines) {
                    cList.add(mm.deserialize(l));
                }
                meta.lore(cList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 40) { // Return to Admin Hub
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new AceAdminHubGUI(plugin, player).open();
            return;
        }

        if (slot == 21) { // Multiplier cycle
            double mul = plugin.getSpecificBookMultiplier();
            double nextMul = mul >= 5.0 ? 2.0 : mul + 1.0;
            plugin.setSpecificBookMultiplier(nextMul);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            buildGUI();
            return;
        }

        if (slot == 23) { // Success chance cycle
            double cur = plugin.getSpecificBookSuccessChance();
            double next = cur >= 75.0 ? 40.0 : cur + 10.0;
            if (next == 70.0) next = 75.0;
            plugin.setSpecificBookSuccessChance(next);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            buildGUI();
            return;
        }

        EnchantmentGroup grp = slotGroupMap.get(slot);
        if (grp != null) {
            ClickType click = event.getClick();
            if (click == ClickType.LEFT) {
                // Toggle currency
                String cur = grp.getCurrency().equalsIgnoreCase("rupiah") ? "diamond" : "rupiah";
                grp.setCurrency(cur);
                // Adjust default scale when switching
                if (cur.equals("diamond") && grp.getCost() > 1000) {
                    grp.setCost(25.0);
                } else if (cur.equals("rupiah") && grp.getCost() < 1000) {
                    grp.setCost(50000.0);
                }
                plugin.getGroupRegistry().saveGroup(grp);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                buildGUI();
            } else if (click == ClickType.RIGHT) {
                // Toggle Coming soon / enabled
                if (grp.isComingSoon()) {
                    grp.setComingSoon(false);
                    grp.setEnabled(true);
                } else if (grp.isEnabled()) {
                    grp.setEnabled(false);
                } else {
                    grp.setComingSoon(true);
                }
                plugin.getGroupRegistry().saveGroup(grp);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                buildGUI();
            } else if (click == ClickType.SHIFT_LEFT) {
                // Increase price
                if (grp.getCurrency().equalsIgnoreCase("diamond")) {
                    grp.setCost(grp.getCost() + 5.0);
                } else {
                    grp.setCost(grp.getCost() + 10000.0);
                }
                plugin.getGroupRegistry().saveGroup(grp);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
                buildGUI();
            } else if (click == ClickType.SHIFT_RIGHT) {
                // Decrease price
                if (grp.getCurrency().equalsIgnoreCase("diamond")) {
                    grp.setCost(Math.max(1.0, grp.getCost() - 5.0));
                } else {
                    grp.setCost(Math.max(1000.0, grp.getCost() - 10000.0));
                }
                plugin.getGroupRegistry().saveGroup(grp);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.0f);
                buildGUI();
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
