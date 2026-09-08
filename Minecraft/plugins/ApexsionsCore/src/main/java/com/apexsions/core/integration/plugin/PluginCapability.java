package com.apexsions.core.integration.plugin;

/**
 * Record representing a declared capability within the Apexsions Custom Plugin ecosystem.
 */
public record PluginCapability(
        String id,
        String name,
        String type, // READ, WRITE, ACTION, EVENT, METRIC
        String access, // PUBLIC_INTERNAL, ADMIN, SYSTEM
        String status, // AVAILABLE, DEGRADED, DISABLED
        String description,
        boolean requiresReason,
        boolean requiresConfirmation
) {
    public String toJson() {
        return String.format(
                "{\"capability_id\":\"%s\",\"name\":\"%s\",\"type\":\"%s\",\"access\":\"%s\",\"status\":\"%s\",\"description\":\"%s\",\"requires_reason\":%b,\"requires_confirmation\":%b}",
                escape(id), escape(name), escape(type), escape(access), escape(status),
                escape(description), requiresReason, requiresConfirmation
        );
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
