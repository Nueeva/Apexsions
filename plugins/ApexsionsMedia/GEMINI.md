# Gemini Guidelines — ApexsionsMedia

Standar pengembangan khusus modul `ApexsionsMedia`:
- **Package Root**: `com.apexsions.media`
- **Asynchronous Image Pipeline**: Pengunduhan gambar, decoding, pemotongan tile 128x128, dan palet mapping wajib dieksekusi secara asinkron (`CompletableFuture`).
- **Entity & Map Operations**: Modifikasi entity ItemFrame dan map canvas Bukkit wajib dieksekusi pada Main Thread.
- **Provider API**: Seluruh interaksi eksternal publik harus diakses lewat `ApexsionsMediaProvider.get()`.
- **Bedrock / Geyser Compatibility**: Semua render banner harus 100% menggunakan vanilla maps canvas tanpa ketergantungan mod klien.
