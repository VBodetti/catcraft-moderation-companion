package net.catcraft.ccmc.gui;

import net.catcraft.ccmc.client.CcmcText;
import net.catcraft.ccmc.client.ClientFeedback;
import net.catcraft.ccmc.client.ClientScreens;
import net.catcraft.ccmc.config.CcmcConfig;
import net.catcraft.ccmc.config.CcmcSettingsScreen;
import net.catcraft.ccmc.config.PlayerRank;
import net.catcraft.ccmc.config.StaffCapability;
import net.catcraft.ccmc.config.StaffRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class StaffMenuScreen extends Screen {
    private enum View {
        ROOT("CatCraft Companion"),
        TRAVEL("Travel & Homes"),
        QUICK_TRAVEL("Quick Travel"),
        HOMES("Homes"),
        WARPS("Warps & Biomes"),
        KITS("Kits"),
        SOCIAL("Player & Social"),
        SOCIAL_CHAT("Chat & Messaging"),
        CLAIMS("Claims & Pets"),
        CLAIM_BASICS("Claim Basics"),
        MARKET("Market & Trading"),
        MARKET_SHOPS("Shop Tools"),
        CLANS("Clans"),
        CLAN_TOOLS("Clan Tools"),
        UTILITIES("Utilities & Perks"),
        PERSONAL_TOOLS("Personal Tools"),
        STAFF("Staff Tools"),
        STAFF_MODERATION("Moderation"),
        STAFF_INVESTIGATION("Investigation"),
        STAFF_TELEPORT("Teleport & Recovery"),
        STAFF_CLAIMS("Claims & Grief"),
        STAFF_ANTICHEAT("Anti-Cheat"),
        STAFF_CHAT("Staff Communication"),
        STAFF_SERVER("Server & Realm"),
        STAFF_ELEVATED("Elevated Tools");

        private final String label;
        View(String label) { this.label = label; }
        String label() { return label; }
    }

    private static final int PANEL_WIDTH = 336;
    private static final int COLUMN_GAP = 6;
    private static final int ROW_GAP = 4;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_HEIGHT = BUTTON_HEIGHT + ROW_GAP;

    private final Screen oldScreen;
    private final View view;

    public StaffMenuScreen(Screen oldScreen) { this(oldScreen, View.ROOT); }

    private StaffMenuScreen(Screen oldScreen, View view) {
        super(Component.literal(view.label()));
        this.oldScreen = oldScreen;
        this.view = view;
    }

    public static void open() {
        ClientScreens.show(new StaffMenuScreen(ClientScreens.current()));
    }

    @Override protected void init() {
        super.init();
        int x = Math.max(8, width / 2 - PANEL_WIDTH / 2);
        int y = Math.max(30, height / 2 - 78);

        switch (view) {
            case ROOT -> root(x, y);
            case TRAVEL -> travel(x, y);
            case QUICK_TRAVEL -> quickTravel(x, y);
            case HOMES -> homes(x, y);
            case WARPS -> warps(x, y);
            case KITS -> kits(x, y);
            case SOCIAL -> social(x, y);
            case SOCIAL_CHAT -> socialChat(x, y);
            case CLAIMS -> claims(x, y);
            case CLAIM_BASICS -> claimBasics(x, y);
            case MARKET -> market(x, y);
            case MARKET_SHOPS -> marketShops(x, y);
            case CLANS -> clans(x, y);
            case CLAN_TOOLS -> clanTools(x, y);
            case UTILITIES -> utilities(x, y);
            case PERSONAL_TOOLS -> personalTools(x, y);
            case STAFF -> staff(x, y);
            case STAFF_MODERATION -> staffModeration(x, y);
            case STAFF_INVESTIGATION -> staffInvestigation(x, y);
            case STAFF_TELEPORT -> staffTeleport(x, y);
            case STAFF_CLAIMS -> staffClaims(x, y);
            case STAFF_ANTICHEAT -> staffAntiCheat(x, y);
            case STAFF_CHAT -> staffChat(x, y);
            case STAFF_SERVER -> staffServer(x, y);
            case STAFF_ELEVATED -> staffElevated(x, y);
        }
    }

    private void root(int x, int y) {
        int r = 0;
        grid(x, y, r++, "Travel & Homes", () -> show(View.TRAVEL), "Player & Social", () -> show(View.SOCIAL));
        grid(x, y, r++, "Claims & Pets", () -> show(View.CLAIMS), "Market & Trading", () -> show(View.MARKET));
        grid(x, y, r++, "Clans", () -> show(View.CLANS), "Utilities & Perks", () -> show(View.UTILITIES));

        if (role() != StaffRole.NONE) {
            grid(x, y, r++, "Staff Tools", () -> show(View.STAFF), "Settings & Profile", this::settings);
        } else {
            full(x, y, r++, "Settings & Profile", this::settings);
        }

        int footerY = y + r * ROW_HEIGHT + 8;
        full(x, footerY, 0, profileLabel(), this::settings);
        full(x, footerY + ROW_HEIGHT, 0, "Close", this::closeToPrevious);
    }

    private void travel(int x, int y) {
        grid(x, y, 0, "Quick Travel", () -> show(View.QUICK_TRAVEL), "Homes", () -> show(View.HOMES));
        grid(x, y, 1, "Warps & Biomes", () -> show(View.WARPS), "Kits", () -> show(View.KITS));
        nav(x, y + 2 * ROW_HEIGHT + 10, View.ROOT);
    }

    private void quickTravel(int x, int y) {
        gridCmd(x, y, 0, "Hub", "server Hub", "Back", "back");
        gridCmd(x, y, 1, "Wild", "wild", "Mines", "mine");
        gridCmd(x, y, 2, "Games", "games", "PvP Arena", "pvp");
        nav(x, y + 3 * ROW_HEIGHT + 10, View.TRAVEL);
    }

    private void homes(int x, int y) {
        grid(x, y, 0, "Choose Stored Home", () -> note("Stored-home picker is the next dynamic-command milestone."),
                "Go to Home...", () -> prefill("/home ", "Home"));
        grid(x, y, 1, "Set Home...", () -> prefill("/sethome ", "Set Home"),
                "Delete Home...", () -> prefill("/delhome ", "Delete Home"));
        nav(x, y + 2 * ROW_HEIGHT + 10, View.TRAVEL);
    }

    private void warps(int x, int y) {
        gridCmd(x, y, 0, "Warps", "warps", "Biome Selector", "biomes");
        gridCmd(x, y, 1, "Overworld Biomes", "biome", "Nether Biomes", "warp netherbiome");
        nav(x, y + 2 * ROW_HEIGHT + 10, View.TRAVEL);
    }

    private void kits(int x, int y) {
        gridCmd(x, y, 0, "Default Kit", "kit default", "Claim Kit", "kit claim");
        full(x, y, 1, "Available-kit detection comes in the dynamic-command milestone", () -> note("Kit discovery will be added after the navigation shell passes."));
        nav(x, y + 2 * ROW_HEIGHT + 10, View.TRAVEL);
    }

    private void social(int x, int y) {
        grid(x, y, 0, "Chat & Messaging", () -> show(View.SOCIAL_CHAT), "Player Interactions", () -> note("Player interaction commands are populated in the command-tree milestone."));
        grid(x, y, 1, "Mail & ItemBox", () -> note("Mail and ItemBox actions are populated next."), "Notifications", () -> note("Notification and ignore controls are populated next."));
        nav(x, y + 2 * ROW_HEIGHT + 10, View.ROOT);
    }

    private void socialChat(int x, int y) {
        grid(x, y, 0, "Message...", () -> prefill("/msg ", "Message"), "Reply...", () -> prefill("/reply ", "Reply"));
        grid(x, y, 1, "Mail...", () -> prefill("/mail send ", "Mail"), "Global Chat", () -> send("g"));
        gridCmd(x, y, 2, "Group Chat", "channel group", "Market Chat", "channel market");
        nav(x, y + 3 * ROW_HEIGHT + 10, View.SOCIAL);
    }

    private void claims(int x, int y) {
        grid(x, y, 0, "Claim Basics", () -> show(View.CLAIM_BASICS), "Trust & Access", () -> note("Trust/access commands are populated next."));
        grid(x, y, 1, "Claim Entry Bans", () -> note("Claim-ban commands are populated next."), "Pets", () -> note("Pet commands are populated next."));
        nav(x, y + 2 * ROW_HEIGHT + 10, View.ROOT);
    }

    private void claimBasics(int x, int y) {
        gridCmd(x, y, 0, "Claim Kit", "kit claim", "Claims List", "claimslist");
        gridCmd(x, y, 1, "Trapped", "trapped", "Basic Claim Mode", "basicclaims");
        nav(x, y + 2 * ROW_HEIGHT + 10, View.CLAIMS);
    }

    private void market(int x, int y) {
        gridCmd(x, y, 0, "Go to Market", "market", "My Shop Menu", "shopmenu");
        grid(x, y, 1, "Shop Tools", () -> show(View.MARKET_SHOPS), "Trading", () -> note("Trading commands are populated next."));
        grid(x, y, 2, "Heads", () -> note("Head browsing/search actions are populated next."), "Find a Shop", () -> send("shopfinder"));
        nav(x, y + 3 * ROW_HEIGHT + 10, View.ROOT);
    }

    private void marketShops(int x, int y) {
        grid(x, y, 0, "Teleport to Shop...", () -> prefill("/arm tp ", "Shop Teleport"),
                "Set Shop Landing", () -> send("arm settplocation"));
        grid(x, y, 1, "TradeShop Help", () -> send("ts help"), "Stock Status", () -> send("ts status"));
        nav(x, y + 2 * ROW_HEIGHT + 10, View.MARKET);
    }

    private void clans(int x, int y) {
        gridCmd(x, y, 0, "Clan Menu", "clan", "Clan Chat", "clan chat");
        grid(x, y, 1, "Clan Tools", () -> show(View.CLAN_TOOLS), "Clan Message...", () -> prefill("/c ", "Clan Message"));
        nav(x, y + 2 * ROW_HEIGHT + 10, View.ROOT);
    }

    private void clanTools(int x, int y) {
        grid(x, y, 0, "Clan Info...", () -> prefill("/clan info ", "Clan Info"), "Create Clan...", () -> prefill("/clan create ", "Create Clan"));
        grid(x, y, 1, "Rename Clan...", () -> prefill("/clan rename ", "Rename Clan"), "Leave Clan", () -> prefill("/clan leave", "Leave Clan"));
        nav(x, y + 2 * ROW_HEIGHT + 10, View.CLANS);
    }

    private void utilities(int x, int y) {
        grid(x, y, 0, "Personal Tools", () -> show(View.PERSONAL_TOOLS), "VeinMiner", () -> note("VeinMiner controls are populated next."));
        grid(x, y, 1, "Item & Map Tools", () -> note("ItemBox, frames, trademarks and map tools are populated next."), "Server Info & Links", () -> note("Wiki, rules, vote and server links are populated next."));
        nav(x, y + 2 * ROW_HEIGHT + 10, View.ROOT);
    }

    private void personalTools(int x, int y) {
        gridCmd(x, y, 0, "Playtime", "playtime", "Top Playtime", "toppt");
        grid(x, y, 1, "Sit", () -> send("sit"), "Lay", () -> send("lay"));
        grid(x, y, 2, "Crawl", () -> send("crawl"), "Rank Perks", () -> note("Rank-specific perks will be filtered here in the command-tree milestone."));
        nav(x, y + 3 * ROW_HEIGHT + 10, View.UTILITIES);
    }

    private void staff(int x, int y) {
        List<MenuEntry> entries = new ArrayList<>();
        if (has(StaffCapability.BASIC_MODERATION)) {
            entries.add(new MenuEntry("Moderation", () -> show(View.STAFF_MODERATION)));
            entries.add(new MenuEntry("Investigation", () -> show(View.STAFF_INVESTIGATION)));
        }
        if (has(StaffCapability.PLAYER_TELEPORT)) {
            entries.add(new MenuEntry("Teleport & Recovery", () -> show(View.STAFF_TELEPORT)));
        }
        if (has(StaffCapability.ADVANCED_INVESTIGATION)) {
            entries.add(new MenuEntry("Claims & Grief", () -> show(View.STAFF_CLAIMS)));
            entries.add(new MenuEntry("Anti-Cheat", () -> show(View.STAFF_ANTICHEAT)));
        }
        if (has(StaffCapability.STAFF_CHAT)) {
            entries.add(new MenuEntry("Communication", () -> show(View.STAFF_CHAT)));
        }
        if (has(StaffCapability.MODERATOR_TOOLS) || has(StaffCapability.ADMIN_TOOLS) || has(StaffCapability.DEVELOPER_TOOLS)) {
            entries.add(new MenuEntry("Server & Realm", () -> show(View.STAFF_SERVER)));
        }
        if (hasElevated()) {
            entries.add(new MenuEntry("Elevated Tools", () -> show(View.STAFF_ELEVATED)));
        }

        int rows = renderEntries(x, y, entries);
        nav(x, y + rows * ROW_HEIGHT + 10, View.ROOT);
    }

    private void staffModeration(int x, int y) {
        grid(x, y, 0, "Punish Player...", () -> prefill("/punish ", "Punish"), "Player History...", () -> prefill("/history ", "History"));
        grid(x, y, 1, "Jail Player...", () -> prefill("/togglejail ", "Jail"), "Kick Player...", () -> prefill("/kick ", "Kick"));
        nav(x, y + 2 * ROW_HEIGHT + 10, View.STAFF);
    }

    private void staffInvestigation(int x, int y) {
        grid(x, y, 0, "Playtime...", () -> prefill("/eplaytime ", "Playtime"), "Inventory...", () -> prefill("/open ", "Inventory"));
        if (has(StaffCapability.ADVANCED_INVESTIGATION)) {
            grid(x, y, 1, "Ender Chest...", () -> prefill("/openender ", "Ender Chest"), "Trade Logs...", () -> prefill("/trade logs ", "Trade Logs"));
            nav(x, y + 2 * ROW_HEIGHT + 10, View.STAFF);
        } else {
            nav(x, y + ROW_HEIGHT + 10, View.STAFF);
        }
    }

    private void staffTeleport(int x, int y) {
        grid(x, y, 0, "TPO to Player...", () -> prefill("/tpo ", "TPO"), "Bring Player...", () -> prefill("/tphere ", "TP Here"));
        grid(x, y, 1, "Offline TP...", () -> prefill("/offlinetp ", "Offline TP"), "Teleport Coordinates...", () -> prefill("/tp ", "Coordinate TP"));
        nav(x, y + 2 * ROW_HEIGHT + 10, View.STAFF);
    }

    private void staffClaims(int x, int y) {
        gridCmd(x, y, 0, "Inspect Block", "co i", "Nearby Lookup", "co near");
        grid(x, y, 1, "CoreProtect Lookup...", () -> prefill("/co lookup ", "CoreProtect Lookup"), "Ignore Claims", () -> send("ignoreclaims"));
        nav(x, y + 2 * ROW_HEIGHT + 10, View.STAFF);
    }

    private void staffAntiCheat(int x, int y) {
        grid(x, y, 0, "Vulcan Profile...", () -> prefill("/vulcan profile ", "Vulcan Profile"), "Violations...", () -> prefill("/vulcan violations ", "Vulcan Violations"));
        grid(x, y, 1, "CPS...", () -> prefill("/vulcan cps ", "Vulcan CPS"), "Freeze...", () -> prefill("/vulcan freeze ", "Vulcan Freeze"));
        nav(x, y + 2 * ROW_HEIGHT + 10, View.STAFF);
    }

    private void staffChat(int x, int y) {
        gridCmd(x, y, 0, "Staff Chat", "scc", "Global Chat", "g");
        grid(x, y, 1, "VSC Message...", () -> prefill("/vsc ", "VSC Message"), "Chat On/Off", () -> send("togglechat"));
        if (has(StaffCapability.MODERATOR_TOOLS)) {
            full(x, y, 2, "Mod Chat", () -> send("channel mod"));
            nav(x, y + 3 * ROW_HEIGHT + 10, View.STAFF);
        } else {
            nav(x, y + 2 * ROW_HEIGHT + 10, View.STAFF);
        }
    }

    private void staffServer(int x, int y) {
        gridCmd(x, y, 0, "Vanish", "vanish", "Spectator", "gamemode spectator");
        gridCmd(x, y, 1, "Survival", "gamemode survival", "TPS", "tps");
        gridCmd(x, y, 2, "TradeShop Admin", "ts toggleadmin", "Report Lag", "reportlag");
        nav(x, y + 3 * ROW_HEIGHT + 10, View.STAFF);
    }

    private void staffElevated(int x, int y) {
        int r = 0;
        if (has(StaffCapability.SENIOR_TOOLS)) {
            gridCmd(x, y, r++, "Fly", "fly", "God", "god");
        }
        if (has(StaffCapability.ADMIN_TOOLS)) {
            full(x, y, r++, "Admin command groups are added after the navigation shell passes", () -> note("Admin commands are intentionally not populated in this milestone."));
        }
        if (has(StaffCapability.DEVELOPER_TOOLS)) {
            full(x, y, r++, "Developer command groups are added after the navigation shell passes", () -> note("Developer commands are intentionally not populated in this milestone."));
        }
        nav(x, y + r * ROW_HEIGHT + 10, View.STAFF);
    }

    private int renderEntries(int x, int y, List<MenuEntry> entries) {
        int row = 0;
        for (int i = 0; i < entries.size(); i += 2) {
            MenuEntry left = entries.get(i);
            if (i + 1 < entries.size()) {
                MenuEntry right = entries.get(i + 1);
                grid(x, y, row++, left.label(), left.action(), right.label(), right.action());
            } else {
                full(x, y, row++, left.label(), left.action());
            }
        }
        return row;
    }

    private record MenuEntry(String label, Runnable action) {}

    private void gridCmd(int x, int y, int row, String leftLabel, String leftCommand, String rightLabel, String rightCommand) {
        grid(x, y, row, leftLabel, () -> send(leftCommand), rightLabel, () -> send(rightCommand));
    }

    private void grid(int x, int y, int row, String leftLabel, Runnable left, String rightLabel, Runnable right) {
        int w = (PANEL_WIDTH - COLUMN_GAP) / 2;
        int rowY = y + row * ROW_HEIGHT;
        add(x, rowY, w, leftLabel, left);
        add(x + w + COLUMN_GAP, rowY, w, rightLabel, right);
    }

    private void full(int x, int y, int row, String label, Runnable action) {
        add(x, y + row * ROW_HEIGHT, PANEL_WIDTH, label, action);
    }

    private void nav(int x, int y, View parent) {
        int w = (PANEL_WIDTH - COLUMN_GAP * 2) / 3;
        add(x, y, w, "Back", () -> show(parent));
        add(x + w + COLUMN_GAP, y, w, "Main Menu", () -> show(View.ROOT));
        add(x + (w + COLUMN_GAP) * 2, y, w, "Close", this::closeToPrevious);
    }

    private void add(int x, int y, int buttonWidth, String label, Runnable action) {
        addRenderableWidget(Button.builder(Component.literal(label), b -> action.run())
                .pos(x, y)
                .size(buttonWidth, BUTTON_HEIGHT)
                .build());
    }

    private void settings() {
        ClientScreens.show(CcmcSettingsScreen.create(this));
    }

    private void show(View next) {
        ClientScreens.show(new StaffMenuScreen(oldScreen, next));
    }

    private View parentOf(View current) {
        return switch (current) {
            case ROOT -> ROOT;
            case TRAVEL, SOCIAL, CLAIMS, MARKET, CLANS, UTILITIES, STAFF -> ROOT;
            case QUICK_TRAVEL, HOMES, WARPS, KITS -> TRAVEL;
            case SOCIAL_CHAT -> SOCIAL;
            case CLAIM_BASICS -> CLAIMS;
            case MARKET_SHOPS -> MARKET;
            case CLAN_TOOLS -> CLANS;
            case PERSONAL_TOOLS -> UTILITIES;
            case STAFF_MODERATION, STAFF_INVESTIGATION, STAFF_TELEPORT, STAFF_CLAIMS,
                    STAFF_ANTICHEAT, STAFF_CHAT, STAFF_SERVER, STAFF_ELEVATED -> STAFF;
        };
    }

    private StaffRole role() {
        return StaffRole.parse(CcmcConfig.getString("catcraft.StaffRole"));
    }

    private PlayerRank playerRank() {
        return PlayerRank.parse(CcmcConfig.getString("catcraft.PlayerRank"));
    }

    private boolean has(StaffCapability capability) {
        return role().has(capability);
    }

    private boolean hasElevated() {
        return has(StaffCapability.SENIOR_TOOLS)
                || has(StaffCapability.ADMIN_TOOLS)
                || has(StaffCapability.DEVELOPER_TOOLS);
    }

    private String profileLabel() {
        return "Profile: " + playerRank().displayName() + " • " + role().displayName();
    }

    private void prefill(String value, String label) {
        if (!ChatInputPrefill.prefill(this, value)) {
            local("[CCMC] couldn't prepare " + label + " in chat; no command was sent.");
        }
    }

    private void send(String command) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            local("[CCMC] action unavailable: not connected to a server.");
            return;
        }
        connection.sendCommand(command);
    }

    private void note(String message) {
        local("[CCMC] " + message);
    }

    private static void local(String message) {
        ClientFeedback.send(CcmcText.literal(message));
    }

    private void closeToPrevious() {
        ClientScreens.show(oldScreen);
    }

    @Override public void onClose() {
        if (view == View.ROOT) {
            closeToPrevious();
        } else {
            show(parentOf(view));
        }
    }
}
