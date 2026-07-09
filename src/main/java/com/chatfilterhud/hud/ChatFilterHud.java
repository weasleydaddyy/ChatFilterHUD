package com.chatfilterhud.hud;

import com.chatfilterhud.config.ChatFilterConfig;
import com.chatfilterhud.notification.NotificationToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ChatFilterHud {
    private static final int BACKGROUND_COLOR = 0x88000000;
    private static final int LINE_HEIGHT = 9;
    private static final int VERTICAL_PADDING = 2;
    private static final int HORIZONTAL_PADDING = 4;
    private static final int SCROLLBAR_WIDTH = 2;

    private final MinecraftClient client;
    private final ChatFilterConfig config;
    private NotificationToast notificationToast;
    private final List<FilteredMessage> messages = new ArrayList<>();
    private int scrollOffset = 0;
    private int scrollAccumulator = 0;

    public ChatFilterHud(ChatFilterConfig config) {
        this.client = MinecraftClient.getInstance();
        this.config = config;
        this.notificationToast = new NotificationToast(config);
    }

    public NotificationToast getNotificationToast() {
        return notificationToast;
    }

    public void addMessage(Text original, Text highlighted) {
        messages.add(0, new FilteredMessage(original, highlighted, System.currentTimeMillis()));
        while (messages.size() > config.getMaxMessages()) {
            messages.remove(messages.size() - 1);
        }
    }

    public void scroll(int amount) {
        int totalLines = getTotalLineCount();
        int visibleCount = (config.getHudHeight() - VERTICAL_PADDING * 2) / (LINE_HEIGHT + 1);
        int maxScroll = Math.max(0, totalLines - visibleCount);
        scrollOffset = Math.max(0, Math.min(scrollOffset + amount, maxScroll));
    }

    public void addScroll(int amount) {
        scrollAccumulator += amount;
    }

    public void applyScroll() {
        if (scrollAccumulator != 0) {
            scroll(scrollAccumulator);
            scrollAccumulator = 0;
        }
    }

    private int getTotalLineCount() {
        int total = 0;
        int maxWidth = config.getHudWidth() - HORIZONTAL_PADDING * 2 - SCROLLBAR_WIDTH;
        for (FilteredMessage msg : messages) {
            total += msg.getWrappedLines(client.textRenderer, maxWidth).size();
        }
        return total;
    }

    public void render(DrawContext context) {
        notificationToast.render(context);
        if (messages.isEmpty()) return;

        long now = System.currentTimeMillis();
        TextRenderer textRenderer = client.textRenderer;
        int hudWidth = config.getHudWidth();
        int hudHeight = config.getHudHeight();
        int x = config.getHudOffsetX();
        int y = client.getWindow().getScaledHeight() - hudHeight - config.getHudOffsetY();
        int maxWidth = hudWidth - HORIZONTAL_PADDING * 2 - SCROLLBAR_WIDTH;

        int totalDuration = (config.getMessageDuration() + config.getFadeOutDuration()) * 1000;
        messages.removeIf(msg -> (now - msg.timestamp) > totalDuration);

        List<LineEntry> allLines = new ArrayList<>();
        for (FilteredMessage msg : messages) {
            float opacity = getMessageOpacity(now - msg.timestamp);
            if (opacity <= 0.005f) continue;

            List<OrderedText> wrapped = msg.getWrappedLines(textRenderer, maxWidth);
            for (OrderedText line : wrapped) {
                allLines.add(new LineEntry(line, opacity));
            }
        }

        int totalLines = allLines.size();
        int visibleCount = (hudHeight - VERTICAL_PADDING * 2) / (LINE_HEIGHT + 1);
        int maxScroll = Math.max(0, totalLines - visibleCount);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        int startIdx = Math.max(0, totalLines - visibleCount - scrollOffset);
        int endIdx = Math.min(totalLines, startIdx + visibleCount);

        if (startIdx >= endIdx) return;

        context.fill(x, y, x + hudWidth, y + hudHeight, BACKGROUND_COLOR);

        int drawY = y + hudHeight - VERTICAL_PADDING - LINE_HEIGHT;
        for (int i = startIdx; i < endIdx; i++) {
            LineEntry entry = allLines.get(i);
            int alpha = Math.min(255, Math.max(4, (int)(entry.opacity * 255)));
            int color = 0xFFFFFF | (alpha << 24);
            context.drawText(textRenderer, entry.text, x + HORIZONTAL_PADDING, drawY, color, true);
            drawY -= LINE_HEIGHT + 1;
        }

        if (totalLines > visibleCount) {
            int scrollbarHeight = Math.max(10, (int)((float)visibleCount / totalLines * hudHeight));
            int scrollbarY = y + (int)((float)scrollOffset / (totalLines - visibleCount) * (hudHeight - scrollbarHeight));
            context.fill(x + hudWidth - SCROLLBAR_WIDTH, scrollbarY, x + hudWidth, scrollbarY + scrollbarHeight, 0x88FFFFFF);
        }
    }

    private float getMessageOpacity(long ageMs) {
        int fadeDuration = config.getFadeOutDuration() * 1000;
        int messageDuration = config.getMessageDuration() * 1000;
        long totalDuration = messageDuration + fadeDuration;

        if (ageMs <= messageDuration) {
            return 1.0f;
        } else if (ageMs >= totalDuration) {
            return 0.0f;
        } else {
            return 1.0f - (float)(ageMs - messageDuration) / fadeDuration;
        }
    }

    private static class LineEntry {
        final OrderedText text;
        final float opacity;

        LineEntry(OrderedText text, float opacity) {
            this.text = text;
            this.opacity = opacity;
        }
    }
}