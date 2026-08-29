# ApexsionsMedia — Comprehensive Technical Manual

Panduan teknis dan operasional lengkap untuk modul **`ApexsionsMedia`** (Sistem Render Banner & Logo Gambar Multi-Tile Async, Raytrace Line-of-Sight Hover Glowing, Penempatan Fleksibel, dan Konfirmasi Aksi Tautan Web / Salin Clipboard).

---

## 🖼️ 1. Ikhtisar Modul & Arsitektur

`ApexsionsMedia` adalah modul ke-6 dari ekosistem Apexsions yang memungkinkan administrator dan builder memasang logo, banner promosi, papan informasi, dan grafis berkualitas tinggi langsung di dalam dunia Minecraft tanpa memerlukan mod klien (100% kompatibel dengan Vanilla Java & Bedrock Edition via Geyser/Floodgate).

```
                             ┌────────────────────────┐
                             │     ApexsionsMedia     │
                             │ (Async Image Pipeline) │
                             └───────────┬────────────┘
                                         │
        ┌────────────────────────────────┼────────────────────────────────┐
        ▼                                ▼                                ▼
┌──────────────────┐           ┌───────────────────┐            ┌───────────────────┐
│ MapPalette Engine│           │ Raytrace & Hover  │            │ Interactive URLs  │
│128x128 Tile Slice│           │Line-of-sight Glow │            │MiniMessage / GUI  │
│PNG / JPG / URLs  │           │Actionbar Tooltips │            │Open URL & Copy Clp│
└──────────────────┘           └───────────────────┘            └───────────────────┘
```

---

## 🎨 2. Mesin Raster & Rendering Gambar Async

- **Dukungan Format**: PNG, JPG, JPEG dari folder lokal (`plugins/ApexsionsMedia/images/`) atau URL langsung (`https://...`).
- **Deteksi Dimensi & Rasio Otomatis**: Jika lebar dan tinggi tidak disertakan dalam perintah, plugin otomatis mendeteksi resolusi gambar asli dan menyesuaikan ubin secara proporsional.
- **Contextual MapRenderer & Auto-Broadcast**:
  - Menggunakan renderer peta kontekstual per-pemain sehingga saat pemain baru login atau berpindah dunia, data visual peta otomatis dikirimkan (`player.sendMap`) seketika tanpa visual kosong/goib.
- **Caffeine Cache Integration**:
  - Buffer raster ubin disimpan di memori RAM sehingga tidak perlu mengunduh/merender ulang gambar yang sama saat diduplikasi ke banyak lokasi.

---

## ✨ 3. Raytrace Hover Glowing & Deteksi Pandangan

- **Line-of-Sight Raytracing (`MediaRaytraceService`)**:
  - Bekerja secara asinkron setiap **3 tick** (~6.6 kali/detik) mendeteksi jika crosshair pemain menatap ke arah bounding box banner dalam radius hingga **7 blok**.
- **Efek Partikel Border**:
  - Menampilkan partikel bercahaya (`GLOW`, `END_ROD`, `DUST`) di sekeliling sudut dan batas banner saat pemain mengarahkan pandangannya ke banner tersebut.
- **Actionbar Tooltip Real-time**:
  - Menampilkan prompt teks halus di layar aksi bawah: `<gradient:#f39c12:#f1c40f><bold>✨ KLIK KANAN</bold></gradient> <gray>untuk membuka link informasi!</gray>`.

---

## 🔗 4. Interaksi Tautan & UX Konfirmasi

Pemain dapat mengklik kanan pada banner untuk memicu aksi URL dengan 3 pilihan mode:

1. **`CHAT_PROMPT` (Bawaan)**:
   - Mengirim pesan chat interaktif berbasis MiniMessage:
     - `[🌐 BUKA URL]`: Menggunakan event `ClickEvent.openUrl(...)` sehingga peramban web pemain langsung membuka halaman tujuan.
     - `[📋 SALIN LINK]`: Menggunakan event `ClickEvent.copyToClipboard(...)` untuk menyalin URL ke clipboard pemain dalam 1 klik.
2. **`GUI_CONFIRM`**:
   - Membuka menu inventaris 27-slot khusus (`MediaConfirmGUI`) dengan 3 tombol besar:
     - Tombol Hijau: "Buka di Browser"
     - Tombol Biru: "Salin ke Clipboard"
     - Tombol Merah: "Batal / Tutup"
3. **`DIRECT_URL`**:
   - Mengirim tautan langsung tanpa tombol tambahan.

---

## 📜 5. Matriks Perintah & Hak Akses (Permissions)

| Perintah | Alias | Deskripsi | Hak Akses (Permission) | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/media create <id> <file/url> [w] [h] [link] [mode]` | `/banner create` | Memasang banner baru (URL & ukuran opsional) | `apexsionsmedia.admin` | `op` |
| `/media admin` | `/media gui` | Membuka Interactive 54-Slot Banner Management GUI | `apexsionsmedia.admin` | `op` |
| `/media place <id>` | `/media apply`, `/media paste` | Memasang/memindahkan banner yang ada ke lokasi bidikan baru | `apexsionsmedia.admin` | `op` |
| `/media copy <idAsal> <idBaru>` | `/media clone` | Menduplikasi konfigurasi banner ke target baru | `apexsionsmedia.admin` | `op` |
| `/media setlink <id> [link/none] [mode]` | `/banner setlink` | Mengatur, mengubah, atau menghapus tautan URL banner | `apexsionsmedia.admin` | `op` |
| `/media resize <id> <w> <h>` | `/banner resize` | Mengubah ukuran dimensi ubin banner (1-10) | `apexsionsmedia.admin` | `op` |
| `/media delete <id>` | `/banner delete` | Menghapus banner dan membersihkan entity | `apexsionsmedia.admin` | `op` |
| `/media list` | `/banner list` | Menampilkan daftar seluruh banner aktif | `apexsionsmedia.admin` | `op` |
| `/media reload` | `/banner reload` | Memuat ulang konfigurasi dan render ulang seluruh banner | `apexsionsmedia.admin` | `op` |

---

## ⚙️ 6. Contoh Penggunaan Cepat & Fleksibel

### A. Memasang Banner Sederhana (Tanpa URL, Ukuran Otomatis/Default):
```bash
/media create logo logo.png
```

### B. Memasang Banner dengan Ukuran Tertentu Saja (Tanpa URL):
```bash
/media create serverbanner https://example.com/banner.png 3 2
```

### C. Memasang Banner Lengkap dengan Tautan URL Discord:
```bash
/media create discord https://example.com/discord.png 3 2 https://discord.gg/apexsions CHAT_PROMPT
```

### D. Mengaplikasikan Banner yang Sudah Dibuat ke Tempat Lain:
Arahkan crosshair ke dinding tempat baru, lalu ketik:
```bash
# Untuk memindahkan banner yang sudah ada:
/media place discord

# Untuk membuat duplikat banner di tempat baru:
/media copy discord discord_spawn2
```
