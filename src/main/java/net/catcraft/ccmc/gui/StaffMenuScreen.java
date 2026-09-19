package net.catcraft.ccmc.gui;

import net.catcraft.ccmc.client.CcmcText;
import net.catcraft.ccmc.client.ClientFeedback;
import net.catcraft.ccmc.client.ClientScreens;
import net.catcraft.ccmc.config.CcmcConfig;
import net.catcraft.ccmc.config.CcmcSettingsScreen;
import net.catcraft.ccmc.config.StaffCapability;
import net.catcraft.ccmc.config.StaffRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class StaffMenuScreen extends Screen {
    private enum View { ROOT, TOOLS, CHAT }
    private final Screen oldScreen;
    private final View view;

    public StaffMenuScreen(Screen oldScreen) { this(oldScreen, View.ROOT); }
    private StaffMenuScreen(Screen oldScreen, View view) {
        super(Component.literal("CatCraft Companion"));
        this.oldScreen = oldScreen;
        this.view = view;
    }

    public static void open() { ClientScreens.show(new StaffMenuScreen(ClientScreens.current())); }

    @Override protected void init() {
        super.init();
        int w = 180;
        int x = width / 2 - w / 2;
        int y = Math.max(24, height / 2 - 90);

        if (view == View.ROOT) {
            if (has(StaffCapability.MODERATOR_TOOLS)) {
                add(x, y, w, "Server Tools", () -> show(View.TOOLS)); y += 24;
            }
            if (has(StaffCapability.STAFF_CHAT)) {
                add(x, y, w, "Staff Chat", () -> show(View.CHAT)); y += 24;
            }
            add(x, y, w, "Settings & Profile", () -> ClientScreens.show(CcmcSettingsScreen.create(this))); y += 24;
            add(x, y, w, "Close", this::onClose);
            return;
        }

        if (view == View.TOOLS) {
            add(x, y, w, "Vanish", () -> send("vanish")); y += 22;
            add(x, y, w, "Spectator Mode", () -> send("gamemode spectator")); y += 22;
            add(x, y, w, "Survival Mode", () -> send("gamemode survival")); y += 22;
            add(x, y, w, "Ignore Claims", () -> send("ignoreclaims")); y += 22;
            add(x, y, w, "TradeShop Admin", () -> send("ts toggleadmin")); y += 22;
            add(x, y, w, "TPS", () -> send("tps")); y += 22;
            add(x, y, w, "Report Lag", () -> send("reportlag")); y += 22;
            if (has(StaffCapability.SENIOR_TOOLS)) {
                add(x, y, w / 2 - 2, "Fly", () -> send("fly"));
                add(x + w / 2 + 2, y, w / 2 - 2, "God", () -> send("god")); y += 22;
            }
            add(x, y, w, "Back", () -> show(View.ROOT));
            return;
        }

        add(x, y, w, "SCC Staff Chat", () -> send("scc")); y += 22;
        add(x, y, w, "VSC Message", () -> prefill("/vsc ", "VSC Message")); y += 22;
        if (has(StaffCapability.MODERATOR_TOOLS)) {
            add(x, y, w, "Mod Chat", () -> send("channel mod")); y += 22;
        }
        add(x, y, w, "Global", () -> send("g")); y += 22;
        add(x, y, w, "Chat On/Off", () -> send("togglechat")); y += 22;
        add(x, y, w, "Back", () -> show(View.ROOT));
    }

    private void add(int x, int y, int width, String label, Runnable action) {
        addRenderableWidget(Button.builder(Component.literal(label), b -> action.run()).pos(x, y).size(width, 20).build());
    }

    private void show(View next) { ClientScreens.show(new StaffMenuScreen(oldScreen, next)); }
    private StaffRole role() { return StaffRole.parse(CcmcConfig.getString("catcraft.StaffRole")); }
    private boolean has(StaffCapability capability) { return role().has(capability); }

    private void prefill(String value, String label) {
        if (!ChatInputPrefill.prefill(this, value)) local("[CCMC] couldn't prepare " + label + " in chat; no command was sent.");
    }

    private void send(String command) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            local("[CCMC] action unavailable: not connected to a server.");
            return;
        }
        connection.sendCommand(command);
    }

    private static void local(String message) { ClientFeedback.send(CcmcText.literal(message)); }
    @Override public void onClose() { ClientScreens.show(oldScreen); }
}
