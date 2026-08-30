# ApexsionsChat — Comprehensive Technical Manual

Panduan teknis dan operasional lengkap untuk modul **`ApexsionsChat`** (Sistem Komunikasi Terpadu, Channel MiniMessage, ID-Card Profil Sosial, Showcase Item, Offline Mail, Chat Games, Pengumuman, Chat Settings GUI, dan Moderasi Lapis Tiga).

---

## 💬 1. Ikhtisar Modul & Arsitektur

`ApexsionsChat` mengelola seluruh arus percakapan, profil sosial pemain, preferensi notifikasi personal, resolusi tiket laporan staf, dan keamanan teks di server dengan perenderan modern Kyori Adventure & MiniMessage.

```
                         ┌────────────────────────┐
                         │     ApexsionsChat      │
                         │(Adventure & Formatter) │
                         └───────────┬────────────┘
                                     │
      ┌──────────────────────────────┼──────────────────────────────┐
      ▼                              ▼                              ▼
┌──────────────────┐       ┌───────────────────┐          ┌───────────────────┐
│ Channels & Social│       │  Interactive Hub  │          │  Security Lapis 3 │
│Global / Kingdom /│       │/showitem, /mail,  │          │Anti-Spam, Profane,│
│  Staff Channels  │       │Chat Settings GUI  │          │Anti-Ad & Exploits │
└──────────────────┘       └───────────────────┘          └───────────────────┘
```

---

## 📢 2. Channel Obrolan & Preferensi Pemain (`/channel settings`)

Pemain dapat beralih channel atau membuka GUI pengaturan personal:

- **`/channel` / `/channel settings`**: Membuka GUI 27-slot untuk:
  - Mengaktifkan/menonaktifkan suara notifikasi saat di-mention (`@Player`).
  - Mengganti channel aktif secara visual (Global ➜ Kingdom ➜ Staff).
  - Melakukan uji coba audio alerts.
- **`/channel profile <player>`**: Membuka ID-Card profil sosial pemain (Level, Kerajaan, Saldo, Rank, Ping, dan tombol aksi cepat `/msg`, `/trade`, `/mail send`, `/report`).

| Channel | Format Prefix | Perintah Cepat | Hak Akses | Deskripsi |
| :--- | :--- | :--- | :--- | :--- |
| **Global** | `[G] <player>:` | `/g <pesan>` / `/global` | `apexsionschat.channel.global` | Obrolan publik terbuka ke seluruh server |
| **Kingdom** | `[Zenithar] <player>:` | `/kc <pesan>` / `/kingdomchat` | `apexsionschat.channel.kingdom` | Obrolan privat khusus sesama anggota kerajaan |
| **Staff** | `[STAFF] <player>:` | `/sc <pesan>` / `/staffchat` | `apexsionschat.channel.staff` | Obrolan terenkripsi staf & moderator |

---

## 🛡️ 3. Sistem Keamanan & Moderasi Lapis Tiga

1. **Lapis 1 — Rate Limiting & Anti-Spam**:
   - Sliding window timer (maksimal 3 pesan per 2 detik).
   - Deteksi pesan berulang menggunakan algoritma Levenshtein Distance $\ge 80\%$.
2. **Lapis 2 — Anti-Profanity & Toksisitas**:
   - Filter kata-kata kotor multi-bahasa dengan substitusi karakter angka/simbol.
3. **Lapis 3 — Anti-Ad & Exploit Blocker**:
   - Blocker alamat IP server liar, domain ilegal, dan exploit syntax (JNDI, template injections).
   - **Staff Reports Desk (`/reports`)**: Antarmuka interaktif staf 54-slot untuk meninjau laporan pemain, teleportasi ke posisi terlapor, dan eksekusi sanksi 1-klik (Mute 10m, Warn, Kick, Ban 1h).

---

## 📜 4. Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/channel [settings]` | `/ch` | Mengganti channel obrolan atau buka preferensi GUI | `apexsionschat.channel` | `true` |
| `/channel profile <p>`| - | Membuka antarmuka profil sosial pemain (ID-Card) | `apexsionschat.channel` | `true` |
| `/g [pesan]` | `/global` | Berbicara di obrolan Global | `apexsionschat.channel.global` | `true` |
| `/kc [pesan]` | `/kchat`, `/kingdomchat` | Berbicara di obrolan Kerajaan | `apexsionschat.channel.kingdom` | `true` |
| `/sc [pesan]` | `/staffchat` | Berbicara di obrolan Staf | `apexsionschat.channel.staff` | `op` |
| `/showitem` | `/item`, `/i`, `/hand` | Memamerkan item di tangan ke obrolan | `apexsionschat.showitem` | `true` |
| `/mail send <p> <msg>`| - | Mengirim surat offline ke pemain | `apexsionschat.mail` | `true` |
| `/mail read` | `/inbox` | Membaca kotak masuk surat offline | `apexsionschat.mail` | `true` |
| `/mail clear` | - | Menghapus seluruh pesan di kotak masuk | `apexsionschat.mail` | `true` |
| `/report <p> <alasan>`| - | Melaporkan pemain yang melanggar aturan | `apexsionschat.report` | `true` |
| `/reports` | `/reportlist` | Membuka antarmuka resolusi laporan meja staf 54-slot | `apexsionschat.staff.reports` | `op` |
| `/apexsionschat reload`| `/chatadmin reload`, `/acchat reload` | Memuat ulang konfigurasi chat, games & pengumuman | `apexsionschat.admin` | `op` |
| `/apexsionschat mute` | `/acchat lock` | Toggle kunci/mute obrolan global server | `apexsionschat.admin` | `op` |
| `/apexsionschat clear`| - | Membersihkan tampilan layar obrolan pemain | `apexsionschat.admin` | `op` |
| `/apexsionschat game start`| - | Memulai paksa chat game mini-event | `apexsionschat.admin` | `op` |
| `/apexsionschat announce`| - | Menyiarkan pengumuman berkala berikutnya instan | `apexsionschat.admin` | `op` |

---

## 🔐 Matriks Izin Khusus & Bypass

| Permission | Deskripsi | Default |
| :--- | :--- | :---: |
| `apexsionschat.mention` | Mengizinkan penggunaan mention `@Player` dengan audio ping | `true` |
| `apexsionschat.mention.all` | Mengizinkan mention `@all` tanpa cooldown | `op` |
| `apexsionschat.staff.alerts` | Menerima notifikasi alert moderasi real-time | `op` |
| `apexsionschat.staff.mutebypass` | Mengizinkan berbicara saat global chat dikunci | `op` |
| `apexsionschat.bypass.all` | Melewati seluruh filter moderasi secara total | `false` |
| `apexsionschat.bypass.spam` | Melewati filter spam & rate limit | `false` |
| `apexsionschat.bypass.profanity`| Melewati filter kata kotor | `false` |
| `apexsionschat.bypass.advertising`| Melewati filter link & promosi server | `false` |
