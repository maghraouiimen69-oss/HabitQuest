package com.example.habitquest;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public final class BottomNavHelper {

    private BottomNavHelper() {
    }

    public static void setup(Activity activity, int currentItemId) {
        BottomNavigationView nav = activity.findViewById(R.id.bottomNav);
        nav.setSelectedItemId(currentItemId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == currentItemId) {
                return true;
            }
            Class<?> target;
            if (id == R.id.nav_habits) {
                target = HabitsActivity.class;
            } else if (id == R.id.nav_achievements) {
                target = AchievementsActivity.class;
            } else if (id == R.id.nav_stats) {
                target = StatisticsActivity.class;
            } else if (id == R.id.nav_profile) {
                target = ProfileActivity.class;
            } else {
                target = DashboardActivity.class;
            }
            activity.startActivity(new Intent(activity, target)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            activity.overridePendingTransition(0, 0);
            activity.finish();
            return true;
        });
    }
}
