package net.catcraft.ccmc.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class CcmcSettingsScreen {
    private CcmcSettingsScreen() {}
    private static Component tr(String key) { return Component.translatable(key); }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(tr("key.ccmc.gui.title"))
                .setTransparentBackground(true);
        builder.setSavingRunnable(CcmcConfig::save);
        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(tr("key.ccmc.category.general"));

        addString(general, entries, "catcraft.StaffRank");
        addString(general, entries, "catcraft.PlayerClickMode");
        addString(general, entries, "general.ChatIntegrationMode");
        addBoolean(general, entries, "general.Timestamp.Enabled");
        addString(general, entries, "general.Timestamp.Pattern");
        addBoolean(general, entries, "general.Timestamp.CopyToChatBar.Enabled");
        addSlider(general, entries, "general.MessageStacking.MaxRepeatCount", 1, 500);
        addSlider(general, entries, "general.StoredChatLines", 100, 5000);
        return builder.build();
    }

    private static void addString(ConfigCategory category, ConfigEntryBuilder entries, String key) {
        category.addEntry(entries.startStrField(tr("key.ccmc." + key), CcmcConfig.getString(key))
                .setDefaultValue(String.valueOf(CcmcConfig.getDefault(key)))
                .setTooltip(tr("key.ccmc." + key + ".@Tooltip"))
                .setSaveConsumer(value -> CcmcConfig.set(key, value))
                .build());
    }

    private static void addBoolean(ConfigCategory category, ConfigEntryBuilder entries, String key) {
        category.addEntry(entries.startBooleanToggle(tr("key.ccmc." + key), CcmcConfig.getBoolean(key))
                .setDefaultValue((Boolean) CcmcConfig.getDefault(key))
                .setTooltip(tr("key.ccmc." + key + ".@Tooltip"))
                .setSaveConsumer(value -> CcmcConfig.set(key, value))
                .build());
    }

    private static void addSlider(ConfigCategory category, ConfigEntryBuilder entries, String key, int min, int max) {
        category.addEntry(entries.startIntSlider(tr("key.ccmc." + key), CcmcConfig.getInt(key), min, max)
                .setDefaultValue(((Number) CcmcConfig.getDefault(key)).intValue())
                .setTooltip(tr("key.ccmc." + key + ".@Tooltip"))
                .setSaveConsumer(value -> CcmcConfig.set(key, value))
                .build());
    }
}
