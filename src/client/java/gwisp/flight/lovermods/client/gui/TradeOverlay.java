package gwisp.flight.lovermods.client.gui;

import gwisp.flight.lovermods.client.LovermodsClient;
import gwisp.flight.lovermods.skins.SkinData;
import gwisp.flight.lovermods.skins.SkinPriceManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TradeOverlay {
    private static final Pattern MONEY_PATTERN = Pattern.compile("\\$([0-9,]+)");
    private static final int LINE_HEIGHT = 10;

    public static void render(DrawContext context, GenericContainerScreen screen) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (LovermodsClient.getConfig() == null || !LovermodsClient.getConfig().isTradeHudEnabled()) {
            return;
        }

        GenericContainerScreenHandler handler = screen.getScreenHandler();
        String title = screen.getTitle().getString();

        if (!title.toLowerCase().contains("trading")) {
            return;
        }

        List<TradeItem> theirItems = new ArrayList<>();
        List<TradeItem> yourItems = new ArrayList<>();
        long theirMoney = 0;
        long yourMoney = 0;

        int[] rightSlots = {6, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35, 41, 42, 43, 44};

        for (int slotIndex : rightSlots) {
            if (slotIndex >= handler.slots.size()) continue;

            Slot slot = handler.slots.get(slotIndex);
            ItemStack stack = slot.getStack();

            if (stack.isEmpty()) continue;

            String itemName = getItemName(stack);
            String skinName = extractSkinName(stack);
            String value = null;
            String displayName = itemName;

            if (skinName != null && SkinPriceManager.hasSkinData(skinName)) {
                SkinData skinData = SkinPriceManager.getSkinData(skinName);
                value = skinData.getValue();
                displayName = skinName;
            }

            theirItems.add(new TradeItem(displayName, skinName != null, value, stack.getCount()));
        }

        int[] leftSlots = {2, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39};

        for (int slotIndex : leftSlots) {
            if (slotIndex >= handler.slots.size()) continue;

            Slot slot = handler.slots.get(slotIndex);
            ItemStack stack = slot.getStack();

            if (stack.isEmpty()) continue;

            String itemName = getItemName(stack);
            String skinName = extractSkinName(stack);
            String value = null;
            String displayName = itemName;

            if (skinName != null && SkinPriceManager.hasSkinData(skinName)) {
                SkinData skinData = SkinPriceManager.getSkinData(skinName);
                value = skinData.getValue();
                displayName = skinName;
            }

            yourItems.add(new TradeItem(displayName, skinName != null, value, stack.getCount()));
        }

        if (handler.slots.size() > 6) {
            ItemStack moneyStack = handler.slots.get(6).getStack();
            if (!moneyStack.isEmpty()) {
                long money = extractMoney(moneyStack);
                if (money > 0) {
                    theirMoney = money;
                }
            }
        }

        if (handler.slots.size() > 2) {
            ItemStack moneyStack = handler.slots.get(2).getStack();
            if (!moneyStack.isEmpty()) {
                long money = extractMoney(moneyStack);
                if (money > 0) {
                    yourMoney = money;
                }
            }
        }

        int guiLeft = (screen.width - 176) / 2;
        int guiRight = guiLeft + 176;
        int guiTop = (screen.height - 166) / 2;

        int rightX = guiRight + 10;
        int rightY = guiTop;

        int leftX = guiLeft - 10;
        int leftY = guiTop;

        renderTradeOverlay(context, theirItems, theirMoney, client, rightX, rightY, "§6§lTheir Offer", false);
        renderTradeOverlay(context, yourItems, yourMoney, client, leftX, leftY, "§6§lYour Offer", true);
    }

    private static void renderTradeOverlay(DrawContext context, List<TradeItem> items, long money, MinecraftClient client, int x, int y, String title, boolean alignRight) {
        if (items.isEmpty() && money == 0) return;

        int currentY = y;

        int maxWidth = 0;
        if (alignRight) {
            maxWidth = client.textRenderer.getWidth(title);
            if (money > 0) {
                maxWidth = Math.max(maxWidth, client.textRenderer.getWidth("$" + formatNumber(money)));
            }
            for (TradeItem item : items) {
                String displayText = formatTradeItem(item);
                maxWidth = Math.max(maxWidth, client.textRenderer.getWidth(displayText));
            }
            long totalValue = calculateTotalValue(items);
            if (totalValue > 0) {
                maxWidth = Math.max(maxWidth, client.textRenderer.getWidth("Total Value: " + formatValue(totalValue)));
            }
        }

        int titleX = alignRight ? x - maxWidth : x;
        context.drawTextWithShadow(client.textRenderer, title, titleX, currentY, 0xFFFFFF);
        currentY += LINE_HEIGHT + 2;

       /* if (money > 0) {
            String moneyText = "§a$" + formatNumber(money);
            int moneyX = alignRight ? x - client.textRenderer.getWidth(moneyText) : x;
            context.drawTextWithShadow(client.textRenderer, moneyText, moneyX, currentY, 0xFFFFFF);
            currentY += LINE_HEIGHT;
        }

        */

        for (TradeItem item : items) {
            String displayText = formatTradeItem(item);
            int itemX = alignRight ? x - client.textRenderer.getWidth(displayText) : x;
            context.drawTextWithShadow(client.textRenderer, displayText, itemX, currentY, 0xFFFFFF);
            currentY += LINE_HEIGHT;
        }

        long totalValue = calculateTotalValue(items);
        if (totalValue > 0) {
            currentY += 3;
            String totalText = "§e§lTotal Value: §f" + formatValue(totalValue);
            int totalX = alignRight ? x - client.textRenderer.getWidth(totalText) : x;
            context.drawTextWithShadow(client.textRenderer, totalText, totalX, currentY, 0xFFFFFF);
        }
    }

    private static String formatTradeItem(TradeItem item) {
        StringBuilder sb = new StringBuilder();

        if (item.count > 1) {
            sb.append("§7").append(item.count).append("x §f");
        }

        sb.append("§f").append(item.displayName);

        if (item.hasSkin && item.value != null) {
            sb.append(" §7(§e").append(item.value).append("§7)");
        }

        return sb.toString();
    }

    private static String getItemName(ItemStack stack) {
        String name = stack.getName().getString();
        return name.replaceAll("§[0-9a-fk-or]", "").trim();
    }

    private static String extractSkinName(ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return null;

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
                        if (!afterSkin.isEmpty()) {
                            return afterSkin;
                        }
                    }
                }

                if (cleaned.toLowerCase().contains("year:")) {
                    return getItemName(stack);
                }
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    private static long extractMoney(ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return 0;

        try {
            List<Text> tooltip = stack.getTooltip(
                    Item.TooltipContext.DEFAULT,
                    client.player,
                    TooltipType.BASIC
            );

            for (Text line : tooltip) {
                String lineText = line.getString();
                Matcher matcher = MONEY_PATTERN.matcher(lineText);
                if (matcher.find()) {
                    String moneyStr = matcher.group(1).replace(",", "");
                    try {
                        return Long.parseLong(moneyStr);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        } catch (Exception e) {
            return 0;
        }

        return 0;
    }

    private static String formatNumber(long number) {
        if (number >= 1_000_000_000) {
            return String.format("%.1fb", number / 1_000_000_000.0);
        } else if (number >= 1_000_000) {
            return String.format("%.1fm", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fk", number / 1_000.0);
        }
        return String.valueOf(number);
    }

    private static long calculateTotalValue(List<TradeItem> items) {
        long total = 0;
        for (TradeItem item : items) {
            if (item.hasSkin && item.value != null) {
                long value = parseValue(item.value);
                total += value;
            }
        }
        return total;
    }

    private static long parseValue(String valueStr) {
        if (valueStr == null || valueStr.isEmpty()) return 0;

        valueStr = valueStr.trim().toLowerCase();

        if (valueStr.contains("-")) {
            String[] parts = valueStr.split("-");
            if (parts.length == 2) {
                String higherValue = parts[1].trim();
                return parseSingleValue(higherValue);
            }
        }

        return parseSingleValue(valueStr);
    }

    private static long parseSingleValue(String valueStr) {
        if (valueStr == null || valueStr.isEmpty()) return 0;

        valueStr = valueStr.trim().toLowerCase();

        try {
            if (valueStr.endsWith("b")) {
                double num = Double.parseDouble(valueStr.substring(0, valueStr.length() - 1));
                return (long) (num * 1_000_000_000);
            } else if (valueStr.endsWith("m")) {
                double num = Double.parseDouble(valueStr.substring(0, valueStr.length() - 1));
                return (long) (num * 1_000_000);
            } else if (valueStr.endsWith("k")) {
                double num = Double.parseDouble(valueStr.substring(0, valueStr.length() - 1));
                return (long) (num * 1_000);
            } else {
                return Long.parseLong(valueStr);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String formatValue(long value) {
        if (value >= 1_000_000_000) {
            double b = value / 1_000_000_000.0;
            if (b == (long) b) {
                return (long) b + "b";
            }
            return String.format("%.1fb", b);
        } else if (value >= 1_000_000) {
            double m = value / 1_000_000.0;
            if (m == (long) m) {
                return (long) m + "m";
            }
            return String.format("%.1fm", m);
        } else if (value >= 1_000) {
            double k = value / 1_000.0;
            if (k == (long) k) {
                return (long) k + "k";
            }
            return String.format("%.1fk", k);
        }
        return String.valueOf(value);
    }

    private static class TradeItem {
        String displayName;
        boolean hasSkin;
        String value;
        int count;

        TradeItem(String displayName, boolean hasSkin, String value, int count) {
            this.displayName = displayName;
            this.hasSkin = hasSkin;
            this.value = value;
            this.count = count;
        }
    }
}