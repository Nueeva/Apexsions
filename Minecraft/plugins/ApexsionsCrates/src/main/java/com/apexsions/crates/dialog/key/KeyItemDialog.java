package com.apexsions.crates.dialog.key;

import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.dialog.generic.GenericItemDialog;
import com.apexsions.crates.key.CrateKey;
import su.nightexpress.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.TextLocale;

import java.util.List;

public class KeyItemDialog extends GenericItemDialog<CrateKey> {

    private static final TextLocale TITLE = LangEntry.builder("Dialog.Key.Item.Title").text(title("Key", "Item"));

    @Override
    @NotNull
    protected TextLocale title() {
        return TITLE;
    }

    @Override
    protected void setName(@NotNull CrateKey source, @NotNull String name) {
        source.setName(name);
    }

    @Override
    protected void setDescription(@NotNull CrateKey source, @NotNull List<String> description) {
        // TODO
    }

    @Override
    protected void setItem(@NotNull CrateKey source, @NotNull AdaptedItem item) {
        source.setItem(item);
        source.markDirty();
    }
}
