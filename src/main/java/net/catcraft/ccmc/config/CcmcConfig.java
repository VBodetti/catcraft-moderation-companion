package net.catcraft.ccmc.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;

public final class CcmcConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("catcraft_moderation_companion.json");
    private static final Map<String, Object> DEFAULTS = new LinkedHashMap<>();
    private static final Map<String, Object> VALUES = new LinkedHashMap<>();
    private static final Pattern ENTRY = Pattern.compile("\\\"((?:\\\\.|[^\\\"])*)\\\"\\s*:\\s*(\\\"(?:\\\\.|[^\\\"])*\\\"|true|false|-?\\d+(?:\\.\\d+)?)");

    static {
        DEFAULTS.put("config.version", 3.0d);
        DEFAULTS.put("general.Timestamp.Enabled", true);
        DEFAULTS.put("general.Timestamp.Pattern", "&8[{hour}:{minute}:{second}] &r");
        DEFAULTS.put("general.Timestamp.CopyToChatBar.Enabled", true);
        DEFAULTS.put("general.MessageStacking.Enabled", false);
        DEFAULTS.put("general.MessageStacking.ExactMatchOnly", false);
        DEFAULTS.put("general.MessageStacking.MaxRepeatCount", 100);
        DEFAULTS.put("general.StoredChatLines", 500);
        DEFAULTS.put("catcraft.PlayerRank", "member");
        DEFAULTS.put("catcraft.StaffRole", "moderator");
        DEFAULTS.put("catcraft.PlayerClickMode", "normal");
        DEFAULTS.put("general.ChatIntegrationMode", "auto");
    }

    private CcmcConfig() {}

    public static synchronized void init() {
        VALUES.clear();
        VALUES.putAll(DEFAULTS);

        String legacyStaffRank = null;
        boolean explicitStaffRole = false;

        if (Files.isRegularFile(FILE)) {
            try {
                Matcher matcher = ENTRY.matcher(Files.readString(FILE, StandardCharsets.UTF_8));
                while (matcher.find()) {
                    String key = unescape(matcher.group(1));
                    Object parsed = parseValue(matcher.group(2));
                    if ("catcraft.StaffRank".equals(key)) {
                        legacyStaffRank = String.valueOf(parsed);
                        continue;
                    }
                    if (DEFAULTS.containsKey(key)) {
                        VALUES.put(key, parsed);
                        if ("catcraft.StaffRole".equals(key)) explicitStaffRole = true;
                    }
                }
            } catch (IOException ignored) {}
        }

        if (!explicitStaffRole && legacyStaffRank != null) {
            VALUES.put("catcraft.StaffRole", StaffRole.parse(legacyStaffRank).configValue());
        }

        forceLockedSettings();
        save();
        refreshCache();
    }

    public static void refreshCache() { forceLockedSettings(); }

    public static synchronized void save() {
        forceLockedSettings();
        try {
            Files.createDirectories(FILE.getParent());
            StringBuilder json = new StringBuilder("{\n");
            int i = 0;
            for (Map.Entry<String, Object> entry : VALUES.entrySet()) {
                if (i++ > 0) json.append(",\n");
                json.append("  \"").append(escape(entry.getKey())).append("\": ").append(jsonValue(entry.getValue()));
            }
            json.append("\n}\n");
            Files.writeString(FILE, json, StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
    }

    public static synchronized Object get(String key) { return VALUES.getOrDefault(key, DEFAULTS.get(key)); }
    public static boolean getBoolean(String key) { Object v = get(key); return v instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(v)); }
    public static int getInt(String key) { Object v = get(key); return v instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(v)); }
    public static long getLong(String key) { Object v = get(key); return v instanceof Number n ? n.longValue() : Long.parseLong(String.valueOf(v)); }
    public static float getFloat(String key) { Object v = get(key); return v instanceof Number n ? n.floatValue() : Float.parseFloat(String.valueOf(v)); }
    public static String getString(String key) { Object v = get(key); return v == null ? "" : String.valueOf(v); }
    public static Object getDefault(String key) { return DEFAULTS.get(key); }

    public static synchronized void set(String key, Object value) {
        if (!DEFAULTS.containsKey(key)) return;
        VALUES.put(key, value);
        forceLockedSettings();
    }

    private static void forceLockedSettings() {
        VALUES.put("general.MessageStacking.Enabled", false);
        VALUES.put("general.MessageStacking.ExactMatchOnly", false);
    }

    private static Object parseValue(String raw) {
        if (raw.startsWith("\"") && raw.endsWith("\"")) return unescape(raw.substring(1, raw.length() - 1));
        if ("true".equalsIgnoreCase(raw) || "false".equalsIgnoreCase(raw)) return Boolean.parseBoolean(raw);
        try {
            if (raw.contains(".")) return Double.parseDouble(raw);
            long value = Long.parseLong(raw);
            return value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE ? (int) value : value;
        } catch (NumberFormatException e) { return raw; }
    }

    private static String jsonValue(Object value) {
        if (value instanceof Boolean || value instanceof Number) return String.valueOf(value);
        return "\"" + escape(String.valueOf(value)) + "\"";
    }

    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
    private static String unescape(String value) { return value.replace("\\\"", "\"").replace("\\\\", "\\"); }
}
