<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Models\Setting;
use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\Shop\Models\Category;
use Azuriom\Plugin\Shop\Models\Package;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\File;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;
use Illuminate\View\View;

class WebstoreAdminController extends Controller
{
    /**
     * Display centralized Webstore Manager listing.
     */
    public function index(Request $request): View
    {
        $search = trim((string) $request->input('q', ''));
        $categoryId = $request->input('category', 'all');
        $status = $request->input('status', 'all');

        $query = Package::query()->with('category');

        if (!empty($search)) {
            $query->where(function ($q) use ($search) {
                $q->where('name', 'LIKE', "%{$search}%")
                  ->orWhere('short_description', 'LIKE', "%{$search}%")
                  ->orWhere('description', 'LIKE', "%{$search}%");
            });
        }

        if ($categoryId !== 'all' && is_numeric($categoryId)) {
            $query->where('category_id', (int) $categoryId);
        }

        if ($status === 'enabled') {
            $query->where('is_enabled', true);
        } elseif ($status === 'disabled') {
            $query->where('is_enabled', false);
        }

        $packages = $query->orderBy('category_id', 'asc')
            ->orderBy('position', 'asc')
            ->orderBy('id', 'asc')
            ->paginate(25)
            ->withQueryString();

        $categories = Category::orderBy('position', 'asc')->get();

        // Calculate statistics
        $totalPackages = Package::count();
        $activePackages = Package::where('is_enabled', true)->count();
        $totalCategories = Category::count();
        
        // Count featured packages
        $featuredCount = 0;
        foreach (Package::all() as $pkg) {
            if (setting('apexsions.webstore.pkg_' . $pkg->id . '.is_featured', '0') === '1') {
                $featuredCount++;
            }
        }

        return view('apexsions-bridge::admin.webstore.index', [
            'packages' => $packages,
            'categories' => $categories,
            'search' => $search,
            'selectedCategory' => $categoryId,
            'selectedStatus' => $status,
            'totalPackages' => $totalPackages,
            'activePackages' => $activePackages,
            'totalCategories' => $totalCategories,
            'featuredCount' => $featuredCount,
        ]);
    }

    /**
     * Show the edit form for a webstore package.
     */
    public function edit(int $id): View
    {
        $package = Package::with('category')->findOrFail($id);
        $categories = Category::orderBy('position', 'asc')->get();

        // Preset official server images list
        $officialPresets = [
            'package-diamond.jpg' => [
                'label' => 'DIAMOND — Premium Currency Resmi Apexsions',
                'preview' => '/storage/packages/package-diamond.jpg',
                'category' => 'Diamond Premium Currency',
            ],
            'package-sions.jpg' => [
                'label' => 'SIONS — The Peak Apex Donator (Emas Gelap)',
                'preview' => '/storage/packages/package-sions.jpg',
                'category' => 'Rank Kasta Donatur',
            ],
            'package-emperor.jpg' => [
                'label' => 'EMPEROR — Imperial Donator (Merah Crimson)',
                'preview' => '/storage/packages/package-emperor.jpg',
                'category' => 'Rank Kasta Donatur',
            ],
            'package-sovereign.jpg' => [
                'label' => 'SOVEREIGN — Noble Donator (Biru Kerajaan)',
                'preview' => '/storage/packages/package-sovereign.jpg',
                'category' => 'Rank Kasta Donatur',
            ],
            'package-archon.jpg' => [
                'label' => 'ARCHON — Guardian Donator (Cyan Es)',
                'preview' => '/storage/packages/package-archon.jpg',
                'category' => 'Rank Kasta Donatur',
            ],
            'package-ascendant.jpg' => [
                'label' => 'ASCENDANT — Pioneer Donator (Hijau Zamrud)',
                'preview' => '/storage/packages/package-ascendant.jpg',
                'category' => 'Rank Kasta Donatur',
            ],
            'package-sio-pass.jpg' => [
                'label' => 'SIO PASS — Gold Battlepass Musiman (100 Tier)',
                'preview' => '/storage/packages/package-sio-pass.jpg',
                'category' => 'Battlepass Musiman',
            ],
            'package-exsio-pass.jpg' => [
                'label' => 'EXSIO PASS — Mythic Ultimate Pass (+20 Tier + Kosmetik)',
                'preview' => '/storage/packages/package-exsio-pass.jpg',
                'category' => 'Battlepass Musiman',
            ],
        ];

        // Scan storage/packages for any other uploaded files
        $storageDir = storage_path('app/public/packages');
        $allPresets = $officialPresets;
        if (File::isDirectory($storageDir)) {
            $files = File::files($storageDir);
            foreach ($files as $file) {
                $filename = $file->getFilename();
                if (!isset($allPresets[$filename])) {
                    $allPresets[$filename] = [
                        'label' => 'Unggahan Khusus: ' . $filename,
                        'preview' => '/storage/packages/' . $filename,
                        'category' => 'Unggahan Kustom',
                    ];
                }
            }
        }

        // WhatsApp Admin Options
        $waAdminOptions = [
            '6281212994597' => 'Founder Rifqi (6281212994597)',
            '6285883161047' => 'Founder Friell (6285883161047)',
            '6287729112281' => 'Admin Favian (6287729112281)',
        ];

        // Settings metadata
        $customBadge = setting('apexsions.webstore.pkg_' . $package->id . '.badge', '');
        $waAdmin = setting('apexsions.webstore.pkg_' . $package->id . '.wa_admin', '6281212994597');
        $waTemplate = setting('apexsions.webstore.pkg_' . $package->id . '.wa_template', '');
        $isFeatured = setting('apexsions.webstore.pkg_' . $package->id . '.is_featured', '0') === '1';
        $discountPercent = setting('apexsions.webstore.pkg_' . $package->id . '.discount', '');

        // Commands as textarea string
        $commandsRaw = is_array($package->commands) ? implode("\n", $package->commands) : '';

        return view('apexsions-bridge::admin.webstore.edit', [
            'package' => $package,
            'categories' => $categories,
            'presets' => $allPresets,
            'waAdminOptions' => $waAdminOptions,
            'customBadge' => $customBadge,
            'waAdmin' => $waAdmin,
            'waTemplate' => $waTemplate,
            'isFeatured' => $isFeatured,
            'discountPercent' => $discountPercent,
            'commandsRaw' => $commandsRaw,
        ]);
    }

    /**
     * Update the webstore package.
     */
    public function update(Request $request, int $id): RedirectResponse
    {
        $package = Package::findOrFail($id);

        $validated = $request->validate([
            'name' => ['required', 'string', 'max:100'],
            'short_description' => ['nullable', 'string', 'max:255'],
            'description' => ['nullable', 'string'],
            'price' => ['required', 'numeric', 'min:0'],
            'category_id' => ['required', 'integer', 'exists:shop_categories,id'],
            'position' => ['required', 'integer', 'min:0'],
            'user_limit' => ['nullable', 'integer', 'min:0'],
            'image_file' => ['nullable', 'image', 'max:4096', 'mimes:jpeg,jpg,png,webp'],
            'image_preset' => ['nullable', 'string', 'max:255'],
            'custom_badge' => ['nullable', 'string', 'max:64'],
            'wa_admin' => ['nullable', 'string', 'max:32'],
            'wa_template' => ['nullable', 'string', 'max:500'],
            'discount_percent' => ['nullable', 'numeric', 'min:0', 'max:100'],
            'commands_raw' => ['nullable', 'string'],
        ]);

        $oldValues = [
            'name' => $package->name,
            'price' => $package->price,
            'image' => $package->image,
            'is_enabled' => $package->is_enabled,
        ];

        // 1. Handle Image: Upload > Preset > Remove > Existing
        if ($request->hasFile('image_file')) {
            $file = $request->file('image_file');
            $extension = $file->getClientOriginalExtension();
            $fileName = 'package-' . $package->id . '-' . time() . '.' . $extension;
            
            // Ensure target directory exists
            $storageDir = storage_path('app/public/packages');
            if (!File::isDirectory($storageDir)) {
                File::makeDirectory($storageDir, 0755, true);
            }
            
            $file->move($storageDir, $fileName);
            $package->image = $fileName;
        } elseif ($request->filled('image_preset')) {
            $presetName = $request->input('image_preset');
            $package->image = $presetName;

            // Ensure preset is physically present in storage/app/public/packages
            $destPath = storage_path('app/public/packages/' . $presetName);
            if (!File::exists($destPath)) {
                $themeAssetPath = public_path('assets/themes/apexsions/img/' . $presetName);
                if (File::exists($themeAssetPath)) {
                    File::copy($themeAssetPath, $destPath);
                }
            }
        } elseif ($request->boolean('remove_image')) {
            $package->image = null;
        }

        // 2. Handle In-Game Console Commands
        $commands = [];
        if ($request->filled('commands_raw')) {
            $lines = preg_split('/[\r\n]+/', (string) $request->input('commands_raw'));
            foreach ($lines as $line) {
                $trim = trim($line);
                if ($trim !== '') {
                    $commands[] = $trim;
                }
            }
        }
        $package->commands = $commands;

        // 3. Update Standard Attributes
        $package->name = $validated['name'];
        $package->short_description = $validated['short_description'] ?? null;
        $package->description = $validated['description'] ?? '';
        $package->price = (float) $validated['price'];
        $package->category_id = (int) $validated['category_id'];
        $package->position = (int) $validated['position'];
        $package->user_limit = !empty($validated['user_limit']) ? (int) $validated['user_limit'] : null;
        $package->is_enabled = $request->boolean('is_enabled');
        $package->save();

        // 4. Update Extended Metadata via Settings
        Setting::updateSettings([
            'apexsions.webstore.pkg_' . $package->id . '.badge' => $request->input('custom_badge'),
            'apexsions.webstore.pkg_' . $package->id . '.wa_admin' => $request->input('wa_admin'),
            'apexsions.webstore.pkg_' . $package->id . '.wa_template' => $request->input('wa_template'),
            'apexsions.webstore.pkg_' . $package->id . '.is_featured' => $request->boolean('is_featured') ? '1' : '0',
            'apexsions.webstore.pkg_' . $package->id . '.discount' => $request->input('discount_percent'),
        ]);

        // 5. Audit Logging
        AuditLog::create([
            'action_id' => (string) Str::uuid(),
            'actor_type' => 'USER',
            'actor_id' => (string) (auth()->id() ?? 0),
            'actor_name' => auth()->user()->name ?? 'Administrator',
            'action' => 'WEBSTORE_PACKAGE_UPDATE',
            'target_type' => 'SHOP_PACKAGE',
            'target_id' => (string) $package->id,
            'target_name' => $package->name,
            'old_value' => json_encode($oldValues),
            'new_value' => json_encode([
                'name' => $package->name,
                'price' => $package->price,
                'image' => $package->image,
                'is_enabled' => $package->is_enabled,
                'badge' => $request->input('custom_badge'),
                'is_featured' => $request->boolean('is_featured'),
            ]),
            'reason' => 'Pembaruan paket webstore via Webstore Manager Admin Panel',
            'source' => 'WEB_ADMIN',
            'status' => 'SUCCESS',
        ]);

        return redirect()->route('apexsions-bridge.admin.webstore.index')
            ->with('success', "Paket '{$package->name}' berhasil diperbarui!");
    }

    /**
     * Quick toggle enabled status.
     */
    public function toggleStatus(int $id): RedirectResponse
    {
        $package = Package::findOrFail($id);
        $package->is_enabled = !$package->is_enabled;
        $package->save();

        AuditLog::create([
            'action_id' => (string) Str::uuid(),
            'actor_type' => 'USER',
            'actor_id' => (string) (auth()->id() ?? 0),
            'actor_name' => auth()->user()->name ?? 'Administrator',
            'action' => 'WEBSTORE_PACKAGE_TOGGLE_STATUS',
            'target_type' => 'SHOP_PACKAGE',
            'target_id' => (string) $package->id,
            'target_name' => $package->name,
            'old_value' => json_encode(['is_enabled' => !$package->is_enabled]),
            'new_value' => json_encode(['is_enabled' => $package->is_enabled]),
            'reason' => 'Toggle status keaktifan paket webstore',
            'source' => 'WEB_ADMIN',
            'status' => 'SUCCESS',
        ]);

        $statusText = $package->is_enabled ? 'diaktifkan' : 'dinonaktifkan';
        return redirect()->back()->with('success', "Paket '{$package->name}' berhasil {$statusText}.");
    }

    /**
     * Quick toggle featured status.
     */
    public function toggleFeatured(int $id): RedirectResponse
    {
        $package = Package::findOrFail($id);
        $current = setting('apexsions.webstore.pkg_' . $id . '.is_featured', '0');
        $new = $current === '1' ? '0' : '1';

        Setting::updateSettings([
            'apexsions.webstore.pkg_' . $id . '.is_featured' => $new,
        ]);

        $statusText = $new === '1' ? 'dijadikan produk unggulan' : 'dihapus dari produk unggulan';
        return redirect()->back()->with('success', "Paket '{$package->name}' berhasil {$statusText}.");
    }
}
