package com.apex.battlepass.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

public class ItemSerializer {

    public static String toBase64(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeObject(item);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ItemStack fromBase64(String data) {
        if (data == null || data.trim().isEmpty()) {
            return null;
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            return (ItemStack) dataInput.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getItemDisplayName(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return "Air";
        }
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                return meta.getDisplayName();
            }
        }
        return formatMaterialName(item.getType());
    }

    public static String formatMaterialName(Material material) {
        if (material == null) return "Unknown";
        String name = material.name().toLowerCase().replace('_', ' ');
        return Arrays.stream(name.split(" "))
                .map(word -> word.isEmpty() ? "" : Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
