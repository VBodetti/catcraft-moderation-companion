package net.catcraft.ccmc.chat;

import net.minecraft.network.chat.Component;

public final class ChatMessageRecord {
    public final Component original;
    public final Component rendered;
    public final long receivedEpochSecond;
    public final int repeatCount;
    public final boolean outsideServerChat;

    public ChatMessageRecord(Component original, Component rendered, long receivedEpochSecond, int repeatCount, boolean outsideServerChat) {
        this.original = original;
        this.rendered = rendered;
        this.receivedEpochSecond = receivedEpochSecond;
        this.repeatCount = repeatCount;
        this.outsideServerChat = outsideServerChat;
    }
}
