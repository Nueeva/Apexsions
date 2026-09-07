package com.apexsions.core.region.gui;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.level.reward.Reward;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.gui.holder.MilestoneRewardPreviewHolder;
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

import java.util.*;

public class MilestoneRewardPreviewGUI implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // 14 inner slots for preview items
    private static final int[] PREVIEW_SLOTS = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25 };

    public MilestoneRewardPreviewGUI(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, int milestoneLevel, int fromPage) {
        Optional<Reward> rewardOpt = plugin.getRewardManager().getReward(milestoneLevel);
        if (rewardOpt.isEmpty()) {
            player.sendMessage(miniMessage.deserialize("<red>Data hadiah milestone level " + milestoneLevel + " tidak ditemukan.</red>"));
            return;
        }

        Reward reward = rewardOpt.get();
        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
        int playerLevel = dataOpt.map(PlayerData::getLevel).orElse(1);
        boolean isClaimed = dataOpt.map(d -> d.isRewardClaimed(milestoneLevel)).orElse(false);
        boolean isUnlocked = playerLevel >= milestoneLevel;

        MilestoneRewardPreviewHolder holder = new MilestoneRewardPreviewHolder(milestoneLevel, fromPage);
        String titleStr = milestoneLevel == 100
                ? "<gradient:#ffeaa7:#fdcb6e:#d63031><bold>👑 PREVIEW TAHTA LEVEL 100 👑</bold></gradient>"
                : "<gradient:#ffd32a:#ffa801><bold>✦ PREVIEW MILESTONE LV." + milestoneLevel + " ✦</bold></gradient>";
        Inventory inv = Bukkit.createInventory(holder, 36, miniMessage.deserialize(titleStr));
        holder.setInventory(inv);

        // 1. Background borders
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<gray> </gray>");
        for (int i = 0; i < 36; i++) {
            inv.setItem(i, border);
        }

        // Decorative borders
        ItemStack accent = createItem(Material.YELLOW_STAINED_GLASS_PANE, "<gold>★</gold>");
        inv.setItem(3, accent);
        inv.setItem(5, accent);

        // 2. Overview Card at Slot 4
        Material headerMat = milestoneLevel == 100 ? Material.NETHER_STAR : Material.ENDER_CHEST;
        String headerTitle = milestoneLevel == 100
                ? "<gradient:#ffeaa7:#fdcb6e:#e17055:#d63031><bold>👑 HADIAH PUNCAK: LEVEL 100 👑</bold></gradient>"
                : "<gold><bold>★ PAKET HADIAH MILESTONE: LEVEL " + milestoneLevel + " ★</bold></gold>";

        List<String> headerLore = new ArrayList<>();
        headerLore.add("<gray>Tinjauan paket hadiah istimewa kerajaan.</gray>");
        headerLore.add("<gray>Dibutuhkan: <yellow>Level " + milestoneLevel + "</yellow></gray>");
        headerLore.add("<gray>Level Kamu: <gold>" + playerLevel + "</gold></gray>");
        headerLore.add(" ");
        if (isClaimed) {
            headerLore.add("<gray>Status: <green>✔ Sudah Diambil</green></gray>");
        } else if (isUnlocked) {
            headerLore.add("<gray>Status: <green>★ Siap Diklaim!</green></gray>");
        } else {
            headerLore.add("<gray>Status: <red>🔒 Belum Terbuka</red></gray>");
        }

        inv.setItem(4, createItem(headerMat, headerTitle, headerLore.toArray(new String[0])));

        // 3. Render Reward Items in Preview Slots
        int slotIdx = 0;

        // 3a. Physical Items
        if (reward.getItems() != null && !reward.getItems().isEmpty()) {
            for (ItemStack item : reward.getItems()) {
                if (item == null || item.getType().isAir()) continue;
                if (slotIdx >= PREVIEW_SLOTS.length) break;

                ItemStack display = item.clone();
                ItemMeta meta = display.getItemMeta();
                if (meta != null) {
                    List<Component> currentLore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                    currentLore.add(miniMessage.deserialize("<dark_gray>--------------------</dark_gray>"));
                    currentLore.add(miniMessage.deserialize("<yellow>📦 Paket Milestone Level " + milestoneLevel + "</yellow>"));
                    meta.lore(currentLore);
                    display.setItemMeta(meta);
                }
                inv.setItem(PREVIEW_SLOTS[slotIdx++], display);
            }
        }

        // 3b. Virtual Rewards from Commands (Currencies, Crate Keys, etc.)
        if (reward.getCommands() != null && !reward.getCommands().isEmpty()) {
            for (String cmd : reward.getCommands()) {
                if (slotIdx >= PREVIEW_SLOTS.length) break;

                String lower = cmd.toLowerCase().trim();
                if (lower.startsWith("eco give") || lower.startsWith("economy give")) {
                    String[] parts = cmd.split("\\s+");
                    String amt = parts.length >= 4 ? parts[3] : "1000";
                    ItemStack coinItem = createGlowingItem(Material.GOLD_INGOT,
                            "<gradient:#ffd32a:#ffa801><bold>💰 Saldo Koin: " + amt + "</bold></gradient>",
                            "<gray>Mata uang resmi kerajaan Apexsions.</gray>",
                            "<gray>Jumlah: <green>+" + amt + " Koin</green></gray>",
                            " ",
                            "<dark_gray>--------------------</dark_gray>",
                            "<yellow>📦 Paket Milestone Level " + milestoneLevel + "</yellow>");
                    inv.setItem(PREVIEW_SLOTS[slotIdx++], coinItem);
                } else if (lower.startsWith("crate key give")) {
                    String[] parts = cmd.split("\\s+");
                    String keyName = parts.length >= 4 ? parts[3] : "Special";
                    String amt = parts.length >= 5 ? parts[4] : "1";
                    ItemStack crateItem = createGlowingItem(Material.TRIPWIRE_HOOK,
                            "<gradient:#dfe6e9:#b2bec3><bold>🗝 " + amt + "x Kunci Crate: " + keyName.toUpperCase() + "</bold></gradient>",
                            "<gray>Gunakan pada Crate di Spawn/Realms.</gray>",
                            "<gray>Jumlah: <yellow>" + amt + " Kunci</yellow></gray>",
                            " ",
                            "<dark_gray>--------------------</dark_gray>",
                            "<yellow>📦 Paket Milestone Level " + milestoneLevel + "</yellow>");
                    inv.setItem(PREVIEW_SLOTS[slotIdx++], crateItem);
                }
            }
        }

        // 3c. If preview slots still empty, show lore items or summary
        if (slotIdx == 0 && reward.getLore() != null && !reward.getLore().isEmpty()) {
            ItemStack summary = createGlowingItem(Material.BOOK,
                    "<gold><bold>📜 Paket Hak Istimewa</bold></gold>",
                    reward.getLore().toArray(new String[0]));
            inv.setItem(PREVIEW_SLOTS[slotIdx++], summary);
        }

        // 4. Navigation Controls (Row 3: Slots 27..35)
        // Slot 27: Back Button
        inv.setItem(27, createItem(Material.ARROW,
                "<yellow>« Kembali ke Hadiah Level</yellow>",
                "<gray>Kembali ke daftar level (Hal. " + fromPage + ").</gray>"));

        // Slot 31: Action / Status Button
        if (isUnlocked && !isClaimed) {
            ItemStack claimBtn = createGlowingItem(Material.HOPPER,
                    "<green><bold>» KLAIM HADIAH SEKARANG «</bold></green>",
                    "<gray>Level Anda telah mencukupi!</gray>",
                    "<yellow>Klik untuk langsung mengambil paket hadiah ini.</yellow>");
            inv.setItem(31, claimBtn);
        } else if (isClaimed) {
            ItemStack claimedBtn = createItem(Material.CHEST_MINECART,
                    "<gray>✔ Hadiah Sudah Diambil</gray>",
                    "<gray>Paket hadiah milestone ini sudah masuk ke inventori Anda.</gray>");
            inv.setItem(31, claimedBtn);
        } else {
            ItemStack lockedBtn = createItem(Material.IRON_BARS,
                    "<red><bold>🔒 Hadiah Masih Terkunci</bold></red>",
                    "<gray>Dibutuhkan: <yellow>Level " + milestoneLevel + "</yellow></gray>",
                    "<gray>Tingkatkan level kerajaan Anda untuk mengambil hadiah ini!</gray>");
            inv.setItem(31, lockedBtn);
        }

        // Slot 35: Close Button
        inv.setItem(35, createItem(Material.BARRIER,
                "<red><bold>✖ Tutup</bold></red>",
                "<gray>Tutup menu.</gray>"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.3f);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof MilestoneRewardPreviewHolder holder)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 36) return;

        int milestoneLevel = holder.getMilestoneLevel();
        int fromPage = holder.getFromPage();

        // 1. Back button (Slot 27)
        if (slot == 27) {
            plugin.getLevelRewardsGUI().open(player, fromPage);
            return;
        }

        // 2. Close button (Slot 35)
        if (slot == 35) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return;
        }

        // 3. Claim button (Slot 31)
        if (slot == 31) {
            Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
            if (dataOpt.isPresent()) {
                PlayerData data = dataOpt.get();
                if (data.getLevel() >= milestoneLevel && !data.isRewardClaimed(milestoneLevel)) {
                    boolean claimed = plugin.getRewardManager().claimReward(player, milestoneLevel);
                    if (claimed) {
                        open(player, milestoneLevel, fromPage);
                    }
                } else if (data.isRewardClaimed(milestoneLevel)) {
                    player.sendMessage(miniMessage.deserialize("<gray>Hadiah ini sudah pernah kamu ambil.</gray>"));
                } else {
                    player.sendMessage(miniMessage.deserialize("<red>Capai Level " + milestoneLevel + " untuk membuka hadiah ini!</red>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
                }
            }
        }
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

    private ItemStack createGlowingItem(Material mat, String name, String... lore) {
        ItemStack item = createItem(mat, name, lore);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            try {
                meta.setEnchantmentGlintOverride(true);
            } catch (Throwable ignored) {}
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }
}
