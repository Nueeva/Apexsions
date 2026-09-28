@extends('admin.layouts.admin')

@section('title', 'Live Server Console — Apexsions')

@push('styles')
<style>
    .terminal-container {
        background-color: #0b0d11;
        border: 1px solid rgba(212, 175, 55, 0.25);
        border-radius: 8px;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.6);
        overflow: hidden;
        display: flex;
        flex-direction: column;
    }
    .terminal-toolbar {
        background: #14171d;
        border-bottom: 1px solid rgba(255, 255, 255, 0.08);
        padding: 8px 16px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 8px;
    }
    .terminal-screen {
        background: #0b0d11;
        color: #e2e8f0;
        font-family: 'Consolas', 'Fira Code', 'Monaco', monospace;
        font-size: 0.88rem;
        line-height: 1.45;
        height: 540px;
        overflow-y: auto;
        padding: 14px 18px;
        white-space: pre-wrap;
        word-break: break-all;
    }
    .terminal-screen::-webkit-scrollbar {
        width: 8px;
    }
    .terminal-screen::-webkit-scrollbar-track {
        background: #0b0d11;
    }
    .terminal-screen::-webkit-scrollbar-thumb {
        background: #2a2e39;
        border-radius: 4px;
    }
    .terminal-screen::-webkit-scrollbar-thumb:hover {
        background: #d4af37;
    }
    .terminal-input-bar {
        background: #14171d;
        border-top: 1px solid rgba(255, 255, 255, 0.08);
        padding: 10px 16px;
        display: flex;
        align-items: center;
        gap: 12px;
    }
    .terminal-prompt {
        color: #d4af37;
        font-family: monospace;
        font-weight: bold;
        font-size: 1.1rem;
    }
    .terminal-input {
        background: transparent;
        border: none;
        outline: none;
        color: #ffffff;
        font-family: 'Consolas', monospace;
        font-size: 0.95rem;
        flex: 1;
    }
    .ansi-black { color: #4b5563; }
    .ansi-red { color: #ef4444; }
    .ansi-green { color: #22c55e; }
    .ansi-yellow { color: #eab308; }
    .ansi-blue { color: #3b82f6; }
    .ansi-magenta { color: #ec4899; }
    .ansi-cyan { color: #06b6d4; }
    .ansi-white { color: #f8fafc; }
    .ansi-bold { font-weight: bold; }
    .ansi-dim { opacity: 0.7; }
</style>
@endpush

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <h2 class="h3 fw-bold text-white mb-1">
                <i class="bi bi-terminal-fill text-warning me-2"></i>Live Server Console & Power Desk
            </h2>
            <p class="text-white-50 small mb-0">Streaming real-time log konsol Paper via WebSocket Pterodactyl, eksekusi perintah instan, dan kontrol power server.</p>
        </div>
        <div class="d-flex gap-2 flex-wrap">
            <a href="{{ route('apexsions-bridge.admin.server.index') }}" class="btn btn-outline-secondary btn-sm shadow-sm">
                <i class="bi bi-arrow-left me-1"></i>Server Dashboard
            </a>
            <a href="{{ route('apexsions-bridge.admin.server.files') }}" class="btn btn-outline-warning btn-sm shadow-sm">
                <i class="bi bi-folder2-open me-1"></i>File Manager
            </a>
            <button type="button" class="btn btn-outline-light btn-sm shadow-sm" data-bs-toggle="modal" data-bs-target="#pterodactylConfigModal">
                <i class="bi bi-gear-fill me-1"></i>Pterodactyl Settings
            </button>
        </div>
    </div>

    @if(!$isConfigured)
        <!-- Config Notice -->
        <div class="alert alert-warning border-warning border-opacity-50 bg-dark text-warning p-4 rounded-3 shadow-sm mb-4">
            <div class="d-flex align-items-center gap-3">
                <i class="bi bi-exclamation-triangle-fill fs-2"></i>
                <div class="flex-grow-1">
                    <h5 class="fw-bold mb-1">Pterodactyl API Key Belum Dikonfigurasi</h5>
                    <p class="mb-0 small text-white-50">Untuk menggunakan fitur Live Console, Power Controls, dan File Manager langsung dari web panel, masukkan Client API Key Pterodactyl Anda.</p>
                </div>
                <button type="button" class="btn btn-warning fw-bold text-dark btn-sm px-3 py-2 shadow-sm" data-bs-toggle="modal" data-bs-target="#pterodactylConfigModal">
                    <i class="bi bi-key-fill me-1"></i>Setup Pterodactyl Key
                </button>
            </div>
        </div>
    @endif

    <!-- Telemetry & Power Bar -->
    @php
        $state = strtolower($resources['current_state'] ?? 'offline');
        $resData = $resources['resources'] ?? [];
        $cpu = isset($resData['cpu_absolute']) ? number_format($resData['cpu_absolute'], 1) . '%' : '--%';
        $ramUsed = isset($resData['memory_bytes']) ? number_format($resData['memory_bytes'] / 1073741824, 2) : '--';
        $ramMax = isset($resData['memory_limit_bytes']) ? number_format($resData['memory_limit_bytes'] / 1073741824, 1) : '12.0';
        $disk = isset($resData['disk_bytes']) ? number_format($resData['disk_bytes'] / 1073741824, 1) . ' GB' : '-- GB';
        $uptime = isset($resData['uptime']) ? floor($resData['uptime'] / 3600000) . 'j ' . floor(($resData['uptime'] % 3600000) / 60000) . 'm' : '--';
        $stateBadgeClass = 'bg-danger';
        if ($state === 'running') $stateBadgeClass = 'bg-success';
        elseif ($state === 'starting' || $state === 'stopping') $stateBadgeClass = 'bg-warning text-dark';
    @endphp
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
        <div class="card-body p-3">
            <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
                <!-- Status Badges -->
                <div class="d-flex align-items-center gap-3 flex-wrap">
                    <div class="d-flex align-items-center gap-2">
                        <span class="text-white-50 small">Status:</span>
                        <span id="badgeServerState" class="badge {{ $stateBadgeClass }} px-3 py-2 fw-bold font-monospace">
                            ● {{ strtoupper($state) }}
                        </span>
                    </div>
                    <div class="border-start border-secondary ps-3 d-flex align-items-center gap-3 flex-wrap font-monospace small">
                        <div><span class="text-white-50">CPU:</span> <span id="statCpu" class="text-warning fw-bold">{{ $cpu }}</span></div>
                        <div><span class="text-white-50">RAM:</span> <span id="statRam" class="text-info fw-bold">{{ $ramUsed }} / {{ $ramMax }} GB</span></div>
                        <div><span class="text-white-50">Disk:</span> <span id="statDisk" class="text-white fw-bold">{{ $disk }}</span></div>
                        <div><span class="text-white-50">Uptime:</span> <span id="statUptime" class="text-success fw-bold">{{ $uptime }}</span></div>
                    </div>
                </div>

                <!-- Power Controls -->
                <div class="d-flex gap-2 align-items-center flex-wrap">
                    <form action="{{ route('apexsions-bridge.admin.server.power') }}" method="POST" class="d-inline" onsubmit="return confirm('Mulai jalankan server Minecraft?');">
                        @csrf
                        <input type="hidden" name="signal" value="start">
                        <button type="submit" class="btn btn-success btn-sm px-3 fw-bold shadow-sm" {{ !$isConfigured ? 'disabled' : '' }}>
                            <i class="bi bi-play-fill me-1"></i>Start
                        </button>
                    </form>
                    <form action="{{ route('apexsions-bridge.admin.server.power') }}" method="POST" class="d-inline" onsubmit="return confirm('Restart server Minecraft sekarang?');">
                        @csrf
                        <input type="hidden" name="signal" value="restart">
                        <button type="submit" class="btn btn-warning btn-sm px-3 fw-bold text-dark shadow-sm" {{ !$isConfigured ? 'disabled' : '' }}>
                            <i class="bi bi-arrow-repeat me-1"></i>Restart
                        </button>
                    </form>
                    <form action="{{ route('apexsions-bridge.admin.server.power') }}" method="POST" class="d-inline" onsubmit="return confirm('Hentikan (Stop) server secara aman?');">
                        @csrf
                        <input type="hidden" name="signal" value="stop">
                        <button type="submit" class="btn btn-danger btn-sm px-3 fw-bold shadow-sm" {{ !$isConfigured ? 'disabled' : '' }}>
                            <i class="bi bi-stop-fill me-1"></i>Stop
                        </button>
                    </form>
                    <form action="{{ route('apexsions-bridge.admin.server.power') }}" method="POST" class="d-inline" onsubmit="return confirm('PERINGATAN: Mematikan paksa (Kill) dapat menyebabkan data chunk tidak tersimpan. Lanjutkan?');">
                        @csrf
                        <input type="hidden" name="signal" value="kill">
                        <button type="submit" class="btn btn-outline-danger btn-sm px-2 shadow-sm" title="Kill Server (Paksa)" {{ !$isConfigured ? 'disabled' : '' }}>
                            <i class="bi bi-lightning-fill"></i>
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <!-- Quick Macro Command Buttons -->
    <div class="d-flex gap-2 mb-3 flex-wrap align-items-center">
        <span class="text-white-50 small fw-bold me-1"><i class="bi bi-lightning-charge-fill text-warning me-1"></i>Perintah Cepat:</span>
        @foreach($quickCommands as $macro)
            <button type="button" class="btn {{ $macro['class'] }} btn-sm py-1 px-2" onclick="sendDirectCommand('{{ addslashes($macro['cmd']) }}')">
                <i class="bi {{ $macro['icon'] }} me-1"></i>{{ $macro['label'] }}
            </button>
        @endforeach
    </div>

    <!-- Terminal Box -->
    <div class="terminal-container mb-4">
        <!-- Terminal Toolbar -->
        <div class="terminal-toolbar">
            <div class="d-flex align-items-center gap-3">
                <div class="d-flex align-items-center gap-2">
                    <span id="wsStatusDot" class="badge rounded-pill bg-warning text-dark px-2 py-1 small">
                        ● Menghubungkan...
                    </span>
                </div>
                <span class="text-white-50 small font-monospace d-none d-md-inline">
                    Server: {{ $serverId }} ({{ parse_url($panelUrl, PHP_URL_HOST) }})
                </span>
            </div>
            <div class="d-flex align-items-center gap-2">
                <div class="form-check form-switch mb-0">
                    <input class="form-check-input" type="checkbox" id="chkAutoScroll" checked>
                    <label class="form-check-label text-white-50 small" for="chkAutoScroll">Auto-Scroll</label>
                </div>
                <button type="button" class="btn btn-outline-secondary btn-sm py-1 px-2 text-white-50" onclick="clearConsole()" title="Bersihkan Layar">
                    <i class="bi bi-eraser-fill"></i> Clear
                </button>
                <button type="button" class="btn btn-outline-secondary btn-sm py-1 px-2 text-white-50" onclick="downloadLogs()" title="Download Log">
                    <i class="bi bi-download"></i> Save Log
                </button>
            </div>
        </div>

        <!-- Terminal Output -->
        <pre id="consoleOutput" class="terminal-screen mb-0"></pre>

        <!-- Command Input Bar -->
        <form id="consoleForm" onsubmit="handleCommandSubmit(event)" class="terminal-input-bar">
            <span class="terminal-prompt">❯</span>
            <input type="text" id="commandInput" class="terminal-input" placeholder="Ketik perintah Minecraft (contoh: list, tps, broadcast Halo!)... [Enter untuk kirim]" autocomplete="off" spellcheck="false">
            <button type="submit" id="btnSendCmd" class="btn btn-warning btn-sm px-3 fw-bold text-dark shadow-sm">
                Kirim <span class="font-monospace">↵</span>
            </button>
        </form>
    </div>
</div>

<!-- Modal Pterodactyl Settings -->
<div class="modal fade" id="pterodactylConfigModal" tabindex="-1" aria-labelledby="pterodactylConfigModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-secondary">
            <form action="{{ route('apexsions-bridge.admin.server.settings.pterodactyl') }}" method="POST">
                @csrf
                <div class="modal-header border-secondary">
                    <h5 class="modal-title text-white fw-bold" id="pterodactylConfigModalLabel">
                        <i class="bi bi-gear-fill text-warning me-2"></i>Konfigurasi Pterodactyl Client API
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body text-white">
                    <div class="mb-3">
                        <label class="form-label text-white-50 small fw-bold">Pterodactyl Panel Base URL</label>
                        <input type="url" name="pterodactyl_url" class="form-control bg-secondary bg-opacity-25 text-white border-secondary" value="{{ $panelUrl }}" required placeholder="https://stellar.jagoanhosting.id">
                        <div class="form-text text-muted">URL panel Pterodactyl hosting Anda tanpa garis miring di akhir.</div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label text-white-50 small fw-bold">Server Identifier (Short UUID)</label>
                        <input type="text" name="pterodactyl_server_id" class="form-control bg-secondary bg-opacity-25 text-white border-secondary" value="{{ $serverId }}" required placeholder="27e4a2f6">
                        <div class="form-text text-muted">ID 8 karakter server Pterodactyl Anda (tertera di URL panel).</div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label text-white-50 small fw-bold">Pterodactyl Client API Key</label>
                        <input type="password" name="pterodactyl_api_key" class="form-control bg-secondary bg-opacity-25 text-white border-secondary" placeholder="ptlc_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx">
                        <div class="form-text text-muted">Dibuat dari menu <em>Account Settings &gt; API Credentials</em> di panel Pterodactyl. Kosongkan jika tidak ingin mengubah kunci saat ini.</div>
                    </div>
                </div>
                <div class="modal-footer border-secondary">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning btn-sm fw-bold text-dark">Simpan Konfigurasi</button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection

@push('scripts')
<script>
    let ws = null;
    let wsToken = null;
    let wsSocketUrl = null;
    let isConnected = false;
    let commandHistory = [];
    let historyIndex = -1;
    const maxLogLines = 2000;

    const screen = document.getElementById('consoleOutput');
    const input = document.getElementById('commandInput');
    const wsStatusDot = document.getElementById('wsStatusDot');
    const autoScrollChk = document.getElementById('chkAutoScroll');
    const badgeState = document.getElementById('badgeServerState');
    const statCpu = document.getElementById('statCpu');
    const statRam = document.getElementById('statRam');
    const statDisk = document.getElementById('statDisk');
    const statUptime = document.getElementById('statUptime');

    // ANSI Color Code parser to HTML
    function ansiToHtml(text) {
        if (!text) return '';
        const escapeHtml = (str) => str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
        
        let html = escapeHtml(text);

        // Regex ANSI codes
        const ansiMap = {
            '30': 'ansi-black', '31': 'ansi-red', '32': 'ansi-green', '33': 'ansi-yellow',
            '34': 'ansi-blue', '35': 'ansi-magenta', '36': 'ansi-cyan', '37': 'ansi-white',
            '90': 'ansi-black', '91': 'ansi-red', '92': 'ansi-green', '93': 'ansi-yellow',
            '94': 'ansi-blue', '95': 'ansi-magenta', '96': 'ansi-cyan', '97': 'ansi-white',
            '1': 'ansi-bold', '2': 'ansi-dim'
        };

        html = html.replace(/\u001b\[([0-9;]+)m/g, (match, codes) => {
            const codeList = codes.split(';');
            let classes = [];
            for (let c of codeList) {
                if (c === '0') return '</span>';
                if (ansiMap[c]) classes.push(ansiMap[c]);
            }
            if (classes.length > 0) {
                return '<span class="' + classes.join(' ') + '">';
            }
            return '';
        });

        return html;
    }

    function appendLog(rawText) {
        const line = document.createElement('div');
        line.innerHTML = ansiToHtml(rawText);
        screen.appendChild(line);

        // Trim buffer if too long
        if (screen.childElementCount > maxLogLines) {
            screen.removeChild(screen.firstChild);
        }

        if (autoScrollChk.checked) {
            screen.scrollTop = screen.scrollHeight;
        }
    }

    function clearConsole() {
        screen.innerHTML = '';
        appendLog('\u001b[33m--- Layar konsol telah dibersihkan ---\u001b[0m');
    }

    function downloadLogs() {
        const text = screen.innerText;
        const blob = new Blob([text], { type: 'text/plain' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `apexsions-console-${new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-')}.log`;
        a.click();
        URL.revokeObjectURL(url);
    }

    // Connect WebSocket
    async function initWebSocket() {
        @if(!$isConfigured)
            appendLog('\u001b[31m[ERROR] Pterodactyl API Key belum diset. Klik tombol "Pterodactyl Settings" untuk mengonfigurasi.\u001b[0m');
            wsStatusDot.className = 'badge rounded-pill bg-danger px-2 py-1 small';
            wsStatusDot.textContent = '● Belum Dikonfigurasi';
            return;
        @endif

        wsStatusDot.className = 'badge rounded-pill bg-warning text-dark px-2 py-1 small';
        wsStatusDot.textContent = '● Meminta Token...';

        try {
            const res = await fetch("{{ route('apexsions-bridge.admin.server.console.token') }}");
            const data = await res.json();

            wsToken = data.token;
            wsSocketUrl = data.socket;

            // Connect via secure reverse proxy relay on the same origin (bypasses browser CORS & port 8080 blocking)
            const proto = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
            const relayUrl = proto + window.location.host + '/pterodactyl-ws/';

            connectSocket(relayUrl, wsToken);
        } catch (err) {
            appendLog(`\u001b[31m[ERROR] Gagal mendapatkan WebSocket credentials: ${err.message}\u001b[0m`);
            wsStatusDot.className = 'badge rounded-pill bg-danger px-2 py-1 small';
            wsStatusDot.textContent = '● Gagal Terhubung';
            setTimeout(initWebSocket, 6000);
        }
    }

    function connectSocket(url, token) {
        if (ws) {
            try { ws.close(); } catch(e){}
        }

        wsStatusDot.textContent = '● Menghubungkan Socket...';
        ws = new WebSocket(url);

        ws.onopen = function() {
            // Send auth
            ws.send(JSON.stringify({ event: 'auth', args: [token] }));
            appendLog('\u001b[36m[WS] Terhubung ke daemon Pterodactyl. Mengautentikasi...\u001b[0m');
        };

        ws.onmessage = function(event) {
            try {
                const msg = JSON.parse(event.data);
                const ev = msg.event;
                const args = msg.args || [];

                if (ev === 'auth success') {
                    isConnected = true;
                    wsStatusDot.className = 'badge rounded-pill bg-success px-2 py-1 small';
                    wsStatusDot.textContent = '● Live WebSocket';
                    appendLog('\u001b[32m[WS] Autentikasi berhasil. Live streaming aktif.\u001b[0m');
                    // Request past logs
                    ws.send(JSON.stringify({ event: 'send logs', args: [null] }));
                } else if (ev === 'console output') {
                    appendLog(args[0] || '');
                } else if (ev === 'status') {
                    updateStatus(args[0] || 'unknown');
                } else if (ev === 'stats') {
                    if (args[0]) {
                        try {
                            const stats = typeof args[0] === 'string' ? JSON.parse(args[0]) : args[0];
                            updateStats(stats);
                        } catch(e){}
                    }
                } else if (ev === 'token expiring') {
                    // Refresh token
                    fetch("{{ route('apexsions-bridge.admin.server.console.token') }}")
                        .then(r => r.json())
                        .then(d => {
                            if (d.success && d.token && ws) {
                                ws.send(JSON.stringify({ event: 'auth', args: [d.token] }));
                            }
                        });
                } else if (ev === 'token expired') {
                    initWebSocket();
                }
            } catch(e) {
                console.error(e);
            }
        };

        ws.onclose = function() {
            isConnected = false;
            wsStatusDot.className = 'badge rounded-pill bg-secondary px-2 py-1 small';
            wsStatusDot.textContent = '● Terputus (Reconnecting...)';
            setTimeout(initWebSocket, 4000);
        };

        ws.onerror = function() {
            isConnected = false;
            wsStatusDot.className = 'badge rounded-pill bg-danger px-2 py-1 small';
            wsStatusDot.textContent = '● Error Koneksi';
        };
    }

    function updateStatus(state) {
        state = state.toLowerCase();
        badgeState.textContent = '● ' + state.toUpperCase();
        if (state === 'running') {
            badgeState.className = 'badge bg-success px-3 py-2 fw-bold font-monospace';
        } else if (state === 'starting') {
            badgeState.className = 'badge bg-warning text-dark px-3 py-2 fw-bold font-monospace';
        } else if (state === 'stopping') {
            badgeState.className = 'badge bg-warning text-dark px-3 py-2 fw-bold font-monospace';
        } else {
            badgeState.className = 'badge bg-danger px-3 py-2 fw-bold font-monospace';
        }
    }

    function updateStats(data) {
        if (!data) return;
        if (data.cpu_absolute !== undefined) {
            statCpu.textContent = data.cpu_absolute.toFixed(1) + '%';
        }
        if (data.memory_bytes !== undefined) {
            const usedGb = (data.memory_bytes / 1073741824).toFixed(2);
            const maxGb = ((data.memory_limit_bytes || 12902400000) / 1073741824).toFixed(1);
            statRam.textContent = `${usedGb} / ${maxGb} GB`;
        }
        if (data.disk_bytes !== undefined) {
            const diskGb = (data.disk_bytes / 1073741824).toFixed(1);
            statDisk.textContent = `${diskGb} GB`;
        }
        if (data.uptime !== undefined) {
            const sec = Math.floor(data.uptime / 1000);
            const hrs = Math.floor(sec / 3600);
            const mins = Math.floor((sec % 3600) / 60);
            statUptime.textContent = `${hrs}j ${mins}m`;
        }
    }

    // Command Dispatch
    async function sendDirectCommand(cmd) {
        if (!cmd) return;
        appendLog(`\u001b[33m❯ ${cmd}\u001b[0m`);

        // If WS is connected, send command via WebSocket for instantaneous response!
        if (ws && isConnected) {
            ws.send(JSON.stringify({ event: 'send command', args: [cmd] }));
            return;
        }

        // Fallback: Send via Laravel API
        try {
            const res = await fetch("{{ route('apexsions-bridge.admin.server.console.command') }}", {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                    'X-CSRF-TOKEN': '{{ csrf_token() }}'
                },
                body: JSON.stringify({ command: cmd })
            });
            const data = await res.json();
            if (!data.success) {
                appendLog(`\u001b[31m[FAILED] ${data.error || 'Gagal mengirim perintah'}\u001b[0m`);
            }
        } catch (e) {
            appendLog(`\u001b[31m[ERROR] ${e.message}\u001b[0m`);
        }
    }

    function handleCommandSubmit(e) {
        e.preventDefault();
        const cmd = input.value.trim();
        if (!cmd) return;

        commandHistory.push(cmd);
        historyIndex = commandHistory.length;

        sendDirectCommand(cmd);
        input.value = '';
    }

    // History Navigation (Up / Down)
    input.addEventListener('keydown', function(e) {
        if (e.key === 'ArrowUp') {
            e.preventDefault();
            if (commandHistory.length > 0 && historyIndex > 0) {
                historyIndex--;
                input.value = commandHistory[historyIndex];
            }
        } else if (e.key === 'ArrowDown') {
            e.preventDefault();
            if (historyIndex < commandHistory.length - 1) {
                historyIndex++;
                input.value = commandHistory[historyIndex];
            } else {
                historyIndex = commandHistory.length;
                input.value = '';
            }
        }
    });

    // Start
    document.addEventListener('DOMContentLoaded', () => {
        initWebSocket();
    });
</script>
@endpush
