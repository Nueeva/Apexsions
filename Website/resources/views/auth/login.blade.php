@extends('layouts.app')

@section('title', trans('auth.login'))

@section('content')
<div class="row justify-content-center">
    <div class="col-md-9 col-lg-6">
        <h1>{{ trans('auth.login') }}</h1>

        <div class="card">
            <div class="card-body">
                <form method="POST" action="{{ route('login') }}" id="captcha-form">
                    @csrf

                    <div class="mb-3">
                        <label class="form-label" for="email">{{ trans('auth.email') }}</label>
                        <input id="email" type="text" class="form-control @error('email') is-invalid @enderror" name="email" value="{{ old('email') }}" required autocomplete="email" autofocus>

                        @error('email')
                            <span class="invalid-feedback" role="alert">
                                <strong>{{ $message }}</strong>
                            </span>
                        @enderror
                    </div>

                    <div class="mb-3">
                        <label class="form-label" for="password">{{ trans('auth.password') }}</label>
                        <div class="input-group">
                            <input id="password" type="password" class="form-control @error('password') is-invalid @enderror" name="password" required autocomplete="current-password">
                            <button type="button" class="btn btn-outline-secondary" onclick="togglePasswordVisibility('password', this)" aria-label="Show password" title="Show password">
                                <i class="bi bi-eye"></i>
                            </button>
                        </div>

                        @error('password')
                            <span class="invalid-feedback d-block" role="alert">
                                <strong>{{ $message }}</strong>
                            </span>
                        @enderror
                    </div>

                    <div class="row gy-3 mb-3">
                        <div class="col-md-6">
                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" name="remember" id="remember" @checked(old('remember'))>

                                <label class="form-check-label" for="remember">
                                    {{ trans('auth.remember') }}
                                </label>
                            </div>
                        </div>

                        <div class="col-md-6">
                            @if(Route::has('password.request'))
                                <a class="float-md-end" href="{{ route('password.request') }}">
                                    {{ trans('auth.forgot_password') }}
                                </a>
                            @endif
                        </div>
                    </div>

                    @includeWhen($captcha, 'elements.captcha', ['center' => true])

                    <div class="d-grid">
                        <button type="submit" class="btn btn-primary d-block">
                            {{ trans('auth.login') }}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
@push('scripts')
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
</script>
@endpush
@endsection
