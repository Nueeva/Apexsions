# Apexsions Plugin Suite — Minecraft 1.21.4 / Paper 26.2

Kumpulan plugin server Minecraft profesional berkinerja tinggi yang dirancang secara modular, terintegrasi penuh antar-plugin, dan siap digunakan untuk ekosistem server **Apexsions**.

---

## 📦 Daftar Plugin Suite

| Plugin | Versi | Deskripsi & Fitur Utama |
| :--- | :---: | :--- |
| **`ApexsionsCore`** | `1.0.0` | Otoritas Wilayah Kerajaan (*Zenithar*, *Solterra*, *Sylvamoor*), Sistem Progresi & Leveling (13 XP sources), Terintegrasi Poligon BlueMap, Sistem **Kingdom-Bounded `/rtp`** (mendarat aman hanya di wilayah kerajaan pemain), dan Pembatasan TPA EssentialsX teritorial. |
| **`ApexsionsChat`** | `1.0.0` | Sistem Obrolan Terpadu MiniMessage, Channel (*Global*, *Kingdom*, *Staff*), Pamer Item (`/showitem`), Surat Offline (`/mail`), Chat Games interaktif, Pengumuman berkala, serta Moderasi Anti-Spam/Profanity/Exploit Lapis Tiga. |
| **`ApexsionsEconomy`** | `1.0.0` | Multi-Currency (`Rupiah`, `Diamond`), Transfer (`/pay`), Pasar Lelang (`/ah`) dengan Escrow Claim, dan Sistem Barter/Trade 12-Slot dengan Toggle `/trade toggle` serta Pajak Transportasi Lintas-Kerajaan. |
| **`ApexsionsBattlepass`** | `1.0.0` | Sistem BattlePass Modern 200 Level (100 XP fixed/level), Pool Quests (42 Daily, 120 Weekly, 50 Monthly), Tingkatan Pass (`FREE`, `PREMIUM`, `PREMIUM+`, `ULTIMATE`), Toko Rotasi, Auto-Fill GUI Rewards, dan Editor Admin GUI 54-Slot (`/abp`). |
| **`ApexsionsShop`** | `1.0.0` | Pasar & Toko Dinamis 6 Kategori (`Blocks`, `Makanan`, `Pertanian`, `Ore`, `Mob Drops`, `Dyes`), Rasio Jual **20%**, Formula Dinamis Cuaca & Bioma Kerajaan, Pajak Wilayah 10%, UI Ramah Bedrock/Touchscreen, dan GUI Jual Cepat 45-Slot (`/sell`). |

---

## 🛠️ Kompilasi & Build Otomatis

Seluruh 5 plugin dapat dikompilasi dan dikemas secara serentak menggunakan build script PowerShell bawaan (Java 21 LTS & Paper API 1.21.4):

```powershell
# Jalankan build script untuk seluruh plugin
.\build.ps1
```

Output file `.jar` siap pasang akan otomatis tersedia di:
- `build/libs/ApexsionsCore-1.0.0.jar`
- `build/libs/ApexsionsChat-1.0.0.jar`
- `build/libs/ApexsionsEconomy-1.0.0.jar`
- `build/libs/ApexsionsBattlepass-1.0.0.jar`
- `build/libs/ApexsionsShop-1.0.0.jar`

---

## 🚀 Alur Rilis & Git Repository

- **Repository Utama:** `https://github.com/Nueeva/Apexsions.git`
- **Branch Utama:** `main`
- Setiap perubahan kode, dokumentasi, dan binary `.jar` selalu disinkronkan dan di-push ke branch utama.
