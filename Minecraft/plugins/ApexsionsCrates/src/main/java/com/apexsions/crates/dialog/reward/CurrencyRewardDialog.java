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
        GREEN.wrap("• Rupiah (IDR):") + " Ditampilkan sebagai " + GREEN.and(BOLD).wrap("Glowing Emerald") + ".",
        AQUA.wrap("• Diamond:") + " Ditampilkan sebagai " + AQUA.and(BOLD).wrap("Glowing Diamond") + ".",
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
                Material mat = isRupiah ? Material.EMERALD : Material.DIAMOND;
                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                MiniMessage mm = MiniMessage.miniMessage();
                String formattedAmt = String.format("%,d", (long) amount);
                if (meta != null) {
                    meta.setEnchantmentGlintOverride(true); // GLOWING!
                    if (isRupiah) {
                        meta.displayName(mm.deserialize("<green><bold>💵 Rp " + formattedAmt + "</bold></green>"));
                        meta.lore(List.of(
                            mm.deserialize("<gray>Hadiah Saldo Apexsions Economy</gray>"),
                            mm.deserialize("<gold>Mata Uang: <yellow>Rupiah (IDR)</yellow></gold>"),
                            mm.deserialize("<gold>Nominal: <green><bold>Rp " + formattedAmt + "</bold></green></gold>")
                        ));
                    } else {
                        meta.displayName(mm.deserialize("<aqua><bold>💎 " + formattedAmt + " Diamond</bold></aqua>"));
                        meta.lore(List.of(
                            mm.deserialize("<gray>Hadiah Saldo Apexsions Economy</gray>"),
                            mm.deserialize("<gold>Mata Uang: <aqua>Diamond</aqua></gold>"),
                            mm.deserialize("<gold>Nominal: <aqua><bold>" + formattedAmt + " 💎</bold></aqua></gold>")
                        ));
                    }
                    item.setItemMeta(meta);
                }

                String idPrefix = isRupiah ? "eco_rupiah_" : "eco_diamond_";
                String rewardId = idPrefix + ((long) amount);
                int counter = 1;
                while (crate.getReward(rewardId) != null) {
                    rewardId = idPrefix + ((long) amount) + "_" + counter++;
                }

                Rarity rarity = plugin.getCrateManager().getMostCommonRarity();
                CommandReward reward = (CommandReward) RewardFactory.create(plugin, crate, rewardId, rarity, RewardType.COMMAND);
                reward.setName(isRupiah ? "<green><bold>Rp " + formattedAmt + "</bold></green>" : "<aqua><bold>" + formattedAmt + " Diamond</bold></aqua>");
                reward.setDescription(List.of(
                    "<gray>Hadiah Saldo Apexsions Economy</gray>",
                    isRupiah ? "<gold>Nominal: <green><bold>Rp " + formattedAmt + "</bold></green></gold>" : "<gold>Nominal: <aqua><bold>" + formattedAmt + " 💎</bold></aqua></gold>"
                ));
                reward.setPreview(ItemHelper.vanilla(item));
                reward.setCommands(List.of("ecoadmin give %player% " + (long) amount + " " + (isRupiah ? "rupiah" : "diamond")));

                crate.addReward(reward);
                crate.markDirty();
                player.sendMessage(mm.deserialize("<green>✓ Berhasil menambahkan Reward Currency " + (isRupiah ? "Rp " + formattedAmt : formattedAmt + " Diamond") + " (Glowing) ke dalam crate!</green>"));
                user.callback();
            });
        });
    }

    private List<WrappedSingleOptionEntry> getCurrencyOptions() {
        List<WrappedSingleOptionEntry> list = new ArrayList<>();
        list.add(new WrappedSingleOptionEntry("rupiah", "💵 Rupiah (IDR) — Glowing Emerald", true));
        list.add(new WrappedSingleOptionEntry("diamond", "💎 Diamond — Glowing Diamond", false));
        return list;
    }
}
