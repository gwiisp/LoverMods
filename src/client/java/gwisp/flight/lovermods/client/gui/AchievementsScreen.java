package gwisp.flight.lovermods.client.gui;

import gwisp.flight.lovermods.client.achievements.Achievement;
import gwisp.flight.lovermods.client.achievements.AchievementManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;

import java.util.ArrayList;
import java.util.List;

public class AchievementsScreen extends Screen {
    private final Screen parent;
    private int scrollOffset = 0;
    private static final int ITEM_HEIGHT = 60;
    private static final int VISIBLE_ITEMS = 6;

    private Achievement.AchievementCategory selectedCategory = null;
    private List<Achievement> displayedAchievements;

    private boolean draggingScrollbar = false;
    private double dragStartY;
    private int dragStartOffset;

    public AchievementsScreen(Screen parent) {
        super(Text.literal("Achievements"));
        this.parent = parent;
        updateDisplayedAchievements();
    }

    @Override
    protected void init() {
        super.init();

        TextWidget titleWidget = new TextWidget(
                0, 10,
                this.width, 20,
                Text.literal("§6§lAchievements"),
                this.textRenderer
        );
        titleWidget.alignCenter();
        this.addDrawableChild(titleWidget);

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

        int testButtonY = 10;
        int testButtonX = this.width - 180;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§cReset All"),
                button -> {
                    AchievementManager.resetAll();
                    updateDisplayedAchievements();
                }
        ).dimensions(testButtonX, testButtonY, 80, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§aUnlock All"),
                button -> {
                    AchievementManager.unlockAll();
                    updateDisplayedAchievements();
                }
        ).dimensions(testButtonX + 90, testButtonY, 80, 20).build());

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

        int unlocked = AchievementManager.getUnlockedCount();
        int total = AchievementManager.getTotalCount();
        int points = AchievementManager.getTotalPoints();

        String stats = String.format("§7Progress: §a%d§7/§a%d §7(§e%d pts§7)", unlocked, total, points);
        TextWidget statsWidget = new TextWidget(
                0, 25,
                this.width, 20,
                Text.literal(stats),
                this.textRenderer
        );
        statsWidget.alignCenter();
        statsWidget.render(context, mouseX, mouseY, delta);

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

            renderAchievement(context, achievement, 10, currentY, this.width - 30, mouseX, mouseY, delta);
            currentY += ITEM_HEIGHT + 5;
            index++;
        }

        if (displayedAchievements.size() > VISIBLE_ITEMS) {
            renderScrollbar(context, mouseX, mouseY);
        }
    }

    private void renderAchievement(DrawContext context, Achievement achievement,
                                   int x, int y, int width, int mouseX, int mouseY, float delta) {
        int bgColor = achievement.isUnlocked() ? 0xE0003300 : 0xE0000000;
        context.fill(x, y, x + width, y + ITEM_HEIGHT, bgColor);

        int borderColor = achievement.isUnlocked() ?
                achievement.getRarity().getColor() : 0xFF333333;
        context.drawBorder(x, y, width, ITEM_HEIGHT, borderColor);

        TextWidget iconWidget = new TextWidget(
                x + 5, y + 5,
                20, 10,
                Text.literal(achievement.getIcon()),
                this.textRenderer
        );
        iconWidget.render(context, mouseX, mouseY, delta);

        String title = achievement.getTitle();
        if (title.length() > 35) {
            title = title.substring(0, 32) + "...";
        }

        int rarityColor = achievement.isUnlocked() ? achievement.getRarity().getColor() : 0xAAAAAA;
        Text titleText = Text.literal(title)
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rarityColor)).withBold(true));

        TextWidget titleWidget = new TextWidget(
                x + 25, y + 5,
                width - 100, 10,
                titleText,
                this.textRenderer
        );
        titleWidget.alignLeft();
        titleWidget.render(context, mouseX, mouseY, delta);

        String desc = achievement.getDescription();
        if (desc.length() > 150) {
            desc = desc.substring(0, 47) + "...";
        }
        TextWidget descWidget = new TextWidget(
                x + 25, y + 18,
                width - 30, 10,
                Text.literal("§7" + desc),
                this.textRenderer
        );
        descWidget.alignLeft();
        descWidget.render(context, mouseX, mouseY, delta);

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
            TextWidget progressWidget = new TextWidget(
                    barX, barY + 6,
                    100, 10,
                    Text.literal("§7" + progressText),
                    this.textRenderer
            );
            progressWidget.alignLeft();
            progressWidget.render(context, mouseX, mouseY, delta);
        }

        String info = "§e" + achievement.getPoints() + " pts §7• " +
                achievement.getRarity().getName();
        TextWidget infoWidget = new TextWidget(
                x + 25, y + 45,
                200, 10,
                Text.literal(info),
                this.textRenderer
        );
        infoWidget.alignLeft();
        infoWidget.render(context, mouseX, mouseY, delta);

        if (!achievement.isUnlocked()) {
            TextWidget lockWidget = new TextWidget(
                    x + width - 60, y + 5,
                    60, 10,
                    Text.literal("§8🔒 Locked"),
                    this.textRenderer
            );
            lockWidget.alignLeft();
            lockWidget.render(context, mouseX, mouseY, delta);
        } else {
            TextWidget unlockWidget = new TextWidget(
                    x + width - 70, y + 5,
                    70, 10,
                    Text.literal("§a✓ Unlocked"),
                    this.textRenderer
            );
            unlockWidget.alignLeft();
            unlockWidget.render(context, mouseX, mouseY, delta);
        }
    }

    private void renderScrollbar(DrawContext context, int mouseX, int mouseY) {
        int listHeight = (VISIBLE_ITEMS * (ITEM_HEIGHT + 5));
        int scrollbarX = this.width - 15;
        int scrollbarY = 45;
        int scrollbarHeight = listHeight;
        int handleHeight = Math.max(20, (int) (scrollbarHeight * (VISIBLE_ITEMS / (float) displayedAchievements.size())));
        int maxScroll = Math.max(1, displayedAchievements.size() - VISIBLE_ITEMS);
        int handleY = scrollbarY + (int) ((scrollOffset / (float) maxScroll) * (scrollbarHeight - handleHeight));

        context.fill(scrollbarX, scrollbarY, scrollbarX + 6, scrollbarY + scrollbarHeight, 0x66000000);
        context.fill(scrollbarX, handleY, scrollbarX + 6, handleY + handleHeight, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int scrollbarX = this.width - 15;
        int scrollbarY = 45;
        int scrollbarHeight = (VISIBLE_ITEMS * (ITEM_HEIGHT + 5));
        int handleHeight = Math.max(20, (int) (scrollbarHeight * (VISIBLE_ITEMS / (float) displayedAchievements.size())));
        int maxScroll = Math.max(1, displayedAchievements.size() - VISIBLE_ITEMS);
        int handleY = scrollbarY + (int) ((scrollOffset / (float) maxScroll) * (scrollbarHeight - handleHeight));

        if (mouseX >= scrollbarX && mouseX <= scrollbarX + 6 && mouseY >= handleY && mouseY <= handleY + handleHeight) {
            draggingScrollbar = true;
            dragStartY = mouseY;
            dragStartOffset = scrollOffset;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingScrollbar) {
            int scrollbarHeight = (VISIBLE_ITEMS * (ITEM_HEIGHT + 5));
            int handleHeight = Math.max(20, (int) (scrollbarHeight * (VISIBLE_ITEMS / (float) displayedAchievements.size())));
            int maxScroll = Math.max(1, displayedAchievements.size() - VISIBLE_ITEMS);

            double delta = (mouseY - dragStartY) / (scrollbarHeight - handleHeight);
            scrollOffset = Math.min(maxScroll, Math.max(0, dragStartOffset + (int)(delta * maxScroll)));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
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