package com.apexsions.crates.crate.reward;

import org.jetbrains.annotations.NotNull;

public record RewardKey(@NotNull String holder, @NotNull String crateId, @NotNull String rewardId) {

}
