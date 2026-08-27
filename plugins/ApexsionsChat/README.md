# ApexsionsChat — Minecraft 1.21.4 (Paper 26.2)

Plugin komunikasi, obrolan interaktif, pamer item, permainan obrolan, dan sistem moderasi keamanan (*AdvancedChat style*) untuk server **Apexsions**.

---

## 🌟 Fitur Utama
- **Channel Terstruktur**: Obrolan Global (`/g`), Kerajaan (`/kc`), dan Staf (`/sc`).
- **Showcase Item Modern**: `/showitem` menampilkan kartu item hover & klik aman berbasis Adventure tanpa kebocoran tag.
- **Surat Offline (*Mail*)**: `/mail` untuk mengirim dan membaca pesan saat pemain sedang offline.
- **Chat Games & Pengumuman**: Game matematika dan unscramble otomatis dengan hadiah uang/item, serta broadcast informasi terjadwal.
- **Sistem Keamanan & Moderasi Lapis Tiga**:
  - Deteksi Spam: Sliding window rate limit, near-duplicate checking (Levenshtein), dan auto temp-mute.
  - Filter Kata Kotor (*Profanity*) & Toksisitas multi-bahasa.
  - Filter Iklan (*Anti-Ad*): Deteksi IP, domain, dan link Discord non-whitelist.
  - Deteksi Exploit: Mencegah JNDI syntax dan token leaks.
  - Notifikasi Staf (*Staff Alerts*) dan Umpan Balik Suara saat pelanggaran terdeteksi.

---

## 🛠️ Kompilasi & Build
```powershell
mvn clean package
```
Output: `target/ApexsionsChat-1.0.0.jar`
