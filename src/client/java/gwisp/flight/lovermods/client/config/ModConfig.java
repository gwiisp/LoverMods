package gwisp.flight.lovermods.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "lovermods.json");

    private boolean netherwartHighlightEnabled = true;
    private boolean highlightAlongZ = true;
    private int highlightColor = 0xFF0000;
    private int scanRange = 100;
    private int minRowWidth = 7;

    private boolean skinPricesEnabled = true;
    private boolean showDemand = true;
    private boolean showSeason = true;
    private boolean showSet = true;

    private boolean itemFrameSkinPricesEnabled = true;
    private boolean itemFrameRequireSneak = true;
    private boolean itemFrameShowDemand = true;
    private boolean itemFrameShowSeason = true;
    private boolean itemFrameShowSet = true;

    private List<String> dungeonPartyMembers = new ArrayList<>();

    private boolean autoGgEnabled = true;
    private String rankupMessage = "GG";

    private boolean updateCheckerEnabled = true;

    private boolean tradeHudEnabled = true;

    private boolean maxedItemHighlightEnabled = true;
    private int maxedItemHighlightColor = 0x8800FF00;
    public boolean isNetherwartHighlightEnabled() {
        return netherwartHighlightEnabled;
    }

    public void setNetherwartHighlightEnabled(boolean enabled) {
        this.netherwartHighlightEnabled = enabled;
    }

    public boolean isHighlightAlongZ() {
        return highlightAlongZ;
    }

    public void setHighlightAlongZ(boolean alongZ) {
        this.highlightAlongZ = alongZ;
    }

    public int getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(int color) {
        this.highlightColor = color;
    }

    public int getScanRange() {
        return scanRange;
    }

    public void setScanRange(int range) {
        this.scanRange = Math.max(10, Math.min(200, range));
    }

    public int getMinRowWidth() {
        return minRowWidth;
    }

    public void setMinRowWidth(int width) {
        this.minRowWidth = Math.max(3, Math.min(15, width));
    }

    public boolean isSkinPricesEnabled() {
        return skinPricesEnabled;
    }

    public void setSkinPricesEnabled(boolean enabled) {
        this.skinPricesEnabled = enabled;
    }

    public boolean isShowDemand() {
        return showDemand;
    }

    public void setShowDemand(boolean show) {
        this.showDemand = show;
    }

    public boolean isShowSeason() {
        return showSeason;
    }

    public void setShowSeason(boolean show) {
        this.showSeason = show;
    }

    public boolean isShowSet() {
        return showSet;
    }

    public void setShowSet(boolean show) {
        this.showSet = show;
    }

    public boolean isItemFrameSkinPricesEnabled() {
        return itemFrameSkinPricesEnabled;
    }

    public void setItemFrameSkinPricesEnabled(boolean enabled) {
        this.itemFrameSkinPricesEnabled = enabled;
    }

    public boolean isItemFrameRequireSneak() {
        return itemFrameRequireSneak;
    }

    public void setItemFrameRequireSneak(boolean require) {
        this.itemFrameRequireSneak = require;
    }

    public boolean isItemFrameShowDemand() {
        return itemFrameShowDemand;
    }

    public void setItemFrameShowDemand(boolean show) {
        this.itemFrameShowDemand = show;
    }

    public boolean isItemFrameShowSeason() {
        return itemFrameShowSeason;
    }

    public void setItemFrameShowSeason(boolean show) {
        this.itemFrameShowSeason = show;
    }

    public boolean isItemFrameShowSet() {
        return itemFrameShowSet;
    }

    public void setItemFrameShowSet(boolean show) {
        this.itemFrameShowSet = show;
    }

    public List<String> getDungeonPartyMembers() {
        return new ArrayList<>(dungeonPartyMembers);
    }

    public void setDungeonPartyMembers(List<String> members) {
        this.dungeonPartyMembers = new ArrayList<>(members);
    }

    public boolean isAutoGgEnabled() {
        return autoGgEnabled;
    }

    public void setAutoGgEnabled(boolean enabled) {
        this.autoGgEnabled = enabled;
    }

    public String getRankupMessage() {
        return rankupMessage;
    }

    public void setRankupMessage(String message) {
        this.rankupMessage = message;
    }

    public boolean isUpdateCheckerEnabled() {
        return updateCheckerEnabled;
    }

    public void setUpdateCheckerEnabled(boolean enabled) {
        this.updateCheckerEnabled = enabled;
    }

    public boolean isTradeHudEnabled() {
        return tradeHudEnabled;
    }

    public void setTradeHudEnabled(boolean enabled) {
        this.tradeHudEnabled = enabled;
    }

    public boolean isMaxedItemHighlightEnabled() {
        return maxedItemHighlightEnabled;
    }

    public void setMaxedItemHighlightEnabled(boolean enabled) {
        this.maxedItemHighlightEnabled = enabled;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
            System.out.println("[LoverMods] Config saved to " + CONFIG_FILE.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[LoverMods] Failed to save config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static ModConfig load() {
        if (!CONFIG_FILE.exists()) {
            System.out.println("[LoverMods] Config file not found, creating default config");
            ModConfig config = new ModConfig();
            config.save();
            return config;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ModConfig config = GSON.fromJson(reader, ModConfig.class);
            System.out.println("[LoverMods] Config loaded from " + CONFIG_FILE.getAbsolutePath());
            return config;
        } catch (IOException e) {
            System.err.println("[LoverMods] Failed to load config: " + e.getMessage());
            e.printStackTrace();
            return new ModConfig();
        }
    }
}