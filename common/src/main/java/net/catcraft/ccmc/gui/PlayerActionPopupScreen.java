package net.catcraft.ccmc.gui;

import java.util.ArrayList;
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
import net.minecraft.util.FormattedCharSequence;

/** Shared player identity with player actions or a staff action/results workspace. */
public final class PlayerActionPopupScreen extends Screen {
    private static final int PANEL_WIDTH = 700;
    private static final Pattern PARENTHETICAL_AGO = Pattern.compile("^(.*?)\\s*\\(([^()]*(?:ago|earlier))\\)\\s*\\.?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SEPARATED_AGO = Pattern.compile("^(.*?)(?:\\s+[-•|]\\s+|,\\s+)(.+?\\bago)\\s*\\.?$", Pattern.CASE_INSENSITIVE);
    private final Screen oldScreen;
    private final String playerName;
    private Workspace workspace = Workspace.PLAYER;
    private QueryView selectedQuery;
    private int resultPage;
    private Button previousResults;
    private Button nextResults;
    private boolean profileLoaded;

    public PlayerActionPopupScreen(Screen oldScreen, String playerName, int anchorX, int anchorY) {
        super(Component.literal("CatCraft Player Profile"));
        this.oldScreen = oldScreen;
        this.playerName = playerName;
    }

    private int panelWidth() { return Math.min(PANEL_WIDTH, this.width - 24); }
    private int panelLeft() { return (this.width - panelWidth()) / 2; }
    private int panelTop() { return Math.max(8, (this.height - 320) / 2); }
    private boolean wideHeader() { return panelWidth() >= 520; }
    private int headerHeight() { return wideHeader() ? 100 : 112; }
    private int contentTop() { return panelTop() + headerHeight() + 20; }
    private boolean staffWorkspace() { return workspace == Workspace.STAFF && hasStaffAccess(); }
    private int actionStep() {
        int available = this.height - contentTop() - 8;
        return Math.max(14, Math.min(30, available / 6));
    }
    private int actionsWidth() {
        return staffWorkspace() ? (panelWidth() - 10) * 55 / 100 : panelWidth();
    }
    private int resultsLeft() { return panelLeft() + actionsWidth() + 10; }
    private int resultsTop() { return contentTop(); }
    private int resultsWidth() { return panelWidth() - actionsWidth() - 10; }
    private int resultsHeight() { return Math.max(92, this.height - resultsTop() - 8); }
    private int resultLinesPerPage() { return Math.max(1, (resultsHeight() - 72) / 11); }

    @Override
    protected void init() {
        super.init();
        int left = panelLeft(), top = panelTop(), width = panelWidth();
        PlayerInfo info = playerInfo();
        if (info != null) {
            PlayerSkinWidget model = new PlayerSkinWidget(64, 84, Minecraft.getInstance().getEntityModels(), info::getSkin);
            model.setX(left + 4); model.setY(top + 22);
            addRenderableWidget(model);
        }
        int half = hasStaffAccess() ? width / 2 : width;
        addRenderableWidget(new CommandTab(left, top + headerHeight(), half, "Player Commands", workspace == Workspace.PLAYER,
                () -> switchWorkspace(Workspace.PLAYER)));
        if (hasStaffAccess()) addRenderableWidget(new CommandTab(left + half, top + headerHeight(), width - half,
                "Staff Commands", workspace == Workspace.STAFF, () -> switchWorkspace(Workspace.STAFF)));
        addRenderableWidget(new ProfileRefreshButton(left + width - 82, top, "Refresh profile",
                () -> PlayerProfileQueryCapture.beginOverview(playerName, true)));
        addSized(left + width - 58, top, 58, 18, "Close", this::onClose);
        if (staffWorkspace()) {
            staffActions(left, contentTop(), actionsWidth());
            addResultControls();
        } else playerActions(left, contentTop(), width);
        if (!profileLoaded) {
            profileLoaded = true;
            PlayerProfileQueryCapture.beginOverview(playerName, false);
        }
    }

    private void switchWorkspace(Workspace next) {
        if (next == workspace || (next == Workspace.STAFF && !hasStaffAccess())) return;
        workspace = next;
        rebuildWidgets();
    }

    private void playerActions(int left, int top, int width) {
        int half = (width - 6) / 2, step = actionStep();
        row(left, top, half, "Message", () -> prefill("/msg " + playerName + " ", "Message"), "Mail", () -> prefill("/mail send " + playerName + " ", "Mail"));
        row(left, top + step, half, "TPA", () -> send("tpa " + playerName), "TPA Here", () -> send("tpahere " + playerName));
        row(left, top + step * 2, half, "Trade", () -> send("trade " + playerName), "Itembox Held Item", () -> prefill("/itembox send " + playerName, "ItemBox Send"));
        row(left, top + step * 3, half, "Ignore / Unignore", () -> prefill("/ignoreplayer " + playerName, "Ignore Player"), "Give Tamed Pet", () -> prefill("/GivePet " + playerName, "Give Pet"));
        row(left, top + step * 4, half, "Meow", () -> send("meow " + playerName), "Copy Username", this::copyUsername);
        add(left, top + step * 5, width, "Full Command List", this::fullCommands);
    }

    private void staffActions(int left, int top, int width) {
        int half = (width - 6) / 2, right = left + half + 6, step = actionStep();
        staffButton(left, top, half, "TPO to Player", StaffCapability.PLAYER_TELEPORT, () -> send("tpo " + playerName));
        staffButton(right, top, half, "TPO Player Here", StaffCapability.PLAYER_TELEPORT, () -> send("tphere " + playerName));
        staffButton(left, top + step, half, "Inventory", StaffCapability.BASIC_MODERATION, () -> send("open " + playerName));
        staffButton(right, top + step, half, "Ender Chest", StaffCapability.BASIC_MODERATION, () -> send("openender " + playerName));
        staffButton(left, top + step * 2, half, "History", StaffCapability.BASIC_MODERATION, () -> runQuery(QueryView.HISTORY));
        staffButton(right, top + step * 2, half, "20 Min Mute", StaffCapability.BASIC_MODERATION, () -> LegacyStaffActionScreen.openQuickMute(this, playerName));
        staffButton(left, top + step * 3, half, "Unmute", StaffCapability.MODERATOR_TOOLS, () -> send("lunmute " + playerName));
        staffButton(right, top + step * 3, half, "Jail", StaffCapability.BASIC_MODERATION, () -> send("jail " + playerName));
        staffButton(left, top + step * 4, half, "Unjail", StaffCapability.BASIC_MODERATION, () -> send("unjail " + playerName));
        staffButton(right, top + step * 4, half, "Warn", StaffCapability.BASIC_MODERATION, () -> LegacyStaffActionScreen.openQuickWarn(this, playerName));
        staffButton(left, top + step * 5, half, "Punish", StaffCapability.BASIC_MODERATION, () -> send("punish " + playerName));
        add(right, top + step * 5, half, "Full Command List", this::fullCommands);
    }

    private void staffButton(int x, int y, int width, String label, StaffCapability capability, Runnable action) {
        Button button = Button.builder(Component.literal(label), ignored -> action.run()).pos(x, y).size(width, actionStep() - 3).build();
        button.active = has(capability);
        addRenderableWidget(button);
    }

    private void addResultControls() {
        int x = resultsLeft(), y = resultsTop(), width = resultsWidth(), half = (width - 16) / 2;
        int i = 0;
        for (QueryView query : QueryView.values()) {
            Button button = Button.builder(Component.literal(query.label), ignored -> runQuery(query))
                    .pos(x + 6 + (i % 2) * (half + 4), y + 18 + (i / 2) * 18).size(half, 16).build();
            button.active = has(query.capability);
            addRenderableWidget(button);
            i++;
        }
        addRenderableWidget(new ProfileRefreshButton(x + width - 23, y - 2, "Refresh current query",
                () -> { if (selectedQuery != null) runQuery(selectedQuery); }));
        previousResults = Button.builder(Component.literal("<"), ignored -> resultPage = Math.max(0, resultPage - 1))
                .pos(x + 6, y + resultsHeight() - 18).size(22, 16).build();
        nextResults = Button.builder(Component.literal(">"), ignored -> resultPage++)
                .pos(x + width - 28, y + resultsHeight() - 18).size(22, 16).build();
        previousResults.active = nextResults.active = false;
        addRenderableWidget(previousResults); addRenderableWidget(nextResults);
    }

    private void runQuery(QueryView query) {
        if (!has(query.capability)) return;
        selectedQuery = query; resultPage = 0;
        PlayerProfileQueryCapture.beginSingle(playerName, query.key, query.command(playerName), true);
    }

    private void fullCommands() { ClientScreens.show(new StaffMenuScreen(this, staffWorkspace())); }
    private void row(int left, int y, int half, String a, Runnable aa, String b, Runnable ba) {
        add(left, y, half, a, aa); add(left + half + 6, y, half, b, ba);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // Draw the panel behind its buttons, then the screen/widgets, then profile and live text.
        if (staffWorkspace()) graphics.fill(resultsLeft(), resultsTop(), resultsLeft() + resultsWidth(), resultsTop() + resultsHeight(), 0x88000000);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        PlayerProfileQueryCapture.pump();
        int left = panelLeft(), top = panelTop(), width = panelWidth(), textX = left + 78;
        graphics.text(font, Component.literal("CatCraft Player Profile"), left, top + 4, 0xFFFFFFFF);
        int identityWidth = wideHeader() ? width * 56 / 100 - 84 : width - 84;
        profileText(graphics, playerName, textX, top + 24, identityWidth, 0xFFFFFFFF, mouseX, mouseY);
        Component nickname = formattedNickname();
        if (nickname != null && !nickname.getString().equalsIgnoreCase(playerName)) {
            List<FormattedCharSequence> nickLines = font.split(nickname, identityWidth);
            if (!nickLines.isEmpty()) graphics.text(font, nickLines.getFirst(), textX, top + 36, 0xFFFFFFFF);
            if (nickLines.size() > 1 && mouseX >= textX && mouseX < textX + identityWidth && mouseY >= top + 36 && mouseY < top + 47)
                graphics.setTooltipForNextFrame(nickname, mouseX, mouseY);
        }
        String seen = overviewSeen();
        boolean online = isOnline() || "Online Now".equals(seen);
        profileText(graphics, online ? "Online Now" : "Last Seen: " + seen.replace('\n', ' '), textX, top + 48, identityWidth,
                online ? 0xFF55FF55 : 0xFFCCCCCC, mouseX, mouseY);
        String joined = PlayerProfileQueryCapture.value("member_since");
        profileText(graphics, "Member Since: " + (joined == null ? "Not provided" : joined), textX, top + 60, identityWidth, 0xFFCCCCCC, mouseX, mouseY);
        profileText(graphics, "Playtime: " + overviewPlaytime(), textX, top + 72, identityWidth, 0xFFCCCCCC, mouseX, mouseY);
        int aliasX = wideHeader() ? left + width * 56 / 100 : textX;
        int aliasY = wideHeader() ? top + 24 : top + 86;
        int aliasWidth = left + width - aliasX - 6;
        if (wideHeader()) {
            graphics.fill(aliasX, aliasY, left + width, top + 92, 0x44222222);
            graphics.text(font, Component.literal("Known As"), aliasX + 8, aliasY + 6, 0xFFAAAAAA);
            wrappedText(graphics, overviewAlias(), aliasX + 8, aliasY + 22, aliasWidth - 10, 4, mouseX, mouseY);
        } else wrappedText(graphics, "Known As: " + overviewAlias(), aliasX, aliasY, aliasWidth, 2, mouseX, mouseY);
        if (staffWorkspace()) renderResults(graphics);
    }

    private void profileText(GuiGraphicsExtractor graphics, String text, int x, int y, int width, int color, int mx, int my) {
        graphics.text(font, Component.literal(fit(text, width)), x, y, color);
        if (font.width(text) > width && mx >= x && mx < x + width && my >= y && my < y + 11)
            graphics.setTooltipForNextFrame(Component.literal(text), mx, my);
    }

    private void wrappedText(GuiGraphicsExtractor graphics, String text, int x, int y, int width, int maxLines, int mx, int my) {
        List<FormattedCharSequence> lines = font.split(Component.literal(text), width);
        for (int i = 0; i < Math.min(maxLines, lines.size()); i++) graphics.text(font, lines.get(i), x, y + i * 11, 0xFFCCCCCC);
        if (lines.size() > maxLines && mx >= x && mx < x + width && my >= y && my < y + maxLines * 11)
            graphics.setTooltipForNextFrame(Component.literal(text), mx, my);
    }

    private void renderResults(GuiGraphicsExtractor graphics) {
        int x = resultsLeft(), y = resultsTop(), width = resultsWidth();
        graphics.text(font, Component.literal(selectedQuery == null ? "Query Results" : selectedQuery.label), x + 6, y + 4, 0xFFCCCCCC);
        List<FormattedCharSequence> lines = new ArrayList<>();
        if (selectedQuery == null) lines.addAll(font.split(Component.literal("Select History or a query above to view its response here."), width - 12));
        else {
            for (String message : PlayerProfileQueryCapture.lines(selectedQuery.key))
                for (String line : message.split("\\R")) lines.addAll(font.split(Component.literal(line), width - 12));
            if (lines.isEmpty()) lines.addAll(font.split(Component.literal(PlayerProfileQueryCapture.status()), width - 12));
        }
        int capacity = resultLinesPerPage(), pages = Math.max(1, (lines.size() + capacity - 1) / capacity);
        resultPage = Math.min(resultPage, pages - 1);
        for (int i = 0, index = resultPage * capacity; i < capacity && index < lines.size(); i++, index++)
            graphics.text(font, lines.get(index), x + 6, y + 58 + i * 11, 0xFFDDDDDD);
        previousResults.active = resultPage > 0; nextResults.active = resultPage + 1 < pages;
        graphics.centeredText(font, Component.literal((resultPage + 1) + " / " + pages), x + width / 2, y + resultsHeight() - 14, 0xFFAAAAAA);
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
    private void add(int x, int y, int width, String label, Runnable action) { addSized(x, y, width, actionStep() - 3, label, action); }
    private void addSized(int x, int y, int width, int height, String label, Runnable action) {
        this.addRenderableWidget(Button.builder(Component.literal(label), button -> action.run()).pos(x, y).size(width, height).build());
    }
    private static void local(String text) { ClientFeedback.send(CcmcText.literal(text)); }

    @Override
    public void onClose() { ClientScreens.show(this.oldScreen); }

    private enum Workspace { PLAYER, STAFF }
    private enum QueryView {
        HISTORY("History", "history", StaffCapability.BASIC_MODERATION),
        BLOCKS("Block Logs", "coreprotect", StaffCapability.ADVANCED_INVESTIGATION),
        TRADES("Trade Logs", "trade", StaffCapability.BASIC_MODERATION),
        ANTICHEAT("Anti-Cheat", "anticheat", StaffCapability.ADVANCED_INVESTIGATION);
        final String label, key;
        final StaffCapability capability;
        QueryView(String label, String key, StaffCapability capability) { this.label = label; this.key = key; this.capability = capability; }
        String command(String player) {
            return switch (this) {
                case HISTORY -> "history " + player;
                case BLOCKS -> "co lookup user:" + player + " time:2d";
                case TRADES -> "trade logs " + player;
                case ANTICHEAT -> "vulcan profile " + player;
            };
        }
    }
}
