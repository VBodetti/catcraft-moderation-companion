package net.catcraft.ccmc.chat;

import java.time.LocalDateTime;
import net.catcraft.ccmc.config.CcmcConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class CcmcTimestampService {
    private CcmcTimestampService() {}

    public static Component apply(Component message, String id) {
        String pattern = formatPattern(CcmcConfig.getString("general.Timestamp.Pattern"), LocalDateTime.now());
        ChatFormatting color = leadingColor(pattern);
        MutableComponent timestamp = Component.literal(stripCodes(pattern));
        if (color != null) timestamp.withStyle(color);
        if (CcmcConfig.getBoolean("general.Timestamp.CopyToChatBar.Enabled")) {
            Style style = timestamp.getStyle()
                    .withHoverEvent(new HoverEvent.ShowText(Component.translatable("key.ccmc.texts.copy.launch")))
                    .withClickEvent(new ClickEvent.RunCommand("/ccmc-copy " + id));
            timestamp.setStyle(style);
        }
        return timestamp.append(message);
    }

    public static String formatPattern(String pattern, LocalDateTime time) {
        String p = pattern == null ? "&8[{hour}:{minute}:{second}] &r" : pattern;
        return p.replace("{year}", "%04d".formatted(time.getYear()))
                .replace("{month}", "%02d".formatted(time.getMonthValue()))
                .replace("{day}", "%02d".formatted(time.getDayOfMonth()))
                .replace("{hour}", "%02d".formatted(time.getHour()))
                .replace("{minute}", "%02d".formatted(time.getMinute()))
                .replace("{second}", "%02d".formatted(time.getSecond()));
    }

    public static String stripCodes(String value) {
        return value == null ? "" : value.replaceAll("(?i)[&§][0-9A-FK-OR]", "");
    }

    private static ChatFormatting leadingColor(String value) {
        if (value == null || value.length() < 2) return null;
        for (int i = 0; i < value.length() - 1; i++) {
            char prefix = value.charAt(i);
            if (prefix != '&' && prefix != '§') continue;
            return switch (Character.toLowerCase(value.charAt(i + 1))) {
                case '0' -> ChatFormatting.BLACK;
                case '1' -> ChatFormatting.DARK_BLUE;
                case '2' -> ChatFormatting.DARK_GREEN;
                case '3' -> ChatFormatting.DARK_AQUA;
                case '4' -> ChatFormatting.DARK_RED;
                case '5' -> ChatFormatting.DARK_PURPLE;
                case '6' -> ChatFormatting.GOLD;
                case '7' -> ChatFormatting.GRAY;
                case '8' -> ChatFormatting.DARK_GRAY;
                case '9' -> ChatFormatting.BLUE;
                case 'a' -> ChatFormatting.GREEN;
                case 'b' -> ChatFormatting.AQUA;
                case 'c' -> ChatFormatting.RED;
                case 'd' -> ChatFormatting.LIGHT_PURPLE;
                case 'e' -> ChatFormatting.YELLOW;
                case 'f' -> ChatFormatting.WHITE;
                default -> null;
            };
        }
        return null;
    }
}
