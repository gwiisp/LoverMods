package gwisp.flight.lovermods.client.gui;

import gwisp.flight.lovermods.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class PerformanceSettingsScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;

    private ButtonWidget itemFrameEnabledButton;
    private ButtonWidget itemFrameRadiusButton;

    public PerformanceSettingsScreen(Screen parent, ModConfig config) {
        super(Text.literal("Performance Settings"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2 - buttonWidth / 2;
        int startY = 80;
        int spacing = 25;

        itemFrameEnabledButton = this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Limited Item Frame Range: " + (config.isItemFrameRangeEnabled() ? "§aON" : "§cOFF")),
                button -> {
                    boolean newState = !config.isItemFrameRangeEnabled();
                    config.setItemFrameRangeEnabled(newState);
                    button.setMessage(Text.literal("Limited Item Frame Range: " + (newState ? "§aON" : "§cOFF")));
                }
        ).dimensions(centerX, startY, buttonWidth, buttonHeight).build());

        itemFrameRadiusButton = this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Item Frame Radius: " + config.getItemFrameRenderRadius() + " blocks"),
                button -> {}
        ).dimensions(centerX, startY + spacing, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("-10"),
                button -> {
                    int newRadius = Math.max(10, config.getItemFrameRenderRadius() - 10);
                    config.setItemFrameRenderRadius(newRadius);
                    itemFrameRadiusButton.setMessage(Text.literal("Item Frame Radius: " + newRadius + " blocks"));
                }
        ).dimensions(centerX - 55, startY + spacing + 25, 50, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("-1"),
                button -> {
                    int newRadius = Math.max(10, config.getItemFrameRenderRadius() - 1);
                    config.setItemFrameRenderRadius(newRadius);
                    itemFrameRadiusButton.setMessage(Text.literal("Item Frame Radius: " + newRadius + " blocks"));
                }
        ).dimensions(centerX - 55, startY + spacing + 50, 50, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("+1"),
                button -> {
                    int newRadius = Math.min(500, config.getItemFrameRenderRadius() + 1);
                    config.setItemFrameRenderRadius(newRadius);
                    itemFrameRadiusButton.setMessage(Text.literal("Item Frame Radius: " + newRadius + " blocks"));
                }
        ).dimensions(centerX + buttonWidth + 5, startY + spacing + 50, 50, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("+10"),
                button -> {
                    int newRadius = Math.min(500, config.getItemFrameRenderRadius() + 10);
                    config.setItemFrameRenderRadius(newRadius);
                    itemFrameRadiusButton.setMessage(Text.literal("Item Frame Radius: " + newRadius + " blocks"));
                }
        ).dimensions(centerX + buttonWidth + 5, startY + spacing + 25, 50, buttonHeight).build());

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
                Text.literal("§e§lItem Frame Settings"),
                this.width / 2, 50, 0xFFFF00);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§7Limit how far item frames are rendered"),
                this.width / 2, 60, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§7Useful for being in malls or huge skinshops"),
                this.width / 2, 70, 0xAAAAAA);
    }

    @Override
    public void close() {
        config.save();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}