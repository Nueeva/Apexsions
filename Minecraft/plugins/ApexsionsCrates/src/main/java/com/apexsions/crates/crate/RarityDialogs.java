package com.apexsions.crates.crate;

import com.apexsions.crates.crate.impl.Rarity;
import com.apexsions.crates.dialog.DialogKey;

public class RarityDialogs {

    public static final DialogKey<CrateManager> RARITY_CREATION = new DialogKey<>("rarity_creation");
    public static final DialogKey<Rarity>       RARITY_EDIT     = new DialogKey<>("rarity_edit");
}
