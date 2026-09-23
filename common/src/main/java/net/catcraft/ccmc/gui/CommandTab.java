package net.catcraft.ccmc.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Slim, keyboard-accessible workspace tab, independent of button textures. */
final class CommandTab extends Button {
    private final boolean selected;
    CommandTab(int x, int y, int width, String label, boolean selected, Runnable action) {
        super(x, y, width, 16, Component.literal(label), button -> action.run(), DEFAULT_NARRATION);
        this.selected = selected;
    }
    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX(), y = getY(), width = getWidth();
        graphics.fill(x, y, x + width, y + 16, selected ? 0xDD243746 : 0x88202020);
        graphics.fill(x, y + 14, x + width, y + 16, selected ? 0xFF55CCEE : 0xFF555555);
        graphics.centeredText(Minecraft.getInstance().font, getMessage(), x + width / 2, y + 3,
                selected || isHoveredOrFocused() ? 0xFFFFFFFF : 0xFFAAAAAA);
        if (isFocused()) graphics.outline(x + 1, y + 1, width - 2, 13, 0xFFCCCCCC);
    }
}
