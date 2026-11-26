package com.example.habitquest;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    public interface Listener {
        void onHabitClick(Habit habit);

        void onCompleteClick(Habit habit);
    }

    private final List<Habit> habits = new ArrayList<>();
    private final Listener listener;

    public HabitAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Habit> newHabits) {
        habits.clear();
        habits.addAll(newHabits);
        notifyDataSetChanged();
    }

    public Habit getHabit(int position) {
        if (position < 0 || position >= habits.size()) {
            return null;
        }
        return habits.get(position);
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        holder.bind(habits.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView card;
        private final View iconContainer;
        private final ImageView icon;
        private final TextView name;
        private final TextView meta;
        private final TextView xp;
        private final ImageButton completeButton;

        HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.habitCard);
            iconContainer = itemView.findViewById(R.id.habitIconContainer);
            icon = itemView.findViewById(R.id.habitIcon);
            name = itemView.findViewById(R.id.habitName);
            meta = itemView.findViewById(R.id.habitMeta);
            xp = itemView.findViewById(R.id.habitXp);
            completeButton = itemView.findViewById(R.id.habitComplete);
        }

        void bind(Habit habit, Listener listener) {
            name.setText(habit.getName());
            meta.setText(itemView.getContext().getString(R.string.habit_meta, habit.getCategory(), habit.getDifficulty()));
            xp.setText(itemView.getContext().getString(R.string.habit_xp_reward, habit.getXpReward()));
            icon.setImageResource(habit.getCategoryIconRes());

            int categoryColor = ContextCompat.getColor(itemView.getContext(), habit.getCategoryColorRes());
            icon.setImageTintList(ColorStateList.valueOf(categoryColor));
            iconContainer.setBackgroundTintList(ColorStateList.valueOf(withAlpha(categoryColor, 0x30)));
            xp.setTextColor(categoryColor);
            xp.setBackgroundTintList(ColorStateList.valueOf(withAlpha(categoryColor, 0x26)));

            boolean done = habit.isCompletedToday();
            completeButton.setImageResource(done ? R.drawable.ic_check_circle : R.drawable.ic_circle);
            int doneColor = ContextCompat.getColor(itemView.getContext(), R.color.accent_mint);
            int idleColor = ContextCompat.getColor(itemView.getContext(), R.color.text_muted);
            completeButton.setImageTintList(ColorStateList.valueOf(done ? doneColor : idleColor));
            name.setTextColor(ContextCompat.getColor(itemView.getContext(),
                    done ? R.color.text_secondary : R.color.text_primary));

            card.setStrokeColor(ContextCompat.getColor(itemView.getContext(),
                    done ? R.color.success : R.color.stroke_soft));
            card.setStrokeWidth(done ? dp(2) : dp(1));
            card.setAlpha(done ? 0.92f : 1f);

            card.setOnClickListener(v -> listener.onHabitClick(habit));
            completeButton.setOnClickListener(v -> listener.onCompleteClick(habit));
        }

        private int dp(int value) {
            return Math.round(itemView.getResources().getDisplayMetrics().density * value);
        }

        private static int withAlpha(int color, int alpha) {
            return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
        }
    }
}
