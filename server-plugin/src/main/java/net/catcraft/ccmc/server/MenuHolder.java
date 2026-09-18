package net.catcraft.ccmc.server;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Objects;
import java.util.UUID;

final class MenuHolder implements InventoryHolder {
    enum Type {
        STAFF_ROOT,
        PLAYER_SELECT,
        PLAYER_ROOT,
        TELEPORT,
        MODERATE,
        WARN,
        KICK,
        TEMP_MUTE_DURATION,
        TEMP_MUTE_REASON,
        TEMP_BAN_DURATION,
        TEMP_BAN_REASON,
        INVESTIGATE,
        PLAYER_INFO,
        INVENTORIES,
        ANTICHEAT,
        COREPROTECT,
        PLAYER_TOOLS,
        SERVER_TOOLS,
        CHAT_CHANNELS
    }

    private final Type type;
    private final UUID targetId;
    private final String data;
    private final int page;
    private Inventory inventory;

    MenuHolder(Type type, UUID targetId, String data, int page) {
        this.type = Objects.requireNonNull(type, "type");
        this.targetId = targetId;
        this.data = data;
        this.page = page;
    }

    void attach(Inventory inventory) {
        this.inventory = Objects.requireNonNull(inventory, "inventory");
    }

    Type type() {
        return type;
    }

    UUID targetId() {
        return targetId;
    }

    String data() {
        return data;
    }

    int page() {
        return page;
    }

    @Override
    public Inventory getInventory() {
        return Objects.requireNonNull(inventory, "inventory not attached yet");
    }
}
