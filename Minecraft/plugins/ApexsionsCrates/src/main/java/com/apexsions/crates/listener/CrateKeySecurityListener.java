package com.apexsions.crates.listener;

import com.apexsions.crates.ApexsionsCratesPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class CrateKeySecurityListener implements Listener {

    private final ApexsionsCratesPlugin plugin;

    public CrateKeySecurityListener(ApexsionsCratesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Prevents players from placing crate keys on the ground (e.g. if key is Tripwire Hook, Chest, Lever).
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        String keyId = plugin.getKeyManager().getKeyIdFromItem(item);
        if (keyId != null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(
                    "<red>Kunci peti kerajaan tidak bisa diletakkan sebagai blok!</red>"
            ));
        }
    }

    /**
     * Prevents players from using crate keys in crafting recipes.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item != null && item.getType() != Material.AIR) {
                if (plugin.getKeyManager().getKeyIdFromItem(item) != null) {
                    event.getInventory().setResult(null);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item != null && item.getType() != Material.AIR) {
                if (plugin.getKeyManager().getKeyIdFromItem(item) != null) {
                    event.setCancelled(true);
                    if (event.getWhoClicked() instanceof Player player) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<red>Kunci peti tidak dapat digunakan sebagai bahan crafting!</red>"
                        ));
                    }
                    return;
                }
            }
        }
    }

    /**
     * Prevents modifying or renaming crate keys in anvils.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);

        if ((first != null && plugin.getKeyManager().getKeyIdFromItem(first) != null) ||
            (second != null && plugin.getKeyManager().getKeyIdFromItem(second) != null)) {
            event.setResult(null);
        }
    }
}
