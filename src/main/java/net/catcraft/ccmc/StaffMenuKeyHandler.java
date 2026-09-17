package net.catcraft.ccmc;

import com.mojang.blaze3d.platform.InputConstants;
import net.catcraft.ccmc.gui.StaffMenuScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class StaffMenuKeyHandler implements ClientTickEvents.EndTick {
    private final KeyMapping key;
    private static boolean pending;

    private StaffMenuKeyHandler(KeyMapping key) { this.key = key; }

    public static void register() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("catcraft_moderation_companion", "catcraft_staff"));
        KeyMapping mapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.ccmc.staff_menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F8, category));
        ClientTickEvents.END_CLIENT_TICK.register(new StaffMenuKeyHandler(mapping));
    }

    public static void requestOpen() { pending = true; }

    @Override public void onEndTick(Minecraft client) {
        if (pending) {
            pending = false;
            StaffMenuScreen.open();
            return;
        }
        if (key.consumeClick()) StaffMenuScreen.open();
    }
}
