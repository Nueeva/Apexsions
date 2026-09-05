package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
import com.apexsions.customenchants.group.EnchantmentGroup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Exact replica of AdvancedEnchantments /ae admin catalog GUI, accessible via /ace enchants.
 * Supports dynamic filtering by Rarity, Item Category, and Name Search.
 */
public class AceEnchantsCatalogGUI implements InventoryHolder {

    public static final List<String> RARITIES = List.of(
            "ALL", "SIMPLE", "UNIQUE", "ELITE", "ULTIMATE", "LEGENDARY", "FABLED", "HEROIC"
    );

    public static final List<String> CATEGORIES = List.of(
            "ALL", "SWORD", "ARMOR", "TOOLS", "BOW", "FISHING", "HOE", "TRIDENT", "ELYTRA"
    );

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private int page;
    private String rarityFilter;
    private String categoryFilter;
    private String searchFilter;
    private List<CustomEnchant> filteredList;
    private final Map<Integer, CustomEnchant> slotMap = new HashMap<>();

    public AceEnchantsCatalogGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, int page, String rarityFilter, String categoryFilter, String searchFilter) {
        this.plugin = plugin;
        this.player = player;
        this.page = Math.max(1, page);
        this.rarityFilter = (rarityFilter != null && !rarityFilter.isBlank()) ? rarityFilter.toUpperCase().trim() : "ALL";
        this.categoryFilter = (categoryFilter != null && !categoryFilter.isBlank()) ? categoryFilter.toUpperCase().trim() : "ALL";
        this.searchFilter = (searchFilter != null && !searchFilter.isBlank()) ? searchFilter.toLowerCase().trim() : null;

        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gold><bold>AE</bold></gold> <green>Admin (" + this.page + ")</green>"));
        refreshFilteredList();
        buildGUI();
    }

    public AceEnchantsCatalogGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, int page, String filter) {
        this(plugin, player, page, "ALL", "ALL", filter);
    }

    public void open() {
        player.openInventory(inventory);
    }

    private void refreshFilteredList() {
        List<CustomEnchant> list = new ArrayList<>(plugin.getEnchantmentRegistry().getAllEnchantments());

        // 1. Rarity filter
        if (!rarityFilter.equals("ALL")) {
            list.removeIf(e -> !e.getGroup().getId().equalsIgnoreCase(rarityFilter));
        }

        // 2. Category filter
        if (!categoryFilter.equals("ALL")) {
            list.removeIf(e -> !matchesCategory(e.getAppliesTo().toUpperCase(), categoryFilter));
        }

        // 3. Search query filter
        if (searchFilter != null && !searchFilter.isEmpty()) {
            String q = searchFilter.toLowerCase().trim();
            list.removeIf(e -> !e.getId().contains(q) && !e.getDisplayName().toLowerCase().contains(q) && !e.getDescription().toLowerCase().contains(q));
        }

        list.sort(Comparator.comparing(CustomEnchant::getId));
        this.filteredList = list;
    }

    private boolean matchesCategory(String target, String category) {
        if (category.equalsIgnoreCase("ALL")) return true;
        if (category.equalsIgnoreCase("ARMOR")) {
            return target.contains("ARMOR") || target.contains("HELMET") || target.contains("CHESTPLATE") || target.contains("LEGGING") || target.contains("BOOT");
        }
        if (category.equalsIgnoreCase("SWORD")) return target.contains("SWORD") || target.contains("WEAPON");
        if (category.equalsIgnoreCase("BOW")) return target.contains("BOW") || target.contains("CROSSBOW");
        if (category.equalsIgnoreCase("TOOLS")) return target.contains("TOOL") || target.contains("PICKAXE") || target.contains("AXE") || target.contains("SHOVEL");
        if (category.equalsIgnoreCase("FISHING")) return target.contains("FISHING");
        if (category.equalsIgnoreCase("HOE")) return target.contains("HOE") || target.contains("HOES");
        if (category.equalsIgnoreCase("TRIDENT")) return target.contains("TRIDENT");
        if (category.equalsIgnoreCase("ELYTRA")) return target.contains("ELYTRA");
        return target.contains(category);
    }

    public void buildGUI() {
        inventory.clear();
        slotMap.clear();

        int maxPerPage = 45;
        int totalPages = Math.max(1, (int) Math.ceil((double) filteredList.size() / maxPerPage));
        page = Math.max(1, Math.min(totalPages, page));

        int startIndex = (page - 1) * maxPerPage;
        int endIndex = Math.min(startIndex + maxPerPage, filteredList.size());

        for (int i = startIndex; i < endIndex; i++) {
            CustomEnchant enchant = filteredList.get(i);
            int slot = i - startIndex;
            slotMap.put(slot, enchant);

            EnchantmentGroup grp = enchant.getGroup();
            ItemStack item = new ItemStack(Material.FIREWORK_STAR);
            FireworkEffectMeta meta = (FireworkEffectMeta) item.getItemMeta();
            if (meta != null) {
                FireworkEffect effect = FireworkEffect.builder()
                        .withColor(grp.getBukkitColor())
                        .build();
                meta.setEffect(effect);
                meta.displayName(mm.deserialize("<gray>Enchantment</gray> <gold>" + enchant.getDisplayName() + "</gold>"));
                List<Component> lore = new ArrayList<>();
                lore.add(mm.deserialize("<yellow><bold>x</bold></yellow> <gray>" + enchant.getDescription() + "</gray>"));
                lore.add(Component.empty());
                lore.add(mm.deserialize("<gold><bold>x</bold></gold> <gray>Enchant Type:</gray> <yellow>" + enchant.getAppliesTo() + "</yellow>"));
                lore.add(mm.deserialize("<gold><bold>x</bold></gold> <gray>Applies to:</gray> <yellow>" + enchant.getAppliesTo() + "</yellow>"));
                lore.add(mm.deserialize("<gold><bold>x</bold></gold> <gray>Levels:</gray> <yellow>I - " + CustomEnchant.toRoman(enchant.getMaxLevel()) + " (" + enchant.getMaxLevel() + ")</yellow>"));
                lore.add(mm.deserialize("<gold><bold>x</bold></gold> <gray>Tier Group:</gray> " + grp.getDisplayName()));
                lore.add(Component.empty());
                lore.add(mm.deserialize("<dark_gray>>> </dark_gray><gold>Click</gold> <gray>to</gray> <yellow>Access Books</yellow>"));
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(slot, item);
        }

        if (slotMap.isEmpty()) {
            inventory.setItem(22, createItem(Material.BARRIER, "<red>Tidak ada sihir yang cocok dengan filter!</red>", List.of(
                    "<gray>Coba ubah atau reset filter di bawah.</gray>"
            )));
        }

        // --- Bottom Navigation & Filter Bar ---
        // Slot 45: Dekorasi Info
        inventory.setItem(45, createItem(Material.NETHER_STAR, "<gradient:#9b59b6:#3498db><bold>⚡ APEXSIONS ENCHANTS</bold></gradient>", List.of(
                "<gray>Database 182 Custom Enchantments</gray>",
                "<gray>Identik 100% dengan AdvancedEnchantments.</gray>"
        )));

        // Slot 46: Filter Rarity
        ItemStack rarityItem = new ItemStack(Material.FIREWORK_STAR);
        FireworkEffectMeta rMeta = (FireworkEffectMeta) rarityItem.getItemMeta();
        if (rMeta != null) {
            org.bukkit.Color rColor = org.bukkit.Color.WHITE;
            if (!rarityFilter.equals("ALL")) {
                EnchantmentGroup g = plugin.getGroupRegistry().getGroup(rarityFilter);
                if (g != null) rColor = g.getBukkitColor();
            }
            rMeta.setEffect(FireworkEffect.builder().withColor(rColor).build());
            rMeta.displayName(mm.deserialize("<gradient:#9b59b6:#e74c3c><bold>Filter Rarity:</bold></gradient> <yellow>" + (rarityFilter.equals("ALL") ? "SEMUA TIER" : rarityFilter) + "</yellow>"));
            List<Component> rLore = new ArrayList<>();
            for (String r : RARITIES) {
                boolean active = r.equalsIgnoreCase(rarityFilter);
                rLore.add(mm.deserialize((active ? "<green><bold>▶ " + r + " [AKTIF]</bold></green>" : "<dark_gray>  " + r + "</dark_gray>")));
            }
            rLore.add(Component.empty());
            rLore.add(mm.deserialize("<yellow>● Left-Click: Tier berikutnya ▶</yellow>"));
            rLore.add(mm.deserialize("<yellow>● Right-Click: ◀ Tier sebelumnya</yellow>"));
            rLore.add(mm.deserialize("<red>● Shift-Click: Reset ke Semua Tier</red>"));
            rMeta.lore(rLore);
            rarityItem.setItemMeta(rMeta);
        }
        inventory.setItem(46, rarityItem);

        // Slot 47: Filter Kategori
        ItemStack catItem = new ItemStack(Material.COMPASS);
        ItemMeta cMeta = catItem.getItemMeta();
        if (cMeta != null) {
            cMeta.displayName(mm.deserialize("<gradient:#3498db:#2ecc71><bold>Filter Kategori:</bold></gradient> <yellow>" + (categoryFilter.equals("ALL") ? "SEMUA TARGET" : categoryFilter) + "</yellow>"));
            List<Component> cLore = new ArrayList<>();
            for (String c : CATEGORIES) {
                boolean active = c.equalsIgnoreCase(categoryFilter);
                cLore.add(mm.deserialize((active ? "<green><bold>▶ " + c + " [AKTIF]</bold></green>" : "<dark_gray>  " + c + "</dark_gray>")));
            }
            cLore.add(Component.empty());
            cLore.add(mm.deserialize("<yellow>● Left-Click: Kategori berikutnya ▶</yellow>"));
            cLore.add(mm.deserialize("<yellow>● Right-Click: ◀ Kategori sebelumnya</yellow>"));
            cLore.add(mm.deserialize("<red>● Shift-Click: Reset ke Semua Kategori</red>"));
            cMeta.lore(cLore);
            catItem.setItemMeta(cMeta);
        }
        inventory.setItem(47, catItem);

        // Slot 48: Previous Page
        if (page > 1) {
            inventory.setItem(48, createItem(Material.BOOK, "<dark_gray>«</dark_gray> <gold>Halaman Sebelumnya (" + (page - 1) + ")</gold>", null));
        }

        // Slot 49: Close
        inventory.setItem(49, createItem(Material.ANVIL, "<red><bold>✖ Tutup Halaman</bold></red>", null));

        // Slot 50: Next Page
        if (endIndex < filteredList.size()) {
            inventory.setItem(50, createItem(Material.BOOK, "<dark_gray>>></dark_gray> <gold>Halaman Berikutnya (" + (page + 1) + ")</gold>", null));
        }

        // Slot 51: Info Statistik
        inventory.setItem(51, createItem(Material.CHEST, "<gold><bold>Informasi Filter & Statistik</bold></gold>", List.of(
                "<gray>Total Keseluruhan: <yellow>182 Sihir</yellow></gray>",
                "<gray>Ditemukan (Filter): <green>" + filteredList.size() + " Sihir</green></gray>",
                "<gray>Halaman: <aqua>" + page + " / " + totalPages + "</aqua></gray>"
        )));

        // Slot 52: Pencarian / Reset Filter
        inventory.setItem(52, createItem(Material.NAME_TAG, "<gold><bold>Pencarian & Reset Filter</bold></gold>", List.of(
                "<gray>Rarity: <yellow>" + rarityFilter + "</yellow></gray>",
                "<gray>Kategori: <yellow>" + categoryFilter + "</yellow></gray>",
                "<gray>Pencarian: <yellow>" + (searchFilter != null ? searchFilter : "Tidak Ada") + "</yellow></gray>",
                Component.empty(),
                "<green>▶ Left-Click untuk Reset Semua Filter</green>",
                "<yellow>▶ Right-Click untuk Cari via Chat</yellow>"
        )));
    }

    private ItemStack createItem(Material mat, String name, List<?> loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            if (loreLines != null) {
                List<Component> cList = new ArrayList<>();
                for (Object l : loreLines) {
                    if (l instanceof Component c) cList.add(c);
                    else if (l instanceof String s) cList.add(mm.deserialize(s));
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
        ClickType click = event.getClick();

        // Slot 46: Filter Rarity Cycle
        if (slot == 46) {
            if (click.isShiftClick()) {
                rarityFilter = "ALL";
            } else if (click.isRightClick()) {
                int idx = RARITIES.indexOf(rarityFilter);
                if (idx < 0) idx = 0;
                rarityFilter = RARITIES.get((idx - 1 + RARITIES.size()) % RARITIES.size());
            } else {
                int idx = RARITIES.indexOf(rarityFilter);
                if (idx < 0) idx = 0;
                rarityFilter = RARITIES.get((idx + 1) % RARITIES.size());
            }
            page = 1;
            refreshFilteredList();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            buildGUI();
            return;
        }

        // Slot 47: Filter Category Cycle
        if (slot == 47) {
            if (click.isShiftClick()) {
                categoryFilter = "ALL";
            } else if (click.isRightClick()) {
                int idx = CATEGORIES.indexOf(categoryFilter);
                if (idx < 0) idx = 0;
                categoryFilter = CATEGORIES.get((idx - 1 + CATEGORIES.size()) % CATEGORIES.size());
            } else {
                int idx = CATEGORIES.indexOf(categoryFilter);
                if (idx < 0) idx = 0;
                categoryFilter = CATEGORIES.get((idx + 1) % CATEGORIES.size());
            }
            page = 1;
            refreshFilteredList();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            buildGUI();
            return;
        }

        // Slot 48: Previous Page
        if (slot == 48 && page > 1) {
            page--;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            buildGUI();
            return;
        }

        // Slot 49: Close
        if (slot == 49) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        // Slot 50: Next Page
        int maxPerPage = 45;
        if (slot == 50 && (page * maxPerPage < filteredList.size())) {
            page++;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            buildGUI();
            return;
        }

        // Slot 52: Reset or Search
        if (slot == 52) {
            if (click.isRightClick()) {
                player.closeInventory();
                player.sendMessage(mm.deserialize("<gold>Ketik di chat untuk mencari atau jalankan perintah: <yellow>/ace enchants <kata_kunci></yellow></gold>"));
            } else {
                rarityFilter = "ALL";
                categoryFilter = "ALL";
                searchFilter = null;
                page = 1;
                refreshFilteredList();
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                buildGUI();
            }
            return;
        }

        CustomEnchant enchant = slotMap.get(slot);
        if (enchant != null) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new AceBookLevelsSubGUI(plugin, player, enchant, page, rarityFilter, categoryFilter, searchFilter).open();
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
