package net.catcraft.ccmc.gui;

import net.catcraft.ccmc.client.ClientScreens;
import net.minecraft.client.gui.screens.ChatScreen;

public final class ChatInputPrefill {
    private ChatInputPrefill() {}
    public static boolean prefill(Object ignored, String text) {
        if (text == null) return false;
        try {
            ClientScreens.show(new ChatScreen(text, false));
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
