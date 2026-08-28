# ApexsionsChat — Comprehensive Technical Manual

Panduan teknis dan operasional lengkap untuk modul **`ApexsionsChat`** (Sistem Komunikasi Terpadu, Channel MiniMessage, Showcase Item, Offline Mail, Chat Games, Pengumuman, Chat Settings GUI, dan Moderasi Lapis Tiga).

---

## 💬 1. Ikhtisar Modul & Arsitektur

`ApexsionsChat` mengelola seluruh arus percakapan, preferensi notifikasi personal pemain, resolusi tiket laporan staf, dan keamanan teks di server dengan perenderan modern Kyori Adventure & MiniMessage.

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

| Channel | Format Prefix | Perintah Cepat | Hak Akses | Deskripsi |
| :--- | :--- | :--- | :--- | :--- |
| **Global** | `[G] <player>:` | `/g <pesan>` | `apexsionschat.channel.global` | Obrolan publik terbuka ke seluruh server |
| **Kingdom** | `[Zenithar] <player>:` | `/kc <pesan>` | `apexsionschat.channel.kingdom` | Obrolan privat khusus sesama anggota kerajaan |
| **Staff** | `[STAFF] <player>:` | `/sc <pesan>` | `apexsionschat.channel.staff` | Obrolan terenkripsi staf & moderator |

---

## 🛡️ 3. Sistem Keamanan & Moderasi Lapis Tiga

1. **Lapis 1 — Rate Limiting & Anti-Spam**:
   - Sliding window timer (maksimal 3 pesan per 2 detik).
   - Deteksi pesan berulang menggunakan algoritma Levenshtein Distance $\ge 80\%$.
2. **Lapis 2 — Anti-Profanity & Toksisitas**:
   - Filter kata-kata kotor multi-bahasa dengan substitusi karakter angka.
3. **Lapis 3 — Anti-Ad & Exploit Blocker**:
   - Blocker alamat IP server liar dan exploit syntax.
   - **Staff Reports GUI (`/reports`)**: Antarmuka interaktif staf untuk meninjau laporan, menandai "Reviewing", "Resolved", atau "Dismissed".

---

## 📜 4. Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/channel [settings]` | `/ch` | Mengganti channel atau buka Chat Settings GUI | `apexsionschat.channel` | `true` |
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
