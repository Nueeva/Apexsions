package com.apexsions.core.gui.admin;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 54-Slot Deep Player Inspector & Control Center.
 * Provides Full Access to balances, progression, kingdom allegiance, inventory viewing, titles, cosmetics, and moderation.
 */
public class PlayerInspectorGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player admin;
    private final Player target;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public PlayerInspectorGUI(ApexsionsCorePlugin plugin, Player admin, Player target) {
        this.plugin = plugin;
        this.admin = admin;
        this.target = target;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>👑 PLAYER INSPECTOR: " + target.getName() + "</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        if (!target.isOnline()) {
            admin.sendMessage(mm.deserialize("<red>Pemain target sudah tidak berada di server.</red>"));
            new PlayerManagerGUI(plugin, admin).open();
            return;
        }
        buildGUI();
        admin.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();

        ItemStack borderPane = createGlass(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        ItemStack decorPane = createGlass(Material.ORANGE_STAINED_GLASS_PANE, "<gold>✦</gold>");

        // Border
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderPane);
            }
        }
        inventory.setItem(1, decorPane);
        inventory.setItem(7, decorPane);
        inventory.setItem(46, decorPane);
        inventory.setItem(52, decorPane);

        // Header Slot 4: Player Full Info Skull
        inventory.setItem(4, createPlayerSummarySkull());

        // ════════════════ ROW 2: ECONOMIC MODIFIERS (Slots 10..16) ════════════════
        inventory.setItem(10, createActionItem(Material.RED_DYE, "<red><bold>- Rp 100.000</bold></red>",
                List.of("<gray>Kurangi saldo target sebesar <red>Rp 100.000</red>.</gray>", "<yellow>▶ Klik untuk kurangi</yellow>")));
        inventory.setItem(11, createActionItem(Material.ORANGE_DYE, "<red><bold>- Rp 10.000</bold></red>",
                List.of("<gray>Kurangi saldo target sebesar <red>Rp 10.000</red>.</gray>", "<yellow>▶ Klik untuk kurangi</yellow>")));
        inventory.setItem(12, createActionItem(Material.LIME_DYE, "<green><bold>+ Rp 10.000</bold></green>",
                List.of("<gray>Tambah saldo target sebesar <green>Rp 10.000</green>.</gray>", "<yellow>▶ Klik untuk tambah</yellow>")));
        inventory.setItem(13, createActionItem(Material.EMERALD, "<green><bold>+ Rp 100.000</bold></green>",
                List.of("<gray>Tambah saldo target sebesar <green>Rp 100.000</green>.</gray>", "<yellow>▶ Klik untuk tambah</yellow>")));
        inventory.setItem(14, createActionItem(Material.GOLD_BLOCK, "<gold><bold>+ Rp 1.000.000</bold></gold>",
                List.of("<gray>Tambah saldo target sebesar <gold>Rp 1.000.000</gold>.</gray>", "<yellow>▶ Klik untuk tambah</yellow>")));
        inventory.setItem(15, createActionItem(Material.RAW_GOLD, "<yellow><bold>💰 SET SALDO RUPIAH</bold></yellow>",
                List.of("<gray>Tentukan nominal saldo Rupiah target secara bebas.</gray>", "<yellow>▶ Klik untuk input nominal di chat</yellow>")));
        inventory.setItem(16, createActionItem(Material.DIAMOND, "<aqua><bold>💎 ATUR SALDO DIAMOND 💎</bold></aqua>",
                List.of("<gray>Berikan atau kurangi saldo Diamond 💎 pemain.</gray>", "<yellow>▶ Klik untuk input nominal di chat</yellow>")));

        // ════════════════ ROW 3: PROGRESSION, KINGDOM & MONARCH (Slots 19..26) ════════════════
        inventory.setItem(19, createActionItem(Material.REDSTONE, "<red><bold>-1 Level</bold></red>",
                List.of("<gray>Turunkan 1 Level Karakter pemain.</gray>", "<yellow>▶ Klik untuk kurangi level</yellow>")));
        inventory.setItem(20, createActionItem(Material.GLOWSTONE_DUST, "<green><bold>+1 Level</bold></green>",
                List.of("<gray>Naikkan 1 Level Karakter pemain secara instan.</gray>", "<yellow>▶ Klik untuk naikkan level</yellow>")));
        inventory.setItem(21, createActionItem(Material.EXPERIENCE_BOTTLE, "<aqua><bold>+ 1.000 XP</bold></aqua>",
                List.of("<gray>Beri 1.000 Progression XP.</gray>", "<yellow>▶ Klik untuk tambah XP</yellow>")));
        inventory.setItem(22, createActionItem(Material.NETHER_STAR, "<gold><bold>👑 SET LEVEL BEBAS</bold></gold>",
                List.of("<gray>Atur Level Karakter target (1 - 100).</gray>", "<yellow>▶ Klik untuk input angka level di chat</yellow>")));
        inventory.setItem(23, createActionItem(Material.GOLD_INGOT, "<gold><bold>⚜ PINDAH KE ZENITHAR</bold></gold>",
                List.of("<gray>Ubah afiliasi kerajaan menjadi <gradient:#ffe900:#f39c12><bold>Zenithar</bold></gradient>.</gray>", "<yellow>▶ Klik untuk tetapkan kerajaan</yellow>")));
        inventory.setItem(24, createActionItem(Material.BLAZE_POWDER, "<red><bold>⚜ PINDAH KE SOLTERRA</bold></red>",
                List.of("<gray>Ubah afiliasi kerajaan menjadi <gradient:#ff4d4d:#c0392b><bold>Solterra</bold></gradient>.</gray>", "<yellow>▶ Klik untuk tetapkan kerajaan</yellow>")));
        inventory.setItem(25, createActionItem(Material.LILY_PAD, "<green><bold>⚜ PINDAH KE SYLVAMOOR</bold></green>",
                List.of("<gray>Ubah afiliasi kerajaan menjadi <gradient:#87ceeb:#3498db><bold>Sylvamoor</bold></gradient>.</gray>", "<yellow>▶ Klik untuk tetapkan kerajaan</yellow>")));

        String pKingdom = plugin.getApi().getPlayerRegionKey(target.getUniqueId());
        String kingName = plugin.getConfigManager().getKingdomKing(pKingdom);
        boolean isMonarch = kingName != null && kingName.equalsIgnoreCase(target.getName());

        inventory.setItem(26, createActionItem(Material.DRAGON_HEAD,
                isMonarch ? "<gradient:#e74c3c:#c0392b><bold>👑 CABUT STATUS RAJA</bold></gradient>" : "<gradient:#f1c40f:#e67e22><bold>👑 ANGKAT MENJADI RAJA</bold></gradient>",
                List.of(
                        "<gray>Status Saat Ini: " + (isMonarch ? "<yellow><bold>Raja Kerajaan (" + pKingdom + ")</bold></yellow>" : "<white>Rakyat Biasa</white>") + "</gray>",
                        "<gray>Memberikan hak veto, takhta, dan gelar kehormatan.</gray>",
                        "<yellow>▶ Klik untuk toggle status Raja</yellow>"
                )));

        // ════════════════ ROW 4: MODERATION & UTILITIES (Slots 28..34) ════════════════
        inventory.setItem(28, createActionItem(Material.ENDER_PEARL, "<aqua><bold>🚀 TELEPORT KE PEMAIN</bold></aqua>",
                List.of("<gray>Teleportasikan dirimu ke posisi pemain ini.</gray>", "<yellow>▶ Klik untuk teleport</yellow>")));
        inventory.setItem(29, createActionItem(Material.CHORUS_FRUIT, "<light_purple><bold>🧲 TARIK PEMAIN KE SINI</bold></light_purple>",
                List.of("<gray>Teleportasikan pemain ini ke posisimu.</gray>", "<yellow>▶ Klik untuk menarik pemain</yellow>")));
        inventory.setItem(30, createActionItem(Material.CHEST, "<yellow><bold>🎒 LIHAT INVENTORI (LIVE)</bold></yellow>",
                List.of("<gray>Buka dan inspeksi inventori live pemain.</gray>", "<yellow>▶ Klik untuk membuka inventori</yellow>")));
        inventory.setItem(31, createActionItem(Material.ENDER_CHEST, "<dark_purple><bold>📦 BUKA ENDER CHEST</bold></dark_purple>",
                List.of("<gray>Buka dan inspeksi EnderChest pemain.</gray>", "<yellow>▶ Klik untuk membuka EnderChest</yellow>")));
        inventory.setItem(32, createActionItem(Material.GOLDEN_APPLE, "<green><bold>💖 HEAL & FEED</bold></green>",
                List.of("<gray>Pulihkan seluruh darah, lapar, dan hilangkan debuff.</gray>", "<yellow>▶ Klik untuk pulihkan</yellow>")));
        inventory.setItem(33, createActionItem(Material.COMPASS, "<light_purple><bold>🎮 GAMEMODE: " + target.getGameMode().name() + "</bold></light_purple>",
                List.of("<gray>Ubah GameMode pemain (Survival, Creative, Adventure, Spectator).</gray>", "<yellow>▶ Klik untuk ganti GameMode</yellow>")));
        inventory.setItem(34, createActionItem(Material.IRON_BOOTS, "<red><bold>👢 KICK PEMAIN</bold></red>",
                List.of("<gray>Keluarkan pemain dari server secara paksa.</gray>", "<yellow>▶ Klik untuk kick</yellow>")));

        // Bottom Navigation (Slots 40, 49)
        ItemStack backList = createActionItem(Material.ARROW, "<yellow><bold>◀ KEMBALI KE DAFTAR PEMAIN</bold></yellow>",
                List.of("<gray>Kembali ke daftar seluruh pemain online.</gray>"));
        inventory.setItem(40, backList);

        ItemStack backHub = createActionItem(Material.OAK_DOOR, "<red><bold>◀ KEMBALI KE MASTER ADMIN HUB</bold></red>",
                List.of("<gray>Kembali ke menu utama panel administrasi.</gray>"));
        inventory.setItem(49, backHub);
    }

    private ItemStack createPlayerSummarySkull() {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) skull.getItemMeta();
        if (sm != null) {
            sm.setOwningPlayer(target);
            sm.displayName(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>👑 PROFIL LENGKAP: " + target.getName() + "</bold></gradient>"));

            PlayerData data = plugin.getPlayerDataService().getCached(target.getUniqueId()).orElse(null);
            int level = data != null ? data.getLevel() : 1;
            long xp = data != null ? data.getXp() : 0;
            long reqXp = plugin.getLevelFormula().getRequiredXpForNextLevel(level);
            String title = plugin.getLevelManager().getLevelTitle(target.getUniqueId());
            String kName = "Belum Memilih Kerajaan";
            if (data != null && data.getRegionId() != null) {
                kName = plugin.getRegionManager().getRegion(data.getRegionId()).map(Region::getDisplayName).orElse("Belum Memilih Kerajaan");
            }

            String pKingdom = plugin.getApi().getPlayerRegionKey(target.getUniqueId());
            String kingName = plugin.getConfigManager().getKingdomKing(pKingdom);
            boolean isMonarch = kingName != null && kingName.equalsIgnoreCase(target.getName());

            double balance = getPlayerRupiah(target);
            double diaBal = getPlayerDiamond(target);

            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>UUID:</gray> <dark_gray>" + target.getUniqueId() + "</dark_gray>"));
            lore.add(mm.deserialize("<gray>Kerajaan:</gray> <gold><bold>" + kName + "</bold></gold>" + (isMonarch ? " <yellow><bold>[RAJA]</bold></yellow>" : "")));
            lore.add(mm.deserialize("<gray>Level Karakter:</gray> <yellow>Lv. " + level + "</yellow> <gray>(" + title + ")</gray>"));
            lore.add(mm.deserialize("<gray>Progress XP:</gray> <aqua>" + xp + " / " + (reqXp == Long.MAX_VALUE ? "MAX" : reqXp) + " XP</aqua>"));
            lore.add(mm.deserialize("<gray>Saldo Rupiah:</gray> <green><bold>Rp " + String.format("%,.0f", balance) + "</bold></green>"));
            lore.add(mm.deserialize("<gray>Saldo Diamond 💎:</gray> <aqua><bold>" + String.format("%,.0f", diaBal) + " 💎</bold></aqua>"));
            lore.add(mm.deserialize("<gray>Darah / Lapar:</gray> <red>" + (int) target.getHealth() + "/" + (int) target.getMaxHealth() + " HP</red> <gray>•</gray> <gold>" + target.getFoodLevel() + "/20</gold>"));
            lore.add(mm.deserialize("<gray>GameMode:</gray> <white>" + target.getGameMode().name() + "</white> <gray>• Ping:</gray> <green>" + target.getPing() + "ms</green>"));
            lore.add(mm.deserialize("<gray>Lokasi:</gray> <white>" + target.getWorld().getName() + " (" + target.getLocation().getBlockX() + ", " + target.getLocation().getBlockY() + ", " + target.getLocation().getBlockZ() + ")</white>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<green>● Status: Full Control Connected</green>"));
            sm.lore(lore);
            skull.setItemMeta(sm);
        }
        return skull;
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (!target.isOnline()) {
            admin.sendMessage(mm.deserialize("<red>Pemain target sudah keluar dari server.</red>"));
            admin.closeInventory();
            return;
        }

        // ════════════════ ECONOMIC ACTIONS ════════════════
        if (slot == 10) { // -100k
            modifyBalance(-100000);
            return;
        }
        if (slot == 11) { // -10k
            modifyBalance(-10000);
            return;
        }
        if (slot == 12) { // +10k
            modifyBalance(10000);
            return;
        }
        if (slot == 13) { // +100k
            modifyBalance(100000);
            return;
        }
        if (slot == 14) { // +1M
            modifyBalance(1000000);
            return;
        }
        if (slot == 15) { // Set Saldo Custom
            plugin.getAdminChatInputManager().startSession(admin,
                    "Ketik nominal Saldo Rupiah baru untuk " + target.getName() + " (contoh: 250000):",
                    input -> {
                        try {
                            double newBal = Double.parseDouble(input.replaceAll("[^0-9.-]", ""));
                            if (newBal < 0) newBal = 0;
                            double current = getPlayerRupiah(target);
                            double delta = newBal - current;
                            modifyBalance(delta);
                            admin.sendMessage(mm.deserialize("<green>✓ Saldo Rupiah " + target.getName() + " berhasil diubah menjadi <yellow>Rp " + String.format("%,.0f", newBal) + "</yellow>!</green>"));
                            admin.playSound(admin.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.4f);
                        } catch (NumberFormatException e) {
                            admin.sendMessage(mm.deserialize("<red>Nominal angka tidak valid!</red>"));
                        }
                        open();
                    },
                    this::open
            );
            return;
        }
        if (slot == 16) { // Set Diamond Custom
            plugin.getAdminChatInputManager().startSession(admin,
                    "Ketik jumlah Diamond 💎 yang ingin diberikan ke " + target.getName() + " (contoh: 50):",
                    input -> {
                        try {
                            double dia = Double.parseDouble(input.replaceAll("[^0-9.-]", ""));
                            if (dia > 0) {
                                boolean addedEco = false;
                                try {
                                    Class<?> providerClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyProvider");
                                    if ((boolean) providerClass.getMethod("isAvailable").invoke(null)) {
                                        Object api = providerClass.getMethod("get").invoke(null);
                                        api.getClass().getMethod("deposit", UUID.class, String.class, double.class).invoke(api, target.getUniqueId(), "diamond", dia);
                                        addedEco = true;
                                    }
                                } catch (Throwable ignored) {}
                                admin.sendMessage(mm.deserialize("<green>✓ Berhasil memberikan <aqua>" + String.format("%,.0f", dia) + " 💎</aqua> ke " + target.getName() + "!</green>"));
                                target.sendMessage(mm.deserialize("<green>✓ Kamu menerima <aqua>" + String.format("%,.0f", dia) + " 💎</aqua> dari Administrator!</green>"));
                            }
                            admin.playSound(admin.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.4f);
                        } catch (Exception e) {
                            admin.sendMessage(mm.deserialize("<red>Jumlah Diamond 💎 tidak valid!</red>"));
                        }
                        open();
                    },
                    this::open
            );
            return;
        }

        // ════════════════ PROGRESSION, KINGDOM & MONARCH ════════════════
        if (slot == 19) { // -1 Level
            int currentLvl = plugin.getLevelManager().getLevel(target.getUniqueId());
            plugin.getLevelManager().setLevel(target.getUniqueId(), Math.max(1, currentLvl - 1));
            admin.playSound(admin.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.2f);
            buildGUI();
            return;
        }
        if (slot == 20) { // +1 Level
            int currentLvl = plugin.getLevelManager().getLevel(target.getUniqueId());
            plugin.getLevelManager().setLevel(target.getUniqueId(), Math.min(100, currentLvl + 1));
            admin.playSound(admin.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.4f);
            buildGUI();
            return;
        }
        if (slot == 21) { // +1000 XP
            plugin.getLevelManager().addXp(target.getUniqueId(), 1000, XpSource.ADMIN);
            admin.playSound(admin.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.4f);
            buildGUI();
            return;
        }
        if (slot == 22) { // Set Level Custom
            plugin.getAdminChatInputManager().startSession(admin,
                    "Ketik Level baru untuk " + target.getName() + " (1 - 100):",
                    input -> {
                        try {
                            int lvl = Integer.parseInt(input.replaceAll("[^0-9]", ""));
                            plugin.getLevelManager().setLevel(target.getUniqueId(), lvl);
                            admin.sendMessage(mm.deserialize("<green>✓ Level " + target.getName() + " berhasil diubah ke <gold>Lv. " + lvl + "</gold>!</green>"));
                            admin.playSound(admin.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.4f);
                        } catch (Exception e) {
                            admin.sendMessage(mm.deserialize("<red>Angka level tidak valid!</red>"));
                        }
                        open();
                    },
                    this::open
            );
            return;
        }
        if (slot == 23) { // Zenithar
            changeKingdom("ZENITHAR");
            return;
        }
        if (slot == 24) { // Solterra
            changeKingdom("SOLTERRA");
            return;
        }
        if (slot == 25) { // Sylvamoor
            changeKingdom("SYLVAMOOR");
            return;
        }
        if (slot == 26) { // Toggle Monarch
            toggleMonarch();
            return;
        }

        // ════════════════ MODERATION & UTILITIES ════════════════
        if (slot == 28) { // TP to Player
            admin.teleport(target.getLocation());
            admin.sendMessage(mm.deserialize("<green>✓ Teleportasi ke <yellow>" + target.getName() + "</yellow> berhasil!</green>"));
            admin.playSound(admin.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
            admin.closeInventory();
            return;
        }
        if (slot == 29) { // Pull Player to Admin
            target.teleport(admin.getLocation());
            admin.sendMessage(mm.deserialize("<green>✓ <yellow>" + target.getName() + "</yellow> berhasil ditarik ke posisimu!</green>"));
            admin.playSound(admin.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
            admin.closeInventory();
            return;
        }
        if (slot == 30) { // View Inv Live
            admin.playSound(admin.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.0f);
            admin.openInventory(target.getInventory());
            return;
        }
        if (slot == 31) { // View EnderChest
            admin.playSound(admin.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.8f, 1.0f);
            admin.openInventory(target.getEnderChest());
            return;
        }
        if (slot == 32) { // Heal & Feed
            target.setHealth(target.getMaxHealth());
            target.setFoodLevel(20);
            target.setFireTicks(0);
            admin.sendMessage(mm.deserialize("<green>✓ <yellow>" + target.getName() + "</yellow> berhasil dipulihkan secara penuh (Heal & Feed)!</green>"));
            admin.playSound(admin.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
            buildGUI();
            return;
        }
        if (slot == 33) { // Cycle GameMode
            cycleGameMode();
            return;
        }
        if (slot == 34) { // Kick
            target.kick(mm.deserialize("<red><bold>KAMU DI-KICK DARI SERVER</bold></red>\n<gray>Alasan: Keputusan Administrator / Staf.</gray>"));
            admin.sendMessage(mm.deserialize("<red>✓ " + target.getName() + " berhasil di-kick dari server!</red>"));
            admin.playSound(admin.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.6f, 1.2f);
            new PlayerManagerGUI(plugin, admin).open();
            return;
        }

        // Navigation
        if (slot == 40) {
            admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            new PlayerManagerGUI(plugin, admin).open();
            return;
        }
        if (slot == 49) {
            admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            new MasterAdminGUI(plugin, admin).open();
        }
    }

    private void toggleMonarch() {
        String pKingdom = plugin.getApi().getPlayerRegionKey(target.getUniqueId());
        if (pKingdom == null || pKingdom.equalsIgnoreCase("NONE")) {
            admin.sendMessage(mm.deserialize("<red>Pemain belum memilih kerajaan!</red>"));
            return;
        }

        String currentKing = plugin.getConfigManager().getKingdomKing(pKingdom);
        boolean isCurrentKing = currentKing != null && currentKing.equalsIgnoreCase(target.getName());

        if (isCurrentKing) {
            plugin.getConfigManager().setKingdomKing(pKingdom, "Belum Ditunjuk");
            plugin.getTitleManager().unequipTitle(target);
            admin.sendMessage(mm.deserialize("<yellow>✓ Status Raja " + pKingdom + " untuk " + target.getName() + " telah dicabut.</yellow>"));
        } else {
            plugin.getConfigManager().setKingdomKing(pKingdom, target.getName());
            String titleId = "raja_" + pKingdom.toLowerCase();
            plugin.getTitleManager().getTitle(titleId).ifPresent(t -> {
                plugin.getTitleManager().equipTitle(target, t);
            });
            Bukkit.broadcast(mm.deserialize(
                    "<gradient:#f1c40f:#e67e22><bold>👑 PENOBATAN RAJA</bold></gradient> <dark_gray>➔</dark_gray> <white><bold>"
                    + target.getName() + "</bold></white> <gray>resmi dinobatkan sebagai Raja Kerajaan </gray><gold>" + pKingdom + "</gold><gray>!</gray>"
            ));
            target.showTitle(net.kyori.adventure.title.Title.title(
                    mm.deserialize("<gradient:#f1c40f:#e67e22><bold>👑 PENOBATAN RAJA 👑</bold></gradient>"),
                    mm.deserialize("<yellow>Kamu resmi menjadi Raja Kerajaan <gold>" + pKingdom + "</gold>!</yellow>"),
                    net.kyori.adventure.title.Title.Times.times(java.time.Duration.ofMillis(300), java.time.Duration.ofMillis(3000), java.time.Duration.ofMillis(800))
            ));
            target.playSound(target.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
        buildGUI();
    }

    private void cycleGameMode() {
        GameMode next = switch (target.getGameMode()) {
            case SURVIVAL -> GameMode.CREATIVE;
            case CREATIVE -> GameMode.ADVENTURE;
            case ADVENTURE -> GameMode.SPECTATOR;
            case SPECTATOR -> GameMode.SURVIVAL;
        };
        target.setGameMode(next);
        admin.sendMessage(mm.deserialize("<green>✓ GameMode <yellow>" + target.getName() + "</yellow> diubah ke <aqua>" + next.name() + "</aqua>.</green>"));
        admin.playSound(admin.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.3f);
        buildGUI();
    }

    private double getPlayerRupiah(Player p) {
        try {
            Class<?> providerClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyProvider");
            if ((boolean) providerClass.getMethod("isAvailable").invoke(null)) {
                Object api = providerClass.getMethod("get").invoke(null);
                return (double) api.getClass().getMethod("getBalance", UUID.class, String.class).invoke(api, p.getUniqueId(), "rupiah");
            }
        } catch (Throwable ignored) {}
        if (plugin.getVaultHook().hasEconomy()) {
            return plugin.getVaultHook().getBalance(p);
        }
        return 0.0;
    }

    private double getPlayerDiamond(Player p) {
        try {
            Class<?> providerClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyProvider");
            if ((boolean) providerClass.getMethod("isAvailable").invoke(null)) {
                Object api = providerClass.getMethod("get").invoke(null);
                return (double) api.getClass().getMethod("getBalance", UUID.class, String.class).invoke(api, p.getUniqueId(), "diamond");
            }
        } catch (Throwable ignored) {}
        return 0.0;
    }

    private void modifyBalance(double delta) {
        boolean handled = false;
        try {
            Class<?> providerClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyProvider");
            if ((boolean) providerClass.getMethod("isAvailable").invoke(null)) {
                Object api = providerClass.getMethod("get").invoke(null);
                if (delta > 0) {
                    api.getClass().getMethod("deposit", UUID.class, String.class, double.class).invoke(api, target.getUniqueId(), "rupiah", delta);
                } else {
                    api.getClass().getMethod("withdraw", UUID.class, String.class, double.class).invoke(api, target.getUniqueId(), "rupiah", Math.abs(delta));
                }
                handled = true;
            }
        } catch (Throwable ignored) {}

        if (!handled && plugin.getVaultHook().hasEconomy()) {
            if (delta > 0) {
                plugin.getVaultHook().deposit(target, delta);
            } else {
                plugin.getVaultHook().withdraw(target, Math.abs(delta));
            }
            handled = true;
        }

        if (handled) {
            if (delta > 0) {
                admin.sendMessage(mm.deserialize("<green>✓ Menambahkan <yellow>Rp " + String.format("%,.0f", delta) + "</yellow> ke " + target.getName() + ".</green>"));
            } else {
                admin.sendMessage(mm.deserialize("<yellow>✓ Mengurangi <red>Rp " + String.format("%,.0f", Math.abs(delta)) + "</red> dari " + target.getName() + ".</yellow>"));
            }
            admin.playSound(admin.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f);
        } else {
            admin.sendMessage(mm.deserialize("<red>Layanan ekonomi tidak tersedia.</red>"));
        }
        buildGUI();
    }

    private void changeKingdom(String kingdomKey) {
        Region region = plugin.getRegionManager().getRegion(kingdomKey).orElse(null);
        if (region == null) {
            admin.sendMessage(mm.deserialize("<red>Kerajaan " + kingdomKey + " tidak ditemukan!</red>"));
            return;
        }
        plugin.getPlayerDataService().updateRegion(target.getUniqueId(), region.getId());
        admin.sendMessage(mm.deserialize("<green>✓ Kerajaan " + target.getName() + " berhasil diubah menjadi <gold>" + region.getDisplayName() + "</gold>!</green>"));
        target.sendMessage(mm.deserialize("<gold><bold>👑 STATUS KERAJAAN:</bold> Afiliasi kerajaanmu telah disetel menjadi <yellow>" + region.getDisplayName() + "</yellow> oleh administrator!</gold>"));
        admin.playSound(admin.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);
        buildGUI();
    }

    private ItemStack createActionItem(Material mat, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            List<Component> components = new ArrayList<>();
            for (String l : loreLines) {
                components.add(mm.deserialize(l));
            }
            meta.lore(components);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGlass(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
