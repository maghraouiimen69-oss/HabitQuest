package com.example.habitquest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager session;
    private User user;
    private TextView avatar;
    private TextView profileName;
    private TextView memberSince;
    private TextView levelChip;
    private TextView statAchievements;
    private TextView statLevel;
    private TextView statXp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        avatar = findViewById(R.id.avatar);
        profileName = findViewById(R.id.profileName);
        memberSince = findViewById(R.id.memberSince);
        levelChip = findViewById(R.id.levelChip);
        statAchievements = findViewById(R.id.statAchievements);
        statLevel = findViewById(R.id.statLevel);
        statXp = findViewById(R.id.statXp);

        avatar.setOnClickListener(v -> {
            session.cycleAvatar(user.getId(), PaletteHelper.count());
            refresh();
        });
        findViewById(R.id.rowAvatar).setOnClickListener(v -> {
            session.cycleAvatar(user.getId(), PaletteHelper.count());
            refresh();
        });
        findViewById(R.id.rowReset).setOnClickListener(v -> confirmReset());
        findViewById(R.id.rowLogout).setOnClickListener(v -> confirmLogout());

        MaterialSwitch themeSwitch = findViewById(R.id.switchTheme);
        themeSwitch.setChecked(!ThemeManager.isDark(this));
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeManager.setDark(this, !isChecked);
            AppCompatDelegate.setDefaultNightMode(isChecked
                    ? AppCompatDelegate.MODE_NIGHT_NO
                    : AppCompatDelegate.MODE_NIGHT_YES);
        });

        BottomNavHelper.setup(this, R.id.nav_profile);
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
        profileName.setText(user.getUsername());
        memberSince.setText(getString(R.string.member_since, HabitStats.displayDate(user.getCreatedAt())));
        levelChip.setText(getString(R.string.level_chip, user.getLevel(), user.getTitle()));

        statAchievements.setText(db.getUnlockedAchievements(user.getId()) + "/" + db.getAchievements(user.getId()).size());
        statLevel.setText(String.valueOf(user.getLevel()));
        statXp.setText(String.valueOf(user.getXp()));
    }

    private void confirmReset() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.reset_progress_title)
                .setMessage(R.string.reset_progress_body)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.reset_progress, (dialog, which) -> {
                    db.resetProgress(user.getId());
                    Toast.makeText(this, R.string.progress_reset, Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .show();
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_body)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    session.logout();
                    startActivity(new Intent(this, LoginActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                })
                .show();
    }
}
