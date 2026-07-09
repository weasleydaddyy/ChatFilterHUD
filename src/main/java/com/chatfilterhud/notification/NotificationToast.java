package com.chatfilterhud.notification;

import com.chatfilterhud.config.ChatFilterConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.LinkedList;

public class NotificationToast {
    private static final long ANIMATION_MS = 200;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final ChatFilterConfig config;
    private final LinkedList<ToastEntry> entries = new LinkedList<>();

    public NotificationToast(ChatFilterConfig config) {
        this.config = config;
    }

    public void show(Text message) {
        if (!config.isAlertEnabled()) return;
        entries.addFirst(new ToastEntry(message, System.currentTimeMillis()));
        while (entries.size() > config.getMaxAlerts()) {
            entries.removeLast();
        }
    }

    public void render(DrawContext context) {
        if (entries.isEmpty()) return;

        long now = System.currentTimeMillis();
        TextRenderer textRenderer = client.textRenderer;
        int scw = client.getWindow().getScaledWidth();
        int sch = client.getWindow().getScaledHeight();

        entries.removeIf(e -> (now - e.time) > config.getAlertDuration() * 1000L + ANIMATION_MS);

        int toastHeight = getToastHeight();
        int yStart = switch (config.getAlertPosition()) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 8;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> sch - 8 - entries.size() * (toastHeight + 4);
        };

        int y = yStart;
        for (ToastEntry entry : entries) {
            long age = now - entry.time;
            float opacity;
            if (age < ANIMATION_MS) {
                opacity = (float) age / ANIMATION_MS;
            } else if (age > config.getAlertDuration() * 1000L) {
                long fadeAge = age - config.getAlertDuration() * 1000L;
                opacity = 1.0f - (float) fadeAge / ANIMATION_MS;
            } else {
                opacity = 1.0f;
            }
            if (opacity <= 0.005f) continue;

            int padding = getPadding();
            int textWidth = textRenderer.getWidth(entry.text);
            int toastWidth = Math.min(textWidth + padding * 2, config.getAlertMaxWidth());

            int toastX = switch (config.getAlertPosition()) {
                case TOP_LEFT, BOTTOM_LEFT -> 4;
                case TOP_CENTER, BOTTOM_CENTER -> (scw - toastWidth) / 2;
                case TOP_RIGHT, BOTTOM_RIGHT -> scw - toastWidth - 4;
            };
            int toastY = y;

            int alpha = Math.min(200, Math.max(4, (int) (opacity * 200)));
            int bgColor = (alpha << 24) | 0x44000000;
            int borderColor = (alpha << 24) | 0xCC5500;

            context.fill(toastX, toastY, toastX + toastWidth, toastY + toastHeight, bgColor);
            context.fill(toastX, toastY, toastX + toastWidth, toastY + 1, borderColor);

            Text displayText = Text.literal("\u26A0 ").append(entry.text);
            context.drawText(textRenderer, displayText,
                    toastX + padding, toastY + (toastHeight - textRenderer.fontHeight) / 2,
                    0xFFDD44 | (alpha << 24), false);

            y += toastHeight + 4;
        }
    }

    private int getToastHeight() {
        return switch (config.getAlertSize()) {
            case SMALL -> 16;
            case MEDIUM -> 20;
            case LARGE -> 26;
        };
    }

    private int getPadding() {
        return switch (config.getAlertSize()) {
            case SMALL -> 4;
            case MEDIUM -> 6;
            case LARGE -> 8;
        };
    }

    private static class ToastEntry {
        final Text text;
        final long time;

        ToastEntry(Text text, long time) {
            this.text = text;
            this.time = time;
        }
    }
}