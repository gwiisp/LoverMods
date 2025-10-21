package gwisp.flight.lovermods.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import gwisp.flight.lovermods.skins.SkinPriceManager;
import org.lwjgl.glfw.GLFW;

public class LovermodsClient implements ClientModInitializer {

    private static KeyBinding toggleKey;
    private static KeyBinding flipAxisKey;

    @Override
    public void onInitializeClient() {

        System.out.println("[LoverMods] ========== CLIENT INITIALIZING ==========");
        System.out.println("[LoverMods] About to call SkinPriceManager.init()...");

        SkinPriceManager.init();

        System.out.println("[LoverMods] SkinPriceManager.init() completed!");
        System.out.println("[LoverMods] Skins loaded: " + SkinPriceManager.getSkinCount());
        System.out.println("[LoverMods] ========================================");

        // Register keybindings
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.lovermods.toggle_highlight",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F,
                "category.lovermods"
        ));

        flipAxisKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.lovermods.flip_axis",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.lovermods"
        ));

        // Register tick events
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            while (toggleKey.wasPressed()) {
                NetherwartHighlighterClient.toggleEnabled();
                client.player.sendMessage(Text.literal("§6[LoverMods] Netherwart highlight "
                        + (NetherwartHighlighterClient.isEnabled() ? "§aenabled" : "§cdisabled")), true);
            }

            while (flipAxisKey.wasPressed()) {
                NetherwartHighlighterClient.flipAxis();
                client.player.sendMessage(Text.literal("§6[LoverMods] Rows run along "
                        + (NetherwartHighlighterClient.isHighlightAlongZ()
                        ? "§aZ axis (north-south)" : "§aX axis (east-west)")), true);
            }

            NetherwartHighlighterClient.tick(client);
        });

        // Register render event
        WorldRenderEvents.AFTER_ENTITIES.register(NetherwartHighlighterClient::renderHighlights);
    }
}
