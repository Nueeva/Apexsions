@extends('layouts.app')

@section('title', 'Peraturan Server & Sistem Pencegahan')
@section('description', 'Peraturan resmi peradaban Minecraft Apexsions: tata krama chat, integritas gameplay, etika perang kerajaan, perlindungan teritori, dan sanksi pelanggaran.')

@section('content')
<div class="apx-rules-page py-5">
    <div class="container py-4">
        <!-- Breadcrumb -->
        <nav aria-label="breadcrumb" class="mb-4">
            <ol class="breadcrumb apx-breadcrumb mb-0">
                <li class="breadcrumb-item"><a href="{{ route('home') }}" class="text-gold text-decoration-none" data-i18n="nav_home">Beranda</a></li>
                <li class="breadcrumb-item active text-white" aria-current="page" data-i18n="rules_breadcrumb">Peraturan Server</li>
            </ol>
        </nav>

        <!-- Page Header -->
        <div class="apx-section-header text-center mb-5">
            <div class="apx-section-kicker mb-2" data-i18n="rules_header_kicker">TATANAN HUKUM &amp; KEDAULATAN</div>
            <h1 class="apx-section-title display-5 mb-3" data-i18n="rules_header_title">Peraturan Resmi Peradaban Apexsions</h1>
            <p class="text-muted mx-auto" style="max-width: 760px; font-size: 1.05rem; line-height: 1.8;" data-i18n-html="rules_header_desc">
                Demi menjaga keadilan kompetisi, keutuhan tatanan sosial, dan kenyamanan seluruh warga, setiap individu yang melangkahkan kaki di Apexsions terikat oleh hukum peradaban berikut. Seluruh aturan didukung oleh <strong>sistem pencegahan otomatis server</strong> dan audit staf.
            </p>
        </div>

        <!-- Pillar Navigation Bar (Quick Jump) -->
        <div class="d-flex justify-content-center flex-wrap gap-2 mb-5">
            <a href="#pillar-chat" class="btn btn-apx-outline btn-sm py-2 px-3">
                <i class="bi bi-chat-quote-fill me-1 text-gold"></i> <span data-i18n="rules_nav_chat">I. Chat &amp; Sosial</span>
            </a>
            <a href="#pillar-gameplay" class="btn btn-apx-outline btn-sm py-2 px-3">
                <i class="bi bi-shield-shaded me-1 text-gold"></i> <span data-i18n="rules_nav_gameplay">II. Gameplay &amp; Fair Play</span>
            </a>
            <a href="#pillar-ethics" class="btn btn-apx-outline btn-sm py-2 px-3">
                <i class="bi bi-person-check-fill me-1 text-gold"></i> <span data-i18n="rules_nav_ethics">III. Etika &amp; Integritas</span>
            </a>
            <a href="#pillar-security" class="btn btn-apx-outline btn-sm py-2 px-3">
                <i class="bi bi-lock-fill me-1 text-gold"></i> <span data-i18n="rules_nav_security">IV. Keamanan &amp; Hukum</span>
            </a>
            <a href="#pillar-sanctions" class="btn btn-apx-outline btn-sm py-2 px-3">
                <i class="bi bi-hammer me-1 text-gold"></i> <span data-i18n="rules_nav_sanctions">Matriks Sanksi</span>
            </a>
        </div>

        <div class="row g-4 justify-content-center">
            <div class="col-lg-10">

                <!-- ===================================================================
                     PILLAR I: CHAT & KOMUNIKASI
                     =================================================================== -->
                <div class="apx-rule-card mb-5" id="pillar-chat">
                    <div class="apx-rule-card-header">
                        <div class="d-flex align-items-center gap-3">
                            <div class="apx-rule-icon-box">
                                <i class="bi bi-chat-square-text-fill text-gold fs-4"></i>
                            </div>
                            <div>
                                <span class="apx-rule-pillar-tag" data-i18n="rules_p1_tag">BAGIAN PERTAMA</span>
                                <h2 class="apx-rule-pillar-title mb-0" data-i18n="rules_p1_title">I. Aturan Komunikasi &amp; Etika Chat</h2>
                            </div>
                        </div>
                    </div>
                    <div class="apx-rule-card-body">
                        <!-- Rule 1.1 -->
                        <div class="apx-rule-item">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h3 class="apx-rule-item-title" data-i18n="rules_r1_1_title">1. Spam, Flood &amp; Huruf Kapital Berlebihan</h3>
                                <span class="badge bg-warning-subtle text-warning border border-warning-subtle">Sanksi: Mute</span>
                            </div>
                            <p class="apx-rule-item-desc" data-i18n="rules_r1_1_desc">
                                Dilarang keras mengirim pesan berulang-ulang dalam tempo cepat, membombardir kolom obrolan dengan simbol atau spasi kosong, serta menggunakan huruf kapital (Caps-Lock) melebihi batas wajar yang mengganggu kenyamanan membaca pemain lain.
                            </p>
                            <div class="apx-prevention-box">
                                <div class="apx-prevention-header">
                                    <i class="bi bi-shield-check text-emerald me-1"></i> BENTUK PENCEGAHAN SISTEM AKTIF:
                                </div>
                                <div class="apx-prevention-text" data-i18n="rules_r1_1_prev">
                                    Server menjalankan modul <code>SpamChecker</code> otomatis dengan sliding-window limiter (maksimal 3 pesan per 4 detik, jeda minimal 1.2 detik), algoritma kesamaan teks Levenshtein 80%, serta peredam otomatis huruf kapital (Caps dampener) yang secara langsung mengubah teks teriak menjadi huruf kecil normal.
                                </div>
                            </div>
                        </div>

                        <!-- Rule 1.2 -->
                        <div class="apx-rule-item">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h3 class="apx-rule-item-title" data-i18n="rules_r1_2_title">2. Bahasa Kasar, Provokasi &amp; Konten Tidak Pantas</h3>
                                <span class="badge bg-warning-subtle text-warning border border-warning-subtle">Sanksi: Peringatan / Mute</span>
                            </div>
                            <p class="apx-rule-item-desc" data-i18n="rules_r1_2_desc">
                                Dilarang menggunakan makian vulgar, kata-kata kotor, percakapan bermuatan seksual eksplisit, maupun provokasi toksik yang berniat memancing amarah antarpemain baik pada saluran chat global, saluran kerajaan, maupun pesan pribadi (whisper/tell).
                            </p>
                            <div class="apx-prevention-box">
                                <div class="apx-prevention-header">
                                    <i class="bi bi-shield-check text-emerald me-1"></i> BENTUK PENCEGAHAN SISTEM AKTIF:
                                </div>
                                <div class="apx-prevention-text" data-i18n="rules_r1_2_prev">
                                    Enjin <code>ProfanityChecker</code> dengan normalisasi leetspeak secara otomatis memblokir dan menyensor istilah tabu sebelum diteruskan ke layar publik, serta secara senyap menandai akun untuk dipantau oleh asisten moderasi.
                                </div>
                            </div>
                        </div>

                        <!-- Rule 1.3 -->
                        <div class="apx-rule-item">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h3 class="apx-rule-item-title" data-i18n="rules_r1_3_title">3. Bullying, Perilaku Toksik &amp; Pelecehan Personal</h3>
                                <span class="badge bg-danger-subtle text-danger border border-danger-subtle">Sanksi: Mute &bull; Temp Ban</span>
                            </div>
                            <p class="apx-rule-item-desc" data-i18n="rules_r1_3_desc">
                                Dilarang merendahkan martabat, mempermalukan, mengintimidasi, atau melakukan perundungan siber secara terus-menerus kepada warga tertentu. Persaingan kasta atau perang kerajaan harus tetap berlangsung dalam koridor sportivitas bermain peran (roleplay), bukan permusuhan pribadi.
                            </p>
                            <div class="apx-prevention-box">
                                <div class="apx-prevention-header">
                                    <i class="bi bi-shield-check text-emerald me-1"></i> BENTUK PENCEGAHAN SISTEM AKTIF:
                                </div>
                                <div class="apx-prevention-text" data-i18n="rules_r1_3_prev">
                                    Fitur <code>/report &lt;player&gt; &lt;alasan&gt;</code> langsung membekukan rekaman obrolan 50 baris terakhir ke dalam antrean investigasi <code>StaffReportsDesk</code> untuk diverifikasi tanpa celah manipulasi bukti tangkapan layar.
                                </div>
                            </div>
                        </div>

                        <!-- Rule 1.4 -->
                        <div class="apx-rule-item mb-0">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h3 class="apx-rule-item-title" data-i18n="rules_r1_4_title">4. Ujaran Kebencian &amp; Pelecehan SARA / Negara / Keluarga</h3>
                                <span class="badge bg-danger text-white">Sanksi: Permanent Ban</span>
                            </div>
                            <p class="apx-rule-item-desc" data-i18n="rules_r1_4_desc">
                                Dilarang tanpa pengecualian melontarkan ujaran kebencian berlatar belakang Suku, Agama, Ras, Antargolongan (SARA), menistakan simbol kenegaraan, maupun menghina anggota keluarga pemain lain secara tersurat maupun tersirat.
                            </p>
                            <div class="apx-prevention-box">
                                <div class="apx-prevention-header">
                                    <i class="bi bi-shield-check text-emerald me-1"></i> BENTUK PENCEGAHAN SISTEM AKTIF:
                                </div>
                                <div class="apx-prevention-text" data-i18n="rules_r1_4_prev">
                                    Modul <code>HateSpeechChecker</code> menerapkan proteksi blokir instan berbobot tinggi. Pelanggaran kategori ini otomatis menerbitkan peringatan prioritas tinggi ke terminal pengawas dan mematikan akses bicara pemain secara seketika.
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- ===================================================================
                     PILLAR II: GAMEPLAY, INTEGRITAS & FAIR PLAY
                     =================================================================== -->
                <div class="apx-rule-card mb-5" id="pillar-gameplay">
                    <div class="apx-rule-card-header">
                        <div class="d-flex align-items-center gap-3">
                            <div class="apx-rule-icon-box">
                                <i class="bi bi-controller text-gold fs-4"></i>
                            </div>
                            <div>
                                <span class="apx-rule-pillar-tag" data-i18n="rules_p2_tag">BAGIAN KEDUA</span>
                                <h2 class="apx-rule-pillar-title mb-0" data-i18n="rules_p2_title">II. Gameplay, Integritas Wilayah &amp; Fair Play</h2>
                            </div>
                        </div>
                    </div>
                    <div class="apx-rule-card-body">
                        <!-- Rule 2.1 -->
                        <div class="apx-rule-item">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h3 class="apx-rule-item-title" data-i18n="rules_r2_1_title">1. Penggunaan Cheat, Hack Client &amp; Modifikasi Ilegal</h3>
                                <span class="badge bg-danger text-white">Sanksi: Permanent Ban</span>
                            </div>
                            <p class="apx-rule-item-desc" data-i18n="rules_r2_1_desc">
                                Dilarang keras memakai software/mod pihak ketiga yang memberikan keunggulan tidak adil, termasuk namun tidak terbatas pada X-Ray (resource pack tembus pandang atau mod), Baritone, Litematica Printer otomatis, Auto-Clicker, Killaura, Fly, Speed, Jesus, dan free-cam tak resmi.
                            </p>
                            <div class="apx-prevention-box">
                                <div class="apx-prevention-header">
                                    <i class="bi bi-shield-check text-emerald me-1"></i> BENTUK PENCEGAHAN SISTEM AKTIF:
                                </div>
                                <div class="apx-prevention-text" data-i18n="rules_r2_1_prev">
                                    Pencegahan server-side membatasi jangkauan raytrace interaksi blok, menyamarkan data block ore di bawah tanah (anti-xray native engine), membatasi CPS interaksi, serta mendeteksi anomali transmisi paket pergerakan pemain.
                                </div>
                            </div>
                        </div>

                        <!-- Rule 2.2 -->
                        <div class="apx-rule-item">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h3 class="apx-rule-item-title" data-i18n="rules_r2_2_title">2. Eksploitasi Bug, Glitch &amp; Duplikasi Sumber Daya</h3>
                                <span class="badge bg-danger text-white">Sanksi: Rollback Data &bull; Permanent Ban</span>
                            </div>
                            <p class="apx-rule-item-desc" data-i18n="rules_r2_2_desc">
                                Dilarang memanfaatkan kelemahan kode Minecraft, celah game, atau bug plugin untuk menggandakan item, memperbanyak saldo Rupiah, atau menerobos batasan sistem. Segala temuan bug wajib dilaporkan kepada pengelola server demi integritas bersama.
                            </p>
                            <div class="apx-prevention-box">
                                <div class="apx-prevention-header">
                                    <i class="bi bi-shield-check text-emerald me-1"></i> BENTUK PENCEGAHAN SISTEM AKTIF:
                                </div>
                                <div class="apx-prevention-text" data-i18n="rules_r2_2_prev">
                                    Modul ekonomi <code>ApexsionsEconomy</code> menerapkan arsitektur transaksi atomik ACID dengan penguncian state (lock-boundary). Item dan saldo tidak pernah dapat dipindahkan secara ganda dalam satuan tick yang sama.
                                </div>
                            </div>
                        </div>

                        <!-- Rule 2.3 -->
                        <div class="apx-rule-item">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h3 class="apx-rule-item-title" data-i18n="rules_r2_3_title">3. Griefing &amp; Pencurian di Luar Mekanisme Perang Resmi</h3>
                                <span class="badge bg-danger-subtle text-danger border border-danger-subtle">Sanksi: Rollback Wilayah &bull; Temp Ban</span>
                            </div>
                            <p class="apx-rule-item-desc" data-i18n="rules_r2_3_desc">
                                Dilarang merusak, membongkar paksa, atau mencuri harta di dalam wilayah kerajaan berdaulat dan rumah warga lain tanpa izin pemilik, kecuali saat peristiwa resmi Kingdom War dideklarasikan sesuai protokol perang.
                            </p>
                            <div class="apx-prevention-box">
                                <div class="apx-prevention-header">
                                    <i class="bi bi-shield-check text-emerald me-1"></i> BENTUK PENCEGAHAN SISTEM AKTIF:
                                </div>
                                <div class="apx-prevention-text" data-i18n="rules_r2_3_prev">
                                    Sistem proteksi <code>TerritoryListener</code> dan <code>RegionManager</code> secara otomatis membatalkan event pembongkaran blok, penempatan blok, dan pembukaan peti oleh pemain non-warga wilayah berdaulat.
                                </div>
                            </div>
                        </div>

                        <!-- Rule 2.4 -->
                        <div class="apx-rule-item">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h3 class="apx-rule-item-title" data-i18n="rules_r2_4_title">4. Eksploitasi Redstone &amp; Mesin Perontok Server (Lag Machine)</h3>
                                <span class="badge bg-warning-subtle text-warning border border-warning-subtle">Sanksi: Sita Mesin &bull; Temp Ban</span>
                            </div>
                            <p class="apx-rule-item-desc" data-i18n="rules_r2_4_desc">
                                Dilarang membuat clock loop redstone tanpa tombol pemutus, mesin pelempar item berlebihan, instalasi piston masif tanpa peredam, atau mekanisme yang sengaja dirancang untuk menurunkan TPS (Ticks Per Second) server.
                            </p>
                            <div class="apx-prevention-box">
                                <div class="apx-prevention-header">
                                    <i class="bi bi-shield-check text-emerald me-1"></i> BENTUK PENCEGAHAN SISTEM AKTIF:
                                </div>
                                <div class="apx-prevention-text" data-i18n="rules_r2_4_prev">
                                    Sistem watchdog otomatis membatasi frekuensi pergantian sinyal redstone. Sirkuit loop yang berdetik melebihi batas toleransi per tick server akan otomatis dibekukan dan diputus demi kestabilan 20 TPS server.
                                </div>
                            </div>
                        </div>

                        <!-- Rule 2.5 -->
                        <div class="apx-rule-item mb-0">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h3 class="apx-rule-item-title" data-i18n="rules_r2_5_title">5. Combat Logging (Kabur Saat Bertarung)</h3>
                                <span class="badge bg-warning-subtle text-warning border border-warning-subtle">Sanksi: Kematian Otomatis &bull; Drop Item</span>
                            </div>
                            <p class="apx-rule-item-desc" data-i18n="rules_r2_5_desc">
                                Dilarang keluar dari server secara sengaja (disconnect/alt+F4) saat sedang berada dalam pertempuran aktif melawan pemain lain guna menghindari kematian atau kehilangan perlengkapan.
                            </p>
                            <div class="apx-prevention-box">
                                <div class="apx-prevention-header">
                                    <i class="bi bi-shield-check text-emerald me-1"></i> BENTUK PENCEGAHAN SISTEM AKTIF:
                                </div>
                                <div class="apx-prevention-text" data-i18n="rules_r2_5_prev">
                                    Modul <code>CombatTag</code> mengunci status pertarungan selama 15 detik. Jika pemain disconnect saat combat tag menyala, server otomatis mengeksekusi karakter, mendrop seluruh inventory di titik logout, dan mencatat status kekalahan.
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- ===================================================================
                     PILLAR III: ETIKA, INTEGRITAS & AKUN
                     =================================================================== -->
                <div class="apx-rule-card mb-5" id="pillar-ethics">
                    <div class="apx-rule-card-header">
                        <div class="d-flex align-items-center gap-3">
                            <div class="apx-rule-icon-box">
                                <i class="bi bi-person-lines-fill text-gold fs-4"></i>
                            </div>
                            <div>
                                <span class="apx-rule-pillar-tag" data-i18n="rules_p3_tag">BAGIAN KETIGA</span>
                                <h2 class="apx-rule-pillar-title mb-0" data-i18n="rules_p3_title">III. Etika, Integritas Akun &amp; Staf</h2>
                            </div>
                        </div>
                    </div>
                    <div class="apx-rule-card-body">
                        <!-- Rule 3.1 -->
                        <div class="apx-rule-item">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h3 class="apx-rule-item-title" data-i18n="rules_r3_1_title">1. Berbohong kepada Staf &amp; Penghindaran Sanksi (Ban Evasion)</h3>
                                <span class="badge bg-danger text-white">Sanksi: Blacklist Permanen</span>
                            </div>
                            <p class="apx-rule-item-desc" data-i18n="rules_r3_1_desc">
                                Dilarang memberikan keterangan palsu dalam proses investigasi staf, memalsukan bukti laporan, atau membuat akun baru/alternatif untuk bermain kembali saat akun utama sedang menjalani masa hukuman sanksi.
                            </p>
                            <div class="apx-prevention-box">
                                <div class="apx-prevention-header">
                                    <i class="bi bi-shield-check text-emerald me-1"></i> BENTUK PENCEGAHAN SISTEM AKTIF:
                                </div>
                                <div class="apx-prevention-text" data-i18n="rules_r3_1_prev">
                                    Pemeriksaan authoritative UUID dan jejak koneksi IP mendeteksi hubungan akun alternatif secara otomatis. Login baru yang terasosiasi dengan akun terkena sanksi aktif akan otomatis ditolak oleh gerbang inisiasi.
                                </div>
                            </div>
                        </div>

                        <!-- Rule 3.2 -->
                        <div class="apx-rule-item">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h3 class="apx-rule-item-title" data-i18n="rules_r3_2_title">2. Batasan Multi-Akun (Alt Accounts)</h3>
                                <span class="badge bg-warning-subtle text-warning border border-warning-subtle">Sanksi: Pembekuan Akun Alt</span>
                            </div>
                            <p class="apx-rule-item-desc" data-i18n="rules_r3_2_desc">
                                Setiap pemain hanya diperkenankan memiliki maksimal 2 (dua) akun Minecraft pribadi. Dilarang menggunakan akun alternatif untuk menimbun starter kit, menduduki kasta ganda di kerajaan berlawanan, atau memanipulasi voting dan pasar lelang.
                            </p>
                            <div class="apx-prevention-box">
                                <div class="apx-prevention-header">
                                    <i class="bi bi-shield-check text-emerald me-1"></i> BENTUK PENCEGAHAN SISTEM AKTIF:
                                </div>
                                <div class="apx-prevention-text" data-i18n="rules_r3_2_prev">
                                    Protokol penautan <code>/link</code> portal web menerapkan pembatasan satu akun berdaulat terverifikasi per kartu warga guna menjaga pemerataan perolehan reward harian dan integritas ekonomi.
                                </div>
                            </div>
                        </div>

                        <!-- Rule 3.3 -->
                        <div class="apx-rule-item mb-0">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h3 class="apx-rule-item-title" data-i18n="rules_r3_3_title">3. Penyamaran Identitas &amp; Penghormatan Staf</h3>
                                <span class="badge bg-danger-subtle text-danger border border-danger-subtle">Sanksi: Mute &bull; Temp Ban</span>
                            </div>
                            <p class="apx-rule-item-desc" data-i18n="rules_r3_3_desc">
                                Dilarang meniru nama, prefix, gelar, atau berpura-pura menjadi anggota staf Apexsions (Warden, Herald, Overseer, Architect, Ancestor) untuk memeras atau memperdaya warga. Kritik terhadap keputusan staf disampaikan secara beradab melalui tiket resmi.
                            </p>
                            <div class="apx-prevention-box">
                                <div class="apx-prevention-header">
                                    <i class="bi bi-shield-check text-emerald me-1"></i> BENTUK PENCEGAHAN SISTEM AKTIF:
                                </div>
                                <div class="apx-prevention-text" data-i18n="rules_r3_3_prev">
                                    Sistem hirarki kasta <code>ranks.yml</code> memblokir perubahan display name atau prefix nickname (<code>/nick</code>) yang mengandung kata kunci staf yang telah diproteksi oleh izin otoritas.
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- ===================================================================
                     PILLAR IV: KEAMANAN, IKLAN & KEPATUHAN HUKUM
                     =================================================================== -->
                <div class="apx-rule-card mb-5" id="pillar-security">
                    <div class="apx-rule-card-header">
                        <div class="d-flex align-items-center gap-3">
                            <div class="apx-rule-icon-box">
                                <i class="bi bi-shield-lock-fill text-gold fs-4"></i>
                            </div>
                            <div>
                                <span class="apx-rule-pillar-tag" data-i18n="rules_p4_tag">BAGIAN KEEMPAT</span>
                                <h2 class="apx-rule-pillar-title mb-0" data-i18n="rules_p4_title">IV. Keamanan, Iklan &amp; Kepatuhan Hukum Negara</h2>
                            </div>
                        </div>
                    </div>
                    <div class="apx-rule-card-body">
                        <!-- Rule 4.1 -->
                        <div class="apx-rule-item">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h3 class="apx-rule-item-title" data-i18n="rules_r4_1_title">1. Promosi &amp; Iklan Server Luar Tanpa Izin</h3>
                                <span class="badge bg-danger text-white">Sanksi: Mute Permanen &bull; Banned</span>
                            </div>
                            <p class="apx-rule-item-desc" data-i18n="rules_r4_1_desc">
                                Dilarang menyebarkan alamat IP server Minecraft lain, link komunitas Discord luar, atau tautan promosi komersial tanpa persetujuan resmi dari pihak pengelola Apexsions.
                            </p>
                            <div class="apx-prevention-box">
                                <div class="apx-prevention-header">
                                    <i class="bi bi-shield-check text-emerald me-1"></i> BENTUK PENCEGAHAN SISTEM AKTIF:
                                </div>
                                <div class="apx-prevention-text" data-i18n="rules_r4_1_prev">
                                    Modul <code>AdvertisementChecker</code> memindai pesan secara real-time dengan regex IPv4, nama domain global dan nasional (termasuk <code>.my.id</code>, <code>.id</code>, <code>.com</code>, dll.), serta link undangan Discord luar, dan langsung membatalkan pengiriman pesan ke publik.
                                </div>
                            </div>
                        </div>

                        <!-- Rule 4.2 -->
                        <div class="apx-rule-item">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h3 class="apx-rule-item-title" data-i18n="rules_r4_2_title">2. Doxxing, Pembocoran Data Pribadi &amp; Ancaman Serangan Cyber</h3>
                                <span class="badge bg-danger text-white">Sanksi: Permanent Ban &bull; Hukum Pidana</span>
                            </div>
                            <p class="apx-rule-item-desc" data-i18n="rules_r4_2_desc">
                                Dilarang keras mempublikasikan informasi data pribadi orang lain (nama asli, alamat rumah, nomor kontak, media sosial privat) tanpa izin, serta dilarang melontarkan ancaman serangan DDoS atau eksploitasi jaringan.
                            </p>
                            <div class="apx-prevention-box">
                                <div class="apx-prevention-header">
                                    <i class="bi bi-shield-check text-emerald me-1"></i> BENTUK PENCEGAHAN SISTEM AKTIF:
                                </div>
                                <div class="apx-prevention-text" data-i18n="rules_r4_2_prev">
                                    Infrastruktur Apexsions dilindungi mitigasi DDoS tingkat enterprise, enkripsi SSL/TLS berlapis, dan kebijakan isolasi data sensitif di mana credential pemain tidak pernah terekspos di log obrolan server.
                                </div>
                            </div>
                        </div>

                        <!-- Rule 4.3 -->
                        <div class="apx-rule-item mb-0">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h3 class="apx-rule-item-title" data-i18n="rules_r4_3_title">3. Penipuan Transaksi Riil &amp; Pelanggaran UU ITE</h3>
                                <span class="badge bg-danger text-white">Sanksi: Permanent Ban</span>
                            </div>
                            <p class="apx-rule-item-desc" data-i18n="rules_r4_3_desc">
                                Segala bentuk penipuan finansial uang riil (Rupiah), jual beli akun curian, transaksi pasar gelap di luar Webstore resmi, atau aktivitas yang melanggar Undang-Undang Informasi dan Transaksi Elektronik (UU ITE) Republik Indonesia dilarang mutlak.
                            </p>
                            <div class="apx-prevention-box">
                                <div class="apx-prevention-header">
                                    <i class="bi bi-shield-check text-emerald me-1"></i> BENTUK PENCEGAHAN SISTEM AKTIF:
                                </div>
                                <div class="apx-prevention-text" data-i18n="rules_r4_3_prev">
                                    Semua transaksi webstore diproses melalui gateway pembayaran resmi bersertifikat dengan pencatatan audit log permanen dan sistem pengiriman item terverifikasi langsung ke inventori in-game.
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- ===================================================================
                     SANCTIONS MATRIX TABLE
                     =================================================================== -->
                <div class="apx-rule-card mb-5" id="pillar-sanctions">
                    <div class="apx-rule-card-header">
                        <div class="d-flex align-items-center gap-3">
                            <div class="apx-rule-icon-box">
                                <i class="bi bi-hammer text-gold fs-4"></i>
                            </div>
                            <div>
                                <span class="apx-rule-pillar-tag" data-i18n="rules_sanction_tag">STANDAR YURISDIKSI</span>
                                <h2 class="apx-rule-pillar-title mb-0" data-i18n="rules_sanction_title">Matriks Tingkatan Sanksi &amp; Eskalasi</h2>
                            </div>
                        </div>
                    </div>
                    <div class="apx-rule-card-body p-0">
                        <div class="table-responsive">
                            <table class="table apx-sanction-table mb-0">
                                <thead>
                                    <tr>
                                        <th style="width: 20%;" data-i18n="rules_th_level">Tingkat Sanksi</th>
                                        <th style="width: 40%;" data-i18n="rules_th_type">Jenis Pelanggaran Terkait</th>
                                        <th style="width: 40%;" data-i18n="rules_th_action">Tindakan Penegakan Hukum</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td><span class="badge bg-info-subtle text-info border border-info-subtle" data-i18n="rules_t1_badge">Tingkat I</span></td>
                                        <td data-i18n="rules_t1_type">Spam ringan, Caps-Lock berlebihan, pelanggaran format chat.</td>
                                        <td data-i18n="rules_t1_action">Peringatan tertulis (Warning) &bull; Mute otomatis 15–60 detik oleh sistem.</td>
                                    </tr>
                                    <tr>
                                        <td><span class="badge bg-warning-subtle text-warning border border-warning-subtle" data-i18n="rules_t2_badge">Tingkat II</span></td>
                                        <td data-i18n="rules_t2_type">Toksisitas berulang, provokasi vulgar, AFK farming tanpa batas, redstone lag ringan.</td>
                                        <td data-i18n="rules_t2_action">Mute obrolan 1 hingga 24 jam &bull; Kick dari server &bull; Pembersihan mesin lag.</td>
                                    </tr>
                                    <tr>
                                        <td><span class="badge bg-danger-subtle text-danger border border-danger-subtle" data-i18n="rules_t3_badge">Tingkat III</span></td>
                                        <td data-i18n="rules_t3_type">Griefing wilayah, pelecehan personal berat, combat logging berulang, promosi server lain.</td>
                                        <td data-i18n="rules_t3_action">Temporary Banned 3 hingga 14 hari &bull; Rollback data kerusakan wilayah.</td>
                                    </tr>
                                    <tr>
                                        <td><span class="badge bg-danger text-white" data-i18n="rules_t4_badge">Tingkat IV</span></td>
                                        <td data-i18n="rules_t4_type">Cheat/Hack client, duplikasi item, doxxing, SARA ekstrem, penipuan finansial, ban evasion.</td>
                                        <td data-i18n="rules_t4_action">Permanent Banned &bull; IP/UUID Blacklist &bull; Penyitaan total aset in-game.</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- Callout: Cara Mengajukan Banding Sanksi -->
                <div class="p-4 rounded position-relative overflow-hidden" style="background: linear-gradient(135deg, rgba(212, 163, 89, 0.1) 0%, rgba(13, 18, 30, 0.85) 100%); border: 1px solid var(--apx-gold-border); border-radius: var(--apx-radius-md);">
                    <div class="row align-items-center">
                        <div class="col-lg-8 text-lg-start mb-3 mb-lg-0">
                            <span class="badge mb-2" style="background: rgba(212, 163, 89, 0.2); color: var(--apx-gold-light); border: 1px solid var(--apx-gold-border); font-size: 0.72rem; letter-spacing: 0.08em;">
                                <i class="bi bi-envelope-paper-heart-fill me-1"></i> <span data-i18n="rules_appeal_tag">MEKANISME BANDING</span>
                            </span>
                            <h3 class="mb-1 text-white" style="font-family: 'Cinzel', Georgia, serif; font-size: 1.35rem;" data-i18n="rules_appeal_title">
                                Merasa Mendapat Sanksi yang Kurang Tepat?
                            </h3>
                            <p class="text-muted small mb-0" data-i18n="rules_appeal_desc">
                                Setiap warga berhak memperoleh keadilan. Ajukan banding resmi dengan menyertakan bukti kronologis melalui meja tiket staf di peladen Discord resmi Apexsions.
                            </p>
                        </div>
                        <div class="col-lg-4 text-lg-end">
                            <a href="https://discord.gg/apexsions" target="_blank" rel="noopener noreferrer" class="btn btn-apx-gold">
                                <i class="bi bi-discord me-1"></i> <span data-i18n="rules_appeal_btn">Buka Tiket Banding</span>
                            </a>
                        </div>
                    </div>
                </div>

            </div>
        </div>
    </div>
</div>
@endsection
