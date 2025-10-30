package gwisp.flight.lovermods.client.gui;

import gwisp.flight.lovermods.client.NetherwartHighlighterClient;
import gwisp.flight.lovermods.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class NetherwartSettingsScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;

    public NetherwartSettingsScreen(Screen parent, ModConfig config) {
        super(Text.literal("Netherwart Highlight Settings"));
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

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Enabled: " + (config.isNetherwartHighlightEnabled() ? "§aON" : "§cOFF")),
                button -> {
                    boolean newState = !config.isNetherwartHighlightEnabled();
                    config.setNetherwartHighlightEnabled(newState);
                    if (NetherwartHighlighterClient.isEnabled() != newState) {
                        NetherwartHighlighterClient.toggleEnabled();
                    }
                    button.setMessage(Text.literal("Enabled: " + (newState ? "§aON" : "§cOFF")));
                }
        ).dimensions(centerX, startY, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Axis: " + (config.isHighlightAlongZ() ? "Z (North-South)" : "X (East-West)")),
                button -> {
                    boolean newState = !config.isHighlightAlongZ();
                    config.setHighlightAlongZ(newState);
                    if (NetherwartHighlighterClient.isHighlightAlongZ() != newState) {
                        NetherwartHighlighterClient.flipAxis();
                    }
                    button.setMessage(Text.literal("Axis: " + (newState ? "Z (North-South)" : "X (East-West)")));
                }
        ).dimensions(centerX, startY + spacing, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Color: " + getColorName(config.getHighlightColor())),
                button -> {
                    int currentColor = config.getHighlightColor();
                    int newColor = getNextColor(currentColor);
                    config.setHighlightColor(newColor);
                    button.setMessage(Text.literal("Color: " + getColorName(newColor)));
                }
        ).dimensions(centerX, startY + spacing * 2, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Scan Range: " + config.getScanRange() + " blocks"),
                button -> {}
        ).dimensions(centerX, startY + spacing * 3, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("-10"),
                button -> {
                    config.setScanRange(config.getScanRange() - 10);
                    this.clearAndInit();
                }
        ).dimensions(centerX - 55, startY + spacing * 4, 50, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("+10"),
                button -> {
                    config.setScanRange(config.getScanRange() + 10);
                    this.clearAndInit();
                }
        ).dimensions(centerX + buttonWidth + 5, startY + spacing * 4, 50, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Min Row Width: " + config.getMinRowWidth() + " warts"),
                button -> {}
        ).dimensions(centerX, startY + spacing * 5, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("-1"),
                button -> {
                    config.setMinRowWidth(config.getMinRowWidth() - 1);
                    this.clearAndInit();
                }
        ).dimensions(centerX - 55, startY + spacing * 6, 50, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("+1"),
                button -> {
                    config.setMinRowWidth(config.getMinRowWidth() + 1);
                    this.clearAndInit();
                }
        ).dimensions(centerX + buttonWidth + 5, startY + spacing * 6, 50, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Back"),
                button -> this.close()
        ).dimensions(this.width / 2 - 100, this.height - 30, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
    }

    @Override
    public void close() {
        config.save();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    private String getColorName(int color) {
        return switch (color) {
            case 0xFF0000 -> "§cRed";
            case 0x00FF00 -> "§aGreen";
            case 0x0000FF -> "§9Blue";
            case 0xFFFF00 -> "§eYellow";
            case 0xFF00FF -> "§dMagenta";
            case 0x00FFFF -> "§bCyan";
            case 0xFFFFFF -> "§fWhite";
            case 0xFF8800 -> "§6Orange";
            case 0x8800FF -> "§5Purple";
            default -> "§cRed";
        };
    }

    private int getNextColor(int currentColor) {
        int[] colors = {0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0xFF00FF, 0x00FFFF, 0xFFFFFF, 0xFF8800, 0x8800FF};
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == currentColor) {
                return colors[(i + 1) % colors.length];
            }
        }
        return colors[0];
    }
}