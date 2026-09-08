package com.apexsions.battlepass.reward.gui;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.navigation.CloseButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.pass.PassTier;
import com.apexsions.battlepass.reward.RewardItem;
import com.apexsions.battlepass.reward.RewardType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Preview GUI for locked BattlePass rewards (Milestones & Previewable Rewards).
 */
public class RewardPreviewMenu extends Gui {

    private final int level;
    private final String passId;
    private final List<RewardItem> rewards;

    public RewardPreviewMenu(ApexsionsBattlepass plugin, Player player, int level, String passId, List<RewardItem> rewards, Gui parent) {
        super(plugin, player, "&8[ &6&lPREVIEW HADIAH: &eLVL " + level + " &8]", 36, parent);
        this.level = level;
        this.passId = passId;
        this.rewards = rewards != null ? rewards : List.of();
    }

    @Override
    public void initialize() {
        fillBackground(Material.BLACK_STAINED_GLASS_PANE);

        PassTier tier = plugin.getPassManager().getPass(passId);
        String passName = tier != null ? tier.getDisplayName() : passId.toUpperCase();

        // 1. Header Card (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.CHEST)
                .name("&6&lPREVIEW HADIAH: &eLEVEL " + level)
                .lore(List.of(
                        "&7Tier Pass: &f" + passName,
                        "&7Syarat Buka: &eLevel " + level,
                        "&7Status: &c🔒 Belum Terbuka",
                        " ",
                        "&7Total Hadiah di Level Ini: &e" + rewards.size() + " hadiah",
                        "&aTingkatkan XP dan capai level ini untuk klaim!"
                ))
                .build()));

        // 2. Render Reward Items in row 2 (Slots 10-16) and row 3 if needed
        int[] displaySlots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25 };
        for (int i = 0; i < rewards.size() && i < displaySlots.length; i++) {
            RewardItem ri = rewards.get(i);
            ItemStack baseStack = ri.toItemStack();
            if (baseStack == null || baseStack.getType().isAir()) {
                baseStack = new ItemStack(Material.CHEST);
            } else {
                baseStack = baseStack.clone();
            }

            boolean isItem = ri.getType() == RewardType.ITEM;

            // Minimal footer — only info needed for player preview
            List<String> footer = new ArrayList<>();
            if (!isItem) {
                // For currency/command: show generated info since there is no original lore
                footer.add("§7Tipe: §e" + ri.getType().name());
            }
            if (ri.getType() == RewardType.CURRENCY) {
                if ("rupiah".equalsIgnoreCase(ri.getCurrencyId())) {
                    footer.add("§7Nominal: §aRp " + String.format("%,d", (long) ri.getAmount()).replace(",", "."));
                } else if ("diamond".equalsIgnoreCase(ri.getCurrencyId())) {
                    footer.add("§7Nominal: §b" + ri.getAmount() + " Diamond 💎");
                } else {
                    footer.add("§7Nominal: §e" + ri.getAmount() + " Coins");
                }
            } else if (ri.getType() == RewardType.MONEY) {
                footer.add("§7Nominal: §aRp " + String.format("%,d", (long) ri.getAmount()).replace(",", "."));
            } else if (isItem) {
                footer.add("§7Jumlah: §a" + ri.getAmount() + "x");
            }
            if (!ri.getCommands().isEmpty()) {
                footer.add("§7Perintah: §b" + String.join(", ", ri.getCommands()));
            }
            footer.add(" ");
            footer.add("§b✦ Hadiah ini akan kamu dapatkan di Level " + level + "!");

            ItemBuilder builder = new ItemBuilder(baseStack);
            if (isItem) {
                // Preserve real item name & lore, just append our footer
                builder.appendLore(footer);
            } else {
                builder.name("§e§l" + ri.getDisplayName()).lore(footer);
            }
            ItemStack previewCard = builder.build();

            setButton(displaySlots[i], new GuiButton(previewCard));
        }

        // 3. Navigation Controls
        setButton(27, new BackButton(this, parent));
        setButton(35, new CloseButton());
    }
}
