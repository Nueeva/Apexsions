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
                        <img src="{{ theme_asset('img/logo.jpg') }}" alt="Apexsions Crest" class="apx-brand-logo-img">
                    </div>
                    <div class="d-flex flex-column">
                        <span class="apx-brand-title">APEXSIONS</span>
                        <span class="apx-brand-tagline">THE PEAK CIVILIZATIONS</span>
                    </div>
                </a>
                <p class="apx-footer-desc text-muted small pe-lg-4 mb-4" style="line-height: 1.8;">
                    Apexsions adalah peradaban berdaulat yang dibentuk oleh komunitas. Tatanan kerajaan otonom, sistem pasar atomik, dan progres kasta sosial yang kompetitif di atas Minecraft 1.21.4.
                </p>
                <div class="apx-footer-verbs small text-uppercase" style="letter-spacing: 0.16em; color: #64748b; font-weight: 600; font-size: 0.72rem;">
                    MINECRAFT 1.21.4 &bull; REALM BERDAULAT
                </div>
            </div>

            <!-- Column 1: Peradaban -->
            <div class="col-lg-2 col-md-3 col-6 apx-footer-nav-col">
                <div class="apx-footer-heading">PERADABAN</div>
                <ul class="apx-footer-list">
                    <li><a href="{{ route('home') }}"><i class="bi bi-house me-2"></i> Beranda</a></li>
                    @if(plugins()->isEnabled('shop'))
                        <li><a href="{{ route('shop.home') }}"><i class="bi bi-cart3 me-2"></i> Webstore</a></li>
                    @endif
                    <li><a href="{{ route('home') }}#features"><i class="bi bi-shield-shaded me-2"></i> Tiga Kerajaan</a></li>
                    <li><a href="{{ route('home') }}#ranks"><i class="bi bi-crown me-2"></i> Hierarki Kasta</a></li>
                    @if(plugins()->isEnabled('wiki'))
                        <li><a href="{{ route('wiki.index') }}"><i class="bi bi-journal-text me-2"></i> Arsip Wiki</a></li>
                    @endif
                    <li><a href="#" data-bs-toggle="modal" data-bs-target="#voteModal"><i class="bi bi-trophy me-2"></i> Dukung Vote</a></li>
                </ul>
            </div>

            <!-- Column 2: Panduan & Kebijakan -->
            <div class="col-lg-3 col-md-3 col-6 apx-footer-nav-col">
                <div class="apx-footer-heading">PANDUAN &amp; ATURAN</div>
                <ul class="apx-footer-list">
                    <li><a href="{{ route('home') }}#getting-started"><i class="bi bi-compass me-2"></i> Cara Bergabung</a></li>
                    @if(plugins()->isEnabled('wiki'))
                        <li><a href="{{ route('wiki.index') }}"><i class="bi bi-book me-2"></i> Ensiklopedia Pemain</a></li>
                    @endif
                    <li><a href="{{ route('home') }}#ranks"><i class="bi bi-shield-check me-2"></i> Aturan Ketertiban</a></li>
                    <li><a href="#kebijakan"><i class="bi bi-file-earmark-lock me-2"></i> Ketentuan Transaksi</a></li>
                    <li><a href="https://discord.gg/apexsions" target="_blank" rel="noopener noreferrer"><i class="bi bi-headset me-2"></i> Pusat Bantuan</a></li>
                </ul>
            </div>

            <!-- Column 3: Komunitas & Diplomasi -->
            <div class="col-lg-3 col-md-6 col-12 apx-footer-nav-col">
                <div class="apx-footer-heading">DIPLOMASI KOMUNITAS</div>
                <ul class="apx-footer-list mb-4">
                    <li><a href="https://discord.gg/apexsions" target="_blank" rel="noopener noreferrer"><i class="bi bi-discord me-2 text-primary"></i> Discord Komunitas</a></li>
                    <li><a href="https://youtube.com/@apexsions" target="_blank" rel="noopener noreferrer"><i class="bi bi-youtube me-2 text-danger"></i> YouTube Peradaban</a></li>
                    <li><a href="https://instagram.com/apexsions" target="_blank" rel="noopener noreferrer"><i class="bi bi-instagram me-2 text-warning"></i> Instagram Resmi</a></li>
                    <li><a href="https://x.com/apexsions" target="_blank" rel="noopener noreferrer"><i class="bi bi-twitter-x me-2"></i> X (Twitter)</a></li>
                </ul>

                <!-- Telemetry Line in Footer (Architectural Box, Click to copy) -->
                <div class="apx-footer-quick-copy apx-copyable d-inline-flex align-items-center gap-2 px-3 py-2" data-apx-copy="apexsions.my.id" role="button" tabindex="0" title="Klik untuk menyalin IP">
                    <span class="apx-pulse-dot" style="width: 6px; height: 6px;"></span>
                    <span class="small font-monospace text-white">apexsions.my.id</span>
                    <i class="bi bi-clipboard text-dim small ms-1"></i>
                </div>
            </div>
        </div>

        <!-- Bottom Copyright & Center Emblem Bar -->
        <div class="apx-footer-bottom pt-4 border-top border-secondary border-opacity-15">
            <div class="row align-items-center gy-3">
                <!-- Left Copyright -->
                <div class="col-lg-4 text-center text-lg-start">
                    <div class="text-white small mb-1">&copy; {{ date('Y') }} Apexsions. Seluruh hak cipta dilindungi.</div>
                    <div class="text-muted" style="font-size: 0.78rem;">Dibangun bersama komunitas, untuk peradaban yang berdaulat.</div>
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
                        <a href="#terms" class="text-muted text-decoration-none me-2">Syarat &amp; Ketentuan</a>
                        <span class="text-muted">&bull;</span>
                        <a href="#privacy" class="text-muted text-decoration-none mx-2">Kebijakan Privasi</a>
                        <span class="text-muted">&bull;</span>
                        <a href="https://discord.gg/apexsions" target="_blank" rel="noopener noreferrer" class="text-muted text-decoration-none ms-2">Kontak</a>
                    </div>
                    <div class="text-muted fst-italic" style="font-size: 0.78rem; font-family: Georgia, serif;">
                        Same Blocks, Bigger Stories.
                    </div>
                </div>
            </div>
        </div>
    </div>
</footer>
