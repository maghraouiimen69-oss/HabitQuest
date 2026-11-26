package com.example.habitquest;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
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
    private View swipeHint;
    private TextView habitsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        habitsList = findViewById(R.id.habitsList);
        emptyState = findViewById(R.id.emptyState);
        swipeHint = findViewById(R.id.swipeHint);
        habitsCount = findViewById(R.id.habitsCount);

        adapter = new HabitAdapter(this);
        habitsList.setLayoutManager(new LinearLayoutManager(this));
        habitsList.setAdapter(adapter);
        attachSwipeToComplete();

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
        swipeHint.setVisibility(empty ? View.GONE : View.VISIBLE);
        habitsList.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void attachSwipeToComplete() {
        final float density = getResources().getDisplayMetrics().density;
        final float radius = 20 * density;
        final Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
        background.setColor(ContextCompat.getColor(this, R.color.accent_mint));
        final RectF rect = new RectF();
        final Drawable check = ContextCompat.getDrawable(this, R.drawable.ic_check);
        if (check != null) {
            check.setTint(ContextCompat.getColor(this, R.color.white));
        }

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Habit habit = adapter.getHabit(viewHolder.getBindingAdapterPosition());
                if (habit != null) {
                    onCompleteClick(habit);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                View item = viewHolder.itemView;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX > 0 && check != null) {
                    float inset = 2 * density;
                    rect.set(item.getLeft() + inset, item.getTop() + inset,
                            item.getLeft() + dX - inset, item.getBottom() - inset);
                    c.drawRoundRect(rect, radius, radius, background);

                    int iconSize = Math.round(24 * density);
                    int centerX = Math.round(item.getLeft() + Math.min(dX / 2f, 48 * density));
                    int centerY = (item.getTop() + item.getBottom()) / 2;
                    check.setAlpha((int) Math.min(255, dX / (48 * density) * 255));
                    check.setBounds(centerX - iconSize / 2, centerY - iconSize / 2,
                            centerX + iconSize / 2, centerY + iconSize / 2);
                    check.draw(c);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(habitsList);
    }

    @Override
    public void onHabitClick(Habit habit) {
        startActivity(new Intent(this, HabitDetailActivity.class)
                .putExtra(HabitDetailActivity.EXTRA_HABIT_ID, habit.getId()));
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
