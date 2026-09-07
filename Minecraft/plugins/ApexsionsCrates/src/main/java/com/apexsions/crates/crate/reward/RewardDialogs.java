package com.apexsions.crates.crate.reward;

import com.apexsions.crates.api.crate.Reward;
import com.apexsions.crates.crate.impl.Crate;
import com.apexsions.crates.crate.reward.impl.CommandReward;
import com.apexsions.crates.dialog.DialogKey;
import com.apexsions.crates.dialog.reward.RewardCreationDialog;
import com.apexsions.crates.dialog.reward.RewardItemDialog;
import com.apexsions.crates.dialog.reward.RewardPreviewDialog;

public class RewardDialogs {

    public static final DialogKey<RewardCreationDialog.Data> CREATION    = new DialogKey<>("reward_creation");
    public static final DialogKey<Crate>                     SORTING     = new DialogKey<>("reward_sorting");
    public static final DialogKey<RewardPreviewDialog.Data>  PREVIEW     = new DialogKey<>("reward_preview");
    public static final DialogKey<RewardItemDialog.Data>     ITEM        = new DialogKey<>("reward_item");
    public static final DialogKey<CommandReward>             COMMANDS    = new DialogKey<>("reward_commands");
    public static final DialogKey<CommandReward>             NAME        = new DialogKey<>("reward_name");
    public static final DialogKey<CommandReward>             DESCRIPTION = new DialogKey<>("reward_description");
    public static final DialogKey<Reward>                    WEIGHT      = new DialogKey<>("reward_weight");
    public static final DialogKey<Reward>                    PERMISSIONS = new DialogKey<>("reward_permissions");
    public static final DialogKey<Reward>                    LIMITS      = new DialogKey<>("reward_limits");
    public static final DialogKey<Crate>                     CURRENCY    = new DialogKey<>("reward_currency");
}
