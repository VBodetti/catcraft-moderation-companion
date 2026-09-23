package net.catcraft.ccmc.config;

import java.util.Locale;

public enum PlayerRank {
    CAT("Cat", "cat", 0),
    LEOPARD("Leopard", "leopard", 1),
    CHEETAH("Cheetah", "cheetah", 2),
    JAGUAR("Jaguar", "jaguar", 3),
    TIGER("Tiger", "tiger", 4),
    LION("Lion", "lion", 5);

    private final String displayName;
    private final String configValue;
    private final int level;

    private PlayerRank(String displayName, String configValue, int level) {
        this.displayName = displayName;
        this.configValue = configValue;
        this.level = level;
    }

    public String displayName() {
        return this.displayName;
    }

    public String configValue() {
        return this.configValue;
    }

    public int level() {
        return this.level;
    }

    public boolean atLeast(PlayerRank minimum) {
        return this.level >= minimum.level;
    }

    public static PlayerRank parse(String value) {
        if (value == null) {
            return CAT;
        }
        String normalized = PlayerRank.normalize(value);
        if ("member".equals(normalized)) return CAT;
        for (PlayerRank rank : PlayerRank.values()) {
            if (!rank.configValue.equals(normalized) && !PlayerRank.normalize(rank.displayName).equals(normalized)) continue;
            return rank;
        }
        return CAT;
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replace('-', ' ').replace('_', ' ').replaceAll("\\s+", " ");
    }

    public String toString() {
        return this.displayName;
    }
}
