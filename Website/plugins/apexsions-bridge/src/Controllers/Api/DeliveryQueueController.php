<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Api;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Carbon\Carbon;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class DeliveryQueueController extends Controller
{
    /**
     * Authenticate Minecraft server via pre-shared bridge secret.
     */
    protected function authenticateServer(Request $request): bool
    {
        $serverKey = $request->header('X-Apexsions-Key');
        $expectedKey = config('apexsions-bridge.server_key', env('APEXSIONS_BRIDGE_KEY', 'apexsions_bridge_key_live_2026'));

        return !empty($serverKey) && hash_equals($expectedKey, $serverKey);
    }

    /**
     * Fetch pending commands/deliveries waiting to be executed in-game.
     */
    public function pending(Request $request): JsonResponse
    {
        if (!$this->authenticateServer($request)) {
            return response()->json(['error' => 'Unauthorized server request.'], 401);
        }

        $deliveries = Delivery::where('status', 'PENDING')
            ->orderBy('id', 'asc')
            ->limit(25)
            ->get();

        return response()->json([
            'status' => 'success',
            'count' => $deliveries->count(),
            'deliveries' => $deliveries,
        ]);
    }

    /**
     * Mark a delivery as successfully executed.
     */
    public function complete(Request $request, int $id): JsonResponse
    {
        if (!$this->authenticateServer($request)) {
            return response()->json(['error' => 'Unauthorized server request.'], 401);
        }

        $delivery = Delivery::find($id);
        if (!$delivery) {
            return response()->json(['error' => 'Delivery item not found.'], 404);
        }

        $delivery->update([
            'status' => 'COMPLETED',
            'executed_at' => Carbon::now(),
            'error_message' => null,
        ]);

        return response()->json([
            'status' => 'success',
            'message' => 'Delivery status updated to COMPLETED.',
        ]);
    }

    /**
     * Mark a delivery as failed with diagnostic error details.
     */
    public function fail(Request $request, int $id): JsonResponse
    {
        if (!$this->authenticateServer($request)) {
            return response()->json(['error' => 'Unauthorized server request.'], 401);
        }

        $delivery = Delivery::find($id);
        if (!$delivery) {
            return response()->json(['error' => 'Delivery item not found.'], 404);
        }

        $errorMsg = $request->input('error', 'Execution failed on Minecraft server.');

        $delivery->update([
            'status' => 'FAILED',
            'executed_at' => Carbon::now(),
            'error_message' => substr($errorMsg, 0, 500),
        ]);

        return response()->json([
            'status' => 'success',
            'message' => 'Delivery status updated to FAILED.',
        ]);
    }
}
