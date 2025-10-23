package gwisp.flight.lovermods.client.mixin;

import gwisp.flight.lovermods.client.cosmetics.CosmeticManager;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(method = "method_23185", at = @At("HEAD"), cancellable = true)
    private void checkUpsideDown(AbstractClientPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (CosmeticManager.isLoaded()) {
                String playerName = player.getName().getString();
                if (CosmeticManager.isPlayerUpsideDown(playerName)) {
                    System.out.println("[LoverMods] Flipping player upside down: " + playerName);
                    cir.setReturnValue(true);
                }
            }
        } catch (Exception e) {
            System.err.println("[LoverMods] Error in shouldFlipUpsideDown: " + e.getMessage());
            e.printStackTrace();
        }
    }
}