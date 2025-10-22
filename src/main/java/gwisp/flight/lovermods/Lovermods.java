package gwisp.flight.lovermods;

import gwisp.flight.lovermods.commands.RefreshSkinsCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class Lovermods implements ModInitializer {

    @Override
    public void onInitialize() {
        // Register command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            RefreshSkinsCommand.register(dispatcher);
        });

        System.out.println("[LoverMods] Mod initialized!");
    }
}