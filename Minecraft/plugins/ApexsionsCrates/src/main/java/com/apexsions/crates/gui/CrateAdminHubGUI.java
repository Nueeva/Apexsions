package com.apexsions.crates.gui;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.crate.Crate;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrateAdminHubGUI implements InventoryHolder {

    private final ApexsionsCratesPlugin plugin;
    private final Player admin;
    private final Inventory inventory;
    private final Map<Integer, Crate> slotCrates = new HashMap<>();

    public CrateAdminHubGUI(ApexsionsCratesPlugin plugin, Player admin) {
        this.plugin = plugin;
        this.admin = admin;
        this.inventory = Bukkit.createInventory(this, 54, MiniMessage.miniMessage().deserialize(
                "<gradient:#f39c12:#e74c3c><bold>ADMIN HUB — CRATES</bold></gradient>"
        ));
        build();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void open() {
        admin.openInventory(inventory);
    }

    private void build() {
        slotCrates.clear();
        ItemStack pane = createItem(Material.GRAY_STAINED_GLASS_PANE, "<gray> </gray>", null);
        for (int i = 0; i < 9; i++) inventory.setItem(i, pane);
        for (int i = 45; i < 54; i++) inventory.setItem(i, pane);

        // Header info
        inventory.setItem(4, createItem(Material.NETHER_STAR, "<gradient:#f1c40f:#e67e22><bold>APEXSIONS CRATES CONTROL PANEL</bold></gradient>", List.of(
                "<gray>Kelola seluruh Peti, Kunci, dan Hologram</gray>",
                "<gray>Total Peti Terdaftar: <yellow>" + plugin.getCrateManager().getCrates().size() + "</yellow></gray>",
                "<gray>Total Lokasi Fisik: <yellow>" + plugin.getCrateManager().getLocations().size() + "</yellow></gray>"
        )));

        // Buttons
        inventory.setItem(47, createItem(Material.EMERALD, "<green><bold>Reload Sistem</bold></green>", List.of("<gray>Muat ulang konfigurasi, crate, dan hologram.</gray>")));
        inventory.setItem(49, createItem(Material.BARRIER, "<red><bold>Tutup</bold></red>", null));

        // Crates slots 9..44
        int slot = 9;
        for (Crate crate : plugin.getCrateManager().getCrates()) {
            if (slot >= 45) break;
            List<String> lore = new ArrayList<>();
            lore.add("<gray>ID: <yellow>" + crate.getId() + "</yellow></gray>");
            lore.add("<gray>Animasi: <yellow>" + crate.getAnimationType().name() + "</yellow></gray>");
            lore.add("<gray>Kunci Dibutuhkan: <gold>" + crate.getRequiredKeyId() + "</gold></gray>");
            lore.add("<gray>Total Hadiah: <aqua>" + crate.getRewards().size() + " Hadiah</aqua></gray>");
            lore.add("");
            lore.add("<yellow>▶ Klik Kiri: Preview Hadiah Peti</yellow>");
            lore.add("<gold>▶ Klik Kanan: Ambil 1x Kunci Fisik</gold>");

            ItemStack crateItem = createItem(crate.getBlockMaterial(), crate.getName(), lore);
            inventory.setItem(slot, crateItem);
            slotCrates.put(slot, crate);
            slot++;
        }
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) {
            admin.closeInventory();
            return;
        }

        if (slot == 47) {
            plugin.reloadPlugin();
            admin.sendMessage(MiniMessage.miniMessage().deserialize("<green>ApexsionsCrates berhasil dimuat ulang!</green>"));
            build();
            return;
        }

        if (slotCrates.containsKey(slot)) {
            Crate crate = slotCrates.get(slot);
            if (event.isLeftClick()) {
                new CratePreviewGUI(plugin, admin, crate).open();
            } else if (event.isRightClick()) {
                plugin.getKeyManager().givePhysicalKey(admin, crate.getRequiredKeyId(), 1);
                admin.sendMessage(MiniMessage.miniMessage().deserialize("<green>Diberikan 1x Kunci <yellow>" + crate.getRequiredKeyId() + "</yellow> ke inventory Anda.</green>"));
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
                for (String l : lore) compLore.add(MiniMessage.miniMessage().deserialize(l));
                meta.lore(compLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
