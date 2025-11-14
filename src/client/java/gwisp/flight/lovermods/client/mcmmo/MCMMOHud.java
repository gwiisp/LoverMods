package gwisp.flight.lovermods.client.mcmmo;

import gwisp.flight.lovermods.client.LovermodsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import java.util.List;

public class MCMMOHud {
    private static final int PADDING = 8;
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

            Text skillName = Text.literal(skill.skillName)
                    .setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true));
            Text levelText = Text.literal("Level: " + String.format("%,d", skill.currentLevel))
                    .setStyle(Style.EMPTY.withColor(Formatting.WHITE));
            Text xpText = Text.literal("XP: " + String.format("%.1f%%", skill.currentXPPercent * 100))
                    .setStyle(Style.EMPTY.withColor(Formatting.GREEN));
            Text etaText = Text.literal("ETA: " + skill.getEstimatedTimeToLevel())
                    .setStyle(Style.EMPTY.withColor(Formatting.AQUA));

            int maxWidth = Math.max(Math.max(
                    textRenderer.getWidth(skillName),
                    textRenderer.getWidth(levelText)
            ), Math.max(
                    textRenderer.getWidth(xpText),
                    textRenderer.getWidth(etaText)
            ));

            int hudWidth = maxWidth + (PADDING * 2);
            int hudHeight = (LINE_HEIGHT * 4) + (PADDING * 2) + 3; // extra spacing between lines

            int x = screenWidth - hudWidth - 10;
            int y = currentY - hudHeight;

            context.fill(x, y, x + hudWidth, y + hudHeight, BG_COLOR);
            context.drawBorder(x, y, hudWidth, hudHeight, BORDER_COLOR);

            int textX = x + PADDING;
            int textY = y + PADDING;

            int skillNameWidth = textRenderer.getWidth(skillName);
            new TextWidget(textX, textY, skillNameWidth, LINE_HEIGHT, skillName, textRenderer)
                    .render(context, 0, 0, 0);
            textY += LINE_HEIGHT + 1;

            int levelTextWidth = textRenderer.getWidth(levelText);
            new TextWidget(textX, textY, levelTextWidth, LINE_HEIGHT, levelText, textRenderer)
                    .render(context, 0, 0, 0);
            textY += LINE_HEIGHT + 1;

            int xpTextWidth = textRenderer.getWidth(xpText);
            new TextWidget(textX, textY, xpTextWidth, LINE_HEIGHT, xpText, textRenderer)
                    .render(context, 0, 0, 0);
            textY += LINE_HEIGHT + 1;

            int etaTextWidth = textRenderer.getWidth(etaText);
            new TextWidget(textX, textY, etaTextWidth, LINE_HEIGHT, etaText, textRenderer)
                    .render(context, 0, 0, 0);

            int barY = y + hudHeight + 2;
            int barHeight = 3;
            context.fill(x, barY, x + hudWidth, barY + barHeight, 0xFF333333);
            int progressWidth = (int) (hudWidth * skill.currentXPPercent);
            int color = getProgressColor(skill.currentXPPercent);
            context.fill(x, barY, x + progressWidth, barY + barHeight, color);

            currentY = y - (barHeight + 2) - HUD_SPACING;
        }
    }

    private void renderSingleSkill(DrawContext context, TextRenderer textRenderer, int screenWidth, int screenHeight) {
        Text skillName = Text.literal(tracker.getCurrentSkill())
                .setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true));
        Text levelText = Text.literal("Level: " + String.format("%,d", tracker.getCurrentLevel()))
                .setStyle(Style.EMPTY.withColor(Formatting.WHITE));
        Text xpText = Text.literal("XP: " + String.format("%.1f%%", tracker.getCurrentXPPercent() * 100))
                .setStyle(Style.EMPTY.withColor(Formatting.GREEN));
        Text etaText = Text.literal("ETA: " + tracker.getEstimatedTimeToLevel())
                .setStyle(Style.EMPTY.withColor(Formatting.AQUA));

        int maxWidth = Math.max(Math.max(
                textRenderer.getWidth(skillName),
                textRenderer.getWidth(levelText)
        ), Math.max(
                textRenderer.getWidth(xpText),
                textRenderer.getWidth(etaText)
        ));

        int hudWidth = maxWidth + (PADDING * 2);
        int hudHeight = (LINE_HEIGHT * 4) + (PADDING * 2) + 3; // extra spacing between lines
        int x = screenWidth - hudWidth - 10;
        int y = screenHeight - hudHeight - 10;

        context.fill(x, y, x + hudWidth, y + hudHeight, BG_COLOR);
        context.drawBorder(x, y, hudWidth, hudHeight, BORDER_COLOR);

        int textX = x + PADDING;
        int textY = y + PADDING;

        int skillNameWidth = textRenderer.getWidth(skillName);
        new TextWidget(textX, textY, skillNameWidth, LINE_HEIGHT, skillName, textRenderer)
                .render(context, 0, 0, 0);
        textY += LINE_HEIGHT + 1;

        int levelTextWidth = textRenderer.getWidth(levelText);
        new TextWidget(textX, textY, levelTextWidth, LINE_HEIGHT, levelText, textRenderer)
                .render(context, 0, 0, 0);
        textY += LINE_HEIGHT + 1;

        int xpTextWidth = textRenderer.getWidth(xpText);
        new TextWidget(textX, textY, xpTextWidth, LINE_HEIGHT, xpText, textRenderer)
                .render(context, 0, 0, 0);
        textY += LINE_HEIGHT + 1;

        int etaTextWidth = textRenderer.getWidth(etaText);
        new TextWidget(textX, textY, etaTextWidth, LINE_HEIGHT, etaText, textRenderer)
                .render(context, 0, 0, 0);

        int barY = y + hudHeight + 2;
        int barHeight = 3;
        context.fill(x, barY, x + hudWidth, barY + barHeight, 0xFF333333);
        int progressWidth = (int) (hudWidth * tracker.getCurrentXPPercent());
        int color = getProgressColor(tracker.getCurrentXPPercent());
        context.fill(x, barY, x + progressWidth, barY + barHeight, color);
    }

    private int getProgressColor(float percent) {
        if (percent < 0.33f) return 0xFFFF5555;
        if (percent < 0.66f) return 0xFFFFAA00;
        return 0xFF55FF55;
    }
}