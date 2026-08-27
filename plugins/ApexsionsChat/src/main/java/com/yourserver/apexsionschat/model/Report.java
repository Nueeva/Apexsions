package com.yourserver.apexsionschat.model;

import java.time.Instant;
import java.util.UUID;

public class Report {

    private long reportId;
    private UUID reporterUuid;
    private String reporterName;
    private UUID reportedUuid;
    private String reportedName;
    private String reason;
    private String server;
    private String world;
    private Instant timestamp;
    private ReportStatus status;
    private UUID moderatorUuid;
    private String moderatorName;
    private String resolution;
    private Instant resolvedAt;

    public Report() {
        this.status = ReportStatus.OPEN;
        this.timestamp = Instant.now();
        this.server = "apexsions-survival";
    }

    public Report(UUID reporterUuid, String reporterName, UUID reportedUuid, String reportedName, String reason, String world) {
        this.reporterUuid = reporterUuid;
        this.reporterName = reporterName;
        this.reportedUuid = reportedUuid;
        this.reportedName = reportedName;
        this.reason = reason;
        this.server = "apexsions-survival";
        this.world = world != null ? world : "world";
        this.timestamp = Instant.now();
        this.status = ReportStatus.OPEN;
    }

    public long getReportId() { return reportId; }
    public void setReportId(long reportId) { this.reportId = reportId; }

    public UUID getReporterUuid() { return reporterUuid; }
    public void setReporterUuid(UUID reporterUuid) { this.reporterUuid = reporterUuid; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public UUID getReportedUuid() { return reportedUuid; }
    public void setReportedUuid(UUID reportedUuid) { this.reportedUuid = reportedUuid; }

    public String getReportedName() { return reportedName; }
    public void setReportedName(String reportedName) { this.reportedName = reportedName; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getServer() { return server; }
    public void setServer(String server) { this.server = server; }

    public String getWorld() { return world; }
    public void setWorld(String world) { this.world = world; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }

    public UUID getModeratorUuid() { return moderatorUuid; }
    public void setModeratorUuid(UUID moderatorUuid) { this.moderatorUuid = moderatorUuid; }

    public String getModeratorName() { return moderatorName; }
    public void setModeratorName(String moderatorName) { this.moderatorName = moderatorName; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
}
