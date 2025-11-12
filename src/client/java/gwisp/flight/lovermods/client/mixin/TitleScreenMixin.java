package gwisp.flight.lovermods.client.mixin;

import gwisp.flight.lovermods.client.LovermodsClient;
import gwisp.flight.lovermods.client.gui.ConfigScreen;
import gwisp.flight.lovermods.client.gui.NewsDetailScreen;
import gwisp.flight.lovermods.client.news.NewsManager;
import gwisp.flight.lovermods.client.news.NewsWidget;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URI;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private static final String DISCORD_URL = "https://discord.gg/4nG3fRAKfG";
    @Unique
    private static final String GITHUB_URL = "https://github.com/gwiisp/LoverMods";

    @Unique
    private ButtonWidget discordButton;
    @Unique
    private ButtonWidget githubButton;

    @Unique
    private final NewsWidget newsWidget = new NewsWidget();

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "method_25426", at = @At("RETURN"))
    private void addLoverModsButtons(CallbackInfo ci) {
        int buttonWidth = 60;
        int buttonHeight = 20;
        int x = 2;
        int y = -10;

        discordButton = ButtonWidget.builder(
                Text.literal("Discord"),
                button -> Util.getOperatingSystem().open(URI.create(DISCORD_URL))
        ).dimensions(x, y, buttonWidth, buttonHeight).build();

        githubButton = ButtonWidget.builder(
                Text.literal("GitHub"),
                button -> Util.getOperatingSystem().open(URI.create(GITHUB_URL))
        ).dimensions(x + buttonWidth + 2, y, buttonWidth, buttonHeight).build();

        this.addDrawableChild(discordButton);
        this.addDrawableChild(githubButton);

        int configButtonWidth = 200;
        int configButtonHeight = 20;
        int configX = this.width / 2 - 100;
        int configY = this.height / 4 + 48 + 72 + 35;

        ButtonWidget configButton = ButtonWidget.builder(
                Text.literal("LoverMods Config"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new ConfigScreen(this.client.currentScreen, LovermodsClient.getConfig()));
                    }
                }
        ).dimensions(configX, configY, configButtonWidth, configButtonHeight).build();

        this.addDrawableChild(configButton);

        ButtonWidget achievementsButton = ButtonWidget.builder(
                Text.literal("🏆 Achievements"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new gwisp.flight.lovermods.client.gui.AchievementsScreen((Screen)(Object)this));
                    }
                }
        ).dimensions(configX, configY + 25, configButtonWidth, configButtonHeight).build();

        this.addDrawableChild(achievementsButton);
    }

    @Inject(method = "method_25394", at = @At("RETURN"))
    private void renderLoverModsInfo(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        String version = FabricLoader.getInstance()
                .getModContainer("lovermods")
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElse("Unknown");

        Text text = Text.literal("§6LoverMods §fv" + version);

        TextWidget textWidget = new TextWidget(
                2,
                25,
                this.textRenderer.getWidth(text),
                10,
                text,
                this.textRenderer
        );

        textWidget.render(context, mouseX, mouseY, delta);

        if (NewsManager.isLoaded() && NewsManager.getArticleCount() > 0) {
            newsWidget.render(context, this.width, this.height, mouseX, mouseY);
        }
    }

    @Inject(method = "method_25402", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (NewsManager.isLoaded() && NewsManager.getArticleCount() > 0) {
            if (newsWidget.mouseClicked(this.width, this.height, mouseX, mouseY, button)) {
                gwisp.flight.lovermods.client.achievements.AchievementManager.onNewsClicked();

                var articles = NewsManager.getArticles();
                if (!articles.isEmpty()) {
                    int currentIndex = newsWidget.getCurrentIndex();
                    NewsManager.NewsArticle currentArticle = articles.get(currentIndex);

                    if (this.client != null) {
                        this.client.setScreen(new NewsDetailScreen((Screen)(Object)this, currentArticle));
                    }
                }
                cir.setReturnValue(true);
            }
        }
    }
}