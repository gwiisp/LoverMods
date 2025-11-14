package gwisp.flight.lovermods.client.achievements;

public class Achievement {
    private final String id;
    private final String title;
    private final String description;
    private final String icon;
    private final AchievementCategory category;
    private final int points;
    private final AchievementRarity rarity;

    private boolean unlocked = false;
    private long unlockedTime = 0;
    private float progress = 0.0f;
    private float maxProgress = 1.0f;

    public Achievement(String id, String title, String description, String icon,
                       AchievementCategory category, int points, AchievementRarity rarity) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.category = category;
        this.points = points;
        this.rarity = rarity;
    }

    public Achievement withProgress(float max) {
        this.maxProgress = max;
        return this;
    }

    public void unlock() {
        if (!unlocked) {
            unlocked = true;
            unlockedTime = System.currentTimeMillis();
            progress = maxProgress;
        }
    }

    public void setProgress(float progress) {
        this.progress = Math.min(progress, maxProgress);
        if (this.progress >= maxProgress && !unlocked) {
            unlock();
        }
    }

    public void incrementProgress(float amount) {
        setProgress(progress + amount);
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public AchievementCategory getCategory() { return category; }
    public int getPoints() { return points; }
    public AchievementRarity getRarity() { return rarity; }
    public boolean isUnlocked() { return unlocked; }
    public long getUnlockedTime() { return unlockedTime; }
    public float getProgress() { return progress; }
    public float getMaxProgress() { return maxProgress; }
    public float getProgressPercent() { return maxProgress > 0 ? (progress / maxProgress) : 0; }

    public enum AchievementCategory {
        GENERAL("General", 0xFFFFFF),
        MCMMO("MCMMO", 0x55FF55),
        TRADING("Trading", 0xFFAA00),
        CRATES("Crates", 0x55FFFF),
        COMBAT("Combat", 0xFF5555),
        SOCIAL("Social", 0xFF55FF);

        private final String name;
        private final int color;

        AchievementCategory(String name, int color) {
            this.name = name;
            this.color = color;
        }

        public String getName() { return name; }
        public int getColor() { return color; }
    }

    public enum AchievementRarity {
        COMMON("Common", 0xAAAAAA, 10),
        UNCOMMON("Uncommon", 0x55FF55, 25),
        RARE("Rare", 0x5555FF, 50),
        EPIC("Epic", 0xAA00AA, 100),
        LEGENDARY("Legendary", 0xFFAA00, 250),
        MYTHIC("Mythic", 0xAA00FF, 500),
        IMPOSSIBLE("Impossible", 0x000000, 5000),
        SPECIAL("Special", 0xFF0000, 5000);

        private final String name;
        private final int color;
        private final int basePoints;

        AchievementRarity(String name, int color, int basePoints) {
            this.name = name;
            this.color = color;
            this.basePoints = basePoints;
        }

        public String getName() { return name; }
        public int getColor() { return color; }
        public int getBasePoints() { return basePoints; }
    }
}