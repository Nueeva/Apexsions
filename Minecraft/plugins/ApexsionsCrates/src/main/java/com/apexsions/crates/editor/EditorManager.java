package com.apexsions.crates.editor;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.CratesPlugin;
import com.apexsions.crates.api.crate.Reward;
import com.apexsions.crates.crate.cost.Cost;
import com.apexsions.crates.crate.impl.Crate;
import com.apexsions.crates.crate.reward.impl.ItemReward;
import com.apexsions.crates.dialog.DialogRegistry;
import com.apexsions.crates.editor.crate.*;
import com.apexsions.crates.editor.key.KeyListMenu;
import com.apexsions.crates.editor.key.KeyOptionsMenu;
import com.apexsions.crates.key.CrateKey;
import su.nightexpress.nightcore.manager.AbstractManager;

public class EditorManager extends AbstractManager<CratesPlugin> {

    private final DialogRegistry dialogs;

    private EditorMenu editorMenu;

    private CrateListMenu       crateListMenu;
    private CrateOptionsMenu    crateOptionsMenu;
    private CostsListMenu costsListMenu;
    private CostOptionsMenu costOptionsMenu;
    private CrateMilestonesMenu crateMilestonesMenu;
    private RewardListMenu      rewardListMenu;
    private RewardOptionsMenu   rewardOptionsMenu;
    private RewardContentMenu rewardContentMenu;

    private KeyListMenu    keyListMenu;
    private KeyOptionsMenu keyOptionsMenu;

    public EditorManager(@NotNull CratesPlugin plugin, @NotNull DialogRegistry dialogs) {
        super(plugin);
        this.dialogs = dialogs;
    }

    @Override
    protected void onLoad() {
        this.editorMenu = new EditorMenu(this.plugin);

        this.crateListMenu = new CrateListMenu(this.plugin, this.dialogs);
        this.crateOptionsMenu = new CrateOptionsMenu(this.plugin, this.dialogs);
        this.costsListMenu = new CostsListMenu(this.plugin, this.dialogs);
        this.costOptionsMenu = new CostOptionsMenu(this.plugin, this.dialogs);
        this.crateMilestonesMenu = new CrateMilestonesMenu(this.plugin);
        this.rewardListMenu = new RewardListMenu(this.plugin, this.dialogs);
        this.rewardOptionsMenu = new RewardOptionsMenu(this.plugin, this.dialogs);
        this.rewardContentMenu = new RewardContentMenu(this.plugin, this.dialogs);

        this.keyListMenu = new KeyListMenu(this.plugin, this.dialogs);
        this.keyOptionsMenu = new KeyOptionsMenu(this.plugin, this.dialogs);
    }

    @Override
    protected void onShutdown() {
        if (this.crateListMenu != null) this.crateListMenu.clear();
        if (this.crateOptionsMenu != null) this.crateOptionsMenu.clear();
        if (this.costsListMenu != null) this.costsListMenu.clear();
        if (this.costOptionsMenu != null) this.costOptionsMenu.clear();
        if (this.crateMilestonesMenu != null) this.crateMilestonesMenu.clear();
        if (this.rewardListMenu != null) this.rewardListMenu.clear();
        if (this.rewardOptionsMenu != null) this.rewardOptionsMenu.clear();
        if (this.rewardContentMenu != null) this.rewardContentMenu.clear();

        if (this.keyListMenu != null) this.keyListMenu.clear();
        if (this.keyOptionsMenu != null) this.keyOptionsMenu.clear();

        if (this.editorMenu != null) this.editorMenu.clear();
    }

    public void openEditor(@NotNull Player player) {
        this.editorMenu.open(player);
    }



    public void openCrateList(@NotNull Player player) {
        this.crateListMenu.open(player, this.plugin.getCrateManager());
    }

    public void openOptionsMenu(@NotNull Player player, @NotNull Crate crate) {
        this.crateOptionsMenu.open(player, crate);
    }

    public void openCosts(@NotNull Player player, @NotNull Crate crate) {
        this.costsListMenu.open(player, crate);
    }

    public void openCostOptions(@NotNull Player player, @NotNull Crate crate, @NotNull Cost cost) {
        this.costOptionsMenu.open(player, crate, cost);
    }

    public void openMilestones(@NotNull Player player, @NotNull Crate crate) {
        this.crateMilestonesMenu.open(player, crate);
    }

    public void openRewardList(@NotNull Player player, @NotNull Crate crate) {
        this.rewardListMenu.open(player, crate);
    }

    public void openRewardContent(@NotNull Player player, @NotNull ItemReward reward) {
        this.rewardContentMenu.open(player, reward);
    }

    public void openRewardOptions(@NotNull Player player, @NotNull Reward reward) {
        this.rewardOptionsMenu.open(player, reward);
    }



    public void openKeyList(@NotNull Player player) {
        this.keyListMenu.open(player, this.plugin.getKeyManager());
    }

    public void openKeyOptions(@NotNull Player player, @NotNull CrateKey key) {
        this.keyOptionsMenu.open(player, key);
    }
}
