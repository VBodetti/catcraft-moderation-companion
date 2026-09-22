package net.catcraft.ccmc;

import net.catcraft.ccmc.config.CcmcConfig;
import net.fabricmc.api.ModInitializer;

public final class CcmcCore
implements ModInitializer {
    public void onInitialize() {
        CcmcConfig.init();
    }
}

