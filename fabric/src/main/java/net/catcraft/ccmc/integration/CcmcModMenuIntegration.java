package net.catcraft.ccmc.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.catcraft.ccmc.config.CcmcSettingsScreen;

public final class CcmcModMenuIntegration
implements ModMenuApi {
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return CcmcSettingsScreen::create;
    }
}

