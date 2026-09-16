package com.apexsions.fishing.gui.admin;

import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.gui.dialog.FishingInputGUI;
import com.apexsions.fishing.model.FishingRodData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Pure Admin Fishing Rod Creator GUI.
 * Powered by Native Dialog GUI (NightCore / Paper / Bedrock / Chat Fallback).
 * Completely eliminates left/right click increment adjustments.
 */
public class AdminRodCreatorGUI implements InventoryHolder {

    private final ApexsionsFishing plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    // In-memory working rod state
    private String rodId = "custom_rod";
    private String displayName = "<gradient:#00c6ff:#0072ff><bold>PANCINGAN KUSTOM ADMIN</bold></gradient>";
    private boolean autoCatch = true;
    private double luckBonus = 0.20; // +20%
    private double weightBonus = 0.15; // +15%
    private int catchSpeedSeconds = 15;
    private int minLevel = 10;
    private double priceRupiah = 250000;
    private double priceDiamond = 100;
    private boolean unbreakable = false;
    private int customModelData = 0;
    private final Map<Enchantment, Integer> enchants = new LinkedHashMap<>();

    // Control Slots
    public static final int SLOT_PREVIEW = 13;
    public static final int SLOT_AUTO_CATCH = 10;
    public static final int SLOT_LUCK = 11;
    public static final int SLOT_WEIGHT = 12;
    public static final int SLOT_SPEED = 14;
    public static final int SLOT_MIN_LEVEL = 15;
    public static final int SLOT_UNBREAKABLE = 16;
    public static final int SLOT_CMD = 28;
    public static final int SLOT_ENCHANTS = 29;
    public static final int SLOT_NAME_AND_ID = 30;
    public static final int SLOT_PRICE = 32;
    public static final int SLOT_SAVE = 33;
    public static final int SLOT_GIVE = 40;
    public static final int SLOT_BACK = 49;

    public AdminRodCreatorGUI(@NotNull ApexsionsFishing plugin, @NotNull Player player) {
        this(plugin, player, null);
    }

    public AdminRodCreatorGUI(@NotNull ApexsionsFishing plugin, @NotNull Player player, @Nullable FishingRodData existing) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<dark_red><bold>Admin Rod Creator (Dialog GUI)</bold></dark_red>"));

        if (existing != null) {
            this.rodId = existing.getId();
            this.displayName = existing.getDisplayName();
            this.autoCatch = existing.isAutoCatch();
            this.luckBonus = existing.getLuckBonus();
            this.weightBonus = existing.getWeightBonus();
            this.catchSpeedSeconds = existing.getCatchSpeedSeconds();
            this.minLevel = existing.getMinLevel();
            this.priceRupiah = existing.getPriceRupiah();
            this.priceDiamond = existing.getPriceDiamond();
            this.unbreakable = existing.isUnbreakable();
            this.customModelData = existing.getCustomModelData();
        }

        render();
    }

    public void open() {
        render();
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void render() {
        inventory.clear();

        // Dark red borders
        ItemStack borderGlass = createGuiItem(Material.RED_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderGlass);
            }
        }

        // 1. Center Preview Slot 13
        inventory.setItem(SLOT_PREVIEW, buildPreviewRod());

        // 2. Auto-Catch Toggle Slot 10
        inventory.setItem(SLOT_AUTO_CATCH, createGuiItem(Material.REPEATER,
                "<yellow><bold>Mode Auto-Catch</bold></yellow>",
                List.of(
                        "<gray>Status: " + (autoCatch ? "<green><bold>AKTIF (ON)</bold></green>" : "<red><bold>NON-AKTIF (OFF)</bold></red>") + "</gray>",
                        "",
                        "<yellow>▶ Klik untuk beralih mode auto-catch</yellow>"
                )));

        // 3. Luck Bonus Slot 11
        inventory.setItem(SLOT_LUCK, createGuiItem(Material.SUNFLOWER,
                "<gold><bold>Peluang Keberuntungan (Luck %)</bold></gold>",
                List.of(
                        "<gray>Nilai Saat Ini: <yellow><bold>+" + (int) Math.round(luckBonus * 100) + "%</bold></yellow></gray>",
                        "",
                        "<yellow>▶ Klik untuk buka Dialog Input Angka (0% - 500%)</yellow>"
                )));

        // 4. Weight Bonus Slot 12
        inventory.setItem(SLOT_WEIGHT, createGuiItem(Material.ANVIL,
                "<aqua><bold>Pengali Bobot Tangkapan (Weight %)</bold></aqua>",
                List.of(
                        "<gray>Nilai Saat Ini: <gold><bold>+" + (int) Math.round(weightBonus * 100) + "%</bold></gold></gray>",
                        "",
                        "<yellow>▶ Klik untuk buka Dialog Input Angka (0% - 500%)</yellow>"
                )));

        // 5. Catch Speed Slot 14
        inventory.setItem(SLOT_SPEED, createGuiItem(Material.CLOCK,
                "<light_purple><bold>Kecepatan Strike (Detik)</bold></light_purple>",
                List.of(
                        "<gray>Waktu Sambaran: <aqua><bold>" + catchSpeedSeconds + " Detik</bold></aqua></gray>",
                        "",
                        "<yellow>▶ Klik untuk buka Dialog Input Durasi (3 - 120 Detik)</yellow>"
                )));

        // 6. Syarat Minimal Level Slot 15
        inventory.setItem(SLOT_MIN_LEVEL, createGuiItem(Material.EXPERIENCE_BOTTLE,
                "<gradient:#f39c12:#e67e22><bold>Syarat Minimal Level Pemain</bold></gradient>",
                List.of(
                        "<gray>Syarat Level: <gold><bold>Level " + (minLevel > 0 ? minLevel + "+" : "Bebas (0)") + "</bold></gold></gray>",
                        "",
                        "<yellow>▶ Klik untuk buka Dialog Input Level (0 = Bebas)</yellow>"
                )));

        // 7. Unbreakable Toggle Slot 16
        inventory.setItem(SLOT_UNBREAKABLE, createGuiItem(Material.DIAMOND,
                "<aqua><bold>Ketahanan Tak Terbatas (Unbreakable)</bold></aqua>",
                List.of(
                        "<gray>Status: " + (unbreakable ? "<green><bold>UNBREAKABLE</bold></green>" : "<red><bold>DAPAT RUSAK</bold></red>") + "</gray>",
                        "",
                        "<yellow>▶ Klik untuk beralih status Unbreakable</yellow>"
                )));

        // 8. Custom Model Data Slot 28
        inventory.setItem(SLOT_CMD, createGuiItem(Material.PAINTING,
                "<white><bold>Custom Model Data</bold></white>",
                List.of(
                        "<gray>Model Data ID: <yellow><bold>" + customModelData + "</bold></yellow></gray>",
                        "",
                        "<yellow>▶ Klik untuk buka Dialog Input Custom Model Data</yellow>"
                )));

        // 9. Enchants Slot 29
        List<String> enchLore = new ArrayList<>();
        enchLore.add("<gray>Enchantment Terpasang:</gray>");
        if (enchants.isEmpty()) {
            enchLore.add("<dark_gray>- Tidak ada enchant tambahan</dark_gray>");
        } else {
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                enchLore.add("<aqua>  ● " + entry.getKey().getKey().getKey() + " " + entry.getValue() + "</aqua>");
            }
        }
        enchLore.add("");
        enchLore.add("<yellow>▶ Klik untuk siklus enchant (Lure / Luck / Unbreaking / Mending)</yellow>");
        inventory.setItem(SLOT_ENCHANTS, createGuiItem(Material.ENCHANTED_BOOK, "<light_purple><bold>Atur Enchantment Vanilla</bold></light_purple>", enchLore));

        // 10. Name & ID Slot 30
        inventory.setItem(SLOT_NAME_AND_ID, createGuiItem(Material.NAME_TAG,
                "<green><bold>Ubah Nama & ID Pancingan</bold></green>",
                List.of(
                        "<gray>ID: <yellow>" + rodId + "</yellow></gray>",
                        "<gray>Nama: </gray>" + displayName,
                        "",
                        "<yellow>● Klik Kiri:</yellow> <white>Buka Dialog Edit Nama Tampilan</white>",
                        "<yellow>● Klik Kanan:</yellow> <white>Buka Dialog Edit Rod ID</white>",
                        "<yellow>● Shift + Klik:</yellow> <white>Ganti Template Preset Cepat</white>"
                )));

        // 11. Price Setting Slot 32
        inventory.setItem(SLOT_PRICE, createGuiItem(Material.GOLD_INGOT,
                "<gold><bold>Pengaturan Harga Beli Toko</bold></gold>",
                List.of(
                        "<gray>Harga Rupiah: <yellow><bold>Rp " + String.format("%,d", (long) priceRupiah) + "</bold></yellow></gray>",
                        "<gray>Harga Diamond: <aqua><bold>" + String.format("%,d", (long) priceDiamond) + " Diamond</bold></aqua></gray>",
                        "",
                        "<yellow>● Klik Kiri:</yellow> <white>Buka Dialog Atur Harga Rupiah</white>",
                        "<yellow>● Klik Kanan:</yellow> <white>Buka Dialog Atur Harga Diamond</white>"
                )));

        // 12. Save to Catalog Slot 33
        inventory.setItem(SLOT_SAVE, createGuiItem(Material.WRITABLE_BOOK,
                "<gradient:#00ff87:#60efff><bold>💾 Simpan ke Katalog (rods.yml)</bold></gradient>",
                List.of(
                        "<gray>Simpan konfigurasi pancingan ini</gray>",
                        "<gray>ke berkas resmi rods.yml agar tersedia di toko.</gray>",
                        "",
                        "<yellow>▶ Klik untuk menyimpan data pancingan</yellow>"
                )));

        // 13. Give Rod Slot 40
        inventory.setItem(SLOT_GIVE, createGuiItem(Material.DISPENSER,
                "<gradient:#ffaa00:#ffd700><bold>🎁 Ambil Pancingan Ini</bold></gradient>",
                List.of(
                        "<gray>Berikan 1x pancingan hasil rakitan</gray>",
                        "<gray>ini langsung ke inventory kamu.</gray>",
                        "",
                        "<yellow>▶ Klik untuk mengambil pancingan</yellow>"
                )));

        // 14. Back Button Slot 49
        inventory.setItem(SLOT_BACK, createGuiItem(Material.ARROW,
                "<yellow><bold>◀ Kembali ke Menu Admin</bold></yellow>",
                List.of("<gray>Tutup editor dan kembali ke hub admin.</gray>")));
    }

    public ItemStack buildPreviewRod() {
        FishingRodData data = new FishingRodData(
                rodId, displayName, autoCatch, luckBonus, weightBonus,
                catchSpeedSeconds, minLevel, priceRupiah, priceDiamond, unbreakable, customModelData,
                List.of("<dark_gray>Pancingan kustom resmi peradaban Apexsions.</dark_gray>")
        );

        ItemStack item = plugin.getRodManager().createRod(data);
        if (!enchants.isEmpty()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                for (Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
                    meta.addEnchant(e.getKey(), e.getValue(), true);
                }
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        ClickType click = event.getClick();

        switch (slot) {
            case SLOT_AUTO_CATCH -> {
                autoCatch = !autoCatch;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, autoCatch ? 1.5f : 0.8f);
                render();
            }
            case SLOT_LUCK -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                FishingInputGUI.openNumeric(
                        plugin,
                        player,
                        "Pengali Keberuntungan (Luck)",
                        "Masukkan bonus persentase keberuntungan pancingan:",
                        (int) Math.round(luckBonus * 100),
                        0,
                        500,
                        val -> {
                            luckBonus = val / 100.0;
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.4f);
                            player.sendMessage(mm.deserialize("<green>✓ Luck bonus berhasil diatur ke <yellow>+" + val + "%</yellow>!</green>"));
                            open();
                        },
                        this::open
                );
            }
            case SLOT_WEIGHT -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                FishingInputGUI.openNumeric(
                        plugin,
                        player,
                        "Pengali Bobot Ikan (Weight)",
                        "Masukkan bonus persentase bobot ikan yang ditangkap:",
                        (int) Math.round(weightBonus * 100),
                        0,
                        500,
                        val -> {
                            weightBonus = val / 100.0;
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.4f);
                            player.sendMessage(mm.deserialize("<green>✓ Weight bonus berhasil diatur ke <gold>+" + val + "%</gold>!</green>"));
                            open();
                        },
                        this::open
                );
            }
            case SLOT_SPEED -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                FishingInputGUI.openNumeric(
                        plugin,
                        player,
                        "Kecepatan Sambaran Kail (Strike Speed)",
                        "Masukkan durasi sambaran dalam satuan detik (3 s/d 120):",
                        catchSpeedSeconds,
                        3,
                        120,
                        val -> {
                            catchSpeedSeconds = val;
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.4f);
                            player.sendMessage(mm.deserialize("<green>✓ Kecepatan sambaran berhasil diatur ke <aqua>" + val + " Detik</aqua>!</green>"));
                            open();
                        },
                        this::open
                );
            }
            case SLOT_MIN_LEVEL -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                FishingInputGUI.openNumeric(
                        plugin,
                        player,
                        "Syarat Minimal Level Pemain",
                        "Masukkan batas minimal level untuk memakai pancingan ini (0 = Bebas):",
                        minLevel,
                        0,
                        100,
                        val -> {
                            minLevel = val;
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.4f);
                            player.sendMessage(mm.deserialize("<green>✓ Syarat level berhasil diatur ke <yellow>" + (val > 0 ? "Level " + val : "Bebas Digunakan") + "</yellow>!</green>"));
                            open();
                        },
                        this::open
                );
            }
            case SLOT_UNBREAKABLE -> {
                unbreakable = !unbreakable;
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1.0f, 1.2f);
                render();
            }
            case SLOT_CMD -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                FishingInputGUI.openNumeric(
                        plugin,
                        player,
                        "Custom Model Data ID",
                        "Masukkan angka Custom Model Data untuk custom resource pack (0 = Default):",
                        customModelData,
                        0,
                        999999,
                        val -> {
                            customModelData = val;
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                            player.sendMessage(mm.deserialize("<green>✓ Custom Model Data berhasil diatur ke <yellow>" + val + "</yellow>!</green>"));
                            open();
                        },
                        this::open
                );
            }
            case SLOT_ENCHANTS -> {
                cycleEnchants();
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
                render();
            }
            case SLOT_NAME_AND_ID -> {
                if (click.isShiftClick()) {
                    cyclePresetName();
                    player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.2f);
                    render();
                } else if (click.isRightClick()) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    FishingInputGUI.open(
                            plugin,
                            player,
                            "Ubah ID Pancingan",
                            "Masukkan ID unik pancingan (hanya huruf, angka, underscore):",
                            rodId,
                            newId -> {
                                String cleanId = newId.trim().toLowerCase().replaceAll("[^a-z0-9_]", "_");
                                if (cleanId.isBlank()) cleanId = "custom_rod";
                                rodId = cleanId;
                                player.sendMessage(mm.deserialize("<green>✓ Rod ID berhasil diubah menjadi: <yellow>" + rodId + "</yellow></green>"));
                                open();
                            },
                            this::open
                    );
                } else {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    FishingInputGUI.open(
                            plugin,
                            player,
                            "Ubah Nama Tampilan Pancingan",
                            "Masukkan nama baru (dapat menggunakan MiniMessage / & kode warna):",
                            displayName,
                            newName -> {
                                if (!newName.isBlank()) {
                                    displayName = newName;
                                    player.sendMessage(mm.deserialize("<green>✓ Nama tampilan pancingan berhasil diubah!</green>"));
                                }
                                open();
                            },
                            this::open
                    );
                }
            }
            case SLOT_PRICE -> {
                if (click.isRightClick()) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    FishingInputGUI.openNumeric(
                            plugin,
                            player,
                            "Atur Harga Diamond Toko",
                            "Masukkan harga beli pancingan dalam jumlah Diamond:",
                            (int) priceDiamond,
                            0,
                            100000,
                            val -> {
                                priceDiamond = val;
                                player.sendMessage(mm.deserialize("<green>✓ Harga Diamond diatur ke <aqua>" + val + " Diamond</aqua>!</green>"));
                                open();
                            },
                            this::open
                    );
                } else {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    FishingInputGUI.openNumeric(
                            plugin,
                            player,
                            "Atur Harga Rupiah Toko",
                            "Masukkan harga beli pancingan dalam saldo Rupiah:",
                            (int) priceRupiah,
                            0,
                            100000000,
                            val -> {
                                priceRupiah = val;
                                player.sendMessage(mm.deserialize("<green>✓ Harga Rupiah diatur ke <yellow>Rp " + String.format("%,d", val) + "</yellow>!</green>"));
                                open();
                            },
                            this::open
                    );
                }
            }
            case SLOT_SAVE -> {
                saveToRodsConfig();
            }
            case SLOT_GIVE -> {
                ItemStack rod = buildPreviewRod();
                player.getInventory().addItem(rod);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.5f);
                player.sendMessage(mm.deserialize("<green>Pancingan kustom berhasil diberikan ke inventory Anda!</green>"));
            }
            case SLOT_BACK -> {
                new FishingAdminHubGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
        }
    }

    private void cycleEnchants() {
        if (enchants.isEmpty()) {
            enchants.put(Enchantment.LUCK_OF_THE_SEA, 3);
        } else if (enchants.containsKey(Enchantment.LUCK_OF_THE_SEA) && !enchants.containsKey(Enchantment.LURE)) {
            enchants.put(Enchantment.LURE, 3);
        } else if (enchants.containsKey(Enchantment.LURE) && !enchants.containsKey(Enchantment.UNBREAKING)) {
            enchants.put(Enchantment.UNBREAKING, 3);
            enchants.put(Enchantment.MENDING, 1);
        } else {
            enchants.clear();
        }
    }

    private void cyclePresetName() {
        String[] ids = {"custom_rod", "poseidon_wrath", "leviathan_tamer", "abyssal_caller", "ocean_sovereign"};
        String[] names = {
                "<gradient:#00c6ff:#0072ff><bold>PANCINGAN KUSTOM ADMIN</bold></gradient>",
                "<gradient:#2980b9:#6dd5fa><bold>⚡ TRISULA POSEIDON (ROD) ⚡</bold></gradient>",
                "<gradient:#8e2de2:#4a00e0><bold>🔱 PENAKLUK LEVIATHAN 🔱</bold></gradient>",
                "<gradient:#11998e:#38ef7d><bold>🌊 PEMANGGIL ABYSSAL 🌊</bold></gradient>",
                "<gradient:#f12711:#f5af19><bold>👑 PANCINGAN SANG RATU LAUT 👑</bold></gradient>"
        };

        int curIdx = 0;
        for (int i = 0; i < ids.length; i++) {
            if (ids[i].equalsIgnoreCase(rodId)) {
                curIdx = (i + 1) % ids.length;
                break;
            }
        }
        rodId = ids[curIdx];
        displayName = names[curIdx];
    }

    private void saveToRodsConfig() {
        FishingRodData data = new FishingRodData(
                rodId, displayName, autoCatch, luckBonus, weightBonus,
                catchSpeedSeconds, minLevel, priceRupiah, priceDiamond, unbreakable, customModelData,
                List.of("<dark_gray>Pancingan resmi peradaban Apexsions.</dark_gray>")
        );

        plugin.getRodManager().saveRodData(data);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        player.sendMessage(mm.deserialize("<green><bold>BERHASIL!</bold> Pancingan <yellow>" + rodId + "</yellow> telah disimpan ke dalam <gold>rods.yml</gold>!</green>"));
    }

    private ItemStack createGuiItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            List<Component> compLore = new ArrayList<>();
            for (String l : lore) {
                compLore.add(mm.deserialize(l));
            }
            meta.lore(compLore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
