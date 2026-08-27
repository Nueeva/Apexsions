# Gemini Guidelines — ApexsionsChat

Standar pengembangan khusus modul `ApexsionsChat`:
- **Package Root**: `com.yourserver.apexsionschat`
- **MiniMessage / Adventure TagResolver**: Semua format chat dinamis wajib menggunakan `Placeholder.component(...)` agar karakter seperti `<` atau `>` dari pesan pemain tidak merusak komponen teks.
- **Moderation Bypass**: Bypass permission harus selalu `default: false` dan `general.bypass-for-op: false` agar pengujian lokal staf/admin tetap memeriksa filter dengan benar.
