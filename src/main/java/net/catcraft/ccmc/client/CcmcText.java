package net.catcraft.ccmc.client;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class CcmcText {
    private CcmcText() {}
    public static MutableComponent literal(String text) { return Component.literal(text); }
}
