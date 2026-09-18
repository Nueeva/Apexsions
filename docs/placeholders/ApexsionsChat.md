# 💬 PlaceholderAPI — ApexsionsChat

> **Plugin:** `ApexsionsChat`  
> **Author:** Antigravity / Apexsions  
> **Identifier PAPI:** `%apexsionschat_<placeholder>%`  

Plugin **ApexsionsChat** mengatur sistem perpesanan berformat MiniMessage, manajemen saluran obrolan (*Chat Channels: Global, Kingdom, Staff, Local*), profil sosial peradaban, sistem persuratan (*Mailbox*), serta meja laporan moderasi (*Staff Reports Desk*).

---

## 📊 Daftar Placeholder Lengkap

| Placeholder | Tipe Nilai | Contoh Output | Keterangan Lengkap |
| :--- | :--- | :--- | :--- |
| `%apexsionschat_channel%` | String | `Global`, `Kingdom`, `Staff`, `Local` | Menampilkan nama saluran obrolan yang sedang aktif digunakan oleh pemain untuk berbicara saat ini. Default: `Global`. |
| `%apexsionschat_unread_mail%` | Angka (Integer) | `3` atau `0` | Menampilkan jumlah surat (*mail*) yang belum dibaca oleh pemain di dalam kotak pos peradaban (`/mail`). Sangat ideal diletakkan pada notifikasi masuk server atau actionbar. |
| `%apexsionschat_open_reports%` | Angka (Integer) | `2` atau `0` | Menampilkan total jumlah laporan pelanggaran pemain (*reports desk*) yang masih berstatus terbuka dan butuh ditangani staf. Sangat berguna untuk diletakkan pada papan skor atau TAB khusus staf (`apexsions.staff`). |

---

## 💡 Contoh Penggunaan di Server

### 1. Format TAB Khusus Staf:
```yaml
staff-header:
  - "&c&lSTAFF DESK &8| &fChannel: &e%apexsionschat_channel%"
  - "&fLaporan Terbuka: &e%apexsionschat_open_reports% Menunggu Penanganan &8(Ketik &c/report list&8)"
```

### 2. Notifikasi Actionbar / Title Saat Join:
Jika pemain memiliki surat belum terbaca, Anda dapat menampilkan pemberitahuan dinamis (misal menggunakan ConditionalEvents atau DeluxeMenus):
```yaml
# Menampilkan pemberitahuan surat baru
message: "&eKamu memiliki &6%apexsionschat_unread_mail% &esurat baru yang belum dibaca! Ketik &f/mail read &euntuk membuka."
```

### 3. Format Prefix Chat (Chat Layout):
```text
[&e%apexsionschat_channel%&r] %apexsions_rank_badge% %player_name%: %message%
```
