package com.example.habitquest;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

public final class PaletteHelper {

    private static final int[][] AVATARS = {
            {R.color.p_cobalt, R.color.p_raspberry},
            {R.color.p_teal, R.color.p_sky},
            {R.color.p_indigo, R.color.p_fuchsia},
            {R.color.p_jade, R.color.p_cobalt},
            {R.color.p_purple, R.color.p_rose},
            {R.color.p_grape, R.color.p_peach}
    };

    private PaletteHelper() {
    }

    public static int count() {
        return AVATARS.length;
    }

    public static GradientDrawable avatarDrawable(Context context, int index) {
        int safeIndex = Math.floorMod(index, AVATARS.length);
        int start = context.getColor(AVATARS[safeIndex][0]);
        int end = context.getColor(AVATARS[safeIndex][1]);
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{start, end});
        drawable.setShape(GradientDrawable.OVAL);
        return drawable;
    }

    public static void applyAvatar(Context context, TextView avatarView, String initial, int index) {
        avatarView.setBackground(avatarDrawable(context, index));
        avatarView.setText(initial);
    }
}
