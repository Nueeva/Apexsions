# Gemini Guidelines — ApexsionsEconomy

Standar pengembangan khusus modul `ApexsionsEconomy`:
- **Package Root**: `com.apex.economy`
- **Atomic Money Transactions**: Seluruh penambahan/pengurangan saldo wajib menggunakan method synchronized / atomic query di `EconomyRepository` untuk mencegah duplikasi saldo.
- **ApexsionsCore Hook**: Pengecekan kerajaan pemain harus selalu melalui `plugin.getApexsionsCoreHook()` dengan fallback aman jika `ApexsionsCore` tidak aktif.
