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

    @Inject(method = "method_7950", at = @At("RETURN"), cancellable = true)
    private void addSkinPriceTooltip(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        List<Text> tooltip = cir.getReturnValue();

        String skinName = null;
        for (Text line : tooltip) {
            String lineText = line.getString();

            System.out.println("[LoverMods DEBUG] Tooltip line: " + lineText);

            if (lineText.toLowerCase().contains("skin:")) {
                skinName = extractSkinName(lineText);
                System.out.println("[LoverMods DEBUG] Found skin name: " + skinName);
                break;
            }
        }

        if (skinName != null) {
            System.out.println("[LoverMods DEBUG] Checking for skin: '" + skinName + "'");
            System.out.println("[LoverMods DEBUG] Lowercase version: '" + skinName.toLowerCase() + "'");
            System.out.println("[LoverMods DEBUG] Has skin data: " + SkinPriceManager.hasSkinData(skinName));
            System.out.println("[LoverMods DEBUG] Total skins loaded: " + SkinPriceManager.getSkinCount());

            if (SkinPriceManager.hasSkinData(skinName)) {
                SkinData skinData = SkinPriceManager.getSkinData(skinName);

                System.out.println("[LoverMods DEBUG] Adding skin data to tooltip for: " + skinName);

                tooltip.add(Text.literal(""));
                tooltip.add(Text.literal(skinData.getFormattedValue()));
                tooltip.add(Text.literal(skinData.getFormattedDemand()));
                tooltip.add(Text.literal(skinData.getFormattedSeason()));
                tooltip.add(Text.literal(skinData.getFormattedSet()));
            }
        }
    }

    private String extractSkinName(String line) {
        // Remove all Minecraft color codes
        String cleaned = line.replaceAll("§[0-9a-fk-or]", "");

        int skinIndex = cleaned.toLowerCase().indexOf("skin:");
        if (skinIndex != -1) {
            String afterSkin = cleaned.substring(skinIndex + 5).trim();
            return afterSkin.isEmpty() ? null : afterSkin;
        }

        return null;
    }
}