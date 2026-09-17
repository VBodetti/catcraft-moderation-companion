package net.catcraft.ccmc.gui;

import net.catcraft.ccmc.chat.CcmcTimestampService;
import net.catcraft.ccmc.chat.ChatMessageRecord;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MessageCopyScreen extends Screen {
    private final Screen oldScreen;
    private final ChatMessageRecord unit;

    public MessageCopyScreen(ChatMessageRecord unit) {
        super(Component.translatable("key.ccmc.texts.copy.title"));
        this.oldScreen = Minecraft.getInstance().gui.screen();
        this.unit = unit;
    }

    @Override protected void init() {
        super.init();
        int x = width / 2 - 100;
        int y = height / 2 - 38;
        addRenderableWidget(Button.builder(Component.translatable("key.ccmc.texts.copy.copyRaw"), b -> copy(false)).pos(x, y).size(200, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("key.ccmc.texts.copy.copyWithNoColorCode"), b -> copy(true)).pos(x, y + 24).size(200, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("key.ccmc.texts.copy.cancel"), b -> closeToPrevious()).pos(x, y + 56).size(200, 20).build());
    }

    @Override public void onClose() { closeToPrevious(); }
    public void closeToPrevious() { Minecraft.getInstance().gui.setScreen(oldScreen); }

    public void copy(boolean stripFormatting) {
        String text = unit.original.getString();
        if (stripFormatting) text = CcmcTimestampService.stripCodes(text);
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }
}
