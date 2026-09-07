package com.apexsions.crates.crate.cost;

import com.apexsions.crates.crate.impl.Crate;
import com.apexsions.crates.dialog.DialogKey;

public class CostDialogs {

    public static final DialogKey<Crate> CREATION       = new DialogKey<>("cost_creation");
    public static final DialogKey<Cost>  NAME           = new DialogKey<>("cost_name");
    public static final DialogKey<Cost>  ENTRY_CREATION = new DialogKey<>("cost_entry_creation");

}
