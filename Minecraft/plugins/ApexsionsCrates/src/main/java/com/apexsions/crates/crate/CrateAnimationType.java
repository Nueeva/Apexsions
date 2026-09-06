package com.apexsions.crates.crate;

public enum CrateAnimationType {
    ROULETTE,
    INSTANT,
    IN_WORLD,
    SELECTABLE;

    public static CrateAnimationType fromString(String str) {
        if (str == null) return ROULETTE;
        try {
            return valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ROULETTE;
        }
    }
}
