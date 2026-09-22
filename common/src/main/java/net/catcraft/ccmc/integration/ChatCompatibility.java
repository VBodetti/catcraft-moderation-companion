package net.catcraft.ccmc.integration;

import java.util.Set;
import net.catcraft.ccmc.config.CcmcConfig;
import net.catcraft.ccmc.platform.PlatformBridge;

public final class ChatCompatibility {
    private static final Set<String> EXTERNAL_CHAT_PROCESSORS = Set.of("chattools", "chatpatches", "chatplus");

    private ChatCompatibility() {
    }

    public static boolean externalChatProcessorDetected() {
        return EXTERNAL_CHAT_PROCESSORS.stream().anyMatch(PlatformBridge::isModLoaded);
    }

    public static boolean externalChatOwnsChat() {
        String mode = CcmcConfig.getString("general.ChatIntegrationMode");
        if ("ccc".equalsIgnoreCase(mode) || "ccmc".equalsIgnoreCase(mode)) {
            return false;
        }
        if ("external".equalsIgnoreCase(mode)) {
            return true;
        }
        return ChatCompatibility.externalChatProcessorDetected();
    }
}
