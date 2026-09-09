# ⚔️ Panduan Integrasi — ApexsionsCustomEnchants

> **Plugin:** `ApexsionsCustomEnchants`  
> **Author:** Apexsions Team  
> **Fokus Modul:** Sihir Kustom Peradaban (*Custom Enchants*), Tingkatan Kitab (*Enchantment Tiers*), Gulungan Pengaman (*Protection Scrolls*), Debu Keberhasilan (*Success Dust*), dan Set Efek Perlengkapan.  

Plugin **ApexsionsCustomEnchants** menyediakan sistem sihir tingkat lanjut (*custom enchantment*) yang memperkaya gameplay survival RPG di dunia peradaban Apexsions.

---

## 🔮 Fitur Utama Sistem Sihir Kustom

1. **Tingkatan Sihir (Enchantment Groups / Tiers):**
   - **Simple / Common:** Sihir dasar penunjang aktivitas harian (misal: *AutoSmelt*, *Telepathy*).
   - **Unique / Rare:** Peningkatan statistik pertarungan dan pertambangan.
   - **Elite / Epic:** Sihir tingkat lanjut dengan efek area (*AoE*) dan manipulasi status.
   - **Ultimate / Legendary:** Kemampuan tempur puncak (misal: *Lifesteal*, *Overload*, *Rage*).
   - **Mastery / Mythic:** Sihir legendaris peradaban dengan efek visual partikel dan buff kerajaan.
2. **Item Khusus:**
   - **Scrolls:** *White Scroll* (Mencegah item hancur), *Black Scroll* (Mencabut sihir).
   - **Dust:** *Success Dust* (Menaikkan persentase keberhasilan pemasangan buku sihir).
   - **Tinkerer & Alchemist:** Sistem daur ulang item sihir menjadi pengalaman atau debu rahasia.

---

## 🔗 Integrasi Placeholder & Display

Sistem lore dan antarmuka buku sihir `ApexsionsCustomEnchants` mendukung pemanggilan placeholder standar ekosistem Apexsions:
- Pemain dapat melihat biaya peningkatan buku sihir dengan placeholder mata uang resmi:
  - Rupiah: `%apexsionseconomy_rupiah_formatted%`
  - Diamond: `%apexsionseconomy_diamond_formatted%`
  - Battle Coins: `%apexsionsbattlepass_coins_formatted%`
- Pangkat pemain diperhitungkan dalam batasan pemasangan sihir maksimal per item (%apexsions_rank%).
