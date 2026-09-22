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
    private CcmcTimestampService() {
    }

    public static Component apply(Component component, String string) {
        String string2 = CcmcTimestampService.formatPattern(CcmcConfig.getString("general.Timestamp.Pattern"), LocalDateTime.now());
        ChatFormatting chatFormatting = CcmcTimestampService.leadingColor(string2);
        MutableComponent mutableComponent = Component.literal((String)CcmcTimestampService.stripCodes(string2));
        if (chatFormatting != null) {
            mutableComponent.withStyle(chatFormatting);
        }
        if (CcmcConfig.getBoolean("general.Timestamp.CopyToChatBar.Enabled")) {
            Style style = mutableComponent.getStyle().withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.translatable((String)"key.ccmc.texts.copy.launch", (Object[])new Object[0]))).withClickEvent((ClickEvent)new ClickEvent.RunCommand("/ccc-copy " + string));
            mutableComponent.setStyle(style);
        }
        return Component.empty().append((Component)mutableComponent).append(component);
    }

    public static String formatPattern(String string, LocalDateTime localDateTime) {
        String string2 = string == null ? "&8[{hour}:{minute}:{second}] &r" : string;
        return string2.replace("{year}", "%04d".formatted(localDateTime.getYear())).replace("{month}", "%02d".formatted(localDateTime.getMonthValue())).replace("{day}", "%02d".formatted(localDateTime.getDayOfMonth())).replace("{hour}", "%02d".formatted(localDateTime.getHour())).replace("{minute}", "%02d".formatted(localDateTime.getMinute())).replace("{second}", "%02d".formatted(localDateTime.getSecond()));
    }

    public static String stripCodes(String string) {
        return string == null ? "" : string.replaceAll("(?i)[&\u00a7][0-9A-FK-OR]", "");
    }

    private static ChatFormatting leadingColor(String string) {
        if (string == null || string.length() < 2) {
            return null;
        }
        for (int i = 0; i < string.length() - 1; ++i) {
            char c = string.charAt(i);
            if (c != '&' && c != '\u00a7') continue;
            return switch (Character.toLowerCase(string.charAt(i + 1))) {
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
