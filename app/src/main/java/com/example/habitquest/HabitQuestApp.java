package com.example.habitquest;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public class HabitQuestApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(ThemeManager.isDark(this)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
