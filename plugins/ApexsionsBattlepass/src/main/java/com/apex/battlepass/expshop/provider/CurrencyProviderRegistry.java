package com.apex.battlepass.expshop.provider;

import java.util.HashMap;
import java.util.Map;

public class CurrencyProviderRegistry {

    private final Map<String, ExpShopCurrencyProvider> providers = new HashMap<>();

    public CurrencyProviderRegistry() {
        // Register ApexsionsEconomy currency providers
        registerProvider(new ApexsionsEconomyCurrencyProvider("rupiah", "Rupiah"));
        registerProvider(new ApexsionsEconomyCurrencyProvider("diamond", "Diamond"));
    }

    public void registerProvider(ExpShopCurrencyProvider provider) {
        if (provider != null) {
            providers.put(provider.getCurrencyId().toLowerCase(), provider);
        }
    }

    public ExpShopCurrencyProvider getProvider(String currencyId) {
        return providers.get(currencyId.toLowerCase());
    }

    public Map<String, ExpShopCurrencyProvider> getProviders() {
        return providers;
    }
}
