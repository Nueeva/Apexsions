# Gemini Guidelines — ApexsionsBattlepass

Standar pengembangan khusus modul `ApexsionsBattlepass`:
- **Package Root**: `com.apex.battlepass`
- **Pass Inheritance**: Reward tingkatan pass lebih tinggi wajib mewarisi hak klaim dari tingkatan pass di bawahnya (`ULTIMATE` $\supset$ `ELITE` $\supset$ `PREMIUM` $\supset$ `FREE`).
- **Editor GUI State**: Setiap modifikasi reward via GUI editor `/abp` harus langsung disimpan ke `rewards.yml` dan merefresh cache `RewardManager`.
