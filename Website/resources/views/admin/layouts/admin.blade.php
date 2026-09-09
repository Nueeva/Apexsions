<!DOCTYPE html>
<html lang="{{ str_replace('_', '-', app()->getLocale()) }}">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    @stack('meta')

    <!-- CSRF Token -->
    <meta name="csrf-token" content="{{ csrf_token() }}">

    <title>@yield('title', 'Admin') | {{ site_name() }}</title>

    <!-- Favicon -->
    <link rel="shortcut icon" href="{{ favicon() }}">

    <!-- Scripts -->
    @vite('resources/js/admin/admin.js')
    @stack('scripts')

    <!-- Fonts -->
    <link rel="preconnect" href="https://fonts.bunny.net">
    <link href="https://fonts.bunny.net/css?family=Cinzel:600,700,800,900|Inter:300,400,600,800&display=swap" rel="stylesheet">
    <link href="{{ asset('vendor/bootstrap-icons/bootstrap-icons.css') }}" rel="stylesheet">

    <!-- Styles -->
    @vite('resources/sass/admin/admin.scss')
    @stack('styles')
    <link rel="stylesheet" href="{{ asset('assets/themes/apexsions/css/admin-apexsions.css') }}?v={{ @filemtime(public_path('assets/themes/apexsions/css/admin-apexsions.css')) ?: time() }}">

</head>
<body data-bs-theme="{{ dark_theme() ? 'dark' : 'light' }}">
    <!-- Page Wrapper -->
    <div class="wrapper">

        <!-- Sidebar -->
        <nav id="sidebar" class="sidebar js-sidebar">
            <div class="sidebar-content js-simplebar">

                <a class="sidebar-brand d-flex align-items-center justify-content-center" href="{{ route('home') }}" title="{{ site_name() }} — Kembali ke Beranda">
                    <div class="sidebar-brand-text mx-3 text-center">
                        <div class="d-flex align-items-center justify-content-center gap-2 mb-1">
                            <img src="{{ asset('assets/themes/apexsions/img/logo-ornate-gold.png') }}" alt="Apexsions" style="height: 32px; width: auto; filter: drop-shadow(0 0 8px rgba(201, 164, 92, 0.45));">
                            <span style="font-family: 'Cinzel', serif; font-size: 1.15rem; font-weight: 800; letter-spacing: 2px; color: #F1D58A; text-shadow: 0 0 12px rgba(201,164,92,0.4);">APEXSIONS</span>
                        </div>
                        <small class="d-block text-center font-weight-bold" style="color: #9E7B3E; font-size: 0.68rem; letter-spacing: 1.5px;">
                            THE PEAK CIVILIZATIONS &bull; CORE
                        </small>
                    </div>
                </a>

                <ul class="sidebar-nav">
                    <!-- SECTION 1: OVERVIEW -->
                    <li class="sidebar-header" style="color: #9E7B3E; font-size: 0.68rem; letter-spacing: 1.5px; font-weight: 700; text-transform: uppercase;">
                        Apexsions Realm
                    </li>
                    <li class="sidebar-item {{ add_active('admin.dashboard') }}">
                        <a class="sidebar-link" href="{{ route('admin.dashboard') }}">
                            <i class="bi bi-speedometer2"></i>
                            <span>{{ trans('admin.nav.dashboard') }}</span>
                        </a>
                    </li>

                    <!-- SECTION 2: MANAGEMENT -->
                    <li class="sidebar-header" style="color: #9E7B3E; font-size: 0.68rem; letter-spacing: 1.5px; font-weight: 700; text-transform: uppercase;">
                        Management
                    </li>
                    <li class="sidebar-item {{ add_active('apexsions-bridge.admin.players.*') }}">
                        <a class="sidebar-link" href="{{ route('apexsions-bridge.admin.players.index') }}">
                            <i class="bi bi-person-lines-fill"></i>
                            <span>Player Management</span>
                        </a>
                    </li>
                    <li class="sidebar-item {{ add_active('apexsions-bridge.admin.ranks.*') }}">
                        <a class="sidebar-link" href="{{ route('apexsions-bridge.admin.ranks.index') }}">
                            <i class="bi bi-trophy-fill"></i>
                            <span>Rank Management</span>
                        </a>
                    </li>
                    <li class="sidebar-item {{ add_active('apexsions-bridge.admin.moderation.*') }}">
                        <a class="sidebar-link" href="{{ route('apexsions-bridge.admin.moderation.index') }}">
                            <i class="bi bi-shield-shaded"></i>
                            <span>Moderation Desk</span>
                        </a>
                    </li>
                    <li class="sidebar-item {{ add_active('apexsions-bridge.admin.reports.*') }}">
                        <a class="sidebar-link" href="{{ route('apexsions-bridge.admin.reports.index') }}">
                            <i class="bi bi-flag-fill"></i>
                            <span>Reports Center</span>
                        </a>
                    </li>
                    <li class="sidebar-item {{ add_active('apexsions-bridge.admin.economy.*') }}">
                        <a class="sidebar-link {{ Route::is('apexsions-bridge.admin.economy.*') ? '' : 'collapsed'}}" href="#" data-bs-toggle="collapse" data-bs-target="#collapseEconomy" aria-expanded="{{ Route::is('apexsions-bridge.admin.economy.*') ? 'true' : 'false' }}" aria-controls="collapseEconomy">
                            <i class="bi bi-cash-stack"></i>
                            <span>Economy & Markets</span>
                        </a>
                        <ul id="collapseEconomy" class="sidebar-dropdown list-unstyled collapse {{ Route::is('apexsions-bridge.admin.economy.*') ? 'show' : ''}}" data-parent="#accordionSidebar">
                            <li class="sidebar-item {{ add_active('apexsions-bridge.admin.economy.index') }}">
                                <a class="sidebar-link" href="{{ route('apexsions-bridge.admin.economy.index') }}">
                                    <span>Economy Overview</span>
                                </a>
                            </li>
                            <li class="sidebar-item {{ add_active('apexsions-bridge.admin.economy.transactions.*') }}">
                                <a class="sidebar-link" href="{{ route('apexsions-bridge.admin.economy.transactions.index') }}">
                                    <span>Transaction Explorer</span>
                                </a>
                            </li>
                            <li class="sidebar-item {{ add_active('apexsions-bridge.admin.economy.auctions.*') }}">
                                <a class="sidebar-link" href="{{ route('apexsions-bridge.admin.economy.auctions.index') }}">
                                    <span>Auction Inspector</span>
                                </a>
                            </li>
                        </ul>
                    </li>

                    <!-- SECTION 3: MINECRAFT SERVER -->
                    <li class="sidebar-header" style="color: #9E7B3E; font-size: 0.68rem; letter-spacing: 1.5px; font-weight: 700; text-transform: uppercase;">
                        Minecraft Server
                    </li>
                    <li class="sidebar-item {{ add_active('apexsions-bridge.admin.server.*') }}">
                        <a class="sidebar-link" href="{{ route('apexsions-bridge.admin.server.index') }}">
                            <i class="bi bi-hdd-network-fill"></i>
                            <span>Server Operations</span>
                        </a>
                    </li>
                    <li class="sidebar-item {{ add_active('apexsions-bridge.admin.plugins.*') }}">
                        <a class="sidebar-link" href="{{ route('apexsions-bridge.admin.plugins.index') }}">
                            <i class="bi bi-cpu-fill"></i>
                            <span>Custom Plugin Suite</span>
                        </a>
                    </li>

                    <!-- SECTION 4: MONITORING & SECURITY -->
                    <li class="sidebar-header" style="color: #9E7B3E; font-size: 0.68rem; letter-spacing: 1.5px; font-weight: 700; text-transform: uppercase;">
                        Monitoring & Security
                    </li>
                    <li class="sidebar-item {{ add_active('apexsions-bridge.admin.audit-logs.*') }}">
                        <a class="sidebar-link" href="{{ route('apexsions-bridge.admin.audit-logs.index') }}">
                            <i class="bi bi-journal-text"></i>
                            <span>Unified Audit Logs</span>
                        </a>
                    </li>
                    <li class="sidebar-item {{ add_active('apexsions-bridge.admin.intelligence.*') }}">
                        <a class="sidebar-link" href="{{ route('apexsions-bridge.admin.intelligence.index') }}">
                            <i class="bi bi-shield-check"></i>
                            <span>Intelligence Desk</span>
                        </a>
                    </li>
                    <li class="sidebar-item {{ add_active('apexsions-bridge.admin.incidents.*') }}">
                        <a class="sidebar-link" href="{{ route('apexsions-bridge.admin.incidents.index') }}">
                            <i class="bi bi-exclamation-octagon-fill"></i>
                            <span>Incident Registry</span>
                        </a>
                    </li>
                    <li class="sidebar-item {{ add_active('apexsions-bridge.admin.notifications.*') }}">
                        <a class="sidebar-link" href="{{ route('apexsions-bridge.admin.notifications.index') }}">
                            <i class="bi bi-bell-fill"></i>
                            <span>Notifications Desk</span>
                        </a>
                    </li>
                    <li class="sidebar-item {{ add_active('apexsions-bridge.admin.automation.*') }}">
                        <a class="sidebar-link" href="{{ route('apexsions-bridge.admin.automation.index') }}">
                            <i class="bi bi-gear-wide-connected"></i>
                            <span>Safe Automation</span>
                        </a>
                    </li>

                    <!-- SECTION 5: WEB EXTENSIONS (OTHER PLUGINS LIKE SHOP, WIKI) -->
                    @php
                        $thirdPartyNavItems = collect(plugins()->getAdminNavItems())->filter(function ($item, $key) {
                            return !str_starts_with($key, 'apexsions-');
                        });
                    @endphp
                    @if($thirdPartyNavItems->isNotEmpty())
                        <li class="sidebar-header" style="color: #9E7B3E; font-size: 0.68rem; letter-spacing: 1.5px; font-weight: 700; text-transform: uppercase;">
                            Web Extensions
                        </li>
                        @foreach($thirdPartyNavItems as $navId => $navItem)
                            @if(! isset($navItem['permission']) || Gate::any($navItem['permission']))
                                @if(($navItem['type'] ?? '') !== 'dropdown')
                                    <li class="sidebar-item {{ add_active($navItem['route']) }}">
                                        <a class="sidebar-link" href="{{ route($navItem['route']) }}">
                                            <i class="{{ $navItem['icon'] }}"></i>
                                            <span>{{ $navItem['name'] }}</span>
                                        </a>
                                    </li>
                                @elseif(Arr::first($navItem['items'] ?? [], fn ($item) => ! isset($item['permission']) || Gate::check($item['permission'])))
                                    <li class="sidebar-item @isset($navItem['route']) {{ add_active($navItem['route']) }} @endisset">
                                        <a class="sidebar-link @if(! isset($navItem['route']) || ! Route::is($navItem['route'])) collapsed @endif" href="#" data-bs-toggle="collapse" data-bs-target="#collapse{{ ucfirst($navId) }}" aria-expanded="true" aria-controls="collapse{{ ucfirst($navId) }}">
                                            <i class="{{ $navItem['icon'] }}"></i>
                                            <span>{{ $navItem['name'] }}</span>
                                        </a>
                                        <ul id="collapse{{ ucfirst($navId) }}" class="sidebar-dropdown list-unstyled collapse @if(isset($navItem['route']) && Route::is($navItem['route'])) show @endif" data-parent="#accordionSidebar">
                                            @foreach($navItem['items'] ?? [] as $route => $subItem)
                                                @if(! isset($subItem['permission']) || Gate::check($subItem['permission']))
                                                    <li class="sidebar-item {{ add_active($route) }}">
                                                        <a class="sidebar-link" href="{{ route($route) }}">
                                                            <span>{{ is_array($subItem) ? $subItem['name'] : $subItem }}</span>
                                                        </a>
                                                    </li>
                                                @endif
                                            @endforeach
                                        </ul>
                                    </li>
                                @endif
                            @endif
                        @endforeach
                    @endif

                    <!-- SECTION 6: WEB PLATFORM (AZURIOM CORE) -->
                    @canany(['admin.settings', 'admin.navbar', 'admin.users', 'admin.roles', 'admin.pages', 'admin.posts', 'admin.images', 'admin.plugins', 'admin.themes', 'admin.update'])
                        <li class="sidebar-header" style="color: #9E7B3E; font-size: 0.68rem; letter-spacing: 1.5px; font-weight: 700; text-transform: uppercase;">
                            Web Platform
                        </li>

                        @canany(['admin.settings', 'admin.navbar'])
                            <li class="sidebar-item {{ add_active('admin.settings.*', 'admin.social-links.*', 'admin.navbar-elements.*') }}">
                                <a class="sidebar-link {{ Route::is('admin.settings.*', 'admin.social-links.*', 'admin.navbar-elements.*') ? '' : 'collapsed'}}" href="#" data-bs-toggle="collapse" data-bs-target="#collapseWebSettings" aria-expanded="{{ Route::is('admin.settings.*', 'admin.social-links.*', 'admin.navbar-elements.*') ? 'true' : 'false' }}" aria-controls="collapseWebSettings">
                                    <i class="bi bi-sliders"></i>
                                    <span>Web Settings</span>
                                </a>
                                <ul id="collapseWebSettings" class="sidebar-dropdown list-unstyled collapse {{ Route::is('admin.settings.*', 'admin.social-links.*', 'admin.navbar-elements.*') ? 'show' : ''}}" data-parent="#accordionSidebar">
                                    @can('admin.settings')
                                        <li class="sidebar-item {{ add_active('admin.settings.index') }}">
                                            <a class="sidebar-link" href="{{ route('admin.settings.index') }}">
                                                {{ trans('admin.nav.settings.global') }}
                                            </a>
                                        </li>
                                        <li class="sidebar-item {{ add_active('admin.settings.home') }}">
                                            <a class="sidebar-link" href="{{ route('admin.settings.home') }}">
                                                {{ trans('admin.nav.settings.home') }}
                                            </a>
                                        </li>
                                        @if(! oauth_login())
                                            <li class="sidebar-item {{ add_active('admin.settings.auth') }}">
                                                <a class="sidebar-link" href="{{ route('admin.settings.auth') }}">
                                                    {{ trans('admin.nav.settings.auth') }}
                                                </a>
                                            </li>
                                        @endif
                                        <li class="sidebar-item {{ add_active('admin.settings.mail') }}">
                                            <a class="sidebar-link" href="{{ route('admin.settings.mail') }}">
                                                {{ trans('admin.nav.settings.mail') }}
                                            </a>
                                        </li>
                                        <li class="sidebar-item {{ add_active('admin.settings.performance') }}">
                                            <a class="sidebar-link" href="{{ route('admin.settings.performance') }}">
                                                {{ trans('admin.nav.settings.performances') }}
                                            </a>
                                        </li>
                                        <li class="sidebar-item {{ add_active('admin.settings.maintenance') }}">
                                            <a class="sidebar-link" href="{{ route('admin.settings.maintenance') }}">
                                                {{ trans('admin.nav.settings.maintenance') }}
                                            </a>
                                        </li>
                                        <li class="sidebar-item {{ add_active('admin.social-links.*') }}">
                                            <a class="sidebar-link" href="{{ route('admin.social-links.index') }}">
                                                {{ trans('admin.nav.settings.social') }}
                                            </a>
                                        </li>
                                    @endcan
                                    @can('admin.navbar')
                                        <li class="sidebar-item {{ add_active('admin.navbar-elements.*') }}">
                                            <a class="sidebar-link" href="{{ route('admin.navbar-elements.index') }}">
                                                {{ trans('admin.nav.settings.navbar') }}
                                            </a>
                                        </li>
                                    @endcan
                                </ul>
                            </li>
                        @endcanany

                        @canany(['admin.pages', 'admin.posts', 'admin.images'])
                            <li class="sidebar-item {{ add_active('admin.pages.*', 'admin.posts.*', 'admin.images.*') }}">
                                <a class="sidebar-link {{ Route::is('admin.pages.*', 'admin.posts.*', 'admin.images.*') ? '' : 'collapsed'}}" href="#" data-bs-toggle="collapse" data-bs-target="#collapseWebContent" aria-expanded="{{ Route::is('admin.pages.*', 'admin.posts.*', 'admin.images.*') ? 'true' : 'false' }}" aria-controls="collapseWebContent">
                                    <i class="bi bi-file-earmark-richtext"></i>
                                    <span>Web Content</span>
                                </a>
                                <ul id="collapseWebContent" class="sidebar-dropdown list-unstyled collapse {{ Route::is('admin.pages.*', 'admin.posts.*', 'admin.images.*') ? 'show' : ''}}" data-parent="#accordionSidebar">
                                    @can('admin.pages')
                                        <li class="sidebar-item {{ add_active('admin.pages.*') }}">
                                            <a class="sidebar-link" href="{{ route('admin.pages.index') }}">
                                                {{ trans('admin.nav.content.pages') }}
                                            </a>
                                        </li>
                                    @endcan
                                    @can('admin.posts')
                                        <li class="sidebar-item {{ add_active('admin.posts.*') }}">
                                            <a class="sidebar-link" href="{{ route('admin.posts.index') }}">
                                                {{ trans('admin.nav.content.posts') }}
                                            </a>
                                        </li>
                                    @endcan
                                    @can('admin.images')
                                        <li class="sidebar-item {{ add_active('admin.images.*') }}">
                                            <a class="sidebar-link" href="{{ route('admin.images.index') }}">
                                                {{ trans('admin.nav.content.images') }}
                                            </a>
                                        </li>
                                    @endcan
                                </ul>
                            </li>
                        @endcanany

                        @canany(['admin.users', 'admin.roles'])
                            <li class="sidebar-item {{ add_active('admin.users.*', 'admin.roles.*') }}">
                                <a class="sidebar-link {{ Route::is('admin.users.*', 'admin.roles.*') ? '' : 'collapsed'}}" href="#" data-bs-toggle="collapse" data-bs-target="#collapseWebUsers" aria-expanded="{{ Route::is('admin.users.*', 'admin.roles.*') ? 'true' : 'false' }}" aria-controls="collapseWebUsers">
                                    <i class="bi bi-person-gear"></i>
                                    <span>Web Accounts</span>
                                </a>
                                <ul id="collapseWebUsers" class="sidebar-dropdown list-unstyled collapse {{ Route::is('admin.users.*', 'admin.roles.*') ? 'show' : ''}}" data-parent="#accordionSidebar">
                                    @can('admin.users')
                                        <li class="sidebar-item {{ add_active('admin.users.*') }}">
                                            <a class="sidebar-link" href="{{ route('admin.users.index') }}">
                                                {{ trans('admin.nav.users.users') }}
                                            </a>
                                        </li>
                                    @endcan
                                    @can('admin.roles')
                                        <li class="sidebar-item {{ add_active('admin.roles.*') }}">
                                            <a class="sidebar-link" href="{{ route('admin.roles.index') }}">
                                                {{ trans('admin.nav.users.roles') }}
                                            </a>
                                        </li>
                                    @endcan
                                </ul>
                            </li>
                        @endcanany

                        @canany(['admin.plugins', 'admin.themes', 'admin.update'])
                            <li class="sidebar-item {{ add_active('admin.plugins.*', 'admin.themes.*', 'admin.update.*') }}">
                                <a class="sidebar-link {{ Route::is('admin.plugins.*', 'admin.themes.*', 'admin.update.*') ? '' : 'collapsed'}}" href="#" data-bs-toggle="collapse" data-bs-target="#collapseWebExtensions" aria-expanded="{{ Route::is('admin.plugins.*', 'admin.themes.*', 'admin.update.*') ? 'true' : 'false' }}" aria-controls="collapseWebExtensions">
                                    <i class="bi bi-puzzle"></i>
                                    <span>Extensions & System</span>
                                </a>
                                <ul id="collapseWebExtensions" class="sidebar-dropdown list-unstyled collapse {{ Route::is('admin.plugins.*', 'admin.themes.*', 'admin.update.*') ? 'show' : ''}}" data-parent="#accordionSidebar">
                                    @can('admin.plugins')
                                        <li class="sidebar-item {{ add_active('admin.plugins.*') }}">
                                            <a class="sidebar-link" href="{{ route('admin.plugins.index') }}">
                                                {{ trans('admin.nav.extensions.plugins') }}
                                                @if(($pluginsUpdates ?? 0) > 0)
                                                    <span class="sidebar-badge badge bg-danger">{{ $pluginsUpdates }}</span>
                                                @endif
                                            </a>
                                        </li>
                                    @endcan
                                    @can('admin.themes')
                                        <li class="sidebar-item {{ add_active('admin.themes.*') }}">
                                            <a class="sidebar-link" href="{{ route('admin.themes.index') }}">
                                                {{ trans('admin.nav.extensions.themes') }}
                                                @if(($themesUpdates ?? 0) > 0)
                                                    <span class="sidebar-badge badge bg-danger">{{ $themesUpdates }}</span>
                                                @endif
                                            </a>
                                        </li>
                                    @endcan
                                    @can('admin.update')
                                        <li class="sidebar-item {{ add_active('admin.update.*') }}">
                                            <a class="sidebar-link" href="{{ route('admin.update.index') }}">
                                                {{ trans('admin.nav.other.update') }}
                                                @if($hasUpdate)
                                                    <span class="sidebar-badge badge bg-danger">1</span>
                                                @endif
                                            </a>
                                        </li>
                                    @endcan
                                </ul>
                            </li>
                        @endcanany
                    @endcanany
                </ul>
            </div>
        </nav>

        <!-- Content Wrapper -->
        <div class="main">

            <!-- Topbar -->
            <nav class="navbar navbar-expand navbar-bg">
                <a class="sidebar-toggle js-sidebar-toggle">
                    <i class="hamburger align-self-center"></i>
                </a>

                <div class="navbar-collapse collapse">
                    <div class="d-none d-sm-inline-block">
                        <a href="{{ route('apexsions-bridge.admin.server.index') }}" class="btn btn-outline-warning btn-sm mx-1" style="border-color: rgba(201,164,92,0.4); color: #F1D58A;">
                            <i class="bi bi-hdd-network-fill me-1"></i>
                            <span>Server Operations</span>
                        </a>

                        <a href="{{ route('home') }}" class="btn btn-outline-secondary btn-sm mx-1" target="_blank">
                            <i class="bi bi-box-arrow-up-right me-1"></i>
                            <span>View Live Website</span>
                        </a>
                    </div>

                    <!-- Topbar Navbar -->
                    <ul class="navbar-nav navbar-align">
                        <li class="nav-item dropdown">
                            <a class="nav-icon dropdown-toggle" href="#" id="notificationsDropdown" role="button" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                <div class="position-relative">
                                    <!-- Counter - Notifications -->
                                    <i class="bi bi-bell small"></i>
                                    @if(! $notifications->isEmpty())
                                        <span class="indicator" id="notificationsCounter">{{ $notifications->count() }}</span>
                                    @endif
                                </div>
                            </a>

                            <!-- Dropdown - Notifications -->
                            <div class="dropdown-list dropdown-menu dropdown-menu-lg dropdown-menu-end py-0" aria-labelledby="notificationsDropdown">
                                <div class="dropdown-menu-header">
                                    {{ trans('messages.notifications.notifications') }}
                                </div>

                                @if(! $notifications->isEmpty())
                                    <div id="notifications" class="list-group">
                                        @foreach($notifications as $notification)
                                            <a href="{{ $notification->link ? url($notification->link) : '#' }}" class="list-group-item">
                                                <div class="row g-0 align-items-center">
                                                    <div class="col-2 text-{{ $notification->level }}">
                                                        <span class="d-inline-block rounded-circle p-1 border border-{{ $notification->level }}">
                                                            <i class="bi bi-{{ $notification->icon() }} mx-1"></i>
                                                        </span>
                                                    </div>
                                                    <div class="col-10">
                                                        <p>
                                                            {{ $notification->content }}
                                                        </p>
                                                        <small class="text-body-secondary">
                                                            {{ format_date($notification->created_at, true) }}
                                                        </small>
                                                    </div>
                                                </div>
                                            </a>
                                        @endforeach

                                        <div class="dropdown-menu-footer">
                                            <a href="{{ route('notifications.read.all') }}" id="readNotifications" class="text-body-secondary">
                                                <span class="d-none spinner-border spinner-border-sm loader" role="status"></span>
                                                {{ trans('messages.notifications.read') }}
                                            </a>
                                        </div>
                                    </div>
                                @endif

                                <div id="noNotificationsLabel" class="dropdown-menu-footer text-success @if(! $notifications->isEmpty()) d-none @endif">
                                    <i class="bi bi-check-lg"></i> {{ trans('messages.notifications.empty') }}
                                </div>
                            </div>
                        </li>

                        <li class="nav-item">
                            <a href="{{ route('profile.theme') }}" class="nav-icon @if(! dark_theme()) d-none @endif" data-theme-value="light">
                                <i class="bi bi-sun small" title="{{ trans('messages.theme.light') }}" data-bs-toggle="tooltip"></i>
                            </a>
                            <a href="{{ route('profile.theme') }}" class="nav-icon @if(dark_theme()) d-none @endif" data-theme-value="dark">
                                <i class="bi bi-moon-stars small" title="{{ trans('messages.theme.dark') }}" data-bs-toggle="tooltip"></i>
                            </a>
                        </li>

                        <div class="topbar-divider d-none d-sm-block"></div>

                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown" aria-expanded="false">
                                <img class="avatar img-fluid rounded me-1" src="{{ auth()->user()->getAvatar() }}" alt="Avatar">
                                <span class="me-2 d-none d-lg-inline text-gray-600 small">{{ Auth::user()->name }}</span>
                            </a>
                            <!-- Dropdown - User Information -->
                            <div class="dropdown-menu dropdown-menu-end" aria-labelledby="userDropdown">
                                <a class="dropdown-item" href="{{ route('admin.users.edit', Auth::user()) }}">
                                    <i class="bi bi-person-circle me-1"></i>
                                    {{ trans('admin.nav.profile.profile') }}
                                </a>
                                <a class="dropdown-item" href="{{ route('home') }}">
                                    <i class="bi bi-house me-1"></i>
                                    {{ trans('admin.nav.back') }}
                                </a>
                                <div class="dropdown-divider"></div>
                                <a class="dropdown-item" href="{{ route('logout') }}" data-route="logout">
                                    <i class="bi bi-box-arrow-right me-1"></i>
                                    {{ trans('auth.logout') }}
                                </a>
                            </div>
                        </li>

                    </ul>
                </div>

            </nav>

            <main class="content">
                <div class="container-fluid p-0">

                    <h1 class="h3 mb-3">@yield('title', 'Admin')</h1>

                    @include('admin.elements.session-alerts')

                    @yield('content')
                </div>

                <!-- Delete confirm modal -->
                <div class="modal fade" id="confirmDeleteModal" tabindex="-1" role="dialog" aria-labelledby="confirmDeleteLabel" aria-hidden="true">
                    <div class="modal-dialog" role="document">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h2 class="modal-title" id="confirmDeleteLabel">{{ trans('admin.delete.title') }}</h2>
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                            <div class="modal-body">{{ trans('admin.delete.description') }}</div>
                            <div class="modal-footer">
                                <button class="btn btn-secondary" type="button" data-bs-dismiss="modal">
                                    <i class="bi bi-x-lg"></i> {{ trans('messages.actions.cancel') }}
                                </button>
                                <form id="confirmDeleteForm" method="POST">
                                    @method('DELETE')
                                    @csrf

                                    <button class="btn btn-danger" type="submit">
                                        <i class="bi bi-exclamation-triangle"></i> {{ trans('messages.actions.delete') }}
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </main>

            <footer class="footer">
                <div class="container-fluid">
                    <p class="mb-0 py-2 text-center text-body-secondary">
                        @lang('admin.footer', [
                            'year' => '2019-'.now()->year,
                            'azuriom' => '<a href="https://azuriom.com" target="_blank" rel="noopener noreferrer">Azuriom</a>',
                            'startbootstrap' => '<a href="https://adminkit.io/" target="_blank" rel="noopener noreferrer">AdminKit</a>'
                        ])
                    </p>
                </div>
            </footer>
        </div>
    </div>

<form id="logoutForm" action="{{ route('logout') }}" method="POST" class="d-none">
    @csrf
</form>

@stack('footer-scripts')

</body>
</html>
