# ApexsionsBattlepass — Paper 26.2 (Minecraft 26.2)

Plugin BattlePass modern dengan sistem Season, Daily/Weekly/Monthly Quests, tingkatan Pass (`Citizen`, `Sio`, `Exsio`), Toko Rotasi (*Dynamic Shop*), dan Editor GUI Admin 54-Slot (`/abp`) untuk server **Apexsions**.

> **Game Server Domain:** `apexsions.com:32348` (Java & Bedrock)
> **Web Platform Domain:** `https://web.apexsions.com`

---

## 🌟 Fitur Utama
- **Sistem Quests Komprehensif**: Daily, Weekly, Special Week, dan Monthly Quests dengan pelacakan progress real-time.
- **Tingkatan Pass Fleksibel**: Pewarisan reward otomatis untuk pemegang pass `Sio` dan `Exsio` (mewarisi hak klaim tier di bawahnya).
- **Dynamic Shop Rotasi**: Toko item dengan probabilitas Rarity dan batas beli per pemain.
- **Editor GUI Admin Lengkap (`/abp`)**: Pengelolaan reward, item shop, kategori, dan probabilitas 100% via GUI tanpa perlu edit YAML manual.
- **Top 100 Leaderboards**: Tampilan piramida ranking level BattlePass 10 halaman.

---

## 🛠️ Kompilasi & Build
```powershell
mvn clean package
```
Output: `target/ApexsionsBattlepass-1.0.0.jar`
