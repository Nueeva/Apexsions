package com.apexsions.customenchants.listener;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

/**
 * Handles combining Custom Enchantment Books and Custom Enchanted Items in Anvils,
 * including level upgrading, complementary success/destroy rates, EXP costs, and player enchant limits.
 */
public class CustomAnvilListener implements Listener {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public CustomAnvilListener(ApexsionsCustomEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack first = inv.getItem(0);
        ItemStack second = inv.getItem(1);

        if (first == null || first.getType().isAir() || second == null || second.getType().isAir()) {
            return;
        }

        Player player = event.getView().getPlayer() instanceof Player p ? p : null;

        // Case 1: Custom Enchant Book + Custom Enchant Book
        if (plugin.getEnchantBookManager().isEnchantBook(first) && plugin.getEnchantBookManager().isEnchantBook(second)) {
            CustomEnchant e1 = plugin.getEnchantBookManager().getBookEnchant(first);
            CustomEnchant e2 = plugin.getEnchantBookManager().getBookEnchant(second);

            if (e1 != null && e2 != null && e1.getId().equalsIgnoreCase(e2.getId())) {
                int l1 = plugin.getEnchantBookManager().getBookLevel(first);
                int l2 = plugin.getEnchantBookManager().getBookLevel(second);

                int resultLvl;
                if (l1 == l2) {
                    resultLvl = Math.min(e1.getMaxLevel(), l1 + 1);
                } else {
                    resultLvl = Math.max(l1, l2);
                }

                int success = Math.max(plugin.getEnchantBookManager().getBookSuccess(first), plugin.getEnchantBookManager().getBookSuccess(second));
                int destroy = 100 - success;

                ItemStack resultBook = plugin.getEnchantBookManager().createBook(e1, resultLvl, success, destroy);
                int cost = Math.max(1, resultLvl * 2 + getTierBaseCost(e1.getGroup().getId()));

                event.setResult(resultBook);
                inv.setRepairCost(cost);
                return;
            }
        }

        // Case 2: Equipment + Custom Enchant Book
        if (CustomItemApplyListener.isEquipment(first) && plugin.getEnchantBookManager().isEnchantBook(second)) {
            CustomEnchant e = plugin.getEnchantBookManager().getBookEnchant(second);
            if (e != null && e.canApplyTo(first)) {
                // Check enchant limit if player is present
                if (player != null && !plugin.getEnchantLimitManager().canApplyEnchant(player, first, e)) {
                    event.setResult(new ItemStack(Material.AIR));
                    return;
                }

                int curLvl = plugin.getEnchantmentRegistry().getEnchantLevel(first, e);
                int bookLvl = plugin.getEnchantBookManager().getBookLevel(second);

                int targetLvl;
                if (curLvl > 0) {
                    if (curLvl == bookLvl) {
                        targetLvl = Math.min(e.getMaxLevel(), curLvl + 1);
                    } else {
                        targetLvl = Math.max(curLvl, bookLvl);
                    }
                } else {
                    targetLvl = bookLvl;
                }

                ItemStack result = plugin.getEnchantmentRegistry().applyEnchant(first.clone(), e, targetLvl);

                // Check rename
                String rename = inv.getRenameText();
                if (rename != null && !rename.isBlank()) {
                    ItemMeta meta = result.getItemMeta();
                    if (meta != null) {
                        meta.displayName(mm.deserialize(rename));
                        result.setItemMeta(meta);
                    }
                }

                int cost = Math.max(2, targetLvl * 2 + getTierBaseCost(e.getGroup().getId()));
                event.setResult(result);
                inv.setRepairCost(cost);
                return;
            }
        }

        // Case 3: Equipment + Equipment of same type
        if (CustomItemApplyListener.isEquipment(first) && CustomItemApplyListener.isEquipment(second) && first.getType() == second.getType()) {
            Map<CustomEnchant, Integer> secondEnchants = plugin.getEnchantmentRegistry().getEnchantsOnItem(second);
            if (!secondEnchants.isEmpty()) {
                ItemStack base = event.getResult() != null && !event.getResult().getType().isAir() ? event.getResult().clone() : first.clone();
                int addedCost = 0;

                for (Map.Entry<CustomEnchant, Integer> entry : secondEnchants.entrySet()) {
                    CustomEnchant e = entry.getKey();
                    int secLvl = entry.getValue();

                    if (player != null && !plugin.getEnchantLimitManager().canApplyEnchant(player, base, e)) {
                        continue; // Skip enchant if exceeds player's limit
                    }

                    int curLvl = plugin.getEnchantmentRegistry().getEnchantLevel(base, e);
                    int finalLvl;
                    if (curLvl > 0) {
                        if (curLvl == secLvl) {
                            finalLvl = Math.min(e.getMaxLevel(), curLvl + 1);
                        } else {
                            finalLvl = Math.max(curLvl, secLvl);
                        }
                    } else {
                        finalLvl = secLvl;
                    }

                    base = plugin.getEnchantmentRegistry().applyEnchant(base, e, finalLvl);
                    addedCost += finalLvl + getTierBaseCost(e.getGroup().getId());
                }

                // Check rename
                String rename = inv.getRenameText();
                if (rename != null && !rename.isBlank()) {
                    ItemMeta meta = base.getItemMeta();
                    if (meta != null) {
                        meta.displayName(mm.deserialize(rename));
                        base.setItemMeta(meta);
                    }
                }

                event.setResult(base);
                inv.setRepairCost(Math.max(1, inv.getRepairCost() + addedCost));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvilClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory() instanceof AnvilInventory anvil)) return;
        if (event.getRawSlot() != 2) return; // Raw slot 2 is Anvil Result slot

        ItemStack result = anvil.getItem(2);
        if (result == null || result.getType().isAir()) return;

        ItemStack first = anvil.getItem(0);
        ItemStack second = anvil.getItem(1);

        boolean involvesCustom = (first != null && plugin.getEnchantBookManager().isEnchantBook(first))
                || (second != null && plugin.getEnchantBookManager().isEnchantBook(second))
                || (first != null && !plugin.getEnchantmentRegistry().getEnchantsOnItem(first).isEmpty())
                || (second != null && !plugin.getEnchantmentRegistry().getEnchantsOnItem(second).isEmpty());

        if (!involvesCustom) return;

        // Player cannot take result if holding an item on cursor
        if (event.getCursor() != null && !event.getCursor().getType().isAir()) {
            return;
        }

        int cost = anvil.getRepairCost();
        if (player.getGameMode() != GameMode.CREATIVE && player.getLevel() < cost) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<red>Kamu membutuhkan setidaknya <gold>" + cost + " Level EXP</gold> untuk menggabungkan ini di anvil!</red>"));
            event.setCancelled(true);
            return;
        }

        // Deduct EXP level
        if (player.getGameMode() != GameMode.CREATIVE) {
            player.setLevel(player.getLevel() - cost);
        }

        // Consume slot 0
        anvil.setItem(0, null);

        // Consume slot 1
        if (second != null && second.getAmount() > 1) {
            second.setAmount(second.getAmount() - 1);
            anvil.setItem(1, second);
        } else {
            anvil.setItem(1, null);
        }

        // Deliver result to cursor
        anvil.setItem(2, null);
        event.getView().setCursor(result);
        event.setCancelled(true);

        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        player.updateInventory();
    }

    private int getTierBaseCost(String tierId) {
        if (tierId == null) return 2;
        return switch (tierId.toUpperCase()) {
            case "SIMPLE" -> 2;
            case "UNIQUE" -> 3;
            case "ELITE" -> 4;
            case "ULTIMATE" -> 5;
            case "LEGENDARY" -> 6;
            case "FABLED" -> 7;
            case "HEROIC" -> 8;
            default -> 3;
        };
    }
}
