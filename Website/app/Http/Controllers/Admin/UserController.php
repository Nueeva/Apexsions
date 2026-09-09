<?php

namespace Azuriom\Http\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Http\Requests\UserRequest;
use Azuriom\Models\ActionLog;
use Azuriom\Models\Notification;
use Azuriom\Models\Role;
use Azuriom\Models\User;
use Azuriom\Notifications\AlertNotification;
use Azuriom\Support\Discord\LinkedRoles;
use Illuminate\Auth\Events\PasswordReset;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Http\Request;
use Illuminate\Support\Arr;
use Illuminate\Validation\Rule;
use Illuminate\Validation\ValidationException;

class UserController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index(Request $request)
    {
        $search = $request->input('search');
        $status = $request->input('status');
        $roleId = $request->input('role');
        $sort = $request->input('sort', 'newest');

        $query = User::with(['ban', 'role', 'minecraftAccount'])
            ->scopes('registered')
            ->when($search, fn (Builder $q) => $q->scopes(['search' => $search]))
            ->when($status === 'verified', fn (Builder $q) => $q->whereNotNull('email_verified_at'))
            ->when($status === 'unverified', fn (Builder $q) => $q->whereNull('email_verified_at'))
            ->when($status === '2fa', fn (Builder $q) => $q->whereNotNull('two_factor_secret'))
            ->when($status === 'banned', fn (Builder $q) => $q->whereHas('ban'))
            ->when($status === 'admin', fn (Builder $q) => $q->whereHas('role', fn ($r) => $r->where('is_admin', true)))
            ->when($roleId, fn (Builder $q) => $q->where('role_id', $roleId));

        match ($sort) {
            'oldest' => $query->oldest(),
            'name' => $query->orderBy('name'),
            'last_login' => $query->orderByDesc('last_login_at'),
            default => $query->latest(),
        };

        $users = $query->paginate(15)->withQueryString();

        $metrics = [
            'total' => User::registered()->count(),
            'verified' => User::registered()->whereNotNull('email_verified_at')->count(),
            'unverified' => User::registered()->whereNull('email_verified_at')->count(),
            'two_factor' => User::registered()->whereNotNull('two_factor_secret')->count(),
            'banned' => User::registered()->whereHas('ban')->count(),
            'admins' => User::registered()->whereHas('role', fn ($r) => $r->where('is_admin', true))->count(),
        ];

        return view('admin.users.index', [
            'users' => $users,
            'search' => $search,
            'status' => $status,
            'currentRoleId' => $roleId,
            'sort' => $sort,
            'roles' => Role::orderByDesc('power')->get(),
            'metrics' => $metrics,
            'canViewEmail' => $request->user()->can('admin.users.email'),
            'notificationLevels' => Notification::LEVELS,
        ]);
    }

    /**
     * Send a notification to one or all users.
     *
     * @throws \Illuminate\Validation\ValidationException
     */
    public function notify(Request $request, ?User $user = null)
    {
        $this->validate($request, [
            'level' => ['required', Rule::in(Notification::LEVELS)],
            'content' => ['required', 'string', 'max:100'],
        ]);

        $users = $user !== null ? [$user] : User::registered()->lazy();
        $notification = (new AlertNotification($request->input('content')))
            ->level($request->input('level'))
            ->from($request->user());

        foreach ($users as $localUser) {
            $notification->send($localUser);
        }

        return redirect()->back()->with('success', trans('messages.status.success'));
    }

    /**
     * Show the form for creating a new resource.
     */
    public function create()
    {
        return view('admin.users.create', [
            'roles' => Role::orderByDesc('power')->get(),
            'defaultRoleId' => Role::defaultRoleId(),
        ]);
    }

    /**
     * Store a newly created resource in storage.
     *
     * @throws \Illuminate\Validation\ValidationException
     */
    public function store(UserRequest $request)
    {
        $role = Role::find($request->input('role'));

        $this->validateRole($request->user(), $role);

        User::forceCreate([
            ...Arr::except($request->validated(), 'role'),
            'role_id' => $role->id,
        ]);

        return to_route('admin.users.index')
            ->with('success', trans('messages.status.success'));
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(Request $request, User $user)
    {
        $logs = ActionLog::with('target')
            ->whereBelongsTo($user)
            ->latest()
            ->paginate();

        $adminUsersCount = User::whereRelation('role', 'is_admin', true)->count();
        $isLastAdmin = $user->isAdmin() && $adminUsersCount < 2;
        $isSelf = $request->user()->is($user);
        $canDelete = ! $user->isAdmin() && ! $isSelf;

        return view('admin.users.edit', [
            'user' => $user->load(['ban', 'role', 'minecraftAccount']),
            'minecraftAccount' => $user->getMinecraftAccount(),
            'roles' => Role::orderByDesc('power')->get(),
            'logs' => $logs,
            'notificationLevels' => Notification::LEVELS,
            'isSelf' => $isSelf,
            'isLastAdmin' => $isLastAdmin,
            'canDelete' => $canDelete,
        ]);
    }

    /**
     * Update the specified resource in storage.
     *
     * @throws \Illuminate\Validation\ValidationException
     */
    public function update(UserRequest $request, User $user)
    {
        $this->validateTarget($request->user(), $user);

        $role = Role::find($request->input('role'));

        $this->validateRole($request->user(), $role, $user);

        $user->forceFill(Arr::except($request->validated(), 'role'));
        $user->role()->associate($role);
        $user->save();

        $log = ActionLog::log('users.updated', $user);

        if ($log !== null) {
            $user->createLogEntries($log);
        }

        if ($user->wasChanged('password')) {
            event(new PasswordReset($user));

            $log?->createEntries(['password' => '**old**'], ['password' => '**new**']);
        }

        return to_route('admin.users.edit', $user)
            ->with('success', trans('messages.status.success'));
    }

    public function verifyEmail(Request $request, User $user)
    {
        $this->validateTarget($request->user(), $user);

        $user->markEmailAsVerified();

        ActionLog::log('users.updated', $user)?->createEntries([
            'email_verified_at' => null,
        ], [
            'email_verified_at' => $user->email_verified_at,
        ]);

        return to_route('admin.users.edit', $user)
            ->with('success', trans('admin.users.email.verify_success'));
    }

    public function disable2fa(Request $request, User $user)
    {
        $this->validateTarget($request->user(), $user);

        $user->forceFill([
            'two_factor_secret' => null,
            'two_factor_recovery_codes' => null,
        ])->save();

        ActionLog::log('users.updated', $user)?->createEntries([
            '2fa' => 'enabled',
        ], ['2fa' => 'disabled']);

        return to_route('admin.users.edit', $user)
            ->with('success', trans('admin.users.2fa.disabled'));
    }

    public function forcePasswordChange(Request $request, User $user)
    {
        $this->validateTarget($request->user(), $user);

        $user->update(['password_changed_at' => null]);

        return to_route('admin.users.edit', $user)
            ->with('success', trans('messages.status.success'));
    }

    public function unlinkDiscord(Request $request, User $user)
    {
        $this->validateTarget($request->user(), $user);

        if ($user->discordAccount !== null) {
            LinkedRoles::clearRole($user->discordAccount);

            $user->discordAccount->delete();
        }

        return to_route('admin.users.edit', $user)
            ->with('success', trans('messages.status.success'));
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(Request $request, User $user)
    {
        abort_if($user->isAdmin(), 403, 'Administrator accounts cannot be deleted.');
        abort_if($request->user()->is($user), 403, 'You cannot delete your own administrative account.');

        $this->validateTarget($request->user(), $user);

        $user->delete();

        ActionLog::log('users.deleted', $user);

        return to_route('admin.users.index')
            ->with('success', trans('messages.status.success'));
    }

    /**
     * Ensure a user can assign the specified role.
     *
     * @throws \Illuminate\Validation\ValidationException
     */
    protected function validateRole(User $user, Role $role, ?User $target = null): void
    {
        // Admin roles can assign any role as they already have all permissions.
        // Other users can only assign roles with lower-or-equal power.
        if ($user->role->power < $role->power && ! $user->isAdmin()) {
            throw ValidationException::withMessages([
                'role' => trans('admin.roles.unauthorized'),
            ]);
        }

        $adminUsersCount = User::whereRelation('role', 'is_admin', true)->count();

        // So many users lost access to the admin panel because they were the only admin
        if ($adminUsersCount < 2 && ! $role->is_admin && $target?->isAdmin()) {
            throw ValidationException::withMessages([
                'role' => trans('admin.roles.no_admin'),
            ]);
        }
    }

    /**
     * Ensure a user can manage the specified target.
     *
     * @throws \Illuminate\Validation\ValidationException
     */
    protected function validateTarget(User $user, User $target): void
    {
        abort_if($target->isDeleted(), 404);

        // Admin roles can manage any role regardless of their power.
        if ($user->role->power < $target->role->power && ! $user->isAdmin()) {
            throw ValidationException::withMessages([
                'role' => trans('admin.users.unauthorized'),
            ]);
        }
    }
}
