package com.apexsions.core.api;

/**
 * Canonical permission-node registry for ApexsionsCore (finding M-2).
 *
 * <p>All permission nodes used by ApexsionsCore MUST use the single
 * {@code apexsions.*} prefix and MUST be referenced through these constants
 * instead of inline string literals. New nodes are added here first, then used
 * at call sites.
 *
 * <h2>Legacy mapping table</h2>
 * <p>The pre-standardization codebase used three different prefixes plus a
 * typo variant. The migration wave replaces each legacy node with the
 * canonical constant listed below. Nodes marked DROP have no canonical
 * equivalent and must simply be deleted at call sites.
 *
 * <pre>
 * LEGACY NODE                         → CANONICAL (constant)
 * ──────────────────────────────────────────────────────────────────────────
 * apexsionscore.admin                 → apexsions.admin            (ADMIN)
 * apexsionscore.admin.*               → apexsions.admin.*          (ADMIN_WILDCARD)
 * apexsionscore.admin.bypass.claim    → apexsions.admin.bypass.claim (ADMIN_BYPASS_CLAIM)
 * apexsionscore.admin.bypass.combat   → apexsions.bypass.combat    (BYPASS_COMBAT)
 * apexsionscore.admin.bypass.tpa      → apexsions.tpa.bypass       (TPA_BYPASS)
 * apexsionscore.command.cosmetics     → apexsions.command.cosmetics (COMMAND_COSMETICS)
 * apexsionscore.command.enchant       → apexsions.command.enchant  (COMMAND_ENCHANT)
 * apexsionscore.command.kits          → apexsions.command.kits     (COMMAND_KITS)
 * apexsionscore.command.level         → apexsions.command.level    (COMMAND_LEVEL)
 * apexsionscore.command.link          → apexsions.command.link     (COMMAND_LINK)
 * apexsionscore.command.lobby         → apexsions.command.lobby    (COMMAND_LOBBY)
 * apexsionscore.command.rank          → apexsions.command.rank     (COMMAND_RANK)
 * apexsionscore.command.region        → apexsions.command.region   (COMMAND_REGION)
 * apexsionscore.command.rtp           → apexsions.command.rtp      (COMMAND_RTP)
 * apexsionscore.command.titles        → apexsions.command.titles   (COMMAND_TITLES)
 * apexsionscore.command.warp          → apexsions.command.warp     (COMMAND_WARP)
 * apexsionscore.enchant.bypass        → apexsions.enchant.bypass   (ENCHANT_BYPASS)
 * apexsionscore.rtp.bypass            → apexsions.rtp.bypass       (RTP_BYPASS)
 * apexsionscore.warp.admin            → apexsions.warp.admin       (WARP_ADMIN)
 * apexsionscore.warp.bypass           → apexsions.warp.bypass      (WARP_BYPASS)
 *
 * apexionscore.admin                  → apexsions.admin            (ADMIN)        [typo, missing 's']
 * apexionscore.command.level          → apexsions.command.level    (COMMAND_LEVEL)
 * apexionscore.command.lobby          → apexsions.command.lobby    (COMMAND_LOBBY)
 * apexionscore.command.region         → apexsions.command.region   (COMMAND_REGION)
 * apexionscore.command.rtp            → apexsions.command.rtp      (COMMAND_RTP)
 *
 * kingdomcore.admin                   → apexsions.admin            (ADMIN)
 * kingdomcore.command.kingdom         → apexsions.command.region   (COMMAND_REGION)  [see note 1]
 * kingdomcore.command.kingdom.choose  → apexsions.command.region   (COMMAND_REGION)  [see note 1]
 * kingdomcore.command.level           → apexsions.command.level    (COMMAND_LEVEL)
 * kingdomcore.command.lobby           → apexsions.command.lobby    (COMMAND_LOBBY)
 * kingdomcore.command.rtp             → apexsions.command.rtp      (COMMAND_RTP)
 * ──────────────────────────────────────────────────────────────────────────
 * Note 1: both legacy nodes were always used as OR-fallbacks alongside
 * apexsionscore.command.region, so they intentionally collapse to the same
 * canonical node.
 * </pre>
 *
 * <p>Already-canonical {@code apexsions.*} nodes found in the codebase are
 * kept verbatim (they simply gain a constant here).
 */
public final class Permissions {

    private Permissions() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ------------------------------------------------------------------
    // Admin / staff
    // ------------------------------------------------------------------

    /** Full administrative access. */
    public static final String ADMIN = "apexsions.admin";
    /** Wildcard administrative access (replaces legacy {@code apexsionscore.admin.*}). */
    public static final String ADMIN_WILDCARD = "apexsions.admin.*";
    /** Staff membership (moderation tooling, staff chat, etc.). */
    public static final String STAFF = "apexsions.staff";
    /** Receive anti-xray / security alert broadcasts. */
    public static final String STAFF_ALERTS = "apexsions.staff.alerts";
    /** Conclave (senior staff council) membership. */
    public static final String CONCLAVE = "apexsions.conclave";

    // ------------------------------------------------------------------
    // Admin sub-nodes
    // ------------------------------------------------------------------

    public static final String ADMIN_BAN = "apexsions.admin.ban";
    public static final String ADMIN_BANIP = "apexsions.admin.banip";
    public static final String ADMIN_UNBAN = "apexsions.admin.unban";
    /** Open the centralized Master Admin Hub (/admingui). */
    public static final String ADMIN_GUI = "apexsions.admin.gui";
    /** Bypass land-claim protections. */
    public static final String ADMIN_BYPASS_CLAIM = "apexsions.admin.bypass.claim";
    /** Claim land without limits (admin variant). */
    public static final String ADMIN_CLAIM_UNLIMITED = "apexsions.admin.claim.unlimited";

    // ------------------------------------------------------------------
    // Bypass nodes (anti-cheat / protection)
    // ------------------------------------------------------------------

    public static final String BYPASS_COMBAT = "apexsions.bypass.combat";
    public static final String BYPASS_MOVEMENT = "apexsions.bypass.movement";
    public static final String BYPASS_SCAFFOLD = "apexsions.bypass.scaffold";
    public static final String BYPASS_CHESTSTEALER = "apexsions.bypass.cheststealer";

    // ------------------------------------------------------------------
    // Commands (apexsions.command.*)
    // ------------------------------------------------------------------

    public static final String COMMAND_LEVEL = "apexsions.command.level";
    public static final String COMMAND_LOBBY = "apexsions.command.lobby";
    public static final String COMMAND_REGION = "apexsions.command.region";
    public static final String COMMAND_RTP = "apexsions.command.rtp";
    public static final String COMMAND_WARP = "apexsions.command.warp";
    public static final String COMMAND_ENCHANT = "apexsions.command.enchant";
    public static final String COMMAND_RANK = "apexsions.command.rank";
    public static final String COMMAND_TITLES = "apexsions.command.titles";
    public static final String COMMAND_COSMETICS = "apexsions.command.cosmetics";
    public static final String COMMAND_LINK = "apexsions.command.link";
    public static final String COMMAND_KITS = "apexsions.command.kits";

    // ------------------------------------------------------------------
    // Warp
    // ------------------------------------------------------------------

    public static final String WARP_ADMIN = "apexsions.warp.admin";
    /** Bypass the warp teleportation countdown delay. */
    public static final String WARP_BYPASS = "apexsions.warp.bypass";

    // ------------------------------------------------------------------
    // RTP
    // ------------------------------------------------------------------

    /** Bypass the /rtp cooldown timer. */
    public static final String RTP_BYPASS = "apexsions.rtp.bypass";
    /** Per-rank RTP cooldown overrides, e.g. {@code apexsions.rtp.cooldown.archon}. */
    public static final String RTP_COOLDOWN_PREFIX = "apexsions.rtp.cooldown.";

    public static final String RTP_COOLDOWN_ARCHON = RTP_COOLDOWN_PREFIX + "archon";
    public static final String RTP_COOLDOWN_ASCENDANT = RTP_COOLDOWN_PREFIX + "ascendant";
    public static final String RTP_COOLDOWN_EMPEROR = RTP_COOLDOWN_PREFIX + "emperor";
    public static final String RTP_COOLDOWN_SOVEREIGN = RTP_COOLDOWN_PREFIX + "sovereign";
    public static final String RTP_COOLDOWN_SIONS = RTP_COOLDOWN_PREFIX + "sions";

    // ------------------------------------------------------------------
    // TPA
    // ------------------------------------------------------------------

    /** Bypass TPA restrictions (distance/cooldown/world rules). */
    public static final String TPA_BYPASS = "apexsions.tpa.bypass";

    // ------------------------------------------------------------------
    // Enchant
    // ------------------------------------------------------------------

    /** Bypass the 4x custom-enchant level multiplier up to the server hardcap. */
    public static final String ENCHANT_BYPASS = "apexsions.enchant.bypass";

    // ------------------------------------------------------------------
    // Claims
    // ------------------------------------------------------------------

    public static final String CLAIM_USE = "apexsions.claim.use";
    public static final String CLAIM_UNLIMITED = "apexsions.claim.unlimited";
    /**
     * Prefix for numeric claim-limit overrides, e.g. {@code apexsions.claim.limit.25}
     * (scanned via {@code getEffectivePermissions()} in ClaimManager).
     */
    public static final String CLAIM_LIMIT_PREFIX = "apexsions.claim.limit.";

    // ------------------------------------------------------------------
    // Vanish
    // ------------------------------------------------------------------

    public static final String VANISH = "apexsions.vanish";
    public static final String VANISH_SEE = "apexsions.vanish.see";
    public static final String VANISH_OTHERS = "apexsions.vanish.others";

    // ------------------------------------------------------------------
    // Maintenance mode
    // ------------------------------------------------------------------

    public static final String MAINTENANCE_ADMIN = "apexsions.maintenance.admin";
    /** Join the server while maintenance mode is enabled. */
    public static final String MAINTENANCE_BYPASS = "apexsions.maintenance.bypass";

    // ------------------------------------------------------------------
    // Bounty
    // ------------------------------------------------------------------

    public static final String BOUNTY_USE = "apexsions.bounty.use";
    public static final String BOUNTY_ADMIN = "apexsions.bounty.admin";

    // ------------------------------------------------------------------
    // Caravan (Black Market)
    // ------------------------------------------------------------------

    public static final String CARAVAN_USE = "apexsions.caravan.use";
    public static final String CARAVAN_ADMIN = "apexsions.caravan.admin";

    // ------------------------------------------------------------------
    // Grave / death
    // ------------------------------------------------------------------

    public static final String GRAVE_USE = "apexsions.grave.use";
    public static final String GRAVE_ADMIN = "apexsions.grave.admin";
    public static final String CORE_DEATHCOORDS = "apexsions.core.deathcoords";
    public static final String CORE_DEATHCOORDS_OTHERS = "apexsions.core.deathcoords.others";
    public static final String CORE_DEATHCOORDS_TP = "apexsions.core.deathcoords.tp";

    // ------------------------------------------------------------------
    // Container utilities
    // ------------------------------------------------------------------

    public static final String CONTAINER_USE = "apexsions.container.use";

    // ------------------------------------------------------------------
    // Poses
    // ------------------------------------------------------------------

    public static final String POSE_SIT = "apexsions.pose.sit";
    public static final String POSE_LAY = "apexsions.pose.lay";
    public static final String POSE_CRAWL = "apexsions.pose.crawl";
    public static final String POSE_BELLYFLOP = "apexsions.pose.bellyflop";
    public static final String POSE_SPIN = "apexsions.pose.spin";
    /** Force/toggle poses for other players. */
    public static final String POSE_ADMIN = "apexsions.pose.admin";

    // ------------------------------------------------------------------
    // Titles (dynamic: apexsions.title.<id>)
    // ------------------------------------------------------------------

    /** Wildcard granting every title. */
    public static final String TITLE_ALL = "apexsions.title.*";
    /** Prefix for per-title unlock nodes, e.g. {@code apexsions.title.warrior}. */
    public static final String TITLE_PREFIX = "apexsions.title.";

    // ------------------------------------------------------------------
    // Kits (dynamic: apexsions.kit.<id>)
    // ------------------------------------------------------------------

    /** Allow claiming trial kits regardless of rank weight. */
    public static final String KIT_TRIAL_ALLOW = "apexsions.kit.trial.allow";
    /** Prefix for per-kit access nodes, e.g. {@code apexsions.kit.wanderer}. */
    public static final String KIT_PREFIX = "apexsions.kit.";

    // ------------------------------------------------------------------
    // Ranks (LuckPerms-mirrored)
    // ------------------------------------------------------------------

    public static final String RANK_ARCHON = "apexsions.rank.archon";
    public static final String RANK_ASCENDANT = "apexsions.rank.ascendant";
    public static final String RANK_EMPEROR = "apexsions.rank.emperor";
    public static final String RANK_SOVEREIGN = "apexsions.rank.sovereign";
    public static final String RANK_SIONS = "apexsions.rank.sions";
    public static final String RANK_PERMANENT = "apexsions.rank.permanent";
    public static final String RANK_TRIAL = "apexsions.rank.trial";

    // ------------------------------------------------------------------
    // XP bonuses (per-rank multipliers)
    // ------------------------------------------------------------------

    public static final String EXP_BONUS_ARCHON = "apexsions.exp.bonus.archon";
    public static final String EXP_BONUS_ASCENDANT = "apexsions.exp.bonus.ascendant";
    public static final String EXP_BONUS_EMPEROR = "apexsions.exp.bonus.emperor";
    public static final String EXP_BONUS_SOVEREIGN = "apexsions.exp.bonus.sovereign";
    public static final String EXP_BONUS_SIONS = "apexsions.exp.bonus.sions";

    // ------------------------------------------------------------------
    // Leaderboards & cosmetics
    // ------------------------------------------------------------------

    /** Exclude a player from public leaderboards. */
    public static final String LEADERBOARD_EXEMPT = "apexsions.leaderboard.exempt";
    /** VIP/donator cosmetic unlocks. */
    public static final String COSMETICS_VIP = "apexsions.cosmetics.vip";

    // ------------------------------------------------------------------
    // Dynamic node helpers
    // ------------------------------------------------------------------

    /**
     * Builds the unlock node for a title id, e.g. {@code title("warrior")}
     * → {@code "apexsions.title.warrior"}.
     */
    public static String title(String id) {
        return TITLE_PREFIX + (id == null ? "" : id.toLowerCase(java.util.Locale.ROOT));
    }

    /**
     * Builds the access node for a kit id, e.g. {@code kit("wanderer")}
     * → {@code "apexsions.kit.wanderer"}.
     */
    public static String kit(String id) {
        return KIT_PREFIX + (id == null ? "" : id.toLowerCase(java.util.Locale.ROOT));
    }

    /**
     * Builds a numeric claim-limit override node, e.g. {@code claimLimit(25)}
     * → {@code "apexsions.claim.limit.25"}.
     */
    public static String claimLimit(int claims) {
        return CLAIM_LIMIT_PREFIX + claims;
    }

    /**
     * Builds the per-rank RTP cooldown override node, e.g.
     * {@code rtpCooldown("archon")} → {@code "apexsions.rtp.cooldown.archon"}.
     */
    public static String rtpCooldown(String rank) {
        return RTP_COOLDOWN_PREFIX + (rank == null ? "" : rank.toLowerCase(java.util.Locale.ROOT));
    }
}
