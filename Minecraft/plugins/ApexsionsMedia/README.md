# ApexsionsMedia — Minecraft 1.21.4 (Paper 26.2)

Plugin render banner, logo, dan visual media interaktif multi-tile in-game berkinerja tinggi dengan line-of-sight raytrace hover glow, actionbar tooltips, replikasi template fleksibel, dan aksi tautan web terkonfirmasi untuk server **Apexsions**.

---

## 🌟 Fitur Utama

- **Render Gambar Asinkron Multi-Tile**: Mendukung format PNG, JPG, JPEG dari file lokal (`plugins/ApexsionsMedia/images/`) atau tautan URL langsung.
- **Deteksi Dimensi Otomatis**: Menyesuaikan resolusi gambar asli ke ukuran ubin secara proporsional jika parameter lebar/tinggi tidak ditentukan.
- **Raytrace Line-of-Sight Hover Glowing**: Menampilkan partikel bercahaya di sekeliling border banner dan tooltip Actionbar saat crosshair pemain mengarah ke banner dalam radius 7 blok.
- **Interaksi Tautan URL Interaktif**:
  - `CHAT_PROMPT`: Pilihan tombol MiniMessage `[🌐 BUKA URL]` dan `[📋 SALIN LINK]`.
  - `GUI_CONFIRM`: Menu dialog 27-slot dengan tombol konfirmasi ramah sentuh.
- **Manajemen Fleksibel dari Game**:
  - `/media place <id>`: Memasang/memindahkan banner ke dinding bidikan via Raytracing.
  - `/media copy <idAsal> <idBaru>`: Menduplikasi konfigurasi banner ke template baru.
  - `/media gui` / `/media admin`: Menu interaktif 54-slot untuk mengelola seluruh banner aktif.
- **100% Vanilla & Bedrock Compatible**: Tanpa mod klien, kompatibel penuh dengan Geyser & Floodgate.

---

## 🛠️ Kompilasi & Build

```powershell
# Kompilasi khusus ApexsionsMedia:
powershell -ExecutionPolicy Bypass -File .\build.ps1 Media
```

Output JAR siap pakai:
- `build/libs/ApexsionsMedia-1.0.0.jar`
- `plugins/ApexsionsMedia/ApexsionsMedia-1.0.0.jar`
