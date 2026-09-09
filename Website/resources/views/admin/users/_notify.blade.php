<div class="modal fade" id="{{ $modalId ?? 'notificationModal' }}" tabindex="-1" role="dialog" aria-labelledby="{{ $modalId ?? 'notificationModal' }}Label" aria-modal="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="{{ $modalId ?? 'notificationModal' }}Label">
                    <i class="bi bi-bell-fill text-warning me-1"></i>
                    {{ ($all ?? false) ? trans('admin.users.notify_all') : trans('admin.users.notify_info') }}
                    @if(!empty($targetUser))
                        <span class="text-primary font-monospace">({{ $targetUser->name }})</span>
                    @endif
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <form method="POST" action="{{ $route }}">
                    @csrf

                    <div class="mb-3">
                        <label class="form-label" for="contentInput{{ $modalId ?? 'all' }}">{{ trans('messages.fields.content') }}</label>
                        <input type="text" class="form-control" id="contentInput{{ $modalId ?? 'all' }}" name="content" required maxlength="100" placeholder="Pesan notifikasi (maks. 100 karakter)">
                    </div>

                    <div class="mb-3">
                        <label class="form-label" for="levelSelect{{ $modalId ?? 'all' }}">{{ trans('messages.notifications.level') }}</label>
                        <select class="form-select" id="levelSelect{{ $modalId ?? 'all' }}" name="level" required>
                            @foreach($notificationLevels as $level)
                                <option value="{{ $level }}" @selected($level === 'info')>
                                    {{ trans('messages.notifications.'.$level) }}
                                </option>
                            @endforeach
                        </select>
                    </div>

                    <div class="d-flex justify-content-end gap-2">
                        <button class="btn btn-secondary" type="button" data-bs-dismiss="modal">
                            {{ trans('messages.actions.cancel') }}
                        </button>
                        <button class="btn btn-warning" type="submit">
                            <i class="bi bi-send-fill me-1"></i> {{ trans('messages.actions.send') }}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
