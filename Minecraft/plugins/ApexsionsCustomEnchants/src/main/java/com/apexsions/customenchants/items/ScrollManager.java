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

/**
 * Manager handling White Scrolls (destruction protection) and Black Scrolls (safe enchant extraction).
 */
public class ScrollManager {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final NamespacedKey keyWhiteScroll;
    private final NamespacedKey keyBlackScroll;
    private final NamespacedKey keyItemProtected;

    public ScrollManager(ApexsionsCustomEnchantsPlugin plugin) {
        this.plugin = plugin;
        this.keyWhiteScroll = new NamespacedKey(plugin, "white_scroll");
        this.keyBlackScroll = new NamespacedKey(plugin, "black_scroll");
        this.keyItemProtected = new NamespacedKey(plugin, "white_scroll_protected");
    }

    public ItemStack createWhiteScroll() {
        ItemStack scroll = new ItemStack(Material.PAPER);
        ItemMeta meta = scroll.getItemMeta();
        if (meta == null) return scroll;

        meta.displayName(mm.deserialize("<white><bold>🛡 WHITE SCROLL 🛡</bold></white>"));
        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<gray>Perlindungan mutlak dari kehancuran sihir.</gray>"));
        lore.add(Component.empty());
        lore.add(mm.deserialize("<yellow>Drag & drop gulungan ini ke senjata/armor</yellow>"));
        lore.add(mm.deserialize("<yellow>untuk melindunginya dari kehancuran jika tempa sihir gagal!</yellow>"));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(keyWhiteScroll, PersistentDataType.BYTE, (byte) 1);
        scroll.setItemMeta(meta);
        return scroll;
    }

    public ItemStack createBlackScroll() {
        ItemStack scroll = new ItemStack(Material.INK_SAC);
        ItemMeta meta = scroll.getItemMeta();
        if (meta == null) return scroll;

        meta.displayName(mm.deserialize("<dark_gray><bold>📜 BLACK SCROLL 📜</bold></dark_gray>"));
        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<gray>Ekstraksi sihir murni tanpa risiko.</gray>"));
        lore.add(Component.empty());
        lore.add(mm.deserialize("<yellow>Drag & drop ke item yang memiliki custom enchant</yellow>"));
        lore.add(mm.deserialize("<yellow>untuk mengekstrak 1 sihir menjadi Buku 100% Success!</yellow>"));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(keyBlackScroll, PersistentDataType.BYTE, (byte) 1);
        scroll.setItemMeta(meta);
        return scroll;
    }

    public boolean isWhiteScroll(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(keyWhiteScroll, PersistentDataType.BYTE);
    }

    public boolean isBlackScroll(ItemStack item) {
        if (item == null || item.getType() != Material.INK_SAC) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(keyBlackScroll, PersistentDataType.BYTE);
    }

    public boolean isProtectedByWhiteScroll(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(keyItemProtected, PersistentDataType.BYTE);
    }

    public ItemStack applyWhiteScrollProtection(ItemStack item) {
        if (item == null || item.getType().isAir()) return item;
        ItemStack out = item.clone();
        ItemMeta meta = out.getItemMeta();
        if (meta == null) return out;

        meta.getPersistentDataContainer().set(keyItemProtected, PersistentDataType.BYTE, (byte) 1);

        List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.add(mm.deserialize("<white><bold>🛡 DILINDUNGI WHITE SCROLL</bold></white>"));
        meta.lore(lore);

        out.setItemMeta(meta);
        return out;
    }

    public ItemStack removeWhiteScrollProtection(ItemStack item) {
        if (item == null || item.getType().isAir()) return item;
        ItemStack out = item.clone();
        ItemMeta meta = out.getItemMeta();
        if (meta == null) return out;

        meta.getPersistentDataContainer().remove(keyItemProtected);

        if (meta.hasLore() && meta.lore() != null) {
            List<Component> lore = new ArrayList<>();
            for (Component c : meta.lore()) {
                String s = MiniMessage.miniMessage().serialize(c);
                if (!s.contains("DILINDUNGI WHITE SCROLL")) {
                    lore.add(c);
                }
            }
            meta.lore(lore);
        }

        out.setItemMeta(meta);
        return out;
    }
}
