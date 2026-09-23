package net.catcraft.ccmc;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.catcraft.ccmc.OpenStaffMenuCommand;
import net.catcraft.ccmc.StaffMenuKeyHandler;
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
import net.minecraft.network.chat.Component;

public final class CatCraftModerationCompanion
implements ModInitializer {
    private static final String PLAYER_RANK_KEY = "catcraft.PlayerRank";
    private static final String STAFF_ROLE_KEY = "catcraft.StaffRole";
    private boolean settingsOpenPending;

    public void onInitialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (this.settingsOpenPending) {
                this.settingsOpenPending = false;
                ClientScreens.show(CcmcSettingsScreen.create(ClientScreens.current()));
            }
            DiscordReportService.tick(client);
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommands.literal((String)"ccc").executes(ctx -> this.showProfile())).then(ClientCommands.literal((String)"profile").executes(ctx -> this.showProfile()))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommands.literal((String)"player-rank").executes(ctx -> this.showPlayerRank())).then(this.playerRankLiteral("cat", PlayerRank.CAT))).then(this.playerRankLiteral("leopard", PlayerRank.LEOPARD))).then(this.playerRankLiteral("cheetah", PlayerRank.CHEETAH))).then(this.playerRankLiteral("jaguar", PlayerRank.JAGUAR))).then(this.playerRankLiteral("tiger", PlayerRank.TIGER))).then(this.playerRankLiteral("lion", PlayerRank.LION)))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommands.literal((String)"role").executes(ctx -> this.showStaffRole())).then(this.staffRoleLiteral("none", StaffRole.NONE))).then(this.staffRoleLiteral("helper", StaffRole.HELPER))).then(this.staffRoleLiteral("moderator", StaffRole.MODERATOR))).then(this.staffRoleLiteral("senior", StaffRole.SENIOR_MODERATOR))).then(this.staffRoleLiteral("administrator", StaffRole.ADMINISTRATOR))).then(this.staffRoleLiteral("senior-admin", StaffRole.SENIOR_ADMINISTRATOR))).then(this.staffRoleLiteral("developer", StaffRole.DEVELOPER))).then(this.staffRoleLiteral("owner", StaffRole.OWNER)))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommands.literal((String)"rank").executes(ctx -> this.showStaffRole())).then(this.staffRoleLiteral("helper", StaffRole.HELPER))).then(this.staffRoleLiteral("moderator", StaffRole.MODERATOR))).then(this.staffRoleLiteral("senior", StaffRole.SENIOR_MODERATOR))).then(this.staffRoleLiteral("administrator", StaffRole.ADMINISTRATOR)))).then(ClientCommands.literal((String)"settings").executes(ctx -> this.queueSettings()))).then(ClientCommands.literal((String)"menu").executes((Command)new OpenStaffMenuCommand()))));
        StaffMenuKeyHandler.register();
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> playerRankLiteral(String literal, PlayerRank rank) {
        return (LiteralArgumentBuilder)ClientCommands.literal((String)literal).executes(ctx -> this.setPlayerRank(rank));
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> staffRoleLiteral(String literal, StaffRole role) {
        return (LiteralArgumentBuilder)ClientCommands.literal((String)literal).executes(ctx -> this.setStaffRole(role));
    }

    private int showProfile() {
        CatCraftModerationCompanion.local("[CCC] CatCraft profile: " + this.currentPlayerRank().displayName() + " \u2022 " + this.currentStaffRole().displayName());
        CatCraftModerationCompanion.local("[CCC] Use /ccc menu for Companion tools or /ccc settings to change your profile.");
        return 1;
    }

    private int showPlayerRank() {
        CatCraftModerationCompanion.local("[CCC] Player rank: " + this.currentPlayerRank().displayName());
        return 1;
    }

    private int showStaffRole() {
        CatCraftModerationCompanion.local("[CCC] Staff role: " + this.currentStaffRole().displayName());
        return 1;
    }

    private int queueSettings() {
        this.settingsOpenPending = true;
        return 1;
    }

    private int setPlayerRank(PlayerRank rank) {
        PlayerRank before = this.currentPlayerRank();
        CcmcConfig.set(PLAYER_RANK_KEY, rank.configValue());
        CcmcConfig.save();
        CatCraftModerationCompanion.local("[CCC] Player rank changed: " + before.displayName() + " -> " + rank.displayName());
        return 1;
    }

    private int setStaffRole(StaffRole role) {
        StaffRole before = this.currentStaffRole();
        CcmcConfig.set(STAFF_ROLE_KEY, role.configValue());
        CcmcConfig.save();
        CatCraftModerationCompanion.local("[CCC] Staff role changed: " + before.displayName() + " -> " + role.displayName());
        return 1;
    }

    private PlayerRank currentPlayerRank() {
        return PlayerRank.parse(CcmcConfig.getString(PLAYER_RANK_KEY));
    }

    private StaffRole currentStaffRole() {
        return StaffRole.parse(CcmcConfig.getString(STAFF_ROLE_KEY));
    }

    private static void local(String message) {
        ClientFeedback.send((Component)CcmcText.literal(message));
    }
}
