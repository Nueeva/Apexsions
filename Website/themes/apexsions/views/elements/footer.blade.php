<footer class="apx-footer" aria-label="Footer Website">
    <div class="container">
        <!-- Top Slogan Divider (ChatGPT Concept Match) -->
        <div class="apx-footer-top-slogan text-center mb-5">
            <div class="d-flex align-items-center justify-content-center gap-3">
                <span class="apx-slogan-line"></span>
                <span class="apx-slogan-text">
                    <i class="bi bi-crown text-warning me-1"></i> A GREATER TOMORROW BUILT TOGETHER
                </span>
                <span class="apx-slogan-line"></span>
            </div>
        </div>

        <div class="row g-4 gy-5 mb-5 align-items-start">
            <!-- Brand Column -->
            <div class="col-xl-3 col-lg-4 col-md-6 apx-footer-brand-col">
                <a class="apx-navbar-brand mb-3 d-inline-flex" href="{{ route('home') }}">
                    <div class="apx-brand-logo-box me-2">
                        <img src="{{ theme_asset('img/logo.jpg') }}" alt="Apexsions" class="apx-brand-logo-img">
                    </div>
                    <div class="d-flex flex-column">
                        <span class="apx-brand-title">APEXSIONS</span>
                        <span class="apx-brand-tagline">THE PEAK CIVILIZATIONS</span>
                    </div>
                </a>
                <p class="apx-footer-desc text-muted small pe-lg-3 mb-4" style="line-height: 1.75;">
                    Apexsions adalah dunia yang dibangun oleh pemain, untuk pemain. Rasakan pengalaman berkreasi, berkolaborasi, dan membentuk peradaban yang lebih besar bersama kami.
                </p>
                <div class="apx-footer-verbs small text-uppercase" style="letter-spacing: 0.22em; color: var(--apx-gold-light); font-weight: 700; font-size: 0.72rem;">
                    PLAY &bull; BUILD &bull; TRADE &bull; RULE
                </div>
            </div>

            <!-- Column 1: Navigasi -->
            <div class="col-xl-2 col-lg-2 col-md-3 col-6 apx-footer-nav-col">
                <div class="apx-footer-heading">NAVIGASI</div>
                <ul class="apx-footer-list">
                    <li><a href="{{ route('home') }}"><i class="bi bi-house me-2"></i> Beranda</a></li>
                    @if(plugins()->isEnabled('shop'))
                        <li><a href="{{ route('shop.home') }}"><i class="bi bi-cart3 me-2"></i> Webstore</a></li>
                    @endif
                    <li><a href="{{ route('home') }}#features"><i class="bi bi-stars me-2"></i> Fitur</a></li>
                    @if(plugins()->isEnabled('wiki'))
                        <li><a href="{{ route('wiki.index') }}"><i class="bi bi-book me-2"></i> Wiki</a></li>
                    @endif
                    <li><a href="#" data-bs-toggle="modal" data-bs-target="#voteModal"><i class="bi bi-trophy me-2"></i> Vote</a></li>
                    <li><a href="https://discord.gg/apexsions" target="_blank" rel="noopener noreferrer"><i class="bi bi-discord me-2"></i> Discord</a></li>
                </ul>
            </div>

            <!-- Column 2: Dukungan -->
            <div class="col-xl-2 col-lg-2 col-md-3 col-6 apx-footer-nav-col">
                <div class="apx-footer-heading">DUKUNGAN</div>
                <ul class="apx-footer-list">
                    <li><a href="{{ route('home') }}#getting-started"><i class="bi bi-question-circle me-2"></i> Pusat Bantuan</a></li>
                    @if(plugins()->isEnabled('wiki'))
                        <li><a href="{{ route('wiki.index') }}"><i class="bi bi-file-earmark-text me-2"></i> Panduan Pemain</a></li>
                    @endif
                    <li><a href="{{ route('home') }}#ranks"><i class="bi bi-people me-2"></i> Aturan Server</a></li>
                    <li><a href="#kebijakan"><i class="bi bi-shield-check me-2"></i> Kebijakan</a></li>
                    <li><a href="https://discord.gg/apexsions" target="_blank" rel="noopener noreferrer"><i class="bi bi-envelope me-2"></i> Hubungi Kami</a></li>
                </ul>
            </div>

            <!-- Column 3: Komunitas -->
            <div class="col-xl-2 col-lg-2 col-md-4 col-6 apx-footer-nav-col">
                <div class="apx-footer-heading">KOMUNITAS</div>
                <ul class="apx-footer-list">
                    <li><a href="https://discord.gg/apexsions" target="_blank" rel="noopener noreferrer"><i class="bi bi-discord me-2 text-primary"></i> Discord</a></li>
                    <li><a href="https://youtube.com/@apexsions" target="_blank" rel="noopener noreferrer"><i class="bi bi-youtube me-2 text-danger"></i> YouTube</a></li>
                    <li><a href="https://instagram.com/apexsions" target="_blank" rel="noopener noreferrer"><i class="bi bi-instagram me-2 text-warning"></i> Instagram</a></li>
                    <li><a href="https://x.com/apexsions" target="_blank" rel="noopener noreferrer"><i class="bi bi-twitter-x me-2"></i> X (Twitter)</a></li>
                    <li><a href="https://planetminecraft.com" target="_blank" rel="noopener noreferrer"><i class="bi bi-chat-square-dots me-2 text-info"></i> Minecraft Forum</a></li>
                </ul>
            </div>

            <!-- Column 4: Status Server Card -->
            <div class="col-xl-3 col-lg-6 col-md-8 col-12 apx-footer-status-col">
                <div class="apx-footer-heading d-flex align-items-center gap-2">
                    <span class="apx-pulse-dot" id="apxFooterHeadingDot" style="width: 8px; height: 8px;"></span> STATUS SERVER
                </div>
                <div class="apx-footer-status-box">
                    <div class="d-flex align-items-center justify-content-between mb-3 gap-2">
                        <div class="d-flex align-items-center gap-2 min-w-0 flex-grow-1">
                            <div class="apx-mc-block-icon flex-shrink-0">
                                <i class="bi bi-box-fill text-success"></i>
                            </div>
                            <div class="min-w-0">
                                <div class="apx-status-ip-text text-truncate">APEXSIONS.MY.ID</div>
                                <div class="apx-status-sub-text text-truncate">Java &amp; Bedrock</div>
                            </div>
                        </div>
                        <span class="apx-status-pill apx-pill-online flex-shrink-0 ms-2" id="apxFooterStatusBadge">
                            <span class="apx-pulse-dot-sm"></span> ONLINE
                        </span>
                    </div>

                    <div class="d-flex align-items-center justify-content-between small text-muted mb-3 py-2 border-top border-bottom border-secondary border-opacity-15">
                        <div>
                            <span class="text-dim">VERSI</span>
                            <div class="text-white fw-bold" id="apxFooterVersion">1.21.4</div>
                        </div>
                        <div class="text-center">
                            <span class="text-dim">PEMAIN</span>
                            <div class="text-white fw-bold"><span id="apxFooterPlayers">0</span> / <span id="apxFooterMaxPlayers">500</span></div>
                        </div>
                        <div class="text-end">
                            <span class="text-dim">PORT</span>
                            <div class="text-white fw-bold">19132</div>
                        </div>
                    </div>

                    <button type="button" class="btn btn-apx-join w-100" data-apx-copy="apexsions.my.id">
                        <i class="bi bi-controller me-2"></i> GABUNG SEKARANG <i class="bi bi-arrow-right ms-1"></i>
                    </button>
                </div>
            </div>
        </div>

        <!-- Bottom Copyright & Center Emblem Bar -->
        <div class="apx-footer-bottom pt-4 border-top border-secondary border-opacity-15">
            <div class="row align-items-center gy-3">
                <!-- Left Copyright -->
                <div class="col-lg-4 text-center text-lg-start">
                    <div class="text-white small mb-1">&copy; {{ date('Y') }} Apexsions. All rights reserved.</div>
                    <div class="text-muted" style="font-size: 0.78rem;">Dibangun dengan komunitas, untuk masa depan yang lebih besar.</div>
                </div>

                <!-- Center Emblem -->
                <div class="col-lg-4 text-center">
                    <div class="d-flex align-items-center justify-content-center gap-2 text-warning mb-1">
                        <span class="apx-emblem-line"></span>
                        <span>&diams;</span>
                        <i class="bi bi-crown fs-5"></i>
                        <span>&diams;</span>
                        <span class="apx-emblem-line"></span>
                    </div>
                    <div class="apx-emblem-title" style="font-family: 'Cinzel', serif; font-size: 0.7rem; letter-spacing: 0.25em; color: var(--apx-gold-light);">
                        THE PEAK CIVILIZATIONS
                    </div>
                </div>

                <!-- Right Legal & Script -->
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
