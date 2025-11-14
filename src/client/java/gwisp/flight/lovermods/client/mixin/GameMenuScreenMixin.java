package gwisp.flight.lovermods.client.mixin;

import gwisp.flight.lovermods.client.gui.AchievementsScreen;
import gwisp.flight.lovermods.client.gui.NewsDetailScreen;
import gwisp.flight.lovermods.client.news.NewsManager;
import gwisp.flight.lovermods.client.news.NewsWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    @Unique
    private static NewsWidget lovermods$newsWidget;

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void lovermods$initNewsWidget(CallbackInfo ci) {
        if (lovermods$newsWidget == null) {
            lovermods$newsWidget = new NewsWidget();
        }
        lovermods$newsWidget.setBottomLeft(false);

        int buttonWidth = 140;
        int buttonHeight = 20;

        int padding = 8;
        int x = padding;
        int y = this.height - buttonHeight - padding;

        ButtonWidget achievementsButton = ButtonWidget.builder(
                Text.literal("LoverMods Achievements"),
                button -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null) {
                        client.setScreen(new AchievementsScreen(this));
                    }
                }
        ).dimensions(x, y, buttonWidth, buttonHeight).build();

        this.addDrawableChild(achievementsButton);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void lovermods$renderNewsWidget(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (NewsManager.isLoaded() && NewsManager.getArticleCount() > 0 && lovermods$newsWidget != null) {
            lovermods$newsWidget.render(context, this.width, this.height, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (NewsManager.isLoaded() && lovermods$newsWidget != null) {
            if (lovermods$newsWidget.mouseClicked(this.width, this.height, mouseX, mouseY, button)) {
                List<NewsManager.NewsArticle> articles = NewsManager.getArticles();
                if (!articles.isEmpty()) {
                    int currentIndex = lovermods$newsWidget.getCurrentIndex();
                    NewsManager.NewsArticle article = articles.get(currentIndex);

                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null) {
                        client.setScreen(new NewsDetailScreen(this, article));
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
