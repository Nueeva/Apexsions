@extends('admin.layouts.admin')

@section('title', trans('admin.users.edit', ['user' => $user->name]))

@section('content')
<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <a href="{{ route('admin.users.index') }}" class="btn btn-outline-secondary btn-sm mb-2">
            <i class="bi bi-arrow-left me-1"></i> Back to User Center
        </a>
        <h1 class="h3 mb-0 text-gray-800">
            <i class="bi bi-person-badge-fill text-warning me-2"></i>User Dossier: {{ $user->name }}
        </h1>
        <p class="text-muted small mb-0">ID #{{ $user->id }} &bull; Registered {{ format_date_compact($user->created_at) }}</p>
    </div>
    <div class="d-flex gap-2">
        @if(! $user->isDeleted())
            <button type="button" class="btn btn-outline-info btn-sm" data-bs-toggle="modal" data-bs-target="#userNotificationModal">
                <i class="bi bi-megaphone-fill me-1"></i> {{ trans('admin.users.notify') }}
            </button>
        @endif
        @if($canDelete ?? false)
            <a href="{{ route('admin.users.destroy', $user) }}" class="btn btn-outline-danger btn-sm" data-confirm="delete">
                <i class="bi bi-trash-fill me-1"></i> {{ trans('admin.users.delete') }}
            </a>
        @endif
    </div>
</div>

@if($isSelf ?? false)
    <div class="alert alert-info shadow-sm d-flex align-items-center mb-4" role="alert">
        <i class="bi bi-shield-check display-6 me-3"></i>
        <div>
            <div class="fw-bold">Active Session Account</div>
            <div class="small">You are currently inspecting your own administrator account. Self-deletion is locked for administrative safety.</div>
        </div>
    </div>
@endif

@if($isLastAdmin ?? false)
    <div class="alert alert-warning shadow-sm d-flex align-items-center mb-4" role="alert">
        <i class="bi bi-exclamation-triangle-fill display-6 me-3"></i>
        <div>
            <div class="fw-bold">Last Administrator Protection Active</div>
            <div class="small">This account is the only active administrator on Apexsions. Role demotion and deletion are strictly prevented by system policy.</div>
        </div>
    </div>
@endif

@if($user->isDeleted())
    <div class="alert alert-secondary shadow-sm mb-4" role="alert">
        <i class="bi bi-person-x-fill me-2"></i> {{ trans('admin.users.alert-deleted') }}
    </div>
@elseif($user->isBanned())
    <div class="alert alert-danger shadow-sm mb-4" role="alert">
        <div class="d-flex justify-content-between align-items-center">
            <div>
                <h5 class="alert-heading mb-1"><i class="bi bi-slash-circle-fill me-2"></i>Account Suspended / Banned</h5>
                <div class="small">
                    <strong>Banned By:</strong> {{ $user->ban->author->name ?? 'System' }} &bull;
                    <strong>Reason:</strong> {{ $user->ban->reason ?? 'Violation of Terms' }} &bull;
                    <strong>Date:</strong> {{ format_date_compact($user->ban->created_at) }}
                </div>
            </div>
            <form method="POST" action="{{ route('admin.users.bans.destroy', [$user, $user->ban]) }}" class="ms-3">
                @method('DELETE')
                @csrf
                <button type="submit" class="btn btn-sm btn-light border">
                    <i class="bi bi-check2-circle text-success me-1"></i> Lift Ban (Reactivate)
                </button>
            </form>
        </div>
    </div>
@endif

<div class="row g-4 mb-4">
    {{-- SECTION 1 & 2: IDENTITY & ACCOUNT STATUS --}}
    <div class="col-lg-6">
        <div class="card shadow-sm border-0 h-100">
            <div class="card-header bg-body-tertiary py-3 d-flex align-items-center">
                <i class="bi bi-person-circle text-warning fs-5 me-2"></i>
                <h5 class="card-title mb-0 fw-bold">1. Identity & Account Status</h5>
            </div>
            <div class="card-body">
                <form action="{{ route('admin.users.update', $user) }}" method="POST">
                    @method('PATCH')
                    @csrf

                    <div class="row mb-3 align-items-center">
                        <div class="col-auto">
                            <img src="{{ $user->getAvatar(128) }}" alt="{{ $user->name }}" width="80" height="80" class="rounded-circle border shadow-sm" style="object-fit: cover;">
                        </div>
                        <div class="col">
                            <h4 class="mb-1 fw-bold">{{ $user->name }}</h4>
                            <div class="d-flex flex-wrap gap-1">
                                <span class="badge" style="{{ $user->role->getBadgeStyle() }}">
                                    @if($user->role->icon) <i class="{{ $user->role->icon }} me-1"></i> @endif
                                    {{ $user->role->name }}
                                </span>
                                @if($user->isAdmin())
                                    <span class="badge bg-warning text-dark"><i class="bi bi-shield-lock-fill me-1"></i>Administrator</span>
                                @endif
                                @if($user->hasVerifiedEmail())
                                    <span class="badge bg-success-subtle text-success border border-success-subtle"><i class="bi bi-patch-check-fill me-1"></i>Verified</span>
                                @else
                                    <span class="badge bg-warning-subtle text-warning border border-warning-subtle"><i class="bi bi-clock-history me-1"></i>Pending Verification</span>
                                @endif
                            </div>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold" for="nameInput">{{ trans('auth.name') }}</label>
                        <input type="text" class="form-control @error('name') is-invalid @enderror" id="nameInput" name="name" value="{{ old('name', $user->name) }}" required @disabled($user->isDeleted())>
                        @error('name')
                            <span class="invalid-feedback"><strong>{{ $message }}</strong></span>
                        @enderror
                    </div>

                    @can('admin.users.personal')
                        <div class="mb-3">
                            <label class="form-label fw-semibold" for="emailInput">{{ trans('auth.email') }}</label>
                            <input type="email" class="form-control @error('email') is-invalid @enderror" id="emailInput" name="email" value="{{ old('email', $user->email ?? '') }}" @disabled($user->isDeleted())>
                            @error('email')
                                <span class="invalid-feedback"><strong>{{ $message }}</strong></span>
                            @enderror
                        </div>
                    @else
                        <div class="mb-3">
                            <label class="form-label fw-semibold" for="emailInput">{{ trans('auth.email') }}</label>
                            <input type="email" class="form-control" id="emailInput" value="{{ Str::mask($user->email ?? '', '*', 2, -2) }}" disabled>
                            <div class="form-text">Protected by administrator privacy permission.</div>
                        </div>
                    @endcan

                    @if(! oauth_login())
                        <div class="mb-3">
                            <label class="form-label fw-semibold" for="passwordInput">Update Password</label>
                            <input type="password" class="form-control @error('password') is-invalid @enderror" id="passwordInput" name="password" placeholder="Leave blank to keep existing password" @disabled($user->isDeleted())>
                            @error('password')
                                <span class="invalid-feedback"><strong>{{ $message }}</strong></span>
                            @enderror
                        </div>
                    @endif

                    <div class="row g-3 mb-3">
                        <div class="col-md-6">
                            <label class="form-label fw-semibold" for="roleSelect">{{ trans('messages.fields.role') }}</label>
                            <select class="form-select @error('role') is-invalid @enderror" id="roleSelect" name="role" @disabled($user->isDeleted() || ($isLastAdmin ?? false))>
                                @foreach($roles as $role)
                                    <option value="{{ $role->id }}" @selected($user->role->is($role))>
                                        {{ $role->name }} (Power: {{ $role->power }})
                                    </option>
                                @endforeach
                            </select>
                            @error('role')
                                <span class="invalid-feedback"><strong>{{ $message }}</strong></span>
                            @enderror
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-semibold" for="moneyInput">{{ trans('messages.fields.money') }}</label>
                            <div class="input-group @error('money') has-validation @enderror">
                                <input type="number" min="0" max="999999999999" step="0.01" class="form-control @error('money') is-invalid @enderror" id="moneyInput" name="money" value="{{ old('money', $user->money) }}" required @disabled($user->isDeleted())>
                                <span class="input-group-text">{{ money_name() }}</span>
                                @error('money')
                                    <span class="invalid-feedback"><strong>{{ $message }}</strong></span>
                                @enderror
                            </div>
                        </div>
                    </div>

                    <div class="d-flex justify-content-between align-items-center pt-2 border-top">
                        <button type="submit" class="btn btn-primary" @disabled($user->isDeleted())>
                            <i class="bi bi-save me-1"></i> {{ trans('messages.actions.save') }}
                        </button>

                        @if(! $user->isDeleted() && ! $user->isAdmin() && ! $user->is(Auth::user()))
                            @if(! $user->isBanned())
                                <button type="button" class="btn btn-outline-danger" data-bs-toggle="modal" data-bs-target="#banModal">
                                    <i class="bi bi-slash-circle me-1"></i> Suspend User
                                </button>
                            @endif
                        @endif
                    </div>
                </form>
            </div>
        </div>
    </div>

    {{-- SECTION 3: SECURITY & AUTHENTICATION AUDIT --}}
    <div class="col-lg-6">
        <div class="card shadow-sm border-0 h-100">
            <div class="card-header bg-body-tertiary py-3 d-flex align-items-center">
                <i class="bi bi-shield-lock-fill text-info fs-5 me-2"></i>
                <h5 class="card-title mb-0 fw-bold">2. Security & Authentication Audit</h5>
            </div>
            <div class="card-body">
                {{-- Email Verification Control --}}
                <div class="mb-4 pb-3 border-bottom">
                    <label class="form-label fw-semibold d-block mb-1">Email Verification Status</label>
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            @if($user->hasVerifiedEmail())
                                <span class="badge bg-success-subtle text-success border border-success-subtle p-2">
                                    <i class="bi bi-patch-check-fill me-1"></i> Verified on {{ format_date_compact($user->email_verified_at) }}
                                </span>
                            @else
                                <span class="badge bg-warning-subtle text-warning border border-warning-subtle p-2">
                                    <i class="bi bi-exclamation-circle-fill me-1"></i> Pending Verification (Unverified)
                                </span>
                            @endif
                        </div>
                        @if(!$user->hasVerifiedEmail() && $user->email !== null && !$user->isDeleted())
                            <form action="{{ route('admin.users.verify', $user) }}" method="POST">
                                @csrf
                                <button class="btn btn-sm btn-outline-success" type="submit">
                                    <i class="bi bi-check2-all me-1"></i> Force Verify
                                </button>
                            </form>
                        @endif
                    </div>
                </div>

                {{-- Two-Factor Authentication (2FA) --}}
                @if(! oauth_login())
                    <div class="mb-4 pb-3 border-bottom">
                        <label class="form-label fw-semibold d-block mb-1">Two-Factor Authentication (2FA / TOTP)</label>
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                @if($user->hasTwoFactorAuth())
                                    <span class="badge bg-info-subtle text-info border border-info-subtle p-2">
                                        <i class="bi bi-shield-check me-1"></i> Active (TOTP Authenticator Enabled)
                                    </span>
                                @else
                                    <span class="badge bg-secondary-subtle text-muted border p-2">
                                        <i class="bi bi-shield-x me-1"></i> Disabled
                                    </span>
                                @endif
                            </div>
                            @if($user->hasTwoFactorAuth())
                                <form action="{{ route('admin.users.2fa', $user) }}" method="POST" onsubmit="return confirm('Disable 2FA for this user?');">
                                    @csrf
                                    <button class="btn btn-sm btn-outline-danger" type="submit">
                                        <i class="bi bi-key me-1"></i> Disable 2FA
                                    </button>
                                </form>
                            @endif
                        </div>
                    </div>

                    {{-- Password Policy & Reset --}}
                    <div class="mb-4 pb-3 border-bottom">
                        <label class="form-label fw-semibold d-block mb-1">Password Lifecycle</label>
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                @if($user->mustChangePassword())
                                    <span class="badge bg-danger-subtle text-danger border border-danger-subtle p-2">
                                        <i class="bi bi-exclamation-triangle-fill me-1"></i> Password Reset Mandatory
                                    </span>
                                @else
                                    <span class="text-muted small">
                                        Last changed: {{ $user->password_changed_at ? format_date_compact($user->password_changed_at) : 'Never' }}
                                    </span>
                                @endif
                            </div>
                            @if(! $user->mustChangePassword())
                                <form action="{{ route('admin.users.force-password', $user) }}" method="POST" onsubmit="return confirm('Force user to change password upon next login?');">
                                    @csrf
                                    <button class="btn btn-sm btn-outline-warning" type="submit">
                                        <i class="bi bi-arrow-repeat me-1"></i> Force Reset
                                    </button>
                                </form>
                            @endif
                        </div>
                    </div>
                @endif

                {{-- Session & Access Logs --}}
                <div class="row g-2">
                    <div class="col-6">
                        <div class="p-2 bg-body-tertiary rounded">
                            <div class="text-muted small">Registered At</div>
                            <div class="fw-semibold small">{{ format_date_compact($user->created_at) }}</div>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="p-2 bg-body-tertiary rounded">
                            <div class="text-muted small">Last Login</div>
                            <div class="fw-semibold small">{{ $user->last_login_at ? format_date_compact($user->last_login_at) : 'Never' }}</div>
                        </div>
                    </div>
                    @can('admin.users.personal-data')
                        <div class="col-12 mt-2">
                            <div class="p-2 bg-body-tertiary rounded">
                                <div class="text-muted small">Last Known IP</div>
                                <div class="font-monospace small">{{ $user->last_login_ip ?? 'Unknown' }}</div>
                            </div>
                        </div>
                    @endcan
                </div>
            </div>
        </div>
    </div>
</div>

{{-- SECTION 4: MINECRAFT INTEGRATION --}}
<div class="card shadow-sm border-0 mb-4">
    <div class="card-header bg-body-tertiary py-3 d-flex align-items-center">
        <i class="bi bi-controller text-success fs-5 me-2"></i>
        <h5 class="card-title mb-0 fw-bold">3. Minecraft Server Synchronization</h5>
    </div>
    <div class="card-body">
        @if($minecraftAccount)
            <div class="row g-4 align-items-center">
                <div class="col-md-3 text-center border-end">
                    <img src="https://mc-heads.net/avatar/{{ $minecraftAccount->minecraft_username }}/100" alt="{{ $minecraftAccount->minecraft_username }}" class="rounded shadow-sm mb-2" width="100" height="100">
                    <div class="h5 mb-0 fw-bold text-warning">{{ $minecraftAccount->minecraft_username }}</div>
                    <span class="badge bg-dark border border-warning text-warning mt-1">{{ strtoupper($minecraftAccount->rank ?? 'wanderer') }}</span>
                </div>
                <div class="col-md-9">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label text-muted small mb-1">Minecraft UUID</label>
                            <div class="input-group input-group-sm">
                                <input type="text" class="form-control font-monospace" value="{{ $minecraftAccount->minecraft_uuid }}" id="mcUuidInput" readonly>
                                <button class="btn btn-outline-secondary" type="button" onclick="navigator.clipboard.writeText(document.getElementById('mcUuidInput').value); alert('UUID Copied!');">
                                    <i class="bi bi-clipboard"></i>
                                </button>
                            </div>
                        </div>
                        <div class="col-md-3 col-6">
                            <label class="form-label text-muted small mb-1">Kingdom</label>
                            <div class="fw-bold">{{ $minecraftAccount->kingdom ?? 'None' }}</div>
                        </div>
                        <div class="col-md-3 col-6">
                            <label class="form-label text-muted small mb-1">Level & XP</label>
                            <div class="fw-bold">Level {{ $minecraftAccount->level ?? 1 }} <span class="text-muted small">({{ $minecraftAccount->xp ?? 0 }} XP)</span></div>
                        </div>
                        <div class="col-md-3 col-6">
                            <label class="form-label text-muted small mb-1">Edition</label>
                            <div class="fw-bold">{{ $minecraftAccount->isBedrock() ? 'Bedrock Edition' : 'Java Edition' }}</div>
                        </div>
                        <div class="col-md-3 col-6">
                            <label class="form-label text-muted small mb-1">Battlepass Tier</label>
                            <div class="fw-bold">Tier {{ $minecraftAccount->battlepass_tier ?? 1 }}</div>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label text-muted small mb-1">In-Game Last Seen</label>
                            <div class="fw-bold small">{{ $minecraftAccount->last_seen_at ? format_date_compact($minecraftAccount->last_seen_at) : 'Not recorded' }}</div>
                        </div>
                    </div>
                </div>
            </div>
        @else
            <div class="text-center py-4 text-muted">
                <i class="bi bi-link-45deg display-6 d-block mb-2 text-secondary"></i>
                <div class="fw-semibold">No Minecraft Account Linked</div>
                <div class="small">This web user has not completed in-game verification with <code>/verify</code> or registered with an linked Minecraft identity.</div>
            </div>
        @endif
    </div>
</div>

{{-- SECTION 5: AUDIT HISTORY --}}
@can('admin.logs')
    <div class="card shadow-sm border-0 mb-4">
        <div class="card-header bg-body-tertiary py-3 d-flex align-items-center justify-content-between">
            <div class="d-flex align-items-center">
                <i class="bi bi-clock-history text-primary fs-5 me-2"></i>
                <h5 class="card-title mb-0 fw-bold">4. Administrative Audit History</h5>
            </div>
            <span class="badge bg-secondary">{{ $logs->total() }} events recorded</span>
        </div>
        <div class="card-body p-0">
            @if($logs->isEmpty())
                <div class="text-center py-4 text-muted small">
                    No administrative audit actions recorded for this user yet.
                </div>
            @else
                <div class="table-responsive">
                    <table class="table table-hover mb-0 align-middle">
                        <thead class="table-light">
                            <tr>
                                <th scope="col" style="width: 60px;" class="ps-3">#</th>
                                <th scope="col">Action</th>
                                <th scope="col">Timestamp</th>
                                <th scope="col" class="text-end pe-3">Details</th>
                            </tr>
                        </thead>
                        <tbody>
                            @foreach($logs as $log)
                                <tr>
                                    <th scope="row" class="ps-3">{{ $log->id }}</th>
                                    <td>
                                        <i class="text-{{ $log->getActionFormat()['color'] }} bi bi-{{ $log->getActionFormat()['icon'] }} me-1"></i>
                                        {{ $log->getActionMessage() }}
                                    </td>
                                    <td class="small">{{ format_date_compact($log->created_at) }}</td>
                                    <td class="text-end pe-3">
                                        <a href="{{ route('admin.logs.show', $log) }}" class="btn btn-sm btn-outline-secondary" title="{{ trans('messages.actions.show') }}">
                                            <i class="bi bi-eye"></i>
                                        </a>
                                    </td>
                                </tr>
                            @endforeach
                        </tbody>
                    </table>
                </div>
                @if($logs->hasPages())
                    <div class="card-footer bg-transparent py-2">
                        {{ $logs->links() }}
                    </div>
                @endif
            @endif
        </div>
    </div>
@endcan

{{-- BAN MODAL --}}
@if(! $user->isBanned())
    <div class="modal fade" id="banModal" tabindex="-1" role="dialog" aria-labelledby="banLabel" aria-modal="true">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title text-danger" id="banLabel">
                        <i class="bi bi-slash-circle-fill me-1"></i> Suspend User: {{ $user->name }}
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <form method="POST" action="{{ route('admin.users.bans.store', $user) }}">
                    @csrf
                    <div class="modal-body">
                        <p class="small text-muted mb-3">
                            Suspending this user will prevent them from logging in to the website and interacting with community features.
                        </p>
                        <div class="mb-3">
                            <label class="form-label fw-semibold" for="reasonInput">{{ trans('admin.bans.reason') }}</label>
                            <input type="text" class="form-control" id="reasonInput" name="reason" placeholder="e.g. Violation of server conduct, chargeback, etc." required>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button class="btn btn-secondary" type="button" data-bs-dismiss="modal">
                            {{ trans('messages.actions.cancel') }}
                        </button>
                        <button class="btn btn-danger" type="submit">
                            <i class="bi bi-slash-circle me-1"></i> Suspend Account
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
@endif

{{-- USER NOTIFICATION MODAL --}}
@include('admin.users._notify', [
    'route' => route('admin.users.notify', ['user' => $user]),
    'all' => false,
    'modalId' => 'userNotificationModal',
    'targetUser' => $user
])
@endsection
