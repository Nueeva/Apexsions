package com.apexsions.core.region.gui;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.region.gui.holder.XpCategoryDetailHolder;
import com.apexsions.core.region.gui.holder.XpGuideHubHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Interactive XP Guide Directory featuring a main category hub and in-depth
 * sub-menus where EVERY single mob and item is completely separated with concise labels.
 */
public class XpGuideGUI implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final int ITEMS_PER_PAGE = 28;

    public record GuideEntry(Material material, String name, long xp, String note) {}

    public XpGuideGUI(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    // =========================================================================
    // 1. MAIN XP GUIDE HUB (45 SLOTS)
    // =========================================================================
    public void open(Player player) {
        XpGuideHubHolder holder = new XpGuideHubHolder();
        String titleStr = plugin.getConfigManager().getGuiConfig().getString("xp-guide.title", "<dark_gray><bold>Panduan XP</bold></dark_gray>");
        Component title = miniMessage.deserialize(titleStr);
        Inventory inv = Bukkit.createInventory(holder, 45, title);
        holder.setInventory(inv);

        // Decorative borders
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, "<gray> </gray>");
        ItemStack cyanAccent = createItem(Material.CYAN_STAINED_GLASS_PANE, "<dark_aqua>⚔</dark_aqua>");

        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, border);
            }
        }
        inv.setItem(0, cyanAccent);
        inv.setItem(8, cyanAccent);
        inv.setItem(36, cyanAccent);
        inv.setItem(44, cyanAccent);

        // Top Center: Guide Overview
        ItemStack guideBook = createItem(Material.KNOWLEDGE_BOOK,
                "<gold><bold>Panduan XP</bold></gold>",
                "<gray>Daftar 13 sumber perolehan XP.",
                "<yellow>Klik kategori untuk melihat rincian.</yellow>");
        inv.setItem(4, guideBook);

        // Row 2: Gathering & Combat Categories
        inv.setItem(10, createCategoryCard(Material.DIAMOND_PICKAXE,
                "<yellow><bold>Mining</bold></yellow>",
                "<gray>Ore, kristal & bebatuan</gray>",
                "<yellow>» Klik untuk buka</yellow>"));

        inv.setItem(11, createCategoryCard(Material.NETHERITE_AXE,
                "<green><bold>Woodcutting</bold></green>",
                "<gray>Log pohon & stem nether</gray>",
                "<green>» Klik untuk buka</green>"));

        inv.setItem(12, createCategoryCard(Material.DIAMOND_HOE,
                "<gold><bold>Farming</bold></gold>",
                "<gray>Tanaman & breeding ternak</gray>",
                "<gold>» Klik untuk buka</gold>"));

        inv.setItem(13, createCategoryCard(Material.DIAMOND_SWORD,
                "<red><bold>Monster & Boss</bold></red>",
                "<gray>Monster, nether & apex boss</gray>",
                "<red>» Klik untuk buka</red>"));

        inv.setItem(14, createCategoryCard(Material.NETHERITE_SWORD,
                "<dark_red><bold>Duel PvP</bold></dark_red>",
                "<gray>Kill pemain: <gold>+25 XP</gold></gray>",
                "<dark_gray>Cooldown 120 detik</dark_gray>"));

        // Row 3: Arcane, Crafting, Mobility & Fishing
        inv.setItem(20, createCategoryCard(Material.FISHING_ROD,
                "<aqua><bold>Fishing</bold></aqua>",
                "<gray>Ikan & harta karun</gray>",
                "<aqua>» Klik untuk buka</aqua>"));

        inv.setItem(21, createCategoryCard(Material.FURNACE,
                "<gold><bold>Cooking</bold></gold>",
                "<gray>Peleburan & makanan</gray>",
                "<gold>» Klik untuk buka</gold>"));

        inv.setItem(22, createCategoryCard(Material.BREWING_STAND,
                "<blue><bold>Brewing</bold></blue>",
                "<gray>Ramuan & golden apple</gray>",
                "<blue>» Klik untuk buka</blue>"));

        inv.setItem(23, createCategoryCard(Material.ENCHANTING_TABLE,
                "<light_purple><bold>Enchanting</bold></light_purple>",
                "<gray>Sihir meja & anvil</gray>",
                "<light_purple>» Klik untuk buka</light_purple>"));

        inv.setItem(24, createCategoryCard(Material.COMPASS,
                "<yellow><bold>Exploration</bold></yellow>",
                "<gray>Jalan, renang, elytra & lompat</gray>",
                "<yellow>» Klik untuk buka</yellow>"));

        // Bottom Navigation Bar
        inv.setItem(39, createItem(Material.NETHER_STAR,
                "<aqua><bold>Profil</bold></aqua>",
                "<gray>Kembali ke profil.</gray>"));

        inv.setItem(40, createItem(Material.BARRIER,
                "<red><bold>Tutup</bold></red>"));

        inv.setItem(41, createItem(Material.CHEST,
                "<gold><bold>Hadiah</bold></gold>",
                "<gray>Buka menu hadiah level.</gray>"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
    }

    // =========================================================================
    // 2. CATEGORY DETAIL SUB-MENU (54 SLOTS) WITH PAGINATION
    // =========================================================================
    public void openCategoryDetail(Player player, String category) {
        openCategoryDetail(player, category, 1);
    }

    public void openCategoryDetail(Player player, String category, int page) {
        List<GuideEntry> allEntries = getCategoryEntries(category);
        int totalItems = allEntries.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE));
        int safePage = Math.max(1, Math.min(page, totalPages));

        XpCategoryDetailHolder holder = new XpCategoryDetailHolder(category, safePage);
        String categoryName = getCategoryDisplayName(category);
        Component title = miniMessage.deserialize("<dark_gray><bold>XP: " + categoryName + "</bold></dark_gray>");
        Inventory inv = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inv);

        // Borders
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, "<gray> </gray>");
        ItemStack cyanBorder = createItem(Material.CYAN_STAINED_GLASS_PANE, "<dark_aqua>⚔</dark_aqua>");

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, border);
            }
        }
        inv.setItem(0, cyanBorder);
        inv.setItem(8, cyanBorder);
        inv.setItem(45, cyanBorder);
        inv.setItem(53, cyanBorder);

        // Grid slots (28 inner slots)
        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int startIndex = (safePage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(totalItems, startIndex + ITEMS_PER_PAGE);

        int slotIndex = 0;
        for (int i = startIndex; i < endIndex && slotIndex < slots.length; i++, slotIndex++) {
            GuideEntry entry = allEntries.get(i);
            addGuideCard(inv, slots[slotIndex], entry);
        }

        // Navigation Footer
        if (safePage > 1) {
            inv.setItem(47, createItem(Material.ARROW, "<yellow>« Prev</yellow>", "<gray>Halaman sebelumnya.</gray>"));
        }

        inv.setItem(48, createItem(Material.NETHER_STAR, "<aqua><bold>Profil</bold></aqua>", "<gray>Kembali ke profil.</gray>"));

        if (totalPages > 1) {
            inv.setItem(49, createItem(Material.BOOK,
                    "<gold><bold>Hal " + safePage + "/" + totalPages + "</bold></gold>",
                    "<gray>Total " + totalItems + " jenis</gray>"));
        } else {
            inv.setItem(49, createItem(Material.ARROW, "<yellow><bold>Direktori XP</bold></yellow>", "<gray>Kembali ke direktori.</gray>"));
        }

        inv.setItem(50, createItem(Material.CHEST, "<gold><bold>Hadiah</bold></gold>", "<gray>Buka menu hadiah level.</gray>"));

        if (safePage < totalPages) {
            inv.setItem(51, createItem(Material.ARROW, "<yellow>Next »</yellow>", "<gray>Halaman selanjutnya.</gray>"));
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
    }

    private String getCategoryDisplayName(String category) {
        return switch (category) {
            case "mining" -> "Mining";
            case "woodcutting" -> "Woodcutting";
            case "farming" -> "Farming";
            case "mobs" -> "Monster & Boss";
            case "fishing" -> "Fishing";
            case "cooking" -> "Cooking";
            case "brewing" -> "Brewing";
            case "enchanting" -> "Enchanting";
            case "exploration" -> "Exploration";
            default -> "Direktori";
        };
    }

    private void addGuideCard(Inventory inv, int slot, GuideEntry entry) {
        ItemStack item = new ItemStack(entry.material());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize("<gold>" + entry.name() + "</gold>"));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>XP: <gold>+" + entry.xp() + " XP</gold></gray>"));
            if (entry.note() != null && !entry.note().isEmpty()) {
                lore.add(miniMessage.deserialize("<dark_gray>" + entry.note() + "</dark_gray>"));
            }
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
    }

    private List<GuideEntry> getCategoryEntries(String category) {
        List<GuideEntry> list = new ArrayList<>();
        switch (category) {
            case "mobs" -> {
                // Apex Bosses
                list.add(new GuideEntry(Material.ENDER_DRAGON_SPAWN_EGG, "Ender Dragon", 500, "Apex Boss The End"));
                list.add(new GuideEntry(Material.WITHER_SPAWN_EGG, "Wither", 250, "Apex Boss Nether"));
                list.add(new GuideEntry(Material.WARDEN_SPAWN_EGG, "Warden", 200, "Deep Dark Boss"));
                list.add(new GuideEntry(Material.ELDER_GUARDIAN_SPAWN_EGG, "Elder Guardian", 80, "Ocean Monument Boss"));
                list.add(new GuideEntry(Material.RAVAGER_SPAWN_EGG, "Ravager", 60, "Raid Beast"));
                // Illagers & Raids
                list.add(new GuideEntry(Material.EVOKER_SPAWN_EGG, "Evoker", 40, "Illager Mage"));
                list.add(new GuideEntry(Material.PIGLIN_BRUTE_SPAWN_EGG, "Piglin Brute", 35, "Bastion Elite"));
                list.add(new GuideEntry(Material.BREEZE_SPAWN_EGG, "Breeze", 30, "Trial Chambers"));
                list.add(new GuideEntry(Material.SHULKER_SPAWN_EGG, "Shulker", 25, "End City"));
                list.add(new GuideEntry(Material.WITHER_SKELETON_SPAWN_EGG, "Wither Skeleton", 20, "Nether Fortress"));
                list.add(new GuideEntry(Material.WITCH_SPAWN_EGG, "Witch", 20, "Penyihir"));
                list.add(new GuideEntry(Material.IRON_GOLEM_SPAWN_EGG, "Iron Golem", 20, "Golem Desa"));
                list.add(new GuideEntry(Material.GHAST_SPAWN_EGG, "Ghast", 18, "Nether"));
                list.add(new GuideEntry(Material.ENDERMAN_SPAWN_EGG, "Enderman", 16, "Makhluk Ender"));
                list.add(new GuideEntry(Material.GUARDIAN_SPAWN_EGG, "Guardian", 15, "Ocean Monument"));
                list.add(new GuideEntry(Material.VINDICATOR_SPAWN_EGG, "Vindicator", 15, "Prajurit Kapak"));
                list.add(new GuideEntry(Material.ZOGLIN_SPAWN_EGG, "Zoglin", 15, "Zombi Hoglin"));
                list.add(new GuideEntry(Material.PHANTOM_SPAWN_EGG, "Phantom", 14, "Monster Malam"));
                list.add(new GuideEntry(Material.BLAZE_SPAWN_EGG, "Blaze", 12, "Nether Fortress"));
                list.add(new GuideEntry(Material.PILLAGER_SPAWN_EGG, "Pillager", 12, "Pemanah Illager"));
                list.add(new GuideEntry(Material.CREEPER_SPAWN_EGG, "Creeper", 12, "Monster Ledakan"));
                list.add(new GuideEntry(Material.HOGLIN_SPAWN_EGG, "Hoglin", 12, "Nether"));
                list.add(new GuideEntry(Material.BOGGED_SPAWN_EGG, "Bogged", 10, "Panah Beracun Rawa"));
                list.add(new GuideEntry(Material.ZOMBIE_VILLAGER_SPAWN_EGG, "Zombie Villager", 10, "Zombi Warga"));
                list.add(new GuideEntry(Material.DROWNED_SPAWN_EGG, "Drowned", 9, "Zombi Air"));
                list.add(new GuideEntry(Material.HUSK_SPAWN_EGG, "Husk", 9, "Zombi Gurun"));
                list.add(new GuideEntry(Material.STRAY_SPAWN_EGG, "Stray", 9, "Pemanah Salju"));
                list.add(new GuideEntry(Material.ZOMBIE_SPAWN_EGG, "Zombie", 8, "Mayat Hidup"));
                list.add(new GuideEntry(Material.SKELETON_SPAWN_EGG, "Skeleton", 8, "Kerangka Pemanah"));
                list.add(new GuideEntry(Material.CAVE_SPIDER_SPAWN_EGG, "Cave Spider", 8, "Laba-Laba Gua"));
                list.add(new GuideEntry(Material.PIGLIN_SPAWN_EGG, "Piglin", 8, "Prajurit Nether"));
                list.add(new GuideEntry(Material.ZOMBIFIED_PIGLIN_SPAWN_EGG, "Zombified Piglin", 8, "Zombi Piglin"));
                list.add(new GuideEntry(Material.SPIDER_SPAWN_EGG, "Spider", 6, "Laba-Laba"));
                list.add(new GuideEntry(Material.VEX_SPAWN_EGG, "Vex", 6, "Roh Pedang"));
                list.add(new GuideEntry(Material.MAGMA_CUBE_SPAWN_EGG, "Magma Cube", 5, "Lendir Nether"));
                list.add(new GuideEntry(Material.ENDERMITE_SPAWN_EGG, "Endermite", 5, "Parasit Ender"));
                list.add(new GuideEntry(Material.SLIME_SPAWN_EGG, "Slime", 4, "Lendir Hijau"));
                list.add(new GuideEntry(Material.SILVERFISH_SPAWN_EGG, "Silverfish", 3, "Serangga Batu"));
                list.add(new GuideEntry(Material.STRIDER_SPAWN_EGG, "Strider", 3, "Lahar Nether"));
                list.add(new GuideEntry(Material.COW_SPAWN_EGG, "Cow", 2, "Sapi"));
                list.add(new GuideEntry(Material.PIG_SPAWN_EGG, "Pig", 2, "Babi"));
                list.add(new GuideEntry(Material.SHEEP_SPAWN_EGG, "Sheep", 2, "Domba"));
                list.add(new GuideEntry(Material.CHICKEN_SPAWN_EGG, "Chicken", 1, "Ayam"));
            }
            case "mining" -> {
                list.add(new GuideEntry(Material.ANCIENT_DEBRIS, "Ancient Debris", 50, "Nether"));
                list.add(new GuideEntry(Material.DEEPSLATE_EMERALD_ORE, "Deepslate Emerald", 30, "Kedalaman"));
                list.add(new GuideEntry(Material.EMERALD_ORE, "Emerald Ore", 25, "Pegunungan"));
                list.add(new GuideEntry(Material.RAW_GOLD_BLOCK, "Raw Gold Block", 25, "Gumpalan Emas"));
                list.add(new GuideEntry(Material.DEEPSLATE_DIAMOND_ORE, "Deepslate Diamond", 20, "Kedalaman"));
                list.add(new GuideEntry(Material.DIAMOND_ORE, "Diamond Ore", 15, "Berlian"));
                list.add(new GuideEntry(Material.RAW_IRON_BLOCK, "Raw Iron Block", 15, "Gumpalan Besi"));
                list.add(new GuideEntry(Material.CRYING_OBSIDIAN, "Crying Obsidian", 15, "Obsidian Mistis"));
                list.add(new GuideEntry(Material.OBSIDIAN, "Obsidian", 12, "Batuan Vulkanik"));
                list.add(new GuideEntry(Material.DEEPSLATE_GOLD_ORE, "Deepslate Gold", 10, "Kedalaman"));
                list.add(new GuideEntry(Material.RAW_COPPER_BLOCK, "Raw Copper Block", 10, "Gumpalan Tembaga"));
                list.add(new GuideEntry(Material.GOLD_ORE, "Gold Ore", 8, "Emas Natural"));
                list.add(new GuideEntry(Material.DEEPSLATE_REDSTONE_ORE, "Deepslate Redstone", 7, "Kedalaman"));
                list.add(new GuideEntry(Material.DEEPSLATE_LAPIS_ORE, "Deepslate Lapis", 7, "Kedalaman"));
                list.add(new GuideEntry(Material.AMETHYST_CLUSTER, "Amethyst Cluster", 6, "Kristal Matang"));
                list.add(new GuideEntry(Material.REDSTONE_ORE, "Redstone Ore", 6, "Redstone"));
                list.add(new GuideEntry(Material.LAPIS_ORE, "Lapis Lazuli Ore", 6, "Lapis"));
                list.add(new GuideEntry(Material.DEEPSLATE_IRON_ORE, "Deepslate Iron", 6, "Kedalaman"));
                list.add(new GuideEntry(Material.IRON_ORE, "Iron Ore", 5, "Besi Natural"));
                list.add(new GuideEntry(Material.NETHER_GOLD_ORE, "Nether Gold", 5, "Nether"));
                list.add(new GuideEntry(Material.NETHER_QUARTZ_ORE, "Nether Quartz", 4, "Nether"));
                list.add(new GuideEntry(Material.DEEPSLATE_COAL_ORE, "Deepslate Coal", 4, "Kedalaman"));
                list.add(new GuideEntry(Material.DEEPSLATE_COPPER_ORE, "Deepslate Copper", 4, "Kedalaman"));
                list.add(new GuideEntry(Material.SEA_LANTERN, "Sea Lantern", 4, "Monumen Laut"));
                list.add(new GuideEntry(Material.COAL_ORE, "Coal Ore", 3, "Batu Bara"));
                list.add(new GuideEntry(Material.COPPER_ORE, "Copper Ore", 3, "Tembaga"));
                list.add(new GuideEntry(Material.DARK_PRISMARINE, "Dark Prismarine", 3, "Monumen Laut"));
                list.add(new GuideEntry(Material.GLOWSTONE, "Glowstone", 3, "Nether"));
                list.add(new GuideEntry(Material.PRISMARINE, "Prismarine", 2, "Monumen Laut"));
                list.add(new GuideEntry(Material.PRISMARINE_BRICKS, "Prismarine Bricks", 2, "Monumen Laut"));
                list.add(new GuideEntry(Material.STONE, "Stone", 1, "Batu Alam"));
                list.add(new GuideEntry(Material.DEEPSLATE, "Deepslate", 1, "Batu Dalam"));
                list.add(new GuideEntry(Material.GRANITE, "Granite", 1, "Batu Granit"));
                list.add(new GuideEntry(Material.DIORITE, "Diorite", 1, "Batu Diorit"));
                list.add(new GuideEntry(Material.ANDESITE, "Andesite", 1, "Batu Andesit"));
                list.add(new GuideEntry(Material.TUFF, "Tuff", 1, "Batu Tuff"));
                list.add(new GuideEntry(Material.CALCITE, "Calcite", 1, "Kalsit"));
                list.add(new GuideEntry(Material.BASALT, "Basalt", 1, "Nether"));
                list.add(new GuideEntry(Material.BLACKSTONE, "Blackstone", 1, "Nether"));
                list.add(new GuideEntry(Material.NETHERRACK, "Netherrack", 1, "Nether"));
                list.add(new GuideEntry(Material.END_STONE, "End Stone", 1, "The End"));
                list.add(new GuideEntry(Material.SANDSTONE, "Sandstone", 1, "Gurun"));
            }
            case "farming" -> {
                list.add(new GuideEntry(Material.SNIFFER_SPAWN_EGG, "Breeding Sniffer", 25, "Hewan Purba"));
                list.add(new GuideEntry(Material.CAMEL_SPAWN_EGG, "Breeding Unta", 15, "Gurun"));
                list.add(new GuideEntry(Material.PANDA_SPAWN_EGG, "Breeding Panda", 12, "Hutan Bambu"));
                list.add(new GuideEntry(Material.HORSE_SPAWN_EGG, "Breeding Kuda", 10, "Padang Rumput"));
                list.add(new GuideEntry(Material.TURTLE_SPAWN_EGG, "Breeding Penyu", 10, "Pantai"));
                list.add(new GuideEntry(Material.LLAMA_SPAWN_EGG, "Breeding Llama", 8, "Pegunungan"));
                list.add(new GuideEntry(Material.WOLF_SPAWN_EGG, "Breeding Serigala", 6, "Hewan Jinak"));
                list.add(new GuideEntry(Material.CAT_SPAWN_EGG, "Breeding Kucing", 6, "Hewan Jinak"));
                list.add(new GuideEntry(Material.COW_SPAWN_EGG, "Breeding Sapi", 5, "Ternak"));
                list.add(new GuideEntry(Material.SHEEP_SPAWN_EGG, "Breeding Domba", 5, "Ternak"));
                list.add(new GuideEntry(Material.PIG_SPAWN_EGG, "Breeding Babi", 5, "Ternak"));
                list.add(new GuideEntry(Material.CHICKEN_SPAWN_EGG, "Breeding Ayam", 3, "Ternak"));
                list.add(new GuideEntry(Material.FROG_SPAWN_EGG, "Breeding Katak", 3, "Rawa"));
                list.add(new GuideEntry(Material.TORCHFLOWER, "Panen Torchflower", 6, "Tanaman Purba"));
                list.add(new GuideEntry(Material.PITCHER_PLANT, "Panen Pitcher Plant", 6, "Tanaman Purba"));
                list.add(new GuideEntry(Material.NETHER_WART, "Panen Nether Wart", 5, "Nether"));
                list.add(new GuideEntry(Material.CHORUS_FLOWER, "Panen Chorus Flower", 5, "The End"));
                list.add(new GuideEntry(Material.COCOA_BEANS, "Panen Cocoa Beans", 4, "Jungle"));
                list.add(new GuideEntry(Material.WHEAT, "Panen Wheat", 3, "Tanaman Matang"));
                list.add(new GuideEntry(Material.CARROT, "Panen Carrot", 3, "Tanaman Matang"));
                list.add(new GuideEntry(Material.POTATO, "Panen Potato", 3, "Tanaman Matang"));
                list.add(new GuideEntry(Material.BEETROOT, "Panen Beetroot", 3, "Tanaman Matang"));
                list.add(new GuideEntry(Material.MELON, "Panen Melon", 3, "Buah Matang"));
                list.add(new GuideEntry(Material.PUMPKIN, "Panen Pumpkin", 3, "Labu Matang"));
                list.add(new GuideEntry(Material.SWEET_BERRIES, "Panen Sweet Berries", 2, "Semak Beri"));
                list.add(new GuideEntry(Material.GLOW_BERRIES, "Panen Glow Berries", 2, "Beri Gua"));
                list.add(new GuideEntry(Material.CHORUS_PLANT, "Panen Chorus Plant", 2, "The End"));
                list.add(new GuideEntry(Material.SUGAR_CANE, "Panen Sugar Cane", 1, "Tebu"));
                list.add(new GuideEntry(Material.CACTUS, "Panen Cactus", 1, "Kaktus"));
                list.add(new GuideEntry(Material.BAMBOO, "Panen Bamboo", 1, "Bambu"));
            }
            case "woodcutting" -> {
                list.add(new GuideEntry(Material.CRIMSON_STEM, "Crimson Stem", 4, "Kayu Merah Nether"));
                list.add(new GuideEntry(Material.WARPED_STEM, "Warped Stem", 4, "Kayu Biru Nether"));
                list.add(new GuideEntry(Material.JUNGLE_LOG, "Jungle Log", 3, "Hutan Tropis"));
                list.add(new GuideEntry(Material.DARK_OAK_LOG, "Dark Oak Log", 3, "Ek Gelap"));
                list.add(new GuideEntry(Material.MANGROVE_LOG, "Mangrove Log", 3, "Bakau Rawa"));
                list.add(new GuideEntry(Material.CHERRY_LOG, "Cherry Log", 3, "Sakura"));
                list.add(new GuideEntry(Material.ACACIA_LOG, "Acacia Log", 3, "Savana"));
                list.add(new GuideEntry(Material.PALE_OAK_LOG, "Pale Oak Log", 3, "Ek Pucat"));
                list.add(new GuideEntry(Material.OAK_LOG, "Oak Log", 2, "Pohon Ek"));
                list.add(new GuideEntry(Material.BIRCH_LOG, "Birch Log", 2, "Pohon Birch"));
                list.add(new GuideEntry(Material.SPRUCE_LOG, "Spruce Log", 2, "Cemara"));
                list.add(new GuideEntry(Material.BAMBOO_BLOCK, "Bamboo Block", 2, "Blok Bambu"));
                list.add(new GuideEntry(Material.MUSHROOM_STEM, "Mushroom Stem", 2, "Batang Jamur"));
                list.add(new GuideEntry(Material.RED_MUSHROOM_BLOCK, "Red Mushroom", 1, "Jamur Merah"));
                list.add(new GuideEntry(Material.BROWN_MUSHROOM_BLOCK, "Brown Mushroom", 1, "Jamur Cokelat"));
            }
            case "fishing" -> {
                list.add(new GuideEntry(Material.ENCHANTED_BOOK, "Enchanted Book", 50, "Harta Karun"));
                list.add(new GuideEntry(Material.NAUTILUS_SHELL, "Nautilus Shell", 45, "Harta Karun"));
                list.add(new GuideEntry(Material.SADDLE, "Saddle", 35, "Harta Karun"));
                list.add(new GuideEntry(Material.NAME_TAG, "Name Tag", 30, "Harta Karun"));
                list.add(new GuideEntry(Material.PUFFERFISH, "Pufferfish", 20, "Ikan Buntal"));
                list.add(new GuideEntry(Material.BOW, "Enchanted Bow", 20, "Harta Karun"));
                list.add(new GuideEntry(Material.TROPICAL_FISH, "Tropical Fish", 15, "Ikan Tropis"));
                list.add(new GuideEntry(Material.FISHING_ROD, "Enchanted Rod", 15, "Harta Karun"));
                list.add(new GuideEntry(Material.SALMON, "Raw Salmon", 10, "Ikan Salmon"));
                list.add(new GuideEntry(Material.COD, "Raw Cod", 8, "Ikan Kod"));
                list.add(new GuideEntry(Material.LILY_PAD, "Lily Pad", 5, "Teratai"));
                list.add(new GuideEntry(Material.TRIPWIRE_HOOK, "Tripwire Hook", 4, "Kait"));
                list.add(new GuideEntry(Material.LEATHER, "Leather", 3, "Kulit"));
                list.add(new GuideEntry(Material.LEATHER_BOOTS, "Leather Boots", 3, "Sepatu"));
                list.add(new GuideEntry(Material.INK_SAC, "Ink Sac", 3, "Tinta"));
                list.add(new GuideEntry(Material.BOWL, "Bowl", 2, "Mangkuk"));
                list.add(new GuideEntry(Material.STRING, "String", 2, "Benang"));
                list.add(new GuideEntry(Material.BONE, "Bone", 2, "Tulang"));
                list.add(new GuideEntry(Material.STICK, "Stick", 1, "Ranting"));
                list.add(new GuideEntry(Material.ROTTEN_FLESH, "Rotten Flesh", 1, "Daging Busuk"));
            }
            case "cooking" -> {
                list.add(new GuideEntry(Material.NETHERITE_SCRAP, "Netherite Scrap", 30, "Peleburan"));
                list.add(new GuideEntry(Material.GOLD_INGOT, "Gold Ingot", 6, "Peleburan"));
                list.add(new GuideEntry(Material.IRON_INGOT, "Iron Ingot", 4, "Peleburan"));
                list.add(new GuideEntry(Material.COOKED_BEEF, "Steak", 3, "Daging Sapi"));
                list.add(new GuideEntry(Material.COOKED_PORKCHOP, "Cooked Porkchop", 3, "Daging Babi"));
                list.add(new GuideEntry(Material.COOKED_MUTTON, "Cooked Mutton", 3, "Daging Domba"));
                list.add(new GuideEntry(Material.COOKED_SALMON, "Cooked Salmon", 3, "Ikan Salmon"));
                list.add(new GuideEntry(Material.COPPER_INGOT, "Copper Ingot", 2, "Peleburan"));
                list.add(new GuideEntry(Material.COOKED_CHICKEN, "Cooked Chicken", 2, "Daging Ayam"));
                list.add(new GuideEntry(Material.COOKED_COD, "Cooked Cod", 2, "Ikan Kod"));
                list.add(new GuideEntry(Material.COOKED_RABBIT, "Cooked Rabbit", 2, "Daging Kelinci"));
                list.add(new GuideEntry(Material.BAKED_POTATO, "Baked Potato", 2, "Kentang"));
                list.add(new GuideEntry(Material.DRIED_KELP, "Dried Kelp", 1, "Rumput Laut"));
                list.add(new GuideEntry(Material.CHARCOAL, "Charcoal", 1, "Arang"));
                list.add(new GuideEntry(Material.SMOOTH_STONE, "Smooth Stone", 1, "Batu"));
                list.add(new GuideEntry(Material.STONE_BRICKS, "Stone Bricks", 1, "Batu Bata"));
                list.add(new GuideEntry(Material.GLASS, "Glass", 1, "Kaca"));
                list.add(new GuideEntry(Material.TERRACOTTA, "Terracotta", 1, "Tanah Liat"));
            }
            case "brewing" -> {
                list.add(new GuideEntry(Material.ENCHANTED_GOLDEN_APPLE, "God Apple", 60, "Apel Emas Mistis"));
                list.add(new GuideEntry(Material.GOLDEN_APPLE, "Golden Apple", 20, "Apel Emas"));
                list.add(new GuideEntry(Material.POTION, "Turtle Master", 15, "Ramuan Kura-Kura"));
                list.add(new GuideEntry(Material.SPLASH_POTION, "Regeneration", 10, "Pemulihan"));
                list.add(new GuideEntry(Material.POTION, "Strength", 10, "Kekuatan"));
                list.add(new GuideEntry(Material.POTION, "Invisibility", 10, "Menghilang"));
                list.add(new GuideEntry(Material.SPLASH_POTION, "Healing", 8, "Penyembuhan"));
                list.add(new GuideEntry(Material.SPLASH_POTION, "Fire Resistance", 8, "Tahan Api"));
                list.add(new GuideEntry(Material.SPLASH_POTION, "Speed", 8, "Kecepatan"));
                list.add(new GuideEntry(Material.POTION, "Night Vision", 8, "Penglihatan Malam"));
                list.add(new GuideEntry(Material.POTION, "Water Breathing", 8, "Napas Air"));
                list.add(new GuideEntry(Material.SPLASH_POTION, "Slow Falling", 7, "Jatuh Perlahan"));
                list.add(new GuideEntry(Material.SPLASH_POTION, "Leaping", 7, "Lompatan"));
                list.add(new GuideEntry(Material.POTION, "Poison", 6, "Racun"));
                list.add(new GuideEntry(Material.POTION, "Harming", 6, "Kerusakan"));
                list.add(new GuideEntry(Material.POTION, "Awkward Potion", 3, "Ramuan Dasar"));
            }
            case "enchanting" -> {
                list.add(new GuideEntry(Material.ENCHANTED_BOOK, "Enchant Tier III", 35, "Meja Sihir (+2x level)"));
                list.add(new GuideEntry(Material.BOOK, "Enchant Tier II", 20, "Meja Sihir"));
                list.add(new GuideEntry(Material.LAPIS_LAZULI, "Enchant Tier I", 10, "Meja Sihir"));
                list.add(new GuideEntry(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, "Netherite Upgrade", 50, "Smithing Table"));
                list.add(new GuideEntry(Material.ANVIL, "Anvil Combine", 10, "Penggabungan Sihir"));
                list.add(new GuideEntry(Material.DAMAGED_ANVIL, "Anvil Repair", 6, "Perbaikan Alat"));
                list.add(new GuideEntry(Material.NAME_TAG, "Anvil Rename", 3, "Ganti Nama"));
                list.add(new GuideEntry(Material.GRINDSTONE, "Grindstone", 2, "Hapus Sihir"));
            }
            case "exploration" -> {
                list.add(new GuideEntry(Material.ELYTRA, "Elytra", 2, "Tiap 64 blok terbang"));
                list.add(new GuideEntry(Material.HEART_OF_THE_SEA, "Renang", 2, "Tiap 32 blok berenang"));
                list.add(new GuideEntry(Material.LEATHER_BOOTS, "Jalan / Lari", 1, "Tiap 16 blok jelajah"));
                list.add(new GuideEntry(Material.SADDLE, "Tunggangan", 1, "Tiap 32 blok berkendara"));
                list.add(new GuideEntry(Material.FEATHER, "Lompat Parkour", 1, "Cooldown 5 detik"));
            }
        }
        return list;
    }

    private ItemStack createCategoryCard(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(name));
            if (lore.length > 0) {
                List<Component> compLore = new ArrayList<>();
                for (String l : lore) {
                    compLore.add(miniMessage.deserialize(l));
                }
                meta.lore(compLore);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(name));
            if (lore.length > 0) {
                List<Component> compLore = new ArrayList<>();
                for (String l : lore) {
                    compLore.add(miniMessage.deserialize(l));
                }
                meta.lore(compLore);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    // =========================================================================
    // 3. EVENT LISTENER (INVENTORY HOLDER PATTERN)
    // =========================================================================
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory inv = event.getInventory();

        // 1. Check Main Hub Click
        if (inv.getHolder() instanceof XpGuideHubHolder) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot < 0 || slot >= 45) return;

            switch (slot) {
                case 10 -> openCategoryDetail(player, "mining", 1);
                case 11 -> openCategoryDetail(player, "woodcutting", 1);
                case 12 -> openCategoryDetail(player, "farming", 1);
                case 13 -> openCategoryDetail(player, "mobs", 1);
                case 20 -> openCategoryDetail(player, "fishing", 1);
                case 21 -> openCategoryDetail(player, "cooking", 1);
                case 22 -> openCategoryDetail(player, "brewing", 1);
                case 23 -> openCategoryDetail(player, "enchanting", 1);
                case 24 -> openCategoryDetail(player, "exploration", 1);
                case 39 -> plugin.getKingdomProfileGUI().open(player);
                case 40 -> player.closeInventory();
                case 41 -> plugin.getLevelRewardsGUI().open(player, 1);
            }
            return;
        }

        // 2. Check Category Detail Sub-Menu Click
        if (inv.getHolder() instanceof XpCategoryDetailHolder holder) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot < 0 || slot >= 54) return;

            String category = holder.getCategoryId();
            int page = holder.getPage();

            // Previous Page (Slot 47)
            if (slot == 47 && page > 1) {
                openCategoryDetail(player, category, page - 1);
                return;
            }

            // Back to Profile (Slot 48)
            if (slot == 48) {
                plugin.getKingdomProfileGUI().open(player);
                return;
            }

            // Back to XP Hub / Page Indicator (Slot 49)
            if (slot == 49) {
                open(player);
                return;
            }

            // Open Rewards (Slot 50)
            if (slot == 50) {
                plugin.getLevelRewardsGUI().open(player, 1);
                return;
            }

            // Next Page (Slot 51)
            if (slot == 51) {
                List<GuideEntry> entries = getCategoryEntries(category);
                int totalPages = Math.max(1, (int) Math.ceil((double) entries.size() / ITEMS_PER_PAGE));
                if (page < totalPages) {
                    openCategoryDetail(player, category, page + 1);
                }
            }
        }
    }
}
