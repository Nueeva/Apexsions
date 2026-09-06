@extends('layouts.app')

@section('title', 'Tautkan Akun Minecraft — Apexsions')

@section('content')
<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="card shadow-lg border-0 bg-dark text-white rounded-4 overflow-hidden mb-4">
                <div class="card-header bg-gradient p-4 border-bottom border-secondary d-flex justify-content-between align-items-center">
                    <div>
                        <h4 class="mb-1 fw-bold text-white"><i class="bi bi-controller text-info me-2"></i> Tautkan Akun Minecraft</h4>
                        <p class="text-muted small mb-0">Hubungkan akun Java Edition atau Bedrock Edition Anda ke portal web Apexsions</p>
                    </div>
                    <span class="badge bg-primary px-3 py-2 rounded-pill"><i class="bi bi-shield-check me-1"></i> Identity Model v2.2</span>
                </div>

                <div class="card-body p-4">
                    @if(session('success_pin'))
                        <div class="alert alert-success border-0 shadow rounded-3 p-4 mb-4 text-center">
                            <div class="mb-2 text-success"><i class="bi bi-key-fill display-4"></i></div>
                            <h5 class="fw-bold text-dark mb-1">Kode PIN Verifikasi Anda Telah Dibuat!</h5>
                            <p class="text-muted small mb-3">
                                Masuk ke server Minecraft kami dan jalankan perintah berikut dalam waktu <strong>5 menit</strong>:
                            </p>
                            <div class="d-inline-flex align-items-center bg-white p-3 rounded-3 shadow-sm border mb-3">
                                <span class="fs-4 fw-mono text-primary fw-bold letter-spacing-2 me-3">
                                    /link {{ session('success_pin')['code'] }}
                                </span>
                                <button type="button" class="btn btn-sm btn-outline-primary" onclick="navigator.clipboard.writeText('/link {{ session('success_pin')['code'] }}')">
                                    <i class="bi bi-clipboard"></i> Salin
                                </button>
                            </div>
                            <div class="text-muted small">
                                Username target: <strong>{{ session('success_pin')['username'] }}</strong> | Berlaku hingga: {{ session('success_pin')['expires_at'] }}
                            </div>
                        </div>
                    @endif

                    @if(session('error'))
                        <div class="alert alert-danger border-0 rounded-3 mb-4">
                            <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ session('error') }}
                        </div>
                    @endif

                    @if(session('success'))
                        <div class="alert alert-success border-0 rounded-3 mb-4">
                            <i class="bi bi-check-circle-fill me-2"></i> {{ session('success') }}
                        </div>
                    @endif

                    <h5 class="fw-bold mb-3 text-light">Akun Terhubung Saat Ini</h5>
                    @if($accounts->isEmpty())
                        <div class="text-center py-4 border border-secondary border-dashed rounded-3 mb-4 bg-black bg-opacity-25">
                            <i class="bi bi-person-x display-6 text-muted mb-2"></i>
                            <p class="text-muted mb-0">Belum ada akun Minecraft yang ditautkan ke akun web ini.</p>
                        </div>
                    @else
                        <div class="list-group mb-4">
                            @foreach($accounts as $acc)
                                <div class="list-group-item list-group-item-dark d-flex justify-content-between align-items-center rounded-3 mb-2 border-secondary p-3">
                                    <div class="d-flex align-items-center">
                                        <img src="https://crafatar.com/avatars/{{ $acc->minecraft_uuid ?? 'steve' }}?size=48&overlay" alt="{{ $acc->minecraft_username }}" class="rounded shadow-sm me-3" style="width: 48px; height: 48px;">
                                        <div>
                                            <div class="fw-bold fs-5 text-white">{{ $acc->minecraft_username }}</div>
                                            <div class="d-flex align-items-center gap-2 mt-1">
                                                @if($acc->isVerified())
                                                    <span class="badge bg-success"><i class="bi bi-check2"></i> Terverifikasi</span>
                                                @else
                                                    <span class="badge bg-warning text-dark"><i class="bi bi-hourglass-split"></i> Menunggu In-Game Link</span>
                                                @endif
                                                <span class="badge bg-secondary">{{ $acc->edition }} ({{ $acc->auth_mode }})</span>
                                            </div>
                                        </div>
                                    </div>
                                    <form action="{{ route('apexsions-bridge.link.unlink', $acc) }}" method="POST" onsubmit="return confirm('Yakin ingin melepas tautan akun Minecraft ini?');">
                                        @csrf
                                        @method('DELETE')
                                        <button type="submit" class="btn btn-outline-danger btn-sm">
                                            <i class="bi bi-trash me-1"></i> Lepas
                                        </button>
                                    </form>
                                </div>
                            @endforeach
                        </div>
                    @endif

                    <hr class="border-secondary my-4">

                    <h5 class="fw-bold mb-3 text-light">Tautkan Akun Baru</h5>
                    <form action="{{ route('apexsions-bridge.link.pin') }}" method="POST" class="bg-black bg-opacity-25 p-4 rounded-3 border border-secondary">
                        @csrf
                        <div class="mb-3">
                            <label for="usernameInput" class="form-label text-light fw-semibold">Minecraft Username</label>
                            <input type="text" name="username" id="usernameInput" class="form-control bg-dark text-white border-secondary" placeholder="Contoh: Steve atau .Steve (Bedrock)" required>
                            <div class="form-text text-muted small">Pemain Bedrock dapat menggunakan prefix Floodgate (misalnya tanda titik atau bintang).</div>
                        </div>

                        <div class="mb-4">
                            <label class="form-label text-light fw-semibold">Edisi Minecraft</label>
                            <div class="d-flex gap-3">
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="edition" id="editionJava" value="JAVA" checked>
                                    <label class="form-check-label text-light" for="editionJava">
                                        <strong>Java Edition</strong> (PC / Mac / Linux)
                                    </label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="edition" id="editionBedrock" value="BEDROCK">
                                    <label class="form-check-label text-light" for="editionBedrock">
                                        <strong>Bedrock Edition</strong> (Geyser / Floodgate / Mobile / Console)
                                    </label>
                                </div>
                            </div>
                        </div>

                        <button type="submit" class="btn btn-primary px-4 py-2 fw-semibold">
                            <i class="bi bi-shield-plus me-1"></i> Dapatkan Kode PIN Verifikasi
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
