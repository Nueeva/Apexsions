package com.apexsions.core.region.gui;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.event.KingdomRegionChooseEvent;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import com.apexsions.core.region.gui.holder.KingdomConfirmHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import java.time.Duration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Optional;

/**
 * 27-Slot Confirmation Dialog for pledging allegiance to a Kingdom.
 */
public class KingdomConfirmGUI implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public KingdomConfirmGUI(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, String kingdomKey) {
        Optional<Region> regionOpt = plugin.getRegionManager().getRegion(kingdomKey);
        if (regionOpt.isEmpty()) return;

        Region region = regionOpt.get();
        KingdomConfirmHolder holder = new KingdomConfirmHolder(kingdomKey);
        Component title = mm.deserialize("<dark_red><bold>⚔ KONFIRMASI SUMPAH SETIA ⚔</bold></dark_red>");

        Inventory inv = Bukkit.createInventory(holder, 27, title);
        holder.setInventory(inv);

        // Fill borders
        ItemStack darkGlass = createGlass(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack redGlass = createGlass(Material.RED_STAINED_GLASS_PANE);

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, darkGlass);
        }
        inv.setItem(0, redGlass);
        inv.setItem(8, redGlass);
        inv.setItem(18, redGlass);
        inv.setItem(26, redGlass);

        // 1. Slot 4: Warning Header Card
        ItemStack header = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta hm = header.getItemMeta();
        if (hm != null) {
            hm.displayName(mm.deserialize("<gold><bold>PERINGATAN SUMPAH SETIA KERAJAAN</bold></gold>"));
            hm.lore(List.of(
                    Component.empty(),
                    mm.deserialize("<gray>Anda akan bersumpah setia kepada </gray>" + region.getDisplayName()),
                    Component.empty(),
                    mm.deserialize("<red><bold>PERINGATAN SANGAT PENTING:</bold></red>"),
                    mm.deserialize("<yellow>Pilihan ini bersifat <white><bold>SEKALI SEUMUR HIDUP</bold></white> dan</yellow>"),
                    mm.deserialize("<red><bold>TIDAK DAPAT DIUBAH LAGI DI KEMUDIAN HARI!</bold></red>"),
                    Component.empty(),
                    mm.deserialize("<dark_gray>Pastikan Anda telah membaca seluruh info kerajaan.</dark_gray>")
            ));
            header.setItemMeta(hm);
        }
        inv.setItem(4, header);

        // 2. Slot 11: Confirm Button (YA, SAYA YAKIN)
        ItemStack confirmBtn = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta cm = confirmBtn.getItemMeta();
        if (cm != null) {
            cm.displayName(mm.deserialize("<green><bold>✔ YA, SAYA YAKIN & BERSUMPAH SETIA</bold></green>"));
            cm.lore(List.of(
                    Component.empty(),
                    mm.deserialize("<gray>Saya menerima seluruh hak, kewajiban,</gray>"),
                    mm.deserialize("<gray>buff, nerf, dan aturan wilayah kerajaan ini.</gray>"),
                    Component.empty(),
                    mm.deserialize("<yellow>Klik untuk meresmikan status warganegara! ▶</yellow>")
            ));
            cm.addEnchant(Enchantment.UNBREAKING, 1, true);
            cm.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            confirmBtn.setItemMeta(cm);
        }
        inv.setItem(11, confirmBtn);

        // 3. Slot 15: Cancel Button (BATAL)
        ItemStack cancelBtn = new ItemStack(Material.RED_CONCRETE);
        ItemMeta canm = cancelBtn.getItemMeta();
        if (canm != null) {
            canm.displayName(mm.deserialize("<red><bold>✖ BATAL / KEMBALI KE DETAIL</bold></red>"));
            canm.lore(List.of(
                    Component.empty(),
                    mm.deserialize("<gray>Kembali ke halaman informasi untuk</gray>"),
                    mm.deserialize("<gray>mempertimbangkan atau melihat kerajaan lain.</gray>")
            ));
            cancelBtn.setItemMeta(canm);
        }
        inv.setItem(15, cancelBtn);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
    }

    private ItemStack createGlass(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof KingdomConfirmHolder holder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();
        String kingdomKey = holder.getKingdomKey();

        // Cancel / Back (Slot 15)
        if (slot == 15) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            new KingdomInfoGUI(plugin).open(player, kingdomKey);
            return;
        }

        // Confirm (Slot 11)
        if (slot == 11) {
            Optional<Region> regionOpt = plugin.getRegionManager().getRegion(kingdomKey);
            if (regionOpt.isEmpty()) {
                player.sendMessage(mm.deserialize("<red>Kerajaan tidak ditemukan!</red>"));
                player.closeInventory();
                return;
            }

            Region region = regionOpt.get();
            Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
            if (dataOpt.isEmpty()) {
                player.sendMessage(mm.deserialize("<red>Data profilmu sedang dimuat. Silakan coba lagi.</red>"));
                return;
            }

            PlayerData data = dataOpt.get();
            if (data.hasRegion()) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<red>Kamu sudah bersumpah setia pada suatu kerajaan!</red>"));
                player.closeInventory();
                return;
            }

            KingdomRegionChooseEvent chooseEvent = new KingdomRegionChooseEvent(player, region);
            Bukkit.getPluginManager().callEvent(chooseEvent);
            if (chooseEvent.isCancelled()) {
                return;
            }

            // Save selected region
            data.setRegionId(region.getId());
            plugin.getPlayerDataService().save(data);

            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

            // 1. Show spectacular on-screen Title & Subtitle
            Title.Times times = Title.Times.times(Duration.ofMillis(400), Duration.ofMillis(3500), Duration.ofMillis(1000));
            Title title = Title.title(
                    mm.deserialize("<gradient:#f1c40f:#e67e22><bold>⚔ " + region.getKey().toUpperCase() + " ⚔</bold></gradient>"),
                    mm.deserialize("<yellow>Selamat Datang di Kerajaan <white>" + region.getDisplayName() + "</white>!</yellow>"),
                    times
            );
            player.showTitle(title);

            // 2. Clean, modern chat confirmation
            player.sendMessage(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>KINGDOM</bold></gradient> <dark_gray>➔</dark_gray> <green>Sumpah setia resmi diterima! Selamat datang di </green>" + region.getDisplayName() + "<green>!</green>"));
            player.sendMessage(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>KINGDOM</bold></gradient> <dark_gray>➔</dark_gray> <gray>Petualanganmu dimulai. Teleportasi ke ibukota...</gray>"));

            // 3. Broadcast to server
            Bukkit.broadcast(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>KINGDOM</bold></gradient> <dark_gray>➔</dark_gray> <white>" + player.getName() + "</white> <gray>resmi bersumpah setia kepada </gray>" + region.getDisplayName() + "<gray>!</gray>"));

            // Teleport player to kingdom spawn
            plugin.getRegionTeleportService().teleport(player, region);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof KingdomConfirmHolder) {
            event.setCancelled(true);
        }
    }
}
