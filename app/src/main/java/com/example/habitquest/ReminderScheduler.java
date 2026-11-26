package com.example.habitquest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public final class ReminderScheduler {

    private static final long WINDOW_MS = 5 * 60 * 1000L;

    private ReminderScheduler() {
    }

    public static void schedule(Context context, Habit habit) {
        if (habit == null || !habit.hasReminder() || habit.getId() <= 0) {
            return;
        }
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) {
            return;
        }
        long triggerAt = nextTriggerMillis(habit.getReminderTime());
        if (triggerAt <= 0) {
            return;
        }
        PendingIntent pending = pendingIntent(context, habit);
        manager.cancel(pending);
        if (manager.canScheduleExactAlarms()) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending);
        } else {
            // Without the exact-alarm grant the system fires inside this window instead.
            manager.setWindow(AlarmManager.RTC_WAKEUP, triggerAt, WINDOW_MS, pending);
        }
    }

    public static void cancel(Context context, int habitId) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null || habitId <= 0) {
            return;
        }
        PendingIntent pending = PendingIntent.getBroadcast(context, habitId,
                intentFor(context, habitId, null, null),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        manager.cancel(pending);
        pending.cancel();
    }

    public static void restore(Context context) {
        SessionManager session = new SessionManager(context);
        if (!session.isLoggedIn()) {
            return;
        }
        DatabaseHelper db = new DatabaseHelper(context);
        for (Habit habit : db.getHabitsWithReminders(session.getUserId())) {
            schedule(context, habit);
        }
        db.close();
    }

    static long nextTriggerMillis(String time) {
        try {
            LocalTime reminder = LocalTime.parse(time);
            LocalDateTime next = LocalDate.now().atTime(reminder);
            if (!next.isAfter(LocalDateTime.now())) {
                next = next.plusDays(1);
            }
            return next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            return -1;
        }
    }

    private static PendingIntent pendingIntent(Context context, Habit habit) {
        return PendingIntent.getBroadcast(context, habit.getId(),
                intentFor(context, habit.getId(), habit.getName(), habit.getReminderTime()),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static Intent intentFor(Context context, int habitId, String name, String reminderTime) {
        Intent intent = new Intent(context, ReminderReceiver.class)
                .setAction(ReminderReceiver.ACTION_REMIND)
                .putExtra(ReminderReceiver.EXTRA_HABIT_ID, habitId);
        if (name != null) {
            intent.putExtra(ReminderReceiver.EXTRA_HABIT_NAME, name);
        }
        if (reminderTime != null) {
            intent.putExtra(ReminderReceiver.EXTRA_REMINDER_TIME, reminderTime);
        }
        return intent;
    }
}
