<footer class="apx-footer" aria-label="Footer Website">
    <div class="container">
        <!-- Top Slogan Divider: Sovereign Architectural Border -->
        <div class="apx-footer-top-slogan text-center mb-5">
            <div class="d-flex align-items-center justify-content-center gap-3">
                <span class="apx-slogan-line"></span>
                <span class="apx-slogan-text font-monospace" style="font-size: 0.72rem; letter-spacing: 0.2em; color: #64748b;">
                    APEXSIONS &bull; THE PEAK CIVILIZATIONS
                </span>
                <span class="apx-slogan-line"></span>
            </div>
        </div>

        <div class="row g-4 gy-5 mb-5 align-items-start">
            <!-- Brand & Sovereign Manifesto Column -->
            <div class="col-lg-4 col-md-6 apx-footer-brand-col">
                <a class="apx-navbar-brand mb-3 d-inline-flex" href="{{ route('home') }}">
                    <div class="apx-brand-logo-box me-2">
                        <img src="{{ theme_asset('img/logo.png') }}&v={{ @filemtime(public_path('assets/themes/apexsions/img/logo.png')) ?: '3' }}" alt="Apexsions Crest" class="apx-brand-logo-img" width="42" height="42" loading="lazy">
                    </div>
                    <div class="d-flex flex-column">
                        <span class="apx-brand-title">APEXSIONS</span>
                        <span class="apx-brand-tagline">THE PEAK CIVILIZATIONS</span>
                    </div>
                </a>
                <p class="apx-footer-desc text-muted small pe-lg-4 mb-4" style="line-height: 1.8;" data-i18n="footer_desc">
                    Apexsions adalah peradaban berdaulat yang dibentuk oleh komunitas. Tatanan kerajaan otonom, sistem pasar atomik, dan progres kasta sosial yang kompetitif di atas Minecraft 26.2.
                </p>
                <div class="apx-footer-verbs small text-uppercase" style="letter-spacing: 0.16em; color: #64748b; font-weight: 600; font-size: 0.72rem;" data-i18n="footer_subline">
                    MINECRAFT 26.2 &bull; REALM BERDAULAT
                </div>
            </div>

            <!-- Column 1: Peradaban -->
            <div class="col-lg-2 col-md-3 col-6 apx-footer-nav-col">
                <div class="apx-footer-heading" data-i18n="footer_col_civ">PERADABAN</div>
                <ul class="apx-footer-list">
                    <li><a href="{{ route('home') }}"><i class="bi bi-house me-2"></i> <span data-i18n="nav_home">Beranda</span></a></li>
                    @if(plugins()->isEnabled('shop'))
                        <li><a href="{{ route('shop.home') }}"><i class="bi bi-cart3 me-2"></i> <span data-i18n="nav_shop">Webstore</span></a></li>
                    @endif
                    <li><a href="{{ route('home') }}#features"><i class="bi bi-shield-shaded me-2"></i> <span data-i18n="footer_three_kingdoms">Tiga Kerajaan</span></a></li>
                    <li><a href="{{ route('home') }}#ranks"><i class="bi bi-crown me-2"></i> <span data-i18n="footer_caste_hierarchy">Hierarki Kasta</span></a></li>
                    @if(plugins()->isEnabled('wiki'))
                        <li><a href="{{ route('wiki.index') }}"><i class="bi bi-journal-text me-2"></i> <span data-i18n="footer_wiki_archive">Arsip Wiki</span></a></li>
                    @endif
                    <li><a href="{{ route('vote') }}"><i class="bi bi-trophy me-2"></i> <span data-i18n="footer_vote_support">Dukung Vote</span></a></li>
                </ul>
            </div>

            <!-- Column 2: Panduan & Kebijakan -->
            <div class="col-lg-3 col-md-3 col-6 apx-footer-nav-col">
                <div class="apx-footer-heading" data-i18n="footer_col_guides">PANDUAN &amp; ATURAN</div>
                <ul class="apx-footer-list">
                    <li><a href="{{ route('home') }}#getting-started"><i class="bi bi-compass me-2"></i> <span data-i18n="footer_how_to_join">Cara Bergabung</span></a></li>
                    @if(plugins()->isEnabled('wiki'))
                        <li><a href="{{ route('wiki.index') }}"><i class="bi bi-book me-2"></i> <span data-i18n="footer_encyclopedia">Ensiklopedia Pemain</span></a></li>
                    @endif
                    <li><a href="{{ route('rules') }}"><i class="bi bi-shield-check me-2"></i> <span data-i18n="footer_official_rules">Peraturan Resmi</span></a></li>
                    <li><a href="{{ route('terms') }}"><i class="bi bi-file-earmark-lock me-2"></i> <span data-i18n="footer_terms_trans">Ketentuan Transaksi</span></a></li>
                    <li><a href="https://discord.gg/apexsions" target="_blank" rel="noopener noreferrer"><i class="bi bi-headset me-2"></i> <span data-i18n="footer_help_center">Pusat Bantuan</span></a></li>
                </ul>
            </div>

            <!-- Column 3: Komunitas & Diplomasi -->
            <div class="col-lg-3 col-md-6 col-12 apx-footer-nav-col">
                <div class="apx-footer-heading" data-i18n="footer_col_community">DIPLOMASI KOMUNITAS</div>
                <ul class="apx-footer-list mb-4">
                    <li><a href="https://discord.gg/apexsions" target="_blank" rel="noopener noreferrer"><i class="bi bi-discord me-2 text-primary"></i> Discord Komunitas</a></li>
                    <li><a href="https://youtube.com/@apexsions" target="_blank" rel="noopener noreferrer"><i class="bi bi-youtube me-2 text-danger"></i> YouTube Peradaban</a></li>
                    <li><a href="https://instagram.com/apexsions" target="_blank" rel="noopener noreferrer"><i class="bi bi-instagram me-2 text-warning"></i> Instagram Resmi</a></li>
                    <li><a href="https://x.com/apexsions" target="_blank" rel="noopener noreferrer"><i class="bi bi-twitter-x me-2"></i> X (Twitter)</a></li>
                </ul>

                <!-- Telemetry Line in Footer (Architectural Box, Click to copy) -->
                <div class="d-flex flex-wrap gap-2">
                    <div class="apx-footer-quick-copy apx-copyable d-inline-flex align-items-center gap-2 px-3 py-2" data-apx-copy="apexsions.my.id:32348" role="button" tabindex="0" title="Klik atau tekan Enter untuk menyalin IP Java" aria-label="Salin Alamat IP Server Java">
                        <span class="apx-pulse-dot" style="width: 6px; height: 6px;" aria-hidden="true"></span>
                        <span class="small font-monospace apx-footer-ip-text">JAVA: apexsions.my.id:32348</span>
                        <i class="bi bi-clipboard text-dim small ms-1"></i>
                    </div>
                    <div class="apx-footer-quick-copy apx-copyable d-inline-flex align-items-center gap-2 px-3 py-2" data-apx-copy="IP: apexsions.my.id | Port: 32348" role="button" tabindex="0" title="Klik atau tekan Enter untuk menyalin IP dan Port Bedrock" aria-label="Salin IP dan Port Server Bedrock">
                        <span class="badge bg-secondary" style="font-size: 0.65rem; padding: 2px 5px;">BEDROCK</span>
                        <span class="small font-monospace apx-footer-ip-text">IP: apexsions.my.id | Port: 32348</span>
                        <i class="bi bi-clipboard text-dim small ms-1"></i>
                    </div>
                </div>
            </div>
        </div>

        <!-- Bottom Copyright & Center Emblem Bar -->
        <div class="apx-footer-bottom pt-4 border-top border-secondary border-opacity-15">
            <div class="row align-items-center gy-3">
                <!-- Left Copyright -->
                <div class="col-lg-4 text-center text-lg-start">
                    <div class="apx-footer-copyright-text small mb-1">&copy; {{ date('Y') }} <span data-i18n="footer_copyright">Apexsions. Seluruh hak cipta dilindungi.</span></div>
                    <div class="text-muted" style="font-size: 0.78rem;" data-i18n="footer_sub_copyright">Dibangun bersama komunitas, untuk peradaban yang berdaulat.</div>
                </div>

                <!-- Center Emblem -->
                <div class="col-lg-4 text-center">
                    <div class="d-flex align-items-center justify-content-center gap-2 mb-1">
                        <i class="bi bi-shield-shaded text-dim fs-6"></i>
                    </div>
                    <div class="apx-emblem-title" style="font-family: 'Cinzel', serif; font-size: 0.68rem; letter-spacing: 0.28em; color: var(--apx-text-dim);">
                        THE PEAK CIVILIZATIONS
                    </div>
                </div>

                <!-- Right Legal -->
                <div class="col-lg-4 text-center text-lg-end">
                    <div class="small mb-1">
                        <a href="{{ route('terms') }}" class="text-muted text-decoration-none me-2" data-i18n="footer_terms">Syarat &amp; Ketentuan</a>
                        <span class="text-muted">&bull;</span>
                        <a href="{{ route('privacy') }}" class="text-muted text-decoration-none mx-2" data-i18n="footer_privacy">Kebijakan Privasi</a>
                        <span class="text-muted">&bull;</span>
                        <a href="https://discord.gg/apexsions" target="_blank" rel="noopener noreferrer" class="text-muted text-decoration-none ms-2" data-i18n="footer_contact">Kontak</a>
                    </div>
                    <div class="text-muted fst-italic" style="font-size: 0.78rem; font-family: Georgia, serif;">
                        Same Blocks, Bigger Stories.
                    </div>
                </div>
            </div>
        </div>
    </div>
</footer>
