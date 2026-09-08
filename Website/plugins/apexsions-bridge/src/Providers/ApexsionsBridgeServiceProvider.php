<?php

namespace Azuriom\Plugin\ApexsionsBridge\Providers;

use Azuriom\Extensions\Plugin\BasePluginServiceProvider;

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
            'apexsions-audit' => [
                'name' => 'Unified Audit Logs',
                'icon' => 'bi bi-journal-text',
                'route' => 'apexsions-bridge.admin.audit-logs.index',
                'permission' => 'admin.users',
            ],
        ];
    }
}
