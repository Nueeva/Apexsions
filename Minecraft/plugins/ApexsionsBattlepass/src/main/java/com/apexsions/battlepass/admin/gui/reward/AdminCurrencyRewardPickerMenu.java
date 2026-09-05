package com.apexsions.battlepass.admin.gui.reward;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.navigation.CloseButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.reward.RewardItem;
import com.apexsions.battlepass.reward.RewardType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminCurrencyRewardPickerMenu extends Gui {

    private final String passId;
    private final int level;

    public AdminCurrencyRewardPickerMenu(ApexsionsBattlepass plugin, Player player, String passId, int level, Gui parent) {
        super(plugin, player, "&8[ &4&lPILIH MATA UANG HADIAH &8]", 45, parent);
        this.passId = passId;
        this.level = level;
    }

    @Override
    public void initialize() {
        fillBackground();

        // Header Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.EMERALD)
                .name("&6&lPILIH MATA UANG REWARD: &eLVL " + level)
                .lore(List.of(
                        "&7Pilih jenis mata uang yang ingin ditambahkan.",
                        "&7Hadiah akan langsung ditambahkan dengan nominal default &e100&7.",
                        "&7Anda dapat mengubah nominalnya kapan saja dengan mengklik item."
                ))
                .build()));

        // 1. Battle Coins (Slot 20)
        setButton(20, new GuiButton(new ItemBuilder(Material.GOLD_INGOT)
                .name("&e&l[🪙] BATTLE COINS")
                .lore(List.of(
                        "&7Mata uang eksklusif BattlePass.",
                        "&7Nominal Awal: &e100 Coins",
                        " ",
                        "&aKlik untuk langsung menambahkan >"
                ))
                .build(), event -> {
            addCurrencyReward("battle_coins", Material.GOLD_INGOT, "Battle Coins");
        }));

        // 2. Rupiah (Slot 22)
        setButton(22, new GuiButton(new ItemBuilder(Material.EMERALD)
                .name("&a&l[💵] RUPIAH (Rp.)")
                .lore(List.of(
                        "&7Mata uang Rupiah ekonomi server.",
                        "&7Nominal Awal: &eRp.100",
                        " ",
                        "&aKlik untuk langsung menambahkan >"
                ))
                .build(), event -> {
            addCurrencyReward("rupiah", Material.EMERALD, "Rp.100");
        }));

        // 3. Diamond (Slot 24)
        setButton(24, new GuiButton(new ItemBuilder(Material.DIAMOND)
                .name("&b&l[💎] DIAMOND")
                .lore(List.of(
                        "&7Mata uang Diamond ekonomi server.",
                        "&7Nominal Awal: &e100 💎",
                        " ",
                        "&aKlik untuk langsung menambahkan >"
                ))
                .build(), event -> {
            addCurrencyReward("diamond", Material.DIAMOND, "Diamond");
        }));

        // Navigation
        setButton(36, new BackButton(this, parent));
        setButton(44, new CloseButton());
    }

    private void addCurrencyReward(String currencyId, Material icon, String displayName) {
        String name = currencyId.equalsIgnoreCase("rupiah") ? "Rp.100" : (displayName.contains("100") ? displayName : ("100 " + displayName));
        RewardItem ri = new RewardItem(RewardType.CURRENCY, icon, 100, name, List.of(), null, null, currencyId.toLowerCase());
        plugin.getRewardManager().addReward(level, passId, ri);
        player.sendMessage("§aBerhasil menambahkan reward §e" + name + "§a! Klik item pada daftar untuk mengubah jumlahnya.");
        if (parent != null) {
            parent.open();
        }
    }
}
