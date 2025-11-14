package gwisp.flight.lovermods.client.achievements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrateDetector {
    private static String lastCrateType = null;
    private static final Set<String> detectedCratesThisSession = new HashSet<>();
    private static final Pattern REMOVE_PATTERN = Pattern.compile("Remove\\s+(\\d+)", Pattern.CASE_INSENSITIVE);

    public static void onSlotClick(Slot slot) {
        if (slot == null || slot.getStack().isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.currentScreen == null) return;

        String screenTitle = client.currentScreen.getTitle().getString();

        if (screenTitle.contains("BULK OPENING")) {
            ItemStack clickedStack = slot.getStack();
            String itemName = clickedStack.getName().getString();

            if (itemName.toLowerCase().contains("key")) {
                int bulkAmount = detectBulkAmount(slot);

                System.out.println("[CrateDetector] Bulk opening detected: " + bulkAmount + "x " + itemName);

                for (int i = 0; i < bulkAmount; i++) {
                    AchievementManager.onCrateOpened(itemName);
                }

                lastCrateType = itemName;
            }
            return;
        }

        if (!screenTitle.toLowerCase().contains("crate")) return;

        ItemStack stack = slot.getStack();
        String itemName = stack.getName().getString();

        if (itemName.toLowerCase().contains("crate")) {
            if (!itemName.equals(lastCrateType)) {
                if (!detectedCratesThisSession.contains(itemName)) {
                    AchievementManager.onCrateOpened(itemName);
                    detectedCratesThisSession.add(itemName);
                    lastCrateType = itemName;
                    System.out.println("[CrateDetector] Crate opened: " + itemName);
                }
            }
        }
    }

    private static int detectBulkAmount(Slot keySlot) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen == null) return 1;

        ScreenHandler handler = client.player.currentScreenHandler;
        int keySlotIndex = keySlot.getIndex();

        int checkSlot3Left = keySlotIndex - 3;
        if (checkSlot3Left >= 0 && checkSlot3Left < handler.slots.size()) {
            Slot slot3Left = handler.slots.get(checkSlot3Left);
            if (!slot3Left.getStack().isEmpty()) {
                int amount = parseRemoveAmount(slot3Left.getStack());
                if (amount > 0) {
                    return amount + 1;
                }
            }
        }

        int checkSlot2Left = keySlotIndex - 2;
        if (checkSlot2Left >= 0 && checkSlot2Left < handler.slots.size()) {
            Slot slot2Left = handler.slots.get(checkSlot2Left);
            if (!slot2Left.getStack().isEmpty()) {
                int amount = parseRemoveAmount(slot2Left.getStack());
                if (amount > 0) {
                    return amount + 1;
                }
            }
        }

        return 1;
    }

    private static int parseRemoveAmount(ItemStack stack) {
        try {
            String itemName = stack.getName().getString();
            Matcher matcher = REMOVE_PATTERN.matcher(itemName);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public static void reset() {
        lastCrateType = null;
    }
}