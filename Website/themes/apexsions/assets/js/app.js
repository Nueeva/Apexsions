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
        footer_contact: 'Kontak'
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
        footer_contact: 'Contact'
    }
};

document.addEventListener('DOMContentLoaded', () => {
    // 0. Bilingual Internationalization Engine
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

            try {
                localStorage.setItem('apx_locale', targetLang);
                document.cookie = `apx_locale=${targetLang};path=/;max-age=31536000;SameSite=Lax`;
            } catch (e) {}

            // Re-evaluate dynamic telemetry text in new language
            if (typeof fetchServerStatus === 'function') {
                fetchServerStatus();
            }
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

    // 2. Live Minecraft Server Bridge Integration
    const fetchServerStatus = () => {
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
    };

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

    // 4. Cinematic Inter-Page Transition
    const initPageTransitions = () => {
        const overlay = document.getElementById('apxPageTransition');
        if (!overlay) return;

        const dismissTransition = () => {
            overlay.classList.remove('is-navigating', 'is-entering');
            overlay.classList.add('is-loaded');
        };

        requestAnimationFrame(() => {
            setTimeout(dismissTransition, 80);
        });

        window.addEventListener('pageshow', () => {
            dismissTransition();
        });

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
