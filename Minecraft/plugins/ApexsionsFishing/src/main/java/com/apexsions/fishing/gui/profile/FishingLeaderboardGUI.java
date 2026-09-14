package com.apexsions.fishing.gui.profile;

import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.model.PlayerFishingStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Fishing Leaderboard GUI.
 * Shows top 10 players across 3 categories:
 * 1. Single Heaviest Catch (Tangkapan Terberat)
 * 2. Total Cumulative Weight (Total Bobot Ditimbang)
 * 3. Total Fish Count (Jumlah Ikan Tertangkap)
 */
public class FishingLeaderboardGUI implements InventoryHolder {

    public enum LeaderboardType {
        HEAVIEST_CATCH("Tangkapan Terberat Tunggal", Material.TROPICAL_FISH),
        TOTAL_WEIGHT("Total Bobot Kumulatif (kg)", Material.ANVIL),
        TOTAL_FISH("Total Ikan Tertangkap", Material.COD);

        private final String title;
        private final Material icon;

        LeaderboardType(String title, Material icon) {
            this.title = title;
            this.icon = icon;
        }

        public String getTitle() {
            return title;
        }

        public Material getIcon() {
            return icon;
        }
    }

    private final ApexsionsFishing plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private LeaderboardType currentType = LeaderboardType.HEAVIEST_CATCH;

    public static final int SLOT_TAB_HEAVIEST = 2;
    public static final int SLOT_TAB_WEIGHT = 4;
    public static final int SLOT_TAB_COUNT = 6;
    public static final int SLOT_BACK = 49;

    public static final int[] TOP_SLOTS = {
            19, 20, 21, 22, 23, 24, 25,
            29, 30, 31
    };

    public FishingLeaderboardGUI(@NotNull ApexsionsFishing plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#f1c40f:#e67e22><bold>Papan Peringkat Pemancing Terbaik</bold></gradient>"));
        render();
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void render() {
        inventory.clear();

        // Border
        ItemStack borderGlass = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderGlass);
            }
        }

        // Category Tabs at top row
        renderTabs();

        // Query top 10 list
        List<PlayerFishingStats> topList;
        if (currentType == LeaderboardType.HEAVIEST_CATCH) {
            topList = plugin.getVaultStorageManager().getTopHeaviestCatch(10);
        } else if (currentType == LeaderboardType.TOTAL_WEIGHT) {
            topList = plugin.getVaultStorageManager().getTopTotalWeight(10);
        } else {
            topList = plugin.getVaultStorageManager().getTopTotalFish(10);
        }

        // Fill top 10 slots
        for (int i = 0; i < TOP_SLOTS.length; i++) {
            int slot = TOP_SLOTS[i];
            int rank = i + 1;

            if (i < topList.size()) {
                PlayerFishingStats stats = topList.get(i);
                OfflinePlayer op = Bukkit.getOfflinePlayer(stats.getPlayerUuid());
                String pName = op.getName() != null ? op.getName() : "Nelayan #" + rank;

                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                if (meta != null) {
                    meta.setOwningPlayer(op);
                    String rankBadge = switch (rank) {
                        case 1 -> "<gradient:#ffd700:#ffa500><bold>👑 JUARA 1: " + pName + "</bold></gradient>";
                        case 2 -> "<gradient:#e0e0e0:#bdc3c7><bold>🥈 JUARA 2: " + pName + "</bold></gradient>";
                        case 3 -> "<gradient:#d35400:#e67e22><bold>🥉 JUARA 3: " + pName + "</bold></gradient>";
                        default -> "<yellow><bold>#" + rank + " - " + pName + "</bold></yellow>";
                    };

                    meta.displayName(mm.deserialize(rankBadge));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.empty());

                    if (currentType == LeaderboardType.HEAVIEST_CATCH) {
                        lore.add(mm.deserialize("<yellow>● Tangkapan Terberat:</yellow> <gold><bold>" + String.format("%.2f", stats.getHeaviestFishWeight()) + " kg</bold></gold>"));
                        lore.add(mm.deserialize("<gray>Spesies:</gray> <aqua>" + stats.getHeaviestFishName() + "</aqua>"));
                    } else if (currentType == LeaderboardType.TOTAL_WEIGHT) {
                        lore.add(mm.deserialize("<yellow>● Total Bobot Ditimbang:</yellow> <gold><bold>" + String.format("%.2f", stats.getTotalWeightCaught()) + " kg</bold></gold>"));
                        lore.add(mm.deserialize("<gray>Total Ikan:</gray> <white>" + String.format("%,d", stats.getTotalFishCaught()) + " Ekor</white>"));
                    } else {
                        lore.add(mm.deserialize("<yellow>● Total Ikan Tertangkap:</yellow> <gold><bold>" + String.format("%,d", stats.getTotalFishCaught()) + " Ekor</bold></gold>"));
                        lore.add(mm.deserialize("<gray>Total Bobot:</gray> <white>" + String.format("%.2f", stats.getTotalWeightCaught()) + " kg</white>"));
                    }

                    meta.lore(lore);
                    skull.setItemMeta(meta);
                }
                inventory.setItem(slot, skull);
            } else {
                // Empty rank
                inventory.setItem(slot, createGuiItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                        "<gray><bold>#" + rank + " - Belum Ada Data</bold></gray>",
                        List.of("<dark_gray>Jadilah yang pertama mengisi posisi ini!</dark_gray>")));
            }
        }

        // Back button at Slot 49
        inventory.setItem(SLOT_BACK, createGuiItem(Material.ARROW,
                "<yellow><bold>◀ Kembali ke Profil</bold></yellow>",
                List.of("<gray>Klik untuk kembali ke profil memancing.</gray>")));
    }

    private void renderTabs() {
        inventory.setItem(SLOT_TAB_HEAVIEST, createTabItem(LeaderboardType.HEAVIEST_CATCH));
        inventory.setItem(SLOT_TAB_WEIGHT, createTabItem(LeaderboardType.TOTAL_WEIGHT));
        inventory.setItem(SLOT_TAB_COUNT, createTabItem(LeaderboardType.TOTAL_FISH));
    }

    private ItemStack createTabItem(LeaderboardType type) {
        boolean selected = (currentType == type);
        Material mat = selected ? Material.ENCHANTED_BOOK : type.getIcon();
        String prefix = selected ? "<green><bold>▶ " : "<gray>";
        String suffix = selected ? " ◀</bold></green>" : "</gray>";

        return createGuiItem(mat, prefix + type.getTitle() + suffix,
                List.of(
                        selected ? "<green>● Kategori saat ini sedang aktif.</green>" : "<yellow>Klik untuk melihat kategori ini.</yellow>"
                ));
    }

    public void handleClick(@NotNull InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == SLOT_BACK) {
            new FishingProfileGUI(plugin, player).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return;
        }

        if (slot == SLOT_TAB_HEAVIEST && currentType != LeaderboardType.HEAVIEST_CATCH) {
            currentType = LeaderboardType.HEAVIEST_CATCH;
            render();
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.2f);
        } else if (slot == SLOT_TAB_WEIGHT && currentType != LeaderboardType.TOTAL_WEIGHT) {
            currentType = LeaderboardType.TOTAL_WEIGHT;
            render();
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.2f);
        } else if (slot == SLOT_TAB_COUNT && currentType != LeaderboardType.TOTAL_FISH) {
            currentType = LeaderboardType.TOTAL_FISH;
            render();
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.2f);
        }
    }

    private ItemStack createGuiItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            List<Component> compLore = new ArrayList<>();
            for (String l : lore) {
                compLore.add(mm.deserialize(l));
            }
            meta.lore(compLore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
