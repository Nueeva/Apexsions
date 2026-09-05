package com.apexsions.chat.nick.gui;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.chat.nick.NickColorStyle;
import com.apexsions.chat.nick.NicknameData;
import com.apexsions.chat.nick.NicknameService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

import java.util.*;

/**
 * Modern 36-slot GUI allowing players to select solid colors and premium gradients for their nickname.
 */
public class NickColorGUI implements Listener {

    private static final int[] SOLID_SLOTS = { 10, 11, 12, 13, 14, 15, 16 };
    private static final int[] GRADIENT_SLOTS = { 19, 20, 21, 22, 23, 24, 25 };

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<Integer, NickColorStyle> slotStyleMap = new HashMap<>();

    public NickColorGUI(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        NicknameData data = plugin.getNicknameService().getNicknameData(player.getUniqueId());
        String currentName = data.hasNickname() ? data.getNicknameRaw() : player.getName();
        String currentStyleId = data.getColorStyleId();
        NickColorStyle currentStyle = NickColorStyle.fromId(currentStyleId);

        NickColorHolder holder = new NickColorHolder();
        String titleStr = "<dark_gray><bold>🎨 KUSTOMISASI WARNA NICKNAME</bold></dark_gray>";
        Inventory inv = Bukkit.createInventory(holder, 36, miniMessage.deserialize(titleStr));
        holder.setInventory(inv);
        slotStyleMap.clear();

        // 1. Fill borders & backgrounds
        ItemStack darkGlass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        ItemStack blackGlass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        ItemStack accentGlass = createItem(Material.CYAN_STAINED_GLASS_PANE, " ", null);

        for (int i = 0; i < 36; i++) {
            inv.setItem(i, darkGlass);
        }
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, blackGlass);
            inv.setItem(27 + i, blackGlass);
        }
        inv.setItem(0, accentGlass);
        inv.setItem(8, accentGlass);
        inv.setItem(27, accentGlass);
        inv.setItem(35, accentGlass);

        // 2. Slot 4: Dynamic Preview Card
        List<Component> previewLore = new ArrayList<>();
        previewLore.add(miniMessage.deserialize("<gray>Pratinjau Nama:</gray> "));
        previewLore.add(currentStyle.apply(currentName));
        previewLore.add(miniMessage.deserialize(""));
        previewLore.add(miniMessage.deserialize("<gray>Gaya Terpilih: <yellow>" + currentStyle.getDisplayName() + "</yellow></gray>"));
        previewLore.add(miniMessage.deserialize("<gray>Sisa Token Ganti Nama: <gold><bold>" + data.getTokens() + " Token</bold></gold></gray>"));
        previewLore.add(miniMessage.deserialize(""));
        if (data.hasNickname()) {
            previewLore.add(miniMessage.deserialize("<green>Pilih palet warna/gradien di bawah untuk berganti gaya!</green>"));
        } else {
            previewLore.add(miniMessage.deserialize("<yellow>⚠️ Kamu belum mengubah nama teks. Gunakan <aqua>/nick <nama></aqua> dahulu.</yellow>"));
        }
        inv.setItem(4, createItem(Material.NAME_TAG, "<gradient:#f1c40f:#e67e22><bold>👑 PRATINJAU NICKNAME KAMU</bold></gradient>", previewLore));

        // 3. Row 1: Solid Colors (Slots 10-16)
        List<NickColorStyle> solids = NickColorStyle.getSolidStyles();
        for (int i = 0; i < SOLID_SLOTS.length && i < solids.size(); i++) {
            int slot = SOLID_SLOTS[i];
            NickColorStyle style = solids.get(i);
            slotStyleMap.put(slot, style);

            boolean hasPerm = style.hasPermission(player);
            boolean isSelected = style.getId().equalsIgnoreCase(currentStyleId);

            ItemStack item = buildStyleIcon(style, currentName, hasPerm, isSelected);
            inv.setItem(slot, item);
        }

        // 4. Row 2: Premium Gradients (Slots 19-25)
        List<NickColorStyle> gradients = NickColorStyle.getGradientStyles();
        for (int i = 0; i < GRADIENT_SLOTS.length && i < gradients.size(); i++) {
            int slot = GRADIENT_SLOTS[i];
            NickColorStyle style = gradients.get(i);
            slotStyleMap.put(slot, style);

            boolean hasPerm = style.hasPermission(player);
            boolean isSelected = style.getId().equalsIgnoreCase(currentStyleId);

            ItemStack item = buildStyleIcon(style, currentName, hasPerm, isSelected);
            inv.setItem(slot, item);
        }

        // 5. Slot 30: Reset Color to Default (White)
        List<Component> resetLore = new ArrayList<>();
        resetLore.add(miniMessage.deserialize("<gray>Kembalikan warna nickname ke putih default.</gray>"));
        resetLore.add(miniMessage.deserialize(""));
        resetLore.add(miniMessage.deserialize("<green>» Klik untuk mereset warna</green>"));
        inv.setItem(30, createItem(Material.WHITE_DYE, "<white><bold>Warna Putih Default</bold></white>", resetLore));

        // 6. Slot 32: Close Button
        inv.setItem(32, createItem(Material.BARRIER, "<red><bold>✖ Tutup</bold></red>", Collections.singletonList(miniMessage.deserialize("<gray>Tutup menu warna</gray>"))));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
    }

    private ItemStack buildStyleIcon(NickColorStyle style, String sampleName, boolean hasPerm, boolean isSelected) {
        Material mat = hasPerm ? style.getGuiMaterial() : Material.GRAY_DYE;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String titlePrefix = hasPerm ? "✦ " : "🔒 ";
            meta.displayName(miniMessage.deserialize(titlePrefix + style.getDisplayName()));

            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Pratinjau: </gray>"));
            lore.add(style.apply(sampleName));
            lore.add(miniMessage.deserialize(""));

            if (isSelected) {
                lore.add(miniMessage.deserialize("<green><bold>✔ SEDANG DIGUNAKAN</bold></green>"));
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } else if (hasPerm) {
                lore.add(miniMessage.deserialize("<yellow>» Klik untuk gunakan warna ini</yellow>"));
            } else {
                lore.add(miniMessage.deserialize("<red><bold>🔒 TERKUNCI</bold></red>"));
                lore.add(miniMessage.deserialize("<gray>Khusus donator: <gold>" + style.getRequiredRank() + "</gold></gray>"));
            }

            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof NickColorHolder)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 36) return;

        // Reset to default white (Slot 30)
        if (slot == 30) {
            plugin.getNicknameService().setColorStyle(player, NickColorStyle.DEFAULT);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
            open(player);
            return;
        }

        // Close (Slot 32)
        if (slot == 32) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        // Check if clicked a color/gradient slot
        NickColorStyle style = slotStyleMap.get(slot);
        if (style != null) {
            if (!style.hasPermission(player)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(miniMessage.deserialize(
                        "<red>🔒 Warna <yellow>" + style.getDisplayName() + "</yellow> terkunci!</red>\n" +
                        "<gray>Gaya ini hanya dapat digunakan oleh rank <gold><bold>" + style.getRequiredRank() + "</bold></gold>.</gray>"
                ));
                return;
            }

            NicknameData data = plugin.getNicknameService().getNicknameData(player.getUniqueId());
            if (!data.hasNickname()) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(miniMessage.deserialize(
                        "<red>Kamu belum mengatur nama panggilan! Ketik <yellow>/nick <nama_baru></yellow> terlebih dahulu.</red>"
                ));
                return;
            }

            NicknameService.NicknameResult res = plugin.getNicknameService().setColorStyle(player, style);
            if (res.success()) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.6f);
                player.sendMessage(miniMessage.deserialize(res.message()));
                open(player); // refresh preview card and current selection
            } else {
                player.sendMessage(miniMessage.deserialize(res.message()));
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof NickColorHolder) {
            event.setCancelled(true);
        }
    }

    private ItemStack createItem(Material mat, String name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) meta.displayName(miniMessage.deserialize(name));
            if (lore != null) meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }
}
