package com.apex.battlepass.admin.gui.reward;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.navigation.CloseButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.battlepass.pass.PassTier;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AdminRewardPassMenu extends Gui {

    public AdminRewardPassMenu(ApexsionsBattlepass plugin, Player player, Gui parent) {
        super(plugin, player, "&8[ &4&lREWARD EDITOR &8- Pilih Pass ]", 36, parent);
    }

    @Override
    public void initialize() {
        fillBackground();

        // 1. Header Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.CHEST)
                .name("&6&lPILIH PASS TIER")
                .lore(List.of(
                        "&7Pilih tingkatan pass yang ingin dikelola hadiahnya.",
                        "&7Setiap level dan setiap pass mendukung multiple rewards.",
                        " ",
                        "&7Total Pass Terdaftar: &e" + plugin.getPassManager().getPasses().size()
                ))
                .build()));

        // 2. Render Pass Tiers
        int[] slots = { 10, 11, 12, 13, 14, 15, 16 };
        int idx = 0;

        for (PassTier tier : plugin.getPassManager().getPasses().values()) {
            if (idx >= slots.length) break;

            ItemStack item = new ItemBuilder(tier.getIcon())
                    .name("&e&l" + tier.getDisplayName())
                    .lore(List.of(
                            "&7ID: &f" + tier.getId(),
                            "&7Priority: &b" + tier.getPriority(),
                            "&7Default Owned: " + (tier.isDefaultOwned() ? "&aYa" : "&cTidak"),
                            " ",
                            "&eKlik untuk memilih pass ini >"
                    ))
                    .build();

            setButton(slots[idx++], new GuiButton(item, event -> {
                new AdminRewardLevelListMenu(plugin, player, tier.getId(), this).open();
            }));
        }

        // Navigation
        setButton(27, new BackButton(this, parent));
        setButton(35, new CloseButton());
    }
}
