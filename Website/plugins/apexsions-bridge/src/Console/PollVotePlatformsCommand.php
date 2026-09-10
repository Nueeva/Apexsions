<?php

namespace Azuriom\Plugin\ApexsionsBridge\Console;

use Azuriom\Plugin\ApexsionsBridge\Models\VotingSite;
use Azuriom\Plugin\ApexsionsBridge\Services\VoteService;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\Log;

class PollVotePlatformsCommand extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'apexsions:poll-votes {--site= : Slug platform voting tertentu yang ingin diperiksa}';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Poll external Minecraft vote platforms (Minecraft-MP, TopG, etc.) and automatically dispatch in-game rewards';

    /**
     * Execute the console command.
     */
    public function handle(VoteService $voteService): int
    {
        $this->info('Starting Apexsions Vote Platforms auto-poller...');

        $siteSlug = $this->option('site');
        $query = VotingSite::where('is_active', true);

        if ($siteSlug) {
            $query->where('slug', $siteSlug);
        }

        $sites = $query->get();

        if ($sites->isEmpty()) {
            $this->comment('No active voting sites configured.');
            return self::SUCCESS;
        }

        $totalProcessed = 0;
        $totalDuplicates = 0;

        foreach ($sites as $site) {
            if (empty($site->api_key) && $site->slug === 'minecraft-mp') {
                $this->comment("Skipping {$site->name} (No API key configured).");
                continue;
            }

            $this->line("Polling {$site->name} ({$site->slug})...");
            $result = $voteService->pollExternalVotes($site);

            if (($result['status'] ?? '') === 'success') {
                $processed = $result['processed'] ?? 0;
                $duplicates = $result['duplicates'] ?? 0;
                $totalProcessed += $processed;
                $totalDuplicates += $duplicates;
                $this->info("✓ {$site->name}: {$processed} new votes auto-rewarded, {$duplicates} duplicates skipped.");
            } else {
                $msg = $result['message'] ?? 'Unknown response';
                $this->warn("! {$site->name}: {$msg}");
            }
        }

        $this->info("Vote auto-polling finished. Total new rewards enqueued: {$totalProcessed}.");

        return self::SUCCESS;
    }
}
