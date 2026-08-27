# 💬 ApexsionsChat — Minecraft Paper 26.2 Dedicated Chat Plugin

**ApexsionsChat** adalah plugin obrolan dan komunitas mandiri untuk server Minecraft **Apexsions** (Paper 26.2 / Java 21+). Plugin ini mengonsumsi data domain dari **ApexsionsCore** via API publik (`ApexsionsCoreProvider.get()`) tanpa menyentuh database ApexsionsCore secara langsung.

---

## 🌟 Fitur Utama

- 🎨 **Format Chat Adventure MiniMessage & Channels**:
  - Saluran Obrolan:
    - `global` (`/global`, `/g`): Saluran publik seluruh server.
    - `kingdom` (`/kingdomchat`, `/kchat`): Hanya diterima oleh sesama anggota realm/kerajaan asal (Zenithar, Solterra, Sylvamoor).
    - `staff` (`/staffchat`, `/sc`): Saluran tertutup staf moderator.
  - Template: `[G][Lv. {level} {title}][{rank}][{kingdom}] {player} » {message}`
  - Dukungan penuh Hex Colors, Gradient, Hover event info pemain, dan Click event.

- ⚔️ **Item Showcase (`/showitem`, `[item]`, `[i]`, `[hand]`)**:
  - Menyematkan item yang sedang dipegang ke dalam pesan obrolan dengan warna dan nama item.
  - **Hover Tooltip**: Menampilkan lore, enchantments, dan kuantitas item.
  - **Click to Inspect GUI**: Mengklik item di obrolan akan membuka menu inspect inventori interaktif 27-slot (dilindungi server-side agar tidak dapat diambil oleh penonton).

- ⚡ **Chat Games Otomatis (Interval Acak 5–10 Menit)**:
  - **Unscramble Word**: Menyusun kembali kata acak dari daftar kata (`Apexsions`, `Minecraft`, `Sions`, `Zenithar`, `Solterra`, `Sylvamoor`).
  - **Quick Math**: Menghitung ekspresi matematika 1–3 operator (`+`, `-`, `*`, `/`) dengan urutan operasi matematika presisi dan hasil bilangan bulat.
  - **Atomic Winner Resolution**: Hanya 1 pemenang pertama yang mendapatkan hadiah (XP ApexsionsCore, Vault Money, item/command).

- 📢 **Scheduled Announcements**:
  - Siaran pesan berkala dengan interval configurable dan efek warna MiniMessage gradien.

- 🔔 **Smart Mentions**:
  - `@PlayerName`: Menyorot nama penerima, memunculkan notifikasi ActionBar, dan membunyikan nada chime.
  - `@all`: Notifikasi seluruh server dengan proteksi cooldown anti-spam.

- 🛡️ **Modular Moderation Pipeline**:
  - `SpamChecker`: Batas frekuensi pesan cepat (flood), deteksi pesan berulang (Levenshtein distance), batas huruf kapital (caps), dan simbol berlebihan.
  - `AdvertisementChecker`: Deteksi regex IP address, domain web, dan invite Discord (`discord.gg/*`) dengan whitelist domain resmi.
  - `ProfanityChecker` & `HateSpeechChecker`: Penyaringan kata kotor & ujaran kebencian dengan normalisasi leetspeak (`@->a`, `3->e`, `1->i`, dll) dan aksi `BLOCK` / `REPLACE (***)`.
  - Pencatatan log pelanggaran otomatis ke database.

- 📝 **Sistem Laporan Pemain (`/report <player> <reason>`) & In-Game Report GUI (`/reports`)**:
  - Penyimpanan database persisten (SQLite / PostgreSQL) dengan status `OPEN`, `REVIEWING`, `RESOLVED`, `DISMISSED`.
  - Notifikasi instan ke staf online saat ada laporan baru.
  - GUI moderasi interaktif bagi staf untuk meninjau detail dan mengubah status laporan.

- 📬 **Sistem Surat Offline Persisten (`/mail`) & Fitur "Read as Book"**:
  - `/mail send <player> <message>`: Mengirim surat ke pemain mana pun (online maupun offline).
  - `/mail`: GUI kotak surat interaktif dengan penanda surat baru/sudah dibaca.
  - **"Collect as Book Item"**: Mengambil surat fisik dalam bentuk buku (`WrittenBook`) dengan validasi kapasitas inventori pemain yang aman.

---

## 📌 Commands & Permissions

| Command | Aliases | Permission | Deskripsi |
|---|---|---|---|
| `/channel` | `/ch` | `apexsionschat.channel` | Ganti saluran obrolan aktif |
| `/global` | `/g` | `apexsionschat.channel.global` | Bicara langsung di saluran Global |
| `/kingdomchat` | `/kchat`, `/kc` | `apexsionschat.channel.kingdom` | Bicara langsung di saluran Kingdom |
| `/staffchat` | `/sc` | `apexsionschat.channel.staff` | Bicara di saluran Staf |
| `/showitem` | `/item`, `/i`, `/hand` | `apexsionschat.showitem` | Pamerkan item yang sedang dipegang ke chat |
| `/report` | - | `apexsionschat.report` | Laporkan pemain yang melanggar aturan |
| `/reports` | `/reportlist` | `apexsionschat.staff.reports` | Buka GUI manajemen laporan untuk staf |
| `/mail` | - | `apexsionschat.mail` | Buka mailbox atau kirim surat offline (`/mail send`) |
| `/apexsionschat` | `/chatadmin`, `/acchat` | `apexsionschat.admin` | Perintah admin (reload, trigger game, announce) |

---

## 🛠️ Kompilasi & Build

```powershell
$env:JAVA_HOME = "c:\Users\Friel\Documents\Rifqi Ariansyah\Apexsions\plugins\ApexsionsCore\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\apache-maven-3.9.9\bin\mvn.cmd clean package
```
Output fat jar final: `target/ApexsionsChat-1.0.0.jar` (dan `./ApexsionsChat-1.0.0.jar`).
