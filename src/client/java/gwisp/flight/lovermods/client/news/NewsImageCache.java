package gwisp.flight.lovermods.client.news;

import net.minecraft.util.Identifier;
import java.util.concurrent.ConcurrentHashMap;

public class NewsImageCache {
    private static final ConcurrentHashMap<String, Identifier> cache = new ConcurrentHashMap<>();

    public static boolean has(String url) {
        return cache.containsKey(url);
    }

    public static Identifier get(String url) {
        return cache.get(url);
    }

    public static void put(String url, Identifier id) {
        cache.put(url, id);
    }
}
