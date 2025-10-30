package gwisp.flight.lovermods.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import gwisp.flight.lovermods.config.ModConfig;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;

    public ConfigScreen(Screen parent, ModConfig config) {
        super(Text.literal("LoverMods Settings"));
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

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Netherwart Highlight"),
                button -> this.client.setScreen(new NetherwartSettingsScreen(this, config))
        ).dimensions(centerX, startY, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Skin Prices"),
                button -> this.client.setScreen(new SkinPricesSettingsScreen(this, config))
        ).dimensions(centerX, startY + spacing, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Dungeon Mods"),
                button -> this.client.setScreen(new DungeonSettingsScreen(this, config))
        ).dimensions(centerX, startY + spacing * 2, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Auto GG"),
                button -> this.client.setScreen(new AutoGGSettingsScreen(this, config))
        ).dimensions(centerX, startY + spacing * 3, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Misc"),
                button -> this.client.setScreen(new MiscSettingsScreen(this, config))
        ).dimensions(centerX, startY + spacing * 4, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"),
                button -> this.close()
        ).dimensions(this.width / 2 - 100, this.height - 30, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Click a module to configure"),
                this.width / 2, 40, 0xAAAAAA);
    }

    @Override
    public void close() {
        config.save();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}