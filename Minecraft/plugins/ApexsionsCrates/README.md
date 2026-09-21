# ApexsionsCrates 📦

> **Plugin Suite:** Apexsions  
> **Brand:** `Apexsions`  
> **Tagline:** `The Peak Civilizations`  
> **Target Runtime:** Paper 26.2 (Minecraft 26.2, Java 21 LTS)

Modul peti hadiah dan kunci resmi untuk ekosistem **Apexsions — The Peak Civilizations**. Modul ini menghadirkan sistem gacha hadiah berbasis probabilitas berbobot, animasi pembukaan paket virtual tanpa lag, toko pembelian kunci (*Crate Key Shop*) dual-currency, sistem progresi milestone/pity, dan integrasi penuh dengan Master Admin Hub ApexsionsCore.

---

## 🌟 Fitur Utama

- **Toko Kunci Dual-Currency (`/crateshop`):** Pembelian kunci peti langsung menggunakan saldo `Rupiah` atau `Diamond` secara atomic via `ApexsionsEconomyAPI`.
- **Animasi Pembukaan Berbasis Paket:** Menggunakan PacketEvents dan ProtocolLib untuk menampilkan animasi pembukaan peti visual tanpa mengorbankan performa main thread.
- **Milestone Progression & Pity Engine:** Melacak jumlah pembukaan peti pemain secara persisten dan memberikan hadiah spesial terjamin saat mencapai target milestone.
- **Pratinjau Hadiah Transparan (`/crate preview`):** Pemain dapat memeriksa seluruh kemungkinan hadiah, bobot peluang, dan tier sebelum membuka peti.
- **Editor Interaktif In-Game (`/crate editor`):** Antarmuka GUI lengkap untuk menambah, menyunting, dan mengatur probabilitas hadiah tanpa perlu mengubah berkas YAML secara manual.
- **Integrasi Master Admin Hub:** Terdaftar secara otomatis di `/admingui` ApexsionsCore untuk pengelolaan terpusat.

---

## ⌨️ Daftar Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/crateshop` | `/keyshop`, `/cratekeyshop` | Membuka Toko Kunci Peti Dual-Currency GUI | `apexsionscrates.command.menu` | `true` |
| `/crate` | `/crates` | Menampilkan antarmuka daftar peti hadiah dan milestone | `apexsionscrates.command.menu` | `true` |
| `/crate open <id>` | - | Membuka peti hadiah tertentu | `apexsionscrates.command.open` | `true` |
| `/crate preview <id>` | - | Pratinjau daftar hadiah dan peluang peti | `apexsionscrates.command.preview` | `true` |
| `/crate key <give\|take\|set\|show> <p> <crate> <amt>` | - | Mengelola jumlah kunci peti pemain | `apexsionscrates.command.key` | `op` |
| `/crate give <p> <crate> [amt]` | - | Memberikan item/kunci peti kepada pemain | `apexsionscrates.command.give` | `op` |
| `/crate editor` | - | Editor interaktif peti, hadiah, dan milestone | `apexsionscrates.command.editor` | `op` |
| `/crate reload` | - | Memuat ulang seluruh konfigurasi peti | `apexsionscrates.command.reload` | `op` |

---

## 🔗 Dependensi
- **NightCore** (Required runtime library)
- **ApexsionsCore** (Softdepend / Master Admin Hub)
- **ApexsionsEconomy** (Softdepend / Dual-currency key shop)
- **PlaceholderAPI** (Softdepend / Placeholder expansion)
- **ProtocolLib** & **PacketEvents** (Animasi & packet handling)
