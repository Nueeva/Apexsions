package com.apexsions.core.gui.admin;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
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
 * 54-Slot Central Player Management & Selector GUI.
 */
public class PlayerManagerGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player admin;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private int page = 0;
    private String kingdomFilter = "ALL"; // ALL, ZENITHAR, SOLTERRA, SYLVAMOOR
    private String searchFilter = "";
    private final Map<Integer, UUID> slotPlayerMap = new HashMap<>();

    public PlayerManagerGUI(ApexsionsCorePlugin plugin, Player admin) {
        this.plugin = plugin;
        this.admin = admin;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#3498db:#9b59b6><bold>👤 APEXSIONS PLAYER MANAGER</bold></gradient>"));
        buildGUI();
    }

    public void open() {
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

        // Header Slot 0: Kingdom Filter Button
        ItemStack filterItem = new ItemStack(Material.HOPPER);
        ItemMeta fMeta = filterItem.getItemMeta();
        if (fMeta != null) {
            fMeta.displayName(mm.deserialize("<gold><bold>🔍 FILTER KERAJAAN</bold></gold>"));
            fMeta.lore(List.of(
                    mm.deserialize("<gray>Filter Aktif: <yellow><bold>" + kingdomFilter + "</bold></yellow></gray>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk mengganti filter kerajaan</yellow>")
            ));
            filterItem.setItemMeta(fMeta);
        }
        inventory.setItem(0, filterItem);

        // Header Slot 4: Player Hub Status
        int totalOnline = Bukkit.getOnlinePlayers().size();
        ItemStack statusItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta sMeta = statusItem.getItemMeta();
        if (sMeta != null) {
            sMeta.displayName(mm.deserialize("<gradient:#2ecc71:#f1c40f><bold>👑 STATUS PEMAIN AKTIF 👑</bold></gradient>"));
            sMeta.lore(List.of(
                    mm.deserialize("<gray>Total Online:</gray> <green><bold>" + totalOnline + " Pemain</bold></green>"),
                    mm.deserialize("<gray>Pencarian:</gray> <yellow>" + (searchFilter.isEmpty() ? "Semua" : searchFilter) + "</yellow>"),
                    mm.deserialize("<gray>Halaman:</gray> <gold>" + (page + 1) + "</gold>")
            ));
            statusItem.setItemMeta(sMeta);
        }
        inventory.setItem(4, statusItem);

        // Header Slot 8: Search Player via Chat
        ItemStack searchItem = new ItemStack(Material.COMPASS);
        ItemMeta searchMeta = searchItem.getItemMeta();
        if (searchMeta != null) {
            searchMeta.displayName(mm.deserialize("<aqua><bold>🔎 CARI NAMA PEMAIN</bold></aqua>"));
            searchMeta.lore(List.of(
                    mm.deserialize("<gray>Cari pemain berdasarkan username spesifik.</gray>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk input teks pencarian di chat</yellow>")
            ));
            searchItem.setItemMeta(searchMeta);
        }
        inventory.setItem(8, searchItem);

        // Fetch and filter players
        List<Player> onlineList = new ArrayList<>(Bukkit.getOnlinePlayers());
        List<Player> filtered = onlineList.stream().filter(p -> {
            if (!searchFilter.isEmpty() && !p.getName().toLowerCase().contains(searchFilter.toLowerCase())) {
                return false;
            }
            if (!kingdomFilter.equals("ALL")) {
                String pKingdom = plugin.getApi().getPlayerRegionKey(p.getUniqueId());
                return pKingdom.equalsIgnoreCase(kingdomFilter);
            }
            return true;
        }).sorted(Comparator.comparing(Player::getName)).toList();

        // 28 Display slots (Rows 2..5: 10..16, 19..25, 28..34, 37..43)
        int[] playerSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int pageSize = playerSlots.length;
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, filtered.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slot = playerSlots[i - startIndex];
            Player target = filtered.get(i);
            slotPlayerMap.put(slot, target.getUniqueId());

            PlayerData data = plugin.getPlayerDataService().getCached(target.getUniqueId()).orElse(null);
            int level = data != null ? data.getLevel() : 1;
            long xp = data != null ? data.getXp() : 0;
            String kName = "Belum Memilih";
            if (data != null && data.getRegionId() != null) {
                kName = plugin.getRegionManager().getRegion(data.getRegionId()).map(Region::getDisplayName).orElse("Belum Memilih");
            }

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) skull.getItemMeta();
            if (sm != null) {
                sm.setOwningPlayer(target);
                sm.displayName(mm.deserialize("<gold><bold>" + target.getName() + "</bold></gold>"));
                sm.lore(List.of(
                        mm.deserialize("<gray>Kerajaan: <yellow>" + kName + "</yellow></gray>"),
                        mm.deserialize("<gray>Level: <gold>Lv. " + level + "</gold> <dark_gray>(" + xp + " XP)</dark_gray></gray>"),
                        mm.deserialize("<gray>Ping: <green>" + target.getPing() + "ms</green></gray>"),
                        mm.deserialize("<gray>Darah: <red>" + (int) target.getHealth() + "/" + (int) target.getMaxHealth() + " HP</red></gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk Membuka Player Inspector (Full Access)!</yellow>")
                ));
                skull.setItemMeta(sm);
            }
            inventory.setItem(slot, skull);
        }

        // Pagination buttons
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta pMeta = prev.getItemMeta();
            if (pMeta != null) {
                pMeta.displayName(mm.deserialize("<yellow><bold>◀ Halaman Sebelumnya (" + page + ")</bold></yellow>"));
                prev.setItemMeta(pMeta);
            }
            inventory.setItem(45, prev);
        }

        if (endIndex < filtered.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nMeta = next.getItemMeta();
            if (nMeta != null) {
                nMeta.displayName(mm.deserialize("<yellow><bold>Halaman Berikutnya (" + (page + 2) + ") ▶</bold></yellow>"));
                next.setItemMeta(nMeta);
            }
            inventory.setItem(53, next);
        }

        // Slot 49: Back to Master Hub
        ItemStack back = new ItemStack(Material.OAK_DOOR);
        ItemMeta bMeta = back.getItemMeta();
        if (bMeta != null) {
            bMeta.displayName(mm.deserialize("<red><bold>◀ KEMBALI KE MASTER ADMIN HUB</bold></red>"));
            back.setItemMeta(bMeta);
        }
        inventory.setItem(49, back);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slotPlayerMap.containsKey(slot)) {
            UUID targetUuid = slotPlayerMap.get(slot);
            Player target = Bukkit.getPlayer(targetUuid);
            if (target != null) {
                admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                new PlayerInspectorGUI(plugin, admin, target).open();
            } else {
                admin.sendMessage(mm.deserialize("<red>Pemain tersebut sudah tidak online.</red>"));
                buildGUI();
            }
            return;
        }

        if (slot == 0) { // Cycle Kingdom Filter
            admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            kingdomFilter = switch (kingdomFilter) {
                case "ALL" -> "ZENITHAR";
                case "ZENITHAR" -> "SOLTERRA";
                case "SOLTERRA" -> "SYLVAMOOR";
                default -> "ALL";
            };
            page = 0;
            buildGUI();
            return;
        }

        if (slot == 8) { // Search via Chat Session
            plugin.getAdminChatInputManager().startSession(admin,
                    "Ketik nama pemain yang ingin dicari (atau 'all' untuk reset filter):",
                    query -> {
                        if (query.equalsIgnoreCase("all") || query.equalsIgnoreCase("reset")) {
                            searchFilter = "";
                        } else {
                            searchFilter = query;
                        }
                        page = 0;
                        buildGUI();
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

        if (slot == 49) {
            admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            new MasterAdminGUI(plugin, admin).open();
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
