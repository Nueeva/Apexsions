package com.apexsions.battlepass.admin.gui.reward;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.navigation.CloseButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.reward.RewardItem;
import com.apexsions.battlepass.reward.RewardType;
import com.apexsions.battlepass.util.ItemSerializer;
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
        boolean isCurrency = item.isCurrency();
        boolean isStackable = !isCurrency && itemStack != null && itemStack.getMaxStackSize() > 1;

        // 1. Overview Card (Slot 4)
        List<String> overviewLore = new ArrayList<>();
        overviewLore.add("&7Tipe: &e" + item.getType());
        if (isCurrency) {
            String cId = item.getCurrencyId();
            if ("rupiah".equalsIgnoreCase(cId) || item.getType() == RewardType.MONEY) {
                overviewLore.add("&7Nama: &fRp." + String.format("%,d", (long) item.getAmount()).replace(',', '.'));
                overviewLore.add("&7Jumlah: &aRp." + String.format("%,d", (long) item.getAmount()).replace(',', '.'));
                overviewLore.add("&7Mata Uang: &eRUPIAH");
            } else if ("diamond".equalsIgnoreCase(cId)) {
                overviewLore.add("&7Nama: &f" + item.getAmount() + " Diamond 💎");
                overviewLore.add("&7Jumlah: &a" + item.getAmount() + " Diamond");
                overviewLore.add("&7Mata Uang: &eDIAMOND");
            } else {
                overviewLore.add("&7Nama: &f" + item.getAmount() + " Battle Coins");
                overviewLore.add("&7Jumlah: &a" + item.getAmount() + " Coins");
                overviewLore.add("&7Mata Uang: &e" + (cId != null ? cId.toUpperCase() : "BATTLE_COINS"));
            }
        } else {
            overviewLore.add("&7Nama: &f" + item.getDisplayName());
            overviewLore.add("&7Jumlah: &a" + item.getAmount() + "x");
            overviewLore.add("&7Stackable: " + (isStackable ? "&aYa (Maks " + itemStack.getMaxStackSize() + ")" : "&cTidak (Maks 1)"));
        }
        if (!item.getCommands().isEmpty()) {
            overviewLore.add("&7Command: &f" + String.join(", ", item.getCommands()));
        }

        String overviewTitle = "&6&lDETAIL HADIAH #" + (rewardIndex + 1);
        if (isCurrency) {
            overviewTitle = "&6&lDETAIL HADIAH: " + item.getDisplayName();
        }

        ItemStack cardIcon = itemStack != null ? itemStack.clone() : new ItemStack(Material.CHEST);
        cardIcon.setAmount(1);

        setButton(4, new GuiButton(new ItemBuilder(cardIcon)
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
                                "&eKlik untuk mengubah jumlah via GUI >"
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
                        RewardItem updated = new RewardItem(item.getType(), item.getMaterial(), newAmount, item.getName(), item.getCommands(), item.getPermission(), updatedData, item.getCurrencyId(), item.isSpecialPreview());
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
        } else if (isCurrency) {
            // Currency Amount Editing
            String cId = item.getCurrencyId();
            if (cId == null || cId.isBlank() || item.getType() == RewardType.MONEY) {
                cId = "rupiah";
            }
            String amountDisplay = "rupiah".equalsIgnoreCase(cId) ? ("Rp." + String.format("%,d", (long) item.getAmount()).replace(',', '.')) : (item.getAmount() + " " + cId.toUpperCase());
            final String activeCId = cId;

            setButton(19, new GuiButton(new ItemBuilder(Material.GOLD_INGOT)
                    .name("&e&l[💰] UBAH JUMLAH SALDO (Saat ini: " + amountDisplay + ")")
                    .lore(List.of("&7Atur nominal saldo yang diberikan.", " ", "&eKlik untuk mengubah via GUI >"))
                    .build(), event -> {
                plugin.getChatInputManager().startNumericInput(player, "Masukkan nominal saldo baru:", newAmount -> {
                    String name = "rupiah".equalsIgnoreCase(activeCId)
                            ? ("Rp." + String.format("%,d", (long) newAmount).replace(',', '.'))
                            : ("diamond".equalsIgnoreCase(activeCId) ? (newAmount + " Diamond 💎") : (newAmount + " Battle Coins"));
                    RewardItem updated = new RewardItem(RewardType.CURRENCY, item.getMaterial(), newAmount, name, item.getCommands(), item.getPermission(), item.getItemData(), activeCId, item.isSpecialPreview());
                    plugin.getRewardManager().updateReward(level, passId, rewardIndex, updated);
                    player.sendMessage("§aNominal saldo berhasil diubah menjadi §e" + name + "§a!");
                    open();
                }, this::open, 1, 1000000000);
            }));

            // Currency Switcher (100% GUI Buttons)
            setButton(21, new GuiButton(new ItemBuilder(Material.SUNFLOWER)
                    .name("&6&l[🔄] GANTI MATA UANG (Saat ini: " + activeCId.toUpperCase() + ")")
                    .lore(List.of(
                            "&7Klik untuk beralih tipe mata uang:",
                            "&f- Rupiah",
                            "&f- Battle Coins",
                            "&f- Diamond",
                            " ",
                            "&eKlik untuk beralih >"
                    ))
                    .build(), event -> {
                String[] currs = new String[]{"rupiah", "battle_coins", "diamond"};
                int next = 0;
                for (int i = 0; i < currs.length; i++) {
                    if (currs[i].equalsIgnoreCase(activeCId)) {
                        next = (i + 1) % currs.length;
                        break;
                    }
                }
                String nextCurr = currs[next];
                Material icon = nextCurr.equalsIgnoreCase("rupiah") ? Material.GOLD_INGOT : (nextCurr.equalsIgnoreCase("diamond") ? Material.DIAMOND : Material.SUNFLOWER);
                String name = "rupiah".equalsIgnoreCase(nextCurr)
                        ? ("Rp." + String.format("%,d", (long) item.getAmount()).replace(',', '.'))
                        : (nextCurr.equalsIgnoreCase("diamond") ? (item.getAmount() + " Diamond 💎") : (item.getAmount() + " Battle Coins"));
                RewardItem updated = new RewardItem(RewardType.CURRENCY, icon, item.getAmount(), name, item.getCommands(), item.getPermission(), item.getItemData(), nextCurr, item.isSpecialPreview());
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
                            "&bKlik untuk mengubah command via GUI >"
                    ))
                    .build(), event -> {
                plugin.getChatInputManager().startInput(player, "Masukkan command baru (gunakan placeholder %player%):", newCmd -> {
                    RewardItem updated = new RewardItem(item.getType(), item.getMaterial(), 1, newCmd, List.of(newCmd), item.getPermission(), item.getItemData(), item.getCurrencyId(), item.isSpecialPreview());
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
