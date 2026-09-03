# Dokumentasi Lengkap ApexsionsMedia

Panduan teknis resmi modul **`ApexsionsMedia`** untuk sistem render banner/logo gambar multi-tile, mesin palet peta async, raytrace hover glow, interaksi tautan URL web, serta **Unified Content Creator Verification & Reward Suite (YouTube & TikTok)**.

---

## 📂 Struktur Direktori & Konfigurasi YAML

```
plugins/ApexsionsMedia/
├── config.yml            <-- Pengaturan database (SQLite/PostgreSQL), cache, raytrace interval, dan opsi Creator
├── banners.db / creator.db <-- Database penyimpanan banner & profil/klaim kreator
├── plugin.yml            <-- Deklarasi commands, permissions, dan metadata
└── images/               <-- Direktori lokal untuk menaruh file gambar PNG / JPG
```

---

## ⚡ Matriks Perintah & Permissions

### 🖼️ Banner & Media Display
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

### 🎥 Unified Content Creator Suite (YouTube & TikTok)
| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/creator` | `/kreator`, `/creator menu` | Membuka Interactive Creator Hub GUI (54-slot) | `apexsionsmedia.creator` | `true` |
| `/creator submit <url>` | `/creator claim <url>` | Submit URL video YouTube / TikTok untuk verifikasi views & klaim reward | `apexsionsmedia.creator` | `true` |
| `/creator link <youtube\|tiktok> <id>` | `/creator link yt/tt` | Memulai proses penautan akun YouTube / TikTok | `apexsionsmedia.creator` | `true` |
| `/creator verify youtube` | `/creator verify` | Memverifikasi kode linking kepemilikan channel YouTube | `apexsionsmedia.creator` | `true` |
| `/creator unlink <youtube\|tiktok>` | `/creator unbind` | Memutuskan tautan akun YouTube atau TikTok | `apexsionsmedia.creator` | `true` |
| `/creator tiers` | `/creator rewards` | Membuka GUI daftar tier, statistik minimal, dan rewards | `apexsionsmedia.creator` | `true` |
| `/creator admin reload` | `/creator admin reload` | Reload konfigurasi creator & tiers dari `config.yml` | `apexsionsmedia.creator.admin` | `op` |
| `/creator admin info <pemain>` | `/creator admin info` | Melihat status channel tertaut pemain | `apexsionsmedia.creator.admin` | `op` |
| `/creator admin reset <pemain>` | `/creator admin reset` | Mereset data penautan akun kreator pemain | `apexsionsmedia.creator.admin` | `op` |

---

## 🎬 Alur Kerja Sistem Verifikasi Kreator

1. **Penautan Akun (Account Linking)**:
   - **YouTube**: Pemain mengetik `/creator link youtube <ChannelID/@Handle>`. Plugin menghasilkan kode verifikasi acak (`APEX-XXXX`). Pemain menaruh kode tersebut di deskripsi channel atau deskripsi video terbarunya, lalu mengetik `/creator verify youtube`.
   - **TikTok**: Pemain mengetik `/creator link tiktok <Username>` (atau klik via GUI). Akun terhubung ke profil pemain.
2. **Submisi Video & Verifikasi Non-Blocking**:
   - Pemain mengetik `/creator submit <VideoURL>` atau mengklik tombol Submit di `/creator`.
   - Plugin menjalankan request HTTP Asynchronous (`HttpClient` Java 21) ke YouTube API v3 atau TikTok scraper engine.
   - Melakukan validasi:
     1. Kepemilikan video sesuai akun terdaftar pemain.
     2. Judul/deskripsi memuat hashtag wajib server (`#Apexsions`, `#ApexsionsMC`, dll).
     3. Umur video di bawah batas maksimal (contoh: 14 hari).
     4. Anti-duplikasi (video ID dicek di database `creator_claims`).
     5. Pencocokan ke Tier tertinggi (Bronze, Silver, Gold, Diamond).
3. **Eksekusi Reward & Broadcast**:
   - Menjalankan command reward (Coins/Vault, LuckPerms Media Rank, Title) di Main Thread secara aman.
   - Mengirim MiniMessage broadcast mewah ke seluruh server.

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
