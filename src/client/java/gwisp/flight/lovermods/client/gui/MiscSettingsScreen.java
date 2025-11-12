package gwisp.flight.lovermods.client.gui;

import gwisp.flight.lovermods.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public class MiscSettingsScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;

    private TextWidget titleWidget;
    private TextWidget descWidget;

    public MiscSettingsScreen(Screen parent, ModConfig config) {
        super(Text.literal("Miscellaneous Settings"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2 - buttonWidth / 2;
        int startY = 70;
        int spacing = 25;

        titleWidget = new TextWidget(0, 20, this.width, 20, this.title, this.textRenderer);
        titleWidget.alignCenter();
        this.addDrawableChild(titleWidget);

        descWidget = new TextWidget(0, 40, this.width, 20,
                Text.literal("§7Additional features and utilities"), this.textRenderer);
        descWidget.alignCenter();
        this.addDrawableChild(descWidget);

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("MCMMO Tracker: " + (config.isMcmmoTrackerEnabled() ? "§aON" : "§cOFF")),
                button -> {
                    boolean newState = !config.isMcmmoTrackerEnabled();
                    config.setMcmmoTrackerEnabled(newState);
                    button.setMessage(Text.literal("MCMMO Tracker: " + (newState ? "§aON" : "§cOFF")));
                }
        ).dimensions(centerX, startY, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Update Checker: " + (config.isUpdateCheckerEnabled() ? "§aON" : "§cOFF")),
                button -> {
                    boolean newState = !config.isUpdateCheckerEnabled();
                    config.setUpdateCheckerEnabled(newState);
                    button.setMessage(Text.literal("Update Checker: " + (newState ? "§aON" : "§cOFF")));
                }
        ).dimensions(centerX, startY + spacing, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Trade HUD: " + (config.isTradeHudEnabled() ? "§aON" : "§cOFF")),
                button -> {
                    boolean newState = !config.isTradeHudEnabled();
                    config.setTradeHudEnabled(newState);
                    button.setMessage(Text.literal("Trade HUD: " + (newState ? "§aON" : "§cOFF")));
                }
        ).dimensions(centerX, startY + spacing * 2, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Maxed Rune Highlight: " + (config.isMaxedItemHighlightEnabled() ? "§aON" : "§cOFF")),
                button -> {
                    boolean newState = !config.isMaxedItemHighlightEnabled();
                    config.setMaxedItemHighlightEnabled(newState);
                    button.setMessage(Text.literal("Maxed Rune Highlight: " + (newState ? "§aON" : "§cOFF")));
                }
        ).dimensions(centerX, startY + spacing * 3, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Back"),
                button -> this.close()
        ).dimensions(this.width / 2 - 100, this.height - 30, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        config.save();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}