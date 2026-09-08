@extends('admin.layouts.admin')

@section('title', trans('admin.dashboard.title'))

@section('content')
    @if(! $secure)
        <div id="notHttpsAlert" class="alert alert-warning shadow-sm" role="alert">
            <i class="bi bi-exclamation-triangle"></i> {{ trans('admin.dashboard.http') }}
        </div>
        <div id="proxyAlert" class="alert alert-info shadow-sm d-none" role="alert">
            <i class="bi bi-info-circle"></i> {{ trans('admin.dashboard.cloudflare') }}
        </div>
    @endif

    @if(config('mail.default') === 'array')
        <div class="alert alert-warning shadow-sm" role="alert">
            <i class="bi bi-info-circle"></i> @lang('admin.dashboard.emails', ['url' => route('admin.settings.mail')])
        </div>
    @endif

    @if($newVersion !== null)
        <div class="alert alert-info shadow-sm" role="alert">
            <i class="bi bi-plus-lg"></i> {{ trans('admin.dashboard.update', ['version' => $newVersion]) }}.
            <a href="{{ route('admin.update.index') }}">
                {{ trans('messages.actions.install') }}
            </a>.
        </div>
    @endif

    @foreach($apiAlerts as $alertLevel => $alertMessage)
        <div class="alert alert-{{ $alertLevel }} shadow-sm" role="alert">
            {!! $alertMessage !!}
        </div>
    @endforeach

    <!-- =========================================================================
         APEXSIONS REALM COMMAND CORE: LIVE SERVER HEALTH & TELEMETRY MONITOR
         ========================================================================= -->
    @php
        $initialStatus = Cache::get('apexsions.server_status') ?? [
            'online' => false,
            'players' => 0,
            'max_players' => 500,
            'player_list' => [],
            'tps' => 20.0,
            'version' => '26.2',
            'ram_used_mb' => 0,
            'ram_max_mb' => 0,
            'free_ram_mb' => 0,
            'uptime_seconds' => 0,
            'loaded_chunks' => 0,
            'entities' => 0,
            'last_heartbeat' => null,
        ];
    @endphp

    <div class="card mb-4" style="border: 1px solid rgba(201, 164, 92, 0.35); box-shadow: 0 10px 30px rgba(0,0,0,0.85); background: linear-gradient(180deg, #161922 0%, #111319 100%);">
        <div class="card-header d-flex flex-wrap align-items-center justify-content-between gap-2" style="background: rgba(201, 164, 92, 0.08); border-bottom: 1px solid rgba(201, 164, 92, 0.22); padding: 1.1rem 1.4rem;">
            <div class="d-flex align-items-center gap-3">
                <div class="p-2 rounded" style="background: rgba(201, 164, 92, 0.15); border: 1px solid rgba(201, 164, 92, 0.3);">
                    <i class="bi bi-cpu-fill text-warning fs-4"></i>
                </div>
                <div>
                    <h4 class="mb-0 fw-bold" style="font-family: 'Cinzel', serif; letter-spacing: 1.5px; color: #F1D58A;">
                        APEXSIONS REALM TELEMETRY & LIVE MONITOR
                    </h4>
                    <small class="text-muted" style="letter-spacing: 0.5px;">
                        Pengawasan Performa Server &middot; Runtime Paper 26.2 &middot; IP: <code class="text-warning">apexsions.my.id:32348</code>
                    </small>
                </div>
            </div>

            <div class="d-flex align-items-center gap-2">
                <span id="realmLiveStatusBadge" class="badge {{ ($initialStatus['online'] ?? false) ? 'bg-success' : 'bg-danger' }} px-3 py-2" style="font-size: 0.78rem; letter-spacing: 1px;">
                    <i class="bi bi-circle-fill me-1" style="font-size: 0.65rem;"></i>
                    <span id="realmLiveStatusText">{{ ($initialStatus['online'] ?? false) ? 'REALM ONLINE' : 'SERVER OFFLINE' }}</span>
                </span>
                <span id="realmLiveUptimeBadge" class="badge bg-dark border border-secondary px-3 py-2 text-light" style="font-size: 0.78rem;">
                    <i class="bi bi-stopwatch me-1 text-warning"></i>
                    <span id="realmLiveUptimeText">Uptime: --</span>
                </span>
                <button type="button" class="btn btn-sm btn-outline-primary px-3 py-1" id="btnRefreshTelemetry" title="Perbarui Data Realtime">
                    <i class="bi bi-arrow-clockwise me-1" id="refreshIcon"></i> Perbarui
                </button>
            </div>
        </div>

        <div class="card-body p-4">
            <!-- 4 Core Metric Gauges -->
            <div class="row g-3 mb-4">
                <!-- TPS Metric -->
                <div class="col-sm-6 col-xl-3">
                    <div class="p-3 rounded h-100" style="background: rgba(0,0,0,0.35); border: 1px solid rgba(255,255,255,0.06);">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="text-muted small text-uppercase fw-bold" style="letter-spacing: 1px;">Tick Stability</span>
                            <i class="bi bi-speedometer2 text-warning fs-5"></i>
                        </div>
                        <div class="d-flex align-items-baseline gap-2">
                            <h2 class="mb-0 fw-bold" id="gaugeTps" style="color: #F4EFE6;">{{ number_format($initialStatus['tps'] ?? 20.0, 1) }}</h2>
                            <small class="text-muted">TPS</small>
                        </div>
                        <div class="progress mt-2" style="height: 6px; background: rgba(255,255,255,0.1);">
                            <div id="gaugeTpsBar" class="progress-bar bg-success" role="progressbar" style="width: 100%;"></div>
                        </div>
                        <small class="d-block mt-2 text-muted" id="gaugeTpsLabel" style="font-size: 0.75rem;">
                            Stabilitas Engine: 100% (Target: 20.0)
                        </small>
                    </div>
                </div>

                <!-- RAM Metric -->
                <div class="col-sm-6 col-xl-3">
                    <div class="p-3 rounded h-100" style="background: rgba(0,0,0,0.35); border: 1px solid rgba(255,255,255,0.06);">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="text-muted small text-uppercase fw-bold" style="letter-spacing: 1px;">Alokasi RAM JVM</span>
                            <i class="bi bi-memory text-info fs-5"></i>
                        </div>
                        <div class="d-flex align-items-baseline gap-2">
                            <h2 class="mb-0 fw-bold" id="gaugeRam" style="color: #F4EFE6;">{{ $initialStatus['ram_used_mb'] ?? 0 }} MB</h2>
                        </div>
                        <div class="progress mt-2" style="height: 6px; background: rgba(255,255,255,0.1);">
                            <div id="gaugeRamBar" class="progress-bar bg-info" role="progressbar" style="width: 25%;"></div>
                        </div>
                        <small class="d-block mt-2 text-muted" id="gaugeRamLabel" style="font-size: 0.75rem;">
                            Batas Alokasi: {{ $initialStatus['ram_max_mb'] ?? 0 }} MB
                        </small>
                    </div>
                </div>

                <!-- World Activity Metric -->
                <div class="col-sm-6 col-xl-3">
                    <div class="p-3 rounded h-100" style="background: rgba(0,0,0,0.35); border: 1px solid rgba(255,255,255,0.06);">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="text-muted small text-uppercase fw-bold" style="letter-spacing: 1px;">Aktivitas Dunia</span>
                            <i class="bi bi-boxes text-success fs-5"></i>
                        </div>
                        <div class="d-flex align-items-baseline gap-2">
                            <h2 class="mb-0 fw-bold" id="gaugeChunks" style="color: #F4EFE6;">{{ $initialStatus['loaded_chunks'] ?? 0 }}</h2>
                            <small class="text-muted">Chunks</small>
                        </div>
                        <div class="progress mt-2" style="height: 6px; background: rgba(255,255,255,0.1);">
                            <div class="progress-bar bg-success" role="progressbar" style="width: 50%;"></div>
                        </div>
                        <small class="d-block mt-2 text-muted" id="gaugeEntitiesLabel" style="font-size: 0.75rem;">
                            Entitas Aktif: {{ $initialStatus['entities'] ?? 0 }} entitas
                        </small>
                    </div>
                </div>

                <!-- Online Players Metric -->
                <div class="col-sm-6 col-xl-3">
                    <div class="p-3 rounded h-100" style="background: rgba(0,0,0,0.35); border: 1px solid rgba(255,255,255,0.06);">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="text-muted small text-uppercase fw-bold" style="letter-spacing: 1px;">Populasi Online</span>
                            <i class="bi bi-people text-warning fs-5"></i>
                        </div>
                        <div class="d-flex align-items-baseline gap-2">
                            <h2 class="mb-0 fw-bold" id="gaugePlayers" style="color: #F4EFE6;">{{ $initialStatus['players'] ?? 0 }} <span style="font-size: 1rem; color: #8E877D;">/ {{ $initialStatus['max_players'] ?? 500 }}</span></h2>
                        </div>
                        <div class="progress mt-2" style="height: 6px; background: rgba(255,255,255,0.1);">
                            <div id="gaugePlayersBar" class="progress-bar bg-warning" role="progressbar" style="width: 5%;"></div>
                        </div>
                        <small class="d-block mt-2 text-muted" id="gaugePlayersLabel" style="font-size: 0.75rem;">
                            Kapasitas Realm: 500 Slot
                        </small>
                    </div>
                </div>
            </div>

            <!-- Player Roster & Broadcast Deck -->
            <div class="row g-4">
                <!-- Live Online Player Roster -->
                <div class="col-lg-7">
                    <div class="p-3 rounded" style="background: rgba(0,0,0,0.25); border: 1px solid rgba(201, 164, 92, 0.18);">
                        <div class="d-flex align-items-center justify-content-between mb-3">
                            <h6 class="mb-0 fw-bold text-uppercase" style="color: #E8C878; font-size: 0.8rem; letter-spacing: 1px;">
                                <i class="bi bi-person-lines-fill me-2 text-warning"></i>
                                Warga Peradaban yang Sedang Online
                            </h6>
                            <span class="badge bg-secondary" id="rosterCountBadge">0 Pemain</span>
                        </div>

                        <div id="onlinePlayersContainer" class="d-flex flex-wrap gap-2 align-items-center" style="min-height: 50px;">
                            <div class="p-3 text-center text-muted w-100" id="emptyPlayersNotice" style="font-size: 0.85rem;">
                                <i class="bi bi-moon-stars me-2"></i> Belum ada pemain yang online di dalam Realm saat ini.
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Web-to-Game Quick Broadcast -->
                <div class="col-lg-5">
                    <div class="p-3 rounded h-100 d-flex flex-column justify-content-between" style="background: rgba(0,0,0,0.25); border: 1px solid rgba(201, 164, 92, 0.18);">
                        <div>
                            <h6 class="mb-2 fw-bold text-uppercase" style="color: #E8C878; font-size: 0.8rem; letter-spacing: 1px;">
                                <i class="bi bi-megaphone-fill me-2 text-warning"></i>
                                Kirim Pengumuman Global In-game
                            </h6>
                            <p class="text-muted small mb-3" style="font-size: 0.78rem;">
                                Broadcast pesan emas resmi dari Web Admin langsung ke seluruh layar pemain yang sedang berada di dalam server Minecraft.
                            </p>
                            <form id="formAdminBroadcast">
                                @csrf
                                <div class="input-group mb-2">
                                    <input type="text" id="broadcastMessageInput" class="form-control" placeholder="Tulis pengumuman resmi server..." maxlength="250" required>
                                    <button class="btn btn-primary" type="submit" id="btnSubmitBroadcast">
                                        <i class="bi bi-send-fill me-1"></i> Kirim
                                    </button>
                                </div>
                            </form>
                        </div>
                        <div id="broadcastFeedback" class="small mt-2 d-none"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Content Row -->
    <div class="row">

        <div class="col-sm-6 col-xl-3">
            <div class="card">
                <div class="card-body">
                    <div class="row">
                        <div class="col mt-0">
                            <h5 class="card-title mb-0">{{ trans('admin.dashboard.users') }}</h5>
                        </div>

                        <div class="col-auto">
                            <div class="stat text-primary h3">
                                <i class="bi bi-people"></i>
                            </div>
                        </div>
                    </div>
                    <h1 class="mt-1 mb-3">{{ $userCount }}</h1>
                </div>
            </div>
        </div>

        <div class="col-sm-6 col-xl-3">
            <div class="card">
                <div class="card-body">
                    <div class="row">
                        <div class="col mt-0">
                            <h5 class="card-title mb-0">{{ trans('admin.dashboard.posts') }}</h5>
                        </div>

                        <div class="col-auto">
                            <div class="stat text-primary h3">
                                <i class="bi bi-newspaper"></i>
                            </div>
                        </div>
                    </div>
                    <h1 class="mt-1 mb-3">{{ $postCount }}</h1>
                </div>
            </div>
        </div>

        <div class="col-sm-6 col-xl-3">
            <div class="card">
                <div class="card-body">
                    <div class="row">
                        <div class="col mt-0">
                            <h5 class="card-title mb-0">{{ trans('admin.dashboard.pages') }}</h5>
                        </div>

                        <div class="col-auto">
                            <div class="stat text-primary h3">
                                <i class="bi bi-file-earmark"></i>
                            </div>
                        </div>
                    </div>
                    <h1 class="mt-1 mb-3">{{ $pageCount }}</h1>
                </div>
            </div>
        </div>

        <div class="col-sm-6 col-xl-3">
            <div class="card">
                <div class="card-body">
                    <div class="row">
                        <div class="col mt-0">
                            <h5 class="card-title mb-0">{{ trans('admin.dashboard.images') }}</h5>
                        </div>

                        <div class="col-auto">
                            <div class="stat text-primary h3">
                                <i class="bi bi-image"></i>
                            </div>
                        </div>
                    </div>
                    <h1 class="mt-1 mb-3">{{ $imageCount }}</h1>
                </div>
            </div>
        </div>

        @foreach($cards ?? [] as $card)
            <div class="col-sm-6 col-xl-3">
                <div class="card">
                    <div class="card-body">
                        <div class="row">
                            <div class="col mt-0">
                                <h5 class="card-title mb-0">{{ $card['name'] }}</h5>
                            </div>

                            <div class="col-auto">
                                <div class="stat text-primary h3">
                                    <i class="{{ $card['icon'] }}"></i>
                                </div>
                            </div>
                        </div>
                        <h1 class="mt-1 mb-3">{{ $card['value'] }}</h1>
                    </div>
                </div>
            </div>
        @endforeach
    </div>

    <div class="row">
        <!-- Area Chart -->

        <div class="col-xl-8 col-lg-7">
            <div class="card flex-fill w-100">
                <div class="card-header">
                    <h5 class="card-title mb-0">
                        {{ trans('admin.dashboard.recent_users') }}
                    </h5>
                </div>
                <div class="card-body pt-2 pb-3">
                    <div class="tab-content mb-3">
                        <div class="tab-pane fade show active" id="monthlyChart" role="tabpanel" aria-labelledby="monthlyChartTab">
                            <div class="chart">
                                <canvas id="newUsersPerMonthsChart"></canvas>
                            </div>
                        </div>
                        <div class="tab-pane fade" id="dailyChart" role="tabpanel" aria-labelledby="dailyChartTab">
                            <div class="chart">
                                <canvas id="newUsersPerDaysChart"></canvas>
                            </div>
                        </div>
                    </div>

                    <ul class="nav nav-pills" id="pills-tab" role="tablist">
                        <li class="nav-item" role="presentation">
                            <a class="nav-link active" id="monthlyChartTab" data-bs-toggle="pill" href="#monthlyChart" role="tab" aria-controls="monthlyChart" aria-selected="true">
                                {{ trans('messages.range.months') }}
                            </a>
                        </li>
                        <li class="nav-item" role="presentation">
                            <a class="nav-link" id="dailyChartTab" data-bs-toggle="pill" href="#dailyChart" role="tab" aria-controls="dailyChart" aria-selected="false">
                                {{ trans('messages.range.days') }}
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
        </div>

        <!-- Pie Chart -->
        <div class="col-xl-4 col-lg-5">
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h5 class="card-title mb-0">{{ trans('admin.dashboard.active_users') }}</h5>
                </div>
                <div class="card-body">
                    <div class="chart-pie pt-4 pb-2">
                        <canvas id="activeUsersChart"></canvas>
                    </div>
                    <div class="mt-4 text-center small">
                        <span class="me-1">
                            <i class="bi bi-circle-fill text-primary"></i> {{ now()->subDay()->longAbsoluteDiffForHumans() }}
                        </span>
                        <span class="me-1">
                            <i class="bi bi-circle-fill text-success"></i> {{ now()->subWeek()->longAbsoluteDiffForHumans() }}
                        </span>
                        <span class="me-1">
                            <i class="bi bi-circle-fill text-info"></i> {{ now()->subMonth()->longAbsoluteDiffForHumans() }}
                        </span>
                        <span class="me-1">
                            <i class="bi bi-circle-fill text-warning"></i> + {{ now()->subMonth()->longAbsoluteDiffForHumans() }}
                        </span>
                    </div>
                </div>
            </div>
        </div>

    </div>
@endsection

@push('footer-scripts')
    <script src="{{ asset('vendor/chart.js/chart.umd.js') }}"></script>
    <script src="{{ asset('admin/js/charts.js') }}"></script>
    <script>
        createLineChart('newUsersPerMonthsChart', @json($newUsersPerMonths), '{{ trans('admin.dashboard.recent_users') }}');
        createLineChart('newUsersPerDaysChart', @json($newUsersPerDays), '{{ trans('admin.dashboard.recent_users') }}');
        createPieChart('activeUsersChart', @json($activeUsers));
    </script>

    @if(! $secure)
        <script>
            // When using a proxy, if the traffic is encrypted only between the
            // proxy and the web server, the warning can be show even if the user use https
            // (like with Cloudflare flexible encryption). In this case we just
            // hide the warning and display an info text.
            if (window.location.protocol === 'https:') {
                document.getElementById('notHttpsAlert').classList.add('d-none');
                document.getElementById('proxyAlert').classList.remove('d-none');
            }
        </script>
    @endif

    <script>
        (function() {
            function formatUptime(seconds) {
                if (!seconds || seconds <= 0) return 'Uptime: --';
                const d = Math.floor(seconds / 86400);
                const h = Math.floor((seconds % 86400) / 3600);
                const m = Math.floor((seconds % 3600) / 60);
                const s = seconds % 60;
                if (d > 0) return `Uptime: ${d}h ${h}j ${m}m`;
                if (h > 0) return `Uptime: ${h}j ${m}m ${s}d`;
                return `Uptime: ${m}m ${s}d`;
            }

            async function fetchTelemetry() {
                const refreshIcon = document.getElementById('refreshIcon');
                if (refreshIcon) refreshIcon.classList.add('spin-animation');

                try {
                    const res = await fetch('{{ url('/api/apexsions-bridge/status') }}', {
                        headers: { 'Accept': 'application/json' }
                    });
                    if (!res.ok) throw new Error('Status HTTP ' + res.status);
                    const data = await res.json();

                    const isOnline = data.online === true;

                    // 1. Badge & Status
                    const statusBadge = document.getElementById('realmLiveStatusBadge');
                    const statusText = document.getElementById('realmLiveStatusText');
                    if (statusBadge && statusText) {
                        statusBadge.className = isOnline ? 'badge bg-success px-3 py-2' : 'badge bg-danger px-3 py-2';
                        statusText.textContent = isOnline ? 'REALM ONLINE' : 'SERVER OFFLINE';
                    }

                    // 2. Uptime
                    const uptimeText = document.getElementById('realmLiveUptimeText');
                    if (uptimeText) {
                        uptimeText.textContent = isOnline ? formatUptime(data.uptime_seconds) : 'Uptime: Offline';
                    }

                    // 3. TPS
                    const tps = typeof data.tps === 'number' ? data.tps : 20.0;
                    const gaugeTps = document.getElementById('gaugeTps');
                    const gaugeTpsBar = document.getElementById('gaugeTpsBar');
                    const gaugeTpsLabel = document.getElementById('gaugeTpsLabel');
                    if (gaugeTps) gaugeTps.textContent = isOnline ? tps.toFixed(1) : '0.0';
                    if (gaugeTpsBar) {
                        const pct = isOnline ? Math.min(100, Math.round((tps / 20.0) * 100)) : 0;
                        gaugeTpsBar.style.width = pct + '%';
                        gaugeTpsBar.className = tps >= 19.5 ? 'progress-bar bg-success' : (tps >= 17.0 ? 'progress-bar bg-warning' : 'progress-bar bg-danger');
                    }
                    if (gaugeTpsLabel) {
                        const stability = isOnline ? Math.min(100, Math.round((tps / 20.0) * 100)) : 0;
                        gaugeTpsLabel.textContent = isOnline ? `Stabilitas Engine: ${stability}% (Target: 20.0)` : 'Server offline / tidak terhubung';
                    }

                    // 4. RAM
                    const ramUsed = data.ram_used_mb || 0;
                    const ramMax = data.ram_max_mb || 1;
                    const gaugeRam = document.getElementById('gaugeRam');
                    const gaugeRamBar = document.getElementById('gaugeRamBar');
                    const gaugeRamLabel = document.getElementById('gaugeRamLabel');
                    if (gaugeRam) gaugeRam.textContent = isOnline && ramMax > 1 ? `${ramUsed} MB` : `${ramUsed} MB`;
                    if (gaugeRamBar && ramMax > 0) {
                        const ramPct = Math.min(100, Math.round((ramUsed / ramMax) * 100));
                        gaugeRamBar.style.width = (isOnline ? ramPct : 0) + '%';
                    }
                    if (gaugeRamLabel) {
                        gaugeRamLabel.textContent = isOnline && ramMax > 1 ? `Batas Alokasi: ${ramMax} MB (Sisa: ${data.free_ram_mb || 0} MB)` : 'Batas Alokasi: --';
                    }

                    // 5. Chunks & Entities
                    const gaugeChunks = document.getElementById('gaugeChunks');
                    const gaugeEntitiesLabel = document.getElementById('gaugeEntitiesLabel');
                    if (gaugeChunks) gaugeChunks.textContent = isOnline ? (data.loaded_chunks || 0) : '0';
                    if (gaugeEntitiesLabel) {
                        gaugeEntitiesLabel.textContent = isOnline ? `Entitas Aktif: ${data.entities || 0} entitas` : 'Entitas Aktif: 0 entitas';
                    }

                    // 6. Players
                    const count = data.players || 0;
                    const maxP = data.max_players || 500;
                    const gaugePlayers = document.getElementById('gaugePlayers');
                    const gaugePlayersBar = document.getElementById('gaugePlayersBar');
                    if (gaugePlayers) gaugePlayers.innerHTML = `${count} <span style="font-size: 1rem; color: #8E877D;">/ ${maxP}</span>`;
                    if (gaugePlayersBar) {
                        const pPct = Math.min(100, Math.round((count / maxP) * 100));
                        gaugePlayersBar.style.width = pPct + '%';
                    }

                    // 7. Player Roster
                    const rosterContainer = document.getElementById('onlinePlayersContainer');
                    const rosterCountBadge = document.getElementById('rosterCountBadge');
                    if (rosterCountBadge) rosterCountBadge.textContent = `${count} Pemain`;

                    if (rosterContainer) {
                        const list = data.player_list || [];
                        if (list.length === 0) {
                            rosterContainer.innerHTML = '<div class="p-3 text-center text-muted w-100" style="font-size: 0.85rem;"><i class="bi bi-moon-stars me-2"></i> Belum ada pemain yang online di dalam Realm saat ini.</div>';
                        } else {
                            rosterContainer.innerHTML = list.map(p => {
                                const name = typeof p === 'string' ? p : (p.name || 'Player');
                                const uuid = typeof p === 'object' && p.uuid ? p.uuid : '';
                                const ping = typeof p === 'object' && p.ping !== undefined ? p.ping : null;
                                const profileUrl = uuid ? `{{ url('/player') }}/${uuid}` : '#';
                                return `
                                    <a href="${profileUrl}" class="d-inline-flex align-items-center gap-2 px-3 py-1 rounded text-decoration-none" style="background: rgba(201, 164, 92, 0.08); border: 1px solid rgba(201, 164, 92, 0.25); transition: all 0.2s;" onmouseover="this.style.background='rgba(201,164,92,0.2)'" onmouseout="this.style.background='rgba(201,164,92,0.08)'">
                                        <img src="https://mc-heads.net/avatar/${encodeURIComponent(name)}/22" alt="${name}" class="rounded" width="22" height="22" onerror="this.src='{{ asset('assets/themes/apexsions/img/favicon.ico') }}'">
                                        <span class="fw-bold" style="color: #F4EFE6; font-size: 0.85rem;">${name}</span>
                                        ${ping !== null ? `<span class="badge ${ping < 80 ? 'bg-success' : (ping < 180 ? 'bg-warning' : 'bg-danger')}" style="font-size: 0.65rem; padding: 2px 5px;">${ping}ms</span>` : ''}
                                    </a>
                                `;
                            }).join('');
                        }
                    }

                } catch (e) {
                    console.warn('[Telemetry] Error fetching status:', e);
                } finally {
                    if (refreshIcon) {
                        setTimeout(() => refreshIcon.classList.remove('spin-animation'), 600);
                    }
                }
            }

            // Bind Refresh Button
            const btnRefresh = document.getElementById('btnRefreshTelemetry');
            if (btnRefresh) {
                btnRefresh.addEventListener('click', () => fetchTelemetry());
            }

            // Quick Broadcast Submission
            const formBroadcast = document.getElementById('formAdminBroadcast');
            if (formBroadcast) {
                formBroadcast.addEventListener('submit', async function(e) {
                    e.preventDefault();
                    const input = document.getElementById('broadcastMessageInput');
                    const btnSubmit = document.getElementById('btnSubmitBroadcast');
                    const feedback = document.getElementById('broadcastFeedback');
                    const msg = input ? input.value.trim() : '';

                    if (!msg) return;

                    btnSubmit.disabled = true;
                    btnSubmit.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Mengirim...';
                    feedback.className = 'small mt-2 d-none';

                    try {
                        const token = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
                        const res = await fetch('{{ url('/admin/apexsions/broadcast') }}', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json',
                                'Accept': 'application/json',
                                'X-CSRF-TOKEN': token
                            },
                            body: JSON.stringify({ message: msg })
                        });

                        const data = await res.json();
                        if (!res.ok) throw new Error(data.message || 'Gagal mengirim pengumuman.');

                        feedback.className = 'small mt-2 alert alert-success py-1 px-2';
                        feedback.textContent = '✓ Pengumuman berhasil dipancarkan ke seluruh pemain di dalam server Minecraft!';
                        input.value = '';
                        setTimeout(() => feedback.classList.add('d-none'), 5000);
                    } catch (err) {
                        feedback.className = 'small mt-2 alert alert-danger py-1 px-2';
                        feedback.textContent = '✕ ' + err.message;
                    } finally {
                        btnSubmit.disabled = false;
                        btnSubmit.innerHTML = '<i class="bi bi-send-fill me-1"></i> Kirim';
                    }
                });
            }

            // Initial fetch and 15s interval polling
            fetchTelemetry();
            setInterval(fetchTelemetry, 15000);
        })();
    </script>
    <style>
        .spin-animation {
            animation: spin 0.8s linear infinite;
        }
        @keyframes spin {
            100% { transform: rotate(360deg); }
        }
    </style>
@endpush
