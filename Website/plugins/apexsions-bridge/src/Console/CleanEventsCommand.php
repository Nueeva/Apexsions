<?php

namespace Azuriom\Plugin\ApexsionsBridge\Console;

use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsEvent;
use Azuriom\Plugin\ApexsionsBridge\Services\AuditService;
use Carbon\Carbon;
use Illuminate\Console\Command;

class CleanEventsCommand extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'apexsions:clean-events 
                            {--days=30 : Usia retensi dalam hari untuk event biasa tanpa keterkaitan insiden}
                            {--batch=500 : Ukuran batch chunk per siklus penghapusan}
                            {--dry-run : Simulasikan pembersihan tanpa menghapus data dari database}';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Pembersihan aman event ternormalisasi usang dengan proteksi ketat insiden dan audit trail.';

    /**
     * Execute the console command.
     */
    public function handle(): int
    {
        $days = (int) $this->option('days');
        $batchSize = (int) $this->option('batch');
        $isDryRun = (bool) $this->option('dry-run');

        if ($days < 7) {
            $this->error('Batas usia retensi minimal adalah 7 hari untuk menjaga integritas investigasi.');
            return self::FAILURE;
        }

        $cutoff = Carbon::now()->subDays($days);

        $this->info("Menyiapkan pembersihan event yang lebih tua dari: {$cutoff->toDateTimeString()} ({$days} hari)");
        if ($isDryRun) {
            $this->warn('[DRY-RUN MODE] Tidak ada data yang akan dihapus dari database.');
        }

        // Query criteria:
        // 1. Older than cutoff
        // 2. MUST NOT be linked to an incident (incident_id is null)
        // 3. MUST NOT be CRITICAL or HIGH severity
        $query = ApexsionsEvent::where('occurred_at', '<', $cutoff)
            ->whereNull('incident_id')
            ->whereNotIn('severity', ['CRITICAL', 'HIGH']);

        $totalCandidates = $query->count();

        if ($totalCandidates === 0) {
            $this->info('Tidak ada event usang yang memenuhi kriteria pembersihan.');
            return self::SUCCESS;
        }

        $this->line("Ditemukan {$totalCandidates} event yang dapat dibersihkan secara aman.");

        if ($isDryRun) {
            $this->info("Simulasi selesai. Sebanyak {$totalCandidates} event dapat dibersihkan.");
            return self::SUCCESS;
        }

        $deletedCount = 0;

        // Process in safe batches to prevent transaction log or memory spikes
        while (true) {
            $ids = ApexsionsEvent::where('occurred_at', '<', $cutoff)
                ->whereNull('incident_id')
                ->whereNotIn('severity', ['CRITICAL', 'HIGH'])
                ->limit($batchSize)
                ->pluck('id');

            if ($ids->isEmpty()) {
                break;
            }

            $affected = ApexsionsEvent::whereIn('id', $ids)->delete();
            $deletedCount += $affected;

            $this->line("Membersihkan batch: {$affected} entri (Total: {$deletedCount}/{$totalCandidates})...");
        }

        // Log auditable maintenance action
        AuditService::log([
            'action' => 'DATA_RETENTION_CLEANUP',
            'target_type' => 'EVENT_LEDGER',
            'target_id' => 'GLOBAL',
            'target_name' => 'Event Retention Engine',
            'reason' => "Pembersihan aman {$deletedCount} event usang (> {$days} hari, non-incident, non-critical)",
            'source' => 'SYSTEM',
            'status' => 'SUCCESS',
            'metadata' => [
                'deleted_count' => $deletedCount,
                'retention_days' => $days,
                'cutoff_date' => $cutoff->toIso8601String(),
            ],
        ]);

        $this->info("Pembersihan data retensi berhasil. Total {$deletedCount} event berhasil dihapus.");
        return self::SUCCESS;
    }
}
