package com.apexsions.crates.dialog.reward;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.CratesPlugin;
import com.apexsions.crates.api.crate.Reward;
import com.apexsions.crates.api.crate.RewardType;
import com.apexsions.crates.crate.impl.Crate;
import com.apexsions.crates.crate.impl.Rarity;
import com.apexsions.crates.crate.reward.RewardFactory;
import com.apexsions.crates.crate.reward.impl.CommandReward;
import com.apexsions.crates.dialog.Dialog;
import com.apexsions.crates.util.ItemHelper;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.single.WrappedSingleOptionEntry;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;

import java.util.ArrayList;
import java.util.List;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class CurrencyRewardDialog extends Dialog<Crate> {

    private static final String INPUT_CURRENCY = "currency";
    private static final String INPUT_AMOUNT   = "amount";

    private static final TextLocale TITLE = LangEntry.builder("Dialog.Reward.Currency.Title")
        .text(title("Reward", "Tambah Saldo Currency"));

    private static final DialogElementLocale BODY = LangEntry.builder("Dialog.Reward.Currency.Body").dialogElement(400,
        "Pilih jenis saldo currency yang ingin dijadikan reward untuk crate ini.",
        "",
        GREEN.wrap("• Rupiah (Rp.):") + " Ditampilkan sebagai " + GREEN.and(BOLD).wrap("Glowing Emerald") + ".",
        AQUA.wrap("• Diamond (💎):") + " Ditampilkan sebagai " + AQUA.and(BOLD).wrap("Glowing Diamond") + ".",
        YELLOW.wrap("• Battle Coins (🪙):") + " Ditampilkan sebagai " + YELLOW.and(BOLD).wrap("Glowing Gold Ingot") + ".",
        "",
        GRAY.wrap("Pemenang crate akan mendapatkan saldo langsung ke akun Apexsions.")
    );

    private static final TextLocale LABEL_CURRENCY = LangEntry.builder("Dialog.Reward.Currency.Input.Currency").text(SOFT_YELLOW.wrap("Mata Uang (Currency)"));
    private static final TextLocale LABEL_AMOUNT   = LangEntry.builder("Dialog.Reward.Currency.Input.Amount").text("Nominal Saldo");

    private final CratesPlugin plugin;

    public CurrencyRewardDialog(@NotNull CratesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public WrappedDialog create(@NotNull Player player, @NotNull Crate crate) {
        return Dialogs.create(builder -> {
            builder.base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(
                    DialogInputs.singleOption(INPUT_CURRENCY, LABEL_CURRENCY, getCurrencyOptions()).build(),
                    DialogInputs.text(INPUT_AMOUNT, LABEL_AMOUNT).maxLength(15).initial("50000").build()
                )
                .build()
            );

            builder.type(DialogTypes.multiAction(DialogButtons.ok())
                .exitAction(DialogButtons.back())
                .build()
            );

            builder.handleResponse(DialogActions.OK, (user, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                String currencyId = nbtHolder.getText(INPUT_CURRENCY, "rupiah");
                double amount = nbtHolder.getDouble(INPUT_AMOUNT, 50000.0);
                if (amount <= 0) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>⚠ Nominal saldo harus lebih besar dari 0!</red>"));
                    return;
                }

                boolean isRupiah = currencyId.equalsIgnoreCase("rupiah");
                boolean isDiamond = currencyId.equalsIgnoreCase("diamond");
                boolean isBattleCoins = currencyId.equalsIgnoreCase("battle_coins");

                Material mat = isRupiah ? Material.EMERALD : (isDiamond ? Material.DIAMOND : Material.GOLD_INGOT);
                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                MiniMessage mm = MiniMessage.miniMessage();
                String formattedAmt = String.format("%,d", (long) amount).replace(',', '.');

                String titleDisplay;
                String currencyDisplay;
                String commandStr;
                String idPrefix;

                if (isRupiah) {
                    titleDisplay = "<green><bold>Rp. " + formattedAmt + "</bold></green>";
                    currencyDisplay = "<yellow>Rupiah (Rp.)</yellow>";
                    commandStr = "ecoadmin give %player% " + (long) amount + " rupiah";
                    idPrefix = "eco_rupiah_";
                } else if (isDiamond) {
                    titleDisplay = "<aqua><bold>" + formattedAmt + " 💎</bold></aqua>";
                    currencyDisplay = "<aqua>Diamond (💎)</aqua>";
                    commandStr = "ecoadmin give %player% " + (long) amount + " diamond";
                    idPrefix = "eco_diamond_";
                } else {
                    titleDisplay = "<yellow><bold>" + formattedAmt + " 🪙</bold></yellow>";
                    currencyDisplay = "<yellow>Battle Coins (🪙)</yellow>";
                    commandStr = "abp currency add %player% " + (long) amount;
                    idPrefix = "bp_coins_";
                }

                if (meta != null) {
                    meta.setEnchantmentGlintOverride(true); // GLOWING!
                    meta.displayName(mm.deserialize(titleDisplay));
                    meta.lore(List.of(
                        mm.deserialize("<gray>Hadiah Saldo Apexsions</gray>"),
                        mm.deserialize("<gold>Mata Uang: " + currencyDisplay + "</gold>"),
                        mm.deserialize("<gold>Nominal: " + titleDisplay + "</gold>")
                    ));
                    item.setItemMeta(meta);
                }

                String rewardId = idPrefix + ((long) amount);
                int counter = 1;
                while (crate.getReward(rewardId) != null) {
                    rewardId = idPrefix + ((long) amount) + "_" + counter++;
                }

                Rarity rarity = plugin.getCrateManager().getMostCommonRarity();
                CommandReward reward = (CommandReward) RewardFactory.create(plugin, crate, rewardId, rarity, RewardType.COMMAND);
                reward.setName(titleDisplay);
                reward.setDescription(List.of(
                    "<gray>Hadiah Saldo Apexsions</gray>",
                    "<gold>Nominal: " + titleDisplay + "</gold>"
                ));
                reward.setPreview(ItemHelper.vanilla(item));
                reward.setCommands(List.of(commandStr));

                crate.addReward(reward);
                crate.markDirty();
                player.sendMessage(mm.deserialize("<green>✓ Berhasil menambahkan Reward Currency " + titleDisplay + " (Glowing) ke dalam crate!</green>"));
                user.callback();
            });
        });
    }

    private List<WrappedSingleOptionEntry> getCurrencyOptions() {
        List<WrappedSingleOptionEntry> list = new ArrayList<>();
        list.add(new WrappedSingleOptionEntry("rupiah", "💵 Rupiah (Rp.) — Glowing Emerald", true));
        list.add(new WrappedSingleOptionEntry("diamond", "💎 Diamond (💎) — Glowing Diamond", false));
        list.add(new WrappedSingleOptionEntry("battle_coins", "🪙 Battle Coins (🪙) — Glowing Gold Ingot", false));
        return list;
    }
}
