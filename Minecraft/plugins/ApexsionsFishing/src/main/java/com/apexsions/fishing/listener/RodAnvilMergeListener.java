package com.apexsions.fishing.listener;

import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.service.AutoCatchRodManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class RodAnvilMergeListener implements Listener {

    private final ApexsionsFishing plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public RodAnvilMergeListener(ApexsionsFishing plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack first = inv.getItem(0);
        ItemStack second = inv.getItem(1);

        if (first == null || first.getType() != Material.FISHING_ROD || second == null || second.getType().isAir()) {
            return;
        }

        AutoCatchRodManager mgr = plugin.getRodManager();
        boolean firstIsAutoCatch = mgr.isAutoCatchRod(first);
        boolean secondIsAutoCatch = mgr.isAutoCatchRod(second);

        // Case: Neither is auto-catch rod, let vanilla handle
        if (!firstIsAutoCatch && !secondIsAutoCatch) {
            return;
        }

        // Determine the source rod that holds the auto-catch metadata
        ItemStack baseTemplate = firstIsAutoCatch ? first : second;
        ItemStack other = firstIsAutoCatch ? second : first;

        ItemStack result = first.clone();
        ItemMeta resultMeta = result.getItemMeta();
        ItemMeta templateMeta = baseTemplate.getItemMeta();

        if (resultMeta == null || templateMeta == null) return;

        // Copy all Auto-Catch PDC tags from baseTemplate to resultMeta
        PersistentDataContainer resPdc = resultMeta.getPersistentDataContainer();
        PersistentDataContainer tmplPdc = templateMeta.getPersistentDataContainer();

        if (tmplPdc.has(mgr.keyAutoCatch, PersistentDataType.BYTE)) {
            resPdc.set(mgr.keyAutoCatch, PersistentDataType.BYTE, tmplPdc.get(mgr.keyAutoCatch, PersistentDataType.BYTE));
        }
        if (tmplPdc.has(mgr.keyLuckBonus, PersistentDataType.DOUBLE)) {
            resPdc.set(mgr.keyLuckBonus, PersistentDataType.DOUBLE, tmplPdc.get(mgr.keyLuckBonus, PersistentDataType.DOUBLE));
        }
        if (tmplPdc.has(mgr.keyWeightBonus, PersistentDataType.DOUBLE)) {
            resPdc.set(mgr.keyWeightBonus, PersistentDataType.DOUBLE, tmplPdc.get(mgr.keyWeightBonus, PersistentDataType.DOUBLE));
        }
        if (tmplPdc.has(mgr.keyCatchSpeed, PersistentDataType.INTEGER)) {
            resPdc.set(mgr.keyCatchSpeed, PersistentDataType.INTEGER, tmplPdc.get(mgr.keyCatchSpeed, PersistentDataType.INTEGER));
        }
        if (tmplPdc.has(mgr.keyOrigin, PersistentDataType.STRING)) {
            resPdc.set(mgr.keyOrigin, PersistentDataType.STRING, tmplPdc.get(mgr.keyOrigin, PersistentDataType.STRING));
        }
        if (tmplPdc.has(mgr.keyRodId, PersistentDataType.STRING)) {
            resPdc.set(mgr.keyRodId, PersistentDataType.STRING, tmplPdc.get(mgr.keyRodId, PersistentDataType.STRING));
        }
        if (tmplPdc.has(mgr.keyMinLevel, PersistentDataType.INTEGER)) {
            resPdc.set(mgr.keyMinLevel, PersistentDataType.INTEGER, tmplPdc.get(mgr.keyMinLevel, PersistentDataType.INTEGER));
        }

        // Merge enchantments from second into result
        Map<Enchantment, Integer> toMerge = Map.of();
        if (other.getItemMeta() instanceof EnchantmentStorageMeta bookMeta) {
            toMerge = bookMeta.getStoredEnchants();
        } else if (other.hasItemMeta()) {
            toMerge = other.getItemMeta().getEnchants();
        }

        for (Map.Entry<Enchantment, Integer> entry : toMerge.entrySet()) {
            Enchantment ench = entry.getKey();
            int newLvl = entry.getValue();
            int curLvl = resultMeta.getEnchantLevel(ench);
            int finalLvl;
            if (curLvl > 0) {
                finalLvl = curLvl == newLvl ? Math.min(ench.getMaxLevel(), curLvl + 1) : Math.max(curLvl, newLvl);
            } else {
                finalLvl = newLvl;
            }
            resultMeta.addEnchant(ench, finalLvl, true);
        }

        // Repair damage if other is also fishing rod
        if (other.getType() == Material.FISHING_ROD && resultMeta instanceof Damageable resDmg && other.getItemMeta() instanceof Damageable otherDmg) {
            int maxDur = Material.FISHING_ROD.getMaxDurability();
            int curDmg = resDmg.getDamage();
            int otherDur = maxDur - otherDmg.getDamage();
            int bonus = (int) (maxDur * 0.12);
            int repairedDmg = Math.max(0, curDmg - (otherDur + bonus));
            resDmg.setDamage(repairedDmg);
        }

        // Handle rename if typed
        String renameText = inv.getRenameText();
        if (renameText != null && !renameText.isBlank()) {
            resultMeta.displayName(mm.deserialize(renameText));
        } else if (templateMeta.hasDisplayName()) {
            resultMeta.displayName(templateMeta.displayName());
        }

        // Preserve template lore if not set
        if (templateMeta.hasLore()) {
            resultMeta.lore(templateMeta.lore());
        }

        result.setItemMeta(resultMeta);
        event.setResult(result);
        inv.setRepairCost(Math.max(1, inv.getRepairCost()));
    }
}
