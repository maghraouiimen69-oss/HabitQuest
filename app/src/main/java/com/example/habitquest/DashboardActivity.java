package com.example.habitquest;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import java.time.LocalTime;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager session;
    private User user;

    private TextView avatar;
    private TextView greeting;
    private TextView levelChip;
    private TextView streakText;
    private TextView xpText;
    private TextView xpTotal;
    private TextView todayPercent;
    private TextView todayProgress;
    private TextView motivation;
    private ProgressBar xpBar;
    private ProgressBar todayBar;
    private LinearLayout habitsContainer;
    private LinearLayout habitsHeader;
    private View emptyState;
    private View jokeCard;
    private TextView jokeSetup;
    private TextView jokePunchline;
    private Button laughButton;
    private ImageButton jokeRefresh;
    private JokeApi.Joke joke;
    private boolean jokeLoading;
    private boolean punchlineShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        avatar = findViewById(R.id.avatar);
        greeting = findViewById(R.id.greeting);
        levelChip = findViewById(R.id.levelChip);
        streakText = findViewById(R.id.streakText);
        xpText = findViewById(R.id.xpText);
        xpTotal = findViewById(R.id.xpTotal);
        todayPercent = findViewById(R.id.todayPercent);
        todayProgress = findViewById(R.id.todayProgress);
        motivation = findViewById(R.id.motivation);
        xpBar = findViewById(R.id.xpBar);
        todayBar = findViewById(R.id.todayBar);
        habitsContainer = findViewById(R.id.habitsContainer);
        habitsHeader = findViewById(R.id.habitsHeader);
        emptyState = findViewById(R.id.emptyState);
        jokeCard = findViewById(R.id.jokeCard);
        jokeSetup = findViewById(R.id.jokeSetup);
        jokePunchline = findViewById(R.id.jokePunchline);
        laughButton = findViewById(R.id.laughButton);
        jokeRefresh = findViewById(R.id.jokeRefresh);

        findViewById(R.id.seeAll).setOnClickListener(v ->
                startActivity(new Intent(this, HabitsActivity.class)));
        findViewById(R.id.addHabitEmpty).setOnClickListener(v ->
                startActivity(new Intent(this, AddHabitActivity.class)));

        jokeCard.setOnClickListener(v -> onJokeAction());
        laughButton.setOnClickListener(v -> onJokeAction());
        jokeRefresh.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
            loadJoke();
        });

        JokeApi.Joke cachedJoke = JokeApi.cached(this);
        if (cachedJoke != null) {
            showJoke(cachedJoke);
        }
        if (cachedJoke == null || !cachedJoke.isToday()) {
            loadJoke();
        }

        BottomNavHelper.setup(this, R.id.nav_home);
    }

    private void onJokeAction() {
        if (jokeLoading) {
            return;
        }
        if (joke == null || punchlineShown) {
            loadJoke();
            return;
        }
        laughButton.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
        punchlineShown = true;
        jokePunchline.setText(joke.punchline);
        jokePunchline.setVisibility(View.VISIBLE);
        jokePunchline.setAlpha(0f);
        jokePunchline.setTranslationY(12f);
        jokePunchline.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(130)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .start();
        setLaughButton(R.string.new_joke, R.drawable.ic_refresh);
    }

    private void loadJoke() {
        if (jokeLoading) {
            return;
        }
        jokeLoading = true;
        jokeRefresh.animate().rotationBy(360f).setDuration(700).start();
        if (joke == null) {
            jokeSetup.setText(R.string.joke_loading);
            jokePunchline.setVisibility(View.GONE);
            laughButton.setVisibility(View.GONE);
        }
        JokeApi.random(this, new JokeApi.Callback() {
            @Override
            public void onSuccess(JokeApi.Joke newJoke) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                jokeLoading = false;
                showJoke(newJoke);
            }

            @Override
            public void onError() {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                jokeLoading = false;
                if (joke == null) {
                    jokeSetup.setText(R.string.joke_error);
                    laughButton.setVisibility(View.GONE);
                }
                Toast.makeText(DashboardActivity.this,
                        R.string.joke_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showJoke(JokeApi.Joke newJoke) {
        joke = newJoke;
        punchlineShown = false;
        jokeSetup.setText(joke.setup);
        jokePunchline.setVisibility(View.GONE);
        laughButton.setVisibility(View.VISIBLE);
        setLaughButton(R.string.laugh, R.drawable.ic_smile);
    }

    private void setLaughButton(int textRes, int iconRes) {
        laughButton.setText(textRes);
        Drawable icon = AppCompatResources.getDrawable(this, iconRes);
        if (icon != null) {
            icon = DrawableCompat.wrap(icon).mutate();
        }
        laughButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
        laughButton.setCompoundDrawableTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.white)));
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

        PaletteHelper.applyAvatar(this, avatar, user.getInitial(), session.getAvatarIndex(user.getId()));
        greeting.setText(getString(greetingRes(), user.getUsername()));
        levelChip.setText(getString(R.string.level_chip, user.getLevel(), user.getTitle()));

        int into = LevelSystem.xpIntoLevel(user.getXp());
        int need = LevelSystem.xpForNextLevel(user.getXp());
        xpBar.setMax(need);
        xpBar.setProgress(into);
        xpText.setText(getString(R.string.xp_progress, into, need));
        xpTotal.setText(getString(R.string.xp_total, user.getXp()));

        int streak = HabitStats.currentStreak(db.getDistinctCompletionDates(user.getId()));
        if (streak == 0) {
            streakText.setText(R.string.streak_none);
        } else {
            streakText.setText(streak == 1
                    ? getString(R.string.streak_days, streak)
                    : getString(R.string.streak_days_plural, streak));
        }

        List<Habit> habits = db.getHabits(user.getId());
        int done = 0;
        for (Habit habit : habits) {
            if (habit.isCompletedToday()) {
                done++;
            }
        }
        int percent = HabitStats.percent(done, habits.size());
        todayBar.setProgress(percent);
        todayPercent.setText(percent + "%");
        todayProgress.setText(getString(R.string.today_progress, done, habits.size()));
        motivation.setText(ProgressManager.motivationFor(done, habits.size()));

        habitsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Habit habit : habits) {
            View row = inflater.inflate(R.layout.item_habit_preview, habitsContainer, false);
            bindPreview(row, habit);
            habitsContainer.addView(row);
        }

        boolean hasHabits = !habits.isEmpty();
        habitsHeader.setVisibility(hasHabits ? View.VISIBLE : View.GONE);
        emptyState.setVisibility(hasHabits ? View.GONE : View.VISIBLE);
    }

    private int greetingRes() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) {
            return R.string.greeting_morning;
        }
        if (hour < 18) {
            return R.string.greeting_afternoon;
        }
        return R.string.greeting_evening;
    }

    private void bindPreview(View row, Habit habit) {
        ImageView icon = row.findViewById(R.id.previewIcon);
        TextView name = row.findViewById(R.id.previewName);
        TextView xp = row.findViewById(R.id.previewXp);
        ImageButton check = row.findViewById(R.id.previewCheck);

        int color = ContextCompat.getColor(this, habit.getCategoryColorRes());
        icon.setImageResource(habit.getCategoryIconRes());
        icon.setImageTintList(ColorStateList.valueOf(color));
        icon.setBackgroundTintList(ColorStateList.valueOf(withAlpha(color, 0x30)));
        name.setText(habit.getName());
        xp.setText(getString(R.string.habit_xp_reward, habit.getXpReward()));
        xp.setTextColor(color);
        xp.setBackgroundTintList(ColorStateList.valueOf(withAlpha(color, 0x26)));
        check.setImageResource(habit.isCompletedToday() ? R.drawable.ic_check_circle : R.drawable.ic_circle);
        check.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this,
                habit.isCompletedToday() ? R.color.success : R.color.text_muted)));

        check.setOnClickListener(v -> completeHabit(habit));
        row.setOnClickListener(v -> startActivity(new Intent(this, HabitsActivity.class)));
    }

    private void completeHabit(Habit habit) {
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
            showLevelUpDialog(reward.level);
        }
        refresh();
    }

    private void showLevelUpDialog(int level) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.level_up_title)
                .setMessage(getString(R.string.level_up_body, level, LevelSystem.titleForLevel(level)))
                .setPositiveButton(R.string.continue_label, null)
                .show();
    }

    private int withAlpha(int color, int alpha) {
        return android.graphics.Color.argb(alpha,
                android.graphics.Color.red(color),
                android.graphics.Color.green(color),
                android.graphics.Color.blue(color));
    }
}
