package com.apex.battlepass.expshop.service;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.expshop.model.ExpPackage;
import com.apex.battlepass.expshop.model.ExpPurchaseContext;
import com.apex.battlepass.expshop.provider.CurrencyProviderRegistry;
import com.apex.battlepass.expshop.provider.ExpShopCurrencyProvider;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class ExpShopService {

    private final ApexsionsBattlepass plugin;
    private final CurrencyProviderRegistry currencyRegistry;
    private final Map<String, ExpPackage> packages = new LinkedHashMap<>();

    public ExpShopService(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
        this.currencyRegistry = new CurrencyProviderRegistry();
        loadPackages();
    }

    public void loadPackages() {
        packages.clear();
        File file = new File(plugin.getDataFolder(), "exp-shop/packages.yml");
        if (!file.exists()) {
            plugin.saveResource("exp-shop/packages.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection sec = config.getConfigurationSection("packages");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                int exp = sec.getInt(key + ".exp", 100);
                String displayName = sec.getString(key + ".display-name", "+" + exp + " BP XP");
                Map<String, Double> prices = new HashMap<>();

                ConfigurationSection priceSec = sec.getConfigurationSection(key + ".prices");
                if (priceSec != null) {
                    for (String cKey : priceSec.getKeys(false)) {
                        prices.put(cKey.toLowerCase(), priceSec.getDouble(cKey, 0.0));
                    }
                }

                ExpPackage pkg = new ExpPackage(key, displayName, exp, prices);
                packages.put(key, pkg);
            }
        }
    }

    public Map<String, ExpPackage> getPackages() {
        return packages;
    }

    public CurrencyProviderRegistry getCurrencyRegistry() {
        return currencyRegistry;
    }

    public void processPurchase(ExpPurchaseContext context) {
        Player player = context.getPlayer();

        // 1. Validate Season & Transition
        if (!plugin.getSeasonManager().isActive()) {
            player.sendMessage(plugin.getMessage("shop-locked-transition"));
            return;
        }

        // 2. Validate Provider
        ExpShopCurrencyProvider provider = currencyRegistry.getProvider(context.getCurrencyId());
        if (provider == null || !provider.isAvailable()) {
            player.sendMessage("§8[§6§lEXP Shop§8] §cMetode pembayaran ini belum dikonfigurasi / belum tersedia saat ini. (This payment method has not been configured yet.)");
            return;
        }

        double price = context.getPrice();
        int expAmount = context.getExpPackage().getExpAmount();

        // 3. Check Balance & Withdraw
        provider.hasBalance(player.getUniqueId(), price).thenAccept(hasBalance -> {
            if (!hasBalance) {
                player.sendMessage("§8[§6§lEXP Shop§8] §cSaldo Anda tidak cukup! Butuh §e" + provider.format(price));
                return;
            }

            provider.withdraw(player.getUniqueId(), price).thenAccept(success -> {
                if (success) {
                    // 4. Add XP through centralized service
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        plugin.getXpService().addXp(player, expAmount);
                        player.sendMessage("§8[§6§lEXP Shop§8] §aBerhasil membeli §e+" + expAmount + " BP XP §aseharga §e" + provider.format(price) + "§a!");
                    });
                } else {
                    player.sendMessage("§8[§6§lEXP Shop§8] §cTransaksi gagal diproses oleh payment provider.");
                }
            });
        });
    }
}
