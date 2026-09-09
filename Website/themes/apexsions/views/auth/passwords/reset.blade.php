@extends('layouts.app')

@section('title', trans('auth.passwords.reset'))

@section('content')
<div class="apx-auth-wrapper">
    <div class="apx-auth-card">
        <div class="apx-auth-header">
            <img src="{{ theme_asset('img/logo.png') }}&v={{ @filemtime(public_path('assets/themes/apexsions/img/logo.png')) ?: '3' }}" alt="Apexsions Crest" class="rounded-3 shadow-lg mb-3 border border-warning" width="80" height="80" style="object-fit: contain; padding: 4px; background: rgba(12, 16, 26, 0.95);">
            <h1 class="apx-auth-title">{{ trans('auth.passwords.reset') }}</h1>
            <p class="apx-auth-subtitle">Apexsions | The Peak Civilizations</p>
        </div>

        <form method="POST" action="{{ route('password.update') }}" id="reset-form">
            @csrf

            <input type="hidden" name="token" value="{{ $token }}">

            <div class="mb-3">
                <label class="form-label" for="email">
                    <i class="bi bi-envelope me-1 text-warning"></i> {{ trans('auth.email') }}
                </label>
                <input id="email" type="email" class="form-control @error('email') is-invalid @enderror" name="email" value="{{ $email ?? old('email') }}" required autocomplete="email" autofocus>

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
                <div class="input-group">
                    <input id="password" type="password" class="form-control @error('password') is-invalid @enderror" name="password" placeholder="Kata sandi baru (min. 8 karakter)" required autocomplete="new-password">
                    <button type="button" class="btn btn-outline-secondary apx-password-toggle" onclick="togglePasswordVisibility('password', this)" aria-label="Show password" title="Show password">
                        <i class="bi bi-eye"></i>
                    </button>
                </div>

                @error('password')
                    <span class="invalid-feedback d-block mt-1" role="alert">
                        <strong>{{ $message }}</strong>
                    </span>
                @enderror
            </div>

            <div class="mb-4">
                <label class="form-label" for="password-confirm">
                    <i class="bi bi-shield-check me-1 text-warning"></i> {{ trans('auth.confirm_password') }}
                </label>
                <div class="input-group">
                    <input id="password-confirm" type="password" class="form-control" name="password_confirmation" placeholder="Ulangi kata sandi baru" required autocomplete="new-password">
                    <button type="button" class="btn btn-outline-secondary apx-password-toggle" onclick="togglePasswordVisibility('password-confirm', this)" aria-label="Show password" title="Show password">
                        <i class="bi bi-eye"></i>
                    </button>
                </div>
            </div>

            <div class="d-grid mt-4">
                <button type="submit" class="btn btn-apx-gold py-2" id="reset-submit-btn">
                    <i class="bi bi-check-lg me-1"></i> {{ trans('auth.passwords.reset') }}
                </button>
            </div>
        </form>

        <div class="apx-auth-footer">
            <a href="{{ route('login') }}" class="text-warning fw-bold">Kembali ke halaman masuk</a>
        </div>
    </div>
</div>

<script>
function togglePasswordVisibility(inputId, toggleBtn) {
    const input = document.getElementById(inputId);
    if (!input) return;
    const icon = toggleBtn.querySelector('i');
    if (input.type === 'password') {
        input.type = 'text';
        if (icon) {
            icon.classList.remove('bi-eye');
            icon.classList.add('bi-eye-slash');
        }
        toggleBtn.setAttribute('aria-label', 'Hide password');
        toggleBtn.setAttribute('title', 'Hide password');
    } else {
        input.type = 'password';
        if (icon) {
            icon.classList.remove('bi-eye-slash');
            icon.classList.add('bi-eye');
        }
        toggleBtn.setAttribute('aria-label', 'Show password');
        toggleBtn.setAttribute('title', 'Show password');
    }
}

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('reset-form');
    const submitBtn = document.getElementById('reset-submit-btn');
    if (form && submitBtn) {
        form.addEventListener('submit', function () {
            if (form.checkValidity()) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Menyimpan Sandi...';
            }
        });
    }
});
</script>
@endsection
