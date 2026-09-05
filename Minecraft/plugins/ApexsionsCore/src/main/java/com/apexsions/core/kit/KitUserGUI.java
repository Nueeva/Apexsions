package com.apexsions.core.kit;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 54-Slot Interactive Main Kit Catalog GUI for players (/kits).
 */
public class KitUserGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<Integer, Kit> slotKitMap = new HashMap<>();

    public KitUserGUI(ApexsionsCorePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#f1c40f:#e67e22><bold>📦 APEXSIONS KITS KERJAAN 📦</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotKitMap.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Header slot 4: User Profile & Rank Info
        String pRank = plugin.getLuckPermsHook().getPlayerRankKey(player);
        ItemStack header = createItem(Material.NETHER_STAR, "<gradient:#f1c40f:#e67e22><bold>👑 STATUS KITS KAMU 👑</bold></gradient>", List.of(
                "<gray>Pemain:</gray> <white>" + player.getName() + "</white>",
                "<gray>Rank Kamu:</gray> <gold>" + pRank.toUpperCase() + "</gold>",
                "",
                "<yellow>Klik Kiri: Intip isi kit (Preview)</yellow>",
                "<green>Klik Kanan: Klaim kit langsung</green>"
        ));
        inventory.setItem(4, header);

        // Kit Display Slots
        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        List<Kit> kitList = new ArrayList<>(plugin.getKitManager().getAllKits());
        for (int i = 0; i < kitList.size() && i < slots.length; i++) {
            Kit kit = kitList.get(i);
            int slot = slots[i];
            slotKitMap.put(slot, kit);

            boolean canClaim = plugin.getKitManager().canClaim(player, kit);
            long cd = plugin.getKitManager().getRemainingCooldownSeconds(player, kit);

            List<String> lore = new ArrayList<>();
            lore.add("<gray>Syarat Rank:</gray> <gold>" + kit.getRequiredRank().toUpperCase() + "</gold>");
            lore.add("<gray>Cooldown:</gray> <yellow>" + (kit.getCooldownSeconds() / 3600) + " Jam</yellow>");

            if (kit.getSetBonus() != null) {
                KitArmorSetBonus bonus = kit.getSetBonus();
                lore.add("");
                lore.add("<gradient:#f1c40f:#e67e22><bold>✦ Armor Set Bonus:</bold></gradient>");
                lore.add("<dark_gray>•</dark_gray> <gray>Efek (" + bonus.getRequiredPieces() + " Set):</gray> <yellow>" + bonus.getStatType().formatValue(bonus.getValue()) + " " + bonus.getStatType().getDisplayName() + "</yellow>");
            }

            lore.add("");
            if (canClaim) {
                lore.add("<green><bold>● SIAP DIKLAIM!</bold></green>");
            } else if (cd > 0) {
                lore.add("<red><bold>⏳ COOLDOWN:</bold> " + plugin.getKitManager().formatRemainingCooldown(cd) + "</red>");
            } else {
                lore.add("<red><bold>✖ TERKUNCI:</bold> Memerlukan rank " + kit.getRequiredRank().toUpperCase() + "</red>");
            }

            lore.add("");
            lore.add("<yellow>▶ [Klik Kiri] Intip Isi Kit</yellow>");
            if (canClaim) {
                lore.add("<green>▶ [Klik Kanan] Klaim Sekarang</green>");
            }

            ItemStack kitItem = createItem(kit.getDisplayIcon(), kit.getDisplayName(), lore);
            inventory.setItem(slot, kitItem);
        }

        // Slot 49: Close
        ItemStack close = createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>", List.of("<gray>Tutup menu kits.</gray>"));
        inventory.setItem(49, close);

        // Slot 53: Admin Kit Builder Shortcut (if OP / admin)
        if (player.hasPermission("apexsions.admin") || player.isOp()) {
            ItemStack adminBtn = createItem(Material.ANVIL, "<gradient:#e74c3c:#f39c12><bold>⚙ ADMIN KIT CREATOR</bold></gradient>", List.of(
                    "<gray>Buat atau kelola kit baru secara interaktif.</gray>",
                    "<yellow>▶ Klik untuk buka Kit Builder!</yellow>"
            ));
            inventory.setItem(53, adminBtn);
        }
    }

    private ItemStack createItem(Material mat, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(mat != null ? mat : Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            if (loreLines != null) {
                List<Component> cList = new ArrayList<>();
                for (String l : loreLines) {
                    cList.add(mm.deserialize(l));
                }
                meta.lore(cList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        if (slot == 53 && (player.hasPermission("apexsions.admin") || player.isOp())) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new KitAdminCreatorGUI(plugin, player, null).open();
            return;
        }

        Kit kit = slotKitMap.get(slot);
        if (kit != null) {
            if (event.getClick() == ClickType.RIGHT) {
                // Quick Claim
                if (plugin.getKitManager().claimKit(player, kit)) {
                    buildGUI();
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
            } else {
                // Preview
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                new KitPreviewGUI(plugin, player, kit).open();
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
