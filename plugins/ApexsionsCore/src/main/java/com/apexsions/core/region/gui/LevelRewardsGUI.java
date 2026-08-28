package com.apexsions.core.region.gui;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.level.reward.Reward;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.gui.holder.LevelRewardsHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * Deterministic Paginated 54-slot Level Reward GUI.
 * Milestone rewards (Level 11, 21, 31, 41, 51, 61, 71, 81, 91, 100) are ALWAYS
 * fixed at Slot 31 across all 10 pages, ensuring visual consistency without shifting.
 */
public class LevelRewardsGUI implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final int TOTAL_PAGES = 10;
    private static final int MILESTONE_SLOT = 31;

    // Normal reward slots (Row 2, 9 slots: 9..17)
    private static final int[] NORMAL_SLOTS = {9, 10, 11, 12, 13, 14, 15, 16, 17};

    public LevelRewardsGUI(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, int page) {
        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
        if (dataOpt.isEmpty()) return;

        PlayerData data = dataOpt.get();
        page = Math.max(1, Math.min(page, TOTAL_PAGES));

        LevelRewardsHolder holder = new LevelRewardsHolder(page);
        String titleStr = plugin.getConfigManager().getGuiConfig().getString("level-rewards.title", "<dark_gray><bold>Hadiah Level</bold></dark_gray>");
        Component title = miniMessage.deserialize(titleStr);
        Inventory inv = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inv);

        // 1. Fill decorative borders
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, "<gray> </gray>");
        ItemStack cyanBorder = createItem(Material.CYAN_STAINED_GLASS_PANE, "<dark_aqua>⚔</dark_aqua>");
        ItemStack goldAccent = createItem(Material.YELLOW_STAINED_GLASS_PANE, "<gold>★</gold>");

        for (int i = 0; i < 54; i++) {
            if (i < 9 || (i >= 18 && i <= 26) || (i >= 27 && i <= 35 && i != MILESTONE_SLOT && i != 30 && i != 32) || (i >= 36 && i <= 44) || i >= 45) {
                inv.setItem(i, border);
            }
        }
        inv.setItem(0, cyanBorder);
        inv.setItem(8, cyanBorder);
        inv.setItem(45, cyanBorder);
        inv.setItem(53, cyanBorder);

        // Gold accents flanking fixed milestone slot
        inv.setItem(30, goldAccent);
        inv.setItem(32, goldAccent);

        // 2. Player Progression Crest at Slot 4
        int level = data.getLevel();
        long xp = data.getXp();
        long nextXp = plugin.getLevelFormula().getXpForLevel(level + 1);
        int percent = (int) Math.min(100, Math.max(0, (xp * 100) / Math.max(1, nextXp)));
        String progressBar = buildProgressBar(percent);
        String levelTitle = plugin.getLevelManager().getLevelTitle(player.getUniqueId());
        int unclaimedCount = plugin.getRewardManager().getUnclaimedCount(data);

        ItemStack crest = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) crest.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            skullMeta.displayName(miniMessage.deserialize("<gold><bold>Status: " + player.getName() + "</bold></gold>"));
            List<Component> crestLore = new ArrayList<>();
            crestLore.add(miniMessage.deserialize("<gray>Level: <gold><bold>" + level + "</bold></gold> <dark_gray>/ 100</dark_gray></gray>"));
            crestLore.add(miniMessage.deserialize("<gray>Gelar: <yellow>" + levelTitle + "</yellow></gray>"));
            crestLore.add(miniMessage.deserialize("<gray>EXP: <yellow>" + xp + "</yellow><dark_gray>/</dark_gray><gold>" + (nextXp == Long.MAX_VALUE ? "MAX" : nextXp) + "</gold> <gray>(" + percent + "%)</gray></gray>"));
            crestLore.add(miniMessage.deserialize("<gray>Progres: </gray>" + progressBar));
            if (unclaimedCount > 0) {
                crestLore.add(miniMessage.deserialize("<green>⚡ " + unclaimedCount + " Hadiah Siap Diambil</green>"));
            } else {
                crestLore.add(miniMessage.deserialize("<gray>Hadiah: <green>Sudah Diambil</green></gray>"));
            }
            skullMeta.lore(crestLore);
            crest.setItemMeta(skullMeta);
        }
        inv.setItem(4, crest);

        // 3. Claim All Button at Slot 6
        if (unclaimedCount > 0) {
            ItemStack claimAll = createItem(Material.HOPPER,
                    "<green><bold>Klaim Semua (" + unclaimedCount + ")</bold></green>",
                    "<gray>Kumpulkan seluruh paket hadiah.</gray>",
                    "<green>» Klik untuk klaim semua</green>");
            ItemMeta cMeta = claimAll.getItemMeta();
            if (cMeta != null) {
                cMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
                cMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                claimAll.setItemMeta(cMeta);
            }
            inv.setItem(6, claimAll);
        } else {
            ItemStack allClaimed = createItem(Material.CHEST_MINECART,
                    "<gray>Klaim Semua</gray>",
                    "<gray>Tidak ada hadiah tertunda.</gray>");
            inv.setItem(6, allClaimed);
        }

        // 4. Render Normal Level Rewards (Slots 9..17)
        // Page 1: 2..10 (9 levels)
        // Page 2: 12..20 (9 levels)
        // Page P: (P-1)*10 + 2 .. (P-1)*10 + 10
        int startNormal = (page - 1) * 10 + 2;
        int endNormal = page == TOTAL_PAGES ? 99 : (page - 1) * 10 + 10;
        int milestoneLevel = page == TOTAL_PAGES ? 100 : (page - 1) * 10 + 11;

        int slotIdx = 0;
        for (int lvl = startNormal; lvl <= endNormal && slotIdx < NORMAL_SLOTS.length; lvl++, slotIdx++) {
            renderRewardAtSlot(inv, NORMAL_SLOTS[slotIdx], lvl, data);
        }

        // 5. Render Fixed Milestone Reward at Slot 31 on EVERY page
        renderMilestoneAtSlot(inv, MILESTONE_SLOT, milestoneLevel, data);

        // 6. Navigation & Shortcut Buttons (Row 6)
        if (page > 1) {
            inv.setItem(47, createItem(Material.ARROW, "<yellow>« Prev</yellow>", "<gray>Ke halaman " + (page - 1) + "</gray>"));
        }

        inv.setItem(48, createItem(Material.NETHER_STAR, "<aqua><bold>Profil</bold></aqua>", "<gray>Kembali ke profil.</gray>"));

        inv.setItem(49, createItem(Material.BOOK,
                "<gold><bold>Hal " + page + "/" + TOTAL_PAGES + "</bold></gold>",
                "<gray>Level " + startNormal + "–" + milestoneLevel + "</gray>"));

        inv.setItem(50, createItem(Material.KNOWLEDGE_BOOK, "<green><bold>Panduan XP</bold></green>", "<gray>Buka daftar perolehan XP.</gray>"));

        if (page < TOTAL_PAGES) {
            inv.setItem(51, createItem(Material.ARROW, "<yellow>Next »</yellow>", "<gray>Ke halaman " + (page + 1) + "</gray>"));
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
    }

    private void renderRewardAtSlot(Inventory inv, int slot, int lvl, PlayerData data) {
        Optional<Reward> rewardOpt = plugin.getRewardManager().getReward(lvl);
        if (rewardOpt.isEmpty()) return;

        Reward reward = rewardOpt.get();
        boolean isClaimed = data.isRewardClaimed(lvl);
        boolean isUnlocked = data.getLevel() >= lvl;

        Material iconMat = null;
        if (reward.getIcon() != null) {
            iconMat = Material.matchMaterial(reward.getIcon());
        }
        if (iconMat == null) {
            iconMat = Material.GOLD_NUGGET;
        }

        ItemStack item;
        if (isClaimed) {
            item = createRewardCard(iconMat,
                    "<green>✔ Level " + lvl + "</green>",
                    reward.getLore(),
                    "<gray>Sudah diambil</gray>", false);
        } else if (isUnlocked) {
            item = createRewardCard(iconMat,
                    "<yellow><bold>Level " + lvl + "</bold></yellow>",
                    reward.getLore(),
                    "<green>» Klik untuk klaim</green>", true);
        } else {
            item = createRewardCard(iconMat,
                    "<gray>🔒 Level " + lvl + "</gray>",
                    reward.getLore(),
                    "<red>Terkunci (Capai Lv." + lvl + ")</red>", false);
        }

        inv.setItem(slot, item);
    }

    private void renderMilestoneAtSlot(Inventory inv, int slot, int lvl, PlayerData data) {
        Optional<Reward> rewardOpt = plugin.getRewardManager().getReward(lvl);
        if (rewardOpt.isEmpty()) return;

        Reward reward = rewardOpt.get();
        boolean isClaimed = data.isRewardClaimed(lvl);
        boolean isUnlocked = data.getLevel() >= lvl;

        Material iconMat = Material.ENDER_CHEST;
        if (reward.getIcon() != null) {
            Material customMat = Material.matchMaterial(reward.getIcon());
            if (customMat != null) iconMat = customMat;
        }

        ItemStack item;
        if (isClaimed) {
            item = createRewardCard(iconMat,
                    "<green>✔ ★ Milestone Lv." + lvl + "</green>",
                    reward.getLore(),
                    "<gray>Hadiah istimewa sudah diambil</gray>", false);
        } else if (isUnlocked) {
            item = createRewardCard(iconMat,
                    "<gold><bold>★ MILESTONE LV." + lvl + " ★</bold></gold>",
                    reward.getLore(),
                    "<green>» KLIK KLAIM HADIAH ISTIMEWA «</green>", true);
        } else {
            item = createRewardCard(iconMat,
                    "<gray>🔒 ★ Milestone Lv." + lvl + "</gray>",
                    reward.getLore(),
                    "<red>Terkunci (Capai Milestone Lv." + lvl + ")</red>", false);
        }

        inv.setItem(slot, item);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof LevelRewardsHolder holder)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        int currentPage = holder.getPage();

        // 1. Claim All button (Slot 6)
        if (slot == 6) {
            int claimed = plugin.getRewardManager().claimAllAvailable(player);
            if (claimed > 0) {
                open(player, currentPage);
            }
            return;
        }

        // 2. Previous Page (Slot 47)
        if (slot == 47 && currentPage > 1) {
            open(player, currentPage - 1);
            return;
        }

        // 3. Back to Profile (Slot 48)
        if (slot == 48) {
            plugin.getKingdomProfileGUI().open(player);
            return;
        }

        // 4. XP Guide (Slot 50)
        if (slot == 50) {
            plugin.getXpGuideGUI().open(player);
            return;
        }

        // 5. Next Page (Slot 51)
        if (slot == 51 && currentPage < TOTAL_PAGES) {
            open(player, currentPage + 1);
            return;
        }

        // 6. Check Fixed Milestone Reward (Slot 31)
        if (slot == MILESTONE_SLOT) {
            int milestoneLevel = currentPage == TOTAL_PAGES ? 100 : (currentPage - 1) * 10 + 11;
            boolean claimed = plugin.getRewardManager().claimReward(player, milestoneLevel);
            if (claimed) {
                open(player, currentPage);
            }
            return;
        }

        // 7. Check Normal Level Reward Slots (Slots 9..17)
        for (int i = 0; i < NORMAL_SLOTS.length; i++) {
            if (slot == NORMAL_SLOTS[i]) {
                int targetLevel = (currentPage - 1) * 10 + 2 + i;
                if (targetLevel <= 100) {
                    boolean claimed = plugin.getRewardManager().claimReward(player, targetLevel);
                    if (claimed) {
                        open(player, currentPage);
                    }
                }
                return;
            }
        }
    }

    private ItemStack createRewardCard(Material mat, String title, List<String> loreLines, String statusLine, boolean glow) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(title));
            List<Component> lore = new ArrayList<>();
            if (loreLines != null) {
                for (String line : loreLines) {
                    lore.add(miniMessage.deserialize(line));
                }
            }
            if (statusLine != null && !statusLine.isEmpty()) {
                lore.add(miniMessage.deserialize(statusLine));
            }
            if (glow) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(name));
            if (lore.length > 0) {
                List<Component> compLore = new ArrayList<>();
                for (String l : lore) {
                    compLore.add(miniMessage.deserialize(l));
                }
                meta.lore(compLore);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String buildProgressBar(int percent) {
        int totalBars = 12;
        int filledBars = (int) Math.round((percent / 100.0) * totalBars);
        filledBars = Math.max(0, Math.min(totalBars, filledBars));
        int emptyBars = totalBars - filledBars;

        return "<gold>" + "■".repeat(filledBars) + "</gold><gray>" + "░".repeat(emptyBars) + "</gray> <yellow>" + percent + "%</yellow>";
    }
}
