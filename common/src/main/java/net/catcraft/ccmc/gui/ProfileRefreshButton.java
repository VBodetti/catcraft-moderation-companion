package net.catcraft.ccmc.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/** Small refresh icon with a tooltip and an explicit screen-reader label. */
final class ProfileRefreshButton extends Button {
    ProfileRefreshButton(int x, int y, String label, Runnable action) {
        super(x, y, 18, 18, Component.literal(label), ignored -> action.run(), DEFAULT_NARRATION);
        setTooltip(Tooltip.create(Component.literal(label)));
    }
    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX(), y = getY(), color = isHoveredOrFocused() ? 0xFFFFFFFF : 0xFFAAAAAA;
        if (isHoveredOrFocused()) graphics.fill(x, y, x + 18, y + 18, 0x88243746);
        graphics.fill(x + 5, y + 3, x + 12, y + 5, color);
        graphics.fill(x + 3, y + 5, x + 5, y + 12, color);
        graphics.fill(x + 5, y + 12, x + 12, y + 14, color);
        graphics.fill(x + 12, y + 9, x + 14, y + 12, color);
        graphics.fill(x + 12, y + 3, x + 14, y + 7, color);
        graphics.fill(x + 9, y + 6, x + 14, y + 8, color);
        if (isFocused()) graphics.outline(x, y, 18, 18, color);
    }
}
