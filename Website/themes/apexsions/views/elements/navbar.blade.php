<nav class="navbar navbar-expand-lg apx-navbar">
    <div class="container">
        <a class="apx-navbar-brand" href="{{ route('home') }}">
            <span class="text-primary"><i class="bi bi-gem"></i></span>
            <span>APEXSIONS</span>
        </a>

        <button class="navbar-toggler border-secondary text-white" type="button" data-bs-toggle="collapse" data-bs-target="#navbarMain">
            <i class="bi bi-list fs-2 text-light"></i>
        </button>

        <div class="collapse navbar-collapse" id="navbarMain">
            <ul class="navbar-nav mx-auto mb-2 mb-lg-0 gap-1">
                <li class="nav-item">
                    <a class="nav-link @if(request()->routeIs('home')) active @endif" href="{{ route('home') }}">
                        <i class="bi bi-house me-1"></i> Beranda
                    </a>
                </li>
                @if(plugins()->isEnabled('shop'))
                    <li class="nav-item">
                        <a class="nav-link @if(request()->is('shop*')) active @endif" href="{{ route('shop.home') }}">
                            <i class="bi bi-cart3 me-1"></i> Store
                        </a>
                    </li>
                @endif
                @if(plugins()->isEnabled('wiki'))
                    <li class="nav-item">
                        <a class="nav-link @if(request()->is('wiki*')) active @endif" href="{{ route('wiki.home') }}">
                            <i class="bi bi-journal-text me-1"></i> Wiki & Perintah
                        </a>
                    </li>
                @endif
                @if(plugins()->isEnabled('apexsions-bridge') && auth()->check())
                    <li class="nav-item">
                        <a class="nav-link @if(request()->is('apexsions-bridge*')) active @endif" href="{{ route('apexsions-bridge.link.index') }}">
                            <i class="bi bi-controller me-1"></i> Tautkan Akun
                        </a>
                    </li>
                @endif
            </ul>

            <div class="d-flex align-items-center gap-2">
                @auth
                    <div class="dropdown">
                        <button class="btn btn-apx-secondary dropdown-toggle d-flex align-items-center gap-2" type="button" data-bs-toggle="dropdown">
                            <img src="{{ auth()->user()->getAvatar() }}" alt="{{ auth()->user()->name }}" class="rounded-circle" width="28" height="28">
                            <span>{{ auth()->user()->name }}</span>
                        </button>
                        <ul class="dropdown-menu dropdown-menu-dark dropdown-menu-end shadow-lg border-secondary">
                            <li><a class="dropdown-item" href="{{ route('profile.index') }}"><i class="bi bi-person me-2"></i> Profil</a></li>
                            @if(plugins()->isEnabled('apexsions-bridge'))
                                <li><a class="dropdown-item" href="{{ route('apexsions-bridge.link.index') }}"><i class="bi bi-controller me-2"></i> Tautkan Minecraft</a></li>
                            @endif
                            @can('admin-access')
                                <li><hr class="dropdown-divider border-secondary"></li>
                                <li><a class="dropdown-item text-warning" href="{{ route('admin.dashboard') }}"><i class="bi bi-speedometer2 me-2"></i> Admin Panel</a></li>
                            @endcan
                            <li><hr class="dropdown-divider border-secondary"></li>
                            <li>
                                <form action="{{ route('logout') }}" method="POST">
                                    @csrf
                                    <button type="submit" class="dropdown-item text-danger"><i class="bi bi-box-arrow-right me-2"></i> Keluar</button>
                                </form>
                            </li>
                        </ul>
                    </div>
                @else
                    <a href="{{ route('login') }}" class="btn btn-apx-secondary">Masuk</a>
                    <a href="{{ route('register') }}" class="btn btn-apx-primary">Daftar</a>
                @endauth
            </div>
        </div>
    </div>
</nav>
