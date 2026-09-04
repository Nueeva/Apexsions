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
 * - 11 Pages:
 *   - Page 1: Level 2–10 (9 levels) on Row 4 (Slots 27..35).
 *   - Pages 2–9: Milestone Level 11, 21, ..., 81 on Row 3 Center (Slot 22). Next 9 levels on Row 4 (Slots 27..35).
 *   - Page 10: Milestone Level 91 on Row 3 Center (Slot 22). Levels 92–99 (8 levels) on Row 4 (Slots 27..34).
 *   - Page 11: Ultimate Level 100 Crown Altar with luxury presentation standing alone in Row 3 Center (Slot 22).
 * - Styling Rules:
 *   - Special Milestones (Lv 11..91): ENDER_CHEST (glowing when locked & ready, MINECART when claimed).
 *   - Level 100: NETHER_STAR with glorious altar (glowing when locked & ready, MINECART when claimed).
 *   - Regular Rewards: CHEST (wooden chest) with Paper glint override glowing effect when locked & ready, MINECART when claimed.
 *   - Unified reward details: Single cohesive "Hadiah:" section without duplicate headers.
 */
public class LevelRewardsGUI implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final int TOTAL_PAGES = 11;

    // Both special milestone and level 100 are now at Row 3 center (Slot 22)
    private static final int MILESTONE_SLOT = 22;       // Row 3 center (Baris ke-3 dari atas)
    private static final int FINAL_LEVEL_100_SLOT = 22;  // Row 3 center

    // Regular reward slots: Row 4 (Slots 27..35)
    private static final int[] NORMAL_SLOTS = {27, 28, 29, 30, 31, 32, 33, 34, 35};

    public LevelRewardsGUI(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, int page) {
        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
        if (dataOpt.isEmpty()) return;

        PlayerData data = dataOpt.get();
        page = Math.max(1, Math.min(page, TOTAL_PAGES));

        LevelRewardsHolder holder = new LevelRewardsHolder(page);
        String titleStr = page == 11
                ? "<gradient:#ffeaa7:#fdcb6e:#e17055:#d63031><bold>👑 TAHTA KERAJAAN: LEVEL 100 👑</bold></gradient>"
                : plugin.getConfigManager().getGuiConfig().getString("level-rewards.title", "<dark_gray><bold>Hadiah Level</bold></dark_gray>");
        Component title = miniMessage.deserialize(titleStr);
        Inventory inv = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inv);

        // 1. Fill entire inventory background with BLACK_STAINED_GLASS_PANE
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<gray> </gray>");
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, border);
        }

        // 2. Decorative accents based on page
        ItemStack goldAccent = createItem(Material.YELLOW_STAINED_GLASS_PANE, "<gold>★</gold>");
        ItemStack chainAccent = createItem(Material.CHAIN, "<dark_gray>⛓</dark_gray>");

        if (page >= 2 && page <= 10) {
            // Accent flanking Milestone at Slot 22 (Baris ke-3)
            inv.setItem(21, goldAccent);
            inv.setItem(23, goldAccent);
            inv.setItem(13, chainAccent);
        } else if (page == 11) {
            // =========================================================================
            // LUXURY LEVEL 100 IMPERIAL ALTAR FORMATION
            // =========================================================================
            ItemStack cyanBorder = createItem(Material.CYAN_STAINED_GLASS_PANE, "<aqua>⚡ TAHTA ABADI ⚡</aqua>");
            inv.setItem(0, cyanBorder);
            inv.setItem(8, cyanBorder);
            inv.setItem(45, cyanBorder);
            inv.setItem(53, cyanBorder);

            // Radiant Halo around Slot 22
            inv.setItem(13, createItem(Material.BEACON, "<gradient:#f1c40f:#e67e22><bold>✦ CAHAYA KEJAYAAN ✦</bold></gradient>",
                    "<gray>Simbol puncak supremasi kerajaan tertinggi.</gray>"));
            inv.setItem(21, createItem(Material.GOLD_BLOCK, "<gold><bold>★ SINGGASANA EMAS ★</bold></gold>",
                    "<gray>Pilar pelindung tahta Level 100.</gray>"));
            inv.setItem(23, createItem(Material.GOLD_BLOCK, "<gold><bold>★ SINGGASANA EMAS ★</bold></gold>",
                    "<gray>Pilar pelindung tahta Level 100.</gray>"));
            inv.setItem(31, createItem(Material.CRYING_OBSIDIAN, "<gradient:#9b59b6:#8e44ad><bold>⚡ RELIK KEABADIAN ⚡</bold></gradient>",
                    "<gray>Pondasi kekuatan mistis tanpa batas.</gray>"));

            // Star accents on diagonals
            inv.setItem(12, createItem(Material.PURPLE_STAINED_GLASS_PANE, "<light_purple>✦</light_purple>"));
            inv.setItem(14, createItem(Material.PURPLE_STAINED_GLASS_PANE, "<light_purple>✦</light_purple>"));
            inv.setItem(30, createItem(Material.PURPLE_STAINED_GLASS_PANE, "<light_purple>✦</light_purple>"));
            inv.setItem(32, createItem(Material.PURPLE_STAINED_GLASS_PANE, "<light_purple>✦</light_purple>"));
        } else {
            // Page 1: Decorative chain on row 3 center
            inv.setItem(22, chainAccent);
            inv.setItem(13, chainAccent);
        }

        // 3. Player Progression Crest at Slot 4
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
            String crestName = page == 11
                    ? "<gradient:#ffeaa7:#fdcb6e:#d63031><bold>👑 KAISAR AGUNG: " + player.getName() + " 👑</bold></gradient>"
                    : "<gold><bold>Status: " + player.getName() + "</bold></gold>";
            skullMeta.displayName(miniMessage.deserialize(crestName));

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

        // 4. Claim All Button at Slot 6
        if (unclaimedCount > 0) {
            ItemStack claimAll = createItem(Material.HOPPER,
                    "<green><bold>Klaim Semua (" + unclaimedCount + ")</bold></green>",
                    "<gray>Kumpulkan seluruh paket hadiah yang terbuka.</gray>",
                    "<green>» Klik untuk klaim semua</green>");
            ItemMeta cMeta = claimAll.getItemMeta();
            if (cMeta != null) {
                try {
                    cMeta.setEnchantmentGlintOverride(true);
                } catch (Throwable ignored) {}
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

        // 5. Render Normal Level Rewards on Row 4 (Slots 27..35)
        if (page <= 10) {
            int startNormal = (page - 1) * 10 + 2;
            int endNormal = page == 10 ? 99 : (page - 1) * 10 + 10;

            int slotIdx = 0;
            for (int lvl = startNormal; lvl <= endNormal && slotIdx < NORMAL_SLOTS.length; lvl++, slotIdx++) {
                renderRewardAtSlot(inv, NORMAL_SLOTS[slotIdx], lvl, data);
            }
        }

        // 6. Render Milestone Rewards on Row 3 Center (Slot 22)
        if (page >= 2 && page <= 10) {
            // Milestone on Row 3 Center (Slot 22): Levels 11, 21, ..., 91 (ENDER_CHEST)
            int milestoneLevel = (page - 1) * 10 + 1;
            renderMilestoneAtSlot(inv, MILESTONE_SLOT, milestoneLevel, data, false);
        } else if (page == 11) {
            // Page 11: Level 100 alone on Row 3 Center (Slot 22) (NETHER_STAR)
            renderMilestoneAtSlot(inv, FINAL_LEVEL_100_SLOT, 100, data, true);
        }

        // 7. Navigation Controls (Row 6)
        if (page > 1) {
            inv.setItem(47, createItem(Material.ARROW, "<yellow>« Prev</yellow>", "<gray>Ke halaman " + (page - 1) + "</gray>"));
        }

        inv.setItem(48, createItem(Material.NETHER_STAR, "<aqua><bold>Profil</bold></aqua>", "<gray>Kembali ke profil kerajaan.</gray>"));

        String pageRange;
        if (page == 1) {
            pageRange = "Level 2–10";
        } else if (page <= 9) {
            int m = (page - 1) * 10 + 1;
            pageRange = "Level " + m + "–" + (m + 9);
        } else if (page == 10) {
            pageRange = "Level 91–99";
        } else {
            pageRange = "Level 100 (Puncak)";
        }

        inv.setItem(49, createItem(Material.BOOK,
                "<gold><bold>Hal " + page + "/" + TOTAL_PAGES + "</bold></gold>",
                "<gray>" + pageRange + "</gray>"));

        inv.setItem(50, createItem(Material.KNOWLEDGE_BOOK, "<green><bold>Panduan XP</bold></green>", "<gray>Buka daftar perolehan XP.</gray>"));

        if (page < TOTAL_PAGES) {
            inv.setItem(51, createItem(Material.ARROW, "<yellow>Next »</yellow>", "<gray>Ke halaman " + (page + 1) + "</gray>"));
        }

        inv.setItem(53, createItem(Material.BARRIER, "<red><bold>✖ Tutup</bold></red>", "<gray>Tutup menu.</gray>"));

        player.openInventory(inv);
        if (page == 11) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);
        } else {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
        }
    }

    private void renderRewardAtSlot(Inventory inv, int slot, int lvl, PlayerData data) {
        Optional<Reward> rewardOpt = plugin.getRewardManager().getReward(lvl);
        if (rewardOpt.isEmpty()) return;

        Reward reward = rewardOpt.get();
        boolean isClaimed = data.isRewardClaimed(lvl);
        boolean isUnlocked = data.getLevel() >= lvl;

        List<String> details = buildRewardItemDetails(reward, false);

        ItemStack item;
        if (isClaimed) {
            // Pas kebuka: minecart (tanpa glow)
            item = createRewardCard(Material.MINECART,
                    "<gray>✔ Level " + lvl + " (Selesai)</gray>",
                    details,
                    "<gray>Hadiah sudah diambil</gray>", false);
        } else if (isUnlocked) {
            // Bisa diklaim: chest kayu dengan efek glowing
            item = createRewardCard(Material.CHEST,
                    "<green><bold>✔ Level " + lvl + " (Bisa Diklaim)</bold></green>",
                    details,
                    "<green><bold>» KLIK UNTUK KLAIM «</bold></green>", true);
        } else {
            // Pas kekunci: chest kayu dengan EFEK GLOWING
            item = createRewardCard(Material.CHEST,
                    "<red>🔒 Level " + lvl + "</red>",
                    details,
                    "<red>Terkunci — Capai Level " + lvl + "</red>", true);
        }

        inv.setItem(slot, item);
    }

    private void renderMilestoneAtSlot(Inventory inv, int slot, int lvl, PlayerData data, boolean isFinalLevel100) {
        Optional<Reward> rewardOpt = plugin.getRewardManager().getReward(lvl);
        if (rewardOpt.isEmpty()) return;

        Reward reward = rewardOpt.get();
        boolean isClaimed = data.isRewardClaimed(lvl);
        boolean isUnlocked = data.getLevel() >= lvl;

        List<String> details = buildRewardItemDetails(reward, true);

        // Special: ENDER_CHEST, Level 100: NETHER_STAR
        Material icon = isFinalLevel100 ? Material.NETHER_STAR : Material.ENDER_CHEST;

        ItemStack item;
        if (isClaimed) {
            // Pas kebuka: minecart
            String title = isFinalLevel100
                    ? "<gray>✔ 👑 Level 100 (Selesai)</gray>"
                    : "<gray>✔ ★ Milestone Lv." + lvl + " (Selesai)</gray>";

            item = createRewardCard(Material.MINECART,
                    title,
                    details,
                    "<gray>Hadiah istimewa sudah diambil</gray>", false);
        } else if (isUnlocked) {
            // Bisa diklaim: glowing
            String title = isFinalLevel100
                    ? "<gradient:#ffeaa7:#fdcb6e:#e17055:#d63031><bold>👑 HADIAH PUNCAK LEVEL 100 ASCENSION 👑</bold></gradient>"
                    : "<gold><bold>★ MILESTONE LV." + lvl + " ★</bold></gold>";

            item = createRewardCard(icon,
                    title,
                    details,
                    "<green><bold>» KLIK KLAIM HADIAH ISTIMEWA «</bold></green>", true);
        } else {
            // Pas kekunci: glowing
            String title = isFinalLevel100
                    ? "<red>🔒 👑 HADIAH PUNCAK LEVEL 100</red>"
                    : "<red>🔒 ★ Milestone Lv." + lvl + "</red>";

            item = createRewardCard(icon,
                    title,
                    details,
                    "<red>Terkunci — Capai Level " + lvl + "</red>", true);
        }

        inv.setItem(slot, item);
    }

    private List<String> buildRewardItemDetails(Reward reward, boolean isMilestone) {
        List<String> list = new ArrayList<>();
        list.add(isMilestone ? "<gold><bold>Hadiah Milestone:</bold></gold>" : "<yellow><bold>Hadiah:</bold></yellow>");

        Set<String> itemNamesAdded = new HashSet<>();

        // 1. Physical items
        if (reward.getItems() != null && !reward.getItems().isEmpty()) {
            for (ItemStack is : reward.getItems()) {
                if (is != null && !is.getType().isAir()) {
                    String formattedName = is.getItemMeta() != null && is.getItemMeta().hasDisplayName()
                            ? miniMessage.serialize(is.getItemMeta().displayName())
                            : formatItemName(is.getType().name());
                    list.add(" <gray>•</gray> <white>" + is.getAmount() + "x " + formattedName + "</white>");
                    itemNamesAdded.add(is.getType().name().toLowerCase().replace("_", " "));
                }
            }
        }

        // 2. Lore lines from config (commands, currency, permissions, etc.)
        if (reward.getLore() != null) {
            for (String rawLine : reward.getLore()) {
                String stripped = rawLine.replaceAll("<[^>]*>", "").trim();
                // Filter out all duplicate headers
                if (stripped.equalsIgnoreCase("Rewards:")
                        || stripped.equalsIgnoreCase("Hadiah:")
                        || stripped.equalsIgnoreCase("Isi Paket Hadiah:")
                        || stripped.equalsIgnoreCase("★ SPECIAL MILESTONE REWARD ★")
                        || stripped.equalsIgnoreCase("★ ULTIMATE SERVER PINNACLE REWARD ★")
                        || stripped.toLowerCase().startsWith("milestone tier unlocked:")
                        || stripped.toLowerCase().startsWith("standard level advancement reward.")) {
                    continue;
                }
                // Check if this lore line mentions an item that is already listed
                boolean alreadyListed = false;
                for (String added : itemNamesAdded) {
                    if (!added.isEmpty() && stripped.toLowerCase().contains(added)) {
                        alreadyListed = true;
                        break;
                    }
                }
                if (!alreadyListed) {
                    list.add(rawLine.startsWith(" ") ? rawLine : " " + rawLine);
                }
            }
        }

        // 3. Fallback to commands if no items and no lore lines were added
        if (list.size() == 1) {
            if (reward.getCommands() != null && !reward.getCommands().isEmpty()) {
                for (String cmd : reward.getCommands()) {
                    String lower = cmd.toLowerCase().trim();
                    if (lower.startsWith("eco give") || lower.startsWith("economy give")) {
                        String[] parts = cmd.split("\\s+");
                        if (parts.length >= 4) {
                            list.add(" <gray>•</gray> <green>" + parts[3] + " Saldo Koin</green>");
                        }
                    } else if (lower.startsWith("crate key give")) {
                        String[] parts = cmd.split("\\s+");
                        if (parts.length >= 5) {
                            list.add(" <gray>•</gray> <light_purple>" + parts[4] + "x Kunci Crate " + parts[3] + "</light_purple>");
                        }
                    } else if (lower.startsWith("give ")) {
                        String[] parts = cmd.split("\\s+");
                        if (parts.length >= 3) {
                            String item = parts[2].replace("minecraft:", "").toUpperCase();
                            String count = parts.length >= 4 ? parts[3] : "1";
                            list.add(" <gray>•</gray> <white>" + count + "x " + formatItemName(item) + "</white>");
                        }
                    } else {
                        list.add(" <gray>•</gray> <yellow>Hadiah Spesial Kerajaan</yellow>");
                    }
                }
            } else {
                list.add(" <gray>•</gray> <yellow>150 Koin & XP Kerajaan</yellow>");
            }
        }

        list.add("<gray> </gray>"); // Spacing before status
        return list;
    }

    private String formatItemName(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        String[] words = raw.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (!words[i].isEmpty()) {
                sb.append(Character.toUpperCase(words[i].charAt(0)))
                  .append(words[i].substring(1));
                if (i < words.length - 1) sb.append(" ");
            }
        }
        return sb.toString();
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

        // 6. Close (Slot 53)
        if (slot == 53) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return;
        }

        // 7. Check Milestone Reward Slot (Slot 22 on Pages 2..10)
        if (currentPage >= 2 && currentPage <= 10 && slot == MILESTONE_SLOT) {
            int milestoneLevel = (currentPage - 1) * 10 + 1;
            boolean claimed = plugin.getRewardManager().claimReward(player, milestoneLevel);
            if (claimed) {
                open(player, currentPage);
            }
            return;
        }

        // 8. Check Level 100 Reward Slot (Slot 22 on Page 11)
        if (currentPage == 11 && slot == FINAL_LEVEL_100_SLOT) {
            boolean claimed = plugin.getRewardManager().claimReward(player, 100);
            if (claimed) {
                open(player, currentPage);
            }
            return;
        }

        // 9. Check Normal Level Reward Slots (Row 4: Slots 27..35 on Pages 1..10)
        if (currentPage <= 10) {
            int startNormal = (currentPage - 1) * 10 + 2;
            int endNormal = currentPage == 10 ? 99 : (currentPage - 1) * 10 + 10;

            for (int i = 0; i < NORMAL_SLOTS.length; i++) {
                if (slot == NORMAL_SLOTS[i]) {
                    int targetLevel = startNormal + i;
                    if (targetLevel <= endNormal) {
                        boolean claimed = plugin.getRewardManager().claimReward(player, targetLevel);
                        if (claimed) {
                            open(player, currentPage);
                        }
                    }
                    return;
                }
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
            meta.lore(lore); // Set lore on item meta!

            if (glow) {
                // Paper 1.20.5+ / 1.21.4 Component Glint Override
                try {
                    meta.setEnchantmentGlintOverride(true);
                } catch (Throwable ignored) {}
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
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
