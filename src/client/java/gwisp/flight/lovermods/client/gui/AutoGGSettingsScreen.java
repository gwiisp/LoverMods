package gwisp.flight.lovermods.client.gui;

import gwisp.flight.lovermods.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AutoGGSettingsScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;
    private TextFieldWidget messageField;

    public AutoGGSettingsScreen(Screen parent, ModConfig config) {
        super(Text.literal("Auto GG Settings"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        super.init();

        int fieldWidth = 300;
        int centerX = this.width / 2 - fieldWidth / 2;
        int startY = 80;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Auto GG: " + (config.isAutoGgEnabled() ? "§aON" : "§cOFF")),
                button -> {
                    boolean newState = !config.isAutoGgEnabled();
                    config.setAutoGgEnabled(newState);
                    button.setMessage(Text.literal("Auto GG: " + (newState ? "§aON" : "§cOFF")));
                }
        ).dimensions(centerX, startY, fieldWidth, 20).build());

        messageField = new TextFieldWidget(
                this.textRenderer,
                centerX,
                startY + 40,
                fieldWidth,
                20,
                Text.literal("Rankup message")
        );
        messageField.setMaxLength(256);
        messageField.setText(config.getRankupMessage());
        messageField.setPlaceholder(Text.literal("Enter your message"));
        this.addDrawableChild(messageField);

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
                Text.literal("§7Automatically send a message when someone ranks up"),
                this.width / 2, 40, 0xAAAAAA);

        context.drawText(this.textRenderer,
                "Message to send:",
                this.width / 2 - 150, 110, 0xFFFFFF, false);
    }

    @Override
    public void close() {
        if (messageField != null) {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                config.setRankupMessage(message);
            }
        }
        config.save();

        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}
