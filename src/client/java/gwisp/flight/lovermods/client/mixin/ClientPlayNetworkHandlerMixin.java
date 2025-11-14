package gwisp.flight.lovermods.client.mixin;

import gwisp.flight.lovermods.client.achievements.AchievementManager;
import gwisp.flight.lovermods.client.afk.AFKManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "sendChatMessage", at = @At("HEAD"))
    private void onChatMessage(String message, CallbackInfo ci) {
        if (message.startsWith("/")) {
            AchievementManager.onCommandSent(message);

            if (message.trim().equalsIgnoreCase("/afk")) {
                AFKManager.onAFKCommand();
            }
        } else {
            AchievementManager.onChatMessage(message);
        }
    }

    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        String message = packet.content().getString();
        AchievementManager.onChatReceived(message);
        AFKManager.onChatMessage(packet.content());
    }

    @Inject(method = "onOverlayMessage", at = @At("HEAD"))
    private void onOverlayMessage(OverlayMessageS2CPacket packet, CallbackInfo ci) {
        try {
            java.lang.reflect.Field field = packet.getClass().getDeclaredField("message");
            field.setAccessible(true);
            net.minecraft.text.Text message = (net.minecraft.text.Text) field.get(packet);
            AFKManager.onActionBarMessage(message);
        } catch (Exception e) {
            try {
                java.lang.reflect.Field[] fields = packet.getClass().getDeclaredFields();
                for (java.lang.reflect.Field f : fields) {
                    if (f.getType().equals(net.minecraft.text.Text.class)) {
                        f.setAccessible(true);
                        net.minecraft.text.Text message = (net.minecraft.text.Text) f.get(packet);
                        AFKManager.onActionBarMessage(message);
                        break;
                    }
                }
            } catch (Exception ex) {
            }
        }
    }
}