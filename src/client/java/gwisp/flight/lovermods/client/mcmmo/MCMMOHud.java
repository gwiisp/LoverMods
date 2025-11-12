package gwisp.flight.lovermods.client.mcmmo;

import gwisp.flight.lovermods.client.LovermodsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class MCMMOHud {
    private static final int PADDING = 5;
    private static final int LINE_HEIGHT = 10;
    private static final int HUD_SPACING = 5;
    private static final int BG_COLOR = 0xE0000000;
    private static final int BORDER_COLOR = 0xFF444444;

    private final MCMMOTracker tracker;

    public MCMMOHud(MCMMOTracker tracker) {
        this.tracker = tracker;
    }

    public void render(DrawContext context, int screenWidth, int screenHeight) {
        if (!LovermodsClient.getConfig().isMcmmoTrackerEnabled() || !tracker.isActive()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        List<MCMMOTracker.SkillData> activeSkills = tracker.getActiveSkills();

        if (activeSkills == null || activeSkills.isEmpty()) {
            renderSingleSkill(context, textRenderer, screenWidth, screenHeight);
            return;
        }

        int currentY = screenHeight - 10;

        for (int i = activeSkills.size() - 1; i >= 0; i--) {
            MCMMOTracker.SkillData skill = activeSkills.get(i);

            String skillName = "§6§l" + skill.skillName;
            String levelText = "§7Level: §f" + String.format("%,d", skill.currentLevel);
            String xpText = "§7XP: §a" + String.format("%.1f%%", skill.currentXPPercent * 100);
            String etaText = "§7ETA: §b" + skill.getEstimatedTimeToLevel();

            int maxWidth = Math.max(Math.max(
                    textRenderer.getWidth(skillName),
                    textRenderer.getWidth(levelText)
            ), Math.max(
                    textRenderer.getWidth(xpText),
                    textRenderer.getWidth(etaText)
            ));

            int hudWidth = maxWidth + (PADDING * 2);
            int hudHeight = (LINE_HEIGHT * 4) + (PADDING * 2);

            int x = screenWidth - hudWidth - 10;
            int y = currentY - hudHeight;

            context.fill(x, y, x + hudWidth, y + hudHeight, BG_COLOR);
            context.drawBorder(x, y, hudWidth, hudHeight, BORDER_COLOR);

            int textX = x + PADDING;
            int textY = y + PADDING;

            context.drawTextWithShadow(textRenderer, skillName, textX, textY, 0xFFFFFF);
            textY += LINE_HEIGHT;

            context.drawTextWithShadow(textRenderer, levelText, textX, textY, 0xFFFFFF);
            textY += LINE_HEIGHT;

            context.drawTextWithShadow(textRenderer, xpText, textX, textY, 0xFFFFFF);
            textY += LINE_HEIGHT;

            context.drawTextWithShadow(textRenderer, etaText, textX, textY, 0xFFFFFF);

            int barY = y + hudHeight + 2;
            int barHeight = 3;

            context.fill(x, barY, x + hudWidth, barY + barHeight, 0xFF333333);

            int progressWidth = (int) (hudWidth * skill.currentXPPercent);
            int color = getProgressColor(skill.currentXPPercent);
            context.fill(x, barY, x + progressWidth, barY + barHeight, color);

            currentY = y - (barHeight + 2) - HUD_SPACING;
        }
    }

    /**
     * Render a single skill HUD if tracker.getActiveSkills() is empty.
     */
    private void renderSingleSkill(DrawContext context, TextRenderer textRenderer, int screenWidth, int screenHeight) {
        String skillName = "§6§l" + tracker.getCurrentSkill();
        String levelText = "§7Level: §f" + String.format("%,d", tracker.getCurrentLevel());
        String xpText = "§7XP: §a" + String.format("%.1f%%", tracker.getCurrentXPPercent() * 100);
        String etaText = "§7ETA: §b" + tracker.getEstimatedTimeToLevel();

        int maxWidth = Math.max(Math.max(
                textRenderer.getWidth(skillName),
                textRenderer.getWidth(levelText)
        ), Math.max(
                textRenderer.getWidth(xpText),
                textRenderer.getWidth(etaText)
        ));

        int hudWidth = maxWidth + (PADDING * 2);
        int hudHeight = (LINE_HEIGHT * 4) + (PADDING * 2);

        int x = screenWidth - hudWidth - 10;
        int y = screenHeight - hudHeight - 10;

        context.fill(x, y, x + hudWidth, y + hudHeight, BG_COLOR);
        context.drawBorder(x, y, hudWidth, hudHeight, BORDER_COLOR);

        int textX = x + PADDING;
        int textY = y + PADDING;

        context.drawTextWithShadow(textRenderer, skillName, textX, textY, 0xFFFFFF);
        textY += LINE_HEIGHT;
        context.drawTextWithShadow(textRenderer, levelText, textX, textY, 0xFFFFFF);
        textY += LINE_HEIGHT;
        context.drawTextWithShadow(textRenderer, xpText, textX, textY, 0xFFFFFF);
        textY += LINE_HEIGHT;
        context.drawTextWithShadow(textRenderer, etaText, textX, textY, 0xFFFFFF);

        int barY = y + hudHeight + 2;
        int barHeight = 3;

        context.fill(x, barY, x + hudWidth, barY + barHeight, 0xFF333333);

        int progressWidth = (int) (hudWidth * tracker.getCurrentXPPercent());
        int color = getProgressColor(tracker.getCurrentXPPercent());
        context.fill(x, barY, x + progressWidth, barY + barHeight, color);
    }

    private int getProgressColor(float percent) {
        if (percent < 0.33f) {
            return 0xFFFF5555;
        } else if (percent < 0.66f) {
            return 0xFFFFAA00;
        } else {
            return 0xFF55FF55;
        }
    }
}
