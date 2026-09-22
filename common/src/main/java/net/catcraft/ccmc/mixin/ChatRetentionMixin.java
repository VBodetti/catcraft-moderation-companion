package net.catcraft.ccmc.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.time.Instant;
import net.catcraft.ccmc.chat.CcmcTimestampService;
import net.catcraft.ccmc.chat.ChatHistoryStore;
import net.catcraft.ccmc.chat.ChatMessageRecord;
import net.catcraft.ccmc.config.CcmcConfig;
import net.catcraft.ccmc.integration.ChatCompatibility;
import net.catcraft.ccmc.gui.PlayerProfileQueryCapture;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={ChatComponent.class})
public abstract class ChatRetentionMixin {
    @ModifyExpressionValue(method={"addMessageToQueue", "addMessageToDisplayQueue", "addMessage*", "addRecentChat"}, at={@At(value="CONSTANT", args={"intValue=100"})})
    public int ccmc$storedLineLimit(int original) {
        return ChatCompatibility.externalChatOwnsChat() ? original : CcmcConfig.getInt("general.StoredChatLines");
    }

    @ModifyArgs(method={"addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/chat/GuiMessage;<init>(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V"))
    public void ccmc$decorateAndRemember(Args args) {
        Component original = (Component)args.get(1);
        PlayerProfileQueryCapture.observe(original);
        if (ChatCompatibility.externalChatOwnsChat()) {
            return;
        }
        String id = ChatHistoryStore.newId(original);
        Component rendered = CcmcConfig.getBoolean("general.Timestamp.Enabled") ? CcmcTimestampService.apply(original, id) : original;
        ChatHistoryStore.remember(id, new ChatMessageRecord(original, rendered, Instant.now().getEpochSecond(), 1, false));
        args.set(1, (Object)rendered);
    }
}
