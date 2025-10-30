package gwisp.flight.lovermods.client.mixin;

import gwisp.flight.lovermods.client.LovermodsClient;
import gwisp.flight.lovermods.config.ModConfig;
import gwisp.flight.lovermods.skins.SkinData;
import gwisp.flight.lovermods.skins.SkinPriceManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemFrameEntity.class)
public class ItemFrameNameMixin {

    @Inject(method = "method_5476", at = @At("RETURN"), cancellable = true)
    private void addSkinPriceToItemFrameName(CallbackInfoReturnable<Text> cir) {
        System.out.println("[LoverMods] ========== ITEM FRAME MIXIN CALLED ==========");

        ItemFrameEntity itemFrame = (ItemFrameEntity)(Object)this;
        System.out.println("[LoverMods] ItemFrameEntity confirmed!");

        MinecraftClient client = MinecraftClient.getInstance();
        System.out.println("[LoverMods] Client: " + (client != null ? "OK" : "NULL"));
        System.out.println("[LoverMods] Player: " + (client != null && client.player != null ? "OK" : "NULL"));

        if (client == null || client.player == null) {
            System.out.println("[LoverMods] Client or player is null, returning");
            return;
        }

        boolean isSneaking = client.player.isSneaking();
        System.out.println("[LoverMods] Player sneaking: " + isSneaking);

        if (!isSneaking) {
            System.out.println("[LoverMods] Player not sneaking, returning");
            return;
        }

        ModConfig config = LovermodsClient.getConfig();
        System.out.println("[LoverMods] Config: " + (config != null ? "OK" : "NULL"));
        System.out.println("[LoverMods] Skin prices enabled: " + (config != null && config.isSkinPricesEnabled()));

        if (config == null || !config.isSkinPricesEnabled()) {
            System.out.println("[LoverMods] Config null or skin prices disabled, returning");
            return;
        }

        ItemStack stack = itemFrame.getHeldItemStack();
        System.out.println("[LoverMods] ItemStack empty: " + stack.isEmpty());

        if (stack.isEmpty()) {
            System.out.println("[LoverMods] ItemStack is empty, returning");
            return;
        }

        System.out.println("[LoverMods] ItemStack: " + stack.getItem().toString());
        System.out.println("[LoverMods] ItemStack name: " + stack.getName().getString());

        String skinName = extractSkinName(stack, client);
        System.out.println("[LoverMods] Extracted skin name: " + (skinName != null ? "'" + skinName + "'" : "NULL"));

        if (skinName == null) {
            System.out.println("[LoverMods] No skin name found, returning");
            return;
        }

        boolean hasSkinData = SkinPriceManager.hasSkinData(skinName);
        System.out.println("[LoverMods] Has skin data: " + hasSkinData);

        if (!hasSkinData) {
            System.out.println("[LoverMods] No skin data found, returning");
            return;
        }

        SkinData skinData = SkinPriceManager.getSkinData(skinName);
        System.out.println("[LoverMods] Skin data: " + (skinData != null ? "OK" : "NULL"));

        if (skinData == null) {
            System.out.println("[LoverMods] Skin data is null, returning");
            return;
        }

        Text originalName = cir.getReturnValue();
        System.out.println("[LoverMods] Original name: " + originalName.getString());

        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(originalName.getString());
        nameBuilder.append(" §7(§e").append(skinData.getValue());

        if (config.isShowDemand()) {
            nameBuilder.append(" §7• ").append(getDemandColor(skinData.getDemand()))
                    .append(skinData.getDemand());
        }

        if (config.isShowSeason()) {
            nameBuilder.append(" §7• §f").append(skinData.getSeason());
        }

        if (config.isShowSet()) {
            nameBuilder.append(" §7• §f").append(skinData.getSet());
        }

        nameBuilder.append("§7)");

        String enhancedName = nameBuilder.toString();
        System.out.println("[LoverMods] Enhanced name: " + enhancedName);

        cir.setReturnValue(Text.literal(enhancedName));
        System.out.println("[LoverMods] Return value set!");
        System.out.println("[LoverMods] ========== ITEM FRAME MIXIN COMPLETE ==========");
    }

    private String extractSkinName(ItemStack stack, MinecraftClient client) {
        System.out.println("[LoverMods] Extracting skin name from tooltip...");

        try {
            List<Text> tooltip = stack.getTooltip(
                    Item.TooltipContext.DEFAULT,
                    client.player,
                    TooltipType.BASIC
            );

            System.out.println("[LoverMods] Tooltip has " + tooltip.size() + " lines");

            for (int i = 0; i < tooltip.size(); i++) {
                Text line = tooltip.get(i);
                String lineText = line.getString();
                System.out.println("[LoverMods] Tooltip[" + i + "] raw: " + lineText);

                String cleaned = lineText.replaceAll("§[0-9a-fk-or]", "");
                System.out.println("[LoverMods] Tooltip[" + i + "] cleaned: " + cleaned);

                if (cleaned.toLowerCase().contains("skin:")) {
                    System.out.println("[LoverMods] Found 'skin:' in line!");
                    int skinIndex = cleaned.toLowerCase().indexOf("skin:");
                    String afterSkin = cleaned.substring(skinIndex + 5).trim();
                    System.out.println("[LoverMods] After 'skin:': '" + afterSkin + "'");

                    if (!afterSkin.isEmpty()) {
                        System.out.println("[LoverMods] Returning skin name: " + afterSkin);
                        return afterSkin;
                    }
                }

                if (cleaned.toLowerCase().contains("year:")) {
                    System.out.println("[LoverMods] Found 'year:' line, using item name");
                    String name = stack.getName().getString();
                    String cleanedName = name.replaceAll("§[0-9a-fk-or]", "").trim();
                    System.out.println("[LoverMods] Item name: " + cleanedName);
                    return cleanedName;
                }
            }
        } catch (Exception e) {
            System.err.println("[LoverMods] ERROR extracting skin name:");
            e.printStackTrace();
            return null;
        }

        System.out.println("[LoverMods] No skin name found in tooltip");
        return null;
    }

    private String getDemandColor(String demand) {
        if (demand == null) return "§7";
        return switch (demand.toUpperCase()) {
            case "HIGH" -> "§c";
            case "MEDIUM" -> "§e";
            case "LOW" -> "§a";
            default -> "§7";
        };
    }
}