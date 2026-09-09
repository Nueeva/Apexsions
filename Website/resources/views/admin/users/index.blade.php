@extends('admin.layouts.admin')

@section('title', trans('admin.users.title'))

@section('content')
<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <h1 class="h3 mb-1 text-gray-800"><i class="bi bi-people-fill text-warning me-2"></i>{{ trans('admin.users.title') }}</h1>
        <p class="text-muted small mb-0">Apexsions User Management Center &mdash; Identity, Security & Minecraft Sync</p>
    </div>
    <div class="d-flex gap-2">
        @if(! oauth_login())
            <a class="btn btn-primary btn-sm px-3 shadow-sm" href="{{ route('admin.users.create') }}">
                <i class="bi bi-person-plus-fill me-1"></i> {{ trans('messages.actions.add') }}
            </a>
        @endif
        <button type="button" class="btn btn-outline-secondary btn-sm px-3 shadow-sm" data-bs-toggle="modal" data-bs-target="#notificationModal">
            <i class="bi bi-megaphone-fill me-1"></i> {{ trans('admin.users.notify') }}
        </button>
    </div>
</div>

{{-- KPI Summary Cards --}}
<div class="row g-3 mb-4">
    <div class="col-xl col-md-4 col-sm-6">
        <div class="card border-0 shadow-sm rounded-3 h-100 bg-body-tertiary">
            <div class="card-body p-3 d-flex align-items-center justify-content-between">
                <div>
                    <div class="text-muted small text-uppercase fw-semibold">Total Users</div>
                    <div class="h4 mb-0 fw-bold">{{ number_format($metrics['total'] ?? 0) }}</div>
                </div>
                <div class="rounded-circle p-2 bg-primary bg-opacity-10 text-primary fs-4">
                    <i class="bi bi-people"></i>
                </div>
            </div>
        </div>
    </div>
    <div class="col-xl col-md-4 col-sm-6">
        <div class="card border-0 shadow-sm rounded-3 h-100 bg-body-tertiary">
            <div class="card-body p-3 d-flex align-items-center justify-content-between">
                <div>
                    <div class="text-muted small text-uppercase fw-semibold">Verified</div>
                    <div class="h4 mb-0 fw-bold text-success">{{ number_format($metrics['verified'] ?? 0) }}</div>
                </div>
                <div class="rounded-circle p-2 bg-success bg-opacity-10 text-success fs-4">
                    <i class="bi bi-patch-check-fill"></i>
                </div>
            </div>
        </div>
    </div>
    <div class="col-xl col-md-4 col-sm-6">
        <div class="card border-0 shadow-sm rounded-3 h-100 bg-body-tertiary">
            <div class="card-body p-3 d-flex align-items-center justify-content-between">
                <div>
                    <div class="text-muted small text-uppercase fw-semibold">Pending</div>
                    <div class="h4 mb-0 fw-bold text-warning">{{ number_format($metrics['unverified'] ?? 0) }}</div>
                </div>
                <div class="rounded-circle p-2 bg-warning bg-opacity-10 text-warning fs-4">
                    <i class="bi bi-clock-history"></i>
                </div>
            </div>
        </div>
    </div>
    <div class="col-xl col-md-4 col-sm-6">
        <div class="card border-0 shadow-sm rounded-3 h-100 bg-body-tertiary">
            <div class="card-body p-3 d-flex align-items-center justify-content-between">
                <div>
                    <div class="text-muted small text-uppercase fw-semibold">2FA Secured</div>
                    <div class="h4 mb-0 fw-bold text-info">{{ number_format($metrics['two_factor'] ?? 0) }}</div>
                </div>
                <div class="rounded-circle p-2 bg-info bg-opacity-10 text-info fs-4">
                    <i class="bi bi-shield-lock-fill"></i>
                </div>
            </div>
        </div>
    </div>
    <div class="col-xl col-md-4 col-sm-6">
        <div class="card border-0 shadow-sm rounded-3 h-100 bg-body-tertiary">
            <div class="card-body p-3 d-flex align-items-center justify-content-between">
                <div>
                    <div class="text-muted small text-uppercase fw-semibold">Suspended / Banned</div>
                    <div class="h4 mb-0 fw-bold text-danger">{{ number_format($metrics['banned'] ?? 0) }}</div>
                </div>
                <div class="rounded-circle p-2 bg-danger bg-opacity-10 text-danger fs-4">
                    <i class="bi bi-slash-circle-fill"></i>
                </div>
            </div>
        </div>
    </div>
</div>

{{-- Filters & Search Bar --}}
<div class="card shadow-sm border-0 mb-4 bg-body-tertiary">
    <div class="card-body p-3">
        <form action="{{ route('admin.users.index') }}" method="GET" class="row g-2 align-items-center" role="search">
            <div class="col-lg-4 col-md-6 col-12">
                <div class="input-group input-group-sm">
                    <span class="input-group-text bg-transparent border-end-0"><i class="bi bi-search text-muted"></i></span>
                    <input type="search" class="form-control border-start-0" name="search" value="{{ $search ?? '' }}" placeholder="Search username, email, IGN, UUID...">
                </div>
            </div>

            <div class="col-lg-2 col-md-3 col-6">
                <select name="status" class="form-select form-select-sm" onchange="this.form.submit()">
                    <option value="">All Statuses</option>
                    <option value="verified" @selected(($status ?? '') === 'verified')>Verified Only</option>
                    <option value="unverified" @selected(($status ?? '') === 'unverified')>Pending Verification</option>
                    <option value="2fa" @selected(($status ?? '') === '2fa')>2FA Protected</option>
                    <option value="banned" @selected(($status ?? '') === 'banned')>Banned / Suspended</option>
                    <option value="admin" @selected(($status ?? '') === 'admin')>Admins / Staff</option>
                </select>
            </div>

            <div class="col-lg-2 col-md-3 col-6">
                <select name="role" class="form-select form-select-sm" onchange="this.form.submit()">
                    <option value="">All Web Roles</option>
                    @foreach($roles as $r)
                        <option value="{{ $r->id }}" @selected(($currentRoleId ?? '') == $r->id)>{{ $r->name }}</option>
                    @endforeach
                </select>
            </div>

            <div class="col-lg-2 col-md-4 col-6">
                <select name="sort" class="form-select form-select-sm" onchange="this.form.submit()">
                    <option value="newest" @selected(($sort ?? '') === 'newest')>Newest First</option>
                    <option value="oldest" @selected(($sort ?? '') === 'oldest')>Oldest First</option>
                    <option value="name" @selected(($sort ?? '') === 'name')>Username (A-Z)</option>
                    <option value="last_login" @selected(($sort ?? '') === 'last_login')>Recently Active</option>
                </select>
            </div>

            <div class="col-lg-2 col-md-4 col-6 d-flex gap-1">
                <button type="submit" class="btn btn-primary btn-sm flex-grow-1">
                    <i class="bi bi-funnel-fill me-1"></i> Filter
                </button>
                @if(!empty($search) || !empty($status) || !empty($currentRoleId) || (!empty($sort) && $sort !== 'newest'))
                    <a href="{{ route('admin.users.index') }}" class="btn btn-outline-secondary btn-sm" title="Reset Filters">
                        <i class="bi bi-x-lg"></i>
                    </a>
                @endif
            </div>
        </form>
    </div>
</div>

{{-- Users Table --}}
<div class="card shadow-sm border-0 mb-4">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th scope="col" style="width: 50px;" class="ps-3">#</th>
                        <th scope="col">User Identity</th>
                        @if($canViewEmail || oauth_login())
                            <th scope="col">Email & Verification</th>
                        @endif
                        <th scope="col">Minecraft Account</th>
                        <th scope="col">Web Role</th>
                        <th scope="col">Security</th>
                        <th scope="col">Registered</th>
                        <th scope="col" class="text-end pe-3">Actions</th>
                    </tr>
                </thead>
                <tbody>
                @forelse($users as $user)
                    @php
                        $mc = $user->getMinecraftAccount();
                    @endphp
                    <tr>
                        <td class="ps-3">
                            <span class="text-muted small">#{{ $user->id }}</span>
                        </td>
                        <td>
                            <div class="d-flex align-items-center">
                                <img src="{{ $user->getAvatar(36) }}" alt="{{ $user->name }}" width="36" height="36" class="rounded-circle border me-2" style="object-fit: cover;">
                                <div>
                                    <div class="fw-bold @if($user->isDeleted()) text-decoration-line-through text-muted @endif">
                                        {{ $user->name }}
                                        @if($user->isAdmin())
                                            <span class="badge bg-warning text-dark ms-1" title="Administrator" data-bs-toggle="tooltip"><i class="bi bi-shield-lock-fill"></i> Admin</span>
                                        @endif
                                    </div>
                                    @if($user->isDeleted())
                                        <span class="badge bg-secondary small">Deleted Account</span>
                                    @endif
                                </div>
                            </div>
                        </td>

                        @if($canViewEmail || oauth_login())
                            <td>
                                @if(oauth_login())
                                    <code>{{ $user->game_id ?? 'N/A' }}</code>
                                @else
                                    <div class="small fw-semibold">{{ $user->email ?? 'N/A' }}</div>
                                    @if($user->email !== null)
                                        @if($user->hasVerifiedEmail())
                                            <span class="badge bg-success-subtle text-success border border-success-subtle" title="Verified: {{ format_date_compact($user->email_verified_at) }}" data-bs-toggle="tooltip">
                                                <i class="bi bi-patch-check-fill me-1"></i>Verified
                                            </span>
                                        @else
                                            <span class="badge bg-warning-subtle text-warning border border-warning-subtle" title="Email verification pending" data-bs-toggle="tooltip">
                                                <i class="bi bi-clock-history me-1"></i>Pending
                                            </span>
                                        @endif
                                    @endif
                                @endif
                            </td>
                        @endif

                        <td>
                            @if($mc)
                                <div>
                                    <span class="fw-semibold text-warning">
                                        <i class="bi bi-controller me-1"></i>{{ $mc->minecraft_username }}
                                    </span>
                                    @if($mc->rank)
                                        <span class="badge bg-dark border border-warning text-warning ms-1" style="font-size: 0.75rem;">
                                            {{ strtoupper($mc->rank) }}
                                        </span>
                                    @endif
                                </div>
                                @if($mc->minecraft_uuid)
                                    <div class="text-muted font-monospace" style="font-size: 0.75rem;" title="{{ $mc->minecraft_uuid }}" data-bs-toggle="tooltip">
                                        {{ Str::limit($mc->minecraft_uuid, 14, '...') }}
                                    </div>
                                @endif
                            @else
                                <span class="badge bg-secondary-subtle text-muted border">
                                    <i class="bi bi-link-45deg me-1"></i>Unlinked
                                </span>
                            @endif
                        </td>

                        <td>
                            <span class="badge" style="{{ $user->role->getBadgeStyle() }}">
                                @if($user->role->icon) <i class="{{ $user->role->icon }} me-1"></i> @endif
                                {{ $user->role->name }}
                            </span>
                            <div class="text-muted" style="font-size: 0.75rem;">Power: {{ $user->role->power }}</div>
                        </td>

                        <td>
                            <div class="d-flex flex-column gap-1">
                                @if($user->isBanned())
                                    <span class="badge bg-danger text-wrap" title="Reason: {{ $user->ban->reason ?? 'N/A' }}" data-bs-toggle="tooltip">
                                        <i class="bi bi-slash-circle me-1"></i>Banned
                                    </span>
                                @else
                                    <span class="badge bg-success-subtle text-success border border-success-subtle">
                                        <i class="bi bi-check-circle me-1"></i>Active
                                    </span>
                                @endif

                                @if($user->hasTwoFactorAuth())
                                    <span class="badge bg-info-subtle text-info border border-info-subtle" title="Two-Factor Authentication Active" data-bs-toggle="tooltip">
                                        <i class="bi bi-shield-check me-1"></i>2FA Active
                                    </span>
                                @endif
                            </div>
                        </td>

                        <td>
                            <div class="small">{{ format_date_compact($user->created_at) }}</div>
                            <div class="text-muted" style="font-size: 0.75rem;" title="Last Login">
                                <i class="bi bi-box-arrow-in-right me-1"></i>{{ $user->last_login_at ? format_date_compact($user->last_login_at) : 'Never' }}
                            </div>
                        </td>

                        <td class="text-end pe-3">
                            <div class="btn-group btn-group-sm">
                                <a href="{{ route('admin.users.edit', $user) }}" class="btn btn-outline-primary" title="{{ trans('messages.actions.edit') }}" data-bs-toggle="tooltip">
                                    <i class="bi bi-pencil-square"></i>
                                </a>
                                @if(!$user->hasVerifiedEmail() && $user->email !== null)
                                    <form action="{{ route('admin.users.verify', $user) }}" method="POST" class="d-inline" onsubmit="return confirm('Manually mark email as verified for {{ $user->name }}?');">
                                        @csrf
                                        <button type="submit" class="btn btn-outline-success" title="Manually Verify Email" data-bs-toggle="tooltip">
                                            <i class="bi bi-check2-all"></i>
                                        </button>
                                    </form>
                                @endif
                                <button type="button" class="btn btn-outline-secondary" data-bs-toggle="modal" data-bs-target="#userNotifyModal{{ $user->id }}" title="{{ trans('admin.users.notify') }}">
                                    <i class="bi bi-bell"></i>
                                </button>
                            </div>

                            {{-- Individual User Notify Modal --}}
                            @include('admin.users._notify', [
                                'route' => route('admin.users.notify', ['user' => $user]),
                                'all' => false,
                                'modalId' => 'userNotifyModal'.$user->id,
                                'targetUser' => $user
                            ])
                        </td>
                    </tr>
                @empty
                    <tr>
                        <td colspan="8" class="text-center py-5 text-muted">
                            <i class="bi bi-people display-6 d-block mb-2 text-secondary"></i>
                            No users matched the selected search or filter criteria.
                        </td>
                    </tr>
                @endforelse
                </tbody>
            </table>
        </div>
    </div>
    @if($users->hasPages())
        <div class="card-footer bg-transparent py-3">
            {{ $users->links() }}
        </div>
    @endif
</div>

{{-- Broadcast Notification Modal --}}
@include('admin.users._notify', ['route' => route('admin.users.notify.all'), 'all' => true, 'modalId' => 'notificationModal'])
@endsection
