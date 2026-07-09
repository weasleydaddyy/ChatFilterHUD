package com.chatfilterhud.hud;

import com.chatfilterhud.config.ChatFilterConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BannedWordsChatHud {
    private static final int BACKGROUND_COLOR = 0xAA000000;
    private static final int LINE_HEIGHT = 9;
    private static final int VERTICAL_PADDING = 2;
    private static final int HORIZONTAL_PADDING = 4;
    private static final int SCROLLBAR_WIDTH = 2;

    private final MinecraftClient client;
    private final ChatFilterConfig config;
    private final List<FilteredMessage> messages = new ArrayList<>();
    private int scrollOffset = 0;
    private int scrollAccumulator = 0;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;
    private int lastMouseX;
    private int lastMouseY;

    public BannedWordsChatHud(ChatFilterConfig config) {
        this.client = MinecraftClient.getInstance();
        this.config = config;
    }

    public void addMessage(Text original, Text highlighted) {
        messages.add(0, new FilteredMessage(original, highlighted, System.currentTimeMillis()));
        while (messages.size() > config.getBannedChatMaxMessages()) {
            messages.remove(messages.size() - 1);
        }
    }

    public void scroll(int amount) {
        int totalLines = getTotalLineCount();
        int visibleCount = (config.getBannedChatHeight() - VERTICAL_PADDING * 2) / (LINE_HEIGHT + 1);
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

    public boolean mouseButton(double mouseX, double mouseY, int button, int action) {
        int x = config.getBannedChatOffsetX();
        int y = client.getWindow().getScaledHeight() - config.getBannedChatHeight() - config.getBannedChatOffsetY();

        if (button == 0 && action == 1 && mouseX >= x && mouseX <= x + config.getBannedChatWidth()
                && mouseY >= y && mouseY <= y + config.getBannedChatHeight()) {
            dragging = true;
            dragOffsetX = (int) mouseX - x;
            dragOffsetY = (int) mouseY - y;
            lastMouseX = (int) mouseX;
            lastMouseY = (int) mouseY;
            return true;
        }

        if (button == 0 && action == 0 && dragging) {
            dragging = false;
            config.save();
            return true;
        }

        return false;
    }

    public void mouseMoved(double mouseX, double mouseY) {
        if (!dragging) return;

        int screenHeight = client.getWindow().getScaledHeight();
        int newX = Math.max(0, (int) mouseX - dragOffsetX);
        int newY = Math.max(0, (int) mouseY - dragOffsetY);
        config.setBannedChatOffsetX(newX);
        config.setBannedChatOffsetY(Math.max(0, screenHeight - config.getBannedChatHeight() - newY));
    }

    private int getTotalLineCount() {
        int total = 0;
        int maxWidth = config.getBannedChatWidth() - HORIZONTAL_PADDING * 2 - SCROLLBAR_WIDTH;
        for (FilteredMessage message : messages) {
            total += message.getWrappedLines(client.textRenderer, maxWidth).size();
        }
        return total;
    }

    public void render(DrawContext context) {
        if (messages.isEmpty()) return;

        TextRenderer textRenderer = client.textRenderer;
        int hudWidth = config.getBannedChatWidth();
        int hudHeight = config.getBannedChatHeight();
        int x = config.getBannedChatOffsetX();
        int y = client.getWindow().getScaledHeight() - hudHeight - config.getBannedChatOffsetY();
        int maxWidth = hudWidth - HORIZONTAL_PADDING * 2 - SCROLLBAR_WIDTH;

        List<OrderedText> allLines = new ArrayList<>();
        for (FilteredMessage message : messages) {
            allLines.addAll(message.getWrappedLines(textRenderer, maxWidth));
        }

        int visibleCount = (hudHeight - VERTICAL_PADDING * 2) / (LINE_HEIGHT + 1);
        int maxScroll = Math.max(0, allLines.size() - visibleCount);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        int startIdx = Math.max(0, allLines.size() - visibleCount - scrollOffset);
        int endIdx = Math.min(allLines.size(), startIdx + visibleCount);
        if (startIdx >= endIdx) return;

        context.fill(x, y, x + hudWidth, y + hudHeight, BACKGROUND_COLOR);

        int drawY = y + hudHeight - VERTICAL_PADDING - LINE_HEIGHT;
        for (int i = startIdx; i < endIdx; i++) {
            context.drawText(textRenderer, allLines.get(i), x + HORIZONTAL_PADDING, drawY, 0xFFFFFFFF, true);
            drawY -= LINE_HEIGHT + 1;
        }

        if (allLines.size() > visibleCount) {
            int scrollbarHeight = Math.max(10, (int) ((float) visibleCount / allLines.size() * hudHeight));
            int scrollbarY = y + (int) ((float) scrollOffset / (allLines.size() - visibleCount)
                    * (hudHeight - scrollbarHeight));
            context.fill(x + hudWidth - SCROLLBAR_WIDTH, scrollbarY, x + hudWidth,
                    scrollbarY + scrollbarHeight, 0x88FFFFFF);
        }
    }
}