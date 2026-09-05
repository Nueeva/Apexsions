package com.apexsions.chat.nick;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Registry and enumeration of nickname color styles and premium gradients.
 */
public enum NickColorStyle {

    DEFAULT("default", "<white>Putih Murni (Default)</white>", "<white>{name}</white>", Material.WHITE_DYE, false, null, "Semua Pemain"),

    // --- Solid Colors (Donator Tier 1 / Ascendant+) ---
    GOLD("gold", "<gold>Emas Bangsawan</gold>", "<gold>{name}</gold>", Material.GOLD_INGOT, false, "apexsions.nick.color", "Rank Ascendant+"),
    RUBY("ruby", "<red>Ruby Merah</red>", "<red>{name}</red>", Material.REDSTONE, false, "apexsions.nick.color", "Rank Ascendant+"),
    EMERALD("emerald", "<green>Zamrud Hijau</green>", "<green>{name}</green>", Material.EMERALD, false, "apexsions.nick.color", "Rank Ascendant+"),
    SAPPHIRE("sapphire", "<blue>Safir Biru</blue>", "<blue>{name}</blue>", Material.LAPIS_LAZULI, false, "apexsions.nick.color", "Rank Ascendant+"),
    AMETHYST("amethyst", "<light_purple>Kecubung Ungu</light_purple>", "<light_purple>{name}</light_purple>", Material.AMETHYST_SHARD, false, "apexsions.nick.color", "Rank Ascendant+"),
    ORANGE("orange", "<#ff793f>Senja Oranye</#ff793f>", "<#ff793f>{name}</#ff793f>", Material.COPPER_INGOT, false, "apexsions.nick.color", "Rank Ascendant+"),
    CYAN("cyan", "<aqua>Aqua Kristal</aqua>", "<aqua>{name}</aqua>", Material.PRISMARINE_SHARD, false, "apexsions.nick.color", "Rank Ascendant+"),

    // --- Premium Gradients (Donator Tier 2+ / Archon, Sovereign, Emperor, Sions) ---
    SOLAR_GOLD("solar_gold", "<gradient:#ffeaa7:#f39c12>Solar Gold</gradient>", "<gradient:#ffeaa7:#f39c12>{name}</gradient>", Material.RAW_GOLD, true, "apexsions.nick.gradient", "Rank Archon+"),
    CRIMSON_FIRE("crimson_fire", "<gradient:#f12711:#f5af19>Crimson Fire</gradient>", "<gradient:#f12711:#f5af19>{name}</gradient>", Material.BLAZE_POWDER, true, "apexsions.nick.gradient", "Rank Archon+"),
    DEEP_OCEAN("deep_ocean", "<gradient:#00c6ff:#0072ff>Deep Ocean</gradient>", "<gradient:#00c6ff:#0072ff>{name}</gradient>", Material.HEART_OF_THE_SEA, true, "apexsions.nick.gradient", "Rank Archon+"),
    NEON_VIOLET("neon_violet", "<gradient:#e056fd:#686de0>Neon Violet</gradient>", "<gradient:#e056fd:#686de0>{name}</gradient>", Material.ECHO_SHARD, true, "apexsions.nick.gradient", "Rank Sovereign+"),
    SUNSET_HORIZON("sunset_horizon", "<gradient:#ff9a9e:#fecfef>Sunset Horizon</gradient>", "<gradient:#ff9a9e:#fecfef>{name}</gradient>", Material.NETHER_STAR, true, "apexsions.nick.gradient", "Rank Sovereign+"),
    EMERALD_FOREST("emerald_forest", "<gradient:#11998e:#38ef7d>Emerald Forest</gradient>", "<gradient:#11998e:#38ef7d>{name}</gradient>", Material.EXPERIENCE_BOTTLE, true, "apexsions.nick.gradient", "Rank Emperor+"),
    CYBERPUNK("cyberpunk", "<gradient:#f857a6:#ff5858>Cyberpunk Rose</gradient>", "<gradient:#f857a6:#ff5858>{name}</gradient>", Material.DRAGON_BREATH, true, "apexsions.nick.gradient", "Rank Sions+");

    private static final Map<String, NickColorStyle> BY_ID = new HashMap<>();

    static {
        for (NickColorStyle style : values()) {
            BY_ID.put(style.getId().toLowerCase(Locale.ROOT), style);
        }
    }

    private final String id;
    private final String displayName;
    private final String formatTemplate;
    private final Material guiMaterial;
    private final boolean isGradient;
    private final String requiredPermission;
    private final String requiredRank;

    NickColorStyle(String id, String displayName, String formatTemplate, Material guiMaterial,
                   boolean isGradient, String requiredPermission, String requiredRank) {
        this.id = id;
        this.displayName = displayName;
        this.formatTemplate = formatTemplate;
        this.guiMaterial = guiMaterial;
        this.isGradient = isGradient;
        this.requiredPermission = requiredPermission;
        this.requiredRank = requiredRank;
    }

    public static NickColorStyle fromId(String id) {
        if (id == null) return DEFAULT;
        return BY_ID.getOrDefault(id.toLowerCase(Locale.ROOT), DEFAULT);
    }

    public static List<NickColorStyle> getSolidStyles() {
        List<NickColorStyle> list = new ArrayList<>();
        for (NickColorStyle style : values()) {
            if (!style.isGradient && style != DEFAULT) {
                list.add(style);
            }
        }
        return list;
    }

    public static List<NickColorStyle> getGradientStyles() {
        List<NickColorStyle> list = new ArrayList<>();
        for (NickColorStyle style : values()) {
            if (style.isGradient) {
                list.add(style);
            }
        }
        return list;
    }

    public boolean hasPermission(Player player) {
        if (requiredPermission == null) return true;
        if (player.hasPermission("apexsions.nick.admin") || player.isOp()) return true;
        if (player.hasPermission(requiredPermission)) return true;
        return player.hasPermission(requiredPermission + "." + id);
    }

    public Component apply(String rawName) {
        String safeName = (rawName != null && !rawName.isBlank()) ? rawName : "Player";
        String formatted = formatTemplate.replace("{name}", safeName);
        return MiniMessage.miniMessage().deserialize(formatted);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFormatTemplate() {
        return formatTemplate;
    }

    public Material getGuiMaterial() {
        return guiMaterial;
    }

    public boolean isGradient() {
        return isGradient;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public String getRequiredRank() {
        return requiredRank;
    }
}
