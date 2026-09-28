@extends('admin.layouts.admin')

@section('title', 'Server File Manager — Apexsions')

@push('styles')
<style>
    .file-manager-card {
        background-color: #111317;
        border: 1px solid rgba(212, 175, 55, 0.2);
        border-radius: 8px;
    }
    .file-row:hover {
        background-color: rgba(212, 175, 55, 0.05);
    }
    .file-code-editor {
        font-family: 'Consolas', 'Fira Code', 'Monaco', monospace;
        font-size: 0.9rem;
        line-height: 1.5;
        background-color: #0b0d11;
        color: #f1f5f9;
        border: 1px solid #2a2e39;
        border-radius: 6px;
        width: 100%;
        height: 520px;
        padding: 14px;
        resize: vertical;
        tab-size: 2;
    }
    .file-code-editor:focus {
        outline: 1px solid #d4af37;
    }
    .breadcrumb-item a {
        color: #d4af37;
        text-decoration: none;
    }
    .breadcrumb-item a:hover {
        text-decoration: underline;
    }
</style>
@endpush

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <h2 class="h3 fw-bold text-white mb-1">
                <i class="bi bi-folder2-open text-warning me-2"></i>Server File Manager & Config Editor
            </h2>
            <p class="text-white-50 small mb-0">Eksplorasi folder server Minecraft, edit file konfigurasi YAML secara live, dan unggah plugin JAR via browser.</p>
        </div>
        <div class="d-flex gap-2 flex-wrap">
            <a href="{{ route('apexsions-bridge.admin.server.console') }}" class="btn btn-outline-warning btn-sm shadow-sm">
                <i class="bi bi-terminal-fill me-1"></i>Live Console
            </a>
            <a href="{{ route('apexsions-bridge.admin.server.index') }}" class="btn btn-outline-secondary btn-sm shadow-sm">
                <i class="bi bi-hdd-network me-1"></i>Server Dashboard
            </a>
            <button type="button" class="btn btn-outline-light btn-sm shadow-sm" data-bs-toggle="modal" data-bs-target="#newFolderModal">
                <i class="bi bi-folder-plus me-1"></i>Folder Baru
            </button>
            <button type="button" class="btn btn-warning btn-sm fw-bold text-dark shadow-sm" data-bs-toggle="modal" data-bs-target="#uploadFileModal">
                <i class="bi bi-cloud-arrow-up-fill me-1"></i>Upload File / JAR
            </button>
        </div>
    </div>

    @if(!$isConfigured)
        <!-- Config Notice -->
        <div class="alert alert-warning border-warning border-opacity-50 bg-dark text-warning p-4 rounded-3 shadow-sm mb-4">
            <div class="d-flex align-items-center gap-3">
                <i class="bi bi-exclamation-triangle-fill fs-2"></i>
                <div class="flex-grow-1">
                    <h5 class="fw-bold mb-1">Pterodactyl API Key Belum Dikonfigurasi</h5>
                    <p class="mb-0 small text-white-50">Untuk mengakses sistem berkas server hosting secara langsung dari web panel, masukkan Client API Key Pterodactyl Anda.</p>
                </div>
                <a href="{{ route('apexsions-bridge.admin.server.console') }}" class="btn btn-warning fw-bold text-dark btn-sm px-3 py-2 shadow-sm">
                    <i class="bi bi-gear-fill me-1"></i>Buka Halaman Pengaturan
                </a>
            </div>
        </div>
    @elseif(!empty($fetchError))
        <div class="alert alert-danger bg-dark border-danger text-danger p-3 rounded-3 shadow-sm mb-4">
            <i class="bi bi-exclamation-octagon-fill me-2"></i><strong>Gagal memuat berkas:</strong> {{ $fetchError }}
        </div>
    @endif

    <!-- Breadcrumb Path Navigator -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-3">
        <div class="card-body py-2 px-3">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0 align-items-center font-monospace small">
                    <li class="breadcrumb-item">
                        <a href="{{ route('apexsions-bridge.admin.server.files', ['directory' => '/']) }}">
                            <i class="bi bi-hdd-fill text-warning me-1"></i>root
                        </a>
                    </li>
                    @php
                        $segments = array_filter(explode('/', trim($currentDirectory, '/')));
                        $builtPath = '';
                    @endphp
                    @foreach($segments as $segment)
                        @php $builtPath .= '/' . $segment; @endphp
                        @if($loop->last)
                            <li class="breadcrumb-item active text-white" aria-current="page">{{ $segment }}</li>
                        @else
                            <li class="breadcrumb-item">
                                <a href="{{ route('apexsions-bridge.admin.server.files', ['directory' => $builtPath]) }}">{{ $segment }}</a>
                            </li>
                        @endif
                    @endforeach
                </ol>
            </nav>
        </div>
    </div>

    <!-- File List Table -->
    <div class="file-manager-card shadow-sm overflow-hidden mb-4">
        <div class="table-responsive">
            <table class="table table-dark table-hover mb-0 align-middle">
                <thead>
                    <tr class="border-secondary text-white-50 small text-uppercase">
                        <th style="width: 45%;">Nama Berkas / Folder</th>
                        <th style="width: 15%;">Ukuran</th>
                        <th style="width: 20%;">Terakhir Diubah</th>
                        <th style="width: 20%;" class="text-end pe-3">Aksi</th>
                    </tr>
                </thead>
                <tbody>
                    @if($currentDirectory !== '/')
                        @php
                            $parentDir = dirname($currentDirectory);
                            if ($parentDir === '\\' || $parentDir === '.') $parentDir = '/';
                        @endphp
                        <tr class="file-row">
                            <td colspan="4">
                                <a href="{{ route('apexsions-bridge.admin.server.files', ['directory' => $parentDir]) }}" class="text-warning text-decoration-none fw-bold small">
                                    <i class="bi bi-arrow-up-left-square-fill me-2"></i>.. (Kembali ke folder sebelumnya)
                                </a>
                            </td>
                        </tr>
                    @endif

                    @forelse($files as $item)
                        @php
                            $name = $item['name'] ?? '';
                            $isFile = $item['is_file'] ?? false;
                            $size = $item['size'] ?? 0;
                            $mimetype = $item['mimetype'] ?? '';
                            $modified = $item['modified_at'] ?? '';
                            $itemPath = rtrim($currentDirectory, '/') . '/' . $name;
                            
                            $isEditable = $isFile && (
                                str_ends_with(strtolower($name), '.yml') ||
                                str_ends_with(strtolower($name), '.yaml') ||
                                str_ends_with(strtolower($name), '.json') ||
                                str_ends_with(strtolower($name), '.txt') ||
                                str_ends_with(strtolower($name), '.properties') ||
                                str_ends_with(strtolower($name), '.log') ||
                                str_ends_with(strtolower($name), '.conf')
                            );

                            $iconClass = 'bi-file-earmark-text text-secondary';
                            if (!$isFile) {
                                $iconClass = 'bi-folder-fill text-warning';
                            } elseif (str_ends_with(strtolower($name), '.jar')) {
                                $iconClass = 'bi-filetype-jar text-danger';
                            } elseif (str_ends_with(strtolower($name), '.yml') || str_ends_with(strtolower($name), '.yaml')) {
                                $iconClass = 'bi-file-earmark-code-fill text-info';
                            } elseif (str_ends_with(strtolower($name), '.json')) {
                                $iconClass = 'bi-filetype-json text-success';
                            }
                        @endphp
                        <tr class="file-row">
                            <td>
                                <div class="d-flex align-items-center gap-2">
                                    <i class="bi {{ $iconClass }} fs-5"></i>
                                    @if(!$isFile)
                                        <a href="{{ route('apexsions-bridge.admin.server.files', ['directory' => $itemPath]) }}" class="text-white fw-bold text-decoration-none hover-warning">
                                            {{ $name }}
                                        </a>
                                    @elseif($isEditable)
                                        <a href="javascript:void(0)" onclick="openFileEditor('{{ addslashes($itemPath) }}', '{{ addslashes($name) }}')" class="text-white text-decoration-none">
                                            {{ $name }}
                                        </a>
                                    @else
                                        <span class="text-white-50">{{ $name }}</span>
                                    @endif
                                </div>
                            </td>
                            <td class="font-monospace small text-white-50">
                                @if(!$isFile)
                                    <span class="badge bg-secondary bg-opacity-25 text-white-50">Folder</span>
                                @elseif($size < 1024)
                                    {{ $size }} B
                                @elseif($size < 1048576)
                                    {{ round($size / 1024, 1) }} KB
                                @else
                                    {{ round($size / 1048576, 2) }} MB
                                @endif
                            </td>
                            <td class="small text-white-50 font-monospace">
                                {{ $modified ? date('Y-m-d H:i', strtotime($modified)) : '-' }}
                            </td>
                            <td class="text-end pe-3">
                                <div class="btn-group btn-group-sm">
                                    @if($isEditable)
                                        <button type="button" class="btn btn-outline-warning py-1" onclick="openFileEditor('{{ addslashes($itemPath) }}', '{{ addslashes($name) }}')" title="Edit Berkas">
                                            <i class="bi bi-pencil-square"></i> Edit
                                        </button>
                                    @endif
                                    <form action="{{ route('apexsions-bridge.admin.server.files.delete') }}" method="POST" class="d-inline" onsubmit="return confirm('Apakah Anda yakin ingin menghapus [{{ addslashes($name) }}]?');">
                                        @csrf
                                        <input type="hidden" name="root" value="{{ $currentDirectory }}">
                                        <input type="hidden" name="files[]" value="{{ $name }}">
                                        <button type="submit" class="btn btn-outline-danger py-1" title="Hapus">
                                            <i class="bi bi-trash"></i>
                                        </button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="4" class="text-center py-4 text-white-50">
                                <i class="bi bi-inbox fs-2 d-block mb-2 text-secondary"></i>
                                Direktori ini kosong atau belum ada berkas yang dimuat.
                            </td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Modal In-Browser Code Editor -->
<div class="modal fade" id="codeEditorModal" tabindex="-1" aria-labelledby="codeEditorModalLabel" aria-hidden="true" data-bs-backdrop="static">
    <div class="modal-dialog modal-xl modal-dialog-centered">
        <div class="modal-content bg-dark border-secondary">
            <div class="modal-header border-secondary py-2">
                <div class="d-flex align-items-center gap-2">
                    <i class="bi bi-file-earmark-code-fill text-warning fs-5"></i>
                    <h5 class="modal-title text-white fw-bold mb-0" id="codeEditorModalLabel">Editor Konfigurasi</h5>
                    <span id="editorPathBadge" class="badge bg-secondary font-monospace small"></span>
                </div>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body p-3">
                <div id="editorLoadingIndicator" class="text-center py-5 text-warning">
                    <div class="spinner-border spinner-border-sm me-2" role="status"></div>
                    Memuat isi berkas dari server...
                </div>
                <div id="editorContainer" style="display: none;">
                    <textarea id="fileEditorTextarea" class="file-code-editor" spellcheck="false"></textarea>
                </div>
            </div>
            <div class="modal-footer border-secondary py-2 d-flex justify-content-between">
                <div class="text-white-50 small">
                    <span id="editorSaveStatus" class="text-success fw-bold"></span>
                </div>
                <div class="d-flex gap-2">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Tutup</button>
                    <button type="button" id="btnSaveFile" class="btn btn-warning btn-sm fw-bold text-dark px-3" onclick="saveFileContent()">
                        <i class="bi bi-save me-1"></i>Simpan Perubahan
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal Upload File -->
<div class="modal fade" id="uploadFileModal" tabindex="-1" aria-labelledby="uploadFileModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-secondary">
            <form action="{{ route('apexsions-bridge.admin.server.files.upload') }}" method="POST" enctype="multipart/form-data">
                @csrf
                <input type="hidden" name="directory" value="{{ $currentDirectory }}">
                <div class="modal-header border-secondary">
                    <h5 class="modal-title text-white fw-bold" id="uploadFileModalLabel">
                        <i class="bi bi-cloud-arrow-up-fill text-warning me-2"></i>Upload File ke Server
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body text-white">
                    <div class="mb-3">
                        <label class="form-label text-white-50 small fw-bold">Target Direktori</label>
                        <input type="text" class="form-control bg-secondary bg-opacity-25 text-white border-secondary font-monospace" value="{{ $currentDirectory }}" readonly>
                    </div>
                    <div class="mb-3">
                        <label class="form-label text-white-50 small fw-bold">Pilih Berkas (.jar, .yml, .json, zip, dll)</label>
                        <input type="file" name="file" class="form-control bg-secondary bg-opacity-25 text-white border-secondary" required>
                        <div class="form-text text-muted">Maksimal 100 MB per berkas.</div>
                    </div>
                </div>
                <div class="modal-footer border-secondary">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning btn-sm fw-bold text-dark">Mulai Unggah</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal Folder Baru -->
<div class="modal fade" id="newFolderModal" tabindex="-1" aria-labelledby="newFolderModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-secondary">
            <form action="{{ route('apexsions-bridge.admin.server.files.create-folder') }}" method="POST">
                @csrf
                <input type="hidden" name="root" value="{{ $currentDirectory }}">
                <div class="modal-header border-secondary">
                    <h5 class="modal-title text-white fw-bold" id="newFolderModalLabel">
                        <i class="bi bi-folder-plus text-warning me-2"></i>Buat Folder Baru
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body text-white">
                    <div class="mb-3">
                        <label class="form-label text-white-50 small fw-bold">Nama Folder</label>
                        <input type="text" name="name" class="form-control bg-secondary bg-opacity-25 text-white border-secondary font-monospace" required placeholder="nama_folder_baru">
                    </div>
                </div>
                <div class="modal-footer border-secondary">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning btn-sm fw-bold text-dark">Buat Folder</button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection

@push('scripts')
<script>
    let activeFilePath = '';
    const editorModal = new bootstrap.Modal(document.getElementById('codeEditorModal'));
    const editorTextarea = document.getElementById('fileEditorTextarea');
    const loadingIndicator = document.getElementById('editorLoadingIndicator');
    const editorContainer = document.getElementById('editorContainer');
    const pathBadge = document.getElementById('editorPathBadge');
    const saveStatus = document.getElementById('editorSaveStatus');
    const btnSave = document.getElementById('btnSaveFile');

    async function openFileEditor(filePath, fileName) {
        activeFilePath = filePath;
        pathBadge.textContent = filePath;
        saveStatus.textContent = '';
        loadingIndicator.style.display = 'block';
        editorContainer.style.display = 'none';
        btnSave.disabled = true;

        editorModal.show();

        try {
            const res = await fetch(`{{ route('apexsions-bridge.admin.server.files.content') }}?file=${encodeURIComponent(filePath)}`);
            const data = await res.json();

            if (!data.success) {
                throw new Error(data.error || 'Gagal memuat isi berkas.');
            }

            editorTextarea.value = data.content;
            loadingIndicator.style.display = 'none';
            editorContainer.style.display = 'block';
            btnSave.disabled = false;
        } catch (err) {
            loadingIndicator.innerHTML = `<span class="text-danger"><i class="bi bi-x-circle me-1"></i>${err.message}</span>`;
        }
    }

    async function saveFileContent() {
        if (!activeFilePath) return;

        btnSave.disabled = true;
        saveStatus.className = 'text-warning small font-monospace';
        saveStatus.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Menyimpan ke server...';

        try {
            const res = await fetch("{{ route('apexsions-bridge.admin.server.files.save') }}", {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                    'X-CSRF-TOKEN': '{{ csrf_token() }}'
                },
                body: JSON.stringify({
                    file: activeFilePath,
                    content: editorTextarea.value
                })
            });

            const data = await res.json();
            if (data.success) {
                saveStatus.className = 'text-success fw-bold small font-monospace';
                saveStatus.textContent = '✓ Berhasil disimpan!';
                setTimeout(() => { saveStatus.textContent = ''; }, 3000);
            } else {
                throw new Error(data.error || 'Gagal menyimpan berkas.');
            }
        } catch (err) {
            saveStatus.className = 'text-danger fw-bold small font-monospace';
            saveStatus.textContent = `❌ ${err.message}`;
        } finally {
            btnSave.disabled = false;
        }
    }
</script>
@endpush
