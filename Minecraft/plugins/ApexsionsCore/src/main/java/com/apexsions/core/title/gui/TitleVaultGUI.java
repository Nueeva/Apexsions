package com.apexsions.core.title.gui;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.title.TitleItem;
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

import java.util.*;

/**
 * 54-Slot Interactive Title Vault GUI for equipping prestige titles and honorific badges.
 */
public class TitleVaultGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<Integer, TitleItem> slotTitleMap = new HashMap<>();

    public TitleVaultGUI(ApexsionsCorePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#9b59b6:#3498db><bold>🏷️ KOLEKSI GELAR KEHORMATAN 🏷️</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotTitleMap.clear();

        ItemStack border = createGlass(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        ItemStack decor = createGlass(Material.PURPLE_STAINED_GLASS_PANE, "<light_purple>✦</light_purple>");

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }
        inventory.setItem(1, decor);
        inventory.setItem(7, decor);
        inventory.setItem(46, decor);
        inventory.setItem(52, decor);

        // Header Slot 4: Info Active Title
        PlayerData data = plugin.getPlayerDataService().getCached(player.getUniqueId()).orElse(null);
        String activeTitle = (data != null && data.getActiveTitle() != null) ? data.getActiveTitle() : "<gray>Tidak Ada (Polos)</gray>";

        ItemStack header = new ItemStack(Material.NAME_TAG);
        ItemMeta hMeta = header.getItemMeta();
        if (hMeta != null) {
            hMeta.displayName(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>👑 STATUS GELAR AKTIFMU 👑</bold></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Gelar Terpasang:</gray> " + activeTitle));
            lore.add(mm.deserialize("<gray>Gelar akan tampil di samping namamu di chat dan di atas kepala!</gray>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<yellow>Pilih salah satu gelar yang terbuka di bawah untuk memasangnya.</yellow>"));
            hMeta.lore(lore);
            header.setItemMeta(hMeta);
        }
        inventory.setItem(4, header);

        // Available Display Slots (Rows 2 to 4)
        int[] displaySlots = new int[]{
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        List<TitleItem> allTitles = new ArrayList<>(plugin.getTitleManager().getAllTitles());
        for (int i = 0; i < Math.min(displaySlots.length, allTitles.size()); i++) {
            int slot = displaySlots[i];
            TitleItem title = allTitles.get(i);
            slotTitleMap.put(slot, title);

            boolean unlocked = plugin.getTitleManager().isTitleUnlocked(player, title);
            boolean equipped = data != null && title.getDisplayName().equalsIgnoreCase(data.getActiveTitle());

            Material mat = equipped ? Material.ENCHANTED_BOOK : (unlocked ? Material.BOOK : Material.BARRIER);
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(mm.deserialize(title.getDisplayName()));
                List<Component> lore = new ArrayList<>();
                lore.add(mm.deserialize("<gray>" + title.getDescription() + "</gray>"));
                lore.add(Component.empty());

                if (title.getCondition() != null) {
                    lore.add(mm.deserialize(title.getCondition().getDescription()));
                }
                if (title.getPermission() != null && !title.getPermission().isBlank()) {
                    lore.add(mm.deserialize("<gray>Izin / Permission: <light_purple>" + title.getPermission() + "</light_purple></gray>"));
                }

                lore.add(Component.empty());
                if (equipped) {
                    lore.add(mm.deserialize("<green><bold>✔ SEDANG TERPASANG</bold></green>"));
                    lore.add(mm.deserialize("<yellow>▶ Klik untuk melepas gelar ini</yellow>"));
                } else if (unlocked) {
                    lore.add(mm.deserialize("<aqua><bold>★ TERBUKA (UNLOCKED)</bold></aqua>"));
                    lore.add(mm.deserialize("<yellow>▶ Klik untuk memasang gelar ini!</yellow>"));
                } else {
                    lore.add(mm.deserialize("<red><bold>🔒 TERKUNCI</bold></red>"));
                    lore.add(mm.deserialize("<gray>Tingkatkan level atau penuhi syarat di atas.</gray>"));
                }

                meta.lore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(slot, item);
        }

        // Slot 49: Close
        ItemStack closeItem = new ItemStack(Material.OAK_DOOR);
        ItemMeta cMeta = closeItem.getItemMeta();
        if (cMeta != null) {
            cMeta.displayName(mm.deserialize("<red><bold>◀ TUTUP VAULT</bold></red>"));
            closeItem.setItemMeta(cMeta);
        }
        inventory.setItem(49, closeItem);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        if (slotTitleMap.containsKey(slot)) {
            TitleItem title = slotTitleMap.get(slot);
            boolean unlocked = plugin.getTitleManager().isTitleUnlocked(player, title);

            if (!unlocked) {
                player.sendMessage(mm.deserialize("<red>🔒 Gelar ini masih terkunci! Anda memerlukan permission <yellow>" + title.getPermission() + "</yellow> atau memenuhi syarat di atas.</red>"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
                return;
            }

            PlayerData data = plugin.getPlayerDataService().getCached(player.getUniqueId()).orElse(null);
            if (data != null && title.getDisplayName().equalsIgnoreCase(data.getActiveTitle())) {
                // Unequip
                plugin.getTitleManager().unequipTitle(player);
                player.sendMessage(mm.deserialize("<yellow>Gelar kehormatan berhasil dilepas.</yellow>"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 1.2f);
            } else {
                // Equip
                plugin.getTitleManager().equipTitle(player, title);
                player.sendMessage(mm.deserialize("<green>✓ Kamu sekarang menggunakan gelar </green>" + title.getDisplayName() + "<green>!</green>"));
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.4f);
            }
            buildGUI();
        }
    }

    private ItemStack createGlass(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
