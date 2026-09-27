package com.apexsions.shop.gui;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.contract.TradeContract;
import com.apexsions.shop.gui.core.ShopGui;
import com.apexsions.shop.gui.core.ShopGuiButton;
import com.apexsions.shop.gui.core.ShopItemBuilder;
import com.apexsions.shop.gui.navigation.BackButton;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 54-Slot Interactive Dashboard for Royal Trade Orders & Quotas.
 */
public class TradeContractsMenu extends ShopGui {

    public TradeContractsMenu(ApexsionsShop plugin, Player player, ShopGui parent) {
        super(plugin, player, "<dark_gray><bold>[ KONTRAK EKSPOR KERAJAAN ]</bold></dark_gray>", 54, parent);
    }

    @Override
    public void initialize() {
        fillBorder();

        // 1. Header Information (Slot 4)
        String timeLeft = plugin.getTradeContractService() != null
                ? plugin.getTradeContractService().getFormattedRemainingTime()
                : "12j 00m";

        setButton(4, new ShopGuiButton(new ShopItemBuilder(Material.WRITABLE_BOOK)
                .name("<gradient:#f1c40f:#e67e22><bold>📜 DEWAN EKSPOR TIGA KERAJAAN</bold></gradient>")
                .lore(List.of(
                        "<gray>Penuhi pesanan pasokan resmi dari para penguasa</gray>",
                        "<gray>untuk meraih imbalan Rupiah berlimpah dan reputasi kerajaan.</gray>",
                        " ",
                        "<gray>Sisa Waktu Periode:</gray> <yellow><bold>" + timeLeft + "</bold></yellow>",
                        "<gray>Pembaruan Kontrak:</gray> <white>Setiap 12 Jam Sekali</white>",
                        " ",
                        "<yellow>Sentuh / Klik kontrak di bawah untuk menyetor pasokan!</yellow>"
                ))
                .glow()
                .build()));

        // 2. Active Contracts Grid (Slots 20, 21, 22, 23, 24, 30, 31, 32)
        int[] slots = { 20, 21, 22, 23, 24, 29, 31, 33 };
        List<TradeContract> contracts = plugin.getTradeContractService() != null
                ? plugin.getTradeContractService().getActiveContracts()
                : List.of();

        for (int i = 0; i < contracts.size() && i < slots.length; i++) {
            TradeContract contract = contracts.get(i);
            int slot = slots[i];

            int progress = plugin.getTradeContractService().getPlayerProgress(player.getUniqueId(), contract.getId());
            boolean isCompleted = plugin.getTradeContractService().isContractCompleted(player.getUniqueId(), contract.getId());

            // Count player's matching items
            int playerHas = 0;
            for (ItemStack is : player.getInventory().getContents()) {
                if (is != null && is.getType() == contract.getRequiredMaterial()) {
                    playerHas += is.getAmount();
                }
            }

            List<String> lore = new ArrayList<>();
            lore.add("<dark_gray>────────────────────────</dark_gray>");
            lore.add("<gray>Kerajaan Pemesan:</gray> <bold>" + contract.getKingdomDisplayName() + "</bold>");
            lore.add("<gray>Tuntutan:</gray> <white>" + contract.getLore() + "</white>");
            lore.add("<dark_gray>────────────────────────</dark_gray>");
            lore.add("<gray>Komoditas Dibutuhkan:</gray> <yellow>" + contract.getRequiredAmount() + "x " + formatMaterialName(contract.getRequiredMaterial()) + "</yellow>");
            lore.add("<gray>Imbalan Rupiah:</gray> <green><bold>" + plugin.getEconomyHook().format(contract.getRewardRupiah()) + "</bold></green>");
            lore.add("<gray>Imbalan Reputasi:</gray> <gold><bold>+" + contract.getRewardKingdomXp() + " XP Kerajaan</bold></gold>");
            lore.add("<dark_gray>────────────────────────</dark_gray>");

            // Progress bar
            int percent = (int) Math.min(100, Math.floor(((double) progress / contract.getRequiredAmount()) * 100));
            lore.add("<gray>Progres Penyetoran:</gray> " + getProgressBar(progress, contract.getRequiredAmount()) + " <yellow>" + progress + "/" + contract.getRequiredAmount() + " (" + percent + "%)</yellow>");
            lore.add("<gray>Stok di Inventori Kamu:</gray> <white>" + playerHas + " item</white>");
            lore.add(" ");

            if (isCompleted) {
                lore.add("<green><bold>✔ KONTRAK SELESAI (SUDAH DICLAIM)</bold></green>");
                lore.add("<dark_gray>Tunggu periode berikutnya untuk kontrak baru.</dark_gray>");
            } else if (playerHas > 0) {
                int canDeposit = Math.min(playerHas, contract.getRequiredAmount() - progress);
                lore.add("<gradient:#2ecc71:#27ae60><bold>▶ KLIK UNTUK SETOR " + canDeposit + " ITEM SEKARANG</bold></gradient>");
            } else {
                lore.add("<red><bold>✘ ITEM TIDAK MENCUKUPI DI INVENTORI</bold></red>");
                lore.add("<dark_gray>Kumpulkan item yang diminta lalu kembali ke menu ini.</dark_gray>");
            }

            ShopItemBuilder builder = new ShopItemBuilder(contract.getIcon())
                    .name("<bold>" + contract.getTitle() + "</bold>")
                    .lore(lore)
                    .hideAttributes();

            if (isCompleted) {
                builder.glow();
            }

            setButton(slot, new ShopGuiButton(builder.build(), event -> {
                if (isCompleted) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 1.2f);
                    return;
                }

                boolean delivered = plugin.getTradeContractService().deliverItems(player, contract);
                if (delivered) {
                    // Refresh GUI
                    initialize();
                }
            }));
        }

        // 3. Back Button (Slot 49)
        setButton(49, new BackButton(this, parent));
    }

    private String getProgressBar(int current, int max) {
        int totalBars = 10;
        int filled = (int) Math.min(totalBars, Math.floor(((double) current / Math.max(1, max)) * totalBars));
        StringBuilder sb = new StringBuilder("<green>");
        for (int i = 0; i < filled; i++) {
            sb.append("█");
        }
        sb.append("</green><dark_gray>");
        for (int i = filled; i < totalBars; i++) {
            sb.append("░");
        }
        sb.append("</dark_gray>");
        return sb.toString();
    }

    private String formatMaterialName(Material mat) {
        if (mat == null) return "-";
        String[] words = mat.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
