package com.example.habitquest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HabitCalendarView extends View {

    private static final int DAYS = 30;
    private static final int COLUMNS = 6;

    private final Set<LocalDate> completed = new HashSet<>();

    private final Paint cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private final int doneColor;
    private final int idleColor;
    private final int todayColor;
    private final int doneText;
    private final int idleText;

    public HabitCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        doneColor = ContextCompat.getColor(context, R.color.accent_mint);
        idleColor = ContextCompat.getColor(context, R.color.surface_alt);
        todayColor = ContextCompat.getColor(context, R.color.accent_primary);
        doneText = ContextCompat.getColor(context, R.color.white);
        idleText = ContextCompat.getColor(context, R.color.text_muted);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2 * getResources().getDisplayMetrics().density);
        strokePaint.setColor(todayColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setDates(List<String> dates) {
        completed.clear();
        for (String date : dates) {
            try {
                completed.add(LocalDate.parse(date));
            } catch (DateTimeParseException ignored) {
            }
        }
        invalidate();
    }

    public int countLast30Days() {
        int total = 0;
        LocalDate today = LocalDate.now();
        for (int i = 0; i < DAYS; i++) {
            if (completed.contains(today.minusDays(i))) {
                total++;
            }
        }
        return total;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int cell = Math.max(1, width / COLUMNS);
        int rows = DAYS / COLUMNS;
        int height = rows * cell;
        setMeasuredDimension(width, resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        textPaint.setTextSize(12 * density);
        textPaint.setFakeBoldText(true);

        float cell = (float) getWidth() / COLUMNS;
        float gap = 3 * density;
        float radius = 9 * density;
        LocalDate today = LocalDate.now();

        for (int index = 0; index < DAYS; index++) {
            LocalDate day = today.minusDays(DAYS - 1L - index);
            int column = index % COLUMNS;
            int row = index / COLUMNS;

            float left = column * cell + gap;
            float top = row * cell + gap;
            float right = (column + 1) * cell - gap;
            float bottom = (row + 1) * cell - gap;
            rect.set(left, top, right, bottom);

            boolean done = completed.contains(day);
            cellPaint.setColor(done ? doneColor : idleColor);
            canvas.drawRoundRect(rect, radius, radius, cellPaint);
            if (day.equals(today)) {
                canvas.drawRoundRect(rect, radius, radius, strokePaint);
            }

            textPaint.setColor(done ? doneText : idleText);
            float centerX = (left + right) / 2f;
            float centerY = (top + bottom) / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
            canvas.drawText(String.valueOf(day.getDayOfMonth()), centerX, centerY, textPaint);
        }
    }
}
