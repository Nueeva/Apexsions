@extends('layouts.app')

@section('title', 'Kebijakan Privasi & Perlindungan Data (Privacy Policy)')

@section('description', 'Kebijakan privasi dan perlindungan data akun warga server Minecraft Apexsions. Transparansi penyimpanan UUID, email, log sesi, dan keamanan transaksi.')

@section('content')
<div class="apx-legal-page py-5">
    <div class="container py-4">
        <!-- Breadcrumb Navigation -->
        <nav aria-label="breadcrumb" class="mb-4">
            <ol class="breadcrumb apx-breadcrumb mb-0">
                <li class="breadcrumb-item"><a href="{{ route('home') }}" class="text-gold text-decoration-none" data-i18n="nav_home">Beranda</a></li>
                <li class="breadcrumb-item active text-white" aria-current="page" data-i18n="privacy_breadcrumb">Kebijakan Privasi</li>
            </ol>
        </nav>

        <!-- Page Header -->
        <div class="apx-section-header text-center mb-5">
            <div class="apx-section-kicker mb-2" data-i18n="privacy_kicker">KEBIJAKAN PRIVASI &amp; PERLINDUNGAN DATA</div>
            <h1 class="apx-section-title display-5 mb-3" data-i18n="privacy_title">Privasi &amp; Keamanan Data Warga</h1>
            <p class="text-muted mx-auto" style="max-width: 760px; font-size: 1.05rem; line-height: 1.8;" data-i18n="privacy_desc">
                Apexsions memegang teguh integritas kedaulatan data warganya. Kami hanya menghimpun data yang esensial untuk sinkronisasi permainan, pengiriman paket webstore, dan perlindungan keamanan server dari ancaman siber.
            </p>
            <div class="d-flex align-items-center justify-content-center gap-3 mt-3">
                <span class="badge bg-gold-subtle text-gold border border-gold-subtle font-monospace" data-i18n="privacy_doc_version">VERSI DOKUMEN: 2026.2</span>
                <span class="text-dim small">&bull;</span>
                <span class="text-dim small" data-i18n="privacy_doc_updated">DIPERBARUI: 6 SEPTEMBER 2026</span>
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
                            <h2 class="h5 text-white mb-0 font-cinzel" data-i18n="privacy_art1_title">Informasi yang Kami Himpun</h2>
                            <span class="text-dim small" data-i18n="privacy_art1_sub">Data Esensial untuk Menjalankan Ekosistem Peradaban</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-3" style="line-height: 1.8;" data-i18n="privacy_art1_desc">
                        Saat Anda mendaftarkan diri, menautkan akun dengan perintah <code>/link</code>, atau bermain di dalam realm, sistem kami mencatat informasi berikut:
                    </p>
                    <ul class="text-muted small mb-0 ps-3" style="line-height: 1.8;">
                        <li data-i18n-html="privacy_art1_li1"><strong>Identitas Karakter Minecraft:</strong> Nama pengguna (Username) dan nomor pengidentifikasi unik (UUID) resmi dari Mojang atau Floodgate (Bedrock).</li>
                        <li data-i18n-html="privacy_art1_li2"><strong>Kredensial Akun Web:</strong> Alamat surel (email) aktif dan kata sandi yang telah dienkripsi secara satu arah dengan algoritma <code>bcrypt</code> berstandar industri.</li>
                        <li data-i18n-html="privacy_art1_li3"><strong>Data Telemetri Jaringan:</strong> Alamat Protokol Internet (IP) dan catatan waktu login untuk mendeteksi upaya peretasan, serangan DDoS, serta pencegahan multi-akun ilegal (ban evasion).</li>
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
                            <h2 class="h5 text-white mb-0 font-cinzel" data-i18n="privacy_art2_title">Tujuan &amp; Pemanfaatan Data</h2>
                            <span class="text-dim small" data-i18n="privacy_art2_sub">Optimalisasi Layanan &amp; Integritas Permainan</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-3" style="line-height: 1.8;" data-i18n="privacy_art2_desc">
                        Data warga diolah secara ketat untuk keperluan operasional teknis berikut:
                    </p>
                    <ul class="text-muted small mb-0 ps-3" style="line-height: 1.8;">
                        <li data-i18n-html="privacy_art2_li1">Mengonfirmasi dan mendistribusikan kasta donatur, kunci peti pusaka, serta koin yang dipesan melalui webstore secara otomatis via <code>WebBridgeService</code>.</li>
                        <li data-i18n="privacy_art2_li2">Menjaga tatanan sosial server dari pemain toksik, cheater, dan pelaku duplikasi melalui sistem audit log staf terpusat.</li>
                        <li data-i18n="privacy_art2_li3">Mengirimkan notifikasi pemulihan kata sandi atau pengumuman penting seputar pemeliharaan peradaban ke email terdaftar.</li>
                    </ul>
                </div>

                <!-- Pasal 3 -->
                <div class="apx-legal-card p-4 p-lg-5 mb-4 rounded" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <div class="text-center" style="min-width: 50px;">
                        <span class="d-block text-gold font-monospace fw-bold" style="font-size: 0.65rem; letter-spacing: 0.12em;" data-i18n="legal_article_word">PASAL</span>
                        <span class="apx-legal-num text-gold font-monospace fs-4 lh-1">03</span>
                    </div>
                        <div>
                            <h2 class="h5 text-white mb-0 font-cinzel" data-i18n="privacy_art3_title">Keamanan Finansial &amp; Transaksi</h2>
                            <span class="text-dim small" data-i18n="privacy_art3_sub">Nol Penyimpanan Informasi Finansial Sensitif</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-0" style="line-height: 1.8;" data-i18n-html="privacy_art3_desc">
                        Apexsions <strong>tidak pernah menyimpan</strong> nomor kartu kredit, debit, kode CVV, atau sandi perbankan Anda di server kami. Seluruh proses checkout dialihkan ke saluran pembayaran resmi terlisensi (Midtrans Gateway) atau konfirmasi manual WhatsApp founder berizin resmi dengan enkripsi TLS end-to-end.
                    </p>
                </div>

                <!-- Pasal 4 -->
                <div class="apx-legal-card p-4 p-lg-5 mb-4 rounded" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <div class="text-center" style="min-width: 50px;">
                        <span class="d-block text-gold font-monospace fw-bold" style="font-size: 0.65rem; letter-spacing: 0.12em;" data-i18n="legal_article_word">PASAL</span>
                        <span class="apx-legal-num text-gold font-monospace fs-4 lh-1">04</span>
                    </div>
                        <div>
                            <h2 class="h5 text-white mb-0 font-cinzel" data-i18n="privacy_art4_title">Kerahasiaan Mutlak Tanpa Pihak Ketiga</h2>
                            <span class="text-dim small" data-i18n="privacy_art4_sub">Komitmen Perlindungan Privasi Komunitas</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-0" style="line-height: 1.8;" data-i18n="privacy_art4_desc">
                        Kami tidak pernah dan tidak akan pernah menjual, menyewakan, atau membagikan data identitas pribadi warga kepada pihak ketiga manapun untuk tujuan iklan atau pemasaran. Informasi hanya dapat dibuka apabila terdapat instruksi berkekuatan hukum resmi dari otoritas penegak hukum yang berwenang.
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
                            <h2 class="h5 text-white mb-0 font-cinzel" data-i18n="privacy_art5_title">Hak Warga atas Pengendalian Akun</h2>
                            <span class="text-dim small" data-i18n="privacy_art5_sub">Otentikasi Dua Faktor (2FA) &amp; Penghapusan Data</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-0" style="line-height: 1.8;" data-i18n-html="privacy_art5_desc">
                        Setiap warga memiliki hak penuh untuk mengakses riwayat akun, memperbarui email, mengaktifkan fitur perlindungan otentikasi dua faktor (2FA), atau meminta penghapusan akun secara permanen melalui <a href="{{ route('profile.index') }}" class="text-gold text-decoration-none">Pengaturan Profil</a>.
                    </p>
                </div>

                <!-- Navigation Footer -->
                <div class="d-flex align-items-center justify-content-between pt-4 border-top border-secondary border-opacity-15 flex-wrap gap-3">
                    <a href="{{ route('terms') }}" class="text-gold text-decoration-none small">
                        <i class="bi bi-file-earmark-lock me-1"></i> <span data-i18n="privacy_link_terms">Baca Syarat &amp; Ketentuan Layanan Webstore &rarr;</span>
                    </a>
                    <a href="{{ route('rules') }}" class="text-dim text-decoration-none small">
                        <i class="bi bi-shield-check me-1"></i> <span data-i18n="privacy_link_rules">Baca Peraturan Lengkap Server</span>
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
