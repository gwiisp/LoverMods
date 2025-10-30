package gwisp.flight.lovermods.client;

import gwisp.flight.lovermods.client.commands.ClientRefreshSkinsCommand;
import gwisp.flight.lovermods.client.commands.ConfigCommand;
import gwisp.flight.lovermods.client.cosmetics.CosmeticManager;
import gwisp.flight.lovermods.client.gui.ConfigScreen;
import gwisp.flight.lovermods.client.render.ItemFrameSkinValueRenderer;
import gwisp.flight.lovermods.client.splash.SplashTextManager;
import gwisp.flight.lovermods.config.ModConfig;
import gwisp.flight.lovermods.update.UpdateChecker;
import gwisp.flight.lovermods.update.UpdateScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import gwisp.flight.lovermods.skins.SkinPriceManager;
import org.lwjgl.glfw.GLFW;

public class LovermodsClient implements ClientModInitializer {

    private static ModConfig config;
    private static KeyBinding toggleKey;
    private static KeyBinding flipAxisKey;
    private static KeyBinding openConfigKey;
    private static boolean checkedForUpdates = false;

    @Override
    public void onInitializeClient() {
        config = ModConfig.load();
        System.out.println("[LoverMods] Config loaded successfully");

        SkinPriceManager.init();
        System.out.println("[LoverMods] Skins loaded: " + SkinPriceManager.getSkinCount());

        SplashTextManager.loadSplashes();

        CosmeticManager.loadCosmetics();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            ClientRefreshSkinsCommand.register(dispatcher);
            ConfigCommand.register(dispatcher, config);
            gwisp.flight.lovermods.client.commands.DungeonInviteCommand.register(dispatcher, config);
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            System.out.println("[LoverMods] Joined server/world, refreshing cosmetics...");
            CosmeticManager.loadCosmetics();
        });

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

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.lovermods.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.lovermods"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            if (!checkedForUpdates && client.currentScreen == null && config.isUpdateCheckerEnabled()) {
                checkedForUpdates = true;
                checkForUpdates(client);
            }

            while (openConfigKey.wasPressed()) {
                client.setScreen(new ConfigScreen(client.currentScreen, config));
            }

            while (toggleKey.wasPressed()) {
                boolean newState = !config.isNetherwartHighlightEnabled();
                config.setNetherwartHighlightEnabled(newState);
                config.save();

                if (NetherwartHighlighterClient.isEnabled() != newState) {
                    NetherwartHighlighterClient.toggleEnabled();
                }

                client.player.sendMessage(Text.literal("§6[LoverMods] Netherwart highlight "
                        + (newState ? "§aenabled" : "§cdisabled")), true);
            }

            while (flipAxisKey.wasPressed()) {
                boolean newState = !config.isHighlightAlongZ();
                config.setHighlightAlongZ(newState);
                config.save();

                if (NetherwartHighlighterClient.isHighlightAlongZ() != newState) {
                    NetherwartHighlighterClient.flipAxis();
                }

                client.player.sendMessage(Text.literal("§6[LoverMods] Rows run along "
                        + (newState ? "§aZ axis (north-south)" : "§aX axis (east-west)")), true);
            }

            if (config.isNetherwartHighlightEnabled()) {
                NetherwartHighlighterClient.tick(client);
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (config.isNetherwartHighlightEnabled()) {
                NetherwartHighlighterClient.renderHighlights(context);
            }
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            ItemFrameSkinValueRenderer.renderItemFrameText(
                    context.matrixStack(),
                    context.consumers(),
                    0xF000F0
            );
        });
    }

    private void checkForUpdates(MinecraftClient client) {
        UpdateChecker.checkForUpdates().thenAccept(updateInfo -> {
            if (updateInfo != null && updateInfo.hasUpdate()) {
                client.execute(() -> {
                    client.setScreen(new UpdateScreen(null, updateInfo));
                });
            }
        });
    }

    public static ModConfig getConfig() {
        return config;
    }

    public static void saveConfig() {
        if (config != null) {
            config.save();
        }
    }
}