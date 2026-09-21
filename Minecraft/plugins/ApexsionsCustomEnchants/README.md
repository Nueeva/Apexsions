# ApexsionsCustomEnchants ✨

> **Plugin Suite:** Apexsions  
> **Brand:** `Apexsions`  
> **Tagline:** `The Peak Civilizations`  
> **Target Runtime:** Paper 26.2 (Minecraft 26.2, Java 21 LTS)

Modul sihir khusus dan kustomisasi perlengkapan resmi untuk peradaban **Apexsions — The Peak Civilizations**. Menghadirkan 182 custom enchantments lintas 7 tingkatan tier kekuatan, sistem gacha Enchanter dual-currency, toko buku sihir spesifik 54-slot, item utilitas magis (Magic Dust, Scrolls), serta builder set armor terintegrasi.

---

## 🌟 Fitur Utama

- **182 Custom Enchantments:** Terbagi ke dalam 7 kelompok kekuatan: `Simple`, `Unique`, `Elite`, `Ultimate`, `Legendary`, `Fabled`, dan `Master`.
- **Enchanter Gacha Dual-Currency (`/ce`):** Membeli buku sihir acak per tier menggunakan saldo `Rupiah` atau `Diamond` via `ApexsionsEconomyAPI`.
- **Toko Buku Sihir Spesifik (`/ce shop`):** Antarmuka 54-slot untuk membeli buku sihir tertentu dengan harga transparan tanpa elemen keberuntungan.
- **Item Utilitas Magis:**
  - **Magic Dust:** Menambah persentase keberhasilan (*success rate*) buku sihir (hingga 100%).
  - **Mystery Dust:** Debu pemulihan hasil daur ulang buku yang gagal.
  - **White Scroll:** Melindungi armor atau senjata dari kehancuran saat pemasangan sihir gagal.
  - **Black Scroll:** Mengekstraksi salah satu sihir custom secara aman dari item kembali menjadi buku.
- **Interactive Armor Set Builder (`/ace create` & `/presets`):** Alat pembuat set perlengkapan kustom dengan sinkronisasi ID otomatis dan efek bonus full-set.
- **Pemberian Custom Enchants Melampaui Vanilla Limit:** Mendukung level sihir hingga 4x lipat limit standar vanilla.

---

## ⌨️ Daftar Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/ce` | `/enchanter`, `/customenchants` | Membuka Enchanter Gacha Dual-Currency GUI | `apexsions.customenchants.use` | `true` |
| `/ce shop` | `/ce toko` | Membuka Toko Buku Sihir Spesifik 54-Slot | `apexsions.customenchants.use` | `true` |
| `/ce tinkerer` | - | Membuka antarmuka Tinkerer daur ulang item sihir | `apexsions.customenchants.use` | `true` |
| `/ace` | `/apexsionscustomenchants`, `/aceadmin` | Central Admin Hub GUI (45-Slot) | `apexsions.admin` | `op` |
| `/ace enchants` | - | Katalog interaktif seluruh custom enchantments | `apexsions.admin` | `op` |
| `/ace create` | - | Interactive Item & Armor Set Builder | `apexsions.admin` | `op` |
| `/ace pricing` | - | Pengaturan harga gacha, rate, dan multiplier | `apexsions.admin` | `op` |
| `/ace reload` | - | Memuat ulang konfigurasi sihir, grup, dan set | `apexsions.admin` | `op` |
| `/presets` | `/preset`, `/acepresets` | Membuka antarmuka preset armor & tool set tersimpan | `apexsions.admin` | `op` |

---

## 🔗 Dependensi
- **ApexsionsCore** (Required / Framework & Progression)
- **ApexsionsEconomy** (Softdepend / Transaksi gacha Rupiah & Diamond)
- **NightCore** (Softdepend / Dialog UI)
- **Vault** (Softdepend / Economy bridge)
