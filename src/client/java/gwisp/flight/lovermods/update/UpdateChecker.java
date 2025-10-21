package gwisp.flight.lovermods.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger("LoverMods");
    private static final String MODRINTH_API = "https://api.modrinth.com/v2/project/YOUR_PROJECT_ID/version";
    private static final String GITHUB_API = "https://api.github.com/repos/gwiisp/lovermods/releases/latest";

    private static String latestVersion = null;
    private static String updateSummary = null;
    private static String downloadUrl = null;

    public static CompletableFuture<UpdateInfo> checkForUpdates() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String currentVersion = FabricLoader.getInstance()
                        .getModContainer("lovermods")
                        .get()
                        .getMetadata()
                        .getVersion()
                        .getFriendlyString();

                UpdateInfo modrinthUpdate = checkModrinth(currentVersion);
                if (modrinthUpdate != null && modrinthUpdate.hasUpdate()) {
                    return modrinthUpdate;
                }

                UpdateInfo githubUpdate = checkGitHub(currentVersion);
                if (githubUpdate != null && githubUpdate.hasUpdate()) {
                    return githubUpdate;
                }

                return new UpdateInfo(false, currentVersion, currentVersion, null, null);
            } catch (Exception e) {
                LOGGER.error("Failed to check for updates", e);
                return null;
            }
        });
    }

    public static UpdateInfo checkModrinth(String currentVersion) {
        try {
            URL url = new URL(MODRINTH_API);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "LoverMods/" + currentVersion);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JsonArray versions = JsonParser.parseString(response.toString()).getAsJsonArray();
                if (versions.size() > 0) {
                    JsonObject latestVersionObj = versions.get(0).getAsJsonObject();
                    String latestVer = latestVersionObj.get("version_number").getAsString();
                    String changelog = latestVersionObj.has("changelog") ?
                            latestVersionObj.get("changelog").getAsString() : "No changelog available";

                    JsonArray files = latestVersionObj.getAsJsonArray("files");
                    String downloadUrl = files.size() > 0 ?
                            files.get(0).getAsJsonObject().get("url").getAsString() : null;

                    boolean hasUpdate = isNewerVersion(currentVersion, latestVer);
                    return new UpdateInfo(hasUpdate, currentVersion, latestVer, changelog, downloadUrl);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to check Modrinth for updates: {}", e.getMessage());
        }
        return null;
    }

    private static UpdateInfo checkGitHub(String currentVersion) {
        try {
            URL url = new URL(GITHUB_API);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "LoverMods/" + currentVersion);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JsonObject release = JsonParser.parseString(response.toString()).getAsJsonObject();
                String latestVer = release.get("tag_name").getAsString().replace("v", "");
                String changelog = release.has("body") ?
                        release.get("body").getAsString() : "No changelog available";

                JsonArray assets = release.getAsJsonArray("assets");
                String downloadUrl = null;
                if (assets.size() > 0) {
                    for (int i = 0; i < assets.size(); i++) {
                        JsonObject asset = assets.get(i).getAsJsonObject();
                        String name = asset.get("name").getAsString();
                        if (name.endsWith(".jar") && !name.contains("sources")) {
                            downloadUrl = asset.get("browser_download_url").getAsString();
                            break;
                        }
                    }
                }

                boolean hasUpdate = isNewerVersion(currentVersion, latestVer);
                return new UpdateInfo(hasUpdate, currentVersion, latestVer, changelog, downloadUrl);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to check GitHub for updates: {}", e.getMessage());
        }
        return null;
    }

    private static boolean isNewerVersion(String current, String latest) {
        try {
            String[] currentParts = current.split("\\.");
            String[] latestParts = latest.split("\\.");

            int length = Math.max(currentParts.length, latestParts.length);
            for (int i = 0; i < length; i++) {
                int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
                int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;

                if (latestPart > currentPart) {
                    return true;
                } else if (latestPart < currentPart) {
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static class UpdateInfo {
        private final boolean hasUpdate;
        private final String currentVersion;
        private final String latestVersion;
        private final String changelog;
        private final String downloadUrl;

        public UpdateInfo(boolean hasUpdate, String currentVersion, String latestVersion,
                          String changelog, String downloadUrl) {
            this.hasUpdate = hasUpdate;
            this.currentVersion = currentVersion;
            this.latestVersion = latestVersion;
            this.changelog = changelog;
            this.downloadUrl = downloadUrl;
        }

        public boolean hasUpdate() {
            return hasUpdate;
        }

        public String getCurrentVersion() {
            return currentVersion;
        }

        public String getLatestVersion() {
            return latestVersion;
        }

        public String getChangelog() {
            return changelog;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }
    }
}