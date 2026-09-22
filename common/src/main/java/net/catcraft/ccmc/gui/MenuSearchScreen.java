package net.catcraft.ccmc.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.catcraft.ccmc.client.ClientScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Live search across the currently rank/capability-filtered Companion menu. */
public final class MenuSearchScreen extends Screen {
    private static final int WIDTH = 420;
    private static final int MAX_RESULTS = 8;
    private final Screen parent;
    private String query = "";
    private int matchCount;

    private MenuSearchScreen(Screen parent) {
        super(Component.literal("Search CatCraft Companion"));
        this.parent = parent;
    }

    public static void open(Screen parent) {
        ClientScreens.show(new MenuSearchScreen(parent));
    }

    @Override
    protected void init() {
        super.init();
        int left = (this.width - WIDTH) / 2;
        int top = Math.max(42, this.height / 2 - 118);
        EditBox search = new EditBox(this.font, left, top, WIDTH, 20, Component.literal("Search commands"));
        search.setMaxLength(64);
        search.setValue(this.query);
        search.setCursorPosition(this.query.length());
        search.setResponder(this::queryChanged);
        this.addRenderableWidget(search);
        this.setInitialFocus(search);

        List<StaffMenuScreen.SearchEntry> matches = matches();
        this.matchCount = matches.size();
        int shown = Math.min(MAX_RESULTS, matches.size());
        for (int i = 0; i < shown; i++) {
            StaffMenuScreen.SearchEntry entry = matches.get(i);
            String label = compact(entry.path() + "  ›  " + entry.label(), WIDTH - 12);
            this.addRenderableWidget(Button.builder(Component.literal(label), button -> entry.action().run())
                    .pos(left, top + 28 + i * 24).size(WIDTH, 20).build());
        }
        this.addRenderableWidget(Button.builder(Component.literal("Back"), button -> onClose())
                .pos(left, top + 28 + MAX_RESULTS * 24 + 6).size(WIDTH, 20).build());
    }

    private void queryChanged(String value) {
        if (value.equals(this.query)) return;
        this.query = value;
        Minecraft.getInstance().execute(() -> {
            if (ClientScreens.current() == this) this.rebuildWidgets();
        });
    }

    private List<StaffMenuScreen.SearchEntry> matches() {
        String needle = normalize(this.query);
        if (needle.isEmpty()) return List.of();
        String compactNeedle = compactKey(needle);
        ArrayList<StaffMenuScreen.SearchEntry> matches = new ArrayList<>();
        for (StaffMenuScreen.SearchEntry entry : StaffMenuScreen.searchEntries(this)) {
            String label = normalize(entry.label());
            String path = normalize(entry.path());
            String combined = label + " " + path;
            if (combined.contains(needle) || compactKey(combined).contains(compactNeedle)) matches.add(entry);
        }
        matches.sort(Comparator
                .comparingInt((StaffMenuScreen.SearchEntry entry) -> score(entry, needle, compactNeedle))
                .thenComparing(StaffMenuScreen.SearchEntry::label, String.CASE_INSENSITIVE_ORDER));
        return matches;
    }

    private static int score(StaffMenuScreen.SearchEntry entry, String needle, String compactNeedle) {
        String label = normalize(entry.label());
        if (label.equals(needle) || compactKey(label).equals(compactNeedle)) return 0;
        if (label.startsWith(needle) || compactKey(label).startsWith(compactNeedle)) return 1;
        if (label.contains(needle) || compactKey(label).contains(compactNeedle)) return 2;
        return 3;
    }

    private static String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }

    private static String compactKey(String text) {
        return text.replaceAll("[^a-z0-9]", "");
    }

    private String compact(String text, int width) {
        if (this.font.width(text) <= width) return text;
        int end = text.length();
        while (end > 1 && this.font.width(text.substring(0, end) + "...") > width) end--;
        return text.substring(0, end) + "...";
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int center = this.width / 2;
        int top = Math.max(12, this.height / 2 - 148);
        graphics.centeredText(this.font, Component.literal("Search CatCraft Companion"), center, top, -1);
        String status = this.query.isBlank() ? "Type a command, task, or destination — for example: homes"
                : this.matchCount == 0 ? "No matching Companion actions"
                : "Showing " + Math.min(MAX_RESULTS, this.matchCount) + " of " + this.matchCount + " matches";
        graphics.centeredText(this.font, Component.literal(status), center, top + 13, 0xFFAAAAAA);
    }

    @Override
    public void onClose() {
        ClientScreens.show(this.parent);
    }
}
