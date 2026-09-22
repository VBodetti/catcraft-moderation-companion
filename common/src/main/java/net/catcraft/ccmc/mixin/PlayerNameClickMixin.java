package net.catcraft.ccmc.mixin;

import com.mojang.blaze3d.platform.Window;
import net.catcraft.ccmc.chat.ChatHistoryStore;
import net.catcraft.ccmc.chat.ChatMessageRecord;
import net.catcraft.ccmc.client.ClientScreens;
import net.catcraft.ccmc.config.CcmcConfig;
import net.catcraft.ccmc.gui.MessageCopyScreen;
import net.catcraft.ccmc.gui.PlayerActionPopupScreen;
import net.catcraft.ccmc.gui.PlayerProfileQueryCapture;
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

@Mixin(value={ChatScreen.class})
public class PlayerNameClickMixin {
    @Inject(method={"handleComponentClicked"}, at={@At(value="HEAD")}, cancellable=true)
    private void ccmc$onComponentClicked(Style style, boolean insert, CallbackInfoReturnable<Boolean> cir) {
        ChatMessageRecord record;
        ClickEvent.RunCommand run;
        ClickEvent clickEvent;
        if (style != null && (clickEvent = style.getClickEvent()) instanceof ClickEvent.RunCommand && (run = (ClickEvent.RunCommand)clickEvent).command().startsWith("/ccc-copy ") && (record = ChatHistoryStore.find(run.command().substring(10))) != null) {
            ClientScreens.show(new MessageCopyScreen(record));
            cir.setReturnValue(true);
            return;
        }
        String mode = CcmcConfig.getString("catcraft.PlayerClickMode");
        if ("disabled".equalsIgnoreCase(mode)) {
            return;
        }
        if ("ctrl".equalsIgnoreCase(mode) && !this.ccmc$isCtrlDown()) {
            return;
        }
        if (this.ccmc$isShiftDown() || style == null) {
            return;
        }
        String player = this.ccmc$extractPlayerName(style);
        if (player == null) {
            return;
        }
        HoverEvent hover = style.getHoverEvent();
        if (hover instanceof HoverEvent.ShowText showText) {
            PlayerProfileQueryCapture.observeHover(player, showText.value());
        }
        ClientScreens.show(new PlayerActionPopupScreen(ClientScreens.current(), player, this.ccmc$cursorGuiX(), this.ccmc$cursorGuiY()));
        cir.setReturnValue(true);
    }

    private boolean ccmc$isShiftDown() {
        long handle = Minecraft.getInstance().getWindow().handle();
        return GLFW.glfwGetKey((long)handle, (int)340) == 1 || GLFW.glfwGetKey((long)handle, (int)344) == 1;
    }

    private boolean ccmc$isCtrlDown() {
        long handle = Minecraft.getInstance().getWindow().handle();
        return GLFW.glfwGetKey((long)handle, (int)341) == 1 || GLFW.glfwGetKey((long)handle, (int)345) == 1;
    }

    private String ccmc$extractPlayerName(Style style) {
        HoverEvent.ShowText showText;
        String text;
        HoverEvent hoverEvent = style.getHoverEvent();
        if (hoverEvent instanceof HoverEvent.ShowText && this.ccmc$isValidPlayerName(text = (showText = (HoverEvent.ShowText)hoverEvent).value().getString().trim())) {
            return text;
        }
        ClickEvent clickEvent = style.getClickEvent();
        if (clickEvent instanceof ClickEvent.SuggestCommand) {
            ClickEvent.SuggestCommand suggest = (ClickEvent.SuggestCommand)clickEvent;
            return this.ccmc$extractNameFromCommand(suggest.command());
        }
        if (clickEvent instanceof ClickEvent.RunCommand) {
            ClickEvent.RunCommand run = (ClickEvent.RunCommand)clickEvent;
            return this.ccmc$extractNameFromCommand(run.command());
        }
        return null;
    }

    private String ccmc$extractNameFromCommand(String command) {
        String[] parts;
        if (command == null) {
            return null;
        }
        String normalized = command.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if ((parts = normalized.split("\\s+")).length < 2) {
            return null;
        }
        String root = parts[0];
        int colon = root.lastIndexOf(58);
        if (colon >= 0) {
            root = root.substring(colon + 1);
        }
        if (!(root.equalsIgnoreCase("msg") || root.equalsIgnoreCase("tell") || root.equalsIgnoreCase("w"))) {
            return null;
        }
        return this.ccmc$isValidPlayerName(parts[1]) ? parts[1] : null;
    }

    private boolean ccmc$isValidPlayerName(String name) {
        return name != null && name.matches("[A-Za-z0-9_]{3,16}");
    }

    private int ccmc$cursorGuiX() {
        Window window = Minecraft.getInstance().getWindow();
        double[] x = new double[1];
        double[] y = new double[1];
        GLFW.glfwGetCursorPos((long)window.handle(), (double[])x, (double[])y);
        return (int)(x[0] * (double)window.getGuiScaledWidth() / (double)window.getScreenWidth());
    }

    private int ccmc$cursorGuiY() {
        Window window = Minecraft.getInstance().getWindow();
        double[] x = new double[1];
        double[] y = new double[1];
        GLFW.glfwGetCursorPos((long)window.handle(), (double[])x, (double[])y);
        return (int)(y[0] * (double)window.getGuiScaledHeight() / (double)window.getScreenHeight());
    }
}
