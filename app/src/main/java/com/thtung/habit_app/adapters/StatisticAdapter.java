package com.thtung.habit_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thtung.habit_app.R;
import com.thtung.habit_app.model.Statistic;

import java.util.List;

public class StatisticAdapter extends RecyclerView.Adapter<StatisticAdapter.StatisticViewHolder> {
    private List<Statistic> statisticList;

    public StatisticAdapter(List<Statistic> statisticList) {
        this.statisticList = statisticList;
    }

    @NonNull
    @Override
    public StatisticViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_statistic, parent, false);
        return new StatisticViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticViewHolder holder, int position) {
        Statistic stat = statisticList.get(position);
        holder.tvHabitId.setText("Thói quen: " + stat.getHabit_id());
        int completionPercent = (int) ((stat.getTotal_completed() * 100) / 30); // Giả sử mục tiêu là 30 lần
        holder.tvCompletionPercent.setText(completionPercent + "%");
    }

    @Override
    public int getItemCount() {
        return statisticList.size();
    }

    public static class StatisticViewHolder extends RecyclerView.ViewHolder {
        TextView tvHabitId, tvCompletionPercent;

        public StatisticViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHabitId = itemView.findViewById(R.id.tv_habit_id);
            tvCompletionPercent = itemView.findViewById(R.id.tv_completion_percent);
        }
    }
}