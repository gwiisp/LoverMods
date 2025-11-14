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
                Achievement.AchievementRarity.UNCOMMON
        ));

        register(new Achievement(
                "mcmmo_level_1000",
                "Legendary Grinder",
                "Reach level 1,000 in any MCMMO skill",
                "🔥",
                Achievement.AchievementCategory.MCMMO,
                500,
                Achievement.AchievementRarity.RARE
        ));

        register(new Achievement(
                "mcmmo_level_5000",
                "Mythic Grinder",
                "Reach level 5,000 in any MCMMO skill",
                "🔥",
                Achievement.AchievementCategory.MCMMO,
                1500,
                Achievement.AchievementRarity.LEGENDARY
        ));

        register(new Achievement(
                "mcmmo_level_10000",
                "Diamond Grinder",
                "Reach level 10,000 in any MCMMO skill",
                "🔥",
                Achievement.AchievementCategory.MCMMO,
                5000,
                Achievement.AchievementRarity.MYTHIC
        ));

        register(new Achievement(
                "mcmmo_level_100000",
                "Uhhh Grinder..??",
                "Reach level 100,000 in any MCMMO skill. This isnt actually possible dont even bother",
                "🔥",
                Achievement.AchievementCategory.MCMMO,
                100000,
                Achievement.AchievementRarity.IMPOSSIBLE
        ));

        register(new Achievement(
                "first_crate",
                "Lucky Box",
                "Open your first crate",
                "📦",
                Achievement.AchievementCategory.CRATES,
                10,
                Achievement.AchievementRarity.COMMON
        ));

        register(new Achievement(
                "open_10_crates",
                "Crate Enthusiast",
                "Open 10 crates",
                "🎁",
                Achievement.AchievementCategory.CRATES,
                25,
                Achievement.AchievementRarity.UNCOMMON
        ).withProgress(10));

        register(new Achievement(
                "open_50_crates",
                "Crate Hunter",
                "Open 50 crates",
                "🎰",
                Achievement.AchievementCategory.CRATES,
                75,
                Achievement.AchievementRarity.RARE
        ).withProgress(50));

        register(new Achievement(
                "open_100_crates",
                "Crate Master",
                "Open 100 crates",
                "👑",
                Achievement.AchievementCategory.CRATES,
                150,
                Achievement.AchievementRarity.LEGENDARY
        ).withProgress(100));

        register(new Achievement(
                "open_500_crates",
                "Crate Legend",
                "Open 500 crates",
                "💎",
                Achievement.AchievementCategory.CRATES,
                250,
                Achievement.AchievementRarity.MYTHIC
        ).withProgress(500));

        register(new Achievement(
                "daily_crate",
                "Daily Grinder",
                "Open a Daily Crate",
                "📅",
                Achievement.AchievementCategory.CRATES,
                15,
                Achievement.AchievementRarity.COMMON
        ));

        register(new Achievement(
                "vote_crate",
                "Democracy",
                "Open a Vote Crate",
                "🗳️",
                Achievement.AchievementCategory.CRATES,
                15,
                Achievement.AchievementRarity.COMMON
        ));

        register(new Achievement(
                "flipper_crate",
                "Flipper Master",
                "Open a Flipper Crate",
                "🐬",
                Achievement.AchievementCategory.CRATES,
                25,
                Achievement.AchievementRarity.UNCOMMON
        ));

        register(new Achievement(
                "invader_crate",
                "Space Invader",
                "Open an Invader Crate",
                "👾",
                Achievement.AchievementCategory.CRATES,
                50,
                Achievement.AchievementRarity.RARE
        ));

        register(new Achievement(
                "risk_it_crate",
                "High Roller",
                "Open a Risk It Crate",
                "🎲",
                Achievement.AchievementCategory.CRATES,
                75,
                Achievement.AchievementRarity.EPIC
        ));

        register(new Achievement(
                "loverflex_crate",
                "Flexing Hard",
                "Open a Loverflex Crate",
                "💪",
                Achievement.AchievementCategory.CRATES,
                1000,
                Achievement.AchievementRarity.LEGENDARY
        ));

        register(new Achievement(
                "lover_noble_crate",
                "Noble Unboxing",
                "Open a Lover Noble Crate",
                "🎩",
                Achievement.AchievementCategory.CRATES,
                5000,
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
                "holy_collexl",
                "True Believer",
                "Say the holy phrase in chat",
                "🙏",
                Achievement.AchievementCategory.SOCIAL,
                1000,
                Achievement.AchievementRarity.SPECIAL
        ));

        register(new Achievement(
                "doesnt_work_lol",
                "Reach The End.",
                "Reach the end of life.",
                "📰",
                Achievement.AchievementCategory.GENERAL,
                500,
                Achievement.AchievementRarity.SPECIAL
        ));

        register(new Achievement(
                "lunargato_nuub",
                "Certified Nuub",
                "Someones a nuub xD",
                "🤡",
                Achievement.AchievementCategory.SOCIAL,
                50,
                Achievement.AchievementRarity.SPECIAL
        ));

        register(new Achievement(
                "cisland",
                "Hello Collexl Island!",
                "Live in your new paradise!",
                "☀",
                Achievement.AchievementCategory.GENERAL,
                250,
                Achievement.AchievementRarity.SPECIAL
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
        if (level >= 100000) {
            unlock("mcmmo_level_100000");
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

    public static void onChatReceived(String message) {
        if (message == null) return;

        String normalized = message
                .replace("’", "'")
                .replaceAll("§.", "")
                .toLowerCase()
                .trim();

        if (normalized.contains("entering collexl") || normalized.contains("collexl's claimed land")) {
            unlock("cisland");
            System.out.println("[LoverMods] Entered Collexl's claimed land — unlocked cisland");
        }
    }

    public static void onCrateOpened(String crateType) {
        unlock("first_crate");
        incrementProgress("open_10_crates", 1);
        incrementProgress("open_50_crates", 1);
        incrementProgress("open_100_crates", 1);
        incrementProgress("open_500_crates", 1);

        String normalized = crateType.toLowerCase().trim();

        if (normalized.contains("daily")) {
            unlock("daily_crate");
        } else if (normalized.contains("vote")) {
            unlock("vote_crate");
        } else if (normalized.contains("flipper")) {
            unlock("flipper_crate");
        } else if (normalized.contains("invader")) {
            unlock("invader_crate");
        } else if (normalized.contains("risk")) {
            unlock("risk_it_crate");
        } else if (normalized.contains("loverflex")) {
            unlock("loverflex_crate");
        } else if (normalized.contains("noble")) {
            unlock("lover_noble_crate");
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

    /**
     * FOR TESTING ONLY - Unlocks all achievements
     */
    public static void unlockAll() {
        for (Achievement achievement : achievements.values()) {
            if (!achievement.isUnlocked()) {
                achievement.unlock();
            }
        }
        saveProgress();
        System.out.println("[LoverMods] All achievements unlocked (TEST MODE)");
    }

    public static void resetAll() {
        for (Achievement achievement : achievements.values()) {
            achievement.setProgress(0);
            if (achievement.isUnlocked()) {
                achievement.setProgress(0);
            }
        }
        mcmmoSkillsSeen.clear();

        try {
            if (Files.exists(SAVE_FILE)) {
                Files.delete(SAVE_FILE);
            }
        } catch (IOException e) {
            System.err.println("[LoverMods] Failed to delete save file: " + e.getMessage());
        }

        achievements.clear();
        registerAchievements();

        unlock("mod_installed");

        System.out.println("[LoverMods] All achievements reset (TEST MODE)");
    }

    public static void onCommandSent(String command) {
        String normalized = command.toLowerCase().trim();

        if (normalized.startsWith("/msg lunargato") || normalized.startsWith("/w lunargato") ||
                normalized.startsWith("/tell lunargato") || normalized.startsWith("/whisper lunargato")) {

            if (normalized.contains("nuub")) {
                unlock("lunargato_nuub");
            }
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