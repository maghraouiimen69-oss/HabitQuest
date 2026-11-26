package com.example.habitquest;

public class Habit {

    public static final String CAT_HEALTH = "Health";
    public static final String CAT_FITNESS = "Fitness";
    public static final String CAT_EDUCATION = "Education";
    public static final String CAT_PRODUCTIVITY = "Productivity";
    public static final String CAT_PERSONAL = "Personal Development";

    public static final String DIFF_EASY = "Easy";
    public static final String DIFF_MEDIUM = "Medium";
    public static final String DIFF_HARD = "Hard";

    private int id;
    private int userId;
    private String name;
    private String description;
    private String category;
    private String difficulty;
    private int xpReward;
    private String creationDate;
    private String reminderTime;
    private boolean completedToday;

    public Habit() {
        this.category = CAT_HEALTH;
        this.difficulty = DIFF_EASY;
        this.xpReward = 10;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean hasReminder() {
        return reminderTime != null && !reminderTime.trim().isEmpty();
    }

    public boolean isCompletedToday() {
        return completedToday;
    }

    public void setCompletedToday(boolean completedToday) {
        this.completedToday = completedToday;
    }

    public static int xpForDifficulty(String difficulty) {
        if (DIFF_HARD.equals(difficulty)) {
            return 30;
        }
        if (DIFF_MEDIUM.equals(difficulty)) {
            return 20;
        }
        return 10;
    }

    public int getCategoryIconRes() {
        if (CAT_FITNESS.equals(category)) {
            return R.drawable.ic_fitness;
        }
        if (CAT_EDUCATION.equals(category)) {
            return R.drawable.ic_book;
        }
        if (CAT_PRODUCTIVITY.equals(category)) {
            return R.drawable.ic_bolt;
        }
        if (CAT_PERSONAL.equals(category)) {
            return R.drawable.ic_sparkle;
        }
        return R.drawable.ic_heart;
    }

    public int getCategoryColorRes() {
        if (CAT_FITNESS.equals(category)) {
            return R.color.cat_fitness;
        }
        if (CAT_EDUCATION.equals(category)) {
            return R.color.cat_education;
        }
        if (CAT_PRODUCTIVITY.equals(category)) {
            return R.color.cat_productivity;
        }
        if (CAT_PERSONAL.equals(category)) {
            return R.color.cat_personal;
        }
        return R.color.cat_health;
    }

    public int getDifficultyColorRes() {
        if (DIFF_HARD.equals(difficulty)) {
            return R.color.diff_hard;
        }
        if (DIFF_MEDIUM.equals(difficulty)) {
            return R.color.diff_medium;
        }
        return R.color.diff_easy;
    }
}
