@extends('layouts.app')

@section('title', trans('auth.login'))

@section('content')
<div class="apx-auth-wrapper">
    <div class="apx-auth-card">
        <div class="apx-auth-header">
            <img src="{{ theme_asset('img/logo.jpg') }}" alt="Apexsions Crest" class="rounded-3 shadow-lg mb-3 border border-warning" width="80" height="80" style="object-fit: cover;">
            <h1 class="apx-auth-title">{{ trans('auth.login') }}</h1>
            <p class="apx-auth-subtitle">Apexsions | The Peak Civilizations</p>
        </div>

        <form method="POST" action="{{ route('login') }}" id="captcha-form">
            @csrf

            <div class="mb-3">
                <label class="form-label" for="email">
                    <i class="bi bi-envelope me-1 text-warning"></i> {{ trans('auth.email') }}
                </label>
                <input id="email" type="text" class="form-control @error('email') is-invalid @enderror" name="email" value="{{ old('email') }}" placeholder="nama@email.com atau username" required autocomplete="email" autofocus>

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
                <input id="password" type="password" class="form-control @error('password') is-invalid @enderror" name="password" placeholder="Masukkan kata sandi akun" required autocomplete="current-password">

                @error('password')
                    <span class="invalid-feedback d-block mt-1" role="alert">
                        <strong>{{ $message }}</strong>
                    </span>
                @enderror
            </div>

            <div class="row gy-2 mb-4 align-items-center">
                <div class="col-6">
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" name="remember" id="remember" @checked(old('remember'))>
                        <label class="form-check-label small" for="remember">
                            {{ trans('auth.remember') }}
                        </label>
                    </div>
                </div>

                <div class="col-6 text-end">
                    @if(Route::has('password.request'))
                        <a class="small text-warning" href="{{ route('password.request') }}">
                            {{ trans('auth.forgot_password') }}
                        </a>
                    @endif
                </div>
            </div>

            @includeWhen($captcha, 'elements.captcha', ['center' => true])

            <div class="d-grid mt-4">
                <button type="submit" class="btn btn-apx-gold py-2">
                    <i class="bi bi-box-arrow-in-right me-1"></i> {{ trans('auth.login') }}
                </button>
            </div>
        </form>

        <div class="apx-auth-footer">
            Belum memiliki akun? <a href="{{ route('register') }}" class="text-warning fw-bold">Daftar sekarang</a>
        </div>
    </div>
</div>
@endsection
