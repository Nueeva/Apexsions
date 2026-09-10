<nav class="navbar navbar-expand-lg apx-navbar" aria-label="Navigasi Utama">
    <div class="container">
        <!-- Brand Logo & Title -->
        <a class="apx-navbar-brand" href="{{ route('home') }}">
            <div class="apx-brand-logo-box">
                <img src="{{ theme_asset('img/logo.png') }}&v={{ @filemtime(public_path('assets/themes/apexsions/img/logo.png')) ?: '3' }}" alt="Apexsions Crest" class="apx-brand-logo-img" width="42" height="42">
            </div>
            <div class="d-flex flex-column">
                <span class="apx-brand-title">APEXSIONS</span>
                <span class="apx-brand-tagline">THE PEAK CIVILIZATIONS</span>
            </div>
        </a>

        <!-- Desktop Navigation Links (Center) -->
        <div class="collapse navbar-collapse d-none d-lg-flex" id="navbarMain">
            <ul class="navbar-nav mx-auto mb-2 mb-lg-0 apx-nav-list">
                <!-- 1. Beranda -->
                <li class="nav-item">
                    <a class="nav-link apx-nav-link @if(request()->routeIs('home') && !request()->has('page')) active @endif" href="{{ route('home') }}" data-i18n="nav_home">
                        Beranda
                    </a>
                </li>

                <!-- 2. Webstore -->
                @if(plugins()->isEnabled('shop'))
                    <li class="nav-item">
                        <a class="nav-link apx-nav-link @if(request()->is('shop*')) active @endif" href="{{ route('shop.home') }}" data-i18n="nav_shop">
                            Webstore
                        </a>
                    </li>
                @endif

                <!-- 3. Wiki -->
                @if(plugins()->isEnabled('wiki'))
                    <li class="nav-item">
                        <a class="nav-link apx-nav-link @if(request()->is('wiki*')) active @endif" href="{{ route('wiki.index') }}" data-i18n="nav_wiki">
                            Wiki
                        </a>
                    </li>
                @endif

                <!-- 4. Peraturan -->
                <li class="nav-item">
                    <a class="nav-link apx-nav-link @if(request()->routeIs('rules')) active @endif" href="{{ route('rules') }}" data-i18n="nav_rules">
                        Peraturan
                    </a>
                </li>

                <!-- 5. Vote -->
                @if(Route::has('vote'))
                    <li class="nav-item">
                        <a class="nav-link apx-nav-link @if(request()->routeIs('vote')) active @endif" href="{{ route('vote') }}" data-i18n="nav_vote">
                            Vote
                        </a>
                    </li>
                @endif
            </ul>

            <!-- Right Actions for Desktop: Theme Toggle, Language, Discord, Cart, Profile -->
            <div class="d-flex align-items-center gap-2">
                <!-- Theme Mode Switcher Toggle (Desktop) -->
                <button class="btn apx-theme-btn apx-theme-toggle" type="button" aria-label="Ganti Tema (Gelap / Terang)" title="Ganti Tema (Gelap / Terang)">
                    <i class="bi bi-moon-stars-fill apx-theme-icon-dark text-warning"></i>
                    <i class="bi bi-sun-fill apx-theme-icon-light text-warning d-none"></i>
                </button>

                <!-- Language Accessibility Switcher (ID / EN) -->
                <div class="dropdown apx-lang-dropdown-wrapper">
                    <button class="btn apx-lang-btn dropdown-toggle d-flex align-items-center gap-1" type="button" id="apxLangDropdown" data-bs-toggle="dropdown" aria-expanded="false" title="Pilih Bahasa / Select Language" aria-label="Pilih Bahasa / Select Language">
                        <i class="bi bi-translate text-gold"></i>
                        <span class="apx-lang-current-label fw-bold">ID</span>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow-lg apx-nav-dropdown apx-lang-menu" aria-labelledby="apxLangDropdown">
                        <li>
                            <button type="button" class="dropdown-item py-2 d-flex align-items-center justify-content-between apx-lang-choice active" data-apx-lang="id">
                                <span class="d-flex align-items-center gap-2">
                                    <span class="apx-flag-emoji">🇮🇩</span>
                                    <span class="apx-lang-name">Bahasa Indonesia</span>
                                </span>
                                <i class="bi bi-check2 text-gold apx-lang-active-check" data-lang-check="id"></i>
                            </button>
                        </li>
                        <li>
                            <button type="button" class="dropdown-item py-2 d-flex align-items-center justify-content-between apx-lang-choice" data-apx-lang="en">
                                <span class="d-flex align-items-center gap-2">
                                    <span class="apx-flag-emoji">🇬🇧</span>
                                    <span class="apx-lang-name">English</span>
                                </span>
                                <i class="bi bi-check2 text-gold apx-lang-active-check d-none" data-lang-check="en"></i>
                            </button>
                        </li>
                    </ul>
                </div>

                <!-- Dedicated Discord Community CTA Button -->
                <a href="https://discord.gg/apexsions" target="_blank" rel="noopener noreferrer" class="btn btn-apx-discord d-inline-flex align-items-center gap-2" title="Gabung Komunitas Discord">
                    <i class="bi bi-discord text-white" style="font-size: 1rem;"></i>
                    <span class="fw-bold" data-i18n="nav_btn_discord">Discord</span>
                </a>

                @if(plugins()->isEnabled('shop') && Route::has('shop.cart.index') && request()->is('shop*'))
                    <a href="{{ route('shop.cart.index') }}" class="apx-nav-cart-btn" title="Keranjang Belanja">
                        <i class="bi bi-cart3"></i>
                    </a>
                @endif

                @auth
                    <div class="dropdown">
                        <button class="apx-user-box dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                            <img src="{{ auth()->user()->getAvatar(28) }}" alt="{{ auth()->user()->name }}" class="apx-user-avatar-mini">
                            <span class="apx-user-box-name">{{ auth()->user()->name }}</span>
                            <i class="bi bi-chevron-down apx-user-box-chevron"></i>
                        </button>
                        <ul class="dropdown-menu dropdown-menu-end shadow-lg apx-nav-dropdown">
                            <li class="px-3 py-2 border-bottom border-secondary border-opacity-25 mb-1">
                                <small class="text-muted d-block" style="font-size: 0.7rem; letter-spacing: 0.08em;" data-i18n="nav_acc_registered">AKUN TERDAFTAR</small>
                                <span class="fw-bold text-main">{{ auth()->user()->name }}</span>
                            </li>
                            <li><a class="dropdown-item py-2" href="{{ route('profile.index') }}"><i class="bi bi-person me-2 text-dim"></i> <span data-i18n="nav_profile">Profil Pemain</span></a></li>
                            @if(Route::has('leaderboard') || Route::has('apexsions-bridge.leaderboard'))
                                <li><a class="dropdown-item py-2" href="{{ url('/leaderboard') }}"><i class="bi bi-trophy me-2 text-warning"></i> <span data-i18n="nav_leaderboard">Papan Peringkat</span></a></li>
                            @endif
                            @if(plugins()->isEnabled('apexsions-bridge'))
                                <li><a class="dropdown-item py-2" href="{{ route('apexsions-bridge.link.index') }}"><i class="bi bi-controller me-2 text-dim"></i> <span data-i18n="nav_link_mc">Tautkan Minecraft</span></a></li>
                            @endif
                            @if(plugins()->isEnabled('shop') && Route::has('shop.payments.index'))
                                <li><a class="dropdown-item py-2" href="{{ route('shop.payments.index') }}"><i class="bi bi-receipt me-2 text-dim"></i> <span data-i18n="nav_order_history">Riwayat Belanja</span></a></li>
                            @endif
                            @can('admin-access')
                                <li><hr class="dropdown-divider border-secondary border-opacity-25"></li>
                                <li><a class="dropdown-item py-2 text-gold fw-bold" href="{{ route('admin.dashboard') }}"><i class="bi bi-speedometer2 me-2 text-gold"></i> <span data-i18n="nav_admin_panel">Admin Panel</span></a></li>
                            @endcan
                            <li><hr class="dropdown-divider border-secondary border-opacity-25"></li>
                            <li>
                                <form action="{{ route('logout') }}" method="POST">
                                    @csrf
                                    <button type="submit" class="dropdown-item py-2 text-danger"><i class="bi bi-box-arrow-right me-2"></i> <span data-i18n="nav_logout">Keluar</span></button>
                                </form>
                            </li>
                        </ul>
                    </div>
                @else
                    <a href="{{ route('login') }}" class="btn btn-apx-outline btn-sm px-3 py-2" data-i18n="nav_login">Masuk</a>
                    <a href="{{ route('register') }}" class="btn btn-apx-gold btn-sm px-3 py-2" data-i18n="nav_register">Daftar</a>
                @endauth
            </div>
        </div>

        <!-- Mobile Header Quick Controls (Right Side on Mobile Screen) -->
        <div class="d-flex d-lg-none align-items-center gap-2">
            <!-- Theme Toggle Mobile Quick Button -->
            <button class="btn apx-theme-btn apx-theme-toggle" type="button" aria-label="Ganti Tema" title="Ganti Tema">
                <i class="bi bi-moon-stars-fill apx-theme-icon-dark text-warning"></i>
                <i class="bi bi-sun-fill apx-theme-icon-light text-warning d-none"></i>
            </button>

            @if(plugins()->isEnabled('shop') && Route::has('shop.cart.index') && request()->is('shop*'))
                <a href="{{ route('shop.cart.index') }}" class="apx-nav-cart-btn" title="Keranjang Belanja">
                    <i class="bi bi-cart3"></i>
                </a>
            @endif

            <!-- Mobile Offcanvas Drawer Trigger Button (Touch Target >= 44x44px) -->
            <button class="btn apx-mobile-drawer-toggle" type="button" data-bs-toggle="offcanvas" data-bs-target="#apxMobileDrawer" aria-controls="apxMobileDrawer" aria-label="Buka Menu Navigasi Apexsions">
                <i class="bi bi-list fs-3"></i>
            </button>
        </div>
    </div>
</nav>

<!-- ==============================================================================
     Apexsions Mobile Navigation Offcanvas Drawer
     Native App-like Fluid Sliding Navigation (Zero Layout Shift)
     ============================================================================== -->
<div class="offcanvas offcanvas-end apx-offcanvas-drawer" tabindex="-1" id="apxMobileDrawer" aria-labelledby="apxMobileDrawerLabel">
    <div class="offcanvas-header apx-drawer-header d-flex align-items-center justify-content-between p-3 border-bottom border-secondary border-opacity-15">
        <div class="d-flex align-items-center gap-2" id="apxMobileDrawerLabel">
            <div class="apx-brand-logo-box" style="width: 36px; height: 36px;">
                <img src="{{ theme_asset('img/logo.png') }}&v={{ @filemtime(public_path('assets/themes/apexsions/img/logo.png')) ?: '3' }}" alt="Apexsions Crest" class="apx-brand-logo-img" width="36" height="36">
            </div>
            <div class="d-flex flex-column">
                <span class="apx-brand-title" style="font-size: 1.05rem;">APEXSIONS</span>
                <span class="apx-brand-tagline" style="font-size: 0.52rem;">THE PEAK CIVILIZATIONS</span>
            </div>
        </div>
        <button type="button" class="btn-close apx-drawer-close-btn" data-bs-dismiss="offcanvas" aria-label="Tutup"></button>
    </div>

    <div class="offcanvas-body apx-drawer-body p-3 d-flex flex-column justify-content-between">
        <!-- Top Section: User Status Card & Main Navigation -->
        <div>
            <!-- User Profile / Guest Action Card -->
            @auth
                <div class="apx-drawer-user-card mb-3 p-3 rounded-3 border border-secondary border-opacity-20 d-flex align-items-center gap-3">
                    <img src="{{ auth()->user()->getAvatar(42) }}" alt="{{ auth()->user()->name }}" class="rounded-2 border border-warning" width="42" height="42">
                    <div class="flex-grow-1 overflow-hidden">
                        <small class="text-muted d-block font-monospace" style="font-size: 0.68rem; letter-spacing: 0.08em;" data-i18n="nav_acc_registered">WARGA AKTIF</small>
                        <div class="fw-bold text-truncate text-main">{{ auth()->user()->name }}</div>
                    </div>
                    <a href="{{ route('profile.index') }}" class="btn btn-sm btn-apx-outline" title="Profil">
                        <i class="bi bi-person"></i>
                    </a>
                </div>
            @else
                <div class="apx-drawer-guest-card mb-3 p-3 rounded-3 border border-secondary border-opacity-20">
                    <div class="small text-muted mb-2">Bergabunglah ke Realm Peradaban:</div>
                    <div class="d-flex gap-2">
                        <a href="{{ route('login') }}" class="btn btn-apx-outline btn-sm flex-fill py-2 text-center" data-i18n="nav_login">Masuk</a>
                        <a href="{{ route('register') }}" class="btn btn-apx-gold btn-sm flex-fill py-2 text-center" data-i18n="nav_register">Daftar</a>
                    </div>
                </div>
            @endauth

            <!-- Navigation Links List -->
            <ul class="nav flex-column apx-drawer-nav-list gap-1">
                <li class="nav-item">
                    <a class="apx-drawer-link @if(request()->routeIs('home') && !request()->has('page')) active @endif" href="{{ route('home') }}">
                        <i class="bi bi-house text-warning"></i>
                        <span data-i18n="nav_home">Beranda</span>
                    </a>
                </li>

                @if(plugins()->isEnabled('shop'))
                    <li class="nav-item">
                        <a class="apx-drawer-link @if(request()->is('shop*')) active @endif" href="{{ route('shop.home') }}">
                            <i class="bi bi-shop text-warning"></i>
                            <span data-i18n="nav_shop">Webstore</span>
                        </a>
                    </li>
                @endif

                @if(plugins()->isEnabled('wiki'))
                    <li class="nav-item">
                        <a class="apx-drawer-link @if(request()->is('wiki*')) active @endif" href="{{ route('wiki.index') }}">
                            <i class="bi bi-journal-text text-warning"></i>
                            <span data-i18n="nav_wiki">Wiki &amp; Ensiklopedia</span>
                        </a>
                    </li>
                @endif

                <li class="nav-item">
                    <a class="apx-drawer-link @if(request()->routeIs('rules')) active @endif" href="{{ route('rules') }}">
                        <i class="bi bi-shield-check text-warning"></i>
                        <span data-i18n="nav_rules">Peraturan Realm</span>
                    </a>
                </li>

                @if(Route::has('vote'))
                    <li class="nav-item">
                        <a class="apx-drawer-link @if(request()->routeIs('vote')) active @endif" href="{{ route('vote') }}">
                            <i class="bi bi-trophy text-warning"></i>
                            <span data-i18n="nav_vote">Dukung Vote</span>
                        </a>
                    </li>
                @endif

                @if(Route::has('leaderboard') || Route::has('apexsions-bridge.leaderboard'))
                    <li class="nav-item">
                        <a class="apx-drawer-link" href="{{ url('/leaderboard') }}">
                            <i class="bi bi-bar-chart-line text-warning"></i>
                            <span data-i18n="nav_leaderboard">Papan Peringkat</span>
                        </a>
                    </li>
                @endif

                @auth
                    @if(plugins()->isEnabled('apexsions-bridge'))
                        <li class="nav-item">
                            <a class="apx-drawer-link" href="{{ route('apexsions-bridge.link.index') }}">
                                <i class="bi bi-controller text-warning"></i>
                                <span data-i18n="nav_link_mc">Tautkan Akun Minecraft</span>
                            </a>
                        </li>
                    @endif
                    @if(plugins()->isEnabled('shop') && Route::has('shop.payments.index'))
                        <li class="nav-item">
                            <a class="apx-drawer-link" href="{{ route('shop.payments.index') }}">
                                <i class="bi bi-receipt text-warning"></i>
                                <span data-i18n="nav_order_history">Riwayat Belanja</span>
                            </a>
                        </li>
                    @endif
                    @can('admin-access')
                        <li class="nav-item">
                            <a class="apx-drawer-link text-gold fw-bold" href="{{ route('admin.dashboard') }}">
                                <i class="bi bi-speedometer2 text-gold"></i>
                                <span data-i18n="nav_admin_panel">Admin Panel</span>
                            </a>
                        </li>
                    @endcan
                @endauth
            </ul>
        </div>

        <!-- Bottom Drawer Actions: Language, Discord, Theme Toggle, Logout -->
        <div class="apx-drawer-bottom-actions pt-3 border-top border-secondary border-opacity-15 mt-3">
            <!-- Theme Mode Switcher in Drawer -->
            <div class="d-flex align-items-center justify-content-between mb-3 px-2">
                <span class="small text-muted d-flex align-items-center gap-2">
                    <i class="bi bi-palette text-gold"></i> Tampilan Tema
                </span>
                <button type="button" class="btn btn-sm btn-apx-outline apx-theme-toggle d-flex align-items-center gap-2 py-1 px-3" aria-label="Ganti Tema">
                    <i class="bi bi-moon-stars-fill apx-theme-icon-dark text-warning"></i>
                    <i class="bi bi-sun-fill apx-theme-icon-light text-warning d-none"></i>
                    <span class="apx-theme-text font-monospace small">Dark / Light</span>
                </button>
            </div>

            <!-- Language Switcher in Drawer -->
            <div class="d-flex align-items-center justify-content-between mb-3 px-2">
                <span class="small text-muted d-flex align-items-center gap-2">
                    <i class="bi bi-translate text-gold"></i> Bahasa
                </span>
                <div class="btn-group btn-group-sm" role="group" aria-label="Pilih Bahasa">
                    <button type="button" class="btn btn-apx-outline apx-lang-choice active px-2" data-apx-lang="id">🇮🇩 ID</button>
                    <button type="button" class="btn btn-apx-outline apx-lang-choice px-2" data-apx-lang="en">🇬🇧 EN</button>
                </div>
            </div>

            <!-- Discord Community Button -->
            <a href="https://discord.gg/apexsions" target="_blank" rel="noopener noreferrer" class="btn btn-apx-discord w-100 mb-2 py-2 d-flex align-items-center justify-content-center gap-2">
                <i class="bi bi-discord"></i>
                <span class="fw-bold">Gabung Discord Komunitas</span>
            </a>

            @auth
                <form action="{{ route('logout') }}" method="POST" class="mt-2">
                    @csrf
                    <button type="submit" class="btn btn-outline-danger w-100 py-2 d-flex align-items-center justify-content-center gap-2 small">
                        <i class="bi bi-box-arrow-right"></i> <span data-i18n="nav_logout">Keluar dari Akun</span>
                    </button>
                </form>
            @endauth
        </div>
    </div>
</div>
