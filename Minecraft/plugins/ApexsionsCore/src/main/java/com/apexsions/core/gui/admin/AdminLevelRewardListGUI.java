package com.apexsions.core.gui.admin;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.level.reward.Reward;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminLevelRewardListGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final int page;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private static final int LEVELS_PER_PAGE = 28;
    private static final int MAX_PAGES = 4; // Levels 2..100 = 99 levels -> ceil(99/28) = 4 pages
    private static final int[] CENTER_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public AdminLevelRewardListGUI(ApexsionsCorePlugin plugin, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.page = Math.max(1, Math.min(page, MAX_PAGES));
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#c0392b:#e74c3c><bold>⚙ EDITOR HADIAH LEVEL - HAL " + this.page + " ⚙</bold></gradient>"));
        build();
    }

    public void build() {
        inventory.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<gray> </gray>", List.of());
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Header Info (Slot 4)
        ItemStack header = createItem(Material.NETHER_STAR, "<gradient:#f1c40f:#e67e22><bold>★ KELOLA HADIAH LEVEL (DRAG & DROP) ★</bold></gradient>", List.of(
                mm.deserialize("<gray>Pilih level untuk mengedit hadiah item fisik & perintah.</gray>"),
                mm.deserialize("<gray>Sistem drag-and-drop memungkinkan admin memasukkan item langsung!</gray>")
        ));
        inventory.setItem(4, header);

        // Render Levels in Center Slots
        int startLevel = 2 + (page - 1) * LEVELS_PER_PAGE;
        int endLevel = Math.min(100, startLevel + LEVELS_PER_PAGE - 1);

        int slotIdx = 0;
        for (int lvl = startLevel; lvl <= endLevel && slotIdx < CENTER_SLOTS.length; lvl++) {
            final int level = lvl;
            Optional<Reward> rewardOpt = plugin.getRewardManager().getReward(level);

            boolean isMilestone = (level % 10 == 1 && level > 1) || level == 100;
            int itemCount = rewardOpt.map(r -> r.getItems().size()).orElse(0);
            int cmdCount = rewardOpt.map(r -> r.getCommands().size()).orElse(0);
            long reqXp = plugin.getLevelFormula().getXpForLevel(level);

            Material icon = isMilestone ? Material.ENDER_CHEST : (itemCount > 0 ? Material.CHEST : Material.EXPERIENCE_BOTTLE);

            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Required XP: <yellow>" + (reqXp == Long.MAX_VALUE ? "MAX" : reqXp + " XP") + "</yellow></gray>"));
            lore.add(mm.deserialize("<gray>Tipe: " + (isMilestone ? "<gold>★ Milestone</gold>" : "<aqua>Reguler</aqua>") + "</gray>"));
            lore.add(mm.deserialize("<gray>Hadiah Item Fisik: <green>" + itemCount + " Item</green></gray>"));
            lore.add(mm.deserialize("<gray>Hadiah Perintah: <light_purple>" + cmdCount + " Command</light_purple></gray>"));
            lore.add(mm.deserialize(""));
            lore.add(mm.deserialize("<yellow><bold>» KLIK UNTUK BUKA EDITOR LEVEL «</bold></yellow>"));

            String title = isMilestone
                    ? "<gold><bold>★ LEVEL " + level + " (MILESTONE) ★</bold></gold>"
                    : "<aqua><bold>Level " + level + "</bold></aqua>";

            inventory.setItem(CENTER_SLOTS[slotIdx++], createItem(icon, title, lore));
        }

        // Navigation (Row 5)
        // Slot 45: Back to Admin Hub
        inventory.setItem(45, createItem(Material.ARROW, "<gradient:#3498db:#2980b9><bold>⬅ KEMBALI KE ADMIN HUB</bold></gradient>", List.of(
                mm.deserialize("<gray>Kembali ke panel Master Admin.</gray>")
        )));

        // Slot 47: Prev
        if (page > 1) {
            inventory.setItem(47, createItem(Material.ARROW, "<yellow>« Halaman " + (page - 1) + "</yellow>", List.of(
                    mm.deserialize("<gray>Buka halaman sebelumnya.</gray>")
            )));
        }

        // Slot 49: Page indicator
        inventory.setItem(49, createItem(Material.BOOK, "<gold><bold>Halaman " + page + " / " + MAX_PAGES + "</bold></gold>", List.of(
                mm.deserialize("<gray>Level " + startLevel + " - " + endLevel + "</gray>")
        )));

        // Slot 51: Next
        if (page < MAX_PAGES) {
            inventory.setItem(51, createItem(Material.ARROW, "<yellow>Halaman " + (page + 1) + " »</yellow>", List.of(
                    mm.deserialize("<gray>Buka halaman selanjutnya.</gray>")
            )));
        }

        // Slot 53: Close
        inventory.setItem(53, createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>", List.of(
                mm.deserialize("<gray>Tutup menu.</gray>")
        )));
    }

    public void handleClick(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        if (slot == 53) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return;
        }

        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            new CoreAdminSubGUI(plugin, player).open();
            return;
        }

        if (slot == 47 && page > 1) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            player.openInventory(new AdminLevelRewardListGUI(plugin, player, page - 1).getInventory());
            return;
        }

        if (slot == 51 && page < MAX_PAGES) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            player.openInventory(new AdminLevelRewardListGUI(plugin, player, page + 1).getInventory());
            return;
        }

        // Check center level slots
        for (int i = 0; i < CENTER_SLOTS.length; i++) {
            if (slot == CENTER_SLOTS[i]) {
                int startLevel = 2 + (page - 1) * LEVELS_PER_PAGE;
                int targetLevel = startLevel + i;
                if (targetLevel <= 100) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                    player.openInventory(new AdminLevelRewardEditorGUI(plugin, player, targetLevel, page).getInventory());
                }
                return;
            }
        }
    }

    private ItemStack createItem(Material mat, String name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
