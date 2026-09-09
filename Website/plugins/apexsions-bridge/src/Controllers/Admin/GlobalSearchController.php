<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\Plugin\ApexsionsBridge\Models\Report;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class GlobalSearchController extends Controller
{
    /**
     * Search across players, navigation routes, and reports.
     */
    public function search(Request $request): JsonResponse
    {
        $query = trim($request->input('q', ''));

        if (mb_strlen($query) < 2) {
            return response()->json([
                'players' => [],
                'navigation' => [],
                'reports' => [],
            ]);
        }

        // 1. Search Players
        $players = MinecraftAccount::query()
            ->where('minecraft_username', 'LIKE', "%{$query}%")
            ->orWhere('minecraft_uuid', 'LIKE', "%{$query}%")
            ->limit(5)
            ->get()
            ->map(function ($player) {
                return [
                    'title' => $player->minecraft_username,
                    'subtitle' => 'Rank: ' . ($player->rank_display ?: ($player->rank ?: 'Wanderer')) . ' • Lvl ' . ($player->level ?? 1),
                    'url' => route('apexsions-bridge.admin.players.show', $player->minecraft_uuid ?: $player->id),
                    'icon' => 'bi bi-person-badge',
                    'avatar' => 'https://mc-heads.net/avatar/' . urlencode($player->minecraft_username) . '/20',
                    'type' => 'player',
                ];
            });

        // 2. Navigation items index
        $navRegistry = [
            [
                'title' => 'Admin Dashboard',
                'subtitle' => 'Live realm telemetry, TPS gauge, RAM usage & population',
                'url' => route('admin.dashboard'),
                'icon' => 'bi bi-speedometer2',
                'keywords' => ['dashboard', 'home', 'tps', 'ram', 'telemetry', 'monitor', 'uptime'],
            ],
            [
                'title' => 'Player Management',
                'subtitle' => 'Inspect player dossiers, balances, inventories & battlepass',
                'url' => route('apexsions-bridge.admin.players.index'),
                'icon' => 'bi bi-person-lines-fill',
                'keywords' => ['players', 'user', 'profile', 'inspect', 'balance', 'inventory', 'dossier', 'battlepass'],
            ],
            [
                'title' => 'Rank Management',
                'subtitle' => 'Official hierarchy, permissions weights & assign ranks',
                'url' => route('apexsions-bridge.admin.ranks.index'),
                'icon' => 'bi bi-trophy-fill',
                'keywords' => ['ranks', 'role', 'hierarchy', 'luckperms', 'ancestor', 'architect', 'warden', 'sions'],
            ],
            [
                'title' => 'Moderation Desk',
                'subtitle' => 'Sanctions, player bans, mutes, kicks & pardons',
                'url' => route('apexsions-bridge.admin.moderation.index'),
                'icon' => 'bi bi-shield-shaded',
                'keywords' => ['moderation', 'punish', 'ban', 'mute', 'kick', 'pardon', 'sanctions', 'blacklist'],
            ],
            [
                'title' => 'Reports Center',
                'subtitle' => 'Handle player-submitted incident reports & triage',
                'url' => route('apexsions-bridge.admin.reports.index'),
                'icon' => 'bi bi-flag-fill',
                'keywords' => ['reports', 'complaints', 'triage', 'cheat', 'grief', 'ticket'],
            ],
            [
                'title' => 'Economy Overview',
                'subtitle' => 'Multi-currency metrics, circulating rupiah & diamonds',
                'url' => route('apexsions-bridge.admin.economy.index'),
                'icon' => 'bi bi-cash-stack',
                'keywords' => ['economy', 'money', 'rupiah', 'diamond', 'currency', 'treasury', 'tax'],
            ],
            [
                'title' => 'Transaction Explorer',
                'subtitle' => 'Audit ledger for market trades, transfers & escrow',
                'url' => route('apexsions-bridge.admin.economy.transactions.index'),
                'icon' => 'bi bi-arrow-left-right',
                'keywords' => ['transactions', 'transfer', 'trades', 'ledger', 'payment', 'history'],
            ],
            [
                'title' => 'Auction Inspector',
                'subtitle' => 'Review active auction listings, sellers & quarantine lots',
                'url' => route('apexsions-bridge.admin.economy.auctions.index'),
                'icon' => 'bi bi-shop-window',
                'keywords' => ['auctions', 'ah', 'market', 'listings', 'quarantine', 'sales'],
            ],
            [
                'title' => 'Server Operations',
                'subtitle' => 'Toggle maintenance, broadcast announcements & runtime control',
                'url' => route('apexsions-bridge.admin.server.index'),
                'icon' => 'bi bi-hdd-network-fill',
                'keywords' => ['server', 'maintenance', 'restart', 'broadcast', 'operations', 'health'],
            ],
            [
                'title' => 'Custom Plugin Suite',
                'subtitle' => 'ApexsionsCore, Chat, Economy, Shop, Media & Battlepass status',
                'url' => route('apexsions-bridge.admin.plugins.index'),
                'icon' => 'bi bi-cpu-fill',
                'keywords' => ['plugins', 'core', 'chat', 'shop', 'battlepass', 'media', 'modules'],
            ],
            [
                'title' => 'Unified Audit Logs',
                'subtitle' => 'Comprehensive historical audit trail of staff actions',
                'url' => route('apexsions-bridge.admin.audit-logs.index'),
                'icon' => 'bi bi-journal-text',
                'keywords' => ['audit', 'logs', 'history', 'security', 'trail', 'staff', 'actions'],
            ],
            [
                'title' => 'Intelligence Desk',
                'subtitle' => 'Real-time threat heuristics & anomaly detection',
                'url' => route('apexsions-bridge.admin.intelligence.index'),
                'icon' => 'bi bi-shield-check',
                'keywords' => ['intelligence', 'threats', 'anomalies', 'heuristics', 'security', 'radar'],
            ],
            [
                'title' => 'Incident Registry',
                'subtitle' => 'Formal infrastructure incident management & RCA tracking',
                'url' => route('apexsions-bridge.admin.incidents.index'),
                'icon' => 'bi bi-exclamation-octagon-fill',
                'keywords' => ['incidents', 'outage', 'downtime', 'postmortem', 'status'],
            ],
            [
                'title' => 'Notifications Desk',
                'subtitle' => 'Configure staff alert channels & webhook notifications',
                'url' => route('apexsions-bridge.admin.notifications.index'),
                'icon' => 'bi bi-bell-fill',
                'keywords' => ['notifications', 'alerts', 'discord', 'webhook', 'announcements'],
            ],
            [
                'title' => 'Safe Automation',
                'subtitle' => 'Policy-governed automated recovery routines & sweeps',
                'url' => route('apexsions-bridge.admin.automation.index'),
                'icon' => 'bi bi-gear-wide-connected',
                'keywords' => ['automation', 'cron', 'tasks', 'recovery', 'policy', 'approvals'],
            ],
            [
                'title' => 'Web Platform Settings',
                'subtitle' => 'Azuriom site settings, mail configuration & themes',
                'url' => route('admin.settings.index'),
                'icon' => 'bi bi-sliders',
                'keywords' => ['settings', 'config', 'mail', 'cms', 'website', 'general'],
            ],
            [
                'title' => 'Web Staff Accounts',
                'subtitle' => 'Manage Azuriom admin & moderator CMS accounts',
                'url' => route('admin.users.index'),
                'icon' => 'bi bi-person-gear',
                'keywords' => ['web', 'staff', 'admin', 'moderator', 'cms', 'accounts', 'users'],
            ],
        ];

        $lowerQuery = mb_strtolower($query);
        $navigation = collect($navRegistry)->filter(function ($item) use ($lowerQuery) {
            if (str_contains(mb_strtolower($item['title']), $lowerQuery)) {
                return true;
            }
            if (str_contains(mb_strtolower($item['subtitle']), $lowerQuery)) {
                return true;
            }
            foreach ($item['keywords'] as $kw) {
                if (str_contains($kw, $lowerQuery)) {
                    return true;
                }
            }
            return false;
        })->take(5)->values();

        // 3. Search Reports
        $reports = Report::query()
            ->where('reported_name', 'LIKE', "%{$query}%")
            ->orWhere('reporter_name', 'LIKE', "%{$query}%")
            ->orWhere('reason', 'LIKE', "%{$query}%")
            ->limit(3)
            ->get()
            ->map(function ($report) {
                return [
                    'title' => 'Report #' . $report->id . ': ' . $report->reported_name,
                    'subtitle' => ($report->reason ?: 'No reason stated') . ' [' . strtoupper($report->status ?? 'OPEN') . ']',
                    'url' => route('apexsions-bridge.admin.reports.show', $report->id),
                    'icon' => 'bi bi-flag',
                    'type' => 'report',
                ];
            });

        return response()->json([
            'players' => $players,
            'navigation' => $navigation,
            'reports' => $reports,
        ]);
    }
}
