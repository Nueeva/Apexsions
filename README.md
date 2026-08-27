# Apexsions Plugin Suite — Minecraft 1.21.4 / Paper 26.2

Kumpulan plugin server Minecraft profesional berkinerja tinggi yang dirancang secara modular, terintegrasi penuh antar-plugin, dan siap digunakan untuk server **Apexsions**.

---

## 📦 Daftar Plugin

| Plugin | Versi | Deskripsi |
| :--- | :--- | :--- |
| **ApexsionsCore** | `1.0.0` | Sistem Kerajaan (*Zenithar*, *Solterra*, *Sylvamoor*), Leveling & Progresi (13 XP sources), BlueMap Territory Polygon, Pembatasan TPA EssentialsX (sesama kerajaan & wajib di dalam area wilayah). |
| **ApexsionsChat** | `1.0.0` | Sistem Obrolan Dedicated, MiniMessage format, Showcase item (`/showitem`), Surat Offline (`/mail`), Chat Games, Pengumuman, dan Moderasi & Anti-Spam Lapis Tiga (gaya AdvancedChat). |
| **ApexsionsEconomy** | `1.0.0` | Multi-Currency (`Rupiah`, `Diamond`), Transfer (`/pay`), Auction House (`/ah`) dengan Escrow Claim, dan Barter/Trade terintegrasi Kerajaan dengan Pajak Transportasi Lintas-Kerajaan. |
| **ApexsionsBattlepass** | `1.0.0` | Sistem BattlePass modern dengan Season, Daily/Weekly/Monthly Quests, Tingkatan Pass (`FREE`, `PREMIUM`, `ELITE`, `ULTIMATE`), Toko Rotasi, dan Editor GUI Admin 54-Slot (`/abp`). |

---

## 🛠️ Kompilasi & Build

Setiap plugin dapat dikompilasi menggunakan Apache Maven dan Java 21:

```powershell
# Kompilasi semua plugin
cd plugins/ApexsionsCore; mvn clean package; cd ../..
cd plugins/ApexsionsChat; mvn clean package; cd ../..
cd plugins/ApexsionsEconomy; mvn clean package; cd ../..
cd plugins/ApexsionsBattlepass; mvn clean package; cd ../..
```
