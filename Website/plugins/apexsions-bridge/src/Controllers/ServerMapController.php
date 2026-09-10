<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Services\ServerMapService;
use Illuminate\Http\RedirectResponse;

class ServerMapController extends Controller
{
    /**
     * Redirect directly to the live 3D Server Map (BlueMap).
     */
    public function index(): RedirectResponse
    {
        return redirect()->away(ServerMapService::getMapUrl());
    }
}
