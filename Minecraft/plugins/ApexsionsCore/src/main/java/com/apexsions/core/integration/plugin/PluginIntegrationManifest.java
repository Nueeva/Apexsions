package com.apexsions.core.integration.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Manifest definition holding metadata and declared capabilities for an Apexsions custom plugin.
 */
public class PluginIntegrationManifest {
    private final String pluginId;
    private final String name;
    private final String version;
    private final String type;
    private final String integrationStatus;
    private final String description;
    private final List<PluginCapability> capabilities;

    public PluginIntegrationManifest(String pluginId, String name, String version, String type,
                                     String integrationStatus, String description,
                                     List<PluginCapability> capabilities) {
        this.pluginId = pluginId;
        this.name = name;
        this.version = version;
        this.type = type;
        this.integrationStatus = integrationStatus;
        this.description = description;
        this.capabilities = capabilities != null ? capabilities : new ArrayList<>();
    }

    public String getPluginId() { return pluginId; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getType() { return type; }
    public String getIntegrationStatus() { return integrationStatus; }
    public String getDescription() { return description; }
    public List<PluginCapability> getCapabilities() { return capabilities; }

    public String toJson(boolean enabled, String healthStatus) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"plugin_id\":\"").append(escape(pluginId)).append("\",");
        sb.append("\"name\":\"").append(escape(name)).append("\",");
        sb.append("\"version\":\"").append(escape(version)).append("\",");
        sb.append("\"type\":\"").append(escape(type)).append("\",");
        sb.append("\"enabled\":").append(enabled).append(",");
        sb.append("\"health\":\"").append(escape(healthStatus)).append("\",");
        sb.append("\"integration_status\":\"").append(escape(integrationStatus)).append("\",");
        sb.append("\"description\":\"").append(escape(description)).append("\",");
        sb.append("\"capabilities\":[");
        for (int i = 0; i < capabilities.size(); i++) {
            sb.append(capabilities.get(i).toJson());
            if (i < capabilities.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
