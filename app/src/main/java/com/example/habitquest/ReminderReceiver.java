package com.example.habitquest;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String ACTION_REMIND = "com.example.habitquest.action.REMIND";
    public static final String EXTRA_HABIT_ID = "extra_habit_id";
    public static final String EXTRA_HABIT_NAME = "extra_habit_name";
    public static final String EXTRA_REMINDER_TIME = "extra_reminder_time";

    @Override
    public void onReceive(Context context, Intent intent) {
        int habitId = intent.getIntExtra(EXTRA_HABIT_ID, -1);
        String name = intent.getStringExtra(EXTRA_HABIT_NAME);
        String time = intent.getStringExtra(EXTRA_REMINDER_TIME);
        if (habitId <= 0 || name == null) {
            return;
        }

        NotificationManager manager = ContextCompat.getSystemService(context, NotificationManager.class);
        if (manager != null) {
            PendingIntent open = PendingIntent.getActivity(context, habitId,
                    new Intent(context, DashboardActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, HabitQuestApp.CHANNEL_REMINDERS)
                    .setSmallIcon(R.drawable.ic_alarm)
                    .setContentTitle(context.getString(R.string.reminder_notification_title))
                    .setContentText(context.getString(R.string.reminder_body, name))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(open);
            try {
                manager.notify(habitId, builder.build());
            } catch (SecurityException ignored) {
                // POST_NOTIFICATIONS not granted: the habit is still saved, only the nudge is skipped.
            }
        }

        Habit habit = new Habit();
        habit.setId(habitId);
        habit.setName(name);
        habit.setReminderTime(time);
        ReminderScheduler.schedule(context, habit);
    }
}
