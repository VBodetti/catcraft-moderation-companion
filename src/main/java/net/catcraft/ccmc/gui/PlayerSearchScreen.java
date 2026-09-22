package net.catcraft.ccmc.gui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.catcraft.ccmc.client.ClientScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

/** Player lookup with live/session suggestions and exact offline-name support. */
public final class PlayerSearchScreen extends Screen {
    private static final int WIDTH = 360;
    private static final int MAX_RESULTS = 7;
    private final Screen parent;
    private final LinkedHashSet<String> candidates = new LinkedHashSet<>();
    private String query = "";
    private boolean requested;
    private boolean loading = true;
    private int matchCount;

    private PlayerSearchScreen(Screen parent) {
        super(Component.literal("Find Player"));
        this.parent = parent;
    }

    public static void open(Screen parent) {
        ClientScreens.show(new PlayerSearchScreen(parent));
    }

    @Override
    protected void init() {
        super.init();
        collectSessionNames();
        if (!this.requested) {
            this.requested = true;
            requestServerSuggestions();
        }
        int left = (this.width - WIDTH) / 2;
        int top = Math.max(42, this.height / 2 - 110);
        EditBox search = new EditBox(this.font, left, top, WIDTH, 20, Component.literal("Minecraft username"));
        search.setMaxLength(16);
        search.setValue(this.query);
        search.setCursorPosition(this.query.length());
        search.setResponder(this::queryChanged);
        this.addRenderableWidget(search);
        this.setInitialFocus(search);

        Button exact = Button.builder(Component.literal(validName(this.query)
                        ? "Open profile for " + this.query.trim() : "Type an exact username to open an offline profile"),
                button -> openProfile(this.query.trim())).pos(left, top + 28).size(WIDTH, 20).build();
        exact.active = validName(this.query);
        this.addRenderableWidget(exact);

        List<String> matches = filteredCandidates();
        this.matchCount = matches.size();
        int shown = Math.min(MAX_RESULTS, matches.size());
        for (int i = 0; i < shown; i++) {
            String name = matches.get(i);
            boolean online = isOnline(name);
            this.addRenderableWidget(Button.builder(Component.literal(name + (online ? "  •  Online" : "")),
                    button -> openProfile(name)).pos(left, top + 56 + i * 24).size(WIDTH, 20).build());
        }
        this.addRenderableWidget(Button.builder(Component.literal("Back"), button -> onClose())
                .pos(left, top + 56 + MAX_RESULTS * 24 + 6).size(WIDTH, 20).build());
    }

    private void queryChanged(String value) {
        if (value.equals(this.query)) return;
        this.query = value;
        Minecraft.getInstance().execute(() -> {
            if (ClientScreens.current() == this) this.rebuildWidgets();
        });
    }

    private void collectSessionNames() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;
        for (PlayerInfo info : connection.getSeenPlayers().values()) {
            String name = profileName(info);
            if (validName(name)) this.candidates.add(name);
        }
    }

    private String profileName(PlayerInfo info) {
        if (info == null) return null;
        try {
            Object profile = info.getClass().getMethod("getProfile").invoke(info);
            for (String methodName : new String[]{"name", "getName"}) {
                try {
                    Object value = profile.getClass().getMethod(methodName).invoke(profile);
                    if (value instanceof String name) return name;
                } catch (ReflectiveOperationException ignored) { }
            }
        } catch (ReflectiveOperationException ignored) { }
        return null;
    }

    private void requestServerSuggestions() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            this.loading = false;
            return;
        }
        try {
            Object dispatcher = connection.getClass().getMethod("getCommands").invoke(connection);
            Object provider = connection.getClass().getMethod("getSuggestionsProvider").invoke(connection);
            Method parse = findMethod(dispatcher.getClass(), "parse", 2);
            Object parsed = parse.invoke(dispatcher, "seen ", provider);
            Method complete = findMethod(dispatcher.getClass(), "getCompletionSuggestions", 1);
            Object future = complete.invoke(dispatcher, parsed);
            if (future instanceof CompletableFuture<?> completion) {
                completion.whenComplete((suggestions, error) -> applySuggestions(suggestions, error));
                return;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) { }
        this.loading = false;
    }

    private static Method findMethod(Class<?> type, String name, int parameters) throws NoSuchMethodException {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == parameters) return method;
        }
        throw new NoSuchMethodException(name);
    }

    private void applySuggestions(Object suggestions, Throwable error) {
        ArrayList<String> found = new ArrayList<>();
        if (error == null && suggestions != null) {
            try {
                Object list = suggestions.getClass().getMethod("getList").invoke(suggestions);
                if (list instanceof Iterable<?> iterable) {
                    for (Object suggestion : iterable) {
                        Object value = suggestion.getClass().getMethod("getText").invoke(suggestion);
                        if (value instanceof String name && validName(name)) found.add(name);
                    }
                }
            } catch (ReflectiveOperationException ignored) { }
        }
        Minecraft.getInstance().execute(() -> {
            this.candidates.addAll(found);
            this.loading = false;
            if (ClientScreens.current() == this) this.rebuildWidgets();
        });
    }

    private List<String> filteredCandidates() {
        String needle = this.query.trim().toLowerCase(Locale.ROOT);
        ArrayList<String> matches = new ArrayList<>();
        for (String candidate : this.candidates) {
            if (needle.isEmpty() || candidate.toLowerCase(Locale.ROOT).contains(needle)) matches.add(candidate);
        }
        matches.sort(Comparator
                .comparingInt((String name) -> name.toLowerCase(Locale.ROOT).startsWith(needle) ? 0 : 1)
                .thenComparing(String.CASE_INSENSITIVE_ORDER));
        return matches;
    }

    private boolean isOnline(String name) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection != null && connection.getPlayerInfoIgnoreCase(name) != null;
    }

    private void openProfile(String name) {
        if (!validName(name)) return;
        ClientScreens.show(new PlayerActionPopupScreen(this, name, this.width / 2, this.height / 2));
    }

    private static boolean validName(String name) {
        return name != null && name.trim().matches("[A-Za-z0-9_]{3,16}");
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int center = this.width / 2;
        int top = Math.max(12, this.height / 2 - 140);
        graphics.centeredText(this.font, Component.literal("Find Player"), center, top, -1);
        String status = this.loading ? "Loading CatCraft player suggestions..."
                : this.matchCount == 0 ? "Enter an exact username — offline names are supported"
                : "Showing " + Math.min(MAX_RESULTS, this.matchCount) + " of " + this.matchCount + " known players";
        graphics.centeredText(this.font, Component.literal(status), center, top + 13, 0xFFAAAAAA);
    }

    @Override
    public void onClose() {
        ClientScreens.show(this.parent);
    }
}
