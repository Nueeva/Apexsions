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

        <!-- Mobile Toggler -->
        <button class="navbar-toggler border-0 text-white p-2" type="button" data-bs-toggle="collapse" data-bs-target="#navbarMain" aria-controls="navbarMain" aria-expanded="false" aria-label="Buka menu navigasi">
            <i class="bi bi-list fs-2 text-white"></i>
        </button>

        <!-- Nav Links & Right Actions -->
        <div class="collapse navbar-collapse" id="navbarMain">
            <ul class="navbar-nav mx-auto mb-2 mb-lg-0 apx-nav-list">
                <li class="nav-item">
                    <a class="nav-link apx-nav-link @if(request()->routeIs('home') && !request()->has('page')) active @endif" href="{{ route('home') }}" data-i18n="nav_home">
                        Beranda
                    </a>
                </li>
                @if(plugins()->isEnabled('shop'))
                    <li class="nav-item">
                        <a class="nav-link apx-nav-link @if(request()->is('shop*')) active @endif" href="{{ route('shop.home') }}" data-i18n="nav_shop">
                            Webstore
                        </a>
                    </li>
                @endif
                <li class="nav-item">
                    <a class="nav-link apx-nav-link" href="{{ route('home') }}#features" data-i18n="nav_features">
                        Fitur
                    </a>
                </li>
                @if(plugins()->isEnabled('wiki'))
                    <li class="nav-item">
                        <a class="nav-link apx-nav-link @if(request()->is('wiki*')) active @endif" href="{{ route('wiki.index') }}" data-i18n="nav_wiki">
                            Wiki
                        </a>
                    </li>
                @endif
                @if(Route::has('leaderboard') || Route::has('apexsions-bridge.leaderboard'))
                    <li class="nav-item">
                        <a class="nav-link apx-nav-link @if(request()->is('leaderboard*')) active @endif" href="{{ url('/leaderboard') }}" data-i18n="nav_leaderboard">
                            Leaderboard
                        </a>
                    </li>
                @endif
                <li class="nav-item">
                    <a class="nav-link apx-nav-link @if(request()->routeIs('rules')) active @endif" href="{{ route('rules') }}" data-i18n="nav_rules">
                        Peraturan
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link apx-nav-link @if(request()->routeIs('vote')) active @endif" href="{{ route('vote') }}" data-i18n="nav_vote">
                        Vote
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link apx-nav-link" href="https://discord.gg/apexsions" target="_blank" rel="noopener noreferrer" data-i18n="nav_discord">
                        Discord
                    </a>
                </li>
            </ul>

            <!-- Right Actions: Language Switcher, Cart & User Profile Box -->
            <div class="d-flex align-items-center gap-2 mt-3 mt-lg-0">
                <!-- Language Accessibility Switcher (ID / EN) -->
                <div class="dropdown apx-lang-dropdown-wrapper">
                    <button class="btn apx-lang-btn dropdown-toggle d-flex align-items-center gap-1" type="button" id="apxLangDropdown" data-bs-toggle="dropdown" aria-expanded="false" title="Pilih Bahasa / Select Language" aria-label="Pilih Bahasa / Select Language">
                        <i class="bi bi-translate text-gold"></i>
                        <span class="apx-lang-current-label fw-bold">ID</span>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-dark dropdown-menu-end shadow-lg apx-nav-dropdown apx-lang-menu" aria-labelledby="apxLangDropdown">
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
                        <ul class="dropdown-menu dropdown-menu-dark dropdown-menu-end shadow-lg apx-nav-dropdown">
                            <li class="px-3 py-2 border-bottom border-secondary border-opacity-25 mb-1">
                                <small class="text-muted d-block" style="font-size: 0.7rem; letter-spacing: 0.08em;" data-i18n="nav_acc_registered">AKUN TERDAFTAR</small>
                                <span class="fw-bold text-white">{{ auth()->user()->name }}</span>
                            </li>
                            <li><a class="dropdown-item py-2" href="{{ route('profile.index') }}"><i class="bi bi-person me-2 text-dim"></i> <span data-i18n="nav_profile">Profil Pemain</span></a></li>
                            @if(plugins()->isEnabled('apexsions-bridge'))
                                <li><a class="dropdown-item py-2" href="{{ route('apexsions-bridge.link.index') }}"><i class="bi bi-controller me-2 text-dim"></i> <span data-i18n="nav_link_mc">Tautkan Minecraft</span></a></li>
                            @endif
                            @if(plugins()->isEnabled('shop') && Route::has('shop.payments.index'))
                                <li><a class="dropdown-item py-2" href="{{ route('shop.payments.index') }}"><i class="bi bi-receipt me-2 text-dim"></i> <span data-i18n="nav_order_history">Riwayat Belanja</span></a></li>
                            @endif
                            @can('admin-access')
                                <li><hr class="dropdown-divider border-secondary border-opacity-25"></li>
                                <li><a class="dropdown-item py-2 text-white fw-bold" href="{{ route('admin.dashboard') }}"><i class="bi bi-speedometer2 me-2 text-gold"></i> <span data-i18n="nav_admin_panel">Admin Panel</span></a></li>
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
    </div>
</nav>
