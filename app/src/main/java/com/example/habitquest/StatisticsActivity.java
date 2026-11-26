package com.example.habitquest;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        BottomNavHelper.setup(this, R.id.nav_stats);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int userId = session.getUserId();

        List<String> dates = db.getDistinctCompletionDates(userId);
        int currentStreak = HabitStats.currentStreak(dates);
        int longestStreak = HabitStats.longestStreak(dates);

        ((TextView) findViewById(R.id.statCompleted)).setText(String.valueOf(db.getTotalCompletions(userId)));
        ((TextView) findViewById(R.id.statXp)).setText(String.valueOf(db.getTotalXpEarned(userId)));
        ((TextView) findViewById(R.id.statCurrentStreak)).setText(String.valueOf(currentStreak));
        ((TextView) findViewById(R.id.statLongestStreak)).setText(String.valueOf(longestStreak));

        Map<String, Integer> daily = db.getDailyCounts(userId,
                LocalDate.now().minusDays(6).toString());
        int[] counts = HabitStats.weeklyCounts(daily);
        WeeklyChartView chart = findViewById(R.id.weeklyChart);
        chart.setData(counts, HabitStats.weeklyLabels());

        int habitCount = db.getHabitCount(userId);
        int doneThisWeek = HabitStats.sum(counts);
        int possible = habitCount * 7;
        int weeklyPercent = HabitStats.percent(doneThisWeek, possible);
        ProgressBar weeklyBar = findViewById(R.id.weeklyBar);
        weeklyBar.setProgress(weeklyPercent);
        ((TextView) findViewById(R.id.weeklyPercent)).setText(weeklyPercent + "%");
        ((TextView) findViewById(R.id.weeklyText)).setText(getString(R.string.today_progress, doneThisWeek, possible));

        String topHabit = db.getTopHabitName(userId);
        ((TextView) findViewById(R.id.topHabit)).setText(topHabit == null ? getString(R.string.no_data_yet) : topHabit);
    }
}
