package com.apexsions.customenchants.listener;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
import com.apexsions.customenchants.gui.*;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Event listener managing drag-and-drop interactions for Enchant Books, Magic Dust, and Scrolls,
 * including bidirectional clicks, inventory update synchronization, and enchant limits.
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

        // Do not process in plugin browsing/shop GUIs
        if (event.getInventory().getHolder() instanceof EnchanterGUI ||
                event.getInventory().getHolder() instanceof SpecificBookShopGUI ||
                event.getInventory().getHolder() instanceof ShopCategorySelectGUI ||
                event.getInventory().getHolder() instanceof AdminPresetsGUI ||
                event.getInventory().getHolder() instanceof AdminTierPricingGUI ||
                event.getInventory().getHolder() instanceof AceAdminHubGUI ||
                event.getInventory().getHolder() instanceof AceEnchantsCatalogGUI ||
                event.getInventory().getHolder() instanceof AceBookLevelsSubGUI ||
                event.getInventory().getHolder() instanceof TinkererComingSoonGUI) {
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (cursor == null || cursor.getType().isAir() || current == null || current.getType().isAir()) {
            return;
        }

        // 1. Magic Dust onto Enchantment Book (Bidirectional)
        boolean cursorIsDust = plugin.getMagicDustManager().isMagicDust(cursor);
        boolean currentIsDust = plugin.getMagicDustManager().isMagicDust(current);
        boolean cursorIsBook = plugin.getEnchantBookManager().isEnchantBook(cursor);
        boolean currentIsBook = plugin.getEnchantBookManager().isEnchantBook(current);

        if ((cursorIsDust && currentIsBook) || (cursorIsBook && currentIsDust)) {
            event.setCancelled(true);
            ItemStack dustItem = cursorIsDust ? cursor : current;
            ItemStack bookItem = cursorIsDust ? current : cursor;

            int rate = plugin.getMagicDustManager().getMagicDustRate(dustItem);
            int curSuccess = plugin.getEnchantBookManager().getBookSuccess(bookItem);
            if (curSuccess >= 100) {
                player.sendMessage(mm.deserialize("<red>Buku ini sudah mencapai batas maksimal 100% Success Rate!</red>"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            ItemStack updatedBook = plugin.getEnchantBookManager().updateBookSuccess(bookItem, rate);
            if (cursorIsDust) {
                event.setCurrentItem(updatedBook);
                decrementCursor(cursor, event::setCursor);
            } else {
                event.getView().setCursor(updatedBook);
                decrementSlot(event, current);
            }

            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
            player.sendMessage(mm.deserialize("<green>✨ Magic Dust berhasil meningkatkan Success Rate buku sebesar <gold>+" + rate + "%</gold>!</green>"));
            player.updateInventory();
            return;
        }

        // 2. White Scroll onto Equipment (Bidirectional)
        boolean cursorIsWhite = plugin.getScrollManager().isWhiteScroll(cursor);
        boolean currentIsWhite = plugin.getScrollManager().isWhiteScroll(current);
        boolean cursorIsEquip = isEquipment(cursor);
        boolean currentIsEquip = isEquipment(current);

        if ((cursorIsWhite && currentIsEquip) || (cursorIsEquip && currentIsWhite)) {
            event.setCancelled(true);
            ItemStack scrollItem = cursorIsWhite ? cursor : current;
            ItemStack equipItem = cursorIsWhite ? current : cursor;

            if (plugin.getScrollManager().isProtectedByWhiteScroll(equipItem)) {
                player.sendMessage(mm.deserialize("<red>Item ini sudah dilindungi oleh White Scroll!</red>"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            ItemStack protectedItem = plugin.getScrollManager().applyWhiteScrollProtection(equipItem);
            if (cursorIsWhite) {
                event.setCurrentItem(protectedItem);
                decrementCursor(cursor, event::setCursor);
            } else {
                event.getView().setCursor(protectedItem);
                decrementSlot(event, current);
            }

            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1.0f, 1.2f);
            player.sendMessage(mm.deserialize("<aqua>🛡 White Scroll berhasil diterapkan! Itemmu terlindungi dari 1x kehancuran sihir.</aqua>"));
            player.updateInventory();
            return;
        }

        // 3. Black Scroll onto Enchanted Equipment (Bidirectional)
        boolean cursorIsBlack = plugin.getScrollManager().isBlackScroll(cursor);
        boolean currentIsBlack = plugin.getScrollManager().isBlackScroll(current);

        if ((cursorIsBlack && currentIsEquip) || (cursorIsEquip && currentIsBlack)) {
            ItemStack equipItem = cursorIsBlack ? current : cursor;
            Map<CustomEnchant, Integer> enchants = plugin.getEnchantmentRegistry().getEnchantsOnItem(equipItem);
            if (!enchants.isEmpty()) {
                event.setCancelled(true);
                // Pick the first custom enchant
                Map.Entry<CustomEnchant, Integer> entry = enchants.entrySet().iterator().next();
                CustomEnchant ench = entry.getKey();
                int lvl = entry.getValue();

                // Remove from item
                ItemStack stripped = plugin.getEnchantmentRegistry().removeEnchant(equipItem, ench);
                if (cursorIsBlack) {
                    event.setCurrentItem(stripped);
                    decrementCursor(cursor, event::setCursor);
                } else {
                    event.getView().setCursor(stripped);
                    decrementSlot(event, current);
                }

                // Give 100% success book
                ItemStack book = plugin.getEnchantBookManager().createBook(ench, lvl, 100, 0);
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(book);
                if (!overflow.isEmpty()) {
                    for (ItemStack drop : overflow.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    }
                }

                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<dark_gray>📜 Black Scroll berhasil mengekstrak sihir <gold>" + ench.getDisplayName() + "</gold> menjadi buku 100% Success!</dark_gray>"));
                player.updateInventory();
                return;
            }
        }

        // 4. Enchantment Book onto Equipment (Bidirectional)
        if ((cursorIsBook && currentIsEquip) || (cursorIsEquip && currentIsBook)) {
            ItemStack bookItem = cursorIsBook ? cursor : current;
            ItemStack equipItem = cursorIsBook ? current : cursor;

            CustomEnchant enchant = plugin.getEnchantBookManager().getBookEnchant(bookItem);
            if (enchant == null) return;

            if (!enchant.canApplyTo(equipItem)) {
                return; // Not compatible, let vanilla behavior
            }

            event.setCancelled(true);

            // Check custom enchant limits
            if (!plugin.getEnchantLimitManager().canApplyEnchant(player, equipItem, enchant)) {
                int limit = plugin.getEnchantLimitManager().getPlayerEnchantLimit(player);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<red><bold>✖ BATAS MAKSIMAL TERCAPAI!</bold> Item ini sudah mencapai batas maksimal sihirmu (<gold>" + limit + " custom enchant</gold>)!</red>"));
                return;
            }

            int bookLevel = plugin.getEnchantBookManager().getBookLevel(bookItem);
            int currentLevel = plugin.getEnchantmentRegistry().getEnchantLevel(equipItem, enchant);

            int targetLevel = bookLevel;
            if (currentLevel > 0) {
                if (currentLevel == bookLevel) {
                    if (currentLevel >= enchant.getMaxLevel()) {
                        player.sendMessage(mm.deserialize("<red>Sihir " + enchant.getDisplayName() + " pada item ini sudah mencapai level maksimal!</red>"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    }
                    targetLevel = Math.min(enchant.getMaxLevel(), currentLevel + 1);
                } else if (bookLevel < currentLevel) {
                    player.sendMessage(mm.deserialize("<red>Item ini sudah memiliki sihir " + enchant.getDisplayName() + " level lebih tinggi!</red>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
            }

            int successRate = plugin.getEnchantBookManager().getBookSuccess(bookItem);
            int destroyRate = plugin.getEnchantBookManager().getBookDestroy(bookItem);

            // Roll Success
            int roll = ThreadLocalRandom.current().nextInt(1, 101);
            if (roll <= successRate) {
                // SUCCESS!
                ItemStack enchantedItem = plugin.getEnchantmentRegistry().applyEnchant(equipItem, enchant, targetLevel);
                if (cursorIsBook) {
                    event.setCurrentItem(enchantedItem);
                    decrementCursor(cursor, event::setCursor);
                } else {
                    event.getView().setCursor(enchantedItem);
                    decrementSlot(event, current);
                }

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                player.sendMessage(mm.deserialize("<green><bold>✓ BERHASIL!</bold> Sihir <gold>" + enchant.getDisplayName() + " " + CustomEnchant.toRoman(targetLevel) + "</gold> berhasil meresap ke dalam item!</green>"));
            } else {
                // FAILED!
                if (cursorIsBook) {
                    decrementCursor(cursor, event::setCursor);
                } else {
                    decrementSlot(event, current);
                }

                // Roll Destroy
                int destroyRoll = ThreadLocalRandom.current().nextInt(1, 101);
                if (destroyRoll <= destroyRate) {
                    // Check White Scroll Protection
                    if (plugin.getScrollManager().isProtectedByWhiteScroll(equipItem)) {
                        ItemStack savedItem = plugin.getScrollManager().removeWhiteScrollProtection(equipItem);
                        if (cursorIsBook) {
                            event.setCurrentItem(savedItem);
                        } else {
                            event.getView().setCursor(savedItem);
                        }

                        player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
                        player.sendMessage(mm.deserialize("<aqua><bold>🛡 WHITE SCROLL AKTIF!</bold> Itemmu terlindungi dari kehancuran, namun White Scroll telah terpakai!</aqua>"));
                    } else {
                        // Destroyed
                        if (cursorIsBook) {
                            event.setCurrentItem(new ItemStack(Material.AIR));
                        } else {
                            event.getView().setCursor(new ItemStack(Material.AIR));
                        }
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 0.8f);
                        player.sendMessage(mm.deserialize("<red><bold>☠ HANCUR!</bold> Energi sihir tidak terkendali dan item hancur berkeping-keping!</red>"));
                    }
                } else {
                    player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red><bold>✖ GAGAL!</bold> Penempaan sihir gagal, tetapi itemmu selamat dari kehancuran.</red>"));
                }
            }
            player.updateInventory();
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() == null) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());
        if (item == null || item.getType().isAir()) return;

        if (plugin.getMagicDustManager().isMysteryDust(item)) {
            event.setCancelled(true);
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItem(event.getHand(), new ItemStack(Material.AIR));
            }

            ItemStack uncovered = plugin.getMagicDustManager().uncoverMysteryDust();
            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(uncovered);
            if (!overflow.isEmpty()) {
                for (ItemStack drop : overflow.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
            player.sendMessage(mm.deserialize("<green>✨ Kamu membuka Mystery Dust dan menemukan item sihir!</green>"));
            player.updateInventory();
        }
    }

    private void decrementCursor(ItemStack item, java.util.function.Consumer<ItemStack> setCursor) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            setCursor.accept(item);
        } else {
            setCursor.accept(new ItemStack(Material.AIR));
        }
    }

    private void decrementSlot(InventoryClickEvent event, ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            event.setCurrentItem(item);
        } else {
            event.setCurrentItem(new ItemStack(Material.AIR));
        }
    }

    public static boolean isEquipment(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        String name = item.getType().name();
        return name.endsWith("_SWORD") || name.endsWith("_AXE") || name.endsWith("_PICKAXE") ||
                name.endsWith("_SHOVEL") || name.endsWith("_HOE") || name.endsWith("_HELMET") ||
                name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") ||
                item.getType() == Material.BOW || item.getType() == Material.CROSSBOW || item.getType() == Material.TRIDENT ||
                item.getType() == Material.MACE || item.getType() == Material.FISHING_ROD || item.getType() == Material.SHEARS ||
                item.getType() == Material.SHIELD || item.getType() == Material.ELYTRA || item.getType() == Material.TURTLE_HELMET;
    }
}
