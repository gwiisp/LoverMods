package gwisp.flight.lovermods.client.afk;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AFKTracker {
    private boolean isAFK = false;
    private boolean afkPending = false;
    private long afkStartTime = 0;
    private int totalTokensGained = 0;
    private double lastX = 0;
    private double lastY = 0;
    private double lastZ = 0;
    private boolean hasInitialPosition = false;
    private String lastTokenMessage = "";

    private static final int PADDING = 8;
    private static final int LINE_HEIGHT = 10;
    private static final int BG_COLOR = 0xE0000000;
    private static final int BORDER_COLOR = 0xFFFF5555;

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\+(\\d+)\\s+Afk\\s+Tokens?", Pattern.CASE_INSENSITIVE);

    public void onChatMessage(Text message) {
        String messageText = message.getString();

        boolean isSystemMessage = messageText.startsWith("[System]") ||
                (!messageText.contains(":") && !messageText.contains("»"));

        if (!isSystemMessage) {
            return;
        }

        if (messageText.contains("You are now AFK")) {
            startAFK();
            afkPending = false;
        }

        if (messageText.contains("You are no longer AFK")) {
            stopAFK();
        }
    }

    public void startAFKPending() {
        afkPending = true;
    }

    public void onActionBarMessage(Text message) {
        if (!isAFK) return;

        String messageText = message.getString();

        if (messageText.equals(lastTokenMessage)) {
            return;
        }

        Matcher matcher = TOKEN_PATTERN.matcher(messageText);

        if (matcher.find()) {
            try {
                int tokens = Integer.parseInt(matcher.group(1));
                totalTokensGained += tokens;
                lastTokenMessage = messageText;
                System.out.println("[AFKTracker] Added " + tokens + " tokens. Total: " + totalTokensGained);
            } catch (NumberFormatException e) {
            }
        }
    }

    public void tick() {
        if (!isAFK) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        double currentX = client.player.getX();
        double currentY = client.player.getY();
        double currentZ = client.player.getZ();

        if (!hasInitialPosition) {
            lastX = currentX;
            lastY = currentY;
            lastZ = currentZ;
            hasInitialPosition = true;
            return;
        }

        double distance = Math.sqrt(
                Math.pow(currentX - lastX, 2) +
                        Math.pow(currentY - lastY, 2) +
                        Math.pow(currentZ - lastZ, 2)
        );

        if (distance > 0.1) {
            stopAFK();
        }
    }

    public void render(DrawContext context, int screenWidth, int screenHeight) {
        if (!isAFK) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        Text titleText = Text.literal("AFK MODE")
                .setStyle(Style.EMPTY.withColor(Formatting.RED).withBold(true));
        Text timerText = Text.literal("Time: " + getAFKTime())
                .setStyle(Style.EMPTY.withColor(Formatting.WHITE));
        Text tokensText = Text.literal("Tokens: " + String.format("%,d", totalTokensGained))
                .setStyle(Style.EMPTY.withColor(Formatting.GOLD));

        int titleWidth = textRenderer.getWidth(titleText);
        int timerWidth = textRenderer.getWidth(timerText);
        int tokensWidth = textRenderer.getWidth(tokensText);

        int maxWidth = Math.max(Math.max(titleWidth, timerWidth), tokensWidth);

        int hudWidth = maxWidth + (PADDING * 2);
        int hudHeight = (LINE_HEIGHT * 3) + (PADDING * 2) + 2;

        int x = (screenWidth / 2) - (hudWidth / 2);
        int y = 10;

        context.fill(x, y, x + hudWidth, y + hudHeight, BG_COLOR);
        context.drawBorder(x, y, hudWidth, hudHeight, BORDER_COLOR);

        int textX = x + PADDING;
        int textY = y + PADDING;

        new TextWidget(textX, textY, titleWidth, LINE_HEIGHT, titleText, textRenderer)
                .render(context, 0, 0, 0);
        textY += LINE_HEIGHT + 1;

        new TextWidget(textX, textY, timerWidth, LINE_HEIGHT, timerText, textRenderer)
                .render(context, 0, 0, 0);
        textY += LINE_HEIGHT + 1;

        new TextWidget(textX, textY, tokensWidth, LINE_HEIGHT, tokensText, textRenderer)
                .render(context, 0, 0, 0);
    }

    private void startAFK() {
        isAFK = true;
        afkPending = false;
        afkStartTime = System.currentTimeMillis();
        totalTokensGained = 0;
        hasInitialPosition = false;
    }

    private void stopAFK() {
        isAFK = false;
        afkPending = false;
        afkStartTime = 0;
        totalTokensGained = 0;
        hasInitialPosition = false;
        lastTokenMessage = "";
    }

    public boolean isAFK() {
        return isAFK;
    }

    public String getAFKTime() {
        if (!isAFK) return "00:00:00";

        long elapsed = System.currentTimeMillis() - afkStartTime;
        long seconds = (elapsed / 1000) % 60;
        long minutes = (elapsed / (1000 * 60)) % 60;
        long hours = (elapsed / (1000 * 60 * 60));

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public int getTotalTokensGained() {
        return totalTokensGained;
    }
}