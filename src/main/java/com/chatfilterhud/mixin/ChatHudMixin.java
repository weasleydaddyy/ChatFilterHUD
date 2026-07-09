package com.chatfilterhud.mixin;

import com.chatfilterhud.ChatFilterHUDMod;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
    private void onAddMessage(Text message, CallbackInfo ci) {
        ChatFilterHUDMod mod = ChatFilterHUDMod.getInstance();
        if (mod == null) return;
        if (mod.getWordFilter() != null && mod.getWordFilter().containsBannedWord(message.getString())) {
            Text highlighted = mod.getWordFilter().highlightBannedWords(message);
            mod.getFilterHud().addMessage(message, highlighted);
            mod.getBannedWordsChat().addMessage(message, highlighted);
            mod.getFilterHud().getNotificationToast().show(message);
            ci.cancel();
        }
    }
}