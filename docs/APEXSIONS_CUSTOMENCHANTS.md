# ApexsionsCustomEnchants — Comprehensive Technical Manual

Panduan teknis dan operasional lengkap untuk modul **`ApexsionsCustomEnchants`** (182 Custom Enchantments lintas 7 Tier, Enchanter Gacha Dual-Currency, Toko Buku Sihir Spesifik 54-Slot, Item Utilitas Magis, dan Interactive Armor Set Builder).

> **Game Server Domain:** `apexsions.com:32348` (Java & Bedrock)
> **Web Platform Domain:** `https://web.apexsions.com`

---

## 🔮 1. Ikhtisar Modul & Arsitektur

`ApexsionsCustomEnchants` adalah modul sistem sihir dan perlengkapan kustom terdepan untuk ekosistem **Apexsions — The Peak Civilizations**. Modul ini menghadirkan 182 custom enchantments unik yang melampaui batasan vanilla hingga 4x lipat, didukung mesin gacha dual-currency, perlindungan item magis, dan pembuat set armor peradaban.

```
                           ┌────────────────────────┐
                           │ApexsionsCustomEnchants │
                           │(Custom Enchant Engine) │
                           └───────────┬────────────┘
                                       │
          ┌────────────────────────────┼────────────────────────────┐
          ▼                            ▼                            ▼
┌───────────────────┐        ┌───────────────────┐        ┌───────────────────┐
│ 182 Custom Enchants│       │ Dual Enchanter    │        │ Armor Sets & Dust │
│ 7 Power Tiers     │        │ Gacha (/ce) &     │        │ Magic/Mystery Dust│
│ Simple -> Master  │        │ Book Shop 54-Slot │        │ White/Black Scroll│
└───────────────────┘        └───────────────────┘        └───────────────────┘
```

---

## 📜 2. Tujuh Kelompok Kekuatan Sihir (7 Enchantment Tiers)

Sistem mengkategorikan 182 sihir ke dalam 7 kelompok tingkat kekuatan dengan bobot visual dan gameplay yang khas:

| Tier | Kode Warna | Tingkat Kekuatan | Karakteristik Gameplay & Spesialisasi |
| :--- | :--- | :---: | :--- |
| **Simple** | `#aaaaaa` (Abu-abu) | Tier I | Utilitas dasar pemula, auto-smelt ringan, efisiensi penggalian tanah. |
| **Unique** | `#55ff55` (Hijau) | Tier II | Mobilitas, pernapasan air, peningkatan peluang panen ganda. |
| **Elite** | `#00aaaa` (Sian Gelap) | Tier III | Pertahanan proyektil, serangan racun ringan, daya pantul duri tajam. |
| **Ultimate** | `#ff5555` (Merah Terang) | Tier IV | Serangan critical tinggi, reduksi damage ledakan, ketahanan senjata. |
| **Legendary** | `#ffaa00` (Emas Oranye) | Tier V | Efek vampirisme (*lifesteal*), serangan tebasan suci, imunitas racun. |
| **Fabled** | `#aa00aa` (Ungu Mistis) | Tier VI | Petir multi-target, regenerasi cepat, pelindung kematian instan. |
| **Master** | `#ff55ff` (Magenta Kosmik) | Tier VII | Sihir kedaulatan tertinggi, manipulasi gravitasi, dan aura peradaban. |

---

## 💰 3. Sistem Pembelian Dual-Currency: Gacha & Toko Spesifik

1. **Enchanter Gacha GUI (`/ce`)**:
   - Pemain memilih tier kelompok sihir yang diinginkan.
   - Pembelian buku sihir acak dapat menggunakan **Rupiah** atau **Diamond** secara instan via `ApexsionsEconomyAPI`.
   - Buku yang diperoleh memiliki variasi acak *Success Rate* ($0\% - 100\%$) dan *Destroy Rate* ($0\% - 100\%$).
2. **Toko Buku Sihir Spesifik (`/ce shop`)**:
   - Antarmuka 54-slot untuk membeli buku sihir tertentu yang sudah pasti tanpa elemen probabilitas gacha.
   - Dirancang bagi pemain yang ingin meracik kombinasi build perlengkapan tertentu secara presisi.

---

## 🛡️ 4. Item Utilitas Magis & Bahan Habis Pakai

- **Magic Dust**: Meningkatkan persentase keberhasilan (*Success Rate*) suatu buku sihir saat ditumpuk (*drag & drop*).
- **Mystery Dust**: Diperoleh dari pemurnian buku gagal, dapat dikumpulkan untuk ditukar dengan debu sihir murni.
- **White Scroll**: Memberikan lapisan pelindung (*Protected*) pada perlengkapan. Jika pemasangan sihir gagal fatal (*Destroy*), White Scroll lenyap namun perlengkapan pemain tetap utuh.
- **Black Scroll**: Mengekstrak satu sihir custom acak dari item kembali menjadi buku sihir dengan tingkat keberhasilan $100\%$.

---

## 👑 5. Interactive Armor Set Builder & Full-Set Bonuses

Melalui perintah `/ace create` dan `/presets`:
- Admin dan builder dapat merancang set perlengkapan lengkap (*Helmet*, *Chestplate*, *Leggings*, *Boots*).
- **Full-Set Bonus Engine**: Memberikan efek pasif kuat jika pemain mengenakan 4 potong set armor yang identik (misal bonus defense ekstra dan kecepatan gerak).
- Kompatibilitas penggabungan anvil kustom (`CustomAnvilListener`) mencegah konflik NBT dan menjaga batas level progresi pemain (`ItemLevelRestrictionListener`).

---

## ⚡ 6. Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/ce` | `/enchanter`, `/customenchants` | Membuka Enchanter Gacha Dual-Currency GUI | `apexsions.customenchants.use` | `true` |
| `/ce shop` | `/ce toko` | Membuka Toko Buku Sihir Spesifik 54-Slot | `apexsions.customenchants.use` | `true` |
| `/ce tinkerer` | - | Membuka antarmuka Tinkerer daur ulang item sihir | `apexsions.customenchants.use` | `true` |
| `/ace` | `/apexsionscustomenchants`, `/aceadmin` | Central Admin Hub GUI (45-Slot) | `apexsions.admin` | `op` |
| `/ace enchants` | - | Katalog interaktif seluruh custom enchantments | `apexsions.admin` | `op` |
| `/ace create` | - | Interactive Item & Armor Set Builder | `apexsions.admin` | `op` |
| `/ace pricing` | - | Konfigurasi harga gacha, rate, dan multiplier | `apexsions.admin` | `op` |
| `/ace reload` | - | Memuat ulang konfigurasi sihir, grup, dan set | `apexsions.admin` | `op` |
| `/presets` | `/preset`, `/acepresets` | Membuka antarmuka preset armor & tool set tersimpan | `apexsions.admin` | `op` |
