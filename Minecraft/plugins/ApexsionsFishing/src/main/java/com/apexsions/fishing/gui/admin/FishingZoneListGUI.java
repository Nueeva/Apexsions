package com.apexsions.fishing.gui.admin;

import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.integration.WorldEditHook;
import com.apexsions.fishing.model.FishingZone;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
 * Admin GUI for inspecting and managing all WorldEdit-defined Fishing Zones.
 */
public class FishingZoneListGUI implements InventoryHolder {

    private final ApexsionsFishing plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final List<FishingZone> zoneList = new ArrayList<>();

    public static final int SLOT_BACK = 49;
    public static final int SLOT_CREATE_INFO = 4;

    public FishingZoneListGUI(@NotNull ApexsionsFishing plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#00c6ff:#0072ff><bold>Pengelola Zona Mancing</bold></gradient>"));
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
        zoneList.clear();
        zoneList.addAll(plugin.getZoneManager().getAllZones());

        // Decorative borders
        ItemStack border = createGuiItem(Material.CYAN_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < 9; i++) inventory.setItem(i, border);
        for (int i = 45; i < 54; i++) inventory.setItem(i, border);
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, border);
            inventory.setItem(i + 8, border);
        }

        // Slot 4: Info / Create from WorldEdit
        boolean weAvailable = WorldEditHook.isAvailable();
        inventory.setItem(SLOT_CREATE_INFO, createGuiItem(
                Material.GOLDEN_AXE,
                "<gradient:#ffd700:#ffaa00><bold>⚡ Panduan Pembuatan Area WorldEdit</bold></gradient>",
                List.of(
                        "<gray>Integrasi WorldEdit: " + (weAvailable ? "<green><bold>TERHUBUNG</bold></green>" : "<red><bold>TIDAK DITEMUKAN</bold></red>") + "</gray>",
                        "<gray>Total Area Aktif: <yellow><bold>" + zoneList.size() + " Zona</bold></yellow></gray>",
                        "",
                        "<yellow>Langkah membuat area baru:</yellow>",
                        "<white>1. Gunakan wand (//wand) untuk memilih Pos 1 & Pos 2</white>",
                        "<white>2. Ketik perintah pembuatan:</white>",
                        "<aqua>   /fish zone create <nama> [rate] [rarity]</aqua>",
                        "<gray>   Contoh: <white>/fish zone create danau_vip 2.0 1.5</white></gray>"
                )
        ));

        // Render Zones in inner slots (10-16, 19-25, 28-34, 37-43)
        int[] itemSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        for (int i = 0; i < zoneList.size() && i < itemSlots.length; i++) {
            FishingZone zone = zoneList.get(i);
            Material iconMat = zone.isAfkAllowed() ? Material.TROPICAL_FISH : Material.PUFFERFISH;

            List<String> lore = new ArrayList<>();
            lore.add("<gray>ID Teknis: <white>" + zone.getName() + "</white></gray>");
            lore.add("<gray>Dunia: <yellow>" + zone.getWorldName() + "</yellow></gray>");
            lore.add("<gray>Rentang Koordinat:</gray>");
            lore.add("<aqua>  (" + zone.getMinX() + ", " + zone.getMinY() + ", " + zone.getMinZ() + ") s/d (" +
                    zone.getMaxX() + ", " + zone.getMaxY() + ", " + zone.getMaxZ() + ")</aqua>");
            lore.add("<gray>Volume Area: <gold>" + String.format("%,d", zone.getVolume()) + " Blok</gold></gray>");
            lore.add("");
            lore.add("<yellow>✦ Pengganda / Multipliers:</yellow>");
            lore.add("<gray>  • AFK Fishing: " + (zone.isAfkAllowed() ? "<green><bold>AKTIF</bold></green>" : "<red><bold>NON-AKTIF</bold></red>") + "</gray>");
            lore.add("<gray>  • Kecepatan Sambaran (Rate): <yellow><bold>x" + zone.getRateMultiplier() + "</bold></yellow></gray>");
            lore.add("<gray>  • Peluang Ikan Langka (Rarity): <gold><bold>x" + zone.getRarityMultiplier() + "</bold></gold></gray>");
            lore.add("<gray>  • Bobot Tangkapan (Weight): <aqua><bold>x" + zone.getWeightMultiplier() + "</bold></aqua></gray>");
            lore.add("<gray>  • Bonus Level EXP: <light_purple><bold>x" + zone.getXpMultiplier() + "</bold></light_purple></gray>");
            lore.add("");
            lore.add("<green>▶ Klik Kiri:</green> <gray>Teleportasi ke Tengah Zona</gray>");
            lore.add("<yellow>▶ Klik Kanan:</yellow> <gray>Toggle Izin AFK Fishing</gray>");
            lore.add("<red>▶ Shift + Klik Kanan:</red> <gray>Hapus Zona</gray>");

            inventory.setItem(itemSlots[i], createGuiItem(iconMat, zone.getDisplayName(), lore));
        }

        // Slot 49: Back to Admin Hub
        inventory.setItem(SLOT_BACK, createGuiItem(
                Material.ARROW,
                "<red><bold>⬅ Kembali ke Admin Hub</bold></red>",
                List.of("<gray>Klik untuk kembali ke menu admin utama.</gray>")
        ));
    }

    public void handleClick(@NotNull InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == SLOT_BACK) {
            new FishingAdminHubGUI(plugin, player).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return;
        }

        int[] itemSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        for (int i = 0; i < itemSlots.length && i < zoneList.size(); i++) {
            if (itemSlots[i] == slot) {
                FishingZone zone = zoneList.get(i);

                if (event.isShiftClick() && event.isRightClick()) {
                    // Delete Zone
                    plugin.getZoneManager().deleteZone(zone.getName());
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red>Zona memancing <yellow>" + zone.getName() + "</yellow> berhasil dihapus!</red>"));
                    render();
                    return;
                }

                if (event.isRightClick()) {
                    // Toggle AFK Allowed
                    boolean newState = !zone.isAfkAllowed();
                    zone.setAfkAllowed(newState);
                    plugin.getZoneManager().save();
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, newState ? 1.5f : 0.8f);
                    player.sendMessage(mm.deserialize("<green>Status AFK Fishing untuk zona <yellow>" + zone.getName() + "</yellow> diubah menjadi: " +
                            (newState ? "<green><bold>AKTIF</bold></green>" : "<red><bold>NON-AKTIF</bold></red>") + ".</green>"));
                    render();
                    return;
                }

                if (event.isLeftClick()) {
                    // Teleport to zone center
                    Location center = zone.getCenter(Bukkit.getServer());
                    if (center != null) {
                        player.teleport(center);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
                        player.sendMessage(mm.deserialize("<green>Teleportasi ke tengah zona memancing <yellow>" + zone.getName() + "</yellow>!</green>"));
                    } else {
                        player.sendMessage(mm.deserialize("<red>Dunia '" + zone.getWorldName() + "' tidak sedang dimuat!</red>"));
                    }
                    return;
                }
            }
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
