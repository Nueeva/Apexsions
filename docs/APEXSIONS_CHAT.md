# ApexsionsChat — Comprehensive Technical Manual

Panduan teknis dan operasional lengkap untuk modul **`ApexsionsChat`** (Sistem Komunikasi Terpadu, Channel MiniMessage, Showcase Item, Offline Mail, Chat Games, Pengumuman, dan Moderasi Lapis Tiga).

---

## 💬 1. Ikhtisar Modul & Arsitektur

`ApexsionsChat` mengelola seluruh arus percakapan dan keamanan teks di server dengan perenderan modern Kyori Adventure & MiniMessage.

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
│  Staff Channels  │       │Chat Games, Announce│         │Anti-Ad & Exploits │
└──────────────────┘       └───────────────────┘          └───────────────────┘
```

---

## 📢 2. Channel Obrolan Terpadu

Pemain dapat beralih atau mengirim pesan langsung ke channel tertentu:

| Channel | Format Prefix | Perintah Cepat | Hak Akses | Deskripsi |
| :--- | :--- | :--- | :--- | :--- |
| **Global** | `[G] <player>:` | `/g <pesan>` | `apexsionschat.channel.global` | Obrolan publik terbuka ke seluruh server |
| **Kingdom** | `[Zenithar] <player>:` | `/kc <pesan>` | `apexsionschat.channel.kingdom` | Obrolan privat khusus sesama anggota kerajaan |
| **Staff** | `[STAFF] <player>:` | `/sc <pesan>` | `apexsionschat.channel.staff` | Obrolan terenkripsi staf & moderator |

---

## 🛡️ 3. Sistem Keamanan & Moderasi Lapis Tiga (Advanced Security)

Mencegah serangan spam, toksisitas kata-kata kasar, promosi link/IP liar, dan exploit crash server:

1. **Lapis 1 — Rate Limiting & Anti-Spam**:
   - Sliding window timer (maksimal 3 pesan per 2 detik).
   - Deteksi pesan berulang (*near-duplicate*) menggunakan algoritma Levenshtein Distance $\ge 80\%$.
   - Auto temp-mute jika pemain terus melakukan spam cepat.
2. **Lapis 2 — Anti-Profanity & Toksisitas**:
   - Filter kata-kata kotor multi-bahasa dengan substitusi karakter angka (`4` $\to$ `a`, `1` $\to$ `i`, `0` $\to$ `o`).
   - Peringatan instan ke pemain dan sensor otomatis.
3. **Lapis 3 — Anti-Ad & Exploit Blocker**:
   - Regex blocker untuk alamat IP server (`\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}`), domain web, dan tautan Discord non-whitelist.
   - Deteksi exploit syntax (seperti JNDI lookup `${jndi:...}` atau token leaks).
   - **Staff Alerts**: Staf dengan izin `apexsionschat.staff.alerts` menerima notifikasi merah real-time beserta audio alert saat ada pesan mencurigakan.

---

## 🎁 4. Fitur Interaktif & Komunitas

- **`/showitem`**: Menampilkan item di tangan dengan hover tooltips interaktif (nama item, enchantments, lore, potion effects) tanpa kebocoran syntax tag.
- **Offline `/mail`**: Sistem kotak surat untuk mengirim dan membaca pesan pemain lain bahkan saat mereka sedang offline.
- **Chat Games Otomatis**: Mini game tebak matematika dan susun kata teracak (*unscramble*) berhadiah saldo Rupiah/Diamond.
- **Pengumuman Berkala**: Siaran terjadwal MiniMessage dengan teks animasi dan tautan web server.

---

## 📜 5. Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/channel <g\|k\|s>` | `/ch` | Mengganti channel obrolan aktif | `apexsionschat.channel` | `true` |
| `/g [pesan]` | `/global` | Berbicara di obrolan global | `apexsionschat.channel.global` | `true` |
| `/kc [pesan]` | `/kingdomchat` | Berbicara di obrolan kerajaan | `apexsionschat.channel.kingdom` | `true` |
| `/sc [pesan]` | `/staffchat` | Berbicara di obrolan staf | `apexsionschat.channel.staff` | `op` |
| `/showitem` | `/item`, `/hand` | Memamerkan item di tangan ke obrolan | `apexsionschat.showitem` | `true` |
| `/mail send <p> <msg>`| - | Mengirim surat offline ke pemain | `apexsionschat.mail` | `true` |
| `/mail read` | `/inbox` | Membaca kotak masuk surat | `apexsionschat.mail` | `true` |
| `/report <p> <alasan>`| - | Melaporkan pemain yang melanggar aturan | `apexsionschat.report` | `true` |
| `/reports` | `/reportlist` | Membuka antarmuka resolusi laporan staf | `apexsionschat.staff.reports` | `op` |
| `/apexsionschat mute` | `/chat mute` | Mematikan/mengunci obrolan global server | `apexsionschat.admin` | `op` |
| `/apexsionschat clear`| `/chat clear` | Membersihkan tampilan obrolan pemain | `apexsionschat.admin` | `op` |
| `/apexsionschat reload`| `/acchat reload`| Memuat ulang file konfigurasi modul chat | `apexsionschat.admin` | `op` |
