package com.chatfilterhud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ChatFilterConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("chatfilterhud.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private List<String> bannedWords = new ArrayList<>();
    private float hudOpacity = 0.5f;
    private int maxMessages = 100;
    private int messageDuration = 10;
    private int fadeOutDuration = 2;
    private int hudWidth = 320;
    private int hudHeight = 180;
    private int hudOffsetX = 4;
    private int hudOffsetY = 4;
    private int bannedChatWidth = 320;
    private int bannedChatHeight = 180;
    private int bannedChatOffsetX = 4;
    private int bannedChatOffsetY = 190;
    private int bannedChatMaxMessages = 100;

    // Alert / notification settings
    private boolean alertEnabled = true;
    private int maxAlerts = 5;
    private int alertDuration = 4;
    private int alertMaxWidth = 300;
    private AlertPosition alertPosition = AlertPosition.TOP_RIGHT;
    private boolean fuzzyMatching = true;
    private AlertSize alertSize = AlertSize.MEDIUM;

    public enum AlertSize {
        SMALL, MEDIUM, LARGE
    }

    public enum AlertPosition {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    public void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                ChatFilterConfig loaded = GSON.fromJson(reader, ChatFilterConfig.class);
                if (loaded != null) {
                    this.bannedWords = loaded.bannedWords != null ? loaded.bannedWords : new ArrayList<>();
                    this.hudOpacity = clamp(loaded.hudOpacity, 0.0f, 1.0f);
                    this.maxMessages = Math.max(1, loaded.maxMessages);
                    this.messageDuration = Math.max(1, loaded.messageDuration);
                    this.fadeOutDuration = Math.max(0, loaded.fadeOutDuration);
                    this.hudWidth = Math.max(100, loaded.hudWidth);
                    this.hudHeight = Math.max(40, loaded.hudHeight);
                    this.hudOffsetX = Math.max(0, loaded.hudOffsetX);
                    this.hudOffsetY = Math.max(0, loaded.hudOffsetY);
                    this.bannedChatWidth = Math.max(100, loaded.bannedChatWidth);
                    this.bannedChatHeight = Math.max(40, loaded.bannedChatHeight);
                    this.bannedChatOffsetX = Math.max(0, loaded.bannedChatOffsetX);
                    this.bannedChatOffsetY = Math.max(0, loaded.bannedChatOffsetY);
                    this.bannedChatMaxMessages = Math.max(1, loaded.bannedChatMaxMessages);
                    this.alertEnabled = loaded.alertEnabled;
                    this.maxAlerts = Math.max(1, loaded.maxAlerts);
                    this.alertDuration = Math.max(1, loaded.alertDuration);
                    this.alertMaxWidth = Math.max(100, loaded.alertMaxWidth);
                    this.alertPosition = loaded.alertPosition != null ? loaded.alertPosition : AlertPosition.TOP_RIGHT;
                    this.fuzzyMatching = loaded.fuzzyMatching;
                    this.alertSize = loaded.alertSize != null ? loaded.alertSize : AlertSize.MEDIUM;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            bannedWords.add("badword");
            bannedWords.add("spam");
            save();
        }
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public List<String> getBannedWords() {
        return bannedWords;
    }

    public void setBannedWords(List<String> bannedWords) {
        this.bannedWords = bannedWords;
    }

    public float getHudOpacity() {
        return hudOpacity;
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public int getMessageDuration() {
        return messageDuration;
    }

    public int getFadeOutDuration() {
        return fadeOutDuration;
    }

    public int getHudWidth() {
        return hudWidth;
    }

    public int getHudHeight() {
        return hudHeight;
    }

    public int getHudOffsetX() {
        return hudOffsetX;
    }

    public int getHudOffsetY() {
        return hudOffsetY;
    }

    public void setHudOffsetX(int hudOffsetX) {
        this.hudOffsetX = Math.max(0, hudOffsetX);
    }

    public void setHudOffsetY(int hudOffsetY) {
        this.hudOffsetY = Math.max(0, hudOffsetY);
    }

    public int getBannedChatWidth() {
        return bannedChatWidth;
    }

    public int getBannedChatHeight() {
        return bannedChatHeight;
    }

    public int getBannedChatOffsetX() {
        return bannedChatOffsetX;
    }

    public int getBannedChatOffsetY() {
        return bannedChatOffsetY;
    }

    public int getBannedChatMaxMessages() {
        return bannedChatMaxMessages;
    }

    public void setBannedChatOffsetX(int bannedChatOffsetX) {
        this.bannedChatOffsetX = Math.max(0, bannedChatOffsetX);
    }

    public void setBannedChatOffsetY(int bannedChatOffsetY) {
        this.bannedChatOffsetY = Math.max(0, bannedChatOffsetY);
    }

    public boolean isAlertEnabled() {
        return alertEnabled;
    }

    public void setAlertEnabled(boolean alertEnabled) {
        this.alertEnabled = alertEnabled;
    }

    public int getMaxAlerts() {
        return maxAlerts;
    }

    public void setMaxAlerts(int maxAlerts) {
        this.maxAlerts = maxAlerts;
    }

    public int getAlertDuration() {
        return alertDuration;
    }

    public void setAlertDuration(int alertDuration) {
        this.alertDuration = alertDuration;
    }

    public int getAlertMaxWidth() {
        return alertMaxWidth;
    }

    public void setAlertMaxWidth(int alertMaxWidth) {
        this.alertMaxWidth = alertMaxWidth;
    }

    public AlertPosition getAlertPosition() {
        return alertPosition;
    }

    public void setAlertPosition(AlertPosition alertPosition) {
        this.alertPosition = alertPosition;
    }

    public boolean isFuzzyMatching() {
        return fuzzyMatching;
    }

    public void setFuzzyMatching(boolean fuzzyMatching) {
        this.fuzzyMatching = fuzzyMatching;
    }

    public AlertSize getAlertSize() {
        return alertSize;
    }

    public void setAlertSize(AlertSize alertSize) {
        this.alertSize = alertSize;
    }
}