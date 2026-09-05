package com.apexsions.core.kit;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Interactive Admin Drag-and-Drop GUI for creating and editing server kits with strict armor validation.
 */
public class KitAdminCreatorGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private String kitId;
    private String displayName;
    private String requiredRank = "wanderer";
    private long cooldownSeconds = 86400; // 24h default
    private Material displayIcon = Material.NETHERITE_CHESTPLATE;

    // Set Bonus Configurations
    private boolean setBonusEnabled = true;
    private KitStatType statType = KitStatType.DAMAGE_REDUCTION;
    private int requiredPieces = 4;
    private double statValue = 15.0;

    private static final List<String> RANKS = List.of(
            "wanderer", "ascendant", "archon", "sovereign", "emperor", "sions", "herald", "warden", "ancestor"
    );

    private static final List<Long> COOLDOWNS = List.of(
            3600L, 7200L, 21600L, 43200L, 86400L, 172800L, 604800L
    );

    private static final Set<Integer> EXTRA_SLOTS = Set.of(
            12, 13, 14, 15, 16,
            21, 22, 23, 24, 25,
            30, 31, 32, 33, 34
    );

    public KitAdminCreatorGUI(ApexsionsCorePlugin plugin, Player player, Kit existingKit) {
        this.plugin = plugin;
        this.player = player;
        this.kitId = existingKit != null ? existingKit.getId() : "kit_" + System.currentTimeMillis() % 10000;
        this.displayName = existingKit != null ? existingKit.getDisplayName() : "<gradient:#f1c40f:#e67e22><bold>📦 " + kitId.toUpperCase() + "</bold></gradient>";

        if (existingKit != null) {
            this.requiredRank = existingKit.getRequiredRank();
            this.cooldownSeconds = existingKit.getCooldownSeconds();
            this.displayIcon = existingKit.getDisplayIcon();
            if (existingKit.getSetBonus() != null) {
                this.setBonusEnabled = true;
                this.statType = existingKit.getSetBonus().getStatType();
                this.requiredPieces = existingKit.getSetBonus().getRequiredPieces();
                this.statValue = existingKit.getSetBonus().getValue();
            } else {
                this.setBonusEnabled = false;
            }
        }

        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>⚙ ADMIN KIT BUILDER: " + kitId.toUpperCase() + "</bold></gradient>"));
        buildGUI(existingKit);
    }

    public void open() {
        player.openInventory(inventory);
    }

    private void buildGUI(Kit existingKit) {
        inventory.clear();

        ItemStack border = createControlItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8 || i % 9 == 2) {
                inventory.setItem(i, border);
            }
        }

        // Header slot 0: Info Guide
        inventory.setItem(0, createControlItem(Material.BOOK, "<gold><bold>📜 PANDUAN KIT BUILDER</bold></gold>", List.of(
                "<gray>Aturan Penempatan Item:</gray>",
                "<yellow>1. Slot Armor:</yellow> <white>Maksimal 1 full set (Helm, Baju, Celana, Sepatu).</white>",
                "<yellow>2. Validasi Ketat:</yellow> <red>DILARANG menaruh armor di slot item ekstra!</red>",
                "<yellow>3. Set Bonus:</yellow> <aqua>Atur stat bonus di tombol bawah.</aqua>"
        )));

        // Header slot 4: Kit ID & Name
        inventory.setItem(4, createControlItem(displayIcon, displayName, List.of(
                "<gray>Kit ID:</gray> <yellow>" + kitId + "</yellow>",
                "<yellow>Klik tombol kontrol di bawah untuk mengatur rank & efek.</yellow>"
        )));

        // Armor slot headers / indicators
        inventory.setItem(1, createControlItem(Material.ORANGE_STAINED_GLASS_PANE, "<gold>Slot Armor (Maks 1 Set)</gold>", null));
        inventory.setItem(3, createControlItem(Material.CYAN_STAINED_GLASS_PANE, "<aqua>Slot Item Ekstra (Senjata/Alat/dll)</aqua>", null));

        // Place existing armor if editing
        if (existingKit != null) {
            if (existingKit.getHelmet() != null) inventory.setItem(10, existingKit.getHelmet());
            if (existingKit.getChestplate() != null) inventory.setItem(19, existingKit.getChestplate());
            if (existingKit.getLeggings() != null) inventory.setItem(28, existingKit.getLeggings());
            if (existingKit.getBoots() != null) inventory.setItem(37, existingKit.getBoots());

            List<ItemStack> extra = existingKit.getExtraItems();
            Iterator<Integer> it = EXTRA_SLOTS.iterator();
            for (ItemStack item : extra) {
                if (it.hasNext() && item != null) {
                    inventory.setItem(it.next(), item);
                }
            }
        }

        updateControlButtons();
    }

    public void updateControlButtons() {
        // Slot 8: Required Rank Selector
        inventory.setItem(8, createControlItem(Material.PLAYER_HEAD, "<gradient:#3498db:#2ecc71><bold>👑 RANK MINIMAL</bold></gradient>", List.of(
                "<gray>Pangkat yang berhak klaim:</gray>",
                "<gold><bold>" + requiredRank.toUpperCase() + "</bold></gold>",
                "",
                "<yellow>▶ Klik untuk ganti rank!</yellow>"
        )));

        // Slot 45: Cooldown Selector
        long h = cooldownSeconds / 3600;
        inventory.setItem(45, createControlItem(Material.CLOCK, "<gradient:#f39c12:#f1c40f><bold>⏳ COOLDOWN KIT</bold></gradient>", List.of(
                "<gray>Waktu tunggu klaim:</gray>",
                "<yellow><bold>" + (h >= 24 ? (h / 24) + " Hari" : h + " Jam") + "</bold></yellow>",
                "",
                "<yellow>▶ Klik untuk ubah durasi cooldown!</yellow>"
        )));

        // Slot 46: Set Bonus Stat Type Selector
        inventory.setItem(46, createControlItem(Material.ENCHANTED_BOOK, "<gradient:#9b59b6:#e74c3c><bold>✦ TIPE SET BONUS</bold></gradient>", List.of(
                "<gray>Status Set Bonus:</gray> " + (setBonusEnabled ? "<green><bold>AKTIF</bold></green>" : "<red><bold>NON-AKTIF</bold></red>"),
                "<gray>Tipe Efek:</gray> <yellow>" + statType.getDisplayName() + "</yellow>",
                "",
                "<yellow>▶ Klik Kiri: Ganti Tipe Stat</yellow>",
                "<red>▶ Klik Kanan: Toggle Aktif/Nonaktif</red>"
        )));

        // Slot 47: Required Pieces (2 or 4)
        inventory.setItem(47, createControlItem(Material.SHIELD, "<gradient:#e67e22:#d35400><bold>🛡 SYARAT KEPING ARMOR</bold></gradient>", List.of(
                "<gray>Jumlah keping dibutuhkan:</gray>",
                "<gold><bold>" + requiredPieces + " Pieces (" + (requiredPieces == 4 ? "Full Set" : "Half Set") + ")</bold></gold>",
                "",
                "<yellow>▶ Klik untuk toggle (2 atau 4 keping)!</yellow>"
        )));

        // Slot 48: Stat Value Adjuster
        inventory.setItem(48, createControlItem(Material.REDSTONE, "<gradient:#e74c3c:#c0392b><bold>⚡ PERSENTASE / NILAI EFEK</bold></gradient>", List.of(
                "<gray>Nilai Efek Diberikan:</gray>",
                "<yellow><bold>" + statType.formatValue(statValue) + "</bold></yellow>",
                "",
                "<yellow>▶ Klik untuk ubah nilai efek!</yellow>"
        )));

        // Slot 49: Batal / Keluar
        inventory.setItem(49, createControlItem(Material.BARRIER, "<red><bold>✖ BATAL</bold></red>", List.of("<gray>Tutup tanpa menyimpan.</gray>")));

        // Slot 53: Simpan Kit
        inventory.setItem(53, createControlItem(Material.EMERALD_BLOCK, "<gradient:#2ecc71:#27ae60><bold>✔ SIMPAN & DAFTARKAN KIT</bold></gradient>", List.of(
                "<gray>Validasi kelengkapan armor set & item,</gray>",
                "<gray>lalu simpan langsung ke database kits.</gray>",
                "",
                "<green>▶ Klik untuk simpan kit!</green>"
        )));
    }

    private ItemStack createControlItem(Material mat, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(mat != null ? mat : Material.STONE);
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
        int rawSlot = event.getRawSlot();

        // 1. Control Buttons Interception (Cancel click and handle control logic)
        if (rawSlot == 8) { // Cycle rank
            event.setCancelled(true);
            int idx = RANKS.indexOf(requiredRank);
            requiredRank = RANKS.get((idx + 1) % RANKS.size());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            updateControlButtons();
            return;
        }

        if (rawSlot == 45) { // Cycle cooldown
            event.setCancelled(true);
            int idx = COOLDOWNS.indexOf(cooldownSeconds);
            cooldownSeconds = COOLDOWNS.get((idx + 1) % COOLDOWNS.size());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            updateControlButtons();
            return;
        }

        if (rawSlot == 46) { // Set Bonus Toggle or Stat Type
            event.setCancelled(true);
            if (event.isRightClick()) {
                setBonusEnabled = !setBonusEnabled;
            } else {
                KitStatType[] vals = KitStatType.values();
                int idx = statType.ordinal();
                statType = vals[(idx + 1) % vals.length];
                statValue = statType.getDefaultValue();
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            updateControlButtons();
            return;
        }

        if (rawSlot == 47) { // Required pieces toggle
            event.setCancelled(true);
            requiredPieces = requiredPieces == 4 ? 2 : 4;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            updateControlButtons();
            return;
        }

        if (rawSlot == 48) { // Stat value cycle
            event.setCancelled(true);
            if (statType == KitStatType.EXTRA_MAX_HEALTH) {
                statValue = statValue >= 10.0 ? 2.0 : statValue + 2.0;
            } else {
                statValue = statValue >= 35.0 ? 10.0 : statValue + 5.0;
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            updateControlButtons();
            return;
        }

        if (rawSlot == 49) { // Cancel
            event.setCancelled(true);
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        if (rawSlot == 53) { // Save Kit
            event.setCancelled(true);
            saveKitAndClose();
            return;
        }

        // Prevent clicking on borders / header slots
        if (rawSlot < 9 || (rawSlot >= 45 && rawSlot <= 53) || rawSlot % 9 == 0 || rawSlot % 9 == 8 || rawSlot % 9 == 2) {
            event.setCancelled(true);
            return;
        }

        // 2. STRICT ARMOR VALIDATION CHECK
        ItemStack cursorItem = event.getCursor();
        if (cursorItem != null && cursorItem.getType() != Material.AIR) {
            // Check Armor Slots
            if (rawSlot == 10) { // Helmet Slot
                if (!isHelmet(cursorItem)) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red><bold>❌ DITOLAK!</bold> Slot ini hanya menerima <yellow>HELMET</yellow>!</red>"));
                    return;
                }
            } else if (rawSlot == 19) { // Chestplate Slot
                if (!isChestplate(cursorItem)) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red><bold>❌ DITOLAK!</bold> Slot ini hanya menerima <yellow>CHESTPLATE</yellow>!</red>"));
                    return;
                }
            } else if (rawSlot == 28) { // Leggings Slot
                if (!isLeggings(cursorItem)) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red><bold>❌ DITOLAK!</bold> Slot ini hanya menerima <yellow>LEGGINGS</yellow>!</red>"));
                    return;
                }
            } else if (rawSlot == 37) { // Boots Slot
                if (!isBoots(cursorItem)) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red><bold>❌ DITOLAK!</bold> Slot ini hanya menerima <yellow>BOOTS</yellow>!</red>"));
                    return;
                }
            } else if (EXTRA_SLOTS.contains(rawSlot)) {
                // Extra items slot: STRICTLY REJECT ARMOR TO PREVENT DUPLICATES
                if (isArmor(cursorItem)) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red><bold>❌ DITOLAK!</bold> Tidak boleh memasukkan keping armor ke slot item ekstra! Armor hanya boleh 1 full set di slot khusus sebelah kiri.</red>"));
                    return;
                }
            }
        }
    }

    private void saveKitAndClose() {
        ItemStack helm = inventory.getItem(10);
        ItemStack chest = inventory.getItem(19);
        ItemStack legs = inventory.getItem(28);
        ItemStack boots = inventory.getItem(37);

        // Final sanity check: verify no duplicates or illegal items in extra slots
        List<ItemStack> extra = new ArrayList<>();
        for (int slot : EXTRA_SLOTS) {
            ItemStack it = inventory.getItem(slot);
            if (it != null && it.getType() != Material.AIR) {
                if (isArmor(it)) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red><bold>❌ GAGAL MENYIMPAN:</bold> Ditemukan keping armor di slot ekstra (slot " + slot + ")! Hapus armor duplikat terlebih dahulu.</red>"));
                    return;
                }
                extra.add(it.clone());
            }
        }

        // Determine icon
        Material icon = Material.CHEST;
        if (chest != null) icon = chest.getType();
        else if (helm != null) icon = helm.getType();
        else if (!extra.isEmpty()) icon = extra.get(0).getType();

        Kit kit = new Kit(kitId, displayName, requiredRank, cooldownSeconds, icon);
        kit.setHelmet(helm);
        kit.setChestplate(chest);
        kit.setLeggings(legs);
        kit.setBoots(boots);
        kit.setExtraItems(extra);

        if (setBonusEnabled) {
            KitArmorSetBonus bonus = new KitArmorSetBonus(kitId, kitId.substring(0, 1).toUpperCase() + kitId.substring(1) + " Set", statType, statValue, requiredPieces);
            kit.setSetBonus(bonus);
        } else {
            kit.setSetBonus(null);
        }

        plugin.getKitManager().saveKit(kit);
        player.closeInventory();
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        player.sendMessage(mm.deserialize("<gradient:#2ecc71:#f1c40f><bold>═════════════════════════════════════════════════</bold></gradient>"));
        player.sendMessage(mm.deserialize("<green><bold>✓ KIT '" + kitId.toUpperCase() + "' BERHASIL DISIMPAN & DIAKTIFKAN!</bold></green>"));
        player.sendMessage(mm.deserialize("<gray>Rank Syarat: <gold>" + requiredRank.toUpperCase() + "</gold> | Cooldown: <yellow>" + (cooldownSeconds / 3600) + " Jam</yellow></gray>"));
        if (setBonusEnabled) {
            player.sendMessage(mm.deserialize("<gray>Set Bonus: <yellow>" + statType.formatValue(statValue) + " " + statType.getDisplayName() + " (" + requiredPieces + " Pieces)</yellow></gray>"));
        }
        player.sendMessage(mm.deserialize("<gradient:#2ecc71:#f1c40f><bold>═════════════════════════════════════════════════</bold></gradient>"));
    }

    public static boolean isHelmet(ItemStack item) {
        if (item == null) return false;
        String name = item.getType().name();
        return name.endsWith("_HELMET") || item.getType() == Material.TURTLE_HELMET;
    }

    public static boolean isChestplate(ItemStack item) {
        if (item == null) return false;
        String name = item.getType().name();
        return name.endsWith("_CHESTPLATE") || item.getType() == Material.ELYTRA;
    }

    public static boolean isLeggings(ItemStack item) {
        if (item == null) return false;
        return item.getType().name().endsWith("_LEGGINGS");
    }

    public static boolean isBoots(ItemStack item) {
        if (item == null) return false;
        return item.getType().name().endsWith("_BOOTS");
    }

    public static boolean isArmor(ItemStack item) {
        return isHelmet(item) || isChestplate(item) || isLeggings(item) || isBoots(item);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
