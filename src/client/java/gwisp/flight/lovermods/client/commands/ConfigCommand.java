package gwisp.flight.lovermods.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import gwisp.flight.lovermods.client.gui.ConfigScreen;
import gwisp.flight.lovermods.config.ModConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ConfigCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, ModConfig config) {
        dispatcher.register(ClientCommandManager.literal("lovermods")
                .executes(context -> openConfigScreen(context, config))
                .then(ClientCommandManager.literal("config")
                        .executes(context -> openConfigScreen(context, config)))
                .then(ClientCommandManager.literal("settings")
                        .executes(context -> openConfigScreen(context, config))));

        dispatcher.register(ClientCommandManager.literal("lmconfig")
                .executes(context -> openConfigScreen(context, config)));

        System.out.println("[LoverMods] Config commands registered: /lovermods, /lovermods config, /lovermods settings, /lmconfig");
    }

    private static int openConfigScreen(CommandContext<FabricClientCommandSource> context, ModConfig config) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null) {
            context.getSource().sendError(Text.literal("§c[LoverMods] Error: Minecraft client is null"));
            return 0;
        }

        if (config == null) {
            context.getSource().sendError(Text.literal("§c[LoverMods] Error: Config is null"));
            return 0;
        }

        client.execute(() -> {
            try {
                ConfigScreen screen = new ConfigScreen(client.currentScreen, config);
                client.setScreen(screen);
            } catch (Exception e) {
                System.err.println("[LoverMods] Failed to open config screen:");
                e.printStackTrace();
            }
        });

        return 1;
    }
}