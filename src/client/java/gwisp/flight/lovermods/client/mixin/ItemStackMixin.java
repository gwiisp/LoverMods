package gwisp.flight.lovermods.client.mixin;

import gwisp.flight.lovermods.client.LovermodsClient;
import gwisp.flight.lovermods.config.ModConfig;
import gwisp.flight.lovermods.skins.SkinData;
import gwisp.flight.lovermods.skins.SkinPriceManager;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "method_7950", at = @At("RETURN"))
    private void addSkinPriceTooltip(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {

        ModConfig config = LovermodsClient.getConfig();
        if (config == null || !config.isSkinPricesEnabled()) {
            return;
        }

        List<Text> tooltip = cir.getReturnValue();

        String skinName = null;
        int skinInfoPosition = -1;
        int skinnerPosition = -1;
        boolean hasYearLine = false;
        String itemName = null;

        for (int i = 0; i < tooltip.size(); i++) {
            String lineText = tooltip.get(i).getString();

            if (i == 0) {
                itemName = extractItemName(lineText);
            }

            if (lineText.toLowerCase().contains("skin:")) {
                skinName = extractSkinName(lineText);
            }

            if (lineText.toUpperCase().contains("SKIN INFORMATION:")) {
                skinInfoPosition = i;
            }

            if (lineText.toLowerCase().contains("skinner:")) {
                skinnerPosition = i;
            }

            if (lineText.toLowerCase().contains("year:")) {
                hasYearLine = true;
            }
        }

        if (hasYearLine && skinInfoPosition != -1 && itemName != null) {
            skinName = itemName;
        }

        if (skinName != null && SkinPriceManager.hasSkinData(skinName)) {
            SkinData skinData = SkinPriceManager.getSkinData(skinName);

            int insertPosition = skinInfoPosition != -1 ? skinInfoPosition + 1 : (skinnerPosition != -1 ? skinnerPosition + 1 : -1);

            if (insertPosition != -1) {

                tooltip.add(insertPosition, Text.literal(skinData.getFormattedValue()));
                int nextPosition = insertPosition + 1;

                if (config.isShowDemand()) {
                    tooltip.add(nextPosition, Text.literal(skinData.getFormattedDemand()));
                    nextPosition++;
                }

                if (config.isShowSeason()) {
                    tooltip.add(nextPosition, Text.literal(skinData.getFormattedSeason()));
                    nextPosition++;
                }

                if (config.isShowSet()) {
                    tooltip.add(nextPosition, Text.literal(skinData.getFormattedSet()));
                }
            }
        }
    }

    private String extractItemName(String line) {
        String cleaned = line.replaceAll("§[0-9a-fk-or]", "").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private String extractSkinName(String line) {
        String cleaned = line.replaceAll("§[0-9a-fk-or]", "");

        int skinIndex = cleaned.toLowerCase().indexOf("skin:");
        if (skinIndex != -1) {
            String afterSkin = cleaned.substring(skinIndex + 5).trim();
            return afterSkin.isEmpty() ? null : afterSkin;
        }

        return null;
    }
}