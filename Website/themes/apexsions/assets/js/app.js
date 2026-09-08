// ==========================================================================
// Apexsions Bilingual Internationalization Engine (ID / EN)
// ==========================================================================
const APX_I18N = {
    id: {
        // Navigation
        nav_home: 'Beranda',
        nav_civilizations: 'Peradaban',
        nav_gameplay: 'Gameplay',
        nav_leaderboard: 'Leaderboard',
        nav_shop: 'Webstore',
        nav_more: 'Lainnya',
        nav_wiki: 'Wiki',
        nav_wiki_sub: 'Mekanisme sistem, kasta & penempaan',
        nav_rules: 'Peraturan',
        nav_rules_sub: 'Tata tertib dan etika kedaulatan realm',
        nav_vote: 'Vote',
        nav_vote_sub: 'Vote harian & raih hadiah in-game',
        nav_discord: 'Discord',
        nav_btn_discord: 'Discord',
        nav_features: 'Fitur',
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
        wiki_all_categories: 'Jelajahi Seluruh Kategori',
        // Legal and Rules Additions
        legal_article_word: 'PASAL',
        rules_prevention_title: 'BENTUK PENCEGAHAN SISTEM AKTIF:',
        rules_s_mute: 'Sanksi: Mute',
        rules_s_warn_mute: 'Sanksi: Peringatan / Mute',
        rules_s_mute_tempban: 'Sanksi: Mute &bull; Temp Ban',
        rules_s_perm_ban: 'Sanksi: Permanent Ban',
        rules_s_rollback_permban: 'Sanksi: Rollback Data &bull; Permanent Ban',
        rules_s_rollback_tempban: 'Sanksi: Rollback Wilayah &bull; Temp Ban',
        rules_s_confiscate_tempban: 'Sanksi: Sita Mesin &bull; Temp Ban',
        rules_s_death_drop: 'Sanksi: Kematian Otomatis &bull; Drop Item',
        rules_s_blacklist: 'Sanksi: Blacklist Permanen',
        rules_s_freeze_alt: 'Sanksi: Pembekuan Akun Alt',
        rules_s_perm_mute_ban: 'Sanksi: Mute Permanen &bull; Banned',
        rules_s_perm_ban_legal: 'Sanksi: Permanent Ban &bull; Hukum Pidana',

        // Wiki Show & Navigation Additions
        wiki_back_category: 'Kembali ke Kategori',
        wiki_article_list: 'DAFTAR ARTIKEL',
        wiki_share: 'Bagikan',
        wiki_official_doc: 'Dokumen Resmi Apexsions',
        wiki_min_read: 'menit baca',
        wiki_copy_code: 'Salin',
        wiki_open_encyclopedia: 'Buka Ensiklopedia',

        // Posts / News Articles
        posts_breadcrumb: 'Warta & Artikel',
        posts_header_kicker: 'WARTA & DOKUMEN RESMI',
        posts_header_title: 'Warta & Artikel Peradaban',
        posts_header_desc: 'Ikuti seluruh rilis fitur terbaru, catatan pembaruan server, berita perang kerajaan, serta artikel komunitas resmi Apexsions.',
        posts_search_placeholder: 'Cari warta atau artikel...',
        posts_empty_title: 'Belum Ada Warta Diterbitkan',
        posts_empty_desc: 'Seluruh dokumentasi dan panduan mekanik dapat Anda pelajari secara mendalam melalui portal Ensiklopedia Wiki resmi.',
        posts_read_more: 'Baca Artikel',
        posts_back: 'Kembali ke Daftar Artikel',
        posts_comments_title: 'Komentar & Diskusi Warga',
        posts_leave_comment: 'Tulis Komentar',
        posts_comment_placeholder: 'Sampaikan pandangan Anda dengan sopan...',
        posts_send_comment: 'Kirim Komentar',
        posts_guest_comment: 'Silakan masuk ke akun untuk berpartisipasi dalam diskusi.',

        // Caste System (Home Page)
        caste_t5_roman: 'TINGKAT V',
        caste_t5_name: 'PUNCAK LELUHUR',
        caste_ancestor_badge: 'TAHTA TERTINGGI',
        caste_ancestor_desc: 'Pendiri mutlak dan penjaga kedaulatan utama realm Apexsions. Memegang mandat kedaulatan tertinggi atas tatanan dunia dan kelangsungan peradaban.',
        caste_ancestor_p1: 'Mandat tertinggi tatanan hukum server',
        caste_ancestor_p2: 'Kekuasaan penuh dewan kehormatan',
        caste_ancestor_p3: 'Akses tak terbatas seluruh teritori',
        caste_ancestor_p4: 'Imunitas mutlak kedaulatan realm',
        caste_btn_mandate: 'Mandat Leluhur',

        caste_t4_roman: 'TINGKAT IV',
        caste_t4_name: 'DEWAN OTORITAS',
        caste_architect_badge: 'OTORITAS REALM',
        caste_architect_desc: 'Perancang lanskap dunia dan pembangun megastruktur agung kerajaan. Mengatur tata ruang geografis dan keajaiban arsitektur antar-kerajaan.',
        caste_architect_p1: 'Pengawas konstruksi teritori kerajaan',
        caste_architect_p2: 'Izin intervensi tata kota peradaban',
        caste_architect_p3: 'Hak istimewa penjelajahan alam liar',
        caste_architect_p4: 'Dewan pertimbangan arsitektur realm',
        caste_btn_role: 'Peran Otoritas',

        caste_overseer_badge: 'INTEGRITAS REALM',
        caste_overseer_desc: 'Pengawas keadilan, keseimbangan ekonomi, dan penegak hukum peradaban. Menjaga integritas server dari kecurangan dan penyimpangan tatanan.',
        caste_overseer_p1: 'Audit transaksi ekonomi dan pasar',
        caste_overseer_p2: 'Penyelidikan sengketa wilayah kerajaan',
        caste_overseer_p3: 'Penegakan hukum dan sanksi pelanggaran',
        caste_overseer_p4: 'Otoritas investigasi staf dan warga',

        caste_t3_roman: 'TINGKAT III',
        caste_t3_name: 'ADMINISTRASI & STAF',
        caste_warden_badge: 'KEPALA STAF',
        caste_warden_desc: 'Panglima penjaga ketertiban sipil dan koordinator operasional staf. Menangani eskalasi laporan warga dan memimpin pengamanan wilayah netral.',
        caste_warden_p1: 'Koordinator operasional moderasi',
        caste_warden_p2: 'Penanganan banding sanksi warga',
        caste_warden_p3: 'Pengawasan perdamaian zona netral',
        caste_warden_p4: 'Manajemen laporan tiket Discord',

        caste_herald_badge: 'MODERATOR REALM',
        caste_herald_desc: 'Penyambung lidah peradaban, pemandu warga baru, dan penengah komunikasi chat. Memastikan interaksi antarwarga berlangsung damai dan beradab.',
        caste_herald_p1: 'Moderasi aktif saluran chat publik',
        caste_herald_p2: 'Bimbingan dan panduan warga baru',
        caste_herald_p3: 'Mediasi perselisihan ringan antarpemain',
        caste_herald_p4: 'Pemberian sanksi peringatan dan mute',

        caste_t2_roman: 'TINGKAT II',
        caste_t2_name: 'ORDO BANGSAWAN (DONATUR)',
        caste_sions_badge: 'KASTA TERTINGGI DONATUR',
        caste_sions_desc: 'Gelar kehormatan tertinggi bagi penyokong kedaulatan utama realm. Menikmati hak istimewa terlengkap, akses wilayah eksklusif, dan wibawa tanpa tanding.',
        caste_sions_p1: 'Akses 10x Set Home & Fly di klaim wilayah',
        caste_sions_p2: 'Kunci Peti Mythic & Golden mingguan',
        caste_sions_p3: 'Prioritas masuk saat server penuh',
        caste_sions_p4: 'Prefiks bergengsi & efek kosmetik spesial',
        caste_btn_webstore_small: 'Webstore',

        caste_emperor_badge: 'BANGSAWAN TIER 4',
        caste_emperor_desc: 'Penguasa tanah luas dengan pengaruh militer dan ekonomi yang disegani. Memiliki keleluasaan ekspansi wilayah dan fasilitas logistik superior.',
        caste_emperor_p1: 'Akses 7x Set Home & Virtual Chest',
        caste_emperor_p2: 'Kunci Peti Relic & Diamond mingguan',
        caste_emperor_p3: 'Kit perlengkapan tempur Emperor berkala',
        caste_emperor_p4: 'Akses perintah perbaikan peralatan (/repair)',

        caste_sovereign_badge: 'BANGSAWAN TIER 3',
        caste_sovereign_desc: 'Bangsawan berwibawa penyokong kemakmuran pasar dan benteng pertahanan. Menikmati kenyamanan utilitas portabel dan akselerasi progres.',
        caste_sovereign_p1: 'Akses 5x Set Home & Workbench portabel',
        caste_sovereign_p2: 'Kunci Peti Emas & bonus uang mingguan',
        caste_sovereign_p3: 'Kit armor Sovereign berkualitas tinggi',
        caste_sovereign_p4: 'Diskon biaya transaksi pasar lelang',

        caste_archon_badge: 'BANGSAWAN TIER 2',
        caste_archon_desc: 'Kesatria terpandang dan tuan tanah yang dihormati di seantero negeri. Didukung berbagai kemudahan hidup dan efisiensi penempaan.',
        caste_archon_p1: 'Akses 3x Set Home & Anvil portabel',
        caste_archon_p2: 'Kunci Peti Pusaka mingguan',
        caste_archon_p3: 'Kit senjata Archon tempaan khusus',
        caste_archon_p4: 'Batas penyimpanan item lebih lapang',

        caste_ascendant_badge: 'BANGSAWAN TIER 1',
        caste_ascendant_desc: 'Langkah pertama memasuki lingkaran bangsawan terhormat peradaban. Memberikan percepatan awal yang berharga bagi warga ambisius.',
        caste_ascendant_p1: 'Akses 2x Set Home & Furnace portabel',
        caste_ascendant_p2: 'Bonus saldo Rupiah dan Diamond awal',
        caste_ascendant_p3: 'Kit pemula Ascendant lengkap',
        caste_ascendant_p4: 'Prefiks hijau cerah pembeda identitas',

        caste_t1_roman: 'TINGKAT I',
        caste_t1_name: 'FONDASI PERADABAN',
        caste_wanderer_badge: 'DEFAULT / SEMUA WARGA',
        caste_wanderer_desc: 'Pondasi utama masyarakat Apexsions. Setiap warga memulai perjalanannya dari sini: bebas memilih kerajaan, mengolah tanah, berdagang di pasar bebas, dan meniti tangga kemuliaan.',
        caste_wanderer_p1: 'Hak memilih 1 dari 3 kerajaan berdaulat',
        caste_wanderer_p2: 'Akses 1x Set Home & perlindungan klaim tanah',
        caste_wanderer_p3: 'Partisipasi penuh dalam ekonomi & Kingdom War',
        caste_wanderer_p4: 'Akses starter kit inisiasi (/kit starter)',
        caste_btn_join: 'Cara Bergabung',
        caste_btn_full_guide: 'Buka Panduan Lengkap Kasta',
        caste_btn_webstore: 'Kunjungi Webstore Resmi',

        // Profile Page
        profile_title: 'Profil Pengguna',
        profile_sub: 'Kelola informasi akun website dan karakter in-game Apexsions Anda.',
        profile_btn_leaderboard: 'Papan Peringkat',
        profile_btn_public: 'Lihat Profil Publik',
        profile_kingdom_label: 'Kerajaan Faksi:',
        profile_kingdom_none: 'Belum Memilih',
        profile_balance_rp: 'Saldo Rupiah:',
        profile_balance_dia: 'Diamond:',
        profile_balance_coins: 'Apex Coins:',
        profile_bp_title: 'Musim Peradaban (BattlePass)',
        profile_bp_tier: 'Tier',
        profile_bp_premium: 'PREMIUM PASS',
        profile_bp_free: 'FREE PASS',
        profile_bp_exsio: 'EXSIO PASS',
        profile_bp_sio: 'SIO PASS',
        profile_bp_citizen: 'CITIZEN PASS',
        profile_bp_xp: 'Progress Musim:',
        profile_reward_daily: 'Hadiah Harian',
        profile_reward_ready: 'Siap Diklaim',
        profile_reward_ready_sub: 'Dapatkan <strong>+Rp 5.000</strong> & <strong>+25 EXP</strong> in-game setiap 24 jam!',
        profile_reward_btn_claim: 'Klaim Hadiah Harian',
        profile_reward_claimed: 'Sudah Diklaim',
        profile_reward_next_available: 'Klaim berikutnya tersedia dalam:',
        profile_reward_tomorrow_prefix: 'Tersedia besok pukul',
        profile_reward_btn_cooldown: 'Sedang Cooldown',
        profile_btn_reset_pass: 'Reset Password In-Game',
        profile_btn_unlink: 'Putuskan Tautan (Unlink)',
        profile_unlink_title: 'Hubungkan Karakter Minecraft Anda',
        profile_unlink_desc: 'Kaitkan akun website Anda dengan server Minecraft Apexsions untuk menampilkan skin 3D, sinkronisasi rank & level otomatis, mengklaim hadiah harian, dan mengelola profil in-game secara langsung.',
        profile_unlink_btn: 'Tautkan Akun Minecraft Sekarang',
        profile_lbl_registered: 'Terdaftar:',
        profile_lbl_points: 'Web Points:',
        profile_btn_manage_2fa: 'Kelola 2FA',
        profile_btn_enable_2fa: 'Aktifkan 2FA',
        profile_title_change_email: 'Ganti Alamat Email',
        profile_lbl_new_email: 'Email Baru',
        profile_lbl_curr_pass: 'Password Saat Ini',
        profile_btn_update_email: 'Perbarui Email',
        profile_title_change_pass: 'Ganti Password Web',
        profile_lbl_new_pass: 'Password Baru',
        profile_lbl_confirm_pass: 'Konfirmasi Password Baru',
        profile_btn_update_pass: 'Perbarui Password Web',
        profile_modal_reset_title: 'Reset Password In-Game (AuthMe)',
        profile_modal_reset_alert: 'Perintah reset akan dikirimkan ke server Minecraft untuk akun',
        profile_modal_lbl_new_pass: 'Password In-Game Baru (Minimal 6 karakter)',
        profile_modal_ph_new_pass: 'Ketik password baru...',
        profile_modal_lbl_confirm_pass: 'Konfirmasi Password Baru',
        profile_modal_ph_confirm_pass: 'Ulangi password baru...',
        profile_modal_btn_cancel: 'Batal',
        profile_modal_btn_submit: 'Konfirmasi & Ganti Password',

        // Account Link Page
        link_header_title: 'Tautkan Akun Minecraft',
        link_header_sub: 'Hubungkan akun Java Edition atau Bedrock Edition Anda ke portal web Apexsions',
        link_pin_success_title: 'Kode PIN Verifikasi Anda Telah Dibuat!',
        link_pin_success_sub: 'Masuk ke server Minecraft kami dan jalankan perintah berikut dalam waktu <strong>5 menit</strong>:',
        link_pin_copy: 'Salin',
        link_pin_target_user: 'Username target:',
        link_pin_valid_until: 'Berlaku hingga:',
        link_connected_title: 'Akun Terhubung Saat Ini',
        link_empty_text: 'Belum ada akun Minecraft yang ditautkan ke akun web ini.',
        link_badge_verified: 'Terverifikasi',
        link_badge_pending: 'Menunggu In-Game Link',
        link_btn_unlink: 'Lepas',
        link_form_title: 'Tautkan Akun Baru',
        link_lbl_username: 'Minecraft Username',
        link_ph_username: 'Contoh: Steve atau .Steve (Bedrock)',
        link_floodgate_hint: 'Pemain Bedrock dapat menggunakan prefix Floodgate (misalnya tanda titik atau bintang).',
        link_lbl_edition: 'Edisi Minecraft',
        link_opt_java: '(PC / Mac / Linux)',
        link_opt_bedrock: '(Geyser / Floodgate / Mobile / Console)',
        link_btn_get_pin: 'Dapatkan Kode PIN Verifikasi',

        // Webstore & Shop
        shop_pkg_subtitle: 'Webstore Resmi Apexsions • Transaksi Langsung & Aman',
        shop_pkg_wa_title: 'Pesan Langsung via WhatsApp Founder:',
        shop_pkg_wa_note: 'Pilih salah satu Founder untuk memulai chat WhatsApp dengan data pesanan Anda yang telah terisi secara otomatis:',
        shop_founder_role: 'Founder',
        shop_btn_close: 'Tutup',
        shop_btn_order_now: 'Pesan Cepat Sekarang',
        shop_badge_3days: '3 Hari',
        shop_badge_perm: 'Permanen',
        shop_sions_p1: 'Prefiks [✦ SIONS] & Chat Gradien',
        shop_sions_p2: 'Akses 10x Set Home & Fly di Klaim',
        shop_sions_p3: 'Kunci Peti Mythic & Golden Mingguan',
        shop_sions_p4: 'Prioritas Masuk Saat Penuh & Kit Apex',
        shop_emperor_p1: 'Prefiks [⚔ EMPEROR] & Chat Merah',
        shop_emperor_p2: 'Akses 7x Set Home & Virtual Chest',
        shop_emperor_p3: 'Kunci Peti Relic & Diamond Mingguan',
        shop_emperor_p4: 'Kit Perlengkapan Emperor & Emote',
        shop_sovereign_p1: 'Prefiks [⚜ SOVEREIGN] & Chat Emas',
        shop_sovereign_p2: 'Akses 5x Set Home & Workbench Portabel',
        shop_sovereign_p3: 'Kunci Peti Emas & Uang Tunai Mingguan',
        shop_sovereign_p4: 'Kit Armor Sovereign & Efek Partikel',
        shop_archon_p1: 'Prefiks [💎 ARCHON] & Chat Biru Laut',
        shop_archon_p2: 'Akses 3x Set Home & Anvil Portabel',
        shop_archon_p3: 'Kunci Peti Pusaka Mingguan',
        shop_archon_p4: 'Kit Senjata Archon & Diskon Toko',
        shop_ascendant_p1: 'Prefiks [☘ ASCENDANT] & Chat Hijau',
        shop_ascendant_p2: 'Akses 2x Set Home & Furnace Portabel',
        shop_ascendant_p3: 'Bonus Saldo Uang Awal',
        shop_ascendant_p4: 'Kit Pemula Ascendant',
        shop_vippass_p1: 'Buka Jalur Hadiah VIP Musiman',
        shop_vippass_p2: 'Kosmetik Jubah & Partikel Sayap Eksklusif',
        shop_vippass_p3: 'Akselerasi EXP Quest Battlepass +25%',
        shop_vippass_p4: 'Kunci Peti Legendaris Tiap 10 Level',
        shop_prempass_p1: 'Akses Penuh Jalur Hadiah Premium',
        shop_prempass_p2: 'Kosmetik Title & Banner Spesial',
        shop_prempass_p3: 'Akselerasi EXP Quest Battlepass +50%',
        shop_prempass_p4: 'Kunci Peti Mythic & Skin Eksklusif',
        shop_fallback_p1: 'Paket Resmi Webstore Apexsions',
        shop_fallback_p2: 'Aktivasi Otomatis & Terverifikasi',
        shop_fallback_p3: 'Dukungan Teknis Penuh dari Founder',
        shop_hist_kicker: '<i class="bi bi-receipt"></i> RIWAYAT TRANSAKSI RESMI',
        shop_hist_sub: 'Arsip catatan transaksi, perolehan kasta donatur, dan langganan resmi peradaban akun Anda.',
        shop_hist_empty: 'Belum ada riwayat transaksi tercatat untuk akun Anda.',
        shop_btn_back: 'Kembali ke Toko',
        shop_top_title: 'Pelindung Kerajaan',
        shop_top_title_header: 'Donatur Teratas',
        shop_recent_title: 'Pembayaran Terkini',
        shop_goal_title: 'Target Donasi Server',
        shop_credit: 'Isi Saldo',
        shop_cart_title: 'Keranjang',
        shop_profile_payments: 'Riwayat Transaksi',
        shop_th_price: 'Harga',
        shop_th_type: 'Tipe',
        shop_th_status: 'Status',
        shop_th_payment_id: 'ID Transaksi',
        shop_th_date: 'Tanggal',
        shop_purchases_title: 'Riwayat Pembelian',
        shop_subscriptions_title: 'Riwayat Langganan',
        cart_total_pay: 'TOTAL PEMBAYARAN:',
        cart_th_name: 'Nama',
        cart_th_price: 'Harga',
        cart_th_total: 'Total',
        cart_th_quantity: 'Jumlah',
        cart_th_action: 'Aksi',
        cart_btn_update: 'Perbarui',
        cart_btn_clear: 'Kosongkan Keranjang',
        cart_btn_back: 'Kembali',
        cart_btn_checkout: 'Lanjutkan ke Pembayaran',

        // Auth
        auth_login_title: 'Masuk',
        auth_login_email_label: 'Alamat Email',
        auth_login_email_ph: 'nama@email.com atau username',
        auth_login_pass_label: 'Kata Sandi',
        auth_login_pass_ph: 'Masukkan kata sandi akun',
        auth_remember: 'Ingat saya',
        auth_forgot_pass: 'Lupa kata sandi?',
        auth_login_btn: 'Masuk',
        auth_login_no_account: 'Belum memiliki akun?',
        auth_login_register_link: 'Daftar sekarang',
        auth_reg_title: 'Daftar',
        auth_reg_name_label: 'Nama / IGN',
        auth_reg_ign_ph: 'Nickname in-game Minecraft Anda',
        auth_reg_email_label: 'Alamat Email',
        auth_reg_email_ph: 'Alamat email aktif',
        auth_reg_pass_label: 'Kata Sandi',
        auth_reg_pass_ph: 'Minimal 8 karakter',
        auth_reg_confirm_label: 'Konfirmasi Kata Sandi',
        auth_reg_confirm_ph: 'Ulangi kata sandi',
        auth_reg_btn: 'Daftar',
        auth_reg_has_account: 'Sudah memiliki akun?',
        auth_reg_login_link: 'Masuk sekarang',
        auth_logout: 'Keluar',

        // Vote Rewards
        vote_r_crate_key: '1x Kunci Peti Pusaka (Vote Key)',
        vote_r_bp_250: '+250 Battlepass XP',
        vote_r_bp_200: '+200 Battlepass XP',
        vote_r_bp_300: '+300 Battlepass XP',
        vote_r_balance_10k: 'Rp 10.000 Saldo Peradaban',
        vote_r_diamond_3: '3x Diamond Murni Kerajaan',
        vote_r_bottle_mystic: '1x Botol Penempaan Mistis',

        // Posts
        posts_likes: 'Menyukai',

        // Leaderboard
        leaderboard_kicker: 'DEWAN KEHORMATAN APEXSIONS',
        leaderboard_title: 'Papan Peringkat Peradaban',
        leaderboard_desc: 'Catatan kejayaan pengembara terhebat, kekayaan konglomerat kerajaan, dan dominasi faksi Tiga Kerajaan di seluruh realm Apexsions.',
        leaderboard_population: 'Populasi:',
        leaderboard_citizens: 'Warga',
        leaderboard_power_level: 'Kekuatan Level:',
        leaderboard_lv: 'Lv',
        leaderboard_top_level_title: 'Top 10 Level & Pengalaman (EXP)',
        leaderboard_badge_progression: 'Progresi',
        leaderboard_top_balance_title: 'Top 10 Konglomerat Realm (Saldo)',
        leaderboard_badge_economy: 'Ekonomi',
        leaderboard_th_player: 'Pemain',
        leaderboard_th_rank: 'Rank',
        leaderboard_th_level_xp: 'Level & XP',
        leaderboard_th_kingdom: 'Kerajaan',
        leaderboard_th_wealth: 'Kekayaan',
        leaderboard_empty_players: 'Belum ada data pemain terverifikasi.',
        leaderboard_empty_economy: 'Belum ada data kekayaan pemain.',

        // Public Profile & Kingdoms
        profile_pub_back: 'Kembali ke Papan Peringkat',
        profile_pub_kingdom_prefix: 'Kerajaan',
        profile_pub_level_badge: 'Level',
        profile_pub_tier_title: 'Gelar Tingkat:',
        profile_pub_default_title: 'Pengelana Awal',
        profile_pub_last_seen: 'Terakhir Aktif:',
        profile_pub_uuid: 'Minecraft UUID:',
        profile_pub_status: 'Status Akun:',
        profile_pub_verified: 'Terverifikasi Resmi',
        kingdom_zenithar_name: 'Zenithar',
        kingdom_solterra_name: 'Solterra',
        kingdom_sylvamoor_name: 'Sylvamoor',
        kingdom_none_name: 'Tanpa Kerajaan',

        // Profile & BattlePass
        profile_title: 'Profil Pengguna',
        profile_sub: 'Kelola informasi akun website dan karakter in-game Apexsions Anda.',
        profile_btn_refresh: 'Segarkan',
        profile_btn_leaderboard: 'Papan Peringkat',
        profile_btn_public: 'Lihat Profil Publik',
        profile_kingdom_label: 'Kerajaan Faksi:',
        profile_kingdom_none: 'Belum Memilih',
        profile_core_level_label: 'Level Peradaban',
        profile_core_xp_label: 'Exp Karakter',
        profile_balance_rp: 'Saldo Rupiah:',
        profile_balance_dia: 'Diamond:',
        profile_balance_coins: 'Apex Coins:',
        profile_bp_title: 'BattlePass: Musim Peradaban',
        profile_bp_tier: 'Tier',
        profile_bp_premium: 'PREMIUM PASS',
        profile_bp_free: 'FREE PASS',
        profile_bp_xp: 'Progress Tier Pass',
        profile_bp_sync_tip: 'Ketik /sync atau /bp in-game untuk update seketika ke web.',
        profile_reward_daily: 'Hadiah Harian',
        profile_reward_ready: 'Siap Diklaim',
        profile_reward_ready_sub: 'Dapatkan <strong>+Rp 5.000</strong> & <strong>+25 EXP</strong> in-game setiap 24 jam!',
        profile_reward_btn_claim: 'Klaim Hadiah Harian',
        profile_reward_claimed: 'Sudah Diklaim',
        profile_reward_next_available: 'Klaim berikutnya tersedia dalam:',

        // Shop Webstore Keys
        shop_kicker: 'TRANSAKSI RESMI PERADABAN',
        shop_index_title: 'Webstore Resmi Apexsions',
        shop_index_sub: 'Selamat datang di pusat logistik peradaban tertinggi. Dukung kelangsungan server sembari memperkuat supremasi karaktermu dengan kasta donatur, seasonal battlepass, booster, dan pundi koin.',
        shop_nav_home: 'Beranda Toko',
        shop_wa_title: 'Pemesanan Langsung & Terverifikasi via WhatsApp Founder',
        shop_wa_badge: 'AKTIVASI CEPAT',
        shop_wa_desc: 'Pilih paket yang diinginkan lalu hubungi salah satu dari 2 Founder resmi kami: <strong class="text-white">Rifqi</strong> atau <strong class="text-white">Friell</strong>. Pesanan diproses aman, transparan, dan langsung aktif ke in-game.',
        shop_hub_badge: 'LOGISTIK REALM • APEXSIONS',
        shop_hub_title: 'Pusat Perbekalan & Kehormatan Peradaban',
        shop_hub_desc: 'Pilih kategori di bawah atau jelajahi paket unggulan. Setiap kontribusi langsung dialokasikan untuk pemeliharaan server berkecepatan tinggi dan otomatis aktif ke akun Minecraft kamu.',
        shop_btn_explore: 'Mulai Jelajahi Katalog',
        shop_btn_guide: 'Bimbingan Transaksi',
        shop_featured_title: 'Paket Paling Populer & Direkomendasikan',
        shop_featured_sub: 'Pilihan terfavorit para kaisar dan penguasa peradaban Apexsions.',
        shop_badge_3days: '3 HARI AKTIF',
        shop_badge_perm: 'PERMANEN',
        shop_sions_p1: 'Prefix Mahkota ✦ SIONS ✦',
        shop_sions_p2: 'Seluruh Kit + Kit Sions Eksklusif',
        shop_sions_p3: 'Pesan Broadcast Masuk Server Megah',
        shop_sions_p4: '+15 Batas Klaim Wilayah Kerajaan',
        shop_prempass_p1: 'Buka Jalur Emas 100 Level Hadiah',
        shop_prempass_p2: 'Akses Quests Harian & Mingguan',
        shop_prempass_p3: '+25% Pengganda Perolehan EXP Pass',
        shop_prempass_p4: 'Diskon Toko Berputar /abp shop',
        shop_fallback_p1: 'Aktivasi Otomatis via Akun Minecraft',
        shop_fallback_p2: 'Dukungan Transaksi Aman & Terverifikasi',
        shop_choose_founder: 'Pilih Founder:',
        shop_direct_order: 'Pesan Langsung',
        shop_btn_wa: 'Pesan Cepat via WhatsApp',
        shop_btn_details: 'Rincian & Benefit Lengkap',
        shop_guarantee_title: 'Jaminan & Transparansi Layanan Webstore',
        shop_g1_title: 'Aktivasi Instan',
        shop_g1_desc: 'Sinkronisasi real-time via WebBridge in-game tanpa jeda.',
        shop_g2_title: 'Pembayaran Fleksibel',
        shop_g2_desc: 'Mendukung QRIS, GoPay, OVO, DANA, ShopeePay, & Bank Transfer.',
        shop_g3_title: 'Terikat UUID Pemain',
        shop_g3_desc: 'Perk dan donasi tersimpan aman pada identitas unik akun Minecraft Anda.',
        shop_g4_title: 'Bimbingan Founder Langsung',
        shop_g4_desc: 'Konsultasi dan bantuan transaksi langsung ditangani oleh Rifqi & Friell.',
        shop_steps_title: '4 Langkah Mudah Berbelanja di Webstore Apexsions',
        shop_step1_title: '1. Pilih Paket',
        shop_step1_desc: 'Pilih kasta rank, battlepass, atau booster yang Anda perlukan dari katalog.',
        shop_step2_title: '2. Konfirmasi Akun',
        shop_step2_desc: 'Pastikan Username Minecraft (IGN) telah sesuai dan terdaftar di server.',
        shop_step3_title: '3. Chat Founder via WA',
        shop_step3_desc: 'Dapatkan nomor rekening/QRIS resmi dari Founder Rifqi atau Friell.',
        shop_step4_title: '4. Nikmati Keuntungan',
        shop_step4_desc: 'Login ke Minecraft, perk langsung aktif dan siap digunakan!',
    },
    en: {
        // Navigation
        nav_home: 'Home',
        nav_civilizations: 'Civilizations',
        nav_gameplay: 'Gameplay',
        nav_leaderboard: 'Leaderboard',
        nav_shop: 'Webstore',
        nav_more: 'More',
        nav_wiki: 'Wiki',
        nav_wiki_sub: 'Game mechanics, castes & forging',
        nav_rules: 'Rules',
        nav_rules_sub: 'Code of conduct & realm sovereignty',
        nav_vote: 'Vote',
        nav_vote_sub: 'Daily voting & in-game rewards',
        nav_discord: 'Discord',
        nav_btn_discord: 'Discord',
        nav_features: 'Features',
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
        wiki_all_categories: 'Browse All Categories',
        // Legal and Rules Additions
        legal_article_word: 'ARTICLE',
        rules_prevention_title: 'ACTIVE SYSTEM PREVENTION MECHANISM:',
        rules_s_mute: 'Sanction: Mute',
        rules_s_warn_mute: 'Sanction: Warning / Mute',
        rules_s_mute_tempban: 'Sanction: Mute &bull; Temp Ban',
        rules_s_perm_ban: 'Sanction: Permanent Ban',
        rules_s_rollback_permban: 'Sanction: Data Rollback &bull; Permanent Ban',
        rules_s_rollback_tempban: 'Sanction: Region Rollback &bull; Temp Ban',
        rules_s_confiscate_tempban: 'Sanction: Machine Confiscation &bull; Temp Ban',
        rules_s_death_drop: 'Sanction: Auto-Death &bull; Drop Inventory',
        rules_s_blacklist: 'Sanction: Permanent Blacklist',
        rules_s_freeze_alt: 'Sanction: Alt Account Freeze',
        rules_s_perm_mute_ban: 'Sanction: Permanent Mute &bull; Banned',
        rules_s_perm_ban_legal: 'Sanction: Permanent Ban &bull; Criminal Prosecution',

        // Wiki Show & Navigation Additions
        wiki_back_category: 'Back to Category',
        wiki_article_list: 'ARTICLE LIST',
        wiki_share: 'Share',
        wiki_official_doc: 'Apexsions Official Document',
        wiki_min_read: 'min read',
        wiki_copy_code: 'Copy',
        wiki_open_encyclopedia: 'Open Encyclopedia',

        // Posts / News Articles
        posts_breadcrumb: 'Dispatches & Articles',
        posts_header_kicker: 'OFFICIAL DISPATCHES & ARTICLES',
        posts_header_title: 'Civilization Dispatches & Articles',
        posts_header_desc: 'Follow all recent feature releases, server patch notes, kingdom war chronicles, and official Apexsions community articles.',
        posts_search_placeholder: 'Search dispatches or articles...',
        posts_empty_title: 'No Dispatches Published Yet',
        posts_empty_desc: 'You can explore all server documentation, mechanical guides, and lore via our official Wiki Encyclopedia.',
        posts_read_more: 'Read Article',
        posts_back: 'Return to Articles List',
        posts_comments_title: 'Citizen Discussions & Comments',
        posts_leave_comment: 'Leave a Comment',
        posts_comment_placeholder: 'Share your respectful thoughts...',
        posts_send_comment: 'Post Comment',
        posts_guest_comment: 'Please sign in to participate in discussions.',

        // Caste System (Home Page)
        caste_t5_roman: 'TIER V',
        caste_t5_name: 'ANCESTRAL APEX',
        caste_ancestor_badge: 'SUPREME THRONE',
        caste_ancestor_desc: 'Absolute founder and primary sovereign guardian of the Apexsions realm. Holds supreme sovereign mandate over world order and civilization continuity.',
        caste_ancestor_p1: 'Supreme mandate over server legal order',
        caste_ancestor_p2: 'Full power of the honor council',
        caste_ancestor_p3: 'Unrestricted access to all territories',
        caste_ancestor_p4: 'Absolute realm sovereignty immunity',
        caste_btn_mandate: 'Ancestral Mandate',

        caste_t4_roman: 'TIER IV',
        caste_t4_name: 'COUNCIL OF AUTHORITY',
        caste_architect_badge: 'REALM AUTHORITY',
        caste_architect_desc: 'World landscape designers and builders of grand kingdom megastructures. Oversees geographical layout and architectural wonders between kingdoms.',
        caste_architect_p1: 'Kingdom territory construction overseer',
        caste_architect_p2: 'Urban planning intervention permits',
        caste_architect_p3: 'Wilderness exploration privileges',
        caste_architect_p4: 'Realm architectural advisory council',
        caste_btn_role: 'Authority Role',

        caste_overseer_badge: 'REALM INTEGRITY',
        caste_overseer_desc: 'Guardian of justice, economic balance, and civilization law enforcer. Safeguards server integrity from cheating and disruption of order.',
        caste_overseer_p1: 'Audit economic transactions and markets',
        caste_overseer_p2: 'Investigate kingdom territory disputes',
        caste_overseer_p3: 'Enforce laws and infraction sanctions',
        caste_overseer_p4: 'Investigative authority over staff and citizens',

        caste_t3_roman: 'TIER III',
        caste_t3_name: 'ADMINISTRATION & STAFF',
        caste_warden_badge: 'HEAD OF STAFF',
        caste_warden_desc: 'Commander of civil order and staff operations coordinator. Resolves citizen report escalations and directs neutral territory security.',
        caste_warden_p1: 'Operational moderation coordinator',
        caste_warden_p2: 'Review citizen penalty appeals',
        caste_warden_p3: 'Maintain peace in neutral zones',
        caste_warden_p4: 'Manage Discord support ticket queues',

        caste_herald_badge: 'REALM MODERATOR',
        caste_herald_desc: 'Voice of the realm, guide for newcomers, and chat mediator. Ensures communication among citizens remains peaceful and civilized.',
        caste_herald_p1: 'Active moderation of public chat channels',
        caste_herald_p2: 'Guidance and onboarding for new citizens',
        caste_herald_p3: 'Mediate minor player disputes',
        caste_herald_p4: 'Issue formal warnings and mute penalties',

        caste_t2_roman: 'TIER II',
        caste_t2_name: 'ORDER OF NOBLES (DONATORS)',
        caste_sions_badge: 'HIGHEST DONATOR CASTE',
        caste_sions_desc: 'Highest honor for primary benefactors of realm sovereignty. Enjoys complete privileges, exclusive territory access, and unmatched prestige.',
        caste_sions_p1: '10x Set Homes & Claim Flying privileges',
        caste_sions_p2: 'Weekly Mythic & Golden Crate Keys',
        caste_sions_p3: 'Priority queue when the server is full',
        caste_sions_p4: 'Prestige prefix & special cosmetic trails',
        caste_btn_webstore_small: 'Webstore',

        caste_emperor_badge: 'TIER 4 NOBLE',
        caste_emperor_desc: 'Ruler of vast domains with formidable military and economic prestige. Enjoys territorial expansion leeway and superior logistical amenities.',
        caste_emperor_p1: '7x Set Homes & Virtual Chest access',
        caste_emperor_p2: 'Weekly Relic & Diamond Crate Keys',
        caste_emperor_p3: 'Periodic Emperor combat gear kits',
        caste_emperor_p4: 'On-demand equipment repair (/repair)',

        caste_sovereign_badge: 'TIER 3 NOBLE',
        caste_sovereign_desc: 'Prestigious aristocrat supporting market prosperity and border fortifications. Enjoys portable crafting utilities and swift progression.',
        caste_sovereign_p1: '5x Set Homes & Portable Workbench',
        caste_sovereign_p2: 'Weekly Gold Crate Key & bonus coins',
        caste_sovereign_p3: 'High-grade Sovereign armor kits',
        caste_sovereign_p4: 'Reduced auction house transaction fees',

        caste_archon_badge: 'TIER 2 NOBLE',
        caste_archon_desc: 'Respected knight and landowner across the realm. Supported by daily conveniences and enhanced forging efficiency.',
        caste_archon_p1: '3x Set Homes & Portable Anvil',
        caste_archon_p2: 'Weekly Relic Crate Key',
        caste_archon_p3: 'Specially forged Archon weapon kits',
        caste_archon_p4: 'Expanded storage limits',

        caste_ascendant_badge: 'TIER 1 NOBLE',
        caste_ascendant_desc: 'The initial step into the esteemed circle of realm nobility. Provides valuable acceleration for ambitious citizens.',
        caste_ascendant_p1: '2x Set Homes & Portable Furnace',
        caste_ascendant_p2: 'Initial Rupiah & Diamond bonus coins',
        caste_ascendant_p3: 'Complete Ascendant starter gear kit',
        caste_ascendant_p4: 'Vibrant emerald prefix distinction',

        caste_t1_roman: 'TIER I',
        caste_t1_name: 'CIVILIZATION FOUNDATION',
        caste_wanderer_badge: 'DEFAULT / ALL CITIZENS',
        caste_wanderer_desc: 'The bedrock of Apexsions society. Every citizen begins their journey here: free to pledge to any kingdom, harvest the land, trade in open markets, and ascend the ladder of prestige.',
        caste_wanderer_p1: 'Right to pledge to 1 of 3 sovereign kingdoms',
        caste_wanderer_p2: '1x Set Home & land claim protection',
        caste_wanderer_p3: 'Full participation in economy & Kingdom Wars',
        caste_wanderer_p4: 'Access to beginner initiation kits (/kit starter)',
        caste_btn_join: 'How to Join',
        caste_btn_full_guide: 'Open Full Caste Guide',
        caste_btn_webstore: 'Visit Official Webstore',

        // Profile Page
        profile_title: 'User Profile',
        profile_sub: 'Manage your website account details and Apexsions in-game character.',
        profile_btn_leaderboard: 'Leaderboard',
        profile_btn_public: 'View Public Profile',
        profile_kingdom_label: 'Pledged Kingdom:',
        profile_kingdom_none: 'None Chosen',
        profile_balance_rp: 'Rupiah Balance:',
        profile_balance_dia: 'Diamond:',
        profile_balance_coins: 'Apex Coins:',
        profile_bp_title: 'Civilization Season (BattlePass)',
        profile_bp_tier: 'Tier',
        profile_bp_premium: 'PREMIUM PASS',
        profile_bp_free: 'FREE PASS',
        profile_bp_exsio: 'EXSIO PASS',
        profile_bp_sio: 'SIO PASS',
        profile_bp_citizen: 'CITIZEN PASS',
        profile_bp_xp: 'Season Progress:',
        profile_reward_daily: 'Daily Reward',
        profile_reward_ready: 'Ready to Claim',
        profile_reward_ready_sub: 'Receive <strong>+Rp 5,000</strong> & <strong>+25 EXP</strong> in-game every 24 hours!',
        profile_reward_btn_claim: 'Claim Daily Reward',
        profile_reward_claimed: 'Claimed',
        profile_reward_next_available: 'Next claim available in:',
        profile_reward_tomorrow_prefix: 'Available tomorrow at',
        profile_reward_btn_cooldown: 'On Cooldown',
        profile_btn_reset_pass: 'Reset In-Game Password',
        profile_btn_unlink: 'Unlink Account',
        profile_unlink_title: 'Link Your Minecraft Character',
        profile_unlink_desc: 'Link your website account with Apexsions Minecraft server to showcase your 3D skin, sync rank & level automatically, claim daily rewards, and manage your in-game profile.',
        profile_unlink_btn: 'Link Minecraft Account Now',
        profile_lbl_registered: 'Registered:',
        profile_lbl_points: 'Web Points:',
        profile_btn_manage_2fa: 'Manage 2FA',
        profile_btn_enable_2fa: 'Enable 2FA',
        profile_title_change_email: 'Change Email Address',
        profile_lbl_new_email: 'New Email',
        profile_lbl_curr_pass: 'Current Password',
        profile_btn_update_email: 'Update Email',
        profile_title_change_pass: 'Change Web Password',
        profile_lbl_new_pass: 'New Password',
        profile_lbl_confirm_pass: 'Confirm New Password',
        profile_btn_update_pass: 'Update Web Password',
        profile_modal_reset_title: 'Reset In-Game Password (AuthMe)',
        profile_modal_reset_alert: 'A password reset command will be dispatched to the Minecraft server for account',
        profile_modal_lbl_new_pass: 'New In-Game Password (Min 6 characters)',
        profile_modal_ph_new_pass: 'Enter new password...',
        profile_modal_lbl_confirm_pass: 'Confirm New Password',
        profile_modal_ph_confirm_pass: 'Re-enter new password...',
        profile_modal_btn_cancel: 'Cancel',
        profile_modal_btn_submit: 'Confirm & Change Password',

        // Account Link Page
        link_header_title: 'Link Minecraft Account',
        link_header_sub: 'Connect your Java Edition or Bedrock Edition account to the Apexsions web portal',
        link_pin_success_title: 'Your Verification PIN Has Been Generated!',
        link_pin_success_sub: 'Log in to our Minecraft server and run the following command within <strong>5 minutes</strong>:',
        link_pin_copy: 'Copy',
        link_pin_target_user: 'Target username:',
        link_pin_valid_until: 'Valid until:',
        link_connected_title: 'Currently Linked Accounts',
        link_empty_text: 'No Minecraft account is currently linked to this web account.',
        link_badge_verified: 'Verified',
        link_badge_pending: 'Pending In-Game Link',
        link_btn_unlink: 'Unlink',
        link_form_title: 'Link New Account',
        link_lbl_username: 'Minecraft Username',
        link_ph_username: 'Example: Steve or .Steve (Bedrock)',
        link_floodgate_hint: 'Bedrock players can include their Floodgate prefix (such as a dot or asterisk).',
        link_lbl_edition: 'Minecraft Edition',
        link_opt_java: '(PC / Mac / Linux)',
        link_opt_bedrock: '(Geyser / Floodgate / Mobile / Console)',
        link_btn_get_pin: 'Generate Verification PIN',

        // Webstore & Shop
        shop_pkg_subtitle: 'Apexsions Official Webstore • Direct & Secure Transactions',
        shop_pkg_wa_title: 'Direct Order via Founder WhatsApp:',
        shop_pkg_wa_note: 'Choose a Founder to start a WhatsApp chat with your pre-filled order details:',
        shop_founder_role: 'Founder',
        shop_btn_close: 'Close',
        shop_btn_order_now: 'Order Fast Now',
        shop_badge_3days: '3 Days',
        shop_badge_perm: 'Permanent',
        shop_sions_p1: 'Prefix [✦ SIONS] & Gradient Chat',
        shop_sions_p2: '10x Set Homes & Claim Flying privileges',
        shop_sions_p3: 'Weekly Mythic & Golden Crate Keys',
        shop_sions_p4: 'Priority Queue & Apex Gear Kit',
        shop_emperor_p1: 'Prefix [⚔ EMPEROR] & Crimson Chat',
        shop_emperor_p2: '7x Set Homes & Virtual Chest',
        shop_emperor_p3: 'Weekly Relic & Diamond Crate Keys',
        shop_emperor_p4: 'Emperor Gear Kit & Emotes',
        shop_sovereign_p1: 'Prefix [⚜ SOVEREIGN] & Golden Chat',
        shop_sovereign_p2: '5x Set Homes & Portable Workbench',
        shop_sovereign_p3: 'Weekly Gold Crate Key & Bonus Coins',
        shop_sovereign_p4: 'Sovereign Armor Kit & Particle Trail',
        shop_archon_p1: 'Prefix [💎 ARCHON] & Cyan Chat',
        shop_archon_p2: '3x Set Homes & Portable Anvil',
        shop_archon_p3: 'Weekly Relic Crate Key',
        shop_archon_p4: 'Archon Weapon Kit & Shop Discount',
        shop_ascendant_p1: 'Prefix [☘ ASCENDANT] & Emerald Chat',
        shop_ascendant_p2: '2x Set Homes & Portable Furnace',
        shop_ascendant_p3: 'Starter Currency Bonus',
        shop_ascendant_p4: 'Ascendant Starter Gear Kit',
        shop_vippass_p1: 'Unlock Seasonal VIP Reward Track',
        shop_vippass_p2: 'Exclusive Cloak & Wing Particles',
        shop_vippass_p3: '+25% Quest Battlepass EXP Boost',
        shop_vippass_p4: 'Legendary Crate Key Every 10 Levels',
        shop_prempass_p1: 'Full Access to Premium Track',
        shop_prempass_p2: 'Special Title & Banner Cosmetics',
        shop_prempass_p3: '+50% Quest Battlepass EXP Boost',
        shop_prempass_p4: 'Mythic Crate Key & Exclusive Skins',
        shop_fallback_p1: 'Apexsions Official Webstore Package',
        shop_fallback_p2: 'Automatic & Verified Activation',
        shop_fallback_p3: 'Full Technical Support from Founders',
        shop_hist_kicker: '<i class="bi bi-receipt"></i> OFFICIAL TRANSACTION HISTORY',
        shop_hist_sub: 'Archive of transaction records, acquired donor ranks, and official subscriptions of your account.',
        shop_hist_empty: 'No transaction history recorded for your account yet.',
        shop_btn_back: 'Back to Store',
        shop_top_title: 'Realm Protector',
        shop_top_title_header: 'Top Donator',
        shop_recent_title: 'Recent Payments',
        shop_goal_title: 'Server Donation Goal',
        shop_credit: 'Add Balance',
        shop_cart_title: 'Cart',
        shop_profile_payments: 'Purchase History',
        shop_th_price: 'Price',
        shop_th_type: 'Type',
        shop_th_status: 'Status',
        shop_th_payment_id: 'Transaction ID',
        shop_th_date: 'Date',
        shop_purchases_title: 'Purchase History',
        shop_subscriptions_title: 'Subscription History',
        cart_total_pay: 'TOTAL PAYMENT:',
        cart_th_name: 'Name',
        cart_th_price: 'Price',
        cart_th_total: 'Total',
        cart_th_quantity: 'Quantity',
        cart_th_action: 'Action',
        cart_btn_update: 'Update',
        cart_btn_clear: 'Clear Cart',
        cart_btn_back: 'Back',
        cart_btn_checkout: 'Proceed to Checkout',

        // Auth
        auth_login_title: 'Login',
        auth_login_email_label: 'Email Address',
        auth_login_email_ph: 'name@email.com or username',
        auth_login_pass_label: 'Password',
        auth_login_pass_ph: 'Enter account password',
        auth_remember: 'Remember me',
        auth_forgot_pass: 'Forgot password?',
        auth_login_btn: 'Login',
        auth_login_no_account: 'Don\'t have an account?',
        auth_login_register_link: 'Register now',
        auth_reg_title: 'Register',
        auth_reg_name_label: 'Name / IGN',
        auth_reg_ign_ph: 'Your in-game Minecraft nickname',
        auth_reg_email_label: 'Email Address',
        auth_reg_email_ph: 'Active email address',
        auth_reg_pass_label: 'Password',
        auth_reg_pass_ph: 'Min. 8 characters',
        auth_reg_confirm_label: 'Confirm Password',
        auth_reg_confirm_ph: 'Re-enter password',
        auth_reg_btn: 'Register',
        auth_reg_has_account: 'Already have an account?',
        auth_reg_login_link: 'Sign in now',
        auth_logout: 'Logout',

        // Vote Rewards
        vote_r_crate_key: '1x Relic Crate Key (Vote Key)',
        vote_r_bp_250: '+250 Battlepass XP',
        vote_r_bp_200: '+200 Battlepass XP',
        vote_r_bp_300: '+300 Battlepass XP',
        vote_r_balance_10k: 'Rp 10,000 Realm Balance',
        vote_r_diamond_3: '3x Pure Realm Diamonds',
        vote_r_bottle_mystic: '1x Mystic Forging Bottle',

        // Posts
        posts_likes: 'Likes',

        // Leaderboard
        leaderboard_kicker: 'APEXSIONS HALL OF FAME',
        leaderboard_title: 'Civilization Leaderboard',
        leaderboard_desc: 'Hall of records celebrating the greatest adventurers, conglomerate wealth, and Three Kingdoms faction dominance across Apexsions.',
        leaderboard_population: 'Population:',
        leaderboard_citizens: 'Citizens',
        leaderboard_power_level: 'Total Power:',
        leaderboard_lv: 'Lv',
        leaderboard_top_level_title: 'Top 10 Level & Experience (EXP)',
        leaderboard_badge_progression: 'Progression',
        leaderboard_top_balance_title: 'Top 10 Realm Conglomerates (Balance)',
        leaderboard_badge_economy: 'Economy',
        leaderboard_th_player: 'Player',
        leaderboard_th_rank: 'Rank',
        leaderboard_th_level_xp: 'Level & XP',
        leaderboard_th_kingdom: 'Kingdom',
        leaderboard_th_wealth: 'Wealth',
        leaderboard_empty_players: 'No verified player data recorded yet.',
        leaderboard_empty_economy: 'No player wealth data recorded yet.',

        // Public Profile & Kingdoms
        profile_pub_back: 'Back to Leaderboards',
        profile_pub_kingdom_prefix: 'Kingdom',
        profile_pub_level_badge: 'Level',
        profile_pub_tier_title: 'Tier Title:',
        profile_pub_default_title: 'Initial Wanderer',
        profile_pub_last_seen: 'Last Seen:',
        profile_pub_uuid: 'Minecraft UUID:',
        profile_pub_status: 'Account Status:',
        profile_pub_verified: 'Officially Verified',
        kingdom_zenithar_name: 'Zenithar',
        kingdom_solterra_name: 'Solterra',
        kingdom_sylvamoor_name: 'Sylvamoor',
        kingdom_none_name: 'No Kingdom',

        // Profile & BattlePass
        profile_title: 'User Profile',
        profile_sub: 'Manage your website account and Apexsions in-game character information.',
        profile_btn_refresh: 'Refresh',
        profile_btn_leaderboard: 'Leaderboards',
        profile_btn_public: 'View Public Profile',
        profile_kingdom_label: 'Kingdom Faction:',
        profile_kingdom_none: 'Not Chosen Yet',
        profile_core_level_label: 'Civilization Level',
        profile_core_xp_label: 'Character EXP',
        profile_balance_rp: 'Rupiah Balance:',
        profile_balance_dia: 'Diamond:',
        profile_balance_coins: 'Apex Coins:',
        profile_bp_title: 'BattlePass: Season of Civilizations',
        profile_bp_tier: 'Tier',
        profile_bp_premium: 'PREMIUM PASS',
        profile_bp_free: 'FREE PASS',
        profile_bp_xp: 'Pass Tier Progress',
        profile_bp_sync_tip: 'Type /sync or /bp in-game to update instantly to web.',
        profile_reward_daily: 'Daily Reward',
        profile_reward_ready: 'Ready to Claim',
        profile_reward_ready_sub: 'Receive <strong>+Rp 5,000</strong> & <strong>+25 EXP</strong> in-game every 24 hours!',
        profile_reward_btn_claim: 'Claim Daily Reward',
        profile_reward_claimed: 'Already Claimed',
        profile_reward_next_available: 'Next claim available in:',

        // Shop Webstore Keys
        shop_kicker: 'OFFICIAL CIVILIZATION STORE',
        shop_index_title: 'Apexsions Official Webstore',
        shop_index_sub: 'Welcome to the citadel logistics hub of the highest civilizations. Support server longevity while elevating your realm supremacy with donor ranks, seasonal battlepass, boosters, and coins.',
        shop_nav_home: 'Store Home',
        shop_wa_title: 'Direct & Verified Order via WhatsApp Founder',
        shop_wa_badge: 'FAST ACTIVATION',
        shop_wa_desc: 'Select your desired package and reach out directly to one of our 2 official Founders: <strong class="text-white">Rifqi</strong> or <strong class="text-white">Friell</strong>. Orders are processed securely, transparently, and activated in-game.',
        shop_hub_badge: 'REALM LOGISTICS • APEXSIONS',
        shop_hub_title: 'Citadel Depot & Civilization Honor',
        shop_hub_desc: 'Select a category below or explore featured packages. Every contribution directly funds high-performance server hosting and activates automatically on your Minecraft account.',
        shop_btn_explore: 'Start Exploring Catalog',
        shop_btn_guide: 'Order Guidance',
        shop_featured_title: 'Most Popular & Recommended Packages',
        shop_featured_sub: 'Top favorites among emperors and rulers of Apexsions.',
        shop_badge_3days: '3 DAYS ACTIVE',
        shop_badge_perm: 'PERMANENT',
        shop_sions_p1: 'Crown Prefix ✦ SIONS ✦',
        shop_sions_p2: 'All Kits + Exclusive Sions Kit',
        shop_sions_p3: 'Majestic Server Join Broadcast',
        shop_sions_p4: '+15 Kingdom Land Claim Limit',
        shop_prempass_p1: 'Unlock Golden Track with 100 Tiers of Rewards',
        shop_prempass_p2: 'Access Daily & Weekly Quests',
        shop_prempass_p3: '+25% Pass EXP Gain Multiplier',
        shop_prempass_p4: 'Discounts in Rotating Shop /abp shop',
        shop_fallback_p1: 'Automatic In-Game Account Activation',
        shop_fallback_p2: 'Secure & Verified Transaction Support',
        shop_choose_founder: 'Choose Founder:',
        shop_direct_order: 'Direct Order',
        shop_btn_wa: 'Quick Order via WhatsApp',
        shop_btn_details: 'Full Details & Benefits',
        shop_guarantee_title: 'Webstore Service Guarantees & Transparency',
        shop_g1_title: 'Instant Activation',
        shop_g1_desc: 'Real-time synchronization via in-game WebBridge without delay.',
        shop_g2_title: 'Flexible Payments',
        shop_g2_desc: 'Supports QRIS, GoPay, OVO, DANA, ShopeePay, & Bank Transfer.',
        shop_g3_title: 'Bound to Player UUID',
        shop_g3_desc: 'Perks and donations are securely bound to your unique Minecraft UUID.',
        shop_g4_title: 'Direct Founder Guidance',
        shop_g4_desc: 'Consultation and order assistance handled directly by Rifqi & Friell.',
        shop_steps_title: '4 Easy Steps to Order on Apexsions Webstore',
        shop_step1_title: '1. Select Package',
        shop_step1_desc: 'Choose your desired caste rank, battlepass, or booster from the catalog.',
        shop_step2_title: '2. Confirm Account',
        shop_step2_desc: 'Ensure your Minecraft Username (IGN) is accurate and registered in-game.',
        shop_step3_title: '3. Chat Founder on WA',
        shop_step3_desc: 'Receive official bank/QRIS transfer instructions from Founder Rifqi or Friell.',
        shop_step4_title: '4. Enjoy Perks',
        shop_step4_desc: 'Join the Minecraft server, your perks activate within seconds!',
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

            
            // Translate Wiki Dynamic Content
            if (typeof WIKI_DATA !== 'undefined') {
                document.querySelectorAll('[data-wiki-cat-name]').forEach(el => {
                    const catId = el.getAttribute('data-wiki-cat-name');
                    if (WIKI_DATA.categories[catId]) {
                        el.textContent = WIKI_DATA.categories[catId][targetLang] || el.textContent;
                    }
                });

                document.querySelectorAll('[data-wiki-cat-desc]').forEach(el => {
                    const catId = el.getAttribute('data-wiki-cat-desc');
                    if (WIKI_DATA.categories[catId]) {
                        const k = targetLang === 'en' ? 'desc_en' : 'desc_id';
                        el.textContent = WIKI_DATA.categories[catId][k] || el.textContent;
                    }
                });

                document.querySelectorAll('[data-wiki-header-title]').forEach(el => {
                    for (const catId in WIKI_DATA.categories) {
                        const cat = WIKI_DATA.categories[catId];
                        if (el.textContent.trim() === cat.id || el.textContent.trim() === cat.en) {
                            el.textContent = cat[targetLang];
                            break;
                        }
                    }
                });

                document.querySelectorAll('[data-wiki-title-id]').forEach(el => {
                    const pageId = el.getAttribute('data-wiki-title-id');
                    if (WIKI_DATA.pages[pageId]) {
                        el.textContent = WIKI_DATA.pages[pageId][targetLang] || el.textContent;
                    }
                });

                document.querySelectorAll('[data-wiki-more-articles]').forEach(el => {
                    const count = el.getAttribute('data-count') || '0';
                    el.textContent = targetLang === 'en' ? `+${count} more articles...` : `+${count} artikel lainnya...`;
                });

                document.querySelectorAll('[data-wiki-body-id]').forEach(el => {
                    const pageId = el.getAttribute('data-wiki-body-id');
                    if (!el.hasAttribute('data-original-html')) {
                        el.setAttribute('data-original-html', el.innerHTML);
                    }
                    if (targetLang === 'en' && WIKI_DATA.bodies && WIKI_DATA.bodies[pageId]) {
                        el.innerHTML = WIKI_DATA.bodies[pageId];
                    } else if (targetLang === 'id') {
                        el.innerHTML = el.getAttribute('data-original-html');
                    }
                });
            }

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


// ==========================================================================
// Apexsions Bilingual Wiki Data & English Body Content
// ==========================================================================
const WIKI_DATA = {
    categories: {
        '1': { id: 'Panduan Pemula & Perintah', en: 'Beginner Guides & Commands', desc_id: 'Pelajari seluk-beluk panduan pemula & perintah, aturan wilayah, dan panduan mekanik server Apexsions.', desc_en: 'Master beginner guides, foundational commands, kingdom rules, and core gameplay mechanics.' },
        '2': { id: 'Tiga Kerajaan & Kedaulatan', en: 'Three Kingdoms & Sovereignty', desc_id: 'Pelajari seluk-beluk tiga kerajaan & kedaulatan, aturan wilayah, dan panduan mekanik server Apexsions.', desc_en: 'Explore the 3 sovereign realms, ancient historical lore, territorial nexus claims, and war protocols.' },
        '3': { id: 'Ekonomi & Perdagangan', en: 'Economy & Commerce', desc_id: 'Pelajari seluk-beluk ekonomi & perdagangan, aturan wilayah, dan panduan mekanik server Apexsions.', desc_en: 'Discover atomic currency exchange, dynamic markets, auction houses, and barter escrow mechanics.' },
        '4': { id: 'Custom Enchants & Kits', en: 'Custom Enchants & Kits', desc_id: 'Pelajari seluk-beluk custom enchants & kits, aturan wilayah, dan panduan mekanik server Apexsions.', desc_en: 'Inspect 182 custom enchantments across 7 tiers, magic scrolls, dust, and native class armor set bonuses.' },
        '5': { id: 'Battlepass & Komunikasi', en: 'Battlepass & Communication', desc_id: 'Pelajari seluk-beluk battlepass & komunikasi, aturan wilayah, dan panduan mekanik server Apexsions.', desc_en: 'Understand 100-tier seasonal quest lines, EXP rotating shops, chat channels, and community reporting desk.' },
        '6': { id: 'Hierarki Kasta Resmi', en: 'Official Caste Hierarchy', desc_id: 'Pelajari seluk-beluk hierarki kasta resmi, aturan wilayah, dan panduan mekanik server Apexsions.', desc_en: 'Learn the 5-tier social structure, 11 official castes, and 10 spiritual sequence pathways.' }
    },
    pages: {
        '1': { id: 'Panduan 15 Menit Pertama Warga Baru (Zero-to-Hero Roadmap)', en: 'First 15 Minutes Guide for New Citizens (Zero-to-Hero Roadmap)' },
        '2': { id: 'Cara Bergabung ke Server Apexsions', en: 'How to Join Apexsions Server' },
        '3': { id: 'Daftar Perintah Resmi Server (Commands Cheat Sheet)', en: 'Official Server Commands Cheat Sheet' },
        '4': { id: 'Sistem Progresi Level 1–100 & Gelar Peradaban', en: 'Level 1–100 Progression System & Civilization Titles' },
        '5': { id: 'Babad Sejarah: Runtuhnya Kekaisaran Sions & Eksodus Akbar (The Fall of Sions)', en: 'Historical Lore: Fall of the Sions Empire & The Great Exodus' },
        '6': { id: 'Ensiklopedia 3 Kerajaan Berdaulat & Kondisi Wilayah', en: 'Encyclopedia of 3 Sovereign Kingdoms & Regional Traits' },
        '7': { id: 'Klaim Wilayah Kerajaan & Proteksi Nexus', en: 'Kingdom Territory Claims & Nexus Protection' },
        '8': { id: 'Perang Kerajaan, Siege & Combat Tag', en: 'Kingdom Wars, Sieges & Combat Tag System' },
        '9': { id: 'Sistem Mata Uang: Rupiah (Rp) & Diamond (💎)', en: 'Currency System: Rupiah (Rp) & Diamond (💎)' },
        '10': { id: 'Pasar Dinamis (Dynamic Market /shop & /sell)', en: 'Dynamic Market (/shop & /sell)' },
        '11': { id: 'Pasar Lelang (/ah) & Barter Escrow (/trade)', en: 'Auction House (/ah) & Barter Escrow (/trade)' },
        '12': { id: 'Strategi Kemakmuran: Panduan Menjadi Saudagar Sukses', en: 'Prosperity Strategy: Merchant Success Guide' },
        '13': { id: '182 Custom Enchantments & 7 Tingkatan Tier', en: '182 Custom Enchantments & 7 Tier Ranks' },
        '14': { id: 'Gulungan Sihir (Scrolls), Magic Dust & Scrambler', en: 'Magic Scrolls, Magic Dust & Scrambler' },
        '15': { id: 'Sistem Native Kits & Bonus Set Armor Berbasis Stat', en: 'Native Kits System & Stat-Based Armor Set Bonuses' },
        '16': { id: 'Panduan Meta Build Sihir & Sinergi Persenjataan', en: 'Meta Sorcery Builds & Weapon Synergy Guide' },
        '17': { id: 'Battlepass Musiman: Quests & EXP Shop', en: 'Seasonal Battlepass: Quests & EXP Shop' },
        '18': { id: 'Kanal Chat, Kingdom Tags & Layanan Pelaporan', en: 'Chat Channels, Kingdom Tags & Staff Reports Desk' },
        '19': { id: 'Etika Komunitas, Roleplay & Kode Kehormatan Peradaban', en: 'Community Ethics, Roleplay & Civilization Code of Honor' },
        '20': { id: 'Struktur 5 Tingkat & 11 Kasta Resmi Apexsions', en: '5-Tier Structure & 11 Official Castes of Apexsions' },
        '21': { id: 'Jalur Kenaikan Spiritual: 10 Urutan Kehormatan (Sequence Pathways)', en: 'Spiritual Ascension: 10 Sequence Pathways' }
    },
    bodies: {
        '1': `<h1>First 15 Minutes Guide: From Wanderer to Sovereign Citizen</h1>
<p>Welcome to <strong>Apexsions: The Peak Civilizations</strong>! You might feel the vastness of this kingdom universe is overwhelming at first glance. Fear not! Simply follow this 15-minute roadmap to become self-sufficient, establish your first shelter, and earn your initial Rupiah:</p>
<hr>
<h3>⏱ Minutes 0–2: Arrival at Spawn &amp; Claim Starter Kit</h3>
<ol>
<li>You will materialize at the grand court of <strong>Spawn Nexus</strong>.</li>
<li>Immediately execute the command:
<div class="apx-code-block-wrap"><pre><code>/kit starter</code></pre></div></li>
<li>You will receive a complete set of foundational tools, torches, nutritious bread, and the kingdom guidebook.</li>
<li><em>Tip:</em> Never discard your guidebook; keep it as your initial compass!</li>
</ol>
<hr>
<h3>⏱ Minutes 2–5: Teleport to Wilderness with Random Teleport (/rtp)</h3>
<ol>
<li>Do not waste precious time walking hundreds of blocks away from spawn!</li>
<li>Type:
<div class="apx-code-block-wrap"><pre><code>/rtp</code></pre></div></li>
<li>The <code>ApexsionsCore</code> engine automatically finds safe wilderness coordinates (free of steep drops or lava pools) in seconds.</li>
</ol>
<hr>
<h3>⏱ Minutes 5–8: Swear Allegiance to a Kingdom (/k)</h3>
<p>In Apexsions, your power multiplies upon joining one of the Three Sovereign Kingdoms. Type <code>/k</code> to open the selection interface:</p>
<ul>
<li>☀️ <strong>Zenithar (Celestial Horizon):</strong> Suited for miners and castle architects. Grants <code>+15%</code> Mining Speed &amp; Experience Buff.</li>
<li>🔥 <strong>Solterra (Crimson Flames &amp; Sands):</strong> Tailored for PvP duelists and conquerors. Grants <code>+15%</code> Melee Damage &amp; Fire Immunity in homeland.</li>
<li>🌿 <strong>Sylvamoor (Living Canopy &amp; Oceans):</strong> Perfect for farmers, breeders, and merchants. Grants <code>+20%</code> Crop Harvests &amp; Health Regeneration.</li>
</ul>
<p><em>Choose the realm that best aligns with your playstyle!</em></p>
<hr>
<h3>⏱ Minutes 8–12: Plant Your Home Banner (/sethome)</h3>
<ol>
<li>Harvest several wood logs and construct a Crafting Table.</li>
<li>Build a temporary shelter to protect against nocturnal threats.</li>
<li>Lock your outpost coordinates by typing:
<div class="apx-code-block-wrap"><pre><code>/sethome home</code></pre></div></li>
<li>Whenever lost in the wilderness or returning from an expedition, simply type <code>/home home</code> to return instantly.</li>
</ol>
<hr>
<h3>⏱ Minutes 12–15: Earn Your First Rupiah at the Market (/sell)</h3>
<ol>
<li>Gather surplus logs, coal, or raw iron nearby.</li>
<li>Open the quick selling portal via:
<div class="apx-code-block-wrap"><pre><code>/sell</code></pre></div></li>
<li>Deposit items you wish to sell into the merchant basket. Your Rupiah (<code>Rp</code>) balance updates instantly!</li>
<li>Check your financial treasury anytime with <code>/balance</code> or <code>/money</code>.</li>
</ol>
<hr>
<h3>🚀 Next Steps: Journey to the Pinnacle</h3>
<ul>
<li><strong>Advance Character Level:</strong> Mine precious ores and defeat monsters to progress from Level 1 to 100 and unlock prestigious <strong>Sequence Titles</strong>.</li>
<li><strong>Claim Sovereign Territory:</strong> Once your treasury permits, utilize <code>/k claim</code> to secure your civilization lands from raiding and griefing.</li>
<li><strong>Explore the Sorcery Altar:</strong> Access <code>/ce</code> or <code>/enchanter</code> to empower your armaments with 182 custom enchantments!</li>
</ul>`,

        '2': `<h1>How to Join Apexsions Server</h1>
<p>Apexsions is a high-performance modular Minecraft civilization server supporting both <strong>Java Edition</strong> and <strong>Bedrock Edition</strong> players concurrently via cross-platform Geyser technology.</p>
<hr>
<h3>Server Connection Information</h3>
<div class="table-responsive mb-4"><table class="table fandom-stat-table">
<thead><tr><th>Platform</th><th>Host / IP Address</th><th>Port</th><th>Minecraft Version</th></tr></thead>
<tbody>
<tr><td><strong>Java Edition</strong> (PC / Mac / Linux)</td><td><code>apexsions.my.id</code></td><td><code>32348</code></td><td><strong>26.2</strong> (Paper API)</td></tr>
<tr><td><strong>Bedrock Edition</strong> (Mobile / Win10 / Console)</td><td><code>apexsions.my.id</code></td><td><strong><code>32348</code></strong></td><td>Latest Bedrock Version</td></tr>
</tbody>
</table></div>
<hr>
<h3>Quick Connection Steps</h3>
<ol>
<li><strong>Launch Minecraft:</strong> Ensure you are running Minecraft <strong>26.2</strong> (Java Edition) or the latest Bedrock release.</li>
<li><strong>Multiplayer Menu:</strong> Click <strong>Add Server</strong>.</li>
<li><strong>Enter Credentials:</strong>
<ul>
<li><strong>Server Name:</strong> Apexsions</li>
<li><strong>Server Address:</strong> <code>apexsions.my.id:32348</code></li>
<li><em>(For Bedrock, explicitly set Port to <code>32348</code>)</em></li>
</ul></li>
<li><strong>Join Realm:</strong> Click <strong>Join Server</strong>. You will be welcomed at the central civilization plaza.</li>
<li><strong>Claim Equipment:</strong> Use <code>/kit starter</code> to immediately embark on your journey.</li>
</ol>
<hr>
<h3>Account Web Integration</h3>
<p>For transaction security, auction history, and seasonal rewards:</p>
<ul>
<li>Visit the official portal: <a href="https://web.apexsions.my.id" class="text-gold">web.apexsions.my.id</a>.</li>
<li>Use the <code>/link</code> command in-game when prompted to synchronize your identity with the web platform.</li>
</ul>`,

        '3': `<h1>Official Server Commands Cheat Sheet</h1>
<p>All official commands operate safely under the Apexsions plugin suite. Use this quick reference guide for effortless navigation:</p>
<hr>
<h3>1. Navigation &amp; Basic Exploration</h3>
<ul>
<li><code>/spawn</code> — Teleport back to the primary civilization hub.</li>
<li><code>/rtp</code> — Randomly teleport into safe wilderness to found a settlement.</li>
<li><code>/sethome &lt;name&gt;</code> — Set coordinates for your personal outpost.</li>
<li><code>/home &lt;name&gt;</code> — Teleport back to your marked home waypoint.</li>
<li><code>/tpa &lt;player&gt;</code> — Send a friendly teleport request to another citizen.</li>
<li><code>/tpaccept</code> — Accept incoming teleport requests.</li>
</ul>
<hr>
<h3>2. Kingdoms &amp; Territorial Sovereignty (<code>ApexsionsCore</code>)</h3>
<ul>
<li><code>/kingdom</code> or <code>/k</code> — Open the Three Sovereign Kingdoms interface.</li>
<li><code>/k info [name]</code> — Inspect capital status, active buffs, and kingdom leadership.</li>
<li><code>/k claim</code> — Claim a 16x16 block chunk in the name of your kingdom.</li>
<li><code>/k map</code> — Display a real-time territorial boundary radar.</li>
<li><code>/k deposit &lt;amount&gt;</code> — Deposit Rupiah into your kingdom nexus treasury.</li>
</ul>
<hr>
<h3>3. Economy &amp; Trade (<code>ApexsionsEconomy</code> &amp; <code>ApexsionsShop</code>)</h3>
<ul>
<li><code>/money</code> or <code>/balance</code> or <code>/bal</code> — Inspect dual balances (Rupiah <code>Rp</code> &amp; Diamond <code>💎</code>).</li>
<li><code>/pay &lt;player&gt; &lt;amount&gt;</code> — Transfer Rupiah securely to another player.</li>
<li><code>/trade &lt;player&gt;</code> — Open a two-way barter window secured by atomic escrow.</li>
<li><code>/ah</code> — Browse the 24-hour Auction House.</li>
<li><code>/ah sell &lt;price&gt;</code> — List held item on the auction marketplace.</li>
<li><code>/shop</code> — Open the Dynamic Market catalog.</li>
<li><code>/sell</code> — Sell raw ores, crops, or mob drops instantaneously.</li>
</ul>
<hr>
<h3>4. Armaments, Kits &amp; Sorcery</h3>
<ul>
<li><code>/kits</code> or <code>/kit</code> — Open periodic gear kits interface.</li>
<li><code>/kit preview &lt;name&gt;</code> — Preview kit contents and armor set stat bonuses.</li>
<li><code>/enchanter</code> or <code>/ce</code> — Open the 182 Custom Enchantments forge altar.</li>
</ul>
<hr>
<h3>5. Battlepass &amp; Communications</h3>
<ul>
<li><code>/abp</code> — Open seasonal Battlepass progression dashboard.</li>
<li><code>/abp quests</code> — View active daily and weekly quest objectives.</li>
<li><code>/ch g</code> — Switch to Global Chat channel.</li>
<li><code>/ch k</code> — Switch to Kingdom Chat channel (internal secrecy).</li>
<li><code>/mail send &lt;player&gt; &lt;message&gt;</code> — Send offline messages to other players.</li>
<li><code>/report &lt;player&gt; &lt;reason&gt;</code> — Report suspicious behavior to the staff desk.</li>
</ul>`,

        '4': `<h1>Level 1–100 Progression System &amp; Civilization Titles</h1>
<p>The progression architecture in <code>ApexsionsCore</code> rewards citizen dedication in building civilization. Progression spans <strong>Level 1 through Level 100</strong>, paired with dynamic honorific titles bound to your sworn allegiance.</p>
<hr>
<h3>Experience (XP) Sources</h3>
<ol>
<li><strong>Mining:</strong> Extracting coal, pure gold, diamonds, and ancient debris yields abundant civilization XP.</li>
<li><strong>Combat:</strong> Slaying monsters, repelling pillager raids, and slaying dungeon bosses.</li>
<li><strong>Agrarian Cultivation:</strong> Harvesting wheat, carrots, sugarcane, and nether wart on kingdom scales.</li>
<li><strong>Construction &amp; Sovereignty:</strong> Donating resources to kingdom nexuses and defending borders.</li>
</ol>
<hr>
<h3>Honorific Titles by Kingdom (Every 10 Levels)</h3>
<p>Upon reaching milestone tiers, your chat prefix automatically elevates:</p>
<div class="table-responsive mb-4"><table class="table fandom-stat-table">
<thead><tr><th>Tier</th><th>Zenithar (Solar)</th><th>Solterra (Crimson)</th><th>Sylvamoor (Azure)</th></tr></thead>
<tbody>
<tr><td><strong>Lv. 1–10</strong></td><td>Acolyte of Zenith</td><td>Dune Wanderer</td><td>Sylvan Citizen</td></tr>
<tr><td><strong>Lv. 11–20</strong></td><td>Celestial Scout</td><td>Sun Scout</td><td>Grove Keeper</td></tr>
<tr><td><strong>Lv. 21–30</strong></td><td>Sky Warden</td><td>Terra Blade</td><td>Forest Warden</td></tr>
<tr><td><strong>Lv. 31–40</strong></td><td>Astral Knight</td><td>Solar Knight</td><td>Wild Knight</td></tr>
<tr><td><strong>Lv. 41–50</strong></td><td>Apex Templar</td><td>Flame Vanguard</td><td>Nature Commander</td></tr>
<tr><td><strong>Lv. 51–60</strong></td><td>Star Commander</td><td>Dune Warlord</td><td>Druidic Lord</td></tr>
<tr><td><strong>Lv. 61–70</strong></td><td>Solaris Archon</td><td>Solaris Champion</td><td>Verdant Archon</td></tr>
<tr><td><strong>Lv. 71–80</strong></td><td>⚡ High Celestial ⚡</td><td>🔥 Sun Sovereign 🔥</td><td>🌿 Elder Guardian 🌿</td></tr>
<tr><td><strong>Lv. 81–90</strong></td><td>👑 Zenith Paragon 👑</td><td>⚔ Solterra Overlord ⚔</td><td>⚜ Sylvan Sovereign ⚜</td></tr>
<tr><td><strong>Lv. 91–100</strong></td><td>✦ EMPEROR OF ZENITHAR ✦</td><td>✦ LORD OF SOLTERRA ✦</td><td>✦ AVATAR OF SYLVAMOOR ✦</td></tr>
</tbody>
</table></div>
<hr>
<h3>Milestone Chest Rewards (/rewards)</h3>
<p>At every 10th level, claim an exclusive relic chest via <code>/rewards</code> containing:</p>
<ul>
<li>Server Rupiah and Pure Diamonds.</li>
<li>Legendary &amp; Fabled Custom Enchantment tomes.</li>
<li>Rare Crate Keys &amp; Auction tax reduction vouchers.</li>
</ul>`,

        '5': `<h1>Historical Lore: Fall of the Sions Empire &amp; The Great Exodus</h1>
<blockquote><em>"Long ago, one banner sheltered the whole firmament. But when pride breached the gates of the Dark Dimension to breed an undefeatable legion, our ancestral lands collapsed into the abyss of oblivion."</em><br>— <strong>Sacred Realm Archives, Chronicles of Sions, Book I: Canto of Ruin</strong></blockquote>
<hr>
<h3>The Golden Era of the Sions Empire</h3>
<p>Centuries ago, the entirety of Apexsions was united under a single boundless imperium: the <strong>Sions Empire</strong>. No perimeter walls divided the provinces; civilization flourished in architectural splendor, economic abundance, and pristine legal harmony.</p>
<p>Throughout its history, the empire upheld one sacred decree: <strong>never delve into dark sorcery or forbidden dimensions</strong>. The Sions people relied purely on engineering marvels, elemental natural magic, and chivalric discipline.</p>
<hr>
<h3>Forbidden Ambition &amp; The Dark Dimension Cataclysm</h3>
<p>Peace was shattered by the hubris of the last emperor. Obsessed with perpetual expansion, the sovereign secretly instructed royal scholars to pierce reality's fabric and unseal the <strong>Dark Dimension</strong> to infuse dark vitality into imperial soldiers.</p>
<p>Having never adapted to dark energy, the rift erupted into a cosmic dark vortex (<em>The Dark Rift Cataclysm</em>). The heavens ruptured, the ancestral capital shattered into dust, and the Sions throne dissolved in a single catastrophic night.</p>
<hr>
<h3>The Great Exodus in Three Directions</h3>
<p>From the apocalyptic ash, survivors rallied and fled toward three compass directions, giving birth to the <strong>Three Sovereign Kingdoms</strong>:</p>
<h4>1. Zenithar (Eastward / Zenith) — Royal Dynasty &amp; Elite Cavalry</h4>
<p>Those who held the inner palace—the <strong>Royal Bloodline</strong> and imperial honor guard—fled east toward high mountain crags and the celestial horizon (<em>Zenith</em>). They built the <em>Solarium Spire Citadel</em>, preserving court etiquette and royal cavalry discipline.</p>
<h4>2. Sylvamoor (Westward) — Laborers, Agrarians &amp; Rangers</h4>
<p>The workforce—builders, stone artisans, agrarians, and ranger militias—escaped west. Renouncing imperial hubris, they settled in the primordial ancient canopy and crystal seas, founding <em>Eldergrove Sanctuary</em> to live in sacred harmony with the World Tree.</p>
<h4>3. Solterra (Southward) — Battle Mages &amp; Front-Line Veterans</h4>
<p>The supreme battle arcanists and hardened frontline veterans migrated south into volcanic calderas and arid canyons. There, they erected <em>Ignis Bastion Fortress</em>, fusing flame sorcery with brutal martial prowess to conquer a deadly frontier.</p>
<hr>
<h3>The Central Epicenter Mystery (Terra Interdicta)</h3>
<p>In the exact center of the uncharted wilderness lies the colossal sunken ruins of the old imperial palace. Veiled in dimensional anomalies and anomaly fogs, it holds legendary treasures guarded by <strong>high-tier mutated legions</strong> exposed to eternal dark energy.</p>`,

        '6': `<h1>Encyclopedia of 3 Sovereign Kingdoms &amp; Territorial Conditions</h1>
<div class="table-responsive mb-4"><table class="table fandom-stat-table">
<thead><tr><th>Kingdom</th><th>Direction</th><th>Capital</th><th>Tax</th><th>Primary Attribute Buffs</th></tr></thead>
<tbody>
<tr><td><strong class="text-warning">Zenithar</strong></td><td>East</td><td>Solarium Spire Citadel</td><td>25%</td><td>+5% Speed, +7% Luck, +6% All Damage &amp; Defense, -5% Enemy Crit</td></tr>
<tr><td><strong class="text-danger">Solterra</strong></td><td>South</td><td>Ignis Bastion Fortress</td><td>20%</td><td>+15% All Damage, +10% Crit Damage, +10% Mining Speed, 65% Ore Sell Ratio</td></tr>
<tr><td><strong class="text-success">Sylvamoor</strong></td><td>West</td><td>Eldergrove Sanctuary</td><td>15%</td><td>+2 Max HP (11 Hearts), +12% Luck, +7% Mob Drops, High Physical Defense</td></tr>
</tbody>
</table></div>
<hr>
<h3>1. Zenithar (Celestial Horizon &amp; Solar Realm)</h3>
<ul>
<li><strong>Founders:</strong> Imperial dynasty survivors &amp; elite palace cavalry.</li>
<li><strong>Capital:</strong> Solarium Spire Citadel <code>world (-3028, 64, -5597)</code></li>
<li><strong>Official Buffs:</strong> +5% Movement Speed, +7% Luck, +6% Total Damage &amp; Defense, -5% Enemy Crit Damage.</li>
<li><strong>Debuffs:</strong> +7% Poison Vulnerability, food restores 1 fewer hunger point due to aristocratic lifestyle.</li>
</ul>
<hr>
<h3>2. Solterra (Crimson Earth &amp; Volcanic Empire)</h3>
<ul>
<li><strong>Founders:</strong> Master combat arcanists &amp; front-line military veterans.</li>
<li><strong>Capital:</strong> Ignis Bastion Fortress <code>world (-5843, 65, 889)</code></li>
<li><strong>Official Buffs:</strong> +15% Damage, +10% Crit Damage, +10% Mining Speed, High Ore Selling Ratio (65%).</li>
<li><strong>Debuffs:</strong> -2 Max HP (9 Hearts total), +8% Incoming Damage, +7% Faster Hunger depletion.</li>
</ul>
<hr>
<h3>3. Sylvamoor (Ancient Canopy &amp; Crystal Ocean Realm)</h3>
<ul>
<li><strong>Founders:</strong> Working class builders, agrarians, and wildwood rangers.</li>
<li><strong>Capital:</strong> Eldergrove Sanctuary <code>world (-9666, 64, -4812)</code></li>
<li><strong>Official Buffs:</strong> +2 Max HP (11 Hearts total), +12% Luck, +7% Mob Drops, Eternal Soil Hydration.</li>
<li><strong>Debuffs:</strong> Altitude Sickness at Y > 110, +15% Fire Vulnerability, -10% PvP Damage &amp; Mining Speed.</li>
</ul>`,

        '7': `<h1>Kingdom Territory Claims &amp; Nexus Protection</h1>
<p>Territorial sovereignty in Apexsions is safeguarded by <code>ApexsionsCore</code>. Citizens can officially stake claims to preserve their structures, farms, and storage against griefing and unauthorized intrusion.</p>
<hr>
<h3>How to Claim Territory</h3>
<ol>
<li>Navigate to the unowned chunk (16x16 blocks from bedrock to sky) you wish to claim.</li>
<li>Verify territorial boundaries with:
<div class="apx-code-block-wrap"><pre><code>/k map</code></pre></div></li>
<li>Claim the chunk for your kingdom:
<div class="apx-code-block-wrap"><pre><code>/k claim</code></pre></div></li>
<li>Each chunk requires a small initial fee from your personal Rupiah balance, which contributes to the kingdom treasury.</li>
</ol>
<hr>
<h3>The Kingdom Nexus</h3>
<p>Each kingdom capital houses an invincible <strong>Nexus Core</strong>. Citizens can deposit funds using <code>/k deposit &lt;amount&gt;</code> to upgrade kingdom-wide infrastructure, territorial shields, and wartime defensive arrays.</p>`,

        '8': `<h1>Kingdom Wars, Sieges &amp; Combat Tag System</h1>
<p>When diplomatic negotiations collapse, warfare ignites across borders under strictly enforced protocols.</p>
<hr>
<h3>Wartime Protocols &amp; Sieges</h3>
<ul>
<li><strong>Scheduled Sieges:</strong> Battles occur during declared wartime windows to ensure fair competition.</li>
<li><strong>Nexus Vulnerability:</strong> Attackers must breach outer defensive fortifications before channeling siege energy into enemy nexus obelisks.</li>
<li><strong>Territory Conquest:</strong> Victorious kingdoms claim contested frontier chunks and collect reparations.</li>
</ul>
<hr>
<h3>Combat Tag System</h3>
<p>During active combat against other citizens:</p>
<ul>
<li>Your status locks into <strong>Combat Tag</strong> for 15 seconds.</li>
<li>Teleportation commands (<code>/spawn</code>, <code>/home</code>, <code>/rtp</code>) are disabled.</li>
<li><strong>Combat Logging Penalty:</strong> Disconnecting while tagged results in instant character death, dropping your complete inventory at logout coordinates.</li>
</ul>`,

        '9': `<h1>Currency System: Rupiah (Rp) &amp; Diamond (💎)</h1>
<p>Apexsions utilizes a robust dual-currency economy backed by atomic ACID transactions in <code>ApexsionsEconomy</code>.</p>
<hr>
<h3>The Two Currencies</h3>
<ol>
<li><strong>Rupiah (<code>Rp</code>) — Fiat Sovereign Balance:</strong> Primary trading currency used for server shops (<code>/shop</code>), quick selling (<code>/sell</code>), kingdom taxes, and auction bidding.</li>
<li><strong>Diamonds (<code>💎</code>) — Material Hard Currency:</strong> Pure physical gemstones used for high-tier crafting, elite bartering, and ancient relic exchanges.</li>
</ol>
<hr>
<h3>Checking Balances &amp; Transfers</h3>
<ul>
<li><code>/balance</code> or <code>/money</code> — View both currency balances simultaneously.</li>
<li><code>/pay &lt;player&gt; &lt;amount&gt;</code> — Transfer Rupiah with cryptographic safety.</li>
</ul>`,

        '10': `<h1>Dynamic Market (/shop &amp; /sell)</h1>
<p>The economy in <code>ApexsionsShop</code> employs dynamic supply-demand pricing to ensure market vitality and prevent inflation.</p>
<hr>
<h3>How Dynamic Pricing Works</h3>
<ul>
<li><strong>Heavy Influx (Over-supply):</strong> If citizens flood the market with one item (e.g., cobblestone or iron), its buy/sell value gradually dips.</li>
<li><strong>Scarcity (High Demand):</strong> As supply drops, purchasing prices and selling rewards automatically appreciate.</li>
<li><strong>Regional Selling Buff:</strong> Solterra citizens enjoy a permanent 65% minimum ore sale value due to their industrial heritage.</li>
</ul>`,

        '11': `<h1>Auction House (/ah) &amp; Barter Escrow (/trade)</h1>
<p>Peer-to-peer commerce is protected against theft and deceit through automated escrow systems.</p>
<hr>
<h3>Auction House (/ah)</h3>
<ul>
<li><code>/ah</code> — Browse all citizen listings sorted by price, category, and rarity.</li>
<li><code>/ah sell &lt;price&gt;</code> — List your held item for global purchase. Listings remain active for 24 hours.</li>
</ul>
<hr>
<h3>Barter Escrow (/trade)</h3>
<ul>
<li>Initiate secure trade: <code>/trade &lt;player&gt;</code>.</li>
<li>Both players lock items into the trading matrix.</li>
<li>Both parties must confirm the trade twice. If inventory space is insufficient, items safely return to original owners.</li>
</ul>`,

        '12': `<h1>Prosperity Strategy: Merchant Success Guide</h1>
<p>Mastering commerce is the most reliable pathway to royal nobility in Apexsions.</p>
<hr>
<h3>Proven Strategies for New Merchants</h3>
<ol>
<li><strong>Specialize by Kingdom Buff:</strong> Sylvamoor citizens should dominate food and crop markets; Solterra citizens should focus on raw mineral smelting.</li>
<li><strong>Monitor Market Price Swings:</strong> Sell mined ores when global inventory dips to capture peak multipliers.</li>
<li><strong>Enchanted Armaments Trade:</strong> Forge custom-enchanted weapons and armor sets to sell at high premiums on <code>/ah</code>.</li>
</ol>`,

        '13': `<h1>182 Custom Enchantments &amp; 7 Tier Ranks</h1>
<p>Apexsions features an expansive library of 182 custom enchantments categorized across 7 balanced tiers.</p>
<hr>
<h3>The 7 Enchantment Ranks</h3>
<div class="table-responsive mb-4"><table class="table fandom-stat-table">
<thead><tr><th>Tier</th><th>Color Code</th><th>Nature &amp; Rarity</th></tr></thead>
<tbody>
<tr><td><strong>Tier I: Simple</strong></td><td>Gray</td><td>Basic utilities, minor stat upgrades, quality of life.</td></tr>
<tr><td><strong>Tier II: Unique</strong></td><td>Green</td><td>Specialized environmental adaptations and tool buffs.</td></tr>
<tr><td><strong>Tier III: Elite</strong></td><td>Aqua</td><td>Potent combat enhancements and mobility skills.</td></tr>
<tr><td><strong>Tier IV: Ultimate</strong></td><td>Gold</td><td>Major combat abilities and defensive auras.</td></tr>
<tr><td><strong>Tier V: Legendary</strong></td><td>Orange</td><td>Devastating combat spells and passive regeneration.</td></tr>
<tr><td><strong>Tier VI: Fabled</strong></td><td>Red</td><td>Cataclysmic spell effects and rare set triggers.</td></tr>
<tr><td><strong>Tier VII: Mythic</strong></td><td>Purple</td><td>God-tier relics with game-altering cosmic abilities.</td></tr>
</tbody>
</table></div>
<p>Open the enchanting altar anytime via <code>/ce</code> or <code>/enchanter</code>.</p>`,

        '14': `<h1>Magic Scrolls, Magic Dust &amp; Scrambler</h1>
<p>Safely manage and optimize your custom enchantments using magical alchemy items:</p>
<ul>
<li><strong>Magic Dust:</strong> Increases success rate percentage on enchantment tomes.</li>
<li><strong>White Scrolls:</strong> Protects your weapon or armor piece from breaking if an enchantment fails.</li>
<li><strong>Black Scrolls:</strong> Extracts a random custom enchantment from an item into an applicable book.</li>
<li><strong>Scrambler:</strong> Randomizes the success and destroy chances of an enchantment book.</li>
</ul>`,

        '15': `<h1>Native Kits System &amp; Stat-Based Armor Set Bonuses</h1>
<p>Donator ranks and leveling milestones grant access to comprehensive gear kits (<code>/kits</code>).</p>
<hr>
<h3>Armor Set Bonuses</h3>
<p>Wearing a full cohesive armor set (Helmet, Chestplate, Leggings, Boots) activates passive combat synergies:</p>
<ul>
<li><strong>Knight Set:</strong> +10% Resistance against physical damage.</li>
<li><strong>Arcane Set:</strong> +15% Spell critical chance and reduced cooldowns.</li>
<li><strong>Ranger Set:</strong> +20% Arrow velocity and perpetual Swiftness II.</li>
</ul>`,

        '16': `<h1>Meta Sorcery Builds &amp; Weapon Synergy Guide</h1>
<p>Combine custom enchantments strategically to construct dominant PvP and PvE configurations:</p>
<ul>
<li><strong>Vampiric Berserker:</strong> Combine <em>Lifesteal V</em>, <em>Rage VI</em>, and <em>Bleed IV</em> for overwhelming sustained melee DPS.</li>
<li><strong>Impenetrable Bastion:</strong> Stack <em>Overload V</em> (+Max Health), <em>Armored IV</em>, and <em>Enlighted IV</em> for near-invulnerable defense.</li>
<li><strong>Phantom Archer:</strong> Utilize <em>Sniper V</em>, <em>Piercing IV</em>, and <em>Venom IV</em> to neutralize opponents from extreme range.</li>
</ul>`,

        '17': `<h1>Seasonal Battlepass: Quests &amp; EXP Shop</h1>
<p>Every season brings 100 tiers of progression in <code>ApexsionsBattlepass</code> with free and premium pathways.</p>
<hr>
<h3>Quests &amp; EXP Points</h3>
<ul>
<li><code>/abp quests</code> — Complete daily farming, mining, and monster hunting objectives.</li>
<li><strong>EXP Shop:</strong> Spend Battlepass points in rotating weekly storefronts for cosmetic particle trails, titles, and exclusive crate keys.</li>
</ul>`,

        '18': `<h1>Chat Channels, Kingdom Tags &amp; Staff Reports Desk</h1>
<p>The communication network in <code>ApexsionsChat</code> keeps public interaction orderly and vibrant.</p>
<hr>
<h3>Chat Channels</h3>
<ul>
<li><code>/ch g</code> — Global Chat (Visible realm-wide to all citizens).</li>
<li><code>/ch k</code> — Kingdom Chat (Encrypted to citizens of your realm).</li>
<li><code>/mail send &lt;player&gt; &lt;msg&gt;</code> — Deliver messages to offline citizens.</li>
</ul>
<hr>
<h3>Staff Reporting Desk (/report)</h3>
<p>Witness rule infractions? Type <code>/report &lt;player&gt; &lt;reason&gt;</code> to snapshot the last 50 lines of chat and player coordinates directly to active moderators.</p>`,

        '19': `<h1>Community Ethics, Roleplay &amp; Civilization Code of Honor</h1>
<p>Apexsions thrives on healthy competition, immersion, and mutual respect.</p>
<ul>
<li><strong>Distinguish Roleplay from Personal Hostility:</strong> Kingdom rivalries should remain in-character. Harassment or toxicity is strictly penalized.</li>
<li><strong>Fair Competition:</strong> Exploiting unintended game bugs or client modifications is forbidden.</li>
<li><strong>Support New Citizens:</strong> Guiding newcomers strengthens your kingdom's workforce and global standing.</li>
</ul>`,

        '20': `<h1>5-Tier Structure &amp; 11 Official Castes of Apexsions</h1>
<p>The social structure is divided into 5 authoritative tiers governed by <code>ranks.yml</code>:</p>
<div class="table-responsive mb-4"><table class="table fandom-stat-table">
<thead><tr><th>Tier</th><th>Rank</th><th>Weight</th><th>Role &amp; Responsibilities</th></tr></thead>
<tbody>
<tr><td>Tier V</td><td><code>ancestor</code></td><td>100</td><td>The Ancestor / Founder (Apex Sovereign)</td></tr>
<tr><td>Tier IV</td><td><code>architect</code></td><td>95</td><td>Realm Architect / Technical Authority</td></tr>
<tr><td>Tier IV</td><td><code>overseer</code></td><td>95</td><td>Integrity, Balance &amp; Community Overseer</td></tr>
<tr><td>Tier III</td><td><code>warden</code></td><td>90</td><td>Head Staff / Server Administrator</td></tr>
<tr><td>Tier III</td><td><code>herald</code></td><td>80</td><td>Staff Moderator &amp; Player Guide</td></tr>
<tr><td>Tier II</td><td><code>sions</code></td><td>70</td><td>Apex Donator / Pinnacle Civilization Rank</td></tr>
<tr><td>Tier II</td><td><code>emperor</code></td><td>60</td><td>Noble Donator Tier 4</td></tr>
<tr><td>Tier II</td><td><code>sovereign</code></td><td>50</td><td>Noble Donator Tier 3</td></tr>
<tr><td>Tier II</td><td><code>archon</code></td><td>40</td><td>Noble Donator Tier 2</td></tr>
<tr><td>Tier II</td><td><code>ascendant</code></td><td>30</td><td>Pioneer Donator Tier 1</td></tr>
<tr><td>Tier I</td><td><code>wanderer</code></td><td>10</td><td>New Citizen / Foundation of the Realm</td></tr>
</tbody>
</table></div>`,

        '21': `<h1>Spiritual Ascension: 10 Sequence Pathways</h1>
<p>Beyond material rank, citizens may ascend through 10 Sequence Pathways by achieving in-game milestones:</p>
<ol>
<li><strong>Sequence 9:</strong> The Seeker — Exploration of all 3 kingdom capitals.</li>
<li><strong>Sequence 8:</strong> The Artisan — Crafting 100 masterwork armaments.</li>
<li><strong>Sequence 7:</strong> The Tactician — Leading 10 successful kingdom skirmishes.</li>
<li><strong>Sequence 6:</strong> The Alchemist — Synthesizing 50 custom magic scrolls.</li>
<li><strong>Sequence 5:</strong> The Arbiter — Resolving territorial disputes honorably.</li>
<li><strong>Sequence 4:</strong> The Warlord — Inflicting over 100,000 damage in official wars.</li>
<li><strong>Sequence 3:</strong> The Grand Architect — Contributing 1,000,000 Rp to nexus upgrades.</li>
<li><strong>Sequence 2:</strong> The Archon — Reaching Level 100 with flawless honor.</li>
<li><strong>Sequence 1:</strong> The Sovereign Vanguard — Holding highest seasonal Battlepass rank.</li>
<li><strong>Sequence 0:</strong> The Ascended Divinity — The legendary champion of the civilization.</li>
</ol>`
    }
};
