package com.apex.battlepass.admin.gui.reward;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.navigation.CloseButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.battlepass.reward.RewardItem;
import com.apex.battlepass.reward.RewardType;
import org.bukkit.Material;
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
                .name("&a&l[💡] DRAG & DROP ITEM KE SINI")
                .lore(List.of(
                        "&7Area tengah kosong untuk memasukkan item hadiah.",
                        "&7● &fDrag & Drop &7item dari inventory Anda ke slot kosong.",
                        "&7● Atau &fShift-Klik &7item di inventory Anda.",
                        "&7● Klik item yang sudah ada untuk edit jumlah atau hapus."
                ))
                .build()));

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
            boolean isStackable = is != null && is.getMaxStackSize() > 1;

            List<String> lore = new ArrayList<>();
            lore.add("&7Tipe: &e" + ri.getType());
            if (ri.getType() == RewardType.CURRENCY) {
                if ("rupiah".equalsIgnoreCase(ri.getCurrencyId())) {
                    lore.add("&7Jumlah: &aRp." + ri.getAmount());
                } else if ("diamond".equalsIgnoreCase(ri.getCurrencyId())) {
                    lore.add("&7Jumlah: &a" + ri.getAmount() + " Diamond");
                } else {
                    lore.add("&7Jumlah: &a" + ri.getAmount() + " Coins");
                }
                lore.add("&7Currency ID: &e" + ri.getCurrencyId().toUpperCase());
            } else {
                lore.add("&7Jumlah: &a" + ri.getAmount() + "x");
                lore.add("&7Stackable: " + (isStackable ? "&aYa (Maks " + (is != null ? is.getMaxStackSize() : 64) + ")" : "&cTidak (Maks 1)"));
            }
            if (!ri.getCommands().isEmpty()) {
                lore.add("&7Commands: &f" + String.join(", ", ri.getCommands()));
            }
            lore.add(" ");
            lore.add("&e&l[KLIK UNTUK EDIT / UBAH JUMLAH / HAPUS]");

            String displayName = ri.getDisplayName();
            if (ri.getType() == RewardType.CURRENCY && "rupiah".equalsIgnoreCase(ri.getCurrencyId())) {
                displayName = "&a&lRp." + ri.getAmount();
            }

            ItemStack display = new ItemBuilder(is != null ? is : new ItemStack(Material.CHEST))
                    .name(displayName)
                    .lore(lore)
                    .build();

            int slot = CENTER_SLOTS[idx++];
            setButton(slot, new GuiButton(display, event -> {
                new AdminRewardItemEditMenu(plugin, player, passId, level, rewardIndex, this).open();
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
                        "&eKlik untuk mengubah via chat >"
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
