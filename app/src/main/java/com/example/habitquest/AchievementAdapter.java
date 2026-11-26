package com.example.habitquest;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private final List<Achievement> achievements = new ArrayList<>();

    public void submit(List<Achievement> newAchievements) {
        achievements.clear();
        achievements.addAll(newAchievements);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AchievementViewHolder((MaterialCardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        holder.bind(achievements.get(position));
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView card;
        private final View badge;
        private final ImageView icon;
        private final TextView name;
        private final TextView description;
        private final TextView status;

        AchievementViewHolder(@NonNull MaterialCardView itemView) {
            super(itemView);
            card = itemView;
            badge = itemView.findViewById(R.id.achievementBadge);
            icon = itemView.findViewById(R.id.achievementIcon);
            name = itemView.findViewById(R.id.achievementName);
            description = itemView.findViewById(R.id.achievementDescription);
            status = itemView.findViewById(R.id.achievementStatus);
        }

        void bind(Achievement achievement) {
            int color = ContextCompat.getColor(itemView.getContext(), achievement.getColorRes());
            boolean unlocked = achievement.isUnlocked();

            name.setText(achievement.getName());
            description.setText(achievement.getDescription());
            icon.setImageResource(achievement.getIconRes());
            icon.setImageTintList(ColorStateList.valueOf(unlocked ? color : ContextCompat.getColor(itemView.getContext(), R.color.text_muted)));
            badge.setBackgroundTintList(ColorStateList.valueOf(withAlpha(color, unlocked ? 0x33 : 0x14)));

            if (unlocked) {
                status.setText(itemView.getContext().getString(R.string.unlocked_on,
                        HabitStats.displayDate(achievement.getUnlockDate())));
                status.setTextColor(color);
                card.setStrokeColor(withAlpha(color, 0x66));
                card.setAlpha(1f);
            } else {
                status.setText(itemView.getContext().getString(R.string.locked) + " · " + achievement.getRequirement());
                status.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_muted));
                card.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.stroke_soft));
                card.setAlpha(0.62f);
            }
        }

        private static int withAlpha(int color, int alpha) {
            return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
        }
    }
}
