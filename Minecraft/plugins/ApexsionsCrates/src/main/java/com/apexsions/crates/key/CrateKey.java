package com.apexsions.crates.key;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class CrateKey {

    private final String id;
    private final String name;
    private final Material material;
    private final boolean glowing;
    private final int customModelData;
    private final List<String> lore;

    public CrateKey(String id, String name, Material material, boolean glowing, int customModelData, List<String> lore) {
        this.id = id;
        this.name = name != null ? name : "Kunci " + id;
        this.material = material != null ? material : Material.TRIPWIRE_HOOK;
        this.glowing = glowing;
        this.customModelData = customModelData;
        this.lore = lore != null ? lore : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Component getDisplayName() {
        return MiniMessage.miniMessage().deserialize(name);
    }

    public Material getMaterial() {
        return material;
    }

    public boolean isGlowing() {
        return glowing;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public List<String> getLore() {
        return lore;
    }

    public ItemStack createItem(Plugin plugin, int amount) {
        ItemStack item = new ItemStack(material, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(getDisplayName());
            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }

            List<Component> componentLore = new ArrayList<>();
            for (String l : lore) {
                componentLore.add(MiniMessage.miniMessage().deserialize(l));
            }
            meta.lore(componentLore);

            if (glowing) {
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            // Exploit-proof PersistentDataContainer tag
            NamespacedKey key = new NamespacedKey(plugin, "crate_key_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);

            item.setItemMeta(meta);
        }
        return item;
    }
}
