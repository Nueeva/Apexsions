package com.apexsions.fishing.gui;

import com.apexsions.core.api.ApexsionsCoreAPI;
import com.apexsions.core.api.ApexsionsCoreProvider;
import com.apexsions.economy.api.ApexsionsEconomyAPI;
import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.model.FishingRodData;
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

import java.util.*;

/**
 * Official Fishing Rod Shop GUI.
 * Allows purchasing official Auto-Catch and Lucky rods using Rupiah or Diamond.
 * Enforces minimum level requirements.
 */
public class RodShopGUI implements InventoryHolder {

    private final ApexsionsFishing plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final Map<Integer, FishingRodData> slotToRod = new HashMap<>();

    public RodShopGUI(@NotNull ApexsionsFishing plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 36, mm.deserialize("<gradient:#00c6ff:#0072ff><bold>Toko Pancingan Resmi Apexsions</bold></gradient>"));
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
        slotToRod.clear();

        // Border
        ItemStack cyanGlass = createGuiItem(Material.CYAN_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < 36; i++) {
            if (i < 9 || i >= 27 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, cyanGlass);
            }
        }

        // Informational banner at Slot 4
        inventory.setItem(4, createGuiItem(Material.NETHER_STAR,
                "<gradient:#ff007f:#7928ca><bold>★ PANCINGAN EKSKLUSIF RESMI ★</bold></gradient>",
                List.of(
                        "<gray>Pancingan di toko ini dirancang khusus dengan teknologi</gray>",
                        "<gray>Auto-Catch otomatis yang tidak dapat ditemukan di mana pun!</gray>",
                        "",
                        "<yellow>✔ Kompatibel Anvil:</yellow> <white>Dapat digabung dengan enchant buku!</white>",
                        "<aqua>✔ Pilihan Mata Uang:</aqua> <white>Bisa dibeli via Rupiah atau Diamond.</white>"
                )));

        int[] displaySlots = {11, 15, 12, 14, 13};
        int index = 0;

        for (FishingRodData rod : plugin.getRodManager().getAllRods()) {
            if (index >= displaySlots.length) break;
            int slot = displaySlots[index++];
            slotToRod.put(slot, rod);

            ItemStack preview = plugin.getRodManager().createRod(rod);
            ItemMeta meta = preview.getItemMeta();
            if (meta != null) {
                List<Component> lore = meta.hasLore() ? new ArrayList<>(Objects.requireNonNull(meta.lore())) : new ArrayList<>();
                lore.add(Component.empty());
                lore.add(mm.deserialize("<gold><bold>━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gold>"));
                lore.add(mm.deserialize("<yellow>✦ Harga Rupiah:</yellow> <gold><bold>Rp " + String.format("%,d", (long) rod.getPriceRupiah()) + "</bold></gold>"));
                lore.add(mm.deserialize("<aqua>✦ Harga Diamond:</aqua> <blue><bold>" + String.format("%,d", (long) rod.getPriceDiamond()) + " Diamond</bold></blue>"));
                lore.add(Component.empty());
                lore.add(mm.deserialize("<yellow>[Klik Kiri]</yellow> <white>Beli menggunakan Rupiah</white>"));
                lore.add(mm.deserialize("<aqua>[Klik Kanan]</aqua> <white>Beli menggunakan Diamond</white>"));

                meta.lore(lore);
                preview.setItemMeta(meta);
            }
            inventory.setItem(slot, preview);
        }

        // Back button at Slot 31
        inventory.setItem(31, createGuiItem(Material.ARROW,
                "<yellow><bold>◀ Kembali</bold></yellow>",
                List.of("<gray>Klik untuk kembali ke menu utama.</gray>")));
    }

    public void handleClick(@NotNull InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 31) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return;
        }

        if (!slotToRod.containsKey(slot)) {
            return;
        }

        FishingRodData rod = slotToRod.get(slot);

        // Check player level requirement
        int playerLevel = 1;
        try {
            ApexsionsCoreAPI coreAPI = ApexsionsCoreProvider.get();
            if (coreAPI != null) {
                playerLevel = coreAPI.getLevel(player.getUniqueId());
            }
        } catch (Throwable ignored) {
            playerLevel = player.getLevel();
        }

        if (playerLevel < rod.getMinLevel()) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<red>⚠ Level Anda belum mencukupi! Diperlukan minimal <yellow>Level " + rod.getMinLevel() + "</yellow> untuk membeli pancingan ini. (Level Anda: " + playerLevel + ")</red>"));
            return;
        }

        if (!com.apexsions.economy.api.ApexsionsEconomyProvider.isAvailable()) {
            player.sendMessage(mm.deserialize("<red>Sistem ekonomi sedang tidak tersedia.</red>"));
            return;
        }
        ApexsionsEconomyAPI eco = com.apexsions.economy.api.ApexsionsEconomyProvider.get();

        boolean buyWithDiamond = event.getClick().isRightClick();
        double cost = buyWithDiamond ? rod.getPriceDiamond() : rod.getPriceRupiah();
        String currencyId = buyWithDiamond ? "diamond" : "rupiah";
        String currencyLabel = buyWithDiamond ? "Diamond" : "Rupiah";
        String formattedCost = buyWithDiamond ? String.format("%,d Diamond", (long) cost) : "Rp " + String.format("%,d", (long) cost);

        if (!eco.has(player.getUniqueId(), currencyId, cost)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<red>Saldo " + currencyLabel + " Anda tidak mencukupi! Diperlukan <yellow>" + formattedCost + "</yellow>.</red>"));
            return;
        }

        // Check if player inventory has space
        if (player.getInventory().firstEmpty() == -1) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<red>Inventory Anda penuh! Kosongkan setidaknya 1 slot sebelum membeli pancingan.</red>"));
            return;
        }

        eco.withdraw(player.getUniqueId(), currencyId, cost);
        ItemStack item = plugin.getRodManager().createRod(rod);
        player.getInventory().addItem(item);

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        player.sendMessage(mm.deserialize("<green><bold>SUKSES!</bold> Anda berhasil membeli </green>").append(mm.deserialize(rod.getDisplayName())).append(mm.deserialize("<green> seharga <yellow>" + formattedCost + "</yellow>!</green>")));
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
