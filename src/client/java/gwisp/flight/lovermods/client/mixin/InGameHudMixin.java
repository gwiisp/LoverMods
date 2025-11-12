package gwisp.flight.lovermods.client.mixin;

import gwisp.flight.lovermods.client.achievements.Achievement;
import gwisp.flight.lovermods.client.achievements.AchievementManager;
import gwisp.flight.lovermods.client.achievements.AchievementToast;
import gwisp.flight.lovermods.client.mcmmo.MCMMOHud;
import gwisp.flight.lovermods.client.mcmmo.MCMMOTracker;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Unique
    private static final MCMMOTracker lovermods$mcmmoTracker = new MCMMOTracker();

    @Unique
    private static final MCMMOHud lovermods$mcmmoHud = new MCMMOHud(lovermods$mcmmoTracker);

    @Unique
    private static final List<AchievementToast> lovermods$toasts = new ArrayList<>();

    @Unique
    private static boolean lovermods$listenerRegistered = false;

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!lovermods$listenerRegistered) {
            AchievementManager.addListener(achievement -> {
                lovermods$toasts.add(new AchievementToast(achievement));
            });
            lovermods$listenerRegistered = true;
        }

        lovermods$mcmmoTracker.update();

        lovermods$mcmmoHud.render(context, context.getScaledWindowWidth(), context.getScaledWindowHeight());

        int toastY = 10;
        lovermods$toasts.removeIf(AchievementToast::isExpired);

        for (AchievementToast toast : lovermods$toasts) {
            int toastX = context.getScaledWindowWidth() - 210;
            toast.render(context, toastX, toastY);
            toastY += 45;
        }
    }
}