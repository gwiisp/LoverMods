package gwisp.flight.lovermods.client.afk;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class AFKManager {
    private static final AFKTracker tracker = new AFKTracker();

    public static void onAFKCommand() {
        tracker.startAFKPending();
    }

    public static void onChatMessage(Text message) {
        tracker.onChatMessage(message);
    }

    public static void onActionBarMessage(Text message) {
        tracker.onActionBarMessage(message);
    }

    public static void tick() {
        tracker.tick();
    }

    public static void render(DrawContext context, int screenWidth, int screenHeight) {
        tracker.render(context, screenWidth, screenHeight);
    }

    public static AFKTracker getTracker() {
        return tracker;
    }
}