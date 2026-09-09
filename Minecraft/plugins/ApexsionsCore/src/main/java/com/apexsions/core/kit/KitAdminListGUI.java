package com.apexsions.core.kit;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.gui.admin.CoreAdminSubGUI;
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
 * Dedicated 54-slot Interactive Administration Dashboard for Server Kits.
 * Exclusively accessible by server staff & administrators.
 * Provides live kit inspection, creation, inline editing, testing, cooldown reset, and deletion.
 */
public class KitAdminListGUI implements InventoryHolder {

    private static final int[] KIT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<Integer, Kit> slotKitMap = new HashMap<>();
    private int page = 0;

    public KitAdminListGUI(ApexsionsCorePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>⚙ ADMIN KIT MANAGEMENT ⚙</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotKitMap.clear();

        // 1. Borders & Background
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        ItemStack cornerDecor = createItem(Material.RED_STAINED_GLASS_PANE, "<gold>✦</gold>", null);

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }
        inventory.setItem(0, cornerDecor);
        inventory.setItem(8, cornerDecor);
        inventory.setItem(45, cornerDecor);
        inventory.setItem(53, cornerDecor);

        List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());
        int totalKits = allKits.size();
        int maxPages = Math.max(1, (int) Math.ceil((double) totalKits / KIT_SLOTS.length));
        if (page >= maxPages) page = maxPages - 1;
        if (page < 0) page = 0;

        // 2. Header Slot 4: Admin Hub Status
        ItemStack header = createItem(Material.NETHER_STAR, "<gradient:#e74c3c:#f39c12><bold>👑 DIREKTORI KITS SERVER 👑</bold></gradient>", List.of(
                "<gray>Total Kits Terdaftar:</gray> <yellow>" + totalKits + " Kits</yellow>",
                "<gray>Halaman:</gray> <gold>" + (page + 1) + " / " + maxPages + "</gold>",
                "<gray>Hak Akses:</gray> <red>Administrator Master</red>",
                "",
                "<yellow>Kelola, uji coba, edit, atau buat kit baru dari panel ini.</yellow>"
        ));
        inventory.setItem(4, header);

        // 3. Populate Kits for Current Page
        int startIndex = page * KIT_SLOTS.length;
        for (int i = 0; i < KIT_SLOTS.length; i++) {
            int kitIndex = startIndex + i;
            if (kitIndex >= totalKits) break;

            Kit kit = allKits.get(kitIndex);
            int slot = KIT_SLOTS[i];
            slotKitMap.put(slot, kit);

            int armorCount = (kit.getHelmet() != null ? 1 : 0)
                    + (kit.getChestplate() != null ? 1 : 0)
                    + (kit.getLeggings() != null ? 1 : 0)
                    + (kit.getBoots() != null ? 1 : 0);

            List<String> lore = new ArrayList<>();
            lore.add("<gray>ID Internal:</gray> <white>" + kit.getId() + "</white>");
            lore.add("<gray>Syarat Rank:</gray> <gold>" + kit.getRequiredRank().toUpperCase() + "</gold>");
            lore.add("<gray>Cooldown:</gray> <yellow>" + (kit.getCooldownSeconds() / 3600) + " Jam (" + kit.getCooldownSeconds() + "s)</yellow>");
            lore.add("<gray>Perlengkapan Armor:</gray> <white>" + armorCount + "/4 Potong</white>");
            lore.add("<gray>Item Ekstra:</gray> <white>" + kit.getExtraItems().size() + " Item</white>");

            if (kit.getSetBonus() != null) {
                KitArmorSetBonus bonus = kit.getSetBonus();
                lore.add("");
                lore.add("<gradient:#f1c40f:#e67e22><bold>✦ Set Bonus (" + bonus.getRequiredPieces() + " Set):</bold></gradient>");
                lore.add("<dark_gray>•</dark_gray> <gray>Efek:</gray> <yellow>" + bonus.getStatType().formatValue(bonus.getValue()) + " " + bonus.getStatType().getDisplayName() + "</yellow>");
            }

            lore.add("");
            lore.add("<gold><bold>Aksi Administrator:</bold></gold>");
            lore.add("<yellow>▶ [Klik Kiri] Edit Kit (Buka Kit Builder)</yellow>");
            lore.add("<green>▶ [Klik Kanan] Test Give Kit ke Tas Sendiri</green>");
            lore.add("<aqua>▶ [Tekan Q / Drop] Reset Cooldown Kit untuk Diri</aqua>");
            lore.add("<red>▶ [Shift + Klik Kanan] Hapus Kit Permanen</red>");

            ItemStack kitCard = createItem(kit.getDisplayIcon(), kit.getDisplayName(), lore);
            inventory.setItem(slot, kitCard);
        }

        // 4. Action Controls on Bottom Bar
        // Slot 46: Create New Kit
        ItemStack createBtn = createItem(Material.EMERALD_BLOCK, "<gradient:#2ecc71:#27ae60><bold>➕ BUAT KIT BARU</bold></gradient>", List.of(
                "<gray>Buka Interactive Kit Creator untuk merancang</gray>",
                "<gray>kit baru dengan drag & drop armor dan item ekstra.</gray>",
                "",
                "<yellow>▶ Klik untuk buka Kit Creator!</yellow>"
        ));
        inventory.setItem(46, createBtn);

        // Slot 48: Previous Page (if applicable)
        if (page > 0) {
            ItemStack prevBtn = createItem(Material.ARROW, "<yellow><bold>◀ HALAMAN SEBELUMNYA</bold></yellow>", List.of(
                    "<gray>Beralih ke halaman " + page + ".</gray>"
            ));
            inventory.setItem(48, prevBtn);
        }

        // Slot 49: Back to Core Admin Hub
        ItemStack backBtn = createItem(Material.OAK_DOOR, "<red><bold>◀ KEMBALI KE ADMIN HUB</bold></red>", List.of(
                "<gray>Kembali ke menu kontrol Apexsions Core.</gray>"
        ));
        inventory.setItem(49, backBtn);

        // Slot 50: Next Page (if applicable)
        if (page + 1 < maxPages) {
            ItemStack nextBtn = createItem(Material.ARROW, "<yellow><bold>HALAMAN BERIKUTNYA ▶</bold></yellow>", List.of(
                    "<gray>Beralih ke halaman " + (page + 2) + ".</gray>"
            ));
            inventory.setItem(50, nextBtn);
        }

        // Slot 52: Reload Kits from Disk
        ItemStack reloadBtn = createItem(Material.REDSTONE, "<gradient:#f39c12:#e67e22><bold>🔄 MUAT ULANG KITS (RELOAD)</bold></gradient>", List.of(
                "<gray>Baca ulang file kits.yml dan cooldowns.yml dari disk.</gray>",
                "",
                "<yellow>▶ Klik untuk reload!</yellow>"
        ));
        inventory.setItem(52, reloadBtn);
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

        // 1. Back button (Slot 49)
        if (slot == 49) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            player.closeInventory();
            new CoreAdminSubGUI(plugin, player).open();
            return;
        }

        // 2. Create Kit (Slot 46)
        if (slot == 46) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            player.closeInventory();
            new KitAdminCreatorGUI(plugin, player, null).open();
            return;
        }

        // 3. Navigation
        if (slot == 48 && page > 0) {
            page--;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.1f);
            buildGUI();
            return;
        }
        if (slot == 50) {
            int maxPages = Math.max(1, (int) Math.ceil((double) plugin.getKitManager().getAllKits().size() / KIT_SLOTS.length));
            if (page + 1 < maxPages) {
                page++;
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.1f);
                buildGUI();
                return;
            }
        }

        // 4. Reload Kits (Slot 52)
        if (slot == 52) {
            plugin.getKitManager().loadKits();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
            player.sendMessage(mm.deserialize("<green>✓ Berhasil memuat ulang seluruh kit dari kits.yml!</green>"));
            buildGUI();
            return;
        }

        // 5. Kit card actions
        Kit kit = slotKitMap.get(slot);
        if (kit != null) {
            ClickType click = event.getClick();

            // Action A: Shift + Right Click -> Delete Kit
            if (click == ClickType.SHIFT_RIGHT) {
                if (plugin.getKitManager().deleteKit(kit.getId())) {
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<green>✓ Kit <gold>'" + kit.getId() + "'</gold> berhasil dihapus permanen!</green>"));
                    buildGUI();
                } else {
                    player.sendMessage(mm.deserialize("<red>Gagal menghapus kit '" + kit.getId() + "'!</red>"));
                }
                return;
            }

            // Action B: Drop / Control Drop -> Reset Cooldown for Self
            if (click == ClickType.DROP || click == ClickType.CONTROL_DROP) {
                plugin.getKitManager().resetCooldown(player.getUniqueId(), kit.getId());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
                player.sendMessage(mm.deserialize("<aqua>✓ Cooldown kit <gold>'" + kit.getId() + "'</gold> untuk dirimu berhasil di-reset!</aqua>"));
                return;
            }

            // Action C: Right Click -> Test Give to Self
            if (click == ClickType.RIGHT) {
                plugin.getKitManager().giveKitDirect(player, kit);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
                player.sendMessage(mm.deserialize("<green>✓ Berhasil memberikan kit <gold>'" + kit.getId() + "'</gold> ke tasmu (Test Mode)!</green>"));
                return;
            }

            // Action D: Left Click -> Edit Kit
            if (click == ClickType.LEFT) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                player.closeInventory();
                new KitAdminCreatorGUI(plugin, player, kit).open();
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
