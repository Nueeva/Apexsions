package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
import com.apexsions.customenchants.group.EnchantmentGroup;
import com.apexsions.economy.api.ApexsionsEconomyAPI;
import com.apexsions.economy.api.ApexsionsEconomyProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 54-Slot Main Gacha Enchanter GUI (/ce) supporting Rupiah and Diamond payments.
 */
public class EnchanterGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<Integer, EnchantmentGroup> slotGroupMap = new HashMap<>();

    public EnchanterGUI(ApexsionsCustomEnchantsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#9b59b6:#e74c3c><bold>✨ ENCHANTER GACHA KERJAAN ✨</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotGroupMap.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Header slot 4: Player Balances
        ApexsionsEconomyAPI eco = ApexsionsEconomyProvider.get();
        double rupiah = eco != null ? eco.getBalance(player.getUniqueId(), "rupiah") : 0;
        double diamond = eco != null ? eco.getBalance(player.getUniqueId(), "diamond") : 0;

        ItemStack profile = createItem(Material.PLAYER_HEAD, "<gradient:#f1c40f:#e67e22><bold>👑 STATUS SALDO KAMU</bold></gradient>", List.of(
                "<gray>Pemain:</gray> <white>" + player.getName() + "</white>",
                "<gray>Saldo Rupiah:</gray> <green>Rp " + String.format("%,d", (long) rupiah).replace(',', '.') + "</green>",
                "<gray>Saldo Diamond:</gray> <aqua>" + (long) diamond + " 💎</aqua>",
                "",
                "<yellow>Beli buku sihir acak berdasarkan kasta tier di bawah.</yellow>"
        ));
        inventory.setItem(4, profile);

        // 7 Tiers placement (Slots 19, 20, 21, 22, 23, 24, 25)
        String[] tierOrder = {"SIMPLE", "UNIQUE", "ELITE", "ULTIMATE", "LEGENDARY", "FABLED", "HEROIC"};
        int[] tierSlots = {19, 20, 21, 22, 23, 24, 25};

        for (int i = 0; i < tierOrder.length; i++) {
            String tId = tierOrder[i];
            EnchantmentGroup grp = plugin.getGroupRegistry().getGroup(tId);
            if (grp == null) continue;

            int slot = tierSlots[i];
            slotGroupMap.put(slot, grp);

            List<String> lore = new ArrayList<>();
            lore.add("<gray>Dapatkan 1 Buku Sihir Acak</gray>");
            lore.add("<gray>kasta <color:" + grp.getColor() + ">" + grp.getId() + "</color>.</gray>");
            lore.add("");

            if (grp.isComingSoon() || !grp.isEnabled()) {
                lore.add("<red><bold>🔒 STATUS: COMING SOON</bold></red>");
                lore.add("<gray>Tier ini sedang dalam persiapan konten baru!</gray>");
                lore.add("");
                lore.add("<dark_gray>Tier God-tier akan segera hadir.</dark_gray>");
            } else {
                lore.add("<gray>Harga Gacha Acak:</gray> <gold><bold>" + grp.getFormattedCost() + "</bold></gold>");
                lore.add("<gray>Mata Uang:</gray> <yellow>" + grp.getCurrency().toUpperCase() + "</yellow>");
                lore.add("");
                lore.add("<green>● Success Rate: 40% - 100%</green>");
                lore.add("<red>● Destroy Rate: 0% - 60% (Auto 100% - Success)</red>");
                lore.add("");
                lore.add("<yellow>▶ Klik untuk gacha buku sihir!</yellow>");
            }

            ItemStack icon = createItem(grp.getIcon(), grp.getDisplayName(), lore);
            inventory.setItem(slot, icon);
        }

        // Magic Dust & Scrolls Row (Slots 29, 31, 33)
        // Slot 29: Mystery Dust
        inventory.setItem(29, createItem(Material.SUGAR, "<gradient:#9b59b6:#3498db><bold>✦ MYSTERY DUST ✦</bold></gradient>", List.of(
                "<gray>Beli Mystery Dust untuk mendapatkan booster success rate.</gray>",
                "<gray>Harga:</gray> <gold>Rp 20.000</gold>",
                "",
                "<yellow>▶ Klik untuk beli Mystery Dust!</yellow>"
        )));

        // Slot 31: White Scroll
        inventory.setItem(31, createItem(Material.PAPER, "<white><bold>🛡 WHITE SCROLL 🛡</bold></white>", List.of(
                "<gray>Melindungi senjata/armormu dari kehancuran sihir.</gray>",
                "<gray>Harga:</gray> <aqua>10 💎</aqua>",
                "",
                "<yellow>▶ Klik untuk beli White Scroll!</yellow>"
        )));

        // Slot 33: Black Scroll
        inventory.setItem(33, createItem(Material.INK_SAC, "<dark_gray><bold>📜 BLACK SCROLL 📜</bold></dark_gray>", List.of(
                "<gray>Mengekstrak 1 sihir dari item menjadi buku 100% success.</gray>",
                "<gray>Harga:</gray> <aqua>15 💎</aqua>",
                "",
                "<yellow>▶ Klik untuk beli Black Scroll!</yellow>"
        )));

        // Bottom Navigations
        // Slot 47: Tinkerer Coming Soon button
        inventory.setItem(47, createItem(Material.ANVIL, "<gradient:#e67e22:#f39c12><bold>⚙ TINKERER (COMING SOON)</bold></gradient>", List.of(
                "<gray>Daur ulang peralatan & buku sihir bekas.</gray>",
                "<yellow>▶ Klik untuk status!</yellow>"
        )));

        // Slot 49: Toko Buku Spesifik Button (CRITICAL NAVIGATION)
        inventory.setItem(49, createItem(Material.ENCHANTED_BOOK, "<gradient:#9b59b6:#3498db><bold>📚 TOKO BUKU SIHIR SPESIFIK 📚</bold></gradient>", List.of(
                "<gray>Pilih dan beli buku sihir yang kamu mau secara langsung!</gray>",
                "<dark_gray>•</dark_gray> <gray>Harga:</gray> <gold>" + (int) plugin.getSpecificBookMultiplier() + "x lipat harga gacha acak</gold>",
                "<dark_gray>•</dark_gray> <gray>Success Rate:</gray> <green>" + (int) plugin.getSpecificBookSuccessChance() + "% Fixed Chance</green>",
                "",
                "<yellow>▶ Klik untuk buka Toko Buku Spesifik!</yellow>"
        )));

        // Slot 51: Close
        inventory.setItem(51, createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>", List.of("<gray>Tutup menu enchanter.</gray>")));
    }

    private ItemStack createItem(Material mat, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(mat != null ? mat : Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            if (loreLines != null) {
                List<Component> cList = new ArrayList<>();
                for (String l : loreLines) {
                    cList.add(mm.deserialize(l));
                }
                meta.lore(cList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 51) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        if (slot == 47) { // Tinkerer Coming Soon
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new TinkererComingSoonGUI(player).open();
            return;
        }

        if (slot == 49) { // Specific Book Shop
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new ShopCategorySelectGUI(plugin, player).open();
            return;
        }

        if (slot == 29) { // Buy Mystery Dust (Rp 20.000)
            buyMysteryDust();
            return;
        }

        if (slot == 31) { // Buy White Scroll (10 Diamond)
            buyWhiteScroll();
            return;
        }

        if (slot == 33) { // Buy Black Scroll (15 Diamond)
            buyBlackScroll();
            return;
        }

        EnchantmentGroup grp = slotGroupMap.get(slot);
        if (grp != null) {
            if (grp.isComingSoon() || !grp.isEnabled()) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<red><bold>🔒 TIER HEROIC COMING SOON!</bold> Tier para dewa ini sedang dalam tahap pengembangan khusus.</red>"));
                return;
            }
            gachaRandomBook(grp);
        }
    }

    private void gachaRandomBook(EnchantmentGroup grp) {
        ApexsionsEconomyAPI eco = ApexsionsEconomyProvider.get();
        if (eco == null) {
            player.sendMessage(mm.deserialize("<red>Layanan ekonomi sedang tidak tersedia!</red>"));
            return;
        }

        String cur = grp.getCurrency().toLowerCase();
        double cost = grp.getCost();

        if (!eco.has(player.getUniqueId(), cur, cost)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<red><bold>✖ SALDO TIDAK CUKUP!</bold> Kamu memerlukan <gold>" + grp.getFormattedCost() + "</gold> untuk gacha tier ini.</red>"));
            return;
        }

        List<CustomEnchant> pool = new ArrayList<>();
        for (CustomEnchant e : plugin.getEnchantmentRegistry().getEnchantmentsByGroup(grp)) {
            if (e.isPurchasable()) {
                pool.add(e);
            }
        }
        if (pool.isEmpty()) {
            player.sendMessage(mm.deserialize("<red>Belum ada sihir yang dapat dibeli pada tier " + grp.getId() + "!</red>"));
            return;
        }

        // Withdraw
        eco.withdraw(player.getUniqueId(), cur, cost);

        // Pick random enchant
        CustomEnchant chosen = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        int success = ThreadLocalRandom.current().nextInt(40, 101);
        int destroy = 100 - success;

        ItemStack book = plugin.getEnchantBookManager().createBook(chosen, 1, success, destroy);
        HashMap<Integer, ItemStack> left = player.getInventory().addItem(book);
        if (!left.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), book);
            player.sendMessage(mm.deserialize("<yellow>Buku dijatuhkan ke kakimu karena tas penuh!</yellow>"));
        }

        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.4f);
        player.sendMessage(mm.deserialize("<green><bold>✨ GACHA BERHASIL!</bold> Kamu memperoleh <color:" + grp.getColor() + ">" + chosen.getDisplayName() + " I</color> (" + success + "% Success / " + destroy + "% Destroy)!</green>"));

        buildGUI();
    }

    private void buyMysteryDust() {
        ApexsionsEconomyAPI eco = ApexsionsEconomyProvider.get();
        if (eco == null) return;
        if (!eco.has(player.getUniqueId(), "rupiah", 20000)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<red>Saldo Rupiah tidak mencukupi (Rp 20.000)!</red>"));
            return;
        }
        eco.withdraw(player.getUniqueId(), "rupiah", 20000);
        player.getInventory().addItem(plugin.getMagicDustManager().createMysteryDust());
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        player.sendMessage(mm.deserialize("<green>✓ Berhasil membeli 1x Mystery Dust!</green>"));
        buildGUI();
    }

    private void buyWhiteScroll() {
        ApexsionsEconomyAPI eco = ApexsionsEconomyProvider.get();
        if (eco == null) return;
        if (!eco.has(player.getUniqueId(), "diamond", 10)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<red>Saldo Diamond tidak mencukupi (10 💎)!</red>"));
            return;
        }
        eco.withdraw(player.getUniqueId(), "diamond", 10);
        player.getInventory().addItem(plugin.getScrollManager().createWhiteScroll());
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        player.sendMessage(mm.deserialize("<green>✓ Berhasil membeli 1x White Scroll!</green>"));
        buildGUI();
    }

    private void buyBlackScroll() {
        ApexsionsEconomyAPI eco = ApexsionsEconomyProvider.get();
        if (eco == null) return;
        if (!eco.has(player.getUniqueId(), "diamond", 15)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<red>Saldo Diamond tidak mencukupi (15 💎)!</red>"));
            return;
        }
        eco.withdraw(player.getUniqueId(), "diamond", 15);
        player.getInventory().addItem(plugin.getScrollManager().createBlackScroll());
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        player.sendMessage(mm.deserialize("<green>✓ Berhasil membeli 1x Black Scroll!</green>"));
        buildGUI();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
