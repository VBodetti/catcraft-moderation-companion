package net.catcraft.ccmc.config;

import java.util.Locale;

public enum PlayerRank {
    MEMBER("Member", "member", 0),
    CAT("Cat", "cat", 1),
    LEOPARD("Leopard", "leopard", 2),
    CHEETAH("Cheetah", "cheetah", 3),
    JAGUAR("Jaguar", "jaguar", 4),
    TIGER("Tiger", "tiger", 5),
    LION("Lion", "lion", 6);

    private final String displayName;
    private final String configValue;
    private final int level;

    PlayerRank(String displayName, String configValue, int level) {
        this.displayName = displayName;
        this.configValue = configValue;
        this.level = level;
    }

    public String displayName() { return displayName; }
    public String configValue() { return configValue; }
    public int level() { return level; }
    public boolean atLeast(PlayerRank minimum) { return level >= minimum.level; }

    public static PlayerRank parse(String value) {
        if (value == null) return MEMBER;
        String normalized = normalize(value);
        for (PlayerRank rank : values()) {
            if (rank.configValue.equals(normalized) || normalize(rank.displayName).equals(normalized)) return rank;
        }
        return MEMBER;
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replace('-', ' ').replace('_', ' ').replaceAll("\\s+", " ");
    }

    @Override public String toString() { return displayName; }
}
