package gwisp.flight.lovermods.client.news;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NewsManager {
    private static final String NEWS_URL = "https://gwiisp.github.io/lovermods-prices/news.json";
    private static final List<NewsArticle> articles = new ArrayList<>();
    private static boolean loaded = false;

    public static class NewsArticle {
        private final String title;
        private final String description;
        private final String imageUrl;
        private final String link;

        public NewsArticle(String title, String description, String imageUrl, String link) {
            this.title = title;
            this.description = description;
            this.imageUrl = imageUrl;
            this.link = link;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getLink() {
            return link;
        }
    }

    public static CompletableFuture<Void> loadNews() {
        return CompletableFuture.runAsync(() -> {
            System.out.println("[LoverMods] Loading news from: " + NEWS_URL);
            try {
                URL url = new URL(NEWS_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "LoverMods");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                System.out.println("[LoverMods] News response code: " + responseCode);

                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JsonObject root = JsonParser.parseString(response.toString()).getAsJsonObject();

                    articles.clear();
                    if (root.has("news")) {
                        JsonArray newsArray = root.getAsJsonArray("news");
                        for (int i = 0; i < newsArray.size(); i++) {
                            JsonObject article = newsArray.get(i).getAsJsonObject();

                            String title = article.has("title") ? article.get("title").getAsString() : "Untitled";
                            String description = article.has("description") ? article.get("description").getAsString() : "";
                            String imageUrl = article.has("image") ? article.get("image").getAsString() : "";
                            String link = article.has("link") ? article.get("link").getAsString() : "";

                            articles.add(new NewsArticle(title, description, imageUrl, link));
                            System.out.println("[LoverMods] Loaded news: " + title);
                        }
                    }

                    loaded = true;
                    System.out.println("[LoverMods] Successfully loaded " + articles.size() + " news articles");
                } else {
                    System.out.println("[LoverMods] Failed to fetch news - HTTP " + responseCode);
                }
            } catch (Exception e) {
                System.out.println("[LoverMods] EXCEPTION loading news: " + e.getClass().getName() + " - " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public static List<NewsArticle> getArticles() {
        return new ArrayList<>(articles);
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static int getArticleCount() {
        return articles.size();
    }
}