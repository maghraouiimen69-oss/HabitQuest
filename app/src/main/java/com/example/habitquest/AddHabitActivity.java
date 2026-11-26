package com.example.habitquest;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

public class AddHabitActivity extends AppCompatActivity {

    public static final String EXTRA_HABIT_ID = "extra_habit_id";

    private DatabaseHelper db;
    private SessionManager session;

    private TextInputLayout tilName;
    private TextInputLayout tilDescription;
    private ChipGroup categoryGroup;
    private ChipGroup difficultyGroup;
    private TextView rewardText;
    private View btnDelete;

    private int habitId = -1;

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
        btnDelete = findViewById(R.id.btnDelete);

        habitId = getIntent().getIntExtra(EXTRA_HABIT_ID, -1);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveHabit());
        btnDelete.setOnClickListener(v -> confirmDelete());

        difficultyGroup.setOnCheckedStateChangeListener((group, checkedIds) -> updateReward());

        if (habitId > 0) {
            loadHabit();
        } else {
            categoryGroup.check(R.id.chipHealth);
            difficultyGroup.check(R.id.chipEasy);
        }
        updateReward();
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

        if (habitId > 0) {
            db.updateHabit(habit);
        } else {
            long newId = db.insertHabit(habit);
            if (newId == -1) {
                Toast.makeText(this, R.string.err_fill_all, Toast.LENGTH_SHORT).show();
                return;
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
                    db.deleteHabit(habitId);
                    Toast.makeText(this, R.string.habit_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }
}
