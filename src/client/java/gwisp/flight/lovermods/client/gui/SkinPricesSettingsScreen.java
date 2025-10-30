package gwisp.flight.lovermods.client.gui;

import gwisp.flight.lovermods.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SkinPricesSettingsScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;

    public SkinPricesSettingsScreen(Screen parent, ModConfig config) {
        super(Text.literal("Skin Prices Settings"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2 - buttonWidth / 2;
        int startY = 60;
        int spacing = 25;
        int currentY = startY;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Show Skin Prices: " + (config.isSkinPricesEnabled() ? "§aON" : "§cOFF")),
                button -> {
                    boolean newState = !config.isSkinPricesEnabled();
                    config.setSkinPricesEnabled(newState);
                    button.setMessage(Text.literal("Show Skin Prices: " + (newState ? "§aON" : "§cOFF")));
                }
        ).dimensions(centerX, currentY, buttonWidth, buttonHeight).build());
        currentY += spacing;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Show Demand: " + (config.isShowDemand() ? "§aON" : "§cOFF")),
                button -> {
                    boolean newState = !config.isShowDemand();
                    config.setShowDemand(newState);
                    button.setMessage(Text.literal("Show Demand: " + (newState ? "§aON" : "§cOFF")));
                }
        ).dimensions(centerX, currentY, buttonWidth, buttonHeight).build());
        currentY += spacing;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Show Season: " + (config.isShowSeason() ? "§aON" : "§cOFF")),
                button -> {
                    boolean newState = !config.isShowSeason();
                    config.setShowSeason(newState);
                    button.setMessage(Text.literal("Show Season: " + (newState ? "§aON" : "§cOFF")));
                }
        ).dimensions(centerX, currentY, buttonWidth, buttonHeight).build());
        currentY += spacing;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Show Set: " + (config.isShowSet() ? "§aON" : "§cOFF")),
                button -> {
                    boolean newState = !config.isShowSet();
                    config.setShowSet(newState);
                    button.setMessage(Text.literal("Show Set: " + (newState ? "§aON" : "§cOFF")));
                }
        ).dimensions(centerX, currentY, buttonWidth, buttonHeight).build());
        currentY += spacing + 10;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Item Frame Prices: " + (config.isItemFrameSkinPricesEnabled() ? "§aON" : "§cOFF")),
                button -> {
                    boolean newState = !config.isItemFrameSkinPricesEnabled();
                    config.setItemFrameSkinPricesEnabled(newState);
                    button.setMessage(Text.literal("Item Frame Prices: " + (newState ? "§aON" : "§cOFF")));
                }
        ).dimensions(centerX, currentY, buttonWidth, buttonHeight).build());
        currentY += spacing;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Require Sneak: " + (config.isItemFrameRequireSneak() ? "§aON" : "§cOFF")),
                button -> {
                    boolean newState = !config.isItemFrameRequireSneak();
                    config.setItemFrameRequireSneak(newState);
                    button.setMessage(Text.literal("Require Sneak: " + (newState ? "§aON" : "§cOFF")));
                }
        ).dimensions(centerX, currentY, buttonWidth, buttonHeight).build());
        currentY += spacing;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("IF Demand: " + (config.isItemFrameShowDemand() ? "§aON" : "§cOFF")),
                button -> {
                    boolean newState = !config.isItemFrameShowDemand();
                    config.setItemFrameShowDemand(newState);
                    button.setMessage(Text.literal("IF Demand: " + (newState ? "§aON" : "§cOFF")));
                }
        ).dimensions(centerX, currentY, buttonWidth, buttonHeight).build());
        currentY += spacing;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("IF Season: " + (config.isItemFrameShowSeason() ? "§aON" : "§cOFF")),
                button -> {
                    boolean newState = !config.isItemFrameShowSeason();
                    config.setItemFrameShowSeason(newState);
                    button.setMessage(Text.literal("IF Season: " + (newState ? "§aON" : "§cOFF")));
                }
        ).dimensions(centerX, currentY, buttonWidth, buttonHeight).build());
        currentY += spacing;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("IF Set: " + (config.isItemFrameShowSet() ? "§aON" : "§cOFF")),
                button -> {
                    boolean newState = !config.isItemFrameShowSet();
                    config.setItemFrameShowSet(newState);
                    button.setMessage(Text.literal("IF Set: " + (newState ? "§aON" : "§cOFF")));
                }
        ).dimensions(centerX, currentY, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Back"),
                button -> this.close()
        ).dimensions(this.width / 2 - 100, this.height - 30, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§7Configure what information appears in tooltips and item frames"),
                this.width / 2, 40, 0xAAAAAA);

        context.drawTextWithShadow(this.textRenderer,
                Text.literal("§e§lTooltip Settings"),
                this.width / 2 - 100, 47, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
                Text.literal("§b§lItem Frame Settings"),
                this.width / 2 - 100, 167, 0xFFFFFF);
    }

    @Override
    public void close() {
        config.save();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}