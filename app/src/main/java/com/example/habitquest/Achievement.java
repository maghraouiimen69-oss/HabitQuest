package com.example.habitquest;

public class Achievement {

    private int id;
    private String name;
    private String description;
    private String requirement;
    private boolean unlocked;
    private String unlockDate;

    public Achievement(int id, String name, String description, String requirement, boolean unlocked, String unlockDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requirement = requirement;
        this.unlocked = unlocked;
        this.unlockDate = unlockDate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getRequirement() {
        return requirement;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public String getUnlockDate() {
        return unlockDate;
    }

    public int getIconRes() {
        if (DatabaseHelper.ACH_EARLY_BIRD.equals(name)) {
            return R.drawable.ic_fire;
        }
        if (DatabaseHelper.ACH_CONSISTENCY.equals(name)) {
            return R.drawable.ic_calendar;
        }
        if (DatabaseHelper.ACH_WEEK_WARRIOR.equals(name)) {
            return R.drawable.ic_bolt;
        }
        if (DatabaseHelper.ACH_HABIT_MASTER.equals(name)) {
            return R.drawable.ic_trophy;
        }
        return R.drawable.ic_star;
    }

    public int getColorRes() {
        if (DatabaseHelper.ACH_EARLY_BIRD.equals(name)) {
            return R.color.accent_peach;
        }
        if (DatabaseHelper.ACH_CONSISTENCY.equals(name)) {
            return R.color.accent_mint;
        }
        if (DatabaseHelper.ACH_WEEK_WARRIOR.equals(name)) {
            return R.color.accent_pink;
        }
        if (DatabaseHelper.ACH_HABIT_MASTER.equals(name)) {
            return R.color.accent_violet;
        }
        return R.color.accent_primary;
    }
}
