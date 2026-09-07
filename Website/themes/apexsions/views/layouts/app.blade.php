<!DOCTYPE html>
<html lang="{{ str_replace('_', '-', app()->getLocale()) }}" data-bs-theme="dark">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="{{ csrf_token() }}">

    @php
        $site = 'Apexsions';
        $rawTitle = trim($__env->yieldContent('title'));
        $isHome = request()->routeIs('home') || empty($rawTitle) || $rawTitle === 'Beranda' || $rawTitle === 'Home';

        if ($isHome) {
            $pageTitle = 'Apexsions — Server Minecraft Survival Kerajaan & RPG Indonesia (Java & Bedrock)';
        } else {
            $cleanTitle = preg_replace('/\s*(\||—|-)\s*Apexsions$/i', '', $rawTitle);
            $cleanTitle = preg_replace('/\s*(\||—|-)\s*Webstore Apexsions$/i', ' — Webstore Resmi', $cleanTitle);
            $pageTitle = $cleanTitle . ' | ' . $site;
        }

        $defaultDesc = 'Apexsions adalah server Minecraft Survival Kerajaan & RPG Indonesia terbaik (1.21+ / 26.2). Jelajahi 3 Kerajaan berdaulat (Zenithar, Solterra, Sylvamoor), 11 kasta sosial, ekonomi Rupiah & Diamond, custom enchants, dan crossplay Java & Bedrock. IP Server: apexsions.my.id:32348.';
        $rawDesc = trim($__env->yieldContent('description'));
        if (empty($rawDesc) || $rawDesc === 'The Peak Civilizations' || $rawDesc === setting('description')) {
            $metaDesc = $defaultDesc;
        } else {
            $metaDesc = $rawDesc;
        }
    @endphp

    <title>{{ $pageTitle }}</title>

    <meta name="description" content="{{ $metaDesc }}">
    <meta name="keywords" content="Minecraft Indonesia, server Minecraft survival, server Minecraft kerajaan, Apexsions, Minecraft RPG Indonesia, Minecraft Java Bedrock crossplay, SMP kerajaan, Zenithar, Solterra, Sylvamoor, IP Minecraft server Indonesia, Minecraft 1.21, Minecraft 26.2, server Minecraft terbaik">
    <meta name="author" content="Apexsions">
    <meta name="robots" content="index, follow">
    <meta name="theme-color" content="#090c13">
    <link rel="canonical" href="{{ url()->current() }}">

    <!-- Open Graph & Social Cards -->
    <meta property="og:site_name" content="Apexsions">
    <meta property="og:title" content="{{ $pageTitle }}">
    <meta property="og:description" content="{{ $metaDesc }}">
    <meta property="og:type" content="website">
    <meta property="og:url" content="{{ url()->current() }}">
    <meta property="og:image" content="{{ theme_asset('img/logo.jpg') }}&v={{ @filemtime(public_path('assets/themes/apexsions/img/logo.jpg')) ?: '3' }}">
    <meta property="og:locale" content="id_ID">
    <meta property="og:locale:alternate" content="en_US">

    <!-- Twitter Card -->
    <meta name="twitter:card" content="summary_large_image">
    <meta name="twitter:site" content="@Apexsions">
    <meta name="twitter:title" content="{{ $pageTitle }}">
    <meta name="twitter:description" content="{{ $metaDesc }}">
    <meta name="twitter:image" content="{{ theme_asset('img/logo.jpg') }}&v={{ @filemtime(public_path('assets/themes/apexsions/img/logo.jpg')) ?: '3' }}">
    <link rel="shortcut icon" href="{{ theme_asset('img/favicon.ico') }}&v=3">

    <!-- Schema.org JSON-LD Structured Data for Minecraft Server & WebSite -->
    <script type="application/ld+json">
    {
        "@@context": "https://schema.org",
        "@@graph": [
            {
                "@@type": "WebSite",
                "@@id": "{{ url('/') }}/#website",
                "url": "{{ url('/') }}",
                "name": "Apexsions",
                "description": "The Peak Civilizations — Server Minecraft Survival Kerajaan & RPG Indonesia (Java & Bedrock Crossplay)",
                "inLanguage": ["id", "en"],
                "potentialAction": {
                    "@@type": "SearchAction",
                    "target": "{{ route('home') }}?search={search_term_string}",
                    "query-input": "required name=search_term_string"
                }
            },
            {
                "@@type": "VideoGame",
                "@@id": "{{ url('/') }}/#game",
                "name": "Apexsions — The Peak Civilizations",
                "description": "Server Minecraft Survival Kerajaan, RPG, dan peradaban berdaulat dengan 11 kasta sosial, 3 kerajaan otonom (Zenithar, Solterra, Sylvamoor), ekonomi Rupiah & Diamond, dan perang wilayah mingguan.",
                "genre": ["Survival", "Role-playing video game", "Sandbox", "Multiplayer"],
                "gamePlatform": ["PC / Java Edition", "Mobile / iOS / Android / Bedrock Edition", "Console"],
                "operatingSystem": "Cross-platform",
                "applicationCategory": "GameServer",
                "url": "{{ url('/') }}",
                "image": "{{ theme_asset('img/logo.jpg') }}",
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
    <link rel="stylesheet" href="{{ theme_asset('css/style.css') }}&t={{ @filemtime(public_path('assets/themes/apexsions/css/style.css')) ?: time() }}">
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
</body>
</html>
