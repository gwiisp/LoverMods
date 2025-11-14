package gwisp.flight.lovermods.client.achievements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrateDetector {
    private static String lastDetectedCrate = null;

    private static final Pattern CRATE_PATTERN = Pattern.compile("Opens a (.+?) Crate", Pattern.CASE_INSENSITIVE);

    /**
     * Call this when a slot is clicked in a container
     */
    public static void onSlotClick(Slot slot) {
        if (slot == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.currentScreen == null) {
            return;
        }

        ItemStack stack = slot.getStack();
        if (stack.isEmpty()) return;

        String screenTitle = client.currentScreen.getTitle().getString();

        if (!screenTitle.toLowerCase().contains("bulk") &&
                !screenTitle.toLowerCase().contains("crate")) {
            return;
        }

        String crateType = getCrateTypeFromTooltip(stack);

        if (crateType != null && !crateType.equals(lastDetectedCrate)) {
            System.out.println("[LoverMods] Clicked on crate: " + crateType);
            AchievementManager.onCrateOpened(crateType);
            lastDetectedCrate = crateType;
        }
    }

    /**
     * Alternative: Call this when you detect a crate key in the middle slot
     */
    public static void checkMiddleSlotForCrate() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.currentScreen == null) {
            return;
        }

        ScreenHandler handler = client.player.currentScreenHandler;
        if (handler == null || handler.slots.size() < 14) {
            return;
        }

        Slot middleSlot = handler.slots.get(13);
        ItemStack stack = middleSlot.getStack();

        if (!stack.isEmpty()) {
            String crateType = getCrateTypeFromTooltip(stack);
            if (crateType != null) {
                System.out.println("[LoverMods] Found crate key in middle slot: " + crateType);
            }
        }
    }

    /**
     * Extract crate type from item tooltip
     */
    private static String getCrateTypeFromTooltip(ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return null;

        try {
            List<Text> tooltip = stack.getTooltip(
                    Item.TooltipContext.DEFAULT,
                    client.player,
                    TooltipType.BASIC
            );

            for (Text line : tooltip) {
                String text = line.getString();

                Matcher matcher = CRATE_PATTERN.matcher(text);
                if (matcher.find()) {
                    return matcher.group(1);
                }

                if (text.contains("Crate") && !text.contains("Opens")) {
                    String[] parts = text.split(" Crate");
                    if (parts.length > 0) {
                        String crateName = parts[0].trim();
                        crateName = crateName.replaceAll("(?i)^(Opens?\\s+a?\\s*)", "");
                        return crateName;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[LoverMods] Error reading tooltip: " + e.getMessage());
        }

        return null;
    }

    /**
     * Reset detector state (call when screen closes)
     */
    public static void reset() {
        lastDetectedCrate = null;
    }
}