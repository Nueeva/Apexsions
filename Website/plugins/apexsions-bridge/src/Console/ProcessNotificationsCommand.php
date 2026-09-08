<?php

namespace Azuriom\Plugin\ApexsionsBridge\Console;

use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsNotification;
use Azuriom\Plugin\ApexsionsBridge\Models\NotificationDelivery;
use Azuriom\Plugin\ApexsionsBridge\Services\AuditService;
use Azuriom\Plugin\ApexsionsBridge\Services\NotificationDispatcherService;
use Carbon\Carbon;
use Illuminate\Console\Command;

class ProcessNotificationsCommand extends Command
{
    /**
     * The name and signature of the console command.
     */
    protected $signature = 'apexsions:process-notifications {--dry-run : Simulate processing without updating state}';

    /**
     * The console command description.
     */
    protected $description = 'Process retry queues for pending/failed notification deliveries and monitor unacknowledged critical alerts';

    /**
     * Execute the console command.
     */
    public function handle(): int
    {
        $dryRun = (bool) $this->option('dry-run');
        $this->info("Starting Apexsions notification queue processing" . ($dryRun ? " [DRY-RUN]" : ""));

        // 1. Process Retrying Deliveries
        $retryingDeliveries = NotificationDelivery::where('status', 'RETRYING')
            ->where(function ($q) {
                $q->whereNull('next_retry_at')
                  ->orWhere('next_retry_at', '<=', Carbon::now());
            })
            ->where('attempt_count', '<', 3)
            ->get();

        $this->line("Found {$retryingDeliveries->count()} deliveries eligible for retry.");

        $retriedCount = 0;
        foreach ($retryingDeliveries as $delivery) {
            if ($dryRun) {
                $this->line(" [DRY-RUN] Would retry delivery {$delivery->delivery_id} ({$delivery->channel})");
                $retriedCount++;
                continue;
            }

            $notification = $delivery->notification;
            if (!$notification) {
                $delivery->update(['status' => 'FAILED', 'error_summary' => 'Parent notification missing']);
                continue;
            }

            $delivery->increment('attempt_count');
            $delivery->update(['last_attempt_at' => Carbon::now()]);

            // Re-attempt dispatching to the channel
            $newDeliveries = NotificationDispatcherService::dispatch($notification, [$delivery->channel], false);
            $retriedCount++;
        }

        // 2. Mark dead-letter deliveries
        if (!$dryRun) {
            $exhausted = NotificationDelivery::where('status', 'RETRYING')
                ->where('attempt_count', '>=', 3)
                ->update(['status' => 'FAILED', 'error_summary' => 'Max delivery attempts (3) reached. Dead-lettered.']);
            if ($exhausted > 0) {
                $this->line("Moved {$exhausted} exhausted deliveries to FAILED status.");
            }
        }

        // 3. Monitor Unacknowledged Critical Alerts (> 15 minutes)
        $unackedCritical = ApexsionsNotification::where('severity', 'CRITICAL')
            ->where('status', 'UNACKNOWLEDGED')
            ->where('last_occurred_at', '<=', Carbon::now()->subMinutes(15))
            ->get();

        $this->line("Found {$unackedCritical->count()} unacknowledged critical notifications requiring escalation monitoring.");

        if (!$dryRun && ($retriedCount > 0 || $unackedCritical->count() > 0)) {
            AuditService::log([
                'action' => 'NOTIFICATIONS_QUEUE_PROCESSED',
                'category' => 'SYSTEM',
                'source' => 'CONSOLE',
                'status' => 'SUCCESS',
                'metadata' => [
                    'retried_deliveries' => $retriedCount,
                    'unacknowledged_critical_alerts' => $unackedCritical->count(),
                ],
            ]);
        }

        $this->info("Apexsions notification queue processing finished successfully.");
        return 0;
    }
}
