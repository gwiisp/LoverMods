package gwisp.flight.lovermods.client;

import net.minecraft.block.Blocks;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import java.util.*;

public class NetherwartHighlighterClient {

    private static final Set<BlockPos> highlightedWarts = Collections.synchronizedSet(new HashSet<>());
    private static boolean enabled = true;
    private static boolean highlightAlongZ = true;
    private static int tickCounter = 0;

    public static void toggleEnabled() {
        enabled = !enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void flipAxis() {
        highlightAlongZ = !highlightAlongZ;
    }

    public static boolean isHighlightAlongZ() {
        return highlightAlongZ;
    }

    public static void tick(MinecraftClient client) {
        if (!enabled || client.player == null || client.world == null) return;

        tickCounter = (tickCounter + 1) % 20;
        if (tickCounter == 0) {
            scanNearbyForRows(client);
        }
    }

    public static void renderHighlights(WorldRenderContext context) {
        if (!enabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;

        MatrixStack matrices = context.matrixStack();

        synchronized (highlightedWarts) {
            for (BlockPos pos : highlightedWarts) {
                matrices.push();

                double camX = context.camera().getPos().x;
                double camY = context.camera().getPos().y;
                double camZ = context.camera().getPos().z;
                matrices.translate(pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ);

                DebugRenderer.drawBox(
                        matrices,
                        consumers,
                        0, 0, 0,
                        1.0, 1.0, 1.0,
                        1.0F, 0.0F, 0.0F, 0.5F
                );

                matrices.pop();
            }
        }
    }

    private static void scanNearbyForRows(MinecraftClient client) {
        BlockPos playerPos = client.player.getBlockPos();
        int range = 100;
        int rowWidth = 7;
        highlightedWarts.clear();

        Map<Integer, List<BlockPos>> rows = new HashMap<>();

        for (int y = playerPos.getY() - 1; y <= playerPos.getY() + 1; y++) {
            for (int x = playerPos.getX() - range; x <= playerPos.getX() + range; x++) {
                for (int z = playerPos.getZ() - range; z <= playerPos.getZ() + range; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isValidBlock(client, pos)) {
                        int rowKey = highlightAlongZ ? x : z;
                        rows.computeIfAbsent(rowKey, k -> new ArrayList<>()).add(pos);
                    }
                }
            }
        }

        for (List<BlockPos> row : rows.values()) {
            if (row.size() < rowWidth) continue;

            if (highlightAlongZ) {
                row.sort(Comparator.comparingInt(BlockPos::getZ));
            } else {
                row.sort(Comparator.comparingInt(BlockPos::getX));
            }

            List<BlockPos> currentSegment = new ArrayList<>();
            int expectedPos = -1;

            for (BlockPos pos : row) {
                int currentPos = highlightAlongZ ? pos.getZ() : pos.getX();

                if (expectedPos == -1 || currentPos == expectedPos + 1) {
                    currentSegment.add(pos);
                    expectedPos = currentPos;
                } else {
                    if (currentSegment.size() >= rowWidth) {
                        int centerIndex = currentSegment.size() / 2;
                        highlightedWarts.add(currentSegment.get(centerIndex));
                    }
                    currentSegment = new ArrayList<>();
                    currentSegment.add(pos);
                    expectedPos = currentPos;
                }
            }

            if (currentSegment.size() >= rowWidth) {
                int centerIndex = currentSegment.size() / 2;
                highlightedWarts.add(currentSegment.get(centerIndex));
            }
        }
    }

    private static boolean isValidBlock(MinecraftClient client, BlockPos pos) {
        var state = client.world.getBlockState(pos);
        if (!state.isOf(Blocks.NETHER_WART)) return false;
        if (!state.getProperties().contains(NetherWartBlock.AGE)) return false;
        int age = state.get(NetherWartBlock.AGE);
        if (age < 3) return false;
        return client.world.getBlockState(pos.down()).isOf(Blocks.SOUL_SAND);
    }
}
