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
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Luxury Special Preview GUI for BattlePass milestone & special rewards (e.g. Level 50, 100, etc.).
 */
public class SpecialRewardPreviewMenu extends Gui {

    private final int level;
    private final String passId;
    private final List<RewardItem> rewards;

    public SpecialRewardPreviewMenu(ApexsionsBattlepass plugin, Player player, int level, String passId, List<RewardItem> rewards, Gui parent) {
        super(plugin, player, "&8[ &6&l👑 PREVIEW ISTIMEWA: &eLVL " + level + " &8]", 45, parent);
        this.level = level;
        this.passId = passId;
        this.rewards = rewards != null ? rewards : List.of();
    }

    @Override
    public void initialize() {
        fillBorder(Material.BLACK_STAINED_GLASS_PANE);

        PassTier tier = plugin.getPassManager().getPass(passId);
        String passName = tier != null ? tier.getDisplayName() : passId.toUpperCase();

        // 1. Luxury Altar Accents
        ItemStack purpleAccent = new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).name("&d✦ TAHTA ISTIMEWA ✦").build();
        ItemStack goldAccent = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("&6★ RELIK PUNCAK ★").build();

        setButton(0, new GuiButton(purpleAccent));
        setButton(8, new GuiButton(purpleAccent));
        setButton(36, new GuiButton(purpleAccent));
        setButton(44, new GuiButton(purpleAccent));

        setButton(3, new GuiButton(goldAccent));
        setButton(5, new GuiButton(goldAccent));

        // 2. Crown Altar Header (Slot 4)
        ItemStack header = new ItemBuilder(Material.NETHER_STAR)
                .name("&6&l👑 PREVIEW HADIAH ISTIMEWA: &eLEVEL " + level + " 👑")
                .lore(List.of(
                        "&7Tinjauan paket hadiah istimewa season BattlePass.",
                        "&7Tier Pass: &f" + passName,
                        "&7Syarat Buka: &eLevel " + level,
                        "&7Status: &c🔒 Masih Terkunci",
                        " ",
                        "&6✦ PAKET HADIAH SPESIAL BATTLEPASS ✦",
                        "&7Total Hadiah Istimewa: &e" + rewards.size() + " reward",
                        "&aTingkatkan XP kerajaanmu dan raih tahta hadiah ini!"
                ))
                .glow()
                .build();
        setButton(4, new GuiButton(header));

        // 3. Showcase Pedestal for Rewards (Slots 20, 21, 22, 23, 24, etc.)
        int[] displaySlots = { 20, 21, 22, 23, 24, 11, 12, 13, 14, 15, 29, 30, 31, 32, 33 };
        for (int i = 0; i < rewards.size() && i < displaySlots.length; i++) {
            RewardItem ri = rewards.get(i);
            ItemStack baseStack = ri.toItemStack();
            if (baseStack == null || baseStack.getType().isAir()) {
                baseStack = new ItemStack(Material.CHEST);
            } else {
                baseStack = baseStack.clone();
            }

            boolean isItem = ri.getType() == RewardType.ITEM;

            // Minimal footer — no excessive header labels for ITEM type
            List<String> footer = new ArrayList<>();
            if (!isItem) {
                footer.add("§6§l✦ HADIAH ISTIMEWA ✦");
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
                footer.add("§7Bonus Perintah: §b" + String.join(", ", ri.getCommands()));
            }
            footer.add(" ");
            footer.add("§e📦 Hadiah megah ini akan terbuka saat kamu mencapai §6Level " + level + "§e!");

            ItemBuilder builder = new ItemBuilder(baseStack);
            if (isItem) {
                // Preserve real item name & lore, append footer only
                builder.appendLore(footer).glow();
            } else {
                builder.name("§6§l★ " + ri.getDisplayName() + " ★").lore(footer).glow();
            }
            ItemStack previewCard = builder.build();

            setButton(displaySlots[i], new GuiButton(previewCard));
        }

        // 4. Navigation Controls (Row 4)
        setButton(37, new BackButton(this, parent));
        setButton(43, new CloseButton());

        // Status Banner (Slot 40)
        ItemStack statusCard = new ItemBuilder(Material.CRYING_OBSIDIAN)
                .name("&c&l🔒 HADIAH MASIH TERKUNCI")
                .lore(List.of(
                        "&7Capai &eLevel " + level + " &7pada &f" + passName + " &7untuk klaim.",
                        "&7Selesaikan quest harian & mingguan untuk meraih XP!"
                ))
                .build();
        setButton(40, new GuiButton(statusCard));

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
    }
}
