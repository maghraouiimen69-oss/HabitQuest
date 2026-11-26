package com.example.habitquest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "habitquest.db";
    private static final int DB_VERSION = 2;

    private static final String T_USERS = "users";
    private static final String T_HABITS = "habits";
    private static final String T_HISTORY = "habit_history";
    private static final String T_ACHIEVEMENTS = "achievements";

    public static final String ACH_FIRST_STEP = "First Step";
    public static final String ACH_EARLY_BIRD = "Early Bird";
    public static final String ACH_CONSISTENCY = "Consistency";
    public static final String ACH_WEEK_WARRIOR = "Week Warrior";
    public static final String ACH_HABIT_MASTER = "Habit Master";

    private static final String[][] ACHIEVEMENT_CATALOG = {
            {ACH_FIRST_STEP, "Complete your first habit", "1 completion"},
            {ACH_EARLY_BIRD, "Complete three habits in one day", "3 in a day"},
            {ACH_CONSISTENCY, "Keep a three-day streak", "3-day streak"},
            {ACH_WEEK_WARRIOR, "Keep a seven-day streak", "7-day streak"},
            {ACH_HABIT_MASTER, "Reach level 10", "Level 10"}
    };

    private static final int PBKDF2_ITERATIONS = 30000;
    private static final int PBKDF2_KEY_LENGTH = 256;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_USERS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL UNIQUE, "
                + "email TEXT NOT NULL UNIQUE, "
                + "password TEXT NOT NULL, "
                + "xp INTEGER NOT NULL DEFAULT 0, "
                + "level INTEGER NOT NULL DEFAULT 1, "
                + "created_at TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + T_HABITS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "description TEXT, "
                + "category TEXT NOT NULL, "
                + "difficulty TEXT NOT NULL, "
                + "xp_reward INTEGER NOT NULL, "
                + "creation_date TEXT NOT NULL, "
                + "reminder_time TEXT, "
                + "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE " + T_HISTORY + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "habit_id INTEGER NOT NULL, "
                + "user_id INTEGER NOT NULL, "
                + "completion_date TEXT NOT NULL, "
                + "xp_earned INTEGER NOT NULL, "
                + "UNIQUE(habit_id, completion_date), "
                + "FOREIGN KEY(habit_id) REFERENCES habits(id) ON DELETE CASCADE, "
                + "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE " + T_ACHIEVEMENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "description TEXT NOT NULL, "
                + "requirement TEXT NOT NULL, "
                + "unlocked_status INTEGER NOT NULL DEFAULT 0, "
                + "unlock_date TEXT, "
                + "UNIQUE(user_id, name), "
                + "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + T_HABITS + " ADD COLUMN reminder_time TEXT");
        }
    }

    // ---------------------------------------------------------------- users

    public boolean usernameExists(String username) {
        return rowExists("SELECT 1 FROM " + T_USERS + " WHERE username = ? COLLATE NOCASE",
                new String[]{username.trim()});
    }

    public boolean emailExists(String email) {
        return rowExists("SELECT 1 FROM " + T_USERS + " WHERE email = ? COLLATE NOCASE",
                new String[]{email.trim().toLowerCase(Locale.ROOT)});
    }

    public long insertUser(String username, String email, String password) {
        ContentValues values = new ContentValues();
        values.put("username", username.trim());
        values.put("email", email.trim().toLowerCase(Locale.ROOT));
        values.put("password", hashPassword(password));
        values.put("xp", 0);
        values.put("level", 1);
        values.put("created_at", HabitStats.today());
        long id = getWritableDatabase().insert(T_USERS, null, values);
        if (id != -1) {
            seedAchievements((int) id);
        }
        return id;
    }

    public User login(String identifier, String password) {
        String value = identifier.trim();
        String sql = "SELECT * FROM " + T_USERS + " WHERE username = ? COLLATE NOCASE OR email = ? COLLATE NOCASE";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql,
                new String[]{value, value.toLowerCase(Locale.ROOT)})) {
            if (cursor.moveToFirst() && verifyPassword(password, cursor.getString(cursor.getColumnIndexOrThrow("password")))) {
                return readUser(cursor);
            }
        }
        return null;
    }

    public User getUser(int userId) {
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + T_USERS + " WHERE id = ?",
                new String[]{String.valueOf(userId)})) {
            if (cursor.moveToFirst()) {
                return readUser(cursor);
            }
        }
        return null;
    }

    public void updateUserProgress(int userId, int xp, int level) {
        ContentValues values = new ContentValues();
        values.put("xp", xp);
        values.put("level", level);
        getWritableDatabase().update(T_USERS, values, "id = ?", new String[]{String.valueOf(userId)});
    }

    private User readUser(Cursor cursor) {
        return new User(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("username")),
                cursor.getString(cursor.getColumnIndexOrThrow("email")),
                cursor.getInt(cursor.getColumnIndexOrThrow("xp")),
                cursor.getInt(cursor.getColumnIndexOrThrow("level")),
                cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
    }

    // --------------------------------------------------------------- habits

    public long insertHabit(Habit habit) {
        ContentValues values = new ContentValues();
        values.put("user_id", habit.getUserId());
        values.put("name", habit.getName().trim());
        values.put("description", habit.getDescription() == null ? "" : habit.getDescription().trim());
        values.put("category", habit.getCategory());
        values.put("difficulty", habit.getDifficulty());
        values.put("xp_reward", habit.getXpReward());
        values.put("creation_date", HabitStats.today());
        values.put("reminder_time", habit.getReminderTime());
        return getWritableDatabase().insert(T_HABITS, null, values);
    }

    public int updateHabit(Habit habit) {
        ContentValues values = new ContentValues();
        values.put("name", habit.getName().trim());
        values.put("description", habit.getDescription() == null ? "" : habit.getDescription().trim());
        values.put("category", habit.getCategory());
        values.put("difficulty", habit.getDifficulty());
        values.put("xp_reward", habit.getXpReward());
        values.put("reminder_time", habit.getReminderTime());
        return getWritableDatabase().update(T_HABITS, values, "id = ? AND user_id = ?",
                new String[]{String.valueOf(habit.getId()), String.valueOf(habit.getUserId())});
    }

    public void deleteHabit(int habitId) {
        getWritableDatabase().delete(T_HABITS, "id = ?", new String[]{String.valueOf(habitId)});
    }

    public Habit getHabit(int habitId) {
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + T_HABITS + " WHERE id = ?",
                new String[]{String.valueOf(habitId)})) {
            if (cursor.moveToFirst()) {
                Habit habit = readHabit(cursor);
                habit.setCompletedToday(isCompletedOn(habitId, HabitStats.today()));
                return habit;
            }
        }
        return null;
    }

    public List<Habit> getHabits(int userId) {
        List<Habit> habits = new ArrayList<>();
        Set<Integer> completedToday = new HashSet<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT habit_id FROM " + T_HISTORY + " WHERE user_id = ? AND completion_date = ?",
                new String[]{String.valueOf(userId), HabitStats.today()})) {
            while (cursor.moveToNext()) {
                completedToday.add(cursor.getInt(0));
            }
        }
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM " + T_HABITS + " WHERE user_id = ? ORDER BY id ASC",
                new String[]{String.valueOf(userId)})) {
            while (cursor.moveToNext()) {
                Habit habit = readHabit(cursor);
                habit.setCompletedToday(completedToday.contains(habit.getId()));
                habits.add(habit);
            }
        }
        return habits;
    }

    public int getHabitCount(int userId) {
        return intQuery("SELECT COUNT(*) FROM " + T_HABITS + " WHERE user_id = ?", userId);
    }

    private Habit readHabit(Cursor cursor) {
        Habit habit = new Habit();
        habit.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        habit.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        habit.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        habit.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        habit.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
        habit.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow("difficulty")));
        habit.setXpReward(cursor.getInt(cursor.getColumnIndexOrThrow("xp_reward")));
        habit.setCreationDate(cursor.getString(cursor.getColumnIndexOrThrow("creation_date")));
        habit.setReminderTime(cursor.getString(cursor.getColumnIndexOrThrow("reminder_time")));
        return habit;
    }

    // -------------------------------------------------------------- history

    public boolean markCompleted(int habitId, int userId, int xpEarned, String date) {
        ContentValues values = new ContentValues();
        values.put("habit_id", habitId);
        values.put("user_id", userId);
        values.put("completion_date", date);
        values.put("xp_earned", xpEarned);
        long id = getWritableDatabase().insertWithOnConflict(T_HISTORY, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        return id != -1;
    }

    public boolean isCompletedOn(int habitId, String date) {
        return rowExists("SELECT 1 FROM " + T_HISTORY + " WHERE habit_id = ? AND completion_date = ?",
                new String[]{String.valueOf(habitId), date});
    }

    public int getCompletionsOn(int userId, String date) {
        return intQuery("SELECT COUNT(*) FROM " + T_HISTORY + " WHERE user_id = ? AND completion_date = ?",
                userId, date);
    }

    public int getTotalCompletions(int userId) {
        return intQuery("SELECT COUNT(*) FROM " + T_HISTORY + " WHERE user_id = ?", userId);
    }

    public int getTotalXpEarned(int userId) {
        return intQuery("SELECT COALESCE(SUM(xp_earned), 0) FROM " + T_HISTORY + " WHERE user_id = ?", userId);
    }

    public List<String> getDistinctCompletionDates(int userId) {
        List<String> dates = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT DISTINCT completion_date FROM " + T_HISTORY + " WHERE user_id = ? ORDER BY completion_date DESC",
                new String[]{String.valueOf(userId)})) {
            while (cursor.moveToNext()) {
                dates.add(cursor.getString(0));
            }
        }
        return dates;
    }

    public List<String> getHabitCompletionDates(int habitId) {
        List<String> dates = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT completion_date FROM " + T_HISTORY + " WHERE habit_id = ? ORDER BY completion_date ASC",
                new String[]{String.valueOf(habitId)})) {
            while (cursor.moveToNext()) {
                dates.add(cursor.getString(0));
            }
        }
        return dates;
    }

    public List<Habit> getHabitsWithReminders(int userId) {
        List<Habit> habits = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM " + T_HABITS + " WHERE user_id = ? AND reminder_time IS NOT NULL",
                new String[]{String.valueOf(userId)})) {
            while (cursor.moveToNext()) {
                habits.add(readHabit(cursor));
            }
        }
        return habits;
    }

    public Map<String, Integer> getDailyCounts(int userId, String fromDate) {
        Map<String, Integer> counts = new HashMap<>();
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT completion_date, COUNT(*) FROM " + T_HISTORY
                        + " WHERE user_id = ? AND completion_date >= ? GROUP BY completion_date",
                new String[]{String.valueOf(userId), fromDate})) {
            while (cursor.moveToNext()) {
                counts.put(cursor.getString(0), cursor.getInt(1));
            }
        }
        return counts;
    }

    public String getTopHabitName(int userId) {
        String sql = "SELECT h.name, COUNT(*) AS total FROM " + T_HISTORY + " hh"
                + " INNER JOIN " + T_HABITS + " h ON h.id = hh.habit_id"
                + " WHERE hh.user_id = ? GROUP BY hh.habit_id ORDER BY total DESC, h.name ASC LIMIT 1";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(userId)})) {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        }
        return null;
    }

    // --------------------------------------------------------- achievements

    public void seedAchievements(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (String[] entry : ACHIEVEMENT_CATALOG) {
                ContentValues values = new ContentValues();
                values.put("user_id", userId);
                values.put("name", entry[0]);
                values.put("description", entry[1]);
                values.put("requirement", entry[2]);
                values.put("unlocked_status", 0);
                db.insertWithOnConflict(T_ACHIEVEMENTS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<Achievement> getAchievements(int userId) {
        List<Achievement> achievements = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM " + T_ACHIEVEMENTS + " WHERE user_id = ? ORDER BY id ASC",
                new String[]{String.valueOf(userId)})) {
            while (cursor.moveToNext()) {
                achievements.add(new Achievement(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("requirement")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("unlocked_status")) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow("unlock_date"))));
            }
        }
        return achievements;
    }

    public boolean unlockAchievement(int userId, String name) {
        ContentValues values = new ContentValues();
        values.put("unlocked_status", 1);
        values.put("unlock_date", HabitStats.today());
        int rows = getWritableDatabase().update(T_ACHIEVEMENTS, values,
                "user_id = ? AND name = ? AND unlocked_status = 0",
                new String[]{String.valueOf(userId), name});
        return rows > 0;
    }

    public int getUnlockedAchievements(int userId) {
        return intQuery("SELECT COUNT(*) FROM " + T_ACHIEVEMENTS + " WHERE user_id = ? AND unlocked_status = 1", userId);
    }

    public void resetProgress(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(T_HISTORY, "user_id = ?", new String[]{String.valueOf(userId)});
            db.delete(T_ACHIEVEMENTS, "user_id = ?", new String[]{String.valueOf(userId)});
            ContentValues values = new ContentValues();
            values.put("xp", 0);
            values.put("level", 1);
            db.update(T_USERS, values, "id = ?", new String[]{String.valueOf(userId)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        seedAchievements(userId);
    }

    // ---------------------------------------------------------------- utils

    private boolean rowExists(String sql, String[] args) {
        try (Cursor cursor = getReadableDatabase().rawQuery(sql, args)) {
            return cursor.moveToFirst();
        }
    }

    private int intQuery(String sql, Object... args) {
        String[] selectionArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            selectionArgs[i] = String.valueOf(args[i]);
        }
        try (Cursor cursor = getReadableDatabase().rawQuery(sql, selectionArgs)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    private static String hashPassword(String password) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS);
            return Base64.encodeToString(salt, Base64.NO_WRAP) + ":" + Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot hash password", e);
        }
    }

    private static boolean verifyPassword(String password, String stored) {
        try {
            String[] parts = stored.split(":");
            if (parts.length != 2) {
                return false;
            }
            byte[] salt = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] expected = Base64.decode(parts[1], Base64.NO_WRAP);
            byte[] actual = pbkdf2(password, salt, PBKDF2_ITERATIONS);
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, PBKDF2_KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }
}
