package gwisp.flight.lovermods.client.splash;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class SplashTextManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("LoverMods");
    private static final String SPLASH_URL = "https://gwiisp.github.io/lovermods-prices/splashes.json";
    private static final Random RANDOM = new Random();

    private static List<String> customSplashes = new ArrayList<>();
    private static boolean loaded = false;

    public static CompletableFuture<Void> loadSplashes() {
        return CompletableFuture.runAsync(() -> {
            System.out.println("[LoverMods] Attempting to load custom splashes from: " + SPLASH_URL);
            try {
                URL url = new URL(SPLASH_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "LoverMods");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                System.out.println("[LoverMods] Splash fetch response code: " + responseCode);

                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    System.out.println("[LoverMods] Raw splash JSON: " + response.toString());

                    JsonArray splashArray = JsonParser.parseString(response.toString()).getAsJsonArray();
                    customSplashes.clear();

                    for (int i = 0; i < splashArray.size(); i++) {
                        customSplashes.add(splashArray.get(i).getAsString());
                    }

                    loaded = true;
                    System.out.println("[LoverMods] Successfully loaded " + customSplashes.size() + " custom splash texts!");
                    System.out.println("[LoverMods] Custom splashes: " + customSplashes);
                } else {
                    System.out.println("[LoverMods] Failed to load splashes - bad response code");
                }
            } catch (Exception e) {
                System.out.println("[LoverMods] Error loading custom splashes: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public static String getRandomSplash() {
        System.out.println("[LoverMods] getRandomSplash called - loaded: " + loaded + ", size: " + customSplashes.size());
        if (!loaded || customSplashes.isEmpty()) {
            System.out.println("[LoverMods] Returning null (use vanilla splash)");
            return null;
        }
        String splash = customSplashes.get(RANDOM.nextInt(customSplashes.size()));
        System.out.println("[LoverMods] Returning custom splash: " + splash);
        return splash;
    }

    public static boolean hasCustomSplashes() {
        return loaded && !customSplashes.isEmpty();
    }
}