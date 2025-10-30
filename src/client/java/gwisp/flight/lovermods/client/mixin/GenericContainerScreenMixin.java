package gwisp.flight.lovermods.client.mixin;

import gwisp.flight.lovermods.client.gui.TradeOverlay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenericContainerScreen.class)
public class GenericContainerScreenMixin {

    @Inject(method = "method_25394", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        GenericContainerScreen screen = (GenericContainerScreen) (Object) this;
        TradeOverlay.render(context, screen);
    }
}