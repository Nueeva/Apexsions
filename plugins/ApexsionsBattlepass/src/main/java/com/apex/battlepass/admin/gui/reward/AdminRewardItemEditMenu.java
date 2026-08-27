package com.apex.battlepass.admin.gui.reward;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.navigation.CloseButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.battlepass.reward.RewardItem;
import com.apex.battlepass.reward.RewardType;
import com.apex.battlepass.util.ItemSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AdminRewardItemEditMenu extends Gui {

    private final String passId;
    private final int level;
    private final int rewardIndex;

    public AdminRewardItemEditMenu(ApexsionsBattlepass plugin, Player player, String passId, int level, int rewardIndex, Gui parent) {
        super(plugin, player, "&8[ &4&lEDIT HADIAH: &e" + passId.toUpperCase() + " LVL " + level + " &8]", 45, parent);
        this.passId = passId;
        this.level = level;
        this.rewardIndex = rewardIndex;
    }

    @Override
    public void initialize() {
        fillBorder();

        List<RewardItem> rewards = plugin.getRewardManager().getRewards(level, passId);
        if (rewardIndex < 0 || rewardIndex >= rewards.size()) {
            if (parent != null) parent.open();
            return;
        }

        RewardItem item = rewards.get(rewardIndex);
        ItemStack itemStack = item.toItemStack();
        boolean isStackable = itemStack != null && itemStack.getMaxStackSize() > 1;

        // 1. Overview Card (Slot 4)
        List<String> overviewLore = new ArrayList<>();
        overviewLore.add("&7Tipe: &e" + item.getType());
        if (item.getType() == RewardType.CURRENCY) {
            if ("rupiah".equalsIgnoreCase(item.getCurrencyId())) {
                overviewLore.add("&7Nama: &fRp." + item.getAmount());
                overviewLore.add("&7Jumlah: &aRp." + item.getAmount());
            } else if ("diamond".equalsIgnoreCase(item.getCurrencyId())) {
                overviewLore.add("&7Nama: &f" + item.getAmount() + " Diamond");
                overviewLore.add("&7Jumlah: &a" + item.getAmount() + " Diamond");
            } else {
                overviewLore.add("&7Nama: &f" + item.getAmount() + " Coins");
                overviewLore.add("&7Jumlah: &a" + item.getAmount() + " Coins");
            }
            overviewLore.add("&7Currency ID: &e" + item.getCurrencyId().toUpperCase());
        } else {
            overviewLore.add("&7Nama: &f" + item.getDisplayName());
            overviewLore.add("&7Jumlah: &a" + item.getAmount() + "x");
            overviewLore.add("&7Stackable: " + (isStackable ? "&aYa (Maks " + itemStack.getMaxStackSize() + ")" : "&cTidak (Maks 1)"));
        }
        if (!item.getCommands().isEmpty()) {
            overviewLore.add("&7Command: &f" + String.join(", ", item.getCommands()));
        }

        String overviewTitle = "&6&lDETAIL HADIAH #" + (rewardIndex + 1);
        if (item.getType() == RewardType.CURRENCY && "rupiah".equalsIgnoreCase(item.getCurrencyId())) {
            overviewTitle = "&6&lDETAIL HADIAH: &a&lRp." + item.getAmount();
        }

        setButton(4, new GuiButton(new ItemBuilder(itemStack != null ? itemStack : new ItemStack(Material.CHEST))
                .name(overviewTitle)
                .lore(overviewLore)
                .build()));

        // 2. Action Controls
        if (item.getType() == RewardType.ITEM) {
            if (isStackable) {
                // Stackable: Allow Amount Editing via Chat Input
                setButton(20, new GuiButton(new ItemBuilder(Material.ANVIL)
                        .name("&e&l[🔢] UBAH JUMLAH ITEM (Saat ini: x" + item.getAmount() + ")")
                        .lore(List.of(
                                "&7Item ini &adapat di-stack&7.",
                                "&7Maksimum stack: &f" + itemStack.getMaxStackSize(),
                                " ",
                                "&eKlik untuk mengubah jumlah via chat >"
                        ))
                        .build(), event -> {
                    plugin.getChatInputManager().startNumericInput(player, "Masukkan jumlah item baru (1 - " + itemStack.getMaxStackSize() + "):", newAmount -> {
                        String updatedData = item.getItemData();
                        if (item.getItemData() != null && !item.getItemData().isBlank()) {
                            ItemStack is = ItemSerializer.fromBase64(item.getItemData());
                            if (is != null) {
                                is.setAmount(newAmount);
                                updatedData = ItemSerializer.toBase64(is);
                            }
                        }
                        RewardItem updated = new RewardItem(item.getType(), item.getMaterial(), newAmount, item.getName(), item.getCommands(), item.getPermission(), updatedData, item.getCurrencyId());
                        plugin.getRewardManager().updateReward(level, passId, rewardIndex, updated);
                        player.sendMessage("§aJumlah item berhasil diubah menjadi §e" + newAmount + "x§a!");
                        open();
                    }, this::open, 1, itemStack.getMaxStackSize());
                }));
            } else {
                // Non-stackable: Locked to 1
                setButton(20, new GuiButton(new ItemBuilder(Material.BARRIER)
                        .name("&c&l[🔒] JUMLAH TERKUNCI (x1)")
                        .lore(List.of(
                                "&7Item ini &ctidak dapat di-stack&7",
                                "&7(seperti senjata, armor, alat, totem, dll).",
                                "&7Jumlah reward otomatis terkunci pada &f1x&7."
                        ))
                        .build()));
            }
        } else if (item.getType() == RewardType.CURRENCY) {
            // Currency Amount Editing
            String amountDisplay = "rupiah".equalsIgnoreCase(item.getCurrencyId()) ? ("Rp." + item.getAmount()) : (item.getAmount() + " " + item.getCurrencyId().toUpperCase());
            setButton(19, new GuiButton(new ItemBuilder(Material.GOLD_INGOT)
                    .name("&e&l[💰] UBAH JUMLAH SALDO (Saat ini: " + amountDisplay + ")")
                    .lore(List.of("&7Atur nominal saldo yang diberikan.", " ", "&eKlik untuk mengubah via chat >"))
                    .build(), event -> {
                plugin.getChatInputManager().startNumericInput(player, "Masukkan nominal saldo baru:", newAmount -> {
                    String name = "rupiah".equalsIgnoreCase(item.getCurrencyId()) ? ("Rp." + newAmount) : (newAmount + " " + item.getCurrencyId().toUpperCase());
                    RewardItem updated = new RewardItem(item.getType(), item.getMaterial(), newAmount, name, item.getCommands(), item.getPermission(), item.getItemData(), item.getCurrencyId());
                    plugin.getRewardManager().updateReward(level, passId, rewardIndex, updated);
                    player.sendMessage("§aNominal saldo berhasil diubah menjadi §e" + name + "§a!");
                    open();
                }, this::open, 1, 1000000000);
            }));

            // Currency Switcher (100% GUI Buttons)
            setButton(21, new GuiButton(new ItemBuilder(Material.EMERALD)
                    .name("&a&l[🪙] GANTI MATA UANG (Saat ini: " + item.getCurrencyId().toUpperCase() + ")")
                    .lore(List.of(
                            "&7Klik untuk beralih mata uang:",
                            "&8- &fRupiah (Rp.)",
                            "&8- &fDiamond",
                            "&8- &fBattle Coins",
                            " ",
                            "&aKlik untuk beralih >"
                    ))
                    .build(), event -> {
                String[] currs = { "rupiah", "diamond", "battle_coins" };
                int next = 0;
                for (int i = 0; i < currs.length; i++) {
                    if (currs[i].equalsIgnoreCase(item.getCurrencyId())) {
                        next = (i + 1) % currs.length;
                        break;
                    }
                }
                String nextCurr = currs[next];
                String name = "rupiah".equalsIgnoreCase(nextCurr) ? ("Rp." + item.getAmount()) : (item.getAmount() + " " + nextCurr.toUpperCase());
                RewardItem updated = new RewardItem(item.getType(), item.getMaterial(), item.getAmount(), name, item.getCommands(), item.getPermission(), item.getItemData(), nextCurr);
                plugin.getRewardManager().updateReward(level, passId, rewardIndex, updated);
                player.sendMessage("§aMata uang diubah menjadi §e" + nextCurr.toUpperCase() + "§a!");
                open();
            }));
        } else if (item.getType() == RewardType.COMMAND) {
            setButton(20, new GuiButton(new ItemBuilder(Material.COMMAND_BLOCK)
                    .name("&b&l[⚡] UBAH PERINTAH COMMAND")
                    .lore(List.of(
                            "&7Perintah saat ini:",
                            "&f" + (item.getName() != null ? item.getName() : "None"),
                            " ",
                            "&bKlik untuk mengubah command via chat >"
                    ))
                    .build(), event -> {
                plugin.getChatInputManager().startInput(player, "Masukkan command baru (gunakan placeholder %player%):", newCmd -> {
                    RewardItem updated = new RewardItem(item.getType(), item.getMaterial(), 1, newCmd, List.of(newCmd), item.getPermission(), item.getItemData(), item.getCurrencyId());
                    plugin.getRewardManager().updateReward(level, passId, rewardIndex, updated);
                    player.sendMessage("§aCommand berhasil diperbarui!");
                    open();
                }, this::open);
            }));
        }

        // Delete Reward Button (Slot 24)
        setButton(24, new GuiButton(new ItemBuilder(Material.RED_CONCRETE)
                .name("&4&l[✖] HAPUS HADIAH INI")
                .lore(List.of(
                        "&7Hapus hadiah ini dari Level " + level + ".",
                        " ",
                        "&cKlik untuk menghapus >"
                ))
                .build(), event -> {
            plugin.getRewardManager().removeReward(level, passId, rewardIndex);
            player.sendMessage("§cHadiah berhasil dihapus dari Level " + level + "!");
            if (parent != null) parent.open();
        }));

        // Navigation
        setButton(36, new BackButton(this, parent));
        setButton(44, new CloseButton());
    }
}
