package net.catcraft.ccmc.gui;

import java.lang.reflect.Method;
import net.catcraft.ccmc.client.CcmcText;
import net.catcraft.ccmc.client.ClientFeedback;
import net.catcraft.ccmc.client.ClientScreens;
import net.catcraft.ccmc.config.CcmcConfig;
import net.catcraft.ccmc.config.StaffCapability;
import net.catcraft.ccmc.config.StaffRole;
import net.catcraft.ccmc.gui.ChatInputPrefill;
import net.catcraft.ccmc.report.DiscordReportService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;

public final class LegacyStaffActionScreen
extends Screen {
    private final Screen oldScreen;
    private final String playerName;
    private final int anchorX;
    private final int anchorY;
    private final Mode mode;
    private final View view;
    private final String duration;

    public LegacyStaffActionScreen(Screen screen, String string, int n, int n2) {
        this(screen, string, n, n2, LegacyStaffActionScreen.defaultMode(), View.ROOT, null);
    }

    public static void openQuickMute(Screen parent, String playerName) {
        ClientScreens.show(new LegacyStaffActionScreen(parent, playerName, 20, 40, Mode.STAFF, View.TEMP_MUTE_REASON, "20m"));
    }

    public static void openQuickWarn(Screen parent, String playerName) {
        ClientScreens.show(new LegacyStaffActionScreen(parent, playerName, 20, 40, Mode.STAFF, View.WARN, null));
    }

    private LegacyStaffActionScreen(Screen screen, String string, int n, int n2, Mode mode, View view, String string2) {
        super((Component)Component.literal((String)(mode == Mode.STAFF ? "CatCraft Staff Actions" : "CatCraft Player Actions")));
        this.oldScreen = screen;
        this.playerName = string;
        this.anchorX = n;
        this.anchorY = n2;
        this.mode = mode;
        this.view = view;
        this.duration = string2;
    }

    protected void init() {
        super.init();
        int n = 176;
        int n2 = Math.max(4, Math.min(this.anchorX, this.width - n - 4));
        int n3 = Math.max(20, Math.min(this.anchorY, this.height - 210));
        int n4 = 22;
        if (this.hasStaffAccess()) {
            this.addTabs(n2, n3, n);
            n3 += 18;
        }
        if (this.mode == Mode.PLAYER) {
            this.playerActions(n2, n3, n, n4);
            return;
        }
        switch (this.view.ordinal()) {
            case 0: {
                this.add(n2, n3, n / 2 - 2, "Message", () -> this.prefill("/msg " + this.playerName + " ", "Message"));
                this.add(n2 + n / 2 + 2, n3, n / 2 - 2, "Mail", () -> this.prefill("/mail send " + this.playerName + " ", "Mail"));
                n3 += n4;
                if (this.has(StaffCapability.PLAYER_TELEPORT)) {
                    this.add(n2, n3, n, "Teleport", () -> this.show(View.TELEPORT, null));
                    n3 += n4;
                }
                if (this.has(StaffCapability.BASIC_MODERATION)) {
                    this.add(n2, n3, n, "Moderate", () -> this.show(View.MODERATE, null));
                    this.add(n2, n3 += n4, n, "Investigate", () -> this.show(View.INVESTIGATE, null));
                    n3 += n4;
                }
                if (this.has(StaffCapability.MODERATOR_TOOLS)) {
                    this.add(n2, n3, n, "Tools", () -> this.show(View.TOOLS, null));
                    n3 += n4;
                }
                this.add(n2, n3, n, "Copy Username", this::copyUsername);
                this.add(n2, n3 += n4, n, "Close", this::onClose);
                break;
            }
            case 1: {
                this.addCmd(n2, n3, n, "TPO to Player", "tpo " + this.playerName);
                this.addCmd(n2, n3 += n4, n, "TPO Here", "tphere " + this.playerName);
                this.addCmd(n2, n3 += n4, n, "Offline TP", "offlinetp " + this.playerName);
                this.addCmd(n2, n3 += n4, n, "Here Offline", "tphereoffline " + this.playerName);
                this.back(n2, n3 += n4, n, View.ROOT);
                break;
            }
            case 2: {
                this.addCmd(n2, n3, n, "Punish", "punish " + this.playerName);
                this.add(n2, n3 += n4, n, "Temp Mute", () -> this.show(View.TEMP_MUTE_DURATION, null));
                this.add(n2, n3 += n4, n, "Warn", () -> this.show(View.WARN, null));
                n3 += n4;
                if (this.has(StaffCapability.MODERATOR_TOOLS)) {
                    this.addCmd(n2, n3, n, "Unmute", "lunmute " + this.playerName);
                    n3 += n4;
                }
                this.add(n2, n3, n, "Kick", () -> this.show(View.KICK, null));
                this.addCmd(n2, n3 += n4, n / 2 - 2, "Jail", "jail " + this.playerName);
                this.addCmd(n2 + n / 2 + 2, n3, n / 2 - 2, "Unjail", "unjail " + this.playerName);
                n3 += n4;
                if (this.has(StaffCapability.TEMP_BAN)) {
                    this.add(n2, n3, n, "Temp Ban", () -> this.show(View.TEMP_BAN_DURATION, null));
                    n3 += n4;
                }
                this.back(n2, n3, n, View.ROOT);
                break;
            }
            case 10: {
                this.reasonButton(n2, n3, n, "Causing Drama", "warn " + this.playerName + " Causing Drama");
                this.reasonButton(n2, n3 += n4, n, "Spamming", "warn " + this.playerName + " Spamming");
                this.reasonButton(n2, n3 += n4, n, "Begging", "warn " + this.playerName + " Begging");
                this.reasonButton(n2, n3 += n4, n, "Mini-Modding", "warn " + this.playerName + " Please refrain from mini-modding players");
                this.reasonButton(n2, n3 += n4, n, "Admin Demands", "warn " + this.playerName + " Continued demands for an admin despite assistance");
                this.reasonButton(n2, n3 += n4, n, "Threatening Players", "warn " + this.playerName + " Threatening other players is against the server rules");
                this.add(n2, n3 += n4, n, "Custom Reason", () -> this.prefill("/warn " + this.playerName + " ", "Warn"));
                this.back(n2, n3 += n4, n, View.MODERATE);
                break;
            }
            case 11: {
                this.addReportableCmd(n2, n3, n, "Inappropriate Name/Skin", "kick " + this.playerName + " Inappropriate name/skin. Please change before re-joining or it will result in a ban!", "Inappropriate Name/Skin", "Kick");
                this.add(n2, n3 += n4, n, "Custom Reason", () -> this.prefill("/kick " + this.playerName + " ", "Kick"));
                this.back(n2, n3 += n4, n, View.MODERATE);
                break;
            }
            case 12: {
                this.add(n2, n3, n, "5 min", () -> this.show(View.TEMP_MUTE_REASON, "5m"));
                this.add(n2, n3 += n4, n, "20 min", () -> this.show(View.TEMP_MUTE_REASON, "20m"));
                this.add(n2, n3 += n4, n, "Custom Duration", () -> this.prefill("/ltempmute " + this.playerName + " ", "Temp Mute"));
                this.back(n2, n3 += n4, n, View.MODERATE);
                break;
            }
            case 13: {
                this.addReportableCmd(n2, n3, n, "Causing Drama", "ltempmute " + this.playerName + " " + this.duration + " Causing Drama", "Causing Drama", "Temp Mute " + this.duration);
                this.addReportableCmd(n2, n3 += n4, n, "Spamming", "ltempmute " + this.playerName + " " + this.duration + " Spamming", "Spamming", "Temp Mute " + this.duration);
                this.addReportableCmd(n2, n3 += n4, n, "Begging", "ltempmute " + this.playerName + " " + this.duration + " Begging", "Begging", "Temp Mute " + this.duration);
                this.add(n2, n3 += n4, n, "Custom Reason", () -> this.prefill("/ltempmute " + this.playerName + " " + this.duration + " ", "Temp Mute"));
                this.back(n2, n3 += n4, n, View.TEMP_MUTE_DURATION);
                break;
            }
            case 14: {
                String[] stringArray = new String[]{"3d", "1w", "2w", "3w", "4w"};
                int n5 = stringArray.length;
                for (int i = 0; i < n5; ++i) {
                    String string;
                    String string2 = string = stringArray[i];
                    this.add(n2, n3, n, string, () -> this.show(View.TEMP_BAN_REASON, string2));
                    n3 += n4;
                }
                this.add(n2, n3, n, "Custom Duration", () -> this.prefill("/tempban " + this.playerName + " ", "Temp Ban"));
                this.back(n2, n3 += n4, n, View.MODERATE);
                break;
            }
            case 15: {
                this.addBanReason(n2, n3, n, "Minor Grief/Stealing", "Minor Grief/Stealing");
                this.addBanReason(n2, n3 += n4, n, "Medium Grief/Stealing", "Medium Grief/Stealing");
                this.addBanReason(n2, n3 += n4, n, "TP-Killing", "TP-Killing");
                this.addBanReason(n2, n3 += n4, n, "Map Art Theft/Cloning", "Stealing/Cloning Map Art");
                this.addBanReason(n2, n3 += n4, n, "Base Raiding", "Base Raiding");
                this.addBanReason(n2, n3 += n4, n, "Hacked Client", "Hacked Client");
                this.addBanReason(n2, n3 += n4, n, "Inappropriate Name/Skin", "Inappropriate name/skin");
                n3 += n4;
                if ("3w".equals(this.duration)) {
                    this.addBanReason(n2, n3, n, "Admitting to Xray", "Admitting to xray. Reduced ban for being honest. Next time, its a permanent ban.");
                    n3 += n4;
                }
                if ("4w".equals(this.duration)) {
                    this.addBanReason(n2, n3, n, "Xraying", "Xraying. Next time, its a permanent ban.");
                    n3 += n4;
                }
                this.add(n2, n3, n, "Custom Reason", () -> this.prefill("/tempban " + this.playerName + " " + this.duration + " ", "Temp Ban"));
                this.back(n2, n3 += n4, n, View.TEMP_BAN_DURATION);
                break;
            }
            case 3: {
                this.add(n2, n3, n, "Player Info", () -> this.show(View.PLAYER_INFO, null));
                n3 += n4;
                if (this.has(StaffCapability.ADVANCED_INVESTIGATION)) {
                    this.add(n2, n3, n, "Inventories", () -> this.show(View.INVENTORIES, null));
                    this.add(n2, n3 += n4, n, "Anti-Cheat", () -> this.show(View.ANTICHEAT, null));
                    this.add(n2, n3 += n4, n, "CoreProtect", () -> this.show(View.COREPROTECT, null));
                    n3 += n4;
                }
                this.back(n2, n3, n, View.ROOT);
                break;
            }
            case 4: {
                this.addCmd(n2, n3, n, "History", "history " + this.playerName);
                this.addCmd(n2, n3 += n4, n, "Playtime", "eplaytime " + this.playerName);
                this.back(n2, n3 += n4, n, View.INVESTIGATE);
                break;
            }
            case 5: {
                this.addCmd(n2, n3, n, "Inventory", "open " + this.playerName);
                this.addCmd(n2, n3 += n4, n, "Ender Chest", "openender " + this.playerName);
                this.addCmd(n2, n3 += n4, n, "Trade Logs", "trade logs " + this.playerName);
                this.back(n2, n3 += n4, n, View.INVESTIGATE);
                break;
            }
            case 6: {
                this.addCmd(n2, n3, n, "Vulcan Profile", "vulcan profile " + this.playerName);
                this.addCmd(n2, n3 += n4, n, "Violations", "vulcan violations " + this.playerName);
                this.addCmd(n2, n3 += n4, n, "CPS", "vulcan cps " + this.playerName);
                this.addCmd(n2, n3 += n4, n, "Knockback Test", "vulcan knockback " + this.playerName);
                this.addCmd(n2, n3 += n4, n, "Freeze", "vulcan freeze " + this.playerName);
                this.back(n2, n3 += n4, n, View.INVESTIGATE);
                break;
            }
            case 7: {
                this.addCmd(n2, n3, n, "All Actions (2d)", "co lookup user:" + this.playerName + " time:2d");
                this.addCmd(n2, n3 += n4, n, "Container (2d)", "co lookup user:" + this.playerName + " time:2d action:container");
                this.addCmd(n2, n3 += n4, n, "Pickup (2d)", "co lookup user:" + this.playerName + " time:2d action:pickup");
                this.add(n2, n3 += n4, n, "Custom Lookup", () -> this.prefill("/co lookup user:" + this.playerName + " ", "CoreProtect Lookup"));
                this.back(n2, n3 += n4, n, View.INVESTIGATE);
                break;
            }
            case 8: {
                this.addCmd(n2, n3, n, "Vanish", "vanish");
                this.addCmd(n2, n3 += n4, n, "Spectator Mode", "gamemode spectator");
                this.addCmd(n2, n3 += n4, n, "Survival Mode", "gamemode survival");
                n3 += n4;
                if (this.has(StaffCapability.SENIOR_TOOLS)) {
                    this.addCmd(n2, n3, n / 2 - 2, "Fly", "fly");
                    this.addCmd(n2 + n / 2 + 2, n3, n / 2 - 2, "God", "god");
                    n3 += n4;
                }
                this.add(n2, n3, n, "Chat Channels", () -> this.show(View.CHAT_CHANNELS, null));
                this.addCmd(n2, n3 += n4, n, "Ignore Claims", "ignoreclaims");
                this.addCmd(n2, n3 += n4, n, "TradeShop Admin", "ts toggleadmin");
                this.addCmd(n2, n3 += n4, n, "Report Lag", "reportlag");
                this.back(n2, n3 += n4, n, View.ROOT);
                break;
            }
            case 9: {
                this.addCmd(n2, n3, n, "SCC Staff Chat", "scc");
                this.add(n2, n3 += n4, n, "VSC Message", () -> this.prefill("/vsc ", "VSC Message"));
                this.addCmd(n2, n3 += n4, n, "Mod Chat", "channel mod");
                this.addCmd(n2, n3 += n4, n, "Global", "g");
                this.addCmd(n2, n3 += n4, n, "Chat On/Off", "togglechat");
                this.back(n2, n3 += n4, n, View.TOOLS);
            }
        }
    }

    private void addTabs(int x, int y, int width) {
        addRenderableWidget(new CommandTab(x, y, width / 2, "Player", mode == Mode.PLAYER, () -> switchMode(Mode.PLAYER)));
        addRenderableWidget(new CommandTab(x + width / 2, y, width / 2, "Staff", mode == Mode.STAFF, () -> switchMode(Mode.STAFF)));
    }

    private void playerActions(int n, int n2, int n3, int n4) {
        this.add(n, n2, n3 / 2 - 2, "Message", () -> this.prefill("/msg " + this.playerName + " ", "Message"));
        this.add(n + n3 / 2 + 2, n2, n3 / 2 - 2, "Mail", () -> this.prefill("/mail send " + this.playerName + " ", "Mail"));
        this.addCmd(n, n2 += n4, n3 / 2 - 2, "TPA", "tpa " + this.playerName);
        this.addCmd(n + n3 / 2 + 2, n2, n3 / 2 - 2, "TPA Here", "tpahere " + this.playerName);
        this.addCmd(n, n2 += n4, n3 / 2 - 2, "Trade", "trade " + this.playerName);
        this.add(n + n3 / 2 + 2, n2, n3 / 2 - 2, "Send Item", () -> this.prefill("/itembox send " + this.playerName, "ItemBox Send"));
        this.addCmd(n, n2 += n4, n3 / 2 - 2, "Ignore / Unignore", "ignoreplayer " + this.playerName);
        this.add(n + n3 / 2 + 2, n2, n3 / 2 - 2, "Give Pet", () -> this.prefill("/GivePet " + this.playerName, "Give Pet"));
        this.addCmd(n, n2 += n4, n3 / 2 - 2, "Meow", "meow " + this.playerName);
        this.addCmd(n + n3 / 2 + 2, n2, n3 / 2 - 2, "Purr", "purr " + this.playerName);
        this.add(n, n2 += n4, n3 / 2 - 2, "Copy Username", this::copyUsername);
        this.add(n + n3 / 2 + 2, n2, n3 / 2 - 2, "Close", this::onClose);
    }

    private void switchMode(Mode mode) {
        if (mode == Mode.STAFF && !this.hasStaffAccess()) {
            return;
        }
        ClientScreens.show(new LegacyStaffActionScreen(this.oldScreen, this.playerName, this.anchorX, this.anchorY, mode, View.ROOT, null));
    }

    private static Mode defaultMode() {
        StaffRole staffRole = StaffRole.parse(CcmcConfig.getString("catcraft.StaffRole"));
        return staffRole == StaffRole.NONE ? Mode.PLAYER : Mode.STAFF;
    }

    private boolean hasStaffAccess() {
        return this.role() != StaffRole.NONE;
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        String string;
        super.extractRenderState(guiGraphicsExtractor, n, n2, f);
        int n3 = 176;
        int n4 = Math.max(4, Math.min(this.anchorX, this.width - n3 - 4));
        int n5 = Math.max(20, Math.min(this.anchorY, this.height - 210));
        int n6 = n4 + n3 / 2;
        guiGraphicsExtractor.centeredText(this.font, (Component)Component.literal((String)this.playerName), n6, Math.max(2, n5 - 20), -1);
        Component component = this.formattedNickname();
        if (component != null && (string = component.getString()) != null && !string.isBlank() && !string.equalsIgnoreCase(this.playerName)) {
            guiGraphicsExtractor.centeredText(this.font, component, n6, Math.max(11, n5 - 10), -1);
        }
    }

    private Component formattedNickname() {
        ClientPacketListener clientPacketListener = Minecraft.getInstance().getConnection();
        if (clientPacketListener == null) {
            return null;
        }
        try {
            Object object;
            Object object2 = null;
            for (Method object3 : clientPacketListener.getClass().getMethods()) {
                if (object3.getParameterCount() != 1 || object3.getParameterTypes()[0] != String.class || !object3.getReturnType().getSimpleName().equals("PlayerInfo") || (object = object3.invoke((Object)clientPacketListener, this.playerName)) == null) continue;
                object2 = object;
                break;
            }
            if (object2 == null) {
                return null;
            }
            for (String string : new String[]{"getTabListDisplayName", "getDisplayName", "displayName"}) {
                try {
                    object = object2.getClass().getMethod(string, new Class[0]);
                    Object object3 = ((Method)object).invoke(object2, new Object[0]);
                    if (!(object3 instanceof Component)) continue;
                    Component component = (Component)object3;
                    return component;
                }
                catch (ReflectiveOperationException reflectiveOperationException) {
                    // empty catch block
                }
            }
        }
        catch (ReflectiveOperationException | RuntimeException exception) {
            // empty catch block
        }
        return null;
    }

    private void addBanReason(int n, int n2, int n3, String string, String string2) {
        this.addReportableCmd(n, n2, n3, string, "tempban " + this.playerName + " " + this.duration + " " + string2, string, "Temp Ban " + this.duration);
    }

    private void reasonButton(int n, int n2, int n3, String string, String string2) {
        this.addReportableCmd(n, n2, n3, string, string2, string, "Warn");
    }

    private void addReportableCmd(int n, int n2, int n3, String string, String string2, String string3, String string4) {
        this.add(n, n2, n3, string, () -> this.sendAndReport(string2, string3, string4));
    }

    private void addCmd(int n, int n2, int n3, String string, String string2) {
        this.add(n, n2, n3, string, () -> this.send(string2));
    }

    private void back(int n, int n2, int n3, View view) {
        this.add(n, n2, n3, "Back", () -> this.show(view, null));
    }

    private void add(int n, int n2, int n3, String string, Runnable runnable) {
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)string), button -> runnable.run()).pos(n, n2).size(n3, 20).build());
    }

    private void addSized(int n, int n2, int n3, int n4, String string, Runnable runnable) {
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)string), button -> runnable.run()).pos(n, n2).size(n3, n4).build());
    }

    private void show(View view, String string) {
        ClientScreens.show(new LegacyStaffActionScreen(this.oldScreen, this.playerName, this.anchorX, this.anchorY, Mode.STAFF, view, string));
    }

    private StaffRole role() {
        return StaffRole.parse(CcmcConfig.getString("catcraft.StaffRole"));
    }

    private boolean has(StaffCapability staffCapability) {
        return this.role().has(staffCapability);
    }

    private void prefill(String string, String string2) {
        if (!ChatInputPrefill.prefill((Object)this, string)) {
            LegacyStaffActionScreen.local("[CCC] couldn't prepare " + string2 + " in chat; no command was sent.");
        }
    }

    private void sendAndReport(String string, String string2, String string3) {
        if (!this.send(string)) {
            return;
        }
        ClientScreens.show(this.oldScreen);
        DiscordReportService.queue(this.playerName, string2, string3);
    }

    private boolean send(String string) {
        ClientPacketListener clientPacketListener = Minecraft.getInstance().getConnection();
        if (clientPacketListener == null) {
            LegacyStaffActionScreen.local("[CCC] action unavailable: not connected to a server.");
            return false;
        }
        clientPacketListener.sendCommand(string);
        LegacyStaffActionScreen.local("[CCC] ran /" + string);
        return true;
    }

    private void copyUsername() {
        Minecraft.getInstance().keyboardHandler.setClipboard(this.playerName);
    }

    private static void local(String string) {
        ClientFeedback.send((Component)CcmcText.literal(string));
    }

    public void onClose() {
        ClientScreens.show(this.oldScreen);
    }

    private static enum Mode {
        STAFF,
        PLAYER;

    }

    private static enum View {
        ROOT,
        TELEPORT,
        MODERATE,
        INVESTIGATE,
        PLAYER_INFO,
        INVENTORIES,
        ANTICHEAT,
        COREPROTECT,
        TOOLS,
        CHAT_CHANNELS,
        WARN,
        KICK,
        TEMP_MUTE_DURATION,
        TEMP_MUTE_REASON,
        TEMP_BAN_DURATION,
        TEMP_BAN_REASON;

    }
}
