package com.apexsions.core.kit;

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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-only GUI to preview the exact contents and stats of a kit.
 */
public class KitPreviewGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Kit kit;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public KitPreviewGUI(ApexsionsCorePlugin plugin, Player player, Kit kit) {
        this.plugin = plugin;
        this.player = player;
        this.kit = kit;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#f1c40f:#e67e22><bold>📦 PREVIEW KIT: " + kit.getId().toUpperCase() + "</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Header slot 4
        ItemStack info = createItem(kit.getDisplayIcon(), kit.getDisplayName(), List.of(
                "<gray>Rank Syarat:</gray> <gold>" + kit.getRequiredRank().toUpperCase() + "</gold>",
                "<gray>Cooldown:</gray> <yellow>" + (kit.getCooldownSeconds() / 3600) + " Jam</yellow>",
                kit.getSetBonus() != null ? "<gradient:#f1c40f:#e67e22><bold>✦ Set Bonus:</bold> " + kit.getSetBonus().getStatType().formatValue(kit.getSetBonus().getValue()) + " " + kit.getSetBonus().getStatType().getDisplayName() + "</gradient>" : "<gray>Tidak memiliki set bonus</gray>"
        ));
        inventory.setItem(4, info);

        // Armor Row Slots: 10 (Helm), 11 (Chest), 12 (Legs), 13 (Boots)
        inventory.setItem(10, kit.getHelmet() != null ? plugin.getKitManager().prepareArmorPiece(kit, kit.getHelmet(), "Helmet") : createPlaceholder("HELMET", "Tidak ada helm"));
        inventory.setItem(11, kit.getChestplate() != null ? plugin.getKitManager().prepareArmorPiece(kit, kit.getChestplate(), "Chestplate") : createPlaceholder("CHESTPLATE", "Tidak ada baju"));
        inventory.setItem(12, kit.getLeggings() != null ? plugin.getKitManager().prepareArmorPiece(kit, kit.getLeggings(), "Leggings") : createPlaceholder("LEGGINGS", "Tidak ada celana"));
        inventory.setItem(13, kit.getBoots() != null ? plugin.getKitManager().prepareArmorPiece(kit, kit.getBoots(), "Boots") : createPlaceholder("BOOTS", "Tidak ada sepatu"));

        // Extra items separator at slot 14-16
        ItemStack decor = createItem(Material.GRAY_STAINED_GLASS_PANE, "<gray>↓ Item Ekstra ↓</gray>", null);
        inventory.setItem(14, decor);
        inventory.setItem(15, decor);
        inventory.setItem(16, decor);

        // Extra items placed in slots 19-25, 28-34, 37-43
        int[] extraSlots = {
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        List<ItemStack> extra = kit.getExtraItems();
        for (int i = 0; i < extra.size() && i < extraSlots.length; i++) {
            ItemStack it = extra.get(i);
            if (it != null && it.getType() != Material.AIR) {
                inventory.setItem(extraSlots[i], it.clone());
            }
        }

        // Bottom Controls
        // Slot 48: Kembali ke /kits
        ItemStack back = createItem(Material.ARROW, "<yellow><bold>◀ KEMBALI KE DAFTAR KITS</bold></yellow>", List.of("<gray>Buka menu /kits utama.</gray>"));
        inventory.setItem(48, back);

        // Slot 50: Klaim Kit Sekarang
        boolean canClaim = plugin.getKitManager().canClaim(player, kit);
        long cd = plugin.getKitManager().getRemainingCooldownSeconds(player, kit);
        ItemStack claim;
        if (canClaim) {
            claim = createItem(Material.EMERALD, "<green><bold>✔ KLAIM KIT SEKARANG</bold></green>", List.of("<gray>Klik untuk mengambil kit ini langsung ke tasmu!</gray>"));
        } else if (cd > 0) {
            claim = createItem(Material.CLOCK, "<red><bold>⏳ SEDANG COOLDOWN</bold></red>", List.of("<gray>Tunggu:</gray> <yellow>" + plugin.getKitManager().formatRemainingCooldown(cd) + "</yellow>"));
        } else {
            claim = createItem(Material.BARRIER, "<red><bold>✖ RANK BELUM MENCUKUPI</bold></red>", List.of("<gray>Memerlukan rank:</gray> <gold>" + kit.getRequiredRank().toUpperCase() + "</gold>"));
        }
        inventory.setItem(50, claim);
    }

    private ItemStack createPlaceholder(String type, String desc) {
        Material mat = switch (type) {
            case "HELMET" -> Material.CHAINMAIL_HELMET;
            case "CHESTPLATE" -> Material.CHAINMAIL_CHESTPLATE;
            case "LEGGINGS" -> Material.CHAINMAIL_LEGGINGS;
            default -> Material.CHAINMAIL_BOOTS;
        };
        return createItem(mat, "<gray><italic>" + desc + "</italic></gray>", null);
    }

    private ItemStack createItem(Material mat, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(mat);
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

        if (slot == 48) { // Back to /kits
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            new KitUserGUI(plugin, player).open();
            return;
        }

        if (slot == 50) { // Claim
            if (plugin.getKitManager().claimKit(player, kit)) {
                player.closeInventory();
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
