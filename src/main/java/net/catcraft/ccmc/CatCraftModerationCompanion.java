package net.catcraft.ccmc;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.catcraft.ccmc.client.CcmcText;
import net.catcraft.ccmc.client.ClientFeedback;
import net.catcraft.ccmc.client.ClientScreens;
import net.catcraft.ccmc.config.CcmcConfig;
import net.catcraft.ccmc.config.CcmcSettingsScreen;
import net.catcraft.ccmc.config.PlayerRank;
import net.catcraft.ccmc.config.StaffRole;
import net.catcraft.ccmc.report.DiscordReportService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class CatCraftModerationCompanion implements ModInitializer {
    private static final String PLAYER_RANK_KEY = "catcraft.PlayerRank";
    private static final String STAFF_ROLE_KEY = "catcraft.StaffRole";
    private boolean settingsOpenPending;

    @Override public void onInitialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (settingsOpenPending) {
                settingsOpenPending = false;
                ClientScreens.show(CcmcSettingsScreen.create(ClientScreens.current()));
            }
            DiscordReportService.tick(client);
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> dispatcher.register(
                ClientCommands.literal("ccmc")
                        .executes(ctx -> showProfile())
                        .then(ClientCommands.literal("profile").executes(ctx -> showProfile()))
                        .then(ClientCommands.literal("player-rank")
                                .executes(ctx -> showPlayerRank())
                                .then(playerRankLiteral("member", PlayerRank.MEMBER))
                                .then(playerRankLiteral("cat", PlayerRank.CAT))
                                .then(playerRankLiteral("leopard", PlayerRank.LEOPARD))
                                .then(playerRankLiteral("cheetah", PlayerRank.CHEETAH))
                                .then(playerRankLiteral("jaguar", PlayerRank.JAGUAR))
                                .then(playerRankLiteral("tiger", PlayerRank.TIGER))
                                .then(playerRankLiteral("lion", PlayerRank.LION)))
                        .then(ClientCommands.literal("role")
                                .executes(ctx -> showStaffRole())
                                .then(staffRoleLiteral("none", StaffRole.NONE))
                                .then(staffRoleLiteral("helper", StaffRole.HELPER))
                                .then(staffRoleLiteral("moderator", StaffRole.MODERATOR))
                                .then(staffRoleLiteral("senior", StaffRole.SENIOR_MODERATOR))
                                .then(staffRoleLiteral("administrator", StaffRole.ADMINISTRATOR))
                                .then(staffRoleLiteral("senior-admin", StaffRole.SENIOR_ADMINISTRATOR))
                                .then(staffRoleLiteral("developer", StaffRole.DEVELOPER))
                                .then(staffRoleLiteral("owner", StaffRole.OWNER)))
                        // Legacy 1.0/early-1.1 alias. "rank" continues to mean staff role.
                        .then(ClientCommands.literal("rank")
                                .executes(ctx -> showStaffRole())
                                .then(staffRoleLiteral("helper", StaffRole.HELPER))
                                .then(staffRoleLiteral("moderator", StaffRole.MODERATOR))
                                .then(staffRoleLiteral("senior", StaffRole.SENIOR_MODERATOR))
                                .then(staffRoleLiteral("administrator", StaffRole.ADMINISTRATOR)))
                        .then(ClientCommands.literal("settings").executes(ctx -> queueSettings()))
                        .then(ClientCommands.literal("menu").executes(new OpenStaffMenuCommand()))));

        StaffMenuKeyHandler.register();
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> playerRankLiteral(String literal, PlayerRank rank) {
        return ClientCommands.literal(literal).executes(ctx -> setPlayerRank(rank));
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> staffRoleLiteral(String literal, StaffRole role) {
        return ClientCommands.literal(literal).executes(ctx -> setStaffRole(role));
    }

    private int showProfile() {
        local("[CCMC] CatCraft profile: " + currentPlayerRank().displayName() + " • " + currentStaffRole().displayName());
        local("[CCMC] Use /ccmc menu for Companion tools or /ccmc settings to change your profile.");
        return 1;
    }

    private int showPlayerRank() {
        local("[CCMC] Player rank: " + currentPlayerRank().displayName());
        return 1;
    }

    private int showStaffRole() {
        local("[CCMC] Staff role: " + currentStaffRole().displayName());
        return 1;
    }

    private int queueSettings() {
        settingsOpenPending = true;
        return 1;
    }

    private int setPlayerRank(PlayerRank rank) {
        PlayerRank before = currentPlayerRank();
        CcmcConfig.set(PLAYER_RANK_KEY, rank.configValue());
        CcmcConfig.save();
        local("[CCMC] Player rank changed: " + before.displayName() + " -> " + rank.displayName());
        return 1;
    }

    private int setStaffRole(StaffRole role) {
        StaffRole before = currentStaffRole();
        CcmcConfig.set(STAFF_ROLE_KEY, role.configValue());
        CcmcConfig.save();
        local("[CCMC] Staff role changed: " + before.displayName() + " -> " + role.displayName());
        return 1;
    }

    private PlayerRank currentPlayerRank() { return PlayerRank.parse(CcmcConfig.getString(PLAYER_RANK_KEY)); }
    private StaffRole currentStaffRole() { return StaffRole.parse(CcmcConfig.getString(STAFF_ROLE_KEY)); }
    private static void local(String message) { ClientFeedback.send(CcmcText.literal(message)); }
}
