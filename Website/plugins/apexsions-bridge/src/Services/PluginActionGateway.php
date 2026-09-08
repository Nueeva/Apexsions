<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Models\User;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsPlugin;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\PluginCapability;
use Carbon\Carbon;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Str;

class PluginActionGateway
{
    /**
     * Map of registered capabilities to safe execution commands and requirements.
     */
    public const ACTION_REGISTRY = [
        'core.broadcast' => [
            'command_template' => 'broadcast <%s> %s',
            'classification' => 'SAFE',
            'requires_reason' => false,
            'label' => 'Global Announcement',
        ],
        'core.reload' => [
            'command_template' => 'ac reload',
            'classification' => 'SENSITIVE',
            'requires_reason' => true,
            'label' => 'Reload ApexsionsCore',
        ],
        'core.maintenance.manage' => [
            'command_template' => 'maintenance %s',
            'classification' => 'SENSITIVE',
            'requires_reason' => true,
            'label' => 'Toggle Maintenance Mode',
        ],
        'economy.reload' => [
            'command_template' => 'ecoadmin reload',
            'classification' => 'SENSITIVE',
            'requires_reason' => true,
            'label' => 'Reload ApexsionsEconomy',
        ],
        'chat.reload' => [
            'command_template' => 'apexsionschat reload',
            'classification' => 'SENSITIVE',
            'requires_reason' => true,
            'label' => 'Reload ApexsionsChat',
        ],
        'battlepass.reload' => [
            'command_template' => 'abp reload',
            'classification' => 'SENSITIVE',
            'requires_reason' => true,
            'label' => 'Reload ApexsionsBattlepass',
        ],
        'shop.reload' => [
            'command_template' => 'shopadmin reload',
            'classification' => 'SENSITIVE',
            'requires_reason' => true,
            'label' => 'Reload ApexsionsShop',
        ],
        'shop.market.reset' => [
            'command_template' => 'shopadmin resetprices %s',
            'classification' => 'SENSITIVE',
            'requires_reason' => true,
            'label' => 'Reset Commodity Price Index',
        ],
        'media.reload' => [
            'command_template' => 'media reload',
            'classification' => 'SENSITIVE',
            'requires_reason' => true,
            'label' => 'Reload ApexsionsMedia',
        ],
        'crates.reload' => [
            'command_template' => 'crateshop reload',
            'classification' => 'SENSITIVE',
            'requires_reason' => true,
            'label' => 'Reload ApexsionsCrates',
        ],
        'enchants.reload' => [
            'command_template' => 'ace reload',
            'classification' => 'SENSITIVE',
            'requires_reason' => true,
            'label' => 'Reload ApexsionsCustomEnchants',
        ],
    ];

    /**
     * Authorize and execute a plugin action via the safe, auditable gateway.
     */
    public static function executeAction(
        string $pluginId,
        string $capabilityId,
        array $params = [],
        ?User $actor = null,
        string $reason = ''
    ): array {
        // 1. Verify Plugin Status
        $plugin = ApexsionsPlugin::where('plugin_id', $pluginId)->first();
        if (!$plugin) {
            return [
                'success' => false,
                'message' => "Plugin [{$pluginId}] tidak ditemukan dalam sistem registry.",
            ];
        }

        if ($plugin->status !== 'ENABLED') {
            return [
                'success' => false,
                'message' => "Plugin [{$plugin->name}] saat ini berstatus nonaktif ({$plugin->status}). Aksi ditolak.",
            ];
        }

        if ($plugin->health_status === 'ERROR') {
            return [
                'success' => false,
                'message' => "Plugin [{$plugin->name}] dalam status ERROR kritis. Operasi dinonaktifkan untuk menjaga stabilitas.",
            ];
        }

        // 2. Verify Capability Registration & Status
        $capability = PluginCapability::where('plugin_id', $pluginId)
            ->where('capability_id', $capabilityId)
            ->first();

        if (!$capability) {
            return [
                'success' => false,
                'message' => "Capability [{$capabilityId}] tidak terdaftar untuk plugin [{$plugin->name}].",
            ];
        }

        if ($capability->status !== 'AVAILABLE') {
            return [
                'success' => false,
                'message' => "Capability [{$capability->name}] saat ini tidak tersedia ({$capability->status}).",
            ];
        }

        if (!in_array($capability->type, ['ACTION', 'WRITE'])) {
            return [
                'success' => false,
                'message' => "Capability bertipe [{$capability->type}] tidak dapat dieksekusi sebagai aksi operasional.",
            ];
        }

        // 3. Verify Reason for Sensitive / Mandatory Actions
        if ($capability->requires_reason && empty(trim($reason))) {
            return [
                'success' => false,
                'message' => "Aksi [{$capability->name}] memerlukan alasan administratif tertulis untuk audit trail.",
            ];
        }

        // 4. Verify Whitelist & Template Mapping
        if (!isset(self::ACTION_REGISTRY[$capabilityId])) {
            return [
                'success' => false,
                'message' => "Aksi [{$capabilityId}] belum memiliki gateway template eksekusi aman yang terverifikasi.",
            ];
        }

        $actionDef = self::ACTION_REGISTRY[$capabilityId];
        $actorName = $actor ? $actor->name : 'System';
        $actorId = $actor ? (string) $actor->id : '1';
        $actionId = (string) Str::uuid();

        // 5. Build Sanitized Command Template
        $command = '';
        switch ($capabilityId) {
            case 'core.broadcast':
                $msg = trim(strip_tags($params['message'] ?? ''));
                if (empty($msg)) {
                    return ['success' => false, 'message' => 'Pesan broadcast tidak boleh kosong.'];
                }
                $command = sprintf($actionDef['command_template'], addslashes($actorName), addslashes($msg));
                break;

            case 'core.maintenance.manage':
                $state = !empty($params['enable']) ? 'enable' : 'disable';
                $command = sprintf($actionDef['command_template'], $state);
                break;

            case 'shop.market.reset':
                $scope = preg_replace('/[^a-zA-Z0-9_-]/', '', $params['category'] ?? 'all');
                $command = sprintf($actionDef['command_template'], $scope);
                break;

            default:
                $command = $actionDef['command_template'];
                break;
        }

        // 6. Start Unified Audit Log
        $audit = AuditService::start(
            'PLUGIN_ACTION_REQUESTED',
            'PLUGIN',
            $plugin->plugin_id,
            $plugin->name,
            $reason ?: "Eksekusi aksi {$capability->name}",
            null,
            [
                'action_id' => $actionId,
                'plugin_id' => $plugin->plugin_id,
                'plugin_name' => $plugin->name,
                'capability_id' => $capabilityId,
                'capability_name' => $capability->name,
                'classification' => $actionDef['classification'],
                'command' => $command,
                'staff' => $actorName,
                'staff_id' => $actorId,
                'params' => $params,
            ],
            'WEB'
        );

        // 7. Enqueue Delivery with Unique Action ID and Idempotency
        $delivery = Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'PLG_' . time() . '_' . Str::random(8),
            'command' => $command,
            'status' => 'PENDING',
            'player_uuid' => 'SERVER',
            'transaction_id' => $actionId,
        ]);

        // 8. Update Audit Log
        AuditService::success(
            $audit,
            'ENQUEUED',
            [
                'delivery_id' => $delivery->id,
                'action_id' => $actionId,
                'queued_at' => Carbon::now()->toIso8601String(),
                'status' => 'ENQUEUED',
            ]
        );

        return [
            'success' => true,
            'action_id' => $actionId,
            'message' => "Aksi [{$capability->name}] untuk plugin {$plugin->name} berhasil diotorisasi dan dimasukkan ke antrean bridge.",
            'plugin' => $plugin->name,
            'capability' => $capability->name,
        ];
    }
}
