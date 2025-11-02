package gwisp.flight.lovermods.client.render;

import gwisp.flight.lovermods.client.LovermodsClient;
import gwisp.flight.lovermods.config.ModConfig;
import gwisp.flight.lovermods.skins.SkinData;
import gwisp.flight.lovermods.skins.SkinPriceManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import java.util.List;

public class ItemFrameSkinValueRenderer {

    public static void renderItemFrameText(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null) return;

        ModConfig config = LovermodsClient.getConfig();
        if (config == null || !config.isItemFrameSkinPricesEnabled()) return;

        if (config.isItemFrameRequireSneak() && !client.player.isSneaking()) return;

        Entity targetedEntity = client.targetedEntity;
        if (targetedEntity == null) return;
        if (!(targetedEntity instanceof ItemFrameEntity)) return;

        ItemFrameEntity itemFrame = (ItemFrameEntity) targetedEntity;
        ItemStack stack = itemFrame.getHeldItemStack();
        if (stack.isEmpty()) return;

        String skinName = extractSkinName(stack, client);
        if (skinName == null || !SkinPriceManager.hasSkinData(skinName)) return;

        SkinData skinData = SkinPriceManager.getSkinData(skinName);

        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append("§6").append(skinName.toUpperCase()).append(" §7(");
        textBuilder.append("§e").append(skinData.getValue());

        if (config.isItemFrameShowDemand() && skinData.getDemand() != null) {
            textBuilder.append(" §7• ")
                    .append(getDemandColor(skinData.getDemand()))
                    .append(skinData.getDemand());
        }

        if (config.isItemFrameShowSeason() && skinData.getSeason() != null && !skinData.getSeason().isEmpty()) {
            textBuilder.append(" §7• ").append("§f").append(skinData.getSeason());
        }

        if (config.isItemFrameShowSet() && skinData.getSet() != null && !skinData.getSet().isEmpty()) {
            textBuilder.append(" §7• ").append("§f").append(skinData.getSet());
        }

        textBuilder.append("§7)");
        String displayText = textBuilder.toString();

        renderTextAboveEntity(matrices, vertexConsumers, client.textRenderer, displayText, itemFrame, light);
    }

    private static void renderTextAboveEntity(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                              TextRenderer textRenderer, String text, ItemFrameEntity itemFrame, int light) {
        matrices.push();

        MinecraftClient client = MinecraftClient.getInstance();
        Vec3d camera = client.gameRenderer.getCamera().getPos();

        Vec3d entityPos = itemFrame.getPos();
        double x = entityPos.x - camera.x;
        double y = entityPos.y - camera.y + 0.45;
        double z = entityPos.z - camera.z;

        Vec3d offset = switch (itemFrame.getHorizontalFacing()) {
            case NORTH -> new Vec3d(0, 0, -0.3);
            case SOUTH -> new Vec3d(0, 0, 0.3);
            case EAST  -> new Vec3d(0.3, 0, 0);
            case WEST  -> new Vec3d(-0.3, 0, 0);
            default -> Vec3d.ZERO;
        };

        matrices.translate(x + offset.x, y + offset.y, z + offset.z);

        float yaw = client.gameRenderer.getCamera().getYaw();
        float pitch = client.gameRenderer.getCamera().getPitch();

        matrices.peek().getPositionMatrix().rotate((float) Math.toRadians(-yaw), 0, 1, 0);
        matrices.peek().getPositionMatrix().rotate((float) Math.toRadians(pitch), 1, 0, 0);

        matrices.scale(-0.025f, -0.025f, 0.025f);

        try {
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float xOffset = -textRenderer.getWidth(text) / 2f;

            System.out.println("Rendering: " + text + " at offset " + xOffset);

            java.lang.reflect.Method drawMethod = TextRenderer.class.getDeclaredMethod(
                    "method_27522",
                    Text.class,
                    float.class,
                    float.class,
                    int.class,
                    boolean.class,
                    Matrix4f.class,
                    VertexConsumerProvider.class,
                    TextRenderer.TextLayerType.class,
                    int.class,
                    int.class
            );

            for (TextRenderer.TextLayerType layerType : new TextRenderer.TextLayerType[] {
                    TextRenderer.TextLayerType.SEE_THROUGH,
                    TextRenderer.TextLayerType.NORMAL
            }) {
                try {
                    drawMethod.invoke(
                            textRenderer,
                            Text.literal(text),
                            xOffset,
                            0f,
                            0xFF00FF00,
                            false,
                            matrix,
                            vertexConsumers,
                            layerType,
                            0,
                            0xF000F0
                    );
                    System.out.println("Drew with " + layerType);
                } catch (Exception e) {
                    System.err.println("Failed with " + layerType + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error rendering item frame text: " + e.getMessage());
            e.printStackTrace();
        }

        matrices.pop();
    }

    private static String extractSkinName(ItemStack stack, MinecraftClient client) {
        try {
            List<Text> tooltip = stack.getTooltip(
                    Item.TooltipContext.DEFAULT,
                    client.player,
                    TooltipType.BASIC
            );

            for (Text line : tooltip) {
                String lineText = line.getString();
                String cleaned = lineText.replaceAll("§[0-9a-fk-or]", "");

                if (cleaned.toLowerCase().contains("skin:")) {
                    int skinIndex = cleaned.toLowerCase().indexOf("skin:");
                    if (skinIndex != -1) {
                        String afterSkin = cleaned.substring(skinIndex + 5).trim();
                        if (!afterSkin.isEmpty()) return afterSkin;
                    }
                }

                if (cleaned.toLowerCase().contains("year:")) {
                    String name = stack.getName().getString();
                    return name.replaceAll("§[0-9a-fk-or]", "").trim();
                }
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    private static String getDemandColor(String demand) {
        if (demand == null) return "§7";

        return switch (demand.toUpperCase()) {
            case "HIGH" -> "§c";
            case "MEDIUM" -> "§e";
            case "LOW" -> "§a";
            default -> "§7";
        };
    }
}