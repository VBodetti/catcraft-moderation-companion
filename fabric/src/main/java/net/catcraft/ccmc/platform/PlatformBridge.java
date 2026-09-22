package net.catcraft.ccmc.platform;

import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Fabric-specific access to loader services used by shared Companion code. */
public final class PlatformBridge {
    private PlatformBridge() {
    }

    public static Path configDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static Path gameDirectory() {
        return FabricLoader.getInstance().getGameDir();
    }

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
