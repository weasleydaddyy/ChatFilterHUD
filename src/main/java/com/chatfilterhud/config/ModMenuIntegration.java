package com.chatfilterhud.config;

import com.chatfilterhud.ChatFilterHUDMod;
import com.chatfilterhud.config.ChatFilterConfig.AlertPosition;
import com.chatfilterhud.config.ChatFilterConfig.AlertSize;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ModMenuIntegration {
    public Screen getConfigScreen(Screen parent) {
        return new ConfigScreen(parent);
    }

    private static class ConfigScreen extends GameOptionsScreen {
        private static final int TAB_LEFT = 8;
        private static final int TAB_WIDTH = 80;
        private static final int TAB_HEIGHT = 24;

        private TextFieldWidget addWordField;
        private final List<String> words = new ArrayList<>();
        private int scrollOffset = 0;
        private String tab = "words";

        // Local state for alerts tab (applied on save)
        private int sliderPosIndex = 0;
        private int sliderSizeIndex = 0;
        private int sliderDuration = 4;
        private int sliderMaxAlerts = 5;
        private int sliderMaxWidth = 300;

        protected ConfigScreen(Screen parent) {
            super(parent, null, Text.literal("ChatFilterHUD Config"));
        }

        @Override
        protected void init() {
            clearChildren();
            words.clear();
            words.addAll(ChatFilterHUDMod.getInstance().getConfig().getBannedWords());

            ChatFilterConfig cfg = ChatFilterHUDMod.getInstance().getConfig();
            sliderPosIndex = cfg.getAlertPosition().ordinal();
            sliderSizeIndex = cfg.getAlertSize().ordinal();
            sliderDuration = cfg.getAlertDuration();
            sliderMaxAlerts = cfg.getMaxAlerts();
            sliderMaxWidth = cfg.getAlertMaxWidth();

            // Tab buttons on the left, stacked vertically centered
            int tabStartY = Math.max(8, (height / 2) - ((2 * TAB_HEIGHT + 4) / 2));

            addDrawableChild(ButtonWidget.builder(
                    Text.literal("[Words]"),
                    button -> { tab = "words"; init(); }
            ).dimensions(TAB_LEFT, tabStartY, TAB_WIDTH, TAB_HEIGHT).build());

            addDrawableChild(ButtonWidget.builder(
                    Text.literal("[Alerts]"),
                    button -> { tab = "alerts"; init(); }
            ).dimensions(TAB_LEFT, tabStartY + TAB_HEIGHT + 4, TAB_WIDTH, TAB_HEIGHT).build());

            int contentLeft = TAB_LEFT + TAB_WIDTH + 12;

            if (tab.equals("words")) {
                initWordsTab(contentLeft);
            } else {
                initAlertsTab(contentLeft);
            }

            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Save & Close"),
                    button -> {
                        save();
                        client.setScreen(parent);
                    }
            ).dimensions(width / 2 - 100, height - 40, 200, 20).build());
        }

        private void save() {
            ChatFilterConfig cfg = ChatFilterHUDMod.getInstance().getConfig();
            cfg.setBannedWords(new ArrayList<>(words));

            AlertPosition[] positions = AlertPosition.values();
            if (sliderPosIndex >= 0 && sliderPosIndex < positions.length) {
                cfg.setAlertPosition(positions[sliderPosIndex]);
            }
            AlertSize[] sizes = AlertSize.values();
            if (sliderSizeIndex >= 0 && sliderSizeIndex < sizes.length) {
                cfg.setAlertSize(sizes[sliderSizeIndex]);
            }
            cfg.setAlertDuration(sliderDuration);
            cfg.setMaxAlerts(sliderMaxAlerts);
            cfg.setAlertMaxWidth(sliderMaxWidth);

            cfg.save();
            ChatFilterHUDMod.getInstance().getWordFilter().updateBannedWords(cfg.getBannedWords());
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (super.mouseClicked(mouseX, mouseY, button)) return true;
            if (tab.equals("words")) {
                return mouseClickedWords(mouseX, mouseY, button);
            }
            return false;
        }

        private void initWordsTab(int contentLeft) {
            addWordField = new TextFieldWidget(textRenderer, contentLeft, 30, 200, 20, Text.literal("Add word"));
            addWordField.setMaxLength(100);
            addDrawableChild(addWordField);

            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Add"),
                    button -> {
                        String text = addWordField.getText().trim().toLowerCase();
                        if (!text.isEmpty() && !words.contains(text)) {
                            words.add(text);
                            addWordField.setText("");
                            save();
                        }
                    }
            ).dimensions(contentLeft + 205, 29, 50, 20).build());
        }

        private void initAlertsTab(int contentLeft) {
            ChatFilterConfig cfg = ChatFilterHUDMod.getInstance().getConfig();

            // Toggle alerts on/off
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Alerts: " + (cfg.isAlertEnabled() ? "ON" : "OFF")),
                    button -> {
                        cfg.setAlertEnabled(!cfg.isAlertEnabled());
                        cfg.save();
                        button.setMessage(Text.literal("Alerts: " + (cfg.isAlertEnabled() ? "ON" : "OFF")));
                    }
            ).dimensions(contentLeft, 30, 200, 20).build());

            // Position
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("<"),
                    button -> {
                        AlertPosition[] p = AlertPosition.values();
                        sliderPosIndex = (sliderPosIndex - 1 + p.length) % p.length;
                    }
            ).dimensions(contentLeft, 55, 20, 20).build());
            addDrawableChild(ButtonWidget.builder(
                    Text.literal(">"),
                    button -> {
                        AlertPosition[] p = AlertPosition.values();
                        sliderPosIndex = (sliderPosIndex + 1) % p.length;
                    }
            ).dimensions(contentLeft + 180, 55, 20, 20).build());

            // Size
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("<"),
                    button -> {
                        AlertSize[] s = AlertSize.values();
                        sliderSizeIndex = (sliderSizeIndex - 1 + s.length) % s.length;
                    }
            ).dimensions(contentLeft, 80, 20, 20).build());
            addDrawableChild(ButtonWidget.builder(
                    Text.literal(">"),
                    button -> {
                        AlertSize[] s = AlertSize.values();
                        sliderSizeIndex = (sliderSizeIndex + 1) % s.length;
                    }
            ).dimensions(contentLeft + 180, 80, 20, 20).build());

            // Duration
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("-"),
                    button -> {
                        sliderDuration = Math.max(1, sliderDuration - 1);
                    }
            ).dimensions(contentLeft, 105, 20, 20).build());
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("+"),
                    button -> {
                        sliderDuration = Math.min(10, sliderDuration + 1);
                    }
            ).dimensions(contentLeft + 180, 105, 20, 20).build());

            // Max alerts
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("-"),
                    button -> {
                        sliderMaxAlerts = Math.max(1, sliderMaxAlerts - 1);
                    }
            ).dimensions(contentLeft, 130, 20, 20).build());
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("+"),
                    button -> {
                        sliderMaxAlerts = Math.min(10, sliderMaxAlerts + 1);
                    }
            ).dimensions(contentLeft + 180, 130, 20, 20).build());

            // Max width
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("-"),
                    button -> {
                        sliderMaxWidth = Math.max(100, sliderMaxWidth - 25);
                    }
            ).dimensions(contentLeft, 155, 20, 20).build());
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("+"),
                    button -> {
                        sliderMaxWidth = Math.min(400, sliderMaxWidth + 25);
                    }
            ).dimensions(contentLeft + 180, 155, 20, 20).build());
        }

        private boolean mouseClickedWords(double mouseX, double mouseY, int button) {
            int contentLeft = TAB_LEFT + TAB_WIDTH + 12;
            int listStartY = 75;
            int itemHeight = 25;

            for (int i = 0; i < words.size(); i++) {
                int y = listStartY + i * itemHeight;
                if (y + itemHeight > height - 60) break;
                if (y < 55) continue;

                int removeX = contentLeft + 200;
                if (mouseX >= removeX && mouseX <= removeX + 20 && mouseY >= y + 2 && mouseY <= y + 22) {
                    words.remove(i);
                    save();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);

            int contentLeft = TAB_LEFT + TAB_WIDTH + 12;

            // Highlight active tab
            int tabStartY = Math.max(8, (height / 2) - ((2 * TAB_HEIGHT + 4) / 2));
            int activeTabY = tab.equals("words") ? tabStartY : tabStartY + TAB_HEIGHT + 4;
            context.fill(TAB_LEFT, activeTabY, TAB_LEFT + TAB_WIDTH, activeTabY + TAB_HEIGHT, 0x44888888);

            if (tab.equals("words")) {
                renderWordsTab(context, contentLeft);
            } else {
                renderAlertsTab(context, contentLeft);
            }
        }

        private void renderWordsTab(DrawContext context, int contentLeft) {
            context.drawText(textRenderer, "Add new word:", contentLeft, 18, 0xAAAAAA, true);

            context.drawText(textRenderer, "Banned words (§e" + words.size() + "§r):", contentLeft, 55, 0xFFFFFF, true);

            int listStartY = 75;
            for (int i = 0; i < words.size(); i++) {
                int y = listStartY + i * 22;
                if (y + 22 > height - 65) break;

                context.fill(contentLeft, y, contentLeft + 230, y + 20, 0x44000000);
                context.drawText(textRenderer, "• " + words.get(i), contentLeft + 5, y + 5, 0xFF5555, true);
                context.drawText(textRenderer, "[§cX§r]", contentLeft + 205, y + 5, 0xFFFFFF, true);
            }
        }

        private void renderAlertsTab(DrawContext context, int contentLeft) {
            AlertPosition[] positions = AlertPosition.values();
            AlertSize[] sizes = AlertSize.values();

            context.drawText(textRenderer, "Alert / Notification Settings", contentLeft, 18, 0xFFFFFF, true);

            context.drawText(textRenderer, "Position:", contentLeft + 25, 59, 0xAAAAAA, true);
            context.drawText(textRenderer, positions[sliderPosIndex].name().replace('_', ' '), contentLeft + 100, 59, 0xFFFF55, true);

            context.drawText(textRenderer, "Size:", contentLeft + 25, 84, 0xAAAAAA, true);
            context.drawText(textRenderer, sizes[sliderSizeIndex].name(), contentLeft + 100, 84, 0xFFFF55, true);

            context.drawText(textRenderer, "Duration:", contentLeft + 25, 109, 0xAAAAAA, true);
            context.drawText(textRenderer, sliderDuration + "s", contentLeft + 100, 109, 0xFFFF55, true);

            context.drawText(textRenderer, "Max alerts:", contentLeft + 25, 134, 0xAAAAAA, true);
            context.drawText(textRenderer, String.valueOf(sliderMaxAlerts), contentLeft + 100, 134, 0xFFFF55, true);

            context.drawText(textRenderer, "Max width:", contentLeft + 25, 159, 0xAAAAAA, true);
            context.drawText(textRenderer, sliderMaxWidth + "px", contentLeft + 100, 159, 0xFFFF55, true);
        }
    }
}