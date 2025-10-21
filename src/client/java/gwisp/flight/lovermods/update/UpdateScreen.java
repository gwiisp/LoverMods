package gwisp.flight.lovermods.update;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.awt.*;
import java.net.URI;

public class UpdateScreen extends Screen {
    private final Screen parent;
    private final UpdateChecker.UpdateInfo updateInfo;
    private static final int CHANGELOG_MAX_LINES = 10;

    public UpdateScreen(Screen parent, UpdateChecker.UpdateInfo updateInfo) {
        super(Text.literal("LoverMods Update Available"));
        this.parent = parent;
        this.updateInfo = updateInfo;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int buttonY = this.height - 60;

        // Download button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Download Update"),
                        button -> {
                            if (updateInfo.getDownloadUrl() != null) {
                                Util.getOperatingSystem().open(URI.create(updateInfo.getDownloadUrl()));
                            }
                            this.close();
                        })
                .dimensions(centerX - buttonWidth - 5, buttonY, buttonWidth, buttonHeight)
                .build()
        );

        // Skip button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Skip"),
                        button -> this.close())
                .dimensions(centerX + 5, buttonY, buttonWidth, buttonHeight)
                .build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Title
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("LoverMods Update Available!").formatted(net.minecraft.util.Formatting.YELLOW),
                this.width / 2,
                20,
                0xFFFFFF
        );

        // Version info
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Current: ").formatted(net.minecraft.util.Formatting.GRAY)
                        .append(Text.literal(updateInfo.getCurrentVersion()).formatted(net.minecraft.util.Formatting.WHITE))
                        .append(Text.literal(" → ").formatted(net.minecraft.util.Formatting.GRAY))
                        .append(Text.literal("New: ").formatted(net.minecraft.util.Formatting.GREEN))
                        .append(Text.literal(updateInfo.getLatestVersion()).formatted(net.minecraft.util.Formatting.WHITE)),
                this.width / 2,
                40,
                0xFFFFFF
        );

        // Changelog header
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("What's New:").formatted(net.minecraft.util.Formatting.GOLD),
                this.width / 2,
                60,
                0xFFFFFF
        );

        // Changelog content
        String changelog = updateInfo.getChangelog();
        if (changelog != null && !changelog.isEmpty()) {
            String[] lines = changelog.split("\n");
            int startY = 80;
            int maxLines = Math.min(lines.length, CHANGELOG_MAX_LINES);

            for (int i = 0; i < maxLines; i++) {
                String line = lines[i];
                if (line.length() > 80) {
                    line = line.substring(0, 77) + "...";
                }
                context.drawCenteredTextWithShadow(
                        this.textRenderer,
                        Text.literal(line),
                        this.width / 2,
                        startY + (i * 12),
                        0xAAAAAA
                );
            }

            if (lines.length > CHANGELOG_MAX_LINES) {
                context.drawCenteredTextWithShadow(
                        this.textRenderer,
                        Text.literal("... and more").formatted(net.minecraft.util.Formatting.GRAY),
                        this.width / 2,
                        startY + (maxLines * 12),
                        0xAAAAAA
                );
            }
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}