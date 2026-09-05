package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.tools.ToolStatType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

/**
 * Interactive GUI for configuring custom Tool & Weapon set bonuses.
 * Zero hardcoded default template; admins add/remove precisely what they want.
 */
public class ToolBonusPickerGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final ItemStack item;
    private final InventoryHolder returnGUI;
    private final Consumer<ItemStack> onUpdate;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    // State
    private String setId = "";
    private String setName = "";
    private final Map<ToolStatType, Double> activeStats = new LinkedHashMap<>();

    public ToolBonusPickerGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item,
                              String currentSetId, String currentSetName,
                              InventoryHolder returnGUI, Consumer<ItemStack> onUpdate) {
        this.plugin = plugin;
        this.player = player;
        this.item = item;
        this.setId = currentSetId != null ? currentSetId : "";
        this.setName = currentSetName != null ? currentSetName : "";
        this.returnGUI = returnGUI;
        this.onUpdate = onUpdate;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#3498db:#e67e22><bold>⚔ PENGATURAN TOOL SET BONUS ⚔</bold></gradient>"));

        readExistingToolBonus();
        buildGUI();
    }

    private void readExistingToolBonus() {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        NamespacedKey kId = new NamespacedKey("apexsions", "set_id");
        if (pdc.has(kId, PersistentDataType.STRING)) {
            this.setId = pdc.get(kId, PersistentDataType.STRING);
        }
        NamespacedKey kName = new NamespacedKey("apexsions", "set_name");
        if (pdc.has(kName, PersistentDataType.STRING)) {
            this.setName = pdc.get(kName, PersistentDataType.STRING);
        }

        NamespacedKey kStats = new NamespacedKey("apexsions", "tool_stats");
        if (pdc.has(kStats, PersistentDataType.STRING)) {
            String raw = pdc.get(kStats, PersistentDataType.STRING);
            if (raw != null && !raw.isBlank()) {
                for (String p : raw.split(";")) {
                    String[] kv = p.split(":");
                    if (kv.length == 2) {
                        try {
                            ToolStatType st = ToolStatType.valueOf(kv[0].trim());
                            double val = Double.parseDouble(kv[1].trim());
                            activeStats.put(st, val);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null, false);
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, border);
        }

        // Slot 4: Info Header
        String sName = (setName == null || setName.isBlank()) ? "<dark_gray>(Belum Diatur)</dark_gray>" : "<gold>" + setName + "</gold>";
        String sId = (setId == null || setId.isBlank()) ? "kosong" : setId;

        inventory.setItem(4, createItem(Material.NETHERITE_SWORD,
                "<gradient:#f1c40f:#e67e22><bold>⚔ KONFIGURASI BONUS TOOL / SENJATA ⚔</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Set Armor Penyesuai: " + sName + "</gray>"),
                        mm.deserialize("<gray>ID Set: <yellow>" + sId + "</yellow></gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>Bonus ini HANYA aktif bila pemain mengenakan set armor di atas!</yellow>"),
                        mm.deserialize("<gray>Stat aktif saat ini: <gold>" + activeStats.size() + " Efek</gold></gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik Kiri stat untuk mengatur persentase nilainya.</yellow>"),
                        mm.deserialize("<red>▶ Klik R-Click stat untuk menghapusnya.</red>")
                ), true));

        // Available stats tailored to item
        List<ToolStatType> available = getAvailableStatsForItem(item);
        int[] slots = {19, 20, 21, 22, 23, 24, 25, 29, 30, 31, 32, 33};

        for (int i = 0; i < available.size() && i < slots.length; i++) {
            ToolStatType type = available.get(i);
            int slot = slots[i];
            addStatButton(slot, type);
        }

        // Slot 41: Clear All
        inventory.setItem(41, createItem(Material.LAVA_BUCKET,
                "<red><bold>✖ HAPUS SELURUH BONUS DARI TOOL INI</bold></red>",
                List.of(
                        mm.deserialize("<gray>Hapus seluruh bonus set dari tool/senjata ini.</gray>"),
                        Component.empty(),
                        mm.deserialize("<red>▶ Klik untuk mengosongkan bonus tool!</red>")
                ), false));

        // Slot 45: Back
        inventory.setItem(45, createItem(Material.ARROW,
                "<gradient:#3498db:#2980b9><bold>⬅ KEMBALI</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Kembali ke menu sebelumnya.</gray>")
                ), false));

        // Slot 49: Apply
        List<Component> applyLore = new ArrayList<>();
        applyLore.add(mm.deserialize("<gray>Set Armor Terkait: <gold>" + (setName.isBlank() ? "Custom Set" : setName) + "</gold></gray>"));
        applyLore.add(Component.empty());
        if (activeStats.isEmpty()) {
            applyLore.add(mm.deserialize("<red>● Tidak ada bonus tool yang aktif (Dikosongkan).</red>"));
        } else {
            applyLore.add(mm.deserialize("<green>● Efek yang akan diterapkan (" + activeStats.size() + " Efek):</green>"));
            for (Map.Entry<ToolStatType, Double> e : activeStats.entrySet()) {
                applyLore.add(mm.deserialize("<aqua>  - " + e.getKey().getDisplayName() + ": <gold>" + e.getKey().formatValue(e.getValue()) + "</gold></aqua>"));
            }
        }
        applyLore.add(Component.empty());
        applyLore.add(mm.deserialize("<green><bold>▶ Klik untuk simpan & terapkan ke tool!</bold></green>"));

        inventory.setItem(49, createItem(Material.EMERALD_BLOCK,
                "<gradient:#2ecc71:#27ae60><bold>✔ TERAPKAN KE TOOL</bold></gradient>",
                applyLore, !activeStats.isEmpty()));
    }

    private List<ToolStatType> getAvailableStatsForItem(ItemStack is) {
        List<ToolStatType> list = new ArrayList<>();
        if (is == null) return List.of(ToolStatType.values());

        String n = is.getType().name();
        boolean isSwordOrRanged = n.endsWith("_SWORD") || is.getType() == Material.BOW
                || is.getType() == Material.CROSSBOW || is.getType() == Material.TRIDENT || is.getType() == Material.MACE;
        boolean isAxe = n.endsWith("_AXE");

        if (isSwordOrRanged || isAxe) {
            list.add(ToolStatType.WEAPON_DAMAGE_BOOST);
            list.add(ToolStatType.ATTACK_SPEED_BOOST);
            list.add(ToolStatType.CRITICAL_DAMAGE_BOOST);
            list.add(ToolStatType.ATTACK_REACH_BOOST);
            list.add(ToolStatType.UNBREAKABLE_SET);
        }

        if (!isSwordOrRanged) {
            list.add(ToolStatType.MINING_REACH_BOOST);
            list.add(ToolStatType.EXP_MULTIPLIER);
            if (!list.contains(ToolStatType.UNBREAKABLE_SET)) {
                list.add(ToolStatType.UNBREAKABLE_SET);
            }
            list.add(ToolStatType.FATIGUE_IMMUNITY);
        }

        return list;
    }

    private void addStatButton(int slot, ToolStatType type) {
        boolean isActive = activeStats.containsKey(type);
        double val = activeStats.getOrDefault(type, 0.0);

        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<gray>Stat: <gold>" + type.getDisplayName() + "</gold></gray>"));
        lore.add(Component.empty());
        if (isActive) {
            lore.add(mm.deserialize("<green>● Status: AKTIF (<gold>" + type.formatValue(val) + "</gold>)</green>"));
        } else {
            lore.add(mm.deserialize("<dark_gray>● Status: Non-aktif</dark_gray>"));
        }
        lore.add(Component.empty());
        lore.add(mm.deserialize("<yellow>▶ Klik Kiri: Buka GUI atur nilai/persentase</yellow>"));
        if (isActive) {
            lore.add(mm.deserialize("<red>▶ Klik R-Click: Hapus efek ini</red>"));
        }

        String title = (isActive ? "<green><bold>[✓] " : "<gray>") + type.getDisplayName() + (isActive ? "</bold></green>" : "</gray>");
        inventory.setItem(slot, createItem(type.getIcon(), title, lore, isActive));
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        ClickType click = event.getClick();

        // Back
        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            safeReturn();
            return;
        }

        // Clear All
        if (slot == 41) {
            activeStats.clear();
            removeToolBonusFromItem();
            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<red>Seluruh bonus set pada tool ini telah dihapus.</red>"));
            buildGUI();
            return;
        }

        // Apply
        if (slot == 49) {
            if (activeStats.isEmpty()) {
                removeToolBonusFromItem();
                player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<yellow>Tidak ada bonus tool yang dipilih. Bonus dikosongkan.</yellow>"));
            } else {
                applyToolBonusToItem();
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
                player.sendMessage(mm.deserialize("<green><bold>✓ BERHASIL!</bold> Bonus Tool Set berhasil diterapkan!</green>"));
            }
            safeReturn();
            return;
        }

        // Check stat clicks
        List<ToolStatType> available = getAvailableStatsForItem(item);
        int[] slots = {19, 20, 21, 22, 23, 24, 25, 29, 30, 31, 32, 33};

        for (int i = 0; i < available.size() && i < slots.length; i++) {
            if (slot == slots[i]) {
                ToolStatType type = available.get(i);
                if (click.isRightClick()) {
                    if (activeStats.containsKey(type)) {
                        activeStats.remove(type);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                        player.sendMessage(mm.deserialize("<red>Stat <gold>" + type.getDisplayName() + "</gold> dihapus.</red>"));
                        buildGUI();
                    }
                } else {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                    double cur = activeStats.getOrDefault(type, type.getDefaultValue());
                    new ToolStatValuePickerGUI(plugin, player, type, cur, this, selectedVal -> {
                        if (selectedVal <= 0.0) {
                            activeStats.remove(type);
                        } else {
                            activeStats.put(type, selectedVal);
                        }
                    }).open();
                }
                return;
            }
        }
    }

    private void safeReturn() {
        if (onUpdate != null && item != null) {
            onUpdate.accept(item);
        }
        if (returnGUI instanceof ItemModifierGUI modifier) {
            modifier.open();
        } else if (returnGUI instanceof AdminItemCreatorGUI creator) {
            creator.open();
        } else if (returnGUI != null) {
            player.openInventory(returnGUI.getInventory());
        } else {
            player.closeInventory();
        }
    }

    private void applyToolBonusToItem() {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String validId = setId.isBlank() ? "custom_set" : setId.toLowerCase().replaceAll("[^a-z0-9_-]", "_");
        pdc.set(new NamespacedKey("apexsions", "set_id"), PersistentDataType.STRING, validId);
        if (!setName.isBlank()) {
            pdc.set(new NamespacedKey("apexsions", "set_name"), PersistentDataType.STRING, setName);
        }
        pdc.set(new NamespacedKey("apexsions", "tool_bonus"), PersistentDataType.BYTE, (byte) 1);

        // Serialize stats
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ToolStatType, Double> e : activeStats.entrySet()) {
            if (!sb.isEmpty()) sb.append(";");
            sb.append(e.getKey().name()).append(":").append(e.getValue());
        }
        pdc.set(new NamespacedKey("apexsions", "tool_stats"), PersistentDataType.STRING, sb.toString());

        // Update Lore
        List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.removeIf(c -> {
            String plain = mm.serialize(c);
            return plain.contains("TOOL SET BONUS") || plain.contains("WEAPON SET BONUS") || plain.contains("Syarat: Memakai Set Armor");
        });

        lore.add(Component.empty());
        String headerTitle = AdminItemCreatorGUI.isWeapon(item) ? "WEAPON SET BONUS" : "TOOL SET BONUS";
        lore.add(mm.deserialize("<gradient:#e74c3c:#f39c12><bold>★ " + headerTitle + (setName.isBlank() ? "" : ": " + setName.toUpperCase()) + " ★</bold></gradient>"));
        lore.add(mm.deserialize("<gray>Syarat: <gold>Memakai Set Armor " + (setName.isBlank() ? "Terkait" : setName) + "</gold></gray>"));
        for (Map.Entry<ToolStatType, Double> e : activeStats.entrySet()) {
            lore.add(mm.deserialize("<gray>  ● Efek: <aqua>" + e.getKey().getDisplayName() + " " + e.getKey().formatValue(e.getValue()) + "</aqua></gray>"));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
    }

    private void removeToolBonusFromItem() {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.remove(new NamespacedKey("apexsions", "tool_bonus"));
        pdc.remove(new NamespacedKey("apexsions", "tool_stats"));

        List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.removeIf(c -> {
            String plain = mm.serialize(c);
            return plain.contains("TOOL SET BONUS") || plain.contains("WEAPON SET BONUS") || plain.contains("Syarat: Memakai Set Armor");
        });
        meta.lore(lore);
        item.setItemMeta(meta);
    }

    private ItemStack createItem(Material mat, String name, List<Component> lore, boolean glow) {
        ItemStack is = new ItemStack(mat);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            if (name != null) meta.displayName(mm.deserialize(name));
            if (lore != null) meta.lore(lore);
            if (glow) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            is.setItemMeta(meta);
        }
        return is;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
