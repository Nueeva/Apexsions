package com.apexsions.customenchants.items;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manager handling Mystery Dust, Magic Dust, and success rate boosting.
 */
public class MagicDustManager {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final NamespacedKey keyMysteryDust;
    private final NamespacedKey keyMagicDustRate;

    public MagicDustManager(ApexsionsCustomEnchantsPlugin plugin) {
        this.plugin = plugin;
        this.keyMysteryDust = new NamespacedKey(plugin, "mystery_dust");
        this.keyMagicDustRate = new NamespacedKey(plugin, "magic_dust_rate");
    }

    public ItemStack createMysteryDust() {
        ItemStack dust = new ItemStack(Material.SUGAR);
        ItemMeta meta = dust.getItemMeta();
        if (meta == null) return dust;

        meta.displayName(mm.deserialize("<gradient:#9b59b6:#3498db><bold>✦ MYSTERY DUST ✦</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<gray>Debu sihir misterius berkilau.</gray>"));
        lore.add(mm.deserialize("<yellow>Klik Kanan</yellow> <gray>untuk membuka isinya:</gray>"));
        lore.add(mm.deserialize("<dark_gray>•</dark_gray> <green>Magic Dust (+1% s/d +15% Success Rate)</green>"));
        lore.add(mm.deserialize("<dark_gray>•</dark_gray> <red>Failed Secret Dust</red>"));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(keyMysteryDust, PersistentDataType.BYTE, (byte) 1);
        dust.setItemMeta(meta);
        return dust;
    }

    public ItemStack createMagicDust(int bonusPercent) {
        int rate = Math.max(1, Math.min(25, bonusPercent));
        ItemStack dust = new ItemStack(Material.GLOWSTONE_DUST);
        ItemMeta meta = dust.getItemMeta();
        if (meta == null) return dust;

        meta.displayName(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>✨ MAGIC DUST (+" + rate + "%) ✨</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<green>● +" + rate + "% Success Rate Booster</green>"));
        lore.add(Component.empty());
        lore.add(mm.deserialize("<gray>Drag & drop debu ini ke atas</gray>"));
        lore.add(mm.deserialize("<gray>Buku Enchantment untuk menambah peluang sukses!</gray>"));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(keyMagicDustRate, PersistentDataType.INTEGER, rate);
        dust.setItemMeta(meta);
        return dust;
    }

    public ItemStack createFailedDust() {
        ItemStack dust = new ItemStack(Material.GUNPOWDER);
        ItemMeta meta = dust.getItemMeta();
        if (meta == null) return dust;

        meta.displayName(mm.deserialize("<dark_gray><bold>✖ FAILED SECRET DUST ✖</bold></dark_gray>"));
        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<red>Debu sihir ini telah kehilangan kekuatannya.</red>"));
        meta.lore(lore);

        dust.setItemMeta(meta);
        return dust;
    }

    public boolean isMysteryDust(ItemStack item) {
        if (item == null || item.getType() != Material.SUGAR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(keyMysteryDust, PersistentDataType.BYTE);
    }

    public boolean isMagicDust(ItemStack item) {
        if (item == null || item.getType() != Material.GLOWSTONE_DUST) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(keyMagicDustRate, PersistentDataType.INTEGER);
    }

    public int getMagicDustRate(ItemStack item) {
        if (!isMagicDust(item)) return 0;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(keyMagicDustRate, PersistentDataType.INTEGER, 0);
    }

    public ItemStack uncoverMysteryDust() {
        // 75% success chance to get Magic Dust, 25% to get failed dust
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (roll < 75) {
            int bonus = ThreadLocalRandom.current().nextInt(1, 16); // 1 to 15%
            return createMagicDust(bonus);
        } else {
            return createFailedDust();
        }
    }
}
