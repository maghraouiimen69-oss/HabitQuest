package com.example.habitquest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class WeeklyChartView extends View {

    private int[] counts = new int[7];
    private String[] labels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final int[] barColors;

    public WeeklyChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        barColors = new int[]{
                ContextCompat.getColor(context, R.color.accent_mint),
                ContextCompat.getColor(context, R.color.cat_fitness),
                ContextCompat.getColor(context, R.color.accent_sky),
                ContextCompat.getColor(context, R.color.accent_primary),
                ContextCompat.getColor(context, R.color.accent_violet),
                ContextCompat.getColor(context, R.color.accent_pink),
                ContextCompat.getColor(context, R.color.accent_peach)
        };
        trackPaint.setColor(ContextCompat.getColor(context, R.color.surface_alt));
        labelPaint.setColor(ContextCompat.getColor(context, R.color.text_muted));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setColor(ContextCompat.getColor(context, R.color.text_secondary));
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setFakeBoldText(true);
    }

    public void setData(int[] newCounts, String[] newLabels) {
        counts = newCounts;
        labels = newLabels;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        float labelSize = 11 * density;
        float valueSize = 11 * density;
        labelPaint.setTextSize(labelSize);
        valuePaint.setTextSize(valueSize);

        float topPadding = valueSize + 8 * density;
        float bottomPadding = labelSize + 10 * density;
        float chartBottom = getHeight() - bottomPadding;
        float chartHeight = chartBottom - topPadding;
        float slot = (float) getWidth() / 7f;
        float barWidth = Math.min(slot * 0.46f, 26 * density);

        int max = 1;
        for (int count : counts) {
            max = Math.max(max, count);
        }

        for (int i = 0; i < 7; i++) {
            float centerX = slot * i + slot / 2f;
            float barHeight = counts[i] == 0
                    ? 6 * density
                    : Math.max(10 * density, chartHeight * 0.88f * counts[i] / max);
            float top = chartBottom - barHeight;

            rect.set(centerX - barWidth / 2f, top, centerX + barWidth / 2f, chartBottom);
            if (counts[i] == 0) {
                barPaint.setColor(trackPaint.getColor());
            } else {
                barPaint.setColor(barColors[i]);
            }
            float radius = Math.min(barWidth / 2f, 8 * density);
            canvas.drawRoundRect(rect, radius, radius, barPaint);

            if (counts[i] > 0) {
                canvas.drawText(String.valueOf(counts[i]), centerX, top - 5 * density, valuePaint);
            }
            canvas.drawText(labels[i], centerX, getHeight() - 4 * density, labelPaint);
        }
    }
}
