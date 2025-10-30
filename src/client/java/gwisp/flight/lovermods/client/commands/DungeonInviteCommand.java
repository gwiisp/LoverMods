package gwisp.flight.lovermods.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import gwisp.flight.lovermods.config.ModConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DungeonInviteCommand {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, ModConfig config) {
        dispatcher.register(ClientCommandManager.literal("dunginv")
                .executes(context -> executeDungeonInvites(context, config)));
    }

    private static int executeDungeonInvites(CommandContext<FabricClientCommandSource> context, ModConfig config) {
        MinecraftClient client = MinecraftClient.getInstance();
        List<String> partyMembers = config.getDungeonPartyMembers();

        if (partyMembers.isEmpty()) {
            context.getSource().sendError(Text.literal("§c[LoverMods] No party members configured! Open config to add players."));
            return 0;
        }

        context.getSource().sendFeedback(Text.literal("§6[LoverMods] Sending dungeon invites to " + partyMembers.size() + " players..."));

        AtomicInteger index = new AtomicInteger(0);

        for (String playerName : partyMembers) {
            long delaySeconds = index.getAndIncrement();

            scheduler.schedule(() -> {
                client.execute(() -> {
                    if (client.player != null && client.player.networkHandler != null) {
                        String command = "dungeon invite " + playerName;
                        client.player.networkHandler.sendCommand(command);

                        if (client.player != null) {
                            client.player.sendMessage(
                                    Text.literal("§6[LoverMods] §7Invited §e" + playerName + "§7 to dungeon"),
                                    false
                            );
                        }
                    }
                });
            }, delaySeconds, TimeUnit.SECONDS);
        }

        return 1;
    }

    public static void shutdown() {
        scheduler.shutdown();
    }
}