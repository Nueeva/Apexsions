package com.apexsions.customenchants.enchant;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.group.EnchantmentGroup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;

/**
 * Registry managing all active custom enchantments and item PDC persistence.
 */
public class EnchantmentRegistry {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<String, CustomEnchant> enchantments = new LinkedHashMap<>();
    private final NamespacedKey keyEnchantsContainer;

    public EnchantmentRegistry(ApexsionsCustomEnchantsPlugin plugin) {
        this.plugin = plugin;
        this.keyEnchantsContainer = new NamespacedKey(plugin, "custom_enchants");
        load();
    }

    public void load() {
        enchantments.clear();
        File file = new File(plugin.getDataFolder(), "enchantments.yml");
        if (!file.exists()) {
            plugin.saveResource("enchantments.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection sec = config.getConfigurationSection("enchantments");
        if (sec == null) return;

        for (String id : sec.getKeys(false)) {
            String dName = sec.getString(id + ".display-name", sec.getString(id + ".display", id));
            String groupStr = sec.getString(id + ".group", "SIMPLE");
            EnchantmentGroup group = plugin.getGroupRegistry().getGroup(groupStr);
            if (group == null) {
                group = plugin.getGroupRegistry().getGroup("SIMPLE");
            }

            int maxLvl = sec.getInt(id + ".max-level", 3);
            String applies = sec.getString(id + ".applies", "ALL");
            String desc = sec.getString(id + ".description", "Custom Enchantment");
            boolean purchasable = sec.getBoolean(id + ".purchasable", !id.equalsIgnoreCase("wings"));
            boolean enchantable = sec.getBoolean(id + ".enchantable", !id.equalsIgnoreCase("wings"));

            CustomEnchant enchant = new CustomEnchant(id, dName, group, maxLvl, applies, desc, purchasable, enchantable);
            enchantments.put(id.toLowerCase(), enchant);
        }

        plugin.getLogger().info("Loaded " + enchantments.size() + " custom enchantments.");
    }

    public CustomEnchant getEnchantment(String id) {
        if (id == null) return null;
        return enchantments.get(id.toLowerCase().trim());
    }

    public Collection<CustomEnchant> getAllEnchantments() {
        return Collections.unmodifiableCollection(enchantments.values());
    }

    public Set<String> getAllIds() {
        return Collections.unmodifiableSet(enchantments.keySet());
    }

    public List<CustomEnchant> getEnchantmentsByGroup(EnchantmentGroup group) {
        List<CustomEnchant> list = new ArrayList<>();
        for (CustomEnchant e : enchantments.values()) {
            if (e.getGroup().getId().equalsIgnoreCase(group.getId())) {
                list.add(e);
            }
        }
        return list;
    }

    public Map<CustomEnchant, Integer> getEnchantsOnItem(ItemStack item) {
        Map<CustomEnchant, Integer> map = new LinkedHashMap<>();
        if (item == null || item.getType().isAir()) return map;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return map;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        for (CustomEnchant enchant : enchantments.values()) {
            NamespacedKey key = new NamespacedKey(plugin, "ce_" + enchant.getId());
            if (pdc.has(key, PersistentDataType.INTEGER)) {
                int lvl = pdc.getOrDefault(key, PersistentDataType.INTEGER, 0);
                if (lvl > 0) {
                    map.put(enchant, lvl);
                }
            }
        }
        return map;
    }

    public int getEnchantLevel(ItemStack item, CustomEnchant enchant) {
        if (item == null || item.getType().isAir() || enchant == null) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        NamespacedKey key = new NamespacedKey(plugin, "ce_" + enchant.getId());
        return meta.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    public int getEnchantLevel(ItemStack item, String enchantId) {
        if (item == null || item.getType().isAir() || enchantId == null) return 0;
        CustomEnchant enchant = getEnchantment(enchantId);
        if (enchant == null) return 0;
        return getEnchantLevel(item, enchant);
    }

    public ItemStack applyEnchant(ItemStack item, CustomEnchant enchant, int level) {
        if (item == null || item.getType().isAir() || enchant == null) return item;
        ItemStack out = item.clone();
        ItemMeta meta = out.getItemMeta();
        if (meta == null) return out;

        NamespacedKey key = new NamespacedKey(plugin, "ce_" + enchant.getId());
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);

        // Update Lore
        rebuildItemLore(meta);
        out.setItemMeta(meta);
        return out;
    }

    public void applyEnchantDirect(ItemStack item, CustomEnchant enchant, int level) {
        if (item == null || item.getType().isAir() || enchant == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "ce_" + enchant.getId());
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);

        rebuildItemLore(meta);
        item.setItemMeta(meta);
    }

    public ItemStack removeEnchant(ItemStack item, CustomEnchant enchant) {
        if (item == null || item.getType().isAir() || enchant == null) return item;
        ItemStack out = item.clone();
        ItemMeta meta = out.getItemMeta();
        if (meta == null) return out;

        NamespacedKey key = new NamespacedKey(plugin, "ce_" + enchant.getId());
        meta.getPersistentDataContainer().remove(key);

        // Update Lore
        rebuildItemLore(meta);
        out.setItemMeta(meta);
        return out;
    }

    public void rebuildItemLore(ItemMeta meta) {
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        // 1. Gather existing lore lines that are NOT custom enchants or custom vanilla enchant lines
        List<Component> baseLore = new ArrayList<>();
        if (meta.hasLore() && meta.lore() != null) {
            for (Component c : meta.lore()) {
                String plain = MiniMessage.miniMessage().serialize(c);
                if (!isCustomOrVanillaEnchantLore(plain, meta)) {
                    baseLore.add(c);
                }
            }
        }

        List<Component> newLore = new ArrayList<>();

        // 2. Format vanilla enchants in full Roman numerals if any level > 10
        boolean hasHighLevelVanilla = meta.hasEnchants() && meta.getEnchants().values().stream().anyMatch(lvl -> lvl > 10);
        if (hasHighLevelVanilla) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                String vName = formatVanillaName(entry.getKey().getKey().getKey());
                String line = "<gray>" + vName + " " + CustomEnchant.toRoman(entry.getValue()) + "</gray>";
                newLore.add(mm.deserialize(line));
            }
        }

        // 3. Prepend custom enchant lore lines in full Roman numerals
        boolean hasAnyCustom = false;
        for (CustomEnchant enchant : enchantments.values()) {
            NamespacedKey key = new NamespacedKey(plugin, "ce_" + enchant.getId());
            if (pdc.has(key, PersistentDataType.INTEGER)) {
                int lvl = pdc.getOrDefault(key, PersistentDataType.INTEGER, 0);
                if (lvl > 0) {
                    hasAnyCustom = true;
                    String color = enchant.getGroup().getColor();
                    String line = "<color:" + color + ">" + enchant.getDisplayName() + " " + CustomEnchant.toRoman(lvl) + "</color>";
                    newLore.add(mm.deserialize(line));
                }
            }
        }

        // Append base lore
        newLore.addAll(baseLore);
        meta.lore(newLore);

        // Apply glowing enchantment glint shimmer
        if (hasAnyCustom || hasHighLevelVanilla) {
            meta.setEnchantmentGlintOverride(true);
        } else if (!meta.hasEnchants()) {
            meta.setEnchantmentGlintOverride(null);
        }
    }

    public ItemStack updateLoreAndGlint(ItemStack item) {
        if (item == null || item.getType().isAir()) return item;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        rebuildItemLore(meta);
        item.setItemMeta(meta);
        return item;
    }

    public static String formatVanillaName(String key) {
        String[] parts = key.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1).toLowerCase()).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private boolean isCustomOrVanillaEnchantLore(String plain, ItemMeta meta) {
        for (CustomEnchant enchant : enchantments.values()) {
            if (plain.contains(enchant.getDisplayName())) {
                return true;
            }
        }
        if (meta != null && meta.hasEnchants()) {
            for (Enchantment ve : meta.getEnchants().keySet()) {
                if (plain.contains(formatVanillaName(ve.getKey().getKey()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public NamespacedKey getKeyEnchantsContainer() {
        return keyEnchantsContainer;
    }
}
