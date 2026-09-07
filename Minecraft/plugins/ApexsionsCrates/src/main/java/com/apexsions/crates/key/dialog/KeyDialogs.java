package com.apexsions.crates.key.dialog;

import com.apexsions.crates.dialog.DialogKey;
import com.apexsions.crates.dialog.generic.GenericItemDialog;
import com.apexsions.crates.key.CrateKey;
import com.apexsions.crates.key.KeyManager;

public class KeyDialogs {

    public static final DialogKey<KeyManager> CREATION = new DialogKey<>("key_creation");
    public static final DialogKey<CrateKey>   NAME     = new DialogKey<>("key_name");
    public static final DialogKey<GenericItemDialog.Data<CrateKey>> ITEM = new DialogKey<>("key_item");
}
