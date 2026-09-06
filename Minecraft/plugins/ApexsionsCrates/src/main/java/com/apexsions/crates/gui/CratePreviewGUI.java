package com.apexsions.crates.gui;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.crate.Crate;
import com.apexsions.crates.milestone.Milestone;
import com.apexsions.crates.reward.Reward;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CratePreviewGUI implements InventoryHolder {

    private final ApexsionsCratesPlugin plugin;
    private final Player player;
    private final Crate crate;
    private final Inventory inventory;

    public CratePreviewGUI(ApexsionsCratesPlugin plugin, Player player, Crate crate) {
        this.plugin = plugin;
        this.player = player;
        this.crate = crate;
        this.inventory = Bukkit.createInventory(this, 54, MiniMessage.miniMessage().deserialize(
                "<dark_gray>Preview: </dark_gray>" + crate.getName()
        ));
        build();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void open() {
        player.openInventory(inventory);
    }

    private void build() {
        // Fill top/bottom border
        ItemStack pane = createItem(Material.GRAY_STAINED_GLASS_PANE, "<gray> </gray>", null);
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, pane);
        }
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, pane);
        }

        // Navigation buttons
        inventory.setItem(45, createItem(Material.ARROW, "<yellow><bold>« Kembali ke Katalog</bold></yellow>", List.of("<gray>Klik untuk kembali ke daftar peti.</gray>")));
        inventory.setItem(49, createItem(Material.BARRIER, "<red><bold>Tutup Menu</bold></red>", List.of("<gray>Klik untuk menutup preview.</gray>")));
        inventory.setItem(53, createItem(Material.CHEST, "<gradient:#2ed573:#1e90ff><bold>Buka Peti Sekarang</bold></gradient>", List.of(
                "<gray>Buka peti ini langsung menggunakan</gray>",
                "<gray>kunci fisik atau virtual Anda.</gray>",
                "",
                "<green>▶ Klik Kiri: Buka Normal</green>",
                "<gold>▶ Shift-Klik: Buka Instan</gold>"
        )));

        // Key info at slot 47
        String reqKey = crate.getRequiredKeyId();
        boolean hasPhys = plugin.getKeyManager().hasPhysicalKey(player, reqKey, 1);
        plugin.getRepository().getVirtualKeys(player.getUniqueId(), reqKey).thenAccept(virtCount -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                List<String> keyLore = new ArrayList<>();
                keyLore.add("<gray>Kunci Fisik: " + (hasPhys ? "<green>Ada di inventory</green>" : "<red>Tidak ada</red>") + "</gray>");
                keyLore.add("<gray>Kunci Virtual: <yellow>" + virtCount + " Kunci</yellow></gray>");
                inventory.setItem(47, createItem(Material.TRIPWIRE_HOOK, "<gold><bold>STATUS KUNCI</bold></gold>", keyLore));
            });
        });

        // Populate rewards in slots 9..44
        int slot = 9;
        for (Reward reward : crate.getRewards()) {
            if (slot >= 45) break;
            double chance = crate.getRewardChance(reward);
            inventory.setItem(slot++, reward.createDisplayItem(chance));
        }
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot == 49) {
            player.closeInventory();
            return;
        }

        if (slot == 45) {
            new CratesCatalogueGUI(plugin, player).open();
            return;
        }

        if (slot == 53) {
            player.closeInventory();
            if (event.isShiftClick()) {
                plugin.openCrateInstant(player, crate, true);
            } else {
                plugin.openCrate(player, crate, true);
            }
        }
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MiniMessage.miniMessage().deserialize(name));
            if (lore != null) {
                List<Component> compLore = new ArrayList<>();
                for (String l : lore) {
                    compLore.add(MiniMessage.miniMessage().deserialize(l));
                }
                meta.lore(compLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
