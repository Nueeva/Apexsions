<!DOCTYPE html>
<html lang="{{ str_replace('_', '-', app()->getLocale()) }}" data-bs-theme="dark">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="{{ csrf_token() }}">

    <script>
        (function() {
            try {
                var t = localStorage.getItem('apx_theme');
                if (!t) {
                    t = (window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches) ? 'light' : 'dark';
                }
                document.documentElement.setAttribute('data-bs-theme', t);
            } catch (e) {}
        })();
    </script>

    @php
        $site = 'Apexsions';
        $rawTitle = trim($__env->yieldContent('title'));
        $isHome = request()->routeIs('home') || empty($rawTitle) || $rawTitle === 'Beranda' || $rawTitle === 'Home';

        if ($isHome) {
            $pageTitle = 'Apexsions | Minecraft Server Survival & Kingdom Indonesia';
        } elseif (str_contains($rawTitle, 'Apexsions')) {
            $pageTitle = $rawTitle;
        } else {
            $pageTitle = $rawTitle . ' | ' . $site;
        }

        $defaultDesc = 'Apexsions adalah Official Minecraft Server Indonesia (Java & Bedrock). Bergabunglah ke peradaban Survival Kerajaan, 11 kasta sosial, BattlePass, ekonomi realm, dan perang wilayah.';
        $rawDesc = trim($__env->yieldContent('description'));
        if (empty($rawDesc) || $rawDesc === 'The Peak Civilizations' || $rawDesc === setting('description')) {
            $metaDesc = $defaultDesc;
        } else {
            $metaDesc = $rawDesc;
        }

        // Authoritative Canonical Base URL (Strict HTTPS web.apexsions.my.id)
        $canonicalBase = 'https://web.apexsions.my.id';
        $pathInfo = request()->getPathInfo();
        $canonicalUrl = $canonicalBase . ($pathInfo === '/' ? '' : $pathInfo);
    @endphp

    <title>{{ $pageTitle }}</title>

    <meta name="description" content="{{ $metaDesc }}">
    <meta name="keywords" content="Apexsions, Apexsions Minecraft Server, Apexsions Minecraft, Server Minecraft Indonesia, Minecraft Survival Indonesia, Minecraft SMP Indonesia, Minecraft Java Bedrock Indonesia, Server Minecraft Survival">
    <meta name="author" content="Apexsions">
    <meta name="robots" content="index, follow">
    <meta name="theme-color" content="#090c13">
    <link rel="canonical" href="{{ $canonicalUrl }}">

    <!-- Open Graph & Social Cards -->
    <meta property="og:site_name" content="Apexsions">
    <meta property="og:title" content="{{ $pageTitle }}">
    <meta property="og:description" content="{{ $metaDesc }}">
    <meta property="og:type" content="website">
    <meta property="og:url" content="{{ $canonicalUrl }}">
    <meta property="og:image" content="{{ theme_asset('img/og-preview.jpg') }}&v={{ @filemtime(public_path('assets/themes/apexsions/img/og-preview.jpg')) ?: '4' }}">
    <meta property="og:image:width" content="1200">
    <meta property="og:image:height" content="630">
    <meta property="og:image:alt" content="Apexsions - The Peak Civilizations Minecraft Indonesia">
    <meta property="og:locale" content="id_ID">
    <meta property="og:locale:alternate" content="en_US">

    <!-- Twitter Card -->
    <meta name="twitter:card" content="summary_large_image">
    <meta name="twitter:site" content="@Apexsions">
    <meta name="twitter:title" content="{{ $pageTitle }}">
    <meta name="twitter:description" content="{{ $metaDesc }}">
    <meta name="twitter:image" content="{{ theme_asset('img/og-preview.jpg') }}&v={{ @filemtime(public_path('assets/themes/apexsions/img/og-preview.jpg')) ?: '4' }}">
    <link rel="shortcut icon" href="{{ theme_asset('img/favicon.ico') }}&v=3">

    <!-- Schema.org JSON-LD Structured Data for Organization, WebSite & VideoGameServer -->
    <script type="application/ld+json">
    {
        "@@context": "https://schema.org",
        "@@graph": [
            {
                "@@type": "Organization",
                "@@id": "{{ $canonicalBase }}/#organization",
                "name": "Apexsions",
                "alternateName": [
                    "Apexsions Minecraft Server",
                    "Apexsions Indonesia",
                    "Apexsions Minecraft",
                    "Apexsions SMP",
                    "Apexsions Kingdom"
                ],
                "disambiguatingDescription": "Apexsions adalah Official Game Server Minecraft Indonesia bertema Survival Kerajaan (The Peak Civilizations), bukan penyedia server web hosting.",
                "url": "{{ $canonicalBase }}",
                "logo": "{{ theme_asset('img/logo.png') }}",
                "description": "Apexsions - The Peak Civilizations. Server Minecraft Survival & Kingdom Server Indonesia.",
                "sameAs": [
                    "https://discord.gg/apexsions",
                    "https://minecraft-mp.com/server/363636/"
                ]
            },
            {
                "@@type": "WebSite",
                "@@id": "{{ $canonicalBase }}/#website",
                "url": "{{ $canonicalBase }}",
                "name": "Apexsions Minecraft Server",
                "alternateName": "Apexsions",
                "description": "Apexsions - Official Minecraft Survival & Kingdom Server Indonesia",
                "publisher": {
                    "@@id": "{{ $canonicalBase }}/#organization"
                },
                "inLanguage": ["id", "en"],
                "potentialAction": {
                    "@@type": "SearchAction",
                    "target": "{{ $canonicalBase }}/?search={search_term_string}",
                    "query-input": "required name=search_term_string"
                }
            },
            {
                "@@type": ["VideoGame", "SoftwareApplication"],
                "@@id": "{{ $canonicalBase }}/#game",
                "name": "Apexsions — Minecraft Server Survival & Kingdom Indonesia",
                "alternateName": "Apexsions Minecraft Server",
                "description": "Server Minecraft Survival Kerajaan, RPG, dan peradaban berdaulat dengan 11 kasta sosial, 3 kerajaan otonom (Zenithar, Solterra, Sylvamoor), ekonomi Rupiah & Diamond, dan perang wilayah mingguan.",
                "genre": ["Survival", "Role-playing video game", "Sandbox", "Multiplayer"],
                "gamePlatform": ["PC / Java Edition (1.20 - 1.21.x)", "Mobile / iOS / Android / Bedrock Edition", "Console"],
                "operatingSystem": "Cross-platform Java & Bedrock",
                "applicationCategory": "GameServer",
                "url": "{{ $canonicalBase }}",
                "image": "{{ theme_asset('img/og-preview.jpg') }}",
                "publisher": {
                    "@@id": "{{ $canonicalBase }}/#organization"
                },
                "aggregateRating": {
                    "@@type": "AggregateRating",
                    "ratingValue": "4.9",
                    "reviewCount": "128"
                },
                "offers": {
                    "@@type": "Offer",
                    "price": "0",
                    "priceCurrency": "IDR",
                    "availability": "https://schema.org/InStock"
                }
            }
        ]
    }
    </script>

    <!-- Google Fonts: Cinzel (Majestic Display) & Plus Jakarta Sans (Clean Interface) -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Cinzel:wght@600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <!-- Bootstrap 5 CSS & Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

    <!-- Apexsions Custom Theme CSS -->
    <link rel="stylesheet" href="{{ theme_asset('css/style.css') }}&v=20260911_lightfix_{{ @filemtime(public_path('assets/themes/apexsions/css/style.css')) ?: time() }}">
    @stack('styles')
</head>
<body class="apx-body">
    <!-- Apexsions Inter-Page Cinematic Transition Overlay -->
    <div id="apxPageTransition" class="apx-page-transition is-entering" aria-hidden="true">
        <div class="apx-transition-backdrop"></div>
        <div class="apx-transition-content">
            <div class="apx-transition-crest-halo"></div>
            <div class="apx-transition-crest-wrap">
                <img src="{{ theme_asset('img/logo.png') }}&v={{ @filemtime(public_path('assets/themes/apexsions/img/logo.png')) ?: '3' }}" alt="Apexsions" class="apx-transition-logo" width="96" height="96">
            </div>
            <div class="apx-transition-brand">
                <span class="apx-transition-title">APEXSIONS</span>
                <span class="apx-transition-tagline">THE PEAK CIVILIZATIONS</span>
                <div class="apx-transition-bar">
                    <div class="apx-transition-bar-fill"></div>
                </div>
            </div>
        </div>
    </div>
    <script>
        (function() {
            function apxDismiss() {
                var el = document.getElementById('apxPageTransition');
                if (el) {
                    el.classList.remove('is-navigating', 'is-entering');
                    el.classList.add('is-loaded');
                }
            }
            if (document.readyState === 'interactive' || document.readyState === 'complete') {
                setTimeout(apxDismiss, 60);
            } else {
                document.addEventListener('DOMContentLoaded', function() { setTimeout(apxDismiss, 60); });
                window.addEventListener('load', apxDismiss);
            }
            setTimeout(apxDismiss, 400);
        })();
    </script>

    <!-- Skip to Main Content (WCAG 2.4.1 Bypass Blocks) -->
    <a href="#main-content" class="apx-skip-link">Lewati ke konten utama</a>

    @include('elements.navbar')

    <main class="flex-grow-1" id="main-content" tabindex="-1">
        <div id="status-message"></div>

        @if(request()->routeIs('home'))
            @if(session('success'))
                <div class="container mt-4">
                    <div class="alert alert-success alert-dismissible fade show border-0 shadow-sm" role="alert">
                        <i class="bi bi-check-circle-fill me-2"></i> {{ session('success') }}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Tutup"></button>
                    </div>
                </div>
            @endif

            @if(session('error'))
                <div class="container mt-4">
                    <div class="alert alert-danger alert-dismissible fade show border-0 shadow-sm" role="alert">
                        <i class="bi bi-exclamation-octagon-fill me-2"></i> {{ session('error') }}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Tutup"></button>
                    </div>
                </div>
            @endif

            @yield('content')
        @else
            <div class="container content py-5">
                @if(session('success'))
                    <div class="alert alert-success alert-dismissible fade show border-0 shadow-sm mb-4" role="alert">
                        <i class="bi bi-check-circle-fill me-2"></i> {{ session('success') }}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Tutup"></button>
                    </div>
                @endif

                @if(session('error'))
                    <div class="alert alert-danger alert-dismissible fade show border-0 shadow-sm mb-4" role="alert">
                        <i class="bi bi-exclamation-octagon-fill me-2"></i> {{ session('error') }}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Tutup"></button>
                    </div>
                @endif

                @yield('content')
            </div>
        @endif
    </main>

    @include('elements.footer')

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="{{ theme_asset('js/app.js') }}&t={{ @filemtime(public_path('assets/themes/apexsions/js/app.js')) ?: time() }}"></script>
    @stack('scripts')
    @stack('footer-scripts')
</body>
</html>
