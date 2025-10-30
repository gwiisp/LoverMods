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

        renderTextAboveEntity(matrices, vertexConsumers, client.textRenderer, displayText, itemFrame.getPos(), light);
    }

    private static void renderTextAboveEntity(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                              TextRenderer textRenderer, String text, Vec3d entityPos, int light) {
        matrices.push();

        MinecraftClient client = MinecraftClient.getInstance();
        Vec3d camera = client.gameRenderer.getCamera().getPos();

        double x = entityPos.x - camera.x;
        double y = entityPos.y - camera.y + 0.5;
        double z = entityPos.z - camera.z;

        matrices.translate(x, y, z);
        matrices.translate(0, 0, 0);

        float yaw = client.gameRenderer.getCamera().getYaw();
        float pitch = client.gameRenderer.getCamera().getPitch();

        matrices.peek().getPositionMatrix().rotate((float) Math.toRadians(-yaw), 0, 1, 0);
        matrices.peek().getPositionMatrix().rotate((float) Math.toRadians(pitch), 1, 0, 0);

        matrices.scale(-0.025f, -0.025f, 0.025f);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float xOffset = -textRenderer.getWidth(text) / 2f;

        textRenderer.draw(text, xOffset, 0, 0xFFFFFF, false, matrix, vertexConsumers,
                TextRenderer.TextLayerType.NORMAL, 0x00000000, light);

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