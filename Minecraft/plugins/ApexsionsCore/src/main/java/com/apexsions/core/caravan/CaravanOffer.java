package com.apexsions.core.caravan;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * One purchasable entry in the Wandering Black Market Caravan.
 *
 * Either an item offer (material + amount) or a console command offer
 * (used for exclusive titles / cosmetics). When a command is set, the item is
 * shown only as an icon and no item is granted.
 */
public record CaravanOffer(
        @NotNull String id,
        @NotNull Material icon,
        @Nullable String displayName,
        @NotNull String priceCurrency,
        double price,
        @Nullable Material material,
        int amount,
        @Nullable String command
) {

    public boolean isCommandOffer() {
        return command != null && !command.isBlank();
    }
}
