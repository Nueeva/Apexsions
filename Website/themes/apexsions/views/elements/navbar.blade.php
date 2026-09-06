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
                    <a class="nav-link apx-nav-link @if(request()->routeIs('home') && !request()->has('page')) active @endif" href="{{ route('home') }}">
                        Beranda
                    </a>
                </li>
                @if(plugins()->isEnabled('shop'))
                    <li class="nav-item">
                        <a class="nav-link apx-nav-link @if(request()->is('shop*')) active @endif" href="{{ route('shop.home') }}">
                            Webstore
                        </a>
                    </li>
                @endif
                <li class="nav-item">
                    <a class="nav-link apx-nav-link" href="{{ route('home') }}#features">
                        Fitur
                    </a>
                </li>
                @if(plugins()->isEnabled('wiki'))
                    <li class="nav-item">
                        <a class="nav-link apx-nav-link @if(request()->is('wiki*')) active @endif" href="{{ route('wiki.index') }}">
                            Wiki
                        </a>
                    </li>
                @endif
                @if(Route::has('leaderboard'))
                    <li class="nav-item">
                        <a class="nav-link apx-nav-link @if(request()->routeIs('leaderboard')) active @endif" href="{{ route('leaderboard') }}">
                            Leaderboard
                        </a>
                    </li>
                @endif
                <li class="nav-item">
                    <a class="nav-link apx-nav-link @if(request()->routeIs('rules')) active @endif" href="{{ route('rules') }}">
                        Peraturan
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link apx-nav-link @if(request()->routeIs('vote')) active @endif" href="{{ route('vote') }}">
                        Vote
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link apx-nav-link" href="https://discord.gg/apexsions" target="_blank" rel="noopener noreferrer">
                        Discord
                    </a>
                </li>
            </ul>

            <!-- Right Actions: Cart & User Profile Box -->
            <div class="d-flex align-items-center gap-2 mt-3 mt-lg-0">
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
                                <small class="text-muted d-block" style="font-size: 0.7rem; letter-spacing: 0.08em;">AKUN TERDAFTAR</small>
                                <span class="fw-bold text-white">{{ auth()->user()->name }}</span>
                            </li>
                            <li><a class="dropdown-item py-2" href="{{ route('profile.index') }}"><i class="bi bi-person me-2 text-dim"></i> Profil Pemain</a></li>
                            @if(plugins()->isEnabled('apexsions-bridge'))
                                <li><a class="dropdown-item py-2" href="{{ route('apexsions-bridge.link.index') }}"><i class="bi bi-controller me-2 text-dim"></i> Tautkan Minecraft</a></li>
                            @endif
                            @if(plugins()->isEnabled('shop') && Route::has('shop.payments.index'))
                                <li><a class="dropdown-item py-2" href="{{ route('shop.payments.index') }}"><i class="bi bi-receipt me-2 text-dim"></i> Riwayat Belanja</a></li>
                            @endif
                            @can('admin-access')
                                <li><hr class="dropdown-divider border-secondary border-opacity-25"></li>
                                <li><a class="dropdown-item py-2 text-white fw-bold" href="{{ route('admin.dashboard') }}"><i class="bi bi-speedometer2 me-2 text-gold"></i> Admin Panel</a></li>
                            @endcan
                            <li><hr class="dropdown-divider border-secondary border-opacity-25"></li>
                            <li>
                                <form action="{{ route('logout') }}" method="POST">
                                    @csrf
                                    <button type="submit" class="dropdown-item py-2 text-danger"><i class="bi bi-box-arrow-right me-2"></i> Keluar</button>
                                </form>
                            </li>
                        </ul>
                    </div>
                @else
                    <a href="{{ route('login') }}" class="btn btn-apx-outline btn-sm px-3 py-2">Masuk</a>
                    <a href="{{ route('register') }}" class="btn btn-apx-gold btn-sm px-3 py-2">Daftar</a>
                @endauth
            </div>
        </div>
    </div>
</nav>
