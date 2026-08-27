package com.yourserver.apexsionschat.model;

public enum ReportStatus {
    OPEN,
    REVIEWING,
    RESOLVED,
    DISMISSED;

    public static ReportStatus fromString(String str) {
        if (str == null) return OPEN;
        try {
            return valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OPEN;
        }
    }
}
