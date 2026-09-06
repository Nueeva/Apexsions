package com.apexsions.crates.key;

import com.apexsions.crates.ApexsionsCratesPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class KeyManager {

    private final ApexsionsCratesPlugin plugin;
    private final Map<String, CrateKey> keys = new LinkedHashMap<>();
    private final NamespacedKey keyTag;

    public KeyManager(ApexsionsCratesPlugin plugin) {
        this.plugin = plugin;
        this.keyTag = new NamespacedKey(plugin, "crate_key_id");
    }

    public void loadKeys(FileConfiguration config) {
        keys.clear();
        ConfigurationSection section = config.getConfigurationSection("keys");
        if (section == null) return;

        for (String keyId : section.getKeys(false)) {
            ConfigurationSection sec = section.getConfigurationSection(keyId);
            if (sec == null) continue;

            String name = sec.getString("name", "Kunci " + keyId);
            Material mat = Material.matchMaterial(sec.getString("material", "TRIPWIRE_HOOK"));
            if (mat == null) mat = Material.TRIPWIRE_HOOK;
            boolean glowing = sec.getBoolean("glowing", true);
            int cmd = sec.getInt("custom-model-data", 0);
            List<String> lore = sec.getStringList("lore");

            CrateKey crateKey = new CrateKey(keyId.toLowerCase(), name, mat, glowing, cmd, lore);
            keys.put(keyId.toLowerCase(), crateKey);
        }

        plugin.getLogger().info("Loaded " + keys.size() + " crate keys from keys.yml.");
    }

    public Collection<CrateKey> getKeys() {
        return Collections.unmodifiableCollection(keys.values());
    }

    public CrateKey getKey(String id) {
        if (id == null) return null;
        return keys.get(id.toLowerCase());
    }

    public boolean isKeyItem(ItemStack item, String expectedKeyId) {
        if (item == null || !item.hasItemMeta()) return false;
        String id = item.getItemMeta().getPersistentDataContainer().get(keyTag, PersistentDataType.STRING);
        if (id == null) return false;
        return expectedKeyId == null || id.equalsIgnoreCase(expectedKeyId);
    }

    public String getKeyIdFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(keyTag, PersistentDataType.STRING);
    }

    public boolean hasPhysicalKey(Player player, String keyId, int amount) {
        if (player == null || keyId == null || amount <= 0) return false;
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (isKeyItem(item, keyId)) {
                count += item.getAmount();
                if (count >= amount) return true;
            }
        }
        return false;
    }

    public boolean takePhysicalKey(Player player, String keyId, int amount) {
        if (!hasPhysicalKey(player, keyId, amount)) return false;
        PlayerInventory inv = player.getInventory();
        int remaining = amount;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (isKeyItem(item, keyId)) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    remaining -= itemAmount;
                    inv.setItem(i, null);
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
                if (remaining <= 0) break;
            }
        }
        return true;
    }

    public void givePhysicalKey(Player player, String keyId, int amount) {
        CrateKey key = getKey(keyId);
        if (key == null || player == null || amount <= 0) return;

        ItemStack item = key.createItem(plugin, amount);
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            for (ItemStack left : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), left);
            }
        }
    }
}
