package com.apex.battlepass.currency;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.api.event.BattlePassCurrencyChangeEvent;
import com.apex.battlepass.player.PlayerData;
import com.apex.battlepass.util.ColorUtil;
import org.bukkit.Bukkit;

import java.util.UUID;

public class CurrencyManager implements CurrencyService {

    private final ApexsionsBattlepass plugin;

    public CurrencyManager(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
    }

    @Override
    public int getBalance(UUID uuid) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(uuid);
        return data != null ? data.getCurrency() : 0;
    }

    @Override
    public void addCurrency(UUID uuid, int amount) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(uuid);
        if (data != null && amount > 0) {
            int oldBalance = data.getCurrency();
            data.addCurrency(amount);
            int newBalance = data.getCurrency();

            BattlePassCurrencyChangeEvent event = new BattlePassCurrencyChangeEvent(uuid, oldBalance, newBalance);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    @Override
    public boolean removeCurrency(UUID uuid, int amount) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(uuid);
        if (data != null && amount > 0) {
            int oldBalance = data.getCurrency();
            if (data.removeCurrency(amount)) {
                int newBalance = data.getCurrency();
                BattlePassCurrencyChangeEvent event = new BattlePassCurrencyChangeEvent(uuid, oldBalance, newBalance);
                Bukkit.getPluginManager().callEvent(event);
                return true;
            }
        }
        return false;
    }

    @Override
    public void setCurrency(UUID uuid, int amount) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(uuid);
        if (data != null) {
            int oldBalance = data.getCurrency();
            data.setCurrency(amount);
            int newBalance = data.getCurrency();

            BattlePassCurrencyChangeEvent event = new BattlePassCurrencyChangeEvent(uuid, oldBalance, newBalance);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    @Override
    public boolean hasBalance(UUID uuid, int amount) {
        return getBalance(uuid) >= amount;
    }

    @Override
    public String getCurrencyName() {
        return plugin.getConfig().getString("currency.name", "Battle Coins");
    }

    @Override
    public String getCurrencySymbol() {
        return plugin.getConfig().getString("currency.symbol", "🪙");
    }

    @Override
    public String format(int amount) {
        String fmt = plugin.getConfig().getString("currency.format", "%symbol% %amount%");
        return ColorUtil.colorize(fmt.replace("%symbol%", getCurrencySymbol()).replace("%amount%", String.valueOf(amount)));
    }
}
