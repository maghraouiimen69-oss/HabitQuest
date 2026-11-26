package com.example.habitquest;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFS = "habitquest_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_AVATAR_PREFIX = "avatar_";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveSession(int userId) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public boolean isLoggedIn() {
        return getUserId() != -1;
    }

    public void logout() {
        prefs.edit().remove(KEY_USER_ID).apply();
    }

    public int getAvatarIndex(int userId) {
        return prefs.getInt(KEY_AVATAR_PREFIX + userId, 0);
    }

    public int cycleAvatar(int userId, int count) {
        int next = (getAvatarIndex(userId) + 1) % count;
        prefs.edit().putInt(KEY_AVATAR_PREFIX + userId, next).apply();
        return next;
    }
}
