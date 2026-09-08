package com.apexsions.core.gui.admin;

import com.apexsions.core.ApexsionsCorePlugin;
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
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Interactive 54-slot Admin GUI for selecting a player and adjusting their BattlePass tier.
 */
public class BattlePassSetTierGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player admin;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private int page = 0;
    private final Map<Integer, UUID> slotPlayerMap = new HashMap<>();

    public BattlePassSetTierGUI(ApexsionsCorePlugin plugin, Player admin) {
        this.plugin = plugin;
        this.admin = admin;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#f1c40f:#3498db><bold>⭐ SET TIER BATTLEPASS PEMAIN ⭐</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        admin.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotPlayerMap.clear();

        ItemStack borderPane = createGlass(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        ItemStack decorPane = createGlass(Material.CYAN_STAINED_GLASS_PANE, "<aqua>✦</aqua>");

        // Border
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderPane);
            }
        }
        inventory.setItem(1, decorPane);
        inventory.setItem(7, decorPane);
        inventory.setItem(46, decorPane);
        inventory.setItem(52, decorPane);

        // Header Slot 4: Overview & Instructions
        ItemStack header = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta hMeta = header.getItemMeta();
        if (hMeta != null) {
            hMeta.displayName(mm.deserialize("<gradient:#f1c40f:#3498db><bold>⭐ KELOLA TIER LEVEL PEMAIN ⭐</bold></gradient>"));
            hMeta.lore(List.of(
                    mm.deserialize("<gray>Klik pada pemain di bawah untuk mengubah level Tier BP:</gray>"),
                    Component.empty(),
                    mm.deserialize("<green>▶ [Klik Kiri]</green> <yellow><bold>+1 Tier</bold></yellow>"),
                    mm.deserialize("<red>▶ [Klik Kanan]</red> <yellow><bold>-1 Tier</bold></yellow>"),
                    mm.deserialize("<gold>▶ [Shift + Klik Kiri]</gold> <gold><bold>+5 Tier</bold></gold>"),
                    mm.deserialize("<light_purple>▶ [Shift + Klik Kanan]</light_purple> <aqua><bold>Setel Level Manual</bold></aqua>"),
                    Component.empty(),
                    mm.deserialize("<yellow>Pemain online saat ini: <aqua>" + Bukkit.getOnlinePlayers().size() + " pemain</aqua></yellow>")
            ));
            header.setItemMeta(hMeta);
        }
        inventory.setItem(4, header);

        // Slot 0: Offline / Manual Input
        ItemStack manualItem = new ItemStack(Material.NAME_TAG);
        ItemMeta mMeta = manualItem.getItemMeta();
        if (mMeta != null) {
            mMeta.displayName(mm.deserialize("<aqua><bold>✏️ ATUR TIER PEMAIN OFFLINE</bold></aqua>"));
            mMeta.lore(List.of(
                    mm.deserialize("<gray>Atur level tier pemain offline via chat.</gray>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk input teks <nama> <level></yellow>")
            ));
            manualItem.setItemMeta(mMeta);
        }
        inventory.setItem(0, manualItem);

        // List online players
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.sort(Comparator.comparing(Player::getName));

        int[] playerSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int pageSize = playerSlots.length;
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, players.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slot = playerSlots[i - startIndex];
            Player target = players.get(i);
            slotPlayerMap.put(slot, target.getUniqueId());

            String passBadge = BattlePassGivePassGUI.resolvePlayerPassBadge(target.getUniqueId());
            int bpTier = BattlePassGivePassGUI.resolvePlayerTier(target.getUniqueId());

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) skull.getItemMeta();
            if (sm != null) {
                sm.setOwningPlayer(target);
                sm.displayName(mm.deserialize("<gold><bold>" + target.getName() + "</bold></gold>"));
                sm.lore(List.of(
                        mm.deserialize("<gray>Pass Aktif: " + passBadge + "</gray>"),
                        mm.deserialize("<gray>Tier Saat Ini: <gold><bold>Tier " + bpTier + "</bold></gold></gray>"),
                        Component.empty(),
                        mm.deserialize("<green>▶ [Klik Kiri] +1 Tier</green>"),
                        mm.deserialize("<red>▶ [Klik Kanan] -1 Tier</red>"),
                        mm.deserialize("<gold>▶ [Shift + Klik Kiri] +5 Tier</gold>"),
                        mm.deserialize("<light_purple>▶ [Shift + Klik Kanan] Setel Manual</light_purple>")
                ));
                skull.setItemMeta(sm);
            }
            inventory.setItem(slot, skull);
        }

        // Pagination
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta pMeta = prev.getItemMeta();
            if (pMeta != null) {
                pMeta.displayName(mm.deserialize("<yellow><bold>◀ Halaman Sebelumnya (" + page + ")</bold></yellow>"));
                prev.setItemMeta(pMeta);
            }
            inventory.setItem(45, prev);
        }

        if (endIndex < players.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nMeta = next.getItemMeta();
            if (nMeta != null) {
                nMeta.displayName(mm.deserialize("<yellow><bold>Halaman Berikutnya (" + (page + 2) + ") ▶</bold></yellow>"));
                next.setItemMeta(nMeta);
            }
            inventory.setItem(53, next);
        }

        // Slot 49: Back to Battlepass Sub-GUI
        ItemStack back = new ItemStack(Material.OAK_DOOR);
        ItemMeta bMeta = back.getItemMeta();
        if (bMeta != null) {
            bMeta.displayName(mm.deserialize("<red><bold>◀ KEMBALI KE PANEL BATTLEPASS</bold></red>"));
            bMeta.lore(List.of(mm.deserialize("<gray>Kembali ke menu kontrol BattlePass.</gray>")));
            back.setItemMeta(bMeta);
        }
        inventory.setItem(49, back);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) {
            admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            new BattlePassAdminSubGUI(plugin, admin).open();
            return;
        }

        if (slot == 0) { // Manual Input for Offline Player
            admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            plugin.getAdminChatInputManager().startSession(admin,
                    "Ketik nama pemain dan angka level (contoh: Notch 10):",
                    input -> {
                        String[] parts = input.trim().split("\\s+");
                        if (parts.length >= 2) {
                            admin.performCommand("abp setlevel " + parts[0] + " " + parts[1]);
                            admin.sendMessage(mm.deserialize("<green>✓ Tier BP " + parts[0] + " disetel ke <yellow>" + parts[1] + "</yellow>!</green>"));
                        }
                        open();
                    },
                    this::open
            );
            return;
        }

        if (slot == 45 && page > 0) {
            page--;
            admin.playSound(admin.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.8f, 1.0f);
            buildGUI();
            return;
        }

        if (slot == 53) {
            page++;
            admin.playSound(admin.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.8f, 1.0f);
            buildGUI();
            return;
        }

        if (slotPlayerMap.containsKey(slot)) {
            UUID targetUuid = slotPlayerMap.get(slot);
            Player target = Bukkit.getPlayer(targetUuid);
            if (target == null || !target.isOnline()) {
                admin.sendMessage(mm.deserialize("<red>Pemain tersebut sudah tidak online.</red>"));
                buildGUI();
                return;
            }

            int currentLvl = BattlePassGivePassGUI.resolvePlayerTier(targetUuid);

            if (event.isShiftClick() && event.isRightClick()) {
                // Setel Manual via Chat Session
                plugin.getAdminChatInputManager().startSession(admin,
                        "Ketik angka tier baru untuk " + target.getName() + " (contoh: 25):",
                        input -> {
                            try {
                                int newLvl = Integer.parseInt(input.replaceAll("[^0-9]", ""));
                                admin.performCommand("abp setlevel " + target.getName() + " " + newLvl);
                                admin.sendMessage(mm.deserialize("<green>✓ Tier BP <yellow>" + target.getName() + "</yellow> disetel ke <gold>" + newLvl + "</gold>!</green>"));
                            } catch (Exception e) {
                                admin.sendMessage(mm.deserialize("<red>Angka level tier tidak valid!</red>"));
                            }
                            open();
                        },
                        this::open
                );
                return;
            } else if (event.isShiftClick()) {
                // +5 Tier
                int newLvl = currentLvl + 5;
                admin.performCommand("abp setlevel " + target.getName() + " " + newLvl);
                admin.sendMessage(mm.deserialize("<green>✓ Tier BP <yellow>" + target.getName() + "</yellow> dinaikkan +5 menjadi <gold>Tier " + newLvl + "</gold>!</green>"));
                admin.playSound(admin.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
            } else if (event.isRightClick()) {
                // -1 Tier
                int newLvl = Math.max(1, currentLvl - 1);
                admin.performCommand("abp setlevel " + target.getName() + " " + newLvl);
                admin.sendMessage(mm.deserialize("<yellow>✓ Tier BP <yellow>" + target.getName() + "</yellow> dikurangi -1 menjadi <gold>Tier " + newLvl + "</gold>.</yellow>"));
                admin.playSound(admin.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 0.9f);
            } else {
                // +1 Tier
                int newLvl = currentLvl + 1;
                admin.performCommand("abp setlevel " + target.getName() + " " + newLvl);
                admin.sendMessage(mm.deserialize("<green>✓ Tier BP <yellow>" + target.getName() + "</yellow> dinaikkan +1 menjadi <gold>Tier " + newLvl + "</gold>!</green>"));
                admin.playSound(admin.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.3f);
            }

            // Re-render menu
            buildGUI();
        }
    }

    private ItemStack createGlass(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
