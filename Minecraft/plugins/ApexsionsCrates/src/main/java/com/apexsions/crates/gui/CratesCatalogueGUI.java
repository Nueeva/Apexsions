package com.apexsions.crates.gui;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.crate.Crate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
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

public class CratesCatalogueGUI implements InventoryHolder {

    private final ApexsionsCratesPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final Map<Integer, Crate> slotCrates = new HashMap<>();

    public CratesCatalogueGUI(ApexsionsCratesPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, MiniMessage.miniMessage().deserialize(
                "<gradient:#f1c40f:#e67e22><bold>PETI KERAJAAN APEXSIONS</bold></gradient>"
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
        slotCrates.clear();
        ItemStack pane = createItem(Material.GRAY_STAINED_GLASS_PANE, "<gray> </gray>", null);
        for (int i = 0; i < 9; i++) inventory.setItem(i, pane);
        for (int i = 45; i < 54; i++) inventory.setItem(i, pane);

        // Header info
        inventory.setItem(4, createItem(Material.NETHER_STAR,
                "<gradient:#f1c40f:#e67e22><bold>APEXSIONS CRATES SUITE</bold></gradient>",
                List.of(
                        "<gray>Selamat datang di sistem Crate & Key resmi kerajaan.</gray>",
                        "<gray>Pilih peti untuk melihat peluang atau membuka langsung!</gray>",
                        "",
                        "<yellow>Total Peti Tersedia: <white>" + plugin.getCrateManager().getCrates().size() + "</white></yellow>"
                )
        ));

        // Bottom Navigation Buttons
        inventory.setItem(48, createItem(Material.TRIPWIRE_HOOK,
                "<gold><bold>Dompet Kunci Virtual</bold></gold>",
                List.of(
                        "<gray>Klik untuk melihat seluruh saldo kunci virtual Anda.</gray>",
                        "",
                        "<yellow>▶ Klik untuk buka dompet kunci</yellow>"
                )
        ));

        inventory.setItem(49, createItem(Material.BARRIER,
                "<red><bold>Tutup Menu</bold></red>",
                List.of("<gray>Tutup menu peti.</gray>")
        ));

        if (player.hasPermission("apexsions.crates.admin")) {
            inventory.setItem(50, createItem(Material.COMMAND_BLOCK,
                    "<gradient:#2ed573:#1e90ff><bold>Admin Control Hub</bold></gradient>",
                    List.of(
                            "<gray>Akses khusus administrator untuk mengelola</gray>",
                            "<gray>seluruh crate, lokasi, dan kunci.</gray>",
                            "",
                            "<green>▶ Klik untuk buka /acrates editor</green>"
                    )
            ));
        }

        // Fetch player virtual keys and crate counts asynchronously
        plugin.getRepository().getAllVirtualKeys(player.getUniqueId()).thenAccept(virtKeys -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                int slot = 9;
                for (Crate crate : plugin.getCrateManager().getCrates()) {
                    if (slot >= 45) break;

                    String reqKey = crate.getRequiredKeyId();
                    int virtCount = virtKeys.getOrDefault(reqKey, 0);
                    boolean hasPhys = plugin.getKeyManager().hasPhysicalKey(player, reqKey, 1);

                    List<String> lore = new ArrayList<>();
                    lore.add("<gray>Kunci Dibutuhkan: <gold>" + reqKey + "</gold></gray>");
                    lore.add("<gray>Animasi: <yellow>" + crate.getAnimationType().name() + "</yellow></gray>");
                    lore.add("<gray>Total Hadiah: <aqua>" + crate.getRewards().size() + " Item Hadiah</aqua></gray>");
                    lore.add("");
                    lore.add("<gray>Saldo Kunci Anda:</gray>");
                    lore.add("<dark_gray>•</dark_gray> <gray>Kunci Virtual: <yellow>" + virtCount + " Kunci</yellow></gray>");
                    lore.add("<dark_gray>•</dark_gray> <gray>Kunci Fisik: " + (hasPhys ? "<green>Ada di tas</green>" : "<red>Tidak ada</red>") + "</gray>");
                    lore.add("");
                    lore.add("<yellow>▶ Klik Kiri: Preview Hadiah & Peluang</yellow>");
                    if (virtCount > 0 || hasPhys) {
                        lore.add("<green>▶ Klik Kanan: Buka Peti Ini!</green>");
                        lore.add("<gold>▶ Shift-Klik: Buka Instan (Skip Animasi)</gold>");
                    } else {
                        lore.add("<red>✘ Butuh kunci untuk membuka peti ini</red>");
                    }

                    ItemStack item = createItem(crate.getBlockMaterial(), crate.getName(), lore);
                    inventory.setItem(slot, item);
                    slotCrates.put(slot, crate);
                    slot++;
                }
            });
        });
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) {
            player.closeInventory();
            return;
        }

        if (slot == 48) {
            new CrateVirtualKeysGUI(plugin, player).open();
            return;
        }

        if (slot == 50 && player.hasPermission("apexsions.crates.admin")) {
            new CrateAdminHubGUI(plugin, player).open();
            return;
        }

        if (slotCrates.containsKey(slot)) {
            Crate crate = slotCrates.get(slot);
            ClickType click = event.getClick();

            // Left Click -> Preview GUI
            if (click.isLeftClick()) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                new CratePreviewGUI(plugin, player, crate).open();
                return;
            }

            // Shift Click -> Instant Open
            if (click.isShiftClick()) {
                player.closeInventory();
                plugin.openCrateInstant(player, crate, true);
                return;
            }

            // Right Click -> Normal Open
            if (click.isRightClick()) {
                player.closeInventory();
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
                for (String l : lore) compLore.add(MiniMessage.miniMessage().deserialize(l));
                meta.lore(compLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
