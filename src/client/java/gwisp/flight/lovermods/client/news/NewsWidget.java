package gwisp.flight.lovermods.client.news;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsWidget {
    // Base dimensions at 1080p (scale factor of 1)
    private static final int BASE_SCREEN_HEIGHT = 1080;
    private static final float BASE_SCALE = 0.22f; // Widget takes 22% of screen width
    private static final float IMAGE_RATIO = 0.7f; // Image takes 70% of widget height because like idk actually im really just trying random values it works tho so

    private static final Map<String, Identifier> imageCache = new HashMap<>();

    private int currentIndex = 0;
    private long lastClickTime = 0;
    private long lastAutoSwitchTime = 0;
    private long lastNewsRefreshTime = 0;
    private static final long CLICK_COOLDOWN = 200;
    private static final long AUTO_SWITCH_INTERVAL = 5000;
    private static final long NEWS_REFRESH_INTERVAL = 120000;
    private boolean bottomLeft = false;

    public void setBottomLeft(boolean bottomLeft) {
        this.bottomLeft = bottomLeft;
    }

    public void render(DrawContext context, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        // Disable on incompatible versions (1.21.6+)
        if (!VersionHelper.isNewsWidgetCompatible()) {
            return;
        }

        List<NewsManager.NewsArticle> articles = NewsManager.getArticles();
        if (articles.isEmpty()) return;

        int widgetWidth = (int)(screenWidth * BASE_SCALE);
        int widgetHeight = (int)(widgetWidth * 0.64f);
        int imageHeight = (int)(widgetHeight * IMAGE_RATIO);

        widgetWidth = Math.max(200, Math.min(widgetWidth, 320));
        widgetHeight = Math.max(128, Math.min(widgetHeight, 205));
        imageHeight = Math.max(90, Math.min(imageHeight, 140));

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastNewsRefreshTime >= NEWS_REFRESH_INTERVAL) {
            lastNewsRefreshTime = currentTime;
            NewsManager.loadNews().thenRun(() -> {
                /*
                System.out.println("[LoverMods] News auto-refreshed successfully");
                 */
                List<NewsManager.NewsArticle> newArticles = NewsManager.getArticles();
                if (currentIndex >= newArticles.size()) {
                    currentIndex = 0;
                }
            });
        }

        if (currentTime - lastAutoSwitchTime >= AUTO_SWITCH_INTERVAL) {
            currentIndex = (currentIndex + 1) % articles.size();
            lastAutoSwitchTime = currentTime;
        }

        int x, y;
        if (bottomLeft) {
            x = 10;
            y = screenHeight - widgetHeight - 10;
        } else {
            x = screenWidth - widgetWidth - 10;
            y = screenHeight - widgetHeight - 10;
        }

        context.fill(x, y, x + widgetWidth, y + widgetHeight, 0xE0000000);
        context.drawBorder(x, y, widgetWidth, widgetHeight, 0xFF444444);

        NewsManager.NewsArticle current = articles.get(currentIndex);

        int imageY = y + 5;
        int imagePadding = 5;
        int imageWidth = widgetWidth - (imagePadding * 2);

        Identifier imageId = loadImage(current.getImageUrl());
        if (imageId != null) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            context.drawTexture(
                    RenderLayer::getGuiTextured,
                    imageId,
                    x + imagePadding, imageY,
                    0.0F, 0.0F,
                    imageWidth, imageHeight,
                    imageWidth, imageHeight
            );
        } else {
            context.fill(x + imagePadding, imageY, x + widgetWidth - imagePadding, imageY + imageHeight, 0xFF333333);

            String loading = "Loading...";
            int loadingWidth = MinecraftClient.getInstance().textRenderer.getWidth(loading);
            context.drawTextWithShadow(
                    MinecraftClient.getInstance().textRenderer,
                    loading,
                    x + (widgetWidth - loadingWidth) / 2,
                    imageY + imageHeight / 2,
                    0xFFFFFF
            );
        }

        int titleY = imageY + imageHeight + 5;
        String title = current.getTitle();

        int maxTitleWidth = widgetWidth - 20;
        int titleWidth = MinecraftClient.getInstance().textRenderer.getWidth(title);

        if (titleWidth > maxTitleWidth) {
            while (titleWidth > maxTitleWidth && title.length() > 3) {
                title = title.substring(0, title.length() - 1);
                titleWidth = MinecraftClient.getInstance().textRenderer.getWidth(title + "...");
            }
            title = title + "...";
        }

        context.drawTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                "§6§l" + title,
                x + 10,
                titleY,
                0xFFFFFF
        );

        int dotY = y + widgetHeight - 15;
        int totalDots = articles.size();
        int dotSpacing = Math.max(8, Math.min(12, widgetWidth / (totalDots * 8)));
        int dotWidth = Math.max(6, Math.min(8, dotSpacing - 4));
        int dotsWidth = totalDots * dotSpacing;
        int startX = x + (widgetWidth - dotsWidth) / 2;

        for (int i = 0; i < totalDots; i++) {
            int dotX = startX + (i * dotSpacing);
            int color = i == currentIndex ? 0xFFFFFFFF : 0xFF666666;
            context.fill(dotX, dotY, dotX + dotWidth, dotY + 3, color);
        }

        if (isMouseOver(mouseX, mouseY, x, y, widgetWidth, widgetHeight)) {
            context.drawBorder(x, y, widgetWidth, widgetHeight, 0xFFFFFF00);
        }
    }

    public boolean mouseClicked(int screenWidth, int screenHeight, double mouseX, double mouseY, int button) {
        // Disable on incompatible versions (1.21.6+)
        if (!VersionHelper.isNewsWidgetCompatible()) {
            return false;
        }

        List<NewsManager.NewsArticle> articles = NewsManager.getArticles();
        if (articles.isEmpty()) return false;

        int widgetWidth = (int)(screenWidth * BASE_SCALE);
        int widgetHeight = (int)(widgetWidth * 0.64f);

        widgetWidth = Math.max(200, Math.min(widgetWidth, 320));
        widgetHeight = Math.max(128, Math.min(widgetHeight, 205));

        int x, y;
        if (bottomLeft) {
            x = 10;
            y = screenHeight - widgetHeight - 10;
        } else {
            x = screenWidth - widgetWidth - 10;
            y = screenHeight - widgetHeight - 10;
        }

        if (isMouseOver((int)mouseX, (int)mouseY, x, y, widgetWidth, widgetHeight)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < CLICK_COOLDOWN) {
                return true;
            }
            lastClickTime = currentTime;
            return true;
        }

        return false;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int index) {
        this.currentIndex = index;
        this.lastAutoSwitchTime = System.currentTimeMillis();
    }

    private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;
    }

    private Identifier loadImage(String urlString) {
        if (urlString == null || urlString.isEmpty()) return null;

        if (imageCache.containsKey(urlString)) {
            return imageCache.get(urlString);
        }

        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                conn.setRequestProperty("Referer", "https://imgur.com/");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                InputStream stream = conn.getInputStream();
                NativeImage image = NativeImage.read(stream);
                stream.close();

                MinecraftClient.getInstance().execute(() -> {
                    try {
                        NativeImageBackedTexture texture = createTexture(image);
                        Identifier id = Identifier.of("lovermods", "news_" + urlString.hashCode());
                        MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
                        imageCache.put(urlString, id);
                    } catch (Exception e) {
                       /*
                        System.out.println("[LoverMods] Failed to create texture: " + e.getMessage());
                        */
                    }
                });
            } catch (Exception e) {
                /*
                System.out.println("[LoverMods] Failed to load news image: " + e.getMessage());
                 */
            }
        }).start();

        return null;
    }

    private static NativeImageBackedTexture createTexture(NativeImage image) {
        Constructor<?>[] constructors = NativeImageBackedTexture.class.getConstructors();

        for (int i = 0; i < constructors.length; i++) {
            Constructor<?> constructor = constructors[i];
            Class<?>[] paramTypes = constructor.getParameterTypes();
            int paramCount = paramTypes.length;

            try {
                Object[] args = new Object[paramCount];

                for (int j = 0; j < paramCount; j++) {
                    Class<?> type = paramTypes[j];

                    if (type == NativeImage.class || type.getName().equals("net.minecraft.class_1011")) {
                        args[j] = image;
                    } else if (type == boolean.class || type == Boolean.class) {
                        args[j] = false;
                    } else if (type == int.class || type == Integer.class) {
                        args[j] = 0;
                    } else {
                        args[j] = null;
                    }
                }

                return (NativeImageBackedTexture) constructor.newInstance(args);

            } catch (Exception e) {
            }
        }

        throw new RuntimeException("No compatible NativeImageBackedTexture constructor found");
    }

    public void reset() {
        currentIndex = 0;
        lastAutoSwitchTime = System.currentTimeMillis();
        lastNewsRefreshTime = System.currentTimeMillis();
    }
}