package com.apexsions.battlepass.admin.gui;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminConfirmMenu extends Gui {

    private final List<String> promptLines;
    private final Runnable onConfirm;
    private final Runnable onCancel;

    public AdminConfirmMenu(ApexsionsBattlepass plugin, Player player, String title, List<String> promptLines, Gui parent, Runnable onConfirm, Runnable onCancel) {
        super(plugin, player, title != null ? title : "&8[ &4&lKONFIRMASI AKSI ADMIN &8]", 36, parent);
        this.promptLines = promptLines != null ? promptLines : List.of();
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    @Override
    public void initialize() {
        fillBackground();

        // 1. Prompt Info Card (Slot 13)
        List<String> lore = new ArrayList<>(promptLines);
        lore.add(" ");
        lore.add("&c⚠ Tindakan ini tidak dapat dibatalkan!");

        setButton(13, new GuiButton(new ItemBuilder(Material.BARRIER)
                .name("&c&lKONFIRMASI TINDAKAN")
                .lore(lore)
                .build()));

        // 2. Confirm Button (Slot 20)
        setButton(20, new GuiButton(new ItemBuilder(Material.LIME_CONCRETE)
                .name("&a&l[✔] YA, LANJUTKAN TINDAKAN")
                .lore(List.of(
                        "&7Klik untuk mengeksekusi aksi ini.",
                        " ",
                        "&aKlik untuk konfirmasi >"
                ))
                .build(), event -> {
            if (!player.hasPermission("apexsionsbattlepass.admin")) {
                player.sendMessage(plugin.getMessage("admin-no-permission"));
                player.closeInventory();
                return;
            }
            if (onConfirm != null) {
                onConfirm.run();
            }
        }));

        // 3. Cancel Button (Slot 24)
        setButton(24, new GuiButton(new ItemBuilder(Material.RED_CONCRETE)
                .name("&c&l[✖] BATALKAN")
                .lore(List.of(
                        "&7Kembali tanpa melakukan perubahan.",
                        " ",
                        "&eKlik untuk kembali >"
                ))
                .build(), event -> {
            if (onCancel != null) {
                onCancel.run();
            } else if (parent != null) {
                parent.open();
            } else {
                player.closeInventory();
            }
        }));
    }
}
