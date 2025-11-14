package gwisp.flight.lovermods.client.mixin;

import gwisp.flight.lovermods.client.LovermodsClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.texture.SpriteContents$AnimatorImpl")
public class SpriteContentsAnimationMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, require = 0)
    private void disableAnimationTick(int x, int y, CallbackInfo ci) {
        if (LovermodsClient.getConfig() != null && LovermodsClient.getConfig().isDisableAnimatedTextures()) {
            ci.cancel();
        }
    }
}