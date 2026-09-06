package com.apexsions.crates.reward;

import com.apexsions.battlepass.api.ApexsionsBattlepassAPI;
import com.apexsions.battlepass.api.ApexsionsBattlepassProvider;
import com.apexsions.core.api.ApexsionsCoreAPI;
import com.apexsions.core.api.ApexsionsCoreProvider;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.economy.api.ApexsionsEconomyAPI;
import com.apexsions.economy.api.ApexsionsEconomyProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Reward {

    private final String id;
    private final String name;
    private final RewardRarity rarity;
    private final double weight;
    private final Material material;
    private final int amount;
    private final List<String> commands;
    private final String economyCurrency;
    private final double economyAmount;
    private final long coreXp;
    private final int battlepassXp;
    private final boolean broadcast;
    private final int customModelData;

    public Reward(String id, String name, RewardRarity rarity, double weight, Material material, int amount,
                  List<String> commands, String economyCurrency, double economyAmount,
                  long coreXp, int battlepassXp, boolean broadcast, int customModelData) {
        this.id = id;
        this.name = name != null ? name : id;
        this.rarity = rarity != null ? rarity : RewardRarity.COMMON;
        this.weight = Math.max(0.01, weight);
        this.material = material != null ? material : Material.CHEST;
        this.amount = Math.max(1, amount);
        this.commands = commands != null ? commands : new ArrayList<>();
        this.economyCurrency = economyCurrency;
        this.economyAmount = economyAmount;
        this.coreXp = coreXp;
        this.battlepassXp = battlepassXp;
        this.broadcast = broadcast;
        this.customModelData = customModelData;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Component getDisplayName() {
        return MiniMessage.miniMessage().deserialize(name);
    }

    public RewardRarity getRarity() {
        return rarity;
    }

    public double getWeight() {
        return weight;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public List<String> getCommands() {
        return commands;
    }

    public String getEconomyCurrency() {
        return economyCurrency;
    }

    public double getEconomyAmount() {
        return economyAmount;
    }

    public long getCoreXp() {
        return coreXp;
    }

    public int getBattlepassXp() {
        return battlepassXp;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public ItemStack createDisplayItem(double chancePercent) {
        ItemStack item = new ItemStack(material, Math.min(amount, 64));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(getDisplayName());
            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }
            List<Component> lore = new ArrayList<>();
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Kelangkaan: </gray>" + rarity.getFormattedName()));
            if (chancePercent > 0) {
                lore.add(MiniMessage.miniMessage().deserialize("<gray>Peluang: <yellow>" + String.format("%.2f", chancePercent) + "%</yellow></gray>"));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void give(Player player) {
        // 1. Run commands
        for (String cmd : commands) {
            String processed = cmd.replace("%player%", player.getName()).trim();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processed);
        }

        // 2. Economy reward
        if (economyCurrency != null && economyAmount > 0) {
            if (ApexsionsEconomyProvider.isAvailable()) {
                try {
                    ApexsionsEconomyAPI ecoAPI = ApexsionsEconomyProvider.get();
                    ecoAPI.deposit(player.getUniqueId(), economyCurrency, economyAmount);
                } catch (Exception ignored) {}
            }
        }

        // 3. Core XP reward
        if (coreXp > 0) {
            if (ApexsionsCoreProvider.isAvailable()) {
                try {
                    ApexsionsCoreAPI coreAPI = ApexsionsCoreProvider.get();
                    coreAPI.addXp(player.getUniqueId(), coreXp, XpSource.CUSTOM);
                } catch (Exception ignored) {}
            }
        }

        // 4. Battlepass XP reward
        if (battlepassXp > 0) {
            if (ApexsionsBattlepassProvider.isAvailable()) {
                try {
                    ApexsionsBattlepassAPI bpAPI = ApexsionsBattlepassProvider.get();
                    bpAPI.addPlayerXp(player.getUniqueId(), battlepassXp);
                } catch (Exception ignored) {}
            }
        }
    }
}
