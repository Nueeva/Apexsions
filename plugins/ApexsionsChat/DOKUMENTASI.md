# Dokumentasi Lengkap ApexsionsChat

Panduan teknis modul `ApexsionsChat` untuk pengaturan channel, moderasi keamanan, dan sistem laporan.

---

## 📂 Struktur File YAML
- `config.yml`: Pengaturan umum database log chat dan durasi pesan.
- `channels.yml`: Definisi format MiniMessage channel Global, Kingdom, dan Staff.
- `moderation.yml`: Aturan Anti-Spam, Profanity, Anti-Ad, Exploit regex, bypass perms, dan notifikasi staf.
- `games.yml`: Pengaturan jenis game chat (Matematika & Tebak Kata) dan reward pemenang.
- `announcements.yml`: Daftar pesan pengumuman periodik server.
- `mail.yml`: Pengaturan kapasitas dan kadaluarsa surat offline.

---

## 🛡️ Hak Akses & Permission
| Permission | Deskripsi | Default |
| :--- | :--- | :--- |
| `apexsionschat.channel.global` | Menggunakan obrolan global | `true` |
| `apexsionschat.channel.kingdom` | Menggunakan obrolan kerajaan | `true` |
| `apexsionschat.channel.staff` | Menggunakan obrolan staf | `op` |
| `apexsionschat.showitem` | Menggunakan `/showitem` | `true` |
| `apexsionschat.report` | Mengirim laporan `/report` | `true` |
| `apexsionschat.staff.reports` | Membuka daftar laporan `/reports` | `op` |
| `apexsionschat.staff.alerts` | Menerima alert moderasi chat real-time | `op` |
| `apexsionschat.staff.mutebypass` | Berbicara saat global chat di-mute | `op` |
| `apexsionschat.bypass.all` | Melewati seluruh filter moderasi | `false` |
| `apexsionschat.admin` | Kontrol admin chat (`/apexsionschat`) | `op` |

---

## ⚡ Chat Games & Integrasi Level ApexsionsCore
Sistem chat games berjalan otomatis setiap 5–10 menit sekali dengan dua jenis permainan:
1. **Word Unscramble (`unscramble`)**: Menyusun huruf acak menjadi kata valid kerajaan.
2. **Quick Math (`math`)**: Menghitung operasi matematika cepat.

### Mekanisme Hadiah & Integrasi EXP:
* Hadiah EXP dikirim langsung ke `ApexsionsCoreAPI` (`XpSource.CHAT_GAME_WIN`) via `ApexsionsCoreHook`.
* Mengalir ke sistem level kerajaan (`LevelManager`), memicu `KingdomXpGainEvent`, action bar EXP gain, dan animasi level-up otomatis jika XP mencukupi.
* Pengaturan nilai EXP per game dapat diatur di `games.yml` (`reward-xp: 150`), dengan fallback ke `games.rewards.xp.amount`.
* Kategori **Chat Games** juga otomatis terdaftar di direktori Panduan XP `/xpguide` pada `ApexsionsCore`.
