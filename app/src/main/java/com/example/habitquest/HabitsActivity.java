package com.example.habitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HabitsActivity extends AppCompatActivity implements HabitAdapter.Listener {

    private DatabaseHelper db;
    private SessionManager session;
    private User user;
    private HabitAdapter adapter;
    private RecyclerView habitsList;
    private View emptyState;
    private TextView habitsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        habitsList = findViewById(R.id.habitsList);
        emptyState = findViewById(R.id.emptyState);
        habitsCount = findViewById(R.id.habitsCount);

        adapter = new HabitAdapter(this);
        habitsList.setLayoutManager(new LinearLayoutManager(this));
        habitsList.setAdapter(adapter);

        findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(this, AddHabitActivity.class)));
        findViewById(R.id.addHabitEmpty).setOnClickListener(v ->
                startActivity(new Intent(this, AddHabitActivity.class)));

        BottomNavHelper.setup(this, R.id.nav_habits);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        user = db.getUser(session.getUserId());
        if (user == null) {
            session.logout();
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }
        List<Habit> habits = db.getHabits(user.getId());
        adapter.submit(habits);
        habitsCount.setText(getString(R.string.habits_active, habits.size()));
        boolean empty = habits.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        habitsList.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onHabitClick(Habit habit) {
        startActivity(new Intent(this, AddHabitActivity.class)
                .putExtra(AddHabitActivity.EXTRA_HABIT_ID, habit.getId()));
    }

    @Override
    public void onCompleteClick(Habit habit) {
        ProgressManager.Reward reward = ProgressManager.completeHabit(db, user, habit);
        if (!reward.earned) {
            Toast.makeText(this, R.string.already_done, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, getString(R.string.xp_gained, reward.xp), Toast.LENGTH_SHORT).show();
        for (String achievement : reward.unlocked) {
            Toast.makeText(this, getString(R.string.achievement_unlocked, achievement), Toast.LENGTH_LONG).show();
        }
        if (reward.leveledUp) {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.level_up_title)
                    .setMessage(getString(R.string.level_up_body, reward.level, LevelSystem.titleForLevel(reward.level)))
                    .setPositiveButton(R.string.continue_label, null)
                    .show();
        }
        refresh();
    }
}
