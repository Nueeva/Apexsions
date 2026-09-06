package com.apexsions.crates.api;

import com.apexsions.crates.crate.Crate;
import com.apexsions.crates.key.CrateKey;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Public API specification for ApexsionsCrates.
 */
public interface ApexsionsCratesAPI {

    Collection<Crate> getCrates();

    Crate getCrate(String id);

    Collection<CrateKey> getKeys();

    CrateKey getKey(String id);

    CompletableFuture<Integer> getVirtualKeys(UUID uuid, String keyId);

    CompletableFuture<Void> setVirtualKeys(UUID uuid, String keyId, int amount);

    CompletableFuture<Void> addVirtualKeys(UUID uuid, String keyId, int amount);

    CompletableFuture<Boolean> takeVirtualKeys(UUID uuid, String keyId, int amount);

    CompletableFuture<Integer> getCrateOpenCount(UUID uuid, String crateId);

    boolean openCrate(Player player, Crate crate, boolean useVirtualKey);

    void previewCrate(Player player, Crate crate);

    void setCrateLocation(Location location, String crateId);

    void removeCrateLocation(Location location);

    boolean isCrateLocation(Location location);

    Crate getCrateAt(Location location);
}
