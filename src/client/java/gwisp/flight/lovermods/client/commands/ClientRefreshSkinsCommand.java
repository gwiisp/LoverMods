package gwisp.flight.lovermods.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import gwisp.flight.lovermods.skins.SkinPriceManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ClientRefreshSkinsCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("refreshskins")
                .executes(ClientRefreshSkinsCommand::execute));
    }

    private static int execute(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(
                Text.literal("§6[LoverMods] Refreshing skin prices..."));

        try {
            SkinPriceManager.init();
            int count = SkinPriceManager.getSkinCount();

            context.getSource().sendFeedback(
                    Text.literal("§a[LoverMods] Successfully loaded " + count + " skins!"));

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(
                    Text.literal("§c[LoverMods] Failed to refresh skins: " + e.getMessage()));
            e.printStackTrace();
            return 0; // Failure
        }
    }
}