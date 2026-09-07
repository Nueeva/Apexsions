@extends('layouts.app')

@section('title', 'Syarat & Ketentuan Layanan (Terms of Service)')

@section('description', 'Syarat, ketentuan layanan, dan etika transaksi webstore server Minecraft Apexsions. Panduan pembelian barang digital, aktivasi instan, dan hak donatur.')

@section('content')
<div class="apx-legal-page py-5">
    <div class="container py-4">
        <!-- Breadcrumb Navigation -->
        <nav aria-label="breadcrumb" class="mb-4">
            <ol class="breadcrumb apx-breadcrumb mb-0">
                <li class="breadcrumb-item"><a href="{{ route('home') }}" class="text-gold text-decoration-none" data-i18n="nav_home">Beranda</a></li>
                <li class="breadcrumb-item active text-white" aria-current="page" data-i18n="terms_breadcrumb">Syarat &amp; Ketentuan</li>
            </ol>
        </nav>

        <!-- Page Header -->
        <div class="apx-section-header text-center mb-5">
            <div class="apx-section-kicker mb-2" data-i18n="terms_kicker">KETENTUAN HUKUM &amp; TRANSAKSI</div>
            <h1 class="apx-section-title display-5 mb-3" data-i18n="terms_title">Syarat &amp; Ketentuan Layanan Webstore</h1>
            <p class="text-muted mx-auto" style="max-width: 760px; font-size: 1.05rem; line-height: 1.8;" data-i18n="terms_desc">
                Dokumen ini mengatur hak, kewajiban, tata cara transaksi donasi, dan perolehan barang virtual di dalam ekosistem server Minecraft Apexsions. Dengan melakukan transaksi pada webstore, Anda menyetujui seluruh klausul di bawah ini.
            </p>
            <div class="d-flex align-items-center justify-content-center gap-3 mt-3">
                <span class="badge bg-gold-subtle text-gold border border-gold-subtle font-monospace" data-i18n="terms_doc_version">VERSI DOKUMEN: 2026.2</span>
                <span class="text-dim small">&bull;</span>
                <span class="text-dim small" data-i18n="terms_doc_updated">DIPERBARUI: 6 SEPTEMBER 2026</span>
            </div>
        </div>

        <div class="row g-4 justify-content-center">
            <div class="col-lg-10">
                <!-- Pasal 1 -->
                <div class="apx-legal-card p-4 p-lg-5 mb-4 rounded" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <div class="text-center" style="min-width: 50px;">
                        <span class="d-block text-gold font-monospace fw-bold" style="font-size: 0.65rem; letter-spacing: 0.12em;" data-i18n="legal_article_word">PASAL</span>
                        <span class="apx-legal-num text-gold font-monospace fs-4 lh-1">01</span>
                    </div>
                        <div>
                            <h2 class="h5 text-white mb-0 font-cinzel" data-i18n="terms_art1_title">Sifat Produk Virtual &amp; Pengiriman Instan</h2>
                            <span class="text-dim small" data-i18n="terms_art1_sub">Ketentuan Pengiriman Barang Digital Non-Fisik</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-3" style="line-height: 1.8;" data-i18n="terms_art1_desc">
                        Seluruh item, kasta donatur (Sions, Emperor, Sovereign, Archon, Ascendant), Battlepass, kunci peti pusaka, kosmetik, serta saldo mata uang yang tertera di webstore Apexsions adalah barang/jasa virtual digital. Tidak ada barang berwujud fisik yang dikirimkan ke alamat Anda.
                    </p>
                    <ul class="text-muted small mb-0 ps-3" style="line-height: 1.8;">
                        <li data-i18n-html="terms_art1_li1">Pesanan diproses dan dikirimkan secara instan melalui sistem sinkronisasi otomatis <code>WebBridgeService</code> segera setelah status pembayaran dikonfirmasi.</li>
                        <li data-i18n="terms_art1_li2">Pastikan username Minecraft yang Anda masukkan saat checkout persis sama dengan akun di dalam game. Kesalahan pengetikan nama akun di luar tanggung jawab sistem otomatis.</li>
                    </ul>
                </div>

                <!-- Pasal 2 -->
                <div class="apx-legal-card p-4 p-lg-5 mb-4 rounded" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <div class="text-center" style="min-width: 50px;">
                        <span class="d-block text-gold font-monospace fw-bold" style="font-size: 0.65rem; letter-spacing: 0.12em;" data-i18n="legal_article_word">PASAL</span>
                        <span class="apx-legal-num text-gold font-monospace fs-4 lh-1">02</span>
                    </div>
                        <div>
                            <h2 class="h5 text-white mb-0 font-cinzel" data-i18n="terms_art2_title">Kanal Pembayaran Resmi &amp; Verifikasi</h2>
                            <span class="text-dim small" data-i18n="terms_art2_sub">Saluran Transaksi Terverifikasi Manajemen</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-3" style="line-height: 1.8;" data-i18n="terms_art2_desc">
                        Transaksi resmi hanya dilayani melalui webstore portal <a href="{{ route('home') }}" class="text-gold text-decoration-none">web.apexsions.my.id</a> dan konfirmasi langsung ke WhatsApp Founder/Admin resmi Apexsions:
                    </p>
                    <div class="row g-3 mb-3">
                        <div class="col-md-6">
                            <div class="p-3 rounded text-center" style="background: rgba(255, 255, 255, 0.02); border: 1px solid var(--apx-border);">
                                <i class="bi bi-whatsapp text-success fs-4 mb-1"></i>
                                <div class="text-white small fw-bold">Admin 1 (Rifqi)</div>
                                <div class="text-muted font-monospace small">+62 812-1299-4597</div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="p-3 rounded text-center" style="background: rgba(255, 255, 255, 0.02); border: 1px solid var(--apx-border);">
                                <i class="bi bi-whatsapp text-success fs-4 mb-1"></i>
                                <div class="text-white small fw-bold">Admin 2 (Friell)</div>
                                <div class="text-muted font-monospace small">+62 858-8316-1047</div>
                            </div>
                        </div>
                    </div>
                    <p class="text-muted small mb-0" style="line-height: 1.8;" data-i18n="terms_art2_warn">
                        Manajemen tidak pernah meminta password akun Minecraft Anda dan tidak bertanggung jawab atas transaksi yang dilakukan di luar nomor resmi di atas.
                    </p>
                </div>

                <!-- Pasal 3 -->
                <div class="apx-legal-card p-4 p-lg-5 mb-4 rounded" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <div class="text-center" style="min-width: 50px;">
                        <span class="d-block text-gold font-monospace fw-bold" style="font-size: 0.65rem; letter-spacing: 0.12em;" data-i18n="legal_article_word">PASAL</span>
                        <span class="apx-legal-num text-gold font-monospace fs-4 lh-1">03</span>
                    </div>
                        <div>
                            <h2 class="h5 text-white mb-0 font-cinzel" data-i18n="terms_art3_title">Kebijakan Pengembalian Dana &amp; Anti-Chargeback</h2>
                            <span class="text-dim small" data-i18n="terms_art3_sub">Klausul Final Transaksi &amp; Penegakan Integritas</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-3" style="line-height: 1.8;" data-i18n="terms_art3_desc">
                        Karena manfaat produk virtual langsung diterapkan secara permanen atau berkala ke akun in-game, seluruh transaksi bersifat final dan <strong>tidak dapat dibatalkan atau dikembalikan (No Refund)</strong>.
                    </p>
                    <ul class="text-muted small mb-0 ps-3" style="line-height: 1.8;">
                        <li data-i18n="terms_art3_li1">Tindakan penarikan dana sepihak (chargeback, dispute, atau klaim fraud bank palsu) dianggap sebagai pelanggaran integritas berat dan akan memicu <strong>pemblokiran permanen (Blacklist IP &amp; Akun)</strong> dari seluruh layanan server.</li>
                        <li data-i18n="terms_art3_li2">Apabila terjadi kendala sistem di mana paket belum terkirim setelah 15 menit pasca-pembayaran berhasil, warga wajib membuka tiket investigasi di Discord resmi dengan melampirkan invoice transfer.</li>
                    </ul>
                </div>

                <!-- Pasal 4 -->
                <div class="apx-legal-card p-4 p-lg-5 mb-4 rounded" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <div class="text-center" style="min-width: 50px;">
                        <span class="d-block text-gold font-monospace fw-bold" style="font-size: 0.65rem; letter-spacing: 0.12em;" data-i18n="legal_article_word">PASAL</span>
                        <span class="apx-legal-num text-gold font-monospace fs-4 lh-1">04</span>
                    </div>
                        <div>
                            <h2 class="h5 text-white mb-0 font-cinzel" data-i18n="terms_art4_title">Kepatuhan Hukum &amp; Batasan Hak Donatur</h2>
                            <span class="text-dim small" data-i18n="terms_art4_sub">Donasi Bukan Merupakan Kekebalan Hukum (Immunity)</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-3" style="line-height: 1.8;" data-i18n="terms_art4_desc">
                        Kepemilikan kasta donatur adalah bentuk apresiasi dan kontribusi sukarela warga terhadap pembiayaan infrastruktur peladen. Kasta donatur tidak memberikan kekebalan hukum dari <a href="{{ route('rules') }}" class="text-gold text-decoration-none">Peraturan Resmi Apexsions</a>.
                    </p>
                    <p class="text-muted small mb-0" style="line-height: 1.8;" data-i18n="terms_art4_desc2">
                        Donatur yang terbukti melanggar aturan berat (penggunaan cheat/klien ilegal, duplikasi ekonomi, exploit sistem, perundungan siber) tetap dikenai sanksi banned/mute sesuai matriks sanksi peradaban tanpa adanya kompensasi pengembalian dana.
                    </p>
                </div>

                <!-- Pasal 5 -->
                <div class="apx-legal-card p-4 p-lg-5 mb-4 rounded" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <div class="text-center" style="min-width: 50px;">
                        <span class="d-block text-gold font-monospace fw-bold" style="font-size: 0.65rem; letter-spacing: 0.12em;" data-i18n="legal_article_word">PASAL</span>
                        <span class="apx-legal-num text-gold font-monospace fs-4 lh-1">05</span>
                    </div>
                        <div>
                            <h2 class="h5 text-white mb-0 font-cinzel" data-i18n="terms_art5_title">Kesesuaian Minecraft Commercial Usage Guidelines (EULA)</h2>
                            <span class="text-dim small" data-i18n="terms_art5_sub">Kepatuhan Standar Mojang Studios &amp; Microsoft</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-0" style="line-height: 1.8;" data-i18n="terms_art5_desc">
                        Apexsions adalah peladen independen yang beroperasi selaras dengan Minecraft Commercial Usage Guidelines. Apexsions tidak berafiliasi dengan Mojang Studios atau Microsoft Corporation. Seluruh nama brand dan hak cipta terdaftar Minecraft adalah milik masing-masing pemiliknya.
                    </p>
                </div>

                <!-- Navigation Footer -->
                <div class="d-flex align-items-center justify-content-between pt-4 border-top border-secondary border-opacity-15 flex-wrap gap-3">
                    <a href="{{ route('privacy') }}" class="text-gold text-decoration-none small">
                        <i class="bi bi-shield-lock me-1"></i> <span data-i18n="terms_link_privacy">Baca Kebijakan Privasi &amp; Perlindungan Data &rarr;</span>
                    </a>
                    <a href="{{ route('rules') }}" class="text-dim text-decoration-none small">
                        <i class="bi bi-shield-check me-1"></i> <span data-i18n="terms_link_rules">Baca Peraturan Lengkap Server</span>
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
