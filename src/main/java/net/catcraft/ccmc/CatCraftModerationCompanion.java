package net.catcraft.ccmc;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.catcraft.ccmc.client.CcmcText;
import net.catcraft.ccmc.client.ClientFeedback;
import net.catcraft.ccmc.client.ClientScreens;
import net.catcraft.ccmc.config.CcmcConfig;
import net.catcraft.ccmc.config.CcmcSettingsScreen;
import net.catcraft.ccmc.config.StaffRank;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class CatCraftModerationCompanion implements ModInitializer {
    private static final String RANK_KEY = "catcraft.StaffRank";
    private boolean settingsOpenPending;

    @Override public void onInitialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (settingsOpenPending) {
                settingsOpenPending = false;
                ClientScreens.show(CcmcSettingsScreen.create(ClientScreens.current()));
            }
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> dispatcher.register(
                ClientCommands.literal("ccmc")
                        .executes(ctx -> showStatus())
                        .then(ClientCommands.literal("rank")
                                .executes(ctx -> showRank())
                                .then(rankLiteral("helper", StaffRank.HELPER))
                                .then(rankLiteral("moderator", StaffRank.MODERATOR))
                                .then(rankLiteral("senior", StaffRank.SENIOR_MODERATOR))
                                .then(rankLiteral("administrator", StaffRank.ADMINISTRATOR)))
                        .then(ClientCommands.literal("settings").executes(ctx -> queueSettings()))
                        .then(ClientCommands.literal("menu").executes(new OpenStaffMenuCommand()))));
        StaffMenuKeyHandler.register();
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> rankLiteral(String literal, StaffRank rank) {
        return ClientCommands.literal(literal).executes(ctx -> setRank(rank));
    }

    private int showStatus() {
        local("[CatCraft Staff] Moderation Companion ready. Staff rank: " + currentRank().displayName());
        local("[CatCraft Staff] Use /ccmc menu for staff tools or /ccmc settings for configuration.");
        return 1;
    }

    private int showRank() {
        local("[CatCraft Staff] Current staff rank: " + currentRank().displayName());
        return 1;
    }

    private int queueSettings() {
        settingsOpenPending = true;
        return 1;
    }

    private int setRank(StaffRank rank) {
        StaffRank before = currentRank();
        CcmcConfig.set(RANK_KEY, rank.configValue());
        CcmcConfig.save();
        local("[CatCraft Staff] Staff rank changed: " + before.displayName() + " -> " + rank.displayName());
        return 1;
    }

    private StaffRank currentRank() { return StaffRank.parse(CcmcConfig.getString(RANK_KEY)); }
    private static void local(String message) { ClientFeedback.send(CcmcText.literal(message)); }
}
