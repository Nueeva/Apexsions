@extends('layouts.app')

@section('title', 'Kebijakan Privasi & Perlindungan Data (Privacy Policy)')

@section('description', 'Kebijakan privasi dan perlindungan data akun warga server Minecraft Apexsions. Transparansi penyimpanan UUID, email, log sesi, dan keamanan transaksi.')

@section('content')
<div class="apx-legal-page py-5">
    <div class="container py-4">
        <!-- Breadcrumb Navigation -->
        <nav aria-label="breadcrumb" class="mb-4">
            <ol class="breadcrumb apx-breadcrumb mb-0">
                <li class="breadcrumb-item"><a href="{{ route('home') }}" class="text-gold text-decoration-none">Beranda</a></li>
                <li class="breadcrumb-item active text-white" aria-current="page">Kebijakan Privasi</li>
            </ol>
        </nav>

        <!-- Page Header -->
        <div class="apx-section-header text-center mb-5">
            <div class="apx-section-kicker mb-2">KEBIJAKAN PRIVASI &amp; PERLINDUNGAN DATA</div>
            <h1 class="apx-section-title display-5 mb-3">Privasi &amp; Keamanan Data Warga</h1>
            <p class="text-muted mx-auto" style="max-width: 760px; font-size: 1.05rem; line-height: 1.8;">
                Apexsions memegang teguh integritas kedaulatan data warganya. Kami hanya menghimpun data yang esensial untuk sinkronisasi permainan, pengiriman paket webstore, dan perlindungan keamanan server dari ancaman siber.
            </p>
            <div class="d-flex align-items-center justify-content-center gap-3 mt-3">
                <span class="badge bg-gold-subtle text-gold border border-gold-subtle font-monospace">VERSI DOKUMEN: 2026.2</span>
                <span class="text-dim small">&bull;</span>
                <span class="text-dim small">DIPERBARUI: 6 SEPTEMBER 2026</span>
            </div>
        </div>

        <div class="row g-4 justify-content-center">
            <div class="col-lg-10">
                <!-- Pasal 1 -->
                <div class="apx-legal-card p-4 p-lg-5 mb-4 rounded" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <span class="apx-legal-num text-gold font-monospace fs-4">01</span>
                        <div>
                            <h2 class="h5 text-white mb-0 font-cinzel">Informasi yang Kami Himpun</h2>
                            <span class="text-dim small">Data Esensial untuk Menjalankan Ekosistem Peradaban</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-3" style="line-height: 1.8;">
                        Saat Anda mendaftarkan diri, menautkan akun dengan perintah <code>/link</code>, atau bermain di dalam realm, sistem kami mencatat informasi berikut:
                    </p>
                    <ul class="text-muted small mb-0 ps-3" style="line-height: 1.8;">
                        <li><strong>Identitas Karakter Minecraft:</strong> Nama pengguna (Username) dan nomor pengidentifikasi unik (UUID) resmi dari Mojang atau Floodgate (Bedrock).</li>
                        <li><strong>Kredensial Akun Web:</strong> Alamat surel (email) aktif dan kata sandi yang telah dienkripsi secara satu arah dengan algoritma <code>bcrypt</code> berstandar industri.</li>
                        <li><strong>Data Telemetri Jaringan:</strong> Alamat Protokol Internet (IP) dan catatan waktu login untuk mendeteksi upaya peretasan, serangan DDoS, serta pencegahan multi-akun ilegal (ban evasion).</li>
                    </ul>
                </div>

                <!-- Pasal 2 -->
                <div class="apx-legal-card p-4 p-lg-5 mb-4 rounded" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <span class="apx-legal-num text-gold font-monospace fs-4">02</span>
                        <div>
                            <h2 class="h5 text-white mb-0 font-cinzel">Tujuan &amp; Pemanfaatan Data</h2>
                            <span class="text-dim small">Optimalisasi Layanan &amp; Integritas Permainan</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-3" style="line-height: 1.8;">
                        Data warga diolah secara ketat untuk keperluan operasional teknis berikut:
                    </p>
                    <ul class="text-muted small mb-0 ps-3" style="line-height: 1.8;">
                        <li>Mengonfirmasi dan mendistribusikan kasta donatur, kunci peti pusaka, serta koin yang dipesan melalui webstore secara otomatis via <code>WebBridgeService</code>.</li>
                        <li>Menjaga tatanan sosial server dari pemain toksik, cheater, dan pelaku duplikasi melalui sistem audit log staf terpusat.</li>
                        <li>Mengirimkan notifikasi pemulihan kata sandi atau pengumuman penting seputar pemeliharaan peradaban ke email terdaftar.</li>
                    </ul>
                </div>

                <!-- Pasal 3 -->
                <div class="apx-legal-card p-4 p-lg-5 mb-4 rounded" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <span class="apx-legal-num text-gold font-monospace fs-4">03</span>
                        <div>
                            <h2 class="h5 text-white mb-0 font-cinzel">Keamanan Finansial &amp; Transaksi</h2>
                            <span class="text-dim small">Nol Penyimpanan Informasi Finansial Sensitif</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-0" style="line-height: 1.8;">
                        Apexsions <strong>tidak pernah menyimpan</strong> nomor kartu kredit, debit, kode CVV, atau sandi perbankan Anda di server kami. Seluruh proses checkout dialihkan ke saluran pembayaran resmi terlisensi (Midtrans Gateway) atau konfirmasi manual WhatsApp founder berizin resmi dengan enkripsi TLS end-to-end.
                    </p>
                </div>

                <!-- Pasal 4 -->
                <div class="apx-legal-card p-4 p-lg-5 mb-4 rounded" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <span class="apx-legal-num text-gold font-monospace fs-4">04</span>
                        <div>
                            <h2 class="h5 text-white mb-0 font-cinzel">Kerahasiaan Mutlak Tanpa Pihak Ketiga</h2>
                            <span class="text-dim small">Komitmen Perlindungan Privasi Komunitas</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-0" style="line-height: 1.8;">
                        Kami tidak pernah dan tidak akan pernah menjual, menyewakan, atau membagikan data identitas pribadi warga kepada pihak ketiga manapun untuk tujuan iklan atau pemasaran. Informasi hanya dapat dibuka apabila terdapat instruksi berkekuatan hukum resmi dari otoritas penegak hukum yang berwenang.
                    </p>
                </div>

                <!-- Pasal 5 -->
                <div class="apx-legal-card p-4 p-lg-5 mb-4 rounded" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <span class="apx-legal-num text-gold font-monospace fs-4">05</span>
                        <div>
                            <h2 class="h5 text-white mb-0 font-cinzel">Hak Warga atas Pengendalian Akun</h2>
                            <span class="text-dim small">Otentikasi Dua Faktor (2FA) &amp; Penghapusan Data</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-0" style="line-height: 1.8;">
                        Setiap warga memiliki hak penuh untuk mengakses riwayat akun, memperbarui email, mengaktifkan fitur perlindungan otentikasi dua faktor (2FA), atau meminta penghapusan akun secara permanen melalui <a href="{{ route('profile.index') }}" class="text-gold text-decoration-none">Pengaturan Profil</a>.
                    </p>
                </div>

                <!-- Navigation Footer -->
                <div class="d-flex align-items-center justify-content-between pt-4 border-top border-secondary border-opacity-15 flex-wrap gap-3">
                    <a href="{{ route('terms') }}" class="text-gold text-decoration-none small">
                        <i class="bi bi-file-earmark-lock me-1"></i> Baca Syarat &amp; Ketentuan Layanan Webstore &rarr;
                    </a>
                    <a href="{{ route('rules') }}" class="text-dim text-decoration-none small">
                        <i class="bi bi-shield-check me-1"></i> Baca Peraturan Lengkap Server
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
