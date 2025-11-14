package gwisp.flight.lovermods.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import gwisp.flight.lovermods.client.news.NewsManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.RenderLayer;
import gwisp.flight.lovermods.client.news.NewsImageCache;
import net.minecraft.util.Util;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class NewsDetailScreen extends Screen {
    private final Screen parent;
    private final NewsManager.NewsArticle article;
    private Identifier imageTexture;
    private boolean imageLoaded = false;
    private List<TextWidget> descriptionWidgets = new ArrayList<>();

    public NewsDetailScreen(Screen parent, NewsManager.NewsArticle article) {
        super(Text.literal("News"));
        this.parent = parent;
        this.article = article;
        loadImage();
    }

    @Override
    protected void init() {
        super.init();

        descriptionWidgets.clear();

        int centerX = this.width / 2;
        int startY = 40;

        TextWidget titleWidget = new TextWidget(
                0,
                startY,
                this.width,
                20,
                Text.literal("§6§l" + article.getTitle()),
                this.textRenderer
        );
        titleWidget.alignCenter();
        this.addDrawableChild(titleWidget);

        int imageHeight = 250;
        int descY = startY + 30 + imageHeight + 20;

        String description = article.getDescription();
        int maxWidth = 500;
        String[] words = description.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int currentY = descY;
        int lineHeight = 12;

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            int testWidth = this.textRenderer.getWidth(testLine);

            if (testWidth > maxWidth && currentLine.length() > 0) {
                TextWidget lineWidget = new TextWidget(
                        0,
                        currentY,
                        this.width,
                        lineHeight,
                        Text.literal(currentLine.toString()),
                        this.textRenderer
                );
                lineWidget.alignCenter();
                this.addDrawableChild(lineWidget);
                descriptionWidgets.add(lineWidget);

                currentY += lineHeight;
                currentLine = new StringBuilder(word);
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }

        if (currentLine.length() > 0) {
            TextWidget lineWidget = new TextWidget(
                    0,
                    currentY,
                    this.width,
                    lineHeight,
                    Text.literal(currentLine.toString()),
                    this.textRenderer
            );
            lineWidget.alignCenter();
            this.addDrawableChild(lineWidget);
            descriptionWidgets.add(lineWidget);
        }

        int buttonWidth = 200;
        int buttonCenterX = this.width / 2 - buttonWidth / 2;
        int buttonY = this.height - 60;

        if (article.getLink() != null && !article.getLink().isEmpty()) {
            String domain = extractDomain(article.getLink());

            TextWidget linkInfoWidget = new TextWidget(
                    0,
                    buttonY - 15,
                    this.width,
                    12,
                    Text.literal("§7Link: §b" + domain),
                    this.textRenderer
            );
            linkInfoWidget.alignCenter();
            this.addDrawableChild(linkInfoWidget);

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("§aRead More"),
                    button -> Util.getOperatingSystem().open(URI.create(article.getLink()))
            ).dimensions(buttonCenterX, buttonY, buttonWidth, 20).build());
        }

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Back"),
                button -> this.close()
        ).dimensions(buttonCenterX, this.height - 30, buttonWidth, 20).build());
    }

    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host != null) {
                if (host.startsWith("www.")) {
                    host = host.substring(4);
                }
                return host;
            }
        } catch (Exception e) {
            int slashIndex = url.indexOf("//");
            if (slashIndex != -1) {
                String afterProtocol = url.substring(slashIndex + 2);
                int nextSlash = afterProtocol.indexOf("/");
                if (nextSlash != -1) {
                    return afterProtocol.substring(0, nextSlash);
                }
                return afterProtocol;
            }
        }
        return url;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int startY = 40;

        int imageWidth = 400;
        int imageHeight = 250;
        int imageX = centerX - imageWidth / 2;
        int imageY = startY + 30;

        if (imageLoaded && imageTexture != null) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            context.drawTexture(
                    RenderLayer::getGuiTextured,
                    imageTexture,
                    imageX, imageY,
                    0.0F, 0.0F,
                    imageWidth, imageHeight,
                    imageWidth, imageHeight
            );
            context.drawBorder(imageX, imageY, imageWidth, imageHeight, 0xFFFFFFFF);
        } else {
            context.fill(imageX, imageY, imageX + imageWidth, imageY + imageHeight, 0xFF333333);
            context.drawBorder(imageX, imageY, imageWidth, imageHeight, 0xFF666666);

            TextWidget loadingWidget = new TextWidget(
                    0,
                    imageY + imageHeight / 2 - 5,
                    this.width,
                    10,
                    Text.literal("Loading image..."),
                    this.textRenderer
            );
            loadingWidget.alignCenter();
            loadingWidget.render(context, mouseX, mouseY, delta);
        }
    }

    private static final java.util.concurrent.ExecutorService IMAGE_POOL =
            java.util.concurrent.Executors.newFixedThreadPool(3);

    private static final java.util.Set<String> LOADING_IN_PROGRESS =
            java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    private void loadImage() {
        String urlString = article.getImageUrl();
        if (urlString == null || urlString.isEmpty()) return;

        if (NewsImageCache.has(urlString)) {
            this.imageTexture = NewsImageCache.get(urlString);
            this.imageLoaded = true;
            return;
        }

        if (LOADING_IN_PROGRESS.contains(urlString)) return;
        LOADING_IN_PROGRESS.add(urlString);

        IMAGE_POOL.submit(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "LoverMods");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                try (InputStream stream = conn.getInputStream()) {
                    NativeImage image = NativeImage.read(stream);

                    this.client.execute(() -> {
                        try {
                            Identifier textureId = Identifier.of("lovermods", "news_" + urlString.hashCode());
                            NativeImageBackedTexture texture = createTexture(image);
                            this.client.getTextureManager().registerTexture(textureId, texture);

                            NewsImageCache.put(urlString, textureId);
                            this.imageTexture = textureId;
                            this.imageLoaded = true;
                        } catch (Exception e) {
                            /*
                            System.out.println("[LoverMods] Texture registration failed: " + e.getMessage());
                             */
                        } finally {
                            LOADING_IN_PROGRESS.remove(urlString);
                        }
                    });
                }
            } catch (Exception e) {
                /*
                System.out.println("[LoverMods] Image load failed for " + urlString + ": " + e.getMessage());
                 */
                LOADING_IN_PROGRESS.remove(urlString);
            }
        });
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

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}