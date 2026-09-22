package net.catcraft.ccmc.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.catcraft.ccmc.client.CcmcText;
import net.catcraft.ccmc.client.ClientFeedback;
import net.catcraft.ccmc.client.ClientScreens;
import net.catcraft.ccmc.config.CcmcConfig;
import net.catcraft.ccmc.config.CcmcSettingsScreen;
import net.catcraft.ccmc.config.PlayerRank;
import net.catcraft.ccmc.config.StaffCapability;
import net.catcraft.ccmc.config.StaffRole;
import net.catcraft.ccmc.gui.ChatInputPrefill;
import net.catcraft.ccmc.gui.DynamicCommandPickerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;

public final class StaffMenuScreen
extends Screen {
    private static final int PANEL_WIDTH = 336;
    private static final int COLUMN_GAP = 6;
    private static final int ROW_GAP = 4;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_HEIGHT = 24;
    private static final int PAGE_SIZE = 8;
    private final Screen oldScreen;
    private final View view;
    private final int page;
    private Map<String, SearchEntry> searchTarget;
    private String commandSearchQuery = "";

    public StaffMenuScreen(Screen screen) {
        this(screen, View.ROOT, 0);
    }

    private StaffMenuScreen(Screen screen, View view) {
        this(screen, view, 0);
    }

    private StaffMenuScreen(Screen screen, View view, int n) {
        super((Component)Component.literal((String)view.label()));
        this.oldScreen = screen;
        this.view = view;
        this.page = Math.max(0, n);
    }

    public static void open() {
        ClientScreens.show(new StaffMenuScreen(ClientScreens.current()));
    }

    protected void init() {
        super.init();
        int n = Math.max(8, this.width / 2 - 168);
        if (this.view == View.ROOT && this.searchTarget == null) {
            int searchTop = Math.max(26, this.height / 2 - 106);
            EditBox search = new EditBox(this.font, n, searchTop, PANEL_WIDTH, BUTTON_HEIGHT,
                    Component.literal("Search CatCraft Companion"));
            search.setMaxLength(64);
            search.setHint(Component.literal("Search commands and buttons..."));
            search.setValue(this.commandSearchQuery);
            search.setCursorPosition(this.commandSearchQuery.length());
            search.setResponder(this::commandSearchChanged);
            this.addRenderableWidget(search);
            this.setInitialFocus(search);
            int contentTop = searchTop + 28;
            if (this.commandSearchQuery.isBlank()) this.root(n, contentTop);
            else this.renderCommandSearch(n, contentTop);
            return;
        }
        int n2 = Math.max(26, this.height / 2 - 82);
        this.renderCurrentView(n, n2);
    }

    private void commandSearchChanged(String value) {
        if (value.equals(this.commandSearchQuery)) return;
        this.commandSearchQuery = value;
        Minecraft.getInstance().execute(() -> {
            if (ClientScreens.current() == this) this.rebuildWidgets();
        });
    }

    private void renderCommandSearch(int left, int top) {
        List<SearchEntry> matches = commandSearchMatches(this.commandSearchQuery, this);
        int shown = Math.min(6, matches.size());
        for (int i = 0; i < shown; i++) {
            SearchEntry entry = matches.get(i);
            String label = this.fitSearchLabel(entry.path() + "  ›  " + entry.label(), PANEL_WIDTH - 12);
            this.full(left, top, i, label, entry.action());
        }
        int nextRow = shown;
        if (matches.isEmpty()) {
            this.full(left, top, nextRow++, "No matching Companion actions", () -> {});
        }
        this.full(left, top, nextRow++, "Clear Search", () -> {
            this.commandSearchQuery = "";
            this.rebuildWidgets();
        });
        this.full(left, top, nextRow, "Close", this::closeToPrevious);
    }

    private String fitSearchLabel(String value, int maxWidth) {
        if (this.font.width(value) <= maxWidth) return value;
        int end = value.length();
        while (end > 1 && this.font.width(value.substring(0, end) + "...") > maxWidth) end--;
        return value.substring(0, end) + "...";
    }

    private void renderCurrentView(int n, int n2) {
        switch (this.view.ordinal()) {
            case 0: {
                this.root(n, n2);
                break;
            }
            case 1: {
                this.travel(n, n2);
                break;
            }
            case 2: {
                this.quickTravel(n, n2);
                break;
            }
            case 3: {
                this.homes(n, n2);
                break;
            }
            case 4: {
                this.warps(n, n2);
                break;
            }
            case 5: {
                this.teleportRequests(n, n2);
                break;
            }
            case 6: {
                this.kits(n, n2);
                break;
            }
            case 7: {
                this.social(n, n2);
                break;
            }
            case 8: {
                this.socialChat(n, n2);
                break;
            }
            case 9: {
                this.socialInteractions(n, n2);
                break;
            }
            case 10: {
                this.socialPrivacy(n, n2);
                break;
            }
            case 11: {
                this.socialMailItemBox(n, n2);
                break;
            }
            case 12: {
                this.claims(n, n2);
                break;
            }
            case 13: {
                this.claimBasics(n, n2);
                break;
            }
            case 14: {
                this.claimTrust(n, n2);
                break;
            }
            case 15: {
                this.claimBans(n, n2);
                break;
            }
            case 16: {
                this.claimPets(n, n2);
                break;
            }
            case 17: {
                this.market(n, n2);
                break;
            }
            case 18: {
                this.marketBasics(n, n2);
                break;
            }
            case 19: {
                this.marketShop(n, n2);
                break;
            }
            case 20: {
                this.marketTradeShopProducts(n, n2);
                break;
            }
            case 21: {
                this.marketTradeShopControls(n, n2);
                break;
            }
            case 22: {
                this.marketNews(n, n2);
                break;
            }
            case 23: {
                this.marketHeadsTrade(n, n2);
                break;
            }
            case 24: {
                this.clans(n, n2);
                break;
            }
            case 25: {
                this.clanGeneral(n, n2);
                break;
            }
            case 26: {
                this.clanMembership(n, n2);
                break;
            }
            case 27: {
                this.clanChatProtection(n, n2);
                break;
            }
            case 28: {
                this.utilities(n, n2);
                break;
            }
            case 29: {
                this.personalTools(n, n2);
                break;
            }
            case 30: {
                this.veinMinerModes(n, n2);
                break;
            }
            case 31: {
                this.veinMinerTools(n, n2);
                break;
            }
            case 32: {
                this.itemMapTools(n, n2);
                break;
            }
            case 33: {
                this.serverLinks(n, n2);
                break;
            }
            case 34: {
                this.rankPerks(n, n2);
                break;
            }
            case 35: {
                this.staff(n, n2);
                break;
            }
            case 36: {
                this.staffModeration(n, n2);
                break;
            }
            case 37: {
                this.staffInvestigation(n, n2);
                break;
            }
            case 38: {
                this.staffTeleport(n, n2);
                break;
            }
            case 39: {
                this.staffClaims(n, n2);
                break;
            }
            case 40: {
                this.staffAntiCheat(n, n2);
                break;
            }
            case 41: {
                this.staffChat(n, n2);
                break;
            }
            case 42: {
                this.staffServer(n, n2);
                break;
            }
            case 43: {
                this.staffElevated(n, n2);
            }
        }
    }

    private void root(int n, int n2) {
        int n3 = 0;
        this.grid(n, n2, n3++, "Travel & Homes", () -> this.show(View.TRAVEL), "Player & Social", () -> this.show(View.SOCIAL));
        this.grid(n, n2, n3++, "Claims & Pets", () -> this.show(View.CLAIMS), "Market & Trading", () -> this.show(View.MARKET));
        this.grid(n, n2, n3++, "Clans", () -> this.show(View.CLANS), "Utilities & Perks", () -> this.show(View.UTILITIES));
        if (this.role() != StaffRole.NONE) {
            this.grid(n, n2, n3++, "Staff Tools", () -> this.show(View.STAFF), "Settings & Profile", this::settings);
        } else {
            this.full(n, n2, n3++, "Settings & Profile", this::settings);
        }
        this.full(n, n2, n3++, "Find Player", () -> PlayerSearchScreen.open(this));
        int n4 = n2 + n3 * 24 + 8;
        this.full(n, n4, 0, this.profileLabel(), this::settings);
        this.full(n, n4 + 24, 0, "Close", this::closeToPrevious);
    }

    private void travel(int n, int n2) {
        List<MenuEntry> list = List.of(this.entry("Quick Travel", () -> this.show(View.QUICK_TRAVEL)), this.entry("Homes", () -> this.show(View.HOMES)), this.entry("Warps & Biomes", () -> DynamicCommandPickerScreen.openWarps(this)), this.entry("Teleport Requests", () -> this.show(View.TELEPORT_REQUESTS)), this.entry("Kits", () -> this.show(View.KITS)));
        int n3 = this.renderEntries(n, n2, list);
        this.nav(n, n2 + n3 * 24 + 10, View.ROOT);
    }

    private void quickTravel(int n, int n2) {
        this.gridCmd(n, n2, 0, "Hub", "server Hub", "Back / Last Location", "back");
        this.gridCmd(n, n2, 1, "Wild", "wild", "Cat Canyon Mines", "mines");
        this.gridCmd(n, n2, 2, "Games", "games", "PvP Arena", "pvp");
        this.full(n, n2, 3, "CatGod Pyramids", () -> this.send("catgod"));
        this.nav(n, n2 + 96 + 10, View.TRAVEL);
    }

    private void homes(int n, int n2) {
        this.grid(n, n2, 0, "Choose Stored Home", () -> DynamicCommandPickerScreen.openHomes(this), "Go to Home...", () -> this.prefill("/home ", "Home"));
        this.grid(n, n2, 1, "Set Home...", () -> this.prefill("/sethome ", "Set Home"), "Delete Home...", () -> this.prefill("/delhome ", "Delete Home"));
        this.full(n, n2, 2, this.homeCapacityLabel(), () -> this.note(this.homeCapacityDescription()));
        this.nav(n, n2 + 72 + 10, View.TRAVEL);
    }

    private void warps(int n, int n2) {
        this.gridCmd(n, n2, 0, "Warps", "warps", "Biomes", "biomes");
        this.nav(n, n2 + 24 + 10, View.TRAVEL);
    }

    private void teleportRequests(int n, int n2) {
        this.grid(n, n2, 0, "Request TP...", () -> this.prefill("/tpa ", "Teleport Request"), "Accept Request", () -> this.send("tpaccept"));
        this.gridCmd(n, n2, 1, "Accept All Requests", "tpaccept *", "Cancel / Deny", "tpcancel");
        this.nav(n, n2 + 48 + 10, View.TRAVEL);
    }

    private void kits(int n, int n2) {
        this.gridCmd(n, n2, 0, "Default Kit", "kit default", "Claim Kit", "kit claim");
        this.full(n, n2, 1, "Available Kits", () -> DynamicCommandPickerScreen.openKits(this));
        this.nav(n, n2 + 48 + 10, View.TRAVEL);
    }

    private void social(int n, int n2) {
        this.grid(n, n2, 0, "Chat & Messaging", () -> this.show(View.SOCIAL_CHAT), "Player Interactions", () -> this.show(View.SOCIAL_INTERACTIONS));
        this.grid(n, n2, 1, "Privacy & Notifications", () -> this.show(View.SOCIAL_PRIVACY), "Mail & ItemBox", () -> this.show(View.SOCIAL_MAIL_ITEMBOX));
        this.nav(n, n2 + 48 + 10, View.ROOT);
    }

    private void socialChat(int n, int n2) {
        this.grid(n, n2, 0, "Message...", () -> this.prefill("/msg ", "Message"), "Reply...", () -> this.prefill("/reply ", "Reply"));
        this.gridCmd(n, n2, 1, "Global Chat", "g", "Nearby Group", "channel group");
        this.gridCmd(n, n2, 2, "Market Chat", "channel market", "Cat Cafe Chat", "channel cat");
        this.full(n, n2, 3, "Toggle Chat Visibility", () -> this.send("togglechat"));
        this.nav(n, n2 + 96 + 10, View.SOCIAL);
    }

    private void socialInteractions(int n, int n2) {
        this.grid(n, n2, 0, "Meow at Player...", () -> this.prefill("/meow ", "Meow"), "Purr at Player...", () -> this.prefill("/purr ", "Purr"));
        this.grid(n, n2, 1, "Reveal Real Name...", () -> this.prefill("/realname ", "Real Name"), "Contact Staff...", () -> this.prefill("/helpop ", "HelpOp"));
        this.gridCmd(n, n2, 2, "My Warnings", "warnings", "Toggle PvP", "pvptoggle");
        this.nav(n, n2 + 72 + 10, View.SOCIAL);
    }

    private void socialPrivacy(int n, int n2) {
        this.grid(n, n2, 0, "Ignore / Unignore Player...", () -> this.prefill("/ignoreplayer ", "Ignore Player"), "Ignored Players", () -> this.send("ignoredplayerlist"));
        this.full(n, n2, 1, "Toggle Player Notifications", () -> this.send("notify"));
        this.nav(n, n2 + 48 + 10, View.SOCIAL);
    }

    private void socialMailItemBox(int n, int n2) {
        this.grid(n, n2, 0, "Send Mail...", () -> this.prefill("/mail send ", "Mail"), "Read Mail", () -> this.send("mail read"));
        this.grid(n, n2, 1, "Send Held Item...", () -> this.prefill("/itembox send ", "ItemBox Send"), "Open ItemBox", () -> this.send("itembox open"));
        this.full(n, n2, 2, "Claim All ItemBox Items", () -> this.send("itembox claimall"));
        this.nav(n, n2 + 72 + 10, View.SOCIAL);
    }

    private void claims(int n, int n2) {
        this.grid(n, n2, 0, "Claim Basics", () -> this.show(View.CLAIM_BASICS), "Trust & Access", () -> this.show(View.CLAIM_TRUST));
        this.grid(n, n2, 1, "Claim Entry Bans", () -> this.show(View.CLAIM_BANS), "Pets", () -> this.show(View.CLAIM_PETS));
        this.nav(n, n2 + 48 + 10, View.ROOT);
    }

    private void claimBasics(int n, int n2) {
        this.gridCmd(n, n2, 0, "Claim Kit", "kit claim", "Claims List", "claimslist");
        this.gridCmd(n, n2, 1, "Subdivision Mode", "subdivideclaims", "Basic Claim Mode", "basicclaims");
        this.gridCmd(n, n2, 2, "Trapped", "trapped", "Unlock Death Drops", "unlockdrops");
        this.grid(n, n2, 3, "Abandon This Claim...", () -> this.prefill("/AbandonClaim", "Abandon Claim"), "Abandon ALL Claims...", () -> this.prefill("/AbandonAllClaims", "Abandon All Claims"));
        this.nav(n, n2 + 96 + 10, View.CLAIMS);
    }

    private void claimTrust(int n, int n2) {
        this.grid(n, n2, 0, "Trust Player...", () -> this.prefill("/trust ", "Trust"), "Untrust Player...", () -> this.prefill("/untrust ", "Untrust"));
        this.grid(n, n2, 1, "Access Trust...", () -> this.prefill("/accesstrust ", "Access Trust"), "Container Trust...", () -> this.prefill("/containertrust ", "Container Trust"));
        this.grid(n, n2, 2, "Trust List", () -> this.send("trustlist"), "Untrust Everyone...", () -> this.prefill("/untrust all", "Untrust Everyone"));
        this.nav(n, n2 + 72 + 10, View.CLAIMS);
    }

    private void claimBans(int n, int n2) {
        this.grid(n, n2, 0, "Ban Player from Claim...", () -> this.prefill("/banfromclaim ", "Claim Ban"), "Unban Player...", () -> this.prefill("/unbanfromclaim ", "Claim Unban"));
        this.full(n, n2, 1, "Claim Ban List", () -> this.send("banfromclaimlist"));
        int n3 = 2;
        if (this.playerRank().atLeast(PlayerRank.TIGER)) {
            this.full(n, n2, n3++, "Ban Everyone from Claim... (Tiger+)", () -> this.prefill("/banfromclaimall", "Claim Ban All"));
        }
        this.nav(n, n2 + n3 * 24 + 10, View.CLAIMS);
    }

    private void claimPets(int n, int n2) {
        this.grid(n, n2, 0, "Pet Shop", () -> this.send("petshop"), "Give Tamed Pet...", () -> this.prefill("/givepet ", "Give Pet"));
        this.nav(n, n2 + 24 + 10, View.CLAIMS);
    }

    private void market(int n, int n2) {
        List<MenuEntry> list = List.of(this.entry("Market Basics", () -> this.show(View.MARKET_BASICS)), this.entry("Shop & Stall", () -> this.show(View.MARKET_SHOP)), this.entry("TradeShop Products", () -> this.show(View.MARKET_TS_PRODUCTS)), this.entry("TradeShop Controls", () -> this.show(View.MARKET_TS_CONTROLS)), this.entry("Newspaper", () -> this.show(View.MARKET_NEWS)), this.entry("Heads & Trading", () -> this.show(View.MARKET_HEADS_TRADE)));
        int n3 = this.renderEntries(n, n2, list);
        this.nav(n, n2 + n3 * 24 + 10, View.ROOT);
    }

    private void marketBasics(int n, int n2) {
        this.gridCmd(n, n2, 0, "Go to Market", "market", "Diamond Balance", "balance");
        this.gridCmd(n, n2, 1, "Find Available Shop", "shopfinder", "Find an Item", "search");
        this.gridCmd(n, n2, 2, "Market Chat", "channel market", "Shop Statistics", "shopstats");
        this.nav(n, n2 + 72 + 10, View.MARKET);
    }

    private void marketShop(int n, int n2) {
        this.gridCmd(n, n2, 0, "My Shop Menu", "shopmenu", "ChestShop Help", "help cshop");
        this.grid(n, n2, 1, "Teleport to Shop...", () -> this.prefill("/arm tp ", "Shop Teleport"), "Add Shop Member...", () -> this.prefill("/arm addmember ", "Shop Member"));
        this.gridCmd(n, n2, 2, "Set Shop Landing", "arm settplocation", "Shop Statistics", "shopstats");
        this.nav(n, n2 + 72 + 10, View.MARKET);
    }

    private void marketTradeShopProducts(int n, int n2) {
        this.gridCmd(n, n2, 0, "Inspect Product", "ts what", "TradeShop Help", "ts help");
        this.gridCmd(n, n2, 1, "Set Held Product", "ts setProduct", "Add Held Product", "ts addProduct");
        this.gridCmd(n, n2, 2, "Set Held Cost", "ts setCost", "Add Held Cost", "ts addCost");
        this.nav(n, n2 + 72 + 10, View.MARKET);
    }

    private void marketTradeShopControls(int n, int n2) {
        this.gridCmd(n, n2, 0, "Open TradeShop", "ts open", "Close TradeShop", "ts close");
        this.grid(n, n2, 1, "Add Manager...", () -> this.prefill("/ts addManager ", "TradeShop Manager"), "Stock Status", () -> this.send("ts status"));
        this.nav(n, n2 + 48 + 10, View.MARKET);
    }

    private void marketNews(int n, int n2) {
        this.gridCmd(n, n2, 0, "Browse Newspaper", "news", "Sponsor Looked-at Shop", "ts sponsor");
        this.nav(n, n2 + 24 + 10, View.MARKET);
    }

    private void marketHeadsTrade(int n, int n2) {
        this.grid(n, n2, 0, "Custom Heads", () -> this.send("heads"), "Trade with Player...", () -> this.prefill("/trade ", "Trade"));
        this.full(n, n2, 1, "Search Heads...", () -> this.prefill("/heads search ", "Head Search"));
        this.nav(n, n2 + 48 + 10, View.MARKET);
    }

    private void clans(int n, int n2) {
        this.grid(n, n2, 0, "Clan Basics", () -> this.show(View.CLAN_GENERAL), "Membership", () -> this.show(View.CLAN_MEMBERSHIP));
        this.full(n, n2, 1, "Chat & Protection", () -> this.show(View.CLAN_CHAT_PROTECTION));
        this.nav(n, n2 + 48 + 10, View.ROOT);
    }

    private void clanGeneral(int n, int n2) {
        this.gridCmd(n, n2, 0, "Clan Menu", "clan", "Banner Maker", "bannermaker");
        this.grid(n, n2, 1, "Clan Info...", () -> this.prefill("/clan info ", "Clan Info"), "Create Clan...", () -> this.prefill("/clan create ", "Create Clan"));
        this.full(n, n2, 2, "Rename Clan...", () -> this.prefill("/clan rename ", "Rename Clan"));
        this.nav(n, n2 + 72 + 10, View.CLANS);
    }

    private void clanMembership(int n, int n2) {
        this.grid(n, n2, 0, "Accept Invite...", () -> this.prefill("/clan accept ", "Clan Accept"), "Deny Invite...", () -> this.prefill("/clan deny ", "Clan Deny"));
        this.full(n, n2, 1, "Leave Clan...", () -> this.prefill("/clan leave", "Leave Clan"));
        this.nav(n, n2 + 48 + 10, View.CLANS);
    }

    private void clanChatProtection(int n, int n2) {
        this.grid(n, n2, 0, "Clan Message...", () -> this.prefill("/c ", "Clan Message"), "Toggle Clan Chat", () -> this.send("clan chat"));
        this.gridCmd(n, n2, 1, "Clan PvP Protection ON", "clan protection on", "Clan PvP Protection OFF", "clan protection off");
        this.nav(n, n2 + 48 + 10, View.CLANS);
    }

    private void utilities(int n, int n2) {
        ArrayList<MenuEntry> arrayList = new ArrayList<MenuEntry>();
        arrayList.add(this.entry("Personal Tools", () -> this.show(View.PERSONAL_TOOLS)));
        arrayList.add(this.entry("VeinMiner Modes", () -> this.show(View.VEINMINER_MODES)));
        arrayList.add(this.entry("VeinMiner Tools", () -> this.show(View.VEINMINER_TOOLS)));
        arrayList.add(this.entry("Frames & Map Art", () -> this.show(View.ITEM_MAP_TOOLS)));
        arrayList.add(this.entry("Server Info & Links", () -> this.show(View.SERVER_LINKS)));
        if (this.playerRank().atLeast(PlayerRank.LEOPARD)) {
            arrayList.add(this.entry("Rank Perks", () -> this.show(View.RANK_PERKS)));
        }
        int n3 = this.renderEntries(n, n2, arrayList);
        this.nav(n, n2 + n3 * 24 + 10, View.ROOT);
    }

    private void personalTools(int n, int n2) {
        this.gridCmd(n, n2, 0, "Playtime", "playtime", "Top Playtime", "toppt");
        this.gridCmd(n, n2, 1, "Top Voters", "sv top", "Sit", "sit");
        this.gridCmd(n, n2, 2, "Lay", "lay", "Crawl", "crawl");
        this.nav(n, n2 + 72 + 10, View.UTILITIES);
    }

    private void veinMinerModes(int n, int n2) {
        this.gridCmd(n, n2, 0, "Sneak Only", "veinminer mode sneak", "Stand Only", "veinminer mode stand");
        this.gridCmd(n, n2, 1, "Always", "veinminer mode always", "Turn Off", "veinminer mode none");
        this.nav(n, n2 + 48 + 10, View.UTILITIES);
    }

    private void veinMinerTools(int n, int n2) {
        this.gridCmd(n, n2, 0, "Toggle Hoe", "veinminer toggle hoe", "Toggle Shears", "veinminer toggle shears");
        this.gridCmd(n, n2, 1, "Toggle Shovel", "veinminer toggle shovel", "Toggle Pickaxe", "veinminer toggle pickaxe");
        this.full(n, n2, 2, "Toggle Axe", () -> this.send("veinminer toggle axe"));
        this.nav(n, n2 + 72 + 10, View.UTILITIES);
    }

    private void itemMapTools(int n, int n2) {
        this.gridCmd(n, n2, 0, "Toggle Frame Invisible", "itf toggle", "Frame Toggle Mode", "itf togglemode");
        this.grid(n, n2, 1, "Scan Item Frames...", () -> this.prefill("/itf scan ", "Item Frame Scan"), "BlockLocker Player...", () -> this.prefill("/blocklocker ", "BlockLocker"));
        this.gridCmd(n, n2, 2, "Add Map Trademark", "trademark add", "Remove Map Trademark", "trademark remove");
        this.nav(n, n2 + 72 + 10, View.UTILITIES);
    }

    private void serverLinks(int n, int n2) {
        this.gridCmd(n, n2, 0, "Wiki", "wiki", "Website", "website");
        this.gridCmd(n, n2, 1, "Discord", "discord", "Store", "buy");
        this.gridCmd(n, n2, 2, "Vote", "vote", "Rules", "rules");
        this.full(n, n2, 3, "Official Texture Pack", () -> this.send("texturepack"));
        this.nav(n, n2 + 96 + 10, View.UTILITIES);
    }

    private void rankPerks(int n, int n2) {
        List<MenuEntry> list = this.rankPerkEntries();
        if (list.isEmpty()) {
            this.full(n, n2, 0, "No documented command perks for this rank", () -> this.note("No additional rank commands are documented for " + this.playerRank().displayName() + "."));
            this.nav(n, n2 + 24 + 10, View.UTILITIES);
            return;
        }
        int n3 = Math.max(1, (list.size() + 8 - 1) / 8);
        int n4 = Math.min(this.page, n3 - 1);
        int n5 = n4 * 8;
        int n6 = Math.min(list.size(), n5 + 8);
        int n7 = this.renderEntries(n, n2, list.subList(n5, n6));
        int n8 = n2 + n7 * 24 + 8;
        if (n3 > 1) {
            int n9 = 165;
            this.add(n, n8, n9, (String)(n4 > 0 ? "Previous Page" : "Page " + (n4 + 1) + "/" + n3), n4 > 0 ? () -> this.show(View.RANK_PERKS, n4 - 1) : () -> this.note("Already on the first rank-perk page."));
            this.add(n + n9 + 6, n8, n9, (String)(n4 + 1 < n3 ? "Next Page" : "Page " + (n4 + 1) + "/" + n3), n4 + 1 < n3 ? () -> this.show(View.RANK_PERKS, n4 + 1) : () -> this.note("Already on the last rank-perk page."));
            n8 += 28;
        }
        this.nav(n, n8, View.UTILITIES);
    }

    private List<MenuEntry> rankPerkEntries() {
        ArrayList<MenuEntry> arrayList = new ArrayList<MenuEntry>();
        if (this.playerRank().atLeast(PlayerRank.LEOPARD)) {
            arrayList.add(this.command("Personal Time...", "/ptime ", true));
            arrayList.add(this.command("Portable Crafting", "craft", false));
            arrayList.add(this.command("Daily Cat Diamond", "redeem", false));
        }
        if (this.playerRank().atLeast(PlayerRank.CHEETAH)) {
            arrayList.add(this.command("Wear Held Item as Hat", "hat", false));
            arrayList.add(this.command("Personal Ender Chest", "enderchest", false));
            arrayList.add(this.command("Set Nickname...", "/nickname ", true));
            arrayList.add(this.command("Remove Nickname", "nick off", false));
        }
        if (this.playerRank().atLeast(PlayerRank.JAGUAR)) {
            arrayList.add(this.command("Name Gradients", "gradients", false));
            arrayList.add(this.command("Portable Disposal", "disposal", false));
            arrayList.add(this.command("Feed", "feed", false));
            arrayList.add(this.command("Sort Inventory", "invsort", false));
            arrayList.add(this.command("Chest Sort ON", "chestsort on", false));
            arrayList.add(this.command("Chest Sort Hotkeys", "chestsort hotkeys", false));
        }
        if (this.playerRank().atLeast(PlayerRank.TIGER)) {
            arrayList.add(this.command("Repair Held Item", "repair", false));
        }
        if (this.playerRank().atLeast(PlayerRank.LION)) {
            arrayList.add(this.command("Heal", "heal", false));
            arrayList.add(this.command("Repair All", "repair all", false));
            arrayList.add(this.command("Night Vision", "nv", false));
            arrayList.add(this.command("Return", "return", false));
            arrayList.add(this.command("Virtual Stonecutter", "stonecutter", false));
        }
        return arrayList;
    }

    private void staff(int n, int n2) {
        ArrayList<MenuEntry> arrayList = new ArrayList<MenuEntry>();
        if (this.has(StaffCapability.BASIC_MODERATION)) {
            arrayList.add(this.entry("Moderation", () -> this.show(View.STAFF_MODERATION)));
            arrayList.add(this.entry("Investigation", () -> this.show(View.STAFF_INVESTIGATION)));
        }
        if (this.has(StaffCapability.PLAYER_TELEPORT)) {
            arrayList.add(this.entry("Teleport & Recovery", () -> this.show(View.STAFF_TELEPORT)));
        }
        if (this.has(StaffCapability.ADVANCED_INVESTIGATION)) {
            arrayList.add(this.entry("Claims & Grief", () -> this.show(View.STAFF_CLAIMS)));
            arrayList.add(this.entry("Anti-Cheat", () -> this.show(View.STAFF_ANTICHEAT)));
        }
        if (this.has(StaffCapability.STAFF_CHAT)) {
            arrayList.add(this.entry("Communication", () -> this.show(View.STAFF_CHAT)));
        }
        if (this.has(StaffCapability.MODERATOR_TOOLS) || this.has(StaffCapability.ADMIN_TOOLS) || this.has(StaffCapability.DEVELOPER_TOOLS)) {
            arrayList.add(this.entry("Server & Realm", () -> this.show(View.STAFF_SERVER)));
        }
        if (this.hasElevated()) {
            arrayList.add(this.entry("Elevated Tools", () -> this.show(View.STAFF_ELEVATED)));
        }
        int n3 = this.renderEntries(n, n2, arrayList);
        this.nav(n, n2 + n3 * 24 + 10, View.ROOT);
    }

    private void staffModeration(int n, int n2) {
        this.grid(n, n2, 0, "Punish Player...", () -> this.prefill("/punish ", "Punish"), "Player History...", () -> this.prefill("/history ", "History"));
        this.grid(n, n2, 1, "Jail Player...", () -> this.prefill("/togglejail ", "Jail"), "Kick Player...", () -> this.prefill("/kick ", "Kick"));
        this.nav(n, n2 + 48 + 10, View.STAFF);
    }

    private void staffInvestigation(int n, int n2) {
        this.grid(n, n2, 0, "Playtime...", () -> this.prefill("/eplaytime ", "Playtime"), "Inventory...", () -> this.prefill("/open ", "Inventory"));
        if (this.has(StaffCapability.ADVANCED_INVESTIGATION)) {
            this.grid(n, n2, 1, "Ender Chest...", () -> this.prefill("/openender ", "Ender Chest"), "Trade Logs...", () -> this.prefill("/trade logs ", "Trade Logs"));
            this.nav(n, n2 + 48 + 10, View.STAFF);
        } else {
            this.nav(n, n2 + 24 + 10, View.STAFF);
        }
    }

    private void staffTeleport(int n, int n2) {
        this.grid(n, n2, 0, "TPO to Player...", () -> this.prefill("/tpo ", "TPO"), "Bring Player...", () -> this.prefill("/tphere ", "TP Here"));
        this.grid(n, n2, 1, "Offline TP...", () -> this.prefill("/offlinetp ", "Offline TP"), "Teleport Coordinates...", () -> this.prefill("/tp ", "Coordinate TP"));
        this.nav(n, n2 + 48 + 10, View.STAFF);
    }

    private void staffClaims(int n, int n2) {
        this.gridCmd(n, n2, 0, "Inspect Block", "co i", "Nearby Lookup", "co near");
        this.grid(n, n2, 1, "CoreProtect Lookup...", () -> this.prefill("/co lookup ", "CoreProtect Lookup"), "Ignore Claims", () -> this.send("ignoreclaims"));
        this.nav(n, n2 + 48 + 10, View.STAFF);
    }

    private void staffAntiCheat(int n, int n2) {
        this.grid(n, n2, 0, "Vulcan Profile...", () -> this.prefill("/vulcan profile ", "Vulcan Profile"), "Violations...", () -> this.prefill("/vulcan violations ", "Vulcan Violations"));
        this.grid(n, n2, 1, "CPS...", () -> this.prefill("/vulcan cps ", "Vulcan CPS"), "Freeze...", () -> this.prefill("/vulcan freeze ", "Vulcan Freeze"));
        this.nav(n, n2 + 48 + 10, View.STAFF);
    }

    private void staffChat(int n, int n2) {
        this.gridCmd(n, n2, 0, "Staff Chat", "scc", "Global Chat", "g");
        this.grid(n, n2, 1, "VSC Message...", () -> this.prefill("/vsc ", "VSC Message"), "Chat On/Off", () -> this.send("togglechat"));
        if (this.has(StaffCapability.MODERATOR_TOOLS)) {
            this.full(n, n2, 2, "Mod Chat", () -> this.send("channel mod"));
            this.nav(n, n2 + 72 + 10, View.STAFF);
        } else {
            this.nav(n, n2 + 48 + 10, View.STAFF);
        }
    }

    private void staffServer(int n, int n2) {
        this.gridCmd(n, n2, 0, "Vanish", "vanish", "Spectator", "gamemode spectator");
        this.gridCmd(n, n2, 1, "Survival", "gamemode survival", "TPS", "tps");
        this.gridCmd(n, n2, 2, "TradeShop Admin", "ts toggleadmin", "Report Lag", "reportlag");
        this.nav(n, n2 + 72 + 10, View.STAFF);
    }

    private void staffElevated(int n, int n2) {
        int n3 = 0;
        if (this.has(StaffCapability.SENIOR_TOOLS)) {
            this.gridCmd(n, n2, n3++, "Fly", "fly", "God", "god");
        }
        if (this.has(StaffCapability.ADMIN_TOOLS)) {
            this.full(n, n2, n3++, "Admin command groups are added in the staff-command milestone", () -> this.note("Admin commands are intentionally not populated in this milestone."));
        }
        if (this.has(StaffCapability.DEVELOPER_TOOLS)) {
            this.full(n, n2, n3++, "Developer command groups are added in the staff-command milestone", () -> this.note("Developer commands are intentionally not populated in this milestone."));
        }
        this.nav(n, n2 + n3 * 24 + 10, View.STAFF);
    }

    private int renderEntries(int n, int n2, List<MenuEntry> list) {
        int n3 = 0;
        for (int i = 0; i < list.size(); i += 2) {
            MenuEntry menuEntry = list.get(i);
            if (i + 1 < list.size()) {
                MenuEntry menuEntry2 = list.get(i + 1);
                this.grid(n, n2, n3++, menuEntry.label(), menuEntry.action(), menuEntry2.label(), menuEntry2.action());
                continue;
            }
            this.full(n, n2, n3++, menuEntry.label(), menuEntry.action());
        }
        return n3;
    }

    private MenuEntry entry(String string, Runnable runnable) {
        return new MenuEntry(string, runnable);
    }

    private MenuEntry command(String string, String string2, boolean bl) {
        return this.entry(string, bl ? () -> this.prefill(string2, string) : () -> this.send(string2));
    }

    private void gridCmd(int n, int n2, int n3, String string, String string2, String string3, String string4) {
        this.grid(n, n2, n3, string, () -> this.send(string2), string3, () -> this.send(string4));
    }

    private void grid(int n, int n2, int n3, String string, Runnable runnable, String string2, Runnable runnable2) {
        int n4 = 165;
        int n5 = n2 + n3 * 24;
        this.add(n, n5, n4, string, runnable);
        this.add(n + n4 + 6, n5, n4, string2, runnable2);
    }

    private void full(int n, int n2, int n3, String string, Runnable runnable) {
        this.add(n, n2 + n3 * 24, 336, string, runnable);
    }

    private void nav(int n, int n2, View view) {
        int n3 = 108;
        this.add(n, n2, n3, "Back", () -> this.show(view));
        this.add(n + n3 + 6, n2, n3, "Main Menu", () -> this.show(View.ROOT));
        this.add(n + (n3 + 6) * 2, n2, n3, "Close", this::closeToPrevious);
    }

    private void add(int n, int n2, int n3, String string, Runnable runnable) {
        if (this.searchTarget != null) {
            if (this.isSearchableLabel(string)) {
                String key = this.view.name() + "|" + string;
                this.searchTarget.putIfAbsent(key, new SearchEntry(string, this.view.label(), runnable));
            }
            return;
        }
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)string), button -> runnable.run()).pos(n, n2).size(n3, 20).build());
    }

    static List<SearchEntry> searchEntries(Screen returnScreen) {
        LinkedHashMap<String, SearchEntry> entries = new LinkedHashMap<>();
        for (View candidate : View.values()) {
            int pages = candidate == View.RANK_PERKS ? 4 : 1;
            for (int page = 0; page < pages; page++) {
                StaffMenuScreen collector = new StaffMenuScreen(returnScreen, candidate, page);
                collector.searchTarget = entries;
                collector.renderCurrentView(0, 0);
            }
        }
        return List.copyOf(entries.values());
    }

    private static List<SearchEntry> commandSearchMatches(String query, Screen returnScreen) {
        String needle = normalizeSearch(query);
        if (needle.isEmpty()) return List.of();
        String compactNeedle = compactSearchKey(needle);
        ArrayList<SearchEntry> matches = new ArrayList<>();
        for (SearchEntry entry : searchEntries(returnScreen)) {
            String label = normalizeSearch(entry.label());
            String path = normalizeSearch(entry.path());
            String combined = label + " " + path;
            if (combined.contains(needle) || compactSearchKey(combined).contains(compactNeedle)) matches.add(entry);
        }
        matches.sort(Comparator
                .comparingInt((SearchEntry entry) -> searchScore(entry, needle, compactNeedle))
                .thenComparing(SearchEntry::label, String.CASE_INSENSITIVE_ORDER));
        return matches;
    }

    private static int searchScore(SearchEntry entry, String needle, String compactNeedle) {
        String label = normalizeSearch(entry.label());
        if (label.equals(needle) || compactSearchKey(label).equals(compactNeedle)) return 0;
        if (label.startsWith(needle) || compactSearchKey(label).startsWith(compactNeedle)) return 1;
        if (label.contains(needle) || compactSearchKey(label).contains(compactNeedle)) return 2;
        return 3;
    }

    private static String normalizeSearch(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }

    private static String compactSearchKey(String text) {
        return text.replaceAll("[^a-z0-9]", "");
    }

    private boolean isSearchableLabel(String label) {
        if (label == null || label.isBlank()) return false;
        if (label.equals("Back") || label.equals("Main Menu") || label.equals("Close")) return false;
        if (label.equals("Previous Page") || label.equals("Next Page") || label.startsWith("Page ")) return false;
        if (label.startsWith("Profile:") || label.startsWith("Home Slots:")) return false;
        return true;
    }

    private void settings() {
        ClientScreens.show(CcmcSettingsScreen.create(this));
    }

    private void show(View view) {
        this.show(view, 0);
    }

    private void show(View view, int n) {
        ClientScreens.show(new StaffMenuScreen(this.oldScreen, view, n));
    }

    private View parentOf(View view) {
        return switch (view.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> View.ROOT;
            case 1, 7, 12, 17, 24, 28, 35 -> View.ROOT;
            case 2, 3, 4, 5, 6 -> View.TRAVEL;
            case 8, 9, 10, 11 -> View.SOCIAL;
            case 13, 14, 15, 16 -> View.CLAIMS;
            case 18, 19, 20, 21, 22, 23 -> View.MARKET;
            case 25, 26, 27 -> View.CLANS;
            case 29, 30, 31, 32, 33, 34 -> View.UTILITIES;
            case 36, 37, 38, 39, 40, 41, 42, 43 -> View.STAFF;
        };
    }

    private StaffRole role() {
        return StaffRole.parse(CcmcConfig.getString("catcraft.StaffRole"));
    }

    private PlayerRank playerRank() {
        return PlayerRank.parse(CcmcConfig.getString("catcraft.PlayerRank"));
    }

    private boolean has(StaffCapability staffCapability) {
        return this.role().has(staffCapability);
    }

    private boolean hasElevated() {
        return this.has(StaffCapability.SENIOR_TOOLS) || this.has(StaffCapability.ADMIN_TOOLS) || this.has(StaffCapability.DEVELOPER_TOOLS);
    }

    private String profileLabel() {
        return "Profile: " + this.playerRank().displayName() + " \u2022 " + this.role().displayName();
    }

    private String homeCapacityLabel() {
        return "Home Slots: " + this.homeCapacity() + " \u2022 " + this.playerRank().displayName();
    }

    private String homeCapacityDescription() {
        return this.playerRank().displayName() + " is configured for up to " + this.homeCapacity() + " named homes, plus the normal bed home where applicable.";
    }

    private int homeCapacity() {
        return switch (this.playerRank()) {
            default -> throw new MatchException(null, null);
            case PlayerRank.MEMBER, PlayerRank.CAT -> 2;
            case PlayerRank.LEOPARD -> 3;
            case PlayerRank.CHEETAH -> 5;
            case PlayerRank.JAGUAR -> 12;
            case PlayerRank.TIGER -> 25;
            case PlayerRank.LION -> 50;
        };
    }

    private void prefill(String string, String string2) {
        if (!ChatInputPrefill.prefill((Object)this, string)) {
            StaffMenuScreen.local("[CCC] couldn't prepare " + string2 + " in chat; no command was sent.");
        }
    }

    private void send(String string) {
        ClientPacketListener clientPacketListener = Minecraft.getInstance().getConnection();
        if (clientPacketListener == null) {
            StaffMenuScreen.local("[CCC] action unavailable: not connected to a server.");
            return;
        }
        clientPacketListener.sendCommand(string);
    }

    private void note(String string) {
        StaffMenuScreen.local("[CCC] " + string);
    }

    private static void local(String string) {
        ClientFeedback.send((Component)CcmcText.literal(string));
    }

    private void closeToPrevious() {
        ClientScreens.show(this.oldScreen);
    }

    public void onClose() {
        if (this.view == View.ROOT) {
            this.closeToPrevious();
        } else {
            this.show(this.parentOf(this.view));
        }
    }

    private static enum View {
        ROOT("CatCraft Companion"),
        TRAVEL("Travel & Homes"),
        QUICK_TRAVEL("Quick Travel"),
        HOMES("Homes"),
        WARPS("Warps & Biomes"),
        TELEPORT_REQUESTS("Teleport Requests"),
        KITS("Kits"),
        SOCIAL("Player & Social"),
        SOCIAL_CHAT("Chat & Messaging"),
        SOCIAL_INTERACTIONS("Player Interactions"),
        SOCIAL_PRIVACY("Privacy & Notifications"),
        SOCIAL_MAIL_ITEMBOX("Mail & ItemBox"),
        CLAIMS("Claims & Pets"),
        CLAIM_BASICS("Claim Basics"),
        CLAIM_TRUST("Trust & Access"),
        CLAIM_BANS("Claim Entry Bans"),
        CLAIM_PETS("Pets"),
        MARKET("Market & Trading"),
        MARKET_BASICS("Market Basics"),
        MARKET_SHOP("Shop & Stall"),
        MARKET_TS_PRODUCTS("TradeShop Products"),
        MARKET_TS_CONTROLS("TradeShop Controls"),
        MARKET_NEWS("Newspaper"),
        MARKET_HEADS_TRADE("Heads & Trading"),
        CLANS("Clans"),
        CLAN_GENERAL("Clan Basics"),
        CLAN_MEMBERSHIP("Clan Membership"),
        CLAN_CHAT_PROTECTION("Clan Chat & Protection"),
        UTILITIES("Utilities & Perks"),
        PERSONAL_TOOLS("Personal Tools"),
        VEINMINER_MODES("VeinMiner Modes"),
        VEINMINER_TOOLS("VeinMiner Tools"),
        ITEM_MAP_TOOLS("Frames & Map Art"),
        SERVER_LINKS("Server Info & Links"),
        RANK_PERKS("Rank Perks"),
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

        private View(String string2) {
            this.label = string2;
        }

        String label() {
            return this.label;
        }
    }

    private record MenuEntry(String label, Runnable action) {
    }

    record SearchEntry(String label, String path, Runnable action) {
    }
}
