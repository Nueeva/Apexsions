package com.apexsions.customenchants.listener;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Event listener managing drag-and-drop interactions for Enchant Books, Magic Dust, and Scrolls.
 */
public class CustomItemApplyListener implements Listener {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public CustomItemApplyListener(ApexsionsCustomEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (cursor == null || cursor.getType().isAir() || current == null || current.getType().isAir()) {
            return;
        }

        // 1. Magic Dust onto Enchantment Book
        if (plugin.getMagicDustManager().isMagicDust(cursor) && plugin.getEnchantBookManager().isEnchantBook(current)) {
            event.setCancelled(true);
            int rate = plugin.getMagicDustManager().getMagicDustRate(cursor);
            int curSuccess = plugin.getEnchantBookManager().getBookSuccess(current);
            if (curSuccess >= 100) {
                player.sendMessage(mm.deserialize("<red>Buku ini sudah mencapai batas maksimal 100% Success Rate!</red>"));
                return;
            }

            ItemStack updatedBook = plugin.getEnchantBookManager().updateBookSuccess(current, rate);
            event.setCurrentItem(updatedBook);

            decrementItem(player, cursor, event::setCursor);
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
            player.sendMessage(mm.deserialize("<green>✨ Magic Dust berhasil meningkatkan Success Rate buku sebesar <gold>+" + rate + "%</gold>!</green>"));
            return;
        }

        // 2. White Scroll onto Equipment
        if (plugin.getScrollManager().isWhiteScroll(cursor)) {
            if (isEquipment(current)) {
                event.setCancelled(true);
                if (plugin.getScrollManager().isProtectedByWhiteScroll(current)) {
                    player.sendMessage(mm.deserialize("<red>Item ini sudah dilindungi oleh White Scroll!</red>"));
                    return;
                }

                ItemStack protectedItem = plugin.getScrollManager().applyWhiteScrollProtection(current);
                event.setCurrentItem(protectedItem);

                decrementItem(player, cursor, event::setCursor);
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1.0f, 1.2f);
                player.sendMessage(mm.deserialize("<aqua>🛡 White Scroll berhasil diterapkan! Itemmu terlindungi dari 1x kehancuran sihir.</aqua>"));
                return;
            }
        }

        // 3. Black Scroll onto Enchanted Equipment
        if (plugin.getScrollManager().isBlackScroll(cursor)) {
            Map<CustomEnchant, Integer> enchants = plugin.getEnchantmentRegistry().getEnchantsOnItem(current);
            if (!enchants.isEmpty()) {
                event.setCancelled(true);
                // Pick the first custom enchant
                Map.Entry<CustomEnchant, Integer> entry = enchants.entrySet().iterator().next();
                CustomEnchant ench = entry.getKey();
                int lvl = entry.getValue();

                // Remove from item
                ItemStack stripped = plugin.getEnchantmentRegistry().removeEnchant(current, ench);
                event.setCurrentItem(stripped);

                // Give 100% success book
                ItemStack book = plugin.getEnchantBookManager().createBook(ench, lvl, 100, 0);
                decrementItem(player, cursor, event::setCursor);
                player.getInventory().addItem(book);

                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<dark_gray>📜 Black Scroll berhasil mengekstrak sihir <gold>" + ench.getDisplayName() + "</gold> menjadi buku 100% Success!</dark_gray>"));
                return;
            }
        }

        // 4. Enchantment Book onto Equipment
        if (plugin.getEnchantBookManager().isEnchantBook(cursor)) {
            CustomEnchant enchant = plugin.getEnchantBookManager().getBookEnchant(cursor);
            if (enchant == null) return;

            if (!enchant.canApplyTo(current)) {
                return; // Let vanilla behavior or do nothing
            }

            event.setCancelled(true);

            int bookLevel = plugin.getEnchantBookManager().getBookLevel(cursor);
            int currentLevel = plugin.getEnchantmentRegistry().getEnchantLevel(current, enchant);

            int targetLevel = bookLevel;
            if (currentLevel > 0) {
                if (currentLevel == bookLevel) {
                    targetLevel = Math.min(enchant.getMaxLevel(), currentLevel + 1);
                } else if (bookLevel < currentLevel) {
                    player.sendMessage(mm.deserialize("<red>Item ini sudah memiliki sihir " + enchant.getDisplayName() + " level lebih tinggi!</red>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
            }

            int successRate = plugin.getEnchantBookManager().getBookSuccess(cursor);
            int destroyRate = plugin.getEnchantBookManager().getBookDestroy(cursor);

            // Roll Success
            int roll = ThreadLocalRandom.current().nextInt(1, 101);
            if (roll <= successRate) {
                // SUCCESS!
                ItemStack enchantedItem = plugin.getEnchantmentRegistry().applyEnchant(current, enchant, targetLevel);
                event.setCurrentItem(enchantedItem);
                decrementItem(player, cursor, event::setCursor);

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                player.sendMessage(mm.deserialize("<green><bold>✓ BERHASIL!</bold> Sihir <gold>" + enchant.getDisplayName() + " " + CustomEnchant.toRoman(targetLevel) + "</gold> berhasil meresap ke dalam item!</green>"));
            } else {
                // FAILED!
                decrementItem(player, cursor, event::setCursor);

                // Roll Destroy
                int destroyRoll = ThreadLocalRandom.current().nextInt(1, 101);
                if (destroyRoll <= destroyRate) {
                    // Check White Scroll Protection
                    if (plugin.getScrollManager().isProtectedByWhiteScroll(current)) {
                        ItemStack savedItem = plugin.getScrollManager().removeWhiteScrollProtection(current);
                        event.setCurrentItem(savedItem);

                        player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
                        player.sendMessage(mm.deserialize("<aqua><bold>🛡 WHITE SCROLL AKTIF!</bold> Itemmu terlindungi dari kehancuran, namun White Scroll telah terpakai!</aqua>"));
                    } else {
                        // Destroyed
                        event.setCurrentItem(new ItemStack(Material.AIR));
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 0.8f);
                        player.sendMessage(mm.deserialize("<red><bold>☠ HANCUR!</bold> Energi sihir tidak terkendali dan item hancur berkeping-keping!</red>"));
                    }
                } else {
                    player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red><bold>✖ GAGAL!</bold> Penempaan sihir gagal, tetapi itemmu selamat dari kehancuran.</red>"));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (plugin.getMagicDustManager().isMysteryDust(item)) {
            event.setCancelled(true);
            item.setAmount(item.getAmount() - 1);

            ItemStack uncovered = plugin.getMagicDustManager().uncoverMysteryDust();
            player.getInventory().addItem(uncovered);
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
            player.sendMessage(mm.deserialize("<green>✨ Kamu membuka Mystery Dust dan menemukan item sihir!</green>"));
        }
    }

    private void decrementItem(Player player, ItemStack item, java.util.function.Consumer<ItemStack> setCursor) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            setCursor.accept(item);
        } else {
            setCursor.accept(new ItemStack(Material.AIR));
        }
    }

    private boolean isEquipment(ItemStack item) {
        if (item == null) return false;
        String name = item.getType().name();
        return name.endsWith("_SWORD") || name.endsWith("_AXE") || name.endsWith("_PICKAXE") ||
                name.endsWith("_SHOVEL") || name.endsWith("_HOE") || name.endsWith("_HELMET") ||
                name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") ||
                item.getType() == Material.BOW || item.getType() == Material.CROSSBOW || item.getType() == Material.ELYTRA;
    }
}
