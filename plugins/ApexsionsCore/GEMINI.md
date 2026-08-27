# Gemini Guidelines — ApexsionsCore

Standar pengembangan khusus modul `ApexsionsCore`:
- **Package Root**: `com.yourserver.apexsionscore`
- **Pattern**: Repository pattern dengan asynchronous database operations via CompletableFuture.
- **Provider API**: Seluruh interaksi publik harus diakses lewat `ApexsionsCoreProvider.get()`.
- **Adventure Components**: Hindari chat formatting legacy; gunakan `MiniMessage.miniMessage()`.
