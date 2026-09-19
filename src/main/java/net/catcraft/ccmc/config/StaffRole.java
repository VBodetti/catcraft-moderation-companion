package net.catcraft.ccmc.config;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public enum StaffRole {
    NONE("None", "none"),
    HELPER("Helper", "helper",
            StaffCapability.STAFF_CHAT,
            StaffCapability.BASIC_MODERATION),
    MODERATOR("Moderator", "moderator",
            StaffCapability.STAFF_CHAT,
            StaffCapability.BASIC_MODERATION,
            StaffCapability.PLAYER_TELEPORT,
            StaffCapability.TEMP_BAN,
            StaffCapability.ADVANCED_INVESTIGATION,
            StaffCapability.MODERATOR_TOOLS),
    SENIOR_MODERATOR("Senior Moderator", "senior moderator",
            StaffCapability.STAFF_CHAT,
            StaffCapability.BASIC_MODERATION,
            StaffCapability.PLAYER_TELEPORT,
            StaffCapability.TEMP_BAN,
            StaffCapability.ADVANCED_INVESTIGATION,
            StaffCapability.MODERATOR_TOOLS,
            StaffCapability.SENIOR_TOOLS),
    ADMINISTRATOR("Administrator", "administrator",
            StaffCapability.STAFF_CHAT,
            StaffCapability.BASIC_MODERATION,
            StaffCapability.PLAYER_TELEPORT,
            StaffCapability.TEMP_BAN,
            StaffCapability.ADVANCED_INVESTIGATION,
            StaffCapability.MODERATOR_TOOLS,
            StaffCapability.SENIOR_TOOLS,
            StaffCapability.ADMIN_TOOLS),
    SENIOR_ADMINISTRATOR("Senior Administrator", "senior administrator",
            StaffCapability.STAFF_CHAT,
            StaffCapability.BASIC_MODERATION,
            StaffCapability.PLAYER_TELEPORT,
            StaffCapability.TEMP_BAN,
            StaffCapability.ADVANCED_INVESTIGATION,
            StaffCapability.MODERATOR_TOOLS,
            StaffCapability.SENIOR_TOOLS,
            StaffCapability.ADMIN_TOOLS),
    DEVELOPER("Developer", "developer",
            StaffCapability.STAFF_CHAT,
            StaffCapability.DEVELOPER_TOOLS),
    OWNER("Owner", "owner",
            StaffCapability.STAFF_CHAT,
            StaffCapability.BASIC_MODERATION,
            StaffCapability.PLAYER_TELEPORT,
            StaffCapability.TEMP_BAN,
            StaffCapability.ADVANCED_INVESTIGATION,
            StaffCapability.MODERATOR_TOOLS,
            StaffCapability.SENIOR_TOOLS,
            StaffCapability.ADMIN_TOOLS,
            StaffCapability.DEVELOPER_TOOLS);

    private final String displayName;
    private final String configValue;
    private final Set<StaffCapability> capabilities;

    StaffRole(String displayName, String configValue, StaffCapability... capabilities) {
        this.displayName = displayName;
        this.configValue = configValue;
        this.capabilities = capabilities.length == 0
                ? EnumSet.noneOf(StaffCapability.class)
                : EnumSet.of(capabilities[0], capabilities);
    }

    public String displayName() { return displayName; }
    public String configValue() { return configValue; }
    public boolean has(StaffCapability capability) { return capabilities.contains(capability); }

    public static StaffRole parse(String value) {
        if (value == null) return MODERATOR;
        String normalized = normalize(value);
        return switch (normalized) {
            case "none", "player", "member" -> NONE;
            case "helper" -> HELPER;
            case "moderator", "mod" -> MODERATOR;
            case "senior moderator", "senior mod", "sr moderator", "sr mod", "senior" -> SENIOR_MODERATOR;
            case "administrator", "admin" -> ADMINISTRATOR;
            case "senior administrator", "senior admin", "sr administrator", "sr admin" -> SENIOR_ADMINISTRATOR;
            case "developer", "dev" -> DEVELOPER;
            case "owner" -> OWNER;
            default -> MODERATOR;
        };
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replace('-', ' ').replace('_', ' ').replaceAll("\\s+", " ");
    }

    @Override public String toString() { return displayName; }
}
