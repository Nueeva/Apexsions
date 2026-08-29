package com.apexsions.core.gui.warp;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.warp.Warp;
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

public class WarpGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private String selectedCategory = "ALL";
    private int page = 0;
    private final List<Warp> displayedWarps = new ArrayList<>();

    public WarpGUI(ApexsionsCorePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#3498db:#2ecc71><bold>✦ NAVIGASI WARP SERVER ✦</bold></gradient>"));
        render();
    }

    public void render() {
        inventory.clear();

        // 1. Render Category Tabs on Row 1 (slots 0-8)
        List<String> categories = plugin.getWarpManager().getCategories();
        int catSlot = 0;
        for (String cat : categories) {
            if (catSlot > 8) break;
            boolean isSelected = cat.equalsIgnoreCase(selectedCategory);
            Material catMat = getCategoryMaterial(cat);

            ItemStack catItem = new ItemStack(catMat);
            ItemMeta meta = catItem.getItemMeta();
            if (meta != null) {
                String color = isSelected ? "<yellow><bold>" : "<gray>";
                meta.displayName(mm.deserialize(color + "Kategori: " + cat + (isSelected ? " ✔" : "")));
                List<Component> lore = new ArrayList<>();
                lore.add(mm.deserialize("<dark_gray>Filter warp kategori " + cat + "</dark_gray>"));
                lore.add(Component.empty());
                lore.add(isSelected
                        ? mm.deserialize("<green>● Kategori sedang aktif</green>")
                        : mm.deserialize("<yellow>Klik untuk memilih kategori ini</yellow>"));
                meta.lore(lore);
                if (isSelected) {
                    meta.setEnchantmentGlintOverride(true);
                }
                catItem.setItemMeta(meta);
            }
            inventory.setItem(catSlot++, catItem);
        }

        // Fill empty header slots with filler
        ItemStack pane = createFiller(Material.GRAY_STAINED_GLASS_PANE);
        while (catSlot < 9) {
            inventory.setItem(catSlot++, pane);
        }

        // 2. Filter warps by category and player permissions
        displayedWarps.clear();
        for (Warp w : plugin.getWarpManager().getWarps()) {
            if (w.isHidden() && !player.hasPermission("apexsionscore.warp.admin")) continue;
            if (selectedCategory.equalsIgnoreCase("ALL") || w.getCategory().equalsIgnoreCase(selectedCategory)) {
                displayedWarps.add(w);
            }
        }

        // 3. Render Warp Slots (Slots 9 to 44 = 36 slots per page)
        int pageSize = 36;
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, displayedWarps.size());

        int currentSlot = 9;
        for (int i = startIndex; i < endIndex; i++) {
            Warp warp = displayedWarps.get(i);
            inventory.setItem(currentSlot++, createWarpItem(warp));
        }

        // 4. Fill bottom bar (slots 45-53)
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, createFiller(Material.BLACK_STAINED_GLASS_PANE));
        }

        // Previous Page
        if (page > 0) {
            inventory.setItem(45, createButton(Material.ARROW, "<yellow>◀ Halaman Sebelumnya</yellow>", "<gray>Ke halaman " + page + "</gray>"));
        }

        // Center Info
        ItemStack info = new ItemStack(Material.NETHER_STAR);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(mm.deserialize("<gold><bold>Informasi Warp</bold></gold>"));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Total Warp: <yellow>" + displayedWarps.size() + "</yellow></gray>"));
            lore.add(mm.deserialize("<gray>Kategori: <aqua>" + selectedCategory + "</aqua></gray>"));
            lore.add(mm.deserialize("<gray>Halaman: <white>" + (page + 1) + "</white></gray>"));
            infoMeta.lore(lore);
            info.setItemMeta(infoMeta);
        }
        inventory.setItem(49, info);

        // Next Page
        if (endIndex < displayedWarps.size()) {
            inventory.setItem(53, createButton(Material.ARROW, "<yellow>Halaman Berikutnya ▶</yellow>", "<gray>Ke halaman " + (page + 2) + "</gray>"));
        }
    }

    private ItemStack createWarpItem(Warp warp) {
        boolean canAccess = warp.canAccess(player);
        ItemStack item = new ItemStack(warp.getIcon());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String titleColor = canAccess ? "<gradient:#f1c40f:#e67e22><bold>" : "<red><bold>";
            meta.displayName(mm.deserialize(titleColor + warp.getName() + "</gradient></bold>"));

            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Kategori: <aqua>" + warp.getCategory() + "</aqua></gray>"));
            lore.add(mm.deserialize("<gray>Dunia: <white>" + warp.getWorldName() + "</white></gray>"));
            lore.add(mm.deserialize("<gray>Delay: <yellow>" + (warp.getDelaySeconds() > 0 ? warp.getDelaySeconds() + " Detik" : "Instan") + "</yellow></gray>"));

            if (warp.getDescription() != null && !warp.getDescription().isBlank()) {
                lore.add(Component.empty());
                lore.add(mm.deserialize("<dark_gray>Deskripsi:</dark_gray>"));
                lore.add(mm.deserialize("<white>" + warp.getDescription() + "</white>"));
            }

            lore.add(Component.empty());
            if (canAccess) {
                lore.add(mm.deserialize("<green>✔ Hak Akses Terbuka</green>"));
                lore.add(mm.deserialize("<yellow>▶ Klik Kiri untuk Teleportasi!</yellow>"));
            } else {
                lore.add(mm.deserialize("<red>✖ Terkunci (Perlu Izin Khusus)</red>"));
            }

            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        // Header Category Click (0-8)
        if (slot >= 0 && slot <= 8) {
            List<String> categories = plugin.getWarpManager().getCategories();
            if (slot < categories.size()) {
                String clickedCat = categories.get(slot);
                if (!clickedCat.equalsIgnoreCase(selectedCategory)) {
                    this.selectedCategory = clickedCat;
                    this.page = 0;
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.4f);
                    render();
                }
            }
            return;
        }

        // Pagination Click
        if (slot == 45 && page > 0) {
            page--;
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
            render();
            return;
        }
        if (slot == 53) {
            int pageSize = 36;
            if ((page + 1) * pageSize < displayedWarps.size()) {
                page++;
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                render();
            }
            return;
        }

        // Warp item click (9-44)
        if (slot >= 9 && slot <= 44) {
            int warpIndex = (page * 36) + (slot - 9);
            if (warpIndex < displayedWarps.size()) {
                Warp warp = displayedWarps.get(warpIndex);
                if (warp.canAccess(player)) {
                    player.closeInventory();
                    plugin.getWarpManager().teleportPlayer(player, warp);
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red>Kamu tidak memiliki izin untuk teleportasi ke warp <yellow>" + warp.getName() + "</yellow>!</red>"));
                }
            }
        }
    }

    private Material getCategoryMaterial(String cat) {
        return switch (cat.toUpperCase()) {
            case "ALL" -> Material.COMPASS;
            case "SERVER", "SPAWN" -> Material.BEACON;
            case "RESOURCE", "MINING" -> Material.DIAMOND_PICKAXE;
            case "EVENT" -> Material.GOLDEN_APPLE;
            case "KINGDOM" -> Material.SHIELD;
            case "PVP", "ARENA" -> Material.DIAMOND_SWORD;
            default -> Material.PAPER;
        };
    }

    private ItemStack createFiller(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createButton(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            if (lore != null) {
                meta.lore(List.of(mm.deserialize(lore)));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
