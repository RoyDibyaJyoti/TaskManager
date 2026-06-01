package com.taskmanager.model;

public enum PriorityLevel {
    LOW("Low", "#10B981"),
    MEDIUM("Medium", "#F59E0B"),
    HIGH("High", "#EF4444"),
    CRITICAL("Critical", "#8B5CF6");

    private final String displayName;
    private final String colorHex;

    PriorityLevel(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
