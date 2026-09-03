package com.apexsions.media.creator.gui;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.creator.model.CreatorTier;
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

public class CreatorTiersGUI implements InventoryHolder {

    private final ApexsionsMediaPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public CreatorTiersGUI(ApexsionsMediaPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 36, mm.deserialize("<gradient:#f39c12:#f1c40f><bold>✦ DAFTAR TIER & REWARDS ✦</bold></gradient>"));
        render();
    }

    public void render() {
        inventory.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<gray> </gray>", List.of());
        for (int i = 0; i < 36; i++) {
            if (i < 9 || i >= 27 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        List<CreatorTier> tiers = plugin.getCreatorManager().getTiers();
        // Display in ascending requirement order for progressive viewing
        List<CreatorTier> ascTiers = new ArrayList<>(tiers);
        ascTiers.sort((a, b) -> Long.compare(a.getMinViews(), b.getMinViews()));

        int[] slots = {10, 12, 14, 16, 11, 13, 15};
        Material[] icons = {Material.COPPER_BLOCK, Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.NETHERITE_BLOCK};

        for (int i = 0; i < ascTiers.size() && i < slots.length; i++) {
            CreatorTier tier = ascTiers.get(i);
            Material icon = i < icons.length ? icons[i] : Material.EMERALD_BLOCK;

            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Syarat Minimal Statistik:</gray>"));
            lore.add(mm.deserialize(" <yellow>•</yellow> <white>Views:</white> <yellow>" + tier.getMinViews() + " Views</yellow>"));
            lore.add(mm.deserialize(" <yellow>•</yellow> <white>Likes:</white> <yellow>" + tier.getMinLikes() + " Likes</yellow>"));
            lore.add(mm.deserialize(""));
            lore.add(mm.deserialize("<gold>Hadiah & Keuntungan:</gold>"));

            if (tier.getPerksDescription() != null && !tier.getPerksDescription().isEmpty()) {
                for (String perk : tier.getPerksDescription()) {
                    lore.add(mm.deserialize(" <green>✓</green> " + perk));
                }
            } else {
                for (String cmd : tier.getRewards()) {
                    lore.add(mm.deserialize(" <green>✓</green> <gray>" + cmd + "</gray>"));
                }
            }

            inventory.setItem(slots[i], createItem(icon, tier.getDisplayName(), lore));
        }

        // Slot 27: Back to Creator Hub
        ItemStack backBtn = createItem(Material.ARROW, "<gradient:#3498db:#2980b9><bold>⬅ KEMBALI</bold></gradient>",
                List.of(mm.deserialize("<gray>Kembali ke Creator Verification Hub.</gray>")));
        inventory.setItem(27, backBtn);

        // Slot 35: Close
        ItemStack closeBtn = createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>",
                List.of(mm.deserialize("<gray>Klik untuk menutup menu.</gray>")));
        inventory.setItem(35, closeBtn);
    }

    public void handleClick(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        if (slot == 35) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        } else if (slot == 27) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            player.openInventory(new CreatorHubGUI(plugin, player).getInventory());
        }
    }

    private ItemStack createItem(Material material, String name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
