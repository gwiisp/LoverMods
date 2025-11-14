package gwisp.flight.lovermods.client.news;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

public class VersionHelper {
    private static Boolean isCompatibleVersion = null;

    public static boolean isNewsWidgetCompatible() {
        if (isCompatibleVersion != null) {
            return isCompatibleVersion;
        }

        try {
            String minecraftVersion = FabricLoader.getInstance()
                    .getModContainer("minecraft")
                    .orElseThrow(() -> new RuntimeException("Minecraft not found"))
                    .getMetadata()
                    .getVersion()
                    .getFriendlyString();

            if (minecraftVersion.startsWith("1.21.5") ||
                    minecraftVersion.startsWith("1.21.4") ||
                    minecraftVersion.equals("1.21")) {
                isCompatibleVersion = true;
                System.out.println("[LoverMods] News widget enabled for Minecraft " + minecraftVersion);
            } else {
                isCompatibleVersion = false;
                System.out.println("[LoverMods] News widget disabled for Minecraft " + minecraftVersion + " (requires 1.21.4 or below)");
            }
        } catch (Exception e) {
            System.err.println("[LoverMods] Failed to detect Minecraft version, disabling news widget: " + e.getMessage());
            isCompatibleVersion = false;
        }

        return isCompatibleVersion;
    }
}