package net.catcraft.ccmc.mixin;

import com.mojang.blaze3d.platform.Window;
import net.catcraft.ccmc.chat.ChatHistoryStore;
import net.catcraft.ccmc.chat.ChatMessageRecord;
import net.catcraft.ccmc.client.ClientScreens;
import net.catcraft.ccmc.config.CcmcConfig;
import net.catcraft.ccmc.gui.MessageCopyScreen;
import net.catcraft.ccmc.gui.PlayerActionPopupScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class PlayerNameClickMixin {
    @Inject(method = "handleComponentClicked", at = @At("HEAD"), cancellable = true)
    private void ccmc$onComponentClicked(Style style, boolean insert, CallbackInfoReturnable<Boolean> cir) {
        if (style != null && style.getClickEvent() instanceof ClickEvent.RunCommand run
                && run.command().startsWith("/ccmc-copy ")) {
            ChatMessageRecord record = ChatHistoryStore.find(run.command().substring(11));
            if (record != null) {
                ClientScreens.show(new MessageCopyScreen(record));
                cir.setReturnValue(true);
                return;
            }
        }

        String mode = CcmcConfig.getString("catcraft.PlayerClickMode");
        if ("disabled".equalsIgnoreCase(mode)) return;
        if ("ctrl".equalsIgnoreCase(mode) && !ccmc$isCtrlDown()) return;
        if (ccmc$isShiftDown() || style == null) return;

        String player = ccmc$extractPlayerName(style);
        if (player == null) return;
        ClientScreens.show(new PlayerActionPopupScreen(
                ClientScreens.current(), player, ccmc$cursorGuiX(), ccmc$cursorGuiY()));
        cir.setReturnValue(true);
    }

    private boolean ccmc$isShiftDown() {
        long handle = Minecraft.getInstance().getWindow().handle();
        return GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    private boolean ccmc$isCtrlDown() {
        long handle = Minecraft.getInstance().getWindow().handle();
        return GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    private String ccmc$extractPlayerName(Style style) {
        if (style.getHoverEvent() instanceof HoverEvent.ShowText showText) {
            String text = showText.value().getString().trim();
            if (ccmc$isValidPlayerName(text)) return text;
        }
        if (style.getClickEvent() instanceof ClickEvent.SuggestCommand suggest) return ccmc$extractNameFromCommand(suggest.command());
        if (style.getClickEvent() instanceof ClickEvent.RunCommand run) return ccmc$extractNameFromCommand(run.command());
        return null;
    }

    private String ccmc$extractNameFromCommand(String command) {
        if (command == null) return null;
        String normalized = command.trim();
        if (normalized.isEmpty()) return null;
        if (normalized.startsWith("/")) normalized = normalized.substring(1);
        String[] parts = normalized.split("\\s+");
        if (parts.length < 2) return null;
        String root = parts[0];
        int colon = root.lastIndexOf(':');
        if (colon >= 0) root = root.substring(colon + 1);
        if (!root.equalsIgnoreCase("msg") && !root.equalsIgnoreCase("tell") && !root.equalsIgnoreCase("w")) return null;
        return ccmc$isValidPlayerName(parts[1]) ? parts[1] : null;
    }

    private boolean ccmc$isValidPlayerName(String name) { return name != null && name.matches("[A-Za-z0-9_]{3,16}"); }

    private int ccmc$cursorGuiX() {
        Window window = Minecraft.getInstance().getWindow();
        double[] x = new double[1], y = new double[1];
        GLFW.glfwGetCursorPos(window.handle(), x, y);
        return (int) (x[0] * window.getGuiScaledWidth() / window.getScreenWidth());
    }

    private int ccmc$cursorGuiY() {
        Window window = Minecraft.getInstance().getWindow();
        double[] x = new double[1], y = new double[1];
        GLFW.glfwGetCursorPos(window.handle(), x, y);
        return (int) (y[0] * window.getGuiScaledHeight() / window.getScreenHeight());
    }
}
