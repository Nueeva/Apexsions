# Dokumentasi Teknis ApexsionsShop 📖

## Arsitektur Modular
Struktur folder dan konfigurasi `ApexsionsShop` dibuat rapi dan modular seperti pada `ApexsionsBattlepass`:

```
plugins/ApexsionsShop/
├── README.md
├── DOKUMENTASI.md
├── GEMINI.md
├── pom.xml
└── src/main/
    ├── java/com/apex/shop/
    │   ├── ApexsionsShop.java
    │   ├── api/
    │   │   ├── ApexsionsShopAPI.java
    │   │   └── ApexsionsShopProvider.java
    │   ├── category/
    │   │   ├── ShopCategory.java
    │   │   ├── ShopItem.java
    │   │   └── ShopItemRegistry.java
    │   ├── command/
    │   │   ├── ShopCommand.java
    │   │   └── SellCommand.java
    │   ├── config/
    │   │   └── ConfigManager.java
    │   ├── dynamic/
    │   │   ├── DynamicPriceCalculator.java
    │   │   ├── WeatherPriceService.java
    │   │   ├── KingdomMarketService.java
    │   │   ├── SupplyScannerService.java
    │   │   └── TaxService.java
    │   ├── gui/
    │   │   ├── ShopMainMenu.java
    │   │   ├── CategoryShopMenu.java
    │   │   ├── QuantitySelectMenu.java
    │   │   ├── SellGuiMenu.java
    │   │   ├── core/
    │   │   └── navigation/
    │   ├── integration/
    │   │   ├── EconomyHook.java
    │   │   └── KingdomCoreHook.java
    │   └── util/
    │       ├── NumberFormatUtil.java
    │       └── InventoryUtil.java
    └── resources/
        ├── plugin.yml
        ├── config.yml
        ├── messages.yml
        ├── gui.yml
        ├── markets.yml
        └── categories/
            ├── blocks.yml
            ├── food.yml
            ├── farming.yml
            ├── ores.yml
            ├── mob_drops.yml
            └── dyes.yml
```

## Rumus Harga Dinamis
$$\text{Harga Akhir} = (\text{Harga Dasar} \times M_{\text{Cuaca}} \times M_{\text{Kerajaan}} \times M_{\text{Pasokan}}) \pm \text{Pajak}$$
- **Pembelian (Buy):** $\text{Total} = \text{Harga Raw} + \text{Pajak}$
- **Penjualan (Sell):** $\text{Total} = \text{Harga Raw} - \text{Pajak}$
- **Rasio Jual Dasar:** $20\%$ dari harga beli.
