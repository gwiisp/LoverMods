package gwisp.flight.lovermods.client.mixin;

import gwisp.flight.lovermods.client.LovermodsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.entity.state.ItemFrameEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameEntityRenderer.class)
public class ItemFrameRenderMixin {

    @Unique
    private ItemFrameEntity lovermods$currentEntity;

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/decoration/ItemFrameEntity;Lnet/minecraft/client/render/entity/state/ItemFrameEntityRenderState;F)V",
            at = @At("HEAD"))
    private void captureEntity(ItemFrameEntity entity, ItemFrameEntityRenderState renderState,
                               float tickDelta, CallbackInfo ci) {
        this.lovermods$currentEntity = entity;
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemFrameEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true)
    private void onRender(ItemFrameEntityRenderState renderState, MatrixStack matrices,
                          VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (LovermodsClient.getConfig() != null &&
                LovermodsClient.getConfig().isItemFrameRangeEnabled() &&
                this.lovermods$currentEntity != null) {

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                double distance = client.player.squaredDistanceTo(this.lovermods$currentEntity);
                double maxDistance = LovermodsClient.getConfig().getItemFrameRenderRadius();

                if (distance > maxDistance * maxDistance) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemFrameEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("RETURN"))
    private void cleanupEntity(ItemFrameEntityRenderState renderState, MatrixStack matrices,
                               VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        this.lovermods$currentEntity = null;
    }
}