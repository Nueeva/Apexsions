@extends('layouts.app')

@section('title', trans('auth.register'))

@section('content')
<div class="apx-auth-wrapper">
    <div class="apx-auth-card">
        <div class="apx-auth-header">
            <img src="{{ theme_asset('img/logo.jpg') }}" alt="Apexsions Crest" class="rounded-3 shadow-lg mb-3 border border-warning" width="80" height="80" style="object-fit: cover;">
            <h1 class="apx-auth-title">{{ trans('auth.register') }}</h1>
            <p class="apx-auth-subtitle">Apexsions | The Peak Civilizations</p>
        </div>

        <form method="POST" action="{{ route('register') }}" id="captcha-form">
            @csrf

            <div class="mb-3">
                <label class="form-label" for="name">
                    <i class="bi bi-person me-1 text-warning"></i> {{ trans('auth.name') }}
                </label>
                <input id="name" type="text" class="form-control @error('name') is-invalid @enderror" name="name" value="{{ old('name') }}" placeholder="Nickname in-game Minecraft Anda" required autocomplete="name" autofocus>

                @error('name')
                    <span class="invalid-feedback d-block mt-1" role="alert">
                        <strong>{{ $message }}</strong>
                    </span>
                @enderror
            </div>

            <div class="mb-3">
                <label class="form-label" for="email">
                    <i class="bi bi-envelope me-1 text-warning"></i> {{ trans('auth.email') }}
                </label>
                <input id="email" type="email" class="form-control @error('email') is-invalid @enderror" name="email" value="{{ old('email') }}" placeholder="Alamat email aktif" required autocomplete="email">

                @error('email')
                    <span class="invalid-feedback d-block mt-1" role="alert">
                        <strong>{{ $message }}</strong>
                    </span>
                @enderror
            </div>

            <div class="mb-3">
                <label class="form-label" for="password">
                    <i class="bi bi-key me-1 text-warning"></i> {{ trans('auth.password') }}
                </label>
                <input id="password" type="password" class="form-control @error('password') is-invalid @enderror" name="password" placeholder="Minimal 8 karakter" required autocomplete="new-password">

                @error('password')
                    <span class="invalid-feedback d-block mt-1" role="alert">
                        <strong>{{ $message }}</strong>
                    </span>
                @enderror
            </div>

            <div class="mb-3">
                <label class="form-label" for="password-confirm">
                    <i class="bi bi-shield-check me-1 text-warning"></i> {{ trans('auth.confirm_password') }}
                </label>
                <input id="password-confirm" type="password" class="form-control" name="password_confirmation" placeholder="Ulangi kata sandi" required autocomplete="new-password">
            </div>

            @if($registerConditions !== null)
                <div class="mb-3">
                    <div class="form-check">
                        <input class="form-check-input @error('conditions') is-invalid @enderror" type="checkbox" name="conditions" id="conditions" required @checked(old('conditions'))>

                        <label class="form-check-label small" for="conditions">
                            {{ $registerConditions }}
                        </label>

                        @error('conditions')
                            <span class="invalid-feedback d-block mt-1" role="alert">
                                <strong>{{ $message }}</strong>
                            </span>
                        @enderror
                    </div>
                </div>
            @endif

            @include('elements.captcha', ['center' => true])

            <div class="d-grid mt-4">
                <button type="submit" class="btn btn-apx-gold py-2">
                    <i class="bi bi-check2-circle me-1"></i> {{ trans('auth.register') }}
                </button>
            </div>
        </form>

        <div class="apx-auth-footer">
            Sudah memiliki akun? <a href="{{ route('login') }}" class="text-warning fw-bold">Masuk sekarang</a>
        </div>
    </div>
</div>
@endsection
