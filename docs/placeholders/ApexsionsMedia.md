# 📢 Panduan Integrasi — ApexsionsMedia

> **Plugin:** `ApexsionsMedia`  
> **Author:** Apexsions Team  
> **Fokus Modul:** Spanduk Interaktif (*Interactive Banner*), Raytrace Hover Glow, dan Aksi Tautan Luar (*URL Actions*).  

Plugin **ApexsionsMedia** dirancang khusus untuk menciptakan pengalaman visual sinematik tingkat tinggi bagi pemain saat berinteraksi dengan identitas server, media sosial komunitas, dan tautan eksternal resmi (Web Store, Discord, YouTube, Instagram).

---

## 🎨 Fungsi Utama Plugin

1. **Interactive Banner & Logo:**
   - Menampilkan logo megah *Apexsions: The Peak Civilizations* di area spawn peradaban.
   - Menggunakan model display dan text display modern dengan interpolasi halus.
2. **Raytrace Hover Glow:**
   - Mendeteksi arah pandangan pemain secara real-time (*raytracing*).
   - Memunculkan efek kilau (*glow*) dan animasi partikel saat pemain mengarahkan kursor ke objek media.
3. **URL Actions:**
   - Interaksi klik kanan/kiri yang secara aman memberikan tautan web resmi atau membuka dialog konfirmasi URL kepada pemain di chat.

---

## 🔗 Dukungan PlaceholderAPI dalam ApexsionsMedia

Saat mengonfigurasi teks tampilan spanduk, judul interaktif, dan lore pada file konfigurasi `ApexsionsMedia`, Anda dapat menyematkan **seluruh placeholder dari plugin Apexsions lainnya** secara langsung.

Teks spanduk akan secara otomatis mem-parsing variabel pemain:
- `%player_name%` — Nama pemain yang sedang melihat spanduk.
- `%apexsions_rank_badge%` — Pangkat pemain dengan gradien resmi.
- `%apexsions_kingdom_name%` — Kerajaan tempat pemain bernaung.
- `%apexsions_level%` — Level peradaban pemain.
- `%apexsionseconomy_rupiah_formatted%` — Saldo Rupiah pemain.
- `%apexsionsbattlepass_coins_formatted%` — Saldo Battle Coins pemain.

### Contoh Baris Teks Sambutan Spanduk Spawn:
```yaml
banner:
  title: "<gradient:#FFD700:#FFA500><bold>SELAMAT DATANG DI APEXSIONS</bold></gradient>"
  subtitle: "&fSalam peradaban, %apexsions_rank_badge% %player_name% &7dari Kerajaan &e%apexsions_kingdom_name%!"
  lore:
    - "&7Jelajahi dunia, dirikan kerajaan, dan raih puncak kejayaan!"
    - "&eKlik untuk mengunjungi Discord & Web Komunitas Resmi."
```
