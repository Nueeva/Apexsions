package com.apexsions.crates.crate.cost.entry;

import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.api.cost.CostEntry;
import com.apexsions.crates.api.cost.CostType;
import com.apexsions.crates.registry.CratesRegistries;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;

public abstract class AbstractCostEntry<T extends CostType> implements CostEntry, Writeable {

    protected final T type;

    public AbstractCostEntry(@NotNull T type) {
        this.type = type;
    }

    @NotNull
    public static CostEntry read(@NotNull FileConfig config, @NotNull String path) throws IllegalStateException {
        String typeId = ConfigValue.create(path + ".Type", "null").read(config);
        CostType type = CratesRegistries.COST_TYPE.byKey(typeId);
        if (type == null) throw new IllegalStateException("Unkwnown cost type: " + typeId);

        return type.load(config, path);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Type", this.type.getId());
        this.writeAdditional(config, path);
    }

    protected abstract void writeAdditional(@NotNull FileConfig config, @NotNull String path);

    @Override
    @NotNull
    public T getType() {
        return this.type;
    }
}
