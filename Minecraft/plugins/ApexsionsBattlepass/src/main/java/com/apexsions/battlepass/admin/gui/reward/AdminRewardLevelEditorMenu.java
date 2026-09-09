package com.apexsions.battlepass.admin.gui.reward;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.navigation.CloseButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.reward.RewardItem;
import com.apexsions.battlepass.reward.RewardType;
import com.apexsions.battlepass.reward.gui.RewardPreviewMenu;
import com.apexsions.battlepass.reward.gui.SpecialRewardPreviewMenu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AdminRewardLevelEditorMenu extends Gui {

    private final String passId;
    private final int level;

    private static final int[] CENTER_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public AdminRewardLevelEditorMenu(ApexsionsBattlepass plugin, Player player, String passId, int level, Gui parent) {
        super(plugin, player, "&8[ &4&lKELOLA HADIAH: &e" + passId.toUpperCase() + " LVL " + level + " &8]", 54, parent);
        this.passId = passId.toLowerCase();
        this.level = level;
    }

    @Override
    public void initialize() {
        fillBorder();

        List<RewardItem> rewards = plugin.getRewardManager().getRewards(level, passId);
        int reqXp = plugin.getRewardManager().getRequiredXp(level);

        // 1. Top Header (Row 0)
        setButton(0, new GuiButton(new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name("&e&l[📜] LEVEL " + level + " &8(&a" + reqXp + " XP&8)")
                .lore(List.of(
                        "&7Tier Pass: &f" + passId.toUpperCase(),
                        "&7Required XP: &e" + reqXp + " XP"
                ))
                .build()));

        setButton(4, new GuiButton(new ItemBuilder(Material.HOPPER)
                .name("&a&l[💡] KELOLA HADIAH LEVEL INI")
                .lore(List.of(
                        "&7Area tengah untuk mengatur item hadiah level:",
                        "&7● &fDrag & Drop / Shift-Klik &7item untuk menambahkan.",
                        "&7● &eKlik Kiri &7pada item untuk ubah jumlah / detail.",
                        "&7● &cKlik Kanan &7pada item untuk langsung menghapusnya."
                ))
                .build()));

        // Slot 6: Preview Mode Selector & Direct Test Button
        boolean isSpecial = plugin.getRewardManager().isSpecialPreview(level, passId);
        Material prevMat = isSpecial ? Material.NETHER_STAR : Material.CHEST;
        String prevTitle = isSpecial ? "&6&l[👑] TIPE PREVIEW: ISTIMEWA" : "&b&l[📦] TIPE PREVIEW: BIASA";
        List<String> prevLore = new ArrayList<>();
        prevLore.add("&7Status tampilan preview untuk Level " + level + " (" + passId.toUpperCase() + "):");
        prevLore.add(isSpecial ? "&6● ISTIMEWA &7(Showcase mewah, pedestal & visual 3D)" : "&8○ ISTIMEWA &7(Showcase mewah, pedestal & visual 3D)");
        prevLore.add(!isSpecial ? "&b● BIASA &7(Tampilan preview standar minimalis)" : "&8○ BIASA &7(Tampilan preview standar minimalis)");
        if (level % 50 == 0) {
            prevLore.add("&8(Catatan: Level kelipatan 50 otomatis istimewa)");
        }
        prevLore.add(" ");
        prevLore.add("&e▶ [Klik Kiri] Ganti ke Mode " + (isSpecial ? "BIASA" : "ISTIMEWA"));
        prevLore.add("&a▶ [Klik Kanan] Coba / Buka Tampilan Preview");

        ItemBuilder prevBuilder = new ItemBuilder(prevMat)
                .name(prevTitle)
                .lore(prevLore);
        if (isSpecial) prevBuilder.glow();

        setButton(6, new GuiButton(prevBuilder.build(), event -> {
            if (event.isRightClick()) {
                if (isSpecial) {
                    new SpecialRewardPreviewMenu(plugin, player, level, passId, rewards, this).open();
                } else {
                    new RewardPreviewMenu(plugin, player, level, passId, rewards, this).open();
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            } else {
                boolean nextState = !isSpecial;
                plugin.getRewardManager().setSpecialPreview(level, passId, nextState);
                player.sendMessage(nextState
                        ? "§aTipe preview level berhasil diubah menjadi §6§lISTIMEWA§a!"
                        : "§aTipe preview level berhasil diubah menjadi §b§lBIASA§a!");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                open();
            }
        }));

        setButton(8, new GuiButton(new ItemBuilder(Material.BEACON)
                .name("&6&lTOTAL HADIAH: &e" + rewards.size() + " Hadiah")
                .lore(List.of(
                        "&7Seluruh hadiah pada level ini akan",
                        "&7diberikan saat pemain mengklaim level ini."
                ))
                .build()));

        // 2. Render Existing Rewards in Center Slots
        int idx = 0;
        for (int i = 0; i < rewards.size() && idx < CENTER_SLOTS.length; i++) {
            final int rewardIndex = i;
            RewardItem ri = rewards.get(i);
            ItemStack is = ri.toItemStack();
            boolean isCurrency = ri.isCurrency();
            boolean isItem = ri.getType() == RewardType.ITEM;

            // Footer info appended after original item lore
            List<String> footer = new ArrayList<>();
            footer.add(" ");
            footer.add("§7Tipe: §e" + ri.getType());
            if (isCurrency) {
                String cId = ri.getCurrencyId();
                if ("rupiah".equalsIgnoreCase(cId) || ri.getType() == RewardType.MONEY) {
                    footer.add("§7Jumlah: §aRp. " + String.format("%,d", (long) ri.getAmount()).replace(',', '.'));
                    footer.add("§7Mata Uang: §eRUPIAH (Rp.)");
                } else if ("diamond".equalsIgnoreCase(cId)) {
                    footer.add("§7Jumlah: §a" + ri.getAmount() + " 💎");
                    footer.add("§7Mata Uang: §eDIAMOND (💎)");
                } else {
                    footer.add("§7Jumlah: §a" + ri.getAmount() + " 🪙");
                    footer.add("§7Mata Uang: §eBATTLE COINS (🪙)");
                }
            } else if (isItem) {
                footer.add("§7Jumlah: §a" + ri.getAmount() + "x");
            }
            if (!ri.getCommands().isEmpty()) {
                footer.add("§7Commands: §f" + String.join(", ", ri.getCommands()));
            }
            footer.add(" ");
            footer.add("§e▶ [Klik Kiri] Ubah jumlah / detail hadiah");
            footer.add("§c▶ [Klik Kanan] Hapus hadiah dari level ini");

            ItemStack icon = is != null ? is.clone() : new ItemStack(Material.CHEST);
            icon.setAmount(isCurrency ? 1 : Math.max(1, Math.min(64, ri.getAmount())));

            ItemBuilder builder = new ItemBuilder(icon);
            // For ITEM type: preserve original display name & lore, then append admin footer.
            // For currency/command: use generated name and set lore fresh (no original lore).
            if (isItem) {
                builder.appendLore(footer);
            } else {
                builder.name(ri.getDisplayName()).lore(footer.subList(1, footer.size()));
            }
            ItemStack display = builder.build();

            int slot = CENTER_SLOTS[idx++];
            setButton(slot, new GuiButton(display, event -> {
                if (event.isRightClick()) {
                    plugin.getRewardManager().removeReward(level, passId, rewardIndex);
                    player.sendMessage("§cHadiah " + ri.getDisplayName() + " berhasil dihapus dari Level " + level + "!");
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 1.0f);
                    open();
                } else {
                    new AdminRewardItemEditMenu(plugin, player, passId, level, rewardIndex, this).open();
                }
            }));
        }

        // 3. Set remaining empty center slots to listen for cursor drops
        while (idx < CENTER_SLOTS.length) {
            int emptySlot = CENTER_SLOTS[idx++];
            setButton(emptySlot, new GuiButton(null, event -> {
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    ItemStack dropped = event.getCursor().clone();
                    RewardItem ri = RewardItem.fromItemStack(dropped);
                    plugin.getRewardManager().addReward(level, passId, ri);
                    player.sendMessage("§aBerhasil menambahkan item §e" + ri.getDisplayName() + " §ake Level " + level + "!");
                    open();
                }
            }));
        }

        // 4. Bottom Navigation Bar (Row 5)
        setButton(45, new BackButton(this, parent));

        // Tambah Currency (100% GUI Picker with default 100 amount)
        setButton(47, new GuiButton(new ItemBuilder(Material.EMERALD)
                .name("&e&l[💎] TAMBAH CURRENCY REWARD")
                .lore(List.of(
                        "&7Pilih mata uang melalui GUI (Battle Coins, Rupiah, Diamond).",
                        "&7Otomatis ditambahkan dengan nominal default &e100&7.",
                        " ",
                        "&eKlik untuk memilih currency via GUI >"
                ))
                .build(), event -> {
            new AdminCurrencyRewardPickerMenu(plugin, player, passId, level, this).open();
        }));

        // Tambah Command
        setButton(49, new GuiButton(new ItemBuilder(Material.COMMAND_BLOCK)
                .name("&b&l[⚡] TAMBAH COMMAND REWARD")
                .lore(List.of(
                        "&7Eksekusi perintah konsol saat reward diklaim.",
                        "&7Gunakan placeholder &e%player%&7.",
                        " ",
                        "&bKlik untuk memasukkan command >"
                ))
                .build(), event -> {
            plugin.getChatInputManager().startInput(player, "Masukkan command (contoh: give %player% diamond 5):", cmd -> {
                RewardItem ri = new RewardItem(RewardType.COMMAND, Material.COMMAND_BLOCK, 1, cmd, List.of(cmd), null);
                plugin.getRewardManager().addReward(level, passId, ri);
                player.sendMessage("§aBerhasil menambahkan command reward ke Level " + level + "!");
                open();
            }, this::open);
        }));

        // Atur Required XP
        setButton(51, new GuiButton(new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name("&6&l[📜] ATUR REQUIRED XP (Saat ini: " + reqXp + " XP)")
                .lore(List.of(
                        "&7Ubah XP yang dibutuhkan untuk mencapai level ini.",
                        " ",
                        "&eKlik untuk mengubah via GUI >"
                ))
                .build(), event -> {
            plugin.getChatInputManager().startNumericInput(player, "Masukkan required XP baru untuk Level " + level + ":", newXp -> {
                plugin.getRewardManager().setRequiredXp(level, newXp);
                plugin.getRewardManager().saveRewards();
                player.sendMessage("§aBerhasil mengubah required XP Level " + level + " menjadi §e" + newXp + " XP§a!");
                open();
            }, this::open, 1, 10000000);
        }));

        setButton(53, new CloseButton());
    }

    @Override
    public void handleBottomInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
            ItemStack item = event.getCurrentItem().clone();
            RewardItem ri = RewardItem.fromItemStack(item);
            plugin.getRewardManager().addReward(level, passId, ri);
            player.sendMessage("§aBerhasil menambahkan item §e" + ri.getDisplayName() + " §ake Level " + level + "!");
            open();
        }
    }

    @Override
    public void onInventoryDrag(InventoryDragEvent event) {
        event.setCancelled(true);
        if (event.getOldCursor() != null && event.getOldCursor().getType() != Material.AIR) {
            ItemStack item = event.getOldCursor().clone();
            RewardItem ri = RewardItem.fromItemStack(item);
            plugin.getRewardManager().addReward(level, passId, ri);
            player.sendMessage("§aBerhasil menambahkan item §e" + ri.getDisplayName() + " §ake Level " + level + "!");
            open();
        }
    }
}
