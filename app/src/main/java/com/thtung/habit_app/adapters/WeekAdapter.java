package com.thtung.habit_app.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thtung.habit_app.R;
import com.thtung.habit_app.databinding.ItemDayBinding;
import com.thtung.habit_app.model.DayModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.DayViewHolder> {
    private List<DayModel> days;
    private int selectedPos = -1;

    public WeekAdapter(List<DayModel> days) {
        this.days = days;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDayBinding binding = ItemDayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DayViewHolder(binding);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, @SuppressLint("RecyclerView") int position) {
        DayModel day = days.get(position);
        holder.binding.tvDayName.setText(day.dayName);
        holder.binding.tvDayNumber.setText(day.dayNumber);

        // So sánh ngày hiện tại
        String today = new SimpleDateFormat("dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

        if (day.dayNumber.equals(today)) {
            holder.binding.getRoot().setBackgroundResource(R.drawable.bg_today);  // Nền ngày hôm nay
        }
        else {
            holder.binding.getRoot().setBackgroundResource(R.drawable.day_bg);  // Nền mặc định
        }

//        holder.binding.getRoot().setOnClickListener(v -> {
//            if (selectedPos != -1) days.get(selectedPos).isSelected = false;
//            day.isSelected = true;
//            selectedPos = position;
//            notifyDataSetChanged();
//        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        ItemDayBinding binding;

        DayViewHolder(ItemDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
