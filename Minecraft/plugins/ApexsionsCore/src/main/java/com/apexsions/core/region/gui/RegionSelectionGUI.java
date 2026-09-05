package com.apexsions.core.region.gui;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.event.KingdomRegionChooseEvent;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import com.apexsions.core.region.gui.holder.RegionSelectHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Interactive, richly-styled inventory GUI for choosing a Kingdom region.
 */
public class RegionSelectionGUI implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<Integer, String> slotRegionMap = new HashMap<>();

    public RegionSelectionGUI(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        RegionSelectHolder holder = new RegionSelectHolder();
        String titleStr = plugin.getConfigManager().getGuiConfig().getString("kingdom-select.title", "<dark_gray><bold>⚔ PILIH KERAJAAN APEXSIONS ⚔</bold></dark_gray>");
        int size = plugin.getConfigManager().getGuiConfig().getInt("kingdom-select.size", 45);
        if (size % 9 != 0 || size < 27) size = 45;

        Component title = miniMessage.deserialize(titleStr);
        Inventory inventory = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inventory);
        this.slotRegionMap.clear();

        // 1. Fill decorative borders
        ItemStack darkGlass = createDecorativeGlass(Material.GRAY_STAINED_GLASS_PANE);
        ItemStack blackGlass = createDecorativeGlass(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack cyanAccent = createDecorativeGlass(Material.CYAN_STAINED_GLASS_PANE);

        for (int i = 0; i < size; i++) {
            inventory.setItem(i, darkGlass);
        }

        // Top and bottom row accents
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, blackGlass);
            inventory.setItem(size - 9 + i, blackGlass);
        }
        inventory.setItem(0, cyanAccent);
        inventory.setItem(8, cyanAccent);
        inventory.setItem(size - 9, cyanAccent);
        inventory.setItem(size - 1, cyanAccent);

        // Get player data to reflect current status
        Optional<PlayerData> playerDataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
        UUID currentRegionId = playerDataOpt.map(PlayerData::getRegionId).orElse(null);

        // 2. Populate Kingdom Cards
        ConfigurationSection regionsSec = plugin.getConfigManager().getSection("regions");
        if (regionsSec != null) {
            for (String key : regionsSec.getKeys(false)) {
                Optional<Region> regionOpt = plugin.getRegionManager().getRegion(key);
                if (regionOpt.isEmpty()) continue;

                Region region = regionOpt.get();
                int slot = regionsSec.getInt(key + ".slot", -1);
                String iconName = regionsSec.getString(key + ".icon", "EMERALD_BLOCK");
                String displayName = regionsSec.getString(key + ".display-name", region.getDisplayName());
                List<String> descLines = regionsSec.getStringList(key + ".description");

                Material material = Material.matchMaterial(iconName);
                if (material == null) material = Material.EMERALD_BLOCK;

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.displayName(miniMessage.deserialize(displayName));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.empty());

                    for (String line : descLines) {
                        lore.add(miniMessage.deserialize(line));
                    }

                    boolean isCurrent = currentRegionId != null && currentRegionId.equals(region.getId());
                    lore.add(Component.empty());
                    if (isCurrent) {
                        lore.add(miniMessage.deserialize("<green><bold>✔ Warga Resmi</bold></green>"));
                        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    } else if (currentRegionId != null) {
                        lore.add(miniMessage.deserialize("<red><bold>🔒 Terkunci</bold></red>"));
                    } else {
                        lore.add(miniMessage.deserialize("<gold><bold>» Klik untuk Memilih</bold></gold>"));
                    }

                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    meta.lore(lore);
                    item.setItemMeta(meta);
                }

                if (slot >= 0 && slot < size) {
                    inventory.setItem(slot, item);
                    slotRegionMap.put(slot, region.getKey());
                }
            }
        }

        // 3. Information Book
        int infoSlot = (size == 45) ? 31 : (size == 27 ? 22 : size - 5);
        ItemStack infoBook = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta infoMeta = infoBook.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(miniMessage.deserialize("<gold><bold>Informasi Kerajaan</bold></gold>"));
            List<Component> infoLore = new ArrayList<>();
            infoLore.add(miniMessage.deserialize("<gray>Pilih kerajaan untuk membuka gelar</gray>"));
            infoLore.add(miniMessage.deserialize("<gray>dan hadiah progres Level 1–100.</gray>"));
            infoMeta.lore(infoLore);
            infoBook.setItemMeta(infoMeta);
        }
        inventory.setItem(infoSlot, infoBook);

        // 4. Close Button
        int closeSlot = (size == 45) ? 40 : size - 1;
        ItemStack closeBtn = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeBtn.getItemMeta();
        if (closeMeta != null) {
            closeMeta.displayName(miniMessage.deserialize("<red><bold>Tutup</bold></red>"));
            closeBtn.setItemMeta(closeMeta);
        }
        inventory.setItem(closeSlot, closeBtn);

        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f, 1.2f);
    }

    private ItemStack createDecorativeGlass(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof RegionSelectHolder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();

        // Close button check
        int size = event.getInventory().getSize();
        int closeSlot = (size == 45) ? 40 : size - 1;
        if (slot == closeSlot) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        if (!slotRegionMap.containsKey(slot)) {
            return;
        }

        String regionKey = slotRegionMap.get(slot);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        new KingdomInfoGUI(plugin).open(player, regionKey);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof RegionSelectHolder) {
            event.setCancelled(true);
        }
    }
}
