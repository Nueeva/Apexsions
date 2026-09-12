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
                        <div class="d-flex align-items-center justify-content-center mb-1">
                            <img src="{{ asset('assets/themes/apexsions/img/logo-ornate-gold.png') }}" alt="Apexsions" style="height: 38px; max-height: 44px; width: auto; filter: drop-shadow(0 0 10px rgba(201, 164, 92, 0.45));">
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
                    <li class="sidebar-item {{ add_active('apexsions-bridge.admin.votes.*') }}">
                        <a class="sidebar-link" href="{{ route('apexsions-bridge.admin.votes.index') }}">
                            <i class="bi bi-patch-check-fill"></i>
                            <span>Vote Management</span>
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
                                    <span>Web Staff Accounts</span>
                                </a>
                                <ul id="collapseWebUsers" class="sidebar-dropdown list-unstyled collapse {{ Route::is('admin.users.*', 'admin.roles.*') ? 'show' : ''}}" data-parent="#accordionSidebar">
                                    @can('admin.users')
                                        <li class="sidebar-item {{ add_active('admin.users.*') }}">
                                            <a class="sidebar-link" href="{{ route('admin.users.index') }}">
                                                <i class="bi bi-person-badge me-1"></i> Staff Web CMS
                                            </a>
                                        </li>
                                    @endcan
                                    @can('admin.roles')
                                        <li class="sidebar-item {{ add_active('admin.roles.*') }}">
                                            <a class="sidebar-link" href="{{ route('admin.roles.index') }}">
                                                <i class="bi bi-shield-lock me-1"></i> Staff Web Roles
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
                    <div class="d-none d-sm-flex align-items-center gap-2">
                        <!-- Global Search Widget -->
                        <div class="position-relative" style="width: 270px;">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text bg-transparent border-end-0" style="border-color: var(--apx-border-subtle); color: var(--apx-gold);">
                                    <i class="bi bi-search"></i>
                                </span>
                                <input type="text" id="apxGlobalSearchInput" class="form-control form-control-sm border-start-0 border-end-0" placeholder="Search player, UUID, menu... (Ctrl+K)" autocomplete="off" style="border-color: var(--apx-border-subtle);">
                                <span class="input-group-text bg-transparent border-start-0" style="border-color: var(--apx-border-subtle);">
                                    <kbd class="small text-muted py-0 px-1" style="font-size: 0.65rem; background: var(--apx-bg-surface); border: 1px solid var(--apx-border-subtle);">Ctrl K</kbd>
                                </span>
                            </div>
                            <div id="apxGlobalSearchResults" class="apx-search-dropdown shadow-lg d-none"></div>
                        </div>

                        <!-- Quick Actions Button -->
                        <button type="button" class="btn btn-outline-warning btn-sm" data-bs-toggle="modal" data-bs-target="#quickActionsModal" title="Quick Actions Command Palette">
                            <i class="bi bi-lightning-charge-fill me-1"></i> Quick Actions
                        </button>

                        <a href="{{ route('home') }}" class="btn btn-outline-secondary btn-sm" target="_blank" title="View live public website">
                            <i class="bi bi-box-arrow-up-right me-1"></i> Live Web
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

<!-- Quick Actions Command Palette Modal -->
<div class="modal fade" id="quickActionsModal" tabindex="-1" aria-labelledby="quickActionsModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <div class="d-flex align-items-center gap-2">
                    <div class="p-2 rounded apx-icon-box">
                        <i class="bi bi-lightning-charge-fill text-warning fs-5"></i>
                    </div>
                    <div>
                        <h5 class="modal-title fw-bold mb-0" id="quickActionsModalLabel">Quick Actions Command Palette</h5>
                        <small class="text-muted">Aksi administratif cepat untuk pengelolaan realm Apexsions</small>
                    </div>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body p-4">
                <!-- Fast Player Lookup Form -->
                <div class="mb-4 p-3 rounded apx-gauge-card">
                    <label class="form-label small fw-bold text-uppercase text-muted mb-1">
                        <i class="bi bi-person-bounding-box text-warning me-1"></i> Lompat Langsung ke Dossier Pemain
                    </label>
                    <form id="quickPlayerJumpForm" onsubmit="event.preventDefault(); const u = document.getElementById('quickPlayerUsername').value.trim(); if(u) window.location.href='{{ url('/admin/apexsions/players') }}/' + encodeURIComponent(u);">
                        <div class="input-group">
                            <input type="text" id="quickPlayerUsername" class="form-control" placeholder="Ketik Minecraft Username atau UUID...">
                            <button class="btn btn-primary" type="submit">
                                <i class="bi bi-arrow-right-circle me-1"></i> Buka Dossier
                            </button>
                        </div>
                    </form>
                </div>

                <!-- Action Tiles Grid -->
                <div class="row g-3">
                    <!-- Tile 1: Server Maintenance Toggle -->
                    <div class="col-md-6">
                        <div class="p-3 rounded h-100 d-flex align-items-center justify-content-between apx-gauge-card">
                            <div>
                                <div class="fw-bold"><i class="bi bi-cone-striped text-warning me-2"></i> Mode Maintenance</div>
                                <small class="text-muted">Kunci akses masuk server Minecraft</small>
                            </div>
                            <form action="{{ route('apexsions-bridge.admin.server.maintenance.toggle') }}" method="POST">
                                @csrf
                                <button type="submit" class="btn btn-sm btn-outline-warning">
                                    <i class="bi bi-power me-1"></i> Toggle
                                </button>
                            </form>
                        </div>
                    </div>

                    <!-- Tile 2: Clear Global Chat -->
                    <div class="col-md-6">
                        <div class="p-3 rounded h-100 d-flex align-items-center justify-content-between apx-gauge-card">
                            <div>
                                <div class="fw-bold"><i class="bi bi-chat-square-dots text-info me-2"></i> Bersihkan Chat In-game</div>
                                <small class="text-muted">Kirim 100 baris kosong ke publik</small>
                            </div>
                            <form action="{{ route('apexsions-bridge.admin.server.actions.execute') }}" method="POST">
                                @csrf
                                <input type="hidden" name="action" value="CHAT_CLEAR">
                                <button type="submit" class="btn btn-sm btn-outline-info" onclick="return confirm('Bersihkan riwayat in-game chat untuk seluruh pemain online?')">
                                    <i class="bi bi-eraser-fill me-1"></i> Clear Chat
                                </button>
                            </form>
                        </div>
                    </div>

                    <!-- Tile 3: Mute All Chat -->
                    <div class="col-md-6">
                        <div class="p-3 rounded h-100 d-flex align-items-center justify-content-between apx-gauge-card">
                            <div>
                                <div class="fw-bold"><i class="bi bi-mic-mute text-danger me-2"></i> Heningkan Chat Global</div>
                                <small class="text-muted">Kunci chat publik saat darurat</small>
                            </div>
                            <form action="{{ route('apexsions-bridge.admin.server.actions.execute') }}" method="POST">
                                @csrf
                                <input type="hidden" name="action" value="CHAT_MUTE">
                                <button type="submit" class="btn btn-sm btn-outline-danger" onclick="return confirm('Aktifkan mode senyap chat global?')">
                                    <i class="bi bi-volume-mute-fill me-1"></i> Mute Chat
                                </button>
                            </form>
                        </div>
                    </div>

                    <!-- Tile 4: Auction Expiry Sweep -->
                    <div class="col-md-6">
                        <div class="p-3 rounded h-100 d-flex align-items-center justify-content-between apx-gauge-card">
                            <div>
                                <div class="fw-bold"><i class="bi bi-shop-window text-success me-2"></i> Sweep Lelang Kadaluarsa</div>
                                <small class="text-muted">Kembalikan item lelang kadaluarsa</small>
                            </div>
                            <form action="{{ route('apexsions-bridge.admin.server.actions.execute') }}" method="POST">
                                @csrf
                                <input type="hidden" name="action" value="AH_PURGE">
                                <button type="submit" class="btn btn-sm btn-outline-success">
                                    <i class="bi bi-arrow-repeat me-1"></i> Sweep AH
                                </button>
                            </form>
                        </div>
                    </div>

                    <!-- Tile 5: Kingdom War Status -->
                    <div class="col-md-6">
                        <div class="p-3 rounded h-100 d-flex align-items-center justify-content-between apx-gauge-card">
                            <div>
                                <div class="fw-bold"><i class="bi bi-shield-slash text-danger me-2"></i> Perang Kerajaan</div>
                                <small class="text-muted">Toggle status Kingdom War</small>
                            </div>
                            <form action="{{ route('apexsions-bridge.admin.server.actions.execute') }}" method="POST">
                                @csrf
                                <input type="hidden" name="action" value="KINGDOM_WAR_TOGGLE">
                                <button type="submit" class="btn btn-sm btn-outline-danger" onclick="return confirm('Ubah status Kingdom War?')">
                                    <i class="bi bi-swords me-1"></i> War Toggle
                                </button>
                            </form>
                        </div>
                    </div>

                    <!-- Tile 6: Unified Audit Logs Shortcut -->
                    <div class="col-md-6">
                        <div class="p-3 rounded h-100 d-flex align-items-center justify-content-between apx-gauge-card">
                            <div>
                                <div class="fw-bold"><i class="bi bi-journal-text text-warning me-2"></i> Unified Audit Logs</div>
                                <small class="text-muted">Periksa rekam jejak aksi staff</small>
                            </div>
                            <a href="{{ route('apexsions-bridge.admin.audit-logs.index') }}" class="btn btn-sm btn-outline-warning">
                                <i class="bi bi-box-arrow-up-right me-1"></i> Buka Logs
                            </a>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Tutup</button>
            </div>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('apxGlobalSearchInput');
    const resultsContainer = document.getElementById('apxGlobalSearchResults');
    let debounceTimeout = null;

    if (!searchInput || !resultsContainer) return;

    // Keyboard shortcut: Ctrl+K or '/'
    document.addEventListener('keydown', function(e) {
        if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
            e.preventDefault();
            searchInput.focus();
            searchInput.select();
        } else if (e.key === '/' && document.activeElement.tagName !== 'INPUT' && document.activeElement.tagName !== 'TEXTAREA') {
            e.preventDefault();
            searchInput.focus();
            searchInput.select();
        } else if (e.key === 'Escape') {
            resultsContainer.classList.add('d-none');
            searchInput.blur();
        }
    });

    searchInput.addEventListener('input', function() {
        clearTimeout(debounceTimeout);
        const query = this.value.trim();

        if (query.length < 2) {
            resultsContainer.classList.add('d-none');
            resultsContainer.innerHTML = '';
            return;
        }

        debounceTimeout = setTimeout(async () => {
            try {
                resultsContainer.innerHTML = '<div class="p-3 text-center text-muted small"><span class="spinner-border spinner-border-sm me-2"></span>Mencari...</div>';
                resultsContainer.classList.remove('d-none');

                const res = await fetch('{{ route('apexsions-bridge.admin.global-search') }}?q=' + encodeURIComponent(query), {
                    headers: { 'Accept': 'application/json' }
                });
                if (!res.ok) throw new Error('HTTP ' + res.status);
                const data = await res.json();

                let html = '';
                let hasResults = false;

                // Players
                if (data.players && data.players.length > 0) {
                    hasResults = true;
                    html += '<div class="apx-search-category"><i class="bi bi-people-fill me-1 text-warning"></i> Pemain In-Game</div>';
                    data.players.forEach(p => {
                        html += `
                            <a href="${p.url}" class="apx-search-item">
                                <img src="${p.avatar}" width="20" height="20" class="rounded" onerror="this.src='{{ asset('assets/themes/apexsions/img/favicon.ico') }}'">
                                <div class="text-truncate">
                                    <div class="fw-bold text-truncate">${p.title}</div>
                                    <span class="apx-search-meta text-truncate">${p.subtitle}</span>
                                </div>
                            </a>
                        `;
                    });
                }

                // Navigation
                if (data.navigation && data.navigation.length > 0) {
                    hasResults = true;
                    html += '<div class="apx-search-category"><i class="bi bi-compass-fill me-1 text-warning"></i> Menu & Navigasi</div>';
                    data.navigation.forEach(n => {
                        html += `
                            <a href="${n.url}" class="apx-search-item">
                                <i class="${n.icon}"></i>
                                <div class="text-truncate">
                                    <div class="fw-bold text-truncate">${n.title}</div>
                                    <span class="apx-search-meta text-truncate">${n.subtitle}</span>
                                </div>
                            </a>
                        `;
                    });
                }

                // Reports
                if (data.reports && data.reports.length > 0) {
                    hasResults = true;
                    html += '<div class="apx-search-category"><i class="bi bi-flag-fill me-1 text-danger"></i> Laporan Pelanggaran</div>';
                    data.reports.forEach(r => {
                        html += `
                            <a href="${r.url}" class="apx-search-item">
                                <i class="${r.icon} text-danger"></i>
                                <div class="text-truncate">
                                    <div class="fw-bold text-truncate">${r.title}</div>
                                    <span class="apx-search-meta text-truncate">${r.subtitle}</span>
                                </div>
                            </a>
                        `;
                    });
                }

                if (!hasResults) {
                    html = '<div class="p-3 text-center text-muted small"><i class="bi bi-search me-1"></i> Tidak ditemukan hasil untuk "<strong>' + query.replace(/</g, '&lt;').replace(/>/g, '&gt;') + '</strong>"</div>';
                }

                resultsContainer.innerHTML = html;
                resultsContainer.classList.remove('d-none');
            } catch (err) {
                console.error('[GlobalSearch] Error:', err);
                resultsContainer.innerHTML = '<div class="p-3 text-center text-danger small"><i class="bi bi-exclamation-triangle me-1"></i> Gagal memuat pencarian</div>';
            }
        }, 250);
    });

    // Close dropdown on click outside
    document.addEventListener('click', function(e) {
        if (!searchInput.contains(e.target) && !resultsContainer.contains(e.target)) {
            resultsContainer.classList.add('d-none');
        }
    });
});
</script>

@stack('footer-scripts')

</body>
</html>
