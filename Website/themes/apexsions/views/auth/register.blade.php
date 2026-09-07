@extends('layouts.app')

@section('title', trans('auth.register'))

@section('content')
<div class="apx-auth-wrapper">
    <div class="apx-auth-card">
        <div class="apx-auth-header">
            <img src="{{ theme_asset('img/logo.png') }}&v={{ @filemtime(public_path('assets/themes/apexsions/img/logo.png')) ?: '3' }}" alt="Apexsions Crest" class="rounded-3 shadow-lg mb-3 border border-warning" width="80" height="80" style="object-fit: contain; padding: 4px; background: rgba(12, 16, 26, 0.95);">
            <h1 class="apx-auth-title" data-i18n="auth_reg_title">{{ trans('auth.register') }}</h1>
            <p class="apx-auth-subtitle">Apexsions | The Peak Civilizations</p>
        </div>

        <form method="POST" action="{{ route('register') }}" id="captcha-form">
            @csrf

            <div class="mb-3">
                <label class="form-label" for="name">
                    <i class="bi bi-person me-1 text-warning"></i> <span data-i18n="auth_reg_name_label">{{ trans('auth.name') }}</span>
                </label>
                <input id="name" type="text" class="form-control @error('name') is-invalid @enderror" name="name" value="{{ old('name') }}" placeholder="Nickname in-game Minecraft Anda" data-i18n-placeholder="auth_reg_ign_ph" required autocomplete="name" autofocus>

                @error('name')
                    <span class="invalid-feedback d-block mt-1" role="alert">
                        <strong>{{ $message }}</strong>
                    </span>
                @enderror
            </div>

            <div class="mb-3">
                <label class="form-label" for="email">
                    <i class="bi bi-envelope me-1 text-warning"></i> <span data-i18n="auth_reg_email_label">{{ trans('auth.email') }}</span>
                </label>
                <input id="email" type="email" class="form-control @error('email') is-invalid @enderror" name="email" value="{{ old('email') }}" placeholder="Alamat email aktif" data-i18n-placeholder="auth_reg_email_ph" required autocomplete="email">

                @error('email')
                    <span class="invalid-feedback d-block mt-1" role="alert">
                        <strong>{{ $message }}</strong>
                    </span>
                @enderror
            </div>

            <div class="mb-3">
                <label class="form-label" for="password">
                    <i class="bi bi-key me-1 text-warning"></i> <span data-i18n="auth_reg_pass_label">{{ trans('auth.password') }}</span>
                </label>
                <input id="password" type="password" class="form-control @error('password') is-invalid @enderror" name="password" placeholder="Minimal 8 karakter" data-i18n-placeholder="auth_reg_pass_ph" required autocomplete="new-password">

                @error('password')
                    <span class="invalid-feedback d-block mt-1" role="alert">
                        <strong>{{ $message }}</strong>
                    </span>
                @enderror
            </div>

            <div class="mb-3">
                <label class="form-label" for="password-confirm">
                    <i class="bi bi-shield-check me-1 text-warning"></i> <span data-i18n="auth_reg_confirm_label">{{ trans('auth.confirm_password') }}</span>
                </label>
                <input id="password-confirm" type="password" class="form-control" name="password_confirmation" placeholder="Ulangi kata sandi" data-i18n-placeholder="auth_reg_confirm_ph" required autocomplete="new-password">
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
                    <i class="bi bi-check2-circle me-1"></i> <span data-i18n="auth_reg_btn">{{ trans('auth.register') }}</span>
                </button>
            </div>
        </form>

        <div class="apx-auth-footer">
            <span data-i18n="auth_reg_has_account">Sudah memiliki akun?</span> <a href="{{ route('login') }}" class="text-warning fw-bold" data-i18n="auth_reg_login_link">Masuk sekarang</a>
        </div>
    </div>
</div>
@endsection
