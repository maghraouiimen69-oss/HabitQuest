package com.example.habitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class HabitDetailActivity extends AppCompatActivity {

    public static final String EXTRA_HABIT_ID = "extra_habit_id";

    private DatabaseHelper db;
    private SessionManager session;

    private int habitId;
    private Habit habit;

    private TextView habitName;
    private TextView habitMeta;
    private TextView habitXp;
    private TextView habitDescription;
    private TextView statStreak;
    private TextView statBest;
    private TextView statTotal;
    private TextView monthCount;
    private TextView reminderValue;
    private HabitCalendarView calendar;
    private TextView btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_detail);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        habitId = getIntent().getIntExtra(EXTRA_HABIT_ID, -1);

        habitName = findViewById(R.id.habitName);
        habitMeta = findViewById(R.id.habitMeta);
        habitXp = findViewById(R.id.habitXp);
        habitDescription = findViewById(R.id.habitDescription);
        statStreak = findViewById(R.id.statStreak);
        statBest = findViewById(R.id.statBest);
        statTotal = findViewById(R.id.statTotal);
        monthCount = findViewById(R.id.monthCount);
        reminderValue = findViewById(R.id.reminderValue);
        calendar = findViewById(R.id.calendar);
        btnDone = findViewById(R.id.btnDone);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnEdit).setOnClickListener(v -> openEditor());
        btnDone.setOnClickListener(v -> completeToday());
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        habit = db.getHabit(habitId);
        if (habit == null) {
            finish();
            return;
        }

        habitName.setText(habit.getName());
        habitMeta.setText(getString(R.string.habit_meta, habit.getCategory(), habit.getDifficulty()));
        habitXp.setText(getString(R.string.habit_xp_reward, habit.getXpReward()));

        String description = habit.getDescription();
        if (description == null || description.trim().isEmpty()) {
            habitDescription.setVisibility(View.GONE);
        } else {
            habitDescription.setText(description);
            habitDescription.setVisibility(View.VISIBLE);
        }

        List<String> dates = db.getHabitCompletionDates(habitId);
        calendar.setDates(dates);
        statStreak.setText(getString(R.string.days_value, HabitStats.currentStreak(dates)));
        statBest.setText(getString(R.string.days_value, HabitStats.longestStreak(dates)));
        statTotal.setText(String.valueOf(dates.size()));
        monthCount.setText(getString(R.string.habit_detail_month, calendar.countLast30Days()));
        reminderValue.setText(habit.hasReminder()
                ? getString(R.string.reminder_daily, habit.getReminderTime())
                : getString(R.string.reminder_none));

        btnDone.setEnabled(!habit.isCompletedToday());
        btnDone.setAlpha(habit.isCompletedToday() ? 0.55f : 1f);
        btnDone.setText(habit.isCompletedToday()
                ? R.string.habit_detail_done_today
                : R.string.habit_detail_mark_today);
    }

    private void openEditor() {
        startActivity(new Intent(this, AddHabitActivity.class)
                .putExtra(AddHabitActivity.EXTRA_HABIT_ID, habitId));
    }

    private void completeToday() {
        User user = db.getUser(session.getUserId());
        if (user == null || habit == null) {
            return;
        }
        ProgressManager.Reward reward = ProgressManager.completeHabit(db, user, habit);
        if (!reward.earned) {
            Toast.makeText(this, R.string.already_done, Toast.LENGTH_SHORT).show();
            load();
            return;
        }
        Toast.makeText(this, getString(R.string.xp_gained, reward.xp), Toast.LENGTH_SHORT).show();
        for (String achievement : reward.unlocked) {
            Toast.makeText(this, getString(R.string.achievement_unlocked, achievement), Toast.LENGTH_LONG).show();
        }
        if (reward.leveledUp) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.level_up_title)
                    .setMessage(getString(R.string.level_up_body, reward.level, LevelSystem.titleForLevel(reward.level)))
                    .setPositiveButton(R.string.continue_label, null)
                    .show();
        }
        load();
    }
}
