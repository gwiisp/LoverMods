package gwisp.flight.lovermods.client.mixin;

import gwisp.flight.lovermods.client.cosmetics.CosmeticManager;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Redirect(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;shouldFlipUpsideDown(Lnet/minecraft/entity/LivingEntity;)Z"
            ),
            require = 0
    )
    private boolean forceFlipUpsideDown(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            String playerName = player.getName().getString();
            if (CosmeticManager.isLoaded() && CosmeticManager.isPlayerUpsideDown(playerName)) {
                return true;
            }
        }

        if (entity instanceof PlayerEntity || entity.hasCustomName()) {
            String string = Formatting.strip(entity.getName().getString());
            if ("Dinnerbone".equals(string) || "Grumm".equals(string)) {
                return true;
            }
        }

        return false;
    }

    @Redirect(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;shouldFlipUpsideDown(Lnet/minecraft/entity/LivingEntity;)Z"
            )
    )
    private boolean forceFlipUpsideDownRender(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            String playerName = player.getName().getString();
            if (CosmeticManager.isLoaded() && CosmeticManager.isPlayerUpsideDown(playerName)) {
                return true;
            }
        }

        if (entity instanceof PlayerEntity || entity.hasCustomName()) {
            String string = Formatting.strip(entity.getName().getString());
            if ("Dinnerbone".equals(string) || "Grumm".equals(string)) {
                return true;
            }
        }

        return false;
    }
}