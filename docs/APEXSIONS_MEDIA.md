# ApexsionsMedia — Comprehensive Technical Manual

Panduan teknis dan operasional lengkap untuk modul **`ApexsionsMedia`** (Sistem Render Banner & Logo Gambar Multi-Tile Async, Raytrace Line-of-Sight Hover Glowing, dan Konfirmasi Aksi Tautan Web / Salin Clipboard).

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
- **Algoritma Palet Warna Cepat (`MapPalette`)**:
  - Konversi warna 15-bit RGB ke byte palet warna peta Minecraft bawaan dengan bobot persepsi mata manusia Euclidean ($0.299R^2 + 0.587G^2 + 0.114B^2$).
  - Multi-tile slicing otomatis: Gambar diperkecil dan dipotong menjadi matriks ubin $128 \times 128$ piksel.
- **Caffeine Cache Integration**:
  - Buffer raster ubin disimpan di memori RAM sehingga tidak perlu mengunduh/merender ulang gambar yang sama.

---

## ✨ 3. Raytrace Hover Glowing & Deteksi Pandangan

- **Line-of-Sight Raytracing (`MediaRaytraceService`)**:
  - Bekerja secara asinkron setiap **3 tick** (~6.6 kali/detik) mendeteksi jika crosshair pemain menatap ke arah bounding box banner dalam radius hingga **7 blok**.
- **Efek Partikel Border**:
  - Menampilkan partikel bercahaya (`GLOW`, `END_ROD`, `DUST`) di sekeliling sudut dan batas banner saat pemain mengarahkan pandangannya ke banner tersebut.
- **Actionbar Tooltip Real-time**:
  - Menampilkan prompt teks halus di layar aksi bawah: `✨ KLIK KANAN untuk membuka link informasi!`.

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
| `/media create <id> <file/url> <w> <h> [link] [mode]` | `/banner create` | Memasang banner baru di lokasi target | `apexsionsmedia.admin` | `op` |
| `/media delete <id>` | `/banner delete` | Menghapus banner dan membersihkan entity | `apexsionsmedia.admin` | `op` |
| `/media list` | `/banner list` | Menampilkan daftar seluruh banner aktif | `apexsionsmedia.admin` | `op` |
| `/media setlink <id> <url> [mode]` | `/banner setlink` | Mengatur atau mengubah tautan URL banner | `apexsionsmedia.admin` | `op` |
| `/media reload` | `/banner reload` | Memuat ulang konfigurasi dan memuat banner | `apexsionsmedia.admin` | `op` |

---

## ⚙️ 6. Contoh Penggunaan Cepat

### Memasang Banner Web Discord (Lebar 3 x Tinggi 2):
```bash
/media create discord https://example.com/discord-banner.png 3 2 https://discord.gg/apexsions CHAT_PROMPT
```

### Memasang Logo Server dari File Lokal `plugins/ApexsionsMedia/images/logo.png`:
```bash
/media create serverlogo logo.png 2 2 https://apexsions.net GUI_CONFIRM
```
