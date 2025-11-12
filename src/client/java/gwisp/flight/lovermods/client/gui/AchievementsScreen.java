package gwisp.flight.lovermods.client.gui;

import gwisp.flight.lovermods.client.achievements.Achievement;
import gwisp.flight.lovermods.client.achievements.AchievementManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class AchievementsScreen extends Screen {
    private final Screen parent;
    private int scrollOffset = 0;
    private static final int ITEM_HEIGHT = 60;
    private static final int VISIBLE_ITEMS = 6;

    private Achievement.AchievementCategory selectedCategory = null;
    private List<Achievement> displayedAchievements;

    public AchievementsScreen(Screen parent) {
        super(Text.literal("Achievements"));
        this.parent = parent;
        updateDisplayedAchievements();
    }

    @Override
    protected void init() {
        super.init();

        int buttonY = this.height - 30;
        int buttonWidth = 60;
        int spacing = 65;
        int startX = 10;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("All"),
                button -> {
                    selectedCategory = null;
                    scrollOffset = 0;
                    updateDisplayedAchievements();
                }
        ).dimensions(startX, buttonY, buttonWidth, 20).build());

        for (Achievement.AchievementCategory category : Achievement.AchievementCategory.values()) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(category.getName()),
                    button -> {
                        selectedCategory = category;
                        scrollOffset = 0;
                        updateDisplayedAchievements();
                    }
            ).dimensions(startX + (spacing * (category.ordinal() + 1)), buttonY, buttonWidth, 20).build());
        }

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Back"),
                button -> this.close()
        ).dimensions(this.width - 110, buttonY, 100, 20).build());
    }

    private void updateDisplayedAchievements() {
        if (selectedCategory == null) {
            displayedAchievements = new ArrayList<>(AchievementManager.getAll());
        } else {
            displayedAchievements = AchievementManager.getByCategory(selectedCategory);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        String title = "§6§lAchievements";
        int titleWidth = this.textRenderer.getWidth(title);
        context.drawTextWithShadow(this.textRenderer, title,
                this.width / 2 - titleWidth / 2, 10, 0xFFFFFF);

        int unlocked = AchievementManager.getUnlockedCount();
        int total = AchievementManager.getTotalCount();
        int points = AchievementManager.getTotalPoints();

        String stats = String.format("§7Progress: §a%d§7/§a%d §7(§e%d pts§7)", unlocked, total, points);
        int statsWidth = this.textRenderer.getWidth(stats);
        context.drawTextWithShadow(this.textRenderer, stats,
                this.width / 2 - statsWidth / 2, 25, 0xFFFFFF);

        int startY = 45;
        int maxY = this.height - 60;
        int currentY = startY;

        int index = 0;
        for (Achievement achievement : displayedAchievements) {
            if (index < scrollOffset) {
                index++;
                continue;
            }

            if (currentY + ITEM_HEIGHT > maxY) {
                break;
            }

            renderAchievement(context, achievement, 10, currentY, this.width - 20, mouseX, mouseY);
            currentY += ITEM_HEIGHT + 5;
            index++;
        }

        if (displayedAchievements.size() > VISIBLE_ITEMS) {
            String hint = "§7Scroll to see more...";
            int hintWidth = this.textRenderer.getWidth(hint);
            context.drawTextWithShadow(this.textRenderer, hint,
                    this.width / 2 - hintWidth / 2, maxY + 5, 0xFFFFFF);
        }
    }

    private void renderAchievement(DrawContext context, Achievement achievement,
                                   int x, int y, int width, int mouseX, int mouseY) {
        int bgColor = achievement.isUnlocked() ? 0xE0003300 : 0xE0000000;
        context.fill(x, y, x + width, y + ITEM_HEIGHT, bgColor);

        int borderColor = achievement.isUnlocked() ?
                achievement.getRarity().getColor() : 0xFF333333;
        context.drawBorder(x, y, width, ITEM_HEIGHT, borderColor);

        String icon = achievement.getIcon();
        context.drawTextWithShadow(this.textRenderer, icon, x + 5, y + 5, 0xFFFFFF);

        String title = achievement.getTitle();
        if (title.length() > 35) {
            title = title.substring(0, 32) + "...";
        }
        int titleColor = achievement.isUnlocked() ?
                achievement.getRarity().getColor() : 0xAAAAAA;
        context.drawTextWithShadow(this.textRenderer, "§l" + title, x + 25, y + 5, titleColor);

        String desc = achievement.getDescription();
        if (desc.length() > 50) {
            desc = desc.substring(0, 47) + "...";
        }
        context.drawTextWithShadow(this.textRenderer, "§7" + desc, x + 25, y + 18, 0xFFFFFF);

        if (!achievement.isUnlocked() && achievement.getMaxProgress() > 1) {
            int barX = x + 25;
            int barY = y + 32;
            int barWidth = width - 30;
            int barHeight = 4;

            context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);

            int progressWidth = (int)(barWidth * achievement.getProgressPercent());
            context.fill(barX, barY, barX + progressWidth, barY + barHeight, 0xFF55FF55);

            String progressText = String.format("%.0f/%.0f",
                    achievement.getProgress(), achievement.getMaxProgress());
            context.drawTextWithShadow(this.textRenderer, "§7" + progressText,
                    barX, barY + 6, 0xFFFFFF);
        }

        String info = "§e" + achievement.getPoints() + " pts §7• " +
                achievement.getRarity().getName();
        context.drawTextWithShadow(this.textRenderer, info, x + 25, y + 45, 0xFFFFFF);

        if (!achievement.isUnlocked()) {
            context.drawTextWithShadow(this.textRenderer, "§8🔒 Locked",
                    x + width - 60, y + 5, 0xFFFFFF);
        } else {
            context.drawTextWithShadow(this.textRenderer, "§a✓ Unlocked",
                    x + width - 70, y + 5, 0xFFFFFF);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, displayedAchievements.size() - VISIBLE_ITEMS);

        if (verticalAmount > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (verticalAmount < 0) {
            scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}