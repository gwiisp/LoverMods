package gwisp.flight.lovermods.client.mixin;

import gwisp.flight.lovermods.client.LovermodsClient;
import gwisp.flight.lovermods.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.hud.InGameHud.class)
public class TitleReceiveMixin {

    @Inject(method = "method_34004", at = @At("HEAD"))
    private void onTitleReceived(Text title, CallbackInfo ci) {
        if (title == null) return;

        ModConfig config = LovermodsClient.getConfig();
        if (config == null || !config.isAutoGgEnabled()) return;

        String titleText = title.getString();

        if (titleText.toUpperCase().contains("RANKUP:")) {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.player != null) {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        client.execute(() -> {
                            if (client.player != null) {
                                String message = config.getRankupMessage();
                                client.player.networkHandler.sendChatMessage(message);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }
}