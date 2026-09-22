package net.catcraft.ccmc.platform;

import java.nio.file.Path;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

/** NeoForge-specific access to loader services used by shared Companion code. */
public final class PlatformBridge {
    private PlatformBridge() {
    }

    public static Path configDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static Path gameDirectory() {
        return FMLPaths.GAMEDIR.get();
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
