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
