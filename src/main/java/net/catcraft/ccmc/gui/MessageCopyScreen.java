package net.catcraft.ccmc.gui;

import net.catcraft.ccmc.chat.CcmcTimestampService;
import net.catcraft.ccmc.chat.ChatMessageRecord;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MessageCopyScreen
extends Screen {
    private final Screen oldScreen;
    private final ChatMessageRecord unit;

    public MessageCopyScreen(ChatMessageRecord unit) {
        super((Component)Component.translatable((String)"key.ccmc.texts.copy.title"));
        this.oldScreen = Minecraft.getInstance().gui.screen();
        this.unit = unit;
    }

    protected void init() {
        super.init();
        int x = this.width / 2 - 100;
        int y = this.height / 2 - 38;
        this.addRenderableWidget(Button.builder((Component)Component.translatable((String)"key.ccmc.texts.copy.copyRaw"), b -> this.copy(false)).pos(x, y).size(200, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.translatable((String)"key.ccmc.texts.copy.copyWithNoColorCode"), b -> this.copy(true)).pos(x, y + 24).size(200, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.translatable((String)"key.ccmc.texts.copy.cancel"), b -> this.closeToPrevious()).pos(x, y + 56).size(200, 20).build());
    }

    public void onClose() {
        this.closeToPrevious();
    }

    public void closeToPrevious() {
        Minecraft.getInstance().gui.setScreen(this.oldScreen);
    }

    public void copy(boolean stripFormatting) {
        String text = this.unit.original.getString();
        if (stripFormatting) {
            text = CcmcTimestampService.stripCodes(text);
        }
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }
}
