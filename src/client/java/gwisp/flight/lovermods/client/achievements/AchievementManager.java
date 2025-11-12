package gwisp.flight.lovermods.client.achievements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class AchievementManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path SAVE_FILE = MinecraftClient.getInstance().runDirectory.toPath()
            .resolve("config/lovermods_achievements.json");

    private static final Map<String, Achievement> achievements = new LinkedHashMap<>();
    private static final List<AchievementListener> listeners = new ArrayList<>();

    private static final Set<String> mcmmoSkillsSeen = new HashSet<>();

    static {
        registerAchievements();
        loadProgress();
    }

    private static void registerAchievements() {
        register(new Achievement(
                "mcmmo_first_skill",
                "Baby Steps",
                "Gain XP in any MCMMO skill",
                "🎯",
                Achievement.AchievementCategory.MCMMO,
                10,
                Achievement.AchievementRarity.COMMON
        ));

        register(new Achievement(
                "mcmmo_skill_variety",
                "Jack of All Trades",
                "Gain XP in 5 different MCMMO skills in one session",
                "⭐",
                Achievement.AchievementCategory.MCMMO,
                50,
                Achievement.AchievementRarity.RARE
        ).withProgress(5));

        register(new Achievement(
                "mcmmo_master",
                "Master of Skills",
                "Gain XP in 10 different MCMMO skills in one session",
                "👑",
                Achievement.AchievementCategory.MCMMO,
                100,
                Achievement.AchievementRarity.EPIC
        ).withProgress(10));

        register(new Achievement(
                "mcmmo_level_100",
                "Century Club",
                "Reach level 100 in any MCMMO skill",
                "💯",
                Achievement.AchievementCategory.MCMMO,
                75,
                Achievement.AchievementRarity.RARE
        ));

        register(new Achievement(
                "mcmmo_level_1000",
                "Legendary Grinder",
                "Reach level 1,000 in any MCMMO skill",
                "🔥",
                Achievement.AchievementCategory.MCMMO,
                200,
                Achievement.AchievementRarity.LEGENDARY
        ));

        register(new Achievement(
                "mcmmo_level_5000",
                "Legendary Grinder",
                "Reach level 5,000 in any MCMMO skill",
                "🔥",
                Achievement.AchievementCategory.MCMMO,
                500,
                Achievement.AchievementRarity.LEGENDARY
        ));

        register(new Achievement(
                "mcmmo_level_10000",
                "Legendary Grinder",
                "Reach level 10,000 in any MCMMO skill",
                "🔥",
                Achievement.AchievementCategory.MCMMO,
                1000,
                Achievement.AchievementRarity.MYTHIC
        ));

        register(new Achievement(
                "trade_menu_opened",
                "Window Shopping",
                "Open a trade menu",
                "🛒",
                Achievement.AchievementCategory.TRADING,
                10,
                Achievement.AchievementRarity.COMMON
        ));

        register(new Achievement(
                "trade_10_times",
                "Regular Customer",
                "Open trade menus 10 times",
                "🤝",
                Achievement.AchievementCategory.TRADING,
                25,
                Achievement.AchievementRarity.UNCOMMON
        ).withProgress(10));

        register(new Achievement(
                "trade_100_times",
                "Trade Tycoon",
                "Open trade menus 100 times",
                "💰",
                Achievement.AchievementCategory.TRADING,
                100,
                Achievement.AchievementRarity.EPIC
        ).withProgress(100));

        register(new Achievement(
                "mod_installed",
                "Welcome!",
                "Install LoverMods",
                "✨",
                Achievement.AchievementCategory.GENERAL,
                5,
                Achievement.AchievementRarity.COMMON
        ));

        register(new Achievement(
                "news_reader",
                "Stay Informed",
                "Click on the news widget",
                "📰",
                Achievement.AchievementCategory.GENERAL,
                10,
                Achievement.AchievementRarity.COMMON
        ));

        register(new Achievement(
                "holy_collexl",
                "True Believer",
                "Say the holy phrase in chat",
                "🙏",
                Achievement.AchievementCategory.SOCIAL,
                10000,
                Achievement.AchievementRarity.MYTHIC
        ));

        register(new Achievement(
                "doesnt_work_lol",
                "Reach The End.",
                "Reach the end of life.",
                "📰",
                Achievement.AchievementCategory.GENERAL,
                100000,
                Achievement.AchievementRarity.LEGENDARY
        ));
    }

    public static void register(Achievement achievement) {
        achievements.put(achievement.getId(), achievement);
    }

    public static Achievement get(String id) {
        return achievements.get(id);
    }

    public static Collection<Achievement> getAll() {
        return achievements.values();
    }

    public static List<Achievement> getByCategory(Achievement.AchievementCategory category) {
        return achievements.values().stream()
                .filter(a -> a.getCategory() == category)
                .collect(Collectors.toList());
    }

    public static void unlock(String id) {
        Achievement achievement = achievements.get(id);
        if (achievement != null && !achievement.isUnlocked()) {
            achievement.unlock();
            saveProgress();
            notifyListeners(achievement);
            System.out.println("[LoverMods] Achievement unlocked: " + achievement.getTitle());
        }
    }

    public static void setProgress(String id, float progress) {
        Achievement achievement = achievements.get(id);
        if (achievement != null) {
            boolean wasUnlocked = achievement.isUnlocked();
            achievement.setProgress(progress);

            if (!wasUnlocked && achievement.isUnlocked()) {
                saveProgress();
                notifyListeners(achievement);
                System.out.println("[LoverMods] Achievement unlocked: " + achievement.getTitle());
            } else {
                saveProgress();
            }
        }
    }

    public static void incrementProgress(String id, float amount) {
        Achievement achievement = achievements.get(id);
        if (achievement != null) {
            boolean wasUnlocked = achievement.isUnlocked();
            achievement.incrementProgress(amount);

            if (!wasUnlocked && achievement.isUnlocked()) {
                saveProgress();
                notifyListeners(achievement);
                System.out.println("[LoverMods] Achievement unlocked: " + achievement.getTitle());
            } else {
                saveProgress();
            }
        }
    }

    public static void onMCMMOSkillGained(String skill, int level) {
        unlock("mcmmo_first_skill");

        if (!mcmmoSkillsSeen.contains(skill)) {
            mcmmoSkillsSeen.add(skill);
            setProgress("mcmmo_skill_variety", mcmmoSkillsSeen.size());
            setProgress("mcmmo_master", mcmmoSkillsSeen.size());
        }

        if (level >= 100) {
            unlock("mcmmo_level_100");
        }
        if (level >= 1000) {
            unlock("mcmmo_level_1000");
        }
        if (level >= 5000) {
            unlock("mcmmo_level_5000");
        }
        if (level >= 10000) {
            unlock("mcmmo_level_10000");
        }
    }

    public static void onTradeMenuOpened() {
        unlock("trade_menu_opened");
        incrementProgress("trade_10_times", 1);
        incrementProgress("trade_100_times", 1);
    }

    public static void onNewsClicked() {
        unlock("news_reader");
    }

    public static void onChatMessage(String message) {
        String normalized = message.toLowerCase().trim();
        if (normalized.equals("i am one with him. the one and only holy collexl.")) {
            unlock("holy_collexl");
        }
    }

    public static int getTotalPoints() {
        return achievements.values().stream()
                .filter(Achievement::isUnlocked)
                .mapToInt(Achievement::getPoints)
                .sum();
    }

    public static int getUnlockedCount() {
        return (int) achievements.values().stream()
                .filter(Achievement::isUnlocked)
                .count();
    }

    public static int getTotalCount() {
        return achievements.size();
    }

    public static void addListener(AchievementListener listener) {
        listeners.add(listener);
    }

    private static void notifyListeners(Achievement achievement) {
        for (AchievementListener listener : listeners) {
            listener.onAchievementUnlocked(achievement);
        }
    }

    private static void saveProgress() {
        try {
            Files.createDirectories(SAVE_FILE.getParent());

            Map<String, AchievementData> data = new HashMap<>();
            for (Achievement achievement : achievements.values()) {
                if (achievement.isUnlocked() || achievement.getProgress() > 0) {
                    data.put(achievement.getId(), new AchievementData(
                            achievement.isUnlocked(),
                            achievement.getUnlockedTime(),
                            achievement.getProgress()
                    ));
                }
            }

            try (Writer writer = new FileWriter(SAVE_FILE.toFile())) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("[LoverMods] Failed to save achievements: " + e.getMessage());
        }
    }

    private static void loadProgress() {
        if (!Files.exists(SAVE_FILE)) {
            unlock("mod_installed");
            return;
        }

        try (Reader reader = new FileReader(SAVE_FILE.toFile())) {
            Map<String, AchievementData> data = GSON.fromJson(reader,
                    new com.google.gson.reflect.TypeToken<Map<String, AchievementData>>(){}.getType());

            if (data != null) {
                for (Map.Entry<String, AchievementData> entry : data.entrySet()) {
                    Achievement achievement = achievements.get(entry.getKey());
                    if (achievement != null) {
                        AchievementData achData = entry.getValue();
                        achievement.setProgress(achData.progress);
                        if (achData.unlocked && !achievement.isUnlocked()) {
                            achievement.unlock();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[LoverMods] Failed to load achievements: " + e.getMessage());
        }

        unlock("mod_installed");
    }

    private static class AchievementData {
        boolean unlocked;
        long unlockedTime;
        float progress;

        AchievementData(boolean unlocked, long unlockedTime, float progress) {
            this.unlocked = unlocked;
            this.unlockedTime = unlockedTime;
            this.progress = progress;
        }
    }

    public interface AchievementListener {
        void onAchievementUnlocked(Achievement achievement);
    }
}