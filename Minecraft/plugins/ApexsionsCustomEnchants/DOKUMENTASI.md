# Dokumentasi Lengkap ApexsionsCustomEnchants

Panduan teknis resmi modul **`ApexsionsCustomEnchants`** untuk sistem sihir perlengkapan kustom, 182 custom enchantments lintas 7 tier, Enchanter Gacha dual-currency, Toko Buku Sihir spesifik, item utilitas magis (Scrolls & Dust), serta Interactive Armor Set Builder di **Apexsions — The Peak Civilizations**.

> **Game Server Domain:** `apexsions.com:32348` (Java & Bedrock)
> **Web Platform Domain:** `https://web.apexsions.com`

---

## 📂 Struktur Direktori & Konfigurasi YAML Modular

```
plugins/ApexsionsCustomEnchants/
├── config.yml            <-- Opsi global, batas slot sihir per item, harga gacha bawaan
├── plugin.yml            <-- Deklarasi commands, permissions, metadata
├── enchants/             <-- Definisi 182 custom enchantment YAML
│   ├── simple/           <-- Tier 1: Efek utilitas dasar & minor buff
│   ├── unique/           <-- Tier 2: Peningkatan mobilitas & farming
│   ├── elite/            <-- Tier 3: Kemampuan tempur menengah
│   ├── ultimate/         <-- Tier 4: Efek serangan & pertahanan tinggi
│   ├── legendary/        <-- Tier 5: Efek magis elit perang kerajaan
│   ├── fabled/           <-- Tier 6: Sihir kuno langka berkekuatan besar
│   └── master/           <-- Tier 7: Mahakarya sihir kedaulatan tertinggi
├── groups/               <-- Konfigurasi 7 tier grup sihir (Warna, Nama, Biaya Gacha)
│   └── groups.yml
└── presets/              <-- Penyimpanan set perlengkapan kustom hasil kreasi admin
    └── sets.yml
```

---

## ⚡ Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/ce` | `/enchanter`, `/customenchants` | Membuka Enchanter Gacha Dual-Currency GUI | `apexsions.customenchants.use` | `true` |
| `/ce shop` | `/ce toko` | Membuka Toko Buku Sihir Spesifik 54-Slot | `apexsions.customenchants.use` | `true` |
| `/ce tinkerer` | - | Membuka antarmuka Tinkerer daur ulang sihir | `apexsions.customenchants.use` | `true` |
| `/ace` | `/apexsionscustomenchants`, `/aceadmin` | Central Admin Hub GUI (45-Slot) | `apexsions.admin` | `op` |
| `/ace enchants` | - | Katalog interaktif seluruh custom enchantments | `apexsions.admin` | `op` |
| `/ace create` | - | Interactive Item & Armor Set Builder | `apexsions.admin` | `op` |
| `/ace pricing` | - | Konfigurasi harga gacha, rate, dan multiplier | `apexsions.admin` | `op` |
| `/ace reload` | - | Memuat ulang konfigurasi sihir, grup, dan set | `apexsions.admin` | `op` |
| `/presets` | `/preset`, `/acepresets` | Membuka menu preset armor & tool set tersimpan | `apexsions.admin` | `op` |

---

## 🔮 1. Tujuh Tingkatan Kelompok Sihir (7 Enchantment Tiers)

1. **Simple (`#aaaaaa` / Abu-abu):** Efek utilitas pemula, peningkatan kecepatan penggalian ringan, regenerasi lapar perlahan.
2. **Unique (`#55ff55` / Hijau):** Peningkatan mobilitas, daya tahan armor tambahan, peluang ganda panen pertanian.
3. **Elite (`#00aaaa` / Sian Gelap):** Tambahan proteksi elemen, serangan racun ringan, daya pantul panah.
4. **Ultimate (`#ff5555` / Merah Terang):** Pengurang damage masuk, serangan critical masif, efek knockback perlawanan.
5. **Legendary (`#ffaa00` / Oranye Emas):** Efek vampirisme (lifesteal), serangan ledakan suci, imunitas racun sesaat.
6. **Fabled (`#aa00aa` / Ungu Mistis):** Perlindungan kematian darurat, sambaran petir multi-target, penyerapan energi lawan.
7. **Master (`#ff55ff` / Magenta Kosmik):** Mahakarya sihir kuno, distorsi gravitasi, regenerasi kesehatan kilat saat terdesak.

---

## 📜 2. Item Utilitas Magis & Bahan Habis Pakai

- **Buku Sihir Kustom (`EnchantBookManager`):** Memuat informasi nama sihir, tier, tingkat level (I–X), *Success Rate* ($0\% - 100\%$), dan *Destroy Rate* ($0\% - 100\%$).
- **Magic Dust:** Item konsumsi yang dapat dijatuhkan ke atas buku sihir untuk meningkatkan *Success Rate* secara bertahap hingga $100\%$.
- **Mystery Dust:** Debu sisa yang diperoleh saat membuka kantong debu sihir misterius atau saat pemurnian buku gagal.
- **White Scroll:** Item perlindungan legendaris. Jika diaplikasikan pada senjata/armor, mencegah hancurnya item tersebut apabila pemasangan buku sihir mengalami kegagalan fatal (*Destroyed*).
- **Black Scroll:** Item ekstraksi. Mengambil satu sihir kustom acak dari perlengkapan dan mengubahnya kembali menjadi buku sihir dengan *Success Rate* $100\%$.

---

## 🛡️ 3. Interactive Armor Set Builder & Full-Set Bonuses

Melalui perintah `/ace create` atau `/presets`:
- Admin dapat menyusun paket perlengkapan terpadu (*Helmet*, *Chestplate*, *Leggings*, *Boots*, dan *Weapon*).
- **Full-Set Bonus Engine**: Mengaktifkan efek pasif unik saat pemain mengenakan seluruh 4 bagian armor dari set yang sama (misal: *Celestial Paladin Set* memberikan efek resistensi api permanen dan aura regenerasi).
- **Sinkronisasi ID & NBT**: Menjamin metadata item tersimpan rapi dan tidak bentrok dengan item vanilla standar.

---

## 🧩 4. Integrasi Programatik Java (`ApexsionsCustomEnchantsPlugin`)

Pengembang plugin lain dapat berinteraksi dengan sistem sihir melalui instance plugin:

```java
ApexsionsCustomEnchantsPlugin ce = ApexsionsCustomEnchantsPlugin.getInstance();
if (ce != null) {
    EnchantmentRegistry registry = ce.getEnchantmentRegistry();
    GroupRegistry groups = ce.getGroupRegistry();
    EnchantBookManager bookMgr = ce.getEnchantBookManager();
    // Buat buku sihir kustom secara dinamis
    ItemStack book = bookMgr.createBook("lifesteal", 3, 85, 15);
}
```
