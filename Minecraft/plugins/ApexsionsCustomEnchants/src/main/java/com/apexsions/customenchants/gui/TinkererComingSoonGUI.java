package com.apexsions.customenchants.gui;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Aesthetic Coming Soon interface for Tinkerer feature.
 */
public class TinkererComingSoonGUI implements InventoryHolder {

    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public TinkererComingSoonGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 36, mm.deserialize("<gradient:#e67e22:#f39c12><bold>⚙ TINKERER — COMING SOON ⚙</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        player.openInventory(inventory);
    }

    private void buildGUI() {
        inventory.clear();
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        for (int i = 0; i < 36; i++) {
            inventory.setItem(i, border);
        }

        // Center showcase item (Slot 13)
        ItemStack showcase = createItem(Material.ANVIL, "<gradient:#f39c12:#f1c40f><bold>⚙ FITUR TINKERER SEDANG DIKEMBANGKAN ⚙</bold></gradient>", List.of(
                "<gray>Tinkerer akan segera hadir di pembaruan mendatang!</gray>",
                "",
                "<dark_gray>•</dark_gray> <yellow>Tukar item & armor lama dengan Secret Dust</yellow>",
                "<dark_gray>•</dark_gray> <yellow>Rombak buku sihir yang tidak terpakai</yellow>",
                "<dark_gray>•</dark_gray> <yellow>Daur ulang peralatan tempur kerajaan</yellow>",
                "",
                "<red><bold>Status:</bold> <yellow>Riset & Pengembangan Tim Dev</yellow></red>"
        ));
        inventory.setItem(13, showcase);

        // Back button slot 31
        ItemStack back = createItem(Material.ARROW, "<yellow><bold>◀ KEMBALI</bold></yellow>", List.of("<gray>Kembali ke menu sebelumnya.</gray>"));
        inventory.setItem(31, back);
    }

    private ItemStack createItem(Material mat, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            if (loreLines != null) {
                List<Component> cList = new ArrayList<>();
                for (String l : loreLines) {
                    cList.add(mm.deserialize(l));
                }
                meta.lore(cList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getRawSlot() == 31) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            player.closeInventory();
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
