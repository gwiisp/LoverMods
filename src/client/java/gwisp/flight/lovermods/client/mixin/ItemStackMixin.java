package gwisp.flight.lovermods.client.mixin;

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

            // Check if "Year:" line exists
            if (lineText.toLowerCase().contains("year:")) {
                hasYearLine = true;
            }
        }

        // ONLY use item name if Year line is present AND we have skin info position because uhhh why not? ik this is kinda weird to do but skindex exist
        if (hasYearLine && skinInfoPosition != -1 && itemName != null) {
            skinName = itemName;
        }

        if (skinName != null && SkinPriceManager.hasSkinData(skinName)) {
            SkinData skinData = SkinPriceManager.getSkinData(skinName);

            int insertPosition = skinInfoPosition != -1 ? skinInfoPosition + 1 : (skinnerPosition != -1 ? skinnerPosition + 1 : -1);

            if (insertPosition != -1) {
                tooltip.add(insertPosition, Text.literal(skinData.getFormattedValue()));
                tooltip.add(insertPosition + 1, Text.literal(skinData.getFormattedDemand()));
                tooltip.add(insertPosition + 2, Text.literal(skinData.getFormattedSeason()));
                tooltip.add(insertPosition + 3, Text.literal(skinData.getFormattedSet()));
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