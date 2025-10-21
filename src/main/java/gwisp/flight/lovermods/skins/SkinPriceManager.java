package gwisp.flight.lovermods.skins;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SkinPriceManager {
    private static final String GITHUB_JSON_URL = "https://gwiisp.github.io/lovermods-prices/skins.json";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    private static final Map<String, SkinData> skinPrices = new HashMap<>();
    private static long lastUpdate = 0;

    public static void init() {
        // Initial fetch - do it synchronously so data is ready immediately
        System.out.println("[LoverMods] ===== STARTING SKIN PRICE LOADING =====");
        System.out.println("[LoverMods] URL: " + GITHUB_JSON_URL);

        try {
            System.out.println("[LoverMods] Creating HTTP request...");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GITHUB_JSON_URL))
                    .header("User-Agent", "LoverMods/1.0")
                    .GET()
                    .build();

            System.out.println("[LoverMods] Sending request to GitHub...");
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("[LoverMods] Received response with status: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("[LoverMods] Parsing JSON data...");
                parseAndStorePrices(response.body());
                lastUpdate = System.currentTimeMillis();
                System.out.println("[LoverMods] Successfully loaded " + skinPrices.size() + " skin prices!");
            } else {
                System.err.println("[LoverMods] Failed to fetch prices. Status: " + response.statusCode());
                System.err.println("[LoverMods] Response body: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("[LoverMods] EXCEPTION during initial fetch!");
            System.err.println("[LoverMods] Exception type: " + e.getClass().getName());
            System.err.println("[LoverMods] Exception message: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[LoverMods] ===== SKIN PRICE LOADING COMPLETE =====");

        SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                fetchPrices();
            } catch (Exception e) {
                System.err.println("[LoverMods] Failed to update skin prices: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.HOURS);
    }

    public static void fetchPrices() {
        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GITHUB_JSON_URL))
                        .header("User-Agent", "LoverMods/1.0")
                        .GET()
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    parseAndStorePrices(response.body());
                    lastUpdate = System.currentTimeMillis();
                    System.out.println("[LoverMods] Successfully updated skin prices! Total: " + skinPrices.size());
                } else {
                    System.err.println("[LoverMods] Failed to fetch prices. Status: " + response.statusCode());
                }
            } catch (Exception e) {
                System.err.println("[LoverMods] Error fetching prices: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }


    private static void parseAndStorePrices(String json) {
        try {
            System.out.println("[LoverMods] JSON length: " + json.length() + " characters");
            System.out.println("[LoverMods] First 200 chars of JSON: " + json.substring(0, Math.min(200, json.length())));

            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            System.out.println("[LoverMods] Root JSON keys: " + root.keySet());

            JsonObject skins = root.getAsJsonObject("skins");
            System.out.println("[LoverMods] Found 'skins' object with " + skins.size() + " entries");

            skinPrices.clear();

            skins.entrySet().forEach(entry -> {
                String skinName = entry.getKey();
                JsonObject skinData = entry.getValue().getAsJsonObject();

                String value = skinData.has("value") ? skinData.get("value").getAsString() : "";
                String demand = skinData.has("demand") ? skinData.get("demand").getAsString() : "UNKNOWN";
                String season = skinData.has("season") ? skinData.get("season").getAsString() : "Unknown";
                String set = skinData.has("set") ? skinData.get("set").getAsString() : "";

                skinPrices.put(skinName.toLowerCase(), new SkinData(skinName, value, demand, season, set));
            });

            System.out.println("[LoverMods] Finished parsing. Total skins in map: " + skinPrices.size());

        } catch (Exception e) {
            System.err.println("[LoverMods] EXCEPTION in parseAndStorePrices!");
            System.err.println("[LoverMods] Exception type: " + e.getClass().getName());
            System.err.println("[LoverMods] Exception message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static SkinData getSkinData(String skinName) {
        return skinPrices.get(skinName.toLowerCase());
    }

    public static boolean hasSkinData(String skinName) {
        return skinPrices.containsKey(skinName.toLowerCase());
    }

    public static int getSkinCount() {
        return skinPrices.size();
    }

    public static long getLastUpdateTime() {
        return lastUpdate;
    }

    public static void shutdown() {
        SCHEDULER.shutdown();
    }
}