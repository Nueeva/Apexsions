<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Services\ServerMapService;
use Illuminate\Contracts\View\View;

class ServerMapController extends Controller
{
    /**
     * Show the Server Map landing and inspection page.
     */
    public function index(): View
    {
        $mapUrl = ServerMapService::getMapUrl();
        $isOnline = ServerMapService::isMapOnline();
        $isEnabled = ServerMapService::isMapEnabled();

        return view('apexsions-bridge::server-map', [
            'mapUrl' => $mapUrl,
            'isOnline' => $isOnline,
            'isEnabled' => $isEnabled,
        ]);
    }
}
