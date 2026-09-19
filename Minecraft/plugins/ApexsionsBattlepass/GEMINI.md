# Gemini Guidelines — ApexsionsBattlepass

Standar pengembangan khusus modul `ApexsionsBattlepass`:
- **Package Root**: `com.apexsions.battlepass`
- **Pass Inheritance**: Reward tingkatan pass lebih tinggi wajib mewarisi hak klaim dari tingkatan pass di bawahnya (`EXSIO` $\supset$ `SIO` $\supset$ `CITIZEN`).
- **Editor GUI State**: Setiap modifikasi reward via GUI editor `/abp` harus langsung disimpan ke `rewards.yml` dan merefresh cache `RewardManager`.
- **Database Operations**: Semua update progresi level/quest/currency pemain harus dilakukan secara asinkron dengan fallback SQLite/PostgreSQL.
