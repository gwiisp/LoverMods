package gwisp.flight.lovermods.client.mcmmo;

import gwisp.flight.lovermods.client.achievements.AchievementManager;
import gwisp.flight.lovermods.client.mixin.BossBarHudAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.boss.BossBar;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MCMMOTracker {
    private static final Pattern SKILL_PATTERN = Pattern.compile("^(.+?)\\s+Lv\\.([0-9,]+)$");

    private final Map<String, SkillData> activeSkills = new HashMap<>();

    private String currentSkill = null;
    private int currentLevel = 0;
    private float currentXPPercent = 0.0f;
    private long lastXPGain = 0;

    public void update() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            reset();
            return;
        }

        BossBarHudAccessor bossBarHud = (BossBarHudAccessor) client.inGameHud.getBossBarHud();
        Map<UUID, BossBar> bossBars = bossBarHud.getBossBars();

        if (bossBars.isEmpty()) {
            cleanupOldSkills();
            return;
        }

        Set<String> seenSkills = new HashSet<>();
        long now = System.currentTimeMillis();

        for (BossBar bossBar : bossBars.values()) {
            String bossBarText = bossBar.getName().getString().trim();
            Matcher matcher = SKILL_PATTERN.matcher(bossBarText);

            if (matcher.matches()) {
                String skillName = matcher.group(1).trim();
                int level = Integer.parseInt(matcher.group(2).replace(",", ""));
                float xpPercent = bossBar.getPercent();

                seenSkills.add(skillName);
                SkillData skill = activeSkills.computeIfAbsent(skillName, k -> new SkillData(skillName, level, xpPercent));

                skill.update(level, xpPercent, now);
                AchievementManager.onMCMMOSkillGained(skillName, level);
            }
        }

        activeSkills.entrySet().removeIf(entry -> now - entry.getValue().lastSeen > 3000);

        if (!activeSkills.isEmpty()) {
            SkillData latest = activeSkills.values().stream()
                    .max(Comparator.comparingLong(s -> s.lastSeen))
                    .orElse(null);

            if (latest != null) {
                currentSkill = latest.skillName;
                currentLevel = latest.currentLevel;
                currentXPPercent = latest.currentXPPercent;
                lastXPGain = latest.lastSeen;
            }
        } else {
            reset();
        }
    }

    private void cleanupOldSkills() {
        long now = System.currentTimeMillis();
        activeSkills.entrySet().removeIf(entry -> now - entry.getValue().lastSeen > 3000);

        if (activeSkills.isEmpty()) {
            reset();
        }
    }

    private void reset() {
        currentSkill = null;
        currentLevel = 0;
        currentXPPercent = 0.0f;
        lastXPGain = 0;
        activeSkills.clear();
    }

    public boolean isActive() {
        return !activeSkills.isEmpty() || currentSkill != null;
    }

    public String getCurrentSkill() {
        return currentSkill;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public float getCurrentXPPercent() {
        return currentXPPercent;
    }

    public String getEstimatedTimeToLevel() {
        if (currentSkill == null || !activeSkills.containsKey(currentSkill))
            return "Calculating...";

        return activeSkills.get(currentSkill).getEstimatedTimeToLevel();
    }

    public List<SkillData> getActiveSkills() {
        return new ArrayList<>(activeSkills.values());
    }

    public static class SkillData {
        public final String skillName;
        public int currentLevel;
        public float currentXPPercent;

        private long skillStartTime;
        private int startLevel;
        private long lastSeen;

        private final Map<Long, Float> xpHistory = new HashMap<>();

        public SkillData(String skillName, int startLevel, float startXP) {
            this.skillName = skillName;
            this.currentLevel = startLevel;
            this.currentXPPercent = startXP;
            this.startLevel = startLevel;
            this.skillStartTime = System.currentTimeMillis();
            this.lastSeen = System.currentTimeMillis();
        }

        public void update(int newLevel, float newXP, long now) {
            if (newLevel > currentLevel || newXP > currentXPPercent) {
                float xpGained = (newLevel > currentLevel)
                        ? (1.0f - currentXPPercent) + newXP
                        : (newXP - currentXPPercent);
                recordXPGain(now, xpGained);
            }

            this.currentLevel = newLevel;
            this.currentXPPercent = newXP;
            this.lastSeen = now;
        }

        private void recordXPGain(long timestamp, float xpGained) {
            xpHistory.put(timestamp, xpGained);
            xpHistory.entrySet().removeIf(entry -> timestamp - entry.getKey() > 60000);
        }

        public String getEstimatedTimeToLevel() {
            if (xpHistory.isEmpty() || currentXPPercent >= 0.99f)
                return "Calculating...";

            float totalXP = 0;
            long oldest = Long.MAX_VALUE, newest = 0;

            for (var e : xpHistory.entrySet()) {
                totalXP += e.getValue();
                oldest = Math.min(oldest, e.getKey());
                newest = Math.max(newest, e.getKey());
            }

            if (newest <= oldest) return "Calculating...";
            float seconds = (newest - oldest) / 1000f;
            float rate = totalXP / seconds;

            if (rate <= 0) return "Calculating...";

            float remaining = 1.0f - currentXPPercent;
            int eta = (int) (remaining / rate);
            return formatTime(eta);
        }

        private String formatTime(int seconds) {
            if (seconds < 60) return seconds + "s";
            if (seconds < 3600) return (seconds / 60) + "m " + (seconds % 60) + "s";
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }
}
