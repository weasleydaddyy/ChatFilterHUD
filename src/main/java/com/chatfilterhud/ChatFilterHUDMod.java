package com.chatfilterhud;

import com.chatfilterhud.config.ChatFilterConfig;
import com.chatfilterhud.config.ModMenuIntegration;
import com.chatfilterhud.filter.WordFilter;
import com.chatfilterhud.hud.ChatFilterHud;
import com.chatfilterhud.hud.BannedWordsChatHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ChatFilterHUDMod implements ClientModInitializer {
    private static ChatFilterHUDMod instance;

    private ChatFilterConfig config;
    private WordFilter wordFilter;
    private ChatFilterHud filterHud;
    private BannedWordsChatHud bannedWordsChat;
    private KeyBinding scrollUpKey;
    private KeyBinding scrollDownKey;
    private KeyBinding openConfigKey;

    public static ChatFilterHUDMod getInstance() {
        return instance;
    }

    public ChatFilterConfig getConfig() {
        return config;
    }

    public WordFilter getWordFilter() {
        return wordFilter;
    }

    public ChatFilterHud getFilterHud() {
        return filterHud;
    }

    public BannedWordsChatHud getBannedWordsChat() {
        return bannedWordsChat;
    }

    public KeyBinding getScrollUpKey() {
        return scrollUpKey;
    }

    public KeyBinding getScrollDownKey() {
        return scrollDownKey;
    }

    public KeyBinding getOpenConfigKey() {
        return openConfigKey;
    }

    public void handleMouseButton(double mouseX, double mouseY, int button, int action) {
        if (bannedWordsChat != null) {
            bannedWordsChat.mouseButton(mouseX, mouseY, button, action);
        }
    }

    public void handleMouseMoved(double mouseX, double mouseY) {
        if (bannedWordsChat != null) {
            bannedWordsChat.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    public void onInitializeClient() {
        instance = this;

        config = new ChatFilterConfig();
        config.load();

        wordFilter = new WordFilter(config.getBannedWords());

        filterHud = new ChatFilterHud(config);
        bannedWordsChat = new BannedWordsChatHud(config);

        scrollUpKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.chatfilterhud.scroll_up",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_PAGE_UP,
                "category.chatfilterhud"
        ));

        scrollDownKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.chatfilterhud.scroll_down",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_PAGE_DOWN,
                "category.chatfilterhud"
        ));

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.chatfilterhud.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F6,
                "category.chatfilterhud"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> this.onClientTick());
        HudRenderCallback.EVENT.register((context, tickDelta) -> this.onHudRender(context));
    }

    public void onHudRender(DrawContext context) {
        if (filterHud != null) {
            filterHud.render(context);
        }
        if (bannedWordsChat != null) {
            bannedWordsChat.render(context);
        }
    }

    public void onClientTick() {
        filterHud.applyScroll();
        bannedWordsChat.applyScroll();
        while (scrollUpKey.wasPressed()) {
            filterHud.scroll(-3);
            bannedWordsChat.scroll(-3);
        }
        while (scrollDownKey.wasPressed()) {
            filterHud.scroll(3);
            bannedWordsChat.scroll(3);
        }
        while (openConfigKey.wasPressed()) {
            MinecraftClient.getInstance().setScreen(
                    new ModMenuIntegration().getConfigScreen(null)
            );
        }
    }
}