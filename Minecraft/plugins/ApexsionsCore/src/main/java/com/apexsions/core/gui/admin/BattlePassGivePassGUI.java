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
 * Interactive 54-slot Admin GUI for selecting a player and granting BattlePass tiers (Sio / Exsio / Reset).
 */
public class BattlePassGivePassGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player admin;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private int page = 0;
    private final Map<Integer, UUID> slotPlayerMap = new HashMap<>();

    public BattlePassGivePassGUI(ApexsionsCorePlugin plugin, Player admin) {
        this.plugin = plugin;
        this.admin = admin;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#9b59b6:#f1c40f><bold>👑 BERIKAN BATTLEPASS PASS 👑</bold></gradient>"));
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
        ItemStack decorPane = createGlass(Material.PURPLE_STAINED_GLASS_PANE, "<light_purple>✦</light_purple>");

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
        ItemStack header = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta hMeta = header.getItemMeta();
        if (hMeta != null) {
            hMeta.displayName(mm.deserialize("<gradient:#9b59b6:#f1c40f><bold>👑 KELOLA AKSES PASS PEMAIN 👑</bold></gradient>"));
            hMeta.lore(List.of(
                    mm.deserialize("<gray>Klik pada pemain di bawah untuk memberikan pass:</gray>"),
                    Component.empty(),
                    mm.deserialize("<green>▶ [Klik Kiri]</green> <gold><bold>Berikan SIO PASS</bold></gold>"),
                    mm.deserialize("<light_purple>▶ [Klik Kanan]</light_purple> <light_purple><bold>Berikan EXSIO PASS</bold></light_purple>"),
                    mm.deserialize("<red>▶ [Shift + Klik]</red> <gray>Reset kembali ke </gray><white>FREE PASS</white>"),
                    Component.empty(),
                    mm.deserialize("<yellow>Pemain online saat ini: <aqua>" + Bukkit.getOnlinePlayers().size() + " pemain</aqua></yellow>")
            ));
            header.setItemMeta(hMeta);
        }
        inventory.setItem(4, header);

        // Slot 0: Offline / Manual Chat Input
        ItemStack manualItem = new ItemStack(Material.NAME_TAG);
        ItemMeta mMeta = manualItem.getItemMeta();
        if (mMeta != null) {
            mMeta.displayName(mm.deserialize("<aqua><bold>✏️ INPUT PEMAIN MANUAL / OFFLINE</bold></aqua>"));
            mMeta.lore(List.of(
                    mm.deserialize("<gray>Berikan pass ke pemain offline melalui input chat.</gray>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk input teks <nama> <sio|exsio></yellow>")
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

            String passBadge = resolvePlayerPassBadge(target.getUniqueId());
            int bpTier = resolvePlayerTier(target.getUniqueId());

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) skull.getItemMeta();
            if (sm != null) {
                sm.setOwningPlayer(target);
                sm.displayName(mm.deserialize("<gold><bold>" + target.getName() + "</bold></gold>"));
                sm.lore(List.of(
                        mm.deserialize("<gray>Pass Aktif: " + passBadge + "</gray>"),
                        mm.deserialize("<gray>BattlePass Tier: <yellow>Tier " + bpTier + "</yellow></gray>"),
                        Component.empty(),
                        mm.deserialize("<green>▶ [Klik Kiri] Berikan SIO PASS</green>"),
                        mm.deserialize("<light_purple>▶ [Klik Kanan] Berikan EXSIO PASS</light_purple>"),
                        mm.deserialize("<red>▶ [Shift + Klik] Reset ke FREE PASS</red>")
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

        if (slot == 0) { // Manual Input for Offline / Custom Player
            admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            plugin.getAdminChatInputManager().startSession(admin,
                    "Ketik nama pemain dan tier pass (contoh: Notch sio atau Jeb exsio):",
                    input -> {
                        String[] parts = input.trim().split("\\s+");
                        if (parts.length >= 1 && !parts[0].isBlank()) {
                            String targetName = parts[0];
                            String passTier = parts.length > 1 ? parts[1].toLowerCase() : "sio";
                            admin.performCommand("abp givepass " + targetName + " " + passTier);
                            admin.sendMessage(mm.deserialize("<green>✓ Perintah Give Pass dieksekusi untuk <yellow>" + targetName + "</yellow> (Tier: <gold>" + passTier.toUpperCase() + "</gold>)!</green>"));
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

            if (event.isShiftClick()) {
                // Reset Pass
                admin.performCommand("abp reset " + target.getName());
                admin.sendMessage(mm.deserialize("<yellow>✓ Progres BattlePass & Pass <gold>" + target.getName() + "</gold> berhasil di-reset!</yellow>"));
                admin.playSound(admin.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 1.0f);
            } else if (event.isRightClick()) {
                // Exsio Pass
                admin.performCommand("abp givepass " + target.getName() + " exsio");
                admin.sendMessage(mm.deserialize("<green>✓ Berhasil memberikan <light_purple><bold>EXSIO PASS</bold></light_purple> kepada <yellow>" + target.getName() + "</yellow>!</green>"));
                admin.playSound(admin.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.3f);
            } else {
                // Sio Pass (Left Click)
                admin.performCommand("abp givepass " + target.getName() + " sio");
                admin.sendMessage(mm.deserialize("<green>✓ Berhasil memberikan <gold><bold>SIO PASS</bold></gold> kepada <yellow>" + target.getName() + "</yellow>!</green>"));
                admin.playSound(admin.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
            }

            // Re-render menu to show updated pass badge
            buildGUI();
        }
    }

    public static String resolvePlayerPassBadge(UUID uuid) {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsBattlepass")) {
                Class<?> bpProviderClass = Class.forName("com.apexsions.battlepass.api.ApexsionsBattlepassProvider");
                Object bpApi = bpProviderClass.getMethod("get").invoke(null);
                if (bpApi != null) {
                    boolean exsio = (boolean) bpApi.getClass().getMethod("hasPass", UUID.class, String.class).invoke(bpApi, uuid, "exsio");
                    if (exsio) return "<light_purple><bold>EXSIO PASS</bold></light_purple>";
                    boolean sio = (boolean) bpApi.getClass().getMethod("hasPass", UUID.class, String.class).invoke(bpApi, uuid, "sio");
                    if (sio) return "<gold><bold>SIO PASS</bold></gold>";
                    boolean prem = (boolean) bpApi.getClass().getMethod("hasPremiumPass", UUID.class).invoke(bpApi, uuid);
                    if (prem) return "<gold><bold>PREMIUM PASS</bold></gold>";
                }
            }
        } catch (Throwable ignored) {}
        return "<white>FREE PASS</white>";
    }

    public static int resolvePlayerTier(UUID uuid) {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsBattlepass")) {
                Class<?> bpProviderClass = Class.forName("com.apexsions.battlepass.api.ApexsionsBattlepassProvider");
                Object bpApi = bpProviderClass.getMethod("get").invoke(null);
                if (bpApi != null) {
                    return (int) bpApi.getClass().getMethod("getPlayerTier", UUID.class).invoke(bpApi, uuid);
                }
            }
        } catch (Throwable ignored) {}
        return 1;
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
