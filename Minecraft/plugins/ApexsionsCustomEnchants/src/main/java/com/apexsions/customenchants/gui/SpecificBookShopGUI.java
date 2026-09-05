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

/**
 * 54-Slot GUI enabling players to directly purchase specific custom enchant books at 3x cost and 50% success chance.
 */
public class SpecificBookShopGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private int page = 1;
    private final Map<Integer, CustomEnchant> slotEnchantMap = new HashMap<>();

    public SpecificBookShopGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.page = Math.max(1, page);
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#9b59b6:#3498db><bold>📚 TOKO BUKU SIHIR SPESIFIK 📚</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotEnchantMap.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        for (int i = 0; i < 54; i++) {
            if (i >= 45) {
                inventory.setItem(i, border);
            }
        }

        // Header status
        double multiplier = plugin.getSpecificBookMultiplier();
        int fixedSuccess = (int) plugin.getSpecificBookSuccessChance();

        List<CustomEnchant> all = new ArrayList<>(plugin.getEnchantmentRegistry().getAllEnchantments());
        // Filter out Heroic if coming soon
        all.removeIf(e -> e.getGroup().isComingSoon());

        int maxPerPage = 45;
        int startIndex = (page - 1) * maxPerPage;
        int endIndex = Math.min(startIndex + maxPerPage, all.size());

        for (int i = startIndex; i < endIndex; i++) {
            CustomEnchant enchant = all.get(i);
            int slot = i - startIndex;
            slotEnchantMap.put(slot, enchant);

            EnchantmentGroup grp = enchant.getGroup();
            double specificPrice = grp.getCost() * multiplier;
            String formattedCost = grp.getCurrency().equalsIgnoreCase("diamond")
                    ? (long) specificPrice + " Diamond"
                    : "Rp " + String.format("%,d", (long) specificPrice).replace(',', '.');

            List<String> lore = new ArrayList<>();
            lore.add("<gray>" + enchant.getDescription() + "</gray>");
            lore.add("");
            lore.add("<gold>Tier:</gold> " + grp.getDisplayName());
            lore.add("<gold>Berlaku Pada:</gold> <yellow>" + enchant.getAppliesTo() + "</yellow>");
            lore.add("<gold>Maks Level:</gold> <yellow>" + enchant.getMaxLevel() + "</yellow>");
            lore.add("");
            lore.add("<green>● " + fixedSuccess + "% Success Rate (Pasti)</green>");
            lore.add("<red>● 30% Destroy Rate</red>");
            lore.add("");
            lore.add("<gray>Harga Beli Langsung:</gray> <gold><bold>" + formattedCost + "</bold></gold>");
            lore.add("<dark_gray>(3x lipat harga gacha acak tier)</dark_gray>");
            lore.add("");
            lore.add("<yellow>▶ Klik untuk membeli buku sihir ini!</yellow>");

            ItemStack item = createItem(Material.ENCHANTED_BOOK, "<color:" + grp.getColor() + "><bold>" + enchant.getDisplayName() + " I</bold></color>", lore);
            inventory.setItem(slot, item);
        }

        // Bottom Controls
        // Slot 45: Back to Gacha Enchanter GUI
        ItemStack back = createItem(Material.ENDER_EYE, "<gradient:#f1c40f:#e67e22><bold>◀ KEMBALI KE GACHA ENCHANTER</bold></gradient>", List.of(
                "<gray>Kembali ke menu gacha random book per tier.</gray>"
        ));
        inventory.setItem(45, back);

        // Slot 48: Prev Page
        if (page > 1) {
            inventory.setItem(48, createItem(Material.ARROW, "<yellow><bold>◀ Halaman " + (page - 1) + "</bold></yellow>", null));
        }

        // Slot 49: Info Page
        inventory.setItem(49, createItem(Material.BOOK, "<gold><bold>Halaman " + page + "</bold></gold>", List.of(
                "<gray>Total Sihir: " + all.size() + "</gray>",
                "<gray>Buku spesifik memiliki fixed " + fixedSuccess + "% success rate.</gray>"
        )));

        // Slot 50: Next Page
        if (endIndex < all.size()) {
            inventory.setItem(50, createItem(Material.ARROW, "<yellow><bold>Halaman " + (page + 1) + " ▶</bold></yellow>", null));
        }

        // Slot 53: Close
        inventory.setItem(53, createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>", null));
    }

    private ItemStack createItem(Material mat, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(mat);
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

        if (slot == 45) { // Return to Enchanter GUI
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new EnchanterGUI(plugin, player).open();
            return;
        }

        if (slot == 48 && page > 1) { // Prev Page
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new SpecificBookShopGUI(plugin, player, page - 1).open();
            return;
        }

        if (slot == 50) { // Next Page
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new SpecificBookShopGUI(plugin, player, page + 1).open();
            return;
        }

        if (slot == 53) { // Close
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        CustomEnchant enchant = slotEnchantMap.get(slot);
        if (enchant != null) {
            buySpecificBook(enchant);
        }
    }

    private void buySpecificBook(CustomEnchant enchant) {
        EnchantmentGroup grp = enchant.getGroup();
        double price = grp.getCost() * plugin.getSpecificBookMultiplier();
        String currency = grp.getCurrency().toLowerCase();

        ApexsionsEconomyAPI eco = ApexsionsEconomyProvider.get();
        if (eco == null) {
            player.sendMessage(mm.deserialize("<red>Layanan ekonomi sedang tidak tersedia!</red>"));
            return;
        }

        if (!eco.has(player.getUniqueId(), currency, price)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            String formattedCost = currency.equals("diamond") ? (long) price + " Diamond" : "Rp " + String.format("%,d", (long) price).replace(',', '.');
            player.sendMessage(mm.deserialize("<red><bold>✖ SALDO TIDAK CUKUP!</bold> Kamu membutuhkan <gold>" + formattedCost + "</gold> untuk membeli buku ini.</red>"));
            return;
        }

        // Withdraw
        eco.withdraw(player.getUniqueId(), currency, price);

        // Give book with 50% success chance
        int success = (int) plugin.getSpecificBookSuccessChance();
        int destroy = (int) plugin.getSpecificBookDestroyChance();
        ItemStack book = plugin.getEnchantBookManager().createBook(enchant, 1, success, destroy);

        HashMap<Integer, ItemStack> rem = player.getInventory().addItem(book);
        if (!rem.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), book);
            player.sendMessage(mm.deserialize("<yellow>Buku dijatuhkan ke kakimu karena inventory penuh!</yellow>"));
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.sendMessage(mm.deserialize("<green><bold>✓ PEMBELIAN BERHASIL!</bold> Kamu telah membeli buku sihir <gold>" + enchant.getDisplayName() + " I</gold> (" + success + "% Success)!</green>"));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
