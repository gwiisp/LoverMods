package gwisp.flight.lovermods.client.cosmetics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CosmeticManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("LoverMods");
    private static final String COSMETICS_URL = "https://gwiisp.github.io/lovermods-prices/secret.json";

    private static final Map<String, String> capeMap = new HashMap<>();
    private static final Set<String> upsideDownPlayers = new HashSet<>();
    private static boolean loaded = false;

    public static CompletableFuture<Void> loadCosmetics() {
        return CompletableFuture.runAsync(() -> {
            System.out.println("[LoverMods] Loading cosmetics from: " + COSMETICS_URL);
            try {
                URL url = new URL(COSMETICS_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "LoverMods");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                System.out.println("[LoverMods] Cosmetics response code: " + responseCode);

                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    System.out.println("[LoverMods] Raw cosmetics JSON: " + response.toString());

                    JsonObject cosmetics = JsonParser.parseString(response.toString()).getAsJsonObject();

                    capeMap.clear();
                    if (cosmetics.has("capes")) {
                        JsonObject capes = cosmetics.getAsJsonObject("capes");
                        for (String playerName : capes.keySet()) {
                            String capeName = capes.get(playerName).getAsString();
                            capeMap.put(playerName, capeName);
                            System.out.println("[LoverMods] Loaded cape for " + playerName + ": " + capeName);
                        }
                    } else {
                        System.out.println("[LoverMods] No 'capes' field in JSON!");
                    }

                    upsideDownPlayers.clear();
                    if (cosmetics.has("upside_down")) {
                        JsonArray upsideDown = cosmetics.getAsJsonArray("upside_down");
                        for (int i = 0; i < upsideDown.size(); i++) {
                            String playerName = upsideDown.get(i).getAsString();
                            upsideDownPlayers.add(playerName);
                            System.out.println("[LoverMods] Loaded upside down player: " + playerName);
                        }
                    } else {
                        System.out.println("[LoverMods] No 'upside_down' field in JSON!");
                    }

                    loaded = true;
                    System.out.println("[LoverMods] Successfully loaded cosmetics! Capes: " + capeMap.size() + ", Upside down: " + upsideDownPlayers.size());
                } else {
                    System.out.println("[LoverMods] Failed to fetch cosmetics - HTTP " + responseCode);
                }
            } catch (Exception e) {
                System.out.println("[LoverMods] EXCEPTION loading cosmetics: " + e.getClass().getName() + " - " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public static String getCapeForPlayer(String playerName) {
        return capeMap.get(playerName);
    }

    public static boolean isPlayerUpsideDown(String playerName) {
        return upsideDownPlayers.contains(playerName);
    }

    public static boolean isLoaded() {
        return loaded;
    }
}