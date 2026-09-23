package net.catcraft.ccmc.gui;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;

public final class PlayerProfileQueryCapture {
    private static final Map<String, List<String>> responses = new LinkedHashMap<>();
    private static final ArrayDeque<Query> queue = new ArrayDeque<>();
    private static String target = "";
    private static Query current;
    private static long sentAt;
    private static long lastMessageAt;
    private static String status = "Waiting for CatCraft...";
    private PlayerProfileQueryCapture() { }

    public static synchronized void beginOverview(String player, boolean force) {
        selectTarget(player);
        if (force) {
            queue.clear(); current = null;
            responses.remove("seen"); responses.remove("playtime");
        }
        boolean added = schedule("seen", "seen " + player);
        if (!responses.containsKey("hover_playtime")) added |= schedule("playtime", "eplaytime " + player);
        if (!added) return;
        status = responses.containsKey("hover_playtime") ? "Loading /seen..." : "Loading /seen and playtime...";
        pump();
    }

    public static synchronized void observeHover(String player, Component component) {
        if (player == null || component == null) return;
        selectTarget(player);
        for (String rawLine : component.getString().split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;
            String joined = labeledValue(line, "joined date", "joined on", "first joined", "member since");
            if (joined != null) responses.put("member_since", List.of(joined));
            String playtime = labeledValue(line, "playtime", "time played");
            if (playtime != null) responses.put("hover_playtime", List.of(playtime));
        }
    }

    private static String labeledValue(String line, String... labels) {
        String lower = line.toLowerCase();
        for (String label : labels) {
            int start = lower.indexOf(label);
            if (start < 0) continue;
            String value = line.substring(start + label.length()).trim();
            while (!value.isEmpty() && (value.charAt(0) == ':' || value.charAt(0) == '-' || value.charAt(0) == '—')) {
                value = value.substring(1).trim();
            }
            if (!value.isEmpty()) return value;
        }
        return null;
    }

    public static synchronized void beginSingle(String player, String key, String command, boolean force) {
        selectTarget(player);
        if (force) {
            queue.clear(); current = null;
            responses.remove(key);
        }
        if (!schedule(key, command)) return;
        status = "Loading /" + command + "...";
        pump();
    }

    private static void selectTarget(String player) {
        if (!player.equalsIgnoreCase(target)) {
            target = player;
            responses.clear(); queue.clear(); current = null;
        }
    }

    private static boolean schedule(String key, String command) {
        if (responses.containsKey(key)) return false;
        if (current != null && current.key.equals(key)) return false;
        for (Query pending : queue) if (pending.key.equals(key)) return false;
        queue.add(new Query(key, command));
        return true;
    }

    public static synchronized void observe(Component component) {
        if (current == null || component == null) return;
        long now = System.currentTimeMillis();
        if (now - sentAt > 5000L) return;
        String text = component.getString();
        if (text == null || (text = text.trim()).isBlank()) return;
        if (!matchesCurrentResponse(text)) return;
        List<String> lines = responses.computeIfAbsent(current.key, ignored -> new ArrayList<>());
        if (lines.size() < 128) lines.add(text);
        lastMessageAt = now;
        status = "Live CatCraft response";
    }

    public static synchronized void pump() {
        long now = System.currentTimeMillis();
        if (current != null) {
            boolean received = responses.containsKey(current.key) && !responses.get(current.key).isEmpty();
            boolean timedOut = now - sentAt >= 2600L;
            if ((received && now - lastMessageAt >= 800L) || timedOut) {
                if (timedOut && !received) status = "No matching server response detected.";
                current = null;
            }
        }
        if (current != null || queue.isEmpty()) return;
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) { status = "Not connected to CatCraft."; queue.clear(); return; }
        current = queue.removeFirst();
        sentAt = now; lastMessageAt = now;
        connection.sendCommand(current.command);
    }

    public static synchronized List<String> lines(String key) {
        List<String> lines = responses.get(key);
        return lines == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(lines));
    }
    public static synchronized String value(String key) {
        List<String> lines = responses.get(key);
        return lines == null || lines.isEmpty() ? null : lines.get(0);
    }
    public static synchronized List<String> combinedOverview() {
        ArrayList<String> combined = new ArrayList<>();
        combined.addAll(lines("seen")); combined.addAll(lines("playtime"));
        return Collections.unmodifiableList(combined);
    }
    public static synchronized String status() { return status; }

    private static boolean matchesCurrentResponse(String text) {
        String lower = text.toLowerCase();
        String player = target.toLowerCase();
        return switch (current.key) {
            case "seen" -> (lower.contains(player) && (lower.contains("online since") || lower.contains("last seen")
                    || lower.contains("currently online") || lower.contains("never joined") || lower.contains("offline")))
                    || lower.contains("has also been known as");
            case "playtime" -> lower.contains("playtime") && (lower.contains(player) || lower.contains("playtime of"));
            case "history" -> lower.contains("no history") || lower.contains("no punish") || lower.contains("litebans")
                    || (lower.contains(player) && (lower.contains("history") || lower.contains("warn") || lower.contains("mute")
                    || lower.contains("ban") || lower.contains("kick") || lower.contains("jail") || lower.contains("punish")));
            case "coreprotect" -> lower.contains("coreprotect") || lower.contains("no results")
                    || (lower.contains(player) && (lower.contains("placed") || lower.contains("broke") || lower.contains("removed")
                    || lower.contains("added") || lower.contains("container") || lower.contains("picked up") || lower.contains("dropped")));
            case "anticheat" -> lower.contains("vulcan") || lower.contains("violation") || lower.contains(" cps")
                    || (lower.contains(player) && lower.contains("profile"));
            case "trade" -> lower.contains("no trade") || lower.contains("trade log") || lower.contains("traded")
                    || (lower.contains(player) && lower.contains("trade"));
            default -> false;
        };
    }

    private record Query(String key, String command) { }
}
