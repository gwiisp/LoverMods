package gwisp.flight.lovermods.client.mixin;

import gwisp.flight.lovermods.client.splash.SplashTextManager;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashTextRenderer.class)
public class SplashTextMixin {

    @Shadow @Final @Mutable
    private String text;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void replaceWithCustomSplash(String text, CallbackInfo ci) {
        System.out.println("[LoverMods] SplashTextMixin triggered! Original splash: " + text);
        System.out.println("[LoverMods] Has custom splashes: " + SplashTextManager.hasCustomSplashes());

        if (SplashTextManager.hasCustomSplashes()) {
            String customSplash = SplashTextManager.getRandomSplash();
            if (customSplash != null) {
                System.out.println("[LoverMods] Replacing splash with: " + customSplash);
                this.text = customSplash;
            }
        } else {
            System.out.println("[LoverMods] No custom splashes available, using vanilla");
        }
    }
}