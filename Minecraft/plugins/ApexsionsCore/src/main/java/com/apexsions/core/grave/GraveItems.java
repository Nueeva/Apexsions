package com.apexsions.core.grave;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serializes/deserializes grave item stacks to a Base64 string using the
 * built-in Bukkit object stream. No external dependency required.
 */
final class GraveItems {

    private GraveItems() {
    }

    private static final Logger LOGGER = Logger.getLogger("ApexsionsCore");

    static String serialize(List<ItemStack> items) {
        ItemStack[] arr = items.toArray(new ItemStack[0]);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             BukkitObjectOutputStream out = new BukkitObjectOutputStream(bos)) {
            out.writeInt(arr.length);
            for (ItemStack item : arr) {
                out.writeObject(item);
            }
            out.flush();
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (IOException e) {
            // Returning "" here would silently discard the player's items; surface it loudly.
            LOGGER.log(Level.SEVERE, "Failed serializing grave items (" + arr.length + " stacks); items NOT persisted", e);
            throw new IllegalStateException("Grave item serialization failed", e);
        }
    }

    static List<ItemStack> deserialize(String data) throws IOException, ClassNotFoundException {
        List<ItemStack> out = new ArrayList<>();
        if (data == null || data.isBlank()) {
            return out;
        }
        byte[] raw = Base64.getDecoder().decode(data);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(raw);
             BukkitObjectInputStream in = new BukkitObjectInputStream(bis)) {
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                Object obj = in.readObject();
                if (obj instanceof ItemStack item && !item.getType().isAir()) {
                    out.add(item);
                }
            }
        }
        return out;
    }
}
