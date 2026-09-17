package net.catcraft.ccmc.config;

import java.util.Locale;

public enum StaffRank {
    HELPER("Helper", "helper"),
    MODERATOR("Moderator", "moderator"),
    SENIOR_MODERATOR("Senior Moderator", "senior moderator"),
    ADMINISTRATOR("Administrator", "administrator");

    private final String displayName;
    private final String configValue;
    StaffRank(String displayName, String configValue) { this.displayName = displayName; this.configValue = configValue; }
    public String displayName() { return displayName; }
    public String configValue() { return configValue; }

    public static StaffRank parse(String value) {
        if (value == null) return MODERATOR;
        String normalized = value.trim().toLowerCase(Locale.ROOT).replace('-', ' ').replace('_', ' ').replaceAll("\\s+", " ");
        return switch (normalized) {
            case "helper" -> HELPER;
            case "moderator" -> MODERATOR;
            case "senior moderator" -> SENIOR_MODERATOR;
            case "administrator" -> ADMINISTRATOR;
            default -> MODERATOR;
        };
    }
}
