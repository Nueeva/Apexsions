package com.apexsions.crates.opening.inventory.spinner.provider;

import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.CratesPlugin;
import com.apexsions.crates.Placeholders;
import com.apexsions.crates.crate.impl.Rarity;
import com.apexsions.crates.opening.inventory.InventoryOpening;
import com.apexsions.crates.opening.inventory.spinner.SpinnerProvider;
import com.apexsions.crates.opening.inventory.spinner.SpinnerData;
import com.apexsions.crates.opening.inventory.spinner.impl.RewardSpinner;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.Lists;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class RewardProvider implements SpinnerProvider, Writeable {

    private final Set<String> rarities;

    public RewardProvider(@NotNull Set<String> rarities) {
        this.rarities = new HashSet<>(rarities);
    }

    @NotNull
    public static RewardProvider everything() {
        return new RewardProvider(Lists.newSet(Placeholders.WILDCARD));
    }

    @NotNull
    public static RewardProvider read(@NotNull FileConfig config, @NotNull String path) {
        Set<String> rarities = ConfigValue.create(path + ".Rarities", Set.of(Placeholders.WILDCARD)).read(config);

        return new RewardProvider(rarities);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Rarities", this.rarities);
    }

    @Override
    @NotNull
    public RewardSpinner createSpinner(@NotNull CratesPlugin plugin, @NotNull SpinnerData data, @NotNull InventoryOpening opening) {
        Set<Rarity> rarities = new HashSet<>();
        if (this.rarities.contains(Placeholders.WILDCARD)) {
            rarities.addAll(plugin.getCrateManager().getRarities());
        }
        else {
            rarities.addAll(this.rarities.stream().map(rId -> plugin.getCrateManager().getRarity(rId)).filter(Objects::nonNull).toList());
        }

        return new RewardSpinner(data, opening, rarities);
    }

    @NotNull
    public Set<String> getRarities() {
        return this.rarities;
    }
}
