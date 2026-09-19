package net.catcraft.ccmc.gui;

import net.catcraft.ccmc.client.CcmcText;
import net.catcraft.ccmc.client.ClientFeedback;
import net.catcraft.ccmc.client.ClientScreens;
import net.catcraft.ccmc.config.CcmcConfig;
import net.catcraft.ccmc.config.StaffCapability;\nimport net.catcraft.ccmc.config.StaffRole;
import net.catcraft.ccmc.report.DiscordReportService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PlayerActionPopupScreen extends Screen {
    private enum View {
        ROOT, TELEPORT, MODERATE, INVESTIGATE, PLAYER_INFO, INVENTORIES, ANTICHEAT,
        COREPROTECT, TOOLS, CHAT_CHANNELS, WARN, KICK, TEMP_MUTE_DURATION,
        TEMP_MUTE_REASON, TEMP_BAN_DURATION, TEMP_BAN_REASON
    }

    private final Screen oldScreen;
    private final String playerName;
    private final int anchorX;
    private final int anchorY;
    private final View view;
    private final String duration;

    public PlayerActionPopupScreen(Screen oldScreen, String playerName, int anchorX, int anchorY) {
        this(oldScreen, playerName, anchorX, anchorY, View.ROOT, null);
    }

    private PlayerActionPopupScreen(Screen oldScreen, String playerName, int anchorX, int anchorY, View view, String duration) {
        super(Component.literal("CatCraft Staff Actions"));
        this.oldScreen = oldScreen;
        this.playerName = playerName;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.view = view;
        this.duration = duration;
    }

    @Override protected void init() {
        super.init();
        int w = 176;
        int x = Math.max(4, Math.min(anchorX, width - w - 4));
        int y = Math.max(20, Math.min(anchorY, height - 210));
        int row = 22;

        switch (view) {
            case ROOT -> {
                add(x, y, w / 2 - 2, "Message", () -> prefill("/msg " + playerName + " ", "Message"));
                add(x + w / 2 + 2, y, w / 2 - 2, "Mail", () -> prefill("/mail send " + playerName + " ", "Mail")); y += row;
                if (has(StaffCapability.PLAYER_TELEPORT)) { add(x, y, w, "Teleport", () -> show(View.TELEPORT, null)); y += row; }
                if (has(StaffCapability.BASIC_MODERATION)) {
                    add(x, y, w, "Moderate", () -> show(View.MODERATE, null)); y += row;
                    add(x, y, w, "Investigate", () -> show(View.INVESTIGATE, null)); y += row;
                }
                if (has(StaffCapability.MODERATOR_TOOLS)) { add(x, y, w, "Tools", () -> show(View.TOOLS, null)); y += row; }
                add(x, y, w, "Copy Username", this::copyUsername); y += row;
                add(x, y, w, "Close", this::onClose);
            }
            case TELEPORT -> {
                addCmd(x, y, w, "TPO to Player", "tpo " + playerName); y += row;
                addCmd(x, y, w, "TPO Here", "tphere " + playerName); y += row;
                addCmd(x, y, w, "Offline TP", "offlinetp " + playerName); y += row;
                addCmd(x, y, w, "Here Offline", "tphereoffline " + playerName); y += row;
                back(x, y, w, View.ROOT);
            }
            case MODERATE -> {
                addCmd(x, y, w, "Punish", "punish " + playerName); y += row;
                add(x, y, w, "Temp Mute", () -> show(View.TEMP_MUTE_DURATION, null)); y += row;
                add(x, y, w, "Warn", () -> show(View.WARN, null)); y += row;
                if (has(StaffCapability.MODERATOR_TOOLS)) { addCmd(x, y, w, "Unmute", "lunmute " + playerName); y += row; }
                add(x, y, w, "Kick", () -> show(View.KICK, null)); y += row;
                addCmd(x, y, w / 2 - 2, "Jail", "togglejail " + playerName + " 1");
                addCmd(x + w / 2 + 2, y, w / 2 - 2, "Unjail", "unjail " + playerName); y += row;
                if (has(StaffCapability.TEMP_BAN)) { add(x, y, w, "Temp Ban", () -> show(View.TEMP_BAN_DURATION, null)); y += row; }
                back(x, y, w, View.ROOT);
            }
            case WARN -> {
                reasonButton(x, y, w, "Causing Drama", "warn " + playerName + " Causing Drama"); y += row;
                reasonButton(x, y, w, "Spamming", "warn " + playerName + " Spamming"); y += row;
                reasonButton(x, y, w, "Begging", "warn " + playerName + " Begging"); y += row;
                reasonButton(x, y, w, "Mini-Modding", "warn " + playerName + " Please refrain from mini-modding players"); y += row;
                reasonButton(x, y, w, "Admin Demands", "warn " + playerName + " Continued demands for an admin despite assistance"); y += row;
                reasonButton(x, y, w, "Threatening Players", "warn " + playerName + " Threatening other players is against the server rules"); y += row;
                add(x, y, w, "Custom Reason", () -> prefill("/warn " + playerName + " ", "Warn")); y += row;
                back(x, y, w, View.MODERATE);
            }
            case KICK -> {
                addReportableCmd(x, y, w, "Inappropriate Name/Skin",
                        "kick " + playerName + " Inappropriate name/skin. Please change before re-joining or it will result in a ban!",
                        "Inappropriate Name/Skin", "Kick"); y += row;
                add(x, y, w, "Custom Reason", () -> prefill("/kick " + playerName + " ", "Kick")); y += row;
                back(x, y, w, View.MODERATE);
            }
            case TEMP_MUTE_DURATION -> {
                add(x, y, w, "5 min", () -> show(View.TEMP_MUTE_REASON, "5m")); y += row;
                add(x, y, w, "20 min", () -> show(View.TEMP_MUTE_REASON, "20m")); y += row;
                add(x, y, w, "Custom Duration", () -> prefill("/ltempmute " + playerName + " ", "Temp Mute")); y += row;
                back(x, y, w, View.MODERATE);
            }
            case TEMP_MUTE_REASON -> {
                addReportableCmd(x, y, w, "Causing Drama", "ltempmute " + playerName + " " + duration + " Causing Drama", "Causing Drama", "Temp Mute " + duration); y += row;
                addReportableCmd(x, y, w, "Spamming", "ltempmute " + playerName + " " + duration + " Spamming", "Spamming", "Temp Mute " + duration); y += row;
                addReportableCmd(x, y, w, "Begging", "ltempmute " + playerName + " " + duration + " Begging", "Begging", "Temp Mute " + duration); y += row;
                add(x, y, w, "Custom Reason", () -> prefill("/ltempmute " + playerName + " " + duration + " ", "Temp Mute")); y += row;
                back(x, y, w, View.TEMP_MUTE_DURATION);
            }
            case TEMP_BAN_DURATION -> {
                for (String d : new String[]{"3d", "1w", "2w", "3w", "4w"}) {
                    String value = d;
                    add(x, y, w, d, () -> show(View.TEMP_BAN_REASON, value)); y += row;
                }
                add(x, y, w, "Custom Duration", () -> prefill("/tempban " + playerName + " ", "Temp Ban")); y += row;
                back(x, y, w, View.MODERATE);
            }
            case TEMP_BAN_REASON -> {
                addBanReason(x, y, w, "Minor Grief/Stealing", "Minor Grief/Stealing"); y += row;
                addBanReason(x, y, w, "Medium Grief/Stealing", "Medium Grief/Stealing"); y += row;
                addBanReason(x, y, w, "TP-Killing", "TP-Killing"); y += row;
                addBanReason(x, y, w, "Map Art Theft/Cloning", "Stealing/Cloning Map Art"); y += row;
                addBanReason(x, y, w, "Base Raiding", "Base Raiding"); y += row;
                addBanReason(x, y, w, "Hacked Client", "Hacked Client"); y += row;
                addBanReason(x, y, w, "Inappropriate Name/Skin", "Inappropriate name/skin"); y += row;
                if ("3w".equals(duration)) { addBanReason(x, y, w, "Admitting to Xray", "Admitting to xray. Reduced ban for being honest. Next time, its a permanent ban."); y += row; }
                if ("4w".equals(duration)) { addBanReason(x, y, w, "Xraying", "Xraying. Next time, its a permanent ban."); y += row; }
                add(x, y, w, "Custom Reason", () -> prefill("/tempban " + playerName + " " + duration + " ", "Temp Ban")); y += row;
                back(x, y, w, View.TEMP_BAN_DURATION);
            }
            case INVESTIGATE -> {
                add(x, y, w, "Player Info", () -> show(View.PLAYER_INFO, null)); y += row;
                if (has(StaffCapability.ADVANCED_INVESTIGATION)) {
                    add(x, y, w, "Inventories", () -> show(View.INVENTORIES, null)); y += row;
                    add(x, y, w, "Anti-Cheat", () -> show(View.ANTICHEAT, null)); y += row;
                    add(x, y, w, "CoreProtect", () -> show(View.COREPROTECT, null)); y += row;
                }
                back(x, y, w, View.ROOT);
            }
            case PLAYER_INFO -> {
                addCmd(x, y, w, "History", "history " + playerName); y += row;
                addCmd(x, y, w, "Playtime", "eplaytime " + playerName); y += row;
                back(x, y, w, View.INVESTIGATE);
            }
            case INVENTORIES -> {
                addCmd(x, y, w, "Inventory", "open " + playerName); y += row;
                addCmd(x, y, w, "Ender Chest", "openender " + playerName); y += row;
                addCmd(x, y, w, "Trade Logs", "trade logs " + playerName); y += row;
                back(x, y, w, View.INVESTIGATE);
            }
            case ANTICHEAT -> {
                addCmd(x, y, w, "Vulcan Profile", "vulcan profile " + playerName); y += row;
                addCmd(x, y, w, "Violations", "vulcan violations " + playerName); y += row;
                addCmd(x, y, w, "CPS", "vulcan cps " + playerName); y += row;
                addCmd(x, y, w, "Knockback Test", "vulcan knockback " + playerName); y += row;
                addCmd(x, y, w, "Freeze", "vulcan freeze " + playerName); y += row;
                back(x, y, w, View.INVESTIGATE);
            }
            case COREPROTECT -> {
                addCmd(x, y, w, "All Actions (2d)", "co lookup user:" + playerName + " time:2d"); y += row;
                addCmd(x, y, w, "Container (2d)", "co lookup user:" + playerName + " time:2d action:container"); y += row;
                addCmd(x, y, w, "Pickup (2d)", "co lookup user:" + playerName + " time:2d action:pickup"); y += row;
                add(x, y, w, "Custom Lookup", () -> prefill("/co lookup user:" + playerName + " ", "CoreProtect Lookup")); y += row;
                back(x, y, w, View.INVESTIGATE);
            }
            case TOOLS -> {
                addCmd(x, y, w, "Vanish", "vanish"); y += row;
                addCmd(x, y, w, "Spectator Mode", "gamemode spectator"); y += row;
                addCmd(x, y, w, "Survival Mode", "gamemode survival"); y += row;
                if (has(StaffCapability.SENIOR_TOOLS)) {
                    addCmd(x, y, w / 2 - 2, "Fly", "fly");
                    addCmd(x + w / 2 + 2, y, w / 2 - 2, "God", "god"); y += row;
                }
                add(x, y, w, "Chat Channels", () -> show(View.CHAT_CHANNELS, null)); y += row;
                addCmd(x, y, w, "Ignore Claims", "ignoreclaims"); y += row;
                addCmd(x, y, w, "TradeShop Admin", "ts toggleadmin"); y += row;
                addCmd(x, y, w, "Report Lag", "reportlag"); y += row;
                back(x, y, w, View.ROOT);
            }
            case CHAT_CHANNELS -> {
                addCmd(x, y, w, "SCC Staff Chat", "scc"); y += row;
                add(x, y, w, "VSC Message", () -> prefill("/vsc ", "VSC Message")); y += row;
                addCmd(x, y, w, "Mod Chat", "channel mod"); y += row;
                addCmd(x, y, w, "Global", "g"); y += row;
                addCmd(x, y, w, "Chat On/Off", "togglechat"); y += row;
                back(x, y, w, View.TOOLS);
            }
        }
    }

    private void addBanReason(int x, int y, int w, String label, String reason) {
        addReportableCmd(x, y, w, label, "tempban " + playerName + " " + duration + " " + reason, label, "Temp Ban " + duration);
    }

    private void reasonButton(int x, int y, int w, String label, String command) {
        addReportableCmd(x, y, w, label, command, label, "Warn");
    }

    private void addReportableCmd(int x, int y, int w, String label, String command, String offense, String punishment) {
        add(x, y, w, label, () -> sendAndReport(command, offense, punishment));
    }

    private void addCmd(int x, int y, int w, String label, String command) { add(x, y, w, label, () -> send(command)); }
    private void back(int x, int y, int w, View destination) { add(x, y, w, "Back", () -> show(destination, null)); }

    private void add(int x, int y, int width, String label, Runnable action) {
        addRenderableWidget(Button.builder(Component.literal(label), b -> action.run()).pos(x, y).size(width, 20).build());
    }

    private void show(View next, String duration) { ClientScreens.show(new PlayerActionPopupScreen(oldScreen, playerName, anchorX, anchorY, next, duration)); }

    private StaffRole role() { return StaffRole.parse(CcmcConfig.getString("catcraft.StaffRole")); }
    private boolean has(StaffCapability capability) { return role().has(capability); }

    private void prefill(String text, String label) {
        if (!ChatInputPrefill.prefill(this, text)) local("[CatCraft Staff] couldn't prepare " + label + " in chat; no command was sent.");
    }

    private void sendAndReport(String command, String offense, String punishment) {
        if (!send(command)) return;
        ClientScreens.show(oldScreen);
        DiscordReportService.queue(playerName, offense, punishment);
    }

    private boolean send(String command) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            local("[CatCraft Staff] action unavailable: not connected to a server.");
            return false;
        }
        connection.sendCommand(command);
        local("[CatCraft Staff] ran /" + command);
        return true;
    }

    private void copyUsername() { Minecraft.getInstance().keyboardHandler.setClipboard(playerName); }
    private static void local(String message) { ClientFeedback.send(CcmcText.literal(message)); }
    @Override public void onClose() { ClientScreens.show(oldScreen); }
}
