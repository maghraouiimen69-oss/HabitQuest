package com.example.habitquest;

public final class LevelSystem {

    private static final int[] THRESHOLDS = {0, 50, 150, 300, 500, 700, 900, 1100, 1300, 1500};

    private LevelSystem() {
    }

    public static int xpForLevel(int level) {
        if (level <= 1) {
            return 0;
        }
        if (level <= THRESHOLDS.length) {
            return THRESHOLDS[level - 1];
        }
        return THRESHOLDS[THRESHOLDS.length - 1] + (level - THRESHOLDS.length) * 300;
    }

    public static int levelForXp(int xp) {
        int level = 1;
        while (xp >= xpForLevel(level + 1)) {
            level++;
        }
        return level;
    }

    public static int xpIntoLevel(int xp) {
        int level = levelForXp(xp);
        return xp - xpForLevel(level);
    }

    public static int xpForNextLevel(int xp) {
        int level = levelForXp(xp);
        return xpForLevel(level + 1) - xpForLevel(level);
    }

    public static int nextLevelXp(int xp) {
        return xpForLevel(levelForXp(xp) + 1);
    }

    public static String titleForLevel(int level) {
        switch (level) {
            case 1:
                return "Beginner";
            case 2:
                return "Explorer";
            case 3:
                return "Apprentice";
            case 4:
                return "Challenger";
            case 5:
                return "Habit Hero";
            case 6:
                return "Trailblazer";
            case 7:
                return "Champion";
            case 8:
                return "Virtuoso";
            case 9:
                return "Grandmaster";
            default:
                return "Legend";
        }
    }
}
