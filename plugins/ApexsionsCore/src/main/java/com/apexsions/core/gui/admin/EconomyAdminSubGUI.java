package com.apexsions.core.gui.admin;

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

import java.util.List;

/**
 * 54-Slot Interactive Sub-Menu for ApexsionsEconomy administration.
 */
public class EconomyAdminSubGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public EconomyAdminSubGUI(ApexsionsCorePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#2ecc71:#27ae60><bold>💰 APEXSIONS ECONOMY MANAGER 💰</bold></gradient>"));
        buildGUI();
    }

    private void buildGUI() {
        ItemStack border = createGlass(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Slot 20: Quick Give +100,000 Rupiah
        ItemStack rupiahItem = new ItemStack(Material.EMERALD);
        ItemMeta rupMeta = rupiahItem.getItemMeta();
        if (rupMeta != null) {
            rupMeta.displayName(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>💵 BONUS +Rp 100.000 RUPIAH 💵</bold></gradient>"));
            rupMeta.lore(List.of(
                    mm.deserialize("<gray>Tambahkan saldo Rupiah instan ke akunmu:</gray>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk menambah Rp 100.000!</yellow>")
            ));
            rupiahItem.setItemMeta(rupMeta);
        }
        inventory.setItem(20, rupiahItem);

        // Slot 22: Quick Give +10 Diamond Diamonds
        ItemStack diaItem = new ItemStack(Material.DIAMOND);
        ItemMeta diaMeta = diaItem.getItemMeta();
        if (diaMeta != null) {
            diaMeta.displayName(mm.deserialize("<gradient:#3498db:#2980b9><bold>💎 BONUS +10 DIAMOND SALDO 💎</bold></gradient>"));
            diaMeta.lore(List.of(
                    mm.deserialize("<gray>Tambahkan mata uang Diamond ke akunmu:</gray>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk menambah 10 Diamond!</yellow>")
            ));
            diaItem.setItemMeta(diaMeta);
        }
        inventory.setItem(22, diaItem);

        // Slot 24: Open Auction House (/ah)
        ItemStack ahItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta ahMeta = ahItem.getItemMeta();
        if (ahMeta != null) {
            ahMeta.displayName(mm.deserialize("<gradient:#f1c40f:#f39c12><bold>🏛 AUCTION HOUSE VAULT 🏛</bold></gradient>"));
            ahMeta.lore(List.of(
                    mm.deserialize("<gray>Inspeksi seluruh lelang pasar global (/ah):</gray>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk membuka pasar lelang!</yellow>")
            ));
            ahItem.setItemMeta(ahMeta);
        }
        inventory.setItem(24, ahItem);

        // Slot 31: Reload Economy
        ItemStack reloadItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta relMeta = reloadItem.getItemMeta();
        if (relMeta != null) {
            relMeta.displayName(mm.deserialize("<gradient:#e74c3c:#c0392b><bold>🔄 RELOAD APEXSIONS ECONOMY 🔄</bold></gradient>"));
            relMeta.lore(List.of(
                    mm.deserialize("<gray>Muat ulang konfigurasi mata uang & pajak.</gray>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Klik untuk reload!</yellow>")
            ));
            reloadItem.setItemMeta(relMeta);
        }
        inventory.setItem(31, reloadItem);

        // Slot 45: Back Button
        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backBtn.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(mm.deserialize("<gradient:#e74c3c:#c0392b><bold>⬅ KEMBALI KE ADMIN HUB</bold></gradient>"));
            backBtn.setItemMeta(backMeta);
        }
        inventory.setItem(45, backBtn);

        // Slot 49: Close
        ItemStack closeBtn = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeBtn.getItemMeta();
        if (closeMeta != null) {
            closeMeta.displayName(mm.deserialize("<red><bold>✖ TUTUP</bold></red>"));
            closeBtn.setItemMeta(closeMeta);
        }
        inventory.setItem(49, closeBtn);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 45) {
            player.closeInventory();
            new MasterAdminGUI(plugin, player).open();
            return;
        }

        if (slot == 49) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        if (slot == 20) { // +100,000 Rupiah
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsEconomy")) {
                player.performCommand("eco give " + player.getName() + " rupiah 100000");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f);
            } else {
                player.sendMessage(mm.deserialize("<red>Plugin ApexsionsEconomy tidak aktif.</red>"));
            }
            return;
        }

        if (slot == 22) { // +10 Diamonds
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsEconomy")) {
                player.performCommand("eco give " + player.getName() + " diamond 10");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f);
            } else {
                player.sendMessage(mm.deserialize("<red>Plugin ApexsionsEconomy tidak aktif.</red>"));
            }
            return;
        }

        if (slot == 24) { // /ah
            player.closeInventory();
            player.performCommand("ah");
            return;
        }

        if (slot == 31) { // Reload
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsEconomy")) {
                player.performCommand("ecoadmin reload");
            }
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
            player.sendMessage(mm.deserialize("<green>✓ ApexsionsEconomy berhasil dimuat ulang!</green>"));
        }
    }

    private ItemStack createGlass(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void open() {
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.2f);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
