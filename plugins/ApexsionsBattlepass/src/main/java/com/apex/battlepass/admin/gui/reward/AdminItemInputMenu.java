package com.apex.battlepass.admin.gui.reward;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class AdminItemInputMenu extends Gui {

    private final Consumer<ItemStack> onComplete;

    public AdminItemInputMenu(ApexsionsBattlepass plugin, Player player, Gui parent, Consumer<ItemStack> onComplete) {
        super(plugin, player, "&8[ &4&lINPUT ITEM DARI INVENTORY &8]", 27, parent);
        this.onComplete = onComplete;
    }

    @Override
    public void initialize() {
        fillBackground();

        // 1. Instructions Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.HOPPER)
                .name("&e&lPETUNJUK INPUT ITEM")
                .lore(List.of(
                        "&7Ambil item dari inventory Anda lalu",
                        "&7letakkan ke dalam slot kotak di bawah (Slot 13).",
                        " ",
                        "&7Mendukung item Vanilla, Custom item,",
                        "&7Enchanted, NBT, Lore, & Model Data.",
                        "&7Seluruh metadata akan disimpan 100% identik."
                ))
                .build()));

        // Slot 13 is the item holder slot (leave empty for placement)
        inventory.setItem(13, null);

        // 2. Save Item Button (Slot 22)
        setButton(22, new GuiButton(new ItemBuilder(Material.LIME_CONCRETE)
                .name("&a&l[✔] SIMPAN ITEM INI")
                .lore(List.of(
                        "&7Klik untuk mengonfirmasi item yang",
                        "&7telah Anda letakkan di Slot 13.",
                        " ",
                        "&aKlik untuk simpan >"
                ))
                .build(), event -> {
            ItemStack placed = inventory.getItem(13);
            if (placed == null || placed.getType() == Material.AIR) {
                player.sendMessage("§c[!] Harap letakkan item ke dalam slot 13 terlebih dahulu!");
                return;
            }

            ItemStack finalItem = placed.clone();
            boolean isStackable = finalItem.getMaxStackSize() > 1;

            if (isStackable) {
                // Prompt for amount if stackable
                plugin.getChatInputManager().startNumericInput(player, "Masukkan jumlah reward (1 - 64) [Tekan Enter / ketik jumlah]:", amount -> {
                    finalItem.setAmount(amount);
                    if (onComplete != null) onComplete.accept(finalItem);
                    if (parent != null) parent.open();
                }, () -> {
                    if (parent != null) parent.open();
                }, 1, 64);
            } else {
                finalItem.setAmount(1);
                if (onComplete != null) onComplete.accept(finalItem);
                if (parent != null) parent.open();
            }
        }));

        // Back Button
        setButton(18, new BackButton(this, parent));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        // Allow free item placement/removal in slot 13
        if (rawSlot == 13 || rawSlot >= size) {
            return;
        }
        super.handleClick(event);
    }
}
