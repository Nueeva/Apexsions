package com.apexsions.core.caravan;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Inventory GUI for the Black Market Caravan. Inventory GUIs render natively on
 * Bedrock through Geyser, so no platform-specific form is required.
 */
public class CaravanGUI implements InventoryHolder {

    private static final int SIZE = 54;
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final CaravanManager manager;
    private final Inventory inventory;
    private final Map<Integer, CaravanOffer> slotOffers = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    public CaravanGUI(@NotNull CaravanManager manager, @NotNull Player viewer) {
        this.manager = manager;
        this.inventory = Bukkit.createInventory(this, SIZE,
                mm.deserialize("<dark_gray><bold>Kafilah Pasar Gelap</bold></dark_gray>"));
        populate(viewer);
    }

    private void populate(Player viewer) {
        ItemStack filler = new ItemStack(org.bukkit.Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.displayName(Component.empty());
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, filler);
        }

        List<CaravanOffer> offers = manager.getOffers();
        for (int i = 0; i < offers.size() && i < SLOTS.length; i++) {
            CaravanOffer offer = offers.get(i);
            int slot = SLOTS[i];
            ItemStack item = new ItemStack(offer.icon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(mm.deserialize("<yellow><bold>" + manager.displayName(offer) + "</bold></yellow>"));

                List<Component> lore = new ArrayList<>();
                if (offer.isCommandOffer()) {
                    lore.add(mm.deserialize("<gray>Hadiah eksklusif untuk peradaban Anda.</gray>"));
                } else {
                    lore.add(mm.deserialize("<gray>Jumlah: <white>" + offer.amount() + "x</white></gray>"));
                }
                lore.add(mm.deserialize("<gray>Harga: <gold>" + managerFormat(viewer, offer) + "</gold></gray>"));
                lore.add(Component.empty());
                lore.add(mm.deserialize("<green><bold>[KLIK UNTUK BELI]</bold></green>"));
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(slot, item);
            slotOffers.put(slot, offer);
        }

        ItemStack info = new ItemStack(org.bukkit.Material.NETHER_STAR);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(mm.deserialize("<gradient:#9b59b6:#e74c3c><bold>PASAR GELAP</bold></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Saldo Anda: <yellow>" + managerFormat(viewer, null) + "</yellow></gray>"));
            lore.add(mm.deserialize("<gray>Durasi kafilah: <white>" + manager.getDurationMinutes() + " menit</white></gray>"));
            infoMeta.lore(lore);
            info.setItemMeta(infoMeta);
        }
        inventory.setItem(49, info);
    }

    private String managerFormat(Player viewer, CaravanOffer offer) {
        double balance = manager.getBalance(viewer);
        if (offer == null) {
            return manager.format(balance, com.apexsions.core.integration.EconomyBridge.DEFAULT_CURRENCY);
        }
        return manager.format(offer.price(), offer.priceCurrency());
    }

    public CaravanOffer getOfferAt(int slot) {
        return slotOffers.get(slot);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
