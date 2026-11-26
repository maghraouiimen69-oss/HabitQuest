package com.example.habitquest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class HabitStats {

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);

    private HabitStats() {
    }

    public static String today() {
        return LocalDate.now().toString();
    }

    public static String displayDate(String isoDate) {
        try {
            return LocalDate.parse(isoDate).format(DISPLAY);
        } catch (Exception e) {
            return isoDate;
        }
    }

    public static int currentStreak(List<String> dates) {
        Set<LocalDate> days = new HashSet<>();
        for (String date : dates) {
            try {
                days.add(LocalDate.parse(date));
            } catch (Exception ignored) {
            }
        }
        LocalDate cursor = LocalDate.now();
        if (!days.contains(cursor)) {
            cursor = cursor.minusDays(1);
            if (!days.contains(cursor)) {
                return 0;
            }
        }
        int streak = 0;
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    public static int longestStreak(List<String> dates) {
        List<LocalDate> days = new ArrayList<>();
        for (String date : dates) {
            try {
                days.add(LocalDate.parse(date));
            } catch (Exception ignored) {
            }
        }
        if (days.isEmpty()) {
            return 0;
        }
        days.sort(null);
        int best = 1;
        int run = 1;
        for (int i = 1; i < days.size(); i++) {
            if (days.get(i - 1).plusDays(1).equals(days.get(i))) {
                run++;
            } else if (!days.get(i - 1).equals(days.get(i))) {
                run = 1;
            }
            best = Math.max(best, run);
        }
        return best;
    }

    public static int[] weeklyCounts(Map<String, Integer> countsByDate) {
        int[] counts = new int[7];
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            String key = today.minusDays(6L - i).toString();
            Integer value = countsByDate.get(key);
            counts[i] = value == null ? 0 : value;
        }
        return counts;
    }

    public static String[] weeklyLabels() {
        String[] labels = new String[7];
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            labels[i] = today.minusDays(6L - i).format(DAY_LABEL);
        }
        return labels;
    }

    public static int sum(int[] values) {
        int total = 0;
        for (int value : values) {
            total += value;
        }
        return total;
    }

    public static int percent(int done, int possible) {
        if (possible <= 0) {
            return 0;
        }
        return Math.min(100, Math.round(done * 100f / possible));
    }
}
