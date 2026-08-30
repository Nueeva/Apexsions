# Dokumentasi Lengkap ApexsionsMedia

Panduan teknis resmi modul **`ApexsionsMedia`** untuk sistem render banner/logo gambar multi-tile, mesin palet peta async, raytrace hover glow, dan interaksi tautan URL web.

---

## 📂 Struktur Direktori & Konfigurasi YAML

```
plugins/ApexsionsMedia/
├── config.yml            <-- Pengaturan database (SQLite/PostgreSQL), cache, raytrace interval, dan opsi umum
├── banners.yml           <-- Penyimpanan metadata seluruh instance banner yang aktif
├── plugin.yml            <-- Deklarasi commands, permissions, dan metadata
└── images/               <-- Direktori lokal untuk menaruh file gambar PNG / JPG
```

---

## ⚡ Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/media create <id> <src> [w] [h] [url] [mode]` | `/banner create` | Memasang banner baru dengan auto-dimensi & link | `apexsionsmedia.admin` | `op` |
| `/media place <id>` | `/banner place`, `/media paste` | Memindahkan banner ke dinding target via Raytracing | `apexsionsmedia.admin` | `op` |
| `/media copy <idAsal> <idBaru>` | `/banner copy`, `/media clone` | Menduplikasi template konfigurasi banner | `apexsionsmedia.admin` | `op` |
| `/media move <id>` | `/banner move`, `/media moveto` | Memindahkan lokasi banner yang ada | `apexsionsmedia.admin` | `op` |
| `/media resize <id> <w> <h>` | `/banner resize` | Mengubah dimensi ukuran banner secara langsung | `apexsionsmedia.admin` | `op` |
| `/media setlink <id> [url\|none] [mode]` | `/banner setlink` | Mengatur tautan URL interaktif banner | `apexsionsmedia.admin` | `op` |
| `/media delete <id>` | `/banner delete`, `/media remove` | Menghapus banner dan entity item frame terkait | `apexsionsmedia.admin` | `op` |
| `/media list` | `/banner list` | Menampilkan daftar seluruh banner aktif di server | `apexsionsmedia.admin` | `op` |
| `/media reload` | `/banner reload` | Reload konfigurasi & render ulang seluruh banner aktif | `apexsionsmedia.admin` | `op` |
| `/media gui` | `/media admin`, `/banner gui` | Membuka Interactive Media Admin Management GUI | `apexsionsmedia.admin` | `op` |

---

## 🎨 Arsitektur Raster & Raytrace

1. **MapPalette & Image Slicing**:
   - Gambar dipotong menjadi grid tile berukuran $128 \times 128$ pixel.
   - Warna di-mapping ke palet warna vanilla Minecraft Map Canvas secara efisien menggunakan Caffeine in-memory cache.
2. **Raytrace Line-of-Sight Detection (`MediaRaytraceService`)**:
   - Berjalan asinkron setiap **3 tick** (~6.6 kali/detik) untuk mendeteksi arah pandang pemain ke bounding box banner (radius maksimal 7 blok).
   - Memancarkan partikel bercahaya (`GLOW`, `END_ROD`, `DUST`) dan actionbar tooltip saat hover terdeteksi.

---

## 🧩 Akses Public API (`ApexsionsMediaAPI`)

```java
ApexsionsMediaAPI mediaApi = ApexsionsMediaProvider.get();
if (mediaApi != null) {
    MediaBanner banner = mediaApi.getBanner("server_logo");
    Collection<MediaBanner> all = mediaApi.getAllBanners();
}
```
