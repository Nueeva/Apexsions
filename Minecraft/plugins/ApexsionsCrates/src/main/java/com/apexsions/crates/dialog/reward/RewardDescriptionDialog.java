package com.apexsions.crates.dialog.reward;

import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.crate.reward.impl.CommandReward;
import com.apexsions.crates.dialog.generic.GenericDescriptionDialog;
import su.nightexpress.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.TextLocale;

import java.util.List;

public class RewardDescriptionDialog extends GenericDescriptionDialog<CommandReward> {

    private static final TextLocale TITLE = LangEntry.builder("Dialog.Reward.Description.Title").text(title("Reward", "Description"));

    @Override
    @NotNull
    protected TextLocale title() {
        return TITLE;
    }

    @Override
    @NotNull
    protected AdaptedItem getItem(@NotNull CommandReward source) {
        return source.getPreview();
    }

    @Override
    protected void setItem(@NotNull CommandReward source, @NotNull AdaptedItem item) {
        source.setPreview(item);
    }

    @Override
    @NotNull
    protected List<String> getDescription(@NotNull CommandReward source) {
        return source.getDescription();
    }

    @Override
    protected void setDescription(@NotNull CommandReward source, @NotNull List<String> description) {
        source.setDescription(description);
    }

    @Override
    protected void save(@NotNull CommandReward source) {
        source.getCrate().markDirty();
    }
}
