package com.example.habitquest;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeManager {

    private static final String PREFS = "habitquest_settings";
    private static final String KEY_DARK = "dark_mode";

    private ThemeManager() {
    }

    public static boolean isDark(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_DARK, true);
    }

    public static void setDark(Context context, boolean dark) {
        context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DARK, dark)
                .apply();
    }
}
