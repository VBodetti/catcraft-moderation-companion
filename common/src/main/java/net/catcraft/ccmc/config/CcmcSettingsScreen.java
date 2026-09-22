package net.catcraft.ccmc.config;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import net.catcraft.ccmc.config.CcmcConfig;
import net.catcraft.ccmc.config.PlayerRank;
import net.catcraft.ccmc.config.StaffRole;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class CcmcSettingsScreen {
    private CcmcSettingsScreen() {
    }

    private static Component tr(String key) {
        return Component.translatable((String)key);
    }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(CcmcSettingsScreen.tr("key.ccmc.gui.title")).setTransparentBackground(true);
        builder.setSavingRunnable(CcmcConfig::save);
        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory profile = builder.getOrCreateCategory(CcmcSettingsScreen.tr("key.ccmc.category.profile"));
        CcmcSettingsScreen.addPlayerRank(profile, entries);
        CcmcSettingsScreen.addStaffRole(profile, entries);
        ConfigCategory general = builder.getOrCreateCategory(CcmcSettingsScreen.tr("key.ccmc.category.general"));
        CcmcSettingsScreen.addString(general, entries, "catcraft.PlayerClickMode");
        CcmcSettingsScreen.addString(general, entries, "general.ChatIntegrationMode");
        CcmcSettingsScreen.addBoolean(general, entries, "general.Timestamp.Enabled");
        CcmcSettingsScreen.addString(general, entries, "general.Timestamp.Pattern");
        CcmcSettingsScreen.addBoolean(general, entries, "general.Timestamp.CopyToChatBar.Enabled");
        CcmcSettingsScreen.addSlider(general, entries, "general.MessageStacking.MaxRepeatCount", 1, 500);
        CcmcSettingsScreen.addSlider(general, entries, "general.StoredChatLines", 100, 5000);
        return builder.build();
    }

    private static void addPlayerRank(ConfigCategory category, ConfigEntryBuilder entries) {
        PlayerRank current = PlayerRank.parse(CcmcConfig.getString("catcraft.PlayerRank"));
        PlayerRank defaultValue = PlayerRank.parse(String.valueOf(CcmcConfig.getDefault("catcraft.PlayerRank")));
        category.addEntry(entries.startEnumSelector(CcmcSettingsScreen.tr("key.ccmc.catcraft.PlayerRank"), PlayerRank.class, current).setDefaultValue(defaultValue).setTooltip(new Component[]{CcmcSettingsScreen.tr("key.ccmc.catcraft.PlayerRank.@Tooltip")}).setSaveConsumer(value -> CcmcConfig.set("catcraft.PlayerRank", value.configValue())).build());
    }

    private static void addStaffRole(ConfigCategory category, ConfigEntryBuilder entries) {
        StaffRole current = StaffRole.parse(CcmcConfig.getString("catcraft.StaffRole"));
        StaffRole defaultValue = StaffRole.parse(String.valueOf(CcmcConfig.getDefault("catcraft.StaffRole")));
        category.addEntry(entries.startEnumSelector(CcmcSettingsScreen.tr("key.ccmc.catcraft.StaffRole"), StaffRole.class, current).setDefaultValue(defaultValue).setTooltip(new Component[]{CcmcSettingsScreen.tr("key.ccmc.catcraft.StaffRole.@Tooltip")}).setSaveConsumer(value -> CcmcConfig.set("catcraft.StaffRole", value.configValue())).build());
    }

    private static void addString(ConfigCategory category, ConfigEntryBuilder entries, String key) {
        category.addEntry((AbstractConfigListEntry)entries.startStrField(CcmcSettingsScreen.tr("key.ccmc." + key), CcmcConfig.getString(key)).setDefaultValue(String.valueOf(CcmcConfig.getDefault(key))).setTooltip(new Component[]{CcmcSettingsScreen.tr("key.ccmc." + key + ".@Tooltip")}).setSaveConsumer(value -> CcmcConfig.set(key, value)).build());
    }

    private static void addBoolean(ConfigCategory category, ConfigEntryBuilder entries, String key) {
        category.addEntry(entries.startBooleanToggle(CcmcSettingsScreen.tr("key.ccmc." + key), CcmcConfig.getBoolean(key)).setDefaultValue((Boolean)CcmcConfig.getDefault(key)).setTooltip(new Component[]{CcmcSettingsScreen.tr("key.ccmc." + key + ".@Tooltip")}).setSaveConsumer(value -> CcmcConfig.set(key, value)).build());
    }

    private static void addSlider(ConfigCategory category, ConfigEntryBuilder entries, String key, int min, int max) {
        category.addEntry((AbstractConfigListEntry)entries.startIntSlider(CcmcSettingsScreen.tr("key.ccmc." + key), CcmcConfig.getInt(key), min, max).setDefaultValue(((Number)CcmcConfig.getDefault(key)).intValue()).setTooltip(new Component[]{CcmcSettingsScreen.tr("key.ccmc." + key + ".@Tooltip")}).setSaveConsumer(value -> CcmcConfig.set(key, value)).build());
    }
}
