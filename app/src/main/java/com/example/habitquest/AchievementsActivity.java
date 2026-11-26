package com.example.habitquest;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager session;
    private AchievementAdapter adapter;
    private TextView achievementsProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        achievementsProgress = findViewById(R.id.achievementsProgress);
        RecyclerView list = findViewById(R.id.achievementsList);
        adapter = new AchievementAdapter();
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        BottomNavHelper.setup(this, R.id.nav_achievements);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int userId = session.getUserId();
        List<Achievement> achievements = db.getAchievements(userId);
        adapter.submit(achievements);
        achievementsProgress.setText(getString(R.string.achievements_progress,
                db.getUnlockedAchievements(userId), achievements.size()));
    }
}
