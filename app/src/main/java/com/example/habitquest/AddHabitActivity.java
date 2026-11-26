package com.example.habitquest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Locale;

public class AddHabitActivity extends AppCompatActivity {

    public static final String EXTRA_HABIT_ID = "extra_habit_id";

    private DatabaseHelper db;
    private SessionManager session;

    private TextInputLayout tilName;
    private TextInputLayout tilDescription;
    private ChipGroup categoryGroup;
    private ChipGroup difficultyGroup;
    private TextView rewardText;
    private TextView reminderText;
    private TextView btnReminderPick;
    private ImageButton btnReminderClear;
    private View btnDelete;

    private int habitId = -1;
    private String reminderTime;

    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) {
                    Toast.makeText(this, R.string.reminder_permission_denied, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        tilName = findViewById(R.id.tilHabitName);
        tilDescription = findViewById(R.id.tilHabitDescription);
        categoryGroup = findViewById(R.id.categoryGroup);
        difficultyGroup = findViewById(R.id.difficultyGroup);
        rewardText = findViewById(R.id.rewardText);
        reminderText = findViewById(R.id.reminderText);
        btnReminderPick = findViewById(R.id.btnReminderPick);
        btnReminderClear = findViewById(R.id.btnReminderClear);
        btnDelete = findViewById(R.id.btnDelete);

        habitId = getIntent().getIntExtra(EXTRA_HABIT_ID, -1);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveHabit());
        btnDelete.setOnClickListener(v -> confirmDelete());
        findViewById(R.id.reminderCard).setOnClickListener(v -> pickReminderTime());
        btnReminderPick.setOnClickListener(v -> pickReminderTime());
        btnReminderClear.setOnClickListener(v -> {
            reminderTime = null;
            updateReminderUi();
        });

        difficultyGroup.setOnCheckedStateChangeListener((group, checkedIds) -> updateReward());

        if (habitId > 0) {
            loadHabit();
        } else {
            categoryGroup.check(R.id.chipHealth);
            difficultyGroup.check(R.id.chipEasy);
        }
        updateReward();
        updateReminderUi();
    }

    private void pickReminderTime() {
        int hour = 8;
        int minute = 0;
        if (reminderTime != null && reminderTime.length() == 5) {
            hour = Integer.parseInt(reminderTime.substring(0, 2));
            minute = Integer.parseInt(reminderTime.substring(3, 5));
        }
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(DateFormat.is24HourFormat(this) ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(R.string.time_picker_title)
                .build();
        picker.addOnPositiveButtonClickListener(v -> {
            reminderTime = String.format(Locale.US, "%02d:%02d", picker.getHour(), picker.getMinute());
            updateReminderUi();
            ensureNotificationPermission();
        });
        picker.show(getSupportFragmentManager(), "reminder_time_picker");
    }

    private void ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void updateReminderUi() {
        if (reminderTime == null || reminderTime.isEmpty()) {
            reminderText.setText(R.string.reminder_none);
            btnReminderPick.setText(R.string.reminder_add);
            btnReminderClear.setVisibility(View.GONE);
        } else {
            reminderText.setText(getString(R.string.reminder_daily, reminderTime));
            btnReminderPick.setText(R.string.reminder_change);
            btnReminderClear.setVisibility(View.VISIBLE);
        }
    }

    private void loadHabit() {
        Habit habit = db.getHabit(habitId);
        if (habit == null) {
            finish();
            return;
        }
        ((TextView) findViewById(R.id.screenTitle)).setText(R.string.edit_habit);
        ((TextView) findViewById(R.id.btnSave)).setText(R.string.update_habit);
        btnDelete.setVisibility(View.VISIBLE);

        tilName.getEditText().setText(habit.getName());
        tilDescription.getEditText().setText(habit.getDescription());
        categoryGroup.check(chipForCategory(habit.getCategory()));
        difficultyGroup.check(chipForDifficulty(habit.getDifficulty()));
        reminderTime = habit.getReminderTime();
        updateReminderUi();
    }

    private int chipForCategory(String category) {
        if (Habit.CAT_FITNESS.equals(category)) {
            return R.id.chipFitness;
        }
        if (Habit.CAT_EDUCATION.equals(category)) {
            return R.id.chipEducation;
        }
        if (Habit.CAT_PRODUCTIVITY.equals(category)) {
            return R.id.chipProductivity;
        }
        if (Habit.CAT_PERSONAL.equals(category)) {
            return R.id.chipPersonal;
        }
        return R.id.chipHealth;
    }

    private int chipForDifficulty(String difficulty) {
        if (Habit.DIFF_HARD.equals(difficulty)) {
            return R.id.chipHard;
        }
        if (Habit.DIFF_MEDIUM.equals(difficulty)) {
            return R.id.chipMedium;
        }
        return R.id.chipEasy;
    }

    private String selectedCategory() {
        int id = categoryGroup.getCheckedChipId();
        if (id == R.id.chipFitness) {
            return Habit.CAT_FITNESS;
        }
        if (id == R.id.chipEducation) {
            return Habit.CAT_EDUCATION;
        }
        if (id == R.id.chipProductivity) {
            return Habit.CAT_PRODUCTIVITY;
        }
        if (id == R.id.chipPersonal) {
            return Habit.CAT_PERSONAL;
        }
        return Habit.CAT_HEALTH;
    }

    private String selectedDifficulty() {
        int id = difficultyGroup.getCheckedChipId();
        if (id == R.id.chipHard) {
            return Habit.DIFF_HARD;
        }
        if (id == R.id.chipMedium) {
            return Habit.DIFF_MEDIUM;
        }
        return Habit.DIFF_EASY;
    }

    private void updateReward() {
        rewardText.setText(getString(R.string.reward_value, Habit.xpForDifficulty(selectedDifficulty())));
    }

    private void saveHabit() {
        tilName.setError(null);
        String name = tilName.getEditText().getText().toString().trim();
        if (name.isEmpty()) {
            tilName.setError(getString(R.string.err_habit_name));
            return;
        }

        String difficulty = selectedDifficulty();
        Habit habit = habitId > 0 ? db.getHabit(habitId) : new Habit();
        if (habit == null) {
            finish();
            return;
        }
        habit.setUserId(session.getUserId());
        habit.setName(name);
        habit.setDescription(tilDescription.getEditText().getText().toString().trim());
        habit.setCategory(selectedCategory());
        habit.setDifficulty(difficulty);
        habit.setXpReward(Habit.xpForDifficulty(difficulty));
        habit.setReminderTime(reminderTime);

        if (habitId > 0) {
            db.updateHabit(habit);
            if (habit.hasReminder()) {
                ReminderScheduler.schedule(this, habit);
            } else {
                ReminderScheduler.cancel(this, habit.getId());
            }
        } else {
            long newId = db.insertHabit(habit);
            if (newId == -1) {
                Toast.makeText(this, R.string.err_fill_all, Toast.LENGTH_SHORT).show();
                return;
            }
            habit.setId((int) newId);
            if (habit.hasReminder()) {
                ReminderScheduler.schedule(this, habit);
            }
        }
        Toast.makeText(this, R.string.success_habit_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_habit_title)
                .setMessage(R.string.delete_habit_body)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    ReminderScheduler.cancel(this, habitId);
                    db.deleteHabit(habitId);
                    Toast.makeText(this, R.string.habit_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }
}
