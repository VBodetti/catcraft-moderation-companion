package net.catcraft.ccmc.chat;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.UUID;
import net.catcraft.ccmc.chat.ChatMessageRecord;
import net.catcraft.ccmc.config.CcmcConfig;
import net.minecraft.network.chat.Component;

public final class ChatHistoryStore {
    private static final LinkedHashMap<String, ChatMessageRecord> records = new LinkedHashMap();
    private static ChatMessageRecord latestRecord;

    private ChatHistoryStore() {
    }

    public static synchronized String newId(Component ignored) {
        String id;
        while (records.containsKey(id = UUID.randomUUID().toString().replace("-", "").substring(0, 6))) {
        }
        return id;
    }

    public static synchronized void remember(String id, ChatMessageRecord record) {
        Iterator<String> it;
        records.put(id, record);
        latestRecord = record;
        int max = Math.max(CcmcConfig.getInt("general.StoredChatLines"), 1);
        while (records.size() > max && (it = records.keySet().iterator()).hasNext()) {
            it.next();
            it.remove();
        }
    }

    public static synchronized ChatMessageRecord find(String id) {
        return records.get(id);
    }

    public static synchronized ChatMessageRecord latest() {
        return latestRecord;
    }
}

