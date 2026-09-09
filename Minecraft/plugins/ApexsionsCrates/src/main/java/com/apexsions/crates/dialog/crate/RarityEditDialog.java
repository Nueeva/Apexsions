package com.apexsions.crates.dialog.crate;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.crate.impl.Rarity;
import com.apexsions.crates.dialog.Dialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class RarityEditDialog extends Dialog<Rarity> {

    private static final TextLocale          TITLE = LangEntry.builder("Dialog.Rarity.Edit.Title").text(title("Rarity", "Edit"));
    private static final DialogElementLocale BODY  = LangEntry.builder("Dialog.Rarity.Edit.Body").dialogElement(300,
        "Edit the " + SOFT_YELLOW.wrap("display name") + " and " + SOFT_YELLOW.wrap("weight") + " of this rarity.",
        "",
        "Higher weight = higher chance to roll this rarity. Names support MiniMessage formatting."
    );

    private static final TextLocale INPUT_NAME   = LangEntry.builder("Dialog.Rarity.Edit.Input.Name").text(SOFT_YELLOW.wrap("Display Name"));
    private static final TextLocale INPUT_WEIGHT = LangEntry.builder("Dialog.Rarity.Edit.Input.Weight").text(SOFT_YELLOW.wrap("Weight"));

    private static final String JSON_NAME   = "rarity_name";
    private static final String JSON_WEIGHT = "rarity_weight";

    @Override
    @NotNull
    public WrappedDialog create(@NotNull Player player, @NotNull Rarity rarity) {
        return Dialogs.create(builder -> {
            builder.base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(
                    DialogInputs.text(JSON_NAME,   INPUT_NAME).initial(rarity.getName()).maxLength(100).build(),
                    DialogInputs.text(JSON_WEIGHT, INPUT_WEIGHT).initial(String.valueOf(rarity.getWeight())).maxLength(10).build()
                )
                .build()
            );

            builder.type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build());

            builder.handleResponse(DialogActions.OK, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                String name   = nbtHolder.getText(JSON_NAME, rarity.getName());
                double weight = nbtHolder.getDouble(JSON_WEIGHT, rarity.getWeight());

                rarity.setName(name);
                rarity.setWeight(Math.max(0.01, weight));

                viewer.callback();
            });
        });
    }
}
