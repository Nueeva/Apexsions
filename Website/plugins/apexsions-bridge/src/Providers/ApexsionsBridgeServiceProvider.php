<?php

namespace Azuriom\Plugin\ApexsionsBridge\Providers;

use Azuriom\Extensions\Plugin\BasePluginServiceProvider;
use Azuriom\Models\Permission;
use Azuriom\Plugin\ApexsionsBridge\Console\CleanEventsCommand;
use Illuminate\Console\Scheduling\Schedule;

class ApexsionsBridgeServiceProvider extends BasePluginServiceProvider
{
    /**
     * Register any plugin services.
     */
    public function register(): void
    {
        //
    }

    /**
     * Bootstrap any plugin services.
     */
    public function boot(): void
    {
        $this->loadViews();
        $this->loadTranslations();
        $this->loadMigrations();

        $this->registerRouteDescriptions();
        $this->registerUserNavigation();
        $this->registerAdminNavigation();
        $this->registerSchedule();

        // Register console commands
        if ($this->app->runningInConsole()) {
            $this->commands([
                CleanEventsCommand::class,
            ]);
        }

        // Register granular permissions for Apexsions administration
        $this->registerPermissions();
    }

    /**
     * Define the application's command schedule for background retention and maintenance.
     */
    protected function schedule(Schedule $schedule): void
    {
        $schedule->command('apexsions:clean-events --days=30')->daily();
    }

    /**
     * Register granular permissions for Apexsions administration modules.
     */
    protected function registerPermissions(): void
    {
        Permission::registerPermissions([
            'apexsions.players.view' => 'Lihat Data Pemain Realm',
            'apexsions.players.manage' => 'Kelola & Eksekusi Aksi Pemain',
            'apexsions.reports.view' => 'Lihat Laporan Pemain',
            'apexsions.reports.manage' => 'Kelola & Tindak Lanjuti Laporan',
            'apexsions.moderation.view' => 'Lihat Hukuman & Catatan Moderasi',
            'apexsions.moderation.manage' => 'Terapkan & Cabut Hukuman',
            'apexsions.economy.view' => 'Pantau Transaksi & Pelelangan',
            'apexsions.economy.manage' => 'Penyesuaian Saldo & Karantina Lelang',
            'apexsions.server.view' => 'Pantau Telemetry & Health Server',
            'apexsions.server.manage' => 'Aksi Operasional Server & Maintenance',
            'apexsions.plugins.view' => 'Lihat Plugin Suite & Kapabilitas',
            'apexsions.plugins.manage' => 'Eksekusi Aksi Gateway Plugin',
            'apexsions.audit.view' => 'Lihat Unified Audit Logs',
            'apexsions.intelligence.view' => 'Pantau Intelligence & Anomaly Desk',
            'apexsions.incidents.view' => 'Lihat Berkas Insiden',
            'apexsions.incidents.manage' => 'Kelola Investigasi, Penugasan & Resolusi Insiden',
        ]);
    }

    /**
     * Returns the routes that should be able to be added to the navbar.
     */
    protected function routeDescriptions(): array
    {
        return [
            'apexsions-bridge.link.index' => 'Link Minecraft Account',
        ];
    }

    /**
     * Return the user navigations routes to register in user menu.
     */
    protected function userNavigation(): array
    {
        return [
            'apexsions-bridge' => [
                'name' => 'Link Minecraft',
                'icon' => 'bi bi-controller',
                'route' => 'apexsions-bridge.link.index',
            ],
        ];
    }

    /**
     * Return the admin navigation routes to register in the dashboard sidebar.
     *
     * @return array<string, array<string, string>>
     */
    protected function adminNavigation(): array
    {
        return [
            'apexsions-players' => [
                'name' => 'Player Management',
                'icon' => 'bi bi-person-lines-fill',
                'route' => 'apexsions-bridge.admin.players.index',
                'permission' => 'admin.users',
            ],
            'apexsions-reports' => [
                'name' => 'Reports Center',
                'icon' => 'bi bi-flag-fill',
                'route' => 'apexsions-bridge.admin.reports.index',
                'permission' => 'admin.users',
            ],
            'apexsions-moderation' => [
                'name' => 'Moderation Center',
                'icon' => 'bi bi-shield-shaded',
                'route' => 'apexsions-bridge.admin.moderation.index',
                'permission' => 'admin.users',
            ],
            'apexsions-economy' => [
                'name' => 'Economy Inspector',
                'icon' => 'bi bi-cash-stack',
                'route' => 'apexsions-bridge.admin.economy.index',
                'permission' => 'admin.users',
            ],
            'apexsions-transactions' => [
                'name' => 'Transaction Explorer',
                'icon' => 'bi bi-receipt-cutoff',
                'route' => 'apexsions-bridge.admin.economy.transactions.index',
                'permission' => 'admin.users',
            ],
            'apexsions-auctions' => [
                'name' => 'Auction Inspector',
                'icon' => 'bi bi-shop',
                'route' => 'apexsions-bridge.admin.economy.auctions.index',
                'permission' => 'admin.users',
            ],
            'apexsions-server' => [
                'name' => 'Server Operations',
                'icon' => 'bi bi-hdd-network-fill',
                'route' => 'apexsions-bridge.admin.server.index',
                'permission' => 'admin.users',
            ],
            'apexsions-plugins' => [
                'name' => 'Custom Plugins',
                'icon' => 'bi bi-cpu-fill',
                'route' => 'apexsions-bridge.admin.plugins.index',
                'permission' => 'admin.users',
            ],
            'apexsions-audit' => [
                'name' => 'Unified Audit Logs',
                'icon' => 'bi bi-journal-text',
                'route' => 'apexsions-bridge.admin.audit-logs.index',
                'permission' => 'admin.users',
            ],
            'apexsions-intelligence' => [
                'name' => 'Intelligence Desk',
                'icon' => 'bi bi-shield-check',
                'route' => 'apexsions-bridge.admin.intelligence.index',
                'permission' => 'admin.users',
            ],
            'apexsions-incidents' => [
                'name' => 'Incident Registry',
                'icon' => 'bi bi-exclamation-octagon-fill',
                'route' => 'apexsions-bridge.admin.incidents.index',
                'permission' => 'admin.users',
            ],
        ];
    }
}
