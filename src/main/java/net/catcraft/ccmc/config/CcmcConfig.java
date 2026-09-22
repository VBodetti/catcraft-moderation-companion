package net.catcraft.ccmc.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.catcraft.ccmc.config.StaffRole;
import net.fabricmc.loader.api.FabricLoader;

public final class CcmcConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("catcraft_moderation_companion.json");
    private static final Map<String, Object> DEFAULTS = new LinkedHashMap<String, Object>();
    private static final Map<String, Object> VALUES = new LinkedHashMap<String, Object>();
    private static final Pattern ENTRY = Pattern.compile("\\\"((?:\\\\.|[^\\\"])*)\\\"\\s*:\\s*(\\\"(?:\\\\.|[^\\\"])*\\\"|true|false|-?\\d+(?:\\.\\d+)?)");

    private CcmcConfig() {
    }

    public static synchronized void init() {
        VALUES.clear();
        VALUES.putAll(DEFAULTS);
        String legacyStaffRank = null;
        boolean explicitStaffRole = false;
        if (Files.isRegularFile(FILE, new LinkOption[0])) {
            try {
                Matcher matcher = ENTRY.matcher(Files.readString(FILE, StandardCharsets.UTF_8));
                while (matcher.find()) {
                    String key = CcmcConfig.unescape(matcher.group(1));
                    Object parsed = CcmcConfig.parseValue(matcher.group(2));
                    if ("catcraft.StaffRank".equals(key)) {
                        legacyStaffRank = String.valueOf(parsed);
                        continue;
                    }
                    if (!DEFAULTS.containsKey(key)) continue;
                    VALUES.put(key, parsed);
                    if (!"catcraft.StaffRole".equals(key)) continue;
                    explicitStaffRole = true;
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        if (!explicitStaffRole && legacyStaffRank != null) {
            VALUES.put("catcraft.StaffRole", StaffRole.parse(legacyStaffRank).configValue());
        }
        CcmcConfig.forceLockedSettings();
        CcmcConfig.save();
        CcmcConfig.refreshCache();
    }

    public static void refreshCache() {
        CcmcConfig.forceLockedSettings();
    }

    public static synchronized void save() {
        CcmcConfig.forceLockedSettings();
        try {
            Files.createDirectories(FILE.getParent(), new FileAttribute[0]);
            StringBuilder json = new StringBuilder("{\n");
            int i = 0;
            for (Map.Entry<String, Object> entry : VALUES.entrySet()) {
                if (i++ > 0) {
                    json.append(",\n");
                }
                json.append("  \"").append(CcmcConfig.escape(entry.getKey())).append("\": ").append(CcmcConfig.jsonValue(entry.getValue()));
            }
            json.append("\n}\n");
            Files.writeString(FILE, (CharSequence)json, StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static synchronized Object get(String key) {
        return VALUES.getOrDefault(key, DEFAULTS.get(key));
    }

    public static boolean getBoolean(String key) {
        boolean bl;
        Object v = CcmcConfig.get(key);
        if (v instanceof Boolean) {
            Boolean b = (Boolean)v;
            bl = b;
        } else {
            bl = Boolean.parseBoolean(String.valueOf(v));
        }
        return bl;
    }

    public static int getInt(String key) {
        int n;
        Object v = CcmcConfig.get(key);
        if (v instanceof Number) {
            Number n2 = (Number)v;
            n = n2.intValue();
        } else {
            n = Integer.parseInt(String.valueOf(v));
        }
        return n;
    }

    public static long getLong(String key) {
        long l;
        Object v = CcmcConfig.get(key);
        if (v instanceof Number) {
            Number n = (Number)v;
            l = n.longValue();
        } else {
            l = Long.parseLong(String.valueOf(v));
        }
        return l;
    }

    public static float getFloat(String key) {
        float f;
        Object v = CcmcConfig.get(key);
        if (v instanceof Number) {
            Number n = (Number)v;
            f = n.floatValue();
        } else {
            f = Float.parseFloat(String.valueOf(v));
        }
        return f;
    }

    public static String getString(String key) {
        Object v = CcmcConfig.get(key);
        return v == null ? "" : String.valueOf(v);
    }

    public static Object getDefault(String key) {
        return DEFAULTS.get(key);
    }

    public static synchronized void set(String key, Object value) {
        if (!DEFAULTS.containsKey(key)) {
            return;
        }
        VALUES.put(key, value);
        CcmcConfig.forceLockedSettings();
    }

    private static void forceLockedSettings() {
        VALUES.put("general.MessageStacking.Enabled", false);
        VALUES.put("general.MessageStacking.ExactMatchOnly", false);
    }

    private static Object parseValue(String raw) {
        if (raw.startsWith("\"") && raw.endsWith("\"")) {
            return CcmcConfig.unescape(raw.substring(1, raw.length() - 1));
        }
        if ("true".equalsIgnoreCase(raw) || "false".equalsIgnoreCase(raw)) {
            return Boolean.parseBoolean(raw);
        }
        try {
            if (raw.contains(".")) {
                return Double.parseDouble(raw);
            }
            long value = Long.parseLong(raw);
            return value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE ? (long)((int)value) : value;
        }
        catch (NumberFormatException e) {
            return raw;
        }
    }

    private static String jsonValue(Object value) {
        if (value instanceof Boolean || value instanceof Number) {
            return String.valueOf(value);
        }
        return "\"" + CcmcConfig.escape(String.valueOf(value)) + "\"";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unescape(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    static {
        DEFAULTS.put("config.version", 3.0);
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
}

