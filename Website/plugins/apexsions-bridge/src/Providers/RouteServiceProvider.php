<?php

namespace Azuriom\Plugin\ApexsionsBridge\Providers;

use Azuriom\Extensions\Plugin\BaseRouteServiceProvider;
use Illuminate\Support\Facades\Route;

class RouteServiceProvider extends BaseRouteServiceProvider
{
    /**
     * Define the routes for the plugin.
     */
    public function loadRoutes(): void
    {
        Route::middleware('web')
            ->name("{$this->plugin->id}.")
            ->group(plugin_path($this->plugin->id.'/routes/web.php'));

        Route::prefix("api/{$this->plugin->id}")
            ->middleware('api')
            ->name("{$this->plugin->id}.api.")
            ->group(plugin_path($this->plugin->id.'/routes/api.php'));
    }
}
