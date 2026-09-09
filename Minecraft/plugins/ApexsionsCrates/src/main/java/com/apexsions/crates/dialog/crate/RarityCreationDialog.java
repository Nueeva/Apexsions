package com.apexsions.crates.dialog.crate;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.CratesPlugin;
import com.apexsions.crates.crate.CrateManager;
import com.apexsions.crates.crate.impl.Rarity;
import com.apexsions.crates.dialog.Dialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.util.Strings;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class RarityCreationDialog extends Dialog<CrateManager> {

    private static final TextLocale          TITLE = LangEntry.builder("Dialog.Rarity.Create.Title").text(title("Rarity", "Create"));
    private static final DialogElementLocale BODY  = LangEntry.builder("Dialog.Rarity.Create.Body").dialogElement(300,
        "Enter a unique " + SOFT_YELLOW.wrap("ID") + ", " + SOFT_YELLOW.wrap("display name") + ", and " + SOFT_YELLOW.wrap("weight") + " for the new rarity.",
        "",
        "Higher weight = more common. Example: common=500, secret=3."
    );

    private static final TextLocale INPUT_ID     = LangEntry.builder("Dialog.Rarity.Create.Input.Id").text(SOFT_YELLOW.wrap("ID (lowercase, no spaces)"));
    private static final TextLocale INPUT_NAME   = LangEntry.builder("Dialog.Rarity.Create.Input.Name").text(SOFT_YELLOW.wrap("Display Name (MiniMessage supported)"));
    private static final TextLocale INPUT_WEIGHT = LangEntry.builder("Dialog.Rarity.Create.Input.Weight").text(SOFT_YELLOW.wrap("Weight (e.g. 100)"));

    private static final String JSON_ID     = "rarity_id";
    private static final String JSON_NAME   = "rarity_name";
    private static final String JSON_WEIGHT = "rarity_weight";

    private final CratesPlugin plugin;

    public RarityCreationDialog(@NotNull CratesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public WrappedDialog create(@NotNull Player player, @NotNull CrateManager manager) {
        return Dialogs.create(builder -> {
            builder.base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(
                    DialogInputs.text(JSON_ID,     INPUT_ID).maxLength(32).build(),
                    DialogInputs.text(JSON_NAME,   INPUT_NAME).initial("New Rarity").maxLength(100).build(),
                    DialogInputs.text(JSON_WEIGHT, INPUT_WEIGHT).initial("100").maxLength(10).build()
                )
                .build()
            );

            builder.type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build());

            builder.handleResponse(DialogActions.OK, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                String rawId = nbtHolder.getText(JSON_ID).orElse(null);
                if (rawId == null) return;

                String id = Strings.varStyle(rawId).orElse(null);
                if (id == null || manager.getRarity(id) != null) return; // already exists

                String name   = nbtHolder.getText(JSON_NAME, "New Rarity");
                double weight = nbtHolder.getDouble(JSON_WEIGHT, 100D);

                Rarity rarity = new Rarity(this.plugin, id, name, Math.max(0.01, weight));
                manager.addRarity(rarity);
                viewer.callback();
            });
        });
    }
}
