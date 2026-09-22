package net.catcraft.ccmc.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ClientFeedback {
    private ClientFeedback() {
    }

    public static void send(Component message) {
        Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(message);
    }
}

