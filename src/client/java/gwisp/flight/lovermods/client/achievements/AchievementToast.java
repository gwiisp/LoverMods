package gwisp.flight.lovermods.client.achievements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.Style;

public class AchievementToast {
    private final Achievement achievement;
    private long startTime;
    private static final long DISPLAY_DURATION = 5000;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 40;
    private boolean playedSound = false;

    public AchievementToast(Achievement achievement) {
        this.achievement = achievement;
        this.startTime = System.currentTimeMillis();
    }

    public void render(DrawContext context, int x, int y) {
        long elapsed = System.currentTimeMillis() - startTime;
        float alpha = 1.0f;

        if (elapsed < 200) {
            alpha = elapsed / 200.0f;
        }
        else if (elapsed > DISPLAY_DURATION - 500) {
            alpha = (DISPLAY_DURATION - elapsed) / 500.0f;
        }

        int bgAlpha = (int)(alpha * 224);
        int bgColor = (bgAlpha << 24) | 0x000000;

        context.fill(x, y, x + WIDTH, y + HEIGHT, bgColor);

        int borderColor = achievement.getRarity().getColor() | (int)(alpha * 255) << 24;
        context.fill(x, y, x + WIDTH, y + 1, borderColor);
        context.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, borderColor);
        context.fill(x, y, x + 1, y + HEIGHT, borderColor);
        context.fill(x + WIDTH - 1, y, x + WIDTH, y + HEIGHT, borderColor);

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        int iconColor = 0xFFFFFF | (int)(alpha * 255) << 24;
        int titleColor = achievement.getRarity().getColor() | (int)(alpha * 255) << 24;
        int subtitleColor = 0xAAAAAA | (int)(alpha * 255) << 24;
        int pointsColor = 0xFFFF55 | (int)(alpha * 255) << 24;

        String icon = achievement.getIcon();
        int iconWidth = textRenderer.getWidth(icon);
        Text iconText = Text.literal(icon)
                .setStyle(Style.EMPTY.withColor(iconColor));
        new TextWidget(x + 5, y + 5, iconWidth, 10, iconText, textRenderer)
                .render(context, 0, 0, 0);

        int textX = x + iconWidth + 12;
        int textY = y + 5;

        String title = achievement.getTitle();
        if (title.length() > 20) {
            title = title.substring(0, 17) + "...";
        }
        Text titleText = Text.literal("§l" + title)
                .setStyle(Style.EMPTY.withColor(titleColor));
        int titleWidth = textRenderer.getWidth(titleText);
        new TextWidget(textX, textY, titleWidth, 10, titleText, textRenderer)
                .render(context, 0, 0, 0);
        textY += 11;

        Text subtitleText = Text.literal("Achievement Unlocked!")
                .setStyle(Style.EMPTY.withColor(subtitleColor));
        int subtitleWidth = textRenderer.getWidth(subtitleText);
        new TextWidget(textX, textY, subtitleWidth, 10, subtitleText, textRenderer)
                .render(context, 0, 0, 0);
        textY += 11;

        String points = "+" + achievement.getPoints() + " pts";
        Text pointsText = Text.literal(points)
                .setStyle(Style.EMPTY.withColor(pointsColor));
        int pointsWidth = textRenderer.getWidth(pointsText);
        new TextWidget(textX, textY, pointsWidth, 10, pointsText, textRenderer)
                .render(context, 0, 0, 0);

        if (!playedSound && client.player != null) {
            client.player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            playedSound = true;
        }
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - startTime > DISPLAY_DURATION;
    }
}