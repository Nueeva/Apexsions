package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
import com.apexsions.customenchants.group.EnchantmentGroup;
import com.apexsions.economy.api.ApexsionsEconomyAPI;
import com.apexsions.economy.api.ApexsionsEconomyProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 54-Slot GUI enabling players to directly purchase specific custom enchant books
 * filtered by item category and rarity tabs, displayed as colored FIREWORK_STAR items.
 */
public class SpecificBookShopGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final String categoryFilter;
    private String rarityFilter = "ALL";
    private int page = 1;

    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<Integer, CustomEnchant> slotEnchantMap = new HashMap<>();

    public SpecificBookShopGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, String categoryFilter) {
        this.plugin = plugin;
        this.player = player;
        this.categoryFilter = categoryFilter != null ? categoryFilter.toUpperCase() : "ALL";
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#9b59b6:#3498db><bold>📚 TOKO SIHIR (" + this.categoryFilter + ") 📚</bold></gradient>"));
        buildGUI();
    }

    public SpecificBookShopGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, int page) {
        this(plugin, player, "ALL");
        this.page = page;
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotEnchantMap.clear();

        // 1. Top Filter Row (Slots 0..8)
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null, false);
        inventory.setItem(0, border);
        inventory.setItem(8, border);

        addRarityTab(1, "ALL", "<white><bold>SEMUA</bold></white>", Material.NETHER_STAR);
        addRarityTab(2, "SIMPLE", "<gray><bold>SIMPLE</bold></gray>", Material.GRAY_DYE);
        addRarityTab(3, "UNIQUE", "<green><bold>UNIQUE</bold></green>", Material.LIME_DYE);
        addRarityTab(4, "ELITE", "<blue><bold>ELITE</bold></blue>", Material.LIGHT_BLUE_DYE);
        addRarityTab(5, "ULTIMATE", "<gold><bold>ULTIMATE</bold></gold>", Material.YELLOW_DYE);
        addRarityTab(6, "LEGENDARY", "<gradient:#e67e22:#d35400><bold>LEGENDARY</bold></gradient>", Material.ORANGE_DYE);
        addRarityTab(7, "FABLED", "<gradient:#e74c3c:#c0392b><bold>FABLED</bold></gradient>", Material.RED_DYE);

        // 2. Filter enchants by category and rarity
        double multiplier = plugin.getSpecificBookMultiplier();
        int fixedSuccess = (int) plugin.getSpecificBookSuccessChance();

        List<CustomEnchant> filtered = new ArrayList<>();
        for (CustomEnchant e : plugin.getEnchantmentRegistry().getAllEnchantments()) {
            if (e.getGroup().isComingSoon()) continue;
            if (!e.isPurchasable()) continue;

            // Rarity filter
            if (!rarityFilter.equals("ALL") && !e.getGroup().getId().equalsIgnoreCase(rarityFilter)) {
                continue;
            }

            // Category filter
            if (!categoryFilter.equals("ALL")) {
                String target = e.getAppliesTo().toUpperCase();
                if (!matchesCategory(target, categoryFilter)) {
                    continue;
                }
            }

            filtered.add(e);
        }

        filtered.sort(Comparator.comparing(CustomEnchant::getDisplayName));

        int pageSize = 36;
        int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / pageSize));
        page = Math.max(1, Math.min(totalPages, page));

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, filtered.size());

        int currentSlot = 9;
        for (int i = startIndex; i < endIndex; i++) {
            CustomEnchant enchant = filtered.get(i);
            slotEnchantMap.put(currentSlot, enchant);

            EnchantmentGroup grp = enchant.getGroup();
            double specificPrice = grp.getCost() * multiplier;
            String formattedCost = grp.getCurrency().equalsIgnoreCase("diamond")
                    ? (long) specificPrice + " 💎"
                    : "Rp " + String.format("%,d", (long) specificPrice).replace(',', '.');

            ItemStack star = new ItemStack(Material.FIREWORK_STAR);
            FireworkEffectMeta meta = (FireworkEffectMeta) star.getItemMeta();
            if (meta != null) {
                FireworkEffect effect = FireworkEffect.builder()
                        .withColor(grp.getBukkitColor())
                        .build();
                meta.setEffect(effect);
                meta.displayName(mm.deserialize("<color:" + grp.getColor() + "><bold>" + enchant.getDisplayName() + " I</bold></color>"));

                List<Component> lore = new ArrayList<>();
                lore.add(mm.deserialize("<gray>" + enchant.getDescription() + "</gray>"));
                lore.add(Component.empty());
                lore.add(mm.deserialize("<gold>Tier:</gold> " + grp.getDisplayName()));
                lore.add(mm.deserialize("<gold>Berlaku Pada:</gold> <yellow>" + enchant.getAppliesTo() + "</yellow>"));
                lore.add(mm.deserialize("<gold>Maks Level:</gold> <yellow>" + CustomEnchant.toRoman(enchant.getMaxLevel()) + " (" + enchant.getMaxLevel() + ")</yellow>"));
                lore.add(Component.empty());
                lore.add(mm.deserialize("<green>● " + fixedSuccess + "% Success Rate (Pasti)</green>"));
                lore.add(mm.deserialize("<red>● " + (100 - fixedSuccess) + "% Destroy Rate</red>"));
                lore.add(Component.empty());
                lore.add(mm.deserialize("<gray>Harga Beli Langsung:</gray> <gold><bold>" + formattedCost + "</bold></gold>"));
                lore.add(mm.deserialize("<dark_gray>(3x lipat harga gacha acak tier)</dark_gray>"));
                lore.add(Component.empty());
                lore.add(mm.deserialize("<yellow>▶ Klik untuk membeli buku sihir ini!</yellow>"));

                meta.lore(lore);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                star.setItemMeta(meta);
            }

            inventory.setItem(currentSlot++, star);
        }

        while (currentSlot < 45) {
            inventory.setItem(currentSlot++, createItem(Material.GRAY_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null, false));
        }

        // Bottom Navigation Bar
        for (int s = 45; s < 54; s++) {
            inventory.setItem(s, border);
        }

        // Slot 45: Back to Category Selector
        inventory.setItem(45, createItem(Material.ARROW, "<gradient:#3498db:#2980b9><bold>⬅ PILIH KATEGORI ITEM</bold></gradient>", List.of(
                mm.deserialize("<gray>Kembali untuk memilih jenis armor/tools lain.</gray>")
        ), false));

        // Slot 48: Prev Page
        if (page > 1) {
            inventory.setItem(48, createItem(Material.ARROW, "<yellow><bold>◀ Halaman " + (page - 1) + "</bold></yellow>", null, false));
        }

        // Slot 49: Info Status
        inventory.setItem(49, createItem(Material.BOOK, "<gold><bold>Katalog Toko Sihir</bold></gold>", List.of(
                mm.deserialize("<gray>Kategori: <yellow>" + categoryFilter + "</yellow></gray>"),
                mm.deserialize("<gray>Rarity: <aqua>" + rarityFilter + "</aqua></gray>"),
                mm.deserialize("<gray>Ditemukan: <green>" + filtered.size() + " Sihir</green></gray>"),
                mm.deserialize("<gray>Halaman: <gold>" + page + " / " + totalPages + "</gold></gray>")
        ), false));

        // Slot 50: Next Page
        if (page < totalPages) {
            inventory.setItem(50, createItem(Material.ARROW, "<yellow><bold>Halaman " + (page + 1) + " ▶</bold></yellow>", null, false));
        }

        // Slot 53: Close
        inventory.setItem(53, createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>", null, false));
    }

    private boolean matchesCategory(String target, String category) {
        if (category.equals("ARMOR")) {
            return target.contains("ARMOR") || target.contains("HELMET") || target.contains("CHESTPLATE") || target.contains("LEGGING") || target.contains("BOOT");
        }
        if (category.equals("HELMET")) return target.contains("HELMET") || target.contains("ARMOR");
        if (category.equals("CHESTPLATE")) return target.contains("CHESTPLATE") || target.contains("ELYTRA") || target.contains("ARMOR");
        if (category.equals("LEGGINGS")) return target.contains("LEGGING") || target.contains("ARMOR");
        if (category.equals("BOOTS")) return target.contains("BOOT") || target.contains("ARMOR");

        if (category.equals("SWORD")) return target.contains("SWORD") || target.contains("WEAPON");
        if (category.equals("BOW")) return target.contains("BOW") || target.contains("CROSSBOW") || target.contains("WEAPON");
        if (category.equals("PICKAXE")) return target.contains("PICKAXE") || target.contains("TOOL");
        if (category.equals("AXE")) return target.contains("AXE") || target.contains("TOOL") || target.contains("WEAPON");
        if (category.equals("SHOVEL")) return target.contains("SHOVEL") || target.contains("HOE") || target.contains("TOOL");
        if (category.equals("FISHING")) return target.contains("FISHING") || target.contains("TRIDENT") || target.contains("SHEARS");

        return target.contains(category);
    }

    private void addRarityTab(int slot, String filterKey, String displayName, Material mat) {
        boolean isCurrent = rarityFilter.equalsIgnoreCase(filterKey);
        inventory.setItem(slot, createItem(mat, displayName + (isCurrent ? " <green>[AKTIF]</green>" : ""), List.of(
                mm.deserialize(isCurrent ? "<green>● Filter ini sedang aktif.</green>" : "<yellow>▶ Klik untuk menyaring tier " + filterKey + "</yellow>")
        ), isCurrent));
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        // 1. Rarity Tab Selection (Slots 1..7)
        if (slot >= 1 && slot <= 7) {
            String newFilter = switch (slot) {
                case 1 -> "ALL";
                case 2 -> "SIMPLE";
                case 3 -> "UNIQUE";
                case 4 -> "ELITE";
                case 5 -> "ULTIMATE";
                case 6 -> "LEGENDARY";
                case 7 -> "FABLED";
                default -> "ALL";
            };
            if (!this.rarityFilter.equals(newFilter)) {
                this.rarityFilter = newFilter;
                this.page = 1;
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                buildGUI();
            }
            return;
        }

        // 2. Navigation
        if (slot == 45) { // Back to Category Select
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            new ShopCategorySelectGUI(plugin, player).open();
            return;
        }

        if (slot == 48 && page > 1) {
            page--;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            buildGUI();
            return;
        }

        if (slot == 50) {
            page++;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            buildGUI();
            return;
        }

        if (slot == 53) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            player.closeInventory();
            return;
        }

        // 3. Purchase Enchant Book
        CustomEnchant enchant = slotEnchantMap.get(slot);
        if (enchant == null) return;

        EnchantmentGroup grp = enchant.getGroup();
        double multiplier = plugin.getSpecificBookMultiplier();
        double price = grp.getCost() * multiplier;
        boolean isDiamond = grp.getCurrency().equalsIgnoreCase("diamond");

        ApexsionsEconomyAPI eco = ApexsionsEconomyProvider.get();
        if (isDiamond) {
            int diaCount = 0;
            for (ItemStack is : player.getInventory().getContents()) {
                if (is != null && is.getType() == Material.DIAMOND) {
                    diaCount += is.getAmount();
                }
            }

            if (diaCount < price) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<red>Diamond kamu tidak mencukupi! Butuh <gold>" + (long) price + " 💎</gold>.</red>"));
                return;
            }

            // Deduct diamonds
            int toDeduct = (int) price;
            for (ItemStack is : player.getInventory().getContents()) {
                if (is != null && is.getType() == Material.DIAMOND) {
                    int take = Math.min(is.getAmount(), toDeduct);
                    is.setAmount(is.getAmount() - take);
                    toDeduct -= take;
                    if (toDeduct <= 0) break;
                }
            }
        } else {
            if (eco != null) {
                double bal = eco.getBalance(player.getUniqueId(), "RUPIAH");
                if (bal < price) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red>Saldo Rupiah kamu tidak mencukupi! Butuh <gold>Rp " + String.format("%,d", (long) price).replace(',', '.') + "</gold>.</red>"));
                    return;
                }
                eco.withdraw(player.getUniqueId(), "RUPIAH", price);
            }
        }

        // Give book with fixed success chance and auto-computed destroy rate (sum = 100%)
        int fixedSuccess = (int) plugin.getSpecificBookSuccessChance();
        int destroy = 100 - fixedSuccess;
        ItemStack book = plugin.getEnchantBookManager().createBook(enchant, 1, fixedSuccess, destroy);
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(book);
        if (!overflow.isEmpty()) {
            for (ItemStack drop : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.sendMessage(mm.deserialize("<green>✓ Berhasil membeli buku sihir spesifik <gold>" + enchant.getDisplayName() + " I</gold>!</green>"));
    }

    private ItemStack createItem(Material mat, String name, List<Component> lore, boolean glow) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            if (lore != null) meta.lore(lore);
            if (glow) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
