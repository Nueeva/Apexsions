@extends('layouts.app')

@section('title', trans('auth.passwords.reset'))

@section('content')
<div class="apx-auth-wrapper">
    <div class="apx-auth-card">
        <div class="apx-auth-header">
            <img src="{{ theme_asset('img/logo.png') }}&v={{ @filemtime(public_path('assets/themes/apexsions/img/logo.png')) ?: '3' }}" alt="Apexsions Crest" class="rounded-3 shadow-lg mb-3 border border-warning" width="80" height="80" style="object-fit: contain; padding: 4px; background: rgba(12, 16, 26, 0.95);">
            <h1 class="apx-auth-title">{{ trans('auth.forgot_password') }}</h1>
            <p class="apx-auth-subtitle">Apexsions | The Peak Civilizations</p>
        </div>

        @if(session('status'))
            <div class="alert alert-success d-flex align-items-center mb-4" role="alert">
                <i class="bi bi-check-circle-fill fs-5 me-2"></i>
                <div class="small">{{ session('status') }}</div>
            </div>
        @endif

        <form method="POST" action="{{ route('password.email') }}" id="email-form">
            @csrf

            <div class="mb-3">
                <label class="form-label" for="email">
                    <i class="bi bi-envelope me-1 text-warning"></i> {{ trans('auth.email') }}
                </label>
                <input id="email" type="email" class="form-control @error('email') is-invalid @enderror" name="email" value="{{ old('email') }}" placeholder="nama@email.com" required autocomplete="email" autofocus>

                @error('email')
                    <span class="invalid-feedback d-block mt-1" role="alert">
                        <strong>{{ $message }}</strong>
                    </span>
                @enderror
            </div>

            <div class="d-grid mt-4">
                <button type="submit" class="btn btn-apx-gold py-2" id="email-submit-btn">
                    <i class="bi bi-send-fill me-1"></i> {{ trans('auth.passwords.send') }}
                </button>
            </div>
        </form>

        <div class="apx-auth-footer">
            <a href="{{ route('login') }}" class="text-warning fw-bold">Kembali ke halaman masuk</a>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('email-form');
    const submitBtn = document.getElementById('email-submit-btn');
    if (form && submitBtn) {
        form.addEventListener('submit', function () {
            if (form.checkValidity()) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Mengirim Tautan...';
            }
        });
    }
});
</script>
@endsection
