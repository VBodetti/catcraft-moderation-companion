package net.catcraft.ccmc;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.catcraft.ccmc.client.CcmcText;
import net.catcraft.ccmc.client.ClientFeedback;
import net.catcraft.ccmc.client.ClientScreens;
import net.catcraft.ccmc.config.CcmcConfig;
import net.catcraft.ccmc.config.CcmcSettingsScreen;
import net.catcraft.ccmc.config.PlayerRank;
import net.catcraft.ccmc.config.StaffRole;
import net.catcraft.ccmc.gui.StaffMenuScreen;
import net.catcraft.ccmc.report.DiscordReportService;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = CatCraftCompanionNeoForge.MOD_ID, dist = Dist.CLIENT)
public final class CatCraftCompanionNeoForge {
    public static final String MOD_ID = "catcraft_moderation_companion";
    private static final String PLAYER_RANK_KEY = "catcraft.PlayerRank";
    private static final String STAFF_ROLE_KEY = "catcraft.StaffRole";

    private KeyMapping companionKey;
    private boolean menuOpenPending;
    private boolean settingsOpenPending;

    public CatCraftCompanionNeoForge(IEventBus modBus, ModContainer container) {
        CcmcConfig.init();
        modBus.addListener(this::registerKeyMappings);
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (ignored, parent) -> CcmcSettingsScreen.create(parent));
        NeoForge.EVENT_BUS.addListener(this::registerClientCommands);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        KeyMapping.Category category = new KeyMapping.Category(
                Identifier.fromNamespaceAndPath(MOD_ID, "catcraft_staff"));
        event.registerCategory(category);
        this.companionKey = new KeyMapping(
                "key.ccmc.staff_menu",
                InputConstants.Type.KEYSYM,
                297,
                category);
        event.register(this.companionKey);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (this.settingsOpenPending) {
            this.settingsOpenPending = false;
            ClientScreens.show(CcmcSettingsScreen.create(ClientScreens.current()));
        }
        if (this.menuOpenPending) {
            this.menuOpenPending = false;
            StaffMenuScreen.open();
        }
        if (this.companionKey != null) {
            while (this.companionKey.consumeClick()) {
                StaffMenuScreen.open();
            }
        }
        DiscordReportService.tick(client);
    }

    private void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("ccc")
                        .executes(ctx -> this.showProfile())
                        .then(Commands.literal("profile").executes(ctx -> this.showProfile()))
                        .then(Commands.literal("player-rank")
                                .executes(ctx -> this.showPlayerRank())
                                .then(this.playerRankLiteral("cat", PlayerRank.CAT))
                                .then(this.playerRankLiteral("leopard", PlayerRank.LEOPARD))
                                .then(this.playerRankLiteral("cheetah", PlayerRank.CHEETAH))
                                .then(this.playerRankLiteral("jaguar", PlayerRank.JAGUAR))
                                .then(this.playerRankLiteral("tiger", PlayerRank.TIGER))
                                .then(this.playerRankLiteral("lion", PlayerRank.LION)))
                        .then(Commands.literal("role")
                                .executes(ctx -> this.showStaffRole())
                                .then(this.staffRoleLiteral("none", StaffRole.NONE))
                                .then(this.staffRoleLiteral("helper", StaffRole.HELPER))
                                .then(this.staffRoleLiteral("moderator", StaffRole.MODERATOR))
                                .then(this.staffRoleLiteral("senior", StaffRole.SENIOR_MODERATOR))
                                .then(this.staffRoleLiteral("administrator", StaffRole.ADMINISTRATOR))
                                .then(this.staffRoleLiteral("senior-admin", StaffRole.SENIOR_ADMINISTRATOR))
                                .then(this.staffRoleLiteral("developer", StaffRole.DEVELOPER))
                                .then(this.staffRoleLiteral("owner", StaffRole.OWNER)))
                        .then(Commands.literal("rank")
                                .executes(ctx -> this.showStaffRole())
                                .then(this.staffRoleLiteral("helper", StaffRole.HELPER))
                                .then(this.staffRoleLiteral("moderator", StaffRole.MODERATOR))
                                .then(this.staffRoleLiteral("senior", StaffRole.SENIOR_MODERATOR))
                                .then(this.staffRoleLiteral("administrator", StaffRole.ADMINISTRATOR)))
                        .then(Commands.literal("settings").executes(ctx -> this.queueSettings()))
                        .then(Commands.literal("menu").executes(ctx -> this.queueMenu())));
    }

    private LiteralArgumentBuilder<CommandSourceStack> playerRankLiteral(String literal, PlayerRank rank) {
        return Commands.literal(literal).executes(ctx -> this.setPlayerRank(rank));
    }

    private LiteralArgumentBuilder<CommandSourceStack> staffRoleLiteral(String literal, StaffRole role) {
        return Commands.literal(literal).executes(ctx -> this.setStaffRole(role));
    }

    private int showProfile() {
        local("[CCC] CatCraft profile: " + this.currentPlayerRank().displayName()
                + " • " + this.currentStaffRole().displayName());
        local("[CCC] Use /ccc menu for Companion tools or /ccc settings to change your profile.");
        return 1;
    }

    private int showPlayerRank() {
        local("[CCC] Player rank: " + this.currentPlayerRank().displayName());
        return 1;
    }

    private int showStaffRole() {
        local("[CCC] Staff role: " + this.currentStaffRole().displayName());
        return 1;
    }

    private int queueSettings() {
        this.settingsOpenPending = true;
        return 1;
    }

    private int queueMenu() {
        this.menuOpenPending = true;
        return 1;
    }

    private int setPlayerRank(PlayerRank rank) {
        PlayerRank before = this.currentPlayerRank();
        CcmcConfig.set(PLAYER_RANK_KEY, rank.configValue());
        CcmcConfig.save();
        local("[CCC] Player rank changed: " + before.displayName() + " -> " + rank.displayName());
        return 1;
    }

    private int setStaffRole(StaffRole role) {
        StaffRole before = this.currentStaffRole();
        CcmcConfig.set(STAFF_ROLE_KEY, role.configValue());
        CcmcConfig.save();
        local("[CCC] Staff role changed: " + before.displayName() + " -> " + role.displayName());
        return 1;
    }

    private PlayerRank currentPlayerRank() {
        return PlayerRank.parse(CcmcConfig.getString(PLAYER_RANK_KEY));
    }

    private StaffRole currentStaffRole() {
        return StaffRole.parse(CcmcConfig.getString(STAFF_ROLE_KEY));
    }

    private static void local(String message) {
        ClientFeedback.send((Component) CcmcText.literal(message));
    }
}
