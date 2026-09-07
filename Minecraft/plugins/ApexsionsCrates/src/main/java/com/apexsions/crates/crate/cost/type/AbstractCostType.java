package com.apexsions.crates.crate.cost.type;

import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.api.cost.CostType;

public abstract class AbstractCostType implements CostType {

    protected final String id;

    public AbstractCostType(@NotNull String id) {
        this.id = id;
    }

    @Override
    @NotNull
    public String getId() {
        return this.id;
    }
}
