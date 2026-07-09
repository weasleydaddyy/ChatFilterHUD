package com.chatfilterhud.hud;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

public class FilteredMessage {
    final Text original;
    final Text highlighted;
    final long timestamp;
    private List<OrderedText> wrappedLines;

    public FilteredMessage(Text original, Text highlighted, long timestamp) {
        this.original = original;
        this.highlighted = highlighted;
        this.timestamp = timestamp;
    }

    public List<OrderedText> getWrappedLines(TextRenderer renderer, int maxWidth) {
        if (wrappedLines == null) {
            wrappedLines = ChatMessages.breakRenderedChatMessageLines(highlighted, maxWidth, renderer);
        }
        return wrappedLines;
    }
}