package gwisp.flight.lovermods.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import gwisp.flight.lovermods.skins.SkinPriceManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class RefreshSkinsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("refreshskins")
                .executes(RefreshSkinsCommand::execute));
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() ->
                Text.literal("§6[LoverMods] Refreshing skin prices..."), false);

        try {
            SkinPriceManager.init();
            int count = SkinPriceManager.getSkinCount();

            context.getSource().sendFeedback(() ->
                    Text.literal("§a[LoverMods] Successfully loaded " + count + " skins!"), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(
                    Text.literal("§c[LoverMods] Failed to refresh skins: " + e.getMessage()));
            e.printStackTrace();
            return 0; // Failure
        }
    }
}