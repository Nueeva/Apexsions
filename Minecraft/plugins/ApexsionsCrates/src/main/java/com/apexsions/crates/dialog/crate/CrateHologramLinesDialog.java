package com.apexsions.crates.dialog.crate;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.crate.impl.Crate;
import com.apexsions.crates.dialog.Dialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.WrappedDialogInput;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.util.Plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.apexsions.crates.Placeholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class CrateHologramLinesDialog extends Dialog<Crate> {

    /** Maximum number of editable hologram lines. */
    private static final int LINES_AMOUNT = 8;

    private static final TextLocale TITLE = LangEntry.builder("Dialog.Crate.HologramLines.Title").text(title("Crate", "Hologram Lines"));

    private static final DialogElementLocale BODY = LangEntry.builder("Dialog.Crate.HologramLines.Body").dialogElement(420,
        "Enter up to " + SOFT_YELLOW.wrap(LINES_AMOUNT + " hologram lines") + " to display above the crate block.",
        "",
        SOFT_YELLOW.wrap("→") + " Leave all fields blank to use the " + SOFT_YELLOW.wrap("template") + " text instead.",
        "",
        SOFT_YELLOW.wrap("→") + " Supports " + SOFT_YELLOW.wrap("MiniMessage") + " formatting tags.",
        "",
        SOFT_YELLOW.wrap("→") + " You can also use " + SOFT_YELLOW.wrap(Plugins.PLACEHOLDER_API) + " placeholders.",
        "",
        SOFT_YELLOW.wrap("→") + " Crate placeholders: click " +
            OPEN_URL.with(WIKI_PLACEHOLDERS).wrap(SOFT_GREEN.and(UNDERLINED).wrap("HERE")) + " to view."
    );

    private static final TextLocale INPUT_LINE = LangEntry.builder("Dialog.Crate.HologramLines.Input.Line").text("Line " + SOFT_YELLOW.wrap("#%s"));

    private static final Function<Integer, String> JSON_LINE = index -> "line_" + index;

    @Override
    @NotNull
    public WrappedDialog create(@NotNull Player player, @NotNull Crate crate) {
        List<WrappedDialogInput> inputs = new ArrayList<>();
        // Use getHologramText() so the template lines are pre-filled when no custom override exists yet.
        List<String> existing = crate.getHologramText();
        int size = Math.max(LINES_AMOUNT, existing.size());

        for (int index = 0; index < size; index++) {
            inputs.add(
                DialogInputs.text(JSON_LINE.apply(index), INPUT_LINE.text().formatted(String.valueOf(index + 1)))
                    .initial(existing.size() > index ? existing.get(index) : "")
                    .maxLength(256)
                    .width(350)
                    .build()
            );
        }

        return Dialogs.create(builder -> {
            builder.base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(inputs)
                .build()
            );

            builder.type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build());

            builder.handleResponse(DialogActions.OK, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                List<String> lines = new ArrayList<>();

                for (int index = 0; index < size; index++) {
                    nbtHolder.getText(JSON_LINE.apply(index)).filter(Predicate.not(String::isBlank)).ifPresent(lines::add);
                }

                crate.setCustomHologramLines(lines);
                crate.recreateHologram();
                crate.markDirty();
                viewer.callback();
            });
        });
    }
}
