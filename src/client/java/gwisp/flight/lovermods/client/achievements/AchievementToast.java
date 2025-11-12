package gwisp.flight.lovermods.client.achievements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

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
        context.drawBorder(x, y, WIDTH, HEIGHT, borderColor);

        MinecraftClient client = MinecraftClient.getInstance();

        String icon = achievement.getIcon();
        context.drawTextWithShadow(client.textRenderer, icon, x + 5, y + 5, 0xFFFFFF);

        String title = achievement.getTitle();
        if (title.length() > 20) {
            title = title.substring(0, 17) + "...";
        }
        int titleColor = achievement.getRarity().getColor() | (int)(alpha * 255) << 24;
        context.drawTextWithShadow(client.textRenderer, "§l" + title, x + 25, y + 5, titleColor);

        context.drawTextWithShadow(client.textRenderer, "§7Achievement Unlocked!", x + 25, y + 17,
                0xAAAAAA | (int)(alpha * 255) << 24);

        String points = "+" + achievement.getPoints() + " pts";
        context.drawTextWithShadow(client.textRenderer, "§e" + points, x + 25, y + 28,
                0xFFFF55 | (int)(alpha * 255) << 24);

        if (!playedSound) {
            client.getSoundManager().play(
                    PositionedSoundInstance.master(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f)
            );
            playedSound = true;
        }
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - startTime > DISPLAY_DURATION;
    }
}