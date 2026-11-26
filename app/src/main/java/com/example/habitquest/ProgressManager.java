package com.example.habitquest;

import java.util.ArrayList;
import java.util.List;

public final class ProgressManager {

    public static class Reward {
        public boolean earned;
        public int xp;
        public boolean leveledUp;
        public int level;
        public final List<String> unlocked = new ArrayList<>();
    }

    private ProgressManager() {
    }

    public static Reward completeHabit(DatabaseHelper db, User user, Habit habit) {
        Reward reward = new Reward();
        String today = HabitStats.today();
        if (!db.markCompleted(habit.getId(), user.getId(), habit.getXpReward(), today)) {
            return reward;
        }
        int previousLevel = user.getLevel();
        int newXp = user.getXp() + habit.getXpReward();
        int newLevel = LevelSystem.levelForXp(newXp);
        db.updateUserProgress(user.getId(), newXp, newLevel);
        user.setXp(newXp);
        user.setLevel(newLevel);

        reward.earned = true;
        reward.xp = habit.getXpReward();
        reward.leveledUp = newLevel > previousLevel;
        reward.level = newLevel;
        reward.unlocked.addAll(evaluateAchievements(db, user));
        return reward;
    }

    public static List<String> evaluateAchievements(DatabaseHelper db, User user) {
        List<String> newlyUnlocked = new ArrayList<>();
        int userId = user.getId();

        if (db.getTotalCompletions(userId) >= 1 && db.unlockAchievement(userId, DatabaseHelper.ACH_FIRST_STEP)) {
            newlyUnlocked.add(DatabaseHelper.ACH_FIRST_STEP);
        }
        if (db.getCompletionsOn(userId, HabitStats.today()) >= 3 && db.unlockAchievement(userId, DatabaseHelper.ACH_EARLY_BIRD)) {
            newlyUnlocked.add(DatabaseHelper.ACH_EARLY_BIRD);
        }
        int streak = HabitStats.currentStreak(db.getDistinctCompletionDates(userId));
        if (streak >= 3 && db.unlockAchievement(userId, DatabaseHelper.ACH_CONSISTENCY)) {
            newlyUnlocked.add(DatabaseHelper.ACH_CONSISTENCY);
        }
        if (streak >= 7 && db.unlockAchievement(userId, DatabaseHelper.ACH_WEEK_WARRIOR)) {
            newlyUnlocked.add(DatabaseHelper.ACH_WEEK_WARRIOR);
        }
        if (user.getLevel() >= 10 && db.unlockAchievement(userId, DatabaseHelper.ACH_HABIT_MASTER)) {
            newlyUnlocked.add(DatabaseHelper.ACH_HABIT_MASTER);
        }
        return newlyUnlocked;
    }

    public static int motivationFor(int done, int total) {
        if (total == 0) {
            return R.string.motivation_empty;
        }
        if (done == 0) {
            return R.string.motivation_start;
        }
        if (done < total) {
            return R.string.motivation_almost;
        }
        return R.string.motivation_done;
    }
}
