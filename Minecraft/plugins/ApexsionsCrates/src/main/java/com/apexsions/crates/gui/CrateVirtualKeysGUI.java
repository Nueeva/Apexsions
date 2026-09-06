package com.apexsions.crates.gui;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.crate.Crate;
import com.apexsions.crates.key.CrateKey;
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

public class CrateVirtualKeysGUI implements InventoryHolder {

    private final ApexsionsCratesPlugin plugin;
    private final Player player;
    private final Inventory inventory;

    public CrateVirtualKeysGUI(ApexsionsCratesPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 36, MiniMessage.miniMessage().deserialize(
                "<gradient:#f1c40f:#e67e22><bold>DOMPET KUNCI PETI</bold></gradient>"
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
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta paneMeta = pane.getItemMeta();
        paneMeta.displayName(Component.empty());
        pane.setItemMeta(paneMeta);

        for (int i = 0; i < 9; i++) inventory.setItem(i, pane);
        for (int i = 27; i < 36; i++) inventory.setItem(i, pane);

        inventory.setItem(31, createItem(Material.BARRIER, "<red>Tutup</red>", null));

        plugin.getRepository().getAllVirtualKeys(player.getUniqueId()).thenAccept(keyMap -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                int slot = 9;
                for (CrateKey key : plugin.getKeyManager().getKeys()) {
                    if (slot >= 27) break;
                    int amount = keyMap.getOrDefault(key.getId(), 0);

                    List<String> lore = new ArrayList<>();
                    lore.add("<gray>Saldo Virtual: <yellow>" + amount + " Kunci</yellow></gray>");
                    lore.add("");
                    if (amount > 0) {
                        lore.add("<green>▶ Klik untuk buka menu Crate!</green>");
                    } else {
                        lore.add("<red>Saldo kunci habis.</red>");
                    }

                    ItemStack item = createItem(key.getMaterial(), key.getName(), lore);
                    item.setAmount(Math.max(1, Math.min(amount, 64)));
                    inventory.setItem(slot++, item);
                }
            });
        });
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getRawSlot() == 31) {
            player.closeInventory();
        }
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MiniMessage.miniMessage().deserialize(name));
            if (lore != null) {
                List<Component> compLore = new ArrayList<>();
                for (String l : lore) compLore.add(MiniMessage.miniMessage().deserialize(l));
                meta.lore(compLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
