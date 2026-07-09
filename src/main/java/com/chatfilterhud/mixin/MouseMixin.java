package com.chatfilterhud.mixin;

import com.chatfilterhud.ChatFilterHUDMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        ChatFilterHUDMod mod = ChatFilterHUDMod.getInstance();
        if (mod != null && MinecraftClient.getInstance().currentScreen == null) {
            int scroll = (int) -vertical;
            if (scroll != 0) {
                mod.getFilterHud().addScroll(scroll);
                mod.getBannedWordsChat().addScroll(scroll);
            }
        }
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        ChatFilterHUDMod mod = ChatFilterHUDMod.getInstance();
        if (mod != null && MinecraftClient.getInstance().currentScreen == null) {
            mod.getBannedWordsChat().mouseButton(
                    MinecraftClient.getInstance().mouse.getX(),
                    MinecraftClient.getInstance().mouse.getY(),
                    button,
                    action
            );
        }
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"))
    private void onCursorPos(long window, double x, double y, CallbackInfo ci) {
        ChatFilterHUDMod mod = ChatFilterHUDMod.getInstance();
        if (mod != null && MinecraftClient.getInstance().currentScreen == null) {
            mod.getBannedWordsChat().mouseMoved(x, y);
        }
    }
}