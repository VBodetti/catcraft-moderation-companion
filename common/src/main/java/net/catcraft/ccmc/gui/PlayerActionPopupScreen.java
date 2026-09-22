package net.catcraft.ccmc.gui;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.catcraft.ccmc.client.CcmcText;
import net.catcraft.ccmc.client.ClientFeedback;
import net.catcraft.ccmc.client.ClientScreens;
import net.catcraft.ccmc.config.CcmcConfig;
import net.catcraft.ccmc.config.StaffCapability;
import net.catcraft.ccmc.config.StaffRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerSkinWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

/** Full player profile opened from a chat-name click. */
public final class PlayerActionPopupScreen extends Screen {
    private static final int PANEL_WIDTH = 700;
    private static final Pattern PARENTHETICAL_AGO = Pattern.compile("^(.*?)\\s*\\(([^()]*(?:ago|earlier))\\)\\s*\\.?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SEPARATED_AGO = Pattern.compile("^(.*?)(?:\\s+[-•|]\\s+|,\\s+)(.+?\\bago)\\s*\\.?$", Pattern.CASE_INSENSITIVE);
    private final Screen oldScreen;
    private final String playerName;
    private final int anchorX;
    private final int anchorY;
    private final Tab tab;

    public PlayerActionPopupScreen(Screen oldScreen, String playerName, int anchorX, int anchorY) {
        this(oldScreen, playerName, anchorX, anchorY, Tab.OVERVIEW);
    }

    private PlayerActionPopupScreen(Screen oldScreen, String playerName, int anchorX, int anchorY, Tab tab) {
        super(Component.literal("CatCraft Player Profile"));
        this.oldScreen = oldScreen;
        this.playerName = playerName;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.tab = tab;
    }

    @Override
    protected void init() {
        super.init();
        int panelWidth = Math.min(PANEL_WIDTH, this.width - 24);
        int left = (this.width - panelWidth) / 2;
        int top = Math.max(8, this.height / 2 - 216);
        int contentTop = top + 150;
        addPlayerModel(left + 8, top + 20);
        addTabs(left, top + 122, panelWidth);
        switch (this.tab) {
            case OVERVIEW -> overview(left, contentTop, panelWidth);
            case ACTIONS -> actions(left, contentTop, panelWidth);
            case HISTORY -> history(left, contentTop, panelWidth);
            case COREPROTECT -> coreProtect(left, contentTop, panelWidth);
            case ANTICHEAT -> antiCheat(left, contentTop, panelWidth);
            case TRADE_LOGS -> tradeLogs(left, contentTop, panelWidth);
            case MODERATE -> moderate(left, contentTop, panelWidth);
        }
        addSized(left + panelWidth - 58, top, 58, 18, "Close", this::onClose);
        startAutomaticQuery();
    }

    private void addPlayerModel(int x, int y) {
        PlayerInfo info = playerInfo();
        if (info == null) return;
        PlayerSkinWidget widget = new PlayerSkinWidget(72, 96, Minecraft.getInstance().getEntityModels(), info::getSkin);
        widget.setX(x);
        widget.setY(y);
        this.addRenderableWidget(widget);
    }

    private void addTabs(int left, int y, int width) {
        Tab[] visible = visibleTabs();
        int[] tabWidths = new int[visible.length];
        int naturalWidth = 0;
        for (int i = 0; i < visible.length; i++) {
            tabWidths[i] = this.font.width(visible[i].label) + 18;
            naturalWidth += tabWidths[i];
        }
        int extra = width - naturalWidth;
        for (int i = 0; i < visible.length; i++) {
            int share = extra / (visible.length - i);
            tabWidths[i] += share;
            extra -= share;
        }
        int x = left;
        for (int i = 0; i < visible.length; i++) {
            Tab candidate = visible[i];
            Button tabButton = Button.builder(Component.literal(candidate.label), button -> switchTab(candidate))
                    .pos(x, y).size(tabWidths[i], 20).build();
            tabButton.active = candidate != this.tab;
            this.addRenderableWidget(tabButton);
            x += tabWidths[i];
        }
    }

    private Tab[] visibleTabs() {
        if (!hasStaffAccess()) return new Tab[]{Tab.OVERVIEW, Tab.ACTIONS};
        if (!has(StaffCapability.ADVANCED_INVESTIGATION)) {
            return new Tab[]{Tab.OVERVIEW, Tab.ACTIONS, Tab.HISTORY, Tab.MODERATE};
        }
        return new Tab[]{Tab.OVERVIEW, Tab.ACTIONS, Tab.HISTORY, Tab.COREPROTECT,
                Tab.ANTICHEAT, Tab.TRADE_LOGS, Tab.MODERATE};
    }

    private void overview(int left, int top, int width) {
        int third = (width - 12) / 3;
        add(left, top + 56, third, "Refresh Profile", () -> PlayerProfileQueryCapture.beginOverview(this.playerName, true));
        add(left + third + 6, top + 56, third, "Player Actions", () -> switchTab(Tab.ACTIONS));
        if (hasStaffAccess()) add(left + (third + 6) * 2, top + 56, third, "Open History", () -> switchTab(Tab.HISTORY));
    }

    private void actions(int left, int top, int width) {
        int half = (width - 6) / 2;
        row(left, top, half, "Message", () -> prefill("/msg " + this.playerName + " ", "Message"), "Mail", () -> prefill("/mail send " + this.playerName + " ", "Mail"));
        row(left, top + 24, half, "TPA", () -> send("tpa " + this.playerName), "TPA Here", () -> send("tpahere " + this.playerName));
        row(left, top + 48, half, "Trade", () -> send("trade " + this.playerName), "Ignore / Unignore", () -> prefill("/ignoreplayer " + this.playerName, "Ignore Player"));
        row(left, top + 72, half, "Send Held Item", () -> prefill("/itembox send " + this.playerName, "ItemBox Send"), "Give Tamed Pet", () -> prefill("/GivePet " + this.playerName, "Give Pet"));
        row(left, top + 96, half, "Meow", () -> send("meow " + this.playerName), "Purr", () -> send("purr " + this.playerName));
        row(left, top + 120, half, "Copy Username", this::copyUsername, "Back to Overview", () -> switchTab(Tab.OVERVIEW));
    }

    private void history(int left, int top, int width) {
        int half = (width - 6) / 2;
        row(left, top + 142, half, "Refresh History", () -> PlayerProfileQueryCapture.beginSingle(this.playerName, "history", "history " + this.playerName, true), "Check Playtime", () -> PlayerProfileQueryCapture.beginSingle(this.playerName, "playtime", "eplaytime " + this.playerName, true));
    }

    private void coreProtect(int left, int top, int width) {
        int half = (width - 6) / 2;
        row(left, top + 142, half, "All Actions (2d)", () -> query("coreprotect", "co lookup user:" + this.playerName + " time:2d"), "Containers (2d)", () -> query("coreprotect", "co lookup user:" + this.playerName + " time:2d action:container"));
        row(left, top + 166, half, "Pickups (2d)", () -> query("coreprotect", "co lookup user:" + this.playerName + " time:2d action:pickup"), "Custom Filter...", () -> prefill("/co lookup user:" + this.playerName + " ", "CoreProtect Lookup"));
    }

    private void antiCheat(int left, int top, int width) {
        int half = (width - 6) / 2;
        row(left, top + 142, half, "Refresh Profile", () -> query("anticheat", "vulcan profile " + this.playerName), "Violations", () -> query("anticheat", "vulcan violations " + this.playerName));
        row(left, top + 166, half, "CPS", () -> query("anticheat", "vulcan cps " + this.playerName), "Knockback Test", () -> send("vulcan knockback " + this.playerName));
    }

    private void tradeLogs(int left, int top, int width) {
        int half = (width - 6) / 2;
        row(left, top + 142, half, "Refresh Trade Logs", () -> query("trade", "trade logs " + this.playerName), "Open Inventory", () -> send("open " + this.playerName));
        row(left, top + 166, half, "Open Ender Chest", () -> send("openender " + this.playerName), "Copy Username", this::copyUsername);
    }

    private void moderate(int left, int top, int width) {
        int half = (width - 6) / 2;
        add(left, top, width, "Full Moderation & Investigation", () ->
                ClientScreens.show(new LegacyStaffActionScreen(this, this.playerName, this.anchorX, this.anchorY)));
        row(left, top + 24, half, "Punish", () -> send("punish " + this.playerName), "Warn...", () -> prefill("/warn " + this.playerName + " ", "Warn"));
        row(left, top + 48, half, "Temp Mute...", () -> prefill("/ltempmute " + this.playerName + " ", "Temp Mute"), "Kick...", () -> prefill("/kick " + this.playerName + " ", "Kick"));
        row(left, top + 72, half, "Jail", () -> send("togglejail " + this.playerName + " 1"), "Unjail", () -> send("unjail " + this.playerName));
        row(left, top + 96, half, "Unmute", () -> send("lunmute " + this.playerName), "Player Info", () -> query("history", "eplaytime " + this.playerName));
        if (has(StaffCapability.TEMP_BAN)) add(left, top + 120, width, "Temp Ban...", () -> prefill("/tempban " + this.playerName + " ", "Temp Ban"));
    }

    private void startAutomaticQuery() {
        switch (this.tab) {
            case OVERVIEW -> PlayerProfileQueryCapture.beginOverview(this.playerName, false);
            case HISTORY -> PlayerProfileQueryCapture.beginSingle(this.playerName, "history", "history " + this.playerName, false);
            case COREPROTECT -> PlayerProfileQueryCapture.beginSingle(this.playerName, "coreprotect", "co lookup user:" + this.playerName + " time:2d", false);
            case ANTICHEAT -> PlayerProfileQueryCapture.beginSingle(this.playerName, "anticheat", "vulcan profile " + this.playerName, false);
            case TRADE_LOGS -> PlayerProfileQueryCapture.beginSingle(this.playerName, "trade", "trade logs " + this.playerName, false);
            default -> { }
        }
    }

    private void query(String key, String command) { PlayerProfileQueryCapture.beginSingle(this.playerName, key, command, true); }

    private void row(int left, int y, int half, String first, Runnable firstAction, String second, Runnable secondAction) {
        add(left, y, half, first, firstAction);
        add(left + half + 6, y, half, second, secondAction);
    }

    private void switchTab(Tab next) {
        if (next.staffOnly && !hasStaffAccess()) return;
        ClientScreens.show(new PlayerActionPopupScreen(this.oldScreen, this.playerName, this.anchorX, this.anchorY, next));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        PlayerProfileQueryCapture.pump();
        int panelWidth = Math.min(PANEL_WIDTH, this.width - 24);
        int left = (this.width - panelWidth) / 2;
        int top = Math.max(8, this.height / 2 - 216);
        int center = left + panelWidth / 2;
        graphics.centeredText(this.font, Component.literal("CatCraft Player Profile"), center, top, -1);
        PlayerInfo info = playerInfo();
        if (info == null) graphics.centeredText(this.font, Component.literal("?"), left + 44, top + 62, -1);
        graphics.centeredText(this.font, Component.literal(this.playerName), left + 180, top + 32, -1);
        Component nickname = formattedNickname();
        String nickText = nickname == null ? "" : nickname.getString();
        if (!nickText.isBlank() && !nickText.equalsIgnoreCase(this.playerName)) graphics.centeredText(this.font, nickname, left + 180, top + 48, -1);
        boolean online = isOnline();
        graphics.centeredText(this.font, Component.literal(online ? "Online now" : "Offline / not in tab list"), left + 180, top + 66, online ? 0xFF55FF55 : 0xFFAAAAAA);
        int profileLeft = left + 280;
        int profileWidth = panelWidth - 288;
        int joinedWidth = Math.min(180, profileWidth);
        String memberSince = PlayerProfileQueryCapture.value("member_since");
        renderDataCard(graphics, profileLeft + (profileWidth - joinedWidth) / 2, top + 30, joinedWidth, 38,
                "Member Since", memberSince == null ? "Not provided" : memberSince);
        int contentTop = top + 150;
        if (this.tab == Tab.OVERVIEW) renderOverviewCards(graphics, left, contentTop, panelWidth);
        else if (this.tab.capturedKey != null) renderResponsePanel(graphics, left, contentTop, panelWidth, this.tab.label, PlayerProfileQueryCapture.lines(this.tab.capturedKey));
    }

    private void renderOverviewCards(GuiGraphicsExtractor graphics, int left, int top, int width) {
        int cardWidth = (width - 8) / 3;
        renderDataCard(graphics, left, top, cardWidth, 44, "Last Seen", overviewSeen());
        renderDataCard(graphics, left + cardWidth + 4, top, cardWidth, 44, "Playtime", overviewPlaytime());
        renderDataCard(graphics, left + (cardWidth + 4) * 2, top, cardWidth, 44, "Known As", overviewAlias());
    }

    private void renderDataCard(GuiGraphicsExtractor graphics, int x, int y, int width, int height, String label, String value) {
        graphics.fill(x, y, x + width, y + height, 0x88000000);
        graphics.outline(x, y, width, height, 0xFF555555);
        graphics.centeredText(this.font, Component.literal(label), x + width / 2, y + 5, 0xFFAAAAAA);
        String[] lines = value == null ? new String[]{"—"} : value.split("\\n", 2);
        if (lines.length == 1) {
            graphics.centeredText(this.font, Component.literal(fit(lines[0], width - 10)), x + width / 2, y + 21, 0xFFFFFFFF);
        } else {
            graphics.centeredText(this.font, Component.literal(fit(lines[0], width - 10)), x + width / 2, y + 18, 0xFFFFFFFF);
            graphics.centeredText(this.font, Component.literal(fit(lines[1], width - 10)), x + width / 2, y + 30, 0xFFBBBBBB);
        }
    }

    private void renderResponsePanel(GuiGraphicsExtractor graphics, int left, int top, int width, String label, List<String> lines) {
        graphics.fill(left, top, left + width, top + 136, 0x88000000);
        graphics.outline(left, top, width, 136, 0xFF555555);
        graphics.text(this.font, Component.literal(label), left + 8, top + 6, 0xFFAAAAAA);
        if (lines.isEmpty()) {
            graphics.text(this.font, Component.literal(fit(PlayerProfileQueryCapture.status(), width - 16)), left + 8, top + 24, 0xFFAAAAAA);
            return;
        }
        int y = top + 24;
        for (String line : lines) {
            graphics.text(this.font, Component.literal(fit(line, width - 16)), left + 8, y, 0xFFDDDDDD);
            y += 12;
            if (y > top + 120) break;
        }
    }

    private String overviewSeen() {
        if (isOnline()) return "Online Now";
        for (String line : PlayerProfileQueryCapture.lines("seen")) {
            String lower = line.toLowerCase();
            if (lower.contains("known as")) continue;
            if (lower.contains("currently online")) return "Online Now";
            String summary = summarizeLastSeen(line, lower);
            if (summary != null) return summary;
        }
        return PlayerProfileQueryCapture.status();
    }

    private static String summarizeLastSeen(String line, String lower) {
        String[] markers = {
                "has been offline since", "was last seen", "last seen", "offline since",
                "has been offline for", "offline for", "has been online since"
        };
        for (String marker : markers) {
            int index = lower.indexOf(marker);
            if (index < 0) continue;
            String detail = line.substring(index + marker.length()).trim()
                    .replaceFirst("^[\\s:,-]+", "")
                    .replaceFirst("(?i)^on\\s+", "")
                    .replaceFirst("\\s*\\.$", "");
            if (detail.isBlank()) return "Offline";
            Matcher parenthetical = PARENTHETICAL_AGO.matcher(detail);
            if (parenthetical.matches() && !parenthetical.group(1).isBlank()) {
                return parenthetical.group(1).trim() + "\n" + parenthetical.group(2).trim();
            }
            Matcher separated = SEPARATED_AGO.matcher(detail);
            if (separated.matches() && !separated.group(1).isBlank()) {
                return separated.group(1).trim() + "\n" + separated.group(2).trim();
            }
            return detail;
        }
        return null;
    }

    private String overviewPlaytime() {
        String hoverPlaytime = PlayerProfileQueryCapture.value("hover_playtime");
        if (hoverPlaytime != null) return hoverPlaytime;
        for (String line : PlayerProfileQueryCapture.lines("playtime")) {
            int colon = line.indexOf(':');
            return colon >= 0 ? line.substring(colon + 1).trim() : line;
        }
        return PlayerProfileQueryCapture.status();
    }

    private String overviewAlias() {
        for (String line : PlayerProfileQueryCapture.lines("seen")) {
            if (!line.toLowerCase().contains("known as")) continue;
            int colon = line.indexOf(':');
            return colon >= 0 ? line.substring(colon + 1).trim() : line;
        }
        return "None reported";
    }

    private String fit(String value, int maxWidth) {
        if (value == null || value.isBlank()) return "—";
        if (this.font.width(value) <= maxWidth) return value;
        String suffix = "...";
        int end = value.length();
        while (end > 1 && this.font.width(value.substring(0, end) + suffix) > maxWidth) end--;
        return value.substring(0, end) + suffix;
    }

    private PlayerInfo playerInfo() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) return null;
        return connection.getPlayerInfoIgnoreCase(this.playerName);
    }

    private Component formattedNickname() {
        PlayerInfo info = playerInfo();
        if (info == null) return null;
        return info.getTabListDisplayName();
    }

    private boolean isOnline() { return playerInfo() != null; }
    private StaffRole role() { return StaffRole.parse(CcmcConfig.getString("catcraft.StaffRole")); }
    private boolean hasStaffAccess() { return role() != StaffRole.NONE; }
    private boolean has(StaffCapability capability) { return role().has(capability); }

    private void prefill(String text, String label) {
        if (!ChatInputPrefill.prefill(this, text)) local("[CCC] couldn't prepare " + label + " in chat; no command was sent.");
    }

    private boolean send(String command) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            local("[CCC] action unavailable: not connected to a server.");
            return false;
        }
        connection.sendCommand(command);
        return true;
    }

    private void copyUsername() { Minecraft.getInstance().keyboardHandler.setClipboard(this.playerName); }
    private void add(int x, int y, int width, String label, Runnable action) { addSized(x, y, width, 20, label, action); }
    private void addSized(int x, int y, int width, int height, String label, Runnable action) {
        this.addRenderableWidget(Button.builder(Component.literal(label), button -> action.run()).pos(x, y).size(width, height).build());
    }
    private static void local(String text) { ClientFeedback.send(CcmcText.literal(text)); }

    @Override
    public void onClose() { ClientScreens.show(this.oldScreen); }

    private enum Tab {
        OVERVIEW("Overview", false, null), ACTIONS("Actions", false, null), HISTORY("History", true, "history"),
        COREPROTECT("CoreProtect", true, "coreprotect"), ANTICHEAT("Anti-Cheat", true, "anticheat"),
        TRADE_LOGS("Trade Logs", true, "trade"), MODERATE("Staff", true, null);
        final String label;
        final boolean staffOnly;
        final String capturedKey;
        Tab(String label, boolean staffOnly, String capturedKey) { this.label = label; this.staffOnly = staffOnly; this.capturedKey = capturedKey; }
    }
}
