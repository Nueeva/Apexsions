# 🎣 PlaceholderAPI — ApexsionsFishing

> **Plugin:** `ApexsionsFishing`  
> **Author:** Nueeva / Apexsions Team  
> **Identifier PAPI:** `%apexsionsfishing_<placeholder>%`  

Plugin **ApexsionsFishing** mengelola ekosistem peradaban memancing (*Civilization Fishing*), mencakup sistem AFK Fishing, Active Reel Engine, Fishing Vault brankas multi-halaman, toko penjualan ikan dinamis, kuota umpan virtual, ensiklopedia ikan (*Fish-o-pedia*), dan papan peringkat Top Angler.

---

## 📊 Daftar Placeholder Lengkap

| Placeholder | Alias Alternatif | Tipe Nilai | Contoh Output | Keterangan Lengkap |
| :--- | :--- | :--- | :--- | :--- |
| `%apexsionsfishing_caught_total%` | - | Angka (Integer) | `342` | Total ekor ikan yang telah berhasil ditangkap oleh pemain. |
| `%apexsionsfishing_heaviest_weight%` | - | Berat (String) | `145.8 kg` | Bobot ikan terberat yang pernah ditangkap pemain sepanjang masa. |
| `%apexsionsfishing_heaviest_fish%` | - | Nama (String) | `Kraken Muda` | Nama spesies ikan terberat yang tercatat dalam rekor tangkapan pemain. |
| `%apexsionsfishing_vault_pages%` | - | Angka (Integer) | `4` | Jumlah halaman Fishing Vault yang telah dibuka/dimiliki pemain. |
| `%apexsionsfishing_vault_max_pages%` | - | Angka (Integer) | `30` | Batas maksimum halaman Fishing Vault yang dapat dibuka di server. |
| `%apexsionsfishing_bait_count%` | - | Angka (Integer) | `25` | Jumlah sisa kuota umpan virtual aktif yang dimiliki pemain. |
| `%apexsionsfishing_journal_discovered%` | - | Angka (Integer) | `18` | Jumlah spesies ikan unik yang telah ditemukan pemain di dalam jurnal. |
| `%apexsionsfishing_journal_total%` | - | Angka (Integer) | `24` | Total seluruh spesies ikan yang terdaftar di ekosistem server. |
| `%apexsionsfishing_top_angler_rank%` | - | Angka/String | `#3` atau `Unranked` | Peringkat pemain pada papan skor Top Angler (tersaring dari staf/OP). |

---

## 💡 Contoh Implementasi di Server

### Contoh Tampilan Papan Skor Nelayan (TAB / Scoreboard):
```yaml
lines:
  - "&6&lPERADABAN MEMANCING"
  - "&7• &fTotal Tangkapan: &e%apexsionsfishing_caught_total% Ekor"
  - "&7• &fRekor Terberat: &a%apexsionsfishing_heaviest_weight% &7(%apexsionsfishing_heaviest_fish%&7)"
  - "&7• &fKapasitas Brankas: &b%apexsionsfishing_vault_pages%&7/&b%apexsionsfishing_vault_max_pages% Hal"
  - "&7• &fUmpan Aktif: &6%apexsionsfishing_bait_count% Kuota"
  - "&7• &fEnsiklopedia: &d%apexsionsfishing_journal_discovered%&7/&d%apexsionsfishing_journal_total%"
  - ""
  - "&fBuka Menu: &e/fish &8| &fBrankas: &e/vault"
```
