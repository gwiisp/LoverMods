package gwisp.flight.lovermods.client.mixin;

import gwisp.flight.lovermods.client.cosmetics.CosmeticManager;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "shouldFlipUpsideDown", at = @At("RETURN"), cancellable = true)
    private static void forceFlipUpsideDown(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {

        if (entity instanceof PlayerEntity player) {
            String playerName = player.getName().getString();
            if (CosmeticManager.isLoaded() && CosmeticManager.isPlayerUpsideDown(playerName)) {
                cir.setReturnValue(true);
                return;
            }
        }

        String strippedName = Formatting.strip(entity.getName().getString());
        if ("Dinnerbone".equals(strippedName) || "Grumm".equals(strippedName)) {
            cir.setReturnValue(true);
        }
    }
}
