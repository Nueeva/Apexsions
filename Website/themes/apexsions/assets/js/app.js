// ==========================================================================
// Apexsions Bilingual Internationalization Engine (ID / EN)
// ==========================================================================
const APX_I18N = {
    id: {
        // Navigation
        nav_home: 'Beranda',
        nav_shop: 'Webstore',
        nav_features: 'Fitur',
        nav_wiki: 'Wiki',
        nav_leaderboard: 'Leaderboard',
        nav_rules: 'Peraturan',
        nav_vote: 'Vote',
        nav_discord: 'Discord',
        nav_login: 'Masuk',
        nav_register: 'Daftar',
        nav_acc_registered: 'AKUN TERDAFTAR',
        nav_profile: 'Profil Pemain',
        nav_link_mc: 'Tautkan Minecraft',
        nav_order_history: 'Riwayat Belanja',
        nav_admin_panel: 'Admin Panel',
        nav_logout: 'Keluar',

        // Hero
        hero_brand_mark: 'APEXSIONS • THE PEAK CIVILIZATIONS',
        hero_axis_foundation: 'FONDASI',
        hero_axis_nobles: 'BANGSAWAN',
        hero_axis_admin: 'ADMINISTRASI',
        hero_axis_authority: 'OTORITAS',
        hero_axis_apex: 'PUNCAK',
        hero_headline: 'Peradaban Berdaulat yang Dibangun di Atas Tatanan Hierarki.',
        hero_subtext: 'Sebelas kasta sosial, tiga wilayah kerajaan, dan satu dunia yang dibentuk seutuhnya oleh sejarah warganya.',
        hero_btn_explore: 'JELAJAHI PERADABAN',
        hero_ip_label: 'SERVER IP',
        hero_status_online: 'SERVER ONLINE',
        hero_status_offline: 'SERVER OFFLINE',
        hero_status_ready: 'Gerbang Terbuka • Siap Menjelajah',
        hero_status_resting: 'Dunia Sedang Beristirahat',
        hero_status_maintenance: 'Sedang Pemeliharaan',
        hero_status_crossplay_ready: 'Java & Bedrock Siap',
        hero_citizens: 'Warga',
        hero_copied: 'Disalin!',

        // World & Lore
        world_kicker: 'LORE & TATANAN WILAYAH',
        world_title: 'Runtuhnya Kekaisaran Sions & Eksodus Tiga Kerajaan',
        world_lead: 'Dahulu kala, satu peradaban tunggal yang mahaluas—<strong>Kekaisaran Sions</strong>—menaungi seluruh penjuru realm dalam kemakmuran tanpa pernah menyentuh ilmu hitam. Namun kehancuran tak terelakkan tiba ketika ambisi pemimpinnya memaksakan penggunaan energi terlarang dari <strong>Dimensi Kegelapan (Dark Dimension)</strong> demi melipatgandakan kekuatan pasukan. Kekuatan gelap yang tak terkendali meremukkan ibukota agung kekaisaran dan memicu <em>Eksodus Akbar</em> ke tiga penjuru mata angin: dinasti kerajaan melarikan diri ke timur (<strong>Zenithar</strong>), kaum pekerja dan prajurit bertahan hidup ke rimba barat (<strong>Sylvamoor</strong>), serta para pesulap agung dan prajurit tempur terkuat hijrah ke cadas selatan (<strong>Solterra</strong>).',

        // Kingdoms
        label_characteristics: 'Karakteristik Wilayah',
        label_buffs: 'Buff Kondisi Sejarah',
        label_debuffs: 'Debuff Kondisi Fisik',
        zenithar_type: 'TERITORI TIMUR • DINASTI KERAJAAN',
        zenithar_desc: 'Dipimpin oleh keluarga dinasti dan pengawal elit Kekaisaran Sions yang berhasil mempertahankan diri dari malapetaka, mengungsi ke timur menuju puncak cakrawala (Zenith). Menjunjung tinggi kemurnian tata krama istana, kavaleri suci, dan kubah menara langit.',
        zenithar_spec1: 'Puncak Cakrawala & Solarium Spire Citadel',
        zenithar_spec2: 'Penambangan Emas Murni & Arsitektur Megah',
        zenithar_buffs: '<span class="text-success fw-semibold">+5% Speed</span>, <span class="text-success fw-semibold">+7% Luck</span>, <span class="text-success fw-semibold">+6% All Damage &amp; Defense</span>, <span class="text-success fw-semibold">-5% Reduksi Serangan Kritis Musuh</span>.',
        zenithar_debuffs: '<span class="text-danger fw-semibold">+7% Kerentanan Racun</span>, makanan memulihkan hunger lebih lambat (-1 point) karena terbiasa dengan santapan istana steril.',
        zenithar_link: 'Arsip Zenithar',

        solterra_type: 'TERITORI SELATAN • MAGICIAN & VETERAN',
        solterra_desc: 'Dibentuk oleh para pesulap tempur agung (arcanists) serta prajurit garis depan terkuat bekas legiun Sions yang bermigrasi ke wilayah selatan. Memadukan kedahsyatan sihir elemen api dengan kekuatan fisik brutal tanpa ampun untuk menaklukkan alam yang mematikan.',
        solterra_spec1: 'Cadas Vulkanik, Kawah Lahar & Ignis Bastion',
        solterra_spec2: 'Penempaan Senjata Berat & Nilai Jual Ore Tinggi',
        solterra_buffs: '<span class="text-success fw-semibold">+15% Total Damage</span>, <span class="text-success fw-semibold">+10% Critical Damage</span>, <span class="text-success fw-semibold">+10% Mining Speed</span>, <span class="text-success fw-semibold">Rasio Jual Ore Tinggi (65%)</span>.',
        solterra_debuffs: '<span class="text-danger fw-semibold">-2 HP Maksimal (9 Hati)</span>, <span class="text-danger fw-semibold">+8% Damage Masuk</span>, <span class="text-danger fw-semibold">+7% Cepat Lapar</span>, lahan pertanian cepat kering di tanah cadas.',
        solterra_link: 'Arsip Solterra',

        sylvamoor_type: 'TERITORI BARAT • PEKERJA & PEJUANG RIMBA',
        sylvamoor_desc: 'Dibangun oleh kaum pekerja, pembangun, petani lumbung, serta prajurit garda rakyat (pengguna sihir alam dasar dan prajurit non-sihir) yang mengungsi ke belantara rimba barat. Mengisolasi diri dari ambisi kekuasaan dan hidup selaras menjaga kelestarian Pohon Dunia.',
        sylvamoor_spec1: 'Rimba Kanopi Purba, Samudra Kristal & Eldergrove',
        sylvamoor_spec2: 'Lumbung Agrikultur, Alkemis Herbal & Pangan',
        sylvamoor_buffs: '<span class="text-success fw-semibold">+2 HP Maksimal (11 Hati)</span>, <span class="text-success fw-semibold">+12% Luck</span>, <span class="text-success fw-semibold">+7% Extra Mob Drop</span>, <span class="text-success fw-semibold">Defense Tinggi (~12.6%)</span>, <span class="text-success fw-semibold">Kelembapan Lahan Abadi</span>.',
        sylvamoor_debuffs: '<span class="text-danger fw-semibold">Mabuk Ketinggian di Y > 110</span> (Hunger/Weakness), <span class="text-danger fw-semibold">+15% Damage Terbakar Api</span>, <span class="text-danger fw-semibold">-10% Serangan PvP & Mining</span>.',
        sylvamoor_link: 'Arsip Sylvamoor',

        // Ledger
        ledger_title_war: 'Perjanjian Kedaulatan & Kingdom War',
        ledger_badge_war: 'DEKRIT REALM',
        ledger_desc_war: 'Setiap warga bebas memilih baiat kepada satu kerajaan. Pertahankan perbatasan teritori, bangun benteng pertahanan, dan rebut supremasi pada Kingdom War mingguan.',
        ledger_link_war: 'Dekrit Kedaulatan Kerajaan',
        ledger_title_econ: 'Ekonomi Pasar Terbuka & Escrow',
        ledger_badge_econ: 'DUAL-CURRENCY',
        ledger_desc_econ: 'Sistem transaksi ganda Rupiah (Rp) dan Diamond 💎. Kuasai pasar dinamis berbasis supply-demand (/shop), lelang aman Escrow, dan pertukaran barter langsung lintas kerajaan.',
        ledger_link_econ: 'Mekanisme Pasar & Escrow',
        ledger_link_econ_shop: 'Kunjungi Toko Peradaban',
        ledger_title_ench: 'Sistem Penempaan & 182 Enchants',
        ledger_badge_ench: '7 TIER KEKUATAN',
        ledger_desc_ench: 'Tujuh tingkatan custom enchants melalui Alchemist dan Tinkerer, dipadukan dengan set bonus armor legendaris yang seimbang dan teruji untuk pertempuran kedaulatan.',
        ledger_link_ench: 'Ensiklopedia Penempaan',
        realm_tag: 'ARSIP WILAYAH REALM',
        realm_title: 'BENTANG ALAM PERADABAN',
        realm_sub: 'Dari benteng ibukota yang kokoh hingga hamparan alam liar yang menanti untuk ditaklukkan.',

        // Caste Hierarchy
        caste_kicker: 'TATANAN SOSIAL',
        caste_title: 'Tatanan Sebelas Kasta Sosial',
        caste_lead: 'Dari pijakan awal warga perintis hingga tahta tertinggi sang leluhur pendiri kerajaan, setiap kasta memikul bobot kedaulatan, hak wilayah, dan kehormatan yang terukur.',
        caste_filter_all: 'SEMUA KASTA (11)',
        caste_filter_apex: 'PUNCAK LELUHUR (1)',
        caste_filter_authority: 'DEWAN OTORITAS (2)',
        caste_filter_staff: 'ADMINISTRASI & STAF (2)',
        caste_filter_nobles: 'ORDO BANGSAWAN (5)',
        caste_filter_pioneers: 'WARGA PERINTIS (1)',

        // Onboarding
        step_kicker: 'GERBANG INISIASI',
        step_title: 'Tata Cara Masuk ke Peradaban',
        step_lead: 'Tiga langkah sederhana untuk menghubungkan klien Minecraft dan mencatatkan namamu dalam sejarah warga berdaulat.',
        step1_phase: 'TAHAP PERTAMA',
        step1_title: 'Klien Minecraft 26.2',
        step1_desc: 'Gunakan Minecraft versi resmi atau launcher pilihanmu pada versi <strong>26.2</strong>. Mendukung penuh koneksi <strong>Java Edition &amp; Bedrock Edition</strong>.',
        step1_meta: 'Java & Bedrock Crossplay',
        step2_phase: 'TAHAP KEDUA',
        step2_title: 'Alamat Server & Port',
        step2_desc: 'Buka menu Multiplayer dan masukkan alamat server <code>apexsions.my.id:32348</code>. Untuk pemain Bedrock, masukkan IP <code>apexsions.my.id</code> dengan Port <code>32348</code>.',
        step2_copy_java: 'Salin Java • apexsions.my.id:32348',
        step2_copy_bedrock: 'Salin Bedrock • IP: apexsions.my.id | Port: 32348',
        step3_phase: 'TAHAP KETIGA',
        step3_title: 'Autentikasi Akun (/link)',
        step3_desc: 'Setelah berada di lobi server, ketik perintah <code>/link</code> untuk menerima kode autentikasi rahasia guna menautkan akun dengan portal web.',
        step3_btn_link: 'Buka Portal Tautkan',
        step3_registered: 'Akun Anda Terdaftar',
        step3_btn_register: 'Daftar Akun Peradaban',
        guide_callout_badge: 'PANDUAN KILAT WARGA BARU',
        guide_callout_title: 'Bingung Harus Melakukan Apa Setelah Mendarat di Spawn?',
        guide_callout_desc: 'Pelajari peta jalan 15 menit dari mengambil starter kit, memilih kerajaan, menandai rumah, hingga menghasilkan pundi Rupiah pertama.',
        guide_callout_btn: 'Buka Panduan 15 Menit',

        // Footer
        footer_desc: 'Apexsions adalah peradaban berdaulat yang dibentuk oleh komunitas. Tatanan kerajaan otonom, sistem pasar atomik, dan progres kasta sosial yang kompetitif di atas Minecraft 26.2.',
        footer_subline: 'MINECRAFT 26.2 • REALM BERDAULAT',
        footer_col_civ: 'PERADABAN',
        footer_col_guides: 'PANDUAN & ATURAN',
        footer_col_community: 'DIPLOMASI KOMUNITAS',
        footer_three_kingdoms: 'Tiga Kerajaan',
        footer_caste_hierarchy: 'Hierarki Kasta',
        footer_wiki_archive: 'Arsip Wiki',
        footer_vote_support: 'Dukung Vote',
        footer_how_to_join: 'Cara Bergabung',
        footer_encyclopedia: 'Ensiklopedia Pemain',
        footer_official_rules: 'Peraturan Resmi',
        footer_terms_trans: 'Ketentuan Transaksi',
        footer_help_center: 'Pusat Bantuan',
        footer_copyright: 'Apexsions. Seluruh hak cipta dilindungi.',
        footer_sub_copyright: 'Dibangun bersama komunitas, untuk peradaban yang berdaulat.',
        footer_terms: 'Syarat & Ketentuan',
        footer_privacy: 'Kebijakan Privasi',
        footer_contact: 'Kontak',
        // Rules Page
        rules_breadcrumb: 'Peraturan Server',
        rules_header_kicker: 'TATANAN HUKUM & KEDAULATAN',
        rules_header_title: 'Peraturan Resmi Peradaban Apexsions',
        rules_header_desc: 'Demi menjaga keadilan kompetisi, keutuhan tatanan sosial, dan kenyamanan seluruh warga, setiap individu yang melangkahkan kaki di Apexsions terikat oleh hukum peradaban berikut. Seluruh aturan didukung oleh <strong>sistem pencegahan otomatis server</strong> dan audit staf.',
        rules_nav_chat: 'I. Chat & Sosial',
        rules_nav_gameplay: 'II. Gameplay & Fair Play',
        rules_nav_ethics: 'III. Etika & Integritas',
        rules_nav_security: 'IV. Keamanan & Hukum',
        rules_nav_sanctions: 'Matriks Sanksi',
        rules_p1_tag: 'BAGIAN PERTAMA',
        rules_p1_title: 'I. Aturan Komunikasi & Etika Chat',
        rules_r1_1_title: '1. Spam, Flood & Huruf Kapital Berlebihan',
        rules_r1_1_desc: 'Dilarang keras mengirim pesan berulang-ulang dalam tempo cepat, membombardir kolom obrolan dengan simbol atau spasi kosong, serta menggunakan huruf kapital (Caps-Lock) melebihi batas wajar yang mengganggu kenyamanan membaca pemain lain.',
        rules_r1_1_prev: 'Server menjalankan modul SpamChecker otomatis dengan sliding-window limiter (maksimal 3 pesan per 4 detik, jeda minimal 1.2 detik), algoritma kesamaan teks Levenshtein 80%, serta peredam otomatis huruf kapital (Caps dampener) yang secara langsung mengubah teks teriak menjadi huruf kecil normal.',
        rules_r1_2_title: '2. Bahasa Kasar, Provokasi & Konten Tidak Pantas',
        rules_r1_2_desc: 'Dilarang menggunakan makian vulgar, kata-kata kotor, percakapan bermuatan seksual eksplisit, maupun provokasi toksik yang berniat memancing amarah antarpemain baik pada saluran chat global, saluran kerajaan, maupun pesan pribadi (whisper/tell).',
        rules_r1_2_prev: 'Enjin ProfanityChecker dengan normalisasi leetspeak secara otomatis memblokir dan menyensor istilah tabu sebelum diteruskan ke layar publik, serta secara senyap menandai akun untuk dipantau oleh asisten moderasi.',
        rules_r1_3_title: '3. Bullying, Perilaku Toksik & Pelecehan Personal',
        rules_r1_3_desc: 'Dilarang merendahkan martabat, mempermalukan, mengintimidasi, atau melakukan perundungan siber secara terus-menerus kepada warga tertentu. Persaingan kasta atau perang kerajaan harus tetap berlangsung dalam koridor sportivitas bermain peran (roleplay), bukan permusuhan pribadi.',
        rules_r1_3_prev: 'Fitur /report <player> <alasan> langsung membekukan rekaman obrolan 50 baris terakhir ke dalam antrean investigasi StaffReportsDesk untuk diverifikasi tanpa celah manipulasi bukti tangkapan layar.',
        rules_r1_4_title: '4. Ujaran Kebencian & Pelecehan SARA / Negara / Keluarga',
        rules_r1_4_desc: 'Dilarang tanpa pengecualian melontarkan ujaran kebencian berlatar belakang Suku, Agama, Ras, Antargolongan (SARA), menistakan simbol kenegaraan, maupun menghina anggota keluarga pemain lain secara tersurat maupun tersirat.',
        rules_r1_4_prev: 'Modul HateSpeechChecker menerapkan proteksi blokir instan berbobot tinggi. Pelanggaran kategori ini otomatis menerbitkan peringatan prioritas tinggi ke terminal pengawas dan mematikan akses bicara pemain secara seketika.',
        rules_p2_tag: 'BAGIAN KEDUA',
        rules_p2_title: 'II. Gameplay, Integritas Wilayah & Fair Play',
        rules_r2_1_title: '1. Penggunaan Cheat, Hack Client & Modifikasi Ilegal',
        rules_r2_1_desc: 'Dilarang keras memakai software/mod pihak ketiga yang memberikan keunggulan tidak adil, termasuk namun tidak terbatas pada X-Ray (resource pack tembus pandang atau mod), Baritone, Litematica Printer otomatis, Auto-Clicker, Killaura, Fly, Speed, Jesus, dan free-cam tak resmi.',
        rules_r2_1_prev: 'Pencegahan server-side membatasi jangkauan raytrace interaksi blok, menyamarkan data block ore di bawah tanah (anti-xray native engine), membatasi CPS interaksi, serta mendeteksi anomali transmisi paket pergerakan pemain.',
        rules_r2_2_title: '2. Eksploitasi Bug, Glitch & Duplikasi Sumber Daya',
        rules_r2_2_desc: 'Dilarang memanfaatkan kelemahan kode Minecraft, celah game, atau bug plugin untuk menggandakan item, memperbanyak saldo Rupiah, atau menerobos batasan sistem. Segala temuan bug wajib dilaporkan kepada pengelola server demi integritas bersama.',
        rules_r2_2_prev: 'Modul ekonomi ApexsionsEconomy menerapkan arsitektur transaksi atomik ACID dengan penguncian state (lock-boundary). Item dan saldo tidak pernah dapat dipindahkan secara ganda dalam satuan tick yang sama.',
        rules_r2_3_title: '3. Griefing & Pencurian di Luar Mekanisme Perang Resmi',
        rules_r2_3_desc: 'Dilarang merusak, membongkar paksa, atau mencuri harta di dalam wilayah kerajaan berdaulat dan rumah warga lain tanpa izin pemilik, kecuali saat peristiwa resmi Kingdom War dideklarasikan sesuai protokol perang.',
        rules_r2_3_prev: 'Sistem proteksi TerritoryListener dan RegionManager secara otomatis membatalkan event pembongkaran blok, penempatan blok, dan pembukaan peti oleh pemain non-warga wilayah berdaulat.',
        rules_r2_4_title: '4. Eksploitasi Redstone & Mesin Perontok Server (Lag Machine)',
        rules_r2_4_desc: 'Dilarang membuat clock loop redstone tanpa tombol pemutus, mesin pelempar item berlebihan, instalasi piston masif tanpa peredam, atau mekanisme yang sengaja dirancang untuk menurunkan TPS (Ticks Per Second) server.',
        rules_r2_4_prev: 'Sistem watchdog otomatis membatasi frekuensi pergantian sinyal redstone. Sirkuit loop yang berdetik melebihi batas toleransi per tick server akan otomatis dibekukan dan diputus demi kestabilan 20 TPS server.',
        rules_r2_5_title: '5. Combat Logging (Kabur Saat Bertarung)',
        rules_r2_5_desc: 'Dilarang keluar dari server secara sengaja (disconnect/alt+F4) saat sedang berada dalam pertempuran aktif melawan pemain lain guna menghindari kematian atau kehilangan perlengkapan.',
        rules_r2_5_prev: 'Modul CombatTag mengunci status pertarungan selama 15 detik. Jika pemain disconnect saat combat tag menyala, server otomatis mengeksekusi karakter, mendrop seluruh inventory di titik logout, dan mencatat status kekalahan.',
        rules_p3_tag: 'BAGIAN KETIGA',
        rules_p3_title: 'III. Etika, Integritas Akun & Staf',
        rules_r3_1_title: '1. Berbohong kepada Staf & Penghindaran Sanksi (Ban Evasion)',
        rules_r3_1_desc: 'Dilarang memberikan keterangan palsu dalam proses investigasi staf, memalsukan bukti laporan, atau membuat akun baru/alternatif untuk bermain kembali saat akun utama sedang menjalani masa hukuman sanksi.',
        rules_r3_1_prev: 'Pemeriksaan authoritative UUID dan jejak koneksi IP mendeteksi hubungan akun alternatif secara otomatis. Login baru yang terasosiasi dengan akun terkena sanksi aktif akan otomatis ditolak oleh gerbang inisiasi.',
        rules_r3_2_title: '2. Batasan Multi-Akun (Alt Accounts)',
        rules_r3_2_desc: 'Setiap pemain hanya diperkenankan memiliki maksimal 2 (dua) akun Minecraft pribadi. Dilarang menggunakan akun alternatif untuk menimbun starter kit, menduduki kasta ganda di kerajaan berlawanan, atau memanipulasi voting dan pasar lelang.',
        rules_r3_2_prev: 'Protokol penautan /link portal web menerapkan pembatasan satu akun berdaulat terverifikasi per kartu warga guna menjaga pemerataan perolehan reward harian dan integritas ekonomi.',
        rules_r3_3_title: '3. Penyamaran Identitas & Penghormatan Staf',
        rules_r3_3_desc: 'Dilarang meniru nama, prefix, gelar, atau berpura-pura menjadi anggota staf Apexsions (Warden, Herald, Overseer, Architect, Ancestor) untuk memeras atau memperdaya warga. Kritik terhadap keputusan staf disampaikan secara beradab melalui tiket resmi.',
        rules_r3_3_prev: 'Sistem hirarki kasta ranks.yml memblokir perubahan display name atau prefix nickname (/nick) yang mengandung kata kunci staf yang telah diproteksi oleh izin otoritas.',
        rules_p4_tag: 'BAGIAN KEEMPAT',
        rules_p4_title: 'IV. Keamanan, Iklan & Kepatuhan Hukum Negara',
        rules_r4_1_title: '1. Promosi & Iklan Server Luar Tanpa Izin',
        rules_r4_1_desc: 'Dilarang menyebarkan alamat IP server Minecraft lain, link komunitas Discord luar, atau tautan promosi komersial tanpa persetujuan resmi dari pihak pengelola Apexsions.',
        rules_r4_1_prev: 'Modul AdvertisementChecker memindai pesan secara real-time dengan regex IPv4, nama domain global dan nasional (termasuk .my.id, .id, .com, dll.), serta link undangan Discord luar, dan langsung membatalkan pengiriman pesan ke publik.',
        rules_r4_2_title: '2. Doxxing, Pembocoran Data Pribadi & Ancaman Serangan Cyber',
        rules_r4_2_desc: 'Dilarang keras mempublikasikan informasi data pribadi orang lain (nama asli, alamat rumah, nomor kontak, media sosial privat) tanpa izin, serta dilarang melontarkan ancaman serangan DDoS atau eksploitasi jaringan.',
        rules_r4_2_prev: 'Infrastruktur Apexsions dilindungi mitigasi DDoS tingkat enterprise, enkripsi SSL/TLS berlapis, dan kebijakan isolasi data sensitif di mana credential pemain tidak pernah terekspos di log obrolan server.',
        rules_r4_3_title: '3. Penipuan Transaksi Riil & Pelanggaran UU ITE',
        rules_r4_3_desc: 'Segala bentuk penipuan finansial uang riil (Rupiah), jual beli akun curian, transaksi pasar gelap di luar Webstore resmi, atau aktivitas yang melanggar Undang-Undang Informasi dan Transaksi Elektronik (UU ITE) Republik Indonesia dilarang mutlak.',
        rules_r4_3_prev: 'Semua transaksi webstore diproses melalui gateway pembayaran resmi bersertifikat dengan pencatatan audit log permanen dan sistem pengiriman item terverifikasi langsung ke inventori in-game.',
        rules_sanction_tag: 'STANDAR YURISDIKSI',
        rules_sanction_title: 'Matriks Tingkatan Sanksi & Eskalasi',
        rules_th_level: 'Tingkat Sanksi',
        rules_th_type: 'Jenis Pelanggaran Terkait',
        rules_th_action: 'Tindakan Penegakan Hukum',
        rules_t1_badge: 'Tingkat I',
        rules_t1_type: 'Spam ringan, Caps-Lock berlebihan, pelanggaran format chat.',
        rules_t1_action: 'Peringatan tertulis (Warning) • Mute otomatis 15–60 detik oleh sistem.',
        rules_t2_badge: 'Tingkat II',
        rules_t2_type: 'Toksisitas berulang, provokasi vulgar, AFK farming tanpa batas, redstone lag ringan.',
        rules_t2_action: 'Mute obrolan 1 hingga 24 jam • Kick dari server • Pembersihan mesin lag.',
        rules_t3_badge: 'Tingkat III',
        rules_t3_type: 'Griefing wilayah, pelecehan personal berat, combat logging berulang, promosi server lain.',
        rules_t3_action: 'Temporary Banned 3 hingga 14 hari • Rollback data kerusakan wilayah.',
        rules_t4_badge: 'Tingkat IV',
        rules_t4_type: 'Cheat/Hack client, duplikasi item, doxxing, SARA ekstrem, penipuan finansial, ban evasion.',
        rules_t4_action: 'Permanent Banned • IP/UUID Blacklist • Penyitaan total aset in-game.',
        rules_appeal_tag: 'MEKANISME BANDING',
        rules_appeal_title: 'Merasa Mendapat Sanksi yang Kurang Tepat?',
        rules_appeal_desc: 'Setiap warga berhak memperoleh keadilan. Ajukan banding resmi dengan menyertakan bukti kronologis melalui meja tiket staf di peladen Discord resmi Apexsions.',
        rules_appeal_btn: 'Buka Tiket Banding',

        // Vote Page
        vote_breadcrumb: 'Bilik Suara (Vote)',
        vote_kicker: 'DUKUNGAN REALM & BILIK SUARA',
        vote_title: 'Suarakan Kedaulatan Apexsions',
        vote_desc: 'Setiap suara yang Anda berikan memperluas jangkauan peradaban Apexsions ke seluruh penjuru dunia. Sebagai wujud terima kasih, para tetua kerajaan menganugerahi hadiah pusaka, kunci peti langka, dan akselerasi progres untuk setiap suara sah.',
        vote_cd_24h: 'Cooldown 24 Jam',
        vote_cd_12h: 'Cooldown 12 Jam',
        vote_top_servers: 'Daftar Server Teratas',
        vote_global_ranks: 'Peringkat Server Dunia',
        vote_creative_hub: 'Komunitas Kreatif Global',
        vote_p1_desc: 'Dukung peradaban di daftar server Minecraft paling bergengsi. Suara Anda menaikkan peringkat realm di mata komunitas global.',
        vote_p2_desc: 'Pilihan voting dengan jeda reset lebih cepat (12 jam). Berikan suara dua kali sehari untuk memaksimalkan perolehan kunci peti.',
        vote_p3_desc: 'Sentra kreasi arsitektur dan skin Minecraft terbesar. Perkuat reputasi peradaban Apexsions di kancah internasional.',
        vote_instant_rewards: 'Imbalan Langsung:',
        vote_btn_now: 'Beri Suara Sekarang',
        vote_guide_kicker: 'TATA CARA KLAIM HADIAH',
        vote_guide_title: 'Tiga Langkah Mengklaim Hadiah di Dalam Game',
        vote_guide_desc: 'Sistem WebBridge Apexsions menyinkronkan data pemilih secara otomatis setiap detik.',
        vote_s1_badge: 'Nama Akun Akurat',
        vote_s1_title: 'Masukkan Username Minecraft',
        vote_s1_desc: 'Saat membuka halaman voting salah satu platform di atas, masukkan username Minecraft Anda secara persis (case-sensitive) tanpa spasi tambahan.',
        vote_s2_badge: 'Kirimkan Suara',
        vote_s2_title: 'Selesaikan Verifikasi Suara',
        vote_s2_desc: 'Selesaikan captcha pada situs voting dan tekan tombol kirim (Vote). Situs akan mengonfirmasi bahwa suara Anda telah tercatat sah.',
        vote_s3_badge: 'Klaim Otomatis',
        vote_s3_title: 'Pemberian Hadiah Instan',
        vote_s3_desc: 'Jika sedang online di server, hadiah langsung masuk ke tas Anda. Jika sedang offline, ketik perintah /claim atau /vote saat login.',
        vote_honor_badge: 'PAPAN KEHORMATAN BULANAN',
        vote_honor_reset: 'RESET SETIAP TGL 1',
        vote_honor_title: 'Penghargaan Voter Terbanyak',
        vote_honor_desc: 'Warga yang paling setia mengumandangkan nama peradaban Apexsions setiap bulan akan dianugerahi gelar kehormatan dan paket pusaka eksklusif oleh dewan kerajaan:',
        vote_top1_title: 'JUARA I • MAHKOTA KEDAULATAN',
        vote_top1_desc: 'Gelar [✦ TOP VOTER ✦] (30 Hari) + 3x Golden Keys + Rp 100.000',
        vote_top2_title: 'JUARA II • KESATRIA UTUSAN',
        vote_top2_desc: '2x Golden Keys + Rp 50.000 Uang Realm + 500 XP Pass',
        vote_top3_title: 'JUARA III • PEJUANG SUARA',
        vote_top3_desc: '1x Golden Key + Rp 25.000 Uang Realm + 250 XP Pass',
        vote_cta_kicker: 'GERBANG REALM SIAP MENYAMBUT',
        vote_cta_title: 'Masuk & Rasakan Kemakmurannya',
        vote_cta_desc: 'Setelah memberikan suara, sambungkan klien Anda ke dunia Apexsions untuk segera membuka peti keberuntungan di pelataran spawn utama.',
        vote_back_home: 'Kembali ke Beranda',
        vote_join_discord: 'Gabung Discord Komunitas',

        // Terms Page
        terms_breadcrumb: 'Syarat & Ketentuan',
        terms_kicker: 'KETENTUAN HUKUM & TRANSAKSI',
        terms_title: 'Syarat & Ketentuan Layanan Webstore',
        terms_desc: 'Dokumen ini mengatur hak, kewajiban, tata cara transaksi donasi, dan perolehan barang virtual di dalam ekosistem server Minecraft Apexsions. Dengan melakukan transaksi pada webstore, Anda menyetujui seluruh klausul di bawah ini.',
        terms_doc_version: 'VERSI DOKUMEN: 2026.2',
        terms_doc_updated: 'DIPERBARUI: 6 SEPTEMBER 2026',
        terms_art1_title: 'Sifat Produk Virtual & Pengiriman Instan',
        terms_art1_sub: 'Ketentuan Pengiriman Barang Digital Non-Fisik',
        terms_art1_desc: 'Seluruh item, kasta donatur (Sions, Emperor, Sovereign, Archon, Ascendant), Battlepass, kunci peti pusaka, kosmetik, serta saldo mata uang yang tertera di webstore Apexsions adalah barang/jasa virtual digital. Tidak ada barang berwujud fisik yang dikirimkan ke alamat Anda.',
        terms_art1_li1: 'Pesanan diproses dan dikirimkan secara instan melalui sistem sinkronisasi otomatis WebBridgeService segera setelah status pembayaran dikonfirmasi.',
        terms_art1_li2: 'Pastikan username Minecraft yang Anda masukkan saat checkout persis sama dengan akun di dalam game. Kesalahan pengetikan nama akun di luar tanggung jawab sistem otomatis.',
        terms_art2_title: 'Kanal Pembayaran Resmi & Verifikasi',
        terms_art2_sub: 'Saluran Transaksi Terverifikasi Manajemen',
        terms_art2_desc: 'Transaksi resmi hanya dilayani melalui webstore portal web.apexsions.my.id dan konfirmasi langsung ke WhatsApp Founder/Admin resmi Apexsions:',
        terms_art2_warn: 'Manajemen tidak pernah meminta password akun Minecraft Anda dan tidak bertanggung jawab atas transaksi yang dilakukan di luar nomor resmi di atas.',
        terms_art3_title: 'Kebijakan Pengembalian Dana & Anti-Chargeback',
        terms_art3_sub: 'Klausul Final Transaksi & Penegakan Integritas',
        terms_art3_desc: 'Karena manfaat produk virtual langsung diterapkan secara permanen atau berkala ke akun in-game, seluruh transaksi bersifat final dan tidak dapat dibatalkan atau dikembalikan (No Refund).',
        terms_art3_li1: 'Tindakan penarikan dana sepihak (chargeback, dispute, atau klaim fraud bank palsu) dianggap sebagai pelanggaran integritas berat dan akan memicu pemblokiran permanen (Blacklist IP & Akun) dari seluruh layanan server.',
        terms_art3_li2: 'Apabila terjadi kendala sistem di mana paket belum terkirim setelah 15 menit pasca-pembayaran berhasil, warga wajib membuka tiket investigasi di Discord resmi dengan melampirkan invoice transfer.',
        terms_art4_title: 'Kepatuhan Hukum & Batasan Hak Donatur',
        terms_art4_sub: 'Donasi Bukan Merupakan Kekebalan Hukum (Immunity)',
        terms_art4_desc: 'Kepemilikan kasta donatur adalah bentuk apresiasi dan kontribusi sukarela warga terhadap pembiayaan infrastruktur peladen. Kasta donatur tidak memberikan kekebalan hukum dari Peraturan Resmi Apexsions.',
        terms_art4_desc2: 'Donatur yang terbukti melanggar aturan berat (penggunaan cheat/klien ilegal, duplikasi ekonomi, exploit sistem, perundungan siber) tetap dikenai sanksi banned/mute sesuai matriks sanksi peradaban tanpa adanya kompensasi pengembalian dana.',
        terms_art5_title: 'Kesesuaian Minecraft Commercial Usage Guidelines (EULA)',
        terms_art5_sub: 'Kepatuhan Standar Mojang Studios & Microsoft',
        terms_art5_desc: 'Apexsions adalah peladen independen yang beroperasi selaras dengan Minecraft Commercial Usage Guidelines. Apexsions tidak berafiliasi dengan Mojang Studios atau Microsoft Corporation. Seluruh nama brand dan hak cipta terdaftar Minecraft adalah milik masing-masing pemiliknya.',
        terms_link_privacy: 'Baca Kebijakan Privasi & Perlindungan Data →',
        terms_link_rules: 'Baca Peraturan Lengkap Server',

        // Privacy Page
        privacy_breadcrumb: 'Kebijakan Privasi',
        privacy_kicker: 'KEBIJAKAN PRIVASI & PERLINDUNGAN DATA',
        privacy_title: 'Privasi & Keamanan Data Warga',
        privacy_desc: 'Apexsions memegang teguh integritas kedaulatan data warganya. Kami hanya menghimpun data yang esensial untuk sinkronisasi permainan, pengiriman paket webstore, dan perlindungan keamanan server dari ancaman siber.',
        privacy_doc_version: 'VERSI DOKUMEN: 2026.2',
        privacy_doc_updated: 'DIPERBARUI: 6 SEPTEMBER 2026',
        privacy_art1_title: 'Informasi yang Kami Himpun',
        privacy_art1_sub: 'Data Esensial untuk Menjalankan Ekosistem Peradaban',
        privacy_art1_desc: 'Saat Anda mendaftarkan diri, menautkan akun dengan perintah /link, atau bermain di dalam realm, sistem kami mencatat informasi berikut:',
        privacy_art1_li1: 'Identitas Karakter Minecraft: Nama pengguna (Username) dan nomor pengidentifikasi unik (UUID) resmi dari Mojang atau Floodgate (Bedrock).',
        privacy_art1_li2: 'Kredensial Akun Web: Alamat surel (email) aktif dan kata sandi yang telah dienkripsi secara satu arah dengan algoritma bcrypt berstandar industri.',
        privacy_art1_li3: 'Data Telemetri Jaringan: Alamat Protokol Internet (IP) dan catatan waktu login untuk mendeteksi upaya peretasan, serangan DDoS, serta pencegahan multi-akun ilegal (ban evasion).',
        privacy_art2_title: 'Tujuan & Pemanfaatan Data',
        privacy_art2_sub: 'Optimalisasi Layanan & Integritas Permainan',
        privacy_art2_desc: 'Data warga diolah secara ketat untuk keperluan operasional teknis berikut:',
        privacy_art2_li1: 'Mengonfirmasi dan mendistribusikan kasta donatur, kunci peti pusaka, serta koin yang dipesan melalui webstore secara otomatis via WebBridgeService.',
        privacy_art2_li2: 'Menjaga tatanan sosial server dari pemain toksik, cheater, dan pelaku duplikasi melalui sistem audit log staf terpusat.',
        privacy_art2_li3: 'Mengirimkan notifikasi pemulihan kata sandi atau pengumuman penting seputar pemeliharaan peradaban ke email terdaftar.',
        privacy_art3_title: 'Keamanan Finansial & Transaksi',
        privacy_art3_sub: 'Nol Penyimpanan Informasi Finansial Sensitif',
        privacy_art3_desc: 'Apexsions tidak pernah menyimpan nomor kartu kredit, debit, kode CVV, atau sandi perbankan Anda di server kami. Seluruh proses checkout dialihkan ke saluran pembayaran resmi terlisensi (Midtrans Gateway) atau konfirmasi manual WhatsApp founder berizin resmi dengan enkripsi TLS end-to-end.',
        privacy_art4_title: 'Kerahasiaan Mutlak Tanpa Pihak Ketiga',
        privacy_art4_sub: 'Komitmen Perlindungan Privasi Komunitas',
        privacy_art4_desc: 'Kami tidak pernah dan tidak akan pernah menjual, menyewakan, atau membagikan data identitas pribadi warga kepada pihak ketiga manapun untuk tujuan iklan atau pemasaran. Informasi hanya dapat dibuka apabila terdapat instruksi berkekuatan hukum resmi dari otoritas penegak hukum yang berwenang.',
        privacy_art5_title: 'Hak Warga atas Pengendalian Akun',
        privacy_art5_sub: 'Otentikasi Dua Faktor (2FA) & Penghapusan Data',
        privacy_art5_desc: 'Setiap warga memiliki hak penuh untuk mengakses riwayat akun, memperbarui email, mengaktifkan fitur perlindungan otentikasi dua faktor (2FA), atau meminta penghapusan akun secara permanen melalui Pengaturan Profil.',
        privacy_link_terms: 'Baca Syarat & Ketentuan Layanan Webstore →',
        privacy_link_rules: 'Baca Peraturan Lengkap Server',

        // Shop & Cart
        shop_kicker: 'TRANSAKSI RESMI PERADABAN',
        shop_hero_sub: 'Tingkatkan kasta, kedaulatan, dan supremasi peradabanmu di realm Apexsions dengan mandat resmi, kit berkala, serta perolehan sumber daya terpercaya.',
        shop_wa_title: 'Pemesanan Langsung & Terverifikasi via WhatsApp Founder',
        shop_wa_badge: 'AKTIVASI CEPAT',
        shop_wa_desc: 'Pilih paket yang diinginkan lalu hubungi salah satu dari 2 Founder resmi kami: Rifqi atau Friell. Pesanan diproses aman, transparan, dan langsung aktif ke in-game.',
        shop_btn_wa: 'Pesan Cepat via WhatsApp',
        shop_btn_details: 'Rincian & Benefit Lengkap',
        shop_choose_founder: 'Pilih Founder:',
        shop_direct_order: 'Pesan Langsung',
        shop_sidebar_account: 'Akun Pemain',
        shop_sidebar_account_desc: 'Masuk untuk melihat keranjang dan klaim perk otomatis ke dalam in-game.',
        shop_sidebar_help: 'BANTUAN & FOUNDER',
        shop_sidebar_help_desc: 'Butuh panduan donasi atau konfirmasi manual? Hubungi salah satu Founder resmi kami:',
        cart_kicker: 'KERANJANG BELANJA PERADABAN',
        cart_hero_sub: 'Periksa rincian pesanan paket kedaulatan Anda sebelum melanjutkan ke proses pembayaran atau aktivasi via WhatsApp.',
        cart_wa_title: 'Mau Aktivasi Instan Tanpa Antre?',
        cart_wa_desc: 'Anda dapat langsung konfirmasi dan pesan melalui WhatsApp ke 2 Founder resmi: Rifqi atau Friell.',

        // Wiki
        wiki_badge: 'ENSIKLOPEDIA & PANDUAN',
        wiki_portal_badge: 'ARSIP RESMI REALM',
        wiki_runtime_ver: '• Versi Runtime Minecraft 26.2',
        wiki_portal_title: 'Pusat Pengetahuan & Sejarah Peradaban Puncak',
        wiki_portal_desc: 'Selamat datang di ensiklopedia resmi Apexsions. Temukan dokumentasi lengkap tentang 3 Kerajaan berdaulat, 11 hierarki kasta resmi, 28 Custom Enchants beserta efek set bonus armor, panduan pasar dinamis, serta peta jalan dari pengelana menjadi penguasa wilayah.',
        wiki_popular_label: 'Topik Populer:',
        wiki_stat_categories: 'Kategori Utama',
        wiki_stat_articles: 'Artikel Terinci',
        wiki_stat_castes: 'Kasta Resmi',
        wiki_stat_enchants: 'Custom Enchants',
        wiki_search_placeholder: 'Cari topik atau perintah...',
        wiki_back_index: 'Kembali ke Indeks Wiki',
        wiki_open_guide: 'Buka Panduan Lengkap',
        wiki_no_results: 'Tidak Ada Hasil Ditemukan',
        wiki_all_categories: 'Jelajahi Seluruh Kategori'
    },
    en: {
        // Navigation
        nav_home: 'Home',
        nav_shop: 'Webstore',
        nav_features: 'Features',
        nav_wiki: 'Wiki',
        nav_leaderboard: 'Leaderboard',
        nav_rules: 'Rules',
        nav_vote: 'Vote',
        nav_discord: 'Discord',
        nav_login: 'Login',
        nav_register: 'Register',
        nav_acc_registered: 'REGISTERED ACCOUNT',
        nav_profile: 'Player Profile',
        nav_link_mc: 'Link Minecraft',
        nav_order_history: 'Purchase History',
        nav_admin_panel: 'Admin Panel',
        nav_logout: 'Logout',

        // Hero
        hero_brand_mark: 'APEXSIONS • THE PEAK CIVILIZATIONS',
        hero_axis_foundation: 'FOUNDATION',
        hero_axis_nobles: 'NOBILITY',
        hero_axis_admin: 'ADMINISTRATION',
        hero_axis_authority: 'AUTHORITY',
        hero_axis_apex: 'THE APEX',
        hero_headline: 'A Sovereign Civilization Built Upon Hierarchical Order.',
        hero_subtext: 'Eleven social castes, three sovereign kingdoms, and a world shaped entirely by its citizens\' history.',
        hero_btn_explore: 'EXPLORE CIVILIZATION',
        hero_ip_label: 'SERVER IP',
        hero_status_online: 'SERVER ONLINE',
        hero_status_offline: 'SERVER OFFLINE',
        hero_status_ready: 'Gates Open • Ready to Explore',
        hero_status_resting: 'Realm is Resting',
        hero_status_maintenance: 'Under Maintenance',
        hero_status_crossplay_ready: 'Java & Bedrock Ready',
        hero_citizens: 'Citizens',
        hero_copied: 'Copied!',

        // World & Lore
        world_kicker: 'LORE & TERRITORIAL ORDER',
        world_title: 'Fall of the Sions Empire & Exodus of the Three Kingdoms',
        world_lead: 'Long ago, a single vast civilization—the <strong>Sions Empire</strong>—ruled over the entire realm in prosperity without ever touching dark arts. Yet inevitable doom struck when its leader\'s ambition forced the harnessing of forbidden energy from the <strong>Dark Dimension</strong> to multiply troop power. Uncontrolled darkness shattered the empire\'s grand capital and triggered the <em>Great Exodus</em> in three directions: the royal dynasty fled eastward (<strong>Zenithar</strong>), the laborers and survivalist rangers took refuge in the western wildwoods (<strong>Sylvamoor</strong>), while the supreme battle mages and veteran warriors migrated south into the volcanic crags (<strong>Solterra</strong>).',

        // Kingdoms
        label_characteristics: 'Territory Characteristics',
        label_buffs: 'Historical Lore Buffs',
        label_debuffs: 'Physical Debuffs',
        zenithar_type: 'EASTERN TERRITORY • ROYAL DYNASTY',
        zenithar_desc: 'Led by the royal dynasty and elite imperial guards of the Sions Empire who survived the catastrophe, fleeing eastward toward the celestial zenith. Upholding palace etiquette, sacred cavalry, and skyward spire citadels.',
        zenithar_spec1: 'Celestial Horizon & Solarium Spire Citadel',
        zenithar_spec2: 'Pure Gold Extraction & Grand Architectural Marvels',
        zenithar_buffs: '<span class="text-success fw-semibold">+5% Speed</span>, <span class="text-success fw-semibold">+7% Luck</span>, <span class="text-success fw-semibold">+6% All Damage &amp; Defense</span>, <span class="text-success fw-semibold">-5% Enemy Crit Reduction</span>.',
        zenithar_debuffs: '<span class="text-danger fw-semibold">+7% Poison Vulnerability</span>, food replenishes hunger slower (-1 point) due to aristocratic dining habits.',
        zenithar_link: 'Zenithar Archives',

        solterra_type: 'SOUTHERN TERRITORY • MAGICIANS & VETERANS',
        solterra_desc: 'Founded by supreme battle arcanists and the mightiest front-line veterans of the former Sions legions who migrated south. Blending explosive fire-element sorcery with brutal physical prowess to master a deadly volcanic frontier.',
        solterra_spec1: 'Volcanic Crags, Magma Calderas & Ignis Bastion',
        solterra_spec2: 'Heavy Weapon Forging & High Ore Sell Ratios',
        solterra_buffs: '<span class="text-success fw-semibold">+15% Total Damage</span>, <span class="text-success fw-semibold">+10% Critical Damage</span>, <span class="text-success fw-semibold">+10% Mining Speed</span>, <span class="text-success fw-semibold">High Ore Sell Ratio (65%)</span>.',
        solterra_debuffs: '<span class="text-danger fw-semibold">-2 Max HP (9 Hearts)</span>, <span class="text-danger fw-semibold">+8% Incoming Damage</span>, <span class="text-danger fw-semibold">+7% Faster Hunger</span>, farmland dries rapidly on arid stone.',
        solterra_link: 'Solterra Archives',

        sylvamoor_type: 'WESTERN TERRITORY • WORKERS & WILDWOOD RANGERS',
        sylvamoor_desc: 'Constructed by laborers, master builders, agrarian farmers, and populist rangers who retreated into the deep western canopy. Isolating themselves from imperial power struggles to live in balance with the Great World Tree.',
        sylvamoor_spec1: 'Ancient Canopy Wildwoods, Crystal Seas & Eldergrove',
        sylvamoor_spec2: 'Agrarian Granaries, Herbal Alchemy & Sustainable Food',
        sylvamoor_buffs: '<span class="text-success fw-semibold">+2 Max HP (11 Hearts)</span>, <span class="text-success fw-semibold">+12% Luck</span>, <span class="text-success fw-semibold">+7% Extra Mob Drops</span>, <span class="text-success fw-semibold">High Natural Defense (~12.6%)</span>, <span class="text-success fw-semibold">Eternal Soil Hydration</span>.',
        sylvamoor_debuffs: '<span class="text-danger fw-semibold">Altitude Sickness at Y > 110</span> (Hunger/Weakness), <span class="text-danger fw-semibold">+15% Burn Damage</span>, <span class="text-danger fw-semibold">-10% PvP Damage & Mining Speed</span>.',
        sylvamoor_link: 'Sylvamoor Archives',

        // Ledger
        ledger_title_war: 'Sovereign Treaties & Kingdom Wars',
        ledger_badge_war: 'REALM DECREE',
        ledger_desc_war: 'Every citizen pledges allegiance to one kingdom. Defend borderlands, fortify bastions, and seize supremacy in weekly Kingdom Wars.',
        ledger_link_war: 'Kingdom Sovereignty Decree',
        ledger_title_econ: 'Open Market Economy & Escrow',
        ledger_badge_econ: 'DUAL-CURRENCY',
        ledger_desc_econ: 'Dual currency economy featuring Rupiah (Rp) and Diamonds 💎. Master dynamic supply-demand markets (/shop), secure Escrow auctions, and direct cross-kingdom barter trades.',
        ledger_link_econ: 'Market & Escrow Mechanics',
        ledger_link_econ_shop: 'Visit Civilization Store',
        ledger_title_ench: 'Forging Mastery & 182 Custom Enchants',
        ledger_badge_ench: '7 POWER TIERS',
        ledger_desc_ench: 'Seven tiers of custom enchantments via Alchemists and Tinkerers, combined with balanced legendary armor set bonuses tuned for sovereign combat.',
        ledger_link_ench: 'Forging Encyclopedia',
        realm_tag: 'REALM TERRITORY ARCHIVE',
        realm_title: 'CIVILIZATION LANDSCAPES',
        realm_sub: 'From stalwart capital fortresses to untamed wilderness waiting to be conquered.',

        // Caste Hierarchy
        caste_kicker: 'SOCIAL STRATA',
        caste_title: 'The Eleven Social Castes',
        caste_lead: 'From the pioneer\'s first steps to the supreme throne of the ancestral founders, each caste carries sovereign weight, territorial privileges, and measured honor.',
        caste_filter_all: 'ALL CASTES (11)',
        caste_filter_apex: 'THE APEX (1)',
        caste_filter_authority: 'COUNCIL OF AUTHORITY (2)',
        caste_filter_staff: 'ADMINISTRATION & STAFF (2)',
        caste_filter_nobles: 'ORDER OF NOBLES (5)',
        caste_filter_pioneers: 'PIONEERS (1)',

        // Onboarding
        step_kicker: 'RITE OF PASSAGE',
        step_title: 'How to Join the Civilization',
        step_lead: 'Three simple steps to connect your Minecraft client and carve your name into sovereign history.',
        step1_phase: 'STEP ONE',
        step1_title: 'Minecraft Client 26.2',
        step1_desc: 'Launch official Minecraft or your client of choice on version <strong>26.2</strong>. Full cross-play support for both <strong>Java Edition &amp; Bedrock Edition</strong>.',
        step1_meta: 'Java & Bedrock Crossplay',
        step2_phase: 'STEP TWO',
        step2_title: 'Server Address & Port',
        step2_desc: 'Navigate to Multiplayer and connect to <code>apexsions.my.id:32348</code>. For Bedrock players, use IP <code>apexsions.my.id</code> with Port <code>32348</code>.',
        step2_copy_java: 'Copy Java • apexsions.my.id:32348',
        step2_copy_bedrock: 'Copy Bedrock • IP: apexsions.my.id | Port: 32348',
        step3_phase: 'STEP THREE',
        step3_title: 'Account Authentication (/link)',
        step3_desc: 'Once inside the server lobby, type <code>/link</code> to receive your secret authentication code to link your account to the web portal.',
        step3_btn_link: 'Open Linking Portal',
        step3_registered: 'Your Account is Registered',
        step3_btn_register: 'Register Civilization Account',
        guide_callout_badge: 'QUICK PIONEER GUIDE',
        guide_callout_title: 'Unsure What to Do After Landing at Spawn?',
        guide_callout_desc: 'Discover the 15-minute roadmap from claiming starter kits, pledging to a kingdom, claiming lands, to earning your first coins.',
        guide_callout_btn: 'Open 15-Minute Guide',

        // Footer
        footer_desc: 'Apexsions is a sovereign civilization forged by its community. Autonomous kingdom orders, atomic market systems, and competitive social caste progression on Minecraft 26.2.',
        footer_subline: 'MINECRAFT 26.2 • SOVEREIGN REALM',
        footer_col_civ: 'CIVILIZATION',
        footer_col_guides: 'GUIDES & RULES',
        footer_col_community: 'COMMUNITY DIPLOMACY',
        footer_three_kingdoms: 'Three Kingdoms',
        footer_caste_hierarchy: 'Caste Hierarchy',
        footer_wiki_archive: 'Wiki Archives',
        footer_vote_support: 'Vote Support',
        footer_how_to_join: 'How to Join',
        footer_encyclopedia: 'Player Encyclopedia',
        footer_official_rules: 'Official Rules',
        footer_terms_trans: 'Transaction Terms',
        footer_help_center: 'Help Center',
        footer_copyright: 'Apexsions. All rights reserved.',
        footer_sub_copyright: 'Built with the community, for a sovereign civilization.',
        footer_terms: 'Terms of Service',
        footer_privacy: 'Privacy Policy',
        footer_contact: 'Contact',

        // Rules Page
        rules_breadcrumb: 'Server Rules',
        rules_header_kicker: 'LEGAL ORDER & SOVEREIGNTY',
        rules_header_title: 'Official Regulations of Apexsions',
        rules_header_desc: 'To preserve fair competition, social cohesion, and the safety of all citizens, every individual entering Apexsions is bound by the sovereign laws below. All rules are actively backed by <strong>automated server-side prevention</strong> and staff audits.',
        rules_nav_chat: 'I. Chat & Social',
        rules_nav_gameplay: 'II. Gameplay & Fair Play',
        rules_nav_ethics: 'III. Ethics & Integrity',
        rules_nav_security: 'IV. Security & Law',
        rules_nav_sanctions: 'Sanction Matrix',
        rules_p1_tag: 'PILLAR ONE',
        rules_p1_title: 'I. Communication & Chat Ethics',
        rules_r1_1_title: '1. Spam, Flooding & Excessive Caps-Lock',
        rules_r1_1_desc: 'Rapidly repeating messages, flooding the chatbox with symbols or whitespace, and excessive capitalization (Caps-Lock) disrupting chat readability are strictly prohibited.',
        rules_r1_1_prev: 'The server runs an automated SpamChecker with a sliding-window limiter (max 3 messages per 4s, min 1.2s delay), 80% Levenshtein text similarity algorithm, and an automatic Caps dampener that converts shouting into normal lowercase text.',
        rules_r1_2_title: '2. Vulgar Language, Toxic Provocation & Inappropriate Content',
        rules_r1_2_desc: 'Vulgar cursing, explicit sexual discourse, and toxic provocation intended to instigate player conflict across global, kingdom, or private channels (/tell) are prohibited.',
        rules_r1_2_prev: 'The ProfanityChecker engine with leetspeak normalization automatically blocks and filters taboo keywords before reaching the public chat, flagging offenders for staff review.',
        rules_r1_3_title: '3. Bullying, Toxicity & Personal Harassment',
        rules_r1_3_desc: 'Demeaning, humiliating, intimidating, or cyber-harassing any citizen is forbidden. Caste rivalries and kingdom wars must remain within the bounds of roleplay sportsmanship, not real-life malice.',
        rules_r1_3_prev: 'The /report <player> <reason> command freezes the last 50 lines of chat logs directly into the StaffReportsDesk investigation queue, eliminating screenshot forgery.',
        rules_r1_4_title: '4. Hate Speech & Bigotry (SARA, Nation, Family)',
        rules_r1_4_desc: 'Hate speech targeting race, religion, ethnicity, nationality, national symbols, or personal family members is strictly forbidden without exception.',
        rules_r1_4_prev: 'The HateSpeechChecker module enforces zero-tolerance blocking. Violations trigger high-priority alerts to overseer consoles and immediately mute the offender.',
        rules_p2_tag: 'PILLAR TWO',
        rules_p2_title: 'II. Gameplay, Territorial Integrity & Fair Play',
        rules_r2_1_title: '1. Cheating, Hacked Clients & Illegal Modifications',
        rules_r2_1_desc: 'Using third-party client mods that offer unfair advantages—including X-Ray, Baritone, auto-printers, auto-clickers, killaura, fly, speed, Jesus, and unapproved freecam—is strictly forbidden.',
        rules_r2_1_prev: 'Server-side defenses limit raytrace block distances, obfuscate underground ore packets (native anti-xray engine), cap click CPS, and flag movement packet anomalies.',
        rules_r2_2_title: '2. Bug Exploitation, Glitching & Resource Duplication',
        rules_r2_2_desc: 'Exploiting Minecraft glitches or plugin bugs to duplicate items, artificially generate currency, or bypass restrictions is forbidden. Bugs must be reported immediately to staff.',
        rules_r2_2_prev: 'The ApexsionsEconomy module enforces atomic ACID transactions with strict lock boundaries. Items and currency balances can never be duplicated in the same tick.',
        rules_r2_3_title: '3. Griefing & Theft Outside Declared Kingdom Wars',
        rules_r2_3_desc: 'Destroying, breaking into, or stealing from sovereign kingdom territories or claimed homes without permission is prohibited, except during officially scheduled Kingdom Wars.',
        rules_r2_3_prev: 'TerritoryListener and RegionManager protection systems automatically deny block-break, block-place, and container-open events for non-citizens.',
        rules_r2_4_title: '4. Redstone Abuse & Lag Machines',
        rules_r2_4_desc: 'Creating infinite redstone clock loops without shutoff toggles, excessive item droppers, or contraptions intended to degrade server TPS is prohibited.',
        rules_r2_4_prev: 'An automated watchdog monitors redstone signal frequency. Loops exceeding tolerance thresholds are frozen instantly to preserve a rock-solid 20 TPS performance.',
        rules_r2_5_title: '5. Combat Logging (Disconnecting Mid-Combat)',
        rules_r2_5_desc: 'Deliberately disconnecting or closing the game during active PvP combat to evade death or inventory loss is strictly forbidden.',
        rules_r2_5_prev: 'The CombatTag module locks players into combat for 15 seconds. Disconnecting during this window triggers instant character execution, dropping all items on the spot.',
        rules_p3_tag: 'PILLAR THREE',
        rules_p3_title: 'III. Ethics, Account Integrity & Staff Relations',
        rules_r3_1_title: '1. Dishonesty to Staff & Ban Evasion',
        rules_r3_1_desc: 'Falsifying reports, lying during staff investigations, or creating alternate accounts to circumvent active sanctions will result in immediate permanent blacklisting.',
        rules_r3_1_prev: 'Authoritative UUID verification and IP connection mapping detect alternate accounts automatically, rejecting login attempts linked to actively sanctioned users.',
        rules_r3_2_title: '2. Multi-Account Policy (Alt Limits)',
        rules_r3_2_desc: 'Each citizen is allowed at most 2 personal Minecraft accounts. Using alts to hoard starter kits, spy across rival kingdoms, or manipulate markets/votes is prohibited.',
        rules_r3_2_prev: 'Web portal /link protocol links one verified citizen profile per player, preserving reward fairness and economic stability.',
        rules_r3_3_title: '3. Staff Impersonation & Respect',
        rules_r3_3_desc: 'Impersonating staff members (Warden, Herald, Overseer, Architect, Ancestor) with fake names or titles to deceive citizens is strictly forbidden. Constructive feedback belongs in official tickets.',
        rules_r3_3_prev: 'The ranks.yml hierarchy blocks display name and /nick adjustments containing staff keywords protected by authority permissions.',
        rules_p4_tag: 'PILLAR FOUR',
        rules_p4_title: 'IV. Security, Advertising & Legal Compliance',
        rules_r4_1_title: '1. Unauthorized External Server Advertising',
        rules_r4_1_desc: 'Broadcasting foreign Minecraft server IPs, unauthorized Discord invite links, or commercial promotions without prior management approval is prohibited.',
        rules_r4_1_prev: 'The AdvertisementChecker module scans chat in real-time with IPv4, global domain regex, and Discord invite filters, instantly dropping matching messages.',
        rules_r4_2_title: '2. Doxxing, Privacy Violations & Cyber Threats',
        rules_r4_2_desc: 'Doxxing private personal data (real names, physical addresses, phone numbers, private social media) or issuing DDoS and network attack threats is an extreme violation.',
        rules_r4_2_prev: 'Apexsions infrastructure is guarded by enterprise DDoS mitigation, layered TLS encryption, and strict data isolation where credentials are never leaked.',
        rules_r4_3_title: '3. Real Money Fraud & Cyberlaw Compliance',
        rules_r4_3_desc: 'Real-world monetary fraud, trading stolen accounts, unapproved black-market trades outside the official Webstore, or illegal cyber activities are universally prohibited.',
        rules_r4_3_prev: 'All webstore transactions are processed through verified payment channels with immutable audit logging and instant verified in-game fulfillment.',
        rules_sanction_tag: 'JURISDICTION STANDARDS',
        rules_sanction_title: 'Sanction Matrix & Penalty Escalation',
        rules_th_level: 'Sanction Tier',
        rules_th_type: 'Applicable Infractions',
        rules_th_action: 'Enforcement Action',
        rules_t1_badge: 'Tier I',
        rules_t1_type: 'Minor spam, excessive Caps-Lock, chat format non-compliance.',
        rules_t1_action: 'Formal warning • Automated mute for 15–60 seconds.',
        rules_t2_badge: 'Tier II',
        rules_t2_type: 'Repeated toxicity, vulgar provocation, unbounded AFK farming, minor redstone lag.',
        rules_t2_action: 'Chat mute for 1–24 hours • Server kick • Removal of lag contraption.',
        rules_t3_badge: 'Tier III',
        rules_t3_type: 'Territory griefing, severe harassment, repeated combat-logging, server advertising.',
        rules_t3_action: 'Temporary ban for 3–14 days • Full regional rollback of damaged claims.',
        rules_t4_badge: 'Tier IV',
        rules_t4_type: 'Hacked clients, duping, doxxing, hate speech, real-money fraud, ban evasion.',
        rules_t4_action: 'Permanent ban • IP/UUID Blacklist • Complete forfeiture of all in-game assets.',
        rules_appeal_tag: 'APPEAL PROCESS',
        rules_appeal_title: 'Believe a Penalty Was Issued in Error?',
        rules_appeal_desc: 'Every citizen is entitled to fair justice. Submit an official appeal with chronological evidence via our Discord ticket desk.',
        rules_appeal_btn: 'Open Appeal Ticket',

        // Vote Page
        vote_breadcrumb: 'Ballot Box (Vote)',
        vote_kicker: 'REALM SUPPORT & BALLOT BOX',
        vote_title: 'Cast Your Vote for Apexsions',
        vote_desc: 'Every vote you cast expands the reach of Apexsions across the Minecraft universe. In gratitude, royal elders bestow relic rewards, crate keys, and progression boosts for each verified ballot.',
        vote_cd_24h: '24h Cooldown',
        vote_cd_12h: '12h Cooldown',
        vote_top_servers: 'Top Server Directory',
        vote_global_ranks: 'Global Server Rankings',
        vote_creative_hub: 'Global Creative Community',
        vote_p1_desc: 'Support the realm on Minecraft\'s premier server list. Elevate the civilization\'s standing in the international community.',
        vote_p2_desc: 'Faster 12-hour voting cycle. Vote twice a day to double your key rewards and progression boosts.',
        vote_p3_desc: 'The world\'s largest Minecraft creative hub. Bolster Apexsions\' presence among builders and architects.',
        vote_instant_rewards: 'Instant Perks:',
        vote_btn_now: 'Vote Now',
        vote_guide_kicker: 'HOW TO CLAIM REWARDS',
        vote_guide_title: 'Three Steps to Claim In-Game Rewards',
        vote_guide_desc: 'The Apexsions WebBridge synchronizes voter payloads automatically in real-time.',
        vote_s1_badge: 'Accurate Username',
        vote_s1_title: 'Enter Minecraft Username',
        vote_s1_desc: 'When voting on any platform above, enter your exact case-sensitive Minecraft in-game username without extra spaces.',
        vote_s2_badge: 'Cast Ballot',
        vote_s2_title: 'Complete Vote Verification',
        vote_s2_desc: 'Solve the security captcha and click Vote. The platform will record your ballot as valid.',
        vote_s3_badge: 'Instant Delivery',
        vote_s3_title: 'Receive In-Game Perks',
        vote_s3_desc: 'If online, rewards are delivered to your inventory immediately. If offline, type /claim or /vote upon logging in.',
        vote_honor_badge: 'MONTHLY HONOR ROLL',
        vote_honor_reset: 'RESETS ON 1ST OF MONTH',
        vote_honor_title: 'Top Monthly Voter Honors',
        vote_honor_desc: 'Citizens who consistently proclaim the Apexsions name each month receive exclusive prestige titles and relic bundles from the sovereign council:',
        vote_top1_title: '1ST PLACE • SOVEREIGN CROWN',
        vote_top1_desc: '[✦ TOP VOTER ✦] Title (30 Days) + 3x Golden Keys + Rp 100,000',
        vote_top2_title: '2ND PLACE • REALM KNIGHT',
        vote_top2_desc: '2x Golden Keys + Rp 50,000 Realm Coins + 500 Pass XP',
        vote_top3_title: '3RD PLACE • BALLOT WARRIOR',
        vote_top3_desc: '1x Golden Key + Rp 25,000 Realm Coins + 250 Pass XP',
        vote_cta_kicker: 'THE REALM GATES AWAIT',
        vote_cta_title: 'Connect & Claim Your Prosperity',
        vote_cta_desc: 'After casting your ballot, join the realm of Apexsions to open your reward crates at the central spawn plaza.',
        vote_back_home: 'Return Home',
        vote_join_discord: 'Join Community Discord',

        // Terms Page
        terms_breadcrumb: 'Terms of Service',
        terms_kicker: 'LEGAL & TRANSACTION TERMS',
        terms_title: 'Webstore Terms of Service',
        terms_desc: 'This agreement governs donor rights, purchase workflows, and virtual asset acquisition within the Apexsions server ecosystem. By purchasing on this webstore, you accept all clauses below.',
        terms_doc_version: 'DOCUMENT VERSION: 2026.2',
        terms_doc_updated: 'LAST UPDATED: SEPTEMBER 6, 2026',
        terms_art1_title: 'Virtual Products & Instant Delivery',
        terms_art1_sub: 'Digital Non-Physical Fulfillment Policy',
        terms_art1_desc: 'All ranks (Sions, Emperor, Sovereign, Archon, Ascendant), Battlepasses, crate keys, cosmetics, and currency balances sold on Apexsions are intangible digital goods. No physical items will be shipped to your address.',
        terms_art1_li1: 'Orders are fulfilled instantly via our automated WebBridgeService immediately after payment verification.',
        terms_art1_li2: 'Ensure your entered Minecraft username exactly matches your in-game identity. Typos are not the responsibility of the automated fulfillment pipeline.',
        terms_art2_title: 'Official Payment Channels & Verification',
        terms_art2_sub: 'Management-Verified Payment Outlets',
        terms_art2_desc: 'Official transactions are exclusively processed via web.apexsions.my.id and direct verification with official Apexsions Founders on WhatsApp:',
        terms_art2_warn: 'Staff will never ask for your Minecraft password and cannot guarantee transactions conducted outside our official numbers.',
        terms_art3_title: 'No-Refund Policy & Anti-Chargeback',
        terms_art3_sub: 'Final Transaction Clause & Integrity Enforcement',
        terms_art3_desc: 'Because virtual benefits are immediately applied to player accounts, all purchases are strictly final, non-refundable, and non-reversible.',
        terms_art3_li1: 'Unilateral chargebacks, disputes, or fraudulent bank claims result in an immediate and irreversible permanent blacklist of IP and associated accounts.',
        terms_art3_li2: 'If perks are not delivered within 15 minutes of confirmed payment, please open an investigation ticket on Discord with your payment receipt.',
        terms_art4_title: 'Legal Compliance & Donor Rights Limits',
        terms_art4_sub: 'Donations Do Not Grant Legal Immunity',
        terms_art4_desc: 'Possession of a donor rank is an expression of voluntary appreciation and contribution towards server infrastructure costs. Donor ranks do not grant immunity from Official Apexsions Regulations.',
        terms_art4_desc2: 'Donors found guilty of severe rule violations (cheating/hacked clients, economy duplication, system exploits, cyberbullying) remain subject to ban/mute penalties according to the sanction matrix without any refund compensation.',
        terms_art5_title: 'Minecraft Commercial Usage Guidelines Compliance (EULA)',
        terms_art5_sub: 'Mojang Studios & Microsoft Compliance Standards',
        terms_art5_desc: 'Apexsions is an independent server operating in compliance with the Minecraft Commercial Usage Guidelines. Apexsions is not affiliated with Mojang Studios or Microsoft Corporation. All Minecraft trademarks and registered copyrights belong to their respective owners.',
        terms_link_privacy: 'Read Privacy Policy & Data Protection →',
        terms_link_rules: 'Read Complete Server Rules',

        // Privacy Page
        privacy_breadcrumb: 'Privacy Policy',
        privacy_kicker: 'PRIVACY POLICY & DATA PROTECTION',
        privacy_title: 'Citizen Privacy & Data Protection',
        privacy_desc: 'Apexsions upholds data sovereignty. We collect only data strictly necessary for gameplay synchronization, webstore fulfillment, and cybersecurity defense.',
        privacy_doc_version: 'DOCUMENT VERSION: 2026.2',
        privacy_doc_updated: 'LAST UPDATED: SEPTEMBER 6, 2026',
        privacy_art1_title: 'Information We Collect',
        privacy_art1_sub: 'Essential Data for Realm Operation',
        privacy_art1_desc: 'When you register, link your account with /link, or connect to the realm, our systems process:',
        privacy_art1_li1: 'Minecraft Identity: Player username and authoritative UUID issued by Mojang or Floodgate (Bedrock).',
        privacy_art1_li2: 'Web Credentials: Email address and one-way hashed passwords using industry-standard bcrypt.',
        privacy_art1_li3: 'Network Telemetry: IP addresses and session timestamps to mitigate DDoS attacks and enforce ban policies.',
        privacy_art2_title: 'Purpose & Usage of Data',
        privacy_art2_sub: 'Service Optimization & Fair Play',
        privacy_art2_desc: 'Citizen data is processed strictly for operational workflows:',
        privacy_art2_li1: 'Confirming and distributing purchased donor ranks, keys, and coins automatically via WebBridgeService.',
        privacy_art2_li2: 'Safeguarding server integrity against toxic behavior, cheaters, and item duplication via centralized staff audit logs.',
        privacy_art2_li3: 'Transmitting password recovery notifications or critical maintenance announcements to verified email addresses.',
        privacy_art3_title: 'Financial & Transaction Security',
        privacy_art3_sub: 'Zero Storage of Sensitive Financial Data',
        privacy_art3_desc: 'Apexsions never stores credit/debit card numbers, CVVs, or banking PINs. All checkouts are processed via licensed gateways or encrypted WhatsApp founder channels.',
        privacy_art4_title: 'Absolute Confidentiality & No Third Parties',
        privacy_art4_sub: 'Commitment to Community Privacy Protection',
        privacy_art4_desc: 'We never and will never sell, rent, or share citizens\' personal data with any third parties for advertising or marketing purposes. Information may only be disclosed upon a lawful subpoena from authorized law enforcement authorities.',
        privacy_art5_title: 'Citizen Rights to Account Control',
        privacy_art5_sub: 'Two-Factor Authentication (2FA) & Data Deletion',
        privacy_art5_desc: 'Every citizen has the full right to view account history, update email, enable Two-Factor Authentication (2FA), or request permanent data deletion via Profile Settings.',
        privacy_link_terms: 'Read Webstore Terms of Service →',
        privacy_link_rules: 'Read Complete Server Rules',

        // Shop & Cart
        shop_kicker: 'OFFICIAL REALM TRANSACTION',
        shop_hero_sub: 'Elevate your caste, sovereign influence, and authority across Apexsions with official donor mandates, periodic kits, and trusted resources.',
        shop_wa_title: 'Direct & Verified Orders via Founder WhatsApp',
        shop_wa_badge: 'FAST ACTIVATION',
        shop_wa_desc: 'Select your desired package and contact either of our 2 official Founders: Rifqi or Friell. Orders are processed securely, transparently, and fulfilled instantly in-game.',
        shop_btn_wa: 'Quick Order via WhatsApp',
        shop_btn_details: 'Details & Full Perks',
        shop_choose_founder: 'Choose Founder:',
        shop_direct_order: 'Direct Order',
        shop_sidebar_account: 'Player Account',
        shop_sidebar_account_desc: 'Sign in to access your shopping cart and automatically claim perks in-game.',
        shop_sidebar_help: 'SUPPORT & FOUNDERS',
        shop_sidebar_help_desc: 'Need purchase guidance or manual confirmation? Contact our official Founders:',
        cart_kicker: 'REALM SHOPPING CART',
        cart_hero_sub: 'Review your order summary before proceeding to payment or direct WhatsApp activation.',
        cart_wa_title: 'Want Instant Activation Without Queues?',
        cart_wa_desc: 'You can immediately verify and order via WhatsApp directly with Founders: Rifqi or Friell.',

        // Wiki
        wiki_badge: 'ENCYCLOPEDIA & GUIDES',
        wiki_portal_badge: 'OFFICIAL REALM ARCHIVES',
        wiki_runtime_ver: '• Minecraft 26.2 Runtime Version',
        wiki_portal_title: 'Center of Knowledge & Peak Civilizations Lore',
        wiki_portal_desc: 'Welcome to the official Apexsions encyclopedia. Explore comprehensive archives on 3 sovereign Kingdoms, 11 official castes, 28 Custom Enchants with armor set bonus effects, dynamic market guides, and the roadmap from wanderer to territorial lord.',
        wiki_popular_label: 'Popular Topics:',
        wiki_stat_categories: 'Primary Categories',
        wiki_stat_articles: 'Detailed Articles',
        wiki_stat_castes: 'Official Castes',
        wiki_stat_enchants: 'Custom Enchants',
        wiki_search_placeholder: 'Search topics, commands, or guides...',
        wiki_back_index: 'Return to Wiki Index',
        wiki_open_guide: 'Read Full Guide',
        wiki_no_results: 'No Results Found',
        wiki_all_categories: 'Browse All Categories'
    }
};

document.addEventListener('DOMContentLoaded', () => {
    // 0. Cinematic Inter-Page Transition Dismissal (Highest Priority)
    const initPageTransitions = () => {
        const overlay = document.getElementById('apxPageTransition');
        if (!overlay) return;

        const dismissTransition = () => {
            overlay.classList.remove('is-navigating', 'is-entering');
            overlay.classList.add('is-loaded');
        };

        // Immediate first-tick dismissal
        dismissTransition();
        requestAnimationFrame(dismissTransition);
        setTimeout(dismissTransition, 40);

        window.addEventListener('pageshow', dismissTransition);
        window.addEventListener('load', dismissTransition);

        document.addEventListener('mouseover', (e) => {
            const link = e.target.closest('a');
            if (!link || !link.href) return;
            if (link.origin !== window.location.origin) return;
            if (link.hasAttribute('data-prefetched')) return;

            link.setAttribute('data-prefetched', 'true');
            const prefetchLink = document.createElement('link');
            prefetchLink.rel = 'prefetch';
            prefetchLink.href = link.href;
            document.head.appendChild(prefetchLink);
        }, { passive: true });

        document.addEventListener('click', (e) => {
            const link = e.target.closest('a');
            if (!link) return;

            const href = link.getAttribute('href');
            if (!href) return;

            if (href.startsWith('#') || href.startsWith('javascript:') || href.startsWith('mailto:') || href.startsWith('tel:')) {
                return;
            }

            if (link.target === '_blank' || e.ctrlKey || e.metaKey || e.shiftKey || e.altKey) {
                return;
            }
            if (link.hasAttribute('data-bs-toggle') || link.hasAttribute('data-apx-copy') || link.hasAttribute('download')) {
                return;
            }

            try {
                const targetUrl = new URL(link.href, window.location.origin);
                if (targetUrl.origin !== window.location.origin) {
                    return;
                }

                if (targetUrl.pathname === window.location.pathname && targetUrl.search === window.location.search && targetUrl.hash) {
                    return;
                }

                if (targetUrl.href === window.location.href) {
                    return;
                }

                e.preventDefault();
                overlay.classList.remove('is-loaded');
                overlay.classList.add('is-navigating');

                setTimeout(() => {
                    window.location.href = targetUrl.href;
                }, 200);

                setTimeout(() => {
                    dismissTransition();
                }, 2000);
            } catch (err) {}
        });
    };

    initPageTransitions();

    // 1. Bilingual Internationalization Engine
    const initLanguageSwitcher = () => {
        const langChoices = document.querySelectorAll('.apx-lang-choice');
        const langLabelEl = document.querySelector('.apx-lang-current-label');
        const langChecks = document.querySelectorAll('.apx-lang-active-check');

        const applyLocale = (lang) => {
            const targetLang = (lang === 'en' ? 'en' : 'id');
            const dict = APX_I18N[targetLang];
            if (!dict) return;

            document.documentElement.setAttribute('lang', targetLang);

            if (langLabelEl) {
                langLabelEl.textContent = targetLang.toUpperCase();
            }

            langChoices.forEach(choice => {
                const isTarget = choice.getAttribute('data-apx-lang') === targetLang;
                choice.classList.toggle('active', isTarget);
            });

            langChecks.forEach(check => {
                const isTarget = check.getAttribute('data-lang-check') === targetLang;
                check.classList.toggle('d-none', !isTarget);
            });

            document.querySelectorAll('[data-i18n]').forEach(el => {
                const key = el.getAttribute('data-i18n');
                if (dict[key]) {
                    el.textContent = dict[key];
                }
            });

            document.querySelectorAll('[data-i18n-html]').forEach(el => {
                const key = el.getAttribute('data-i18n-html');
                if (dict[key]) {
                    el.innerHTML = dict[key];
                }
            });

            document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
                const key = el.getAttribute('data-i18n-placeholder');
                if (dict[key]) {
                    el.setAttribute('placeholder', dict[key]);
                }
            });

            try {
                localStorage.setItem('apx_locale', targetLang);
                document.cookie = `apx_locale=${targetLang};path=/;max-age=31536000;SameSite=Lax`;
            } catch (e) {}

            // Re-evaluate dynamic telemetry text in new language
            try {
                if (typeof fetchServerStatus === 'function') {
                    fetchServerStatus();
                }
            } catch (err) {}
        };

        langChoices.forEach(choice => {
            choice.addEventListener('click', (e) => {
                e.preventDefault();
                const lang = choice.getAttribute('data-apx-lang');
                applyLocale(lang);
            });
        });

        let initialLang = 'id';
        try {
            const saved = localStorage.getItem('apx_locale');
            if (saved && (saved === 'en' || saved === 'id')) {
                initialLang = saved;
            }
        } catch (e) {}

        applyLocale(initialLang);
    };

    initLanguageSwitcher();

    // 1. One-Click Copy for Server Address & Port with Visual Feedback
    const copyElements = document.querySelectorAll('[data-apx-copy]');
    copyElements.forEach(el => {
        const handleCopy = (e) => {
            if (e) {
                e.preventDefault();
                e.stopPropagation();
            }
            const textToCopy = el.getAttribute('data-apx-copy') || 'apexsions.my.id';
            navigator.clipboard.writeText(textToCopy).then(() => {
                const isEn = document.documentElement.lang === 'en';
                const copiedText = isEn ? 'Copied!' : 'Disalin!';
                const badgeEl = el.querySelector('.badge-copy');
                const originalHtml = badgeEl ? badgeEl.innerHTML : el.innerHTML;

                if (badgeEl) {
                    badgeEl.innerHTML = '<i class="bi bi-check-lg text-success"></i>';
                    setTimeout(() => {
                        badgeEl.innerHTML = originalHtml;
                    }, 2000);
                } else {
                    const originalBtnContent = el.innerHTML;
                    el.innerHTML = `<i class="bi bi-check-lg me-1 text-success"></i> ${copiedText}`;
                    el.classList.add('border-success');

                    setTimeout(() => {
                        el.innerHTML = originalBtnContent;
                        el.classList.remove('border-success');
                    }, 2000);
                }
            }).catch(err => {
                console.warn('Clipboard write failed:', err);
            });
        };

        el.addEventListener('click', handleCopy);
        el.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                handleCopy(e);
            }
        });
    });

    // 3. Live Minecraft Server Bridge Integration (Hoisted Function)
    function fetchServerStatus() {
        const playersEl = document.getElementById('apxOnlinePlayers');
        const playerStatusTextEl = document.getElementById('apxPlayerStatusText');
        const playerNumbersEl = document.getElementById('apxPlayerNumbers');
        const footerPlayersEl = document.getElementById('apxFooterPlayers');
        const footerMaxPlayersEl = document.getElementById('apxFooterMaxPlayers');
        const footerVersionEl = document.getElementById('apxFooterVersion');
        const footerBadgeEl = document.getElementById('apxFooterStatusBadge');
        const footerHeadingDotEl = document.getElementById('apxFooterHeadingDot');
        const maxPlayersEl = document.getElementById('apxMaxPlayers');
        const versionEl = document.getElementById('apxVersion');
        const liveBadgeEl = document.getElementById('apxLiveBadge');
        const liveSubEl = document.getElementById('apxLiveSub');
        const liveDotEl = document.getElementById('apxLiveDot');

        const applyStatus = (online, players, maxPlayers, version) => {
            const count = parseInt(players, 10) || 0;
            const isEn = document.documentElement.lang === 'en';

            if (playersEl) playersEl.textContent = count;
            if (footerPlayersEl) footerPlayersEl.textContent = count;
            if (maxPlayersEl) maxPlayersEl.textContent = maxPlayers ?? 200;
            if (footerMaxPlayersEl) footerMaxPlayersEl.textContent = maxPlayers ?? 200;
            if (versionEl && version) versionEl.textContent = version;
            if (footerVersionEl && version) footerVersionEl.textContent = version;

            // Dynamic 0-player friendly fallback in hero
            if (playerStatusTextEl && playerNumbersEl) {
                if (online && count > 0) {
                    playerStatusTextEl.classList.add('d-none');
                    playerNumbersEl.classList.remove('d-none');
                } else {
                    playerStatusTextEl.classList.remove('d-none');
                    playerNumbersEl.classList.add('d-none');
                    playerStatusTextEl.innerHTML = online
                        ? (isEn ? 'Gates Open &bull; Ready to Explore' : 'Gerbang Terbuka &bull; Siap Menjelajah')
                        : (isEn ? 'Realm is Resting' : 'Dunia Sedang Beristirahat');
                }
            }

            if (online) {
                if (liveBadgeEl) liveBadgeEl.textContent = 'SERVER ONLINE';
                if (liveSubEl) liveSubEl.textContent = isEn ? 'Java & Bedrock Ready' : 'Java & Bedrock Siap';
                if (liveDotEl) {
                    liveDotEl.style.background = '#10b981';
                    liveDotEl.style.boxShadow = '0 0 10px #10b981';
                }
                if (footerBadgeEl) {
                    footerBadgeEl.className = 'apx-status-pill apx-pill-online flex-shrink-0 ms-2';
                    footerBadgeEl.innerHTML = '<span class="apx-pulse-dot-sm"></span> ONLINE';
                }
                if (footerHeadingDotEl) {
                    footerHeadingDotEl.style.background = '#10b981';
                    footerHeadingDotEl.style.boxShadow = '0 0 8px #10b981';
                }
            } else {
                if (liveBadgeEl) liveBadgeEl.textContent = 'SERVER OFFLINE';
                if (liveSubEl) liveSubEl.textContent = isEn ? 'Under Maintenance' : 'Sedang Pemeliharaan';
                if (liveDotEl) {
                    liveDotEl.style.background = '#ef4444';
                    liveDotEl.style.boxShadow = '0 0 10px #ef4444';
                }
                if (footerBadgeEl) {
                    footerBadgeEl.className = 'apx-status-pill apx-pill-offline flex-shrink-0 ms-2';
                    footerBadgeEl.innerHTML = 'OFFLINE';
                }
                if (footerHeadingDotEl) {
                    footerHeadingDotEl.style.background = '#ef4444';
                    footerHeadingDotEl.style.boxShadow = 'none';
                }
            }
        };

        fetch('/api/apexsions-bridge/status')
            .then(res => {
                if (!res.ok) throw new Error('Bridge status HTTP ' + res.status);
                return res.json();
            })
            .then(data => {
                if (data && typeof data.online !== 'undefined') {
                    applyStatus(Boolean(data.online), data.players ?? 0, data.max_players ?? 200, data.version || '26.2');
                } else {
                    throw new Error('Invalid bridge payload');
                }
            })
            .catch(() => {
                fetch('https://api.mcstatus.io/v2/status/java/apexsions.my.id:32348')
                    .then(res => res.json())
                    .then(mcData => {
                        if (mcData && mcData.online) {
                            applyStatus(true, mcData.players?.online ?? 0, mcData.players?.max ?? 200, mcData.version?.name_clean || '26.2');
                        } else {
                            applyStatus(false, 0, 200, '26.2');
                        }
                    })
                    .catch(() => {
                        applyStatus(false, 0, 200, '26.2');
                    });
            });
    }

    fetchServerStatus();
    setInterval(fetchServerStatus, 30000);

    // 3. Interactive Rank Hierarchy Filter
    const rankPills = document.querySelectorAll('[data-rank-filter]');
    const rankCards = document.querySelectorAll('[data-rank-category]');
    rankPills.forEach(pill => {
        pill.addEventListener('click', () => {
            rankPills.forEach(p => {
                p.classList.remove('active');
                p.setAttribute('aria-selected', 'false');
            });
            pill.classList.add('active');
            pill.setAttribute('aria-selected', 'true');

            const filter = pill.getAttribute('data-rank-filter');
            rankCards.forEach(card => {
                if (filter === 'all' || card.getAttribute('data-rank-category') === filter) {
                    card.style.display = '';
                } else {
                    card.style.display = 'none';
                }
            });
        });
    });

    // 5. Cinematic Scroll Reveal Animations
    const initScrollAnimations = () => {
        if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
            return;
        }

        const autoTargets = [
            '.apx-section-header',
            '.apx-pillar-monolith',
            '.apx-caste-card',
            '.apx-caste-banner',
            '.apx-step-monolith',
            '.apx-showcase-card',
            '.apx-rule-card',
            '.apx-rule-item',
            '.apx-wiki-category-card',
            '.apx-wiki-article-item',
            '.card',
            '.apx-scroll-reveal'
        ];

        const elements = document.querySelectorAll(autoTargets.join(', '));
        if (!elements.length) return;

        if (!('IntersectionObserver' in window)) {
            elements.forEach(el => el.classList.add('is-revealed'));
            return;
        }

        const observer = new IntersectionObserver((entries, obs) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('is-revealed');
                    obs.unobserve(entry.target);
                }
            });
        }, {
            threshold: 0.05,
            rootMargin: '0px 0px -20px 0px'
        });

        const winHeight = window.innerHeight;
        elements.forEach(el => {
            const rect = el.getBoundingClientRect();
            if (rect.top < winHeight * 0.92) {
                el.classList.add('is-revealed');
            } else {
                el.classList.add('apx-scroll-reveal');

                const parent = el.parentElement;
                if (parent && (parent.classList.contains('row') || parent.classList.contains('apx-stepper-grid') || parent.classList.contains('apx-pillar-grid'))) {
                    const childIndex = Array.from(parent.children).indexOf(el);
                    if (childIndex >= 0 && childIndex < 4) {
                        el.classList.add(`apx-reveal-stagger-${childIndex + 1}`);
                    }
                }

                observer.observe(el);
            }
        });
    };

    initScrollAnimations();
});
