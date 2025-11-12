package gwisp.flight.lovermods.client.mixin;

import gwisp.flight.lovermods.client.achievements.AchievementManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "sendChatMessage", at = @At("HEAD"))
    private void onChatMessage(String message, CallbackInfo ci) {
        AchievementManager.onChatMessage(message);
    }
}