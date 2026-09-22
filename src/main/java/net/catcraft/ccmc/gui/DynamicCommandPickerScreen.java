package net.catcraft.ccmc.gui;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.catcraft.ccmc.client.ClientScreens;
import net.catcraft.ccmc.gui.ChatInputPrefill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;

public final class DynamicCommandPickerScreen
extends Screen {
    private static final int PANEL_WIDTH = 300;
    private static final int PAGE_SIZE = 8;
    private static final int GAP = 6;
    private static final int ROW = 24;
    private final Screen parent;
    private final Kind kind;
    private List<String> values = List.of();
    private boolean requested;
    private boolean loading = true;
    private String status = "Loading live CatCraft options...";
    private int page;

    private DynamicCommandPickerScreen(Screen screen, Kind kind) {
        super((Component)Component.literal((String)kind.title));
        this.parent = screen;
        this.kind = kind;
    }

    private DynamicCommandPickerScreen(Screen screen, Kind kind, List<String> list, String string, int n) {
        super((Component)Component.literal((String)kind.title));
        this.parent = screen;
        this.kind = kind;
        this.values = list;
        this.status = string;
        this.page = n;
        this.loading = false;
        this.requested = true;
    }

    public static void openHomes(Screen screen) {
        ClientScreens.show(new DynamicCommandPickerScreen(screen, Kind.HOMES));
    }

    public static void openKits(Screen screen) {
        ClientScreens.show(new DynamicCommandPickerScreen(screen, Kind.KITS));
    }

    public static void openWarps(Screen screen) {
        ClientScreens.show(new DynamicCommandPickerScreen(screen, Kind.WARPS));
    }

    protected void init() {
        int n;
        int n2;
        super.init();
        if (!this.requested) {
            this.requested = true;
            this.requestSuggestions();
        }
        int n3 = this.width / 2 - 150;
        int n4 = Math.max(48, this.height / 2 - 78);
        int n5 = 147;
        if (this.kind == Kind.WARPS) {
            this.add(n3, n4, n5, "Biomes GUI", () -> this.sendAndBack("biomes"));
            this.add(n3 + n5 + 6, n4, n5, "Refresh Warps", this::refresh);
            n4 += 24;
        }
        if (this.loading) {
            this.add(n3, n4, 300, "Loading...", () -> {});
            n4 += 24;
        } else if (this.values.isEmpty()) {
            this.add(n3, n4, 300, this.kind.fallback, this::fallbackAutocomplete);
            n4 += 24;
        } else {
            n2 = this.page * 8;
            n = Math.min(this.values.size(), n2 + 8);
            int n6 = 0;
            for (int i = n2; i < n; i += 2) {
                String string = this.values.get(i);
                int n7 = n4 + n6 * 24;
                this.add(n3, n7, n5, this.compact(string), () -> this.choose(string));
                if (i + 1 < n) {
                    String string2 = this.values.get(i + 1);
                    this.add(n3 + n5 + 6, n7, n5, this.compact(string2), () -> this.choose(string2));
                }
                ++n6;
            }
            n4 += (n - n2 + 1) / 2 * 24;
        }
        n2 = Math.max(1, (this.values.size() + 8 - 1) / 8);
        if (!this.loading && n2 > 1) {
            n = 96;
            this.add(n3, n4, n, "Previous", () -> this.changePage(-1));
            this.add(n3 + n + 6, n4, n, this.page + 1 + " / " + n2, () -> {});
            this.add(n3 + (n + 6) * 2, n4, n, "Next", () -> this.changePage(1));
            n4 += 24;
        }
        n = 147;
        this.add(n3, n4 + 4, n, "Refresh", this::refresh);
        this.add(n3 + n + 6, n4 + 4, n, "Back", this::onClose);
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        super.extractRenderState(guiGraphicsExtractor, n, n2, f);
        int n3 = this.width / 2;
        int n4 = Math.max(8, this.height / 2 - 108);
        guiGraphicsExtractor.centeredText(this.font, (Component)Component.literal((String)this.kind.title), n3, n4, -1);
        guiGraphicsExtractor.centeredText(this.font, (Component)Component.literal((String)this.status), n3, n4 + 12, -5592406);
    }

    private void requestSuggestions() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener clientPacketListener = minecraft.getConnection();
        if (clientPacketListener == null) {
            this.loading = false;
            this.status = "Not connected to a server.";
            return;
        }
        try {
            CommandDispatcher commandDispatcher = clientPacketListener.getCommands();
            ClientSuggestionProvider clientSuggestionProvider = clientPacketListener.getSuggestionsProvider();
            ParseResults parseResults = commandDispatcher.parse(this.kind.query, (Object)clientSuggestionProvider);
            CompletableFuture completableFuture = commandDispatcher.getCompletionSuggestions(parseResults);
            completableFuture.whenComplete(this::applySuggestions);
        }
        catch (Throwable throwable) {
            this.loading = false;
            this.status = "Live options unavailable; use server autocomplete.";
        }
    }

    private void applySuggestions(Suggestions suggestions, Throwable throwable) {
        String string;
        List<String> list = List.of();
        if (throwable != null || suggestions == null) {
            string = "Live options unavailable; use server autocomplete.";
        } else {
            LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>();
            for (Suggestion suggestion : suggestions.getList()) {
                String string2 = suggestion.getText();
                if (string2 == null || (string2 = string2.trim()).isBlank()) continue;
                linkedHashSet.add(string2);
            }
            list = new ArrayList<String>(linkedHashSet);
            string = list.isEmpty() ? "No live entries returned; server autocomplete is available." : list.size() + " live option" + (list.size() == 1 ? "" : "s") + " from CatCraft";
        }
        ClientScreens.show(new DynamicCommandPickerScreen(this.parent, this.kind, list, string, 0));
    }

    private void refresh() {
        ClientScreens.show(new DynamicCommandPickerScreen(this.parent, this.kind));
    }

    private void choose(String string) {
        this.sendAndBack(this.kind.command + " " + string);
    }

    private void sendAndBack(String string) {
        ClientPacketListener clientPacketListener = Minecraft.getInstance().getConnection();
        if (clientPacketListener == null) {
            return;
        }
        clientPacketListener.sendCommand(string);
        ClientScreens.show(this.parent);
    }

    private void fallbackAutocomplete() {
        if (!ChatInputPrefill.prefill((Object)this, "/" + this.kind.query)) {
            ClientScreens.show(this.parent);
        }
    }

    private void changePage(int n) {
        int n2 = Math.max(1, (this.values.size() + 8 - 1) / 8);
        int n3 = Math.max(0, Math.min(n2 - 1, this.page + n));
        ClientScreens.show(new DynamicCommandPickerScreen(this.parent, this.kind, this.values, this.status, n3));
    }

    private String compact(String string) {
        return string.length() <= 25 ? string : string.substring(0, 22) + "...";
    }

    private void add(int n, int n2, int n3, String string, Runnable runnable) {
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.literal((String)string), button -> runnable.run()).pos(n, n2).size(n3, 20).build());
    }

    public void onClose() {
        ClientScreens.show(this.parent);
    }

    private static enum Kind {
        HOMES("Homes", "home ", "home", "Use /home autocomplete..."),
        KITS("Available Kits", "kit ", "kit", "Use /kit autocomplete..."),
        WARPS("Warps & Biomes", "warp ", "warp", "Use /warp autocomplete...");

        final String title;
        final String query;
        final String command;
        final String fallback;

        private Kind(String string2, String string3, String string4, String string5) {
            this.title = string2;
            this.query = string3;
            this.command = string4;
            this.fallback = string5;
        }
    }
}

